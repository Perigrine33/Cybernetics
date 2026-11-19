
package com.perigrine3.createcybernetics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.client.ISandevistanSkinOverlayProvider;
import com.perigrine3.createcybernetics.api.client.SandevistanSkinOverlayApi;
import com.perigrine3.createcybernetics.client.skin.SkinHighlight;
import com.perigrine3.createcybernetics.client.skin.SkinModifier;
import com.perigrine3.createcybernetics.client.skin.SkinModifierManager;
import com.perigrine3.createcybernetics.client.skin.SkinModifierState;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.effect.ModEffects;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.network.payload.SandevistanSnapshotC2SPayload;
import com.perigrine3.createcybernetics.network.payload.SandevistanSnapshotPayload;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = CreateCybernetics.MODID, value = Dist.CLIENT)
public final class SandevistanMirageTrail {

    private SandevistanMirageTrail() {}

    /* ---------------- TUNABLES ---------------- */

    // How many snapshots to draw per player (newest -> oldest). Increase for “more frames”.
    private static final int RENDER_SNAPSHOTS = 241;

    // Desired lifetime: 3–5 seconds. Pick 4 seconds default.
    private static final int TRAIL_LIFETIME_TICKS = 4 * 20;

    // Lower alpha -> more transparent ghosts.
    private static final int MAX_BODY_ALPHA = 130;

    private static final float MIRAGE_MODEL_SCALE = 0.9375F;

    // Keep a small delay to avoid drawing the most recent sample on top of the real player.
    private static final double RENDER_DELAY_TICKS = 0.75;

    // Server/client network sampling is tick-based (20Hz).
    private static final double SAMPLE_PERIOD_TICKS = 1.0;

    // Avoid drawing in your face in first-person.
    private static final float MIN_CAMERA_DIST = 0.55F;
    private static final float MIN_CAMERA_DIST_SQR = MIN_CAMERA_DIST * MIN_CAMERA_DIST;

    // Cap snapshots per player (lifetime / period + slack + render delay).
    private static final int MAX_SNAPSHOTS = Math.max(
            (int) Math.ceil(TRAIL_LIFETIME_TICKS / SAMPLE_PERIOD_TICKS) + 8,
            RENDER_SNAPSHOTS + (int) Math.ceil(RENDER_DELAY_TICKS / SAMPLE_PERIOD_TICKS) + 8
    );

    private static final Map<UUID, Deque<Snapshot>> TRAILS = new HashMap<>();

    private static final int FULL_BRIGHT = 0xF000F0;

    /* ---------------- DATA ---------------- */

    private static final class OverlayPass {
        final ResourceLocation texture;
        final int argb;
        final boolean fullBright;

        OverlayPass(ResourceLocation texture, int argb, boolean fullBright) {
            this.texture = texture;
            this.argb = argb;
            this.fullBright = fullBright;
        }
    }

    private static final class SkinSnapshot {
        final OverlayPass[] passes;

        final boolean hideHat;
        final boolean hideJacket;
        final boolean hideLeftSleeve;
        final boolean hideRightSleeve;
        final boolean hideLeftPants;
        final boolean hideRightPants;

        SkinSnapshot(OverlayPass[] passes,
                     boolean hideHat,
                     boolean hideJacket,
                     boolean hideLeftSleeve,
                     boolean hideRightSleeve,
                     boolean hideLeftPants,
                     boolean hideRightPants) {
            this.passes = (passes == null) ? new OverlayPass[0] : passes;
            this.hideHat = hideHat;
            this.hideJacket = hideJacket;
            this.hideLeftSleeve = hideLeftSleeve;
            this.hideRightSleeve = hideRightSleeve;
            this.hideLeftPants = hideLeftPants;
            this.hideRightPants = hideRightPants;
        }

        static SkinSnapshot empty() {
            return new SkinSnapshot(new OverlayPass[0], false, false, false, false, false, false);
        }
    }

    /**
     * Snapshot stores absolute model-part rotations (radians) captured on the sender client.
     * This makes remote rendering match the sender’s pose, instead of “viewer-side guessing”.
     */
    private static final class Snapshot {
        final Vec3 pos;
        final float bodyYawDeg;
        final boolean crouching;

