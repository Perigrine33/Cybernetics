package com.perigrine3.createcybernetics.cyberspace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CyberspaceDungeonSavedData extends SavedData {
    private static final String DATA_NAME = "createcybernetics_cyberspace_dungeons";

    private static final String NBT_ALLOCATION_SALT = "AllocationSalt";
    private static final String NBT_DUNGEONS = "Dungeons";

    private static final String NBT_SERVER_ID = "ServerId";
    private static final String NBT_CELL_X = "CellX";
    private static final String NBT_CELL_Z = "CellZ";

    private static final String NBT_ENTRY_X = "EntryX";
    private static final String NBT_ENTRY_Y = "EntryY";
    private static final String NBT_ENTRY_Z = "EntryZ";

    private static final String NBT_GENERATED = "Generated";
    private static final String NBT_PROVISIONED = "Provisioned";

    private static final String NBT_ENDPOINT_DIMENSION = "EndpointDimension";
    private static final String NBT_ENDPOINT_X = "EndpointX";
    private static final String NBT_ENDPOINT_Y = "EndpointY";
    private static final String NBT_ENDPOINT_Z = "EndpointZ";

    private long allocationSalt = RandomSource.create().nextLong();

    private final Map<UUID, DungeonRecord> dungeons = new HashMap<>();

    public static CyberspaceDungeonSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(CyberspaceDungeonSavedData::new, CyberspaceDungeonSavedData::load), DATA_NAME);
    }

    public static CyberspaceDungeonSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        CyberspaceDungeonSavedData data = new CyberspaceDungeonSavedData();

        if (tag.contains(NBT_ALLOCATION_SALT, Tag.TAG_LONG)) {
            data.allocationSalt = tag.getLong(NBT_ALLOCATION_SALT);
        } else {
            data.setDirty();
        }

        if (!tag.contains(NBT_DUNGEONS, Tag.TAG_LIST)) {
            return data;
        }

        ListTag dungeonList = tag.getList(NBT_DUNGEONS, Tag.TAG_COMPOUND);

        for (int i = 0; i < dungeonList.size(); i++) {
            CompoundTag dungeonTag = dungeonList.getCompound(i);

            if (!dungeonTag.hasUUID(NBT_SERVER_ID)) continue;

            UUID serverId = dungeonTag.getUUID(NBT_SERVER_ID);

            int cellX = dungeonTag.getInt(NBT_CELL_X);
            int cellZ = dungeonTag.getInt(NBT_CELL_Z);

            BlockPos entryPos = new BlockPos(
                    dungeonTag.getInt(NBT_ENTRY_X),
                    dungeonTag.getInt(NBT_ENTRY_Y),
                    dungeonTag.getInt(NBT_ENTRY_Z)
            );

            boolean generated = dungeonTag.getBoolean(NBT_GENERATED);
            boolean provisioned = dungeonTag.getBoolean(NBT_PROVISIONED);

            ResourceLocation endpointDimension = null;
            BlockPos endpointPos = null;

            if (provisioned) {
                endpointDimension = ResourceLocation.tryParse(dungeonTag.getString(NBT_ENDPOINT_DIMENSION));

                if (endpointDimension != null) {
                    endpointPos = new BlockPos(
                            dungeonTag.getInt(NBT_ENDPOINT_X),
                            dungeonTag.getInt(NBT_ENDPOINT_Y),
                            dungeonTag.getInt(NBT_ENDPOINT_Z)
                    );
                } else {
                    provisioned = false;
                }
            }

            data.dungeons.put(serverId, new DungeonRecord(serverId, cellX, cellZ, entryPos, generated, provisioned, endpointDimension, endpointPos));
        }

        return data;
    }

    public DungeonRecord getDungeon(UUID serverId) {
        return dungeons.get(serverId);
    }

    public DungeonRecord getProvisionedDungeon(UUID serverId) {
        DungeonRecord record = dungeons.get(serverId);

        if (record == null || !record.provisioned()) {
            return null;
        }

        return record;
    }

    public boolean registerProvisionedServer(UUID serverId, ResourceLocation endpointDimension, BlockPos endpointPos) {
        DungeonRecord existing = dungeons.get(serverId);

        if (existing != null) {
            if (existing.provisioned()) {
                return existing.endpointDimension() != null
                        && existing.endpointPos() != null
                        && existing.endpointDimension().equals(endpointDimension)
                        && existing.endpointPos().equals(endpointPos);
            }

            DungeonRecord updated = new DungeonRecord(
                    existing.serverId(),
                    existing.cellX(),
                    existing.cellZ(),
                    existing.entryPos(),
                    existing.generated(),
                    true,
                    endpointDimension,
                    endpointPos.immutable()
            );

            dungeons.put(serverId, updated);
            setDirty();

            return true;
        }

        CellPos cell = findAvailableCell(serverId);
        BlockPos entryPos = CyberspaceDungeonManager.getDefaultEntryPosition(cell.x(), cell.z());

        DungeonRecord record = new DungeonRecord(
                serverId,
                cell.x(),
                cell.z(),
                entryPos,
                false,
                true,
                endpointDimension,
                endpointPos.immutable()
        );

        dungeons.put(serverId, record);
        setDirty();

        return true;
    }

    public boolean isValidEndpoint(UUID serverId, ResourceLocation dimension, BlockPos pos) {
        DungeonRecord record = getProvisionedDungeon(serverId);

        if (record == null || record.endpointDimension() == null || record.endpointPos() == null) {
            return false;
        }

        return record.endpointDimension().equals(dimension) && record.endpointPos().equals(pos);
    }

    public void markGenerated(UUID serverId, BlockPos entryPos) {
        DungeonRecord existing = dungeons.get(serverId);

        if (existing == null || !existing.provisioned()) {
            return;
        }

        DungeonRecord updated = new DungeonRecord(
                existing.serverId(),
                existing.cellX(),
                existing.cellZ(),
                entryPos.immutable(),
                true,
                existing.provisioned(),
                existing.endpointDimension(),
                existing.endpointPos()
        );

        dungeons.put(serverId, updated);
        setDirty();
    }

    public void setEntryPosition(UUID serverId, BlockPos entryPos) {
        DungeonRecord existing = dungeons.get(serverId);

        if (existing == null || !existing.provisioned()) {
            return;
        }

        DungeonRecord updated = new DungeonRecord(
                existing.serverId(),
                existing.cellX(),
                existing.cellZ(),
                entryPos.immutable(),
                existing.generated(),
                existing.provisioned(),
                existing.endpointDimension(),
                existing.endpointPos()
        );

        dungeons.put(serverId, updated);
        setDirty();
    }

    public Collection<DungeonRecord> getDungeons() {
        return dungeons.values();
    }

    private CellPos findAvailableCell(UUID serverId) {
        for (int attempt = 0; attempt < 100_000; attempt++) {
            long seed = mixAllocationSeed(serverId, attempt);
            RandomSource random = RandomSource.create(seed);

            int cellX = random.nextInt(CyberspaceDungeonManager.MAX_CELL_RADIUS * 2 + 1) - CyberspaceDungeonManager.MAX_CELL_RADIUS;
            int cellZ = random.nextInt(CyberspaceDungeonManager.MAX_CELL_RADIUS * 2 + 1) - CyberspaceDungeonManager.MAX_CELL_RADIUS;

            if (!isCellAllowed(cellX, cellZ)) continue;
            if (isCellUsed(cellX, cellZ)) continue;

            return new CellPos(cellX, cellZ);
        }

        throw new IllegalStateException("Unable to allocate a unique cyberspace dungeon cell");
    }

    private long mixAllocationSeed(UUID serverId, int attempt) {
        long seed = allocationSalt;

        seed ^= serverId.getMostSignificantBits();
        seed = Long.rotateLeft(seed, 21);
        seed *= 0x9E3779B97F4A7C15L;

        seed ^= serverId.getLeastSignificantBits();
        seed = Long.rotateLeft(seed, 17);
        seed *= 0xD1B54A32D192ED03L;

        seed ^= (long) attempt * 0x94D049BB133111EBL;

        return seed;
    }

    private boolean isCellAllowed(int cellX, int cellZ) {
        long centerX = (long) cellX * CyberspaceDungeonManager.DUNGEON_CELL_SIZE;
        long centerZ = (long) cellZ * CyberspaceDungeonManager.DUNGEON_CELL_SIZE;

        return Math.abs(centerX) >= CyberspaceDungeonManager.ORIGIN_EXCLUSION_RADIUS
                || Math.abs(centerZ) >= CyberspaceDungeonManager.ORIGIN_EXCLUSION_RADIUS;
    }

    private boolean isCellUsed(int cellX, int cellZ) {
        for (DungeonRecord record : dungeons.values()) {
            if (record.cellX() == cellX && record.cellZ() == cellZ) {
                return true;
            }
        }

        return false;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putLong(NBT_ALLOCATION_SALT, allocationSalt);

        ListTag dungeonList = new ListTag();

        for (DungeonRecord record : dungeons.values()) {
            CompoundTag dungeonTag = new CompoundTag();

            dungeonTag.putUUID(NBT_SERVER_ID, record.serverId());
            dungeonTag.putInt(NBT_CELL_X, record.cellX());
            dungeonTag.putInt(NBT_CELL_Z, record.cellZ());

            dungeonTag.putInt(NBT_ENTRY_X, record.entryPos().getX());
            dungeonTag.putInt(NBT_ENTRY_Y, record.entryPos().getY());
            dungeonTag.putInt(NBT_ENTRY_Z, record.entryPos().getZ());

            dungeonTag.putBoolean(NBT_GENERATED, record.generated());
            dungeonTag.putBoolean(NBT_PROVISIONED, record.provisioned());

            if (record.provisioned() && record.endpointDimension() != null && record.endpointPos() != null) {
                dungeonTag.putString(NBT_ENDPOINT_DIMENSION, record.endpointDimension().toString());
                dungeonTag.putInt(NBT_ENDPOINT_X, record.endpointPos().getX());
                dungeonTag.putInt(NBT_ENDPOINT_Y, record.endpointPos().getY());
                dungeonTag.putInt(NBT_ENDPOINT_Z, record.endpointPos().getZ());
            }

            dungeonList.add(dungeonTag);
        }

        tag.put(NBT_DUNGEONS, dungeonList);

        return tag;
    }

    public record DungeonRecord(
            UUID serverId,
            int cellX,
            int cellZ,
            BlockPos entryPos,
            boolean generated,
            boolean provisioned,
            ResourceLocation endpointDimension,
            BlockPos endpointPos
    ) {}

    private record CellPos(int x, int z) {}
}