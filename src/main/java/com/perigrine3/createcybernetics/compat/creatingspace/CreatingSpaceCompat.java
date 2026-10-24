package com.perigrine3.createcybernetics.compat.creatingspace;

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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;

public final class CreatingSpaceCompat {
    private CreatingSpaceCompat() {}

    public static final String CREATING_SPACE_MODID = "creatingspace";

    public static final int CREATING_SPACE_OXYGEN_MAX_DISPLAY = 3000;
    public static final int CREATING_SPACE_DEPLETION_PER_SECOND = 1;
    public static final int CREATING_SPACE_RECHARGE_PER_SECOND = 10;

    private static boolean bootstrapped = false;
    private static boolean enabled = false;

    public static void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;

        enabled = ModCompats.isInstalled(CREATING_SPACE_MODID);
        if (!enabled) return;

        NeoForge.EVENT_BUS.register(Events.INSTANCE);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isCreatingSpaceDimension(Level level) {
        if (level == null) return false;

        ResourceLocation id = level.dimension().location();
        return CREATING_SPACE_MODID.equals(id.getNamespace());
    }

    public static boolean hasSuitEquivalent(Player player) {
        return enabled
                && player != null
                && CreatingSpaceSuitPredicate.hasCreatingSpaceSuitEquivalentInstalled(player);
    }

    public static boolean isCreatingSpaceOxygenDamage(DamageSource source) {
        if (source == null) return false;

        Optional<ResourceKey<DamageType>> keyOpt = source.typeHolder().unwrapKey();
        if (keyOpt.isEmpty()) return false;

        ResourceLocation id = keyOpt.get().location();
        if (!CREATING_SPACE_MODID.equals(id.getNamespace())) return false;

        String path = id.getPath().toLowerCase(Locale.ROOT);

        return path.equals("suffocation")
                || path.contains("suffocat")
                || path.contains("vacuum")
                || path.contains("no_oxygen")
                || path.contains("asphyx")
                || path.contains("oxygen")
                || path.contains("space");
    }

    public static int getOxygen(Player player) {
        PlayerCyberwareData data = getData(player);
        return data == null ? 0 : data.getCopernicusOxygen();
    }

    public static void setOxygen(Player player, int value) {
        PlayerCyberwareData data = getData(player);
        if (data == null) return;

        data.setCopernicusOxygen(value, CREATING_SPACE_OXYGEN_MAX_DISPLAY);
    }

    private static PlayerCyberwareData getData(Player player) {
        return player.getData(ModAttachments.CYBERWARE);
    }

    private static boolean isOxygenatedEnvironment(Player player) {
        if (player == null) return true;

        if (player.isUnderWater() || player.isInLava()) {
            return false;
        }

        Level level = player.level();

        if (!isCreatingSpaceDimension(level)) {
            return true;
        }

        return CreatingSpaceOxygenAccess.insideOxygenRoom(player);
    }

    private static boolean needsAirSupport(Player player) {
        if (player == null) return false;

        if (player.isUnderWater() || player.isInLava()) {
            return true;
        }

        Level level = player.level();

        return isCreatingSpaceDimension(level) && !CreatingSpaceOxygenAccess.insideOxygenRoom(player);
    }

    private static void applyAirSupport(Player player, PlayerCyberwareData data) {
        if (player == null || data == null) return;
        if (!needsAirSupport(player)) return;

        if (data.getCopernicusOxygen() <= 0) {
            if (player.hasEffect(MobEffects.WATER_BREATHING)) {
                player.removeEffect(MobEffects.WATER_BREATHING);
            }

            if (player.isUnderWater() && player.getAirSupply() > 280) {
                player.setAirSupply(280);
            }

            return;
        }

        player.setAirSupply(player.getMaxAirSupply());
    }

