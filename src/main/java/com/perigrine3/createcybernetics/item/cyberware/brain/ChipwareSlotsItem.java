package com.perigrine3.createcybernetics.item.cyberware.brain;

import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.common.mining.MiningRollEngine;
import com.perigrine3.createcybernetics.common.quickhack.QuickhackExecutionContext;
import com.perigrine3.createcybernetics.common.quickhack.QuickhackExecutionResult;
import com.perigrine3.createcybernetics.common.quickhack.QuickhackExecutor;
import com.perigrine3.createcybernetics.common.quickhack.QuickhackProgramData;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.util.CyberwareAttributeHelper;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChipwareSlotsItem extends Item implements ICyberwareItem {
    private final int humanityCost;

    public ChipwareSlotsItem(Properties props, int humanityCost) {
        super(props);
        this.humanityCost = humanityCost;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.createcybernetics.humanity", humanityCost).withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("tooltip.createcybernetics.brainupgrades_chipwareslots.energy").withStyle(ChatFormatting.RED));

        }
    }

    @Override
    public Map<Item, Integer> getAdditionalAnvilRepairMaterials(ItemStack cyberwareStack) {
        return Map.of(
                ModItems.COMPONENT_SYNTHNERVES.get(), 250,
                ModItems.COMPONENT_SSD.get(), 500
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
        return 2;
    }

    @Override
    public void onInstalled(LivingEntity entity) {
        CyberwareAttributeHelper.applyModifier(entity, "chipware_ram");
        CyberwareAttributeHelper.applyModifier(entity, "chipware_ram_regen");
    }

    @Override
    public void onRemoved(LivingEntity entity) {
        CyberwareAttributeHelper.removeModifier(entity, "chipware_ram");
        CyberwareAttributeHelper.removeModifier(entity, "chipware_ram_regen");

        CyberwareAttributeHelper.removeModifier(entity, "redshard_strength");
        CyberwareAttributeHelper.removeModifier(entity, "redshard_speed");
        CyberwareAttributeHelper.removeModifier(entity, "redshard_knockback");
        CyberwareAttributeHelper.removeModifier(entity, "orangeshard_ore");
        CyberwareAttributeHelper.removeModifier(entity, "orangeshard_ore_2");
        CyberwareAttributeHelper.removeModifier(entity, "orangeshard_mining");
        CyberwareAttributeHelper.removeModifier(entity, "orangeshard_mining_2");
        CyberwareAttributeHelper.removeModifier(entity, "yellowshard_haggling");
        CyberwareAttributeHelper.removeModifier(entity, "greenshard_xp");
        CyberwareAttributeHelper.removeModifier(entity, "cyanshard_aim");
        CyberwareAttributeHelper.removeModifier(entity, "blueshard_swim");
        CyberwareAttributeHelper.removeModifier(entity, "blueshard_mining");
        CyberwareAttributeHelper.removeModifier(entity, "blueshard_movement");
        CyberwareAttributeHelper.removeModifier(entity, "blueshard_oxygen");
        CyberwareAttributeHelper.removeModifier(entity, "purpleshard_pearl");
        CyberwareAttributeHelper.removeModifier(entity, "pinkshard_breeding");
        CyberwareAttributeHelper.removeModifier(entity, "brownshard_crops");
        CyberwareAttributeHelper.removeModifier(entity, "grayshard_speed");
        CyberwareAttributeHelper.removeModifier(entity, "grayshard_handling");
        CyberwareAttributeHelper.removeModifier(entity, "grayshard_speed");
        CyberwareAttributeHelper.removeModifier(entity, "grayshard_handling");
        CyberwareAttributeHelper.removeModifier(entity, "blackshard_sprint");
        CyberwareAttributeHelper.removeModifier(entity, "blackshard_crouch");
    }

    @Override
    public void onRemoved(Player player) {
        CyberwareAttributeHelper.removeModifier(player, "redshard_strength");
        CyberwareAttributeHelper.removeModifier(player, "redshard_speed");
        CyberwareAttributeHelper.removeModifier(player, "redshard_knockback");
        CyberwareAttributeHelper.removeModifier(player, "orangeshard_ore");
        CyberwareAttributeHelper.removeModifier(player, "orangeshard_ore_2");
        CyberwareAttributeHelper.removeModifier(player, "orangeshard_mining");
        CyberwareAttributeHelper.removeModifier(player, "orangeshard_mining_2");
        MiningRollEngine.invalidatePlayerSnapshot(player.getUUID());
        CyberwareAttributeHelper.removeModifier(player, "yellowshard_haggling");
        CyberwareAttributeHelper.removeModifier(player, "greenshard_xp");
        CyberwareAttributeHelper.removeModifier(player, "cyanshard_aim");
        CyberwareAttributeHelper.removeModifier(player, "blueshard_swim");
        CyberwareAttributeHelper.removeModifier(player, "blueshard_mining");
        CyberwareAttributeHelper.removeModifier(player, "blueshard_movement");
        CyberwareAttributeHelper.removeModifier(player, "blueshard_oxygen");
        CyberwareAttributeHelper.removeModifier(player, "purpleshard_pearl");
        CyberwareAttributeHelper.removeModifier(player, "pinkshard_breeding");
        CyberwareAttributeHelper.removeModifier(player, "brownshard_crops");
        CyberwareAttributeHelper.removeModifier(player, "grayshard_speed");
        CyberwareAttributeHelper.removeModifier(player, "grayshard_handling");
        CyberwareAttributeHelper.removeModifier(player, "grayshard_speed");
        CyberwareAttributeHelper.removeModifier(player, "grayshard_handling");
        CyberwareAttributeHelper.removeModifier(player, "blackshard_sprint");
        CyberwareAttributeHelper.removeModifier(player, "blackshard_crouch");
    }

    @Override
    public void onTick(LivingEntity entity) {
    }

    public static void syncChipwareEffects(ServerPlayer player, PlayerCyberwareData data) {
        if (player == null || data == null) return;

        com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile profile =
                com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(player, data);
        if (profile != null && !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.CHIPWARE_SLOTS)) {
            return;
        }

        tickSlottedQuickhacks(player, data);

        if (data.hasChipwareShardExact(ModItems.DATA_SHARD_RED.get())) {
            CyberwareAttributeHelper.applyModifier(player, "redshard_strength");
            CyberwareAttributeHelper.applyModifier(player, "redshard_speed");
            CyberwareAttributeHelper.applyModifier(player, "redshard_knockback");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "redshard_strength");
            CyberwareAttributeHelper.removeModifier(player, "redshard_speed");
            CyberwareAttributeHelper.removeModifier(player, "redshard_knockback");
        }

        int orangeShardCount = data.getChipwareShardExactCount(ModItems.DATA_SHARD_ORANGE.get());
        MiningRollEngine.syncOrangeShardAttributes(player, orangeShardCount);
        MiningRollEngine.refreshPlayerSnapshotAsync(player, data);

        if (data.hasChipwareShardExact(ModItems.DATA_SHARD_YELLOW.get())) {
            CyberwareAttributeHelper.applyModifier(player, "yellowshard_haggling");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "yellowshard_haggling");
        }

        if (data.hasChipwareShardExact(ModItems.DATA_SHARD_GREEN.get())) {
            CyberwareAttributeHelper.applyModifier(player, "greenshard_xp");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "greenshard_xp");
        }

        if (data.hasChipwareShardExact(ModItems.DATA_SHARD_CYAN.get())) {
            CyberwareAttributeHelper.applyModifier(player, "cyanshard_aim");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "cyanshard_aim");
        }

        if (data.hasChipwareShardExact(ModItems.DATA_SHARD_BLUE.get())) {
            CyberwareAttributeHelper.applyModifier(player, "blueshard_swim");
            CyberwareAttributeHelper.applyModifier(player, "blueshard_mining");
            CyberwareAttributeHelper.applyModifier(player, "blueshard_movement");
            CyberwareAttributeHelper.applyModifier(player, "blueshard_oxygen");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "blueshard_swim");
            CyberwareAttributeHelper.removeModifier(player, "blueshard_mining");
            CyberwareAttributeHelper.removeModifier(player, "blueshard_movement");
            CyberwareAttributeHelper.removeModifier(player, "blueshard_oxygen");
        }

        if (data.hasChipwareShardExact(ModItems.DATA_SHARD_PURPLE.get())) {
            CyberwareAttributeHelper.applyModifier(player, "purpleshard_pearl");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "purpleshard_pearl");
        }

        if (data.hasChipwareShardExact(ModItems.DATA_SHARD_PINK.get())) {
            CyberwareAttributeHelper.applyModifier(player, "pinkshard_breeding");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "pinkshard_breeding");
        }

        if (data.hasChipwareShardExact(ModItems.DATA_SHARD_BROWN.get())) {
            CyberwareAttributeHelper.applyModifier(player, "brownshard_crops");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "brownshard_crops");
        }

        if (data.hasChipwareShardExact(ModItems.DATA_SHARD_GRAY.get())) {
            CyberwareAttributeHelper.applyModifier(player, "grayshard_speed");
            CyberwareAttributeHelper.applyModifier(player, "grayshard_handling");
        } else {
            CyberwareAttributeHelper.removeModifier(player, "grayshard_speed");
            CyberwareAttributeHelper.removeModifier(player, "grayshard_handling");
        }

        syncBlackShardModifiers(player, data, player.isSprinting());
    }

    private static void syncBlackShardModifiers(ServerPlayer player, PlayerCyberwareData data, boolean sprinting) {
        if (data.hasChipwareShardExact(ModItems.DATA_SHARD_BLACK.get())) {
            if (sprinting) {
                CyberwareAttributeHelper.removeModifier(player, "blackshard_crouch");
                CyberwareAttributeHelper.applyModifier(player, "blackshard_sprint");
            } else {
                CyberwareAttributeHelper.removeModifier(player, "blackshard_sprint");
                CyberwareAttributeHelper.applyModifier(player, "blackshard_crouch");
            }
        } else {
            CyberwareAttributeHelper.removeModifier(player, "blackshard_sprint");
            CyberwareAttributeHelper.removeModifier(player, "blackshard_crouch");
        }
    }

    @net.neoforged.fml.common.EventBusSubscriber(modid = com.perigrine3.createcybernetics.CreateCybernetics.MODID, bus = net.neoforged.fml.common.EventBusSubscriber.Bus.GAME)
    public static final class EventHooks {
        private EventHooks() {}

        @net.neoforged.bus.api.SubscribeEvent
        public static void onCyberwareStateChange(com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent event) {
            if (!(event.getPlayer() instanceof ServerPlayer player)) return;
            syncChipwareEffects(player, event.getData());
        }

        @net.neoforged.bus.api.SubscribeEvent
        public static void onChipwareShardChange(com.perigrine3.createcybernetics.event.custom.CyberwareChipwareShardChangeEvent event) {
            if (!(event.getPlayer() instanceof ServerPlayer player)) return;
            syncChipwareEffects(player, event.getData());
        }

        @net.neoforged.bus.api.SubscribeEvent
        public static void onMovementStateChange(com.perigrine3.createcybernetics.event.custom.CyberwarePlayerMovementStateEvent event) {
            if (!(event.getPlayer() instanceof ServerPlayer player)) return;
            if (event.getPreviousState().sprinting() == event.getCurrentState().sprinting()) return;
            PlayerCyberwareData data = event.getData();
            if (data == null || !data.hasChipwareShardExact(ModItems.DATA_SHARD_BLACK.get())) return;
            syncBlackShardModifiers(player, data, event.getCurrentState().sprinting());
        }
    }

    private static void tickSlottedQuickhacks(ServerPlayer player, PlayerCyberwareData data) {
        for (int slot = 0; slot < PlayerCyberwareData.CHIPWARE_SLOT_COUNT; slot++) {
            ItemStack quickhack = data.getChipwareStack(slot);

            if (quickhack.isEmpty()) continue;
            if (!QuickhackProgramData.hasProgram(quickhack)) continue;
            if (data.hasChipwareQuickhackFired(slot)) continue;

            data.setChipwareQuickhackFired(slot, true);

            QuickhackExecutionContext context = QuickhackExecutionContext.create(player, player, quickhack);
            if (context == null) continue;

            QuickhackExecutionResult result = QuickhackExecutor.execute(context);

            if (!result.success()) {
                if (result.failureMessage() != null) {
                    player.displayClientMessage(result.failureMessage(), false);
                }
                continue;
            }

            for (Component output : result.casterOutput()) {
                player.displayClientMessage(output, false);
            }
        }
    }
}