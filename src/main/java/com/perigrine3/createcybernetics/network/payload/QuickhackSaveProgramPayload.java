package com.perigrine3.createcybernetics.network.payload;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.common.quickhack.QuickhackProgramData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record QuickhackSaveProgramPayload(BlockPos computerPos, int towerSlot, String name, List<String> nodeIds) implements CustomPacketPayload {
    public static final Type<QuickhackSaveProgramPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "quickhack_save_program"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuickhackSaveProgramPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeBlockPos(payload.computerPos());
                buffer.writeVarInt(payload.towerSlot());
                buffer.writeUtf(payload.name(), QuickhackProgramData.MAX_NAME_LENGTH);
                buffer.writeVarInt(payload.nodeIds().size());

                for (String nodeId : payload.nodeIds()) {
                    buffer.writeUtf(nodeId, 64);
                }
            },
            buffer -> {
                BlockPos computerPos = buffer.readBlockPos();
                int towerSlot = buffer.readVarInt();
                String name = buffer.readUtf(QuickhackProgramData.MAX_NAME_LENGTH);
                int nodeCount = Math.max(0, Math.min(10, buffer.readVarInt()));
                List<String> nodeIds = new ArrayList<>();

                for (int index = 0; index < nodeCount; index++) {
                    nodeIds.add(buffer.readUtf(64));
                }

                return new QuickhackSaveProgramPayload(computerPos, towerSlot, name, List.copyOf(nodeIds));
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
