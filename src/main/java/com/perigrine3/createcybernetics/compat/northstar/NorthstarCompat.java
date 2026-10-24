package com.perigrine3.createcybernetics.compat.northstar;

import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.compat.ModCompats;
import com.perigrine3.createcybernetics.network.payload.CopernicusOxygenSyncPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class NorthstarCompat {
    private NorthstarCompat() {}

    public static final String NORTHSTAR_MODID = "northstar";

    public static final int COPERNICUS_OXYGEN_MAX_DISPLAY = 3000;
    public static final int COPERNICUS_DEPLETION_PER_SECOND = 1;
    public static final int COPERNICUS_RECHARGE_PER_SECOND = 10;

    @FunctionalInterface
    public interface SuitPredicate {
        boolean isSuitEquivalent(Player player);
    }

    private static final List<SuitPredicate> SUIT_PREDICATES = new ArrayList<>();

    private static boolean bootstrapped = false;
    private static boolean enabled = false;

    public static void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;

        enabled = ModCompats.isInstalled(NORTHSTAR_MODID);
        if (!enabled) return;

        SUIT_PREDICATES.add(CopernicusSuitPredicate::hasCopernicusSetInstalled);

        NeoForge.EVENT_BUS.register(Events.INSTANCE);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void registerSuitPredicate(SuitPredicate predicate) {
        if (predicate != null) SUIT_PREDICATES.add(predicate);
    }

    public static boolean isNorthstarDimension(Level level) {
        ResourceLocation id = level.dimension().location();
        return NORTHSTAR_MODID.equals(id.getNamespace());
    }

    public static boolean hasSuitEquivalent(Player player) {
        if (!enabled || player == null) return false;
        for (SuitPredicate pred : SUIT_PREDICATES) {
            if (pred.isSuitEquivalent(player)) return true;
        }
        return false;
    }

    public static boolean isNorthstarOxygenDamage(DamageSource source) {
        if (source == null) return false;

        Optional<ResourceKey<DamageType>> keyOpt = source.typeHolder().unwrapKey();
        if (keyOpt.isEmpty()) return false;

        ResourceLocation id = keyOpt.get().location();
        if (!NORTHSTAR_MODID.equals(id.getNamespace())) return false;

        String path = id.getPath().toLowerCase(Locale.ROOT);

        return path.equals("suffocation") || path.contains("suffocat") || path.contains("vacuum") || path.contains("no_oxygen") || path.contains("asphyx") || path.contains("oxygen");
    }

    private static PlayerCyberwareData getData(Player player) {
        return player.getData(ModAttachments.CYBERWARE);
    }

    public static int getOxygen(Player player) {
        PlayerCyberwareData data = getData(player);
        return (data == null) ? 0 : data.getCopernicusOxygen();
    }

    public static void setOxygen(Player player, int value) {
        PlayerCyberwareData data = getData(player);
        if (data == null) return;
        data.setCopernicusOxygen(value, COPERNICUS_OXYGEN_MAX_DISPLAY);
    }

    private static final class NorthstarOxygenAccess {
        private static final boolean AVAILABLE;
        private static final Method HAS_OXYGEN;

        static {
            Method m = null;
            boolean ok = false;
            try {
                Class<?> cls = Class.forName("com.lightning.northstar.world.oxygen.NorthstarOxygen");
                m = cls.getMethod("hasOxygen", Level.class, Vec3.class);
                ok = true;
            } catch (Throwable ignored) {
                ok = false;
            }
            HAS_OXYGEN = m;
            AVAILABLE = ok;
        }

        static boolean hasOxygen(Level level, Vec3 pos) {
            if (!AVAILABLE || level == null || pos == null) return false;
            try {
                Object r = HAS_OXYGEN.invoke(null, level, pos);
                return (r instanceof Boolean b) && b;
            } catch (Throwable ignored) {
                return false;
            }
        }
    }

    private static final class Events {
        private static final Events INSTANCE = new Events();
        private Events() {}

        @SubscribeEvent
        public void onIncomingDamage(LivingIncomingDamageEvent event) {
            if (!isEnabled()) return;

            LivingEntity entity = event.getEntity();
            if (!(entity instanceof Player player)) return;

            Level level = player.level();
            if (level.isClientSide()) return;
            if (!isNorthstarDimension(level)) return;
            if (!CopernicusSuitPredicate.hasCopernicusSetInstalled(player)) return;

            DamageSource source = event.getSource();

            if (isNorthstarOxygenDamage(source)) {
                PlayerCyberwareData data = getData(player);
                if (data == null) return;

                if (data.getCopernicusOxygen() > 0) {
                    event.setCanceled(true);
                }
                return;
            }

            Optional<ResourceKey<DamageType>> keyOpt = source.typeHolder().unwrapKey();
            if (keyOpt.isEmpty()) return;

            ResourceKey<DamageType> key = keyOpt.get();
            if (key.equals(DamageTypes.FREEZE) || key.equals(DamageTypes.IN_FIRE) || key.equals(DamageTypes.ON_FIRE) || key.equals(DamageTypes.HOT_FLOOR) || key.equals(DamageTypes.LAVA)) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public void onPlayerTickPost(PlayerTickEvent.Post event) {
            if (!isEnabled()) return;

            Player player = event.getEntity();
            Level level = player.level();
            if (level.isClientSide()) return;
            if (player.isCreative() || player.isSpectator()) return;
            if (!CopernicusSuitPredicate.hasCopernicusSetInstalled(player)) return;

            PlayerCyberwareData data = getData(player);
            if (data == null) return;

            final boolean oxygenatedEnvironment;
            if (isNorthstarDimension(level)) {
                oxygenatedEnvironment = NorthstarOxygenAccess.hasOxygen(level, player.getEyePosition());
            } else {
                oxygenatedEnvironment = !player.isUnderWater();
            }

            int previousOxygen = data.getCopernicusOxygen();
            data.setCopernicusOxygenatedEnvironment(oxygenatedEnvironment);

            data.tickCopernicusOxygen(oxygenatedEnvironment, COPERNICUS_DEPLETION_PER_SECOND, COPERNICUS_RECHARGE_PER_SECOND, COPERNICUS_OXYGEN_MAX_DISPLAY);

            if (player instanceof ServerPlayer sp && data.getCopernicusOxygen() != previousOxygen) {
                PacketDistributor.sendToPlayer(sp, new CopernicusOxygenSyncPayload(data.getCopernicusOxygen()));
            }

            if (player.getTicksFrozen() > 0) {
                player.setTicksFrozen(0);
            }
            if (player.isOnFire()) {
                player.clearFire();
            }
        }
    }
}
