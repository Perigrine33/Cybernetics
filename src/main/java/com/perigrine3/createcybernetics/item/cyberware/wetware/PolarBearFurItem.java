package com.perigrine3.createcybernetics.item.cyberware.wetware;

import com.perigrine3.createcybernetics.CreateCybernetics;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Set;

public class PolarBearFurItem extends Item implements ICyberwareItem {
    private final int humanityCost;

    public PolarBearFurItem(Properties props, int humanityCost) {
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
        return Set.of(CyberwareSlot.SKIN);
    }

    @Override
    public boolean replacesOrgan() {
        return true;
    }

    @Override
    public Set<CyberwareSlot> getReplacedOrgans() {
        return Set.of(CyberwareSlot.SKIN);
    }

    @Override
    public Set<Item> incompatibleCyberware(ItemStack installedStack, CyberwareSlot slot) {
        return Set.of(ModItems.WETWARE_DRAGONSKIN.get());
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
    public void onInstalled(LivingEntity entity) {
    }

    @Override
    public void onRemoved(LivingEntity entity) {
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID)
    public static final class PolarBearFurFreezingImmunity {

        private PolarBearFurFreezingImmunity() {}

        @SubscribeEvent
        public static void onCyberwareStateChange(com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent event) {
            Player player = event.getPlayer();
            if (player == null || player.level().isClientSide) return;

            var profile = event.getCurrentProfile();
            boolean installed = profile != null
                    && profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.POLAR_BEAR_FUR)
                    && hasPolarBearFurInstalled(player);

            if (installed) {
                if (profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.BLUBBER)) {
                    ColdSweatCompat.applyColdResistance(player, 1.0);
                    ColdSweatCompat.applyColdDampening(player, 0.0);
                } else {
                    ColdSweatCompat.applyColdResistance(player, 0.60);
                    ColdSweatCompat.applyColdDampening(player, 0.30);
                }
            } else if (profile == null || !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.BLUBBER)) {
                ColdSweatCompat.clearCold(player);
            }
        }

        @SubscribeEvent
        public static void onPlayerTickPost(PlayerTickEvent.Post event) {
            Player player = event.getEntity();
            if (player.level().isClientSide) return;
            if (player.getTicksFrozen() <= 0 && !player.isFullyFrozen()) return;

            var profile = com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(player);
            if (profile == null || !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.POLAR_BEAR_FUR)) {
                return;
            }

            if (hasPolarBearFurInstalled(player)) {
                player.setTicksFrozen(0);
            }
        }

        private static boolean hasPolarBearFurInstalled(Player player) {
            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            return data != null && data.isInstalled(ModItems.WETWARE_POLARBEARFUR.get(), CyberwareSlot.SKIN);
        }
    }
}