        final float headX, headY, headZ;
        final float bodyX, bodyY, bodyZ;
        final float rArmX, rArmY, rArmZ;
        final float lArmX, lArmY, lArmZ;
        final float rLegX, rLegY, rLegZ;
        final float lLegX, lLegY, lLegZ;

        final float ageInTicks;
        final SkinSnapshot skin;

        int ageTicks;

        Snapshot(Vec3 pos,
                 float bodyYawDeg,
                 boolean crouching,
                 float headX, float headY, float headZ,
                 float bodyX, float bodyY, float bodyZ,
                 float rArmX, float rArmY, float rArmZ,
                 float lArmX, float lArmY, float lArmZ,
                 float rLegX, float rLegY, float rLegZ,
                 float lLegX, float lLegY, float lLegZ,
                 float ageInTicks,
                 SkinSnapshot skin) {
            this.pos = pos;
            this.bodyYawDeg = bodyYawDeg;
            this.crouching = crouching;

            this.headX = headX; this.headY = headY; this.headZ = headZ;
            this.bodyX = bodyX; this.bodyY = bodyY; this.bodyZ = bodyZ;
            this.rArmX = rArmX; this.rArmY = rArmY; this.rArmZ = rArmZ;
            this.lArmX = lArmX; this.lArmY = lArmY; this.lArmZ = lArmZ;
            this.rLegX = rLegX; this.rLegY = rLegY; this.rLegZ = rLegZ;
            this.lLegX = lLegX; this.lLegY = lLegY; this.lLegZ = lLegZ;

            this.ageInTicks = ageInTicks;
            this.skin = (skin == null) ? SkinSnapshot.empty() : skin;

            this.ageTicks = 0;
        }
    }

    /* ---------------- LIFECYCLE ---------------- */

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // Age & prune all trails.
        for (Iterator<Map.Entry<UUID, Deque<Snapshot>>> it = TRAILS.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Deque<Snapshot>> e = it.next();
            Deque<Snapshot> q = e.getValue();
            if (q == null || q.isEmpty()) {
                it.remove();
                continue;
            }
            for (Snapshot s : q) s.ageTicks++;
            while (!q.isEmpty() && q.peekFirst().ageTicks > TRAIL_LIFETIME_TICKS) q.pollFirst();
            if (q.isEmpty()) it.remove();
        }

        // Send one snapshot per tick from local player to server (server relays to trackers + self).
        if (!(mc.player instanceof AbstractClientPlayer self)) return;
        if (!self.isAlive()) return;

        boolean active = self.hasEffect(ModEffects.SANDEVISTAN_EFFECT) && self.isSprinting();
        if (!active) {
            // If server stops relaying, trails will naturally age out; optional local cleanup:
            return;
        }

        sendSnapshotToServer(self);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        PoseStack poseStack = event.getPoseStack();
        if (poseStack == null) return;

        Vec3 camPos = event.getCamera().getPosition();
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(true);

        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        for (var p : mc.level.players()) {
            if (!(p instanceof AbstractClientPlayer player)) continue;

            Deque<Snapshot> q = TRAILS.get(player.getUUID());
            if (q == null || q.isEmpty()) continue;

            EntityRenderer<? super AbstractClientPlayer> er = mc.getEntityRenderDispatcher().getRenderer(player);
            if (!(er instanceof PlayerRenderer playerRenderer)) continue;

            renderTrailForPlayer(poseStack, buffer, playerRenderer, player, q, camPos, partial);
        }

