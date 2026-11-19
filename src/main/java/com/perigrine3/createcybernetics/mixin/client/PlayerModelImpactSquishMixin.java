package com.perigrine3.createcybernetics.mixin.client;

import com.perigrine3.createcybernetics.client.animation.PlayerImpactSquishAnimation;
import com.perigrine3.createcybernetics.client.animation.PlayerAnimationPoseState;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelImpactSquishMixin<T extends LivingEntity> {

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
    private void createcybernetics$restoreImpactSquish(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        // A player model is reused for multiple entities and render passes. Undo
        // our previous pose before vanilla or another animation prepares the next one.
        PlayerAnimationPoseState.restore((PlayerModel<?>) (Object) this);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void createcybernetics$applyImpactSquish(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof AbstractClientPlayer player)) return;

        float partialTick = ageInTicks - entity.tickCount;
        PlayerImpactSquishAnimation.apply((PlayerModel<AbstractClientPlayer>) (Object) this, player, partialTick);
    }
}
