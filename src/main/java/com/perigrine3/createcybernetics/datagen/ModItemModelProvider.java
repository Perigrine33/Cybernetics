package com.perigrine3.createcybernetics.datagen;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, CreateCybernetics.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.TITANIUMINGOT.get());
        basicItem(ModItems.TITANIUMSHEET.get());
        basicItem(ModItems.TITANIUMNUGGET.get());
        basicItem(ModItems.RAWTITANIUM.get());
        basicItem(ModItems.CRUSHEDTITANIUM.get());

        basicItem(ModItems.EYEUPGRADEBASE.get());
        basicItem(ModItems.TITANIUM_HAND.get());
        basicItem(ModItems.GRAPHENE_ELASTOMER.get());
        basicItem(ModItems.HOLOIMPRINT_CHIP.get());
        basicItem(ModItems.FRONTAL_LOBE.get());
        basicItem(ModItems.PARIETAL_LOBE.get());
        basicItem(ModItems.TEMPORAL_LOBE.get());
        basicItem(ModItems.OCCIPITAL_LOBE.get());
        basicItem(ModItems.CEREBELLUM.get());
        basicItem(ModItems.XP_CAPSULE.get());
        basicItem(ModItems.FACEPLATE.get());
        basicItem(ModItems.NETHERITE_QPU.get());
        basicItem(ModItems.LINK_CABLE.get());
        basicItem(ModItems.EXOSUIT1.get());

        basicItem(ModItems.TAR.get());
        basicItem(ModItems.UNREFINED_POLYMER.get());
        basicItem(ModItems.PLASTIC_SHEET.get());

        basicItem(ModItems.DATURA_FLOWER.get());
        basicItem(ModItems.DATURA_SEED_POD.get());

        basicItem(ModItems.NEUROPOZYNE_AUTOINJECTOR.get());

        basicItem(ModItems.EMPTY_AUTOINJECTOR.get());
        basicItem(ModItems.INCOMPLETE_EMPTY_AUTOINJECTOR.get());

        basicItem(ModItems.COPPER_UPGRADE_TEMPLATE.get());
        basicItem(ModItems.IRON_UPGRADE_TEMPLATE.get());
        basicItem(ModItems.GOLD_UPGRADE_TEMPLATE.get());

        basicItem(ModItems.MUSIC_DISC_CYBERPSYCHO.get());
        basicItem(ModItems.MUSIC_DISC_NEON_OVERLORDS.get());
        basicItem(ModItems.MUSIC_DISC_NEUROHACK.get());
        basicItem(ModItems.MUSIC_DISC_THE_GRID.get());

        basicItem(ModItems.MUSIC_DISC_DANGER_SNOW.get());
        basicItem(ModItems.MUSIC_DISC_DARK.get());
        basicItem(ModItems.MUSIC_DISC_MY_RETRO_JACKET.get());
        basicItem(ModItems.MUSIC_DISC_GAMEBOY_GIRL.get());
        basicItem(ModItems.MUSIC_DISC_NIGHTMARES_ON_HAUPTSTRASSE.get());
        basicItem(ModItems.MUSIC_DISC_INTERGALACTIC_WALK.get());
        basicItem(ModItems.MUSIC_DISC_SENSATION.get());
        basicItem(ModItems.MUSIC_DISC_SHOTGUN.get());
        basicItem(ModItems.MUSIC_DISC_DESCENT_INTO_MADNESS.get());

        basicItem(ModItems.COOKED_BRAIN.get());
        basicItem(ModItems.COOKED_HEART.get());
        basicItem(ModItems.COOKED_LIVER.get());
        basicItem(ModItems.BONE_MARROW.get());

        withExistingParent(ModItems.RIPPER_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.SMASHER_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.CYBERZOMBIE_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.CYBERSKELETON_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.HOGBOY_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.PUNKLIN_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.PIGSTROM_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));

        //COMPONENT
        {
            basicItem(ModItems.COMPONENT_ACTUATOR.get());
            basicItem(ModItems.COMPONENT_FIBEROPTICS.get());
            basicItem(ModItems.COMPONENT_WIRING.get());
            basicItem(ModItems.COMPONENT_DIODES.get());
            basicItem(ModItems.COMPONENT_PLATING.get());
            basicItem(ModItems.COMPONENT_GRAPHICSCARD.get());
            basicItem(ModItems.COMPONENT_SSD.get());
            basicItem(ModItems.COMPONENT_STORAGE.get());
            basicItem(ModItems.COMPONENT_SYNTHNERVES.get());
            basicItem(ModItems.COMPONENT_MESH.get());
        }

        //BODY PART
        {
            basicItem(ModItems.BODYPART_RIGHTLEG.get());
            basicItem(ModItems.BODYPART_LEFTLEG.get());
            basicItem(ModItems.BODYPART_RIGHTARM.get());
            basicItem(ModItems.BODYPART_LEFTARM.get());
            basicItem(ModItems.BODYPART_SKELETON.get());
            basicItem(ModItems.BODYPART_BRAIN.get());
            basicItem(ModItems.BODYPART_EYEBALLS.get());
            basicItem(ModItems.BODYPART_HEART.get());
            basicItem(ModItems.BODYPART_LUNGS.get());
            basicItem(ModItems.BODYPART_LIVER.get());
            basicItem(ModItems.BODYPART_INTESTINES.get());
            basicItem(ModItems.BODYPART_MUSCLE.get());
            basicItem(ModItems.BODYPART_SKIN.get());
        }

        //ANIMAL BODY PARTS
        {
            basicItem(ModItems.BODYPART_GUARDIANRETINA.get());
            basicItem(ModItems.BODYPART_WARDENESOPHAGUS.get());
            basicItem(ModItems.BODYPART_GYROSCOPICBLADDER.get());
            basicItem(ModItems.BODYPART_SPINNERETTE.get());
            basicItem(ModItems.BODYPART_FIREGLAND.get());
            basicItem(ModItems.BODYPART_GILLS.get());
            basicItem(ModItems.BODYPART_AXOLOTLMARROW.get());
            basicItem(ModItems.BODYPART_DRAGONSCALE.get());

            basicItem(ModItems.BODYPART_SCULKRIGHTLEG.get());
            basicItem(ModItems.BODYPART_SCULKLEFTLEG.get());
            basicItem(ModItems.BODYPART_SCULKRIGHTARM.get());
            basicItem(ModItems.BODYPART_SCULKLEFTARM.get());
            basicItem(ModItems.BODYPART_SCULKBRAIN.get());
            basicItem(ModItems.BODYPART_SCULKLIVER.get());
            basicItem(ModItems.BODYPART_SCULKINTESTINES.get());
            basicItem(ModItems.BODYPART_SCULKMUSCLE.get());
            basicItem(ModItems.BODYPART_SCULKSKIN.get());
        }

        //BASE
        {
            basicItem(ModItems.BASECYBERWARE_LINEARFRAME.get());

            basicItem(ModItems.BASECYBERWARE_LEFTARM_COPPERPLATED.get());
            basicItem(ModItems.BASECYBERWARE_RIGHTARM_COPPERPLATED.get());
            basicItem(ModItems.BASECYBERWARE_LEFTLEG_COPPERPLATED.get());
            basicItem(ModItems.BASECYBERWARE_RIGHTLEG_COPPERPLATED.get());

            basicItem(ModItems.BASECYBERWARE_LEFTARM_IRONPLATED.get());
            basicItem(ModItems.BASECYBERWARE_RIGHTARM_IRONPLATED.get());
            basicItem(ModItems.BASECYBERWARE_LEFTLEG_IRONPLATED.get());
            basicItem(ModItems.BASECYBERWARE_RIGHTLEG_IRONPLATED.get());

            basicItem(ModItems.BASECYBERWARE_LEFTARM_GOLDPLATED.get());
            basicItem(ModItems.BASECYBERWARE_RIGHTARM_GOLDPLATED.get());
            basicItem(ModItems.BASECYBERWARE_LEFTLEG_GOLDPLATED.get());
            basicItem(ModItems.BASECYBERWARE_RIGHTLEG_GOLDPLATED.get());
        }

        //EYES
        {
            basicItem(ModItems.EYEUPGRADES_HUDLENS.get());
            basicItem(ModItems.EYEUPGRADES_HUDJACK.get());
            basicItem(ModItems.EYEUPGRADES_NIGHTVISION.get());
            basicItem(ModItems.EYEUPGRADES_TARGETING.get());
            basicItem(ModItems.EYEUPGRADES_UNDERWATERVISION.get());
            basicItem(ModItems.EYEUPGRADES_ZOOM.get());
            basicItem(ModItems.EYEUPGRADES_TRAJECTORYCALCULATOR.get());
            basicItem(ModItems.EYEUPGRADES_BIOMONITOR.get());
        }

        //ARMS
        {
            basicItem(ModItems.ARMUPGRADES_ARMCANNON.get());
            basicItem(ModItems.ARMUPGRADES_FLYWHEEL.get());
            basicItem(ModItems.ARMUPGRADES_CLAWS.get());
            basicItem(ModItems.ARMUPGRADES_CRAFTHANDS.get());
            basicItem(ModItems.ARMUPGRADES_DRILLFIST.get());
            basicItem(ModItems.ARMUPGRADES_FIRESTARTER.get());
            basicItem(ModItems.ARMUPGRADES_PNEUMATICWRIST.get());
            basicItem(ModItems.ARMUPGRADES_REINFORCEDKNUCKLES.get());
            basicItem(ModItems.ARMUPGRADES_RIPPERCLAW.get());
            basicItem(ModItems.ARMUPGRADES_MANTISBLADE_IRON.get());
            basicItem(ModItems.ARMUPGRADES_MANTISBLADE_COPPER.get());
            basicItem(ModItems.ARMUPGRADES_MANTISBLADE_TITANIUM.get());
            basicItem(ModItems.ARMUPGRADES_MANTISBLADE_GOLD.get());
            basicItem(ModItems.ARMUPGRADES_MANTISBLADE_DIAMOND.get());
            basicItem(ModItems.ARMUPGRADES_MANTISBLADE_NETHERITE.get());
        }

        //LEGS
        {
            basicItem(ModItems.LEGUPGRADES_METALDETECTOR.get());
            basicItem(ModItems.LEGUPGRADES_ANKLEBRACERS.get());
            basicItem(ModItems.LEGUPGRADES_JUMPBOOST.get());
            basicItem(ModItems.LEGUPGRADES_PROPELLERS.get());
            basicItem(ModItems.LEGUPGRADES_SPURS.get());
        }

        //BONE
        {
            basicItem(ModItems.BONEUPGRADES_BONEBATTERY.get());
            basicItem(ModItems.BONEUPGRADES_BONEFLEX.get());
            basicItem(ModItems.BONEUPGRADES_BONELACING.get());
            basicItem(ModItems.BONEUPGRADES_CAPACITORFRAME.get());
            basicItem(ModItems.BONEUPGRADES_PIEZO.get());
            basicItem(ModItems.BONEUPGRADES_SPINALINJECTOR.get());
            basicItem(ModItems.BONEUPGRADES_SANDEVISTAN.get());
            basicItem(ModItems.BONEUPGRADES_CYBERSKULL.get());
        }

        //BRAIN
        {
            basicItem(ModItems.BRAINUPGRADES_CYBERBRAIN.get());
            basicItem(ModItems.BRAINUPGRADES_EYEOFDEFENDER.get());
            basicItem(ModItems.BRAINUPGRADES_ENDERJAMMER.get());
            basicItem(ModItems.BRAINUPGRADES_MATRIX.get());
            basicItem(ModItems.BRAINUPGRADES_NEURALCONTEXTUALIZER.get());
            basicItem(ModItems.BRAINUPGRADES_CYBERDECK.get());
            basicItem(ModItems.BRAINUPGRADES_IDEM.get());
            basicItem(ModItems.BRAINUPGRADES_CHIPWARESLOTS.get());
            basicItem(ModItems.BRAINUPGRADES_NEURALPROCESSOR.get());
            basicItem(ModItems.BRAINUPGRADES_ICEPROTOCOL.get());
            basicItem(ModItems.BRAINUPGRADES_COAXIALPORT.get());
            basicItem(ModItems.BRAINUPGRADES_KERENZIKOV.get());
            basicItem(ModItems.BRAINUPGRADES_BERSERK.get());
            basicItem(ModItems.BRAINUPGRADES_RAMDECK.get());
        }

        //HEART
        {
            basicItem(ModItems.HEARTUPGRADES_CYBERHEART.get());
            basicItem(ModItems.HEARTUPGRADES_COUPLER.get());
            basicItem(ModItems.HEARTUPGRADES_CREEPERHEART.get());
            basicItem(ModItems.HEARTUPGRADES_DEFIBRILLATOR.get());
            basicItem(ModItems.HEARTUPGRADES_STEMCELL.get());
            basicItem(ModItems.HEARTUPGRADES_PLATELETS.get());
        }

        //LUNGS
        {
            basicItem(ModItems.LUNGSUPGRADES_HYPEROXYGENATION.get());
            basicItem(ModItems.LUNGSUPGRADES_OXYGEN.get());
            basicItem(ModItems.LUNGSUPGRADES_SYNTHLUNGS.get());
            basicItem(ModItems.LUNGSUPGRADES_VAMPYRES.get());
        }

        //ORGANS
        {
            basicItem(ModItems.ORGANSUPGRADES_ADRENALINE.get());
            basicItem(ModItems.ORGANSUPGRADES_BATTERY.get());
            basicItem(ModItems.ORGANSUPGRADES_DIAMONDWAFERSTACK.get());
            basicItem(ModItems.ORGANSUPGRADES_DUALISTICCONVERTER.get());
            basicItem(ModItems.ORGANSUPGRADES_LIVERFILTER.get());
            basicItem(ModItems.ORGANSUPGRADES_MAGICCATALYST.get());
            basicItem(ModItems.ORGANSUPGRADES_METABOLIC.get());
            basicItem(ModItems.ORGANSUPGRADES_DENSEBATTERY.get());
            basicItem(ModItems.ORGANSUPGRADES_CREATIVEBATTERY.get());
            basicItem(ModItems.ORGANSUPGRADES_HEATENGINE.get());
            basicItem(ModItems.ORGANSUPGRADES_OREGRINDER.get());
        }

        //SKIN
        {
            basicItem(ModItems.SKINUPGRADES_ARTERIALTURBINE.get());
            basicItem(ModItems.SKINUPGRADES_CHROMATOPHORES.get());
            basicItem(ModItems.SKINUPGRADES_SYNTHSKIN.get());
            basicItem(ModItems.SKINUPGRADES_IMMUNO.get());
            basicItem(ModItems.SKINUPGRADES_FACEPLATE.get());
            basicItem(ModItems.SKINUPGRADES_NETHERITEPLATING.get());
            basicItem(ModItems.SKINUPGRADES_SOLARSKIN.get());
            basicItem(ModItems.SKINUPGRADES_SUBDERMALARMOR.get());
            basicItem(ModItems.SKINUPGRADES_SUBDERMALSPIKES.get());
            basicItem(ModItems.SKINUPGRADES_SYNTHETICSETULES.get());
            basicItem(ModItems.SKINUPGRADES_EMPTHREADING.get());
        }

        //MUSCLE
        {
            basicItem(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get());
            basicItem(ModItems.MUSCLEUPGRADES_REINFORCEDTENDONS.get());
            basicItem(ModItems.MUSCLEUPGRADES_WIREDREFLEXES.get());
            basicItem(ModItems.MUSCLEUPGRADES_BALLISTICGEL.get());
        }

        //WETWARE
        {
            basicItem(ModItems.WETWARE_FIREBREATHINGLUNGS.get());
            basicItem(ModItems.WETWARE_WATERBREATHINGLUNGS.get());
            basicItem(ModItems.WETWARE_GUARDIANEYE.get());
            basicItem(ModItems.WETWARE_POLARBEARFUR.get());
            basicItem(ModItems.WETWARE_RAVAGERTENDONS.get());
            basicItem(ModItems.WETWARE_SCULKLUNGS.get());
            basicItem(ModItems.WETWARE_TACTICALINKSAC.get());
            basicItem(ModItems.WETWARE_AEROSTASISGYROBLADDER.get());
            basicItem(ModItems.WETWARE_GRASSFEDSTOMACH.get());
            basicItem(ModItems.WETWARE_WEBSHOOTINGINTESTINES.get());
            basicItem(ModItems.WETWARE_WEBSHOOTING_RIGHTARM.get());
            basicItem(ModItems.WETWARE_WEBSHOOTING_LEFTARM.get());
            basicItem(ModItems.WETWARE_SPIDEREYES.get());
            basicItem(ModItems.WETWARE_BLASTEMASKELETON.get());
            basicItem(ModItems.WETWARE_DRAGONSKIN.get());
            basicItem(ModItems.WETWARE_WARDENANTLERS.get());
            basicItem(ModItems.WETWARE_SCULKHEART.get());
            basicItem(ModItems.WETWARE_GOOEYMUSCLE.get());
            basicItem(ModItems.WETWARE_ELECTROCYTEMUSCLE.get());
        }

        //SCAVENGED
        {
            basicItem(ModItems.SCAVENGED_RIGHTLEG.get());
            basicItem(ModItems.SCAVENGED_LEFTLEG.get());
            basicItem(ModItems.SCAVENGED_RIGHTARM.get());
            basicItem(ModItems.SCAVENGED_LEFTARM.get());
            basicItem(ModItems.SCAVENGED_CYBEREYES.get());
            basicItem(ModItems.SCAVENGED_LINEARFRAME.get());
            basicItem(ModItems.SCAVENGED_MONOVISION.get());
            basicItem(ModItems.SCAVENGED_HUDLENS.get());
            basicItem(ModItems.SCAVENGED_HUDJACK.get());
            basicItem(ModItems.SCAVENGED_NIGHTVISION.get());
            basicItem(ModItems.SCAVENGED_TARGETING.get());
            basicItem(ModItems.SCAVENGED_UNDERWATERVISION.get());
            basicItem(ModItems.SCAVENGED_ZOOM.get());
            basicItem(ModItems.SCAVENGED_TRAJECTORYCALCULATOR.get());
            basicItem(ModItems.SCAVENGED_BIOMONITOR.get());
            basicItem(ModItems.SCAVENGED_ARMCANNON.get());
            basicItem(ModItems.SCAVENGED_FLYWHEEL.get());
            basicItem(ModItems.SCAVENGED_CLAWS.get());
            basicItem(ModItems.SCAVENGED_CRAFTHANDS.get());
            basicItem(ModItems.SCAVENGED_DRILLFIST.get());
            basicItem(ModItems.SCAVENGED_FIRESTARTER.get());
            basicItem(ModItems.SCAVENGED_PNEUMATICWRIST.get());
            basicItem(ModItems.SCAVENGED_REINFORCEDKNUCKLES.get());
            basicItem(ModItems.SCAVENGED_ARCCANNON.get());
            basicItem(ModItems.ARMUPGRADES_HEAVYKNUCKLES.get());
            basicItem(ModItems.SCAVENGED_MANTISBLADE_TITANIUM.get());
            basicItem(ModItems.SCAVENGED_MANTISBLADE_IRON.get());
            basicItem(ModItems.SCAVENGED_MANTISBLADE_COPPER.get());
            basicItem(ModItems.SCAVENGED_MANTISBLADE_GOLD.get());
            basicItem(ModItems.SCAVENGED_MANTISBLADE_DIAMOND.get());
            basicItem(ModItems.SCAVENGED_MANTISBLADE_NETHERITE.get());
            basicItem(ModItems.SCAVENGED_METALDETECTOR.get());
            basicItem(ModItems.SCAVENGED_ANKLEBRACERS.get());
            basicItem(ModItems.SCAVENGED_JUMPBOOST.get());
            basicItem(ModItems.SCAVENGED_PROPELLERS.get());
            basicItem(ModItems.SCAVENGED_SPURS.get());
            basicItem(ModItems.SCAVENGED_OCELOTPAWS.get());
            basicItem(ModItems.SCAVENGED_BONEBATTERY.get());
            basicItem(ModItems.SCAVENGED_BONEFLEX.get());
            basicItem(ModItems.SCAVENGED_BONELACING.get());
            basicItem(ModItems.SCAVENGED_CAPACITORFRAME.get());
            basicItem(ModItems.SCAVENGED_PIEZO.get());
            basicItem(ModItems.SCAVENGED_SPINALINJECTOR.get());
            basicItem(ModItems.SCAVENGED_SANDEVISTAN.get());
            basicItem(ModItems.SCAVENGED_EYEOFDEFENDER.get());
            basicItem(ModItems.SCAVENGED_ENDERJAMMER.get());
            basicItem(ModItems.SCAVENGED_MATRIX.get());
            basicItem(ModItems.SCAVENGED_NEURALCONTEXTUALIZER.get());
            basicItem(ModItems.SCAVENGED_CYBERDECK.get());
            basicItem(ModItems.SCAVENGED_IDEM.get());
            basicItem(ModItems.SCAVENGED_CHIPWARESLOTS.get());
            basicItem(ModItems.SCAVENGED_ICEPROTOCOL.get());
            basicItem(ModItems.SCAVENGED_COAXIALPORT.get());
            basicItem(ModItems.SCAVENGED_KERENZIKOV.get());
            basicItem(ModItems.SCAVENGED_BERSERK.get());
            basicItem(ModItems.SCAVENGED_RAMDECK.get());
            basicItem(ModItems.SCAVENGED_NEURALPROCESSOR.get());
            basicItem(ModItems.SCAVENGED_CYBERHEART.get());
            basicItem(ModItems.SCAVENGED_COUPLER.get());
            basicItem(ModItems.SCAVENGED_CREEPERHEART.get());
            basicItem(ModItems.SCAVENGED_DEFIBRILLATOR.get());
            basicItem(ModItems.SCAVENGED_STEMCELL.get());
            basicItem(ModItems.SCAVENGED_PLATELETS.get());
            basicItem(ModItems.SCAVENGED_HYPEROXYGENATION.get());
            basicItem(ModItems.SCAVENGED_OXYGEN.get());
            basicItem(ModItems.SCAVENGED_SYNTHLUNGS.get());
            basicItem(ModItems.SCAVENGED_VAMPYRES.get());
            basicItem(ModItems.SCAVENGED_ADRENALINE.get());
            basicItem(ModItems.SCAVENGED_BATTERY.get());
            basicItem(ModItems.SCAVENGED_DIAMONDWAFERSTACK.get());
            basicItem(ModItems.SCAVENGED_DUALISTICCONVERTER.get());
            basicItem(ModItems.SCAVENGED_LIVERFILTER.get());
            basicItem(ModItems.SCAVENGED_METABOLIC.get());
            basicItem(ModItems.SCAVENGED_DENSEBATTERY.get());
            basicItem(ModItems.SCAVENGED_HEATENGINE.get());
            basicItem(ModItems.SCAVENGED_OREGRINDER.get());
            basicItem(ModItems.SCAVENGED_ARTERIALTURBINE.get());
            basicItem(ModItems.SCAVENGED_CHROMATOPHORES.get());
            basicItem(ModItems.SCAVENGED_SYNTHSKIN.get());
            basicItem(ModItems.SCAVENGED_IMMUNO.get());
            basicItem(ModItems.SCAVENGED_FACEPLATE.get());
            basicItem(ModItems.SCAVENGED_NETHERITEPLATING.get());
            basicItem(ModItems.SCAVENGED_SOLARSKIN.get());
            basicItem(ModItems.SCAVENGED_SUBDERMALARMOR.get());
            basicItem(ModItems.SCAVENGED_SUBDERMALSPIKES.get());
            basicItem(ModItems.SCAVENGED_SYNTHETICSETULES.get());
            basicItem(ModItems.SCAVENGED_METALPLATING.get());
            basicItem(ModItems.SCAVENGED_SYNTHMUSCLE.get());
            basicItem(ModItems.SCAVENGED_REINFORCEDTENDONS.get());
            basicItem(ModItems.SCAVENGED_WIREDREFLEXES.get());
            basicItem(ModItems.SCAVENGED_BALLISTICGEL.get());
        }

        // DATA SHARDS
        {
            basicItem(ModItems.DATA_SHARD_RED.get());
            basicItem(ModItems.DATA_SHARD_ORANGE.get());
            basicItem(ModItems.DATA_SHARD_YELLOW.get());
            basicItem(ModItems.DATA_SHARD_GREEN.get());
            basicItem(ModItems.DATA_SHARD_CYAN.get());
            basicItem(ModItems.DATA_SHARD_BLUE.get());
            basicItem(ModItems.DATA_SHARD_PURPLE.get());
            basicItem(ModItems.DATA_SHARD_PINK.get());
            basicItem(ModItems.DATA_SHARD_BROWN.get());
            basicItem(ModItems.DATA_SHARD_GRAY.get());
            basicItem(ModItems.DATA_SHARD_BLACK.get());
            basicItem(ModItems.DATA_SHARD_BIOCHIP.get());

            basicItem(ModItems.DATA_SHARD_MICROPROGRAMS_CREATIVE.get());
        }

