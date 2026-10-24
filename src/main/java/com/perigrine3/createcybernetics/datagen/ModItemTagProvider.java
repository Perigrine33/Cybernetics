package com.perigrine3.createcybernetics.datagen;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.potion.ModPotions;
import com.perigrine3.createcybernetics.util.ModTags;
import com.simibubi.create.AllTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, CreateCybernetics.MODID, existingFileHelper);
    }

    private static ResourceLocation resourceLocation(String id) {
        return ResourceLocation.parse(id);
    }

    private static final TagKey<Item> LAB_STANDARD_DATA_SHARDS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "lab_loot/standard_data_shards"));
    private static final TagKey<Item> LAB_FINISHED_CYBERWARE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "lab_loot/finished_cyberware"));
    private static final TagKey<Item> LAB_SCAVENGED_ENERGY_GENERATORS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "lab_loot/scavenged_energy_generators"));

    @Override
    protected void addTags(HolderLookup.Provider provider) {
//TOGGLEABLE CYBERWARE
        tag(ModTags.Items.TOGGLEABLE_CYBERWARE)
                .add(ModItems.EYEUPGRADES_ZOOM.get())
                .add(ModItems.EYEUPGRADES_NIGHTVISION.get())
                .add(ModItems.ARMUPGRADES_CLAWS.get())
                .add(ModItems.ARMUPGRADES_CLAWS.get())
                .add(ModItems.ARMUPGRADES_FIRESTARTER.get())
                .add(ModItems.SKINUPGRADES_CHROMATOPHORES.get())
                .add(ModItems.BRAINUPGRADES_IDEM.get())
                .add(ModItems.BRAINUPGRADES_MATRIX.get())
                .add(ModItems.BONEUPGRADES_SPINALINJECTOR.get())
                .add(ModItems.BONEUPGRADES_SANDEVISTAN.get())
                .add(ModItems.ARMUPGRADES_ARMCANNON.get())
                .add(ModItems.LEGUPGRADES_JUMPBOOST.get())
                .add(ModItems.SKINUPGRADES_SYNTHETICSETULES.get())
                .add(ModItems.ARMUPGRADES_ARCCANNON.get())
                .add(ModItems.LUNGSUPGRADES_VAMPYRES.get())
                .add(ModItems.BRAINUPGRADES_KERENZIKOV.get())
                .add(ModItems.BRAINUPGRADES_BERSERK.get())

                .add(ModItems.ARMUPGRADES_MANTISBLADE_IRON.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_COPPER.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_TITANIUM.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_GOLD.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_DIAMOND.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_NETHERITE.get())

                .add(ModItems.WETWARE_FIREBREATHINGLUNGS.get())
                .add(ModItems.WETWARE_SCULKLUNGS.get())
                .add(ModItems.WETWARE_GUARDIANEYE.get())
                .add(ModItems.WETWARE_WEBSHOOTING_LEFTARM.get())
                .add(ModItems.WETWARE_WEBSHOOTING_RIGHTARM.get())
                .add(ModItems.WETWARE_WEBSHOOTINGINTESTINES.get())

                .addOptional(resourceLocation("createcybernetics:boneupgrades_elytra"))
                .addOptional(resourceLocation("createcybernetics:skinupgrades_ultraviolent"));


//ENERGY GENERATING CYBERWARE
        tag(ModTags.Items.ENERGY_GENERATING_CYBERWARE)
                .add(ModItems.BONEUPGRADES_PIEZO.get())
                .add(ModItems.ORGANSUPGRADES_DIAMONDWAFERSTACK.get())
                .add(ModItems.HEARTUPGRADES_COUPLER.get())
                .add(ModItems.ORGANSUPGRADES_DUALISTICCONVERTER.get())
                .add(ModItems.ORGANSUPGRADES_MAGICCATALYST.get())
                .add(ModItems.ORGANSUPGRADES_METABOLIC.get())
                .add(ModItems.SKINUPGRADES_SOLARSKIN.get())
                .add(ModItems.SKINUPGRADES_ARTERIALTURBINE.get())
                .add(ModItems.ORGANSUPGRADES_HEATENGINE.get());

//ARM CANNON AMMO
        tag(ModTags.Items.ARM_CANNON_AMMO)
                .addTag(Tags.Items.NUGGETS)
                .add(Items.ARROW)
                .add(Items.SPECTRAL_ARROW)
                .add(Items.TIPPED_ARROW)
                .add(Items.TNT)
                .add(Items.SNOWBALL)
                .add(Items.ENDER_PEARL)
                .add(Items.EGG)
                .add(Items.FIRE_CHARGE)
                .add(Items.FIREWORK_ROCKET)
                .add(Items.WIND_CHARGE);

//MANTIS BLADES
        tag(ModTags.Items.MANTIS_BLADES)
                .add(ModItems.ARMUPGRADES_MANTISBLADE_IRON.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_COPPER.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_TITANIUM.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_GOLD.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_DIAMOND.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_NETHERITE.get());

//DATA SHARDS
        tag(ModTags.Items.DATA_SHARDS)
                .add(ModItems.DATA_SHARD_RED.get())
                .add(ModItems.DATA_SHARD_ORANGE.get())
                .add(ModItems.DATA_SHARD_YELLOW.get())
                .add(ModItems.DATA_SHARD_GREEN.get())
                .add(ModItems.DATA_SHARD_CYAN.get())
                .add(ModItems.DATA_SHARD_BLUE.get())
                .add(ModItems.DATA_SHARD_PURPLE.get())
                .add(ModItems.DATA_SHARD_PINK.get())
                .add(ModItems.DATA_SHARD_BROWN.get())
                .add(ModItems.DATA_SHARD_GRAY.get())
                .add(ModItems.DATA_SHARD_BLACK.get())

                .add(ModItems.DATA_SHARD_BIOCHIP.get())
                .add(ModItems.DATA_SHARD_SHARED_NAVIGATION.get())
                .add(ModItems.QUICKHACK_SHARD.get())
                .add(ModItems.DATA_SHARD_INFOLOG.get())
                .add(ModItems.DATA_SHARD_INFOLOG_GETTING_STARTED.get())
                .add(ModItems.DATA_SHARD_INFOLOG_ROBOSURGEON_MANUAL.get())
                .add(ModItems.DATA_SHARD_INFOLOG_SURGERY_TABLE_MANUAL.get())
                .add(ModItems.DATA_SHARD_INFOLOG_FBC_GUIDE.get())
                .add(ModItems.DATA_SHARD_INFOLOG_CYBERBESTIARY.get())
                .add(ModItems.DATA_SHARD_INFOLOG_CYBERDECK.get())
                .add(ModItems.DATA_SHARD_INFOLOG_HARVESTER_ENCHANTMENT.get())

                .add(ModItems.DATA_SHARD_INFOLOG_SUNSET_SHIFT.get())
                .add(ModItems.DATA_SHARD_INFOLOG_HOUSE_RULES.get())
                .add(ModItems.DATA_SHARD_INFOLOG_DENSE_BATTERY_AD.get())
                .add(ModItems.DATA_SHARD_INFOLOG_IMMUNOSUPPRESSOR_ADVISORY.get())
                .add(ModItems.DATA_SHARD_INFOLOG_CHROMATOPHORE_FIELD_NOTE.get())
                .add(ModItems.DATA_SHARD_INFOLOG_THE_RAISE.get())
                .add(ModItems.DATA_SHARD_INFOLOG_OMNISCIENT_OPTICS_AD.get())
                .add(ModItems.DATA_SHARD_INFOLOG_PRIVATE_WARD_REPORT.get())
                .add(ModItems.DATA_SHARD_INFOLOG_DRILLFIST_AD.get())
                .add(ModItems.DATA_SHARD_INFOLOG_IDEM_ARTICLE.get())
                .add(ModItems.DATA_SHARD_INFOLOG_MOM_IM_FINE.get())
                .add(ModItems.DATA_SHARD_INFOLOG_CYBERDECK_SECURITY_BRIEF.get())
                .add(ModItems.DATA_SHARD_INFOLOG_LUNCH_BREAK.get())
                .add(ModItems.DATA_SHARD_INFOLOG_DATURA.get())
                .add(ModItems.DATA_SHARD_INFOLOG_QUICKHACK_TUTORIAL.get())
                .add(ModItems.DATA_SHARD_INFOLOG_WAIT_YOUR_TURN.get())
                .add(ModItems.DATA_SHARD_INFOLOG_THE_BREACH.get())
                .add(ModItems.DATA_SHARD_INFOLOG_AI_CHAT.get())
                .add(ModItems.DATA_SHARD_INFOLOG_CYBERPSYCHO_TRANSCRIPT.get())
                .add(ModItems.DATA_SHARD_INFOLOG_CPU_INSTALL.get())

                .add(ModItems.DATA_SHARD_GAME_MINESWEEPER.get())
                .add(ModItems.DATA_SHARD_GAME_CHESS.get())

                .add(ModItems.DATA_SHARD_MICROPROGRAMS.get())
                .add(ModItems.DATA_SHARD_MICROPROGRAMS_CREATIVE.get())
                .add(ModItems.QUICKHACK_SHARD.get())

                .addOptional(resourceLocation("createcybernetics:data_shard_infolog_cyberchems"));

//SKILL SHARDS
        tag(ModTags.Items.SKILL_SHARDS)
                .add(ModItems.DATA_SHARD_RED.get())
                .add(ModItems.DATA_SHARD_ORANGE.get())
                .add(ModItems.DATA_SHARD_YELLOW.get())
                .add(ModItems.DATA_SHARD_GREEN.get())
                .add(ModItems.DATA_SHARD_CYAN.get())
                .add(ModItems.DATA_SHARD_BLUE.get())
                .add(ModItems.DATA_SHARD_PURPLE.get())
                .add(ModItems.DATA_SHARD_PINK.get())
                .add(ModItems.DATA_SHARD_BROWN.get())
                .add(ModItems.DATA_SHARD_GRAY.get())
                .add(ModItems.DATA_SHARD_BLACK.get());

//TUTORIAL SHARDS
        tag(ModTags.Items.TUTORIAL_SHARDS)
                .add(ModItems.DATA_SHARD_INFOLOG_GETTING_STARTED.get())
                .add(ModItems.DATA_SHARD_INFOLOG_ROBOSURGEON_MANUAL.get())
                .add(ModItems.DATA_SHARD_INFOLOG_SURGERY_TABLE_MANUAL.get())
                .add(ModItems.DATA_SHARD_INFOLOG_FBC_GUIDE.get())
                .add(ModItems.DATA_SHARD_INFOLOG_CYBERBESTIARY.get())
                .add(ModItems.DATA_SHARD_INFOLOG_CYBERDECK.get())
                .add(ModItems.DATA_SHARD_INFOLOG_HARVESTER_ENCHANTMENT.get())
                .add(ModItems.DATA_SHARD_INFOLOG_QUICKHACK_TUTORIAL.get())

                .addOptional(resourceLocation("createcybernetics:data_shard_infolog_cyberchems"));

//LORE SHARDS
        tag(ModTags.Items.LORE_SHARDS)
                .add(ModItems.DATA_SHARD_INFOLOG_SUNSET_SHIFT.get())
                .add(ModItems.DATA_SHARD_INFOLOG_HOUSE_RULES.get())
                .add(ModItems.DATA_SHARD_INFOLOG_DENSE_BATTERY_AD.get())
                .add(ModItems.DATA_SHARD_INFOLOG_IMMUNOSUPPRESSOR_ADVISORY.get())
                .add(ModItems.DATA_SHARD_INFOLOG_CHROMATOPHORE_FIELD_NOTE.get())
                .add(ModItems.DATA_SHARD_INFOLOG_THE_RAISE.get())
                .add(ModItems.DATA_SHARD_INFOLOG_OMNISCIENT_OPTICS_AD.get())
                .add(ModItems.DATA_SHARD_INFOLOG_PRIVATE_WARD_REPORT.get())
                .add(ModItems.DATA_SHARD_INFOLOG_DRILLFIST_AD.get())
                .add(ModItems.DATA_SHARD_INFOLOG_IDEM_ARTICLE.get())
                .add(ModItems.DATA_SHARD_INFOLOG_MOM_IM_FINE.get())
                .add(ModItems.DATA_SHARD_INFOLOG_CYBERDECK_SECURITY_BRIEF.get())
                .add(ModItems.DATA_SHARD_INFOLOG_LUNCH_BREAK.get())
                .add(ModItems.DATA_SHARD_INFOLOG_DATURA.get())
                .add(ModItems.DATA_SHARD_INFOLOG_WAIT_YOUR_TURN.get())
                .add(ModItems.DATA_SHARD_INFOLOG_THE_BREACH.get())
                .add(ModItems.DATA_SHARD_INFOLOG_AI_CHAT.get())
                .add(ModItems.DATA_SHARD_INFOLOG_CYBERPSYCHO_TRANSCRIPT.get())
                .add(ModItems.DATA_SHARD_INFOLOG_CPU_INSTALL.get());

//GAME SHARDS
        tag(ModTags.Items.GAME_SHARDS)
                .add(ModItems.DATA_SHARD_GAME_MINESWEEPER.get())
                .add(ModItems.DATA_SHARD_GAME_CHESS.get());

//QUICKHACK SHARDS
        tag(ModTags.Items.QUICKHACK_SHARDS)
                .add(ModItems.QUICKHACK_SHARD.get());

//MICROPROGRAM SHARDS
        tag(ModTags.Items.MICROPROGRAM_SHARDS)
                .add(ModItems.DATA_SHARD_MICROPROGRAMS.get());

//MACROPROGRAMS
        tag(ModTags.Items.MACROPROGRAMS)
                .add(ModItems.MACROPROGRAM_DATA_MINER.get())
                .add(ModItems.MACROPROGRAM_BIT_CRUSHER.get())
                .add(ModItems.MACROPROGRAM_STACK_OVERFLOW.get())
                .add(ModItems.MACROPROGRAM_ICEBREAKER.get())
                .add(ModItems.MACROPROGRAM_PING.get())
                .add(ModItems.MACROPROGRAM_DDOS.get())
                .add(ModItems.MACROPROGRAM_BANHAMMER.get())
                .add(ModItems.MACROPROGRAM_FIREWALL.get())
                .add(ModItems.MACROPROGRAM_RECOVERY.get())
                .add(ModItems.MACROPROGRAM_BASIC_ARMOR.get())
                .add(ModItems.MACROPROGRAM_INTERMEDIATE_ARMOR.get())
                .add(ModItems.MACROPROGRAM_ADVANCED_ARMOR.get())
                .add(ModItems.MACROPROGRAM_ADMINISTRATOR_ACCESS_ARMOR.get())
                .add(ModItems.MACROPROGRAM_PACKET_SNIFFER.get())
                .add(ModItems.MACROPROGRAM_VPN_SHROUD.get());

//GRAPHENE
        tag(ModTags.Items.GRAPHENE)
                .add(Items.CHARCOAL)
                .add(Items.COAL)
                .addOptional(resourceLocation("cyberspace:graphite_blend"));

//COMPONENTS
        tag(ModTags.Items.COMPONENT_ITEM)
                .add(ModItems.COMPONENT_ACTUATOR.get())
                .add(ModItems.COMPONENT_FIBEROPTICS.get())
                .add(ModItems.COMPONENT_WIRING.get())
                .add(ModItems.COMPONENT_DIODES.get())
                .add(ModItems.COMPONENT_PLATING.get())
                .add(ModItems.COMPONENT_GRAPHICSCARD.get())
                .add(ModItems.COMPONENT_SSD.get())
                .add(ModItems.COMPONENT_STORAGE.get())
                .add(ModItems.COMPONENT_SYNTHNERVES.get())
                .add(ModItems.COMPONENT_MESH.get())
                .addOptional(resourceLocation("createcybernetics:component_led"))
                .addOptional(resourceLocation("createcybernetics:component_titaniumrod"));


//CYBERWARE
        tag(ModTags.Items.CYBERWARE_ITEM)
                .add(ModItems.BASECYBERWARE_RIGHTLEG_GOLDPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTLEG_GOLDPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTARM_GOLDPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTARM_GOLDPLATED.get())

                .add(ModItems.BASECYBERWARE_RIGHTLEG_COPPERPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTLEG_COPPERPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTARM_COPPERPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTARM_COPPERPLATED.get())

                .add(ModItems.BASECYBERWARE_RIGHTLEG_IRONPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTLEG_IRONPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTARM_IRONPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTARM_IRONPLATED.get())

                .add(ModItems.BASECYBERWARE_RIGHTLEG.get())
                .add(ModItems.BASECYBERWARE_LEFTLEG.get())
                .add(ModItems.BASECYBERWARE_RIGHTARM.get())
                .add(ModItems.BASECYBERWARE_LEFTARM.get())
                .add(ModItems.BASECYBERWARE_CYBEREYES.get())
                .add(ModItems.BASECYBERWARE_LINEARFRAME.get())
                .add(ModItems.EYEUPGRADES_MONOVISION.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS1.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS2.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS3.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS4.get())
                .add(ModItems.EYEUPGRADES_HUDLENS.get())
                .add(ModItems.EYEUPGRADES_HUDJACK.get())
                .add(ModItems.EYEUPGRADES_NIGHTVISION.get())
                .add(ModItems.EYEUPGRADES_TARGETING.get())
                .add(ModItems.EYEUPGRADES_UNDERWATERVISION.get())
                .add(ModItems.EYEUPGRADES_ZOOM.get())
                .add(ModItems.EYEUPGRADES_TRAJECTORYCALCULATOR.get())
                .add(ModItems.EYEUPGRADES_BIOMONITOR.get())
                .add(ModItems.ARMUPGRADES_ARMCANNON.get())
                .add(ModItems.ARMUPGRADES_FLYWHEEL.get())
                .add(ModItems.ARMUPGRADES_CLAWS.get())
                .add(ModItems.ARMUPGRADES_CRAFTHANDS.get())
                .add(ModItems.ARMUPGRADES_DRILLFIST.get())
                .add(ModItems.ARMUPGRADES_FIRESTARTER.get())
                .add(ModItems.ARMUPGRADES_PNEUMATICWRIST.get())
                .add(ModItems.ARMUPGRADES_REINFORCEDKNUCKLES.get())
                .add(ModItems.ARMUPGRADES_RIPPERCLAW.get())
                .add(ModItems.ARMUPGRADES_ARCCANNON.get())
                .add(ModItems.ARMUPGRADES_HEAVYKNUCKLES.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_TITANIUM.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_IRON.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_GOLD.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_COPPER.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_DIAMOND.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_NETHERITE.get())
                .add(ModItems.LEGUPGRADES_METALDETECTOR.get())
                .add(ModItems.LEGUPGRADES_ANKLEBRACERS.get())
                .add(ModItems.LEGUPGRADES_JUMPBOOST.get())
                .add(ModItems.LEGUPGRADES_PROPELLERS.get())
                .add(ModItems.LEGUPGRADES_SPURS.get())
                .add(ModItems.LEGUPGRADES_OCELOTPAWS.get())
                .add(ModItems.BONEUPGRADES_BONEBATTERY.get())
                .add(ModItems.BONEUPGRADES_BONEFLEX.get())
                .add(ModItems.BONEUPGRADES_BONELACING.get())
                .add(ModItems.EYEUPGRADES_TRAJECTORYCALCULATOR.get())
                .add(ModItems.BONEUPGRADES_PIEZO.get())
                .add(ModItems.BONEUPGRADES_SPINALINJECTOR.get())
                .add(ModItems.BONEUPGRADES_SANDEVISTAN.get())
                .add(ModItems.BONEUPGRADES_CYBERSKULL.get())
                .add(ModItems.BRAINUPGRADES_EYEOFDEFENDER.get())
                .add(ModItems.BRAINUPGRADES_ENDERJAMMER.get())
                .add(ModItems.BRAINUPGRADES_MATRIX.get())
                .add(ModItems.BRAINUPGRADES_NEURALCONTEXTUALIZER.get())
                .add(ModItems.BRAINUPGRADES_CYBERDECK.get())
                .add(ModItems.BRAINUPGRADES_IDEM.get())
                .add(ModItems.BRAINUPGRADES_CHIPWARESLOTS.get())
                .add(ModItems.BRAINUPGRADES_NEURALPROCESSOR.get())
                .add(ModItems.BRAINUPGRADES_ICEPROTOCOL.get())
                .add(ModItems.BRAINUPGRADES_COAXIALPORT.get())
                .add(ModItems.BRAINUPGRADES_KERENZIKOV.get())
                .add(ModItems.BRAINUPGRADES_BERSERK.get())
                .add(ModItems.BRAINUPGRADES_RAMDECK.get())
                .add(ModItems.BRAINUPGRADES_CYBERBRAIN.get())
                .add(ModItems.HEARTUPGRADES_CYBERHEART.get())
                .add(ModItems.HEARTUPGRADES_COUPLER.get())
                .add(ModItems.HEARTUPGRADES_CREEPERHEART.get())
                .add(ModItems.HEARTUPGRADES_DEFIBRILLATOR.get())
                .add(ModItems.HEARTUPGRADES_STEMCELL.get())
                .add(ModItems.HEARTUPGRADES_PLATELETS.get())
                .add(ModItems.LUNGSUPGRADES_HYPEROXYGENATION.get())
                .add(ModItems.LUNGSUPGRADES_OXYGEN.get())
                .add(ModItems.LUNGSUPGRADES_SYNTHLUNGS.get())
                .add(ModItems.LUNGSUPGRADES_VAMPYRES.get())
                .add(ModItems.ORGANSUPGRADES_ADRENALINE.get())
                .add(ModItems.ORGANSUPGRADES_BATTERY.get())
                .add(ModItems.ORGANSUPGRADES_DIAMONDWAFERSTACK.get())
                .add(ModItems.ORGANSUPGRADES_DUALISTICCONVERTER.get())
                .add(ModItems.ORGANSUPGRADES_LIVERFILTER.get())
                .add(ModItems.ORGANSUPGRADES_MAGICCATALYST.get())
                .add(ModItems.ORGANSUPGRADES_METABOLIC.get())
                .add(ModItems.ORGANSUPGRADES_DENSEBATTERY.get())
                .add(ModItems.ORGANSUPGRADES_HEATENGINE.get())
                .add(ModItems.ORGANSUPGRADES_OREGRINDER.get())
                .add(ModItems.SKINUPGRADES_ARTERIALTURBINE.get())
                .add(ModItems.SKINUPGRADES_CHROMATOPHORES.get())
                .add(ModItems.SKINUPGRADES_SYNTHSKIN.get())
                .add(ModItems.SKINUPGRADES_IMMUNO.get())
                .add(ModItems.SKINUPGRADES_FACEPLATE.get())
                .add(ModItems.SKINUPGRADES_NETHERITEPLATING.get())
                .add(ModItems.SKINUPGRADES_SOLARSKIN.get())
                .add(ModItems.SKINUPGRADES_SUBDERMALARMOR.get())
                .add(ModItems.SKINUPGRADES_SUBDERMALSPIKES.get())
                .add(ModItems.SKINUPGRADES_SYNTHETICSETULES.get())
                .add(ModItems.SKINUPGRADES_METALPLATING.get())
                .add(ModItems.SKINUPGRADES_EMPTHREADING.get())
                .add(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get())
                .add(ModItems.MUSCLEUPGRADES_REINFORCEDTENDONS.get())
                .add(ModItems.MUSCLEUPGRADES_WIREDREFLEXES.get())
                .add(ModItems.MUSCLEUPGRADES_BALLISTICGEL.get())
                .addOptional(resourceLocation("createcybernetics:boneupgrades_elytra"))
                .addOptional(resourceLocation("createcybernetics:skinupgrades_sweat"))
                .addOptional(resourceLocation("createcybernetics:skinupgrades_manaskin"))
                .addOptional(resourceLocation("createcybernetics:skinupgrades_ultraviolent"))
                .addOptional(resourceLocation("createcybernetics:eyeupgrades_navigationchip"))
                .addOptional(resourceLocation("createcybernetics:brainupgrades_consciousnesstransmitter"))
                .addOptional(resourceLocation("createcybernetics:brainupgrades_corticalstack"))
                .addOptional(resourceLocation("createcybernetics:brainupgrades_spelljammer"))
                .addOptional(resourceLocation("createcybernetics:organsupgrades_manabattery"))
                .addOptional(resourceLocation("createcybernetics:heartupgrades_anomaly"));

//BODYPART DROPS
        tag(ModTags.Items.BODYPART_DROPS)
                .add(ModItems.BODYPART_SKELETON.get())
                .add(ModItems.BODYPART_BRAIN.get())
                .add(ModItems.BODYPART_EYEBALLS.get())
                .add(ModItems.BODYPART_HEART.get())
                .add(ModItems.BODYPART_LUNGS.get())
                .add(ModItems.BODYPART_LIVER.get())
                .add(ModItems.BODYPART_INTESTINES.get())
                .add(ModItems.BODYPART_MUSCLE.get());

//GRASSFED BODYPART DROPS
        tag(ModTags.Items.GRASSFED_BODYPART_DROPS)
                .add(ModItems.BODYPART_SKELETON.get())
                .add(ModItems.BODYPART_BRAIN.get())
                .add(ModItems.BODYPART_EYEBALLS.get())
                .add(ModItems.BODYPART_HEART.get())
                .add(ModItems.BODYPART_LUNGS.get())
                .add(ModItems.BODYPART_LIVER.get())
                .add(ModItems.WETWARE_GRASSFEDSTOMACH.get())
                .add(ModItems.BODYPART_MUSCLE.get());

//FISH BODYPART DROPS
        tag(ModTags.Items.FISH_BODYPART_DROPS)
                .add(ModItems.BODYPART_SKELETON.get())
                .add(ModItems.BODYPART_EYEBALLS.get())
                .add(ModItems.BODYPART_GILLS.get())
                .add(ModItems.BODYPART_MUSCLE.get());

//HUMANOID BODYPART DROPS
        tag(ModTags.Items.HUMANOID_BODYPART_DROPS)
                .add(ModItems.BODYPART_RIGHTLEG.get())
                .add(ModItems.BODYPART_LEFTLEG.get())
                .add(ModItems.BODYPART_RIGHTARM.get())
                .add(ModItems.BODYPART_LEFTARM.get())
                .add(ModItems.BODYPART_SKELETON.get())
                .add(ModItems.BODYPART_BRAIN.get())
                .add(ModItems.BODYPART_EYEBALLS.get())
                .add(ModItems.BODYPART_HEART.get())
                .add(ModItems.BODYPART_LUNGS.get())
                .add(ModItems.BODYPART_LIVER.get())
                .add(ModItems.BODYPART_INTESTINES.get())
                .add(ModItems.BODYPART_MUSCLE.get())
                .add(ModItems.BODYPART_SKIN.get());


//WETWARE/BODY PARTS
        tag(ModTags.Items.WETWARE_ITEM)
                .add(ModItems.BODYPART_RIGHTLEG.get())
                .add(ModItems.BODYPART_LEFTLEG.get())
                .add(ModItems.BODYPART_RIGHTARM.get())
                .add(ModItems.BODYPART_LEFTARM.get())
                .add(ModItems.BODYPART_SKELETON.get())
                .add(ModItems.BODYPART_BRAIN.get())
                .add(ModItems.BODYPART_EYEBALLS.get())
                .add(ModItems.BODYPART_HEART.get())
                .add(ModItems.BODYPART_LUNGS.get())
                .add(ModItems.BODYPART_LIVER.get())
                .add(ModItems.BODYPART_INTESTINES.get())
                .add(ModItems.BODYPART_MUSCLE.get())
                .add(ModItems.BODYPART_SKIN.get())
                .add(ModItems.BODYPART_GUARDIANRETINA.get())
                .add(ModItems.BODYPART_WARDENESOPHAGUS.get())
                .add(ModItems.BODYPART_GYROSCOPICBLADDER.get())
                .add(ModItems.BODYPART_SPINNERETTE.get())
                .add(ModItems.BODYPART_FIREGLAND.get())
                .add(ModItems.BODYPART_GILLS.get())
                .add(ModItems.BODYPART_AXOLOTLMARROW.get())
                .add(ModItems.BODYPART_DRAGONSCALE.get())

                .add(ModItems.BODYPART_SCULKRIGHTLEG.get())
                .add(ModItems.BODYPART_SCULKLEFTLEG.get())
                .add(ModItems.BODYPART_SCULKRIGHTARM.get())
                .add(ModItems.BODYPART_SCULKLEFTARM.get())
                .add(ModItems.BODYPART_SCULKBRAIN.get())
                .add(ModItems.BODYPART_SCULKLIVER.get())
                .add(ModItems.BODYPART_SCULKINTESTINES.get())
                .add(ModItems.BODYPART_SCULKMUSCLE.get())
                .add(ModItems.BODYPART_SCULKSKIN.get())

                .add(ModItems.WETWARE_FIREBREATHINGLUNGS.get())
                .add(ModItems.WETWARE_WATERBREATHINGLUNGS.get())
                .add(ModItems.WETWARE_GUARDIANEYE.get())
                .add(ModItems.WETWARE_SCULKLUNGS.get())
                .add(ModItems.WETWARE_TACTICALINKSAC.get())
                .add(ModItems.WETWARE_AEROSTASISGYROBLADDER.get())
                .add(ModItems.WETWARE_POLARBEARFUR.get())
                .add(ModItems.WETWARE_RAVAGERTENDONS.get())
                .add(ModItems.WETWARE_GRASSFEDSTOMACH.get())
                .add(ModItems.WETWARE_WEBSHOOTING_LEFTARM.get())
                .add(ModItems.WETWARE_WEBSHOOTING_RIGHTARM.get())
                .add(ModItems.WETWARE_WEBSHOOTINGINTESTINES.get())
                .add(ModItems.WETWARE_SPIDEREYES.get())
                .add(ModItems.WETWARE_BLASTEMASKELETON.get())
                .add(ModItems.WETWARE_DRAGONSKIN.get())
                .add(ModItems.WETWARE_WARDENANTLERS.get())
                .add(ModItems.WETWARE_SCULKHEART.get())
                .add(ModItems.WETWARE_GOOEYMUSCLE.get())
                .add(ModItems.WETWARE_ELECTROCYTEMUSCLE.get())
                .addOptional(resourceLocation("createcybernetics:wetware_blubber"));
//BODY PARTS
        tag(ModTags.Items.BODY_PARTS)
                .add(ModItems.BODYPART_RIGHTLEG.get())
                .add(ModItems.BODYPART_LEFTLEG.get())
                .add(ModItems.BODYPART_RIGHTARM.get())
                .add(ModItems.BODYPART_LEFTARM.get())
                .add(ModItems.BODYPART_SKELETON.get())
                .add(ModItems.BODYPART_BRAIN.get())
                .add(ModItems.BODYPART_EYEBALLS.get())
                .add(ModItems.BODYPART_HEART.get())
                .add(ModItems.BODYPART_LUNGS.get())
                .add(ModItems.BODYPART_LIVER.get())
                .add(ModItems.BODYPART_INTESTINES.get())
                .add(ModItems.BODYPART_MUSCLE.get())
                .add(ModItems.BODYPART_SKIN.get());
//BASE CYBERWARE
        tag(ModTags.Items.BASE_CYBERWARE)
                .add(ModItems.BASECYBERWARE_RIGHTLEG.get())
                .add(ModItems.BASECYBERWARE_LEFTLEG.get())
                .add(ModItems.BASECYBERWARE_RIGHTARM.get())
                .add(ModItems.BASECYBERWARE_LEFTARM.get())
                .add(ModItems.BASECYBERWARE_CYBEREYES.get())
                .add(ModItems.BASECYBERWARE_LINEARFRAME.get());
//ARM UPGRADES
        tag(ModTags.Items.ARM_UPGRADES)
                .add(ModItems.ARMUPGRADES_ARMCANNON.get())
                .add(ModItems.ARMUPGRADES_FLYWHEEL.get())
                .add(ModItems.ARMUPGRADES_CLAWS.get())
                .add(ModItems.ARMUPGRADES_CRAFTHANDS.get())
                .add(ModItems.ARMUPGRADES_DRILLFIST.get())
                .add(ModItems.ARMUPGRADES_FIRESTARTER.get())
                .add(ModItems.ARMUPGRADES_PNEUMATICWRIST.get())
                .add(ModItems.ARMUPGRADES_REINFORCEDKNUCKLES.get())
                .add(ModItems.ARMUPGRADES_RIPPERCLAW.get())
                .add(ModItems.ARMUPGRADES_ARCCANNON.get())
                .add(ModItems.ARMUPGRADES_HEAVYKNUCKLES.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_IRON.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_COPPER.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_TITANIUM.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_GOLD.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_DIAMOND.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_NETHERITE.get());
//LEG UPGRADES
        tag(ModTags.Items.LEG_UPGRADES)
                .add(ModItems.LEGUPGRADES_METALDETECTOR.get())
                .add(ModItems.LEGUPGRADES_ANKLEBRACERS.get())
                .add(ModItems.LEGUPGRADES_JUMPBOOST.get())
                .add(ModItems.LEGUPGRADES_PROPELLERS.get())
                .add(ModItems.LEGUPGRADES_SPURS.get())
                .add(ModItems.LEGUPGRADES_OCELOTPAWS.get());
//BONE UPGRADES
        tag(ModTags.Items.BONE_UPGRADES)
                .add(ModItems.BONEUPGRADES_BONEBATTERY.get())
                .add(ModItems.BONEUPGRADES_BONEFLEX.get())
                .add(ModItems.BONEUPGRADES_BONELACING.get())
                .add(ModItems.BONEUPGRADES_PIEZO.get())
                .add(ModItems.BONEUPGRADES_SPINALINJECTOR.get())
                .add(ModItems.BONEUPGRADES_SANDEVISTAN.get())
                .add(ModItems.BONEUPGRADES_CYBERSKULL.get())
                .addOptional(resourceLocation("createcybernetics:boneupgrades_elytra"));
//SKIN UPGRADES
        tag(ModTags.Items.SKIN_UPGRADES)
                .add(ModItems.SKINUPGRADES_ARTERIALTURBINE.get())
                .add(ModItems.SKINUPGRADES_CHROMATOPHORES.get())
                .add(ModItems.SKINUPGRADES_SYNTHSKIN.get())
                .add(ModItems.SKINUPGRADES_IMMUNO.get())
                .add(ModItems.SKINUPGRADES_FACEPLATE.get())
                .add(ModItems.SKINUPGRADES_NETHERITEPLATING.get())
                .add(ModItems.SKINUPGRADES_SOLARSKIN.get())
                .add(ModItems.SKINUPGRADES_SUBDERMALARMOR.get())
                .add(ModItems.SKINUPGRADES_SUBDERMALSPIKES.get())
                .add(ModItems.SKINUPGRADES_SYNTHETICSETULES.get())
                .add(ModItems.SKINUPGRADES_METALPLATING.get())
                .addOptional(resourceLocation("createcybernetics:skinupgrades_sweat"))
                .addOptional(resourceLocation("createcybernetics:skinupgrades_manaskin"))
                .addOptional(resourceLocation("createcybernetics:skinupgrades_ultraviolent"));
//ORGAN UPGRADES
        tag(ModTags.Items.ORGAN_UPGRADES)
                .add(ModItems.ORGANSUPGRADES_ADRENALINE.get())
                .add(ModItems.ORGANSUPGRADES_BATTERY.get())
                .add(ModItems.ORGANSUPGRADES_DIAMONDWAFERSTACK.get())
                .add(ModItems.ORGANSUPGRADES_DUALISTICCONVERTER.get())
                .add(ModItems.ORGANSUPGRADES_LIVERFILTER.get())
                .add(ModItems.ORGANSUPGRADES_MAGICCATALYST.get())
                .add(ModItems.ORGANSUPGRADES_METABOLIC.get())
                .add(ModItems.ORGANSUPGRADES_DENSEBATTERY.get())
                .add(ModItems.ORGANSUPGRADES_CREATIVEBATTERY.get())
                .add(ModItems.ORGANSUPGRADES_HEATENGINE.get())
                .add(ModItems.ORGANSUPGRADES_OREGRINDER.get())
                .addOptional(resourceLocation("createcybernetics:organsupgrades_manabattery"));
//HEART UPGRADES
        tag(ModTags.Items.HEART_UPGRADES)
                .add(ModItems.HEARTUPGRADES_CYBERHEART.get())
                .add(ModItems.HEARTUPGRADES_COUPLER.get())
                .add(ModItems.HEARTUPGRADES_CREEPERHEART.get())
                .add(ModItems.HEARTUPGRADES_DEFIBRILLATOR.get())
                .add(ModItems.HEARTUPGRADES_STEMCELL.get())
                .add(ModItems.HEARTUPGRADES_PLATELETS.get())
                .addOptional(resourceLocation("createcybernetics:heartupgrades_anomaly"));

//LUNG UPGRADES
        tag(ModTags.Items.LUNG_UPGRADES)
                .add(ModItems.LUNGSUPGRADES_HYPEROXYGENATION.get())
                .add(ModItems.LUNGSUPGRADES_OXYGEN.get())
                .add(ModItems.LUNGSUPGRADES_SYNTHLUNGS.get())
                .add(ModItems.LUNGSUPGRADES_VAMPYRES.get());
//EYE UPGRADES
        tag(ModTags.Items.EYE_UPGRADES)
                .add(ModItems.EYEUPGRADES_MONOVISION.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS1.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS2.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS3.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS4.get())
                .add(ModItems.EYEUPGRADES_HUDLENS.get())
                .add(ModItems.EYEUPGRADES_HUDJACK.get())
                .add(ModItems.EYEUPGRADES_NIGHTVISION.get())
                .add(ModItems.EYEUPGRADES_TARGETING.get())
                .add(ModItems.EYEUPGRADES_UNDERWATERVISION.get())
                .add(ModItems.EYEUPGRADES_ZOOM.get())
                .add(ModItems.EYEUPGRADES_TRAJECTORYCALCULATOR.get())
                .add(ModItems.EYEUPGRADES_BIOMONITOR.get())
                .addOptional(resourceLocation("createcybernetics:eyeupgrades_navigationchip"));
//BRAIN UPGRADES
        tag(ModTags.Items.BRAIN_UPGRADES)
                .add(ModItems.BRAINUPGRADES_CYBERBRAIN.get())
                .add(ModItems.BRAINUPGRADES_EYEOFDEFENDER.get())
                .add(ModItems.BRAINUPGRADES_ENDERJAMMER.get())
                .add(ModItems.BRAINUPGRADES_MATRIX.get())
                .add(ModItems.BRAINUPGRADES_NEURALCONTEXTUALIZER.get())
                .add(ModItems.BRAINUPGRADES_CYBERDECK.get())
                .add(ModItems.BRAINUPGRADES_IDEM.get())
                .add(ModItems.BRAINUPGRADES_CHIPWARESLOTS.get())
                .add(ModItems.BONEUPGRADES_CAPACITORFRAME.get())
                .add(ModItems.BRAINUPGRADES_NEURALPROCESSOR.get())
                .add(ModItems.BRAINUPGRADES_ICEPROTOCOL.get())
                .add(ModItems.BRAINUPGRADES_COAXIALPORT.get())
                .add(ModItems.BRAINUPGRADES_BERSERK.get())
                .add(ModItems.BRAINUPGRADES_RAMDECK.get())
                .addOptional(resourceLocation("createcybernetics:brainupgrades_consciousnesstransmitter"))
                .addOptional(resourceLocation("createcybernetics:brainupgrades_corticalstack"))
                .addOptional(resourceLocation("createcybernetics:brainupgrades_spelljammer"));

//MUSCLE UPGRADES
        tag(ModTags.Items.MUSCLE_UPGRADES)
                .add(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get())
                .add(ModItems.MUSCLEUPGRADES_REINFORCEDTENDONS.get())
                .add(ModItems.MUSCLEUPGRADES_WIREDREFLEXES.get())
                .add(ModItems.MUSCLEUPGRADES_BALLISTICGEL.get());

//SCAVENGED CYBERWARE
        tag(ModTags.Items.SCAVENGED_CYBERWARE)
                .add(ModItems.SCAVENGED_RIGHTLEG.get())
                .add(ModItems.SCAVENGED_LEFTLEG.get())
                .add(ModItems.SCAVENGED_RIGHTARM.get())
                .add(ModItems.SCAVENGED_LEFTARM.get())
                .add(ModItems.SCAVENGED_CYBEREYES.get())
                .add(ModItems.SCAVENGED_LINEARFRAME.get())

                .add(ModItems.SCAVENGED_MONOVISION.get())
                .add(ModItems.SCAVENGED_HUDLENS.get())
                .add(ModItems.SCAVENGED_HUDJACK.get())
                .add(ModItems.SCAVENGED_NIGHTVISION.get())
                .add(ModItems.SCAVENGED_TARGETING.get())
                .add(ModItems.SCAVENGED_UNDERWATERVISION.get())
                .add(ModItems.SCAVENGED_ZOOM.get())
                .add(ModItems.SCAVENGED_TRAJECTORYCALCULATOR.get())
                .add(ModItems.SCAVENGED_BIOMONITOR.get())

                .add(ModItems.SCAVENGED_ARMCANNON.get())
                .add(ModItems.SCAVENGED_FLYWHEEL.get())
                .add(ModItems.SCAVENGED_CLAWS.get())
                .add(ModItems.SCAVENGED_CRAFTHANDS.get())
                .add(ModItems.SCAVENGED_DRILLFIST.get())
                .add(ModItems.SCAVENGED_FIRESTARTER.get())
                .add(ModItems.SCAVENGED_PNEUMATICWRIST.get())
                .add(ModItems.SCAVENGED_REINFORCEDKNUCKLES.get())
                .add(ModItems.SCAVENGED_ARCCANNON.get())
                .add(ModItems.SCAVENGED_MANTISBLADE_TITANIUM.get())
                .add(ModItems.SCAVENGED_MANTISBLADE_IRON.get())
                .add(ModItems.SCAVENGED_MANTISBLADE_COPPER.get())
                .add(ModItems.SCAVENGED_MANTISBLADE_GOLD.get())
                .add(ModItems.SCAVENGED_MANTISBLADE_DIAMOND.get())
                .add(ModItems.SCAVENGED_MANTISBLADE_NETHERITE.get())

                .add(ModItems.SCAVENGED_METALDETECTOR.get())
                .add(ModItems.SCAVENGED_ANKLEBRACERS.get())
                .add(ModItems.SCAVENGED_JUMPBOOST.get())
                .add(ModItems.SCAVENGED_PROPELLERS.get())
                .add(ModItems.SCAVENGED_SPURS.get())
                .add(ModItems.SCAVENGED_OCELOTPAWS.get())

                .add(ModItems.SCAVENGED_BONEBATTERY.get())
                .add(ModItems.SCAVENGED_BONEFLEX.get())
                .add(ModItems.SCAVENGED_BONELACING.get())
                .add(ModItems.SCAVENGED_CAPACITORFRAME.get())
                .add(ModItems.SCAVENGED_PIEZO.get())
                .add(ModItems.SCAVENGED_SPINALINJECTOR.get())
                .add(ModItems.SCAVENGED_SANDEVISTAN.get())

                .add(ModItems.SCAVENGED_EYEOFDEFENDER.get())
                .add(ModItems.SCAVENGED_ENDERJAMMER.get())
                .add(ModItems.SCAVENGED_MATRIX.get())
                .add(ModItems.SCAVENGED_NEURALCONTEXTUALIZER.get())
                .add(ModItems.SCAVENGED_CYBERDECK.get())
                .add(ModItems.SCAVENGED_CHIPWARESLOTS.get())
                .add(ModItems.SCAVENGED_NEURALPROCESSOR.get())
                .add(ModItems.SCAVENGED_ICEPROTOCOL.get())
                .add(ModItems.SCAVENGED_COAXIALPORT.get())
                .add(ModItems.SCAVENGED_KERENZIKOV.get())
                .add(ModItems.SCAVENGED_BERSERK.get())
                .add(ModItems.SCAVENGED_RAMDECK.get())
                .add(ModItems.SCAVENGED_IDEM.get())

                .add(ModItems.SCAVENGED_CYBERHEART.get())
                .add(ModItems.SCAVENGED_COUPLER.get())
                .add(ModItems.SCAVENGED_CREEPERHEART.get())
                .add(ModItems.SCAVENGED_DEFIBRILLATOR.get())
                .add(ModItems.SCAVENGED_STEMCELL.get())
                .add(ModItems.SCAVENGED_PLATELETS.get())

                .add(ModItems.SCAVENGED_HYPEROXYGENATION.get())
                .add(ModItems.SCAVENGED_OXYGEN.get())
                .add(ModItems.SCAVENGED_SYNTHLUNGS.get())
                .add(ModItems.SCAVENGED_VAMPYRES.get())

                .add(ModItems.SCAVENGED_ADRENALINE.get())
                .add(ModItems.SCAVENGED_BATTERY.get())
                .add(ModItems.SCAVENGED_DIAMONDWAFERSTACK.get())
                .add(ModItems.SCAVENGED_DUALISTICCONVERTER.get())
                .add(ModItems.SCAVENGED_LIVERFILTER.get())
                .add(ModItems.SCAVENGED_METABOLIC.get())
                .add(ModItems.SCAVENGED_DENSEBATTERY.get())
                .add(ModItems.SCAVENGED_HEATENGINE.get())
                .add(ModItems.SCAVENGED_OREGRINDER.get())

                .add(ModItems.SCAVENGED_ARTERIALTURBINE.get())
                .add(ModItems.SCAVENGED_CHROMATOPHORES.get())
                .add(ModItems.SCAVENGED_SYNTHSKIN.get())
                .add(ModItems.SCAVENGED_IMMUNO.get())
                .add(ModItems.SCAVENGED_FACEPLATE.get())
                .add(ModItems.SCAVENGED_NETHERITEPLATING.get())
                .add(ModItems.SCAVENGED_SOLARSKIN.get())
                .add(ModItems.SCAVENGED_SUBDERMALARMOR.get())
                .add(ModItems.SCAVENGED_SUBDERMALSPIKES.get())
                .add(ModItems.SCAVENGED_SYNTHETICSETULES.get())
                .add(ModItems.SCAVENGED_METALPLATING.get())
                .add(ModItems.SCAVENGED_SYNTHMUSCLE.get())
                .add(ModItems.SCAVENGED_REINFORCEDTENDONS.get())
                .add(ModItems.SCAVENGED_WIREDREFLEXES.get())
                .add(ModItems.SCAVENGED_BALLISTICGEL.get())
                .addOptional(resourceLocation("createcybernetics:scavenged_elytra"))
                .addOptional(resourceLocation("createcybernetics:scavenged_navigationchip"))
                .addOptional(resourceLocation("createcybernetics:scavenged_consciousnesstransmitter"))
                .addOptional(resourceLocation("createcybernetics:scavenged_corticalstack"))
                .addOptional(resourceLocation("createcybernetics:scavenged_sweat"))
                .addOptional(resourceLocation("createcybernetics:scavenged_manabattery"))
                .addOptional(resourceLocation("createcybernetics:scavenged_manaskin"))
                .addOptional(resourceLocation("createcybernetics:scavenged_ultraviolent"))
                .addOptional(resourceLocation("createcybernetics:scavenged_spelljammer"));


        tag(ModTags.Items.BRAIN_ITEMS)
                .add(ModItems.BRAINUPGRADES_CYBERBRAIN.get())
                .add(ModItems.BODYPART_SCULKBRAIN.get())
                .add(ModItems.BODYPART_BRAIN.get());

        tag(ModTags.Items.EYE_ITEMS)
                .add(ModItems.BASECYBERWARE_CYBEREYES.get())
                .add(ModItems.EYEUPGRADES_MONOVISION.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS1.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS2.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS3.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS4.get())
                .add(ModItems.WETWARE_SPIDEREYES.get())
                .add(ModItems.WETWARE_WARDENANTLERS.get())
                .add(ModItems.BODYPART_EYEBALLS.get());

        tag(ModTags.Items.SKIN_ITEMS)
                .add(ModItems.SKINUPGRADES_METALPLATING.get())
                .add(ModItems.SKINUPGRADES_SUBDERMALSPIKES.get())
                .add(ModItems.SKINUPGRADES_SUBDERMALARMOR.get())
                .add(ModItems.SKINUPGRADES_SOLARSKIN.get())
                .add(ModItems.SKINUPGRADES_NETHERITEPLATING.get())
                .add(ModItems.SKINUPGRADES_SYNTHSKIN.get())
                .add(ModItems.SKINUPGRADES_CHROMATOPHORES.get())
                .add(ModItems.SKINUPGRADES_EMPTHREADING.get())
                .add(ModItems.WETWARE_POLARBEARFUR.get())
                .add(ModItems.WETWARE_DRAGONSKIN.get())
                .add(ModItems.BODYPART_SCULKSKIN.get())
                .add(ModItems.BODYPART_SKIN.get())
                .addOptional(resourceLocation("createcybernetics:skinupgrades_sweat"))
                .addOptional(resourceLocation("createcybernetics:skinupgrades_manaskin"))
                .addOptional(resourceLocation("createcybernetics:skinupgrades_ultraviolent"));

        tag(ModTags.Items.MUSCLE_ITEMS)
                .add(ModItems.WETWARE_RAVAGERTENDONS.get())
                .add(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get())
                .add(ModItems.MUSCLEUPGRADES_REINFORCEDTENDONS.get())
                .add(ModItems.BODYPART_SCULKMUSCLE.get())
                .add(ModItems.WETWARE_ELECTROCYTEMUSCLE.get())
                .add(ModItems.WETWARE_GOOEYMUSCLE.get())
                .add(ModItems.BODYPART_MUSCLE.get());

        tag(ModTags.Items.BONE_ITEMS)
                .add(ModItems.BASECYBERWARE_LINEARFRAME.get())
                .add(ModItems.BONEUPGRADES_CAPACITORFRAME.get())
                .add(ModItems.WETWARE_BLASTEMASKELETON.get())
                .add(ModItems.BODYPART_SKELETON.get());

        tag(ModTags.Items.HEART_ITEMS)
                .add(ModItems.HEARTUPGRADES_CYBERHEART.get())
                .add(ModItems.WETWARE_SCULKHEART.get())
                .add(ModItems.BODYPART_HEART.get())
                .addOptional(resourceLocation("createcybernetics:heartupgrades_anomaly"));

        tag(ModTags.Items.LUNGS_ITEMS)
                .add(ModItems.WETWARE_SCULKLUNGS.get())
                .add(ModItems.WETWARE_AEROSTASISGYROBLADDER.get())
                .add(ModItems.WETWARE_FIREBREATHINGLUNGS.get())
                .add(ModItems.LUNGSUPGRADES_SYNTHLUNGS.get())
                .add(ModItems.LUNGSUPGRADES_VAMPYRES.get())
                .add(ModItems.BODYPART_LUNGS.get());

        tag(ModTags.Items.LIVER_ITEMS)
                .add(ModItems.ORGANSUPGRADES_LIVERFILTER.get())
                .add(ModItems.BODYPART_SCULKLIVER.get())
                .add(ModItems.BODYPART_LIVER.get());

        tag(ModTags.Items.INTESTINES_ITEMS)
                .add(ModItems.WETWARE_TACTICALINKSAC.get())
                .add(ModItems.WETWARE_GRASSFEDSTOMACH.get())
                .add(ModItems.WETWARE_WEBSHOOTINGINTESTINES.get())
                .add(ModItems.ORGANSUPGRADES_OREGRINDER.get())
                .add(ModItems.BODYPART_SCULKINTESTINES.get())
                .add(ModItems.BODYPART_INTESTINES.get());

        tag(ModTags.Items.LEFTARM_ITEMS)
                .add(ModItems.BASECYBERWARE_LEFTARM.get())
                .add(ModItems.BASECYBERWARE_LEFTARM_COPPERPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTARM_IRONPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTARM_GOLDPLATED.get())
                .add(ModItems.WETWARE_WEBSHOOTING_LEFTARM.get())
                .add(ModItems.BODYPART_SCULKLEFTARM.get())
                .add(ModItems.BODYPART_LEFTARM.get());

        tag(ModTags.Items.RIGHTARM_ITEMS)
                .add(ModItems.BASECYBERWARE_RIGHTARM.get())
                .add(ModItems.BASECYBERWARE_RIGHTARM_COPPERPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTARM_IRONPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTARM_GOLDPLATED.get())
                .add(ModItems.WETWARE_WEBSHOOTING_RIGHTARM.get())
                .add(ModItems.BODYPART_SCULKRIGHTARM.get())
                .add(ModItems.BODYPART_RIGHTARM.get());

        tag(ModTags.Items.LEFTLEG_ITEMS)
                .add(ModItems.BASECYBERWARE_LEFTLEG.get())
                .add(ModItems.BASECYBERWARE_LEFTLEG_COPPERPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTLEG_IRONPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTLEG_GOLDPLATED.get())
                .add(ModItems.BODYPART_SCULKLEFTLEG.get())
                .add(ModItems.BODYPART_LEFTLEG.get());

        tag(ModTags.Items.RIGHTLEG_ITEMS)
                .add(ModItems.BASECYBERWARE_RIGHTLEG.get())
                .add(ModItems.BASECYBERWARE_RIGHTLEG_COPPERPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTLEG_IRONPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTLEG_GOLDPLATED.get())
                .add(ModItems.BODYPART_SCULKRIGHTLEG.get())
                .add(ModItems.BODYPART_RIGHTLEG.get());



        tag(ModTags.Items.BRAIN_REPLACEMENTS)
                .add(ModItems.BODYPART_SCULKBRAIN.get())
                .add(ModItems.BRAINUPGRADES_CYBERBRAIN.get());

        tag(ModTags.Items.EYE_REPLACEMENTS)
                .add(ModItems.WETWARE_SPIDEREYES.get())
                .add(ModItems.WETWARE_WARDENANTLERS.get())
                .add(ModItems.EYEUPGRADES_MONOVISION.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS1.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS2.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS3.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS4.get())
                .add(ModItems.BASECYBERWARE_CYBEREYES.get());

        tag(ModTags.Items.SKIN_REPLACEMENTS)
                .add(ModItems.SKINUPGRADES_METALPLATING.get())
                .add(ModItems.SKINUPGRADES_SUBDERMALSPIKES.get())
                .add(ModItems.SKINUPGRADES_SUBDERMALARMOR.get())
                .add(ModItems.SKINUPGRADES_SOLARSKIN.get())
                .add(ModItems.SKINUPGRADES_NETHERITEPLATING.get())
                .add(ModItems.SKINUPGRADES_SYNTHSKIN.get())
                .add(ModItems.SKINUPGRADES_CHROMATOPHORES.get())
                .add(ModItems.WETWARE_POLARBEARFUR.get())
                .add(ModItems.WETWARE_DRAGONSKIN.get())
                .add(ModItems.BODYPART_SCULKSKIN.get())
                .addOptional(resourceLocation("createcybernetics:skinupgrades_sweat"))
                .addOptional(resourceLocation("createcybernetics:skinupgrades_manaskin"))
                .addOptional(resourceLocation("createcybernetics:skinupgrades_ultraviolent"));

        tag(ModTags.Items.MUSCLE_REPLACEMENTS)
                .add(ModItems.WETWARE_RAVAGERTENDONS.get())
                .add(ModItems.BODYPART_SCULKMUSCLE.get())
                .add(ModItems.WETWARE_ELECTROCYTEMUSCLE.get())
                .add(ModItems.WETWARE_GOOEYMUSCLE.get())
                .add(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get());

        tag(ModTags.Items.BONE_REPLACEMENTS)
                .add(ModItems.BASECYBERWARE_LINEARFRAME.get())
                .add(ModItems.BONEUPGRADES_CAPACITORFRAME.get())
                .add(ModItems.WETWARE_BLASTEMASKELETON.get());

        tag(ModTags.Items.HEART_REPLACEMENTS)
                .add(ModItems.HEARTUPGRADES_CYBERHEART.get())
                .add(ModItems.WETWARE_SCULKHEART.get())
                .addOptional(resourceLocation("createcybernetics:heartupgrades_anomaly"));

        tag(ModTags.Items.LUNGS_REPLACEMENTS)
                .add(ModItems.LUNGSUPGRADES_SYNTHLUNGS.get());

        tag(ModTags.Items.LIVER_REPLACEMENTS)
                .add(ModItems.BODYPART_SCULKLIVER.get())
                .add(ModItems.ORGANSUPGRADES_LIVERFILTER.get());


        tag(ModTags.Items.LEFTARM_REPLACEMENTS)
                .add(ModItems.BASECYBERWARE_LEFTARM_COPPERPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTARM_IRONPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTARM_GOLDPLATED.get())
                .add(ModItems.WETWARE_WEBSHOOTING_LEFTARM.get())
                .add(ModItems.BODYPART_SCULKLEFTARM.get())
                .add(ModItems.BASECYBERWARE_LEFTARM.get());

        tag(ModTags.Items.LEFT_CYBERARM)
                .add(ModItems.BASECYBERWARE_LEFTARM_COPPERPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTARM_IRONPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTARM_GOLDPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTARM.get());

        tag(ModTags.Items.RIGHTARM_REPLACEMENTS)
                .add(ModItems.BASECYBERWARE_RIGHTARM_COPPERPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTARM_IRONPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTARM_GOLDPLATED.get())
                .add(ModItems.WETWARE_WEBSHOOTING_RIGHTARM.get())
                .add(ModItems.BODYPART_SCULKRIGHTARM.get())
                .add(ModItems.BASECYBERWARE_RIGHTARM.get());

        tag(ModTags.Items.RIGHT_CYBERARM)
                .add(ModItems.BASECYBERWARE_RIGHTARM_COPPERPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTARM_IRONPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTARM_GOLDPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTARM.get());

        tag(ModTags.Items.LEFTLEG_REPLACEMENTS)
                .add(ModItems.BASECYBERWARE_LEFTLEG_COPPERPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTLEG_IRONPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTLEG_GOLDPLATED.get())
                .add(ModItems.BODYPART_SCULKLEFTLEG.get())
                .add(ModItems.BASECYBERWARE_LEFTLEG.get());

        tag(ModTags.Items.LEFT_CYBERLEG)
                .add(ModItems.BASECYBERWARE_LEFTLEG_COPPERPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTLEG_IRONPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTLEG_GOLDPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTLEG.get());

        tag(ModTags.Items.RIGHTLEG_REPLACEMENTS)
                .add(ModItems.BASECYBERWARE_RIGHTLEG_COPPERPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTLEG_IRONPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTLEG_GOLDPLATED.get())
                .add(ModItems.BODYPART_SCULKRIGHTLEG.get())
                .add(ModItems.BASECYBERWARE_RIGHTLEG.get());

        tag(ModTags.Items.RIGHT_CYBERLEG)
                .add(ModItems.BASECYBERWARE_RIGHTLEG_COPPERPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTLEG_IRONPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTLEG_GOLDPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTLEG.get());


        tag(ModTags.Items.MEAT_LIMBS)
                .add(ModItems.BODYPART_LEFTLEG.get())
                .add(ModItems.BODYPART_LEFTARM.get())
                .add(ModItems.BODYPART_RIGHTLEG.get())
                .add(ModItems.BODYPART_RIGHTARM.get());

        tag(ModTags.Items.MEAT_LEGS)
                .add(ModItems.BODYPART_LEFTLEG.get())
                .add(ModItems.BODYPART_RIGHTLEG.get());

        tag(ModTags.Items.MEAT_ARMS)
                .add(ModItems.BODYPART_LEFTARM.get())
                .add(ModItems.BODYPART_RIGHTARM.get());


        tag(ModTags.Items.DEFAULTS_FAIL_AS_MISSING_WHEN_UNPOWERED)
                .add(ModItems.EYEUPGRADES_MONOVISION.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS1.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS2.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS3.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS4.get())
                .add(ModItems.BASECYBERWARE_CYBEREYES.get())
                .add(ModItems.HEARTUPGRADES_CYBERHEART.get());

        tag(ModTags.Items.CYBEREYE_VARIANTS)
                .add(ModItems.EYEUPGRADES_MONOVISION.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS1.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS2.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS3.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS4.get())
                .add(ModItems.BASECYBERWARE_CYBEREYES.get());




        tag(ModTags.Items.BATTERY_REPAIR_MATERIALS)
                .add(ModItems.COMPONENT_STORAGE.get())
                .add(Items.BLAZE_POWDER)
                .add(Items.REDSTONE);




// OFFAL
        tag(ModTags.Items.OFFAL)
                .add(ModItems.BODYPART_BRAIN.get())
                .add(ModItems.BODYPART_LUNGS.get())
                .add(ModItems.BODYPART_LIVER.get())
                .add(ModItems.BODYPART_INTESTINES.get())
                .add(ModItems.BODYPART_EYEBALLS.get())
                .add(ModItems.BODYPART_SKIN.get())
                .add(ModItems.BODYPART_MUSCLE.get());



//LAB LOOT: ORDINARY DATA SHARDS ONLY
        tag(LAB_STANDARD_DATA_SHARDS)
                .add(ModItems.DATA_SHARD_RED.get())
                .add(ModItems.DATA_SHARD_ORANGE.get())
                .add(ModItems.DATA_SHARD_YELLOW.get())
                .add(ModItems.DATA_SHARD_GREEN.get())
                .add(ModItems.DATA_SHARD_CYAN.get())
                .add(ModItems.DATA_SHARD_BLUE.get())
                .add(ModItems.DATA_SHARD_PURPLE.get())
                .add(ModItems.DATA_SHARD_PINK.get())
                .add(ModItems.DATA_SHARD_BROWN.get())
                .add(ModItems.DATA_SHARD_GRAY.get())
                .add(ModItems.DATA_SHARD_BLACK.get());

//LAB LOOT: FINISHED CYBERWARE WITH SCAVENGED COUNTERPARTS
        tag(LAB_FINISHED_CYBERWARE)
                .add(ModItems.BASECYBERWARE_RIGHTLEG.get())
                .add(ModItems.BASECYBERWARE_LEFTLEG.get())
                .add(ModItems.BASECYBERWARE_RIGHTARM.get())
                .add(ModItems.BASECYBERWARE_LEFTARM.get())
                .add(ModItems.BASECYBERWARE_CYBEREYES.get())
                .add(ModItems.BASECYBERWARE_LINEARFRAME.get())

                .add(ModItems.EYEUPGRADES_MONOVISION.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS1.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS2.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS3.get())
                .add(ModItems.EYEUPGRADES_MULTIOPTICS4.get())
                .add(ModItems.EYEUPGRADES_HUDLENS.get())
                .add(ModItems.EYEUPGRADES_HUDJACK.get())
                .add(ModItems.EYEUPGRADES_NIGHTVISION.get())
                .add(ModItems.EYEUPGRADES_TARGETING.get())
                .add(ModItems.EYEUPGRADES_UNDERWATERVISION.get())
                .add(ModItems.EYEUPGRADES_ZOOM.get())
                .add(ModItems.EYEUPGRADES_TRAJECTORYCALCULATOR.get())
                .add(ModItems.EYEUPGRADES_BIOMONITOR.get())

                .add(ModItems.ARMUPGRADES_ARMCANNON.get())
                .add(ModItems.ARMUPGRADES_FLYWHEEL.get())
                .add(ModItems.ARMUPGRADES_CLAWS.get())
                .add(ModItems.ARMUPGRADES_CRAFTHANDS.get())
                .add(ModItems.ARMUPGRADES_DRILLFIST.get())
                .add(ModItems.ARMUPGRADES_FIRESTARTER.get())
                .add(ModItems.ARMUPGRADES_PNEUMATICWRIST.get())
                .add(ModItems.ARMUPGRADES_REINFORCEDKNUCKLES.get())
                .add(ModItems.ARMUPGRADES_ARCCANNON.get())

                .add(ModItems.ARMUPGRADES_MANTISBLADE_IRON.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_COPPER.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_TITANIUM.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_GOLD.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_DIAMOND.get())
                .add(ModItems.ARMUPGRADES_MANTISBLADE_NETHERITE.get())

                .add(ModItems.LEGUPGRADES_METALDETECTOR.get())
                .add(ModItems.LEGUPGRADES_ANKLEBRACERS.get())
                .add(ModItems.LEGUPGRADES_JUMPBOOST.get())
                .add(ModItems.LEGUPGRADES_PROPELLERS.get())
                .add(ModItems.LEGUPGRADES_SPURS.get())
                .add(ModItems.LEGUPGRADES_OCELOTPAWS.get())

                .add(ModItems.BONEUPGRADES_BONEBATTERY.get())
                .add(ModItems.BONEUPGRADES_BONEFLEX.get())
                .add(ModItems.BONEUPGRADES_BONELACING.get())
                .add(ModItems.BONEUPGRADES_CAPACITORFRAME.get())
                .add(ModItems.BONEUPGRADES_PIEZO.get())
                .add(ModItems.BONEUPGRADES_SPINALINJECTOR.get())
                .add(ModItems.BONEUPGRADES_SANDEVISTAN.get())

                .add(ModItems.BRAINUPGRADES_EYEOFDEFENDER.get())
                .add(ModItems.BRAINUPGRADES_ENDERJAMMER.get())
                .add(ModItems.BRAINUPGRADES_MATRIX.get())
                .add(ModItems.BRAINUPGRADES_NEURALCONTEXTUALIZER.get())
                .add(ModItems.BRAINUPGRADES_CYBERDECK.get())
                .add(ModItems.BRAINUPGRADES_IDEM.get())
                .add(ModItems.BRAINUPGRADES_CHIPWARESLOTS.get())
                .add(ModItems.BRAINUPGRADES_NEURALPROCESSOR.get())
                .add(ModItems.BRAINUPGRADES_ICEPROTOCOL.get())

                .add(ModItems.HEARTUPGRADES_CYBERHEART.get())
                .add(ModItems.HEARTUPGRADES_COUPLER.get())
                .add(ModItems.HEARTUPGRADES_CREEPERHEART.get())
                .add(ModItems.HEARTUPGRADES_DEFIBRILLATOR.get())
                .add(ModItems.HEARTUPGRADES_STEMCELL.get())
                .add(ModItems.HEARTUPGRADES_PLATELETS.get())

                .add(ModItems.LUNGSUPGRADES_HYPEROXYGENATION.get())
                .add(ModItems.LUNGSUPGRADES_OXYGEN.get())

                .add(ModItems.ORGANSUPGRADES_ADRENALINE.get())
                .add(ModItems.ORGANSUPGRADES_BATTERY.get())
                .add(ModItems.ORGANSUPGRADES_DIAMONDWAFERSTACK.get())
                .add(ModItems.ORGANSUPGRADES_DUALISTICCONVERTER.get())
                .add(ModItems.ORGANSUPGRADES_LIVERFILTER.get())
                .add(ModItems.ORGANSUPGRADES_MAGICCATALYST.get())
                .add(ModItems.ORGANSUPGRADES_METABOLIC.get())
                .add(ModItems.ORGANSUPGRADES_DENSEBATTERY.get())
                .add(ModItems.ORGANSUPGRADES_CREATIVEBATTERY.get())
                .add(ModItems.ORGANSUPGRADES_HEATENGINE.get())
                .add(ModItems.ORGANSUPGRADES_OREGRINDER.get())

                .add(ModItems.SKINUPGRADES_ARTERIALTURBINE.get())
                .add(ModItems.SKINUPGRADES_CHROMATOPHORES.get())
                .add(ModItems.SKINUPGRADES_SYNTHSKIN.get())
                .add(ModItems.SKINUPGRADES_IMMUNO.get())
                .add(ModItems.SKINUPGRADES_FACEPLATE.get())
                .add(ModItems.SKINUPGRADES_NETHERITEPLATING.get())
                .add(ModItems.SKINUPGRADES_SOLARSKIN.get())
                .add(ModItems.SKINUPGRADES_SUBDERMALARMOR.get())
                .add(ModItems.SKINUPGRADES_SUBDERMALSPIKES.get())
                .add(ModItems.SKINUPGRADES_SYNTHETICSETULES.get())
                .add(ModItems.SKINUPGRADES_METALPLATING.get())

                .add(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE.get())
                .add(ModItems.MUSCLEUPGRADES_REINFORCEDTENDONS.get())
                .add(ModItems.MUSCLEUPGRADES_WIREDREFLEXES.get())
                .add(ModItems.MUSCLEUPGRADES_BALLISTICGEL.get());

//LAB LOOT: SCAVENGED ENERGY-GENERATING CYBERWARE ONLY
        tag(LAB_SCAVENGED_ENERGY_GENERATORS)
                .add(ModItems.SCAVENGED_PIEZO.get())
                .add(ModItems.SCAVENGED_DIAMONDWAFERSTACK.get())
                .add(ModItems.SCAVENGED_COUPLER.get())
                .add(ModItems.SCAVENGED_DUALISTICCONVERTER.get())
                .add(ModItems.SCAVENGED_METABOLIC.get())
                .add(ModItems.SCAVENGED_SOLARSKIN.get())
                .add(ModItems.SCAVENGED_ARTERIALTURBINE.get())
                .add(ModItems.SCAVENGED_HEATENGINE.get());


// CREATE
        tag(ModTags.Items.CRUSHED_RAW_MATERIALS)
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("create", "crushed_raw_materials"));
// VANILLA
        tag(Tags.Items.POTIONS)
                .add(ModItems.NEUROPOZYNE_AUTOINJECTOR.get())
                .add(ModItems.DYNAMIC_POTION_AUTOINJECTOR.get());

        tag(Tags.Items.NUGGETS)
                .add(ModItems.TITANIUMNUGGET.get());

        tag(Tags.Items.INGOTS)
                .add(ModItems.TITANIUMINGOT.get());

        tag(Tags.Items.MUSIC_DISCS)
                .add(ModItems.MUSIC_DISC_CYBERPSYCHO.get())
                .add(ModItems.MUSIC_DISC_THE_GRID.get())
                .add(ModItems.MUSIC_DISC_NEON_OVERLORDS.get())
                .add(ModItems.MUSIC_DISC_CYBERPSYCHO.get())

                .add(ModItems.MUSIC_DISC_DANGER_SNOW.get())
                .add(ModItems.MUSIC_DISC_DARK.get())
                .add(ModItems.MUSIC_DISC_MY_RETRO_JACKET.get())
                .add(ModItems.MUSIC_DISC_GAMEBOY_GIRL.get())
                .add(ModItems.MUSIC_DISC_NIGHTMARES_ON_HAUPTSTRASSE.get())
                .add(ModItems.MUSIC_DISC_INTERGALACTIC_WALK.get())
                .add(ModItems.MUSIC_DISC_SENSATION.get())
                .add(ModItems.MUSIC_DISC_SHOTGUN.get())
                .add(ModItems.MUSIC_DISC_DESCENT_INTO_MADNESS.get());

        tag(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.BASECYBERWARE_RIGHTLEG.get())
                .add(ModItems.BASECYBERWARE_LEFTLEG.get())
                .add(ModItems.BASECYBERWARE_RIGHTARM.get())
                .add(ModItems.BASECYBERWARE_LEFTARM.get())
                .add(ModItems.SKINUPGRADES_METALPLATING.get())
                .add(ModItems.BASECYBERWARE_RIGHTLEG_COPPERPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTLEG_COPPERPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTARM_COPPERPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTARM_COPPERPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTLEG_IRONPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTLEG_IRONPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTARM_IRONPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTARM_IRONPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTLEG_GOLDPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTLEG_GOLDPLATED.get())
                .add(ModItems.BASECYBERWARE_RIGHTARM_GOLDPLATED.get())
                .add(ModItems.BASECYBERWARE_LEFTARM_GOLDPLATED.get())
                .add(ModItems.ARMUPGRADES_ARCCANNON.get());

        tag(ModTags.Items.C_FOODS_RAW_MEATS)
                .add(ModItems.BODYPART_BRAIN.get())
                .add(ModItems.BODYPART_HEART.get())
                .add(ModItems.BODYPART_LUNGS.get())
                .add(ModItems.BODYPART_INTESTINES.get())
                .add(ModItems.BODYPART_EYEBALLS.get())
                .add(ModItems.BODYPART_MUSCLE.get())
                .add(ModItems.BODYPART_SKIN.get())
                .add(ModItems.BODYPART_LIVER.get());

        tag(vectorwing.farmersdelight.common.tag.ModTags.KNIVES)
                .add(ModItems.BONE_SAW.get());

        tag(ModTags.Items.C_TITANIUM)
                .add(ModItems.TITANIUMINGOT.get());

        tag(Tags.Items.RAW_MATERIALS)
                .add(ModItems.RAWTITANIUM.get());
    }
}
