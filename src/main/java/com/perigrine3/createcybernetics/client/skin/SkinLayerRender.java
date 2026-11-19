package com.perigrine3.createcybernetics.client.skin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.client.animation.PlayerAnimationPoseState;
import com.perigrine3.createcybernetics.client.CyberspaceColorConfigClient;
import com.perigrine3.createcybernetics.client.render.CyberwareLimbHider;
import com.perigrine3.createcybernetics.compat.bettercombat.BetterCombatCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderArmEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SkinLayerRender {
    private SkinLayerRender() {}

    private static final Map<UUID, FirstPersonArmRenderRequest> FP_RIGHT = new HashMap<>();
    private static final Map<UUID, FirstPersonArmRenderRequest> FP_LEFT = new HashMap<>();

    private record FirstPersonArmRenderRequest(
            boolean replaceVanillaArm,
            boolean hideVanillaSleeve,
            boolean vanillaSleeveEnabled,
            boolean cyberspaceAvatar
    ) {
        boolean shouldCancelVanillaRender() {
            return replaceVanillaArm || hideVanillaSleeve || cyberspaceAvatar;
        }

        boolean shouldRenderVanillaSleeve() {
            return vanillaSleeveEnabled && !hideVanillaSleeve;
        }

        boolean shouldRenderOverlaySleeve() {
            return vanillaSleeveEnabled;
        }
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, value = Dist.CLIENT)
    public static final class FirstPersonSkinOverlayRenderer {
        private FirstPersonSkinOverlayRenderer() {}

        @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
        public static void onRenderArmCancel(RenderArmEvent event) {
            if (BetterCombatCompat.LOADED) return;

            AbstractClientPlayer player = event.getPlayer();
            if (player == null) return;

            HumanoidArm arm = event.getArm();

            if (arm == HumanoidArm.RIGHT && !CyberwareLimbHider.shouldRenderRightArm(player)) {
                event.setCanceled(true);
                return;
            }

            if (arm == HumanoidArm.LEFT && !CyberwareLimbHider.shouldRenderLeftArm(player)) {
                event.setCanceled(true);
                return;
            }

            boolean cyberspaceAvatar = SkinModifierManager.isCyberspaceAvatar(player);

            SkinModifierState state = SkinModifierManager.getPlayerSkinState(player);
            if (!cyberspaceAvatar && (state == null || !state.hasModifiers())) return;

            UUID id = player.getUUID();

            boolean replaceVanillaArm = false;

            if (state != null) {
                for (SkinModifier modifier : state.getModifiers()) {
                    if (modifier != null && modifier.replacesVanillaArm(arm)) {
                        replaceVanillaArm = true;
                        break;
                    }
                }
            }

            boolean hideVanillaSleeve = state != null && shouldHideVanillaSleeve(state, arm);
            boolean vanillaSleeveEnabled = isSleeveEnabled(player, arm);

            FirstPersonArmRenderRequest request =
                    new FirstPersonArmRenderRequest(replaceVanillaArm, hideVanillaSleeve, vanillaSleeveEnabled, cyberspaceAvatar);

            if (arm == HumanoidArm.RIGHT) {
                FP_RIGHT.put(id, request);
            } else {
                FP_LEFT.put(id, request);
            }

            if (request.shouldCancelVanillaRender()) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
        public static void onRenderArm(RenderArmEvent event) {
            if (BetterCombatCompat.LOADED) return;

            AbstractClientPlayer player = event.getPlayer();
            if (player == null) return;

            HumanoidArm arm = event.getArm();

            if (arm == HumanoidArm.RIGHT && !CyberwareLimbHider.shouldRenderRightArm(player)) {
                FP_RIGHT.remove(player.getUUID());
                return;
            }

            if (arm == HumanoidArm.LEFT && !CyberwareLimbHider.shouldRenderLeftArm(player)) {
                FP_LEFT.remove(player.getUUID());
                return;
            }

            UUID id = player.getUUID();

            FirstPersonArmRenderRequest request = arm == HumanoidArm.RIGHT
                    ? FP_RIGHT.remove(id)
                    : FP_LEFT.remove(id);

            if (request == null || !request.shouldCancelVanillaRender()) return;

            SkinModifierState state = SkinModifierManager.getPlayerSkinState(player);

            Minecraft mc = Minecraft.getInstance();

            EntityRenderer<? super AbstractClientPlayer> renderer =
                    mc.getEntityRenderDispatcher().getRenderer(player);

            if (!(renderer instanceof PlayerRenderer playerRenderer)) return;

            PlayerModel<AbstractClientPlayer> model = playerRenderer.getModel();
            PlayerSkin.Model modelType = player.getSkin().model();

            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource buffer = event.getMultiBufferSource();
            int light = event.getPackedLight();

            int cyberspaceColor = CyberspaceColorConfigClient.getColor(player.getUUID());

            ModelPart armPart = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
            ModelPart sleevePart = arm == HumanoidArm.RIGHT ? model.rightSleeve : model.leftSleeve;

            SkinModifier replacementModifier = state != null
                    ? findReplacementArmModifier(state, arm)
                    : null;

            var prevRightPose = model.rightArmPose;
            var prevLeftPose = model.leftArmPose;
            boolean prevCrouching = model.crouching;
            float prevSwimAmount = model.swimAmount;
            float prevAttackTime = model.attackTime;

            boolean prevHeadVisible = model.head.visible;
            boolean prevHatVisible = model.hat.visible;
            boolean prevBodyVisible = model.body.visible;
            boolean prevJacketVisible = model.jacket.visible;
            boolean prevRightArmVisible = model.rightArm.visible;
            boolean prevRightSleeveVisible = model.rightSleeve.visible;
            boolean prevLeftArmVisible = model.leftArm.visible;
            boolean prevLeftSleeveVisible = model.leftSleeve.visible;
            boolean prevRightLegVisible = model.rightLeg.visible;
            boolean prevRightPantsVisible = model.rightPants.visible;
            boolean prevLeftLegVisible = model.leftLeg.visible;
            boolean prevLeftPantsVisible = model.leftPants.visible;

            poseStack.pushPose();

            try {
                PlayerAnimationPoseState.beginFirstPerson(model);
                poseStack.scale(1.005F, 1.005F, 1.005F);

                model.attackTime = 0.0F;
                model.crouching = false;
                model.swimAmount = 0.0F;
                model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
                model.leftArmPose = HumanoidModel.ArmPose.EMPTY;

                model.head.visible = false;
                model.hat.visible = false;
                model.body.visible = false;
                model.jacket.visible = false;
                model.rightLeg.visible = false;
                model.rightPants.visible = false;
                model.leftLeg.visible = false;
                model.leftPants.visible = false;

                model.rightArm.visible = arm == HumanoidArm.RIGHT;
                model.rightSleeve.visible = arm == HumanoidArm.RIGHT && request.shouldRenderOverlaySleeve();
                model.leftArm.visible = arm == HumanoidArm.LEFT;
                model.leftSleeve.visible = arm == HumanoidArm.LEFT && request.shouldRenderOverlaySleeve();

                model.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.colorMask(true, true, true, true);

                if (request.replaceVanillaArm && replacementModifier != null) {
                    ResourceLocation replacementTexture = replacementModifier.getTexture(modelType);

                    RenderType replacementRenderType = request.cyberspaceAvatar
                            ? SkinRenderTypes.cyberspacePlayer(replacementTexture)
                            : RenderType.entityTranslucent(replacementTexture);

                    var replacementVc = buffer.getBuffer(replacementRenderType);

                    int replacementColor = request.cyberspaceAvatar
                            ? cyberspaceColor
                            : 0xFFFFFFFF;

                    armPart.render(poseStack, replacementVc, light, OverlayTexture.NO_OVERLAY, replacementColor);

                    if (request.shouldRenderOverlaySleeve()) {
                        sleevePart.render(poseStack, replacementVc, light, OverlayTexture.NO_OVERLAY, replacementColor);
                    }
                } else {
                    ResourceLocation baseSkinTexture = player.getSkin().texture();

                    RenderType baseRenderType = request.cyberspaceAvatar
                            ? SkinRenderTypes.cyberspacePlayer(baseSkinTexture)
                            : RenderType.entityTranslucent(baseSkinTexture);

                    var baseVc = buffer.getBuffer(baseRenderType);

                    int baseColor = request.cyberspaceAvatar
                            ? cyberspaceColor
                            : 0xFFFFFFFF;

                    armPart.render(poseStack, baseVc, light, OverlayTexture.NO_OVERLAY, baseColor);

                    if (request.shouldRenderVanillaSleeve()) {
                        sleevePart.render(poseStack, baseVc, light, OverlayTexture.NO_OVERLAY, baseColor);
                    }
                }

                if (!request.replaceVanillaArm && state != null && needsPlayerSkinUnderlay(state)) {
                    ResourceLocation baseSkinTexture = player.getSkin().texture();

                    RenderType underlayRenderType = request.cyberspaceAvatar
                            ? SkinRenderTypes.cyberspacePlayer(baseSkinTexture)
                            : RenderType.entityTranslucent(baseSkinTexture);

                    var underlayVc = buffer.getBuffer(underlayRenderType);

                    int underlayColor = request.cyberspaceAvatar
                            ? cyberspaceColor
                            : 0xFFFFFFFF;

                    armPart.render(poseStack, underlayVc, light, OverlayTexture.NO_OVERLAY, underlayColor);

                    if (request.shouldRenderVanillaSleeve()) {
                        sleevePart.render(poseStack, underlayVc, light, OverlayTexture.NO_OVERLAY, underlayColor);
                    }
                }

                if (state != null) {
                    for (SkinModifier modifier : state.getModifiers()) {
                        if (modifier == null) continue;
                        if (!modifierRendersThisFirstPersonArm(modifier, arm)) continue;

                        ResourceLocation overlayTexture = modifier.getTexture(modelType);

                        RenderType overlayRenderType = request.cyberspaceAvatar
                                ? SkinRenderTypes.cyberspacePlayer(overlayTexture)
                                : RenderType.entityTranslucent(overlayTexture);

                        int color = request.cyberspaceAvatar
                                ? cyberspaceColor
                                : modifier.getColor();

                        var vc = buffer.getBuffer(overlayRenderType);

                        if (arm == HumanoidArm.RIGHT) {
                            if (modifier.rendersOverlayPart(SkinModifier.OverlayPart.RIGHT_ARM)) {
                                model.rightArm.render(poseStack, vc, light, OverlayTexture.NO_OVERLAY, color);
                            }

                            if (request.shouldRenderOverlaySleeve()
                                    && modifier.rendersOverlayPart(SkinModifier.OverlayPart.RIGHT_SLEEVE)) {
                                model.rightSleeve.render(poseStack, vc, light, OverlayTexture.NO_OVERLAY, color);
                            }
                        } else {
                            if (modifier.rendersOverlayPart(SkinModifier.OverlayPart.LEFT_ARM)) {
                                model.leftArm.render(poseStack, vc, light, OverlayTexture.NO_OVERLAY, color);
                            }

                            if (request.shouldRenderOverlaySleeve()
                                    && modifier.rendersOverlayPart(SkinModifier.OverlayPart.LEFT_SLEEVE)) {
                                model.leftSleeve.render(poseStack, vc, light, OverlayTexture.NO_OVERLAY, color);
                            }
                        }

                        if (modifier.hasGlint()) {
                            var glintVc = buffer.getBuffer(SkinRenderTypes.translucentGlintOverlay(overlayTexture));

                            if (arm == HumanoidArm.RIGHT) {
                                if (modifier.rendersOverlayPart(SkinModifier.OverlayPart.RIGHT_ARM)) {
                                    model.rightArm.render(poseStack, glintVc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
                                }

                                if (request.shouldRenderOverlaySleeve()
                                        && modifier.rendersOverlayPart(SkinModifier.OverlayPart.RIGHT_SLEEVE)) {
                                    model.rightSleeve.render(poseStack, glintVc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
                                }
                            } else {
                                if (modifier.rendersOverlayPart(SkinModifier.OverlayPart.LEFT_ARM)) {
                                    model.leftArm.render(poseStack, glintVc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
                                }

                                if (request.shouldRenderOverlaySleeve()
                                        && modifier.rendersOverlayPart(SkinModifier.OverlayPart.LEFT_SLEEVE)) {
                                    model.leftSleeve.render(poseStack, glintVc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
                                }
                            }
                        }
                    }
                }
            } finally {
                PlayerAnimationPoseState.endFirstPerson(model);
                RenderSystem.colorMask(true, true, true, true);
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();

                model.rightArmPose = prevRightPose;
                model.leftArmPose = prevLeftPose;
                model.crouching = prevCrouching;
                model.swimAmount = prevSwimAmount;
                model.attackTime = prevAttackTime;

                model.head.visible = prevHeadVisible;
                model.hat.visible = prevHatVisible;
                model.body.visible = prevBodyVisible;
                model.jacket.visible = prevJacketVisible;
                model.rightArm.visible = prevRightArmVisible;
                model.rightSleeve.visible = prevRightSleeveVisible;
                model.leftArm.visible = prevLeftArmVisible;
                model.leftSleeve.visible = prevLeftSleeveVisible;
                model.rightLeg.visible = prevRightLegVisible;
                model.rightPants.visible = prevRightPantsVisible;
                model.leftLeg.visible = prevLeftLegVisible;
                model.leftPants.visible = prevLeftPantsVisible;

                poseStack.popPose();
            }
        }

        private static boolean shouldHideVanillaSleeve(SkinModifierState state, HumanoidArm arm) {
            var hide = state.getHideMask();

            return arm == HumanoidArm.RIGHT
                    ? hide.contains(SkinModifier.HideVanilla.RIGHT_SLEEVE)
                    : hide.contains(SkinModifier.HideVanilla.LEFT_SLEEVE);
        }

        private static boolean isSleeveEnabled(AbstractClientPlayer player, HumanoidArm arm) {
            return arm == HumanoidArm.RIGHT
                    ? SkinVanillaWearVisibility.isModelPartShownNow(player, PlayerModelPart.RIGHT_SLEEVE)
                    : SkinVanillaWearVisibility.isModelPartShownNow(player, PlayerModelPart.LEFT_SLEEVE);
        }

        private static SkinModifier findReplacementArmModifier(SkinModifierState state, HumanoidArm arm) {
            for (SkinModifier modifier : state.getModifiers()) {
                if (modifier != null && modifier.replacesVanillaArm(arm)) {
                    return modifier;
                }
            }

            return null;
        }

        private static boolean modifierRendersThisFirstPersonArm(SkinModifier modifier, HumanoidArm arm) {
            if (arm == HumanoidArm.RIGHT) {
                return modifier.rendersOverlayPart(SkinModifier.OverlayPart.RIGHT_ARM)
                        || modifier.rendersOverlayPart(SkinModifier.OverlayPart.RIGHT_SLEEVE);
            }

            return modifier.rendersOverlayPart(SkinModifier.OverlayPart.LEFT_ARM)
                    || modifier.rendersOverlayPart(SkinModifier.OverlayPart.LEFT_SLEEVE);
        }

        private static boolean needsPlayerSkinUnderlay(SkinModifierState state) {
            for (SkinModifier modifier : state.getModifiers()) {
                if (modifier == null) continue;
                if (!modifier.needsPlayerSkinUnderlay()) continue;

                if (FastColor.ARGB32.alpha(modifier.getColor()) < 255) {
                    return true;
                }
            }

            return false;
        }
    }
}
