package com.perigrine3.createcybernetics.item.cyberware.lungs;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
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

public class OxygenTankItem extends Item implements ICyberwareItem {
    private final int humanityCost;

    private static final int ENERGY_PER_TICK_UNDERWATER = 3;

    public OxygenTankItem(Properties props, int humanityCost) {
        super(props);
        this.humanityCost = humanityCost;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.createcybernetics.humanity", humanityCost)
                    .withStyle(ChatFormatting.GOLD));

            tooltip.add(Component.translatable("tooltip.createcybernetics.lungsupgrades_oxygen.energy")
                    .withStyle(ChatFormatting.RED));
        }
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
        return false;
    }

    @Override
    public Set<CyberwareSlot> getReplacedOrgans() {
        return Set.of();
    }

    @Override
    public int getEnergyUsedPerTick(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        return (entity != null && entity.isEyeInFluid(FluidTags.WATER)) ? ENERGY_PER_TICK_UNDERWATER : 0;
    }

    @Override
    public boolean requiresEnergyToFunction(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        return true;
    }

    @Override
    public void onInstalled(LivingEntity entity) {
        if (!entity.level().isClientSide && entity instanceof Player player) {
            AirHandler.resetOxygenTankTracking(player);
        }
    }

    @Override
    public void onRemoved(LivingEntity entity) {
        if (!entity.level().isClientSide && entity instanceof Player player) {
            AirHandler.resetOxygenTankTracking(player);
        }
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class AirHandler {
        private static final String KEY_HAS_PREV = "cc_o2tank_has_prev";
        private static final String KEY_PREV_AIR = "cc_o2tank_prev_air";
        private static final String KEY_DECREMENT_COUNT = "cc_o2tank_dec_count";

        private AirHandler() {}

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();
            if (player.level().isClientSide) return;

            var profile = com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(player);
            if (profile == null || !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.OXYGEN_TANK)) {
                return;
            }

            int poweredTankCount = getPoweredOxygenTankCount(player);
            if (poweredTankCount <= 0) {
                resetOxygenTankTracking(player);
                return;
            }

            if (!player.isUnderWater()) {
                syncTrackingToCurrentAir(player);
                return;
            }

            CompoundTag tag = player.getPersistentData();
            int air = player.getAirSupply();
            int maxAir = player.getMaxAirSupply();

            if (!tag.getBoolean(KEY_HAS_PREV)) {
                tag.putBoolean(KEY_HAS_PREV, true);
                tag.putInt(KEY_PREV_AIR, air);
                tag.putInt(KEY_DECREMENT_COUNT, 0);
                return;
            }

            int prevAir = tag.getInt(KEY_PREV_AIR);
            int decCount = tag.getInt(KEY_DECREMENT_COUNT);

            if (air < prevAir) {
                int delta = prevAir - air;
                int refund = 0;

                for (int i = 0; i < delta; i++) {
                    decCount++;

                    // 1 tank: refund 1 out of every 2 losses
                    // 2 tanks: refund 2 out of every 3 losses
                    // 3 tanks: refund 3 out of every 4 losses
                    if ((decCount % (poweredTankCount + 1)) != 0) {
                        refund++;
                    }
                }

                if (refund > 0) {
                    int newAir = Math.min(maxAir, air + refund);
                    player.setAirSupply(newAir);
                    air = newAir;
                }

                tag.putInt(KEY_DECREMENT_COUNT, decCount);
            } else if (air > prevAir) {
                air = Math.min(air, maxAir);
            }

            tag.putBoolean(KEY_HAS_PREV, true);
            tag.putInt(KEY_PREV_AIR, air);
        }

        public static void resetOxygenTankTracking(Player player) {
            CompoundTag tag = player.getPersistentData();
            tag.remove(KEY_HAS_PREV);
            tag.remove(KEY_PREV_AIR);
            tag.remove(KEY_DECREMENT_COUNT);
        }

        private static void syncTrackingToCurrentAir(Player player) {
            CompoundTag tag = player.getPersistentData();
            tag.putBoolean(KEY_HAS_PREV, true);
            tag.putInt(KEY_PREV_AIR, player.getAirSupply());
            tag.putInt(KEY_DECREMENT_COUNT, 0);
        }

        private static int getPoweredOxygenTankCount(Player player) {
            if (!player.hasData(ModAttachments.CYBERWARE)) return 0;

            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            if (data == null) return 0;

            Item target = ModItems.LUNGSUPGRADES_OXYGEN.get();
            int count = 0;

            for (int i = 0; i < CyberwareSlot.LUNGS.size; i++) {
                InstalledCyberware cw = data.get(CyberwareSlot.LUNGS, i);
                if (cw == null) continue;

                ItemStack stack = cw.getItem();
                if (stack == null || stack.isEmpty()) continue;
                if (stack.getItem() != target) continue;
                if (!cw.isPowered()) continue;

                count++;
            }

            return count;
        }
    }
}