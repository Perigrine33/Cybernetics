package com.perigrine3.createcybernetics.network.payload;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.common.navigation.SharedNavigationMapData;
import com.perigrine3.createcybernetics.common.navigation.SharedNavigationServerManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record SharedNavigationWaypointUploadPayload(UUID networkId, String id, String dimension, int x, int y, int z, String name, int color, String waypointType) implements CustomPacketPayload {

    public static final Type<SharedNavigationWaypointUploadPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "shared_navigation_waypoint_upload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SharedNavigationWaypointUploadPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeUUID(payload.networkId());
                buffer.writeUtf(payload.id());
                buffer.writeUtf(payload.dimension());
                buffer.writeInt(payload.x());
                buffer.writeInt(payload.y());
                buffer.writeInt(payload.z());
                buffer.writeUtf(payload.name());
                buffer.writeInt(payload.color());
                buffer.writeUtf(payload.waypointType());
            },
            buffer -> new SharedNavigationWaypointUploadPayload(buffer.readUUID(), buffer.readUtf(), buffer.readUtf(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readUtf(), buffer.readInt(), buffer.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SharedNavigationWaypointUploadPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            UUID activeNetwork = SharedNavigationServerManager.findActiveNetwork(player);

            if (activeNetwork == null || !activeNetwork.equals(payload.networkId())) return;
            if (payload.id() == null || payload.id().isBlank() || payload.id().length() > 512) return;
            if (payload.dimension() == null || payload.dimension().isBlank() || payload.dimension().length() > 256) return;
            if (payload.name() == null || payload.name().length() > 256) return;
            if (payload.waypointType() == null) return;
            if (!payload.waypointType().equals("BANNER") && !payload.waypointType().equals("MAP_BANNER") && !payload.waypointType().equals("EXPLORER") && !payload.waypointType().equals("CUSTOM")) return;

            SharedNavigationMapData data = SharedNavigationMapData.get(player.getServer());

            if (data.mergeWaypoint(payload.networkId(), new SharedNavigationMapData.SharedWaypoint(payload.id(), payload.dimension(), payload.x(), payload.y(), payload.z(), payload.name(), payload.color(), payload.waypointType()))) {
                SharedNavigationServerManager.broadcastWaypointUpdate(player.getServer(), payload.networkId(), payload.id());
            }
        });
    }
}
