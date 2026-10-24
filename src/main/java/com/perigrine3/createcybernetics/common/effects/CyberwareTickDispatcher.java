package com.perigrine3.createcybernetics.common.effects;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.EntityCyberwareData;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.ModMobAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile;
import com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.InstalledSlotRef;
import com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class CyberwareTickDispatcher {
    private CyberwareTickDispatcher() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.hasData(ModAttachments.CYBERWARE)) return;

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        CyberwareLoadoutProfile profile = CyberwareProfileManager.getProfile(player, data);
        if (profile == null) return;

        List<InstalledSlotRef> tickableSlots = profile.tickableSlots();
        if (tickableSlots.isEmpty()) return;

        for (int i = 0, size = tickableSlots.size(); i < size; i++) {
            InstalledSlotRef ref = tickableSlots.get(i);
            InstalledCyberware installed = ref.cyberware();

            ItemStack stack = installed.getItem();
            if (stack == null || stack.isEmpty()) continue;

            ref.item().onTick(player, stack, ref.slot(), ref.index());
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity instanceof Player) return;
        if (!entity.hasData(ModMobAttachments.CYBERENTITY_CYBERWARE)) return;

        EntityCyberwareData data = entity.getData(ModMobAttachments.CYBERENTITY_CYBERWARE);
        if (data == null) return;

        List<InstalledSlotRef> tickableSlots = CyberwareProfileManager.getEntityTickableSlots(data);
        if (tickableSlots.isEmpty()) return;

        for (int i = 0, size = tickableSlots.size(); i < size; i++) {
            InstalledSlotRef ref = tickableSlots.get(i);
            InstalledCyberware installed = ref.cyberware();

            ItemStack stack = installed.getItem();
            if (stack == null || stack.isEmpty()) continue;

            ref.item().onTick(entity, stack, ref.slot(), ref.index());
        }
    }
}
