package com.perigrine3.createcybernetics.item.cyberware.brain;

import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.cyberspace.CyberspaceTravelManager;
import com.perigrine3.createcybernetics.entity.custom.JackedInPlayerBodyEntity;
import com.perigrine3.createcybernetics.util.CyberwareAttributeHelper;
import com.perigrine3.createcybernetics.world.dimension.ModDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Set;

public class CoaxialPortItem extends Item implements ICyberwareItem {
    public static final int JACKED_IN_ENERGY_COST = 10;

    private final int humanityCost;

    public CoaxialPortItem(Properties props, int humanityCost) {
        super(props);
        this.humanityCost = humanityCost;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.createcybernetics.humanity", humanityCost).withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("tooltip.createcybernetics.brainupgrades_coaxialport.energy").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public int getHumanityCost() {
        return humanityCost;
    }

    public boolean isDyeable(ItemStack stack, CyberwareSlot slot) {
        return slot == CyberwareSlot.BRAIN;
    }

    @Override
    public boolean isDyeable(ItemStack stack) {
        return true;
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
        return Set.of(CyberwareSlot.BRAIN);
    }

    @Override
    public void onInstalled(LivingEntity entity) {
        CyberwareAttributeHelper.applyModifier(entity, "coaxial_ram");
    }

    @Override
    public void onRemoved(LivingEntity entity) {
        CyberwareAttributeHelper.removeModifier(entity, "coaxial_ram");
    }

    @Override
    public void onTick(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return;
        if (!player.level().dimension().equals(ModDimensions.CYBERSPACE_LEVEL)) return;
        if (!CyberspaceTravelManager.hasReturnPoint(player)) return;
        if (!player.hasData(ModAttachments.CYBERWARE)) {
            CyberspaceTravelManager.unsafeJackOut(player);
            return;
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) {
            CyberspaceTravelManager.unsafeJackOut(player);
            return;
        }

        if (!data.tryConsumeEnergy(player, JACKED_IN_ENERGY_COST)) {
            player.displayClientMessage(Component.translatable("message.createcybernetics.cyberspace.connection_power_lost").withStyle(ChatFormatting.RED), true);
            CyberspaceTravelManager.unsafeJackOut(player);
            return;
        }

        JackedInPlayerBodyEntity body = CyberspaceTravelManager.getStandInBody(player);

        if (body != null && body.isAlive()) {
            body.setCyberwareSnapshot(PlayerCyberwareData.createSnapshotTagFor(player, player.registryAccess()));
        }
    }
}