package com.perigrine3.createcybernetics.entity.ai.goal;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.ISandevistanTrailState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = CreateCybernetics.MODID)
public final class CyberentitySandevistanTrailSync {

    private CyberentitySandevistanTrailSync() {}

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (living.level().isClientSide) return;
        if (living instanceof Player) return;
        if (!(living instanceof ISandevistanTrailState trailState)) return;
        if (!trailState.createcybernetics$isSandevistanTrailActive()) return;

        long activeUntil = living.getPersistentData().getLong(CyberentitySandevistanGoal.NBT_SANDY_ACTIVE_UNTIL);
        if (activeUntil <= living.level().getGameTime()) {
            trailState.createcybernetics$setSandevistanTrailActive(false);
        }
    }
}