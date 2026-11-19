package com.perigrine3.createcybernetics.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.client.navigation.SharedNavigationClientState;
import com.perigrine3.createcybernetics.client.navigation.NavigationFrameBudget;
import com.perigrine3.createcybernetics.client.navigation.NavigationExplorationScan;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MinimapWaypointClient {

    private MinimapWaypointClient() {}

    public static final int EXPLORATION_TILE_SIZE = 64;

    public enum WaypointType {
        BANNER,
        MAP_BANNER,
        EXPLORER,
        CUSTOM
    }

    public record Waypoint(String id, String dimension, int x, int y, int z, String name, int color, WaypointType type, boolean autoClear) {}

    public record NavigationTileSnapshot(String dimension, int tileX, int tileZ, int[] colors, long[] explored) {}

    public record NavigationWaypointSnapshot(String id, String dimension, int x, int y, int z, String name, int color, String type) {}

    public record ImportedMap(String id, String dimension, int centerX, int centerZ, byte scale, byte[] colors) {
        public int blocksPerPixel() {
            return 1 << scale;
        }

        public int minX() {
            return centerX - 64 * blocksPerPixel();
        }

        public int minZ() {
            return centerZ - 64 * blocksPerPixel();
        }

        public int maxX() {
            return minX() + 128 * blocksPerPixel();
        }

        public int maxZ() {
            return minZ() + 128 * blocksPerPixel();
        }
    }

    public static final class ExploredTile {
        private final String dimension;
        private final int tileX;
        private final int tileZ;
        private final int[] colors;
        private final BitSet explored;
        private long revision;

        private ExploredTile(String dimension, int tileX, int tileZ) {
            this.dimension = dimension;
            this.tileX = tileX;
            this.tileZ = tileZ;
            this.colors = new int[EXPLORATION_TILE_SIZE * EXPLORATION_TILE_SIZE];
            this.explored = new BitSet(EXPLORATION_TILE_SIZE * EXPLORATION_TILE_SIZE);
            this.revision = 0L;
        }

        ExploredTile(String dimension, int tileX, int tileZ, int[] colors, BitSet explored) {
            this.dimension = dimension;
            this.tileX = tileX;
            this.tileZ = tileZ;
            this.colors = colors;
            this.explored = explored;
            this.revision = 0L;
        }

        public String dimension() {
            return dimension;
        }

        public int tileX() {
            return tileX;
        }

        public int tileZ() {
            return tileZ;
        }

        public int minWorldX() {
            return tileX * EXPLORATION_TILE_SIZE;
        }

        public int minWorldZ() {
            return tileZ * EXPLORATION_TILE_SIZE;
        }

        public long revision() {
            return revision;
        }

        public boolean isExplored(int localX, int localZ) {
            if (localX < 0 || localX >= EXPLORATION_TILE_SIZE || localZ < 0 || localZ >= EXPLORATION_TILE_SIZE) return false;
            return explored.get(localZ * EXPLORATION_TILE_SIZE + localX);
        }

        public int color(int localX, int localZ) {
            if (localX < 0 || localX >= EXPLORATION_TILE_SIZE || localZ < 0 || localZ >= EXPLORATION_TILE_SIZE) return 0;
            return colors[localZ * EXPLORATION_TILE_SIZE + localX];
        }

        private boolean setColor(int localX, int localZ, int color, boolean overwrite) {
            if (localX < 0 || localX >= EXPLORATION_TILE_SIZE || localZ < 0 || localZ >= EXPLORATION_TILE_SIZE) return false;

            int index = localZ * EXPLORATION_TILE_SIZE + localX;

            if (!overwrite && explored.get(index)) {
                return false;
            }

            boolean changed = !explored.get(index) || colors[index] != color;

            if (!changed) {
                return false;
            }

            colors[index] = color;
            explored.set(index);
            revision++;
            return true;
        }
    }

    private record NavigationStorageKey(UUID playerId, String worldId) {}

    private static final class NavigationData {
        private final Map<String, Waypoint> waypoints = new LinkedHashMap<>();
        private final List<ImportedMap> maps = new ArrayList<>();
        private final Map<String, ExploredTile> exploredTiles = new HashMap<>();
        private final Set<String> dirtyTileKeys = new LinkedHashSet<>();
        private final Set<String> dirtyWaypointIds = new LinkedHashSet<>();
        private final Set<String> hiddenWaypointIds = new LinkedHashSet<>();
        private String activeWaypointId;
        private boolean dirty;
        private String explorationDimension;
        private int explorationX;
        private int explorationZ;
        private int explorationCursor;
        private int explorationRadius;
        private NavigationExplorationScan explorationScan;
        private int explorationFinishedTick = Integer.MIN_VALUE;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<NavigationStorageKey, NavigationData> CACHE = new ConcurrentHashMap<>();

    private static final java.util.concurrent.ExecutorService SAVE_WORKER = java.util.concurrent.Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "Navigation map saves");
        thread.setDaemon(true);
        return thread;
    });
    private static final Map<NavigationStorageKey, java.util.concurrent.CompletableFuture<Boolean>> SAVES = new HashMap<>();

    private static NavigationData snapshot(NavigationData source) {
        NavigationData copy = new NavigationData();
        copy.waypoints.putAll(source.waypoints);
        copy.maps.addAll(source.maps);
        copy.hiddenWaypointIds.addAll(source.hiddenWaypointIds);
        copy.activeWaypointId = source.activeWaypointId;
        for (var entry : source.exploredTiles.entrySet()) {
            ExploredTile tile = entry.getValue();
            copy.exploredTiles.put(entry.getKey(), new ExploredTile(tile.dimension, tile.tileX, tile.tileZ,
                    tile.colors.clone(), (BitSet) tile.explored.clone()));
        }
        return copy;
    }

    private static void finishSave(NavigationStorageKey key, NavigationData data) {
        var pending = SAVES.remove(key);
        if (pending != null && !pending.join()) data.dirty = true;
    }

    private static String cachedWorldId;

    public static ExploredTile createEmptySharedTile(String dimension, int tileX, int tileZ) {
        return new ExploredTile(dimension, tileX, tileZ, new int[EXPLORATION_TILE_SIZE * EXPLORATION_TILE_SIZE], new BitSet(EXPLORATION_TILE_SIZE * EXPLORATION_TILE_SIZE));
    }

    public static boolean setSharedTileColor(ExploredTile tile, int localX, int localZ, int color) {
        if (tile == null) return false;
        return tile.setColor(localX, localZ, color, false);
    }

    public static List<Waypoint> getWaypoints(LocalPlayer player) {
        if (player == null) return Collections.emptyList();

        Map<String, Waypoint> combined = new LinkedHashMap<>();

        for (Waypoint waypoint : data(player.getUUID()).waypoints.values()) {
            combined.put(waypoint.id(), waypoint);
        }

        for (Waypoint waypoint : SharedNavigationClientState.getWaypointValues()) {
            combined.putIfAbsent(waypoint.id(), waypoint);
        }

        combined.keySet().removeAll(data(player.getUUID()).hiddenWaypointIds);
        return Collections.unmodifiableList(new ArrayList<>(combined.values()));
    }

    public static List<ImportedMap> getImportedMaps(LocalPlayer player) {
        if (player == null) return Collections.emptyList();
        return Collections.unmodifiableList(data(player.getUUID()).maps);
    }

    public static List<ExploredTile> getExploredTiles(LocalPlayer player) {
        if (player == null) return Collections.emptyList();

        List<ExploredTile> combined = new ArrayList<>(data(player.getUUID()).exploredTiles.values());
        combined.addAll(SharedNavigationClientState.getTileValues());
        return Collections.unmodifiableList(combined);
    }

    public static List<NavigationTileSnapshot> createTileSnapshot(LocalPlayer player) {
        if (player == null) return Collections.emptyList();

        List<NavigationTileSnapshot> snapshots = new ArrayList<>();

        for (ExploredTile tile : data(player.getUUID()).exploredTiles.values()) {
            snapshots.add(new NavigationTileSnapshot(tile.dimension, tile.tileX, tile.tileZ, tile.colors.clone(), tile.explored.toLongArray()));
        }

        return snapshots;
    }

    public static List<NavigationWaypointSnapshot> createWaypointSnapshot(LocalPlayer player) {
        if (player == null) return Collections.emptyList();

        List<NavigationWaypointSnapshot> snapshots = new ArrayList<>();

        for (Waypoint waypoint : data(player.getUUID()).waypoints.values()) {
            snapshots.add(new NavigationWaypointSnapshot(waypoint.id(), waypoint.dimension(), waypoint.x(), waypoint.y(), waypoint.z(), waypoint.name(), waypoint.color(), waypoint.type().name()));
        }

        return snapshots;
    }

    public static List<NavigationTileSnapshot> drainDirtyTileSnapshots(LocalPlayer player, int maxCount) {
        if (player == null || maxCount <= 0) return Collections.emptyList();

        NavigationData data = data(player.getUUID());

        if (data.dirtyTileKeys.isEmpty()) {
            return Collections.emptyList();
        }

        List<NavigationTileSnapshot> snapshots = new ArrayList<>(Math.min(maxCount, data.dirtyTileKeys.size()));
        Iterator<String> iterator = data.dirtyTileKeys.iterator();

        while (iterator.hasNext() && snapshots.size() < maxCount) {
            String key = iterator.next();
            iterator.remove();

            ExploredTile tile = data.exploredTiles.get(key);

            if (tile == null) continue;

            snapshots.add(new NavigationTileSnapshot(tile.dimension, tile.tileX, tile.tileZ, tile.colors.clone(), tile.explored.toLongArray()));
        }

        return snapshots;
    }

    public static List<NavigationWaypointSnapshot> drainDirtyWaypointSnapshots(LocalPlayer player, int maxCount) {
        if (player == null || maxCount <= 0) return Collections.emptyList();

        NavigationData data = data(player.getUUID());

        if (data.dirtyWaypointIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<NavigationWaypointSnapshot> snapshots = new ArrayList<>(Math.min(maxCount, data.dirtyWaypointIds.size()));
        Iterator<String> iterator = data.dirtyWaypointIds.iterator();

        while (iterator.hasNext() && snapshots.size() < maxCount) {
            String id = iterator.next();
            iterator.remove();

            Waypoint waypoint = data.waypoints.get(id);

            if (waypoint == null) continue;

            snapshots.add(new NavigationWaypointSnapshot(waypoint.id(), waypoint.dimension(), waypoint.x(), waypoint.y(), waypoint.z(), waypoint.name(), waypoint.color(), waypoint.type().name()));
        }

        return snapshots;
    }

    /** Queue keys rather than cloning the complete explored world on network join. */
    public static void markAllSharedSyncDirty(LocalPlayer player) {
        if (player == null) return;
        NavigationData data = data(player.getUUID());
        data.dirtyTileKeys.addAll(data.exploredTiles.keySet());
        data.dirtyWaypointIds.addAll(data.waypoints.keySet());
    }

    public static void markAllSharedSyncClean(LocalPlayer player) {
        if (player == null) return;

        NavigationData data = data(player.getUUID());
        data.dirtyTileKeys.clear();
        data.dirtyWaypointIds.clear();
    }

    public static Waypoint getActiveWaypoint(LocalPlayer player) {
        if (player == null) return null;

        NavigationStorageKey key = currentStorageKey(player.getUUID());
        NavigationData data = data(key);

        if (data.activeWaypointId == null) {
            return null;
        }

        Waypoint waypoint = data.waypoints.get(data.activeWaypointId);

        if (waypoint == null) {
            waypoint = SharedNavigationClientState.getWaypoint(data.activeWaypointId);
        }

        if (waypoint != null && !data.hiddenWaypointIds.contains(waypoint.id())) {
            return waypoint;
        }

        data.activeWaypointId = null;
        data.dirty = true;
        saveIfDirty(key);
        return null;
    }

    public static void setActiveWaypoint(LocalPlayer player, Waypoint waypoint) {
        if (player == null) return;

        NavigationStorageKey key = currentStorageKey(player.getUUID());
        NavigationData data = data(key);
        data.activeWaypointId = waypoint != null ? waypoint.id() : null;
        data.dirty = true;
        saveIfDirty(key);
    }

    public static void clearActiveWaypoint(LocalPlayer player) {
        setActiveWaypoint(player, null);
    }

    public static Component getWaypointDisplayName(Waypoint waypoint) {
        if (waypoint == null) {
            return Component.translatable("gui.createcybernetics.navigation.default_waypoint");
        }

        if (waypoint.type() != WaypointType.EXPLORER) {
            return Component.literal(waypoint.name());
        }

        ResourceLocation typeId = ResourceLocation.tryParse(waypoint.name());

        if (typeId == null) {
            return Component.translatable("gui.createcybernetics.navigation.explorer.unknown");
        }

        if (typeId.getNamespace().equals("minecraft")) {
            return switch (typeId.getPath()) {
                case "red_x" -> Component.translatable("gui.createcybernetics.navigation.explorer.buried_treasure");
                case "mansion" -> Component.translatable("gui.createcybernetics.navigation.explorer.woodland_mansion");
                case "monument" -> Component.translatable("gui.createcybernetics.navigation.explorer.ocean_monument");
                case "trial_chambers" -> Component.translatable("gui.createcybernetics.navigation.explorer.trial_chambers");
                default -> Component.translatable("gui.createcybernetics.navigation.explorer.unknown");
            };
        }

        return Component.literal(formatExplorerName(typeId.getPath()));
    }

    public static boolean toggleBannerWaypoint(LocalPlayer player, BlockPos pos, String name, int color) {
        if (player == null || pos == null) return false;

        NavigationStorageKey key = currentStorageKey(player.getUUID());
        String dimension = player.level().dimension().location().toString();
        String id = bannerWaypointId(dimension, pos);
        NavigationData data = data(key);
        Waypoint existing = findWaypoint(data, id);

        if (existing != null) {
            removeWaypointInternal(data, existing);
            data.dirty = true;
            saveIfDirty(key);
            return false;
        }

        data.waypoints.put(id, new Waypoint(id, dimension, pos.getX(), pos.getY(), pos.getZ(), sanitizeName(name), color, WaypointType.BANNER, false));
        data.hiddenWaypointIds.remove(id);
        data.dirtyWaypointIds.add(id);
        data.dirty = true;
        saveIfDirty(key);
        return true;
    }

    public static boolean addMapBannerWaypoint(LocalPlayer player, String dimension, BlockPos pos, String name, int color) {
        if (player == null || pos == null) return false;

        NavigationData data = data(player.getUUID());
        String id = mapBannerWaypointId(dimension, pos);

        if (findWaypoint(data, id) != null) {
            return false;
        }

        data.waypoints.put(id, new Waypoint(id, dimension, pos.getX(), pos.getY(), pos.getZ(), sanitizeName(name), color, WaypointType.MAP_BANNER, false));
        data.hiddenWaypointIds.remove(id);
        data.dirtyWaypointIds.add(id);
        data.dirty = true;
        return true;
    }

    public static boolean addExplorerWaypoint(LocalPlayer player, String dimension, int x, int z, String sourceId, String typeId, int color) {
        if (player == null) return false;

        NavigationData data = data(player.getUUID());
        String id = explorerWaypointId(dimension, x, z, sourceId, typeId);

        if (findWaypoint(data, id) != null) {
            return false;
        }

        data.waypoints.put(id, new Waypoint(id, dimension, x, 0, z, typeId, color, WaypointType.EXPLORER, false));
        data.hiddenWaypointIds.remove(id);
        data.dirtyWaypointIds.add(id);
        data.dirty = true;
        return true;
    }

    public static boolean removeWaypoint(LocalPlayer player, Waypoint waypoint) {
        if (player == null || waypoint == null) return false;

        NavigationStorageKey key = currentStorageKey(player.getUUID());
        NavigationData data = data(key);

        boolean shared = SharedNavigationClientState.isSharedWaypoint(waypoint.id());
        if (!removeWaypointInternal(data, waypoint) && !shared) {
            return false;
        }

        // Shared markers belong to the network. Hide them on this player's map;
        // A later sync (including a queued upload's echo) must not resurrect a
        // marker the player removed locally.
        data.hiddenWaypointIds.add(waypoint.id());
        if (waypoint.id().equals(data.activeWaypointId)) data.activeWaypointId = null;

        data.dirty = true;
        saveIfDirty(key);
        return true;
    }

    public static boolean removeWaypointById(LocalPlayer player, String id) {
        if (player == null || id == null) return false;
        Waypoint waypoint = findWaypoint(data(player.getUUID()), id);
        if (waypoint == null) waypoint = SharedNavigationClientState.getWaypoint(id);
        return removeWaypoint(player, waypoint);
    }

    public static void addCustomWaypoint(LocalPlayer player, int x, int z, String name) {
        NavigationData data = data(player.getUUID());
        String id = "custom|" + UUID.randomUUID();
        data.waypoints.put(id, new Waypoint(id, player.level().dimension().location().toString(),
                x, player.getBlockY(), z, sanitizeName(name), 0xFF00E5FF, WaypointType.CUSTOM, false));
        data.dirtyWaypointIds.add(id);
        data.dirty = true;
        saveIfDirty(player.getUUID());
    }

    public static boolean importMap(LocalPlayer player, String mapId, String dimension, int centerX, int centerZ, byte scale, byte[] colors) {
        if (player == null || mapId == null || colors == null || colors.length != 128 * 128) return false;

        NavigationStorageKey key = currentStorageKey(player.getUUID());
        NavigationData data = data(key);
        String id = importedMapId(mapId, dimension);
        boolean added = true;

        for (int index = 0; index < data.maps.size(); index++) {
            ImportedMap existing = data.maps.get(index);

            if (existing.id().equals(id)) {
                data.maps.set(index, new ImportedMap(id, dimension, centerX, centerZ, scale, colors.clone()));
                added = false;
                break;
            }
        }

        if (added) {
            data.maps.add(new ImportedMap(id, dimension, centerX, centerZ, scale, colors.clone()));
        }

        stampImportedMap(data, dimension, centerX, centerZ, scale, colors);
        data.dirty = true;
        saveIfDirty(key);
        return added;
    }

    public static void updateExploration(LocalPlayer player, int radius) {
        if (player == null) return;

        NavigationData data = data(player.getUUID());
        Level level = player.level();
        String dimension = level.dimension().location().toString();
        int centerX = Mth.floor(player.getX());
        int centerZ = Mth.floor(player.getZ());
        boolean radiusChanged = data.explorationScan == null || data.explorationRadius != radius;
        if (radiusChanged) data.explorationScan = new NavigationExplorationScan(radius);
        int total = data.explorationScan.size();
        boolean reset = !dimension.equals(data.explorationDimension) || radiusChanged
                || Math.abs((long) centerX - data.explorationX) > radius || Math.abs((long) centerZ - data.explorationZ) > radius;
        if (reset || data.explorationCursor >= total) {
            if (!reset && centerX == data.explorationX && centerZ == data.explorationZ
                    && player.tickCount - data.explorationFinishedTick < 40) return;
            data.explorationDimension = dimension;
            data.explorationX = centerX;
            data.explorationZ = centerZ;
            data.explorationRadius = radius;
            data.explorationCursor = 0;
        }
        // Bound visits as well as samples: explored columns used to allocate thousands
        // of tile-key strings every other tick, even while standing still.
        ExploredTile lastTile = null;
        int lastTileX = Integer.MIN_VALUE;
        int lastTileZ = Integer.MIN_VALUE;
        for (int count = 0; count < 64 && data.explorationCursor < total; count++) {
            int cursor = data.explorationCursor++;
            int dx = data.explorationScan.x(cursor);
            int dz = data.explorationScan.z(cursor);
            int worldX = data.explorationX + dx;
            int worldZ = data.explorationZ + dz;
            int tileX = Math.floorDiv(worldX, EXPLORATION_TILE_SIZE);
            int tileZ = Math.floorDiv(worldZ, EXPLORATION_TILE_SIZE);
            if (lastTile == null || lastTileX != tileX || lastTileZ != tileZ) {
                lastTile = getOrCreateTile(data, dimension, worldX, worldZ);
                lastTileX = tileX;
                lastTileZ = tileZ;
            }
            int localX = Math.floorMod(worldX, EXPLORATION_TILE_SIZE);
            int localZ = Math.floorMod(worldZ, EXPLORATION_TILE_SIZE);
            if (lastTile.isExplored(localX, localZ)) continue;
            int color = sampleTerrainColor(level, worldX, worldZ);
            if (color == 0) continue;
            if (lastTile.setColor(localX, localZ, color, false)) {
                data.dirtyTileKeys.add(tileKey(dimension, tileX, tileZ));
                data.dirty = true;
            }
        }
        if (data.explorationCursor >= total) data.explorationFinishedTick = player.tickCount;
    }

    public static void refreshExploration(LocalPlayer player, int radius) {
        if (player == null) return;

        NavigationData data = data(player.getUUID());
        Level level = player.level();
        String dimension = level.dimension().location().toString();
        int centerX = Mth.floor(player.getX());
        int centerZ = Mth.floor(player.getZ());
        int radiusSqr = radius * radius;
        boolean changed = false;

        for (int worldZ = centerZ - radius; worldZ <= centerZ + radius; worldZ++) {
            int dz = worldZ - centerZ;

            for (int worldX = centerX - radius; worldX <= centerX + radius; worldX++) {
                int dx = worldX - centerX;

                if (dx * dx + dz * dz > radiusSqr) continue;

                ExploredTile tile = getOrCreateTile(data, dimension, worldX, worldZ);
                int localX = Math.floorMod(worldX, EXPLORATION_TILE_SIZE);
                int localZ = Math.floorMod(worldZ, EXPLORATION_TILE_SIZE);
                int color = sampleTerrainColor(level, worldX, worldZ);
                if (color == 0) continue;

                if (tile.setColor(localX, localZ, color, true)) {
                    data.dirtyTileKeys.add(tileKey(dimension, tile.tileX(), tile.tileZ()));
                    changed = true;
                }
            }
        }

        if (changed) {
            data.dirty = true;
        }
    }

    public static void saveIfDirty(UUID playerId) {
        if (playerId == null) return;
        saveIfDirty(currentStorageKey(playerId));
    }

    public static void reload(UUID playerId) {
        if (playerId == null) return;

        NavigationStorageKey key = currentStorageKey(playerId);
        NavigationData existing = CACHE.remove(key);

        if (existing != null) finishSave(key, existing);

        if (existing != null && existing.dirty) {
            save(key, existing);
        }

        CACHE.put(key, load(key));
    }

    public static void invalidate(UUID playerId) {
        if (playerId == null) return;

        Iterator<Map.Entry<NavigationStorageKey, NavigationData>> iterator = CACHE.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<NavigationStorageKey, NavigationData> entry = iterator.next();

            if (!entry.getKey().playerId().equals(playerId)) continue;

            finishSave(entry.getKey(), entry.getValue());
            if (entry.getValue().dirty) {
                save(entry.getKey(), entry.getValue());
            }

            iterator.remove();
        }
    }

    private static NavigationData data(UUID playerId) {
        return data(currentStorageKey(playerId));
    }

    private static NavigationData data(NavigationStorageKey key) {
        return CACHE.computeIfAbsent(key, MinimapWaypointClient::load);
    }

    private static void saveIfDirty(NavigationStorageKey key) {
        NavigationData data = CACHE.get(key);

        if (data == null) return;
        var pending = SAVES.get(key);
        if (pending != null && !pending.isDone()) return;
        finishSave(key, data);
        if (!data.dirty) return;
        NavigationData copy = snapshot(data);
        data.dirty = false;
        SAVES.put(key, java.util.concurrent.CompletableFuture.supplyAsync(() -> save(key, copy), SAVE_WORKER));
    }

    private static Waypoint findWaypoint(NavigationData data, String id) {
        return id == null ? null : data.waypoints.get(id);
    }

    private static boolean removeWaypointInternal(NavigationData data, Waypoint waypoint) {
        if (waypoint == null) return false;

        boolean removed = data.waypoints.remove(waypoint.id()) != null;
        data.dirtyWaypointIds.remove(waypoint.id());

        if (removed && waypoint.id().equals(data.activeWaypointId)) {
            data.activeWaypointId = null;
        }

        return removed;
    }

    private static ExploredTile getOrCreateTile(NavigationData data, String dimension, int worldX, int worldZ) {
        int tileX = Math.floorDiv(worldX, EXPLORATION_TILE_SIZE);
        int tileZ = Math.floorDiv(worldZ, EXPLORATION_TILE_SIZE);
        String key = tileKey(dimension, tileX, tileZ);
        return data.exploredTiles.computeIfAbsent(key, ignored -> new ExploredTile(dimension, tileX, tileZ));
    }

    private static void stampImportedMap(NavigationData data, String dimension, int centerX, int centerZ, byte scale, byte[] colors) {
        int blocksPerPixel = 1 << scale;
        int minWorldX = centerX - 64 * blocksPerPixel;
        int minWorldZ = centerZ - 64 * blocksPerPixel;

        for (int mapZ = 0; mapZ < 128; mapZ++) {
            for (int mapX = 0; mapX < 128; mapX++) {
                byte packedColor = colors[mapZ * 128 + mapX];

                if (packedColor == 0) continue;

                int color = 0xFF000000 | net.minecraft.world.level.material.MapColor.getColorFromPackedId(packedColor);
                int baseWorldX = minWorldX + mapX * blocksPerPixel;
                int baseWorldZ = minWorldZ + mapZ * blocksPerPixel;

                for (int offsetZ = 0; offsetZ < blocksPerPixel; offsetZ++) {
                    for (int offsetX = 0; offsetX < blocksPerPixel; offsetX++) {
                        int worldX = baseWorldX + offsetX;
                        int worldZ = baseWorldZ + offsetZ;
                        ExploredTile tile = getOrCreateTile(data, dimension, worldX, worldZ);
                        int localX = Math.floorMod(worldX, EXPLORATION_TILE_SIZE);
                        int localZ = Math.floorMod(worldZ, EXPLORATION_TILE_SIZE);
                        if (tile.setColor(localX, localZ, color, false)) {
                            data.dirtyTileKeys.add(tileKey(dimension, tile.tileX(), tile.tileZ()));
                        }
                    }
                }
            }
        }
    }

    private record TerrainSample(int color, long tick) {}
    // Packed neighboring coordinates have poor Long.hashCode() distribution (x ^ z).
    // The primitive map mixes the complete key and avoids collision trees and boxing.
    private static final it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<TerrainSample> TERRAIN_CACHE =
            new it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<>(8192);
    private static Level terrainLevel;
    private static final NavigationFrameBudget TERRAIN_BUDGET = new NavigationFrameBudget(256, 500_000L);

    public static void beginNavigationFrame() {
        TERRAIN_BUDGET.beginFrame();
    }

    public static boolean claimNavigationWork() {
        return TERRAIN_BUDGET.claimWork();
    }

    /** Client-thread only. Zero means unavailable, and must not be recorded as explored. */
    public static int sampleTerrainColor(Level level, int worldX, int worldZ) {
        long tick = level.getGameTime();
        if (terrainLevel != level) {
            TERRAIN_CACHE.clear();
            terrainLevel = level;
        }
        long key = ((long) worldX << 32) ^ (worldZ & 0xFFFFFFFFL);
        TerrainSample cached = TERRAIN_CACHE.get(key);
        if (cached != null && tick - cached.tick() < 20) return cached.color();
        if (!level.hasChunk(worldX >> 4, worldZ >> 4)
                || !level.hasChunk((worldX - 1) >> 4, worldZ >> 4)
                || !level.hasChunk(worldX >> 4, (worldZ - 1) >> 4)) return 0;
        if (!TERRAIN_BUDGET.claimSample(System.nanoTime())) return cached != null ? cached.color() : 0;
        int color = computeTerrainColor(level, worldX, worldZ);
        if (TERRAIN_CACHE.size() >= 32768) TERRAIN_CACHE.clear();
        TERRAIN_CACHE.put(key, new TerrainSample(color, tick));
        return color;
    }

    public static void clearTerrainCache() {
        TERRAIN_CACHE.clear();
        terrainLevel = null;
    }

    private static int computeTerrainColor(Level level, int worldX, int worldZ) {
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, worldX, worldZ) - 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(worldX, surfaceY, worldZ);
        BlockState state = level.getBlockState(pos);
        FluidState fluidState = state.getFluidState();

        while (surfaceY > level.getMinBuildHeight() && state.isAir()) {
            surfaceY--;
            pos.set(worldX, surfaceY, worldZ);
            state = level.getBlockState(pos);
            fluidState = state.getFluidState();
        }

        int rgb = state.getMapColor(level, pos).col;

        if (!fluidState.isEmpty()) {
            rgb = fluidState.createLegacyBlock().getMapColor(level, pos).col;
        }

        int brightness = terrainBrightness(level, worldX, surfaceY, worldZ);
        int red = Mth.clamp(((rgb >> 16) & 0xFF) * brightness / 255, 0, 255);
        int green = Mth.clamp(((rgb >> 8) & 0xFF) * brightness / 255, 0, 255);
        int blue = Mth.clamp((rgb & 0xFF) * brightness / 255, 0, 255);

        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    private static int terrainBrightness(Level level, int worldX, int surfaceY, int worldZ) {
        int northHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE, worldX, worldZ - 1) - 1;
        int westHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE, worldX - 1, worldZ) - 1;
        int difference = surfaceY - northHeight + surfaceY - westHeight;

        if (difference >= 4) return 310;
        if (difference >= 2) return 285;
        if (difference >= 1) return 270;
        if (difference <= -4) return 160;
        if (difference <= -2) return 185;
        if (difference <= -1) return 210;

        return 240;
    }

    private static NavigationStorageKey currentStorageKey(UUID playerId) {
        return new NavigationStorageKey(playerId, resolveWorldId());
    }

    public static void clearWorldIdentityCache() {
        cachedWorldId = null;
    }

    private static String resolveWorldId() {
        if (cachedWorldId != null) {
            return cachedWorldId;
        }

        Minecraft mc = Minecraft.getInstance();
        MinecraftServer singleplayerServer = mc.getSingleplayerServer();

        if (singleplayerServer != null) {
            try {
                Path worldPath = singleplayerServer.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
                return cachedWorldId = hashWorldIdentity("singleplayer|" + worldPath);
            } catch (Throwable ignored) {
            }
        }

        ServerData serverData = mc.getCurrentServer();

        if (serverData != null && serverData.ip != null && !serverData.ip.isBlank()) {
            return cachedWorldId = hashWorldIdentity("multiplayer|" + serverData.ip.trim().toLowerCase());
        }

        if (mc.getConnection() != null) {
            try {
                SocketAddress remoteAddress = mc.getConnection().getConnection().getRemoteAddress();

                if (remoteAddress != null) {
                    return cachedWorldId = hashWorldIdentity("connection|" + remoteAddress);
                }
            } catch (Throwable ignored) {
            }
        }

        return cachedWorldId = hashWorldIdentity("unknown_world");
    }

    private static String hashWorldIdentity(String identity) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(identity.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 16);
        } catch (Throwable ignored) {
            return Integer.toUnsignedString(identity.hashCode(), 16);
        }
    }

    private static String sanitizeName(String name) {
        if (name == null || name.isBlank()) return Component.translatable("gui.createcybernetics.navigation.default_waypoint").getString();

        String sanitized = name.trim();

        if (sanitized.length() > 48) {
            sanitized = sanitized.substring(0, 48);
        }

        return sanitized;
    }

    private static String formatExplorerName(String path) {
        String[] words = path.replace('/', '_').split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.isBlank()) continue;

            if (!result.isEmpty()) {
                result.append(' ');
            }

            result.append(Character.toUpperCase(word.charAt(0)));

            if (word.length() > 1) {
                result.append(word.substring(1));
            }
        }

        return result.isEmpty() ? Component.translatable("gui.createcybernetics.navigation.explorer.unknown").getString() : result.toString();
    }

    private static String bannerWaypointId(String dimension, BlockPos pos) {
        return "banner|" + dimension + "|" + pos.getX() + "|" + pos.getY() + "|" + pos.getZ();
    }

    private static String mapBannerWaypointId(String dimension, BlockPos pos) {
        return "map_banner|" + dimension + "|" + pos.getX() + "|" + pos.getY() + "|" + pos.getZ();
    }

    private static String explorerWaypointId(String dimension, int x, int z, String sourceId, String typeId) {
        return "explorer|" + dimension + "|" + x + "|" + z + "|" + sourceId + "|" + typeId;
    }

    private static String importedMapId(String mapId, String dimension) {
        return "map|" + dimension + "|" + mapId;
    }

    private static String tileKey(String dimension, int tileX, int tileZ) {
        return dimension + "|" + tileX + "|" + tileZ;
    }

    private static Path fileFor(NavigationStorageKey key) {
        Path directory = FMLPaths.CONFIGDIR.get().resolve(CreateCybernetics.MODID).resolve("navigation").resolve(key.worldId());
        return directory.resolve("navigation_data_" + key.playerId() + ".json");
    }

    private static NavigationData load(NavigationStorageKey key) {
        Path file = fileFor(key);

        if (!Files.exists(file)) {
            return new NavigationData();
        }

        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);

            if (root == null) {
                return new NavigationData();
            }

            NavigationData data = new NavigationData();

            if (root.has("hiddenWaypointIds") && root.get("hiddenWaypointIds").isJsonArray()) {
                for (JsonElement id : root.getAsJsonArray("hiddenWaypointIds")) {
                    if (id.isJsonPrimitive() && id.getAsJsonPrimitive().isString()) data.hiddenWaypointIds.add(id.getAsString());
                }
            }

            if (root.has("activeWaypointId") && !root.get("activeWaypointId").isJsonNull()) {
                data.activeWaypointId = root.get("activeWaypointId").getAsString();
            }

            if (root.has("waypoints") && root.get("waypoints").isJsonArray()) {
                JsonArray waypoints = root.getAsJsonArray("waypoints");

                for (JsonElement element : waypoints) {
                    if (!element.isJsonObject()) continue;

                    JsonObject object = element.getAsJsonObject();

                    try {
                        String id = object.get("id").getAsString();
                        String dimension = object.get("dimension").getAsString();
                        int x = object.get("x").getAsInt();
                        int y = object.get("y").getAsInt();
                        int z = object.get("z").getAsInt();
                        String name = object.get("name").getAsString();
                        int color = object.get("color").getAsInt();
                        String typeName = object.get("type").getAsString();
                        WaypointType type = typeName.equals("TREASURE") ? WaypointType.EXPLORER : WaypointType.valueOf(typeName);
                        boolean autoClear = object.get("autoClear").getAsBoolean();

                        if (typeName.equals("TREASURE") && ResourceLocation.tryParse(name) == null) {
                            name = "minecraft:red_x";
                        }

                        data.waypoints.put(id, new Waypoint(id, dimension, x, y, z, sanitizeName(name), color, type, autoClear));
                    } catch (Throwable ignored) {
                    }
                }
            }

            if (root.has("maps") && root.get("maps").isJsonArray()) {
                JsonArray maps = root.getAsJsonArray("maps");

                for (JsonElement element : maps) {
                    if (!element.isJsonObject()) continue;

                    JsonObject object = element.getAsJsonObject();

                    try {
                        String id = object.get("id").getAsString();
                        String dimension = object.get("dimension").getAsString();
                        int centerX = object.get("centerX").getAsInt();
                        int centerZ = object.get("centerZ").getAsInt();
                        byte scale = object.get("scale").getAsByte();
                        byte[] colors = Base64.getDecoder().decode(object.get("colors").getAsString());

                        if (colors.length == 128 * 128) {
                            data.maps.add(new ImportedMap(id, dimension, centerX, centerZ, scale, colors));
                        }
                    } catch (Throwable ignored) {
                    }
                }
            }

            if (root.has("exploredTiles") && root.get("exploredTiles").isJsonArray()) {
                JsonArray exploredTiles = root.getAsJsonArray("exploredTiles");

                for (JsonElement element : exploredTiles) {
                    if (!element.isJsonObject()) continue;

                    JsonObject object = element.getAsJsonObject();

                    try {
                        String dimension = object.get("dimension").getAsString();
                        int tileX = object.get("tileX").getAsInt();
                        int tileZ = object.get("tileZ").getAsInt();
                        int[] colors = decodeColors(object.get("colors").getAsString());
                        BitSet explored = BitSet.valueOf(Base64.getDecoder().decode(object.get("explored").getAsString()));

                        if (colors.length == EXPLORATION_TILE_SIZE * EXPLORATION_TILE_SIZE) {
                            data.exploredTiles.put(tileKey(dimension, tileX, tileZ), new ExploredTile(dimension, tileX, tileZ, colors, explored));
                        }
                    } catch (Throwable ignored) {
                    }
                }
            }

            data.dirty = false;
            return data;
        } catch (Throwable ignored) {
            return new NavigationData();
        }
    }

    private static boolean save(NavigationStorageKey key, NavigationData data) {
        Path file = fileFor(key);

        try {
            Files.createDirectories(file.getParent());

            JsonObject root = new JsonObject();
            root.addProperty("worldId", key.worldId());
            root.addProperty("playerId", key.playerId().toString());
            JsonArray hidden = new JsonArray();
            for (String id : data.hiddenWaypointIds) hidden.add(id);
            root.add("hiddenWaypointIds", hidden);

            if (data.activeWaypointId != null) {
                root.addProperty("activeWaypointId", data.activeWaypointId);
            }

            JsonArray waypoints = new JsonArray();

            for (Waypoint waypoint : data.waypoints.values()) {
                JsonObject object = new JsonObject();

                object.addProperty("id", waypoint.id());
                object.addProperty("dimension", waypoint.dimension());
                object.addProperty("x", waypoint.x());
                object.addProperty("y", waypoint.y());
                object.addProperty("z", waypoint.z());
                object.addProperty("name", waypoint.name());
                object.addProperty("color", waypoint.color());
                object.addProperty("type", waypoint.type().name());
                object.addProperty("autoClear", waypoint.autoClear());

                waypoints.add(object);
            }

            root.add("waypoints", waypoints);

            JsonArray maps = new JsonArray();

            for (ImportedMap importedMap : data.maps) {
                JsonObject object = new JsonObject();

                object.addProperty("id", importedMap.id());
                object.addProperty("dimension", importedMap.dimension());
                object.addProperty("centerX", importedMap.centerX());
                object.addProperty("centerZ", importedMap.centerZ());
                object.addProperty("scale", importedMap.scale());
                object.addProperty("colors", Base64.getEncoder().encodeToString(importedMap.colors()));

                maps.add(object);
            }

            root.add("maps", maps);

            JsonArray exploredTiles = new JsonArray();

            for (ExploredTile tile : data.exploredTiles.values()) {
                JsonObject object = new JsonObject();

                object.addProperty("dimension", tile.dimension());
                object.addProperty("tileX", tile.tileX());
                object.addProperty("tileZ", tile.tileZ());
                object.addProperty("colors", encodeColors(tile.colors));
                object.addProperty("explored", Base64.getEncoder().encodeToString(tile.explored.toByteArray()));

                exploredTiles.add(object);
            }

            root.add("exploredTiles", exploredTiles);

            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                GSON.toJson(root, writer);
            }

            try {
                Files.move(temporary, file, java.nio.file.StandardCopyOption.ATOMIC_MOVE, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            data.dirty = false;
            return true;
        } catch (Exception failure) {
            CreateCybernetics.LOGGER.warn("Failed to save navigation map {}", file, failure);
            return false;
        }
    }

    private static String encodeColors(int[] colors) {
        ByteBuffer buffer = ByteBuffer.allocate(colors.length * Integer.BYTES);

        for (int color : colors) {
            buffer.putInt(color);
        }

        return Base64.getEncoder().encodeToString(buffer.array());
    }

    private static int[] decodeColors(String encoded) {
        byte[] bytes = Base64.getDecoder().decode(encoded);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int[] colors = new int[bytes.length / Integer.BYTES];

        for (int index = 0; index < colors.length; index++) {
            colors[index] = buffer.getInt();
        }

        return colors;
    }
}
