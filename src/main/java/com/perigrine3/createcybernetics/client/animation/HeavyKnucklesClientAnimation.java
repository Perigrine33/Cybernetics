package com.perigrine3.createcybernetics.client.animation;

import com.perigrine3.createcybernetics.item.cyberware.arm.HeavyKnucklesItem;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class HeavyKnucklesClientAnimation {

    private static final float SMASH_DURATION_TICKS = 10.0F;
    private static final float RAISE_DISTANCE = 1.0F;

    private static final Map<UUID, Float> LAST_ATTACK_PROGRESS = new HashMap<>();
    private static final Map<UUID, Integer> SMASH_START_TICK = new HashMap<>();

    private HeavyKnucklesClientAnimation() {}

    public static void applyThirdPerson(PlayerModel<?> model, AbstractClientPlayer player, float ageInTicks) {
        if (model == null) return;
        PlayerAnimationPoseState.restoreEffect(model, PlayerAnimationPoseState.Effect.HEAVY_KNUCKLES);
        if (player == null || PlayerAnimationPoseState.isFirstPerson(model)) {
            return;
        }

        float partialTick = getPartialTick(player, ageInTicks);

        updateSmashState(player, partialTick);

        float smashProgress = getSmashProgress(player, partialTick);

        if (smashProgress >= 0.0F) {
            applySmash(model, smashProgress);
            return;
        }

        if (HeavyKnucklesItem.canSmash(player)) {
            applyRaiseArms(model, player);
        }
    }

    /** Retained for compatibility; Heavy Knuckles never poses first-person arms. */
    public static void applyFirstPersonArm(AbstractClientPlayer player, ModelPart part, HumanoidArm arm) {
        if (part != null) PlayerAnimationPoseState.restoreHand(part);
    }

    private static void updateSmashState(AbstractClientPlayer player, float partialTick) {
        UUID id = player.getUUID();

        if (!HeavyKnucklesItem.canUseHeavyKnuckles(player)) {
            LAST_ATTACK_PROGRESS.remove(id);
            SMASH_START_TICK.remove(id);
            return;
        }

        float attackProgress = player.getAttackAnim(partialTick);
        float lastAttackProgress = LAST_ATTACK_PROGRESS.getOrDefault(id, 0.0F);

        if (HeavyKnucklesItem.canSmash(player) && attackProgress > 0.0F && lastAttackProgress <= 0.0F) {
            SMASH_START_TICK.put(id, player.tickCount);
        }

        LAST_ATTACK_PROGRESS.put(id, attackProgress);

        Integer startTick = SMASH_START_TICK.get(id);
        if (startTick != null && (player.tickCount < startTick || player.tickCount - startTick > SMASH_DURATION_TICKS)) {
            SMASH_START_TICK.remove(id);
        }
    }

    private static float getSmashProgress(AbstractClientPlayer player, float partialTick) {
        Integer startTick = SMASH_START_TICK.get(player.getUUID());
        if (startTick == null) {
            return -1.0F;
        }

        float elapsed = player.tickCount + partialTick - startTick;

        if (elapsed < 0.0F || elapsed > SMASH_DURATION_TICKS) {
            return -1.0F;
        }

        return elapsed / 20.0F;
    }

    private static void applyRaiseArms(PlayerModel<?> model, AbstractClientPlayer player) {
        applyRaisePose(model, getRaiseProgress(player));
    }

    static void applyRaisePose(PlayerModel<?> model, float progress) {
        PlayerAnimationPoseState.beginEffect(model, PlayerAnimationPoseState.Effect.HEAVY_KNUCKLES);
        float raised = degrees(-135.0F);

        model.rightArm.xRot = Mth.lerp(progress, model.rightArm.xRot, raised);
        model.leftArm.xRot = Mth.lerp(progress, model.leftArm.xRot, raised);
        syncOuterLayers(model);
    }

    private static float getRaiseProgress(AbstractClientPlayer player) {
        float distance = player.fallDistance - HeavyKnucklesItem.MIN_SMASH_FALL_DISTANCE;
        return Mth.clamp(distance / RAISE_DISTANCE, 0.0F, 1.0F);
    }

    static void applySmash(PlayerModel<?> model, float time) {
        PlayerAnimationPoseState.beginEffect(model, PlayerAnimationPoseState.Effect.HEAVY_KNUCKLES);
        float headRotation = sampleFour(time, 0.0F, 0.0F, 0.05F, 22.5F, 0.10F, 22.5F, 0.25F, 0.0F);
        float bodyRotation = sampleFour(time, 0.0F, 0.0F, 0.05F, 37.5F, 0.10F, 37.5F, 0.25F, 0.0F);
        float armRotation = sampleFour(time, 0.0F, -135.0F, 0.05F, -62.5F, 0.10F, -52.5F, 0.25F, 0.0F);

        float headY = sampleFour(time, 0.0F, 0.0F, 0.05F, -3.0F, 0.10F, -3.0F, 0.25F, 0.0F);
        float headZ = sampleFour(time, 0.0F, 0.0F, 0.05F, -7.0F, 0.10F, -7.0F, 0.25F, 0.0F);

        float bodyY = sampleFour(time, 0.0F, 0.0F, 0.05F, -3.0F, 0.10F, -3.0F, 0.25F, 0.0F);
        float bodyZ = sampleFour(time, 0.0F, 0.0F, 0.05F, -7.0F, 0.10F, -7.0F, 0.25F, 0.0F);

        float armY = sampleThree(time, 0.0F, 0.0F, 0.05F, -3.0F, 0.25F, 0.0F);
        float armZ = sampleThree(time, 0.0F, 0.0F, 0.05F, -5.0F, 0.25F, 0.0F);

        model.head.xRot = degrees(headRotation);
        model.head.y += headY;
        model.head.z += headZ;

        model.body.xRot = degrees(bodyRotation);
        model.body.y += bodyY;
        model.body.z += bodyZ;

        model.rightArm.xRot = degrees(armRotation);
        model.rightArm.y += armY;
        model.rightArm.z += armZ;

        model.leftArm.xRot = degrees(armRotation);
        model.leftArm.y += armY;
        model.leftArm.z += armZ;
        syncOuterLayers(model);
    }

    public static void clear() {
        LAST_ATTACK_PROGRESS.clear();
        SMASH_START_TICK.clear();
    }

    private static void syncOuterLayers(PlayerModel<?> model) {
        model.hat.copyFrom(model.head);
        model.jacket.copyFrom(model.body);
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
    }

    private static float sampleThree(float time, float time1, float value1, float time2, float value2, float time3, float value3) {
        if (time <= time1) {
            return value1;
        }

        if (time <= time2) {
            return lerp(time, time1, value1, time2, value2);
        }

        if (time <= time3) {
            return lerp(time, time2, value2, time3, value3);
        }

        return value3;
    }

    private static float sampleFour(float time, float time1, float value1, float time2, float value2, float time3, float value3, float time4, float value4) {
        if (time <= time1) {
            return value1;
        }

        if (time <= time2) {
            return lerp(time, time1, value1, time2, value2);
        }

        if (time <= time3) {
            return lerp(time, time2, value2, time3, value3);
        }

        if (time <= time4) {
            return lerp(time, time3, value3, time4, value4);
        }

        return value4;
    }

    private static float lerp(float time, float startTime, float startValue, float endTime, float endValue) {
        if (endTime <= startTime) {
            return endValue;
        }

        float progress = Mth.clamp((time - startTime) / (endTime - startTime), 0.0F, 1.0F);
        return Mth.lerp(progress, startValue, endValue);
    }

    private static float getPartialTick(AbstractClientPlayer player, float ageInTicks) {
        float partialTick = ageInTicks - player.tickCount;

        if (partialTick < 0.0F || partialTick > 1.0F) {
            return 0.0F;
        }

        return partialTick;
    }

    private static float degrees(float degrees) {
        return degrees * Mth.DEG_TO_RAD;
    }
}
