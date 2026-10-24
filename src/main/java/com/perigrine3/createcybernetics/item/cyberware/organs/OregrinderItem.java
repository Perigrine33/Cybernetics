package com.perigrine3.createcybernetics.item.cyberware.organs;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
public class OregrinderItem extends Item implements ICyberwareItem {

    private final int humanityCost;

    private static final String NBT_EATING_METAL = "cc_oregrinder_eating_metal";
    private static final String NBT_EATING_METAL_TICKS = "cc_oregrinder_eating_metal_ticks";
    private static final String NBT_EATING_METAL_HAND = "cc_oregrinder_eating_metal_hand";
    private static final String NBT_ALLOW_REPAIR_HEAL = "cc_oregrinder_allow_repair_heal";

    private static final int METAL_EAT_TICKS = 64;

    private static final float RAW_MATERIALS_HEALING = 2.0F;
    private static final float ORE_BLOCK_HEALING = 4.0F;
    private static final float RAW_ORE_HEALING = 6.0F;
    private static final float INGOT_HEALING = 8.0F;
    private static final float COMPONENT_HEALING = 12.0F;
    private static final float CYBERWARE_HEALING = 16.0F;

    public OregrinderItem(Properties props, int humanityCost) {
        super(props);
        this.humanityCost = humanityCost;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.createcybernetics.humanity", humanityCost)
                    .withStyle(ChatFormatting.GOLD));

