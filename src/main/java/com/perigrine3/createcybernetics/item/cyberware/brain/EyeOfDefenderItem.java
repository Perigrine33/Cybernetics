package com.perigrine3.createcybernetics.item.cyberware.brain;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.EntityCyberwareData;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.ModMobAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.effect.ModEffects;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.util.CyberwareAttributeHelper;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class EyeOfDefenderItem extends Item implements ICyberwareItem {
    private final int humanityCost;

    private static final int ENERGY_PER_TICK = 5;
    private static final int EFFECT_DURATION_TICKS = 40;

    public EyeOfDefenderItem(Properties props, int humanityCost) {
        super(props);
        this.humanityCost = humanityCost;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.createcybernetics.humanity", humanityCost)
                    .withStyle(ChatFormatting.GOLD));

            tooltip.add(Component.translatable("tooltip.createcybernetics.brainupgrades_eyeofdefender.energy")
                    .withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public Map<Item, Integer> getAdditionalAnvilRepairMaterials(ItemStack cyberwareStack) {
        return Map.of(
                Items.ENDER_EYE, 250,
                ModItems.COMPONENT_SYNTHNERVES.get(), 100,
                ModItems.TITANIUMSHEET.get(), 100
        );
    }

    @Override
    public int getHumanityCost() {
        return humanityCost;
    }

    @Override
    public Set<TagKey<Item>> requiresCyberwareTags(ItemStack installedStack, CyberwareSlot slot) {
        return Set.of(ModTags.Items.BRAIN_ITEMS);
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
    public int getEnergyUsedPerTick(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        return ENERGY_PER_TICK;
    }

    @Override
    public void onInstalled(LivingEntity entity) {
        CyberwareAttributeHelper.applyModifier(entity, "eyeofdedender_ram");
    }

    @Override
    public void onRemoved(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide) return;
        entity.removeEffect(ModEffects.PROJECTILE_DODGE_EFFECT);

        CyberwareAttributeHelper.removeModifier(entity, "eyeofdedender_ram");
    }

    private static boolean isActive(Player player) {
        if (player == null) return false;
        if (player.level().isClientSide) return false;
        if (!player.isAlive()) return false;
        if (player.isCreative() || player.isSpectator()) return false;

        if (!player.hasData(ModAttachments.CYBERWARE)) return false;
        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return false;

        var profile = com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(player, data);
        if (profile != null && !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.EYE_OF_DEFENDER)) {
            return false;
        }

        InstalledCyberware[] arr = data.getAll().get(CyberwareSlot.BRAIN);
        if (arr == null) return false;

        for (int idx = 0; idx < arr.length; idx++) {
            InstalledCyberware cw = arr[idx];
            if (cw == null) continue;

            ItemStack st = cw.getItem();
            if (st == null || st.isEmpty()) continue;

            if (!(st.getItem() instanceof EyeOfDefenderItem)) continue;
            if (!data.isEnabled(CyberwareSlot.BRAIN, idx)) return false;

            return cw.isPowered();
        }

        return false;
    }

    private static boolean isActiveEntity(LivingEntity entity) {
        if (entity == null) return false;
        if (entity.level().isClientSide) return false;
        if (!entity.isAlive()) return false;

        if (!entity.hasData(ModMobAttachments.CYBERENTITY_CYBERWARE)) return false;
        EntityCyberwareData data = entity.getData(ModMobAttachments.CYBERENTITY_CYBERWARE);
        if (data == null || !com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.hasEntityEyeOfDefender(data)) {
            return false;
        }

        InstalledCyberware[] arr = data.getAll().get(CyberwareSlot.BRAIN);
        if (arr == null) return false;

        for (int idx = 0; idx < arr.length; idx++) {
            InstalledCyberware cw = arr[idx];
            if (cw == null) continue;

            ItemStack st = cw.getItem();
            if (st == null || st.isEmpty()) continue;

            if (!(st.getItem() instanceof EyeOfDefenderItem)) continue;
            if (!data.isEnabled(CyberwareSlot.BRAIN, idx)) return false;

            return true;
        }

        return false;
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class Events {
        private Events() {}

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();
            if (player.level().isClientSide) return;

            if (isActive(player)) {
                player.addEffect(new MobEffectInstance(
                        ModEffects.PROJECTILE_DODGE_EFFECT,
                        EFFECT_DURATION_TICKS,
                        0,
                        false,
                        false,
                        false
                ));
            } else if (player.hasEffect(ModEffects.PROJECTILE_DODGE_EFFECT)) {
                player.removeEffect(ModEffects.PROJECTILE_DODGE_EFFECT);
            }
        }

        @SubscribeEvent
        public static void onEntityTick(EntityTickEvent.Post event) {
            if (!(event.getEntity() instanceof LivingEntity entity)) return;
            if (entity instanceof Player) return;
            if (entity.level().isClientSide) return;

            if (isActiveEntity(entity)) {
                entity.addEffect(new MobEffectInstance(
                        ModEffects.PROJECTILE_DODGE_EFFECT,
                        EFFECT_DURATION_TICKS,
                        0,
                        false,
                        false,
                        false
                ));
            } else if (entity.hasEffect(ModEffects.PROJECTILE_DODGE_EFFECT)) {
                entity.removeEffect(ModEffects.PROJECTILE_DODGE_EFFECT);
            }
        }
    }

    @Override
    public void onTick(LivingEntity entity) { }
}