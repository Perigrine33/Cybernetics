package com.perigrine3.createcybernetics.common.durability;

import com.perigrine3.createcybernetics.common.scheduling.StaggeredTickScheduler;
import com.perigrine3.createcybernetics.ConfigValues;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.*;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.common.damage.ModDamageTypes;
import com.perigrine3.createcybernetics.item.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = CreateCybernetics.MODID)
public final class CyberwareDurabilityEvents {

    private static final ResourceLocation EMP_EFFECT_ID = ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "emp");

    private CyberwareDurabilityEvents() {}

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        float healthDamage = event.getNewDamage();
        if (healthDamage <= 0.0F) return;

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        int baseDamage = Math.max(1, Mth.ceil(healthDamage * ConfigValues.DURABILITY_DAMAGE_SCALE));

        if (event.getSource().is(ModDamageTypes.BRAIN_DAMAGE)) {
            damageBrainCyberware(player, data, baseDamage);
            return;
        }

        CyberwareDegradationCause cause = resolveCause(event);

        applyIncomingDamage(player, data, cause, baseDamage);
    }

    @SubscribeEvent
    public static void onFoodFinished(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack consumed = event.getItem();
        FoodProperties food = consumed.get(DataComponents.FOOD);

        if (food == null) return;
        if (food.nutrition() <= 0) return;

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);

        for (var entry : data.getAll().entrySet()) {
            InstalledCyberware[] arr = entry.getValue();
            if (arr == null) continue;

            for (InstalledCyberware installed : arr) {
                CyberwareDurabilityManager.repairBiologicalFromFood(player, data, installed, food.nutrition());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!StaggeredTickScheduler.shouldRun(player, 20, StaggeredTickScheduler.TickPhase.DURABILITY_PASSIVE)) {
            return;
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        repairFromRegeneration(player, data);
        applyEmpBatteryDamage(player, data);
        applyPassiveBatteryDamage(player, data);
    }

    private static CyberwareDegradationCause resolveCause(LivingDamageEvent.Post event) {
        if (event.getSource().is(DamageTypeTags.IS_EXPLOSION)) return CyberwareDegradationCause.EXPLOSION;
        if (event.getSource().is(DamageTypeTags.IS_PROJECTILE)) return CyberwareDegradationCause.PROJECTILE;
        if (event.getSource().is(DamageTypeTags.IS_FALL)) return CyberwareDegradationCause.FALL;
        if (event.getSource().is(DamageTypeTags.IS_FIRE)) return CyberwareDegradationCause.FIRE;
        if (event.getSource().is(DamageTypeTags.IS_DROWNING)) return CyberwareDegradationCause.DROWNING;
        if (event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) return CyberwareDegradationCause.MAGIC;

        return event.getSource().getEntity() != null ? CyberwareDegradationCause.MELEE_DAMAGE : CyberwareDegradationCause.GENERAL_DAMAGE;
    }

    private static void damageBrainCyberware(ServerPlayer player, PlayerCyberwareData data, int amount) {
        if (data == null) return;
        if (amount <= 0) return;

        InstalledCyberware[] brain = data.getAll().get(CyberwareSlot.BRAIN);
        if (brain == null) return;

        for (InstalledCyberware installed : brain) {
            if (installed == null) continue;

            ItemStack stack = installed.getItem();
            if (stack == null || stack.isEmpty()) continue;
            if (!CyberwareDurabilityManager.usesDurability(installed)) continue;

            CyberwareDurabilityManager.damage(player, data, installed, CyberwareDegradationCause.BRAIN_DAMAGE, amount);
        }
    }

    private static void applyIncomingDamage(ServerPlayer player, PlayerCyberwareData data, CyberwareDegradationCause cause, int baseDamage) {
        List<InstalledCyberware> limbs = new ArrayList<>();
        List<InstalledCyberware> internals = new ArrayList<>();
        List<InstalledCyberware> exterior = new ArrayList<>();

        collect(data, CyberwareSlot.RARM, limbs);
        collect(data, CyberwareSlot.LARM, limbs);
        collect(data, CyberwareSlot.RLEG, limbs);
        collect(data, CyberwareSlot.LLEG, limbs);

        collect(data, CyberwareSlot.BRAIN, internals);
        collect(data, CyberwareSlot.HEART, internals);
        collect(data, CyberwareSlot.LUNGS, internals);
        collect(data, CyberwareSlot.ORGANS, internals);

        collect(data, CyberwareSlot.SKIN, exterior);
        collect(data, CyberwareSlot.MUSCLE, exterior);
        collect(data, CyberwareSlot.BONE, exterior);
        collect(data, CyberwareSlot.EYES, exterior);

        if (cause == CyberwareDegradationCause.EXPLOSION) {
            if (hasFunctionalBallisticGel(player, data) && player.getRandom().nextInt(3) == 0) return;

            for (InstalledCyberware installed : limbs) {
                CyberwareDurabilityManager.damage(player, data, installed, cause, baseDamage);
            }

            for (InstalledCyberware installed : internals) {
                CyberwareDurabilityManager.damage(player, data, installed, cause, Math.max(1, Mth.ceil(baseDamage * 0.75F)));
            }

            for (InstalledCyberware installed : exterior) {
                CyberwareDurabilityManager.damage(player, data, installed, cause, Math.max(1, Mth.ceil(baseDamage * 1.25F)));
            }

            return;
        }

        damageRandom(player, data, limbs, cause, baseDamage);
        damageRandom(player, data, exterior, cause, Math.max(1, Mth.ceil(baseDamage * 0.5F)));
        damageRandom(player, data, internals, cause, Math.max(1, Mth.ceil(baseDamage * 0.25F)));
    }

    private static void collect(PlayerCyberwareData data, CyberwareSlot slot, List<InstalledCyberware> output) {
        InstalledCyberware[] arr = data.getAll().get(slot);
        if (arr == null) return;

        for (InstalledCyberware installed : arr) {
            if (installed == null) continue;

            ItemStack stack = installed.getItem();
            if (stack == null || stack.isEmpty()) continue;

            output.add(installed);
        }
    }

    private static void damageRandom(ServerPlayer player, PlayerCyberwareData data, List<InstalledCyberware> candidates, CyberwareDegradationCause cause, int amount) {
        if (candidates.isEmpty()) return;
        if (amount <= 0) return;

        InstalledCyberware selected = candidates.get(player.getRandom().nextInt(candidates.size()));

        if (!isDefaultOrgan(selected) && player.getRandom().nextInt(3) == 0) return;

        CyberwareDurabilityManager.damage(player, data, selected, cause, amount);
    }

    private static boolean isDefaultOrgan(InstalledCyberware installed) {
        if (installed == null) return false;

        ItemStack stack = installed.getItem();
        if (stack == null || stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof ICyberwareItem cyberwareItem)) return false;

        return cyberwareItem.getDurabilityCategory(stack, installed.getSlot()) == CyberwareDurabilityCategory.DEFAULT_ORGAN;
    }

    private static boolean hasFunctionalBallisticGel(ServerPlayer player, PlayerCyberwareData data) {
        return data.hasFunctionalSpecificItem(player, ModItems.MUSCLEUPGRADES_BALLISTICGEL.get(), CyberwareSlot.MUSCLE);
    }

    private static void repairFromRegeneration(ServerPlayer player, PlayerCyberwareData data) {
        var effect = player.getEffect(MobEffects.REGENERATION);
        if (effect == null) return;

        int effectLevel = effect.getAmplifier() + 1;

        for (var entry : data.getAll().entrySet()) {
            InstalledCyberware[] arr = entry.getValue();
            if (arr == null) continue;

            for (InstalledCyberware installed : arr) {
                CyberwareDurabilityManager.repairBiologicalFromRegeneration(player, data, installed, effectLevel);
            }
        }
    }

    private static void applyEmpBatteryDamage(ServerPlayer player, PlayerCyberwareData data) {
        var profile = com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(player, data);
        if (profile != null && profile.batterySlots().isEmpty()) return;

        var activeEffects = player.getActiveEffects();
        if (activeEffects.isEmpty()) return;

        net.minecraft.world.effect.MobEffectInstance empEffect = null;
        for (var instance : activeEffects) {
            var keyOpt = instance.getEffect().unwrapKey();
            if (keyOpt.isPresent() && keyOpt.get().location().equals(EMP_EFFECT_ID)) {
                empEffect = instance;
                break;
            }
        }
        if (empEffect == null) return;

        int effectLevel = empEffect.getAmplifier() + 1;
        int amount = ConfigValues.BATTERY_EMP_DAMAGE_PER_SECOND * effectLevel;

        if (amount <= 0) return;

        if (profile != null) {
            for (var ref : profile.batterySlots()) {
                CyberwareDurabilityManager.damage(player, data, ref, CyberwareDegradationCause.EMP, amount);
            }
            return;
        }

        for (var entry : data.getAll().entrySet()) {
            InstalledCyberware[] arr = entry.getValue();
            if (arr == null) continue;

            for (InstalledCyberware installed : arr) {
                if (!CyberwareDurabilityManager.isBattery(installed)) continue;

                CyberwareDurabilityManager.damage(player, data, installed, CyberwareDegradationCause.EMP, amount);
            }
        }
    }

    private static void applyPassiveBatteryDamage(ServerPlayer player, PlayerCyberwareData data) {
        if (ConfigValues.BATTERY_PASSIVE_DAMAGE <= 0) return;
        if (ConfigValues.BATTERY_PASSIVE_DAMAGE_INTERVAL <= 0) return;

        var profile = com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(player, data);
        if (profile != null && profile.batterySlots().isEmpty()) return;

        long gameTime = player.level().getGameTime();

        if (profile != null) {
            for (var ref : profile.batterySlots()) {
                InstalledCyberware installed = ref;
                if (!CyberwareDurabilityManager.usesDurability(installed)) continue;

                long previous = installed.getLastPassiveDurabilityTick();

                if (previous <= 0L) {
                    installed.setLastPassiveDurabilityTick(gameTime);
                    data.setDirty();
                    continue;
                }

                long elapsed = gameTime - previous;
                if (elapsed < ConfigValues.BATTERY_PASSIVE_DAMAGE_INTERVAL) continue;

                long intervals = elapsed / ConfigValues.BATTERY_PASSIVE_DAMAGE_INTERVAL;
                int damage = Mth.clamp((int) intervals * ConfigValues.BATTERY_PASSIVE_DAMAGE, 0, Integer.MAX_VALUE);

                installed.setLastPassiveDurabilityTick(previous + intervals * ConfigValues.BATTERY_PASSIVE_DAMAGE_INTERVAL);
                CyberwareDurabilityManager.damage(player, data, installed, CyberwareDegradationCause.PASSIVE_AGING, damage);
            }
            return;
        }

        for (var entry : data.getAll().entrySet()) {
            InstalledCyberware[] arr = entry.getValue();
            if (arr == null) continue;

            for (InstalledCyberware installed : arr) {
                if (!CyberwareDurabilityManager.isBattery(installed)) continue;
                if (!CyberwareDurabilityManager.usesDurability(installed)) continue;

                long previous = installed.getLastPassiveDurabilityTick();

                if (previous <= 0L) {
                    installed.setLastPassiveDurabilityTick(gameTime);
                    data.setDirty();
                    continue;
                }

                long elapsed = gameTime - previous;
                if (elapsed < ConfigValues.BATTERY_PASSIVE_DAMAGE_INTERVAL) continue;

                long intervals = elapsed / ConfigValues.BATTERY_PASSIVE_DAMAGE_INTERVAL;
                int damage = Mth.clamp((int) intervals * ConfigValues.BATTERY_PASSIVE_DAMAGE, 0, Integer.MAX_VALUE);

                installed.setLastPassiveDurabilityTick(previous + intervals * ConfigValues.BATTERY_PASSIVE_DAMAGE_INTERVAL);
                CyberwareDurabilityManager.damage(player, data, installed, CyberwareDegradationCause.PASSIVE_AGING, damage);
            }
        }
    }
}