// MICROPROGRAM TARGETS
        {
            microprogramItem(ModItems.MICROPROGRAM_SYSTEM_ALL.get(), "microprogram_target");
            microprogramItem(ModItems.MICROPROGRAM_PROCESSOR.get(), "microprogram_target");
            microprogramItem(ModItems.MICROPROGRAM_CYBEROPTICS.get(), "microprogram_target");
            microprogramItem(ModItems.MICROPROGRAM_AUDITORY.get(), "microprogram_target");
            microprogramItem(ModItems.MICROPROGRAM_MOTOR_CONTROL.get(), "microprogram_target");
            microprogramItem(ModItems.MICROPROGRAM_ARMS.get(), "microprogram_target");
            microprogramItem(ModItems.MICROPROGRAM_LEGS.get(), "microprogram_target");
            microprogramItem(ModItems.MICROPROGRAM_INTERNAL_ORGANS.get(), "microprogram_target");
            microprogramItem(ModItems.MICROPROGRAM_SKELETON.get(), "microprogram_target");
            microprogramItem(ModItems.MICROPROGRAM_DERMAL.get(), "microprogram_target");
            microprogramItem(ModItems.MICROPROGRAM_POWER_SYSTEM.get(), "microprogram_target");
            microprogramItem(ModItems.MICROPROGRAM_CYBERDECK.get(), "microprogram_target");
            microprogramItem(ModItems.MICROPROGRAM_NEURAL_SYSTEM.get(), "microprogram_target");
            microprogramItem(ModItems.MICROPROGRAM_ACTIVE_CYBERWARE.get(), "microprogram_target");
            microprogramItem(ModItems.MICROPROGRAM_HIGHEST_DRAW.get(), "microprogram_target");
            microprogramItem(ModItems.MICROPROGRAM_RANDOM_IMPLANT.get(), "microprogram_target");
        }

