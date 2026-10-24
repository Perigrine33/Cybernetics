package com.perigrine3.createcybernetics.common.capabilities;

import com.perigrine3.createcybernetics.block.ModBlocks;
import com.perigrine3.createcybernetics.block.entity.ChargingBlockEntity;
import com.perigrine3.createcybernetics.block.entity.ComputerBlockEntity;
import com.perigrine3.createcybernetics.block.entity.ModBlockEntities;
import com.perigrine3.createcybernetics.block.entity.RobosurgeonBlockEntity;
import com.perigrine3.createcybernetics.block.entity.ServerTowerBlockEntity;
import com.perigrine3.createcybernetics.common.energy.ConditionalBlockPower;
import com.perigrine3.createcybernetics.common.multiblock.ServerTowerMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;

public final class ModBlockCapabilities {
    private ModBlockCapabilities() {}

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.CHARGING_BLOCK.get(), ModBlockCapabilities::getChargingBlockEnergy);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.ROBOSURGEON_BLOCKENTITY.get(), ModBlockCapabilities::getRobosurgeonEnergy);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.COMPUTER.get(), ModBlockCapabilities::getComputerEnergy);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.SERVER_TOWER.get(), ModBlockCapabilities::getServerTowerEnergy);

        event.registerBlock(Capabilities.EnergyStorage.BLOCK, (level, pos, state, blockEntity, side) -> {
            if (!ConditionalBlockPower.shouldUseEnergyInsteadOfRedstone()) return null;

            BlockPos controllerPos = ServerTowerMultiblock.getControllerPos(pos, state);

            if (controllerPos == null) return null;

            BlockEntity controllerBlockEntity = level.getBlockEntity(controllerPos);

            if (!(controllerBlockEntity instanceof ServerTowerBlockEntity tower)) return null;

            return tower.getEnergyStorage();
        }, ModBlocks.SERVER_TOWER_PART.get());
    }

    private static IEnergyStorage getChargingBlockEnergy(ChargingBlockEntity blockEntity, Direction side) {
        if (!ConditionalBlockPower.shouldUseEnergyInsteadOfRedstone()) {
            return null;
        }

        return blockEntity.getEnergyStorage();
    }

    private static IEnergyStorage getRobosurgeonEnergy(RobosurgeonBlockEntity blockEntity, Direction side) {
        if (!ConditionalBlockPower.shouldUseEnergyInsteadOfRedstone()) {
            return null;
        }

        return blockEntity.getEnergyStorage();
    }

    private static IEnergyStorage getComputerEnergy(ComputerBlockEntity blockEntity, Direction side) {
        if (!ConditionalBlockPower.shouldUseEnergyInsteadOfRedstone()) {
            return null;
        }

        return blockEntity.getEnergyStorage();
    }

    private static IEnergyStorage getServerTowerEnergy(ServerTowerBlockEntity blockEntity, Direction side) {
        if (!ConditionalBlockPower.shouldUseEnergyInsteadOfRedstone()) {
            return null;
        }

        return blockEntity.getEnergyStorage();
    }
}