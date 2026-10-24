package com.perigrine3.createcybernetics.effect;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.util.CyberwareAttributeHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
public class SandevistanEffect extends MobEffect {

    public SandevistanEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFFFFF);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return true;

        applyAll(entity);
        return true;
    }

    private static void applyAll(LivingEntity entity) {
        CyberwareAttributeHelper.applyModifier(entity, "sandevistan_speed");
        CyberwareAttributeHelper.applyModifier(entity, "sandevistan_stepheight");
        CyberwareAttributeHelper.applyModifier(entity, "sandevistan_jump");
    }

    private static void removeAll(LivingEntity entity) {
        CyberwareAttributeHelper.removeModifier(entity, "sandevistan_speed");
        CyberwareAttributeHelper.removeModifier(entity, "sandevistan_stepheight");
        CyberwareAttributeHelper.removeModifier(entity, "sandevistan_jump");
    }

    private static boolean isSandevistan(MobEffect effect) {
        return effect == ModEffects.SANDEVISTAN_EFFECT;
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        var inst = event.getEffectInstance();
        if (inst == null) return;

        if (isSandevistan(inst.getEffect().value())) {
            applyAll(entity);
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        var inst = event.getEffectInstance();
        if (inst == null) return;

        if (isSandevistan(inst.getEffect().value())) {
            removeAll(entity);
            if (entity instanceof com.perigrine3.createcybernetics.api.ISandevistanTrailState trailState) {
                trailState.createcybernetics$setSandevistanTrailActive(false);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        var inst = event.getEffectInstance();
        if (inst == null) return;

        if (isSandevistan(inst.getEffect().value())) {
            removeAll(entity);
            if (entity instanceof com.perigrine3.createcybernetics.api.ISandevistanTrailState trailState) {
                trailState.createcybernetics$setSandevistanTrailActive(false);
            }
        }
    }

    @SubscribeEvent
    public static void onCyberwareStateChange(com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent event) {
        var player = event.getPlayer();
        if (player == null || player.level().isClientSide()) return;
        if (!player.hasEffect(ModEffects.SANDEVISTAN_EFFECT)) {
            removeAll(player);
        }
    }
}