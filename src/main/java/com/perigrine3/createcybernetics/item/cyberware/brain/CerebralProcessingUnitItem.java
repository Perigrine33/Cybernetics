package com.perigrine3.createcybernetics.item.cyberware.brain;

import com.mojang.blaze3d.systems.RenderSystem;
import com.perigrine3.createcybernetics.ConfigValues;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.advancement.ModCriteria;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareData;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.EntityCyberwareData;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.common.damage.ModDamageSources;
import com.perigrine3.createcybernetics.common.humanity.DataIntegrityHandler;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.item.generic.BiochipDataShardItem;
import com.perigrine3.createcybernetics.network.payload.CerebralShutdownStatePayload;
import com.perigrine3.createcybernetics.util.CyberwareAttributeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.Input;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CerebralProcessingUnitItem extends Item implements ICyberwareItem {
    private final int humanityCost;

    private static final int ENERGY_PER_TICK = 15;

    private static volatile boolean CLIENT_SHUTDOWN_ACTIVE = false;

    private static final String NBT_SHUTDOWN_ACTIVE = "cc_cpu_shutdown_active";

    private static final String NBT_ANCHOR_SET = "cc_cpu_shutdown_anchor";
    private static final String NBT_AX = "cc_cpu_shutdown_ax";
    private static final String NBT_AY = "cc_cpu_shutdown_ay";
    private static final String NBT_AZ = "cc_cpu_shutdown_az";
    private static final String NBT_AYAW = "cc_cpu_shutdown_yaw";
    private static final String NBT_APITCH = "cc_cpu_shutdown_pitch";

    public CerebralProcessingUnitItem(Properties props, int humanityCost) {
        super(props);
        this.humanityCost = humanityCost;
    }

    public static void setClientShutdownActive(boolean active) {
        CLIENT_SHUTDOWN_ACTIVE = active;
    }

    public static boolean clientShutdownActive() {
        return CLIENT_SHUTDOWN_ACTIVE;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (hasEngramData(stack)) {
            String ownerName = getEngramOwnerName(stack);
            String ownerUuid = getEngramUuid(stack);

            if (!ownerName.isBlank()) {
                tooltip.add(
                        Component.literal(ownerName + ".dhf")
                                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)
                );
            }

            tooltip.add(
                    Component.literal(ownerUuid)
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        } else {
            tooltip.add(
                    Component.translatable("tooltip.createcybernetics.brainupgrades_cyberbrain.unbound")
                            .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)
            );
        }

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.createcybernetics.brainupgrades_cyberbrain.data_integrity")
                    .withStyle(ChatFormatting.YELLOW));

            tooltip.add(Component.translatable("tooltip.createcybernetics.brainupgrades_cyberbrain.boot_down")
                    .withStyle(ChatFormatting.YELLOW));

            tooltip.add(Component.translatable("tooltip.createcybernetics.brainupgrades_cyberbrain.energy")
                    .withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public Map<Item, Integer> getAdditionalAnvilRepairMaterials(ItemStack cyberwareStack) {
        return Map.of(
                Items.GOLD_INGOT, 100,
                ModItems.COMPONENT_SYNTHNERVES.get(), 100,
                ModItems.COMPONENT_SSD.get(), 200,
                ModItems.COMPONENT_GRAPHICSCARD.get(), 200,

                ModItems.COMPONENT_PLATING.get(), 100,
                ModItems.COMPONENT_WIRING.get(), 150,
                ModItems.COMPONENT_MESH.get(), 150
        );
    }


    @Override
    public int getHumanityCost() {
        return humanityCost;
    }

    @Override
    public Set<CyberwareSlot> getSupportedSlots() {
        return Set.of(CyberwareSlot.BRAIN);
    }

    @Override
    public boolean replacesOrgan() {
        return true;
    }

    @Override
    public Set<CyberwareSlot> getReplacedOrgans() {
        return Set.of(CyberwareSlot.BRAIN);
    }

    @Override
    public int getEnergyUsedPerTick(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        return ENERGY_PER_TICK;
    }

    @Override
    public void onInstalled(LivingEntity entity) {
        CyberwareAttributeHelper.applyModifier(entity, "cyberbrain_learn");
        CyberwareAttributeHelper.applyModifier(entity, "cyberbrain_insomnia");
        CyberwareAttributeHelper.applyModifier(entity, "cpu_ram");
        CyberwareAttributeHelper.applyModifier(entity, "cpu_ram_regen");

        if (entity instanceof Player player && !player.level().isClientSide) {
            DataIntegrityHandler.restoreIntegrity(player);
        }
    }

    @Override
    public void onRemoved(LivingEntity entity) {
        CyberwareAttributeHelper.removeModifier(entity, "cyberbrain_learn");
        CyberwareAttributeHelper.removeModifier(entity, "cyberbrain_insomnia");
        CyberwareAttributeHelper.removeModifier(entity, "cpu_ram");
        CyberwareAttributeHelper.removeModifier(entity, "cpu_ram_regen");
    }

    @Override
    public void onTick(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot, int index) {
        if (entity.level().isClientSide) {
            return;
        }

        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        if (slot != CyberwareSlot.BRAIN) {
            return;
        }

        if (!isBoundToPlayerName(installedStack, player)) {
            player.hurt(ModDamageSources.brainDamage(player.level(), player, null), 500000.0F);
        }
    }

    @Override
    public void onTick(LivingEntity entity) {
    }

    private static boolean cpuInstalledEnabledAndUnpowered(ICyberwareData data) {
        if (data == null) return false;

        InstalledCyberware[] arr = data.getAll().get(CyberwareSlot.BRAIN);
        if (arr == null) return false;

        for (int idx = 0; idx < arr.length; idx++) {
            InstalledCyberware installed = arr[idx];
            if (installed == null) continue;

            ItemStack st = installed.getItem();
            if (st == null || st.isEmpty()) continue;

            if (!(st.getItem() instanceof CerebralProcessingUnitItem)) continue;
            if (!isEnabled(data, CyberwareSlot.BRAIN, idx)) continue;

            return !installed.isPowered();
        }

        return false;
    }

    private static boolean isEnabled(ICyberwareData data, CyberwareSlot slot, int index) {
        if (data instanceof PlayerCyberwareData pData) {
            return pData.isEnabled(slot, index);
        }

        if (data instanceof EntityCyberwareData eData) {
            return eData.isEnabled(slot, index);
        }

        return true;
    }

    private static boolean clientOverlayActive(Player player) {
        return player != null && CLIENT_SHUTDOWN_ACTIVE;
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class ShutdownServerDecision {
        private ShutdownServerDecision() {}

        @SubscribeEvent
        public static void onCyberwareStateChange(com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent event) {
            if (!(event.getPlayer() instanceof ServerPlayer sp)) return;
            var profile = event.getCurrentProfile();
            if (profile == null || !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.CEREBRAL_PROCESSING_UNIT)) {
                CompoundTag pt = sp.getPersistentData();
                if (pt.getBoolean(NBT_SHUTDOWN_ACTIVE)) {
                    clearShutdownState(sp);
                    PacketDistributor.sendToPlayer(sp, new CerebralShutdownStatePayload(false));
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onTickPost(PlayerTickEvent.Post event) {
            Player p = event.getEntity();
            if (p.level().isClientSide) return;
            if (!(p instanceof ServerPlayer sp)) return;

            var profile = com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(sp);
            if (profile == null || !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.CEREBRAL_PROCESSING_UNIT)) {
                return;
            }

            if (!sp.hasData(ModAttachments.CYBERWARE)) {
                clearShutdownState(sp);
                PacketDistributor.sendToPlayer(sp, new CerebralShutdownStatePayload(false));
                return;
            }

            PlayerCyberwareData data = sp.getData(ModAttachments.CYBERWARE);
            if (data == null) {
                clearShutdownState(sp);
                PacketDistributor.sendToPlayer(sp, new CerebralShutdownStatePayload(false));
                return;
            }

            if (hasMismatchedInstalledCyberbrain(data, sp)) {
                sp.hurt(ModDamageSources.brainDamage(sp.level(), sp, null), 500000.0F);
                return;
            }

            boolean shutdownNow = cpuInstalledEnabledAndUnpowered(data);

            CompoundTag pt = sp.getPersistentData();
            boolean prev = pt.getBoolean(NBT_SHUTDOWN_ACTIVE);

            if (shutdownNow == prev) {
                return;
            }

            pt.putBoolean(NBT_SHUTDOWN_ACTIVE, shutdownNow);

            if (shutdownNow) {
                ModCriteria.THOUGHTS_NOT_FOUND.get().trigger(sp);
                pt.putBoolean(NBT_ANCHOR_SET, false);
            } else {
                pt.remove(NBT_ANCHOR_SET);
                pt.remove(NBT_AX);
                pt.remove(NBT_AY);
                pt.remove(NBT_AZ);
                pt.remove(NBT_AYAW);
                pt.remove(NBT_APITCH);
            }

            PacketDistributor.sendToPlayer(sp, new CerebralShutdownStatePayload(shutdownNow));
        }
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class ShutdownServerEnforce {
        private ShutdownServerEnforce() {}

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onTickPre(PlayerTickEvent.Pre event) {
            Player p = event.getEntity();
            if (p.level().isClientSide) return;
            if (!(p instanceof ServerPlayer sp)) return;

            var profile = com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(sp);
            if (profile == null || !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.CEREBRAL_PROCESSING_UNIT)) {
                return;
            }

            CompoundTag pt = sp.getPersistentData();
            if (!pt.getBoolean(NBT_SHUTDOWN_ACTIVE)) return;

            if (!pt.getBoolean(NBT_ANCHOR_SET)) {
                pt.putBoolean(NBT_ANCHOR_SET, true);
                pt.putDouble(NBT_AX, sp.getX());
                pt.putDouble(NBT_AY, sp.getY());
                pt.putDouble(NBT_AZ, sp.getZ());
                pt.putFloat(NBT_AYAW, sp.getYRot());
                pt.putFloat(NBT_APITCH, sp.getXRot());
            }

            double ax = pt.getDouble(NBT_AX);
            double ay = pt.getDouble(NBT_AY);
            double az = pt.getDouble(NBT_AZ);
            float yaw = pt.getFloat(NBT_AYAW);
            float pitch = pt.getFloat(NBT_APITCH);

            sp.connection.teleport(ax, ay, az, yaw, pitch);

            sp.setDeltaMovement(Vec3.ZERO);
            sp.fallDistance = 0;
            sp.setSprinting(false);
            sp.stopUsingItem();
        }

        @SubscribeEvent
        public static void onAttackEntity(AttackEntityEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer sp)) return;
            if (sp.getPersistentData().getBoolean(NBT_SHUTDOWN_ACTIVE)) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
            if (!(event.getEntity() instanceof ServerPlayer sp)) return;
            if (sp.getPersistentData().getBoolean(NBT_SHUTDOWN_ACTIVE)) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            if (!(event.getEntity() instanceof ServerPlayer sp)) return;
            if (sp.getPersistentData().getBoolean(NBT_SHUTDOWN_ACTIVE)) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            if (!(event.getEntity() instanceof ServerPlayer sp)) return;
            if (sp.getPersistentData().getBoolean(NBT_SHUTDOWN_ACTIVE)) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
            if (!(event.getEntity() instanceof ServerPlayer sp)) return;
            if (sp.getPersistentData().getBoolean(NBT_SHUTDOWN_ACTIVE)) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
            if (!(event.getEntity() instanceof ServerPlayer sp)) return;
            if (sp.getPersistentData().getBoolean(NBT_SHUTDOWN_ACTIVE)) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
            if (!(event.getEntity() instanceof ServerPlayer sp)) return;
            if (sp.getPersistentData().getBoolean(NBT_SHUTDOWN_ACTIVE)) {
                event.setNewSpeed(0.0F);
            }
        }
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static final class ShutdownClientHooks {
        private ShutdownClientHooks() {}

        private static boolean anchorSet = false;
        private static float anchorYaw;
        private static float anchorPitch;

        @SubscribeEvent
        public static void onMove(MovementInputUpdateEvent event) {
            if (!CLIENT_SHUTDOWN_ACTIVE) return;

            Input in = event.getInput();
            in.leftImpulse = 0.0F;
            in.forwardImpulse = 0.0F;

            in.up = false;
            in.down = false;
            in.left = false;
            in.right = false;

            in.jumping = false;
            in.shiftKeyDown = false;
        }

        @SubscribeEvent
        public static void onInteract(InputEvent.InteractionKeyMappingTriggered event) {
            if (!CLIENT_SHUTDOWN_ACTIVE) return;
            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Pre event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            if (!CLIENT_SHUTDOWN_ACTIVE) {
                anchorSet = false;
                return;
            }

            if (!anchorSet) {
                anchorSet = true;
                anchorYaw = mc.player.getYRot();
                anchorPitch = mc.player.getXRot();
            }

            mc.player.setYRot(anchorYaw);
            mc.player.setXRot(anchorPitch);

            mc.player.yRotO = anchorYaw;
            mc.player.xRotO = anchorPitch;

            mc.player.yHeadRot = anchorYaw;
            mc.player.yHeadRotO = anchorYaw;
            mc.player.yBodyRot = anchorYaw;
            mc.player.yBodyRotO = anchorYaw;
        }

        @SubscribeEvent
        public static void onRenderGuiPre(RenderGuiEvent.Pre event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            if (mc.screen != null) return;

            if (!clientOverlayActive(mc.player)) return;

            GuiGraphics gg = event.getGuiGraphics();
            int w = mc.getWindow().getGuiScaledWidth();
            int h = mc.getWindow().getGuiScaledHeight();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            gg.fill(0, 0, w, h, 0xFF000000);

            RenderSystem.disableBlend();
        }
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class ShutdownResetHooks {
        private ShutdownResetHooks() {}

        @SubscribeEvent
        public static void onClone(PlayerEvent.Clone event) {
            if (!event.isWasDeath()) return;
            if (!(event.getEntity() instanceof ServerPlayer sp)) return;

            clearShutdownState(sp);
            PacketDistributor.sendToPlayer(sp, new CerebralShutdownStatePayload(false));
        }

        @SubscribeEvent
        public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer sp)) return;

            clearShutdownState(sp);
            PacketDistributor.sendToPlayer(sp, new CerebralShutdownStatePayload(false));
        }
    }

    private static void clearShutdownState(ServerPlayer sp) {
        CompoundTag pt = sp.getPersistentData();

        pt.putBoolean(NBT_SHUTDOWN_ACTIVE, false);

        pt.remove(NBT_ANCHOR_SET);
        pt.remove(NBT_AX);
        pt.remove(NBT_AY);
        pt.remove(NBT_AZ);
        pt.remove(NBT_AYAW);
        pt.remove(NBT_APITCH);
    }

    public static boolean hasCompletedBiochipBinding(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        if (!(stack.getItem() instanceof CerebralProcessingUnitItem)) {
            return false;
        }

        CompoundTag tag = BiochipDataShardItem.getTagOrNull(stack);
        if (tag == null) {
            return false;
        }

        if (!tag.getBoolean(BiochipDataShardItem.TAG_DONE)) {
            return false;
        }

        String ownerName = tag.getString(BiochipDataShardItem.TAG_OWNER_NAME);
        String ownerUuid = tag.getString(BiochipDataShardItem.TAG_OWNER_UUID);

        return !ownerName.isBlank() && !ownerUuid.isBlank();
    }

    public static String getBoundOwnerName(ItemStack stack) {
        CompoundTag tag = BiochipDataShardItem.getTagOrNull(stack);
        if (tag == null) {
            return "";
        }

        return tag.getString(BiochipDataShardItem.TAG_OWNER_NAME);
    }

    public static boolean isBoundToPlayerName(ItemStack stack, ServerPlayer player) {
        if (!hasCompletedBiochipBinding(stack) || player == null) {
            return false;
        }

        String brainOwnerName = getBoundOwnerName(stack);
        String playerName = player.getGameProfile().getName();

        return !brainOwnerName.isBlank()
                && !playerName.isBlank()
                && brainOwnerName.equals(playerName);
    }

    private static boolean hasMismatchedInstalledCyberbrain(PlayerCyberwareData data, ServerPlayer player) {
        if (data == null) {
            return false;
        }

        InstalledCyberware[] brains = data.getAll().get(CyberwareSlot.BRAIN);
        if (brains == null) {
            return false;
        }

        for (InstalledCyberware installed : brains) {
            if (installed == null) {
                continue;
            }

            ItemStack stack = installed.getItem();
            if (stack == null || stack.isEmpty()) {
                continue;
            }

            if (!(stack.getItem() instanceof CerebralProcessingUnitItem)) {
                continue;
            }

            if (!isBoundToPlayerName(stack, player)) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasEngramData(ItemStack stack) {
        CompoundTag tag = BiochipDataShardItem.getTagOrNull(stack);

        if (tag == null) {
            return false;
        }

        return tag.getBoolean(BiochipDataShardItem.TAG_DONE)
                && !tag.getString(BiochipDataShardItem.TAG_OWNER_UUID).isBlank();
    }

    public static String getEngramUuid(ItemStack stack) {
        CompoundTag tag = BiochipDataShardItem.getTagOrNull(stack);

        if (tag == null) {
            return "";
        }

        return tag.getString(BiochipDataShardItem.TAG_OWNER_UUID);
    }

    public static String getEngramOwnerName(ItemStack stack) {
        CompoundTag tag = BiochipDataShardItem.getTagOrNull(stack);

        if (tag == null) {
            return "";
        }

        return tag.getString(BiochipDataShardItem.TAG_OWNER_NAME);
    }
}