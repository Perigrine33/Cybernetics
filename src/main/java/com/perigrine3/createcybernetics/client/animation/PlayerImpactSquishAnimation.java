package com.perigrine3.createcybernetics.client.animation;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;


public final class PlayerImpactSquishAnimation {

    private PlayerImpactSquishAnimation() {}

    public static void apply(PlayerModel<AbstractClientPlayer> model, AbstractClientPlayer player, float partialTick) {
        apply(model, PlayerImpactSquishClientState.getSquish(player, partialTick));
    }

    static void apply(PlayerModel<?> model, float strength) {
        PlayerAnimationPoseState.restoreEffect(model, PlayerAnimationPoseState.Effect.IMPACT_SQUISH);
        if (strength <= 0.0F || PlayerAnimationPoseState.isFirstPerson(model)) return;

        PlayerAnimationPoseState.beginEffect(model, PlayerAnimationPoseState.Effect.IMPACT_SQUISH);

        float bodyScaleY = Mth.lerp(strength, 1.0F, 0.45F);
        float limbScaleY = Mth.lerp(strength, 1.0F, 0.55F);

        float headMoveY = 7.0F * strength;
        float bodyMoveY = 5.0F * strength;
        float armMoveY = 5.0F * strength;
        float legMoveY = 0.0F;

        model.body.y += bodyMoveY;
        model.body.yScale *= bodyScaleY;

        model.head.y += headMoveY;
        model.head.yScale *= bodyScaleY;

        model.rightArm.y += armMoveY;
        model.rightArm.zRot += 17.5F * Mth.DEG_TO_RAD * strength;

        model.leftArm.y += armMoveY;
        model.leftArm.zRot -= 20.0F * Mth.DEG_TO_RAD * strength;

        model.rightLeg.y += legMoveY;
        model.rightLeg.yScale *= limbScaleY;
        model.rightLeg.zRot += 5.0F * Mth.DEG_TO_RAD * strength;

        model.leftLeg.y += legMoveY;
        model.leftLeg.yScale *= limbScaleY;
        model.leftLeg.zRot -= 5.0F * Mth.DEG_TO_RAD * strength;

        model.hat.y = model.head.y;
        model.hat.x = model.head.x;
        model.hat.z = model.head.z;
        model.hat.xRot = model.head.xRot;
        model.hat.yRot = model.head.yRot;
        model.hat.zRot = model.head.zRot;
        model.hat.xScale = model.head.xScale;
        model.hat.yScale = model.head.yScale;
        model.hat.zScale = model.head.zScale;

        model.jacket.y = model.body.y;
        model.jacket.x = model.body.x;
        model.jacket.z = model.body.z;
        model.jacket.xRot = model.body.xRot;
        model.jacket.yRot = model.body.yRot;
        model.jacket.zRot = model.body.zRot;
        model.jacket.xScale = model.body.xScale;
        model.jacket.yScale = model.body.yScale;
        model.jacket.zScale = model.body.zScale;

        model.rightSleeve.y = model.rightArm.y;
        model.rightSleeve.x = model.rightArm.x;
        model.rightSleeve.z = model.rightArm.z;
        model.rightSleeve.xRot = model.rightArm.xRot;
        model.rightSleeve.yRot = model.rightArm.yRot;
        model.rightSleeve.zRot = model.rightArm.zRot;
        model.rightSleeve.xScale = model.rightArm.xScale;
        model.rightSleeve.yScale = model.rightArm.yScale;
        model.rightSleeve.zScale = model.rightArm.zScale;

        model.leftSleeve.y = model.leftArm.y;
        model.leftSleeve.x = model.leftArm.x;
        model.leftSleeve.z = model.leftArm.z;
        model.leftSleeve.xRot = model.leftArm.xRot;
        model.leftSleeve.yRot = model.leftArm.yRot;
        model.leftSleeve.zRot = model.leftArm.zRot;
        model.leftSleeve.xScale = model.leftArm.xScale;
        model.leftSleeve.yScale = model.leftArm.yScale;
        model.leftSleeve.zScale = model.leftArm.zScale;

        model.rightPants.y = model.rightLeg.y;
        model.rightPants.x = model.rightLeg.x;
        model.rightPants.z = model.rightLeg.z;
        model.rightPants.xRot = model.rightLeg.xRot;
        model.rightPants.yRot = model.rightLeg.yRot;
        model.rightPants.zRot = model.rightLeg.zRot;
        model.rightPants.xScale = model.rightLeg.xScale;
        model.rightPants.yScale = model.rightLeg.yScale;
        model.rightPants.zScale = model.rightLeg.zScale;

        model.leftPants.y = model.leftLeg.y;
        model.leftPants.x = model.leftLeg.x;
        model.leftPants.z = model.leftLeg.z;
        model.leftPants.xRot = model.leftLeg.xRot;
        model.leftPants.yRot = model.leftLeg.yRot;
        model.leftPants.zRot = model.leftLeg.zRot;
        model.leftPants.xScale = model.leftLeg.xScale;
        model.leftPants.yScale = model.leftLeg.yScale;
        model.leftPants.zScale = model.leftLeg.zScale;
    }

    /** Restore the pre-squish pose, including scales (which PartPose does not store). */
    public static void restore(PlayerModel<?> model) {
        PlayerAnimationPoseState.restore(model);
    }
}
