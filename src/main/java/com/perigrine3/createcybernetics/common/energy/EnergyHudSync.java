package com.perigrine3.createcybernetics.common.energy;

import com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager;
import com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.network.payload.EnergyHudSnapshotPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class EnergyHudSync {

    private EnergyHudSync() {}

    private record Mini(int stored, int cap, int net) {}

    private static final Map<UUID, Mini> LAST = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player p = event.getEntity();
        if (!(p instanceof ServerPlayer sp)) return;
        if (sp.isSpectator()) return;

        PlayerCyberwareData data = sp.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        CyberwareLoadoutProfile profile = CyberwareProfileManager.getProfile(sp, data);
        int stored = data.getEnergyStored();
        if (profile != null && !profile.hasEnergyWork() && stored == 0) {
            Mini prev = LAST.get(sp.getUUID());
            if (prev != null && prev.stored() == 0 && prev.cap() == 0 && prev.net() == 0) {
                return;
            }
            Mini zero = new Mini(0, 0, 0);
            LAST.put(sp.getUUID(), zero);
            PacketDistributor.sendToPlayer(sp, new EnergyHudSnapshotPayload(0, 0, 0));
            return;
        }

        int cap = data.getTotalEnergyCapacity(sp);

        int net = 0;

        Mini now = new Mini(stored, cap, net);
        Mini prev = LAST.get(sp.getUUID());

        if (now.equals(prev)) return;
        LAST.put(sp.getUUID(), now);

        PacketDistributor.sendToPlayer(sp, new EnergyHudSnapshotPayload(stored, cap, net));
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            LAST.remove(sp.getUUID());
        }
    }
}
