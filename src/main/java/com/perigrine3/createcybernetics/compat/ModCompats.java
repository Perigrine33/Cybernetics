package com.perigrine3.createcybernetics.compat;

import net.neoforged.fml.ModList;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ModCompats {
    private static final Map<String, Boolean> LOADED_CACHE = new ConcurrentHashMap<>();

    private ModCompats() {}

    public static boolean isInstalled(String modid) {
        if (modid == null) return false;
        Boolean cached = LOADED_CACHE.get(modid);
        if (cached != null) return cached;
        ModList list = ModList.get();
        if (list == null) return false;
        boolean loaded = list.isLoaded(modid);
        LOADED_CACHE.put(modid, loaded);
        return loaded;
    }
}
