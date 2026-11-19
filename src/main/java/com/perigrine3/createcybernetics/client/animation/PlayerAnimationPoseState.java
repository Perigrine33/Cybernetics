package com.perigrine3.createcybernetics.client.animation;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/** Client render-thread state. Unwind effects in reverse order to preserve combined animations. */
public final class PlayerAnimationPoseState {
    enum Effect { IMPACT_SQUISH, HEAVY_KNUCKLES }

    private static final Map<PlayerModel<?>, List<AppliedEffect>> MODELS = new WeakHashMap<>();
    private static final Map<ModelPart, Transform> HANDS = new WeakHashMap<>();
    private static final Set<PlayerModel<?>> FIRST_PERSON = Collections.newSetFromMap(new WeakHashMap<>());

    private PlayerAnimationPoseState() {}

    static void beginEffect(PlayerModel<?> model, Effect effect) {
        restoreEffect(model, effect);
        MODELS.computeIfAbsent(model, ignored -> new ArrayList<>()).add(new AppliedEffect(effect, ModelPose.capture(model)));
    }

    static void restoreEffect(PlayerModel<?> model, Effect effect) {
        List<AppliedEffect> effects = MODELS.get(model);
        if (effects == null) return;
        for (int index = 0; index < effects.size(); index++) {
            if (effects.get(index).effect() != effect) continue;
            for (int last = effects.size() - 1; last >= index; last--) effects.remove(last).pose().restore();
            if (effects.isEmpty()) MODELS.remove(model);
            return;
        }
    }

    public static void restore(PlayerModel<?> model) {
        restoreHand(model.rightArm);
        restoreHand(model.leftArm);
        restoreHand(model.rightSleeve);
        restoreHand(model.leftSleeve);
        List<AppliedEffect> effects = MODELS.remove(model);
        if (effects == null) return;
        for (int index = effects.size() - 1; index >= 0; index--) effects.get(index).pose().restore();
    }

    public static void beginThirdPerson(PlayerModel<?> model) {
        restore(model);
        FIRST_PERSON.remove(model);
    }

    public static void beginFirstPerson(PlayerModel<?> model) {
        restore(model);
        FIRST_PERSON.add(model);
    }

    public static void endFirstPerson(PlayerModel<?> model) {
        restore(model);
        FIRST_PERSON.remove(model);
    }

    static boolean isFirstPerson(PlayerModel<?> model) {
        return FIRST_PERSON.contains(model);
    }

    static void beginHandEffect(ModelPart part) {
        restoreHand(part);
        HANDS.put(part, Transform.capture(part));
    }

    static void restoreHand(ModelPart part) {
        Transform original = HANDS.remove(part);
        if (original != null) original.restore(part);
    }

    public static void clear() {
        HANDS.forEach((part, original) -> original.restore(part));
        HANDS.clear();
        for (List<AppliedEffect> effects : MODELS.values()) {
            for (int index = effects.size() - 1; index >= 0; index--) effects.get(index).pose().restore();
        }
        MODELS.clear();
        FIRST_PERSON.clear();
    }

    private record AppliedEffect(Effect effect, ModelPose pose) {}

    private record ModelPose(ModelPart[] parts, Transform[] transforms) {
        private static ModelPose capture(PlayerModel<?> model) {
            ModelPart[] parts = {model.head, model.body, model.rightArm, model.leftArm, model.rightLeg, model.leftLeg,
                    model.hat, model.jacket, model.rightSleeve, model.leftSleeve, model.rightPants, model.leftPants};
            Transform[] transforms = new Transform[parts.length];
            for (int index = 0; index < parts.length; index++) transforms[index] = Transform.capture(parts[index]);
            return new ModelPose(parts, transforms);
        }

        private void restore() {
            for (int index = 0; index < parts.length; index++) transforms[index].restore(parts[index]);
        }
    }

    // Keep scale explicitly: vanilla PartPose only records positions and rotations.
    private record Transform(float x, float y, float z, float xRot, float yRot, float zRot,
                             float xScale, float yScale, float zScale) {
        private static Transform capture(ModelPart part) {
            return new Transform(part.x, part.y, part.z, part.xRot, part.yRot, part.zRot,
                    part.xScale, part.yScale, part.zScale);
        }

        private void restore(ModelPart part) {
            part.x = x;
            part.y = y;
            part.z = z;
            part.xRot = xRot;
            part.yRot = yRot;
            part.zRot = zRot;
            part.xScale = xScale;
            part.yScale = yScale;
            part.zScale = zScale;
        }
    }
}
