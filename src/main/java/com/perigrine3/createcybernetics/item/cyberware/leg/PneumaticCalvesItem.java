package com.perigrine3.createcybernetics.item.cyberware.leg;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
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
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PneumaticCalvesItem extends Item implements ICyberwareItem {
    private final int humanityCost;

    public PneumaticCalvesItem(Properties props, int humanityCost) {
        super(props);
        this.humanityCost = humanityCost;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.createcybernetics.humanity", humanityCost).withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("tooltip.createcybernetics.legupgrades_jumpboost.energy").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public Map<Item, Integer> getAdditionalAnvilRepairMaterials(ItemStack cyberwareStack) {
        return Map.of(
                Items.PISTON, 400,
                ModItems.COMPONENT_ACTUATOR.get(), 250,
                ModItems.COMPONENT_STORAGE.get(), 250
        );
    }

    @Override
    public int getHumanityCost() {
        return humanityCost;
    }

    @Override
    public Set<TagKey<Item>> requiresCyberwareTags(ItemStack installedStack, CyberwareSlot slot) {
        return switch (slot) {
            case RLEG -> Set.of(ModTags.Items.RIGHTLEG_REPLACEMENTS);
            case LLEG -> Set.of(ModTags.Items.LEFTLEG_REPLACEMENTS);
            default -> Set.of();
        };
    }

    @Override
    public Set<CyberwareSlot> getSupportedSlots() {
        return Set.of(CyberwareSlot.RLEG, CyberwareSlot.LLEG);
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
    public void onInstalled(LivingEntity entity) {
        // no-op
    }

    @Override
    public void onRemoved(LivingEntity entity) {
        if (entity.level().isClientSide) return;

        if (entity instanceof Player player) {
            cleanup(player);
        }
    }

    @Override
    public void onTick(LivingEntity entity) {
        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide) return;
        var existing = player.getEffect(ModEffects.PNEUMATIC_CALVES_EFFECT);
        if (existing != null && existing.getDuration() > 20) return;
        maintainFor(player);
    }

    public static void cleanup(Player player) {
        if (player == null) return;

        CyberwareAttributeHelper.removeModifier(player, "calves_sprint");
        player.removeEffect(ModEffects.PNEUMATIC_CALVES_EFFECT);
    }

    public static boolean hasActiveInstalledPair(Player player) {
        if (player == null) return false;
        if (player.level().isClientSide) return false;
        if (!player.hasData(ModAttachments.CYBERWARE)) return false;

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return false;

        PairState pair = enforcePairRuleAndGetState(player, data);
        return pair.bothInstalled && pair.bothEnabled;
    }

    private static void maintainFor(Player player) {
        if (player == null) return;
        if (player.level().isClientSide) return;
        if (!player.isAlive()) return;
        if (player.isSpectator()) return;

        if (!player.hasData(ModAttachments.CYBERWARE)) return;

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        PairState pair = enforcePairRuleAndGetState(player, data);

        if (!pair.bothInstalled || !pair.bothEnabled) {
            cleanup(player);
            return;
        }

        player.addEffect(new MobEffectInstance(
                ModEffects.PNEUMATIC_CALVES_EFFECT,
                100,
                0,
                false,
                false,
                false
        ));

        if (player.isSprinting()) {
            CyberwareAttributeHelper.applyModifier(player, "calves_sprint");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "calves_sprint");
        }
    }

    private record Found(CyberwareSlot slot, int index) {}

    private record PairState(boolean bothInstalled, boolean bothEnabled) {}

    private static PairState enforcePairRuleAndGetState(Player player, PlayerCyberwareData data) {
        Found first = null;
        Found second = null;

        for (CyberwareSlot slot : new CyberwareSlot[]{CyberwareSlot.RLEG, CyberwareSlot.LLEG}) {
            for (int i = 0; i < slot.size; i++) {
                InstalledCyberware cw = data.get(slot, i);
                if (cw == null) continue;

                ItemStack st = cw.getItem();
                if (st == null || st.isEmpty()) continue;

                if (!(st.getItem() instanceof PneumaticCalvesItem)) continue;

                if (first == null) {
                    first = new Found(slot, i);
                } else if (second == null) {
                    second = new Found(slot, i);
                } else {
                    break;
                }
            }
        }

        if (first == null) {
            return new PairState(false, false);
        }

        if (second == null) {
            forceDisabled(player, data, first.slot(), first.index());
            cleanup(player);
            return new PairState(false, false);
        }

        boolean e1 = data.isEnabled(first.slot(), first.index());
        boolean e2 = data.isEnabled(second.slot(), second.index());

        return new PairState(true, e1 && e2);
    }

    private static void forceDisabled(Player player, PlayerCyberwareData data, CyberwareSlot slot, int index) {
        if (!data.isEnabled(slot, index)) return;

        data.setEnabled(slot, index, false);
        data.setDirty();
        player.syncData(ModAttachments.CYBERWARE);
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class Events {
        private Events() {}

        @SubscribeEvent
        public static void onCyberwareStateChange(com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent event) {
            Player player = event.getPlayer();
            if (player == null || player.level().isClientSide) return;
            var profile = event.getCurrentProfile();
            if (profile == null || !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.PNEUMATIC_CALVES)) {
                cleanup(player);
                return;
            }
            maintainFor(player);
        }

        @SubscribeEvent
        public static void onMovementStateChange(com.perigrine3.createcybernetics.event.custom.CyberwarePlayerMovementStateEvent event) {
            Player player = event.getPlayer();
            if (player == null || player.level().isClientSide) return;
            if (event.getPreviousState().sprinting() == event.getCurrentState().sprinting()) return;
            var profile = com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(player, event.getData());
            if (profile == null || !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.PNEUMATIC_CALVES)) {
                return;
            }
            maintainFor(player);
        }
    }
}