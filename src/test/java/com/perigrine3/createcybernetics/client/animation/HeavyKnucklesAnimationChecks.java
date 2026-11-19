package com.perigrine3.createcybernetics.client.animation;

import net.minecraft.client.model.PlayerModel;

/** Model-level regression coverage; no running client, world, or OpenGL context is needed. */
public final class HeavyKnucklesAnimationChecks {
    private static int checks;

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static void expectPose(float[][] expected, PlayerModel<?> model, String message) {
        check(java.util.Arrays.deepEquals(expected, PlayerImpactSquishChecks.pose(model)), message);
    }

    public static void main(String[] args) {
        for (boolean slim : new boolean[] {false, true}) {
            var model = PlayerImpactSquishChecks.model(slim);
            float[][] original = PlayerImpactSquishChecks.pose(model);
            for (int slam = 0; slam < 100; slam++) {
                for (float time : new float[] {0.0F, 0.025F, 0.075F, 0.15F, 0.25F, 0.5F}) {
                    PlayerAnimationPoseState.beginThirdPerson(model);
                    HeavyKnucklesClientAnimation.applySmash(model, time);
                    float[][] first = PlayerImpactSquishChecks.pose(model);
                    HeavyKnucklesClientAnimation.applySmash(model, time);
                    expectPose(first, model, "repeated render passes cannot move the torso farther forward");
                    check(model.head.z == model.hat.z, "hat follows head");
                    check(model.body.z == model.jacket.z, "jacket follows torso");
                    PlayerAnimationPoseState.restore(model);
                    expectPose(original, model, "all slam offsets are temporary");
                }
            }

            for (float charge : new float[] {0.0F, 0.3F, 0.8F, 1.0F}) {
                HeavyKnucklesClientAnimation.applyRaisePose(model, charge);
                if (charge == 1.0F) {
                    check(Math.abs(model.rightArm.xRot + (float) Math.toRadians(135.0)) < 0.00001F,
                            "third-person charge retains its full wind-up");
                }
                float[][] first = PlayerImpactSquishChecks.pose(model);
                HeavyKnucklesClientAnimation.applyRaisePose(model, charge);
                expectPose(first, model, "charged-arm pose is not cumulative");
                PlayerAnimationPoseState.restore(model);
                expectPose(original, model, "charged-arm pose restores custom baseline");
            }

            for (int slam = 0; slam < 100; slam++) {
                for (boolean squishFirst : new boolean[] {false, true}) {
                    PlayerAnimationPoseState.beginThirdPerson(model);
                    if (squishFirst) PlayerImpactSquishAnimation.apply(model, 0.8F);
                    HeavyKnucklesClientAnimation.applySmash(model, 0.075F);
                    if (!squishFirst) PlayerImpactSquishAnimation.apply(model, 0.8F);
                    check(Math.abs(model.body.z - (original[1][2] - 7.0F)) < 0.0001F, "combined effects retain exactly one forward offset");
                    check(model.body.yScale < original[1][7], "combined effects retain landing compression");
                    PlayerAnimationPoseState.restore(model);
                    expectPose(original, model, "combined effects unwind in reverse order");
                }
            }

            PlayerImpactSquishAnimation.apply(model, 0.5F);
            float[][] squishOnly = PlayerImpactSquishChecks.pose(model);
            HeavyKnucklesClientAnimation.applySmash(model, 0.075F);
            HeavyKnucklesClientAnimation.applyThirdPerson(model, null, 0.0F);
            expectPose(squishOnly, model, "ending knuckles preserves an earlier landing pose");
            PlayerAnimationPoseState.restore(model);

            HeavyKnucklesClientAnimation.applySmash(model, 0.075F);
            float[][] knucklesOnly = PlayerImpactSquishChecks.pose(model);
            PlayerImpactSquishAnimation.apply(model, 0.5F);
            PlayerImpactSquishAnimation.apply(model, 0.0F);
            expectPose(knucklesOnly, model, "ending landing preserves an earlier knuckles pose");
            PlayerAnimationPoseState.restore(model);

            for (int slam = 0; slam < 100; slam++) {
                PlayerAnimationPoseState.beginFirstPerson(model);
                check(PlayerAnimationPoseState.isFirstPerson(model), "first-person pass is identified");
                HeavyKnucklesClientAnimation.applyThirdPerson(model, null, 0.0F);
                PlayerImpactSquishAnimation.apply(model, 1.0F);
                expectPose(original, model, "first-person setup does not deform the hidden head or torso");
                for (var side : net.minecraft.world.entity.HumanoidArm.values()) {
                    var arm = side == net.minecraft.world.entity.HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
                    var sleeve = side == net.minecraft.world.entity.HumanoidArm.RIGHT ? model.rightSleeve : model.leftSleeve;
                    HeavyKnucklesClientAnimation.applyFirstPersonArm(null, arm, side);
                    HeavyKnucklesClientAnimation.applyFirstPersonArm(null, sleeve, side);
                    expectPose(original, model, "first-person arms and sleeves retain their normal pose");
                }
                PlayerAnimationPoseState.endFirstPerson(model);
                expectPose(original, model, "first-person arm and sleeve restore after rendering");
                check(!PlayerAnimationPoseState.isFirstPerson(model), "first-person state is released");
            }

            PlayerAnimationPoseState.beginFirstPerson(model);
            PlayerAnimationPoseState.restore(model);
            check(PlayerAnimationPoseState.isFirstPerson(model), "setup cleanup keeps the active hand-pass marker");
            PlayerAnimationPoseState.beginThirdPerson(model);
            expectPose(original, model, "next full-body render recovers an interrupted hand render");
            check(!PlayerAnimationPoseState.isFirstPerson(model), "full-body render clears a stale hand-pass marker");

            HeavyKnucklesClientAnimation.applySmash(model, 0.075F);
            PlayerAnimationPoseState.beginFirstPerson(model);
            expectPose(original, model, "first-person render recovers an interrupted full-body render");
            PlayerAnimationPoseState.clear();
            HeavyKnucklesClientAnimation.clear();
            expectPose(original, model, "session reset restores live model parts");
            check(!PlayerAnimationPoseState.isFirstPerson(model), "session reset clears pass markers");

            var other = PlayerImpactSquishChecks.model(!slim);
            float[][] otherOriginal = PlayerImpactSquishChecks.pose(other);
            HeavyKnucklesClientAnimation.applySmash(model, 0.075F);
            PlayerImpactSquishAnimation.apply(other, 0.9F);
            PlayerAnimationPoseState.restore(model);
            expectPose(original, model, "first model restores independently");
            check(other.body.yScale < otherOriginal[1][7], "first model cleanup does not cancel the second model");
            PlayerAnimationPoseState.restore(other);
            expectPose(otherOriginal, other, "second model restores independently");
        }
        System.out.println("Passed " + checks + " Heavy Knuckles animation checks");
    }

}
