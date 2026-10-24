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
import com.perigrine3.createcybernetics.sound.ModSounds;
import com.perigrine3.createcybernetics.util.CyberwareAttributeHelper;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RetractableClawsItem extends Item implements ICyberwareItem {

    private final int humanityCost;

    public RetractableClawsItem(Properties props, int humanityCost) {
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
    public int getHumanityCost() {
        return humanityCost;
    }

    @Override
    public Set<CyberwareSlot> getSupportedSlots() {
        return Set.of(CyberwareSlot.LARM, CyberwareSlot.RARM);
    }

    @Override
    public Set<TagKey<Item>> requiresCyberwareTags(ItemStack installedStack, CyberwareSlot slot) {
        return switch (slot) {
            case RARM -> Set.of(ModTags.Items.RIGHTARM_REPLACEMENTS);
            case LARM -> Set.of(ModTags.Items.LEFTARM_REPLACEMENTS);
            default -> Set.of();
        };
    }

    @Override
    public boolean replacesOrgan() {
        return false;
    }

    @Override
    public Set<CyberwareSlot> getReplacedOrgans() {
        return Set.of();
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class ServerHandler {

        private static final Map<UUID, Boolean> LAST_LEFT = new HashMap<>();
        private static final Map<UUID, Boolean> LAST_RIGHT = new HashMap<>();

        @SubscribeEvent
        public static void onCyberwareStateChange(com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent event) {
            syncClaws(event.getPlayer(), event.getData(), event.getCurrentProfile());
        }

        @SubscribeEvent
        public static void onEquipmentChange(net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent event) {
            if (!(event.getEntity() instanceof Player player)) return;
            if (player.level().isClientSide) return;
            if (event.getSlot() != EquipmentSlot.MAINHAND && event.getSlot() != EquipmentSlot.OFFHAND) return;
            if (!player.hasData(ModAttachments.CYBERWARE)) return;
            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            var profile = com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(player, data);
            if (profile == null || !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.RETRACTABLE_CLAWS)) {
                return;
            }
            syncClaws(player, data, profile);
        }

        private static void syncClaws(
                Player player,
                PlayerCyberwareData data,
                com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile profile
        ) {
            if (player == null || player.level().isClientSide) return;

            UUID id = player.getUUID();
            if (data == null || (profile != null && !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.RETRACTABLE_CLAWS))) {
                LAST_LEFT.remove(id);
                LAST_RIGHT.remove(id);
                CyberwareAttributeHelper.removeModifier(player, "claws_attack1");
                CyberwareAttributeHelper.removeModifier(player, "claws_attack2");
                return;
            }

            boolean leftEnabled = isLeftEnabled(data);
            boolean rightEnabled = isRightEnabled(data);

            boolean prevLeft = LAST_LEFT.getOrDefault(id, false);
            if (leftEnabled != prevLeft) {
                LAST_LEFT.put(id, leftEnabled);
                playToggleSound(player, leftEnabled);
            }

            boolean prevRight = LAST_RIGHT.getOrDefault(id, false);
            if (rightEnabled != prevRight) {
                LAST_RIGHT.put(id, rightEnabled);
                playToggleSound(player, rightEnabled);
            }

            boolean anyEnabled = leftEnabled || rightEnabled;
            boolean weaponEquipped = isHoldingWeapon(player);

            if (anyEnabled && !weaponEquipped) {
                if (rightEnabled && leftEnabled) {
                    CyberwareAttributeHelper.applyModifier(player, "claws_attack1");
                    CyberwareAttributeHelper.applyModifier(player, "claws_attack2");
                } else {
                    CyberwareAttributeHelper.applyModifier(player, "claws_attack1");
                    CyberwareAttributeHelper.removeModifier(player, "claws_attack2");
                }
            } else {
                CyberwareAttributeHelper.removeModifier(player, "claws_attack1");
                CyberwareAttributeHelper.removeModifier(player, "claws_attack2");
            }
        }

        private static boolean isHoldingWeapon(Player player) {
            return isWeaponLike(player.getMainHandItem()) || isWeaponLike(player.getOffhandItem());
        }

        private static boolean isWeaponLike(ItemStack stack) {
            if (stack == null || stack.isEmpty()) return false;

            Item it = stack.getItem();
            if (it instanceof BowItem || it instanceof CrossbowItem || it instanceof TridentItem) return true;
            if (it instanceof SwordItem || it instanceof AxeItem || it instanceof MaceItem || it instanceof DiggerItem) return true;

            ItemAttributeModifiers mods = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
            if (mods == null) return false;

            for (ItemAttributeModifiers.Entry e : mods.modifiers()) {
                Holder<Attribute> attr = e.attribute();

                boolean attackDamage = attr != null && attr.value() == Attributes.ATTACK_DAMAGE;
                boolean mainhand = (e.slot() == EquipmentSlotGroup.MAINHAND) || e.slot().test(EquipmentSlot.MAINHAND);

                if (attackDamage && mainhand && e.modifier().amount() != 0.0) {
                    return true;
                }
            }

            return false;
        }

        private static void playToggleSound(Player player, boolean enabledNow) {
            SoundEvent snd = clawsToggleSound();
            if (snd == null) return;

            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    snd,
                    SoundSource.PLAYERS,
                    0.9F,
                    enabledNow ? 1.15F : 0.95F
            );
        }

        private static SoundEvent clawsToggleSound() {
            return ModSounds.RETRACTABLE_CLAWS_SNIKT.get();
        }

        @SubscribeEvent
        public static void onLogout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
            UUID id = event.getEntity().getUUID();
            LAST_LEFT.remove(id);
            LAST_RIGHT.remove(id);
        }

        private static boolean isLeftEnabled(PlayerCyberwareData data) {
            return hasEnabledInSlot(data, CyberwareSlot.LARM);
        }

        private static boolean isRightEnabled(PlayerCyberwareData data) {
            return hasEnabledInSlot(data, CyberwareSlot.RARM);
        }

        private static boolean hasEnabledInSlot(PlayerCyberwareData data, CyberwareSlot slot) {
            InstalledCyberware[] arr = data.getAll().get(slot);
            if (arr == null) return false;

            for (int i = 0; i < arr.length; i++) {
                InstalledCyberware cw = arr[i];
                if (cw == null) continue;

                ItemStack st = cw.getItem();
                if (st == null || st.isEmpty()) continue;

                if (!(st.getItem() instanceof RetractableClawsItem)) continue;

                if (data.isEnabled(slot, i)) return true;
            }

            return false;
        }

        private ServerHandler() {}
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

            if (mc.player == null || player.getUUID() != mc.player.getUUID()) return;

            if (!player.hasData(ModAttachments.CYBERWARE)) return;
            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            if (data == null) return;

            HumanoidArm arm = event.getArm();
            CyberwareSlot slot = (arm == HumanoidArm.LEFT) ? CyberwareSlot.LARM : CyberwareSlot.RARM;
            if (!hasEnabledClawsInSlot(data, slot)) return;

            float armX = (arm == HumanoidArm.LEFT) ? 5.0F : -5.0F;
            float armY = 2.0F;
            float armZ = 0.0F;

            PoseStack pose = event.getPoseStack();
            MultiBufferSource buffers = event.getMultiBufferSource();
            int light = event.getPackedLight();

            var model = PlayerAttachmentManager.clawsModel();
            var tex = PlayerAttachmentManager.CLAWS_TEXTURE;

            pose.pushPose();
            try {
                pose.translate(armX / 16.0F, armY / 16.0F, armZ / 16.0F);

                pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(0.0F));
                pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(0.0F));
                pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(5F));

                AttachmentAnchor anchor = (arm == HumanoidArm.LEFT)
                        ? AttachmentAnchor.LEFT_ARM
                        : AttachmentAnchor.RIGHT_ARM;

                PlayerAttachmentManager.applyKnuckleClawTransform(pose, anchor);

                var vc = buffers.getBuffer(model.renderType(tex));
                model.renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            } finally {
                pose.popPose();
            }
        }

        private ClientFirstPerson() {}
    }

    private static boolean hasEnabledClawsInSlot(PlayerCyberwareData data, CyberwareSlot slot) {
        InstalledCyberware[] arr = data.getAll().get(slot);
        if (arr == null) return false;

        for (int i = 0; i < arr.length; i++) {
            InstalledCyberware cw = arr[i];
            if (cw == null) continue;

            ItemStack st = cw.getItem();
            if (st == null || st.isEmpty()) continue;

            if (!(st.getItem() instanceof RetractableClawsItem)) continue;

            if (data.isEnabled(slot, i)) return true;
        }

        return false;
    }
}