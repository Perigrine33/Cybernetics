package com.perigrine3.createcybernetics.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.perigrine3.createcybernetics.client.animation.PlayerAnimationPoseState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererHeavyKnucklesMixin {

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void createcybernetics$beginAnimatedPlayerRender(AbstractClientPlayer player, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        PlayerAnimationPoseState.beginThirdPerson(((PlayerRenderer) (Object) this).getModel());
    }

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    private void createcybernetics$endAnimatedPlayerRender(AbstractClientPlayer player, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        PlayerAnimationPoseState.restore(((PlayerRenderer) (Object) this).getModel());
    }

    @Inject(method = "renderHand", at = @At("HEAD"))
    private void createcybernetics$beginAnimatedHandRender(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        PlayerAnimationPoseState.beginFirstPerson(((PlayerRenderer) (Object) this).getModel());
    }

    @Inject(method = "renderHand", at = @At("RETURN"))
    private void createcybernetics$endAnimatedHandRender(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        PlayerAnimationPoseState.endFirstPerson(((PlayerRenderer) (Object) this).getModel());
    }

}
