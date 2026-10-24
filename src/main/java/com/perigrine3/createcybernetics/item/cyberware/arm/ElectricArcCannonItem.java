package com.perigrine3.createcybernetics.item.cyberware.arm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.client.model.AttachmentAnchor;
import com.perigrine3.createcybernetics.client.model.PlayerAttachmentManager;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.entity.ModEntities;
import com.perigrine3.createcybernetics.entity.custom.ArcLightningBoltEntity;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ElectricArcCannonItem extends Item implements ICyberwareItem {
    private static final int BLAST_ENERGY_COST = 180000;
    private static final int BLAST_COOLDOWN_TICKS = 100;
    private static final double BLAST_RANGE = 25.0D;
    private static final double MAX_AIM_ERROR_AT_ONE_BLOCK = 0.45D;
    private static final double AIM_ERROR_PER_BLOCK = 0.085D;

    private static final String LEGACY_COOLDOWN_LEFT = "cc_arc_cannon_left_cooldown_until";
    private static final String LEGACY_COOLDOWN_RIGHT = "cc_arc_cannon_right_cooldown_until";

    private final int humanityCost;

    public ElectricArcCannonItem(Properties props, int humanityCost) {
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
    public boolean isDyeable(ItemStack stack, CyberwareSlot slot) {
        return slot == CyberwareSlot.LARM || slot == CyberwareSlot.RARM;
    }

    @Override
    public boolean isDyeable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnergyUsedPerTick(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        return 10;
    }

    @Override
    public boolean requiresEnergyToFunction(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        return true;
    }

    @Override
    public int getEnergyActivationCost(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        return BLAST_ENERGY_COST;
    }

    @Override
    public boolean isToggleableByWheel(ItemStack installedStack, CyberwareSlot slot) {
        return true;
    }

    @Override
    public Set<CyberwareSlot> getSupportedSlots() {
        return Set.of(CyberwareSlot.LARM, CyberwareSlot.RARM);
    }

    @Override
    public boolean replacesOrgan() {
        return true;
    }

    @Override
    public Set<CyberwareSlot> getReplacedOrgans() {
        return Set.of(CyberwareSlot.LARM, CyberwareSlot.RARM);
    }

    @Override
    public TagKey<Item> getReplacedOrganItemTag(ItemStack installedStack, CyberwareSlot slot) {
        if (slot == CyberwareSlot.LARM) {
            return ModTags.Items.LEFTARM_REPLACEMENTS;
        }

        if (slot == CyberwareSlot.RARM) {
            return ModTags.Items.RIGHTARM_REPLACEMENTS;
        }

        return null;
    }

    @Override
    public Set<TagKey<Item>> incompatibleCyberwareTags(ItemStack installedStack, CyberwareSlot slot) {
        if (slot == CyberwareSlot.LARM) {
            return Set.of(ModTags.Items.LEFTARM_REPLACEMENTS);
        }

        if (slot == CyberwareSlot.RARM) {
            return Set.of(ModTags.Items.RIGHTARM_REPLACEMENTS);
        }

        return Set.of();
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class ServerHandler {
        private static final Map<UUID, Long> LEFT_COOLDOWNS = new HashMap<>();
        private static final Map<UUID, Long> RIGHT_COOLDOWNS = new HashMap<>();

        private ServerHandler() {}

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();
            if (player.level().isClientSide) return;

            PlayerCyberwareData data = getData(player);
            if (data == null) return;

            var profile = com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(player, data);
            if (profile != null && !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.ELECTRIC_ARC_CANNON)) {
                return;
            }

            removeLegacyCooldownNbt(player);
            cleanupExpiredCooldowns(player);

            if (isEnabledForHand(player, data, InteractionHand.MAIN_HAND)) {
                dropHand(player, InteractionHand.MAIN_HAND);
            }

            if (isEnabledForHand(player, data, InteractionHand.OFF_HAND)) {
                dropHand(player, InteractionHand.OFF_HAND);
            }

            if (player.isUsingItem()) {
                InteractionHand hand = player.getUsedItemHand();
                if (isEnabledForHand(player, data, hand)) {
                    player.stopUsingItem();
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            Player player = event.getEntity();
            PlayerCyberwareData data = getData(player);
            if (data == null) return;

            CyberwareSlot slot = chooseSlotForHandOrAny(player, data, event.getHand());
            if (slot == null) return;

            dropHand(player, handForSlot(player, slot));

            boolean fired = tryFire(player, data, slot);
            event.setCanceled(true);
            event.setCancellationResult(fired ? InteractionResult.SUCCESS : InteractionResult.FAIL);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            Player player = event.getEntity();
            PlayerCyberwareData data = getData(player);
            if (data == null) return;

            CyberwareSlot slot = chooseSlotForHandOrAny(player, data, event.getHand());
            if (slot == null) return;

            dropHand(player, handForSlot(player, slot));

            boolean fired = tryFire(player, data, slot);
            event.setCanceled(true);
            event.setCancellationResult(fired ? InteractionResult.SUCCESS : InteractionResult.FAIL);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
            Player player = event.getEntity();
            PlayerCyberwareData data = getData(player);
            if (data == null) return;

            CyberwareSlot slot = chooseSlotForHandOrAny(player, data, event.getHand());
            if (slot == null) return;

            dropHand(player, handForSlot(player, slot));

            boolean fired = tryFire(player, data, slot);
            event.setCanceled(true);
            event.setCancellationResult(fired ? InteractionResult.SUCCESS : InteractionResult.FAIL);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
            Player player = event.getEntity();
            PlayerCyberwareData data = getData(player);
            if (data == null) return;

            CyberwareSlot slot = chooseSlotForHandOrAny(player, data, event.getHand());
            if (slot == null) return;

            dropHand(player, handForSlot(player, slot));

            boolean fired = tryFire(player, data, slot);
            event.setCanceled(true);
            event.setCancellationResult(fired ? InteractionResult.SUCCESS : InteractionResult.FAIL);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onUseItemStart(LivingEntityUseItemEvent.Start event) {
            if (!(event.getEntity() instanceof Player player)) return;

            PlayerCyberwareData data = getData(player);
            if (data == null) return;

            InteractionHand hand = handHolding(player, event.getItem());
            if (hand == null) return;

            if (!isEnabledForHand(player, data, hand)) return;

            dropHand(player, hand);
            event.setCanceled(true);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onAttack(AttackEntityEvent event) {
            Player player = event.getEntity();
            PlayerCyberwareData data = getData(player);
            if (data == null) return;

            CyberwareSlot slot = chooseSlotForAnyAttack(player, data);
            if (slot == null) return;

            dropHand(player, handForSlot(player, slot));
            tryFire(player, data, slot);

            event.setCanceled(true);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
            Player player = event.getEntity();
            PlayerCyberwareData data = getData(player);
            if (data == null) return;

            CyberwareSlot slot = chooseSlotForAnyAttack(player, data);
            if (slot == null) return;

            dropHand(player, handForSlot(player, slot));
            tryFire(player, data, slot);

            event.setCanceled(true);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
            Player player = event.getEntity();
            PlayerCyberwareData data = getData(player);
            if (data == null) return;

            if (!isEnabledForHand(player, data, InteractionHand.MAIN_HAND)) return;

            event.setNewSpeed(0.0F);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onBreakBlock(BlockEvent.BreakEvent event) {
            Player player = event.getPlayer();
            if (player == null) return;

            PlayerCyberwareData data = getData(player);
            if (data == null) return;

            if (!isEnabledForHand(player, data, InteractionHand.MAIN_HAND)) return;

            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            UUID id = event.getEntity().getUUID();
            LEFT_COOLDOWNS.remove(id);
            RIGHT_COOLDOWNS.remove(id);
        }

        public static boolean isOnCooldown(Player player, CyberwareSlot slot) {
            long now = player.level().getGameTime();
            Long until = cooldownMap(slot).get(player.getUUID());

            if (until == null) {
                return false;
            }

            if (until <= now) {
                cooldownMap(slot).remove(player.getUUID());
                return false;
            }

            if (until - now > BLAST_COOLDOWN_TICKS) {
                cooldownMap(slot).remove(player.getUUID());
                return false;
            }

            return true;
        }

        private static void setCooldown(Player player, CyberwareSlot slot) {
            cooldownMap(slot).put(player.getUUID(), player.level().getGameTime() + BLAST_COOLDOWN_TICKS);
        }

        private static void cleanupExpiredCooldowns(Player player) {
            isOnCooldown(player, CyberwareSlot.LARM);
            isOnCooldown(player, CyberwareSlot.RARM);
        }

        private static Map<UUID, Long> cooldownMap(CyberwareSlot slot) {
            return slot == CyberwareSlot.LARM ? LEFT_COOLDOWNS : RIGHT_COOLDOWNS;
        }

        private static void removeLegacyCooldownNbt(Player player) {
            player.getPersistentData().remove(LEGACY_COOLDOWN_LEFT);
            player.getPersistentData().remove(LEGACY_COOLDOWN_RIGHT);
        }
    }

    public static boolean tryFire(Player player, PlayerCyberwareData data, CyberwareSlot slot) {
        if (player == null || data == null || slot == null) return false;
        if (!(player.level() instanceof ServerLevel level)) return false;
        if (!hasEnabledArcCannonInSlot(data, slot)) return false;
        if (ServerHandler.isOnCooldown(player, slot)) return false;

        LivingEntity target = findTarget(player, BLAST_RANGE);
        if (target == null) return false;

        if (!data.tryConsumeEnergy(BLAST_ENERGY_COST)) {
            return false;
        }

        ServerHandler.setCooldown(player, slot);
        spawnArcVisual(level, player, target, slot);
        playArcSounds(level, player, target);
        applyLightningHit(level, player, target);

        return true;
    }

    private static PlayerCyberwareData getData(Player player) {
        if (player == null) return null;
        if (!player.hasData(ModAttachments.CYBERWARE)) return null;
        return player.getData(ModAttachments.CYBERWARE);
    }

    private static boolean isEnabledForHand(Player player, PlayerCyberwareData data, InteractionHand hand) {
        return hasEnabledArcCannonInSlot(data, slotForHand(player, hand));
    }

    private static CyberwareSlot chooseSlotForHandOrAny(Player player, PlayerCyberwareData data, InteractionHand hand) {
        CyberwareSlot handSlot = slotForHand(player, hand);

        if (hasEnabledArcCannonInSlot(data, handSlot) && !ServerHandler.isOnCooldown(player, handSlot)) {
            return handSlot;
        }

        return chooseSlotForAnyAttack(player, data);
    }

    private static CyberwareSlot chooseSlotForAnyAttack(Player player, PlayerCyberwareData data) {
        CyberwareSlot mainSlot = slotForHand(player, InteractionHand.MAIN_HAND);
        CyberwareSlot offSlot = slotForHand(player, InteractionHand.OFF_HAND);

        if (hasEnabledArcCannonInSlot(data, mainSlot) && !ServerHandler.isOnCooldown(player, mainSlot)) {
            return mainSlot;
        }

        if (hasEnabledArcCannonInSlot(data, offSlot) && !ServerHandler.isOnCooldown(player, offSlot)) {
            return offSlot;
        }

        if (hasEnabledArcCannonInSlot(data, mainSlot)) {
            return mainSlot;
        }

        if (hasEnabledArcCannonInSlot(data, offSlot)) {
            return offSlot;
        }

        return null;
    }

    private static CyberwareSlot slotForHand(Player player, InteractionHand hand) {
        HumanoidArm arm = armForHand(player, hand);
        return arm == HumanoidArm.LEFT ? CyberwareSlot.LARM : CyberwareSlot.RARM;
    }

    private static InteractionHand handForSlot(Player player, CyberwareSlot slot) {
        CyberwareSlot mainSlot = slotForHand(player, InteractionHand.MAIN_HAND);
        return slot == mainSlot ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    private static HumanoidArm armForHand(Player player, InteractionHand hand) {
        HumanoidArm main = player.getMainArm();

        if (hand == InteractionHand.MAIN_HAND) {
            return main;
        }

        return main == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    private static boolean hasEnabledArcCannonInSlot(PlayerCyberwareData data, CyberwareSlot slot) {
        if (data == null || slot == null) return false;

        InstalledCyberware[] arr = data.getAll().get(slot);
        if (arr == null) return false;

        for (int i = 0; i < arr.length; i++) {
            InstalledCyberware installed = arr[i];
            if (installed == null) continue;

            ItemStack stack = installed.getItem();
            if (stack == null || stack.isEmpty()) continue;

            if (!(stack.getItem() instanceof ElectricArcCannonItem)) continue;
            if (!data.isEnabled(slot, i)) continue;

            return true;
        }

        return false;
    }

    private static void dropHand(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held == null || held.isEmpty()) return;

        ItemStack dropped = held.copy();
        player.setItemInHand(hand, ItemStack.EMPTY);
        player.drop(dropped, true);
        player.inventoryMenu.broadcastChanges();
    }

    private static InteractionHand handHolding(Player player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;

        if (ItemStack.isSameItemSameComponents(stack, player.getMainHandItem())) {
            return InteractionHand.MAIN_HAND;
        }

        if (ItemStack.isSameItemSameComponents(stack, player.getOffhandItem())) {
            return InteractionHand.OFF_HAND;
        }

        return null;
    }

    private static LivingEntity findTarget(Player player, double range) {
        Level level = player.level();

        Vec3 start = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F).normalize();

        AABB searchBox = player.getBoundingBox()
                .expandTowards(look.scale(range))
                .inflate(3.0D);

        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;

        List<LivingEntity> candidates = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != player && entity.isAlive() && !entity.isSpectator()
        );

        for (LivingEntity entity : candidates) {
            Vec3 target = entity.position().add(0.0D, entity.getBbHeight() * 0.55D, 0.0D);
            Vec3 toTarget = target.subtract(start);

            double forwardDistance = toTarget.dot(look);
            if (forwardDistance <= 0.0D || forwardDistance > range) {
                continue;
            }

            Vec3 closestPointOnAim = start.add(look.scale(forwardDistance));
            double aimError = target.distanceTo(closestPointOnAim);
            double allowedError = Math.max(
                    entity.getBbWidth() * 0.65D + MAX_AIM_ERROR_AT_ONE_BLOCK,
                    forwardDistance * AIM_ERROR_PER_BLOCK
            );

            if (aimError > allowedError) {
                continue;
            }

            if (!hasLineOfSight(player, start, target)) {
                continue;
            }

            double score = aimError * 16.0D + forwardDistance * 0.05D;

            if (score < bestScore) {
                bestScore = score;
                best = entity;
            }
        }

        return best;
    }

    private static boolean hasLineOfSight(Player player, Vec3 start, Vec3 target) {
        BlockHitResult hit = player.level().clip(new ClipContext(
                start,
                target,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        if (hit.getType() == HitResult.Type.MISS) {
            return true;
        }

        double blockDistance = start.distanceToSqr(hit.getLocation());
        double targetDistance = start.distanceToSqr(target);

        return blockDistance >= targetDistance - 0.25D;
    }

    public static boolean tryFireAnyEnabled(Player player) {
        if (player == null) return false;

        PlayerCyberwareData data = getData(player);
        if (data == null) return false;

        CyberwareSlot mainSlot = slotForHand(player, InteractionHand.MAIN_HAND);
        CyberwareSlot offSlot = slotForHand(player, InteractionHand.OFF_HAND);

        if (hasEnabledArcCannonInSlot(data, mainSlot) && !ServerHandler.isOnCooldown(player, mainSlot)) {
            return tryFire(player, data, mainSlot);
        }

        if (hasEnabledArcCannonInSlot(data, offSlot) && !ServerHandler.isOnCooldown(player, offSlot)) {
            return tryFire(player, data, offSlot);
        }

        if (hasEnabledArcCannonInSlot(data, mainSlot)) {
            return tryFire(player, data, mainSlot);
        }

        if (hasEnabledArcCannonInSlot(data, offSlot)) {
            return tryFire(player, data, offSlot);
        }

        return false;
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

    private static void playArcSounds(ServerLevel level, Player player, LivingEntity target) {
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.PLAYERS,
                1.8F,
                1.25F
        );

        level.playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.PLAYERS,
                2.0F,
                1.0F
        );
    }

    private static void spawnArcVisual(ServerLevel level, Player player, LivingEntity target, CyberwareSlot slot) {
        ArcLightningBoltEntity bolt = ModEntities.ARC_LIGHTNING_BOLT.get().create(level);
        if (bolt == null) return;

        Vec3 start = armMuzzlePosition(player, slot);
        Vec3 end = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);

        bolt.setArc(start, end);
        level.addFreshEntity(bolt);
    }

    private static Vec3 armMuzzlePosition(Player player, CyberwareSlot slot) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 right = look.cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();

        double side = slot == CyberwareSlot.RARM ? 1.0D : -1.0D;

        return eye
                .add(look.scale(0.65D))
                .add(right.scale(0.28D * side))
                .add(0.0D, -0.35D, 0.0D);
    }

    private static boolean hasArcCannonInSlot(PlayerCyberwareData data, CyberwareSlot slot) {
        if (data == null || slot == null) return false;

        InstalledCyberware[] arr = data.getAll().get(slot);
        if (arr == null) return false;

        for (InstalledCyberware cw : arr) {
            if (cw == null) continue;

            ItemStack stack = cw.getItem();
            if (stack == null || stack.isEmpty()) continue;

            if (stack.is(ModItems.ARMUPGRADES_ARCCANNON.get())) {
                return true;
            }
        }

        return false;
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static final class ClientFirstPerson {
        private ClientFirstPerson() {}

        @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
        public static void onRenderArm(RenderArmEvent event) {
            AbstractClientPlayer player = event.getPlayer();
            if (player == null) return;

            Minecraft mc = Minecraft.getInstance();
            Player viewer = mc.player;

            if (viewer != null) {
                if (player.isInvisibleTo(viewer)) return;
            } else {
                if (player.isInvisible()) return;
            }

            if (mc.player == null || player.getUUID() != mc.player.getUUID()) return;

            if (!player.hasData(ModAttachments.CYBERWARE)) return;
            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            if (data == null) return;

            HumanoidArm arm = event.getArm();
            CyberwareSlot slot = (arm == HumanoidArm.LEFT) ? CyberwareSlot.LARM : CyberwareSlot.RARM;

            if (!hasArcCannonInSlot(data, slot)) return;

            float armX = (arm == HumanoidArm.LEFT) ? 5.0F : -5.0F;
            float armY = 2.0F;
            float armZ = 0.0F;

            PoseStack pose = event.getPoseStack();
            MultiBufferSource buffers = event.getMultiBufferSource();
            int light = event.getPackedLight();

            var model = PlayerAttachmentManager.arcCannonProngsModel();
            var tex = PlayerAttachmentManager.ARC_CANNON_TEXTURE;

            pose.pushPose();
            try {
                pose.translate(armX / 15.5F, armY / 15.0F, armZ / 25.0F);

                if (arm == player.getMainArm()) {
                    pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(5.0F));
                } else {
                    pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-5.0F));
                }

                AttachmentAnchor anchor = (arm == HumanoidArm.LEFT)
                        ? AttachmentAnchor.LEFT_ARM
                        : AttachmentAnchor.RIGHT_ARM;

                PlayerAttachmentManager.applyArcCannonProngsTransform(pose, anchor);

                var vc = buffers.getBuffer(model.renderType(tex));
                model.renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            } finally {
                pose.popPose();
            }
        }
    }
}