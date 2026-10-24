package com.perigrine3.createcybernetics.item.cyberware.wetware;

import com.perigrine3.createcybernetics.api.CyberwareDurabilityCategory;
import com.perigrine3.createcybernetics.api.CyberwareRepairType;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.compat.coldsweat.ColdSweatCompat;
import com.perigrine3.createcybernetics.item.ModItems;
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

public class BlubberItem extends Item implements ICyberwareItem {
    private final int humanityCost;

    public BlubberItem(Properties props, int humanityCost) {
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
        return Set.of(CyberwareSlot.MUSCLE);
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
    public CyberwareRepairType getRepairType(ItemStack installedStack, CyberwareSlot slot) {
        return CyberwareRepairType.BIOLOGICAL;
    }

    @Override
    public CyberwareDurabilityCategory getDurabilityCategory(ItemStack installedStack, CyberwareSlot slot) {
        return CyberwareDurabilityCategory.WETWARE;
    }

    @Override
    public int getMaxCyberwareDurability(ItemStack installedStack, CyberwareSlot slot) {
        return 1000;
    }

    @Override
    public void onUnpoweredTick(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide) return;

        ColdSweatCompat.clearCold(player);
    }

    @Override
    public void onRemoved(LivingEntity entity) {
        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide) return;

        ColdSweatCompat.clearCold(player);
    }

    @Override
    public void onTick(LivingEntity entity) {
    }

    public static void syncColdSweatForBlubber(Player player, PlayerCyberwareData data) {
        if (player == null || player.level().isClientSide) return;
        if (data == null || !data.hasSpecificItem(ModItems.WETWARE_BLUBBER.get(), CyberwareSlot.MUSCLE)) {
            return;
        }

        boolean hasPolarBearFur = data.hasSpecificItem(ModItems.WETWARE_POLARBEARFUR.get(), CyberwareSlot.MUSCLE);
        if (!hasPolarBearFur) {
            ColdSweatCompat.applyColdResistance(player, 0.50);
            ColdSweatCompat.applyColdDampening(player, 0.3);
        } else {
            ColdSweatCompat.applyColdResistance(player, 1.0);
            ColdSweatCompat.applyColdDampening(player, 0.0);
        }
    }

    @net.neoforged.fml.common.EventBusSubscriber(modid = com.perigrine3.createcybernetics.CreateCybernetics.MODID, bus = net.neoforged.fml.common.EventBusSubscriber.Bus.GAME)
    public static final class EventHooks {
        private EventHooks() {}

        @net.neoforged.bus.api.SubscribeEvent
        public static void onCyberwareStateChange(com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent event) {
            var profile = event.getCurrentProfile();
            if (profile != null && profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.BLUBBER)) {
                syncColdSweatForBlubber(event.getPlayer(), event.getData());
            }
        }
    }
}