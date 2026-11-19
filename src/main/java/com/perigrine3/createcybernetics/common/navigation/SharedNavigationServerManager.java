package com.perigrine3.createcybernetics.common.navigation;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.item.generic.SharedNavigationShardData;
import com.perigrine3.createcybernetics.network.payload.SharedNavigationClearPayload;
import com.perigrine3.createcybernetics.network.payload.SharedNavigationPlayersPayload;
import com.perigrine3.createcybernetics.network.payload.SharedNavigationTileSyncPayload;
import com.perigrine3.createcybernetics.network.payload.SharedNavigationWaypointSyncPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class SharedNavigationServerManager {

    private SharedNavigationServerManager() {}

    private static final int PLAYER_SYNC_INTERVAL = 20;
    private record TileKey(UUID network, String dimension, int x, int z) {}
    private static final Map<UUID, java.util.Iterator<SharedNavigationMapData.SharedTile>> FULL_SYNCS = new java.util.LinkedHashMap<>();
    private static final Map<UUID, Set<TileKey>> DELIVERIES = new java.util.LinkedHashMap<>();
    private static final Map<UUID, Integer> LAST_UPLOAD_TICK = new HashMap<>();
    private static int roundRobin;
    private static final Map<UUID, java.util.Iterator<SharedNavigationMapData.SharedWaypoint>> WAYPOINT_SYNCS = new HashMap<>();

    public static boolean allowTileUpload(ServerPlayer player) {
        int tick = player.getServer().getTickCount();
        Integer previous = LAST_UPLOAD_TICK.put(player.getUUID(), tick);
        return previous == null || previous != tick;
    }

    @SubscribeEvent
    public static void onServerStopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event) {
        LAST_PLAYER_NETWORKS.clear();
        FORCE_FULL_SYNC_NETWORKS.clear();
        FULL_SYNCS.clear();
        WAYPOINT_SYNCS.clear();
        DELIVERIES.clear();
        LAST_UPLOAD_TICK.clear();
    }

    private static final Map<UUID, UUID> LAST_PLAYER_NETWORKS = new HashMap<>();
    private static final Set<UUID> FORCE_FULL_SYNC_NETWORKS = new HashSet<>();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();

        if (server.getTickCount() % PLAYER_SYNC_INTERVAL == 0) {
            syncPlayers(server);
        }
        flushTiles(server);
    }

    private static void syncPlayers(MinecraftServer server) {
        Map<UUID, List<ServerPlayer>> networkMembers = collectNetworkMembers(server);
        Map<UUID, UUID> currentPlayerNetworks = new HashMap<>();
        Map<UUID, List<SharedNavigationPlayersPayload.SharedPlayerData>> positions = new HashMap<>();

        for (Map.Entry<UUID, List<ServerPlayer>> entry : networkMembers.entrySet()) {
            List<SharedNavigationPlayersPayload.SharedPlayerData> list = new ArrayList<>();
            positions.put(entry.getKey(), list);
            for (ServerPlayer member : entry.getValue()) {
                list.add(new SharedNavigationPlayersPayload.SharedPlayerData(member.getUUID(), member.getGameProfile().getName(), member.level().dimension().location().toString(), member.getX(), member.getY(), member.getZ(), member.getYRot()));
                currentPlayerNetworks.put(member.getUUID(), entry.getKey());
            }
        }

        Set<UUID> forcedNetworks = new HashSet<>(FORCE_FULL_SYNC_NETWORKS);
        FORCE_FULL_SYNC_NETWORKS.removeAll(forcedNetworks);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID previousNetwork = LAST_PLAYER_NETWORKS.get(player.getUUID());
            UUID currentNetwork = currentPlayerNetworks.get(player.getUUID());

            if (currentNetwork == null) {
                if (previousNetwork != null) {
                    PacketDistributor.sendToPlayer(player, new SharedNavigationClearPayload());
                }

                continue;
            }

            var playerData = positions.get(currentNetwork);

            boolean networkChanged = !currentNetwork.equals(previousNetwork);
            boolean forceFullSync = forcedNetworks.contains(currentNetwork);

            if (networkChanged || forceFullSync) {
                PacketDistributor.sendToPlayer(player, new SharedNavigationClearPayload());
            }

            PacketDistributor.sendToPlayer(player, new SharedNavigationPlayersPayload(currentNetwork, playerData));

            if (networkChanged || forceFullSync) {
                sendFullMap(player, currentNetwork);
            }
        }

        FULL_SYNCS.keySet().retainAll(currentPlayerNetworks.keySet());
        WAYPOINT_SYNCS.keySet().retainAll(currentPlayerNetworks.keySet());
        DELIVERIES.keySet().retainAll(currentPlayerNetworks.keySet());
        LAST_UPLOAD_TICK.keySet().retainAll(currentPlayerNetworks.keySet());
        LAST_PLAYER_NETWORKS.clear();
        LAST_PLAYER_NETWORKS.putAll(currentPlayerNetworks);
    }

    private static void sendFullMap(ServerPlayer player, UUID networkId) {
        SharedNavigationMapData data = SharedNavigationMapData.get(player.getServer());

        DELIVERIES.remove(player.getUUID());
        FULL_SYNCS.put(player.getUUID(), data.getTiles(networkId).iterator());

        WAYPOINT_SYNCS.put(player.getUUID(), data.getWaypoints(networkId).iterator());
    }

    public static void broadcastTileUpdate(MinecraftServer server, UUID networkId, String dimension, int tileX, int tileZ) {
        if (server == null || networkId == null) return;

        TileKey key = new TileKey(networkId, dimension, tileX, tileZ);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!networkId.equals(LAST_PLAYER_NETWORKS.get(player.getUUID()))) continue;
            Set<TileKey> queue = DELIVERIES.computeIfAbsent(player.getUUID(), ignored -> new java.util.LinkedHashSet<>());
            if (queue.size() >= 256) {
                queue.clear();
                FULL_SYNCS.put(player.getUUID(), SharedNavigationMapData.get(server).getTiles(networkId).iterator());
            } else queue.add(key);
        }
    }

    // At most eight terrain tiles (~128 KiB before compression) per tick globally,
    // and two per player. Rotate the first recipient to keep large histories fair.
    private static void flushTiles(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty() || (FULL_SYNCS.isEmpty() && DELIVERIES.isEmpty() && WAYPOINT_SYNCS.isEmpty())) return;
        int budget = 8;
        int start = Math.floorMod(roundRobin++, players.size());
        SharedNavigationMapData data = SharedNavigationMapData.get(server);
        for (int offset = 0; offset < players.size() && budget > 0; offset++) {
            ServerPlayer player = players.get((start + offset) % players.size());
            UUID network = LAST_PLAYER_NETWORKS.get(player.getUUID());
            if (network == null || !hasFunctioningNavigationChip(player)) continue;
            var waypoints = WAYPOINT_SYNCS.get(player.getUUID());
            for (int count = 0; count < 8 && waypoints != null && waypoints.hasNext(); count++) {
                var waypoint = waypoints.next();
                PacketDistributor.sendToPlayer(player, new SharedNavigationWaypointSyncPayload(waypoint.id(), waypoint.dimension(), waypoint.x(), waypoint.y(), waypoint.z(), waypoint.name(), waypoint.color(), waypoint.type()));
            }
            if (waypoints != null && !waypoints.hasNext()) WAYPOINT_SYNCS.remove(player.getUUID());
            for (int count = 0; count < 2 && budget > 0; count++) {
                SharedNavigationMapData.SharedTile tile = null;
                Set<TileKey> queue = DELIVERIES.get(player.getUUID());
                if (queue != null && !queue.isEmpty()) {
                    var keys = queue.iterator();
                    TileKey key = keys.next();
                    keys.remove();
                    if (queue.isEmpty()) DELIVERIES.remove(player.getUUID());
                    if (key.network().equals(network)) tile = data.getTile(network, key.dimension(), key.x(), key.z());
                } else {
                    var history = FULL_SYNCS.get(player.getUUID());
                    if (history != null && history.hasNext()) tile = history.next();
                    if (history != null && !history.hasNext()) FULL_SYNCS.remove(player.getUUID());
                }
                if (tile != null) {
                    PacketDistributor.sendToPlayer(player, new SharedNavigationTileSyncPayload(tile.dimension(), tile.tileX(), tile.tileZ(), tile.colors().clone(), tile.explored().toLongArray()));
                    budget--;
                }
            }
        }
    }

    public static void broadcastWaypointUpdate(MinecraftServer server, UUID networkId, String waypointId) {
        if (server == null || networkId == null || waypointId == null) return;

        SharedNavigationMapData data = SharedNavigationMapData.get(server);
        SharedNavigationMapData.SharedWaypoint waypoint = data.getWaypoint(networkId, waypointId);

        if (waypoint == null) return;

        SharedNavigationWaypointSyncPayload payload = new SharedNavigationWaypointSyncPayload(waypoint.id(), waypoint.dimension(), waypoint.x(), waypoint.y(), waypoint.z(), waypoint.name(), waypoint.color(), waypoint.type());

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID playerNetwork = findActiveNetwork(player);

            if (!networkId.equals(playerNetwork)) continue;

            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    public static void markNetworkForFullSync(UUID networkId) {
        if (networkId != null) {
            FORCE_FULL_SYNC_NETWORKS.add(networkId);
        }
    }

    private static Map<UUID, List<ServerPlayer>> collectNetworkMembers(MinecraftServer server) {
        Map<UUID, List<ServerPlayer>> networkMembers = new HashMap<>();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID networkId = findActiveNetwork(player);

            if (networkId == null) continue;

            networkMembers.computeIfAbsent(networkId, ignored -> new ArrayList<>()).add(player);
        }

        return networkMembers;
    }

    public static UUID findActiveNetwork(ServerPlayer player) {
        if (player == null) return null;
        if (!hasFunctioningNavigationChip(player)) return null;

        PlayerCyberwareData cyberwareData = player.getData(ModAttachments.CYBERWARE);
        SharedNavigationMapData sharedData = SharedNavigationMapData.get(player.getServer());

        for (int slot = 0; slot < PlayerCyberwareData.CHIPWARE_SLOT_COUNT; slot++) {
            ItemStack stack = cyberwareData.getChipwareStack(slot);

            if (stack == null || stack.isEmpty()) continue;
            if (!stack.is(ModItems.DATA_SHARD_SHARED_NAVIGATION.get())) continue;

            ItemStack updated = stack.copy();
            updated.setCount(1);
            boolean changed = false;

            UUID networkId = SharedNavigationShardData.getNetworkId(updated);

            if (networkId == null) {
                networkId = SharedNavigationShardData.getOrCreateNetworkId(updated);
                changed = true;
            }

            UUID canonicalNetwork = sharedData.resolveNetworkId(networkId);

            if (!canonicalNetwork.equals(networkId)) {
                SharedNavigationShardData.setNetworkId(updated, canonicalNetwork);
                networkId = canonicalNetwork;
                changed = true;
            }

            UUID pendingMergeSource = SharedNavigationShardData.getPendingMergeSource(updated);

            if (pendingMergeSource != null) {
                sharedData.mergeNetworks(networkId, pendingMergeSource);
                canonicalNetwork = sharedData.resolveNetworkId(networkId);
                SharedNavigationShardData.setNetworkId(updated, canonicalNetwork);
                SharedNavigationShardData.clearPendingMergeSource(updated);
                networkId = canonicalNetwork;
                markNetworkForFullSync(networkId);
                changed = true;
            }

            if (changed) {
                cyberwareData.setChipwareStack(slot, updated);
                cyberwareData.setDirty();
                player.syncData(ModAttachments.CYBERWARE);
            }

            return networkId;
        }

        return null;
    }

    private static boolean hasFunctioningNavigationChip(ServerPlayer player) {
        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);

        if (data == null) return false;

        InstalledCyberware[] installedEyes = data.getAll().get(CyberwareSlot.EYES);

        if (installedEyes == null) return false;

        for (int index = 0; index < installedEyes.length; index++) {
            InstalledCyberware installed = installedEyes[index];

            if (installed == null) continue;

            ItemStack stack = installed.getItem();

            if (stack == null || stack.isEmpty()) continue;
            if (!stack.is(ModItems.EYEUPGRADES_NAVIGATIONCHIP.get())) continue;
            if (!data.isEnabled(CyberwareSlot.EYES, index)) continue;

            if (stack.getItem() instanceof ICyberwareItem cyberwareItem && cyberwareItem.requiresEnergyToFunction(player, stack, CyberwareSlot.EYES) && !installed.isPowered()) continue;

            return true;
        }

        return false;
    }
}
