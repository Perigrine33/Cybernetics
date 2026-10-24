package com.perigrine3.createcybernetics.item.cyberware.heart;

import com.perigrine3.createcybernetics.common.scheduling.StaggeredTickScheduler;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.EntityCyberwareData;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.ModMobAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MechanicalHeartItem extends Item implements ICyberwareItem {
    private final int humanityCost;

    private static final int ENERGY_PER_TICK = 6;
    private static final float DAMAGE_PER_SECOND = 2.0F;

    public MechanicalHeartItem(Properties props, int humanityCost) {
        super(props);
        this.humanityCost = humanityCost;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.createcybernetics.humanity", humanityCost).withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("tooltip.createcybernetics.heartupgrades_cyberheart.energy").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public Map<Item, Integer> getAdditionalAnvilRepairMaterials(ItemStack cyberwareStack) {
        return Map.of(
                ModItems.COMPONENT_ACTUATOR.get(), 250,
                ModItems.COMPONENT_STORAGE.get(), 250
        );
    }

    @Override
    public int getHumanityCost() {
        return humanityCost;
    }

    @Override
    public Set<CyberwareSlot> getSupportedSlots() {
        return Set.of(CyberwareSlot.HEART);
    }

    @Override
    public boolean replacesOrgan() {
        return true;
    }

    @Override
    public Set<CyberwareSlot> getReplacedOrgans() {
        return Set.of(CyberwareSlot.HEART);
    }

    @Override
    public int getEnergyUsedPerTick(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        return ENERGY_PER_TICK;
    }

    @Override
    public boolean requiresEnergyToFunction(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        return true;
    }

    @Override
    public void onInstalled(LivingEntity entity) { }

    @Override
    public void onRemoved(LivingEntity entity) { }

    @Override
    public void onTick(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot, int index) {
        if (entity.level().isClientSide) return;
        if (!entity.isAlive()) return;

        InstalledCyberware current = getInstalledCyberware(entity, slot, index);
        if (current == null) return;

        if (current.isPowered()) {
            if (entity.hasEffect(MobEffects.WEAKNESS)) {
                entity.removeEffect(MobEffects.WEAKNESS);
            }
            return;
        }

        if (hasBackupHeart(entity, slot, index)) {
            return;
        }

        if (StaggeredTickScheduler.shouldRun(entity, 20, StaggeredTickScheduler.TickPhase.MECHANICAL_HEART)) {
            DamageSource src = entity.damageSources().generic();
            entity.hurt(src, DAMAGE_PER_SECOND);
        }
    }

    private static InstalledCyberware getInstalledCyberware(LivingEntity entity, CyberwareSlot slot, int index) {
        if (entity instanceof Player player) {
            if (!player.hasData(ModAttachments.CYBERWARE)) return null;

            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            if (data == null) return null;

            return data.get(slot, index);
        }

        if (!entity.hasData(ModMobAttachments.CYBERENTITY_CYBERWARE)) return null;

        EntityCyberwareData data = entity.getData(ModMobAttachments.CYBERENTITY_CYBERWARE);
        if (data == null) return null;

        return data.get(slot, index);
    }

    private static boolean hasBackupHeart(LivingEntity entity, CyberwareSlot currentSlot, int currentIndex) {
        if (entity instanceof Player player) {
            if (!player.hasData(ModAttachments.CYBERWARE)) return false;

            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            if (data == null) return false;

            return hasBackupHeart(data.getAll().get(CyberwareSlot.HEART), currentSlot, currentIndex);
        }

        if (!entity.hasData(ModMobAttachments.CYBERENTITY_CYBERWARE)) return false;

        EntityCyberwareData data = entity.getData(ModMobAttachments.CYBERENTITY_CYBERWARE);
        if (data == null) return false;

        return hasBackupHeart(data.getAll().get(CyberwareSlot.HEART), currentSlot, currentIndex);
    }

    private static boolean hasBackupHeart(InstalledCyberware[] hearts, CyberwareSlot currentSlot, int currentIndex) {
        if (hearts == null) return false;

        for (InstalledCyberware installed : hearts) {
            if (installed == null) continue;
            if (installed.getItem().isEmpty()) continue;
            if (!installed.getItem().is(ModTags.Items.HEART_ITEMS)) continue;

            if (installed.getSlot() == currentSlot && installed.getIndex() == currentIndex) {
                continue;
            }

            return true;
        }

        return false;
    }
}