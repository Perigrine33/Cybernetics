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

import java.util.BitSet;
import java.util.UUID;

public record SharedNavigationTileUploadPayload(UUID networkId, String dimension, int tileX, int tileZ, int[] colors, long[] explored) implements CustomPacketPayload {

    public static final Type<SharedNavigationTileUploadPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "shared_navigation_tile_upload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SharedNavigationTileUploadPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeUUID(payload.networkId());
                buffer.writeUtf(payload.dimension());
                buffer.writeInt(payload.tileX());
                buffer.writeInt(payload.tileZ());
                buffer.writeVarInt(payload.colors().length);

                for (int color : payload.colors()) {
                    buffer.writeInt(color);
                }

                buffer.writeVarInt(payload.explored().length);

                for (long word : payload.explored()) {
                    buffer.writeLong(word);
                }
            },
            buffer -> {
                UUID networkId = buffer.readUUID();
                String dimension = buffer.readUtf();
                int tileX = buffer.readInt();
                int tileZ = buffer.readInt();
                int colorCount = buffer.readVarInt();

                if (colorCount < 0 || colorCount > SharedNavigationMapData.TILE_PIXELS) {
                    throw new IllegalArgumentException("Invalid shared navigation tile color count");
                }

                int[] colors = new int[colorCount];

                for (int index = 0; index < colorCount; index++) {
                    colors[index] = buffer.readInt();
                }

                int exploredCount = buffer.readVarInt();

                if (exploredCount < 0 || exploredCount > 64) {
                    throw new IllegalArgumentException("Invalid shared navigation explored word count");
                }

                long[] explored = new long[exploredCount];

                for (int index = 0; index < exploredCount; index++) {
                    explored[index] = buffer.readLong();
                }

                return new SharedNavigationTileUploadPayload(networkId, dimension, tileX, tileZ, colors, explored);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SharedNavigationTileUploadPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            if (!SharedNavigationServerManager.allowTileUpload(player)) return;
            UUID activeNetwork = SharedNavigationServerManager.findActiveNetwork(player);

            if (activeNetwork == null || !activeNetwork.equals(payload.networkId())) return;
            if (payload.dimension() == null || payload.dimension().isBlank() || payload.dimension().length() > 256) return;
            if (Math.abs((long) payload.tileX()) > 1000000L || Math.abs((long) payload.tileZ()) > 1000000L) return;
            if (payload.colors() == null || payload.colors().length != SharedNavigationMapData.TILE_PIXELS) return;
            if (payload.explored() == null || payload.explored().length > 64) return;

            BitSet explored = BitSet.valueOf(payload.explored());

            if (explored.length() > SharedNavigationMapData.TILE_PIXELS) return;

            SharedNavigationMapData data = SharedNavigationMapData.get(player.getServer());

            if (data.mergeTile(payload.networkId(), payload.dimension(), payload.tileX(), payload.tileZ(), payload.colors(), explored)) {
                SharedNavigationServerManager.broadcastTileUpdate(player.getServer(), payload.networkId(), payload.dimension(), payload.tileX(), payload.tileZ());
            }
        });
    }
}
