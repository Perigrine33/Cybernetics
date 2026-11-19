package com.perigrine3.createcybernetics.effect;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.common.damage.ModDamageTypes;
import com.perigrine3.createcybernetics.particle.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

@EventBusSubscriber(modid = CreateCybernetics.MODID)
public class SynapseBurnoutEffect extends MobEffect {
    private static final float MIN_INITIAL_DAMAGE = 8.0F;
    private static final float MAX_INITIAL_DAMAGE = 12.0F;

    private static final float SPARK_BURST_CHANCE = 0.22F;
    private static final float SMOKE_CHANCE = 0.18F;

    private static final double HEAD_SIDE_OFFSET = 0.22D;
    private static final double HEAD_VERTICAL_SPREAD = 0.24D;

    private static final double MIN_SPARK_SPEED = 0.32D;
    private static final double MAX_SPARK_SPEED = 0.72D;

    @SubscribeEvent
    public static void preventMilkCure(MobEffectEvent.Remove event) {
        // Also protect instances saved before milk immunity was introduced.
        if (event.getCure() == EffectCures.MILK && event.getEffect().equals(ModEffects.SYNAPSE_BURNOUT)) {
            event.setCanceled(true);
        }
    }

    public SynapseBurnoutEffect(MobEffectCategory category, int color) {
        super(category, color);

        addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "synapse_burnout_movement_speed"),
                -0.80D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        addAttributeModifier(Attributes.ATTACK_SPEED, ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "synapse_burnout_attack_speed"),
                -0.90D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        addAttributeModifier(Attributes.BLOCK_BREAK_SPEED, ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "synapse_burnout_block_break_speed"),
                -0.75D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public void onEffectAdded(LivingEntity entity, int amplifier) {
        super.onEffectAdded(entity, amplifier);

        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        float damage = MIN_INITIAL_DAMAGE + entity.getRandom().nextFloat() * (MAX_INITIAL_DAMAGE - MIN_INITIAL_DAMAGE);
        float healthBefore = entity.getHealth();

        DamageSource source = level.damageSources().source(ModDamageTypes.BRAIN_DAMAGE);

        entity.invulnerableTime = 0;
        entity.hurt(source, damage);

        if (entity.getHealth() >= healthBefore && entity.isAlive()) {
            float newHealth = Math.max(0.0F, healthBefore - damage);
            entity.setHealth(newHealth);

            if (newHealth <= 0.0F) {
                entity.die(source);
            }
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return true;
        }

        if (entity.getRandom().nextFloat() < SPARK_BURST_CHANCE) {
            spawnSparkBurst(level, entity);
        }

        if (entity.getRandom().nextFloat() < SMOKE_CHANCE) {
            spawnSmoke(level, entity);
        }

        return true;
    }

    private static void spawnSparkBurst(ServerLevel level, LivingEntity entity) {
        Vec3 outward = getRandomHeadDirection(entity);
        Vec3 origin = getHeadParticleOrigin(entity, outward);

        int count = 2 + entity.getRandom().nextInt(4);

        for (int i = 0; i < count; i++) {
            double speed = MIN_SPARK_SPEED + entity.getRandom().nextDouble() * (MAX_SPARK_SPEED - MIN_SPARK_SPEED);

            Vec3 velocity = outward.scale(speed).add(
                    (entity.getRandom().nextDouble() - 0.5D) * 0.30D,
                    (entity.getRandom().nextDouble() - 0.5D) * 0.30D,
                    (entity.getRandom().nextDouble() - 0.5D) * 0.30D
            );

            double x = origin.x + (entity.getRandom().nextDouble() - 0.5D) * 0.10D;
            double y = origin.y + (entity.getRandom().nextDouble() - 0.5D) * 0.10D;
            double z = origin.z + (entity.getRandom().nextDouble() - 0.5D) * 0.10D;

            level.sendParticles(ModParticles.SPARKING.get(),
                    x, y, z, 0,
                    velocity.x, velocity.y, velocity.z, 1.0D
            );
        }
    }

    private static void spawnSmoke(ServerLevel level, LivingEntity entity) {
        double headY = entity.getY() + entity.getEyeHeight() + 0.02D;

        double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * 0.32D;
        double y = headY + (entity.getRandom().nextDouble() - 0.5D) * 0.20D;
        double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * 0.32D;

        double xSpeed = (entity.getRandom().nextDouble() - 0.5D) * 0.025D;
        double ySpeed = 0.015D + entity.getRandom().nextDouble() * 0.035D;
        double zSpeed = (entity.getRandom().nextDouble() - 0.5D) * 0.025D;

        level.sendParticles(ParticleTypes.SMOKE,
                x, y, z, 0,
                xSpeed, ySpeed, zSpeed, 1.0D
        );

        if (entity.getRandom().nextFloat() < 0.20F) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE,
                    x, y, z, 0,
                    xSpeed * 0.6D, ySpeed * 0.7D, zSpeed * 0.6D, 1.0D
            );
        }
    }

    private static Vec3 getRandomHeadDirection(LivingEntity entity) {
        double yaw = Math.toRadians(entity.getYRot());

        Vec3 forward = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
        Vec3 right = new Vec3(Math.cos(yaw), 0.0D, Math.sin(yaw));

        return switch (entity.getRandom().nextInt(5)) {
            case 0 -> forward;
            case 1 -> forward.scale(-1.0D);
            case 2 -> right;
            case 3 -> right.scale(-1.0D);
            default -> new Vec3(0.0D, 1.0D, 0.0D);
        };
    }

    private static Vec3 getHeadParticleOrigin(LivingEntity entity, Vec3 outward) {
        double headY = entity.getY() + entity.getEyeHeight();

        double verticalOffset = (entity.getRandom().nextDouble() - 0.5D) * HEAD_VERTICAL_SPREAD;

        return new Vec3(
                entity.getX() + outward.x * HEAD_SIDE_OFFSET,
                headY + verticalOffset + outward.y * 0.12D,
                entity.getZ() + outward.z * HEAD_SIDE_OFFSET
        );
    }
}
