package com.perigrine3.createcybernetics.common.macroprogram;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MacroprogramRegistry {
    private static final Map<String, MacroprogramDefinition> DEFINITIONS = new LinkedHashMap<>();

    public static final MacroprogramDefinition DATA_MINER = register("data_miner", MacroprogramCategory.UTILITY, 0, 0, 4);

    public static final MacroprogramDefinition BIT_CRUSHER = register("bit_crusher", MacroprogramCategory.COMBAT, 0, 0, 4);
    public static final MacroprogramDefinition STACK_OVERFLOW = register("stack_overflow", MacroprogramCategory.COMBAT, 0, 0, 8);
    public static final MacroprogramDefinition ICEBREAKER = register("icebreaker", MacroprogramCategory.COMBAT, 0, 0, 12);
    public static final MacroprogramDefinition PING = register("ping", MacroprogramCategory.COMBAT, 0, 1200, 2);
    public static final MacroprogramDefinition DDOS = register("ddos", MacroprogramCategory.COMBAT, 0, 600, 12);
    public static final MacroprogramDefinition BANHAMMER = register("banhammer", MacroprogramCategory.COMBAT, 0, 12000, 18);

    public static final MacroprogramDefinition FIREWALL = register("firewall", MacroprogramCategory.UTILITY, 600, 1200, 6);
    public static final MacroprogramDefinition RECOVERY = register("recovery", MacroprogramCategory.UTILITY, 0, 500, 4);

    public static final MacroprogramDefinition BASIC_ARMOR = register("basic_armor", MacroprogramCategory.UTILITY, 1800, 2400, 4);
    public static final MacroprogramDefinition INTERMEDIATE_ARMOR = register("intermediate_armor", MacroprogramCategory.UTILITY, 1800, 3000, 6);
    public static final MacroprogramDefinition ADVANCED_ARMOR = register("advanced_armor", MacroprogramCategory.UTILITY, 1800, 3600, 8);
    public static final MacroprogramDefinition ADMINISTRATOR_ACCESS_ARMOR = register("administrator_access_armor", MacroprogramCategory.UTILITY, 2400, 6000, 12);

    public static final MacroprogramDefinition PACKET_SNIFFER = register("packet_sniffer", MacroprogramCategory.UTILITY, 300, 900, 4);
    public static final MacroprogramDefinition VPN_SHROUD = register("vpn_shroud", MacroprogramCategory.UTILITY, 2400, 4800, 12);

    private MacroprogramRegistry() {}

    private static MacroprogramDefinition register(String id, MacroprogramCategory category, int activeTicks, int cooldownTicks, int ramCost) {
        MacroprogramDefinition definition = new MacroprogramDefinition(id, category, activeTicks, cooldownTicks, ramCost);
        DEFINITIONS.put(id, definition);
        return definition;
    }

    public static MacroprogramDefinition get(String id) {
        if (id == null) return null;

        return DEFINITIONS.get(id);
    }

    public static boolean contains(String id) {
        return get(id) != null;
    }

    public static List<MacroprogramDefinition> getAll() {
        return List.copyOf(DEFINITIONS.values());
    }

    public static List<MacroprogramDefinition> getByCategory(MacroprogramCategory category) {
        return DEFINITIONS.values().stream().filter(definition -> definition.category() == category).toList();
    }
}