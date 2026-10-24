package com.perigrine3.createcybernetics.common.multiblock;

import com.perigrine3.createcybernetics.block.ComputerBlock;
import com.perigrine3.createcybernetics.block.ModBlocks;
import com.perigrine3.createcybernetics.block.ServerTowerBlock;
import com.perigrine3.createcybernetics.block.ServerTowerPartBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public final class ServerTowerMultiblock {

    private static final PatternPos[] MACHINE_CASINGS = {
            new PatternPos(-1, 0, 1),
            new PatternPos(0, 0, 1),
            new PatternPos(1, 0, 1),

            new PatternPos(-1, 0, 0),
            new PatternPos(1, 0, 0),

            new PatternPos(0, 0, -1),

            new PatternPos(-1, 1, 1),
            new PatternPos(0, 1, 1),
            new PatternPos(1, 1, 1),

            new PatternPos(-1, 1, 0),
            new PatternPos(0, 1, 0),
            new PatternPos(1, 1, 0)
    };

    private static final PatternPos[] REQUIRED_AIR = {
            new PatternPos(-1, 0, -1),
            new PatternPos(1, 0, -1),

            new PatternPos(-1, 1, -1),
            new PatternPos(0, 1, -1),
            new PatternPos(1, 1, -1)
    };

    public static final int FRONT_EXTENSION_INDEX = 5;

    private static boolean dismantling = false;

    private ServerTowerMultiblock() {}

    public static boolean tryForm(Level level, BlockPos placedCasingPos) {
        if (level == null || level.isClientSide) return false;

        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (PatternPos casing : MACHINE_CASINGS) {
                BlockPos potentialAnchor = reverseOffset(placedCasingPos, facing, casing);

                if (matches(level, potentialAnchor, facing)) {
                    form(level, potentialAnchor, facing);
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isDismantling() {
        return dismantling;
    }

    public static BlockPos getFrontExtensionPos(BlockPos anchor, Direction facing) {
        return offset(anchor, facing, MACHINE_CASINGS[FRONT_EXTENSION_INDEX]);
    }

    public static BlockPos getControllerPos(BlockPos partPos, BlockState partState) {
        if (partState == null || !partState.is(ModBlocks.SERVER_TOWER_PART.get())) return null;

        int partIndex = partState.getValue(ServerTowerPartBlock.PART_INDEX);

        if (partIndex < 0 || partIndex >= MACHINE_CASINGS.length) return null;

        Direction facing = partState.getValue(ServerTowerPartBlock.FACING);
        return reverseOffset(partPos, facing, MACHINE_CASINGS[partIndex]);
    }

    public static BlockPos getComputerControllerPos(LevelAccessor level, BlockPos computerPos) {
        BlockPos below = computerPos.below();
        BlockState belowState = level.getBlockState(below);

        if (!belowState.is(ModBlocks.SERVER_TOWER_PART.get())) return null;
        if (belowState.getValue(ServerTowerPartBlock.PART_INDEX) != FRONT_EXTENSION_INDEX) return null;

        BlockPos controllerPos = getControllerPos(below, belowState);

        if (controllerPos == null) return null;

        BlockState controllerState = level.getBlockState(controllerPos);

        if (!controllerState.is(ModBlocks.SERVER_TOWER.get())) return null;
        if (controllerState.getValue(ServerTowerBlock.FACING) != belowState.getValue(ServerTowerPartBlock.FACING)) return null;

        return controllerPos;
    }

    public static boolean isComputerPosition(LevelAccessor level, BlockPos computerPos) {
        return getComputerControllerPos(level, computerPos) != null;
    }

    public static List<BlockPos> getOccupiedPositions(BlockPos anchor, Direction facing) {
        List<BlockPos> positions = new ArrayList<>(MACHINE_CASINGS.length + 1);
        positions.add(anchor.immutable());

        for (PatternPos part : MACHINE_CASINGS) {
            positions.add(offset(anchor, facing, part).immutable());
        }

        return positions;
    }

    public static void ensureRuntimeState(Level level, BlockPos anchor, Direction facing) {
        if (level == null || level.isClientSide) return;

        BlockState controllerState = level.getBlockState(anchor);

        if (!controllerState.is(ModBlocks.SERVER_TOWER.get())) return;

        if (controllerState.getValue(ServerTowerBlock.FACING) != facing) {
            controllerState = controllerState.setValue(ServerTowerBlock.FACING, facing);
            level.setBlock(anchor, controllerState, Block.UPDATE_ALL);
        }

        for (int i = 0; i < MACHINE_CASINGS.length; i++) {
            BlockPos partPos = offset(anchor, facing, MACHINE_CASINGS[i]);
            BlockState partState = level.getBlockState(partPos);

            if (!partState.is(ModBlocks.SERVER_TOWER_PART.get())) continue;

            BlockState expectedState = ModBlocks.SERVER_TOWER_PART.get().defaultBlockState()
                    .setValue(ServerTowerPartBlock.FACING, facing)
                    .setValue(ServerTowerPartBlock.PART_INDEX, i);

            if (partState != expectedState) {
                level.setBlock(partPos, expectedState, Block.UPDATE_ALL);
            }
        }

        BlockPos computerPos = getFrontExtensionPos(anchor, facing).above();
        BlockState computerState = level.getBlockState(computerPos);

        if (computerState.is(ModBlocks.COMPUTER.get())) {
            ComputerBlock.updateConnectionState(level, computerPos);
            level.updateNeighborsAt(computerPos, computerState.getBlock());
        }

        for (BlockPos occupiedPos : getOccupiedPositions(anchor, facing)) {
            BlockState occupiedState = level.getBlockState(occupiedPos);
            level.updateNeighborsAt(occupiedPos, occupiedState.getBlock());
        }
    }

    public static void breakPart(Level level, BlockPos brokenPartPos, Direction facing, int partIndex) {
        if (level == null || level.isClientSide) return;
        if (partIndex < 0 || partIndex >= MACHINE_CASINGS.length) return;

        PatternPos brokenPart = MACHINE_CASINGS[partIndex];
        BlockPos anchor = reverseOffset(brokenPartPos, facing, brokenPart);

        if (!level.getBlockState(anchor).is(ModBlocks.SERVER_TOWER.get())) return;

        dismantle(level, anchor, facing, partIndex, false);
    }

    public static void breakController(Level level, BlockPos anchor, Direction facing) {
        if (level == null || level.isClientSide) return;

        dismantle(level, anchor, facing, -1, true);
    }

    private static void dismantle(Level level, BlockPos anchor, Direction facing, int brokenPartIndex, boolean controllerBroken) {
        if (dismantling) return;

        dismantling = true;

        try {
            for (int i = 0; i < MACHINE_CASINGS.length; i++) {
                if (i == brokenPartIndex) continue;

                BlockPos pos = offset(anchor, facing, MACHINE_CASINGS[i]);

                if (level.getBlockState(pos).is(ModBlocks.SERVER_TOWER_PART.get())) {
                    level.setBlock(pos, ModBlocks.MACHINE_CASING.get().defaultBlockState(), 3);
                }
            }

            if (!controllerBroken && level.getBlockState(anchor).is(ModBlocks.SERVER_TOWER.get())) {
                level.setBlock(anchor, Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
            }
        } finally {
            dismantling = false;
        }
    }

    private static boolean matches(Level level, BlockPos anchor, Direction facing) {
        if (!level.getBlockState(anchor).is(Blocks.REDSTONE_BLOCK)) return false;

        for (PatternPos casing : MACHINE_CASINGS) {
            BlockPos pos = offset(anchor, facing, casing);

            if (!level.getBlockState(pos).is(ModBlocks.MACHINE_CASING.get())) {
                return false;
            }
        }

        for (PatternPos air : REQUIRED_AIR) {
            BlockPos pos = offset(anchor, facing, air);

            if (!level.getBlockState(pos).isAir()) {
                return false;
            }
        }

        return true;
    }

    private static void form(Level level, BlockPos anchor, Direction facing) {
        for (int i = 0; i < MACHINE_CASINGS.length; i++) {
            PatternPos casing = MACHINE_CASINGS[i];
            BlockPos pos = offset(anchor, facing, casing);

            BlockState partState = ModBlocks.SERVER_TOWER_PART.get().defaultBlockState()
                    .setValue(ServerTowerPartBlock.FACING, facing)
                    .setValue(ServerTowerPartBlock.PART_INDEX, i);

            level.setBlock(pos, partState, 3);
        }

        BlockState serverTowerState = ModBlocks.SERVER_TOWER.get().defaultBlockState().setValue(ServerTowerBlock.FACING, facing);
        level.setBlock(anchor, serverTowerState, 3);
    }

    private static BlockPos offset(BlockPos anchor, Direction facing, PatternPos offset) {
        Direction right = facing.getClockWise();
        return anchor.relative(right, offset.x()).above(offset.y()).relative(facing.getOpposite(), offset.z());
    }

    private static BlockPos reverseOffset(BlockPos worldPos, Direction facing, PatternPos offset) {
        Direction right = facing.getClockWise();
        return worldPos.relative(right, -offset.x()).below(offset.y()).relative(facing.getOpposite(), -offset.z());
    }

    private record PatternPos(int x, int y, int z) {}
}