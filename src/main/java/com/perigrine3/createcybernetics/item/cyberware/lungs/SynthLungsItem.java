package com.perigrine3.createcybernetics.item.cyberware.lungs;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.compat.parcool.ParcoolCompat;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.util.CyberwareAttributeHelper;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
public class SynthLungsItem extends Item implements ICyberwareItem {
    private final int humanityCost;

    private static final String SYNTHLUNGS_BREATH_MODIFIER = "synthlungs_breath";

    public SynthLungsItem(Properties props, int humanityCost) {
        super(props);
        this.humanityCost = humanityCost;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.createcybernetics.humanity", humanityCost)
                    .withStyle(ChatFormatting.GOLD));

            tooltip.add(Component.translatable("tooltip.createcybernetics.lungsupgrades_synthlungs.energy")
                    .withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public Map<Item, Integer> getAdditionalAnvilRepairMaterials(ItemStack cyberwareStack) {
        return Map.of(
                ModItems.COMPONENT_ACTUATOR.get(), 250,
                ModItems.COMPONENT_MESH.get(), 250
        );
    }

    @Override
    public int getHumanityCost() {
        return humanityCost;
    }

    @Override
    public Set<TagKey<Item>> requiresCyberwareTags(ItemStack installedStack, CyberwareSlot slot) {
        return Set.of(ModTags.Items.LUNGS_ITEMS);
    }

    @Override
    public Set<CyberwareSlot> getSupportedSlots() {
        return Set.of(CyberwareSlot.LUNGS);
    }

    @Override
    public int maxStacksPerSlotType(ItemStack stack, CyberwareSlot slotType) {
        return 3;
    }

    @Override
    public boolean replacesOrgan() {
        return true;
    }

    @Override
    public Set<CyberwareSlot> getReplacedOrgans() {
        return Set.of();
    }

    @Override
    public int getEnergyUsedPerTick(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        return 7;
    }

    @Override
    public boolean requiresEnergyToFunction(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        return true;
    }

    @Override
    public void onInstalled(LivingEntity entity) {

    }

    @Override
    public void onRemoved(LivingEntity entity) {

    }

    @SubscribeEvent
    public static void onCyberwareStateChange(com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        var profile = event.getCurrentProfile();
        boolean hasSynthLungs = profile != null
                && profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.SYNTH_LUNGS);

        if (hasSynthLungs) {
            CyberwareAttributeHelper.applyModifier(player, SYNTHLUNGS_BREATH_MODIFIER);
            ParcoolCompat.applySynthLungsStaminaRecovery(player);
        } else {
            CyberwareAttributeHelper.removeModifier(player, SYNTHLUNGS_BREATH_MODIFIER);
            ParcoolCompat.removeSynthLungsStaminaRecovery(player);
        }
    }

    private static boolean hasSynthLungsInstalled(PlayerCyberwareData data) {
        if (data == null || data.getAll() == null) {
            return false;
        }

        for (InstalledCyberware[] installedArray : data.getAll().values()) {
            if (installedArray == null) continue;

            for (InstalledCyberware installed : installedArray) {
                if (installed == null) continue;

                ItemStack stack = installed.getItem();
                if (stack.isEmpty()) continue;

                if (stack.getItem() instanceof SynthLungsItem) {
                    return true;
                }
            }
        }

        return false;
    }
}