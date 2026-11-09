package com.perigrine3.createcybernetics.network.handler;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.block.entity.ComputerTowerBlockEntity;
import com.perigrine3.createcybernetics.common.quickhack.MicroprogramDefinition;
import com.perigrine3.createcybernetics.common.quickhack.MicroprogramRegistry;
import com.perigrine3.createcybernetics.common.quickhack.QuickhackProgramData;
import com.perigrine3.createcybernetics.component.MicroprogramLibrary;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.item.generic.MicroprogramCreativeShardItem;
import com.perigrine3.createcybernetics.item.generic.MicroprogramDataShardItem;
import com.perigrine3.createcybernetics.item.generic.QuickhackShardItem;
import com.perigrine3.createcybernetics.network.payload.QuickhackSaveProgramPayload;
import com.perigrine3.createcybernetics.screen.custom.computer.ComputerMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class QuickhackSaveProgramHandler {
    private QuickhackSaveProgramHandler() {}

    public static void handle(QuickhackSaveProgramPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof ComputerMenu menu)) {
            return;
        }

        if (!menu.getComputerPos().equals(payload.computerPos())) {
            return;
        }

        if (player.distanceToSqr(payload.computerPos().getX() + 0.5D, payload.computerPos().getY() + 0.5D, payload.computerPos().getZ() + 0.5D) > 64.0D) {
            return;
        }

        BlockEntity blockEntity = player.level().getBlockEntity(payload.computerPos().below());

        if (!(blockEntity instanceof ComputerTowerBlockEntity tower)) {
            return;
        }

        tower.forceGenerateLootFromBlockNbt(player);

        int slot = payload.towerSlot();

        if (slot < 0 || slot >= ComputerTowerBlockEntity.INVENTORY_SIZE) {
            return;
        }

        ItemStack currentStack = tower.getItem(slot);

        if (currentStack.isEmpty() || !(currentStack.getItem() instanceof QuickhackShardItem)) {
            return;
        }

        String name = QuickhackProgramData.sanitizeName(payload.name());

        if (name.isBlank()) {
            return;
        }

        if (payload.nodeIds().isEmpty() || payload.nodeIds().size() > QuickhackProgramData.MAX_PROGRAM_NODES) {
            return;
        }

        Set<ResourceLocation> availableMicroprograms = getAvailableMicroprograms(tower);

        if (availableMicroprograms.isEmpty()) {
            return;
        }

        List<MicroprogramDefinition> nodes = new ArrayList<>();

        for (String nodeId : payload.nodeIds()) {
            MicroprogramDefinition definition = MicroprogramRegistry.get(nodeId);

            if (definition == null) {
                return;
            }

            ResourceLocation microprogramId = ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, definition.id());

            if (!availableMicroprograms.contains(microprogramId)) {
                return;
            }

            nodes.add(definition);
        }

        if (!QuickhackProgramData.isValidProgram(nodes)) {
            return;
        }

        ItemStack updatedStack = currentStack.copy();
        QuickhackProgramData.setProgram(updatedStack, name, nodes);
        tower.setItem(slot, updatedStack);
    }

    private static Set<ResourceLocation> getAvailableMicroprograms(ComputerTowerBlockEntity tower) {
        Set<ResourceLocation> availableMicroprograms = new HashSet<>();

        for (int slot = 0; slot < ComputerTowerBlockEntity.INVENTORY_SIZE; slot++) {
            ItemStack stack = tower.getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            MicroprogramLibrary library;
            if (stack.is(ModItems.DATA_SHARD_MICROPROGRAMS.get())) {
                library = MicroprogramDataShardItem.getLibrary(stack);
            } else if (stack.getItem() instanceof MicroprogramCreativeShardItem) {
                library = MicroprogramCreativeShardItem.getLibrary(stack);
            } else {
                continue;
            }

            availableMicroprograms.addAll(library.microprograms());
        }

        return availableMicroprograms;
    }
}
