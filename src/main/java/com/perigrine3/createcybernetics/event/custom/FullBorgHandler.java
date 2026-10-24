package com.perigrine3.createcybernetics.event.custom;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile;
import com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.FullBorgModel;
import com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager;
import com.perigrine3.createcybernetics.compat.ModCompats;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.util.CyberwareAttributeHelper;
import com.perigrine3.createcybernetics.util.ModTags;
import com.perigrine3.createcybernetics.util.SculkedConversionHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class FullBorgHandler {
    private FullBorgHandler() {}

    @SubscribeEvent
    public static void onCyberwareStateChange(CyberwareStateChangeEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        CyberwareLoadoutProfile profile = event.getCurrentProfile();
        if (profile == null) return;

        applyModelModifiers(player, profile.fullBorgModel(), profile.sculkedBody());
    }

    @SubscribeEvent
    public static void onMovementStateChange(CyberwarePlayerMovementStateEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        CyberwareLoadoutProfile profile = CyberwareProfileManager.getProfile(player, event.getData());
        if (profile == null) return;

        FullBorgModel model = profile.fullBorgModel();
        if (model == FullBorgModel.ECLIPSE) {
            if (event.getCurrentState().sprinting()) {
                CyberwareAttributeHelper.applyModifier(player, "eclipse_sprintspeed");
            } else {
                CyberwareAttributeHelper.removeModifier(player, "eclipse_sprintspeed");
            }
        } else if (model == FullBorgModel.GENOS) {
            if (event.getCurrentState().sprinting()) {
                CyberwareAttributeHelper.applyModifier(player, "genos_speed");
            } else {
                CyberwareAttributeHelper.removeModifier(player, "genos_speed");
            }
        } else if (model == FullBorgModel.WINGMAN && ModCompats.isInstalled("caelus")) {
            if (event.getCurrentState().crouching()) {
                CyberwareAttributeHelper.removeModifier(player, "wingman_elytraspeed");
            } else {
                CyberwareAttributeHelper.applyModifier(player, "wingman_elytraspeed");
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerXpLevelChange(PlayerXpEvent.LevelChange event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CyberwareLoadoutProfile profile = CyberwareProfileManager.getProfile(player);
        if (profile == null || !profile.sculkedBody()) return;

        applySculkedLevelModifiers(player, player.experienceLevel + event.getLevels());
    }

    public static FullBorgModel detectModel(PlayerCyberwareData data) {
        if (!hasCommonFullBorgChassis(data)) {
            return FullBorgModel.NONE;
        }
        if (isGemini(data)) return FullBorgModel.GEMINI;
        if (isSamson(data)) return FullBorgModel.SAMSON;
        if (isEclipse(data)) return FullBorgModel.ECLIPSE;
        if (isSpyder(data)) return FullBorgModel.SPYDER;
        if (ModCompats.isInstalled("caelus") && isWingman(data)) return FullBorgModel.WINGMAN;
        if (isAquarius(data)) return FullBorgModel.AQUARIUS;
        if (isDymond(data)) return FullBorgModel.DYMOND;
        if (isDragoon(data)) return FullBorgModel.DRAGOON;
        if (isCopernicus(data)) return FullBorgModel.COPERNICUS;
        if (isGenos(data)) return FullBorgModel.GENOS;
        if (isKildare(data)) return FullBorgModel.KILDARE;
        if (ModCompats.isInstalled("irons_spellbooks") && isHexborg(data)) return FullBorgModel.HEXBORG;
        return FullBorgModel.NONE;
    }

    private static boolean hasCommonFullBorgChassis(PlayerCyberwareData data) {
        return data != null
                && data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG)
                && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE);
    }

    private static void applyModelModifiers(Player player, FullBorgModel model, boolean sculkedBody) {
        if (model == FullBorgModel.GEMINI) {
            CyberwareAttributeHelper.applyModifier(player, "gemini_attackstrength");
            CyberwareAttributeHelper.applyModifier(player, "gemini_attackspeed");
            CyberwareAttributeHelper.applyModifier(player, "gemini_miningstrength");
            CyberwareAttributeHelper.applyModifier(player, "gemini_speed");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "gemini_attackstrength");
            CyberwareAttributeHelper.removeModifier(player, "gemini_attackspeed");
            CyberwareAttributeHelper.removeModifier(player, "gemini_miningstrength");
            CyberwareAttributeHelper.removeModifier(player, "gemini_speed");
        }

        if (model == FullBorgModel.SAMSON) {
            CyberwareAttributeHelper.applyModifier(player, "samson_attackstrength");
            CyberwareAttributeHelper.applyModifier(player, "samson_miningstrength");
            CyberwareAttributeHelper.applyModifier(player, "samson_durability");
            CyberwareAttributeHelper.applyModifier(player, "samson_watermove");
            CyberwareAttributeHelper.applyModifier(player, "samson_weight");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "samson_attackstrength");
            CyberwareAttributeHelper.removeModifier(player, "samson_miningstrength");
            CyberwareAttributeHelper.removeModifier(player, "samson_durability");
            CyberwareAttributeHelper.removeModifier(player, "samson_watermove");
            CyberwareAttributeHelper.removeModifier(player, "samson_weight");
        }

        if (model == FullBorgModel.ECLIPSE) {
            CyberwareAttributeHelper.applyModifier(player, "eclipse_crouchspeed");
            CyberwareAttributeHelper.applyModifier(player, "eclipse_speed");
            if (player.isSprinting()) {
                CyberwareAttributeHelper.applyModifier(player, "eclipse_sprintspeed");
            } else {
                CyberwareAttributeHelper.removeModifier(player, "eclipse_sprintspeed");
            }
        } else {
            CyberwareAttributeHelper.removeModifier(player, "eclipse_crouchspeed");
            CyberwareAttributeHelper.removeModifier(player, "eclipse_speed");
            CyberwareAttributeHelper.removeModifier(player, "eclipse_sprintspeed");
        }

        if (model == FullBorgModel.SPYDER) {
            CyberwareAttributeHelper.applyModifier(player, "spyder_crouchspeed");
            CyberwareAttributeHelper.applyModifier(player, "spyder_jumpheight");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "spyder_crouchspeed");
            CyberwareAttributeHelper.removeModifier(player, "spyder_jumpheight");
        }

        if (ModCompats.isInstalled("caelus")) {
            if (model == FullBorgModel.WINGMAN) {
                CyberwareAttributeHelper.applyModifier(player, "wingman_elytraspeed");
                CyberwareAttributeHelper.applyModifier(player, "wingman_elytrahandling");
                if (player.isShiftKeyDown()) {
                    CyberwareAttributeHelper.removeModifier(player, "wingman_elytraspeed");
                }
            } else {
                CyberwareAttributeHelper.removeModifier(player, "wingman_elytraspeed");
                CyberwareAttributeHelper.removeModifier(player, "wingman_elytrahandling");
            }
        }

        if (model == FullBorgModel.AQUARIUS) {
            CyberwareAttributeHelper.applyModifier(player, "aquarius_movement");
            CyberwareAttributeHelper.applyModifier(player, "aquarius_mining");
            CyberwareAttributeHelper.applyModifier(player, "aquarius_swim");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "aquarius_movement");
            CyberwareAttributeHelper.removeModifier(player, "aquarius_mining");
            CyberwareAttributeHelper.removeModifier(player, "aquarius_swim");
        }

        if (model == FullBorgModel.DYMOND) {
            CyberwareAttributeHelper.applyModifier(player, "dymond_miningspeed");
            CyberwareAttributeHelper.applyModifier(player, "dymond_weight");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "dymond_miningspeed");
            CyberwareAttributeHelper.removeModifier(player, "dymond_weight");
        }

        if (model == FullBorgModel.DRAGOON) {
            CyberwareAttributeHelper.applyModifier(player, "dragoon_weight");
            CyberwareAttributeHelper.applyModifier(player, "dragoon_size");
            CyberwareAttributeHelper.applyModifier(player, "dragoon_attack");
            CyberwareAttributeHelper.applyModifier(player, "dragoon_resist");
            CyberwareAttributeHelper.applyModifier(player, "dragoon_knockback");
            CyberwareAttributeHelper.applyModifier(player, "dragoon_jump");
            CyberwareAttributeHelper.applyModifier(player, "dragoon_step");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "dragoon_weight");
            CyberwareAttributeHelper.removeModifier(player, "dragoon_size");
            CyberwareAttributeHelper.removeModifier(player, "dragoon_attack");
            CyberwareAttributeHelper.removeModifier(player, "dragoon_resist");
            CyberwareAttributeHelper.removeModifier(player, "dragoon_knockback");
            CyberwareAttributeHelper.removeModifier(player, "dragoon_jump");
            CyberwareAttributeHelper.removeModifier(player, "dragoon_step");
        }

        if (ModCompats.isInstalled("creatingspace") || ModCompats.isInstalled("northstar")) {
            if (model == FullBorgModel.COPERNICUS) {
                CyberwareAttributeHelper.applyModifier(player, "copernicus_oxygen");
            } else {
                CyberwareAttributeHelper.removeModifier(player, "copernicus_oxygen");
            }
        }

        if (model == FullBorgModel.GENOS) {
            CyberwareAttributeHelper.applyModifier(player, "genos_strength");
            if (player.isSprinting()) {
                CyberwareAttributeHelper.applyModifier(player, "genos_speed");
            } else {
                CyberwareAttributeHelper.removeModifier(player, "genos_speed");
            }
        } else {
            CyberwareAttributeHelper.removeModifier(player, "genos_strength");
            CyberwareAttributeHelper.removeModifier(player, "genos_speed");
        }

        if (model == FullBorgModel.KILDARE) {
            CyberwareAttributeHelper.applyModifier(player, "kildare_strength");
            CyberwareAttributeHelper.applyModifier(player, "kildare_speed");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "kildare_strength");
            CyberwareAttributeHelper.removeModifier(player, "kildare_speed");
        }

        CyberwareAttributeHelper.removeModifier(player, "sculked_strength");
        CyberwareAttributeHelper.removeModifier(player, "sculked_speed");

        if (sculkedBody) {
            if (ModCompats.isInstalled("irons_spellbooks")) {
                CyberwareAttributeHelper.applyModifier(player, "sculked_eldritch_power");
                CyberwareAttributeHelper.applyModifier(player, "sculked_eldritch_resist");
            }
            applySculkedLevelModifiers(player, player.experienceLevel);
        } else {
            CyberwareAttributeHelper.removeModifier(player, "sculked_size1");
            CyberwareAttributeHelper.removeModifier(player, "sculked_size2");
            CyberwareAttributeHelper.removeModifier(player, "sculked_size3");

            if (ModCompats.isInstalled("irons_spellbooks")) {
                CyberwareAttributeHelper.removeModifier(player, "sculked_eldritch_power");
                CyberwareAttributeHelper.removeModifier(player, "sculked_eldritch_resist");
            }
        }
    }

    private static void applySculkedLevelModifiers(Player player, int xpLevel) {
        if (xpLevel >= 10) {
            CyberwareAttributeHelper.applyModifier(player, "sculked_size1");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "sculked_size1");
        }

        if (xpLevel >= 20) {
            CyberwareAttributeHelper.applyModifier(player, "sculked_size2");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "sculked_size2");
        }

        if (xpLevel >= 30) {
            CyberwareAttributeHelper.applyModifier(player, "sculked_size3");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "sculked_size3");
        }
    }

    /* -------------------- Model predicates (reused by other systems) -------------------- */

    public static boolean isGemini(PlayerCyberwareData data) {
        if (data == null) return false;
        return data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG)
                && data.hasSpecificItem(ModItems.SKINUPGRADES_SYNTHSKIN.get(), CyberwareSlot.SKIN)
                && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE)
                && data.hasSpecificItem(ModItems.HEARTUPGRADES_CYBERHEART.get(), CyberwareSlot.HEART)
                && data.hasSpecificItem(ModItems.LUNGSUPGRADES_SYNTHLUNGS.get(), CyberwareSlot.LUNGS)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)
                && data.hasSpecificItem(ModItems.BONEUPGRADES_BONELACING.get(), CyberwareSlot.BONE);
    }

    public static boolean isSamson(PlayerCyberwareData data) {
        if (data == null) return false;
        return data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG)
                && data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)
                && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE)
                && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_BALLISTICGEL.get(), CyberwareSlot.MUSCLE)
                && data.hasSpecificItem(ModItems.HEARTUPGRADES_CYBERHEART.get(), CyberwareSlot.HEART)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)
                && data.hasMultipleSpecificItem(ModItems.BONEUPGRADES_BONELACING.get(), CyberwareSlot.BONE, 3)
                && data.hasMultipleSpecificItem(ModItems.SKINUPGRADES_SUBDERMALARMOR.get(), CyberwareSlot.SKIN, 3)
                && data.hasMultipleSpecificItem(ModItems.ARMUPGRADES_PNEUMATICWRIST.get(), 2, CyberwareSlot.RARM, CyberwareSlot.LARM);
    }

    public static boolean isEclipse(PlayerCyberwareData data) {
        if (data == null) return false;
        return data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG)
                && data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)
                && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE)
                && data.hasSpecificItem(ModItems.HEARTUPGRADES_CYBERHEART.get(), CyberwareSlot.HEART)
                && data.hasSpecificItem(ModItems.LUNGSUPGRADES_SYNTHLUNGS.get(), CyberwareSlot.LUNGS)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE)
                && data.hasSpecificItem(ModItems.EYEUPGRADES_MONOVISION.get(), CyberwareSlot.EYES)
                && data.hasSpecificItem(ModItems.BONEUPGRADES_BONELACING.get(), CyberwareSlot.BONE)
                && data.hasMultipleSpecificItem(ModItems.LUNGSUPGRADES_HYPEROXYGENATION.get(), CyberwareSlot.LUNGS, 3)
                && data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_OCELOTPAWS.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG)
                && data.hasSpecificItem(ModItems.SKINUPGRADES_CHROMATOPHORES.get(), CyberwareSlot.SKIN)
                && data.hasSpecificItem(ModItems.BONEUPGRADES_SANDEVISTAN.get(), CyberwareSlot.BONE);
    }

    public static boolean isSpyder(PlayerCyberwareData data) {
        if (data == null) return false;
        return data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG)
                && data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)
                && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE)
                && data.hasSpecificItem(ModItems.HEARTUPGRADES_CYBERHEART.get(), CyberwareSlot.HEART)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE)
                && data.hasSpecificItem(ModItems.EYEUPGRADES_MONOVISION.get(), CyberwareSlot.EYES)
                && data.hasMultipleSpecificItem(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES, 2)
                && data.hasSpecificItem(ModItems.BONEUPGRADES_BONELACING.get(), CyberwareSlot.BONE)
                && data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_JUMPBOOST.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG)
                && data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_ANKLEBRACERS.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG)
                && data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_OCELOTPAWS.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG)
                && data.hasSpecificItem(ModItems.SKINUPGRADES_CHROMATOPHORES.get(), CyberwareSlot.SKIN)
                && data.hasSpecificItem(ModItems.SKINUPGRADES_SYNTHETICSETULES.get(), CyberwareSlot.SKIN);
    }

    public static boolean isWingman(PlayerCyberwareData data) {
        if (data == null) return false;
        if (ModItems.BONEUPGRADES_ELYTRA != null) {
            return data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM)
                    && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM)
                    && data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG)
                    && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG)
                    && data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)
                    && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE)
                    && data.hasSpecificItem(ModItems.HEARTUPGRADES_CYBERHEART.get(), CyberwareSlot.HEART)
                    && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE)
                    && data.hasSpecificItem(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)
                    && data.hasSpecificItem(ModItems.BONEUPGRADES_BONELACING.get(), CyberwareSlot.BONE)
                    && data.hasSpecificItem(ModItems.BONEUPGRADES_CYBERSKULL.get(), CyberwareSlot.BONE)
                    && data.hasSpecificItem(ModItems.BONEUPGRADES_ELYTRA.get(), CyberwareSlot.BONE)
                    && data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_JUMPBOOST.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG);

        }
        return false;
    }

    public static boolean isAquarius(PlayerCyberwareData data) {
        if (data == null) return false;
        return data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG)
                && data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)
                && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE)
                && data.hasSpecificItem(ModItems.HEARTUPGRADES_CYBERHEART.get(), CyberwareSlot.HEART)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)
                && data.hasSpecificItem(ModItems.BONEUPGRADES_BONELACING.get(), CyberwareSlot.BONE)
                && data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_PROPELLERS.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG)
                && data.hasSpecificItem(ModItems.EYEUPGRADES_UNDERWATERVISION.get(), CyberwareSlot.EYES)
                && data.hasMultipleSpecificItem(ModItems.LUNGSUPGRADES_OXYGEN.get(), 3, CyberwareSlot.LUNGS)
                && data.hasSpecificItem(ModItems.LUNGSUPGRADES_SYNTHLUNGS.get(), CyberwareSlot.LUNGS);
    }

    public static boolean isDymond(PlayerCyberwareData data) {
        if (data == null) return false;
        return data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG)
                && data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)
                && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE)
                && data.hasSpecificItem(ModItems.HEARTUPGRADES_CYBERHEART.get(), CyberwareSlot.HEART)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)
                && data.hasSpecificItem(ModItems.BONEUPGRADES_BONELACING.get(), CyberwareSlot.BONE)
                && data.hasMultipleSpecificItem(ModItems.ARMUPGRADES_PNEUMATICWRIST.get(), 2, CyberwareSlot.RARM, CyberwareSlot.LARM)
                && data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_ANKLEBRACERS.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG)
                && data.hasSpecificItem(ModItems.LEGUPGRADES_METALDETECTOR.get(), CyberwareSlot.RLEG, CyberwareSlot.LLEG)
                && data.hasSpecificItem(ModItems.BRAINUPGRADES_MATRIX.get(), CyberwareSlot.BRAIN)
                && data.hasSpecificItem(ModItems.ARMUPGRADES_DRILLFIST.get(), CyberwareSlot.RARM, CyberwareSlot.LARM)
                && data.hasMultipleSpecificItem(ModItems.ARMUPGRADES_REINFORCEDKNUCKLES.get(), 2, CyberwareSlot.RARM, CyberwareSlot.LARM);
    }

    public static boolean isDragoon(PlayerCyberwareData data) {
        if (data == null) return false;
        return data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG)
                && data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)
                && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE)
                && data.hasSpecificItem(ModItems.HEARTUPGRADES_CYBERHEART.get(), CyberwareSlot.HEART)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)
                && data.hasMultipleSpecificItem(ModItems.BONEUPGRADES_BONELACING.get(), CyberwareSlot.BONE, 3)
                && data.hasMultipleSpecificItem(ModItems.SKINUPGRADES_SUBDERMALARMOR.get(), CyberwareSlot.SKIN, 3)
                && data.hasMultipleSpecificItem(ModItems.ARMUPGRADES_PNEUMATICWRIST.get(), 2, CyberwareSlot.RARM, CyberwareSlot.LARM)
                && data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_ANKLEBRACERS.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG)
                && data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_JUMPBOOST.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG)
                && data.hasSpecificItem(ModItems.ARMUPGRADES_ARMCANNON.get(), CyberwareSlot.RARM, CyberwareSlot.LARM)
                && data.hasSpecificItem(ModItems.EYEUPGRADES_TARGETING.get(), CyberwareSlot.EYES)
                && data.hasSpecificItem(ModItems.BRAINUPGRADES_MATRIX.get(), CyberwareSlot.BRAIN)
                && data.hasSpecificItem(ModItems.BONEUPGRADES_SANDEVISTAN.get(), CyberwareSlot.BONE);
    }

    public static boolean isCopernicus(PlayerCyberwareData data) {
        if (data == null) return false;
        return data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG)
                && data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)
                && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE)
                && data.hasSpecificItem(ModItems.HEARTUPGRADES_CYBERHEART.get(), CyberwareSlot.HEART)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE)
                && data.hasSpecificItem(ModItems.EYEUPGRADES_MONOVISION.get(), CyberwareSlot.EYES)
                && data.hasSpecificItem(ModItems.LUNGSUPGRADES_SYNTHLUNGS.get(), CyberwareSlot.LUNGS)
                && data.hasMultipleSpecificItem(ModItems.LUNGSUPGRADES_OXYGEN.get(), CyberwareSlot.LUNGS, 3)
                && data.hasSpecificItem(ModItems.SKINUPGRADES_SOLARSKIN.get(), CyberwareSlot.SKIN)
                && data.hasSpecificItem(ModItems.SKINUPGRADES_NETHERITEPLATING.get(), CyberwareSlot.SKIN)
                && data.hasSpecificItem(ModItems.EYEUPGRADES_ZOOM.get(), CyberwareSlot.EYES)
                && data.hasSpecificItem(ModItems.ARMUPGRADES_CRAFTHANDS.get(), CyberwareSlot.LARM, CyberwareSlot.RARM);
    }

    public static boolean isGenos(PlayerCyberwareData data) {
        if (data == null) return false;
        return data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG)
                && data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)
                && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE)
                && data.hasSpecificItem(ModItems.HEARTUPGRADES_CYBERHEART.get(), CyberwareSlot.HEART)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)
                && data.hasMultipleSpecificItem(ModItems.BONEUPGRADES_BONELACING.get(), CyberwareSlot.BONE, 2)
                && data.hasMultipleSpecificItem(ModItems.SKINUPGRADES_SUBDERMALARMOR.get(), CyberwareSlot.SKIN, 3)
                && data.hasMultipleSpecificItem(ModItems.ARMUPGRADES_PNEUMATICWRIST.get(), 2, CyberwareSlot.RARM, CyberwareSlot.LARM)
                && data.hasMultipleSpecificItem(ModItems.LUNGSUPGRADES_HYPEROXYGENATION.get(), 3, CyberwareSlot.LUNGS)
                && data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_ANKLEBRACERS.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG)
                && data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_JUMPBOOST.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG)
                && data.hasSpecificItem(ModItems.ARMUPGRADES_ARMCANNON.get(), CyberwareSlot.RARM, CyberwareSlot.LARM)
                && data.hasSpecificItem(ModItems.EYEUPGRADES_TARGETING.get(), CyberwareSlot.EYES)
                && data.hasSpecificItem(ModItems.EYEUPGRADES_HUDJACK.get(), CyberwareSlot.EYES)
                && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_WIREDREFLEXES.get(), CyberwareSlot.MUSCLE)
                && data.hasSpecificItem(ModItems.BRAINUPGRADES_MATRIX.get(), CyberwareSlot.BRAIN);
    }

    public static boolean isKildare(PlayerCyberwareData data) {
        if (data == null) return false;
        return data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG)
                && data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)
                && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE)
                && data.hasSpecificItem(ModItems.HEARTUPGRADES_CYBERHEART.get(), CyberwareSlot.HEART)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE)
                && data.hasSpecificItem(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)
                && data.hasSpecificItem(ModItems.EYEUPGRADES_BIOMONITOR.get(), CyberwareSlot.EYES)
                && data.hasSpecificItem(ModItems.EYEUPGRADES_ZOOM.get(), CyberwareSlot.EYES)
                && data.hasSpecificItem(ModItems.EYEUPGRADES_HUDJACK.get(), CyberwareSlot.EYES)
                && data.hasSpecificItem(ModItems.ARMUPGRADES_CRAFTHANDS.get(), CyberwareSlot.LARM, CyberwareSlot.RARM)
                && data.hasSpecificItem(ModItems.ARMUPGRADES_RIPPERCLAW.get(), CyberwareSlot.LARM, CyberwareSlot.RARM);
    }

    public static boolean isHexborg(PlayerCyberwareData data) {
        if (data == null) return false;
        if (ModItems.HEARTUPGRADES_ANOMALY != null && ModItems.SKINUPGRADES_MANASKIN != null && ModItems.ORGANSUPGRADES_MANABATTERY != null) {
            return data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM)
                    && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM)
                    && data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG)
                    && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG)
                    && data.hasSpecificItem(ModItems.SKINUPGRADES_MANASKIN.get(), CyberwareSlot.SKIN)
                    && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE)
                    && data.hasSpecificItem(ModItems.HEARTUPGRADES_ANOMALY.get(), CyberwareSlot.HEART)
                    && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE)
                    && data.hasSpecificItem(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES)
                    && data.hasMultipleSpecificItem(ModItems.ORGANSUPGRADES_MANABATTERY.get(), 3, CyberwareSlot.ORGANS)
                    && data.hasSpecificItem(ModItems.EYEUPGRADES_HUDJACK.get(), CyberwareSlot.EYES)
                    && data.hasSpecificItem(ModItems.ARMUPGRADES_CRAFTHANDS.get(), CyberwareSlot.LARM, CyberwareSlot.RARM);
        }
        return false;
    }














    public static boolean isSculked(PlayerCyberwareData data) {
        return SculkedConversionHelper.hasFullDefaultSculkConversion(data);
    }








    public static boolean isFullBorg(PlayerCyberwareData data) {
        if (data == null) return false;

        if (isGemini(data)) return true;
        if (isSamson(data)) return true;
        if (isEclipse(data)) return true;
        if (isSpyder(data)) return true;
        if (ModCompats.isInstalled("caelus") && isWingman(data)) return true;
        if (isAquarius(data)) return true;
        if (isDymond(data)) return true;
        if (isDragoon(data)) return true;
        if (isCopernicus(data)) return true;
        if (isGenos(data)) return true;
        if (isKildare(data)) return true;
        if (ModCompats.isInstalled("irons_spellbooks") && isHexborg(data)) return true;

        return false;
    }

    public static boolean isFullBorg(ServerPlayer player) {
        if (player == null) return false;
        if (!player.hasData(ModAttachments.CYBERWARE)) return false;

        return isFullBorg(player.getData(ModAttachments.CYBERWARE));
    }

    public static boolean hasAnyImplantsAtAll(ServerPlayer player) {
        if (player == null) return false;
        if (!player.hasData(ModAttachments.CYBERWARE)) return false;

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data.getAll() == null || data.getAll().isEmpty()) {
            CyberwareAttributeHelper.removeModifier(player, "irons_lightning_weakness");
            return false;
        }

        boolean any = false;

        for (var entry : data.getAll().entrySet()) {
            CyberwareSlot slot = entry.getKey();
            var arr = entry.getValue();
            if (arr == null) continue;

            for (int i = 0; i < arr.length; i++) {
                var installed = arr[i];
                if (installed == null) continue;

                ItemStack st = installed.getItem();
                if (st == null || st.isEmpty()) continue;
                if (st.is(ModTags.Items.CYBERWARE_ITEM)) {
                    any = true;
                    break;
                }
            }

            if (any) break;
        }

        if (any) {
            CyberwareAttributeHelper.applyModifier(player, "irons_lightning_weakness");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "irons_lightning_weakness");
        }

        return any;
    }

}
