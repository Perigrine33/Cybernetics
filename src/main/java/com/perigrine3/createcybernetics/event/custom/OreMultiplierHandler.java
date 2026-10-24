package com.perigrine3.createcybernetics.event.custom;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.common.mining.MiningRollEngine;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.function.Consumer;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class OreMultiplierHandler {

    private OreMultiplierHandler() {
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        MiningRollEngine.rebuildOreIndexAsync();
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        MiningRollEngine.rebuildOreIndexAsync();
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        MiningRollEngine.clearAllCaches();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        MiningRollEngine.refreshPlayerSnapshotAsync(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        MiningRollEngine.refreshPlayerSnapshotAsync(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        MiningRollEngine.refreshPlayerSnapshotAsync(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() != null) {
            MiningRollEngine.invalidatePlayerSnapshot(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled()) {
            return;
        }

        LevelAccessor level = event.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null || player.isCreative() || player.isSpectator()) {
            return;
        }

        BlockState state = event.getState();
        if (!MiningRollEngine.getClassifier().classify(state).isEligibleOre()) {
            return;
        }

        MiningRollEngine.beginBreakContext(
                player,
                event.getPos(),
                state,
                player.getMainHandItem(),
                serverLevel.getGameTime()
        );
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onBlockDrops(BlockDropsEvent event) {
        MiningRollEngine.clearBreakContext();
    }

    public static int resolvePoolRolls(int baseRolls, LootContext context) {
        return MiningRollEngine.resolvePoolRolls(baseRolls, context);
    }

    public static Consumer<ItemStack> decorateDropConsumer(Consumer<ItemStack> downstream, LootContext context) {
        return MiningRollEngine.decorateDropConsumer(downstream, context);
    }
}
