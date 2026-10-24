package com.perigrine3.createcybernetics.item.cyberware.brain;

import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.util.CyberwareAttributeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Set;

public class RamdeckItem extends Item implements ICyberwareItem {
    private final int humanityCost;

    public RamdeckItem(Properties props, int humanityCost) {
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
        return Set.of(CyberwareSlot.BRAIN);
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
    public Set<Item> requiresCyberware(ItemStack installedStack, CyberwareSlot slot) {
        return Set.of(ModItems.BRAINUPGRADES_CYBERDECK.get());
    }

    @Override
    public int maxStacksPerSlotType(ItemStack stack, CyberwareSlot slotType) {
        return 3;
    }

    @Override
    public int getEnergyUsedPerTick(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        PlayerCyberwareData data = entity.getData(ModAttachments.CYBERWARE);
        if (data == null) return 0;

        if (data.hasMultipleSpecificItem(ModItems.BRAINUPGRADES_RAMDECK.get(), CyberwareSlot.BRAIN, 3)) {
            return 18;
        } else if (data.hasMultipleSpecificItem(ModItems.BRAINUPGRADES_RAMDECK.get(), CyberwareSlot.BRAIN, 2)) {
            return 12;
        } else if (data.hasMultipleSpecificItem(ModItems.BRAINUPGRADES_RAMDECK.get(), CyberwareSlot.BRAIN, 1)) {
            return 6;
        } else {
            return 0;
        }
    }

    @Override
    public void onInstalled(Player player) {
        // grant strength modifier
    }

    @Override
    public void onRemoved(Player player) {
        CyberwareAttributeHelper.removeModifier(player, "ramdeck1_ram");
        CyberwareAttributeHelper.removeModifier(player, "ramdeck2_ram");
        CyberwareAttributeHelper.removeModifier(player, "ramdeck3_ram");
        CyberwareAttributeHelper.removeModifier(player, "ramdeck1_ram_regen");
        CyberwareAttributeHelper.removeModifier(player, "ramdeck2_ram_regen");
        CyberwareAttributeHelper.removeModifier(player, "ramdeck3_ram_regen");
    }

    @Override
    public void onTick(LivingEntity entity) {
    }

    public static void syncRamdeckModifiers(Player player, PlayerCyberwareData data) {
        if (player == null || data == null) return;

        if (data.hasMultipleSpecificItem(ModItems.BRAINUPGRADES_RAMDECK.get(), CyberwareSlot.BRAIN, 3)) {
            CyberwareAttributeHelper.applyModifier(player, "ramdeck1_ram");
            CyberwareAttributeHelper.applyModifier(player, "ramdeck2_ram");
            CyberwareAttributeHelper.applyModifier(player, "ramdeck3_ram");

            CyberwareAttributeHelper.applyModifier(player, "ramdeck1_ram_regen");
            CyberwareAttributeHelper.applyModifier(player, "ramdeck2_ram_regen");
            CyberwareAttributeHelper.applyModifier(player, "ramdeck3_ram_regen");
        } else if (data.hasMultipleSpecificItem(ModItems.BRAINUPGRADES_RAMDECK.get(), CyberwareSlot.BRAIN, 2)) {
            CyberwareAttributeHelper.applyModifier(player, "ramdeck1_ram");
            CyberwareAttributeHelper.applyModifier(player, "ramdeck2_ram");
            CyberwareAttributeHelper.removeModifier(player, "ramdeck3_ram");

            CyberwareAttributeHelper.applyModifier(player, "ramdeck1_ram_regen");
            CyberwareAttributeHelper.applyModifier(player, "ramdeck2_ram_regen");
            CyberwareAttributeHelper.removeModifier(player, "ramdeck3_ram_regen");
        } else if (data.hasMultipleSpecificItem(ModItems.BRAINUPGRADES_RAMDECK.get(), CyberwareSlot.BRAIN, 1)) {
            CyberwareAttributeHelper.applyModifier(player, "ramdeck1_ram");
            CyberwareAttributeHelper.removeModifier(player, "ramdeck2_ram");
            CyberwareAttributeHelper.removeModifier(player, "ramdeck3_ram");

            CyberwareAttributeHelper.applyModifier(player, "ramdeck1_ram_regen");
            CyberwareAttributeHelper.removeModifier(player, "ramdeck2_ram_regen");
            CyberwareAttributeHelper.removeModifier(player, "ramdeck3_ram_regen");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "ramdeck1_ram");
            CyberwareAttributeHelper.removeModifier(player, "ramdeck2_ram");
            CyberwareAttributeHelper.removeModifier(player, "ramdeck3_ram");
            CyberwareAttributeHelper.removeModifier(player, "ramdeck1_ram_regen");
            CyberwareAttributeHelper.removeModifier(player, "ramdeck2_ram_regen");
            CyberwareAttributeHelper.removeModifier(player, "ramdeck3_ram_regen");
        }
    }

    @net.neoforged.fml.common.EventBusSubscriber(modid = com.perigrine3.createcybernetics.CreateCybernetics.MODID, bus = net.neoforged.fml.common.EventBusSubscriber.Bus.GAME)
    public static final class EventHooks {
        private EventHooks() {}

        @net.neoforged.bus.api.SubscribeEvent
        public static void onCyberwareStateChange(com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent event) {
            syncRamdeckModifiers(event.getPlayer(), event.getData());
        }
    }
}