        buffer.endBatch();
    }

    /* ---------------- NETWORK PATH ---------------- */

    /**
     * Called from your ModPayloads playToClient handler.
     * Payload contains sender-captured pose rotations.
     */
    public static void acceptNetworkSnapshot(SandevistanSnapshotPayload p) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        var ent = mc.level.getPlayerByUUID(p.playerId());
        if (!(ent instanceof AbstractClientPlayer player)) return;

        // Still capture overlays locally (good enough; if a viewer doesn’t know sender overlays, that’s expected).
        SkinSnapshot skin = captureSkinSnapshot(player);

        Snapshot snap = new Snapshot(
                new Vec3(p.x(), p.y(), p.z()),
                p.bodyYaw(),
                p.crouching(),

                p.headX(), p.headY(), p.headZ(),
                p.bodyX(), p.bodyY(), p.bodyZ(),
                p.rArmX(), p.rArmY(), p.rArmZ(),
                p.lArmX(), p.lArmY(), p.lArmZ(),
                p.rLegX(), p.rLegY(), p.rLegZ(),
                p.lLegX(), p.lLegY(), p.lLegZ(),

                p.ageInTicks(),
                skin
        );

        Deque<Snapshot> q = TRAILS.computeIfAbsent(p.playerId(), k -> new ArrayDeque<>());
        q.addLast(snap);
        while (q.size() > MAX_SNAPSHOTS) q.pollFirst();
    }

    private static void sendSnapshotToServer(AbstractClientPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        var er = mc.getEntityRenderDispatcher().getRenderer(player);
        if (!(er instanceof PlayerRenderer pr)) return;

        PlayerModel<AbstractClientPlayer> model = pr.getModel();

        // Save current model state so we can safely call setupAnim and read out rotations.
        var prevRightPose = model.rightArmPose;
        var prevLeftPose = model.leftArmPose;
        boolean prevCrouching = model.crouching;
        float prevSwimAmount = model.swimAmount;
        float prevAttackTime = model.attackTime;

        // Save part rotations.
        float shX = model.head.xRot, shY = model.head.yRot, shZ = model.head.zRot;
        float sbX = model.body.xRot, sbY = model.body.yRot, sbZ = model.body.zRot;
        float sraX = model.rightArm.xRot, sraY = model.rightArm.yRot, sraZ = model.rightArm.zRot;
        float slaX = model.leftArm.xRot, slaY = model.leftArm.yRot, slaZ = model.leftArm.zRot;
        float srlX = model.rightLeg.xRot, srlY = model.rightLeg.yRot, srlZ = model.rightLeg.zRot;
        float sllX = model.leftLeg.xRot, sllY = model.leftLeg.yRot, sllZ = model.leftLeg.zRot;

        try {
            model.attackTime = 0.0F;
            model.crouching = player.isCrouching();
            model.swimAmount = 0.0F;
            model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
            model.leftArmPose = HumanoidModel.ArmPose.EMPTY;

            // Use “current tick” state. This is what makes it deterministic across viewers.
            float limbSwing = safeWalkPosition(player, 1.0F);
            float limbSwingAmount = safeWalkSpeed(player, 1.0F);

            float bodyYaw = player.getYRot();
            float headYaw = player.getYHeadRot();
            float pitch = player.getXRot();
            float netHeadYaw = wrapDegrees(headYaw - bodyYaw);

            float ageInTicks = (float) player.tickCount;

            model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, pitch);

            var payload = new SandevistanSnapshotC2SPayload(
                    player.getX(), player.getY(), player.getZ(),
                    bodyYaw,
                    player.isCrouching(),

                    model.head.xRot, model.head.yRot, model.head.zRot,
                    model.body.xRot, model.body.yRot, model.body.zRot,
                    model.rightArm.xRot, model.rightArm.yRot, model.rightArm.zRot,
                    model.leftArm.xRot, model.leftArm.yRot, model.leftArm.zRot,
                    model.rightLeg.xRot, model.rightLeg.yRot, model.rightLeg.zRot,
                    model.leftLeg.xRot, model.leftLeg.yRot, model.leftLeg.zRot,

                    ageInTicks
            );

            PacketDistributor.sendToServer(payload);

        } finally {
            // Restore model state.
            model.rightArmPose = prevRightPose;
            model.leftArmPose = prevLeftPose;
            model.crouching = prevCrouching;
            model.swimAmount = prevSwimAmount;
            model.attackTime = prevAttackTime;

            model.head.xRot = shX; model.head.yRot = shY; model.head.zRot = shZ;
            model.body.xRot = sbX; model.body.yRot = sbY; model.body.zRot = sbZ;
            model.rightArm.xRot = sraX; model.rightArm.yRot = sraY; model.rightArm.zRot = sraZ;
            model.leftArm.xRot = slaX; model.leftArm.yRot = slaY; model.leftArm.zRot = slaZ;
            model.rightLeg.xRot = srlX; model.rightLeg.yRot = srlY; model.rightLeg.zRot = srlZ;
            model.leftLeg.xRot = sllX; model.leftLeg.yRot = sllY; model.leftLeg.zRot = sllZ;
        }
    }

    /* ---------------- RENDERING ---------------- */

    private static void renderTrailForPlayer(
            PoseStack poseStack,
            MultiBufferSource buffer,
            PlayerRenderer playerRenderer,
            AbstractClientPlayer player,
            Deque<Snapshot> q,
            Vec3 camPos,
            float partial
    ) {
        PlayerModel<AbstractClientPlayer> model = playerRenderer.getModel();
        Minecraft mc = Minecraft.getInstance();
        boolean isLocalFirstPerson =
                (mc.player != null && mc.player.getUUID().equals(player.getUUID()))
                        && mc.options.getCameraType() == CameraType.FIRST_PERSON;

        var prevRightPose = model.rightArmPose;
        var prevLeftPose = model.leftArmPose;
        boolean prevCrouching = model.crouching;
        float prevSwimAmount = model.swimAmount;
        float prevAttackTime = model.attackTime;

        boolean prevHat = model.hat.visible;
        boolean prevJacket = model.jacket.visible;
        boolean prevLeftSleeve = model.leftSleeve.visible;
        boolean prevRightSleeve = model.rightSleeve.visible;
        boolean prevLeftPants = model.leftPants.visible;
        boolean prevRightPants = model.rightPants.visible;
        boolean prevYoung = model.young;

        // Save rotations to restore after per-snapshot applyPose.
        float shX = model.head.xRot, shY = model.head.yRot, shZ = model.head.zRot;
        float sbX = model.body.xRot, sbY = model.body.yRot, sbZ = model.body.zRot;
        float sraX = model.rightArm.xRot, sraY = model.rightArm.yRot, sraZ = model.rightArm.zRot;
        float slaX = model.leftArm.xRot, slaY = model.leftArm.yRot, slaZ = model.leftArm.zRot;
        float srlX = model.rightLeg.xRot, srlY = model.rightLeg.yRot, srlZ = model.rightLeg.zRot;
        float sllX = model.leftLeg.xRot, sllY = model.leftLeg.yRot, sllZ = model.leftLeg.zRot;

        try {
            model.young = false;
            model.hat.visible = true;
            model.jacket.visible = true;
            model.leftSleeve.visible = true;
            model.rightSleeve.visible = true;
            model.leftPants.visible = true;
            model.rightPants.visible = true;

            Snapshot[] arr = q.toArray(new Snapshot[0]);

            int delaySamples = (int) Math.ceil(RENDER_DELAY_TICKS / SAMPLE_PERIOD_TICKS);
            int endExclusive = Math.max(0, arr.length - delaySamples);
            if (endExclusive <= 0) return;

            int start = Math.max(0, endExclusive - RENDER_SNAPSHOTS);
            int renderCount = endExclusive - start;
            if (renderCount <= 0) return;

            float scaleAttr = (float) player.getAttributeValue(Attributes.SCALE);
            scaleAttr = Mth.clamp(scaleAttr, 0.0625F, 16.0F);
            float mirageScale = MIRAGE_MODEL_SCALE * scaleAttr;

            PlayerCyberwareData data = PlayerCyberwareData.getForVisual(player, player.registryAccess());
            boolean primaryDyed = data != null
                    && data.hasSpecificItem(ModItems.BONEUPGRADES_SANDEVISTAN.get(), CyberwareSlot.BONE)
                    && data.isDyed(ModItems.BONEUPGRADES_SANDEVISTAN.get(), CyberwareSlot.BONE);

            int primaryDyeColor = primaryDyed
                    ? data.dyeColor(ModItems.BONEUPGRADES_SANDEVISTAN.get(), CyberwareSlot.BONE)
                    : 0xFFFFFFFF;

            for (int i = start; i < endExclusive; i++) {
                Snapshot s = arr[i];

                float t = s.ageTicks / (float) TRAIL_LIFETIME_TICKS;
                if (t < 0.0F || t > 1.0F) continue;

                // Smooth-ish falloff; more “ghost” than “sticker”.
                float alpha = 1.0F - t;
                alpha *= alpha;

                int aBody = (int) (alpha * (float) (primaryDyed ? 200 : MAX_BODY_ALPHA));
                if (aBody <= 0) continue;

                int rgb;

                if (primaryDyed) {
                    rgb = primaryDyeColor & 0x00FFFFFF;
                } else {
                    float indexNorm = (i - start) / (float) Math.max(1, (renderCount - 1));
                    float baseHue = ((player.tickCount + partial) * 0.01F) % 1.0F;
                    float hue = (baseHue + indexNorm) % 1.0F;

                    rgb = Mth.hsvToRgb(hue, 0.90F, 1.0F);
                }

                int hueR = (rgb >> 16) & 0xFF;
                int hueG = (rgb >> 8) & 0xFF;
                int hueB = rgb & 0xFF;

                Vec3 renderPos = s.pos;
                if (isLocalFirstPerson) {
                    Vec3 toCam = renderPos.subtract(camPos);
                    if (toCam.lengthSqr() < MIN_CAMERA_DIST_SQR) continue;
                }

                model.attackTime = 0.0F;
                model.crouching = s.crouching;
                model.swimAmount = 0.0F;
                model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
                model.leftArmPose = HumanoidModel.ArmPose.EMPTY;

                // Apply sender-captured pose rotations (fixes “stiff” remote ghosts).
                applyPoseToModel(model, s);

                double dx = renderPos.x - camPos.x;
                double dy = renderPos.y - camPos.y;
                double dz = renderPos.z - camPos.z;

                poseStack.pushPose();
                try {
                    poseStack.translate(dx, dy, dz);
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - s.bodyYawDeg));
                    poseStack.scale(-1.0F, -1.0F, 1.0F);
                    poseStack.scale(mirageScale, mirageScale, mirageScale);
                    poseStack.translate(0.0F, -1.501F / mirageScale, 0.0F);

                    int packedLight = primaryDyed ? FULL_BRIGHT : LevelRenderer.getLightColor(
                            player.level(),
                            BlockPos.containing(renderPos.x, renderPos.y, renderPos.z)
                    );

                    model.hat.visible = true;
                    model.jacket.visible = true;
                    model.leftSleeve.visible = true;
                    model.rightSleeve.visible = true;
                    model.leftPants.visible = true;
                    model.rightPants.visible = true;

                    applyHideVanilla(model, s.skin);

                    int bodyColor = FastColor.ARGB32.color(aBody, hueR, hueG, hueB);
                    ResourceLocation skin = player.getSkin().texture();

                    var vcBody = buffer.getBuffer(RenderType.entityTranslucent(skin));
                    model.renderToBuffer(poseStack, vcBody, packedLight, OverlayTexture.NO_OVERLAY, bodyColor);

                    if (s.skin.passes.length > 0) {
                        for (OverlayPass pass : s.skin.passes) {
                            if (pass == null || pass.texture == null) continue;

                            int hueTinted = applyHueTint(pass.argb, hueR, hueG, hueB);
                            int faded = multiplyAlpha(hueTinted, aBody);

                            int light = pass.fullBright ? FULL_BRIGHT : packedLight;
                            var vc = buffer.getBuffer(RenderType.entityTranslucent(pass.texture));
                            model.renderToBuffer(poseStack, vc, light, OverlayTexture.NO_OVERLAY, faded);
                        }
                    }
                } finally {
                    poseStack.popPose();
                }
            }
        } finally {
            model.rightArmPose = prevRightPose;
            model.leftArmPose = prevLeftPose;
            model.crouching = prevCrouching;
            model.swimAmount = prevSwimAmount;
            model.attackTime = prevAttackTime;

            model.hat.visible = prevHat;
            model.jacket.visible = prevJacket;
            model.leftSleeve.visible = prevLeftSleeve;
            model.rightSleeve.visible = prevRightSleeve;
            model.leftPants.visible = prevLeftPants;
            model.rightPants.visible = prevRightPants;
            model.young = prevYoung;

            model.head.xRot = shX; model.head.yRot = shY; model.head.zRot = shZ;
            model.body.xRot = sbX; model.body.yRot = sbY; model.body.zRot = sbZ;
            model.rightArm.xRot = sraX; model.rightArm.yRot = sraY; model.rightArm.zRot = sraZ;
            model.leftArm.xRot = slaX; model.leftArm.yRot = slaY; model.leftArm.zRot = slaZ;
            model.rightLeg.xRot = srlX; model.rightLeg.yRot = srlY; model.rightLeg.zRot = srlZ;
            model.leftLeg.xRot = sllX; model.leftLeg.yRot = sllY; model.leftLeg.zRot = sllZ;
        }
    }

    private static void applyPoseToModel(PlayerModel<?> model, Snapshot s) {
        // Base parts (what you already do)
        model.head.xRot = s.headX; model.head.yRot = s.headY; model.head.zRot = s.headZ;
        model.body.xRot = s.bodyX; model.body.yRot = s.bodyY; model.body.zRot = s.bodyZ;
        model.rightArm.xRot = s.rArmX; model.rightArm.yRot = s.rArmY; model.rightArm.zRot = s.rArmZ;
        model.leftArm.xRot = s.lArmX; model.leftArm.yRot = s.lArmY; model.leftArm.zRot = s.lArmZ;
        model.rightLeg.xRot = s.rLegX; model.rightLeg.yRot = s.rLegY; model.rightLeg.zRot = s.rLegZ;
        model.leftLeg.xRot = s.lLegX; model.leftLeg.yRot = s.lLegY; model.leftLeg.zRot = s.lLegZ;

        model.hat.copyFrom(model.head);
        model.jacket.copyFrom(model.body);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftPants.copyFrom(model.leftLeg);
        model.rightPants.copyFrom(model.rightLeg);
    }


    /* ---------------- SKIN SNAPSHOT ---------------- */

    private static SkinSnapshot captureSkinSnapshot(AbstractClientPlayer player) {
        SkinModifierState state = SkinModifierManager.getPlayerSkinState(player);
        if (state == null) return SkinSnapshot.empty();

        List<SkinModifier> mods = state.getModifiers();
        List<SkinHighlight> highs = state.getHighlights();

        int modCount = (mods == null) ? 0 : mods.size();
        int hiCount = (highs == null) ? 0 : highs.size();

        List<ISandevistanSkinOverlayProvider.OverlayPass> externalPasses = SandevistanSkinOverlayApi.capture(player);
        int externalCount = externalPasses.size();

        if (modCount == 0 && hiCount == 0) return SkinSnapshot.empty();

        OverlayPass[] passes = new OverlayPass[modCount + hiCount + externalCount];

        boolean hideHat = false;
        boolean hideJacket = false;
        boolean hideLeftSleeve = false;
        boolean hideRightSleeve = false;
        boolean hideLeftPants = false;
        boolean hideRightPants = false;

        int idx = 0;

        if (mods != null) {
            for (SkinModifier m : mods) {
                if (m == null) continue;

                ResourceLocation tex = extractTextureForPlayer(m, player);
                int tint = extractTint(m, 0xFFFFFFFF);

                Object hideSet = extractHideVanillaSet(m);
                if (hideSet instanceof Iterable<?> it) {
                    for (Object o : it) {
                        if (o == null) continue;
                        String n = enumName(o);
                        if (n == null) continue;

                        if ("HAT".equals(n)) hideHat = true;
                        if ("JACKET".equals(n)) hideJacket = true;
                        if ("LEFT_SLEEVE".equals(n)) hideLeftSleeve = true;
                        if ("RIGHT_SLEEVE".equals(n)) hideRightSleeve = true;
                        if ("LEFT_PANTS".equals(n)) hideLeftPants = true;
                        if ("RIGHT_PANTS".equals(n)) hideRightPants = true;
                    }
                }

                if (extractBoolean(m, false, "hideAllVanillaLayers", "isHideAllVanillaLayers", "getHideAllVanillaLayers")) {
                    hideHat = true;
                    hideJacket = true;
                    hideLeftSleeve = true;
                    hideRightSleeve = true;
                    hideLeftPants = true;
                    hideRightPants = true;
                }

                passes[idx++] = new OverlayPass(tex, tint, false);
            }
        }

        if (highs != null) {
            for (SkinHighlight h : highs) {
                if (h == null) continue;

                ResourceLocation tex = extractTextureForPlayer(h, player);
                int tint = extractTint(h, 0xFFFFFFFF);

                passes[idx++] = new OverlayPass(tex, tint, true);
            }

            for (ISandevistanSkinOverlayProvider.OverlayPass pass : externalPasses) {
                if (pass == null || pass.texture() == null) continue;
                passes[idx++] = new OverlayPass(pass.texture(), pass.color(), pass.fullBright());
            }
        }

        if (idx != passes.length) {
            OverlayPass[] compact = new OverlayPass[idx];
            System.arraycopy(passes, 0, compact, 0, idx);
            passes = compact;
        }

        return new SkinSnapshot(
                passes,
                hideHat,
                hideJacket,
                hideLeftSleeve,
                hideRightSleeve,
                hideLeftPants,
                hideRightPants
        );
    }

    private static void applyHideVanilla(PlayerModel<?> model, SkinSnapshot skin) {
        if (skin == null) return;

        if (skin.hideHat) model.hat.visible = false;
        if (skin.hideJacket) model.jacket.visible = false;
        if (skin.hideLeftSleeve) model.leftSleeve.visible = false;
        if (skin.hideRightSleeve) model.rightSleeve.visible = false;
        if (skin.hideLeftPants) model.leftPants.visible = false;
        if (skin.hideRightPants) model.rightPants.visible = false;
    }

    /* ---------------- COLOR HELPERS ---------------- */

    private static int multiplyAlpha(int argb, int alphaMul0to255) {
        alphaMul0to255 = Mth.clamp(alphaMul0to255, 0, 255);
        int a = FastColor.ARGB32.alpha(argb);
        int newA = (a * alphaMul0to255) / 255;
        return (argb & 0x00FFFFFF) | (newA << 24);
    }

    private static int applyHueTint(int argb, int hueR, int hueG, int hueB) {
        int a = FastColor.ARGB32.alpha(argb);
        int r = FastColor.ARGB32.red(argb);
        int g = FastColor.ARGB32.green(argb);
        int b = FastColor.ARGB32.blue(argb);

        int outR = (r * hueR) / 255;
        int outG = (g * hueG) / 255;
        int outB = (b * hueB) / 255;

        return FastColor.ARGB32.color(a, outR, outG, outB);
    }

    /* ---------------- REFLECTION HELPERS ---------------- */

    private static String enumName(Object enumValue) {
        if (enumValue instanceof Enum<?> e) return e.name();
        try {
            Method m = enumValue.getClass().getMethod("name");
            Object r = m.invoke(enumValue);
            return (r instanceof String s) ? s : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object extractHideVanillaSet(Object obj) {
        Object r = extractByMethod(obj, "getHideVanilla", "hideVanilla", "getHideVanillaLayers", "hideVanillaLayers");
        if (r != null) return r;
        return extractByField(obj, "hideVanilla", "hideVanillaLayers", "hiddenVanilla", "hideLayers");
    }

    private static ResourceLocation extractTextureForPlayer(Object obj, AbstractClientPlayer player) {
        boolean slim = player.getSkin().model() == PlayerSkin.Model.SLIM;

        if (slim) {
            ResourceLocation r = extractResourceLocation(obj,
                    "getSlimTexture", "slimTexture", "slim", "getTextureSlim", "textureSlim");
            if (r != null) return r;
        } else {
            ResourceLocation r = extractResourceLocation(obj,
                    "getWideTexture", "wideTexture", "wide", "getTextureWide", "textureWide");
            if (r != null) return r;
        }

        ResourceLocation generic = extractResourceLocation(obj, "getTexture", "texture");
        if (generic != null) return generic;

        if (slim) {
            ResourceLocation r = extractResourceLocationField(obj, "slimTexture", "slim", "textureSlim");
            if (r != null) return r;
            return extractResourceLocationField(obj, "wideTexture", "wide", "textureWide");
        } else {
            ResourceLocation r = extractResourceLocationField(obj, "wideTexture", "wide", "textureWide");
            if (r != null) return r;
            return extractResourceLocationField(obj, "slimTexture", "slim", "textureSlim");
        }
    }

    private static int extractTint(Object obj, int def) {
        Object r = extractByMethod(obj, "getTint", "tint", "getColor", "color", "getArgb", "argb");
        if (r instanceof Integer i) return i;
        r = extractByField(obj, "tint", "color", "argb", "tintColor");
        if (r instanceof Integer i) return i;
        return def;
    }

    private static boolean extractBoolean(Object obj, boolean def, String... names) {
        Object r = extractByMethod(obj, names);
        if (r instanceof Boolean b) return b;
        r = extractByField(obj, names);
        if (r instanceof Boolean b) return b;
        return def;
    }

    private static Object extractByMethod(Object obj, String... names) {
        for (String n : names) {
            try {
                Method m = obj.getClass().getMethod(n);
                m.setAccessible(true);
                return m.invoke(obj);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static Object extractByField(Object obj, String... names) {
        for (String n : names) {
            try {
                Field f = obj.getClass().getDeclaredField(n);
                f.setAccessible(true);
                return f.get(obj);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static ResourceLocation extractResourceLocation(Object obj, String... methodNames) {
        for (String n : methodNames) {
            try {
                Method m = obj.getClass().getMethod(n);
                m.setAccessible(true);
                Object r = m.invoke(obj);
                if (r instanceof ResourceLocation rl) return rl;
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static ResourceLocation extractResourceLocationField(Object obj, String... fieldNames) {
        for (String n : fieldNames) {
            try {
                Field f = obj.getClass().getDeclaredField(n);
                f.setAccessible(true);
                Object r = f.get(obj);
                if (r instanceof ResourceLocation rl) return rl;
            } catch (Throwable ignored) {}
        }
        return null;
    }

    /* ---------------- WALK ANIM SAFE ACCESS ---------------- */

    private static float safeWalkPosition(AbstractClientPlayer player, float partial) {
        try {
            Object wa = player.walkAnimation;
            Method m = wa.getClass().getMethod("position", float.class);
            Object r = m.invoke(wa, partial);
            return (r instanceof Float f) ? f : 0.0F;
        } catch (Throwable ignored1) {
            try {
                Object wa = player.walkAnimation;
                Method m = wa.getClass().getMethod("position");
                Object r = m.invoke(wa);
                return (r instanceof Float f) ? f : 0.0F;
            } catch (Throwable ignored2) {
                return 0.0F;
            }
        }
    }

    private static float safeWalkSpeed(AbstractClientPlayer player, float partial) {
        try {
            Object wa = player.walkAnimation;
            Method m = wa.getClass().getMethod("speed", float.class);
            Object r = m.invoke(wa, partial);
            return (r instanceof Float f) ? f : 0.0F;
        } catch (Throwable ignored1) {
            try {
                Object wa = player.walkAnimation;
                Method m = wa.getClass().getMethod("speed");
                Object r = m.invoke(wa);
                return (r instanceof Float f) ? f : 0.0F;
            } catch (Throwable ignored2) {
                return 0.0F;
            }
        }
    }

    /* ---------------- ANGLE HELPERS ---------------- */

    private static float wrapDegrees(float deg) {
        deg = deg % 360.0F;
        if (deg >= 180.0F) deg -= 360.0F;
        if (deg < -180.0F) deg += 360.0F;
        return deg;
    }
}
