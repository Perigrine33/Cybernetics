package com.perigrine3.createcybernetics.cyberspace;

import com.perigrine3.createcybernetics.block.ModBlocks;
import com.perigrine3.createcybernetics.world.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

public final class CyberspaceDungeonManager {
    public static final int DUNGEON_CELL_SIZE = 4096;
    public static final int MAX_DUNGEON_SIZE = 96;

    public static final int ORIGIN_EXCLUSION_RADIUS = 131_072;
    public static final int MAX_CELL_RADIUS = 7000;

    private static final int TEMPORARY_ROOM_RADIUS = 8;
    private static final int TEMPORARY_ROOM_FLOOR_Y = 80;
    private static final int TEMPORARY_ROOM_HEIGHT = 8;

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

        BlockPos entryPos = getDefaultEntryPosition(record.cellX(), record.cellZ());

        data.markGenerated(record.serverId(), entryPos);
    }
}