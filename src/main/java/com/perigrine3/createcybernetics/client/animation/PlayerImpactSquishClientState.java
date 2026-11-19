package com.perigrine3.createcybernetics.client.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class PlayerImpactSquishClientState {
    private static final int DURATION = 4;

    private static final float PEAK_PROGRESS = 0.43F;

    private static final Map<Integer, ImpactSquish> ACTIVE = new HashMap<>();

    private PlayerImpactSquishClientState() {}

    public static void clear() {
        ACTIVE.clear();
    }

    public static void trigger(Entity entity, float strength) {
        if (entity == null) return;

        long gameTime = entity.level().getGameTime();
        ACTIVE.put(entity.getId(), new ImpactSquish(gameTime, Mth.clamp(strength, 0.0F, 1.5F)));
    }

    public static float getSquish(Entity entity, float partialTick) {
        if (entity == null) return 0.0F;

        ImpactSquish squish = ACTIVE.get(entity.getId());
        if (squish == null) return 0.0F;

        float age = entity.level().getGameTime() - squish.startTick + partialTick;
        float progress = age / DURATION;

        if (progress >= 1.0F) {
            ACTIVE.remove(entity.getId());
            return 0.0F;
        }

        float pulse;

        if (progress <= PEAK_PROGRESS) {
            pulse = progress / PEAK_PROGRESS;
        } else {
            pulse = 1.0F - ((progress - PEAK_PROGRESS) / (1.0F - PEAK_PROGRESS));
        }

        pulse = Mth.clamp(pulse, 0.0F, 1.0F);
        pulse = pulse * pulse * (3.0F - 2.0F * pulse);

        return pulse * squish.strength;
    }

    public static void clientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            ACTIVE.clear();
            return;
        }

        long gameTime = minecraft.level.getGameTime();

        Iterator<Map.Entry<Integer, ImpactSquish>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            ImpactSquish squish = iterator.next().getValue();
            if (gameTime - squish.startTick > DURATION + 2) {
                iterator.remove();
            }
        }
    }

    private record ImpactSquish(long startTick, float strength) {}
}
