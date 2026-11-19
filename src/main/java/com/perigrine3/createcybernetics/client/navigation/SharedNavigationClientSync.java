package com.perigrine3.createcybernetics.client.navigation;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.client.MinimapWaypointClient;
import com.perigrine3.createcybernetics.network.payload.SharedNavigationTileUploadPayload;
import com.perigrine3.createcybernetics.network.payload.SharedNavigationWaypointUploadPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class SharedNavigationClientSync {

    private SharedNavigationClientSync() {}

    private static final int DIRTY_BATCH_SIZE = 8;

    private static final Queue<MinimapWaypointClient.NavigationTileSnapshot> TILE_QUEUE = new ArrayDeque<>();
    private static final Queue<MinimapWaypointClient.NavigationWaypointSnapshot> WAYPOINT_QUEUE = new ArrayDeque<>();

    private static UUID queuedNetworkId;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        UUID networkId = SharedNavigationClientState.getNetworkId();

        if (player == null || networkId == null || minecraft.getConnection() == null) {
            clearQueues();
            return;
        }

        if (!networkId.equals(queuedNetworkId)) {
            clearQueues();
            queuedNetworkId = networkId;

            MinimapWaypointClient.markAllSharedSyncDirty(player);
        }

        if (TILE_QUEUE.isEmpty()) {
            TILE_QUEUE.addAll(MinimapWaypointClient.drainDirtyTileSnapshots(player, DIRTY_BATCH_SIZE));
        }

        if (WAYPOINT_QUEUE.isEmpty()) {
            WAYPOINT_QUEUE.addAll(MinimapWaypointClient.drainDirtyWaypointSnapshots(player, DIRTY_BATCH_SIZE));
        }

        if (player.tickCount % 5 != 0) return;

        // Waypoints should not wait behind a large exploration history.
        if (!WAYPOINT_QUEUE.isEmpty() && (TILE_QUEUE.isEmpty() || player.tickCount % 20 == 0)) {
            var waypoint = WAYPOINT_QUEUE.poll();
            PacketDistributor.sendToServer(new SharedNavigationWaypointUploadPayload(networkId, waypoint.id(), waypoint.dimension(), waypoint.x(), waypoint.y(), waypoint.z(), waypoint.name(), waypoint.color(), waypoint.type()));
            return;
        }
        MinimapWaypointClient.NavigationTileSnapshot tile = TILE_QUEUE.poll();

        if (tile != null) {
            PacketDistributor.sendToServer(new SharedNavigationTileUploadPayload(networkId, tile.dimension(), tile.tileX(), tile.tileZ(), tile.colors(), tile.explored()));
            return;
        }

        MinimapWaypointClient.NavigationWaypointSnapshot waypoint = WAYPOINT_QUEUE.poll();

        if (waypoint != null) {
            PacketDistributor.sendToServer(new SharedNavigationWaypointUploadPayload(networkId, waypoint.id(), waypoint.dimension(), waypoint.x(), waypoint.y(), waypoint.z(), waypoint.name(), waypoint.color(), waypoint.type()));
        }
    }

    public static void clearQueues() {
        TILE_QUEUE.clear();
        WAYPOINT_QUEUE.clear();
        queuedNetworkId = null;
    }
}
