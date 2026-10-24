package com.perigrine3.createcybernetics.common.synergy;

import com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent;
import com.perigrine3.createcybernetics.common.scheduling.StaggeredTickScheduler;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class CyberwareSynergyDispatcher {

    private CyberwareSynergyDispatcher() {}

    private static final String NBT_ROOT = "cc_synergies";
    private static final int TICK_INTERVAL = 20; // once per second

    @SubscribeEvent
    public static void onCyberwareStateChange(CyberwareStateChangeEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide) return;
        PlayerCyberwareData data = event.getData();
        if (data == null) {
            clearAll(player);
            return;
        }
        evaluate(player, data);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!StaggeredTickScheduler.shouldRun(player, TICK_INTERVAL, StaggeredTickScheduler.TickPhase.SYNERGY_REFRESH)) {
            return;
        }
        if (!player.hasData(ModAttachments.CYBERWARE)) {
            clearAll(player);
            return;
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) {
            clearAll(player);
            return;
        }

        evaluate(player, data);
    }

    private static void evaluate(Player player, PlayerCyberwareData data) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag state = persistent.getCompound(NBT_ROOT);
        boolean changed = false;

        for (var synergy : CyberwareSynergies.ALL) {
            String key = synergy.id().toString();

            boolean shouldBeActive = synergy.isActive(player, data);
            boolean isActive = state.getBoolean(key);

            if (shouldBeActive && !isActive) {
                synergy.apply(player);
                state.putBoolean(key, true);
                changed = true;
            } else if (!shouldBeActive && isActive) {
                synergy.remove(player);
                state.putBoolean(key, false);
                changed = true;
            }
        }

        if (changed) {
            persistent.put(NBT_ROOT, state);
        }
    }

    private static void clearAll(Player player) {
        for (var synergy : CyberwareSynergies.ALL) {
            synergy.remove(player);
        }
        player.getPersistentData().remove(NBT_ROOT);
    }
}
