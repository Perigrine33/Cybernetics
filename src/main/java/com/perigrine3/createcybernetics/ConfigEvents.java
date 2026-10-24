package com.perigrine3.createcybernetics;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ConfigEvents {

    private ConfigEvents() {}

    @SubscribeEvent
    public static void onConfigLoading(ModConfigEvent.Loading event) {
        bake(event.getConfig());
    }

    @SubscribeEvent
    public static void onConfigReloading(ModConfigEvent.Reloading event) {
        bake(event.getConfig());
    }

    private static void bake(ModConfig config) {
        if (config.getSpec() != Config.SPEC) return;

        ConfigValues.BASE_HUMANITY = Config.HUMANITY.get();
        ConfigValues.KEEP_CYBERWARE = Config.KEEP_CYBERWARE.get();
        ConfigValues.SURGERY_DAMAGE_SCALING = Config.SURGERY_DAMAGE_SCALING.get();
        ConfigValues.EPILEPSY_MODE = Config.EPILEPSY_MODE.get();
        ConfigValues.TATTOO_UPLOAD_MODE = Config.TATTOO_UPLOAD_MODE.get();

        ConfigValues.CYBERWARE_DURABILITY_MODE = Config.CYBERWARE_DURABILITY_MODE.get();
        ConfigValues.CRITICAL_DURABILITY_DEBUFFS = Config.CRITICAL_DURABILITY_DEBUFFS.get();
        ConfigValues.DURABILITY_DAMAGE_SCALE = Config.DURABILITY_DAMAGE_SCALE.get();
        ConfigValues.FOOD_DURABILITY_REPAIR_PER_NUTRITION = Config.FOOD_DURABILITY_REPAIR_PER_NUTRITION.get();
        ConfigValues.FOOD_REPAIR_FATIGUE_PER_NUTRITION = Config.FOOD_REPAIR_FATIGUE_PER_NUTRITION.get();
        ConfigValues.MINIMUM_FOOD_REPAIR_EFFICIENCY = Config.MINIMUM_FOOD_REPAIR_EFFICIENCY.get();
        ConfigValues.REGENERATION_DURABILITY_REPAIR = Config.REGENERATION_DURABILITY_REPAIR.get();
        ConfigValues.REGENERATION_FATIGUE_RECOVERY = Config.REGENERATION_FATIGUE_RECOVERY.get();

        ConfigValues.ANVIL_REPAIR_LOW = Config.ANVIL_REPAIR_LOW.get();
        ConfigValues.ANVIL_REPAIR_MODERATE = Config.ANVIL_REPAIR_MODERATE.get();
        ConfigValues.ANVIL_REPAIR_HIGH = Config.ANVIL_REPAIR_HIGH.get();

        ConfigValues.BATTERY_ENERGY_RECEIVED_PER_DAMAGE = Config.BATTERY_ENERGY_RECEIVED_PER_DAMAGE.get();
        ConfigValues.BATTERY_ENERGY_EXTRACTED_PER_DAMAGE = Config.BATTERY_ENERGY_EXTRACTED_PER_DAMAGE.get();
        ConfigValues.BATTERY_PASSIVE_DAMAGE = Config.BATTERY_PASSIVE_DAMAGE.get();
        ConfigValues.BATTERY_PASSIVE_DAMAGE_INTERVAL = Config.BATTERY_PASSIVE_DAMAGE_INTERVAL.get();
        ConfigValues.BATTERY_EMP_DAMAGE_PER_SECOND = Config.BATTERY_EMP_DAMAGE_PER_SECOND.get();

        ConfigValues.TITANIUM_SHEET_REPAIR = Config.TITANIUM_SHEET_REPAIR.get();
        ConfigValues.TITANIUM_INGOT_REPAIR = Config.TITANIUM_INGOT_REPAIR.get();
        ConfigValues.PLATING_COMPONENT_REPAIR = Config.PLATING_COMPONENT_REPAIR.get();
        ConfigValues.BATTERY_REPAIR_AMOUNT = Config.BATTERY_REPAIR_AMOUNT.get();

        bakeSpawnSettings();
        Config.bakeRollTables();
    }

    private static void bakeSpawnSettings() {
        ConfigValues.CYBERZOMBIE_SPAWN_WEIGHT = Config.CYBERZOMBIE_SPAWN_WEIGHT.get();
        ConfigValues.CYBERSKELETON_SPAWN_WEIGHT = Config.CYBERSKELETON_SPAWN_WEIGHT.get();

        ConfigValues.HOGBOY_SPAWN_WEIGHT = Config.HOGBOY_SPAWN_WEIGHT.get();
        ConfigValues.HOGBOY_MIN_GROUP = Config.HOGBOY_MIN_GROUP.get();
        ConfigValues.HOGBOY_MAX_GROUP = Math.max(ConfigValues.HOGBOY_MIN_GROUP, Config.HOGBOY_MAX_GROUP.get());

        ConfigValues.PUNKLIN_SPAWN_WEIGHT = Config.PUNKLIN_SPAWN_WEIGHT.get();
        ConfigValues.PUNKLIN_MIN_GROUP = Config.PUNKLIN_MIN_GROUP.get();
        ConfigValues.PUNKLIN_MAX_GROUP = Math.max(ConfigValues.PUNKLIN_MIN_GROUP, Config.PUNKLIN_MAX_GROUP.get());

        ConfigValues.PIGSTROM_SPAWN_WEIGHT = Config.PIGSTROM_SPAWN_WEIGHT.get();
        ConfigValues.PIGSTROM_MIN_GROUP = Config.PIGSTROM_MIN_GROUP.get();
        ConfigValues.PIGSTROM_MAX_GROUP = Math.max(ConfigValues.PIGSTROM_MIN_GROUP, Config.PIGSTROM_MAX_GROUP.get());
    }
}