package com.perigrine3.createcybernetics.common.durability;

import com.perigrine3.createcybernetics.ConfigValues;
import com.perigrine3.createcybernetics.api.CyberwareDegradationCause;
import com.perigrine3.createcybernetics.api.CyberwareDurabilityCategory;
import com.perigrine3.createcybernetics.api.CyberwareDurabilityMode;
import com.perigrine3.createcybernetics.api.CyberwareRepairType;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareData;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.common.quickhack.QuickhackRuntimeManager;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class CyberwareDurabilityManager {

    private CyberwareDurabilityManager() {}

    public static boolean usesDurability(InstalledCyberware installed) {
        if (installed == null) return false;

        ItemStack stack = installed.getItem();
        if (stack == null || stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof ICyberwareItem cyberwareItem)) return false;

        return categoryEnabled(cyberwareItem.getDurabilityCategory(stack, installed.getSlot()));
    }

    public static boolean categoryEnabled(CyberwareDurabilityCategory category) {
        CyberwareDurabilityMode mode = ConfigValues.CYBERWARE_DURABILITY_MODE;

        return switch (mode) {
            case ENABLED -> true;
            case ONLY_IMPLANTS -> category == CyberwareDurabilityCategory.WETWARE || category == CyberwareDurabilityCategory.CYBERNETIC;
            case ONLY_CYBERNETICS -> category == CyberwareDurabilityCategory.CYBERNETIC;
            case DISABLED -> false;
        };
    }

    public static int getEffectiveDurability(InstalledCyberware installed) {
        if (installed == null) return 0;
        if (!usesDurability(installed)) return installed.getMaxDurability();

        return installed.getDurability();
    }

    public static float getEffectiveDurabilityPercent(InstalledCyberware installed) {
        if (installed == null) return 0.0F;
        if (!usesDurability(installed)) return 1.0F;

        return installed.getDurabilityPercent();
    }

    public static boolean isBroken(InstalledCyberware installed) {
        return usesDurability(installed) && installed.isBroken();
    }

    public static boolean isFunctional(LivingEntity entity, InstalledCyberware installed) {
        if (installed == null) return false;

        ItemStack stack = installed.getItem();
        if (stack == null || stack.isEmpty()) return false;

        if (entity != null && QuickhackRuntimeManager.isRebooted(entity, installed)) return false;

        if (!isBroken(installed)) return true;
        if (!(stack.getItem() instanceof ICyberwareItem cyberwareItem)) return false;

        return cyberwareItem.functionsWhenBroken(entity, stack, installed.getSlot());
    }

    public static boolean isBiological(InstalledCyberware installed) {
        if (installed == null) return false;

        ItemStack stack = installed.getItem();
        if (stack == null || stack.isEmpty()) return false;

        return stack.is(ModTags.Items.WETWARE_ITEM);
    }

    public static boolean isBattery(InstalledCyberware installed) {
        if (installed == null) return false;

        ItemStack stack = installed.getItem();
        if (stack == null || stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof ICyberwareItem cyberwareItem)) return false;

        return cyberwareItem.getRepairType(stack, installed.getSlot()) == CyberwareRepairType.BATTERY;
    }

    public static int damage(LivingEntity entity, PlayerCyberwareData data, InstalledCyberware installed, CyberwareDegradationCause cause, int amount) {
        return damageDirect(entity, data, installed, cause, amount);
    }

    public static int damageDirect(LivingEntity entity, ICyberwareData data, InstalledCyberware installed, CyberwareDegradationCause cause, int amount) {
        if (entity == null || data == null || installed == null || cause == null) return 0;
        if (amount <= 0) return 0;
        if (!usesDurability(installed)) return 0;

        ItemStack stack = installed.getItem();
        CyberwareSlot slot = installed.getSlot();

        if (stack == null || stack.isEmpty()) return 0;
        if (!(stack.getItem() instanceof ICyberwareItem cyberwareItem)) return 0;
        if (!cyberwareItem.canDegradeFrom(entity, stack, slot, cause)) return 0;

        float multiplier = Math.max(0.0F, cyberwareItem.getDegradationMultiplier(entity, stack, slot, cause));
        int modifiedAmount = Mth.ceil(amount * multiplier);
        modifiedAmount = cyberwareItem.modifyDegradationAmount(entity, stack, slot, cause, modifiedAmount);

        if (modifiedAmount <= 0) return 0;

        int previousDurability = installed.getDurability();
        int damaged = installed.damageDurability(modifiedAmount);
        int currentDurability = installed.getDurability();

        if (damaged <= 0) return 0;

        cyberwareItem.onDurabilityDamaged(entity, stack, slot, previousDurability, currentDurability, cause);

        if (previousDurability > 0 && currentDurability <= 0) {
            cyberwareItem.onDurabilityBroken(entity, stack, slot, cause);
            if (data instanceof PlayerCyberwareData playerData) {
                playerData.incrementLoadoutVersion();
                if (entity instanceof net.minecraft.world.entity.player.Player player) {
                    com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.synchronizeAndPublish(
                            player,
                            playerData,
                            com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent.ChangeReason.DURABILITY_THRESHOLD_CROSSED
                    );
                }
            } else if (data instanceof com.perigrine3.createcybernetics.common.capabilities.EntityCyberwareData entityData) {
                entityData.incrementLoadoutVersion();
            }
        }

        data.setDirty();
        return damaged;
    }

    public static int repair(LivingEntity entity, PlayerCyberwareData data, InstalledCyberware installed, int amount) {
        if (entity == null || data == null || installed == null) return 0;
        if (amount <= 0) return 0;
        if (!usesDurability(installed)) return 0;

        ItemStack stack = installed.getItem();
        if (stack == null || stack.isEmpty()) return 0;
        if (!(stack.getItem() instanceof ICyberwareItem cyberwareItem)) return 0;

        int previousDurability = installed.getDurability();
        int repaired = installed.repairDurability(amount);
        int currentDurability = installed.getDurability();

        if (repaired <= 0) return 0;

        cyberwareItem.onDurabilityRepaired(entity, stack, installed.getSlot(), previousDurability, currentDurability);
        if (previousDurability <= 0 && currentDurability > 0) {
            data.incrementLoadoutVersion();
            if (entity instanceof net.minecraft.world.entity.player.Player player) {
                com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.synchronizeAndPublish(
                        player,
                        data,
                        com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent.ChangeReason.DURABILITY_THRESHOLD_CROSSED
                );
            }
        }
        data.setDirty();

        return repaired;
    }

    public static int repairBiologicalFromFood(LivingEntity entity, PlayerCyberwareData data, InstalledCyberware installed, int nutrition) {
        if (nutrition <= 0) return 0;
        if (!isBiological(installed)) return 0;
        if (!usesDurability(installed)) return 0;
        if (installed.isAtMaxDurability()) return 0;

        float minimumEfficiency = Mth.clamp((float) ConfigValues.MINIMUM_FOOD_REPAIR_EFFICIENCY, 0.0F, 1.0F);
        float fatigueEfficiency = 1.0F - installed.getNaturalRepairFatigue() / 100.0F;
        float efficiency = Math.max(minimumEfficiency, fatigueEfficiency);
        int baseRepair = Math.max(0, nutrition * ConfigValues.FOOD_DURABILITY_REPAIR_PER_NUTRITION);
        int repairAmount = Mth.floor(baseRepair * efficiency);

        if (baseRepair > 0 && repairAmount <= 0) repairAmount = 1;

        int repaired = repair(entity, data, installed, repairAmount);

        if (repaired > 0) {
            installed.addNaturalRepairFatigue(nutrition * ConfigValues.FOOD_REPAIR_FATIGUE_PER_NUTRITION);
            data.setDirty();
        }

        return repaired;
    }

    public static int repairBiologicalFromRegeneration(LivingEntity entity, PlayerCyberwareData data, InstalledCyberware installed, int effectLevel) {
        if (effectLevel <= 0) return 0;
        if (!isBiological(installed)) return 0;
        if (!usesDurability(installed)) return 0;

        int repaired = repair(entity, data, installed, ConfigValues.REGENERATION_DURABILITY_REPAIR * effectLevel);
        int fatigueRecovery = ConfigValues.REGENERATION_FATIGUE_RECOVERY * effectLevel;

        if (fatigueRecovery > 0 && installed.getNaturalRepairFatigue() > 0) {
            installed.reduceNaturalRepairFatigue(fatigueRecovery);
            data.setDirty();
        }

        return repaired;
    }

    public static int fullyRestoreBiological(LivingEntity entity, PlayerCyberwareData data, InstalledCyberware installed) {
        if (!isBiological(installed)) return 0;
        if (!usesDurability(installed)) return 0;

        int repaired = repair(entity, data, installed, installed.getMaxDurability());
        installed.clearNaturalRepairFatigue();
        data.setDirty();

        return repaired;
    }

    public static int repairByType(LivingEntity entity, PlayerCyberwareData data, InstalledCyberware installed, CyberwareRepairType repairType, int amount, boolean clearBiologicalFatigue) {
        if (installed == null || repairType == null || amount <= 0) return 0;

        ItemStack stack = installed.getItem();
        if (stack == null || stack.isEmpty()) return 0;
        if (!(stack.getItem() instanceof ICyberwareItem cyberwareItem)) return 0;
        if (cyberwareItem.getRepairType(stack, installed.getSlot()) != repairType) return 0;

        int repaired = repair(entity, data, installed, amount);

        if (repaired > 0 && clearBiologicalFatigue && isBiological(installed)) {
            installed.clearNaturalRepairFatigue();
            data.setDirty();
        }

        return repaired;
    }

    public static void recordBatteryEnergyReceived(LivingEntity entity, PlayerCyberwareData data, int amount) {
        if (entity == null || data == null || amount <= 0) return;
        if (ConfigValues.BATTERY_ENERGY_RECEIVED_PER_DAMAGE <= 0) return;

        com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile profile =
                com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(
                        entity instanceof net.minecraft.world.entity.player.Player p ? p : null,
                        data
                );
        if (profile != null) {
            java.util.List<InstalledCyberware> batteries = profile.batterySlots();
            if (batteries.isEmpty()) return;

            long threshold = ConfigValues.BATTERY_ENERGY_RECEIVED_PER_DAMAGE;
            for (InstalledCyberware installed : batteries) {
                if (!usesDurability(installed)) continue;

                installed.addEnergyReceivedSinceDurabilityDamage(amount);
                int durabilityDamage = (int) (installed.getEnergyReceivedSinceDurabilityDamage() / threshold);
                if (durabilityDamage <= 0) continue;

                installed.setEnergyReceivedSinceDurabilityDamage(installed.getEnergyReceivedSinceDurabilityDamage() % threshold);
                damage(entity, data, installed, CyberwareDegradationCause.ENERGY_RECEIVED, durabilityDamage);
            }
            return;
        }

        for (var entry : data.getAll().entrySet()) {
            InstalledCyberware[] arr = entry.getValue();
            if (arr == null) continue;

            for (InstalledCyberware installed : arr) {
                if (!isBattery(installed)) continue;
                if (!usesDurability(installed)) continue;

                installed.addEnergyReceivedSinceDurabilityDamage(amount);

                long threshold = ConfigValues.BATTERY_ENERGY_RECEIVED_PER_DAMAGE;
                int durabilityDamage = (int) (installed.getEnergyReceivedSinceDurabilityDamage() / threshold);

                if (durabilityDamage <= 0) continue;

                installed.setEnergyReceivedSinceDurabilityDamage(installed.getEnergyReceivedSinceDurabilityDamage() % threshold);
                damage(entity, data, installed, CyberwareDegradationCause.ENERGY_RECEIVED, durabilityDamage);
            }
        }
    }

    public static void recordBatteryEnergyExtracted(LivingEntity entity, PlayerCyberwareData data, int amount) {
        if (entity == null || data == null || amount <= 0) return;
        if (ConfigValues.BATTERY_ENERGY_EXTRACTED_PER_DAMAGE <= 0) return;

        com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile profile =
                com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(
                        entity instanceof net.minecraft.world.entity.player.Player p ? p : null,
                        data
                );
        if (profile != null) {
            java.util.List<InstalledCyberware> batteries = profile.batterySlots();
            if (batteries.isEmpty()) return;

            long threshold = ConfigValues.BATTERY_ENERGY_EXTRACTED_PER_DAMAGE;
            for (InstalledCyberware installed : batteries) {
                if (!usesDurability(installed)) continue;

                installed.addEnergyExtractedSinceDurabilityDamage(amount);
                int durabilityDamage = (int) (installed.getEnergyExtractedSinceDurabilityDamage() / threshold);
                if (durabilityDamage <= 0) continue;

                installed.setEnergyExtractedSinceDurabilityDamage(installed.getEnergyExtractedSinceDurabilityDamage() % threshold);
                damage(entity, data, installed, CyberwareDegradationCause.ENERGY_EXTRACTED, durabilityDamage);
            }
            return;
        }

        for (var entry : data.getAll().entrySet()) {
            InstalledCyberware[] arr = entry.getValue();
            if (arr == null) continue;

            for (InstalledCyberware installed : arr) {
                if (!isBattery(installed)) continue;
                if (!usesDurability(installed)) continue;

                installed.addEnergyExtractedSinceDurabilityDamage(amount);

                long threshold = ConfigValues.BATTERY_ENERGY_EXTRACTED_PER_DAMAGE;
                int durabilityDamage = (int) (installed.getEnergyExtractedSinceDurabilityDamage() / threshold);

                if (durabilityDamage <= 0) continue;

                installed.setEnergyExtractedSinceDurabilityDamage(installed.getEnergyExtractedSinceDurabilityDamage() % threshold);
                damage(entity, data, installed, CyberwareDegradationCause.ENERGY_EXTRACTED, durabilityDamage);
            }
        }
    }
}