// MICROPROGRAM FUNCTIONS
        {
            microprogramItem(ModItems.MICROPROGRAM_REBOOT.get(), "microprogram_function");
            microprogramItem(ModItems.MICROPROGRAM_OVERCLOCK.get(), "microprogram_function");
            microprogramItem(ModItems.MICROPROGRAM_DRAIN.get(), "microprogram_function");
            microprogramItem(ModItems.MICROPROGRAM_SPIKE.get(), "microprogram_function");
            microprogramItem(ModItems.MICROPROGRAM_BURNOUT.get(), "microprogram_function");
            microprogramItem(ModItems.MICROPROGRAM_TOGGLE_OFF.get(), "microprogram_function");
            microprogramItem(ModItems.MICROPROGRAM_TOGGLE_ON.get(), "microprogram_function");
            microprogramItem(ModItems.MICROPROGRAM_MALFUNCTION.get(), "microprogram_function");
            microprogramItem(ModItems.MICROPROGRAM_LOOP.get(), "microprogram_function");
            microprogramItem(ModItems.MICROPROGRAM_PING.get(), "microprogram_function");
            microprogramItem(ModItems.MICROPROGRAM_TRACE.get(), "microprogram_function");
            microprogramItem(ModItems.MICROPROGRAM_CORRUPT.get(), "microprogram_function");
            microprogramItem(ModItems.MICROPROGRAM_PURGE.get(), "microprogram_function");
            microprogramItem(ModItems.MICROPROGRAM_DESYNC.get(), "microprogram_function");
        }

