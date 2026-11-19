package com.perigrine3.createcybernetics.client.animation;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = CreateCybernetics.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class PlayerImpactSquishClientEvents {
    private static final float MACE_SQUISH_STRENGTH = 0.85F;
    private static final double MACE_ATTACKER_CHECK_RADIUS = 6.0D;

    private static boolean wasOnGround = true;
    private static float maxFallDistance = 0.0F;
    private static int lastHurtTime = 0;
    private static LocalPlayer trackedPlayer;

    private PlayerImpactSquishClientEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        PlayerAnimationPoseState.restore(event.getRenderer().getModel());
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        resetAnimationState();
        trackedPlayer = null;
    }

    private static void resetAnimationState() {
        PlayerAnimationPoseState.clear();
        HeavyKnucklesClientAnimation.clear();
        PlayerImpactSquishClientState.clear();
        maxFallDistance = 0.0F;
        wasOnGround = true;
        lastHurtTime = 0;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        PlayerImpactSquishClientState.clientTick();

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player != trackedPlayer) {
            resetAnimationState();
            trackedPlayer = player;
        }
        if (player == null) return;

        if (!hasGooeyMuscle(player)) {
            maxFallDistance = 0.0F;
            wasOnGround = player.onGround();
            lastHurtTime = player.hurtTime;
            return;
        }

        handleMaceSquish(player);
        handleFallSquish(player);
    }

    private static void handleMaceSquish(LocalPlayer player) {
        int currentHurtTime = player.hurtTime;
        boolean hurtStarted = currentHurtTime > 0 && lastHurtTime <= 0;

        lastHurtTime = currentHurtTime;

        if (!hurtStarted) return;
        if (!nearbyMaceAttacker(player)) return;

        PlayerImpactSquishClientState.trigger(player, MACE_SQUISH_STRENGTH);
    }

    private static void handleFallSquish(LocalPlayer player) {
        if (!player.onGround()) {
            maxFallDistance = Math.max(maxFallDistance, player.fallDistance);
            wasOnGround = false;
            return;
        }

        if (!wasOnGround) {
            if (maxFallDistance >= 10.0F) {
                float strength = Mth.clamp(maxFallDistance / 10.0F, 0.25F, 1.0F);
                PlayerImpactSquishClientState.trigger(player, strength);
            }

            maxFallDistance = 0.0F;
            wasOnGround = true;
        }
    }

    private static boolean nearbyMaceAttacker(LocalPlayer player) {
        if (player.level() == null) return false;

        AABB box = player.getBoundingBox().inflate(MACE_ATTACKER_CHECK_RADIUS);

        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity == player) continue;
            if (!entity.isAlive()) continue;
            if (entity.isSpectator()) continue;

            if (isHoldingMace(entity)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isHoldingMace(LivingEntity entity) {
        ItemStack mainHand = entity.getMainHandItem();
        if (!mainHand.isEmpty() && mainHand.getItem() instanceof MaceItem) {
            return true;
        }

        ItemStack offHand = entity.getOffhandItem();
        return !offHand.isEmpty() && offHand.getItem() instanceof MaceItem;
    }

    private static boolean hasGooeyMuscle(LocalPlayer player) {
        if (player == null) return false;
        if (!player.hasData(ModAttachments.CYBERWARE)) return false;

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return false;

        return data.hasSpecificItem(ModItems.WETWARE_GOOEYMUSCLE.get(), CyberwareSlot.MUSCLE);
    }
}
