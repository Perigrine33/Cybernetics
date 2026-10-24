package com.perigrine3.createcybernetics.compat.neosync;

import com.perigrine3.createcybernetics.compat.ModCompats;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class NeoSyncShellPhasingHandler {

    private static final Set<UUID> PHASING_PLAYERS = new HashSet<>();

    private NeoSyncShellPhasingHandler() {}

    public static boolean isPhasing(Player player) {
        return PHASING_PLAYERS.contains(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!ModCompats.isInstalled("neosync")) {
            return;
        }

        Player player = event.getEntity();
        boolean phasing = isPhasing(player);

        if (!player.isAlive() || player.isRemoved()) {
            if (phasing) {
                stopPhasing(player);
            }
            return;
        }

        if (!player.isShiftKeyDown() && !phasing) {
            return;
        }

        repairLegacyPhysicsState(player);

        ContainerTarget target = findIntersectingEmptyContainer(player);

        if (target != null) {
            startPhasing(player);
            holdInsideContainer(player, target.pos());
            return;
        }

        if (phasing) {
            stopPhasing(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        resetPlayer(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        resetPlayer(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        resetPlayer(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        resetPlayer(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        resetPlayer(event.getOriginal());
        resetPlayer(event.getEntity());
    }

    private static void startPhasing(Player player) {
        PHASING_PLAYERS.add(player.getUUID());
        player.fallDistance = 0.0F;
    }

    private static void holdInsideContainer(Player player, BlockPos containerPos) {
        Vec3 movement = player.getDeltaMovement();
        player.setDeltaMovement(movement.x, Math.min(0.0D, movement.y), movement.z);
        player.fallDistance = 0.0F;

        double centerX = containerPos.getX() + 0.5D;
        double centerZ = containerPos.getZ() + 0.5D;
        double differenceX = centerX - player.getX();
        double differenceZ = centerZ - player.getZ();

        if (Math.abs(differenceX) < 0.75D && Math.abs(differenceZ) < 0.75D) {
            player.setPos(player.getX() + differenceX * 0.2D, player.getY(), player.getZ() + differenceZ * 0.2D);
        }
    }

    private static void stopPhasing(Player player) {
        PHASING_PLAYERS.remove(player.getUUID());
        player.fallDistance = 0.0F;
    }

    private static void resetPlayer(Player player) {
        PHASING_PLAYERS.remove(player.getUUID());
        repairLegacyPhysicsState(player);
        player.fallDistance = 0.0F;
    }

    private static void repairLegacyPhysicsState(Player player) {
        if (!player.isSpectator()) {
            player.noPhysics = false;
        }

        if (!player.getAbilities().flying) {
            player.setNoGravity(false);
        }
    }

    private static ContainerTarget findIntersectingEmptyContainer(Player player) {
        BlockPos playerPos = player.blockPosition();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);

                    if (!NeoSyncShellContainerAccess.isEmpty(player.level(), pos)) {
                        continue;
                    }

                    AABB containerBounds = new AABB(pos).inflate(0.15D);
                    if (player.getBoundingBox().intersects(containerBounds)) {
                        return new ContainerTarget(pos);
                    }
                }
            }
        }

        return null;
    }

    private record ContainerTarget(BlockPos pos) {}
}