// MICROPROGRAM MODIFIERS
        {
            microprogramItem(ModItems.MICROPROGRAM_DURATION_PLUS.get(), "microprogram_modifier");
            microprogramItem(ModItems.MICROPROGRAM_DURATION_PLUS_PLUS.get(), "microprogram_modifier");
            microprogramItem(ModItems.MICROPROGRAM_INTENSITY_PLUS.get(), "microprogram_modifier");
            microprogramItem(ModItems.MICROPROGRAM_EFFICIENCY.get(), "microprogram_modifier");
            microprogramItem(ModItems.MICROPROGRAM_DELAY.get(), "microprogram_modifier");
            microprogramItem(ModItems.MICROPROGRAM_REPEAT.get(), "microprogram_modifier");
            microprogramItem(ModItems.MICROPROGRAM_CASCADE.get(), "microprogram_modifier");
            microprogramItem(ModItems.MICROPROGRAM_SILENT.get(), "microprogram_modifier");
            microprogramItem(ModItems.MICROPROGRAM_PERSISTENT.get(), "microprogram_modifier");
            microprogramItem(ModItems.MICROPROGRAM_RANDOMIZE.get(), "microprogram_modifier");
            microprogramItem(ModItems.MICROPROGRAM_CONTAGION.get(), "microprogram_modifier");
        }

