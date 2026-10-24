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
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DrillFistItem extends Item implements ICyberwareItem {
    private final int humanityCost;

    private static final float DIAMOND_PICK_SPEED = 8.0F;

    public DrillFistItem(Properties props, int humanityCost) {
        super(props);
        this.humanityCost = humanityCost;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.createcybernetics.humanity", humanityCost)
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public Map<Item, Integer> getAdditionalAnvilRepairMaterials(ItemStack cyberwareStack) {
        return Map.of(
                Items.DIAMOND, 350,
                Items.IRON_INGOT, 150,
                ModItems.TITANIUMINGOT.get(), 250
        );
    }


    @Override
    public int getHumanityCost() {
        return humanityCost;
    }

    @Override
    public Set<TagKey<Item>> requiresCyberwareTags(ItemStack installedStack, CyberwareSlot slot) {
        return switch (slot) {
            case RARM -> Set.of(ModTags.Items.RIGHT_CYBERARM);
            case LARM -> Set.of(ModTags.Items.LEFT_CYBERARM);
            default -> Set.of();
        };
    }

    @Override
    public Set<CyberwareSlot> getSupportedSlots() {
        return Set.of(CyberwareSlot.RARM, CyberwareSlot.LARM);
    }

    @Override
    public boolean replacesOrgan() {
        return false;
    }

    @Override
    public Set<CyberwareSlot> getReplacedOrgans() {
        return Set.of();
    }

    @Override
    public void onInstalled(LivingEntity entity) {
    }

    @Override
    public void onRemoved(LivingEntity entity) {
    }

    private static boolean hasDrillInstalled(Player player, CyberwareSlot slot) {
        if (!player.hasData(ModAttachments.CYBERWARE)) return false;

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return false;

        var profile = com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(player, data);
        if (profile != null && !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.DRILL_FIST)) {
            return false;
        }

        return data.hasSpecificItem(ModItems.ARMUPGRADES_DRILLFIST.get(), slot);
    }

    private static boolean hasAnyDrillInstalled(Player player) {
        return hasDrillInstalled(player, CyberwareSlot.RARM) || hasDrillInstalled(player, CyberwareSlot.LARM);
    }

    private static InteractionHand handForSlot(Player player, CyberwareSlot slot) {
        HumanoidArm main = player.getMainArm();
        return switch (slot) {
            case RARM -> (main == HumanoidArm.RIGHT) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            case LARM -> (main == HumanoidArm.RIGHT) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            default -> InteractionHand.MAIN_HAND;
        };
    }

    private static InteractionHand pickDrillSwingHandDeterministic(Player player) {
        boolean right = hasDrillInstalled(player, CyberwareSlot.RARM);
        boolean left = hasDrillInstalled(player, CyberwareSlot.LARM);

        if (right && !left) return handForSlot(player, CyberwareSlot.RARM);
        if (left && !right) return handForSlot(player, CyberwareSlot.LARM);

        return (player.getMainArm() == HumanoidArm.RIGHT)
                ? handForSlot(player, CyberwareSlot.RARM)
                : handForSlot(player, CyberwareSlot.LARM);
    }

    private static boolean drillBlocksMainHand(Player player) {
        HumanoidArm mainArm = player.getMainArm();
        boolean rightInstalled = hasDrillInstalled(player, CyberwareSlot.RARM);
        boolean leftInstalled = hasDrillInstalled(player, CyberwareSlot.LARM);

        return (mainArm == HumanoidArm.RIGHT) ? rightInstalled : leftInstalled;
    }

    private static boolean drillBlocksOffhand(Player player) {
        HumanoidArm mainArm = player.getMainArm();
        boolean rightInstalled = hasDrillInstalled(player, CyberwareSlot.RARM);
        boolean leftInstalled = hasDrillInstalled(player, CyberwareSlot.LARM);

        return (mainArm == HumanoidArm.RIGHT) ? leftInstalled : rightInstalled;
    }

    private static boolean drillBlocksHand(Player player, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? drillBlocksMainHand(player) : drillBlocksOffhand(player);
    }

    private static void dropAndClearHand(ServerSideDropper dropper, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return;

        ItemStack toDrop = stack.copy();
        player.setItemInHand(hand, ItemStack.EMPTY);
        dropper.drop(player, toDrop);
    }

    @FunctionalInterface
    private interface ServerSideDropper {
        void drop(Player player, ItemStack stack);
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class DrillHooks {
        private DrillHooks() {}

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();
            if (player.level().isClientSide()) return;

            boolean blocksMain = drillBlocksMainHand(player);
            boolean blocksOff = drillBlocksOffhand(player);
            if (!blocksMain && !blocksOff) return;

            ServerSideDropper dropper = (p, stack) -> p.drop(stack, true);

            if (blocksMain) dropAndClearHand(dropper, player, InteractionHand.MAIN_HAND);
            if (blocksOff) dropAndClearHand(dropper, player, InteractionHand.OFF_HAND);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
            Player player = event.getEntity();
            if (player.level().isClientSide()) return;
            if (!hasAnyDrillInstalled(player)) return;

            InteractionHand drillHand = pickDrillSwingHandDeterministic(player);

            if (player instanceof ServerPlayer sp) {
                sp.swing(drillHand, true);
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            Player player = event.getEntity();
            Level level = player.level();
            if (level.isClientSide()) return;

            if (!drillBlocksHand(player, event.getHand())) return;

            BlockState state = level.getBlockState(event.getPos());
            boolean isUiBlock = state.getMenuProvider(level, event.getPos()) != null;
            if (!isUiBlock) return;

            if (player.getRandom().nextBoolean()) return;

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }

        @SubscribeEvent
        public static void onHarvestCheck(PlayerEvent.HarvestCheck event) {
            if (!hasAnyDrillInstalled(event.getEntity())) return;
            event.setCanHarvest(true);
        }

        @SubscribeEvent
        public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
            Player player = event.getEntity();
            if (!hasAnyDrillInstalled(player)) return;

            float original = event.getOriginalSpeed();
            event.setNewSpeed(Math.max(original, DIAMOND_PICK_SPEED));
        }
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static final class DrillClientHooks {
        private DrillClientHooks() {}

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
            if (!event.isAttack()) return;
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) return;
            if (!hasAnyDrillInstalled(player)) return;

            HitResult hit = mc.hitResult;
            if (!(hit instanceof BlockHitResult)) return;

            InteractionHand drillHand = pickDrillSwingHandDeterministic(player);

            event.setSwingHand(false);
            player.swing(drillHand, true);
        }
    }

    private static CyberwareSlot slotForArm(HumanoidArm arm) {
        return (arm == HumanoidArm.LEFT) ? CyberwareSlot.LARM : CyberwareSlot.RARM;
    }

    private static HumanoidArm offArm(HumanoidArm main) {
        return (main == HumanoidArm.LEFT) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static final class ClientFirstPerson {

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

            if (!player.hasData(ModAttachments.CYBERWARE)) return;
            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            if (data == null) return;

            HumanoidArm arm = event.getArm();
            CyberwareSlot slot = (arm == HumanoidArm.LEFT) ? CyberwareSlot.LARM : CyberwareSlot.RARM;

            if (!hasDrillInSlot(data, slot)) return;

            var renderer = mc.getEntityRenderDispatcher().getRenderer(player);
            if (!(renderer instanceof PlayerRenderer pr)) return;

            PlayerModel<?> pm = pr.getModel();
            var armPart = (arm == HumanoidArm.LEFT) ? pm.leftArm : pm.rightArm;

            PoseStack pose = event.getPoseStack();
            MultiBufferSource buffers = event.getMultiBufferSource();
            int light = event.getPackedLight();

            var model = PlayerAttachmentManager.drillModel();
            var tex = PlayerAttachmentManager.DRILL_FIST_TEXTURE;

            pose.pushPose();
            try {
                if (arm == player.getMainArm()) {
                    pose.translate(armPart.x / 20.0F, armPart.y / 32.0F, armPart.z / 16.0F);
                    pose.scale(1.15F, 1.15F, 1.15F);

                    pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(0.0F));
                    pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(0.0F));
                    pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(5F));
                } else {
                    pose.translate(armPart.x / 20.0F, armPart.y / 32.0F, armPart.z / 16.0F);
                    pose.scale(1.15F, 1.15F, 1.15F);

                    pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(0.0F));
                    pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(0.0F));
                    pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-5F));
                }

                AttachmentAnchor anchor = (arm == HumanoidArm.LEFT)
                        ? AttachmentAnchor.LEFT_ARM
                        : AttachmentAnchor.RIGHT_ARM;

                PlayerAttachmentManager.applyDrillFistTransform(pose, anchor);

                var vc = buffers.getBuffer(model.renderType(tex));
                model.renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            } finally {
                pose.popPose();
            }
        }

        private static boolean hasDrillInSlot(PlayerCyberwareData data, CyberwareSlot slot) {
            InstalledCyberware[] arr = data.getAll().get(slot);
            if (arr == null) return false;

            for (int i = 0; i < arr.length; i++) {
                InstalledCyberware cw = arr[i];
                if (cw == null) continue;

                ItemStack st = cw.getItem();
                if (st == null || st.isEmpty()) continue;

                if (st.is(ModItems.ARMUPGRADES_DRILLFIST.get())) return true;
            }

            return false;
        }
    }
}