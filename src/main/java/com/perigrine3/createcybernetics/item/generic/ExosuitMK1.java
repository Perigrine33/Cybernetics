package com.perigrine3.createcybernetics.item.generic;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.util.CyberwareAttributeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class ExosuitMK1 extends Item implements Equipable {

    private static final String PKEY_WORN = "cc_exosuit_worn";
    private static final String PKEY_WAS_GROUND = "cc_exosuit_was_ground";
    private static final String PKEY_JUMP_BOOSTED = "cc_exosuit_jump_boosted";
    private static final String PKEY_MOMENTUM = "cc_exosuit_momentum";

    private static final double MOMENTUM_BUILD_PER_TICK = 0.005D; // ~200 ticks to full
    private static final double MOMENTUM_DECAY_PER_TICK = 0.020D; // decay when not sprinting/invalid

    private static final double MAX_GROUND_FORWARD_SPEED = 1.50D;
    private static final double GROUND_ACCEL_PER_TICK = 0.065D;

    private static final double BASE_JUMP_VELOCITY = 0.42D;
    private static final double MAX_JUMP_VELOCITY = 1.30D;

    private static final double BASE_FORWARD_LAUNCH = 0.45D;
    private static final double MAX_FORWARD_LAUNCH = 1.65D;

    public ExosuitMK1(Properties props) {
        super(props);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.CHEST;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return this.swapWithEquipmentSlot(this, level, player, hand);
    }

    public void onEquipped(Player player, ItemStack stack) {
        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        if (hasProcessor(data)) {
            CyberwareAttributeHelper.applyModifier(player, "exosuit_strength");
            CyberwareAttributeHelper.applyModifier(player, "exosuit_knockback");
            CyberwareAttributeHelper.applyModifier(player, "exosuit_movementspeed");
            CyberwareAttributeHelper.applyModifier(player, "exosuit_attackspeed");
            CyberwareAttributeHelper.applyModifier(player, "exosuit_miningspeed");
            CyberwareAttributeHelper.applyModifier(player, "exosuit_jumpheight");
        } else {
            clearRampState(player);
            CyberwareAttributeHelper.applyModifier(player, "exosuit_no_strength");
            CyberwareAttributeHelper.applyModifier(player, "exosuit_no_knockback");
            CyberwareAttributeHelper.applyModifier(player, "exosuit_no_movementspeed");
            CyberwareAttributeHelper.applyModifier(player, "exosuit_no_attackspeed");
            CyberwareAttributeHelper.applyModifier(player, "exosuit_no_miningspeed");
            CyberwareAttributeHelper.applyModifier(player, "exosuit_no_jumpheight");
        }

        player.getPersistentData().putBoolean(PKEY_WAS_GROUND, player.onGround());
        player.getPersistentData().putBoolean(PKEY_JUMP_BOOSTED, false);
        if (!player.getPersistentData().contains(PKEY_MOMENTUM)) {
            player.getPersistentData().putDouble(PKEY_MOMENTUM, 0.0D);
        }
    }

    public void onRemoved(Player player) {
        CyberwareAttributeHelper.removeModifier(player, "exosuit_strength");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_knockback");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_movementspeed");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_attackspeed");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_miningspeed");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_jumpheight");

        CyberwareAttributeHelper.removeModifier(player, "exosuit_no_strength");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_no_knockback");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_no_movementspeed");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_no_attackspeed");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_no_miningspeed");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_no_jumpheight");

        clearRampState(player);
    }

    public void onWornTick(Player player, ItemStack stack) {
        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) {
            clearRampState(player);
            return;
        }

        if (!hasProcessor(data)) {
            CyberwareAttributeHelper.removeModifier(player, "exosuit_strength");
            CyberwareAttributeHelper.removeModifier(player, "exosuit_knockback");
            CyberwareAttributeHelper.removeModifier(player, "exosuit_movementspeed");
            CyberwareAttributeHelper.removeModifier(player, "exosuit_attackspeed");
            CyberwareAttributeHelper.removeModifier(player, "exosuit_miningspeed");
            CyberwareAttributeHelper.removeModifier(player, "exosuit_jumpheight");

            CyberwareAttributeHelper.applyModifier(player, "exosuit_no_strength");
            CyberwareAttributeHelper.applyModifier(player, "exosuit_no_knockback");
            CyberwareAttributeHelper.applyModifier(player, "exosuit_no_movementspeed");
            CyberwareAttributeHelper.applyModifier(player, "exosuit_no_attackspeed");
            CyberwareAttributeHelper.applyModifier(player, "exosuit_no_miningspeed");
            CyberwareAttributeHelper.applyModifier(player, "exosuit_no_jumpheight");

            clearRampState(player);
            return;
        }

        CyberwareAttributeHelper.removeModifier(player, "exosuit_no_strength");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_no_knockback");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_no_movementspeed");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_no_attackspeed");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_no_miningspeed");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_no_jumpheight");

        CyberwareAttributeHelper.applyModifier(player, "exosuit_strength");
        CyberwareAttributeHelper.applyModifier(player, "exosuit_knockback");
        CyberwareAttributeHelper.applyModifier(player, "exosuit_movementspeed");
        CyberwareAttributeHelper.applyModifier(player, "exosuit_attackspeed");
        CyberwareAttributeHelper.applyModifier(player, "exosuit_miningspeed");
        CyberwareAttributeHelper.applyModifier(player, "exosuit_jumpheight");

        boolean wasGround = player.getPersistentData().getBoolean(PKEY_WAS_GROUND);
        boolean onGround = player.onGround();

        if (onGround) {
            player.getPersistentData().putBoolean(PKEY_JUMP_BOOSTED, false);
        }

        double momentum = player.getPersistentData().getDouble(PKEY_MOMENTUM);
        momentum = updateMomentum(player, momentum);
        player.getPersistentData().putDouble(PKEY_MOMENTUM, momentum);

        if (momentum <= 0.0D) {
            player.getPersistentData().putBoolean(PKEY_WAS_GROUND, onGround);
            return;
        }

        if (onGround) {
            applyGroundRamp(player, momentum);
        }

        if (wasGround && !onGround) {
            boolean boosted = player.getPersistentData().getBoolean(PKEY_JUMP_BOOSTED);
            if (!boosted) {
                applyJumpLaunch(player, momentum);
                player.getPersistentData().putBoolean(PKEY_JUMP_BOOSTED, true);
            }
        }

        player.getPersistentData().putBoolean(PKEY_WAS_GROUND, onGround);
    }

    private static boolean hasProcessor(PlayerCyberwareData data) {
        return data.hasSpecificItem(ModItems.BRAINUPGRADES_NEURALPROCESSOR.get(), CyberwareSlot.BRAIN);
    }

    private static double updateMomentum(Player player, double momentum) {
        if (canBuildMomentum(player)) {
            momentum += MOMENTUM_BUILD_PER_TICK;

            // Let actual speed help catch momentum up so jump strength tracks feel.
            double actualSpeedFactor = Mth.clamp(horizontalSpeed(player.getDeltaMovement()) / MAX_GROUND_FORWARD_SPEED, 0.0D, 1.0D);
            momentum = Math.max(momentum, actualSpeedFactor);
        } else {
            momentum -= MOMENTUM_DECAY_PER_TICK;
        }

        return Mth.clamp(momentum, 0.0D, 1.0D);
    }

    private static boolean canBuildMomentum(Player player) {
        if (!player.isSprinting()) return false;
        if (player.isPassenger()) return false;
        if (player.isInWaterOrBubble() || player.isInLava()) return false;
        if (player.isFallFlying()) return false;

        // Only build while the player is actually trying to move forward.
        return player.zza > 0.0F;
    }

    private static void applyGroundRamp(Player player, double momentum) {
        Vec3 v = player.getDeltaMovement();
        Vec3 forward = horizontalLookDir(player);
        if (forward.lengthSqr() < 1.0E-8D) return;

        double currentForward = v.x * forward.x + v.z * forward.z;
        double targetForward = MAX_GROUND_FORWARD_SPEED * momentum;

        if (currentForward >= targetForward) return;

        double add = Math.min(GROUND_ACCEL_PER_TICK, targetForward - currentForward);

        player.setDeltaMovement(
                v.x + forward.x * add,
                v.y,
                v.z + forward.z * add
        );
        player.hurtMarked = true;
    }

    private static void applyJumpLaunch(Player player, double momentum) {
        Vec3 v = player.getDeltaMovement();
        if (v.y <= 0.0D) return;

        Vec3 forward = horizontalLookDir(player);
        if (forward.lengthSqr() < 1.0E-8D) return;

        double currentForward = v.x * forward.x + v.z * forward.z;
        double sideX = v.x - (forward.x * currentForward);
        double sideZ = v.z - (forward.z * currentForward);

        double targetY = Mth.lerp(momentum, BASE_JUMP_VELOCITY, MAX_JUMP_VELOCITY);
        double targetForward = Mth.lerp(momentum, BASE_FORWARD_LAUNCH, MAX_FORWARD_LAUNCH);

        double newForward = Math.max(currentForward, targetForward);
        double newY = Math.max(v.y, targetY);

        player.setDeltaMovement(
                sideX + forward.x * newForward,
                newY,
                sideZ + forward.z * newForward
        );
        player.hurtMarked = true;
    }

    private static Vec3 horizontalLookDir(Player player) {
        Vec3 look = player.getLookAngle();
        double len2 = look.x * look.x + look.z * look.z;
        if (len2 <= 1.0E-8D) return Vec3.ZERO;
        double invLen = 1.0D / Math.sqrt(len2);
        return new Vec3(look.x * invLen, 0.0D, look.z * invLen);
    }

    private static double horizontalSpeed(Vec3 v) {
        return Math.sqrt(v.x * v.x + v.z * v.z);
    }

    private static void clearRampState(Player player) {
        player.getPersistentData().remove(PKEY_WAS_GROUND);
        player.getPersistentData().remove(PKEY_JUMP_BOOSTED);
        player.getPersistentData().remove(PKEY_MOMENTUM);
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class HookServer {
        @SubscribeEvent
        public static void onEquipmentChange(net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent event) {
            if (event.getSlot() != EquipmentSlot.CHEST) return;
            if (!(event.getEntity() instanceof Player player)) return;
            if (player.level().isClientSide()) return;

            ItemStack to = event.getTo();
            if (to.getItem() instanceof ExosuitMK1 exosuit) {
                player.getPersistentData().putBoolean(PKEY_WORN, true);
                exosuit.onEquipped(player, to);
            } else if (event.getFrom().getItem() instanceof ExosuitMK1 || player.getPersistentData().getBoolean(PKEY_WORN)) {
                player.getPersistentData().remove(PKEY_WORN);
                onRemovedAny(player);
            }
        }

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();
            if (player.level().isClientSide()) return;
            tickCommon(player);
        }
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static final class HookClient {
        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();
            if (!player.level().isClientSide()) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player != player) return;

            tickCommon(player);
        }
    }

    private static void tickCommon(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean wearingNow = chest.getItem() instanceof ExosuitMK1;
        if (!wearingNow) {
            if (player.getPersistentData().getBoolean(PKEY_WORN)) {
                player.getPersistentData().remove(PKEY_WORN);
                onRemovedAny(player);
            }
            return;
        }

        boolean woreLast = player.getPersistentData().getBoolean(PKEY_WORN);
        if (!woreLast) {
            player.getPersistentData().putBoolean(PKEY_WORN, true);
            ((ExosuitMK1) chest.getItem()).onEquipped(player, chest);
        }

        ((ExosuitMK1) chest.getItem()).onWornTick(player, chest);
    }

    private static void onRemovedAny(Player player) {
        CyberwareAttributeHelper.removeModifier(player, "exosuit_strength");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_knockback");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_movementspeed");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_attackspeed");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_miningspeed");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_jumpheight");

        CyberwareAttributeHelper.removeModifier(player, "exosuit_no_strength");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_no_knockback");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_no_movementspeed");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_no_attackspeed");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_no_miningspeed");
        CyberwareAttributeHelper.removeModifier(player, "exosuit_no_jumpheight");

        clearRampState(player);
    }
}