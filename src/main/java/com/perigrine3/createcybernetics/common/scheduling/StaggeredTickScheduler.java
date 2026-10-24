package com.perigrine3.createcybernetics.common.scheduling;

import net.minecraft.world.entity.Entity;

public final class StaggeredTickScheduler {

    public enum TickPhase {
        DURABILITY_PASSIVE(1),
        CREATING_SPACE_OXYGEN(2),
        CRITICAL_ORGANS(3),
        MACROPROGRAM_CLEANUP(4),
        SYNERGY_REFRESH(5),
        HUD_CAPACITY_VERIFY(6),
        DATA_INTEGRITY(7),
        INSOMNIA(9),
        SCULKED_CHECK(11),
        REJECTION_CHECK(13),
        ENERGY_DEBUG(14),
        SYNTH_LUNGS(15),
        BIOCHIP_DOWNLOAD(17),
        MECHANICAL_HEART(19);

        private final int offset;

        TickPhase(int offset) {
            this.offset = offset;
        }

        public int offset() {
            return offset;
        }
    }

    private StaggeredTickScheduler() {}

    public static boolean shouldRun(Entity entity, int intervalTicks, TickPhase phase) {
        if (entity == null || intervalTicks <= 1) {
            return true;
        }
        int phaseOffset = phase != null ? phase.offset() : 0;
        int entitySalt = mixEntityId(entity.getId());
        return Math.floorMod(entity.tickCount + entitySalt + phaseOffset, intervalTicks) == 0;
    }

    public static boolean shouldRunLevel(long gameTime, Entity entity, int intervalTicks, TickPhase phase) {
        if (intervalTicks <= 1) {
            return true;
        }
        int phaseOffset = phase != null ? phase.offset() : 0;
        int entitySalt = entity != null ? mixEntityId(entity.getId()) : 0;
        return Math.floorMod(gameTime + entitySalt + phaseOffset, intervalTicks) == 0;
    }

    private static int mixEntityId(int entityId) {
        int h = entityId * 0x9E3779B9;
        return (h ^ (h >>> 16)) & 0x7FFFFFFF;
    }
}
