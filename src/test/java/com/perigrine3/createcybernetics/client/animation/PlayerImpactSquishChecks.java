package com.perigrine3.createcybernetics.client.animation;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.HashMap;
import java.util.List;

/** Uses real Minecraft model parts; no client, world, OpenGL context, or player is created. */
public final class PlayerImpactSquishChecks {
    private static int checks;

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    static PlayerModel<AbstractClientPlayer> model(boolean slim) {
        var children = new HashMap<String, ModelPart>();
        for (String name : List.of("head", "hat", "body", "right_arm", "left_arm", "right_leg", "left_leg",
                "right_sleeve", "left_sleeve", "right_pants", "left_pants", "jacket", "ear", "cloak")) {
            children.put(name, new ModelPart(List.of(), java.util.Map.of()));
        }
        var model = new PlayerModel<AbstractClientPlayer>(new ModelPart(List.of(), children), slim);
        int index = 0;
        for (ModelPart part : parts(model)) {
            // A non-default base pose catches fixes that blindly reset scales to 1.
            part.x = ++index;
            part.y = index * 2;
            part.z = -index;
            part.xRot = index * 0.01F;
            part.yRot = index * 0.02F;
            part.zRot = index * 0.03F;
            part.xScale = 1.2F;
            part.yScale = 1.7F;
            part.zScale = 0.8F;
        }
        return model;
    }

    private static ModelPart[] parts(PlayerModel<?> model) {
        return new ModelPart[] { model.head, model.body, model.rightArm, model.leftArm, model.rightLeg, model.leftLeg,
                model.hat, model.jacket, model.rightSleeve, model.leftSleeve, model.rightPants, model.leftPants };
    }

    static float[][] pose(PlayerModel<?> model) {
        ModelPart[] parts = parts(model);
        float[][] pose = new float[parts.length][];
        for (int index = 0; index < parts.length; index++) {
            ModelPart part = parts[index];
            pose[index] = new float[] { part.x, part.y, part.z, part.xRot, part.yRot, part.zRot,
                    part.xScale, part.yScale, part.zScale };
        }
        return pose;
    }

    private static void expectPose(float[][] expected, PlayerModel<?> model, String message) {
        float[][] actual = pose(model);
        for (int part = 0; part < expected.length; part++) {
            check(java.util.Arrays.equals(expected[part], actual[part]), message + " (part " + part + ")");
        }
    }

    public static void main(String[] args) {
        for (boolean slim : new boolean[] {false, true}) {
            var model = model(slim);
            float[][] original = pose(model);
            for (int jump = 0; jump < 100; jump++) {
                for (float strength : new float[] {0.2F, 0.6F, 1.0F, 0.7F, 0.3F}) {
                    PlayerImpactSquishAnimation.apply(model, strength);
                    check(model.body.yScale < original[1][7], "squish still compresses the body");
                    check(model.hat.yScale == model.head.yScale, "hat follows squished head");
                    check(model.rightPants.yScale == model.rightLeg.yScale, "pants follow squished leg");
                    float[][] squished = pose(model);
                    // Repeated setup/render passes must not multiply an existing deformation.
                    PlayerImpactSquishAnimation.apply(model, strength);
                    expectPose(squished, model, "repeated application does not accumulate");
                    PlayerImpactSquishAnimation.restore(model);
                    expectPose(original, model, "post-render restores the complete original pose");
                }
            }
            PlayerImpactSquishAnimation.apply(model, 1.0F);
            PlayerImpactSquishAnimation.apply(model, 0.0F);
            expectPose(original, model, "expired animation restores even without a post-render callback");
            PlayerImpactSquishAnimation.restore(model);
            expectPose(original, model, "restoring twice is harmless");

            var other = model(!slim);
            float[][] otherOriginal = pose(other);
            PlayerImpactSquishAnimation.apply(model, 0.9F);
            PlayerImpactSquishAnimation.apply(other, 0.4F);
            float[][] otherSquished = pose(other);
            PlayerImpactSquishAnimation.restore(model);
            expectPose(otherSquished, other, "restoration is scoped to the correct model");
            PlayerImpactSquishAnimation.restore(other);
            expectPose(otherOriginal, other, "second model restores independently");

            model.body.yScale = 2.4F;
            float[][] changedPose = pose(model);
            PlayerImpactSquishAnimation.apply(model, 0.5F);
            PlayerImpactSquishAnimation.restore(model);
            expectPose(changedPose, model, "later external scale changes become the new baseline");
        }
        System.out.println("Passed " + checks + " player impact squish checks");
    }
}
