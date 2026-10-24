package com.perigrine3.createcybernetics;

import com.perigrine3.createcybernetics.api.CyberwareDurabilityMode;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private Config() {}

    public static final ModConfigSpec.IntValue HUMANITY = BUILDER
            .comment("Base Humanity Value")
            .defineInRange("humanity", 100, 50, 1000);

    public static final ModConfigSpec.BooleanValue KEEP_CYBERWARE = BUILDER
            .comment("Keep Cyberware on Death")
            .comment("If true, cyberware will not drop and will persist through death.")
            .define("keepCyberware", false);

    public static final ModConfigSpec.BooleanValue SURGERY_DAMAGE_SCALING = BUILDER
            .comment("Scale Surgery Damage")
            .comment("If true, the surgery chamber will apply damage that scales with the amount of cyberware being removed.")
            .comment("If false, the surgery chamber will apply 10 damage.")
            .define("scaleSurgeryDamage", false);

    public static final ModConfigSpec.BooleanValue EPILEPSY_MODE = BUILDER
            .comment("Epilepsy Mode")
            .comment("Activate this to disable the cyberware rejection overlay. Good if you or a friend has epilepsy.")
            .define("epilepsyMode", false);



    public static final ModConfigSpec.EnumValue<CyberwareDurabilityMode> CYBERWARE_DURABILITY_MODE = BUILDER
            .comment("Controls durability for installed organs and cyberware.")
            .comment("ENABLED: Durability applies to default organs, wetware, and cybernetics.")
            .comment("ONLY_IMPLANTS: Durability applies to wetware and cybernetics, but not default organs.")
            .comment("ONLY_CYBERNETICS: Durability applies only to cybernetics.")
            .comment("DISABLED: Durability is disabled.")
            .defineEnum("cyberwareDurabilityMode", CyberwareDurabilityMode.ENABLED);

    public static final ModConfigSpec.BooleanValue CRITICAL_DURABILITY_DEBUFFS = BUILDER
            .comment("Controls critical durability effects for damaged organs and organ-replacement implants.")
            .comment("When enabled, critically damaged organs may cause effects such as blindness, weakness, slowness, nausea, poison, reduced saturation, and prolonged harmful effects.")
            .comment("When disabled, organ durability still functions, but critical durability debuffs are not applied.")
            .define("criticalDurabilityDebuffs", true);

    public static final ModConfigSpec.DoubleValue DURABILITY_DAMAGE_SCALE = BUILDER
            .comment("Base installed-component durability damage for each point of health damage received.")
            .defineInRange("durabilityDamageScale", 1.0D, 0.0D, 100.0D);

    public static final ModConfigSpec.IntValue FOOD_DURABILITY_REPAIR_PER_NUTRITION = BUILDER
            .comment("Biological durability restored per point of food nutrition before natural repair fatigue.")
            .defineInRange("foodDurabilityRepairPerNutrition", 4, 0, 1000);

    public static final ModConfigSpec.IntValue FOOD_REPAIR_FATIGUE_PER_NUTRITION = BUILDER
            .comment("Natural repair fatigue gained per point of food nutrition after biological healing.")
            .defineInRange("foodRepairFatiguePerNutrition", 1, 0, 100);

    public static final ModConfigSpec.IntValue ANVIL_REPAIR_LOW = BUILDER
            .comment("Durability restored by a low-value anvil repair material.")
            .defineInRange("anvilRepairLow", 100, 0, 1000);

    public static final ModConfigSpec.IntValue ANVIL_REPAIR_MODERATE = BUILDER
            .comment("Durability restored by a moderate-value anvil repair material.")
            .defineInRange("anvilRepairModerate", 250, 0, 1000);

    public static final ModConfigSpec.IntValue ANVIL_REPAIR_HIGH = BUILDER
            .comment("Durability restored by a high-value anvil repair material.")
            .defineInRange("anvilRepairHigh", 500, 0, 1000);

    public static final ModConfigSpec.DoubleValue MINIMUM_FOOD_REPAIR_EFFICIENCY = BUILDER
            .comment("Minimum percentage of normal food durability repair after repeated healing.")
            .defineInRange("minimumFoodRepairEfficiency", 0.10D, 0.0D, 1.0D);

    public static final ModConfigSpec.IntValue REGENERATION_DURABILITY_REPAIR = BUILDER
            .comment("Biological durability restored each second per regeneration-effect level.")
            .defineInRange("regenerationDurabilityRepair", 4, 0, 1000);

    public static final ModConfigSpec.IntValue REGENERATION_FATIGUE_RECOVERY = BUILDER
            .comment("Natural repair fatigue removed each second per regeneration-effect level.")
            .defineInRange("regenerationFatigueRecovery", 1, 0, 100);

    public static final ModConfigSpec.IntValue BATTERY_ENERGY_RECEIVED_PER_DAMAGE = BUILDER
            .comment("Energy a battery may receive before losing one durability.")
            .comment("Set to 0 to disable battery degradation from receiving energy.")
            .defineInRange("batteryEnergyReceivedPerDamage", 5000, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue BATTERY_ENERGY_EXTRACTED_PER_DAMAGE = BUILDER
            .comment("Energy a battery may provide before losing one durability.")
            .comment("Set to 0 to disable battery degradation from providing energy.")
            .defineInRange("batteryEnergyExtractedPerDamage", 2500, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue BATTERY_PASSIVE_DAMAGE = BUILDER
            .comment("Battery durability lost each passive-aging interval.")
            .defineInRange("batteryPassiveDamage", 1, 0, 1000);

    public static final ModConfigSpec.IntValue BATTERY_PASSIVE_DAMAGE_INTERVAL = BUILDER
            .comment("Ticks between passive battery-aging damage.")
            .comment("24000 ticks is one Minecraft day.")
            .defineInRange("batteryPassiveDamageInterval", 24000, 20, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue BATTERY_EMP_DAMAGE_PER_SECOND = BUILDER
            .comment("Battery durability lost each second while affected by EMP.")
            .defineInRange("batteryEmpDamagePerSecond", 10, 0, 10000);

    public static final ModConfigSpec.IntValue TITANIUM_SHEET_REPAIR = BUILDER
            .comment("Cyberlimb durability restored by a titanium sheet.")
            .defineInRange("titaniumSheetRepair", 100, 0, 100000);

    public static final ModConfigSpec.IntValue TITANIUM_INGOT_REPAIR = BUILDER
            .comment("Cyberlimb durability restored by a titanium ingot.")
            .defineInRange("titaniumIngotRepair", 250, 0, 100000);

    public static final ModConfigSpec.IntValue PLATING_COMPONENT_REPAIR = BUILDER
            .comment("Cyberlimb durability restored by a plating component.")
            .defineInRange("platingComponentRepair", 500, 0, 100000);

    public static final ModConfigSpec.IntValue BATTERY_REPAIR_AMOUNT = BUILDER
            .comment("Battery durability restored by one battery repair material.")
            .defineInRange("batteryRepairAmount", 500, 0, 100000);

    public static final ModConfigSpec.EnumValue<ConfigValues.TattooUploadMode> TATTOO_UPLOAD_MODE = BUILDER
            .comment("Tattoo Upload Mode")
            .comment("SERVER_FILES_ONLY: Only PNGs manually placed in the server tattoo folder are available.")
            .comment("OP_ONLY_AUTO_APPROVE: Only operators may upload tattoos, and uploads are immediately approved.")
            .comment("ANY_PLAYER_PENDING_APPROVAL: Any player may upload tattoos, but uploads must be approved by an admin before appearing.")
            .comment("ANY_PLAYER_AUTO_APPROVE: Any player may upload tattoos, and uploads are immediately approved. Not recommended for public servers.")
            .defineEnum("tattooUploadMode", ConfigValues.TattooUploadMode.OP_ONLY_AUTO_APPROVE);



    public static final ModConfigSpec.IntValue CYBERZOMBIE_SPAWN_WEIGHT = BUILDER
            .comment("Cyberzombie replacement weight against naturally spawned vanilla Zombies.")
            .comment("Vanilla Zombies use a fixed baseline weight of 100.")
            .comment("Replacement chance is cyberzombieSpawn / (100 + cyberzombieSpawn).")
            .comment("Examples: 10 = 9.09%, 100 = 50%, 300 = 75%.")
            .comment("Set to 0 to disable Zombie replacement.")
            .comment("This value is checked when each eligible Zombie spawns and may be changed by config reload without rebuilding biome spawn lists.")
            .defineInRange("cyberzombieSpawn", 10, 0, 1000);

    public static final ModConfigSpec.IntValue CYBERSKELETON_SPAWN_WEIGHT = BUILDER
            .comment("Cyberskeleton replacement weight against naturally spawned vanilla Skeletons.")
            .comment("Vanilla Skeletons use a fixed baseline weight of 100.")
            .comment("Replacement chance is cyberskeletonSpawn / (100 + cyberskeletonSpawn).")
            .comment("Examples: 10 = 9.09%, 100 = 50%, 300 = 75%.")
            .comment("Set to 0 to disable Skeleton replacement.")
            .comment("This value is checked when each eligible Skeleton spawns and may be changed by config reload without rebuilding biome spawn lists.")
            .defineInRange("cyberskeletonSpawn", 10, 0, 1000);

    public static final ModConfigSpec.IntValue HOGBOY_SPAWN_WEIGHT = BUILDER
            .comment("Hogboy natural biome spawn-pool weight. Set to 0 to disable natural spawning.")
            .comment("This value is baked into biome spawn settings and requires the server/world to be restarted to take effect.")
            .defineInRange("hogboySpawn", 5, 0, 1000);

    public static final ModConfigSpec.IntValue HOGBOY_MIN_GROUP = BUILDER
            .comment("Hogboy minimum natural spawn group size.")
            .comment("This value is baked into biome spawn settings and requires the server/world to be restarted to take effect.")
            .defineInRange("hogboyMin", 1, 1, 100);

    public static final ModConfigSpec.IntValue HOGBOY_MAX_GROUP = BUILDER
            .comment("Hogboy maximum natural spawn group size.")
            .comment("This value is baked into biome spawn settings and requires the server/world to be restarted to take effect.")
            .defineInRange("hogboyMax", 4, 1, 100);

    public static final ModConfigSpec.IntValue PUNKLIN_SPAWN_WEIGHT = BUILDER
            .comment("Punklin natural biome spawn-pool weight. Set to 0 to disable natural spawning.")
            .comment("This value is baked into biome spawn settings and requires the server/world to be restarted to take effect.")
            .defineInRange("punklinSpawn", 10, 0, 1000);

    public static final ModConfigSpec.IntValue PUNKLIN_MIN_GROUP = BUILDER
            .comment("Punklin minimum natural spawn group size.")
            .comment("This value is baked into biome spawn settings and requires the server/world to be restarted to take effect.")
            .defineInRange("punklinMin", 3, 1, 100);

    public static final ModConfigSpec.IntValue PUNKLIN_MAX_GROUP = BUILDER
            .comment("Punklin maximum natural spawn group size.")
            .comment("This value is baked into biome spawn settings and requires the server/world to be restarted to take effect.")
            .defineInRange("punklinMax", 7, 1, 100);

    public static final ModConfigSpec.IntValue PIGSTROM_SPAWN_WEIGHT = BUILDER
            .comment("Pigstrom natural biome spawn-pool weight. Set to 0 to disable natural spawning.")
            .comment("This value is baked into biome spawn settings and requires the server/world to be restarted to take effect.")
            .defineInRange("pigstromSpawn", 14, 0, 1000);

    public static final ModConfigSpec.IntValue PIGSTROM_MIN_GROUP = BUILDER
            .comment("Pigstrom minimum natural spawn group size.")
            .comment("This value is baked into biome spawn settings and requires the server/world to be restarted to take effect.")
            .defineInRange("pigstromMin", 4, 1, 100);

    public static final ModConfigSpec.IntValue PIGSTROM_MAX_GROUP = BUILDER
            .comment("Pigstrom maximum natural spawn group size.")
            .comment("This value is baked into biome spawn settings and requires the server/world to be restarted to take effect.")
            .defineInRange("pigstromMax", 8, 1, 100);


    public static final ModConfigSpec.ConfigValue<List<? extends String>> ENTITY_SLOT_ROLLS = BUILDER
            .comment("Entity cyberware slot roll settings.")
            .comment("Format per entry: tableId,slot,rollChance,minRolls,maxRolls")
            .comment("Example: cyberzombie,BRAIN,0.25,1,3")
            .defineListAllowEmpty(
                    List.of("entitySlotRolls"),
                    defaultEntitySlotRolls(),
                    obj -> obj instanceof String
            );

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ENTITY_CYBERWARE_ROLLS = BUILDER
            .comment("Entity cyberware weighted roll entries.")
            .comment("Format per entry: tableId,item_id,slot,weight")
            .comment("Optional mod gate format: tableId,item_id,slot,weight,required_modid")
            .comment("Example: cyberzombie,createcybernetics:brainupgrades_cyberbrain,BRAIN,2")
            .comment("Example: cyberzombie,createcybernetics:brainupgrades_spelljammer,BRAIN,2,irons_spellbooks")
            .defineListAllowEmpty(
                    List.of("entityCyberwareRolls"),
                    defaultEntityCyberwareRolls(),
                    obj -> obj instanceof String
            );

    public static final ModConfigSpec SPEC = BUILDER.build();

    static void bakeRollTables() {
        ConfigValues.ENTITY_SLOT_ROLLS = parseEntitySlotRolls(ENTITY_SLOT_ROLLS.get());
        ConfigValues.ENTITY_CYBERWARE_ROLLS = parseEntityCyberwareRolls(ENTITY_CYBERWARE_ROLLS.get());
    }

    private static List<String> defaultEntitySlotRolls() {
        List<String> defaults = new ArrayList<>();

        addDefaultEntitySlotRolls(defaults, "cyberzombie");
        addDefaultEntitySlotRolls(defaults, "cyberskeleton");
        addDefaultEntitySlotRolls(defaults, "smasher");

        addDefaultEntitySlotRolls(defaults, "hogboy");
        addDefaultEntitySlotRolls(defaults, "punklin");
        addDefaultEntitySlotRolls(defaults, "pigstrom");

        return defaults;
    }

    private static void addDefaultEntitySlotRolls(List<String> defaults, String tableId) {
        if ("cyberzombie".equals(tableId)) {
            defaults.add(tableId + ",BRAIN,0.25,1,3");
            defaults.add(tableId + ",EYES,0.50,1,3");
            defaults.add(tableId + ",HEART,0.25,1,5");
            defaults.add(tableId + ",LUNGS,0.25,1,5");
            defaults.add(tableId + ",ORGANS,0.45,1,3");
            defaults.add(tableId + ",BONE,0.25,1,5");
            defaults.add(tableId + ",SKIN,0.25,1,4");
            defaults.add(tableId + ",MUSCLE,0.25,1,2");
            defaults.add(tableId + ",RLEG,0.65,1,3");
            defaults.add(tableId + ",LLEG,0.65,1,3");
            defaults.add(tableId + ",RARM,0.65,1,4");
            defaults.add(tableId + ",LARM,0.65,1,4");
            return;
        }

        if ("cyberskeleton".equals(tableId)) {
            defaults.add(tableId + ",BRAIN,0.25,1,3");
            defaults.add(tableId + ",EYES,0.50,1,3");
            defaults.add(tableId + ",ORGANS,0.1,1,3");
            defaults.add(tableId + ",BONE,1.0,4,5");
            defaults.add(tableId + ",RLEG,0.65,1,3");
            defaults.add(tableId + ",LLEG,0.65,1,3");
            defaults.add(tableId + ",RARM,0.65,1,4");
            defaults.add(tableId + ",LARM,0.65,1,4");
            return;
        }

        if ("smasher".equals(tableId)) {
            defaults.add(tableId + ",BRAIN,0.5,1,3");
            defaults.add(tableId + ",EYES,0.75,1,3");
            defaults.add(tableId + ",HEART,0.5,1,5");
            defaults.add(tableId + ",LUNGS,0.5,1,5");
            defaults.add(tableId + ",ORGANS,0.75,1,3");
            defaults.add(tableId + ",BONE,0.5,1,5");
            defaults.add(tableId + ",SKIN,0.5,1,4");
            defaults.add(tableId + ",MUSCLE,0.5,1,2");
            defaults.add(tableId + ",RLEG,0.85,1,3");
            defaults.add(tableId + ",LLEG,0.85,1,3");
            defaults.add(tableId + ",RARM,0.85,1,4");
            defaults.add(tableId + ",LARM,0.85,1,4");
        }

        if ("hogboy".equals(tableId)) {
            defaults.add(tableId + ",BRAIN,0.25,1,2");
            defaults.add(tableId + ",EYES,0.50,1,3");
            defaults.add(tableId + ",HEART,0.25,1,2");
            defaults.add(tableId + ",LUNGS,0.25,1,2");
            defaults.add(tableId + ",ORGANS,0.25,1,2");
            defaults.add(tableId + ",BONE,0.25,1,2");
            defaults.add(tableId + ",SKIN,0.25,1,2");
            defaults.add(tableId + ",MUSCLE,0.25,1,2");
            defaults.add(tableId + ",RLEG,0.15,1,2");
            defaults.add(tableId + ",LLEG,0.15,1,2");
            defaults.add(tableId + ",RARM,0.15,1,2");
            defaults.add(tableId + ",LARM,0.15,1,2");
            return;
        }

        if ("punklin".equals(tableId)) {
            defaults.add(tableId + ",BRAIN,0.25,1,3");
            defaults.add(tableId + ",EYES,0.50,1,3");
            defaults.add(tableId + ",HEART,0.25,1,5");
            defaults.add(tableId + ",LUNGS,0.25,1,5");
            defaults.add(tableId + ",ORGANS,0.45,1,3");
            defaults.add(tableId + ",BONE,0.25,1,5");
            defaults.add(tableId + ",SKIN,0.25,1,4");
            defaults.add(tableId + ",MUSCLE,0.25,1,2");
            defaults.add(tableId + ",RLEG,0.65,1,3");
            defaults.add(tableId + ",LLEG,0.65,1,3");
            defaults.add(tableId + ",RARM,0.65,1,4");
            defaults.add(tableId + ",LARM,0.65,1,4");
            return;
        }

        if ("pigstrom".equals(tableId)) {
            defaults.add(tableId + ",BRAIN,0.5,1,3");
            defaults.add(tableId + ",EYES,0.75,1,3");
            defaults.add(tableId + ",HEART,0.5,1,5");
            defaults.add(tableId + ",LUNGS,0.5,1,5");
            defaults.add(tableId + ",ORGANS,0.75,1,3");
            defaults.add(tableId + ",BONE,0.5,1,5");
            defaults.add(tableId + ",SKIN,0.5,1,4");
            defaults.add(tableId + ",MUSCLE,0.5,1,2");
            defaults.add(tableId + ",RLEG,0.85,1,3");
            defaults.add(tableId + ",LLEG,0.85,1,3");
            defaults.add(tableId + ",RARM,0.85,1,4");
            defaults.add(tableId + ",LARM,0.85,1,4");
        }

    }

    private static List<String> defaultEntityCyberwareRolls() {
        List<String> defaults = new ArrayList<>();

        addDefaultEntityCyberwareRolls(defaults, "cyberzombie");
        addDefaultEntityCyberwareRolls(defaults, "cyberskeleton");
        addDefaultEntityCyberwareRolls(defaults, "smasher");

        addDefaultEntityCyberwareRolls(defaults, "hogboy");
        addDefaultEntityCyberwareRolls(defaults, "punklin");
        addDefaultEntityCyberwareRolls(defaults, "pigstrom");

        return defaults;
    }

    private static void addDefaultEntityCyberwareRolls(List<String> defaults, String tableId) {
        if ("cyberzombie".equals(tableId)) {
            defaults.add(tableId + ",createcybernetics:brainupgrades_eyeofdefender,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_consciousnesstransmitter,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_corticalstack,BRAIN,3");
            defaults.add(tableId + ",createcybernetics:brainupgrades_enderjammer,BRAIN,3");
            defaults.add(tableId + ",createcybernetics:brainupgrades_matrix,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_neuralcontextualizer,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_cyberdeck,BRAIN,2");
            defaults.add(tableId + ",createcybernetics:brainupgrades_idem,BRAIN,2");
            defaults.add(tableId + ",createcybernetics:brainupgrades_chipwareslots,BRAIN,8");
            defaults.add(tableId + ",createcybernetics:brainupgrades_neuralprocessor,BRAIN,5");
            defaults.add(tableId + ",createcybernetics:brainupgrades_iceprotocol,BRAIN,3");
            defaults.add(tableId + ",createcybernetics:brainupgrades_spelljammer,BRAIN,2,irons_spellbooks");

            defaults.add(tableId + ",createcybernetics:basecyberware_cybereyes,EYES,12");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_hudlens,EYES,6");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_navigationchip,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_hudjack,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_nightvision,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_targeting,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_underwatervision,EYES,4");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_zoom,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_trajectorycalculator,EYES,4");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_biomonitor,EYES,2");

            defaults.add(tableId + ",createcybernetics:heartupgrades_cyberheart,HEART,8");
            defaults.add(tableId + ",createcybernetics:heartupgrades_coupler,HEART,5");
            defaults.add(tableId + ",createcybernetics:heartupgrades_defibrillator,HEART,4");
            defaults.add(tableId + ",createcybernetics:heartupgrades_stemcell,HEART,4");
            defaults.add(tableId + ",createcybernetics:heartupgrades_platelets,HEART,4");

            defaults.add(tableId + ",createcybernetics:lungsupgrades_hyperoxygenation,LUNGS,6");
            defaults.add(tableId + ",createcybernetics:lungsupgrades_oxygen,LUNGS,5");

            defaults.add(tableId + ",createcybernetics:organsupgrades_adrenaline,ORGANS,5");
            defaults.add(tableId + ",createcybernetics:organsupgrades_battery,ORGANS,6");
            defaults.add(tableId + ",createcybernetics:organsupgrades_diamondwaferstack,ORGANS,3");
            defaults.add(tableId + ",createcybernetics:organsupgrades_liverfilter,ORGANS,4");
            defaults.add(tableId + ",createcybernetics:organsupgrades_metabolic,ORGANS,4");
            defaults.add(tableId + ",createcybernetics:organsupgrades_densebattery,ORGANS,4");
            defaults.add(tableId + ",createcybernetics:organsupgrades_heatengine,ORGANS,5");
            defaults.add(tableId + ",createcybernetics:organsupgrades_manabattery,ORGANS,3,irons_spellbooks");

            defaults.add(tableId + ",createcybernetics:basecyberware_linearframe,BONE,10");
            defaults.add(tableId + ",createcybernetics:boneupgrades_bonebattery,BONE,4");
            defaults.add(tableId + ",createcybernetics:boneupgrades_boneflex,BONE,4");
            defaults.add(tableId + ",createcybernetics:boneupgrades_bonelacing,BONE,5");
            defaults.add(tableId + ",createcybernetics:boneupgrades_capacitorframe,BONE,5");
            defaults.add(tableId + ",createcybernetics:boneupgrades_elytra,BONE,2,caelus");
            defaults.add(tableId + ",createcybernetics:boneupgrades_piezo,BONE,4");
            defaults.add(tableId + ",createcybernetics:boneupgrades_spinalinjector,BONE,3");
            defaults.add(tableId + ",createcybernetics:boneupgrades_sandevistan,BONE,2");
            defaults.add(tableId + ",createcybernetics:boneupgrades_cyberskull,BONE,3");

            defaults.add(tableId + ",createcybernetics:skinupgrades_arterialturbine,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_chromatophores,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_synthskin,SKIN,12");
            defaults.add(tableId + ",createcybernetics:skinupgrades_immuno,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_faceplate,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_netheriteplating,SKIN,2");
            defaults.add(tableId + ",createcybernetics:skinupgrades_solarskin,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_subdermalarmor,SKIN,5");
            defaults.add(tableId + ",createcybernetics:skinupgrades_subdermalspikes,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_syntheticsetules,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_sweat,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_metalplating,SKIN,8");
            defaults.add(tableId + ",createcybernetics:skinupgrades_manaskin,SKIN,2,irons_spellbooks");

            defaults.add(tableId + ",createcybernetics:muscleupgrades_synthmuscle,MUSCLE,5");
            defaults.add(tableId + ",createcybernetics:muscleupgrades_wiredreflexes,MUSCLE,4");

            defaults.add(tableId + ",createcybernetics:basecyberware_rightarm,RARM,12");
            defaults.add(tableId + ",createcybernetics:armupgrades_armcannon,RARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_flywheel,RARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_claws,RARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_crafthands,RARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_drillfist,RARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_firestarter,RARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_pneumaticwrist,RARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_reinforcedknuckles,RARM,6");
            defaults.add(tableId + ",createcybernetics:armupgrades_mantisblade_titanium,RARM,2");

            defaults.add(tableId + ",createcybernetics:basecyberware_leftarm,LARM,12");
            defaults.add(tableId + ",createcybernetics:armupgrades_armcannon,LARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_flywheel,LARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_claws,LARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_crafthands,LARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_drillfist,LARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_firestarter,LARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_pneumaticwrist,LARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_reinforcedknuckles,LARM,6");
            defaults.add(tableId + ",createcybernetics:armupgrades_mantisblade_titanium,LARM,2");

            defaults.add(tableId + ",createcybernetics:basecyberware_rightleg,RLEG,12");
            defaults.add(tableId + ",createcybernetics:legupgrades_metaldetector,RLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_anklebracers,RLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_jumpboost,RLEG,5");
            defaults.add(tableId + ",createcybernetics:legupgrades_propellers,RLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_spurs,RLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_ocelotpaws,RLEG,4");

            defaults.add(tableId + ",createcybernetics:basecyberware_leftleg,LLEG,12");
            defaults.add(tableId + ",createcybernetics:legupgrades_metaldetector,LLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_anklebracers,LLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_jumpboost,LLEG,5");
            defaults.add(tableId + ",createcybernetics:legupgrades_propellers,LLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_spurs,LLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_ocelotpaws,LLEG,4");
            return;
        }

        if ("cyberskeleton".equals(tableId)) {
            defaults.add(tableId + ",createcybernetics:brainupgrades_eyeofdefender,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_consciousnesstransmitter,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_corticalstack,BRAIN,3");
            defaults.add(tableId + ",createcybernetics:brainupgrades_enderjammer,BRAIN,3");
            defaults.add(tableId + ",createcybernetics:brainupgrades_matrix,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_neuralcontextualizer,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_cyberdeck,BRAIN,2");
            defaults.add(tableId + ",createcybernetics:brainupgrades_idem,BRAIN,2");
            defaults.add(tableId + ",createcybernetics:brainupgrades_chipwareslots,BRAIN,8");
            defaults.add(tableId + ",createcybernetics:brainupgrades_neuralprocessor,BRAIN,5");
            defaults.add(tableId + ",createcybernetics:brainupgrades_iceprotocol,BRAIN,3");
            defaults.add(tableId + ",createcybernetics:brainupgrades_spelljammer,BRAIN,2,irons_spellbooks");

            defaults.add(tableId + ",createcybernetics:basecyberware_cybereyes,EYES,12");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_hudlens,EYES,6");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_navigationchip,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_hudjack,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_nightvision,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_targeting,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_underwatervision,EYES,4");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_zoom,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_trajectorycalculator,EYES,4");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_biomonitor,EYES,2");

            defaults.add(tableId + ",createcybernetics:organsupgrades_adrenaline,ORGANS,5");
            defaults.add(tableId + ",createcybernetics:organsupgrades_battery,ORGANS,6");
            defaults.add(tableId + ",createcybernetics:organsupgrades_diamondwaferstack,ORGANS,3");
            defaults.add(tableId + ",createcybernetics:organsupgrades_liverfilter,ORGANS,4");
            defaults.add(tableId + ",createcybernetics:organsupgrades_metabolic,ORGANS,4");
            defaults.add(tableId + ",createcybernetics:organsupgrades_densebattery,ORGANS,4");
            defaults.add(tableId + ",createcybernetics:organsupgrades_heatengine,ORGANS,5");
            defaults.add(tableId + ",createcybernetics:organsupgrades_manabattery,ORGANS,3,irons_spellbooks");

            defaults.add(tableId + ",createcybernetics:basecyberware_linearframe,BONE,10");
            defaults.add(tableId + ",createcybernetics:boneupgrades_bonebattery,BONE,4");
            defaults.add(tableId + ",createcybernetics:boneupgrades_boneflex,BONE,4");
            defaults.add(tableId + ",createcybernetics:boneupgrades_bonelacing,BONE,5");
            defaults.add(tableId + ",createcybernetics:boneupgrades_capacitorframe,BONE,5");
            defaults.add(tableId + ",createcybernetics:boneupgrades_elytra,BONE,2,caelus");
            defaults.add(tableId + ",createcybernetics:boneupgrades_piezo,BONE,4");
            defaults.add(tableId + ",createcybernetics:boneupgrades_spinalinjector,BONE,3");
            defaults.add(tableId + ",createcybernetics:boneupgrades_sandevistan,BONE,2");
            defaults.add(tableId + ",createcybernetics:boneupgrades_cyberskull,BONE,3");

            defaults.add(tableId + ",createcybernetics:basecyberware_rightarm,RARM,12");
            defaults.add(tableId + ",createcybernetics:armupgrades_armcannon,RARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_flywheel,RARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_claws,RARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_crafthands,RARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_drillfist,RARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_firestarter,RARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_pneumaticwrist,RARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_reinforcedknuckles,RARM,6");
            defaults.add(tableId + ",createcybernetics:armupgrades_mantisblade_iron,RARM,2");

            defaults.add(tableId + ",createcybernetics:basecyberware_leftarm,LARM,12");
            defaults.add(tableId + ",createcybernetics:armupgrades_armcannon,LARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_flywheel,LARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_claws,LARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_crafthands,LARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_drillfist,LARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_firestarter,LARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_pneumaticwrist,LARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_reinforcedknuckles,LARM,6");
            defaults.add(tableId + ",createcybernetics:armupgrades_mantisblade_iron,LARM,2");

            defaults.add(tableId + ",createcybernetics:basecyberware_rightleg,RLEG,12");
            defaults.add(tableId + ",createcybernetics:legupgrades_metaldetector,RLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_anklebracers,RLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_jumpboost,RLEG,5");
            defaults.add(tableId + ",createcybernetics:legupgrades_propellers,RLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_spurs,RLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_ocelotpaws,RLEG,4");

            defaults.add(tableId + ",createcybernetics:basecyberware_leftleg,LLEG,12");
            defaults.add(tableId + ",createcybernetics:legupgrades_metaldetector,LLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_anklebracers,LLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_jumpboost,LLEG,5");
            defaults.add(tableId + ",createcybernetics:legupgrades_propellers,LLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_spurs,LLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_ocelotpaws,LLEG,4");
            return;
        }

        if ("smasher".equals(tableId)) {
            defaults.add(tableId + ",createcybernetics:brainupgrades_eyeofdefender,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_consciousnesstransmitter,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_corticalstack,BRAIN,3");
            defaults.add(tableId + ",createcybernetics:brainupgrades_enderjammer,BRAIN,3");
            defaults.add(tableId + ",createcybernetics:brainupgrades_matrix,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_neuralcontextualizer,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_cyberdeck,BRAIN,2");
            defaults.add(tableId + ",createcybernetics:brainupgrades_idem,BRAIN,2");
            defaults.add(tableId + ",createcybernetics:brainupgrades_chipwareslots,BRAIN,8");
            defaults.add(tableId + ",createcybernetics:brainupgrades_neuralprocessor,BRAIN,5");
            defaults.add(tableId + ",createcybernetics:brainupgrades_iceprotocol,BRAIN,3");
            defaults.add(tableId + ",createcybernetics:brainupgrades_spelljammer,BRAIN,2,irons_spellbooks");

            defaults.add(tableId + ",createcybernetics:basecyberware_cybereyes,EYES,12");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_hudlens,EYES,6");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_navigationchip,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_hudjack,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_nightvision,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_targeting,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_underwatervision,EYES,4");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_zoom,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_trajectorycalculator,EYES,4");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_biomonitor,EYES,2");

            defaults.add(tableId + ",createcybernetics:heartupgrades_cyberheart,HEART,8");
            defaults.add(tableId + ",createcybernetics:heartupgrades_coupler,HEART,5");
            defaults.add(tableId + ",createcybernetics:heartupgrades_creeperheart,HEART,1");
            defaults.add(tableId + ",createcybernetics:heartupgrades_defibrillator,HEART,4");
            defaults.add(tableId + ",createcybernetics:heartupgrades_stemcell,HEART,4");
            defaults.add(tableId + ",createcybernetics:heartupgrades_platelets,HEART,4");

            defaults.add(tableId + ",createcybernetics:lungsupgrades_hyperoxygenation,LUNGS,6");
            defaults.add(tableId + ",createcybernetics:lungsupgrades_oxygen,LUNGS,5");

            defaults.add(tableId + ",createcybernetics:organsupgrades_adrenaline,ORGANS,5");
            defaults.add(tableId + ",createcybernetics:organsupgrades_battery,ORGANS,6");
            defaults.add(tableId + ",createcybernetics:organsupgrades_diamondwaferstack,ORGANS,3");
            defaults.add(tableId + ",createcybernetics:organsupgrades_liverfilter,ORGANS,4");
            defaults.add(tableId + ",createcybernetics:organsupgrades_metabolic,ORGANS,4");
            defaults.add(tableId + ",createcybernetics:organsupgrades_densebattery,ORGANS,4");
            defaults.add(tableId + ",createcybernetics:organsupgrades_heatengine,ORGANS,5");
            defaults.add(tableId + ",createcybernetics:organsupgrades_manabattery,ORGANS,3,irons_spellbooks");

            defaults.add(tableId + ",createcybernetics:basecyberware_linearframe,BONE,10");
            defaults.add(tableId + ",createcybernetics:boneupgrades_bonebattery,BONE,4");
            defaults.add(tableId + ",createcybernetics:boneupgrades_boneflex,BONE,4");
            defaults.add(tableId + ",createcybernetics:boneupgrades_bonelacing,BONE,5");
            defaults.add(tableId + ",createcybernetics:boneupgrades_capacitorframe,BONE,5");
            defaults.add(tableId + ",createcybernetics:boneupgrades_piezo,BONE,4");
            defaults.add(tableId + ",createcybernetics:boneupgrades_spinalinjector,BONE,3");
            defaults.add(tableId + ",createcybernetics:boneupgrades_sandevistan,BONE,2");
            defaults.add(tableId + ",createcybernetics:boneupgrades_cyberskull,BONE,3");

            defaults.add(tableId + ",createcybernetics:skinupgrades_arterialturbine,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_chromatophores,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_synthskin,SKIN,12");
            defaults.add(tableId + ",createcybernetics:skinupgrades_immuno,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_faceplate,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_netheriteplating,SKIN,2");
            defaults.add(tableId + ",createcybernetics:skinupgrades_solarskin,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_subdermalarmor,SKIN,5");
            defaults.add(tableId + ",createcybernetics:skinupgrades_subdermalspikes,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_syntheticsetules,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_sweat,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_metalplating,SKIN,8");
            defaults.add(tableId + ",createcybernetics:skinupgrades_manaskin,SKIN,2,irons_spellbooks");

            defaults.add(tableId + ",createcybernetics:muscleupgrades_synthmuscle,MUSCLE,5");
            defaults.add(tableId + ",createcybernetics:muscleupgrades_wiredreflexes,MUSCLE,4");

            defaults.add(tableId + ",createcybernetics:basecyberware_rightarm,RARM,12");
            defaults.add(tableId + ",createcybernetics:armupgrades_armcannon,RARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_flywheel,RARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_claws,RARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_crafthands,RARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_drillfist,RARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_firestarter,RARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_pneumaticwrist,RARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_reinforcedknuckles,RARM,6");
            defaults.add(tableId + ",createcybernetics:armupgrades_mantisblade_diamond,RARM,2");
            defaults.add(tableId + ",createcybernetics:armupgrades_mantisblade_netherite,RARM,2");

            defaults.add(tableId + ",createcybernetics:basecyberware_leftarm,LARM,12");
            defaults.add(tableId + ",createcybernetics:armupgrades_armcannon,LARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_flywheel,LARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_claws,LARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_crafthands,LARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_drillfist,LARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_firestarter,LARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_pneumaticwrist,LARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_reinforcedknuckles,LARM,6");
            defaults.add(tableId + ",createcybernetics:armupgrades_mantisblade_diamond,LARM,2");
            defaults.add(tableId + ",createcybernetics:armupgrades_mantisblade_netherite,LARM,2");

            defaults.add(tableId + ",createcybernetics:basecyberware_rightleg,RLEG,12");
            defaults.add(tableId + ",createcybernetics:legupgrades_metaldetector,RLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_anklebracers,RLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_jumpboost,RLEG,5");
            defaults.add(tableId + ",createcybernetics:legupgrades_propellers,RLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_spurs,RLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_ocelotpaws,RLEG,4");

            defaults.add(tableId + ",createcybernetics:basecyberware_leftleg,LLEG,12");
            defaults.add(tableId + ",createcybernetics:legupgrades_metaldetector,LLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_anklebracers,LLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_jumpboost,LLEG,5");
            defaults.add(tableId + ",createcybernetics:legupgrades_propellers,LLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_spurs,LLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_ocelotpaws,LLEG,4");
        }

        if ("hogboy".equals(tableId)) {
            defaults.add(tableId + ",createcybernetics:brainupgrades_eyeofdefender,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_consciousnesstransmitter,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_corticalstack,BRAIN,3");
            defaults.add(tableId + ",createcybernetics:brainupgrades_enderjammer,BRAIN,3");
            defaults.add(tableId + ",createcybernetics:brainupgrades_matrix,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_neuralcontextualizer,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_cyberdeck,BRAIN,2");
            defaults.add(tableId + ",createcybernetics:brainupgrades_chipwareslots,BRAIN,8");
            defaults.add(tableId + ",createcybernetics:brainupgrades_neuralprocessor,BRAIN,5");
            defaults.add(tableId + ",createcybernetics:brainupgrades_iceprotocol,BRAIN,3");
            defaults.add(tableId + ",createcybernetics:brainupgrades_spelljammer,BRAIN,2,irons_spellbooks");

            defaults.add(tableId + ",createcybernetics:basecyberware_cybereyes,EYES,6");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_monovision,EYES,12");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_hudlens,EYES,6");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_navigationchip,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_hudjack,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_nightvision,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_targeting,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_underwatervision,EYES,4");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_zoom,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_trajectorycalculator,EYES,4");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_biomonitor,EYES,2");

            defaults.add(tableId + ",createcybernetics:heartupgrades_cyberheart,HEART,8");
            defaults.add(tableId + ",createcybernetics:heartupgrades_coupler,HEART,5");
            defaults.add(tableId + ",createcybernetics:heartupgrades_creeperheart,HEART,1");
            defaults.add(tableId + ",createcybernetics:heartupgrades_defibrillator,HEART,4");
            defaults.add(tableId + ",createcybernetics:heartupgrades_stemcell,HEART,4");
            defaults.add(tableId + ",createcybernetics:heartupgrades_platelets,HEART,4");

            defaults.add(tableId + ",createcybernetics:lungsupgrades_hyperoxygenation,LUNGS,6");
            defaults.add(tableId + ",createcybernetics:lungsupgrades_oxygen,LUNGS,5");

            defaults.add(tableId + ",createcybernetics:organsupgrades_adrenaline,ORGANS,5");
            defaults.add(tableId + ",createcybernetics:organsupgrades_battery,ORGANS,6");
            defaults.add(tableId + ",createcybernetics:organsupgrades_diamondwaferstack,ORGANS,3");
            defaults.add(tableId + ",createcybernetics:organsupgrades_liverfilter,ORGANS,4");
            defaults.add(tableId + ",createcybernetics:organsupgrades_metabolic,ORGANS,4");
            defaults.add(tableId + ",createcybernetics:organsupgrades_densebattery,ORGANS,4");
            defaults.add(tableId + ",createcybernetics:organsupgrades_heatengine,ORGANS,5");
            defaults.add(tableId + ",createcybernetics:organsupgrades_manabattery,ORGANS,3,irons_spellbooks");

            defaults.add(tableId + ",createcybernetics:basecyberware_linearframe,BONE,10");
            defaults.add(tableId + ",createcybernetics:boneupgrades_bonebattery,BONE,4");
            defaults.add(tableId + ",createcybernetics:boneupgrades_boneflex,BONE,4");
            defaults.add(tableId + ",createcybernetics:boneupgrades_bonelacing,BONE,5");
            defaults.add(tableId + ",createcybernetics:boneupgrades_capacitorframe,BONE,5");
            defaults.add(tableId + ",createcybernetics:boneupgrades_piezo,BONE,4");
            defaults.add(tableId + ",createcybernetics:boneupgrades_spinalinjector,BONE,3");
            defaults.add(tableId + ",createcybernetics:boneupgrades_sandevistan,BONE,2");
            defaults.add(tableId + ",createcybernetics:boneupgrades_cyberskull,BONE,3");

            defaults.add(tableId + ",createcybernetics:skinupgrades_arterialturbine,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_chromatophores,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_synthskin,SKIN,12");
            defaults.add(tableId + ",createcybernetics:skinupgrades_immuno,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_faceplate,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_netheriteplating,SKIN,2");
            defaults.add(tableId + ",createcybernetics:skinupgrades_solarskin,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_subdermalarmor,SKIN,5");
            defaults.add(tableId + ",createcybernetics:skinupgrades_subdermalspikes,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_syntheticsetules,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_sweat,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_metalplating,SKIN,8");
            defaults.add(tableId + ",createcybernetics:skinupgrades_manaskin,SKIN,2,irons_spellbooks");

            defaults.add(tableId + ",createcybernetics:muscleupgrades_synthmuscle,MUSCLE,5");
            defaults.add(tableId + ",createcybernetics:muscleupgrades_wiredreflexes,MUSCLE,4");

            defaults.add(tableId + ",createcybernetics:basecyberware_rightarm,RARM,12");
            defaults.add(tableId + ",createcybernetics:armupgrades_armcannon,RARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_flywheel,RARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_claws,RARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_crafthands,RARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_drillfist,RARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_firestarter,RARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_pneumaticwrist,RARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_reinforcedknuckles,RARM,6");
            defaults.add(tableId + ",createcybernetics:armupgrades_mantisblade_gold,RARM,2");

            defaults.add(tableId + ",createcybernetics:basecyberware_leftarm,LARM,12");
            defaults.add(tableId + ",createcybernetics:armupgrades_armcannon,LARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_flywheel,LARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_claws,LARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_crafthands,LARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_drillfist,LARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_firestarter,LARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_pneumaticwrist,LARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_reinforcedknuckles,LARM,6");
            defaults.add(tableId + ",createcybernetics:armupgrades_mantisblade_gold,LARM,2");

            defaults.add(tableId + ",createcybernetics:basecyberware_rightleg,RLEG,12");
            defaults.add(tableId + ",createcybernetics:legupgrades_metaldetector,RLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_anklebracers,RLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_jumpboost,RLEG,5");
            defaults.add(tableId + ",createcybernetics:legupgrades_propellers,RLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_spurs,RLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_ocelotpaws,RLEG,4");

            defaults.add(tableId + ",createcybernetics:basecyberware_leftleg,LLEG,12");
            defaults.add(tableId + ",createcybernetics:legupgrades_metaldetector,LLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_anklebracers,LLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_jumpboost,LLEG,5");
            defaults.add(tableId + ",createcybernetics:legupgrades_propellers,LLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_spurs,LLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_ocelotpaws,LLEG,4");
        }

        if ("punklin".equals(tableId)) {
            defaults.add(tableId + ",createcybernetics:brainupgrades_eyeofdefender,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_consciousnesstransmitter,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_corticalstack,BRAIN,3");
            defaults.add(tableId + ",createcybernetics:brainupgrades_enderjammer,BRAIN,3");
            defaults.add(tableId + ",createcybernetics:brainupgrades_matrix,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_neuralcontextualizer,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_cyberdeck,BRAIN,2");
            defaults.add(tableId + ",createcybernetics:brainupgrades_chipwareslots,BRAIN,8");
            defaults.add(tableId + ",createcybernetics:brainupgrades_neuralprocessor,BRAIN,5");
            defaults.add(tableId + ",createcybernetics:brainupgrades_iceprotocol,BRAIN,3");
            defaults.add(tableId + ",createcybernetics:brainupgrades_spelljammer,BRAIN,2,irons_spellbooks");

            defaults.add(tableId + ",createcybernetics:basecyberware_cybereyes,EYES,12");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_hudlens,EYES,6");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_navigationchip,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_hudjack,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_nightvision,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_targeting,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_underwatervision,EYES,4");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_zoom,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_trajectorycalculator,EYES,4");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_biomonitor,EYES,2");

            defaults.add(tableId + ",createcybernetics:heartupgrades_cyberheart,HEART,8");
            defaults.add(tableId + ",createcybernetics:heartupgrades_coupler,HEART,5");
            defaults.add(tableId + ",createcybernetics:heartupgrades_creeperheart,HEART,1");
            defaults.add(tableId + ",createcybernetics:heartupgrades_defibrillator,HEART,4");
            defaults.add(tableId + ",createcybernetics:heartupgrades_stemcell,HEART,4");
            defaults.add(tableId + ",createcybernetics:heartupgrades_platelets,HEART,4");

            defaults.add(tableId + ",createcybernetics:lungsupgrades_hyperoxygenation,LUNGS,6");
            defaults.add(tableId + ",createcybernetics:lungsupgrades_oxygen,LUNGS,5");

            defaults.add(tableId + ",createcybernetics:organsupgrades_adrenaline,ORGANS,5");
            defaults.add(tableId + ",createcybernetics:organsupgrades_battery,ORGANS,6");
            defaults.add(tableId + ",createcybernetics:organsupgrades_diamondwaferstack,ORGANS,3");
            defaults.add(tableId + ",createcybernetics:organsupgrades_liverfilter,ORGANS,4");
            defaults.add(tableId + ",createcybernetics:organsupgrades_metabolic,ORGANS,4");
            defaults.add(tableId + ",createcybernetics:organsupgrades_densebattery,ORGANS,4");
            defaults.add(tableId + ",createcybernetics:organsupgrades_heatengine,ORGANS,5");
            defaults.add(tableId + ",createcybernetics:organsupgrades_manabattery,ORGANS,3,irons_spellbooks");

            defaults.add(tableId + ",createcybernetics:basecyberware_linearframe,BONE,10");
            defaults.add(tableId + ",createcybernetics:boneupgrades_bonebattery,BONE,4");
            defaults.add(tableId + ",createcybernetics:boneupgrades_boneflex,BONE,4");
            defaults.add(tableId + ",createcybernetics:boneupgrades_bonelacing,BONE,5");
            defaults.add(tableId + ",createcybernetics:boneupgrades_capacitorframe,BONE,5");
            defaults.add(tableId + ",createcybernetics:boneupgrades_piezo,BONE,4");
            defaults.add(tableId + ",createcybernetics:boneupgrades_spinalinjector,BONE,3");
            defaults.add(tableId + ",createcybernetics:boneupgrades_sandevistan,BONE,2");
            defaults.add(tableId + ",createcybernetics:boneupgrades_cyberskull,BONE,3");

            defaults.add(tableId + ",createcybernetics:skinupgrades_arterialturbine,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_chromatophores,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_synthskin,SKIN,12");
            defaults.add(tableId + ",createcybernetics:skinupgrades_immuno,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_faceplate,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_netheriteplating,SKIN,2");
            defaults.add(tableId + ",createcybernetics:skinupgrades_solarskin,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_subdermalarmor,SKIN,5");
            defaults.add(tableId + ",createcybernetics:skinupgrades_subdermalspikes,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_syntheticsetules,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_sweat,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_metalplating,SKIN,8");
            defaults.add(tableId + ",createcybernetics:skinupgrades_manaskin,SKIN,2,irons_spellbooks");

            defaults.add(tableId + ",createcybernetics:muscleupgrades_synthmuscle,MUSCLE,5");
            defaults.add(tableId + ",createcybernetics:muscleupgrades_wiredreflexes,MUSCLE,4");

            defaults.add(tableId + ",createcybernetics:basecyberware_rightarm_goldplated,RARM,12");
            defaults.add(tableId + ",createcybernetics:armupgrades_armcannon,RARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_flywheel,RARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_claws,RARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_crafthands,RARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_drillfist,RARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_firestarter,RARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_pneumaticwrist,RARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_reinforcedknuckles,RARM,6");
            defaults.add(tableId + ",createcybernetics:armupgrades_mantisblade_gold,RARM,2");

            defaults.add(tableId + ",createcybernetics:basecyberware_leftarm_goldplated,LARM,12");
            defaults.add(tableId + ",createcybernetics:armupgrades_armcannon,LARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_flywheel,LARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_claws,LARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_crafthands,LARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_drillfist,LARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_firestarter,LARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_pneumaticwrist,LARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_reinforcedknuckles,LARM,6");
            defaults.add(tableId + ",createcybernetics:armupgrades_mantisblade_gold,LARM,2");

            defaults.add(tableId + ",createcybernetics:basecyberware_rightleg_goldplated,RLEG,12");
            defaults.add(tableId + ",createcybernetics:legupgrades_metaldetector,RLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_anklebracers,RLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_jumpboost,RLEG,5");
            defaults.add(tableId + ",createcybernetics:legupgrades_propellers,RLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_spurs,RLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_ocelotpaws,RLEG,4");

            defaults.add(tableId + ",createcybernetics:basecyberware_leftleg_goldplated,LLEG,12");
            defaults.add(tableId + ",createcybernetics:legupgrades_metaldetector,LLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_anklebracers,LLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_jumpboost,LLEG,5");
            defaults.add(tableId + ",createcybernetics:legupgrades_propellers,LLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_spurs,LLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_ocelotpaws,LLEG,4");
        }

        if ("pigstrom".equals(tableId)) {
            defaults.add(tableId + ",createcybernetics:brainupgrades_eyeofdefender,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_consciousnesstransmitter,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_corticalstack,BRAIN,3");
            defaults.add(tableId + ",createcybernetics:brainupgrades_enderjammer,BRAIN,3");
            defaults.add(tableId + ",createcybernetics:brainupgrades_matrix,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_neuralcontextualizer,BRAIN,4");
            defaults.add(tableId + ",createcybernetics:brainupgrades_cyberdeck,BRAIN,2");
            defaults.add(tableId + ",createcybernetics:brainupgrades_chipwareslots,BRAIN,8");
            defaults.add(tableId + ",createcybernetics:brainupgrades_neuralprocessor,BRAIN,5");
            defaults.add(tableId + ",createcybernetics:brainupgrades_iceprotocol,BRAIN,3");
            defaults.add(tableId + ",createcybernetics:brainupgrades_spelljammer,BRAIN,2,irons_spellbooks");

            defaults.add(tableId + ",createcybernetics:basecyberware_cybereyes,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_multioptics1,EYES,12");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_multioptics2,EYES,12");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_multioptics3,EYES,12");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_multioptics4,EYES,12");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_hudlens,EYES,6");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_navigationchip,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_hudjack,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_nightvision,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_targeting,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_underwatervision,EYES,4");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_zoom,EYES,5");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_trajectorycalculator,EYES,4");
            defaults.add(tableId + ",createcybernetics:eyeupgrades_biomonitor,EYES,2");

            defaults.add(tableId + ",createcybernetics:heartupgrades_cyberheart,HEART,8");
            defaults.add(tableId + ",createcybernetics:heartupgrades_coupler,HEART,5");
            defaults.add(tableId + ",createcybernetics:heartupgrades_creeperheart,HEART,1");
            defaults.add(tableId + ",createcybernetics:heartupgrades_defibrillator,HEART,4");
            defaults.add(tableId + ",createcybernetics:heartupgrades_stemcell,HEART,4");
            defaults.add(tableId + ",createcybernetics:heartupgrades_platelets,HEART,4");

            defaults.add(tableId + ",createcybernetics:lungsupgrades_hyperoxygenation,LUNGS,6");
            defaults.add(tableId + ",createcybernetics:lungsupgrades_oxygen,LUNGS,5");

            defaults.add(tableId + ",createcybernetics:organsupgrades_adrenaline,ORGANS,5");
            defaults.add(tableId + ",createcybernetics:organsupgrades_battery,ORGANS,6");
            defaults.add(tableId + ",createcybernetics:organsupgrades_diamondwaferstack,ORGANS,3");
            defaults.add(tableId + ",createcybernetics:organsupgrades_liverfilter,ORGANS,4");
            defaults.add(tableId + ",createcybernetics:organsupgrades_metabolic,ORGANS,4");
            defaults.add(tableId + ",createcybernetics:organsupgrades_densebattery,ORGANS,4");
            defaults.add(tableId + ",createcybernetics:organsupgrades_heatengine,ORGANS,5");
            defaults.add(tableId + ",createcybernetics:organsupgrades_manabattery,ORGANS,3,irons_spellbooks");

            defaults.add(tableId + ",createcybernetics:basecyberware_linearframe,BONE,10");
            defaults.add(tableId + ",createcybernetics:boneupgrades_bonebattery,BONE,4");
            defaults.add(tableId + ",createcybernetics:boneupgrades_boneflex,BONE,4");
            defaults.add(tableId + ",createcybernetics:boneupgrades_bonelacing,BONE,5");
            defaults.add(tableId + ",createcybernetics:boneupgrades_capacitorframe,BONE,5");
            defaults.add(tableId + ",createcybernetics:boneupgrades_piezo,BONE,4");
            defaults.add(tableId + ",createcybernetics:boneupgrades_spinalinjector,BONE,3");
            defaults.add(tableId + ",createcybernetics:boneupgrades_sandevistan,BONE,2");
            defaults.add(tableId + ",createcybernetics:boneupgrades_cyberskull,BONE,3");

            defaults.add(tableId + ",createcybernetics:skinupgrades_arterialturbine,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_chromatophores,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_synthskin,SKIN,12");
            defaults.add(tableId + ",createcybernetics:skinupgrades_immuno,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_faceplate,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_netheriteplating,SKIN,2");
            defaults.add(tableId + ",createcybernetics:skinupgrades_solarskin,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_subdermalarmor,SKIN,5");
            defaults.add(tableId + ",createcybernetics:skinupgrades_subdermalspikes,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_syntheticsetules,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_sweat,SKIN,3");
            defaults.add(tableId + ",createcybernetics:skinupgrades_metalplating,SKIN,8");
            defaults.add(tableId + ",createcybernetics:skinupgrades_manaskin,SKIN,2,irons_spellbooks");

            defaults.add(tableId + ",createcybernetics:muscleupgrades_synthmuscle,MUSCLE,5");
            defaults.add(tableId + ",createcybernetics:muscleupgrades_wiredreflexes,MUSCLE,4");

            defaults.add(tableId + ",createcybernetics:basecyberware_rightarm,RARM,12");
            defaults.add(tableId + ",createcybernetics:armupgrades_armcannon,RARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_flywheel,RARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_claws,RARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_crafthands,RARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_drillfist,RARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_firestarter,RARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_pneumaticwrist,RARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_reinforcedknuckles,RARM,6");
            defaults.add(tableId + ",createcybernetics:armupgrades_mantisblade_gold,RARM,2");

            defaults.add(tableId + ",createcybernetics:basecyberware_leftarm,LARM,12");
            defaults.add(tableId + ",createcybernetics:armupgrades_armcannon,LARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_flywheel,LARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_claws,LARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_crafthands,LARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_drillfist,LARM,3");
            defaults.add(tableId + ",createcybernetics:armupgrades_firestarter,LARM,5");
            defaults.add(tableId + ",createcybernetics:armupgrades_pneumaticwrist,LARM,4");
            defaults.add(tableId + ",createcybernetics:armupgrades_reinforcedknuckles,LARM,6");
            defaults.add(tableId + ",createcybernetics:armupgrades_mantisblade_gold,LARM,2");

            defaults.add(tableId + ",createcybernetics:basecyberware_rightleg,RLEG,12");
            defaults.add(tableId + ",createcybernetics:legupgrades_metaldetector,RLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_anklebracers,RLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_jumpboost,RLEG,5");
            defaults.add(tableId + ",createcybernetics:legupgrades_propellers,RLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_spurs,RLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_ocelotpaws,RLEG,4");

            defaults.add(tableId + ",createcybernetics:basecyberware_leftleg,LLEG,12");
            defaults.add(tableId + ",createcybernetics:legupgrades_metaldetector,LLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_anklebracers,LLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_jumpboost,LLEG,5");
            defaults.add(tableId + ",createcybernetics:legupgrades_propellers,LLEG,3");
            defaults.add(tableId + ",createcybernetics:legupgrades_spurs,LLEG,4");
            defaults.add(tableId + ",createcybernetics:legupgrades_ocelotpaws,LLEG,4");
        }

    }

    private static List<ConfigValues.EntitySlotRoll> parseEntitySlotRolls(List<? extends String> rawEntries) {
        List<ConfigValues.EntitySlotRoll> parsed = new ArrayList<>();
        if (rawEntries == null) return parsed;

        for (String raw : rawEntries) {
            if (raw == null) continue;

            String line = raw.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(",");
            if (parts.length != 5) {
                CreateCybernetics.LOGGER.warn("Skipping invalid entity slot roll config entry '{}': expected 5 comma-separated values", line);
                continue;
            }

            try {
                String tableId = parts[0].trim();
                CyberwareSlot slot = CyberwareSlot.valueOf(parts[1].trim());
                float rollChance = Float.parseFloat(parts[2].trim());
                int minRolls = Integer.parseInt(parts[3].trim());
                int maxRolls = Integer.parseInt(parts[4].trim());

                if (tableId.isEmpty()) continue;
                if (rollChance < 0.0F || rollChance > 1.0F) continue;
                if (minRolls < 0) continue;
                if (maxRolls < minRolls) continue;

                parsed.add(new ConfigValues.EntitySlotRoll(tableId, slot, rollChance, minRolls, maxRolls));
            } catch (Exception ex) {
                CreateCybernetics.LOGGER.warn("Skipping invalid entity slot roll config entry '{}'", line, ex);
            }
        }

        return parsed;
    }

    private static List<ConfigValues.EntityCyberwareRoll> parseEntityCyberwareRolls(List<? extends String> rawEntries) {
        List<ConfigValues.EntityCyberwareRoll> parsed = new ArrayList<>();
        if (rawEntries == null) return parsed;

        for (String raw : rawEntries) {
            if (raw == null) continue;

            String line = raw.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(",");
            if (parts.length != 4 && parts.length != 5) {
                CreateCybernetics.LOGGER.warn("Skipping invalid entity cyberware roll config entry '{}': expected 4 or 5 comma-separated values", line);
                continue;
            }

            try {
                String tableId = parts[0].trim();
                ResourceLocation id = ResourceLocation.parse(parts[1].trim());
                Item item = BuiltInRegistries.ITEM.get(id);
                if (item == null || item == BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace("air"))) {
                    CreateCybernetics.LOGGER.warn("Skipping entity cyberware roll config entry '{}': item '{}' was not found", line, id);
                    continue;
                }

                CyberwareSlot slot = CyberwareSlot.valueOf(parts[2].trim());
                int weight = Integer.parseInt(parts[3].trim());
                String requiredModId = parts.length == 5 ? parts[4].trim() : "";

                if (tableId.isEmpty()) continue;
                if (weight <= 0) continue;
                if (!requiredModId.isEmpty() && !ModList.get().isLoaded(requiredModId)) continue;

                parsed.add(new ConfigValues.EntityCyberwareRoll(tableId, item, slot, weight, requiredModId));
            } catch (Exception ex) {
                CreateCybernetics.LOGGER.warn("Skipping invalid entity cyberware roll config entry '{}'", line, ex);
            }
        }

        return parsed;
    }
}