package com.perigrine3.createcybernetics.cyberspace;

import com.perigrine3.createcybernetics.block.ModBlocks;
import com.perigrine3.createcybernetics.block.entity.DataCacheBlockEntity;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.world.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CyberspaceDungeonManager {
    public static final int DUNGEON_CELL_SIZE = 4096;
    public static final int MAX_DUNGEON_SIZE = 96;

    public static final int ORIGIN_EXCLUSION_RADIUS = 131_072;
    public static final int MAX_CELL_RADIUS = 7000;

    private static final int TEMPORARY_ROOM_RADIUS = 8;
    private static final int TEMPORARY_ROOM_FLOOR_Y = 80;
    private static final int TEMPORARY_ROOM_HEIGHT = 8;
    private static final int MIN_FRAGMENTS_PER_TYPE = 4;
    private static final int MAX_FRAGMENTS_PER_TYPE = 8;

    private CyberspaceDungeonManager() {}

    public static CyberspaceDungeonSavedData.DungeonRecord getProvisionedDungeon(MinecraftServer server, UUID serverId) {
        return CyberspaceDungeonSavedData.get(server).getProvisionedDungeon(serverId);
    }

    public static CyberspaceDungeonSavedData.DungeonRecord getOrGenerateProvisionedDungeon(MinecraftServer server, UUID serverId) {
        CyberspaceDungeonSavedData data = CyberspaceDungeonSavedData.get(server);
        CyberspaceDungeonSavedData.DungeonRecord record = data.getProvisionedDungeon(serverId);

        if (record == null) {
            return null;
        }

        if (record.generated()) {
            return record;
        }

        ServerLevel cyberspace = server.getLevel(ModDimensions.CYBERSPACE_LEVEL);

        if (cyberspace == null) {
            return null;
        }

        generateTemporaryArchitecture(cyberspace, data, record);

        return data.getProvisionedDungeon(serverId);
    }

    public static BlockPos getDefaultEntryPosition(int cellX, int cellZ) {
        int centerX = getCellCenterCoordinate(cellX);
        int centerZ = getCellCenterCoordinate(cellZ);

        return new BlockPos(centerX, TEMPORARY_ROOM_FLOOR_Y + 1, centerZ + 4);
    }

    public static BlockPos getCellCenter(int cellX, int cellZ) {
        return new BlockPos(getCellCenterCoordinate(cellX), TEMPORARY_ROOM_FLOOR_Y, getCellCenterCoordinate(cellZ));
    }

    private static int getCellCenterCoordinate(int cell) {
        return cell * DUNGEON_CELL_SIZE;
    }

    private static void generateTemporaryArchitecture(ServerLevel level, CyberspaceDungeonSavedData data, CyberspaceDungeonSavedData.DungeonRecord record) {
        BlockPos center = getCellCenter(record.cellX(), record.cellZ());

        for (int x = -TEMPORARY_ROOM_RADIUS; x <= TEMPORARY_ROOM_RADIUS; x++) {
            for (int z = -TEMPORARY_ROOM_RADIUS; z <= TEMPORARY_ROOM_RADIUS; z++) {
                for (int y = 0; y <= TEMPORARY_ROOM_HEIGHT; y++) {
                    boolean shell = x == -TEMPORARY_ROOM_RADIUS
                            || x == TEMPORARY_ROOM_RADIUS
                            || z == -TEMPORARY_ROOM_RADIUS
                            || z == TEMPORARY_ROOM_RADIUS
                            || y == 0
                            || y == TEMPORARY_ROOM_HEIGHT;

                    if (!shell) continue;

                    BlockPos architecturePos = center.offset(x, y, z);
                    level.setBlock(architecturePos, ModBlocks.DIGITAL_ARCHITECTURE.get().defaultBlockState(), 3);
                }
            }
        }

        BlockPos accessNodePos = center.offset(0, 1, 6);
        level.setBlock(accessNodePos, ModBlocks.ACCESS_NODE.get().defaultBlockState(), 3);

        BlockPos cachePos = center.above();
        level.setBlock(cachePos, ModBlocks.DATA_CACHE.get().defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(cachePos) instanceof DataCacheBlockEntity cache) {
            cache.setItem(0, new ItemStack(ModItems.MACROPROGRAM_DATA_MINER.get()));
            cache.setChanged();
        }

        generateWallFragments(level, center);

        BlockPos entryPos = getDefaultEntryPosition(record.cellX(), record.cellZ());

        data.markGenerated(record.serverId(), entryPos);
    }

    private static void generateWallFragments(ServerLevel level, BlockPos center) {
        List<WallFragmentSpot> availableSpots = new ArrayList<>();

        for (Direction wallDirection : Direction.Plane.HORIZONTAL) {
            for (int offset = -TEMPORARY_ROOM_RADIUS + 2; offset <= TEMPORARY_ROOM_RADIUS - 2; offset++) {
                for (int height = 1; height <= 3; height++) {
                    BlockPos pos = center.relative(wallDirection, TEMPORARY_ROOM_RADIUS - 1)
                            .relative(wallDirection.getClockWise(), offset)
                            .above(height);
                    if (level.isEmptyBlock(pos)) {
                        availableSpots.add(new WallFragmentSpot(pos, wallDirection.getOpposite()));
                    }
                }
            }
        }

        List<Block> fragmentTypes = List.of(
                ModBlocks.TARGET_CODE_FRAGMENT.get(),
                ModBlocks.FUNCTION_CODE_FRAGMENT.get(),
                ModBlocks.MODIFIER_CODE_FRAGMENT.get());

        for (Block fragment : fragmentTypes) {
            int count = level.random.nextIntBetweenInclusive(MIN_FRAGMENTS_PER_TYPE, MAX_FRAGMENTS_PER_TYPE);
            for (int i = 0; i < count && !availableSpots.isEmpty(); i++) {
                WallFragmentSpot spot = availableSpots.remove(level.random.nextInt(availableSpots.size()));
                BlockState state = fragment.defaultBlockState().setValue(AmethystClusterBlock.FACING, spot.facing());
                if (state.canSurvive(level, spot.pos())) {
                    level.setBlock(spot.pos(), state, Block.UPDATE_ALL);
                }
            }
        }
    }

    private record WallFragmentSpot(BlockPos pos, Direction facing) {}
}
