package com.perigrine3.createcybernetics;

import com.perigrine3.createcybernetics.api.CyberwareDurabilityMode;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public final class ConfigValues {

    private ConfigValues() {}

    /** Base Humanity Value (50-1000). */
    public static int BASE_HUMANITY = 100;

    /** Keep Cyberware on Death. */
    public static boolean KEEP_CYBERWARE = false;

    /** Surgery Damage Scaling. */
    public static boolean SURGERY_DAMAGE_SCALING = false;

    /** Epilepsy Mode. */
    public static boolean EPILEPSY_MODE = false;



    /** Cyberware durability policy. */
    public static CyberwareDurabilityMode CYBERWARE_DURABILITY_MODE = CyberwareDurabilityMode.ENABLED;

    /** Critical organ durability debuffs on/off. */
    public static boolean CRITICAL_DURABILITY_DEBUFFS = true;

    /** Durability lost for each point of incoming health damage. */
    public static double DURABILITY_DAMAGE_SCALE = 1.0D;

    /** Biological durability restored per point of food nutrition before fatigue. */
    public static int FOOD_DURABILITY_REPAIR_PER_NUTRITION = 4;

    /** Natural repair fatigue gained per point of food nutrition. */
    public static int FOOD_REPAIR_FATIGUE_PER_NUTRITION = 1;

    /** Lowest possible food-repair efficiency after repeated healing. */
    public static double MINIMUM_FOOD_REPAIR_EFFICIENCY = 0.10D;

    /** Biological durability restored per second for each regeneration effect level. */
    public static int REGENERATION_DURABILITY_REPAIR = 4;

    /** Natural repair fatigue removed per second for each regeneration effect level. */
    public static int REGENERATION_FATIGUE_RECOVERY = 1;

    /** Durability restored by a low-value anvil repair material. */
    public static int ANVIL_REPAIR_LOW = 100;

    /** Durability restored by a moderate-value anvil repair material. */
    public static int ANVIL_REPAIR_MODERATE = 250;

    /** Durability restored by a high-value anvil repair material. */
    public static int ANVIL_REPAIR_HIGH = 500;

    /** Battery durability lost whenever this much energy is received. */
    public static int BATTERY_ENERGY_RECEIVED_PER_DAMAGE = 5000;

    /** Battery durability lost whenever this much energy is extracted. */
    public static int BATTERY_ENERGY_EXTRACTED_PER_DAMAGE = 2500;

    /** Battery durability lost from one passive-aging interval. */
    public static int BATTERY_PASSIVE_DAMAGE = 1;

    /** Number of ticks between passive battery-aging checks. */
    public static int BATTERY_PASSIVE_DAMAGE_INTERVAL = 24000;

    /** Battery durability damage applied each second while EMP is active. */
    public static int BATTERY_EMP_DAMAGE_PER_SECOND = 10;

    /** Cyberlimb durability restored by a titanium sheet. */
    public static int TITANIUM_SHEET_REPAIR = 100;

    /** Cyberlimb durability restored by a titanium ingot. */
    public static int TITANIUM_INGOT_REPAIR = 250;

    /** Cyberlimb durability restored by a plating component. */
    public static int PLATING_COMPONENT_REPAIR = 500;

    /** Battery durability restored by an electron repair material. */
    public static int BATTERY_REPAIR_AMOUNT = 500;



    /** Tattoo upload policy. */
    public static TattooUploadMode TATTOO_UPLOAD_MODE = TattooUploadMode.ANY_PLAYER_AUTO_APPROVE;

    public enum TattooUploadMode {
        SERVER_FILES_ONLY,
        OP_ONLY_AUTO_APPROVE,
        ANY_PLAYER_PENDING_APPROVAL,
        ANY_PLAYER_AUTO_APPROVE
    }



    /**
     * Relative replacement weight for naturally spawned vanilla Zombies against
     * a fixed vanilla baseline weight of 100.
     */
    public static int CYBERZOMBIE_SPAWN_WEIGHT = 10;

    /**
     * Relative replacement weight for naturally spawned vanilla Skeletons against
     * a fixed vanilla baseline weight of 100.
     */
    public static int CYBERSKELETON_SPAWN_WEIGHT = 10;

    /** Natural biome spawn-pool weight for Hogboys. */
    public static int HOGBOY_SPAWN_WEIGHT = 5;
    public static int HOGBOY_MIN_GROUP = 1;
    public static int HOGBOY_MAX_GROUP = 4;

    /** Natural biome spawn-pool weight for Punklins. */
    public static int PUNKLIN_SPAWN_WEIGHT = 10;
    public static int PUNKLIN_MIN_GROUP = 3;
    public static int PUNKLIN_MAX_GROUP = 7;

    /** Natural biome spawn-pool weight for Pigstroms. */
    public static int PIGSTROM_SPAWN_WEIGHT = 14;
    public static int PIGSTROM_MIN_GROUP = 4;
    public static int PIGSTROM_MAX_GROUP = 8;



    public static List<EntitySlotRoll> ENTITY_SLOT_ROLLS = new ArrayList<>();
    public static List<EntityCyberwareRoll> ENTITY_CYBERWARE_ROLLS = new ArrayList<>();

    public record EntitySlotRoll(String tableId, CyberwareSlot slot, float rollChance, int minRolls, int maxRolls) {
    }

    public record EntityCyberwareRoll(String tableId, Item item, CyberwareSlot slot, int weight, String requiredModId) {
    }
}