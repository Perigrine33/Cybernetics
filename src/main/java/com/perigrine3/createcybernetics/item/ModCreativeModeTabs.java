package com.perigrine3.createcybernetics.item;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.block.ModBlocks;
import com.perigrine3.createcybernetics.compat.ModCompats;
import com.perigrine3.createcybernetics.compat.ironsspells.IronsSpellbooksCompat;
import com.perigrine3.createcybernetics.compat.ironsspells.IronsSpellbooksCompatBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateCybernetics.MODID);

    public static final Supplier<CreativeModeTab> CYBERNETICS_TAB = CREATIVE_MODE_TAB.register("create_cybernetics_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.TITANIUMINGOT.get()))
                    .title(Component.translatable("creativetab.createcybernetics.create_cybernetics_tab"))
                    .displayItems((itemDisplayParameters, output) -> {

                        output.accept(ModBlocks.ROBOSURGEON);
                        output.accept(ModBlocks.SURGERY_CHAMBER_BOTTOM);
                        output.accept(ModBlocks.SURGERY_TABLE);
                        output.accept(ModBlocks.ENGINEERING_TABLE);
                        output.accept(ModBlocks.GRAFTING_TABLE);
                        output.accept(ModBlocks.CHARGING_BLOCK);
                        output.accept(ModBlocks.COMPUTER);
                        output.accept(ModBlocks.COMPUTER_TOWER);
                        output.accept(ModBlocks.MACHINE_CASING);
                        output.accept(ModBlocks.HOLOPROJECTOR);
                        output.accept(ModItems.NEUROPOZYNE_AUTOINJECTOR);

                        output.accept(ModItems.EMPTY_AUTOINJECTOR);

                        if (IronsSpellbooksCompat.isLoaded()) {
                            output.accept(IronsSpellbooksCompatBlocks.HEXCORE_ITEM.get());
                            output.accept(IronsSpellbooksCompatBlocks.ANOMALY_CORE_ITEM.get());
                            output.accept(ModItems.ANOMALOUS_STAFF.get());
                        }

                        output.accept(ModItems.RAWTITANIUM);
                        output.accept(ModItems.CRUSHEDTITANIUM);
                        output.accept(ModItems.TITANIUMINGOT);
                        output.accept(ModItems.TITANIUMNUGGET);
                        output.accept(ModItems.TITANIUMSHEET);
                        output.accept(ModItems.EYEUPGRADEBASE);
                        output.accept(ModItems.TITANIUM_HAND);
                        output.accept(ModItems.GRAPHENE_ELASTOMER);
                        output.accept(ModItems.HOLOIMPRINT_CHIP);
                        output.accept(ModItems.FRONTAL_LOBE);
                        output.accept(ModItems.PARIETAL_LOBE);
                        output.accept(ModItems.TEMPORAL_LOBE);
                        output.accept(ModItems.OCCIPITAL_LOBE);
                        output.accept(ModItems.CEREBELLUM);
                        output.accept(ModItems.FACEPLATE);
                        output.accept(ModItems.EMP_GRENADE);
                        output.accept(ModItems.LINK_CABLE);
                        output.accept(ModItems.EXOSUIT1);

                        output.accept(ModItems.TAR);
                        output.accept(ModItems.UNREFINED_POLYMER);
                        output.accept(ModItems.PLASTIC_SHEET);

                        output.accept(ModItems.COPPER_UPGRADE_TEMPLATE);
                        output.accept(ModItems.IRON_UPGRADE_TEMPLATE);
                        output.accept(ModItems.GOLD_UPGRADE_TEMPLATE);

                        //COMPONENTS
                        output.accept(ModItems.COMPONENT_ACTUATOR);
                        output.accept(ModItems.COMPONENT_FIBEROPTICS);
                        output.accept(ModItems.COMPONENT_WIRING);
                        output.accept(ModItems.COMPONENT_DIODES);
                        output.accept(ModItems.COMPONENT_PLATING);
                        output.accept(ModItems.COMPONENT_GRAPHICSCARD);
                        output.accept(ModItems.COMPONENT_SSD);
                        output.accept(ModItems.COMPONENT_STORAGE);
                        output.accept(ModItems.COMPONENT_SYNTHNERVES);
                        output.accept(ModItems.COMPONENT_MESH);

                        if (ModItems.COMPONENT_LED != null && ModItems.COMPONENT_TITANIUMROD != null) {
                            output.accept(ModItems.COMPONENT_LED);
                            output.accept(ModItems.COMPONENT_TITANIUMROD);
                        }

                        output.accept(ModItems.NETHERITE_QPU);

                        //WETWARE PARTS
                        output.accept(ModItems.BODYPART_GUARDIANRETINA);
                        output.accept(ModItems.BODYPART_WARDENESOPHAGUS);
                        output.accept(ModItems.BODYPART_GYROSCOPICBLADDER);
                        output.accept(ModItems.BODYPART_SPINNERETTE);
                        output.accept(ModItems.BODYPART_FIREGLAND);
                        output.accept(ModItems.BODYPART_GILLS);
                        output.accept(ModItems.BODYPART_AXOLOTLMARROW);
                        output.accept(ModItems.BODYPART_DRAGONSCALE);

                        //SCAVENGED PARTS
                        output.accept(ModItems.SCAVENGED_RIGHTLEG);
                        output.accept(ModItems.SCAVENGED_LEFTLEG);
                        output.accept(ModItems.SCAVENGED_RIGHTARM);
                        output.accept(ModItems.SCAVENGED_LEFTARM);
                        output.accept(ModItems.SCAVENGED_LINEARFRAME);
                        output.accept(ModItems.SCAVENGED_CYBEREYES);
                        output.accept(ModItems.SCAVENGED_MONOVISION);
                        output.accept(ModItems.SCAVENGED_HUDLENS);
                        output.accept(ModItems.SCAVENGED_NAVIGATIONCHIP);
                        output.accept(ModItems.SCAVENGED_HUDJACK);
                        output.accept(ModItems.SCAVENGED_NIGHTVISION);
                        output.accept(ModItems.SCAVENGED_TARGETING);
                        output.accept(ModItems.SCAVENGED_UNDERWATERVISION);
                        output.accept(ModItems.SCAVENGED_ZOOM);
                        output.accept(ModItems.SCAVENGED_TRAJECTORYCALCULATOR);
                        output.accept(ModItems.SCAVENGED_BIOMONITOR);
                        output.accept(ModItems.SCAVENGED_ARMCANNON);
                        output.accept(ModItems.SCAVENGED_FLYWHEEL);
                        output.accept(ModItems.SCAVENGED_CLAWS);
                        output.accept(ModItems.SCAVENGED_CRAFTHANDS);
                        output.accept(ModItems.SCAVENGED_DRILLFIST);
                        output.accept(ModItems.SCAVENGED_FIRESTARTER);
                        output.accept(ModItems.SCAVENGED_PNEUMATICWRIST);
                        output.accept(ModItems.SCAVENGED_REINFORCEDKNUCKLES);
                        output.accept(ModItems.SCAVENGED_ARCCANNON);
                        output.accept(ModItems.SCAVENGED_MANTISBLADE_TITANIUM);
                        output.accept(ModItems.SCAVENGED_MANTISBLADE_IRON);
                        output.accept(ModItems.SCAVENGED_MANTISBLADE_COPPER);
                        output.accept(ModItems.SCAVENGED_MANTISBLADE_GOLD);
                        output.accept(ModItems.SCAVENGED_MANTISBLADE_DIAMOND);
                        output.accept(ModItems.SCAVENGED_MANTISBLADE_NETHERITE);
                        output.accept(ModItems.SCAVENGED_METALDETECTOR);
                        output.accept(ModItems.SCAVENGED_ANKLEBRACERS);
                        output.accept(ModItems.SCAVENGED_JUMPBOOST);
                        output.accept(ModItems.SCAVENGED_PROPELLERS);
                        output.accept(ModItems.SCAVENGED_SPURS);
                        output.accept(ModItems.SCAVENGED_OCELOTPAWS);
                        output.accept(ModItems.SCAVENGED_BONEBATTERY);
                        output.accept(ModItems.SCAVENGED_BONEFLEX);
                        output.accept(ModItems.SCAVENGED_BONELACING);
                        output.accept(ModItems.SCAVENGED_CAPACITORFRAME);
                        output.accept(ModItems.SCAVENGED_PIEZO);
                        output.accept(ModItems.SCAVENGED_SPINALINJECTOR);
                        output.accept(ModItems.SCAVENGED_SANDEVISTAN);
                        output.accept(ModItems.SCAVENGED_EYEOFDEFENDER);

                        if (ModItems.SCAVENGED_CONSCIOUSNESSTRANSMITTER != null && ModItems.SCAVENGED_CORTICALSTACK != null) {
                            output.accept(ModItems.SCAVENGED_CONSCIOUSNESSTRANSMITTER);
                            output.accept(ModItems.SCAVENGED_CORTICALSTACK);
                        }

                        if (ModItems.SCAVENGED_ELYTRA != null) {
                            output.accept(ModItems.SCAVENGED_ELYTRA);
                        }


                        output.accept(ModItems.SCAVENGED_ENDERJAMMER);
                        output.accept(ModItems.SCAVENGED_MATRIX);
                        output.accept(ModItems.SCAVENGED_NEURALCONTEXTUALIZER);
                        output.accept(ModItems.SCAVENGED_CYBERDECK);
                        output.accept(ModItems.SCAVENGED_NEURALPROCESSOR);
                        output.accept(ModItems.SCAVENGED_ICEPROTOCOL);
                        //output.accept(ModItems.SCAVENGED_COAXIALPORT);
                        output.accept(ModItems.SCAVENGED_KERENZIKOV);
                        output.accept(ModItems.SCAVENGED_BERSERK);
                        output.accept(ModItems.SCAVENGED_RAMDECK);
                        output.accept(ModItems.SCAVENGED_IDEM);
                        output.accept(ModItems.SCAVENGED_CHIPWARESLOTS);
                        output.accept(ModItems.SCAVENGED_CYBERHEART);
                        output.accept(ModItems.SCAVENGED_COUPLER);
                        output.accept(ModItems.SCAVENGED_CREEPERHEART);
                        output.accept(ModItems.SCAVENGED_DEFIBRILLATOR);
                        output.accept(ModItems.SCAVENGED_STEMCELL);
                        output.accept(ModItems.SCAVENGED_PLATELETS);
                        output.accept(ModItems.SCAVENGED_HYPEROXYGENATION);
                        output.accept(ModItems.SCAVENGED_OXYGEN);
                        output.accept(ModItems.SCAVENGED_SYNTHLUNGS);
                        output.accept(ModItems.SCAVENGED_VAMPYRES);
                        output.accept(ModItems.SCAVENGED_ADRENALINE);
                        output.accept(ModItems.SCAVENGED_BATTERY);
                        output.accept(ModItems.SCAVENGED_DIAMONDWAFERSTACK);
                        output.accept(ModItems.SCAVENGED_DUALISTICCONVERTER);
                        output.accept(ModItems.SCAVENGED_LIVERFILTER);
                        output.accept(ModItems.SCAVENGED_METABOLIC);
                        output.accept(ModItems.SCAVENGED_DENSEBATTERY);
                        output.accept(ModItems.SCAVENGED_HEATENGINE);
                        output.accept(ModItems.SCAVENGED_OREGRINDER);
                        output.accept(ModItems.SCAVENGED_ARTERIALTURBINE);
                        output.accept(ModItems.SCAVENGED_CHROMATOPHORES);
                        output.accept(ModItems.SCAVENGED_SYNTHSKIN);
                        output.accept(ModItems.SCAVENGED_IMMUNO);
                        output.accept(ModItems.SCAVENGED_FACEPLATE);
                        output.accept(ModItems.SCAVENGED_NETHERITEPLATING);
                        output.accept(ModItems.SCAVENGED_SOLARSKIN);
                        output.accept(ModItems.SCAVENGED_SUBDERMALARMOR);
                        output.accept(ModItems.SCAVENGED_SUBDERMALSPIKES);
                        output.accept(ModItems.SCAVENGED_SYNTHETICSETULES);
                        output.accept(ModItems.SCAVENGED_METALPLATING);

                        if (ModItems.SCAVENGED_SWEAT != null) {
                            output.accept(ModItems.SCAVENGED_SWEAT);
                        }
                        if (ModItems.SCAVENGED_MANABATTERY != null && ModItems.SCAVENGED_MANASKIN != null && ModItems.SCAVENGED_SPELLJAMMER != null) {
                            output.accept(ModItems.SCAVENGED_MANABATTERY);
                            output.accept(ModItems.SCAVENGED_MANASKIN);
                            output.accept(ModItems.SCAVENGED_SPELLJAMMER);
                        }

                        output.accept(ModItems.SCAVENGED_SYNTHMUSCLE);
                        output.accept(ModItems.SCAVENGED_REINFORCEDTENDONS);
                        output.accept(ModItems.SCAVENGED_WIREDREFLEXES);
                        output.accept(ModItems.SCAVENGED_BALLISTICGEL);


                    }).build());

    public static final Supplier<CreativeModeTab> CYBERNETICS_UPGRADES_TAB = CREATIVE_MODE_TAB.register("create_cybernetics_upgrades_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BASECYBERWARE_CYBEREYES.get())).withSearchBar()
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "create_cybernetics_tab"))
                    .title(Component.translatable("creativetab.createcybernetics.create_cybernetics_upgrades_tab"))
                    .displayItems((itemDisplayParameters, output) -> {

                    //LIMBS
                        output.accept(ModItems.BODYPART_RIGHTARM);
                        output.accept(ModItems.BODYPART_LEFTARM);
                        output.accept(ModItems.BODYPART_RIGHTLEG);
                        output.accept(ModItems.BODYPART_LEFTLEG);


                    //INTERNAL WETWARE
                        output.accept(ModItems.BODYPART_SKELETON);
                        output.accept(ModItems.BODYPART_BRAIN);
                        output.accept(ModItems.BODYPART_EYEBALLS);
                        output.accept(ModItems.BODYPART_HEART);
                        output.accept(ModItems.BODYPART_LUNGS);
                        output.accept(ModItems.BODYPART_LIVER);
                        output.accept(ModItems.BODYPART_INTESTINES);
                        output.accept(ModItems.BODYPART_MUSCLE);
                        output.accept(ModItems.BODYPART_SKIN);
                    //BASE CYBERWARE
                        output.accept(ModItems.BASECYBERWARE_RIGHTARM);
                        output.accept(ModItems.BASECYBERWARE_LEFTARM);
                        output.accept(ModItems.BASECYBERWARE_RIGHTLEG);
                        output.accept(ModItems.BASECYBERWARE_LEFTLEG);
                        output.accept(ModItems.BASECYBERWARE_LINEARFRAME);
                        output.accept(ModItems.BASECYBERWARE_CYBEREYES);
                    //EYE UPGRADES
                        output.accept(ModItems.EYEUPGRADES_MONOVISION);
                        output.accept(ModItems.EYEUPGRADES_MULTIOPTICS1);
                        output.accept(ModItems.EYEUPGRADES_MULTIOPTICS2);
                        output.accept(ModItems.EYEUPGRADES_MULTIOPTICS3);
                        output.accept(ModItems.EYEUPGRADES_MULTIOPTICS4);
                        output.accept(ModItems.EYEUPGRADES_HUDLENS);
                        output.accept(ModItems.EYEUPGRADES_HUDJACK);
                        output.accept(ModItems.EYEUPGRADES_NIGHTVISION);
                        output.accept(ModItems.EYEUPGRADES_UNDERWATERVISION);
                        output.accept(ModItems.EYEUPGRADES_TARGETING);
                        output.accept(ModItems.EYEUPGRADES_ZOOM);
                        output.accept(ModItems.EYEUPGRADES_TRAJECTORYCALCULATOR);
                        output.accept(ModItems.EYEUPGRADES_NAVIGATIONCHIP);
                        output.accept(ModItems.EYEUPGRADES_BIOMONITOR);
                    //ARM UPGRADES
                        output.accept(ModItems.ARMUPGRADES_ARMCANNON);
                        output.accept(ModItems.ARMUPGRADES_CLAWS);
                        output.accept(ModItems.ARMUPGRADES_CRAFTHANDS);
                        output.accept(ModItems.ARMUPGRADES_DRILLFIST);
                        output.accept(ModItems.ARMUPGRADES_FLYWHEEL);
                        output.accept(ModItems.ARMUPGRADES_FIRESTARTER);
                        output.accept(ModItems.ARMUPGRADES_PNEUMATICWRIST);
                        output.accept(ModItems.ARMUPGRADES_REINFORCEDKNUCKLES);
                        output.accept(ModItems.ARMUPGRADES_RIPPERCLAW);
                        output.accept(ModItems.ARMUPGRADES_ARCCANNON);
                        output.accept(ModItems.ARMUPGRADES_MANTISBLADE_TITANIUM);
                        output.accept(ModItems.ARMUPGRADES_MANTISBLADE_IRON);
                        output.accept(ModItems.ARMUPGRADES_MANTISBLADE_COPPER);
                        output.accept(ModItems.ARMUPGRADES_MANTISBLADE_GOLD);
                        output.accept(ModItems.ARMUPGRADES_MANTISBLADE_DIAMOND);
                        output.accept(ModItems.ARMUPGRADES_MANTISBLADE_NETHERITE);
                        output.accept(ModItems.ARMUPGRADES_HEAVYKNUCKLES);
                    //LEG UPGRADES
                        output.accept(ModItems.LEGUPGRADES_METALDETECTOR);
                        output.accept(ModItems.LEGUPGRADES_ANKLEBRACERS);
                        output.accept(ModItems.LEGUPGRADES_JUMPBOOST);
                        output.accept(ModItems.LEGUPGRADES_PROPELLERS);
                        output.accept(ModItems.LEGUPGRADES_SPURS);
                        output.accept(ModItems.LEGUPGRADES_OCELOTPAWS);
                    //BONE UPGRADES
                        output.accept(ModItems.BONEUPGRADES_BONEBATTERY);
                        output.accept(ModItems.BONEUPGRADES_BONEFLEX);
                        output.accept(ModItems.BONEUPGRADES_BONELACING);
                        output.accept(ModItems.BONEUPGRADES_CAPACITORFRAME);
                        output.accept(ModItems.BONEUPGRADES_PIEZO);
                        output.accept(ModItems.BONEUPGRADES_SPINALINJECTOR);
                        output.accept(ModItems.BONEUPGRADES_SANDEVISTAN);
                        output.accept(ModItems.BONEUPGRADES_CYBERSKULL);
                    //BRAIN UPGRADES
                        output.accept(ModItems.BRAINUPGRADES_CYBERBRAIN);
                        output.accept(ModItems.BRAINUPGRADES_EYEOFDEFENDER);

                        if (ModItems.BRAINUPGRADES_CONSCIOUSNESSTRANSMITTER != null && ModItems.BRAINUPGRADES_CORTICALSTACK != null) {
                            output.accept(ModItems.BRAINUPGRADES_CONSCIOUSNESSTRANSMITTER);
                            output.accept(ModItems.BRAINUPGRADES_CORTICALSTACK);
                        }

                        if (ModItems.BONEUPGRADES_ELYTRA != null) {
                            output.accept(ModItems.BONEUPGRADES_ELYTRA);
                        }

                        output.accept(ModItems.BRAINUPGRADES_ENDERJAMMER);
                        output.accept(ModItems.BRAINUPGRADES_MATRIX);
                        output.accept(ModItems.BRAINUPGRADES_NEURALCONTEXTUALIZER);
                        output.accept(ModItems.BRAINUPGRADES_CYBERDECK);
                        output.accept(ModItems.BRAINUPGRADES_IDEM);
                        output.accept(ModItems.BRAINUPGRADES_CHIPWARESLOTS);
                        output.accept(ModItems.BRAINUPGRADES_NEURALPROCESSOR);
                        output.accept(ModItems.BRAINUPGRADES_ICEPROTOCOL);
                        output.accept(ModItems.BRAINUPGRADES_COAXIALPORT);
                        output.accept(ModItems.BRAINUPGRADES_KERENZIKOV);
                        output.accept(ModItems.BRAINUPGRADES_BERSERK);
                        output.accept(ModItems.BRAINUPGRADES_RAMDECK);

                        if (ModItems.BRAINUPGRADES_SPELLJAMMER != null) {
                            output.accept(ModItems.BRAINUPGRADES_SPELLJAMMER);
                        }
                    //HEART UPGRADES
                        output.accept(ModItems.HEARTUPGRADES_CYBERHEART);
                        output.accept(ModItems.HEARTUPGRADES_COUPLER);
                        output.accept(ModItems.HEARTUPGRADES_CREEPERHEART);
                        output.accept(ModItems.HEARTUPGRADES_DEFIBRILLATOR);
                        output.accept(ModItems.HEARTUPGRADES_STEMCELL);
                        output.accept(ModItems.HEARTUPGRADES_PLATELETS);

                        if (ModItems.HEARTUPGRADES_ANOMALY != null) {
                            output.accept(ModItems.HEARTUPGRADES_ANOMALY);
                        }
                    //LUNG UPGRADES
                        output.accept(ModItems.LUNGSUPGRADES_HYPEROXYGENATION);
                        output.accept(ModItems.LUNGSUPGRADES_OXYGEN);
                        output.accept(ModItems.LUNGSUPGRADES_SYNTHLUNGS);
                        output.accept(ModItems.LUNGSUPGRADES_VAMPYRES);
                    //ORGAN UPGRADES
                        output.accept(ModItems.ORGANSUPGRADES_ADRENALINE);
                        output.accept(ModItems.ORGANSUPGRADES_BATTERY);
                        output.accept(ModItems.ORGANSUPGRADES_DIAMONDWAFERSTACK);
                        output.accept(ModItems.ORGANSUPGRADES_DUALISTICCONVERTER);
                        output.accept(ModItems.ORGANSUPGRADES_LIVERFILTER);
                        output.accept(ModItems.ORGANSUPGRADES_MAGICCATALYST);
                        output.accept(ModItems.ORGANSUPGRADES_METABOLIC);
                        output.accept(ModItems.ORGANSUPGRADES_DENSEBATTERY);
                        output.accept(ModItems.ORGANSUPGRADES_CREATIVEBATTERY);
                        output.accept(ModItems.ORGANSUPGRADES_HEATENGINE);
                        output.accept(ModItems.ORGANSUPGRADES_OREGRINDER);

                        if (ModItems.ORGANSUPGRADES_MANABATTERY != null) {
                            output.accept(ModItems.ORGANSUPGRADES_MANABATTERY);
                        }
                    //SKIN UPGRADES
                        output.accept(ModItems.SKINUPGRADES_ARTERIALTURBINE);
                        output.accept(ModItems.SKINUPGRADES_CHROMATOPHORES);
                        output.accept(ModItems.SKINUPGRADES_SYNTHSKIN);
                        output.accept(ModItems.SKINUPGRADES_IMMUNO);
                        output.accept(ModItems.SKINUPGRADES_FACEPLATE);
                        output.accept(ModItems.SKINUPGRADES_NETHERITEPLATING);
                        output.accept(ModItems.SKINUPGRADES_SOLARSKIN);
                        output.accept(ModItems.SKINUPGRADES_SUBDERMALARMOR);
                        output.accept(ModItems.SKINUPGRADES_SUBDERMALSPIKES);
                        output.accept(ModItems.SKINUPGRADES_SYNTHETICSETULES);
                        output.accept(ModItems.SKINUPGRADES_METALPLATING);
                        output.accept(ModItems.SKINUPGRADES_EMPTHREADING);

                        if (ModItems.SKINUPGRADES_SWEAT != null) {
                            output.accept(ModItems.SKINUPGRADES_SWEAT);
                        }
                        if (ModItems.SKINUPGRADES_MANASKIN != null) {
                            output.accept(ModItems.SKINUPGRADES_MANASKIN);
                        }
                        if (ModItems.SKINUPGRADES_ULTRAVIOLENT != null) {
                            output.accept(ModItems.SKINUPGRADES_ULTRAVIOLENT);
                        }

                    //MUSCLE UPGRADES
                        output.accept(ModItems.MUSCLEUPGRADES_SYNTHMUSCLE);
                        output.accept(ModItems.MUSCLEUPGRADES_REINFORCEDTENDONS);
                        output.accept(ModItems.MUSCLEUPGRADES_WIREDREFLEXES);
                        output.accept(ModItems.MUSCLEUPGRADES_BALLISTICGEL);
                    //WETWARE UPGRADES
                        if (ModItems.WETWARE_BLUBBER != null) {
                            output.accept(ModItems.WETWARE_BLUBBER);
                        }

                        output.accept(ModItems.WETWARE_FIREBREATHINGLUNGS);
                        output.accept(ModItems.WETWARE_DRAGONSKIN);
                        output.accept(ModItems.WETWARE_WATERBREATHINGLUNGS);
                        output.accept(ModItems.WETWARE_GUARDIANEYE);
                        output.accept(ModItems.WETWARE_POLARBEARFUR);
                        output.accept(ModItems.WETWARE_RAVAGERTENDONS);
                        output.accept(ModItems.WETWARE_GOOEYMUSCLE);
                        output.accept(ModItems.WETWARE_ELECTROCYTEMUSCLE);
                        output.accept(ModItems.WETWARE_SCULKLUNGS);
                        output.accept(ModItems.WETWARE_WARDENANTLERS);
                        output.accept(ModItems.WETWARE_SCULKHEART);
                        output.accept(ModItems.WETWARE_TACTICALINKSAC);
                        output.accept(ModItems.WETWARE_AEROSTASISGYROBLADDER);
                        output.accept(ModItems.WETWARE_GRASSFEDSTOMACH);
                        output.accept(ModItems.WETWARE_WEBSHOOTING_RIGHTARM);
                        output.accept(ModItems.WETWARE_WEBSHOOTING_LEFTARM);
                        output.accept(ModItems.WETWARE_WEBSHOOTINGINTESTINES);
                        output.accept(ModItems.WETWARE_SPIDEREYES);
                        output.accept(ModItems.WETWARE_BLASTEMASKELETON);

                        output.accept(ModItems.BASECYBERWARE_RIGHTLEG_COPPERPLATED);
                        output.accept(ModItems.BASECYBERWARE_LEFTLEG_COPPERPLATED);
                        output.accept(ModItems.BASECYBERWARE_RIGHTARM_COPPERPLATED);
                        output.accept(ModItems.BASECYBERWARE_LEFTARM_COPPERPLATED);

                        output.accept(ModItems.BASECYBERWARE_RIGHTLEG_IRONPLATED);
                        output.accept(ModItems.BASECYBERWARE_LEFTLEG_IRONPLATED);
                        output.accept(ModItems.BASECYBERWARE_RIGHTARM_IRONPLATED);
                        output.accept(ModItems.BASECYBERWARE_LEFTARM_IRONPLATED);

                        output.accept(ModItems.BASECYBERWARE_RIGHTLEG_GOLDPLATED);
                        output.accept(ModItems.BASECYBERWARE_LEFTLEG_GOLDPLATED);
                        output.accept(ModItems.BASECYBERWARE_RIGHTARM_GOLDPLATED);
                        output.accept(ModItems.BASECYBERWARE_LEFTARM_GOLDPLATED);



                    }).build());

    public static final Supplier<CreativeModeTab> CYBERNETICS_DATASHARDS_TAB = CREATIVE_MODE_TAB.register("create_cybernetics_datashards_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.DATA_SHARD_BIOCHIP.get())).withSearchBar()
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "create_cybernetics_tab"))
                    .title(Component.translatable("creativetab.createcybernetics.create_cybernetics_datashards_tab"))
                    .displayItems((itemDisplayParameters, output) -> {

                        //SPECIAL SHARDS
                        output.accept(ModItems.DATA_SHARD_BIOCHIP);
                        output.accept(ModItems.DATA_SHARD_SHARED_NAVIGATION.get());
                        output.accept(ModItems.DATA_SHARD_INFOLOG);

                        //DATA SHARDS
                        output.accept(ModItems.DATA_SHARD_RED);
                        output.accept(ModItems.DATA_SHARD_ORANGE);
                        output.accept(ModItems.DATA_SHARD_YELLOW);
                        output.accept(ModItems.DATA_SHARD_GREEN);
                        output.accept(ModItems.DATA_SHARD_CYAN);
                        output.accept(ModItems.DATA_SHARD_BLUE);
                        output.accept(ModItems.DATA_SHARD_PURPLE);
                        output.accept(ModItems.DATA_SHARD_PINK);
                        output.accept(ModItems.DATA_SHARD_BROWN);
                        output.accept(ModItems.DATA_SHARD_GRAY);
                        output.accept(ModItems.DATA_SHARD_BLACK);

                        //TUTORIAL SHARDS
                        output.accept(ModItems.DATA_SHARD_INFOLOG_GETTING_STARTED);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_ROBOSURGEON_MANUAL);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_SURGERY_TABLE_MANUAL);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_FBC_GUIDE);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_CYBERBESTIARY);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_CYBERDECK);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_HARVESTER_ENCHANTMENT);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_QUICKHACK_TUTORIAL);
                        if (ModCompats.isInstalled("cyberchems")) {
                            output.accept(ModItems.DATA_SHARD_INFOLOG_CYBERCHEMS);
                        }

                        //LORE SHARDS
                        output.accept(ModItems.DATA_SHARD_INFOLOG_SUNSET_SHIFT);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_HOUSE_RULES);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_DENSE_BATTERY_AD);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_IMMUNOSUPPRESSOR_ADVISORY);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_CHROMATOPHORE_FIELD_NOTE);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_THE_RAISE);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_OMNISCIENT_OPTICS_AD);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_PRIVATE_WARD_REPORT);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_DRILLFIST_AD);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_IDEM_ARTICLE);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_MOM_IM_FINE);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_CYBERDECK_SECURITY_BRIEF);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_LUNCH_BREAK);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_DATURA);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_WAIT_YOUR_TURN);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_THE_BREACH);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_AI_CHAT);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_CYBERPSYCHO_TRANSCRIPT);
                        output.accept(ModItems.DATA_SHARD_INFOLOG_CPU_INSTALL);

                        //GAME SHARDS
                        output.accept(ModItems.DATA_SHARD_GAME_MINESWEEPER);
                        output.accept(ModItems.DATA_SHARD_GAME_CHESS);

                        //MICROPROGRAM SHARDS
                        output.accept(ModItems.DATA_SHARD_MICROPROGRAMS);
                        output.accept(ModItems.DATA_SHARD_MICROPROGRAMS_CREATIVE);

                        //QUICKHACKS
                        output.accept(ModItems.QUICKHACK_SHARD);

                    }).build());

    public static final Supplier<CreativeModeTab> CYBERNETICS_CYBERSPACE_TAB = CREATIVE_MODE_TAB.register("create_cybernetics_cyberspace_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.ACCESS_NODE.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "create_cybernetics_tab"))
                    .title(Component.translatable("creativetab.createcybernetics.create_cybernetics_cyberspace_tab"))
                    .displayItems((itemDisplayParameters, output) -> {

                        //CYBERSPACE BLOCKS
                        output.accept(ModBlocks.DIGITAL_ARCHITECTURE);
                        output.accept(ModBlocks.ACCESS_NODE);
                        output.accept(ModBlocks.DATA_CACHE);
                        output.accept(ModBlocks.DAEMON_ANCHOR);
                        output.accept(ModBlocks.DIGITAL_GATE);

                        //CODE FRAGMENTS
                        output.accept(ModBlocks.TARGET_CODE_FRAGMENT);
                        output.accept(ModBlocks.FUNCTION_CODE_FRAGMENT);
                        output.accept(ModBlocks.MODIFIER_CODE_FRAGMENT);
                        output.accept(ModBlocks.RAW_DATA_FRAGMENT);

// TARGET MICROPROGRAMS
                        output.accept(ModItems.MICROPROGRAM_SYSTEM_ALL);
                        output.accept(ModItems.MICROPROGRAM_PROCESSOR);
                        output.accept(ModItems.MICROPROGRAM_CYBEROPTICS);
                        output.accept(ModItems.MICROPROGRAM_AUDITORY);
                        output.accept(ModItems.MICROPROGRAM_MOTOR_CONTROL);
                        output.accept(ModItems.MICROPROGRAM_ARMS);
                        output.accept(ModItems.MICROPROGRAM_LEGS);
                        output.accept(ModItems.MICROPROGRAM_INTERNAL_ORGANS);
                        output.accept(ModItems.MICROPROGRAM_SKELETON);
                        output.accept(ModItems.MICROPROGRAM_DERMAL);
                        output.accept(ModItems.MICROPROGRAM_POWER_SYSTEM);
                        output.accept(ModItems.MICROPROGRAM_CYBERDECK);
                        output.accept(ModItems.MICROPROGRAM_NEURAL_SYSTEM);
                        output.accept(ModItems.MICROPROGRAM_ACTIVE_CYBERWARE);
                        output.accept(ModItems.MICROPROGRAM_HIGHEST_DRAW);
                        output.accept(ModItems.MICROPROGRAM_RANDOM_IMPLANT);

// FUNCTION MICROPROGRAMS
                        output.accept(ModItems.MICROPROGRAM_REBOOT);
                        output.accept(ModItems.MICROPROGRAM_OVERCLOCK);
                        output.accept(ModItems.MICROPROGRAM_DRAIN);
                        output.accept(ModItems.MICROPROGRAM_SPIKE);
                        output.accept(ModItems.MICROPROGRAM_BURNOUT);
                        output.accept(ModItems.MICROPROGRAM_TOGGLE_OFF);
                        output.accept(ModItems.MICROPROGRAM_TOGGLE_ON);
                        output.accept(ModItems.MICROPROGRAM_MALFUNCTION);
                        output.accept(ModItems.MICROPROGRAM_LOOP);
                        output.accept(ModItems.MICROPROGRAM_PING);
                        output.accept(ModItems.MICROPROGRAM_TRACE);
                        output.accept(ModItems.MICROPROGRAM_CORRUPT);
                        output.accept(ModItems.MICROPROGRAM_PURGE);
                        output.accept(ModItems.MICROPROGRAM_DESYNC);

// MODIFIER MICROPROGRAMS
                        output.accept(ModItems.MICROPROGRAM_DURATION_PLUS);
                        output.accept(ModItems.MICROPROGRAM_DURATION_PLUS_PLUS);
                        output.accept(ModItems.MICROPROGRAM_INTENSITY_PLUS);
                        output.accept(ModItems.MICROPROGRAM_EFFICIENCY);
                        output.accept(ModItems.MICROPROGRAM_DELAY);
                        output.accept(ModItems.MICROPROGRAM_REPEAT);
                        output.accept(ModItems.MICROPROGRAM_CASCADE);
                        output.accept(ModItems.MICROPROGRAM_SILENT);
                        output.accept(ModItems.MICROPROGRAM_PERSISTENT);
                        output.accept(ModItems.MICROPROGRAM_RANDOMIZE);
                        output.accept(ModItems.MICROPROGRAM_CONTAGION);

//MACROPROGRAMS
                        output.accept(ModItems.MACROPROGRAM_DATA_MINER);

                        output.accept(ModItems.MACROPROGRAM_BIT_CRUSHER);
                        output.accept(ModItems.MACROPROGRAM_STACK_OVERFLOW);
                        output.accept(ModItems.MACROPROGRAM_ICEBREAKER);
                        output.accept(ModItems.MACROPROGRAM_PING);
                        output.accept(ModItems.MACROPROGRAM_DDOS);
                        output.accept(ModItems.MACROPROGRAM_BANHAMMER);

                        output.accept(ModItems.MACROPROGRAM_FIREWALL);
                        output.accept(ModItems.MACROPROGRAM_RECOVERY);

                        output.accept(ModItems.MACROPROGRAM_BASIC_ARMOR);
                        output.accept(ModItems.MACROPROGRAM_INTERMEDIATE_ARMOR);
                        output.accept(ModItems.MACROPROGRAM_ADVANCED_ARMOR);
                        output.accept(ModItems.MACROPROGRAM_ADMINISTRATOR_ACCESS_ARMOR);

                        output.accept(ModItems.MACROPROGRAM_PACKET_SNIFFER);
                        output.accept(ModItems.MACROPROGRAM_VPN_SHROUD);
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