    private static void syncOxygenToClient(Player player, PlayerCyberwareData data) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        PacketDistributor.sendToPlayer(
                serverPlayer,
                new CopernicusOxygenSyncPayload(data.getCopernicusOxygen())
        );
    }

    private static final class CreatingSpaceOxygenAccess {
        private static final String NEED_OXYGEN_CLASS_NAME = "com.rae.creatingspace.content.life_support.INeedOxygen";

        private static final boolean AVAILABLE;
        private static final Class<?> NEED_OXYGEN_CLASS;
        private static final Method INSIDE_OXYGEN_ROOM;

        static {
            Class<?> needOxygenClass = null;
            Method insideOxygenRoomMethod = null;
            boolean available = false;

            try {
                needOxygenClass = Class.forName(NEED_OXYGEN_CLASS_NAME);
                insideOxygenRoomMethod = needOxygenClass.getMethod("insideOxygenRoom");
                available = true;
            } catch (Throwable ignored) {
                available = false;
            }

            NEED_OXYGEN_CLASS = needOxygenClass;
            INSIDE_OXYGEN_ROOM = insideOxygenRoomMethod;
            AVAILABLE = available;
        }

        static boolean insideOxygenRoom(LivingEntity entity) {
            if (!AVAILABLE) return false;
            if (entity == null) return false;
            if (!NEED_OXYGEN_CLASS.isInstance(entity)) return false;

            try {
                Object result = INSIDE_OXYGEN_ROOM.invoke(entity);
                return result instanceof Boolean value && value;
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
            if (!hasSuitEquivalent(player)) return;

            PlayerCyberwareData data = getData(player);
            if (data == null) return;

            DamageSource source = event.getSource();

            if (isCreatingSpaceOxygenDamage(source)) {
                if (isOxygenatedEnvironment(player) || data.getCopernicusOxygen() > 0) {
                    event.setCanceled(true);
                }

                syncOxygenToClient(player, data);
                return;
            }

            Optional<ResourceKey<DamageType>> keyOpt = source.typeHolder().unwrapKey();
            if (keyOpt.isEmpty()) return;

            ResourceKey<DamageType> key = keyOpt.get();

            if (key.equals(DamageTypes.FREEZE)
                    || key.equals(DamageTypes.IN_FIRE)
                    || key.equals(DamageTypes.ON_FIRE)
                    || key.equals(DamageTypes.HOT_FLOOR)
                    || key.equals(DamageTypes.LAVA)) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public void onPlayerTickPost(PlayerTickEvent.Post event) {
            if (!isEnabled()) return;

            Player player = event.getEntity();
            Level level = player.level();

            if (level.isClientSide()) return;
            if (player.isSpectator()) return;

            PlayerCyberwareData data = getData(player);
            if (data == null) return;

            boolean hasSuitEquivalent = hasSuitEquivalent(player);
            if (!hasSuitEquivalent && data.getCopernicusOxygen() >= CREATING_SPACE_OXYGEN_MAX_DISPLAY) {
                return;
            }

            int previousOxygen = data.getCopernicusOxygen();
            boolean oxygenatedEnvironment = isOxygenatedEnvironment(player);

            data.setCopernicusOxygenatedEnvironment(oxygenatedEnvironment);

            if (oxygenatedEnvironment) {
                data.tickCopernicusOxygen(
                        true,
                        CREATING_SPACE_DEPLETION_PER_SECOND,
                        CREATING_SPACE_RECHARGE_PER_SECOND,
                        CREATING_SPACE_OXYGEN_MAX_DISPLAY
                );
            } else if (hasSuitEquivalent && !player.isCreative()) {
                data.tickCopernicusOxygen(
                        false,
                        CREATING_SPACE_DEPLETION_PER_SECOND,
                        CREATING_SPACE_RECHARGE_PER_SECOND,
                        CREATING_SPACE_OXYGEN_MAX_DISPLAY
                );
            }

            if (data.getCopernicusOxygen() != previousOxygen) {
                syncOxygenToClient(player, data);
            }

            if (!hasSuitEquivalent) return;
            if (player.isCreative()) return;

            applyAirSupport(player, data);

            if (player.getTicksFrozen() > 0) {
                player.setTicksFrozen(0);
            }

            if (player.isOnFire()) {
                player.clearFire();
            }
        }
    }
}