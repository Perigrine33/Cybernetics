package com.perigrine3.createcybernetics.item.cyberware.bone;

import com.perigrine3.createcybernetics.compat.caelus.CaelusCompat;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.event.custom.FullBorgHandler;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeployableElytraItem extends Item implements ICyberwareItem {
    private final int humanityCost;

    private static final ResourceLocation CC_CAELUS_FLIGHT_ID =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "deployable_elytra_flight");

    private static final String NBT_ACTIVATION_PAID = "cc_deployable_elytra_paid";

    private static final int ACTIVATION_COST = 1;
    private static final int GLIDE_COST_PER_TICK = 2;

    public DeployableElytraItem(Properties props, int humanityCost) {
        super(props);
        this.humanityCost = humanityCost;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.createcybernetics.humanity", humanityCost)
                    .withStyle(ChatFormatting.GOLD));

            tooltip.add(Component.translatable("tooltip.createcybernetics.boneupgrades_elytra.energy").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public Map<Item, Integer> getAdditionalAnvilRepairMaterials(ItemStack cyberwareStack) {
        return Map.of(
                ModItems.COMPONENT_ACTUATOR.get(), 150,
                Items.PHANTOM_MEMBRANE, 350
        );
    }


    @Override
    public int getEnergyActivationCost(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        return ACTIVATION_COST;
    }

    @Override
    public int getEnergyUsedPerTick(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        return (entity != null && entity.isFallFlying()) ? GLIDE_COST_PER_TICK : 0;
    }

    @Override
    public int getHumanityCost() {
        return humanityCost;
    }

    @Override
    public Set<TagKey<Item>> requiresCyberwareTags(ItemStack installedStack, CyberwareSlot slot) {
        return Set.of(ModTags.Items.BONE_ITEMS);
    }

    @Override
    public Set<CyberwareSlot> getSupportedSlots() {
        return Set.of(CyberwareSlot.BONE);
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
    public static final class CaelusServerSync {

        @SubscribeEvent
        public static void onCyberwareStateChange(com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent event) {
            Player player = event.getPlayer();
            if (player == null || player.level().isClientSide() || !CaelusCompat.isLoaded()) return;
            var profile = event.getCurrentProfile();
            if (profile == null || !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.DEPLOYABLE_ELYTRA)) {
                CaelusCompat.removeFallFlyingModifier(player, CC_CAELUS_FLIGHT_ID);
            }
        }

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();
            if (player.level().isClientSide()) return;
            if (!CaelusCompat.isLoaded()) return;

            var profile = com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(player);
            if (profile == null || !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.DEPLOYABLE_ELYTRA)) {
                return;
            }

            if (cc$hasRealChestElytra(player)) {
                CaelusCompat.removeFallFlyingModifier(player, CC_CAELUS_FLIGHT_ID);
                return;
            }

            if (!player.hasData(ModAttachments.CYBERWARE)) {
                CaelusCompat.removeFallFlyingModifier(player, CC_CAELUS_FLIGHT_ID);
                return;
            }

            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            if (data == null) {
                CaelusCompat.removeFallFlyingModifier(player, CC_CAELUS_FLIGHT_ID);
                return;
            }

            CyberState state = getCyberState(player, data);

            if (state.active) {
                CaelusCompat.addOrUpdateFallFlyingTransient(player, CC_CAELUS_FLIGHT_ID, 1.0D);
            } else {
                CaelusCompat.removeFallFlyingModifier(player, CC_CAELUS_FLIGHT_ID);
            }

            if (state.anyInstalledStack.isEmpty()) return;
            if (!state.active) {
                clearActivationPaid(state.anyInstalledStack);
                return;
            }

            if (player.isFallFlying()) {
                ensureActivationPaid(state.anyInstalledStack, data);

                if (!data.tryConsumeEnergy(GLIDE_COST_PER_TICK)) {

                }

                if (FullBorgHandler.isWingman(data)) {
                    if (!player.isShiftKeyDown()) {
                        player.displayClientMessage(Component.literal("Hold SHIFT to slow down"), true);
                    }
                }
            } else {
                clearActivationPaid(state.anyInstalledStack);
            }
        }

        private static final class CyberState {
            final boolean installed;
            final boolean active;
            final ItemStack anyInstalledStack;

            private CyberState(boolean installed, boolean active, ItemStack anyInstalledStack) {
                this.installed = installed;
                this.active = active;
                this.anyInstalledStack = anyInstalledStack;
            }
        }

        private static CyberState getCyberState(Player player, PlayerCyberwareData data) {
            boolean installed = false;
            boolean active = false;
            ItemStack anyStack = ItemStack.EMPTY;

            for (int i = 0; i < CyberwareSlot.BONE.size; i++) {
                if (!data.isInstalled(ModItems.BONEUPGRADES_ELYTRA.get(), CyberwareSlot.BONE, i)) continue;

                installed = true;

                InstalledCyberware inst = data.get(CyberwareSlot.BONE, i);
                if (inst == null) continue;

                ItemStack stack = inst.getItem();
                if (stack == null || stack.isEmpty()) continue;
                if (anyStack.isEmpty()) anyStack = stack;
                if (!data.isEnabled(CyberwareSlot.BONE, i)) continue;
                if (!inst.isPowered()) continue;

                active = true;
                break;
            }

            return new CyberState(installed, active, anyStack);
        }
    }

    private static boolean cc$hasRealChestElytra(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.isEmpty()) return false;
        if (!(chest.getItem() instanceof ElytraItem)) return false;
        return ElytraItem.isFlyEnabled(chest);
    }

    private static boolean cc$hasEnabledDeployable(Player player) {
        if (player == null) return false;
        if (!player.hasData(ModAttachments.CYBERWARE)) return false;

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return false;

        for (int i = 0; i < CyberwareSlot.BONE.size; i++) {
            if (!data.isInstalled(ModItems.BONEUPGRADES_ELYTRA.get(), CyberwareSlot.BONE, i)) continue;
            if (!data.isEnabled(CyberwareSlot.BONE, i)) continue;
            return true;
        }
        return false;
    }

    private static boolean ensureActivationPaid(ItemStack stack, PlayerCyberwareData data) {
        if (isActivationPaid(stack)) {
            return true;
        }

        if (!data.tryConsumeEnergy(ACTIVATION_COST)) {
            return false;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.putBoolean(NBT_ACTIVATION_PAID, true));
        data.setDirty();
        return true;
    }

    private static boolean isActivationPaid(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null || cd.isEmpty()) return false;

        CompoundTag t = cd.copyTag();
        return t.getBoolean(NBT_ACTIVATION_PAID);
    }

    private static void clearActivationPaid(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null || cd.isEmpty()) return;

        if (!cd.contains(NBT_ACTIVATION_PAID)) return;

        CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.remove(NBT_ACTIVATION_PAID));
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static final class CaelusClientStart {
        private static boolean cc$sentStartThisFall = false;

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) return;

            if (!CaelusCompat.isLoaded()) return;

            if (cc$hasRealChestElytra(player)) {
                cc$sentStartThisFall = false;
                return;
            }

            if (!cc$hasEnabledDeployable(player)) {
                cc$sentStartThisFall = false;
                return;
            }

            if (player.onGround() || player.isFallFlying() || player.isInWaterOrBubble() || player.isInLava()) {
                cc$sentStartThisFall = false;
                return;
            }

            if (!mc.options.keyJump.isDown()) return;
            if (player.getDeltaMovement().y >= 0.0D) return;

            if (cc$sentStartThisFall) return;
            cc$sentStartThisFall = true;

            player.connection.send(new ServerboundPlayerCommandPacket(
                    player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        }
    }
}