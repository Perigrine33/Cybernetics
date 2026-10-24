package com.perigrine3.createcybernetics.entity.ai.goal;

import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.EntityCyberwareData;
import com.perigrine3.createcybernetics.common.capabilities.ModMobAttachments;
import com.perigrine3.createcybernetics.effect.ModEffects;
import com.perigrine3.createcybernetics.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class CyberentitySandevistanGoal extends Goal {

    public static final String NBT_SANDY_ACTIVE_UNTIL = "cc_sandy_active_until";
    public static final String NBT_SANDY_COOLDOWN_UNTIL = "cc_sandy_cooldown_until";

    private static final int ACTIVE_TICKS = 80;
    private static final int COOLDOWN_TICKS = 100;

    private static final int MELEE_ATTACK_COOLDOWN_TICKS = 10;

    private static final double MIN_ACTIVATION_DISTANCE_SQ = 9.0D;
    private static final double MAX_ACTIVATION_DISTANCE_SQ = 256.0D;

    private static final double ACTIVE_MOVE_SPEED = 1.6D;
    private static final double NORMAL_MOVE_SPEED = 1.1D;

    private final Mob mob;
    private int meleeAttackCooldown;

    public CyberentitySandevistanGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!mob.isAlive()) return false;
        if (mob.level().isClientSide) return false;

        LivingEntity target = mob.getTarget();
        if (!isValidTarget(target)) return false;
        if (!hasInstalledSandevistan()) return false;
        if (isSandevistanActive()) return false;
        if (isOnCooldown()) return false;
        if (mob.isPassenger()) return false;

        double distSq = mob.distanceToSqr(target);
        if (distSq < MIN_ACTIVATION_DISTANCE_SQ) return false;
        if (distSq > MAX_ACTIVATION_DISTANCE_SQ) return false;

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!mob.isAlive()) return false;
        if (mob.level().isClientSide) return false;
        return isSandevistanActive();
    }

    @Override
    public void start() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        long now = mob.level().getGameTime();
        CompoundTag tag = mob.getPersistentData();

        tag.putLong(NBT_SANDY_ACTIVE_UNTIL, now + ACTIVE_TICKS);
        tag.putLong(NBT_SANDY_COOLDOWN_UNTIL, now + ACTIVE_TICKS + COOLDOWN_TICKS);

        meleeAttackCooldown = 0;

        mob.addEffect(new MobEffectInstance(
                ModEffects.SANDEVISTAN_EFFECT,
                ACTIVE_TICKS,
                0,
                false,
                false,
                true
        ));

        mob.getNavigation().moveTo(target, ACTIVE_MOVE_SPEED);
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        mob.setSprinting(true);
        if (mob instanceof com.perigrine3.createcybernetics.api.ISandevistanTrailState trailState) {
            trailState.createcybernetics$setSandevistanTrailActive(true);
        }
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        boolean active = isSandevistanActive();

        if (meleeAttackCooldown > 0) {
            meleeAttackCooldown--;
        }

        if (!active) {
            mob.setSprinting(false);

            if (isValidTarget(target)) {
                mob.getNavigation().moveTo(target, NORMAL_MOVE_SPEED);
            }

            return;
        }

        if (isValidTarget(target)) {
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            mob.getNavigation().moveTo(target, ACTIVE_MOVE_SPEED);
            tryMeleeAttack(target);
        }

        mob.setSprinting(true);
    }

    @Override
    public void stop() {
        mob.setSprinting(false);
        meleeAttackCooldown = 0;
        if (mob instanceof com.perigrine3.createcybernetics.api.ISandevistanTrailState trailState) {
            trailState.createcybernetics$setSandevistanTrailActive(false);
        }

        LivingEntity target = mob.getTarget();
        if (isValidTarget(target)) {
            mob.getNavigation().moveTo(target, NORMAL_MOVE_SPEED);
        }
    }

    private void tryMeleeAttack(LivingEntity target) {
        if (meleeAttackCooldown > 0) return;
        if (!mob.hasLineOfSight(target)) return;
        if (!isCloseEnoughToBump(target)) return;

        mob.swing(mob.getUsedItemHand());
        mob.doHurtTarget(target);

        meleeAttackCooldown = MELEE_ATTACK_COOLDOWN_TICKS;
    }

    private boolean isCloseEnoughToBump(LivingEntity target) {
        double reach = mob.getBbWidth() + target.getBbWidth() + 0.35D;
        return mob.distanceToSqr(target) <= reach * reach;
    }

    private boolean hasInstalledSandevistan() {
        if (!mob.hasData(ModMobAttachments.CYBERENTITY_CYBERWARE)) return false;

        EntityCyberwareData data = mob.getData(ModMobAttachments.CYBERENTITY_CYBERWARE);
        if (data == null) return false;

        InstalledCyberware[] arr = data.getAll().get(CyberwareSlot.BONE);
        if (arr == null) return false;

        for (int i = 0; i < arr.length; i++) {
            InstalledCyberware installed = arr[i];
            if (installed == null) continue;
            if (installed.getItem() == null || installed.getItem().isEmpty()) continue;
            if (!installed.getItem().is(ModItems.BONEUPGRADES_SANDEVISTAN.get())) continue;
            if (!data.isEnabled(CyberwareSlot.BONE, i)) continue;
            return true;
        }

        return false;
    }

    private boolean isSandevistanActive() {
        return mob.getPersistentData().getLong(NBT_SANDY_ACTIVE_UNTIL) > mob.level().getGameTime();
    }

    private boolean isOnCooldown() {
        return mob.getPersistentData().getLong(NBT_SANDY_COOLDOWN_UNTIL) > mob.level().getGameTime();
    }

    private static boolean isValidTarget(LivingEntity target) {
        return target != null && target.isAlive();
    }
}