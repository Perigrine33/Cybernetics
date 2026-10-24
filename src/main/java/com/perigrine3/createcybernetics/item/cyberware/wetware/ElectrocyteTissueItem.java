package com.perigrine3.createcybernetics.item.cyberware.wetware;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.*;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.entity.ModEntities;
import com.perigrine3.createcybernetics.entity.custom.ArcLightningBoltEntity;
import com.perigrine3.createcybernetics.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ElectrocyteTissueItem extends Item implements ICyberwareItem {
    private static final int COOLDOWN_TICKS = 2400;
    private static final double TRIGGER_RADIUS = 3.0D;

    private final int humanityCost;

    public ElectrocyteTissueItem(Properties props, int humanityCost) {
        super(props);
        this.humanityCost = humanityCost;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.createcybernetics.humanity", humanityCost).withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public int getHumanityCost() {
        return humanityCost;
    }

    @Override
    public Set<CyberwareSlot> getSupportedSlots() {
        return Set.of(CyberwareSlot.MUSCLE);
    }

    @Override
    public boolean replacesOrgan() {
        return true;
    }

    @Override
    public Set<CyberwareSlot> getReplacedOrgans() {
        return Set.of(CyberwareSlot.MUSCLE);
    }

    @Override
    public CyberwareRepairType getRepairType(ItemStack installedStack, CyberwareSlot slot) {
        return CyberwareRepairType.BIOLOGICAL;
    }

    @Override
    public CyberwareDurabilityCategory getDurabilityCategory(ItemStack installedStack, CyberwareSlot slot) {
        return CyberwareDurabilityCategory.WETWARE;
    }

    @Override
    public int getMaxCyberwareDurability(ItemStack installedStack, CyberwareSlot slot) {
        return 1000;
    }

    @Override
    public void onInstalled(Player player) {
    }

    @Override
    public void onRemoved(Player player) {
    }

    @Override
    public void onTick(LivingEntity entity) {
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class ServerHandler {
        private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
        private static final Map<UUID, Integer> LAST_HURT_TIME = new HashMap<>();
        private static final Map<UUID, Float> LAST_HEALTH = new HashMap<>();
        private static final Map<UUID, Long> LAST_TRIGGER_TICK = new HashMap<>();

        private ServerHandler() {}

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onIncomingDamage(LivingIncomingDamageEvent event) {
            LivingEntity entity = event.getEntity();
            if (!(entity instanceof Player player)) return;
            if (!(player.level() instanceof ServerLevel level)) return;
            if (event.getAmount() <= 0.0F) return;

            tryTrigger(level, player, event.getSource());
        }

        @SubscribeEvent
        public static void onLivingDamaged(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event) {
            LivingEntity entity = event.getEntity();
            if (!(entity instanceof Player player)) return;
            if (!(player.level() instanceof ServerLevel level)) return;
            if (event.getNewDamage() <= 0.0F) return;

            tryTrigger(level, player, event.getSource());
        }

        @SubscribeEvent
        public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            UUID id = event.getEntity().getUUID();
            COOLDOWNS.remove(id);
            LAST_HURT_TIME.remove(id);
            LAST_HEALTH.remove(id);
            LAST_TRIGGER_TICK.remove(id);
        }

        private static void tryTrigger(ServerLevel level, Player player, DamageSource source) {
            if (!hasElectrocyteTissue(player)) return;
            if (isOnCooldown(player)) return;
            if (alreadyTriggeredThisTick(player)) return;

            List<LivingEntity> targets = findTargets(level, player, source);
            if (targets.isEmpty()) return;

            markTriggeredThisTick(player);
            setCooldown(player);

            for (LivingEntity target : targets) {
                spawnArcVisual(level, player, target);
                applyLightningHit(level, player, target);
            }

            playArcSounds(level, player);
        }

        private static boolean alreadyTriggeredThisTick(Player player) {
            long now = player.level().getGameTime();
            Long last = LAST_TRIGGER_TICK.get(player.getUUID());
            return last != null && last == now;
        }

        private static void markTriggeredThisTick(Player player) {
            LAST_TRIGGER_TICK.put(player.getUUID(), player.level().getGameTime());
        }

        private static boolean hasElectrocyteTissue(Player player) {
            if (player == null) return false;
            if (!player.hasData(ModAttachments.CYBERWARE)) return false;

            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            if (data == null) return false;

            return data.hasSpecificItem(ModItems.WETWARE_ELECTROCYTEMUSCLE.get(), CyberwareSlot.MUSCLE);
        }

        private static boolean isOnCooldown(Player player) {
            long now = player.level().getGameTime();
            Long until = COOLDOWNS.get(player.getUUID());

            if (until == null) return false;

            if (until <= now) {
                COOLDOWNS.remove(player.getUUID());
                return false;
            }

            if (until - now > COOLDOWN_TICKS) {
                COOLDOWNS.remove(player.getUUID());
                return false;
            }

            return true;
        }

        private static void setCooldown(Player player) {
            COOLDOWNS.put(player.getUUID(), player.level().getGameTime() + COOLDOWN_TICKS);
        }

        private static List<LivingEntity> findTargets(ServerLevel level, Player player, DamageSource source) {
            AABB box = player.getBoundingBox().inflate(TRIGGER_RADIUS);

            List<LivingEntity> targets = level.getEntitiesOfClass(
                    LivingEntity.class,
                    box,
                    entity -> entity != player
                            && entity.isAlive()
                            && !entity.isSpectator()
                            && entity.getBoundingBox().intersects(box)
            );

            Entity causing = source == null ? null : source.getEntity();
            Entity direct = source == null ? null : source.getDirectEntity();

            if (causing instanceof LivingEntity living && living != player && living.isAlive() && !targets.contains(living)) {
                if (living.getBoundingBox().intersects(box)) {
                    targets.add(living);
                }
            }

            if (direct instanceof LivingEntity living && living != player && living.isAlive() && !targets.contains(living)) {
                if (living.getBoundingBox().intersects(box)) {
                    targets.add(living);
                }
            }

            return targets;
        }

        private static void spawnArcVisual(ServerLevel level, Player player, LivingEntity target) {
            ArcLightningBoltEntity bolt = ModEntities.ARC_LIGHTNING_BOLT.get().create(level);
            if (bolt == null) return;

            Vec3 start = player.position().add(0.0D, player.getBbHeight() * 0.55D, 0.0D);
            Vec3 end = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);

            bolt.setArc(start, end);
            level.addFreshEntity(bolt);
        }

        private static void applyLightningHit(ServerLevel level, Player player, LivingEntity target) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);

            if (bolt == null) {
                target.hurt(level.damageSources().lightningBolt(), 5.0F);
                target.setRemainingFireTicks(160);
                return;
            }

            bolt.moveTo(target.getX(), target.getY(), target.getZ());
            bolt.setVisualOnly(true);
            bolt.setDamage(5.0F);

            if (player instanceof ServerPlayer serverPlayer) {
                bolt.setCause(serverPlayer);
            }

            if (!EventHooks.onEntityStruckByLightning(target, bolt)) {
                target.thunderHit(level, bolt);
            }
        }

        private static void playArcSounds(ServerLevel level, Player player) {
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.LIGHTNING_BOLT_IMPACT,
                    SoundSource.PLAYERS,
                    1.4F,
                    50F
            );
        }
    }
}