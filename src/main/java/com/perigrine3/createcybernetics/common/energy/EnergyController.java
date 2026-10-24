package com.perigrine3.createcybernetics.common.energy;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.common.quickhack.QuickhackRuntimeManager;
import com.perigrine3.createcybernetics.effect.ModEffects;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.event.custom.CyberwarePowerStateChangeEvent;
import com.perigrine3.createcybernetics.item.cyberware.skin.EMPThreadingItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class EnergyController {

    private EnergyController() {}

    private static final Map<Class<?>, Boolean> OVERRIDES_SHOULD_GENERATE = new ConcurrentHashMap<>();

    private static final String NBT_ON_CHARGER_UNTIL = "cc_on_charging_block_until";
    private static final String NBT_CHARGER_ENERGY_BUDGET = "cc_charging_block_energy_budget";

    private static final String NBT_EMP_THREADING_GRACE_UNTIL = "cc_emp_threading_grace_until";
    private static final int EMP_THREADING_GRACE_TICKS = 60;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        if (!player.hasData(ModAttachments.CYBERWARE)) return;

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        boolean empLikeShutdown = hasEmpLikeShutdownEffect(player);

        if (!empLikeShutdown) {
            player.getPersistentData().remove(NBT_EMP_THREADING_GRACE_UNTIL);
        }

        if (empLikeShutdown && !hasEmpProtection(data) && !hasEmpThreadingGrace(player, data)) {
            shutdownAllCyberwareEnergy(player, data);
            return;
        }

        data.clampEnergyToCapacity(player);

        final int chargerEnergyBudget = getActiveChargerEnergyBudget(player);
        final boolean onCharger = chargerEnergyBudget > 0;

        int chargerCharge = computeChargerCharge(player, data, onCharger);
        int tickGenerated = computeGeneratedEnergy(player, data);

        MutableInt genPool = new MutableInt(tickGenerated);
        MutableInt mainsPool = new MutableInt(onCharger ? chargerEnergyBudget : 0);

        payEnergyCosts(player, data, mainsPool, genPool);

        consumeGeneratedSurplus(player, data, genPool);

        depositGeneratedSurplus(player, data, genPool);

        if (onCharger && chargerCharge > 0 && mainsPool.value > 0) {
            int receivedFromCharger = Math.min(chargerCharge, mainsPool.value);

            data.receiveEnergy(player, receivedFromCharger);

            if (mainsPool.value != Integer.MAX_VALUE) {
                mainsPool.value = Math.max(0, mainsPool.value - receivedFromCharger);
            }
        }

        data.clampEnergyToCapacity(player);
    }

    private static void shutdownAllCyberwareEnergy(Player player, PlayerCyberwareData data) {
        data.setEnergyStored(player, 0);

        for (var entry : data.getAll().entrySet()) {
            CyberwareSlot slot = entry.getKey();
            InstalledCyberware[] arr = entry.getValue();
            if (arr == null) continue;

            for (int idx = 0; idx < arr.length; idx++) {
                InstalledCyberware cw = arr[idx];
                if (cw == null) continue;

                ItemStack stack = cw.getItem();
                if (stack == null || stack.isEmpty()) {
                    cw.setPowered(false);
                    continue;
                }

                if (stack.getItem() instanceof ICyberwareItem item) {
                    String paidKey = item.getActivationPaidNbtKey(player, stack, slot);
                    if (paidKey != null && !paidKey.isBlank()) {
                        String persistentKey = buildActivationPersistentKey(paidKey, slot, idx);
                        player.getPersistentData().remove(persistentKey);
                    }
                }

                cw.setPowered(false);
            }
        }

        data.setDirty();
    }

    private static int computeChargerCharge(Player player, PlayerCyberwareData data, boolean onCharger) {
        if (!onCharger) return 0;

        int chargerCharge = 0;

        for (var entry : data.getAll().entrySet()) {
            CyberwareSlot slot = entry.getKey();
            InstalledCyberware[] arr = entry.getValue();
            if (arr == null) continue;

            for (InstalledCyberware cw : arr) {
                if (cw == null) continue;

                ItemStack stack = cw.getItem();
                if (stack == null || stack.isEmpty()) continue;

                if (!(stack.getItem() instanceof ICyberwareItem item)) continue;
                if (QuickhackRuntimeManager.isRebooted(player, cw)) continue;
                if (!item.acceptsChargerEnergy(player, stack, slot)) continue;

                int req = item.getChargerEnergyReceivePerTick(player, stack, slot);
                if (req > 0) {
                    chargerCharge += req;
                }
            }
        }

        return chargerCharge;
    }

    public static int getRequestedChargerEnergy(Player player) {
        if (player == null) return 0;
        if (player.level().isClientSide) return 0;
        if (!player.hasData(ModAttachments.CYBERWARE)) return 0;

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return 0;

        return computeChargerCharge(player, data, true);
    }

    private static int computeGeneratedEnergy(Player player, PlayerCyberwareData data) {
        int tickGenerated = 0;

        for (var entry : data.getAll().entrySet()) {
            CyberwareSlot slot = entry.getKey();
            InstalledCyberware[] arr = entry.getValue();
            if (arr == null) continue;

            for (InstalledCyberware cw : arr) {
                if (cw == null) continue;

                ItemStack stack = cw.getItem();
                if (stack == null || stack.isEmpty()) continue;

                if (!(stack.getItem() instanceof ICyberwareItem item)) continue;
                if (QuickhackRuntimeManager.isRebooted(player, cw)) continue;

                int gen = item.getEnergyGeneratedPerTick(player, stack, slot);
                if (gen <= 0) continue;

                if (shouldGenerate(item, player, stack, slot)) {
                    tickGenerated += gen;
                }
            }
        }

        return Math.max(0, tickGenerated);
    }

    private static void payEnergyCosts(Player player, PlayerCyberwareData data, MutableInt mainsPool, MutableInt genPool) {
        for (var entry : data.getAll().entrySet()) {
            CyberwareSlot slot = entry.getKey();
            InstalledCyberware[] arr = entry.getValue();
            if (arr == null) continue;

            for (int idx = 0; idx < arr.length; idx++) {
                InstalledCyberware cw = arr[idx];
                if (cw == null) continue;

                ItemStack stack = cw.getItem();
                if (stack == null || stack.isEmpty()) {
                    updatePowered(player, data, slot, idx, cw, false);
                    continue;
                }

                if (!(stack.getItem() instanceof ICyberwareItem item)) {
                    updatePowered(player, data, slot, idx, cw, true);
                    continue;
                }

                if (QuickhackRuntimeManager.isRebooted(player, cw)) {
                    updatePowered(player, data, slot, idx, cw, false);
                    clearActivationPaidFlag(player, item, stack, slot, idx);
                    continue;
                }

                if (QuickhackRuntimeManager.isForcedPowerOff(player, slot, idx)) {
                    updatePowered(player, data, slot, idx, cw, false);
                    clearActivationPaidFlag(player, item, stack, slot, idx);
                    continue;
                }

                boolean powered = true;
                int drainMultiplier = QuickhackRuntimeManager.getEnergyConsumptionMultiplier(player, slot, idx);

                int use = item.getEnergyUsedPerTick(player, stack, slot);
                if (use > 0) use *= drainMultiplier;

                if (use > 0) {
                    powered = tryPayEnergy(data, mainsPool, genPool, use);
                }

                if (powered && item.shouldConsumeActivationEnergyThisTick(player, stack, slot)) {
                    int actCost = item.getEnergyActivationCost(player, stack, slot);
                    if (actCost > 0) actCost *= drainMultiplier;

                    if (actCost > 0) {
                        String paidKey = item.getActivationPaidNbtKey(player, stack, slot);

                        if (paidKey == null || paidKey.isBlank()) {
                            powered = tryPayEnergy(data, mainsPool, genPool, actCost);
                        } else {
                            String persistentKey = buildActivationPersistentKey(paidKey, slot, idx);
                            CompoundTag ptag = player.getPersistentData();
                            boolean alreadyPaid = ptag.getBoolean(persistentKey);

                            if (!alreadyPaid) {
                                if (tryPayEnergy(data, mainsPool, genPool, actCost)) {
                                    ptag.putBoolean(persistentKey, true);
                                } else {
                                    powered = false;
                                }
                            }
                        }
                    }
                } else {
                    clearActivationPaidFlag(player, item, stack, slot, idx);
                }

                updatePowered(player, data, slot, idx, cw, powered);
            }
        }
    }

    private static void updatePowered(Player player, PlayerCyberwareData data, CyberwareSlot slot, int index, InstalledCyberware cyberware, boolean powered) {
        if (cyberware.isPowered() == powered) return;

        cyberware.setPowered(powered);
        data.setDirty();
        NeoForge.EVENT_BUS.post(new CyberwarePowerStateChangeEvent(player, data, slot, index, cyberware, powered));
    }

    private static void clearActivationPaidFlag(Player player, ICyberwareItem item, ItemStack stack, CyberwareSlot slot, int index) {
        String paidKey = item.getActivationPaidNbtKey(player, stack, slot);
        if (paidKey == null || paidKey.isBlank()) return;

        String persistentKey = buildActivationPersistentKey(paidKey, slot, index);
        player.getPersistentData().remove(persistentKey);
    }

    private static void consumeGeneratedSurplus(Player player, PlayerCyberwareData data, MutableInt genPool) {
        if (genPool.value <= 0) return;

        int consumed = consumeGeneratedSurplusHooks(player, data, genPool.value);
        if (consumed <= 0) return;

        genPool.value = Math.max(0, genPool.value - consumed);
    }

    private static int consumeGeneratedSurplusHooks(Player player, PlayerCyberwareData data, int availableSurplus) {
        if (player == null || data == null || availableSurplus <= 0) return 0;

        int remaining = availableSurplus;

        for (var entry : data.getAll().entrySet()) {
            CyberwareSlot slot = entry.getKey();
            InstalledCyberware[] arr = entry.getValue();
            if (arr == null) continue;

            for (InstalledCyberware cw : arr) {
                if (remaining <= 0) return availableSurplus;

                if (cw == null) continue;
                if (!cw.isPowered()) continue;

                ItemStack stack = cw.getItem();
                if (stack == null || stack.isEmpty()) continue;

                if (!(stack.getItem() instanceof ICyberwareItem item)) continue;
                if (QuickhackRuntimeManager.isRebooted(player, cw)) continue;

                int consumed = item.consumeGeneratedEnergySurplus(player, stack, slot, remaining);
                if (consumed <= 0) continue;

                consumed = Math.min(consumed, remaining);
                remaining -= consumed;
            }
        }

        return availableSurplus - remaining;
    }

    private static void depositGeneratedSurplus(Player player, PlayerCyberwareData data, MutableInt genPool) {
        int genLeftover = genPool.value;
        if (genLeftover <= 0) return;

        if (canAcceptGeneratedSurplus(player, data)) {
            data.receiveEnergy(player, genLeftover);
            genPool.value = 0;
        }
    }

    private static boolean tryPayEnergy(PlayerCyberwareData data, MutableInt mainsPool, MutableInt genPool, int amount) {
        if (amount <= 0) return true;

        int fromMains = Math.min(mainsPool.value, amount);
        mainsPool.value -= fromMains;
        amount -= fromMains;

        if (amount <= 0) return true;

        int fromGen = Math.min(genPool.value, amount);
        genPool.value -= fromGen;
        amount -= fromGen;

        if (amount <= 0) return true;

        return data.tryConsumeEnergy(amount);
    }

    private static boolean canAcceptGeneratedSurplus(Player player, PlayerCyberwareData data) {
        for (var entry : data.getAll().entrySet()) {
            InstalledCyberware[] arr = entry.getValue();
            if (arr == null) continue;

            for (InstalledCyberware cw : arr) {
                if (cw == null) continue;

                CyberwareSlot slot = entry.getKey();
                ItemStack stack = cw.getItem();
                if (stack == null || stack.isEmpty()) continue;

                if (!(stack.getItem() instanceof ICyberwareItem item)) continue;
                if (QuickhackRuntimeManager.isRebooted(player, cw)) continue;

                if (item.acceptsGeneratedEnergy(player, stack, slot)) return true;
            }
        }

        return false;
    }

    private static boolean shouldGenerate(ICyberwareItem item, Player player, ItemStack stack, CyberwareSlot slot) {
        if (!overridesShouldGenerate(item.getClass())) return true;
        return item.shouldGenerateEnergyThisTick(player, stack, slot);
    }

    private static boolean overridesShouldGenerate(Class<?> cls) {
        return OVERRIDES_SHOULD_GENERATE.computeIfAbsent(cls, c -> {
            try {
                Method m = c.getMethod("shouldGenerateEnergyThisTick", Player.class, ItemStack.class, CyberwareSlot.class);
                return m.getDeclaringClass() != ICyberwareItem.class;
            } catch (NoSuchMethodException e) {
                return false;
            }
        });
    }

    private static String buildActivationPersistentKey(String key, CyberwareSlot slot, int index) {
        return "cc_energy_actpaid_" + key + "_" + slot.name() + "_" + index;
    }

    private static int getActiveChargerEnergyBudget(Player player) {
        long now = player.level().getGameTime();
        long markedUntil = player.getPersistentData().getLong(NBT_ON_CHARGER_UNTIL);

        if (markedUntil < now) {
            player.getPersistentData().remove(NBT_CHARGER_ENERGY_BUDGET);
            return 0;
        }

        return Math.max(0, player.getPersistentData().getInt(NBT_CHARGER_ENERGY_BUDGET));
    }

    public static void markOnChargingBlock(Player player) {
        markOnChargingBlock(player, Integer.MAX_VALUE);
    }

    public static void markOnChargingBlock(Player player, int energyBudget) {
        if (player == null) return;
        if (player.level().isClientSide) return;
        if (energyBudget <= 0) return;

        player.getPersistentData().putLong(NBT_ON_CHARGER_UNTIL, player.level().getGameTime() + 2L);
        player.getPersistentData().putInt(NBT_CHARGER_ENERGY_BUDGET, energyBudget);
    }

    private static boolean hasEmpProtection(PlayerCyberwareData data) {
        return data.hasSpecificItem(ModItems.BONEUPGRADES_CAPACITORFRAME.get(), CyberwareSlot.BONE);
    }

    private static boolean hasEmpThreadingGrace(Player player, PlayerCyberwareData data) {
        if (player == null || data == null) return false;

        if (!hasEnabledEmpThreading(data)) {
            player.getPersistentData().remove(NBT_EMP_THREADING_GRACE_UNTIL);
            return false;
        }

        long now = player.level().getGameTime();
        CompoundTag tag = player.getPersistentData();

        if (!tag.contains(NBT_EMP_THREADING_GRACE_UNTIL)) {
            tag.putLong(NBT_EMP_THREADING_GRACE_UNTIL, now + EMP_THREADING_GRACE_TICKS);
            return true;
        }

        long graceUntil = tag.getLong(NBT_EMP_THREADING_GRACE_UNTIL);
        return now <= graceUntil;
    }

    private static boolean hasEnabledEmpThreading(PlayerCyberwareData data) {
        InstalledCyberware[] arr = data.getAll().get(CyberwareSlot.SKIN);
        if (arr == null) return false;

        for (int i = 0; i < arr.length; i++) {
            InstalledCyberware installed = arr[i];
            if (installed == null) continue;

            ItemStack stack = installed.getItem();
            if (stack == null || stack.isEmpty()) continue;

            if (!(stack.getItem() instanceof EMPThreadingItem)) continue;
            if (!data.isEnabled(CyberwareSlot.SKIN, i)) continue;

            return true;
        }

        return false;
    }

    private static boolean hasEmpLikeShutdownEffect(Player player) {
        return player.hasEffect(ModEffects.EMP);
    }

    private static final class MutableInt {
        int value;

        MutableInt(int value) {
            this.value = value;
        }
    }
}