            tooltip.add(Component.translatable("tooltip.createcybernetics.organsupgrades_oregrinder.energy")
                    .withStyle(ChatFormatting.DARK_RED));
        }
    }

    @Override
    public int getHumanityCost() {
        return humanityCost;
    }

    @Override
    public Set<CyberwareSlot> getSupportedSlots() {
        return Set.of(CyberwareSlot.ORGANS);
    }

    @Override
    public boolean replacesOrgan() {
        return true;
    }

    @Override
    public Set<CyberwareSlot> getReplacedOrgans() {
        return Set.of(CyberwareSlot.ORGANS);
    }

    @Override
    public TagKey<Item> getReplacedOrganItemTag(ItemStack installedStack, CyberwareSlot slot) {
        return ModTags.Items.INTESTINES_ITEMS;
    }

    @Override
    public Set<TagKey<Item>> incompatibleCyberwareTags(ItemStack installedStack, CyberwareSlot slot) {
        return Set.of(ModTags.Items.WETWARE_ITEM);
    }

    @Override
    public int maxStacksPerSlotType(ItemStack stack, CyberwareSlot slotType) {
        return 3;
    }

    @Override
    public void onInstalled(LivingEntity entity) {

    }

    @Override
    public void onRemoved(LivingEntity entity) {
        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide) return;

        clearMetalEating(player);
    }

    public static boolean hasOregrinderInstalled(PlayerCyberwareData data) {
        if (data == null || data.getAll() == null) {
            return false;
        }

        var profile = com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(null, data);
        if (profile != null) {
            return profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.OREGRINDER);
        }

        InstalledCyberware[] installedArray = data.getAll().get(CyberwareSlot.ORGANS);
        if (installedArray == null) return false;

        for (InstalledCyberware installed : installedArray) {
            if (installed == null) continue;

            ItemStack stack = installed.getItem();
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof OregrinderItem) {
                return true;
            }
        }

        return false;
    }

    private static boolean isInstalled(Player player) {
        if (player == null || !player.hasData(ModAttachments.CYBERWARE)) {
            return false;
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        var profile = com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(player, data);
        return profile != null && profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.OREGRINDER);
    }

    private static void keepHungerFull(Player player) {
        FoodData food = player.getFoodData();

        food.setFoodLevel(20);
        food.setSaturation(0.0F);
        food.setExhaustion(0.0F);
    }

    private static boolean isEatingMetal(Player player) {
        return player.getPersistentData().getBoolean(NBT_EATING_METAL);
    }

    public static boolean isEatingMetalForRender(Player player) {
        return player != null && isEatingMetal(player);
    }

    private static void startEatingMetal(Player player, InteractionHand hand) {
        CompoundTag tag = player.getPersistentData();

        tag.putBoolean(NBT_EATING_METAL, true);
        tag.putInt(NBT_EATING_METAL_TICKS, 0);
        tag.putString(
                NBT_EATING_METAL_HAND,
                hand == InteractionHand.OFF_HAND ? "offhand" : "mainhand"
        );

        player.startUsingItem(hand);

        if (!player.level().isClientSide) {
            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.GENERIC_EAT,
                    SoundSource.PLAYERS,
                    0.50F,
                    0.65F
            );
        }
    }

    private static void clearMetalEating(Player player) {
        InteractionHand hand = metalEatingHand(player);

        if (player.isUsingItem() && player.getUsedItemHand() == hand) {
            player.stopUsingItem();
        }

        CompoundTag tag = player.getPersistentData();

        tag.remove(NBT_EATING_METAL);
        tag.remove(NBT_EATING_METAL_TICKS);
        tag.remove(NBT_EATING_METAL_HAND);
    }

    private static InteractionHand metalEatingHand(Player player) {
        String hand = player.getPersistentData().getString(NBT_EATING_METAL_HAND);

        return "offhand".equals(hand)
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
    }

    public static InteractionHand metalEatingHandForRender(Player player) {
        return metalEatingHand(player);
    }

    private static ItemStack metalEatingStack(Player player) {
        return player.getItemInHand(metalEatingHand(player));
    }

    public static int metalEatingTicksForRender(Player player) {
        if (player == null || !isEatingMetal(player)) {
            return 0;
        }

        return player.getPersistentData().getInt(NBT_EATING_METAL_TICKS);
    }

    public static boolean isOregrinderMaterialForRender(ItemStack stack) {
        return isOregrinderMaterial(stack);
    }

    private static float getHealingAmount(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0F;
        }

        if (stack.is(ModTags.Items.CYBERWARE_ITEM)) {
            return CYBERWARE_HEALING;
        }

        if (stack.is(ModTags.Items.COMPONENT_ITEM)) {
            return COMPONENT_HEALING;
        }

        if (stack.is(Tags.Items.INGOTS)) {
            return INGOT_HEALING;
        }

        if (stack.is(Tags.Items.RAW_MATERIALS)) {
            return RAW_ORE_HEALING;
        }

        if (stack.is(ModTags.Items.CRUSHED_RAW_MATERIALS)) {
            return RAW_ORE_HEALING;
        }

        if (stack.is(Tags.Items.ORES)) {
            return ORE_BLOCK_HEALING;
        }

        return 0.0F;
    }

    private static boolean isOregrinderMaterial(ItemStack stack) {
        return getHealingAmount(stack) > 0.0F;
    }

    private static void repairPlayer(Player player, float amount) {
        if (amount <= 0.0F) {
            return;
        }

        CompoundTag tag = player.getPersistentData();
        tag.putBoolean(NBT_ALLOW_REPAIR_HEAL, true);

        try {
            player.heal(amount);
        } finally {
            tag.remove(NBT_ALLOW_REPAIR_HEAL);
        }
    }

    private static void tickClientMetalEating(Player player) {
        if (!isEatingMetal(player)) {
            return;
        }

        ItemStack stack = metalEatingStack(player);
        if (stack.isEmpty() || !isOregrinderMaterial(stack)) {
            clearMetalEating(player);
            return;
        }

        CompoundTag tag = player.getPersistentData();
        int ticks = tag.getInt(NBT_EATING_METAL_TICKS) + 1;
        tag.putInt(NBT_EATING_METAL_TICKS, ticks);

        if (ticks >= METAL_EAT_TICKS) {
            clearMetalEating(player);
        }
    }

    @SubscribeEvent
    public static void onUseItemStart(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isInstalled(player)) return;

        ItemStack using = event.getItem();
        boolean edible = using.getFoodProperties(player) != null;

        if (edible) {
            event.setCanceled(true);
            player.stopUsingItem();
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (!isInstalled(player)) return;

        ItemStack stack = event.getItemStack();
        boolean edible = stack.getFoodProperties(player) != null;
        boolean oregrinderMaterial = isOregrinderMaterial(stack);

        if (!edible && !oregrinderMaterial) {
            return;
        }

        event.setCanceled(true);

        if (!oregrinderMaterial) {
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }

        if (player.getHealth() >= player.getMaxHealth()) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if (isEatingMetal(player)) {
            event.setCancellationResult(InteractionResult.CONSUME);
            return;
        }

        startEatingMetal(player, event.getHand());

        if (player.level().isClientSide) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        event.setCancellationResult(InteractionResult.CONSUME);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide) {
            tickClientMetalEating(player);
            return;
        }

        if (!isInstalled(player)) {
            if (isEatingMetal(player)) {
                clearMetalEating(player);
            }

            return;
        }

        keepHungerFull(player);

        if (!isEatingMetal(player)) {
            return;
        }

        if (player.getHealth() >= player.getMaxHealth()) {
            clearMetalEating(player);
            return;
        }

        ItemStack stack = metalEatingStack(player);
        if (stack.isEmpty() || !isOregrinderMaterial(stack)) {
            clearMetalEating(player);
            return;
        }

        CompoundTag tag = player.getPersistentData();
        int ticks = tag.getInt(NBT_EATING_METAL_TICKS) + 1;
        tag.putInt(NBT_EATING_METAL_TICKS, ticks);

        if (ticks % 6 == 0) {
            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.GENERIC_EAT,
                    SoundSource.PLAYERS,
                    0.35F,
                    0.55F + player.getRandom().nextFloat() * 0.15F
            );
        }

        if (ticks < METAL_EAT_TICKS) {
            return;
        }

        float healingAmount = getHealingAmount(stack);
        if (healingAmount <= 0.0F) {
            clearMetalEating(player);
            return;
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        repairPlayer(player, healingAmount);
        keepHungerFull(player);

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.PLAYER_BURP,
                SoundSource.PLAYERS,
                0.60F,
                0.75F
        );

        clearMetalEating(player);
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (!isInstalled(player)) return;

        if (player.getPersistentData().getBoolean(NBT_ALLOW_REPAIR_HEAL)) {
            return;
        }

        event.setCanceled(true);
    }
}