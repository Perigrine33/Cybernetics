package com.perigrine3.createcybernetics.event.custom;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.advancement.ModCriteria;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.common.damage.ModDamageTypes;
import com.perigrine3.createcybernetics.effect.ModEffects;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class AdvancementEventHooks {
    private AdvancementEventHooks() {}

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Post event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        ItemStack pickedUp = event.getOriginalStack();
        if (pickedUp.isEmpty()) return;

        if (pickedUp.is(ModTags.Items.SCAVENGED_CYBERWARE)) {
            ModCriteria.FIRST_SCAVENGED_CYBERWARE.get().trigger(player);
        }
        if (pickedUp.is(ModTags.Items.CYBERWARE_ITEM)) {
            ModCriteria.FIRST_CYBERWARE.get().trigger(player);
        }
    }

    @SubscribeEvent
    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        var inst = event.getEffectInstance();
        if (inst == null) return;

        if (inst.getEffect() == ModEffects.CYBERWARE_REJECTION) {
            ModCriteria.CYBERPSYCHOSIS.get().trigger(player);
        }

        if (inst.getEffect() == ModEffects.NEUROPOZYNE) {
            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            if (data == null) return;

            int count = data.incrementNeuropozyneApplyCount();
            player.syncData(ModAttachments.CYBERWARE);

            if (count >= 100) {
                ModCriteria.CHROME_JUNKIE.get().trigger(player);
            }
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (event.getSource().is(ModDamageTypes.SURGERY)) {
            ModCriteria.TOO_MUCH_TOO_FAST.get().trigger(player);
        }
        if (event.getSource().is(ModDamageTypes.CYBERWARE_REJECTION)) {
            ModCriteria.OVER_THE_EDGE.get().trigger(player);
        }
        if (event.getSource().is(ModDamageTypes.DAVIDS_DEMISE)) {
            ModCriteria.DAVID_SPECIAL.get().trigger(player);
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;
        if (ModItems.BRAINUPGRADES_CORTICALSTACK != null) {
            if (data.hasSpecificItem(ModItems.BRAINUPGRADES_CORTICALSTACK.get(), CyberwareSlot.BRAIN)) {
                ModCriteria.CORTICAL_STACK.get().trigger(player);
            }
        }

        if (!(event.getEntity() instanceof ServerPlayer victim)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;
        if (killer == victim) return;
        PlayerCyberwareData killerData = killer.getData(ModAttachments.CYBERWARE);
        PlayerCyberwareData victimData = victim.getData(ModAttachments.CYBERWARE);
        boolean killerFullyOrganic = !killerData.hasAnyTagged(ModTags.Items.CYBERWARE_ITEM, CyberwareSlot.values());
        boolean victimFullBorgActive = isFullBorgSetActive(victimData);

        if (killerFullyOrganic && victimFullBorgActive) {
            ModCriteria.LETS_DANCE.get().trigger(killer);
        }
    }

    @SubscribeEvent
    public static void onSurgery(CyberwareSurgeryEvent event) {
        ServerPlayer player = event.getPlayer();

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        if (data.hasAnyTagged(ModTags.Items.CYBERWARE_ITEM, CyberwareSlot.values())) {
            ModCriteria.FIRST_RIPPERDOC_VISIT.get().trigger(player);
        }
        if (isFullBorgSetActive(player.getData(ModAttachments.CYBERWARE))) {
            ModCriteria.WEAK_FLESH.get().trigger(player);
        }
        if (data.hasSpecificItem(ModItems.WETWARE_WEBSHOOTING_RIGHTARM.get(), CyberwareSlot.RARM) || data.hasSpecificItem(ModItems.WETWARE_WEBSHOOTING_LEFTARM.get(), CyberwareSlot.LARM) ||
                data.hasSpecificItem(ModItems.WETWARE_WEBSHOOTINGINTESTINES.get(), CyberwareSlot.LARM)
                && data.hasSpecificItem(ModItems.SKINUPGRADES_SYNTHETICSETULES.get(), CyberwareSlot.SKIN)) {
            ModCriteria.SPIDER_MAN.get().trigger(player);
        }

        if (!data.hasAnyTagged(ModTags.Items.BRAIN_ITEMS, CyberwareSlot.BRAIN)) {
            ModCriteria.THOUGHTLESS.get().trigger(player);
        }
        if (!data.hasAnyTagged(ModTags.Items.HEART_ITEMS, CyberwareSlot.HEART)) {
            ModCriteria.CHEST_PAINS.get().trigger(player);
        }
        if (!data.hasAnyTagged(ModTags.Items.LIVER_ITEMS, CyberwareSlot.ORGANS)) {
            ModCriteria.LIVER_REMOVED.get().trigger(player);
        }
        if (!data.hasAnyTagged(ModTags.Items.SKIN_ITEMS, CyberwareSlot.SKIN)) {
            ModCriteria.SKIN_REMOVED.get().trigger(player);
        }
        if (!data.hasAnyTagged(ModTags.Items.LUNGS_ITEMS, CyberwareSlot.LUNGS)) {
            ModCriteria.LUNGS_REMOVED.get().trigger(player);
        }
        if (!data.hasAnyTagged(ModTags.Items.BONE_ITEMS, CyberwareSlot.BONE)) {
            ModCriteria.BONELESS.get().trigger(player);
        }
        if (!data.hasAnyTagged(ModTags.Items.MUSCLE_ITEMS, CyberwareSlot.MUSCLE)) {
            ModCriteria.MISSING_MUSCLE.get().trigger(player);
        }
        if (!data.hasAnyTagged(ModTags.Items.LEFTARM_ITEMS, CyberwareSlot.LARM) && !data.hasAnyTagged(ModTags.Items.RIGHTARM_ITEMS, CyberwareSlot.RARM) &&
                !data.hasAnyTagged(ModTags.Items.LEFTLEG_ITEMS, CyberwareSlot.LLEG) && !data.hasAnyTagged(ModTags.Items.RIGHTLEG_ITEMS, CyberwareSlot.RLEG)) {
            ModCriteria.FLESH_WOUND.get().trigger(player);
        }
        if (data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN) &&
                data.isDyed(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN)) {
                int tint = data.dyeColor(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN);

                if (tint == 0xFFF38BAA) {
                    ModCriteria.PRETTY_IN_PINK.get().trigger(player);
                }
        }
    }

    @SubscribeEvent
    public static void onCyberwareStateChange(CyberwareStateChangeEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        PlayerCyberwareData data = event.getData();
        com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile profile = event.getCurrentProfile();
        if (data == null || profile == null) return;

        if (data.hasChipwareShardExact(ModItems.DATA_SHARD_RED.get())) {
            ModCriteria.KUNG_FU.get().trigger(player);
        }

        if (profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.ARM_CANNON)) {
            ModCriteria.UPGRADED.get().trigger(player);
        }

        if (profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.RETRACTABLE_CLAWS)) {
            ModCriteria.SNIKT.get().trigger(player);
        }

        if (profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.CYBERBRAIN)) {
            ModCriteria.COGITO_ERGO_SUM.get().trigger(player);
        }

        if (profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.INTERCHANGEABLE_FACEPLATE)) {
            ModCriteria.BODYSNATCHER.get().trigger(player);
            if (profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.CHROMATOPHORES)) {
                ModCriteria.RIGHT_BEHIND_YOU.get().trigger(player);
            }
        }
    }

    @SubscribeEvent
    public static void onChipwareShardChange(CyberwareChipwareShardChangeEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        PlayerCyberwareData data = event.getData();
        if (data != null && data.hasChipwareShardExact(ModItems.DATA_SHARD_RED.get())) {
            ModCriteria.KUNG_FU.get().trigger(player);
        }
    }

    @SubscribeEvent
    public static void onFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack eaten = event.getItem();
        if (eaten.isEmpty()) return;

        if (eaten.is(ModItems.BONE_MARROW.get())) {
            ModCriteria.BONES_AND_ALL.get().trigger(player);
        }
    }


    // HELPERS
    private static boolean isFullBorgSetActive(PlayerCyberwareData data) {
        if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM) && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM) &&
                data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG) && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG) &&
                data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN) && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE) &&
                data.hasSpecificItem(ModItems.HEARTUPGRADES_CYBERHEART.get(), CyberwareSlot.HEART) && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE) &&
                data.hasSpecificItem(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES) && data.hasMultipleSpecificItem(ModItems.BONEUPGRADES_BONELACING.get(), CyberwareSlot.BONE, 3) &&
                data.hasMultipleSpecificItem(ModItems.SKINUPGRADES_SUBDERMALARMOR.get(), CyberwareSlot.SKIN, 3) &&
                data.hasMultipleSpecificItem(ModItems.ARMUPGRADES_PNEUMATICWRIST.get(), 2, CyberwareSlot.RARM, CyberwareSlot.LARM)) {
            return true;
        }
        if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM) && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM) &&
                data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG) && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG) &&
                data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN) && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE) &&
                data.hasSpecificItem(ModItems.HEARTUPGRADES_CYBERHEART.get(), CyberwareSlot.HEART) && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE) &&
                data.hasSpecificItem(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES) && data.hasSpecificItem(ModItems.BONEUPGRADES_BONELACING.get(), CyberwareSlot.BONE) &&
                data.hasMultipleSpecificItem(ModItems.LUNGSUPGRADES_HYPEROXYGENATION.get(), CyberwareSlot.LUNGS, 3) &&
                data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_OCELOTPAWS.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG) &&
                data.hasSpecificItem(ModItems.SKINUPGRADES_CHROMATOPHORES.get(), CyberwareSlot.SKIN) &&
                data.hasSpecificItem(ModItems.BONEUPGRADES_SANDEVISTAN.get(), CyberwareSlot.BONE)) {
            return true;
        }
        if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM) && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM) &&
                data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG) && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG) &&
                data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN) && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE) &&
                data.hasSpecificItem(ModItems.HEARTUPGRADES_CYBERHEART.get(), CyberwareSlot.HEART) && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE) &&
                data.hasMultipleSpecificItem(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES, 3) && data.hasSpecificItem(ModItems.BONEUPGRADES_BONELACING.get(), CyberwareSlot.BONE) &&
                data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_JUMPBOOST.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG) &&
                data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_ANKLEBRACERS.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG) &&
                data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_OCELOTPAWS.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG) &&
                data.hasSpecificItem(ModItems.SKINUPGRADES_CHROMATOPHORES.get(), CyberwareSlot.SKIN) &&
                data.hasSpecificItem(ModItems.SKINUPGRADES_SYNTHETICSETULES.get(), CyberwareSlot.SKIN)) {
            return true;
        }
        if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM) && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM) &&
                data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG) && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG) &&
                data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN) && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE) &&
                data.hasSpecificItem(ModItems.HEARTUPGRADES_CYBERHEART.get(), CyberwareSlot.HEART) && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE) &&
                data.hasSpecificItem(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES) && data.hasSpecificItem(ModItems.BONEUPGRADES_BONELACING.get(), CyberwareSlot.BONE)  &&
                data.hasSpecificItem(ModItems.BONEUPGRADES_CYBERSKULL.get(), CyberwareSlot.BONE) &&
                data.hasSpecificItem(ModItems.BONEUPGRADES_ELYTRA != null ? ModItems.BONEUPGRADES_ELYTRA.get() : null, CyberwareSlot.BONE) &&
                data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_JUMPBOOST.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG)) {
            return true;
        }
        if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM) && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM) &&
                data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG) && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG) &&
                data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN) && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE) &&
                data.hasSpecificItem(ModItems.HEARTUPGRADES_CYBERHEART.get(), CyberwareSlot.HEART) && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE) &&
                data.hasSpecificItem(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES) && data.hasSpecificItem(ModItems.BONEUPGRADES_BONELACING.get(), CyberwareSlot.BONE) &&
                data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_PROPELLERS.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG) &&
                data.hasSpecificItem(ModItems.EYEUPGRADES_UNDERWATERVISION.get(), CyberwareSlot.EYES) &&
                data.hasSpecificItem(ModItems.LUNGSUPGRADES_OXYGEN.get(), CyberwareSlot.LUNGS) &&
                data.hasSpecificItem(ModItems.WETWARE_WATERBREATHINGLUNGS.get(), CyberwareSlot.LUNGS)) {
            return true;
        }
        if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM) && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM) &&
                data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG) && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG) &&
                data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN) && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE) &&
                data.hasSpecificItem(ModItems.HEARTUPGRADES_CYBERHEART.get(), CyberwareSlot.HEART) && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE) &&
                data.hasSpecificItem(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES) && data.hasSpecificItem(ModItems.BONEUPGRADES_BONELACING.get(), CyberwareSlot.BONE) &&
                data.hasMultipleSpecificItem(ModItems.ARMUPGRADES_PNEUMATICWRIST.get(), 2, CyberwareSlot.RARM, CyberwareSlot.LARM) &&
                data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_ANKLEBRACERS.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG) &&
                data.hasSpecificItem(ModItems.LEGUPGRADES_METALDETECTOR.get(), CyberwareSlot.RLEG, CyberwareSlot.LLEG) &&
                data.hasSpecificItem(ModItems.BRAINUPGRADES_MATRIX.get(), CyberwareSlot.BRAIN) &&
                data.hasSpecificItem(ModItems.ARMUPGRADES_DRILLFIST.get(), CyberwareSlot.RARM, CyberwareSlot.LARM) &&
                data.hasMultipleSpecificItem(ModItems.ARMUPGRADES_REINFORCEDKNUCKLES.get(), 2, CyberwareSlot.RARM, CyberwareSlot.LARM)) {
            return true;
        }
        if (data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTARM.get(), CyberwareSlot.RARM) && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTARM.get(), CyberwareSlot.LARM) &&
                data.hasSpecificItem(ModItems.BASECYBERWARE_RIGHTLEG.get(), CyberwareSlot.RLEG) && data.hasSpecificItem(ModItems.BASECYBERWARE_LEFTLEG.get(), CyberwareSlot.LLEG) &&
                data.hasSpecificItem(ModItems.SKINUPGRADES_METALPLATING.get(), CyberwareSlot.SKIN) && data.hasSpecificItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get(), CyberwareSlot.MUSCLE) &&
                data.hasSpecificItem(ModItems.HEARTUPGRADES_CYBERHEART.get(), CyberwareSlot.HEART) && data.hasSpecificItem(ModItems.BASECYBERWARE_LINEARFRAME.get(), CyberwareSlot.BONE) &&
                data.hasSpecificItem(ModItems.BASECYBERWARE_CYBEREYES.get(), CyberwareSlot.EYES) && data.hasMultipleSpecificItem(ModItems.BONEUPGRADES_BONELACING.get(), CyberwareSlot.BONE, 3) &&
                data.hasMultipleSpecificItem(ModItems.ARMUPGRADES_PNEUMATICWRIST.get(), 2, CyberwareSlot.RARM, CyberwareSlot.LARM) &&
                data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_ANKLEBRACERS.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG) &&
                data.hasMultipleSpecificItem(ModItems.LEGUPGRADES_JUMPBOOST.get(), 2, CyberwareSlot.RLEG, CyberwareSlot.LLEG) &&
                data.hasSpecificItem(ModItems.ARMUPGRADES_ARMCANNON.get(), CyberwareSlot.RARM, CyberwareSlot.LARM) &&
                data.hasSpecificItem(ModItems.EYEUPGRADES_TARGETING.get(), CyberwareSlot.EYES) &&
                data.hasSpecificItem(ModItems.BRAINUPGRADES_MATRIX.get(), CyberwareSlot.BRAIN) &&
                data.hasSpecificItem(ModItems.BONEUPGRADES_SANDEVISTAN.get(), CyberwareSlot.BONE)) {
            return true;
        }
        return false;
    }
}
