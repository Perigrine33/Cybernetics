package com.perigrine3.createcybernetics.worldgen;

import com.perigrine3.createcybernetics.ConfigValues;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.entity.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class VanillaMobReplacementHandler {
    private static final int VANILLA_BASELINE_WEIGHT = 100;

    private VanillaMobReplacementHandler() {}

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.loadedFromDisk()) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!(event.getEntity() instanceof Mob mob)) return;

        if (mob.getType() == EntityType.ZOMBIE) {
            tryReplaceZombie(event, serverLevel, (Zombie) mob);
            return;
        }

        if (mob.getType() == EntityType.SKELETON) {
            tryReplaceSkeleton(event, serverLevel, (Skeleton) mob);
        }
    }

    private static void tryReplaceZombie(EntityJoinLevelEvent event, ServerLevel serverLevel, Zombie original) {
        if (!shouldReplace(original.getSpawnType(), ConfigValues.CYBERZOMBIE_SPAWN_WEIGHT, serverLevel)) return;

        Mob replacement = ModEntities.CYBERZOMBIE.get().create(serverLevel);
        if (replacement == null) return;

        finishReplacement(event, serverLevel, original, replacement);

        if (!replacement.isRemoved() && replacement instanceof Zombie replacementZombie) {
            replacementZombie.setBaby(original.isBaby());
        }
    }

    private static void tryReplaceSkeleton(EntityJoinLevelEvent event, ServerLevel serverLevel, Skeleton original) {
        if (!shouldReplace(original.getSpawnType(), ConfigValues.CYBERSKELETON_SPAWN_WEIGHT, serverLevel)) return;

        Mob replacement = ModEntities.CYBERSKELETON.get().create(serverLevel);
        if (replacement == null) return;

        finishReplacement(event, serverLevel, original, replacement);
    }

    private static void finishReplacement(EntityJoinLevelEvent event, ServerLevel serverLevel, Mob original, Mob replacement) {
        MobSpawnType spawnType = original.getSpawnType();
        if (spawnType == null) return;

        replacement.moveTo(original.getX(), original.getY(), original.getZ(), original.getYRot(), original.getXRot());
        replacement.setYBodyRot(original.getYRot());
        replacement.setYHeadRot(original.getYHeadRot());
        replacement.setDeltaMovement(original.getDeltaMovement());

        EventHooks.finalizeMobSpawn(replacement, serverLevel, serverLevel.getCurrentDifficultyAt(original.blockPosition()), spawnType, null);
        if (replacement.isSpawnCancelled()) return;

        event.setCanceled(true);
        serverLevel.tryAddFreshEntityWithPassengers(replacement);
    }

    private static boolean shouldReplace(MobSpawnType spawnType, int configuredWeight, ServerLevel serverLevel) {
        if (configuredWeight <= 0) return false;
        if (!isReplaceableSpawnType(spawnType)) return false;

        int totalWeight = VANILLA_BASELINE_WEIGHT + configuredWeight;
        return serverLevel.getRandom().nextInt(totalWeight) >= VANILLA_BASELINE_WEIGHT;
    }

    private static boolean isReplaceableSpawnType(MobSpawnType spawnType) {
        return spawnType == MobSpawnType.NATURAL || spawnType == MobSpawnType.CHUNK_GENERATION || spawnType == MobSpawnType.REINFORCEMENT;
    }
}