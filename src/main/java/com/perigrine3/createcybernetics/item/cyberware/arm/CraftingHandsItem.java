package com.perigrine3.createcybernetics.item.cyberware.arm;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.network.payload.OpenExpandedInventoryPayload;
import com.perigrine3.createcybernetics.screen.custom.crafting.ExpandedInventoryScreen;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Set;

public class CraftingHandsItem extends Item implements ICyberwareItem {

    private final int humanityCost;

    public CraftingHandsItem(Properties props, int humanityCost) {
        super(props);
        this.humanityCost = humanityCost;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.createcybernetics.humanity", humanityCost).withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("tooltip.createcybernetics.armupgrades_crafthands.energy").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public int getEnergyUsedPerTick(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        return 2;
    }

    @Override
    public boolean requiresEnergyToFunction(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        return true;
    }

    @Override
    public int getHumanityCost() {
        return humanityCost;
    }

    @Override
    public Set<TagKey<Item>> requiresCyberwareTags(ItemStack installedStack, CyberwareSlot slot) {
        return switch (slot) {
            case RLEG -> Set.of(ModTags.Items.RIGHTARM_ITEMS);
            case LLEG -> Set.of(ModTags.Items.LEFTARM_ITEMS);
            default -> Set.of();
        };
    }

    @Override
    public Set<CyberwareSlot> getSupportedSlots() {
        return Set.of(CyberwareSlot.LARM, CyberwareSlot.RARM);
    }

    @Override
    public boolean replacesOrgan() {
        return false;
    }

    @Override
    public Set<CyberwareSlot> getReplacedOrgans() {
        return Set.of();
    }

    private record Found(CyberwareSlot slot, int index) {}

    private static Found findFirst(PlayerCyberwareData data, CyberwareSlot slot) {
        InstalledCyberware[] arr = data.getAll().get(slot);
        if (arr == null) return null;

        for (int i = 0; i < arr.length; i++) {
            InstalledCyberware cw = arr[i];
            if (cw == null) continue;

            ItemStack st = cw.getItem();
            if (st == null || st.isEmpty()) continue;

            if (st.getItem() instanceof CraftingHandsItem) return new Found(slot, i);
        }
        return null;
    }

    public static boolean hasInstalledEitherArm(PlayerCyberwareData data) {
        return findFirst(data, CyberwareSlot.LARM) != null || findFirst(data, CyberwareSlot.RARM) != null;
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class ServerHandler {

        @SubscribeEvent
        public static void onCyberwareStateChange(com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent event) {
            Player player = event.getPlayer();
            if (player == null || player.level().isClientSide) return;
            var profile = event.getCurrentProfile();
            if (profile == null || !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.CRAFTING_HANDS)) {
                return;
            }
            PlayerCyberwareData data = event.getData();
            if (data == null) return;

            Found left = findFirst(data, CyberwareSlot.LARM);
            Found right = findFirst(data, CyberwareSlot.RARM);

            if (left != null && right != null) {
                InstalledCyberware removed = data.remove(right.slot(), right.index());
                data.setDirty();

                if (removed != null) {
                    ItemStack st = removed.getItem();
                    if (st != null && !st.isEmpty()) {
                        ItemStack copy = st.copy();
                        if (!player.getInventory().add(copy)) {
                            player.drop(copy, false);
                        }
                    }
                }
            }
        }
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static final class ClientHandler {

        @SubscribeEvent
        public static void onScreenOpening(net.neoforged.neoforge.client.event.ScreenEvent.Opening event) {
            if (!(event.getNewScreen() instanceof InventoryScreen)) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            if (mc.player.isCreative() || mc.player.isSpectator()) return;

            if (event.getCurrentScreen() instanceof ExpandedInventoryScreen) return;

            if (!mc.player.hasData(ModAttachments.CYBERWARE)) return;
            PlayerCyberwareData data = mc.player.getData(ModAttachments.CYBERWARE);
            if (data == null) return;

            if (!CraftingHandsItem.hasInstalledEitherArm(data)) return;

            event.setNewScreen(null);
            PacketDistributor.sendToServer(new OpenExpandedInventoryPayload());
        }
    }
}