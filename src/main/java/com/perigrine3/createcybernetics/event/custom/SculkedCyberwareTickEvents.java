package com.perigrine3.createcybernetics.event.custom;

import com.perigrine3.createcybernetics.common.scheduling.StaggeredTickScheduler;
import com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager;
import com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.effect.ModEffects;
import com.perigrine3.createcybernetics.item.sculked.SculkedIntestinesItem;
import com.perigrine3.createcybernetics.item.sculked.SculkedMuscleItem;
import com.perigrine3.createcybernetics.util.SculkedConversionHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;

@EventBusSubscriber(modid = CreateCybernetics.MODID)
public final class SculkedCyberwareTickEvents {
    private static final int SCULKED_EFFECT_CHECK_INTERVAL_TICKS = 24000;
    private static final float SCULKED_EFFECT_CHANCE = 0.25F;

    private static final int FOOD_CHECK_INTERVAL_TICKS = 20;
    private static final int XP_PER_FOOD_POINT = 250;
    private static final float SATURATION_PER_FOOD_POINT = 0.15F;

    private static final int EFFECT_CHECK_INTERVAL_TICKS = 20;
    private static final int EFFECT_DURATION_TICKS = 60;

    private static final int XP_LEVEL_TIER_1 = 10;
    private static final int XP_LEVEL_TIER_2 = 20;
    private static final int XP_LEVEL_TIER_3 = 30;

    private SculkedCyberwareTickEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide) {
            return;
        }

        if (!player.hasData(ModAttachments.CYBERWARE)) {
            return;
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) {
            return;
        }

        CyberwareLoadoutProfile profile = CyberwareProfileManager.getProfile(player, data);
        if (profile != null
                && !profile.sculkedBody()
                && !profile.hasCapability(CyberwareLoadoutProfile.CapabilityFlag.SCULKED_IMPLANT)) {
            return;
        }

        InstalledSculkedCyberware installed = getInstalledSculkedCyberware(data);
        boolean fullConversion = SculkedConversionHelper.hasFullDefaultSculkConversion(data);

        if (!installed.hasAnySculkedCyberware() && !fullConversion) {
            return;
        }

        tickSculkedEffect(player);

        if (!StaggeredTickScheduler.shouldRun(player, FOOD_CHECK_INTERVAL_TICKS, StaggeredTickScheduler.TickPhase.SCULKED_CHECK)) {
            return;
        }

        boolean standingOnSculk = SculkedConversionHelper.isStandingOnSculk(player);

        if (installed.hasIntestines()) {
            tickExperienceFeeding(player, standingOnSculk);
        }

        if (installed.hasMuscle() || fullConversion) {
            tickPhysicalEffects(player, standingOnSculk, fullConversion);
        }
    }

    public static boolean isSculkedCyberware(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && (stack.getItem() instanceof SculkedIntestinesItem || stack.getItem() instanceof SculkedMuscleItem);
    }

    private static InstalledSculkedCyberware getInstalledSculkedCyberware(PlayerCyberwareData data) {
        boolean hasIntestines = false;
        boolean hasMuscle = false;

        for (Map.Entry<CyberwareSlot, InstalledCyberware[]> entry : data.getAll().entrySet()) {
            InstalledCyberware[] installedCyberware = entry.getValue();

            if (installedCyberware == null) {
                continue;
            }

            for (InstalledCyberware installed : installedCyberware) {
                if (installed == null) {
                    continue;
                }

                ItemStack stack = installed.getItem();

                if (stack == null || stack.isEmpty()) {
                    continue;
                }

                if (stack.getItem() instanceof SculkedIntestinesItem) {
                    hasIntestines = true;
                }

                if (stack.getItem() instanceof SculkedMuscleItem) {
                    hasMuscle = true;
                }

                if (hasIntestines && hasMuscle) {
                    return new InstalledSculkedCyberware(true, true);
                }
            }
        }

        return new InstalledSculkedCyberware(hasIntestines, hasMuscle);
    }

    private static void tickSculkedEffect(Player player) {
        if (player.hasEffect(ModEffects.SCULKED_EFFECT)) {
            return;
        }

        if (!StaggeredTickScheduler.shouldRun(player, SCULKED_EFFECT_CHECK_INTERVAL_TICKS, StaggeredTickScheduler.TickPhase.SCULKED_CHECK)) {
            return;
        }

        if (player.getRandom().nextFloat() < SCULKED_EFFECT_CHANCE) {
            player.addEffect(new MobEffectInstance(ModEffects.SCULKED_EFFECT, Integer.MAX_VALUE, 0, false, false, true));
        }
    }

    private static void tickExperienceFeeding(Player player, boolean standingOnSculk) {
        FoodData foodData = player.getFoodData();

        if (foodData.getFoodLevel() >= 20) {
            return;
        }

        int beforeFood = foodData.getFoodLevel();

        if (standingOnSculk) {
            foodData.eat(1, SATURATION_PER_FOOD_POINT);
        } else {
            if (player.totalExperience < XP_PER_FOOD_POINT) {
                return;
            }

            player.giveExperiencePoints(-XP_PER_FOOD_POINT);
            foodData.eat(1, SATURATION_PER_FOOD_POINT);
        }

        if (beforeFood < 20 && foodData.getFoodLevel() >= 20) {
            applyGoldenAppleEffects(player);
        }
    }

    private static void tickPhysicalEffects(Player player, boolean standingOnSculk, boolean fullConversion) {
        int strengthAmplifier = -1;
        int speedAmplifier = -1;

        if (standingOnSculk) {
            strengthAmplifier = 0;

            if (fullConversion) {
                speedAmplifier = 0;
            }
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EFFECT_DURATION_TICKS, 0, false, false, true));

            if (fullConversion) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EFFECT_DURATION_TICKS, 0, false, false, true));
            }
        }

        if (fullConversion) {
            int xpLevel = player.experienceLevel;

            if (xpLevel >= XP_LEVEL_TIER_1) {
                strengthAmplifier = Math.max(strengthAmplifier, 0);
            }

            if (xpLevel >= XP_LEVEL_TIER_2) {
                speedAmplifier = Math.max(speedAmplifier, 0);
            }

            if (xpLevel >= XP_LEVEL_TIER_3) {
                strengthAmplifier = Math.max(strengthAmplifier, 1);
                speedAmplifier = Math.max(speedAmplifier, 1);
            }
        }

        if (strengthAmplifier >= 0) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, EFFECT_DURATION_TICKS, strengthAmplifier, false, false, true));
        }

        if (speedAmplifier >= 0) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, EFFECT_DURATION_TICKS, speedAmplifier, false, false, true));
        }
    }

    private static void applyGoldenAppleEffects(Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 2, false, false, true));
    }

    private record InstalledSculkedCyberware(boolean hasIntestines, boolean hasMuscle) {
        private boolean hasAnySculkedCyberware() {
            return hasIntestines || hasMuscle;
        }
    }
}