package com.perigrine3.createcybernetics.common.macroprogram;

import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.cyberspace.CyberspaceHealingHandler;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.item.macroprogram.MacroprogramItem;
import com.perigrine3.createcybernetics.item.macroprogram.MaterializedStackOverflowItem;
import com.perigrine3.createcybernetics.network.payload.MacroprogramRuntimeSyncPayload;
import com.perigrine3.createcybernetics.util.ModTags;
import com.perigrine3.createcybernetics.world.dimension.ModDimensions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public final class MacroprogramExecutionManager {
    private static final String NBT_ROOT = "CreateCyberneticsMacroprogramRuntime";
    private static final String NBT_ACTIVE_PROGRAMS = "ActivePrograms";
    private static final String NBT_COOLDOWNS = "Cooldowns";

    private static final String NBT_ACTIVE_END = "ActiveEnd";
    private static final String NBT_MATERIALIZATION_TYPE = "MaterializationType";
    private static final String NBT_MATERIALIZED_SLOT = "MaterializedSlot";
    private static final String NBT_SECURE_SHELL_BLOCKS = "SecureShellBlocks";

    private static final int SECURE_SHELL_MAX_BLOCKS = 10;
    private static final int NORMAL_INVENTORY_SLOT_COUNT = 36;
    private static final float RECOVERY_HEAL_AMOUNT = 6.0F;

    private MacroprogramExecutionManager() {}

    public static void execute(ServerPlayer player, int slot) {
        if (!player.level().dimension().equals(ModDimensions.CYBERSPACE_LEVEL)) return;
        if (!player.hasData(ModAttachments.CYBERWARE)) return;
        if (slot < PlayerCyberwareData.CYBERDECK_MACROPROGRAM_SLOT_START) return;
        if (slot >= PlayerCyberwareData.CYBERDECK_MACROPROGRAM_SLOT_START + PlayerCyberwareData.CYBERDECK_MACROPROGRAM_SLOT_COUNT) return;

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        ItemStack stack = data.getCyberdeckStack(slot);
        if (stack.isEmpty()) return;
        if (!stack.is(ModTags.Items.MACROPROGRAMS)) return;
        if (!(stack.getItem() instanceof MacroprogramItem macroprogramItem)) return;

        MacroprogramDefinition definition = macroprogramItem.getDefinition();

        if (isActive(player, definition.id())) {
            sync(player);
            return;
        }

        if (getCooldownRemaining(player, definition.id()) > 0) {
            sync(player);
            return;
        }

        MaterializedProgram materializedProgram = getMaterializedProgram(definition.id());

        if (materializedProgram != null) {
            if (materializedProgram.type() == MaterializationType.HELD) {
                activateHeldMaterialization(player, data, definition, materializedProgram.item());
                return;
            }

            if (materializedProgram.type() == MaterializationType.INVENTORY) {
                activateInventoryMaterialization(player, data, definition, materializedProgram.item());
                return;
            }
        }

        if (definition == MacroprogramRegistry.RECOVERY) {
            activateRecovery(player, data, definition);
            return;
        }

        if (hasNonMaterializedImplementation(definition.id())) {
            activateNonMaterialized(player, data, definition);
        }
    }

    public static void tick(ServerPlayer player) {
        CompoundTag runtime = getRuntime(player);

        cleanupCooldowns(player, runtime);

        if (!player.level().dimension().equals(ModDimensions.CYBERSPACE_LEVEL)) {
            endAllActivePrograms(player, true);
            MacroprogramArmorHandler.clear(player);
            MacroprogramArmorEquipmentHandler.clear(player);
            return;
        }

        CompoundTag activePrograms = getActivePrograms(runtime);

        if (!activePrograms.isEmpty()) {
            long gameTime = player.level().getGameTime();
            List<String> activeIds = List.copyOf(activePrograms.getAllKeys());

            for (String id : activeIds) {
                if (!activePrograms.contains(id, Tag.TAG_COMPOUND)) continue;

                CompoundTag active = activePrograms.getCompound(id);
                long activeEnd = active.getLong(NBT_ACTIVE_END);

                if (activeEnd >= 0L && gameTime >= activeEnd) {
                    endProgram(player, id, true);
                    continue;
                }

                MaterializationType type = getStoredMaterializationType(active);

                if (type != MaterializationType.NONE && !hasMaterialization(player, id, active)) {
                    endProgram(player, id, true);
                }
            }
        }

        MacroprogramArmorHandler.tick(player);
        MacroprogramArmorEquipmentHandler.tick(player);
        VPNShroudHandler.tick(player);

        if ((player.tickCount % 10) == 0) {
            sync(player);
        }
    }

    public static void endProgram(ServerPlayer player, String id, boolean startCooldown) {
        if (id == null || id.isBlank()) return;

        CompoundTag runtime = getRuntime(player);
        CompoundTag activePrograms = getActivePrograms(runtime);

        if (!activePrograms.contains(id, Tag.TAG_COMPOUND)) return;

        CompoundTag active = activePrograms.getCompound(id);
        MaterializationType type = getStoredMaterializationType(active);

        if (type != MaterializationType.NONE) {
            removeMaterialization(player, id);
        }

        if (MacroprogramRegistry.STACK_OVERFLOW.id().equals(id)) {
            MaterializedStackOverflowItem.resetCombo(player);
        }

        MacroprogramDefinition definition = MacroprogramRegistry.get(id);

        if (startCooldown && definition != null && definition.cooldownTicks() > 0) {
            CompoundTag cooldowns = getCooldowns(runtime);
            cooldowns.putLong(id, player.level().getGameTime() + definition.cooldownTicks());
        }

        activePrograms.remove(id);

        MacroprogramArmorHandler.tick(player);
        MacroprogramArmorEquipmentHandler.tick(player);

        player.inventoryMenu.broadcastChanges();
        sync(player);
    }

    public static void endAllActivePrograms(ServerPlayer player, boolean startCooldowns) {
        CompoundTag activePrograms = getActivePrograms(getRuntime(player));

        if (!activePrograms.isEmpty()) {
            List<String> ids = List.copyOf(activePrograms.getAllKeys());

            for (String id : ids) {
                endProgram(player, id, startCooldowns);
            }
        }

        MacroprogramArmorHandler.clear(player);
        MacroprogramArmorEquipmentHandler.clear(player);
    }

    public static boolean isActive(ServerPlayer player, String id) {
        if (id == null || id.isBlank()) return false;

        return getActivePrograms(getRuntime(player)).contains(id, Tag.TAG_COMPOUND);
    }

    public static int getActiveRemaining(ServerPlayer player, String id) {
        CompoundTag active = getActiveProgram(player, id);

        if (active == null) return 0;

        long end = active.getLong(NBT_ACTIVE_END);

        if (end < 0L) {
            return -1;
        }

        long remaining = end - player.level().getGameTime();

        return remaining <= 0L ? 0 : (int) Math.min(Integer.MAX_VALUE, remaining);
    }

    public static int getCooldownRemaining(ServerPlayer player, String id) {
        CompoundTag cooldowns = getCooldowns(getRuntime(player));

        if (!cooldowns.contains(id, Tag.TAG_LONG)) return 0;

        long remaining = cooldowns.getLong(id) - player.level().getGameTime();

        return remaining <= 0L ? 0 : (int) Math.min(Integer.MAX_VALUE, remaining);
    }

    public static void sync(ServerPlayer player) {
        List<MacroprogramRuntimeSyncPayload.Entry> entries = new ArrayList<>();

        for (MacroprogramDefinition definition : MacroprogramRegistry.getAll()) {
            boolean active = isActive(player, definition.id());
            int activeRemaining = active ? getActiveRemaining(player, definition.id()) : 0;
            int cooldownRemaining = getCooldownRemaining(player, definition.id());

            if (!active && cooldownRemaining <= 0) continue;

            int usesRemaining = -1;

            entries.add(new MacroprogramRuntimeSyncPayload.Entry(definition.id(), activeRemaining, cooldownRemaining, usesRemaining));
        }

        PacketDistributor.sendToPlayer(player, new MacroprogramRuntimeSyncPayload(entries));
    }

    private static void activateHeldMaterialization(ServerPlayer player, PlayerCyberwareData data, MacroprogramDefinition definition, Item materializedItem) {
        int selectedSlot = player.getInventory().selected;
        ItemStack held = player.getInventory().getItem(selectedSlot);

        String heldMacroprogramId = getMaterializedProgramId(held);
        int emptySlot = -1;

        if (heldMacroprogramId == null && !held.isEmpty()) {
            emptySlot = findEmptyNormalInventorySlot(player, selectedSlot);

            if (emptySlot < 0) {
                sync(player);
                return;
            }
        }

        if (!tryPayRam(player, data, definition)) return;

        if (heldMacroprogramId != null) {
            endProgram(player, heldMacroprogramId, true);
        } else if (!held.isEmpty()) {
            ItemStack moved = held.copy();
            player.getInventory().setItem(emptySlot, moved);
            player.getInventory().setItem(selectedSlot, ItemStack.EMPTY);
        }

        beginProgram(player, definition, MaterializationType.HELD, selectedSlot);

        player.getInventory().setItem(selectedSlot, new ItemStack(materializedItem));
        player.inventoryMenu.broadcastChanges();

        spawnCastParticles(player);
        sync(player);
    }

    private static void activateInventoryMaterialization(ServerPlayer player, PlayerCyberwareData data, MacroprogramDefinition definition, Item materializedItem) {
        if (findMaterializedProgramSlot(player, definition.id()) >= 0) {
            sync(player);
            return;
        }

        int emptySlot = findEmptyNormalInventorySlot(player, -1);

        if (emptySlot < 0) {
            sync(player);
            return;
        }

        if (!tryPayRam(player, data, definition)) return;

        beginProgram(player, definition, MaterializationType.INVENTORY, emptySlot);

        player.getInventory().setItem(emptySlot, new ItemStack(materializedItem));
        player.inventoryMenu.broadcastChanges();

        spawnCastParticles(player);
        sync(player);
    }

    private static void activateRecovery(ServerPlayer player, PlayerCyberwareData data, MacroprogramDefinition definition) {
        if (player.getHealth() >= player.getMaxHealth()) {
            sync(player);
            return;
        }

        if (!tryPayRam(player, data, definition)) return;

        CyberspaceHealingHandler.heal(player, RECOVERY_HEAL_AMOUNT);

        if (definition.cooldownTicks() > 0) {
            CompoundTag cooldowns = getCooldowns(getRuntime(player));
            cooldowns.putLong(definition.id(), player.level().getGameTime() + definition.cooldownTicks());
        }

        spawnCastParticles(player);
        sync(player);
    }

    private static void activateNonMaterialized(ServerPlayer player, PlayerCyberwareData data, MacroprogramDefinition definition) {
        if (isArmorProgram(definition.id()) && !MacroprogramArmorEquipmentHandler.prepareActivation(player)) {
            sync(player);
            return;
        }

        if (!tryPayRam(player, data, definition)) return;

        beginProgram(player, definition, MaterializationType.NONE, -1);

        MacroprogramArmorHandler.tick(player);
        MacroprogramArmorEquipmentHandler.tick(player);
        VPNShroudHandler.tick(player);

        spawnCastParticles(player);
        sync(player);
    }

    private static boolean tryPayRam(ServerPlayer player, PlayerCyberwareData data, MacroprogramDefinition definition) {
        int ramCost = Math.max(0, definition.ramCost());

        if (ramCost <= 0) {
            return true;
        }

        int oldRam = data.getCyberdeckRam();
        boolean paid = data.tryConsumeCyberdeckRam(player, ramCost);
        int newRam = data.getCyberdeckRam();

        if (newRam != oldRam) {
            ModAttachments.syncCyberware(player);
        }

        if (!paid) {
            sync(player);
            return false;
        }

        return true;
    }

    private static void beginProgram(ServerPlayer player, MacroprogramDefinition definition, MaterializationType type, int materializedSlot) {
        CompoundTag runtime = getRuntime(player);
        CompoundTag activePrograms = getActivePrograms(runtime);
        CompoundTag active = new CompoundTag();

        long activeEnd = definition.activeTicks() > 0 ? player.level().getGameTime() + definition.activeTicks() : -1L;

        active.putLong(NBT_ACTIVE_END, activeEnd);
        active.putString(NBT_MATERIALIZATION_TYPE, type.name());

        if (materializedSlot >= 0) {
            active.putInt(NBT_MATERIALIZED_SLOT, materializedSlot);
        }

        activePrograms.put(definition.id(), active);
    }

    private static CompoundTag getActiveProgram(ServerPlayer player, String id) {
        CompoundTag activePrograms = getActivePrograms(getRuntime(player));

        if (!activePrograms.contains(id, Tag.TAG_COMPOUND)) {
            return null;
        }

        return activePrograms.getCompound(id);
    }

    private static boolean hasMaterialization(ServerPlayer player, String id, CompoundTag active) {
        MaterializedProgram program = getMaterializedProgram(id);

        if (program == null) return false;

        if (active.contains(NBT_MATERIALIZED_SLOT, Tag.TAG_INT)) {
            int slot = active.getInt(NBT_MATERIALIZED_SLOT);

            if (slot >= 0 && slot < player.getInventory().getContainerSize()) {
                ItemStack stack = player.getInventory().getItem(slot);

                if (stack.is(program.item())) {
                    return true;
                }
            }
        }

        return findMaterializedProgramSlot(player, id) >= 0;
    }

    private static void removeMaterialization(ServerPlayer player, String id) {
        MaterializedProgram program = getMaterializedProgram(id);

        if (program == null) return;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.is(program.item())) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private static int findMaterializedProgramSlot(ServerPlayer player, String id) {
        MaterializedProgram program = getMaterializedProgram(id);

        if (program == null) return -1;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(program.item())) {
                return i;
            }
        }

        return -1;
    }

    private static String getMaterializedProgramId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;

        if (stack.is(ModItems.MATERIALIZED_DATA_MINER.get())) {
            return MacroprogramRegistry.DATA_MINER.id();
        }


        if (stack.is(ModItems.MATERIALIZED_BIT_CRUSHER.get())) {
            return MacroprogramRegistry.BIT_CRUSHER.id();
        }

        if (stack.is(ModItems.MATERIALIZED_STACK_OVERFLOW.get())) {
            return MacroprogramRegistry.STACK_OVERFLOW.id();
        }

        if (stack.is(ModItems.MATERIALIZED_ICEBREAKER.get())) {
            return MacroprogramRegistry.ICEBREAKER.id();
        }

        if (stack.is(ModItems.MATERIALIZED_PING.get())) {
            return MacroprogramRegistry.PING.id();
        }

        if (stack.is(ModItems.MATERIALIZED_DDOS.get())) {
            return MacroprogramRegistry.DDOS.id();
        }

        if (stack.is(ModItems.MATERIALIZED_BANHAMMER.get())) {
            return MacroprogramRegistry.BANHAMMER.id();
        }

        if (stack.is(ModItems.MATERIALIZED_FIREWALL.get())) {
            return MacroprogramRegistry.FIREWALL.id();
        }

        return null;
    }

    private static int findEmptyNormalInventorySlot(ServerPlayer player, int excludedSlot) {
        int max = Math.min(NORMAL_INVENTORY_SLOT_COUNT, player.getInventory().getContainerSize());

        for (int i = 0; i < max; i++) {
            if (i == excludedSlot) continue;

            if (player.getInventory().getItem(i).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    private static MaterializedProgram getMaterializedProgram(String id) {
        if (MacroprogramRegistry.DATA_MINER.id().equals(id)) {
            return new MaterializedProgram(ModItems.MATERIALIZED_DATA_MINER.get(), MaterializationType.HELD);
        }

        if (MacroprogramRegistry.BIT_CRUSHER.id().equals(id)) {
            return new MaterializedProgram(ModItems.MATERIALIZED_BIT_CRUSHER.get(), MaterializationType.HELD);
        }

        if (MacroprogramRegistry.STACK_OVERFLOW.id().equals(id)) {
            return new MaterializedProgram(ModItems.MATERIALIZED_STACK_OVERFLOW.get(), MaterializationType.HELD);
        }

        if (MacroprogramRegistry.ICEBREAKER.id().equals(id)) {
            return new MaterializedProgram(ModItems.MATERIALIZED_ICEBREAKER.get(), MaterializationType.HELD);
        }

        if (MacroprogramRegistry.PING.id().equals(id)) {
            return new MaterializedProgram(ModItems.MATERIALIZED_PING.get(), MaterializationType.HELD);
        }

        if (MacroprogramRegistry.DDOS.id().equals(id)) {
            return new MaterializedProgram(ModItems.MATERIALIZED_DDOS.get(), MaterializationType.HELD);
        }

        if (MacroprogramRegistry.BANHAMMER.id().equals(id)) {
            return new MaterializedProgram(ModItems.MATERIALIZED_BANHAMMER.get(), MaterializationType.HELD);
        }

        if (MacroprogramRegistry.FIREWALL.id().equals(id)) {
            return new MaterializedProgram(ModItems.MATERIALIZED_FIREWALL.get(), MaterializationType.HELD);
        }

        return null;
    }

    private static boolean isArmorProgram(String id) {
        return MacroprogramRegistry.BASIC_ARMOR.id().equals(id)
                || MacroprogramRegistry.INTERMEDIATE_ARMOR.id().equals(id)
                || MacroprogramRegistry.ADVANCED_ARMOR.id().equals(id)
                || MacroprogramRegistry.ADMINISTRATOR_ACCESS_ARMOR.id().equals(id);
    }

    private static boolean hasNonMaterializedImplementation(String id) {
        return MacroprogramRegistry.BASIC_ARMOR.id().equals(id)
                || MacroprogramRegistry.INTERMEDIATE_ARMOR.id().equals(id)
                || MacroprogramRegistry.ADVANCED_ARMOR.id().equals(id)
                || MacroprogramRegistry.ADMINISTRATOR_ACCESS_ARMOR.id().equals(id)
                || MacroprogramRegistry.PACKET_SNIFFER.id().equals(id)
                || MacroprogramRegistry.VPN_SHROUD.id().equals(id);
    }

    private static CompoundTag getRuntime(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();

        if (!persistent.contains(NBT_ROOT, Tag.TAG_COMPOUND)) {
            persistent.put(NBT_ROOT, new CompoundTag());
        }

        return persistent.getCompound(NBT_ROOT);
    }

    private static CompoundTag getActivePrograms(CompoundTag runtime) {
        if (!runtime.contains(NBT_ACTIVE_PROGRAMS, Tag.TAG_COMPOUND)) {
            runtime.put(NBT_ACTIVE_PROGRAMS, new CompoundTag());
        }

        return runtime.getCompound(NBT_ACTIVE_PROGRAMS);
    }

    private static CompoundTag getCooldowns(CompoundTag runtime) {
        if (!runtime.contains(NBT_COOLDOWNS, Tag.TAG_COMPOUND)) {
            runtime.put(NBT_COOLDOWNS, new CompoundTag());
        }

        return runtime.getCompound(NBT_COOLDOWNS);
    }

    private static void cleanupCooldowns(ServerPlayer player, CompoundTag runtime) {
        CompoundTag cooldowns = getCooldowns(runtime);
        long gameTime = player.level().getGameTime();

        for (String id : List.copyOf(cooldowns.getAllKeys())) {
            if (cooldowns.getLong(id) <= gameTime) {
                cooldowns.remove(id);
            }
        }
    }

    private static MaterializationType getStoredMaterializationType(CompoundTag active) {
        if (!active.contains(NBT_MATERIALIZATION_TYPE, Tag.TAG_STRING)) {
            return MaterializationType.NONE;
        }

        try {
            return MaterializationType.valueOf(active.getString(NBT_MATERIALIZATION_TYPE));
        } catch (IllegalArgumentException ignored) {
            return MaterializationType.NONE;
        }
    }

    private static void spawnCastParticles(ServerPlayer player) {
        player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.45D, 0.65D, 0.45D, 0.08D);
        player.serverLevel().sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 18, 0.35D, 0.55D, 0.35D, 0.04D);
    }

    private enum MaterializationType {
        NONE,
        HELD,
        INVENTORY
    }

    private record MaterializedProgram(Item item, MaterializationType type) {}
}