// MACROPROGRAMS
        {
            basicItem(ModItems.MACROPROGRAM_DATA_MINER.get());
            basicItem(ModItems.MACROPROGRAM_BIT_CRUSHER.get());
            basicItem(ModItems.MACROPROGRAM_STACK_OVERFLOW.get());
            basicItem(ModItems.MACROPROGRAM_ICEBREAKER.get());
            basicItem(ModItems.MACROPROGRAM_PING.get());
            basicItem(ModItems.MACROPROGRAM_DDOS.get());
            basicItem(ModItems.MACROPROGRAM_BANHAMMER.get());
            basicItem(ModItems.MACROPROGRAM_FIREWALL.get());
            basicItem(ModItems.MACROPROGRAM_RECOVERY.get());
            basicItem(ModItems.MACROPROGRAM_BASIC_ARMOR.get());
            basicItem(ModItems.MACROPROGRAM_INTERMEDIATE_ARMOR.get());
            basicItem(ModItems.MACROPROGRAM_ADVANCED_ARMOR.get());
            basicItem(ModItems.MACROPROGRAM_ADMINISTRATOR_ACCESS_ARMOR.get());
            basicItem(ModItems.MACROPROGRAM_PACKET_SNIFFER.get());
            basicItem(ModItems.MACROPROGRAM_VPN_SHROUD.get());
        }

        withExistingParent("materialized_data_miner", mcLoc("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "item/materialized_data_miner"));

        withExistingParent("materialized_secure_shell", mcLoc("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "item/materialized_secure_shell"));

        withExistingParent("materialized_bit_crusher", mcLoc("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "item/materialized_bit_crusher"));

        withExistingParent("materialized_stack_overflow", mcLoc("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "item/materialized_stack_overflow"));

        withExistingParent("materialized_icebreaker", mcLoc("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "item/materialized_icebreaker"));

        withExistingParent("materialized_ping", mcLoc("item/bow"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "item/materialized_ping"));

        withExistingParent("materialized_ddos", mcLoc("item/bow"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "item/materialized_ddos"))
                .override()
                .predicate(mcLoc("pulling"), 1.0F)
                .model(withExistingParent("materialized_ddos_pulling_0", mcLoc("item/bow"))
                        .texture("layer0", ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "item/materialized_ddos_pulling_0")))
                .end()
                .override()
                .predicate(mcLoc("pulling"), 1.0F)
                .predicate(mcLoc("pull"), 0.65F)
                .model(withExistingParent("materialized_ddos_pulling_1", mcLoc("item/bow"))
                        .texture("layer0", ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "item/materialized_ddos_pulling_1")))
                .end()
                .override()
                .predicate(mcLoc("pulling"), 1.0F)
                .predicate(mcLoc("pull"), 0.9F)
                .model(withExistingParent("materialized_ddos_pulling_2", mcLoc("item/bow"))
                        .texture("layer0", ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "item/materialized_ddos_pulling_2")))
                .end();

        withExistingParent("materialized_firewall", mcLoc("item/shield"))
                .override()
                .predicate(mcLoc("blocking"), 1.0F)
                .model(withExistingParent("materialized_firewall_blocking", mcLoc("item/shield_blocking")))
                .end();

        withExistingParent("materialized_banhammer", mcLoc("item/handheld"))
                .texture("layer0", modLoc("item/materialized_banhammer"));

        withExistingParent("macroprogram_basic_armor_helmet", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/macroprogram_basic_armor_helmet"));
        withExistingParent("macroprogram_basic_armor_chestplate", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/macroprogram_basic_armor_chestplate"));
        withExistingParent("macroprogram_basic_armor_leggings", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/macroprogram_basic_armor_leggings"));
        withExistingParent("macroprogram_basic_armor_boots", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/macroprogram_basic_armor_boots"));

        withExistingParent("macroprogram_intermediate_armor_helmet", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/macroprogram_intermediate_armor_helmet"));
        withExistingParent("macroprogram_intermediate_armor_chestplate", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/macroprogram_intermediate_armor_chestplate"));
        withExistingParent("macroprogram_intermediate_armor_leggings", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/macroprogram_intermediate_armor_leggings"));
        withExistingParent("macroprogram_intermediate_armor_boots", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/macroprogram_intermediate_armor_boots"));

        withExistingParent("macroprogram_advanced_armor_helmet", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/macroprogram_advanced_armor_helmet"));
        withExistingParent("macroprogram_advanced_armor_chestplate", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/macroprogram_advanced_armor_chestplate"));
        withExistingParent("macroprogram_advanced_armor_leggings", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/macroprogram_advanced_armor_leggings"));
        withExistingParent("macroprogram_advanced_armor_boots", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/macroprogram_advanced_armor_boots"));

        withExistingParent("macroprogram_administrator_access_armor_helmet", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/macroprogram_administrator_access_armor_helmet"));
        withExistingParent("macroprogram_administrator_access_armor_chestplate", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/macroprogram_administrator_access_armor_chestplate"));
        withExistingParent("macroprogram_administrator_access_armor_leggings", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/macroprogram_administrator_access_armor_leggings"));
        withExistingParent("macroprogram_administrator_access_armor_boots", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/macroprogram_administrator_access_armor_boots"));




    }


    private void microprogramItem(net.minecraft.world.item.Item item, String textureName) {
        ResourceLocation itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);

        withExistingParent(itemId.getPath(), mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "item/" + textureName));
    }
}