package com.perigrine3.createcybernetics.event.custom;

import com.perigrine3.createcybernetics.common.scheduling.StaggeredTickScheduler;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.common.attributes.ModAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class InsomniaHandler {
    private InsomniaHandler() {}

    private static final String NBT_LAST_GAME_TIME = "cc_insomnia_last_gt";
    private static final String NBT_ELAPSED_TICKS  = "cc_insomnia_elapsed";
    private static final String NBT_LAST_WRITTEN   = "cc_insomnia_last_written";

    private static final int VANILLA_THRESHOLD = 3 * 24000;
    private static final int UPDATE_INTERVAL_TICKS = 20;
    private static final int MAX_CATCHUP_TICKS = 20 * 5;

    @SubscribeEvent
    public static void onWake(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        long now = sp.serverLevel().getGameTime();
        var pd = sp.getPersistentData();
        pd.putLong(NBT_LAST_GAME_TIME, now);
        pd.putInt(NBT_ELAPSED_TICKS, 0);
        pd.putInt(NBT_LAST_WRITTEN, Integer.MIN_VALUE);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        if (!StaggeredTickScheduler.shouldRun(sp, UPDATE_INTERVAL_TICKS, StaggeredTickScheduler.TickPhase.INSOMNIA)) {
            return;
        }
        AttributeInstance inst = sp.getAttribute(ModAttributes.INSOMNIA);
        if (inst == null) return;

        double days = inst.getValue();
        if (!Double.isFinite(days)) return;

        var pd = sp.getPersistentData();
        if (Math.abs(days - 3.0D) < 1.0E-6D && !pd.contains(NBT_LAST_GAME_TIME)) {
            return;
        }

        long now = sp.serverLevel().getGameTime();
        long last = pd.getLong(NBT_LAST_GAME_TIME);
        if (last == 0L) last = now;

        long deltaL = now - last;
        if (deltaL < 0L) deltaL = 0L;
        if (deltaL > MAX_CATCHUP_TICKS) deltaL = MAX_CATCHUP_TICKS;

        pd.putLong(NBT_LAST_GAME_TIME, now);

        int elapsed = pd.getInt(NBT_ELAPSED_TICKS);

        if (!sp.isSleeping()) {
            elapsed = safeAdd(elapsed, (int) deltaL);
            pd.putInt(NBT_ELAPSED_TICKS, elapsed);
        }

        int desiredTicks;
        if (days <= 0.0D) {
            desiredTicks = 0;
        } else {
            double raw = days * 24000.0D;
            desiredTicks = (raw >= Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) Math.floor(raw);
        }

        int scaled;
        if (desiredTicks <= 0) {
            scaled = VANILLA_THRESHOLD;
        } else if (desiredTicks == VANILLA_THRESHOLD) {
            scaled = elapsed;
        } else {
            double factor = (double) VANILLA_THRESHOLD / (double) desiredTicks;
            double scaledD = (double) elapsed * factor;

            if (scaledD >= Integer.MAX_VALUE) scaled = Integer.MAX_VALUE;
            else if (scaledD <= 0.0D) scaled = 0;
            else scaled = (int) Math.floor(scaledD);
        }

        int lastWritten = pd.getInt(NBT_LAST_WRITTEN);
        if (lastWritten == scaled) return;

        sp.getStats().setValue(sp, Stats.CUSTOM.get(Stats.TIME_SINCE_REST), scaled);
        pd.putInt(NBT_LAST_WRITTEN, scaled);
    }

    private static int safeAdd(int a, int b) {
        long v = (long) a + (long) b;
        if (v > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (v < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) v;
    }
}
