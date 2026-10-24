package com.perigrine3.createcybernetics.item.cyberware.arm;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareRepairType;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.common.capabilities.EntityCyberwareData;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.ModMobAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.util.CyberwareAttributeHelper;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CyberarmItem extends Item implements ICyberwareItem {

    private final int humanityCost;
    final CyberwareSlot side;

    public CyberarmItem(Properties props, int humanityCost, CyberwareSlot side) {
        super(props);
        this.humanityCost = humanityCost;
        this.side = side;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.createcybernetics.humanity", humanityCost)
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("tooltip.basecyberware_cyberarm.energy").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public boolean isDyeable(ItemStack stack, CyberwareSlot slot) {
        return slot == this.side;
    }

    @Override
    public boolean isDyeable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnergyUsedPerTick(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        return 10;
    }

    @Override
    public boolean requiresEnergyToFunction(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        return true;
    }

    @Override
    public int getHumanityCost() {
        return humanityCost;
    }

    @Override
    public Set<CyberwareSlot> getSupportedSlots() {
        return Set.of(side);
    }

    @Override
    public boolean replacesOrgan() {
        return true;
    }

    @Override
    public Set<CyberwareSlot> getReplacedOrgans() {
        return Set.of(side);
    }

    @Override
    public Set<TagKey<Item>> sameSlotIncompatibleCyberwareTags(ItemStack installedStack, CyberwareSlot slot) {
        if (slot == CyberwareSlot.LARM) {
            return Set.of(ModTags.Items.LEFTARM_REPLACEMENTS);
        }
        if (slot == CyberwareSlot.RARM) {
            return Set.of(ModTags.Items.RIGHTARM_REPLACEMENTS);
        }
        return Set.of();
    }

    @Override
    public CyberwareRepairType getRepairType(ItemStack installedStack, CyberwareSlot slot) {
        return CyberwareRepairType.CYBERLIMB;
    }

    @Override
    public int getMaxCyberwareDurability(ItemStack installedStack, CyberwareSlot slot) {
        return slot == CyberwareSlot.RARM || slot == CyberwareSlot.LARM ? 2200 : 2000;
    }

    @Override
    public void onInstalled(LivingEntity entity) {
        boolean hasRight;
        boolean hasLeft;

        if (entity instanceof Player player) {
            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            hasRight = data.hasAnyTagged(ModTags.Items.RIGHT_CYBERARM, CyberwareSlot.RARM);
            hasLeft = data.hasAnyTagged(ModTags.Items.LEFT_CYBERARM, CyberwareSlot.LARM);
        } else {
            EntityCyberwareData data = entity.getData(ModMobAttachments.CYBERENTITY_CYBERWARE);
            hasRight = data.hasAnyTagged(ModTags.Items.RIGHT_CYBERARM, CyberwareSlot.RARM);
            hasLeft = data.hasAnyTagged(ModTags.Items.LEFT_CYBERARM, CyberwareSlot.LARM);
        }

        if (hasRight && hasLeft) {
            CyberwareAttributeHelper.applyModifier(entity, "cyberarm_strength1");
            CyberwareAttributeHelper.applyModifier(entity, "cyberarm_blockbreak1");
            CyberwareAttributeHelper.applyModifier(entity, "cyberarm_strength2");
            CyberwareAttributeHelper.applyModifier(entity, "cyberarm_blockbreak2");
        } else if (hasRight || hasLeft) {
            CyberwareAttributeHelper.applyModifier(entity, "cyberarm_strength1");
            CyberwareAttributeHelper.applyModifier(entity, "cyberarm_blockbreak1");
            CyberwareAttributeHelper.removeModifier(entity, "cyberarm_strength2");
            CyberwareAttributeHelper.removeModifier(entity, "cyberarm_blockbreak2");
        }
    }

    @Override
    public void onRemoved(LivingEntity entity) {
        CyberwareAttributeHelper.removeModifier(entity, "cyberarm_strength1");
        CyberwareAttributeHelper.removeModifier(entity, "cyberarm_blockbreak1");
        CyberwareAttributeHelper.removeModifier(entity, "cyberarm_strength2");
        CyberwareAttributeHelper.removeModifier(entity, "cyberarm_blockbreak2");
        onInstalled(entity);
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class PowerFailHooks {
        private PowerFailHooks() {}

        private static final ConcurrentHashMap<Class<?>, Method> POWER_METHOD_CACHE = new ConcurrentHashMap<>();
        private static final ConcurrentHashMap<Class<?>, Field> POWER_FIELD_CACHE = new ConcurrentHashMap<>();

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();
            if (player.level().isClientSide) return;

            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            if (data == null) return;

            var profile = com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(player, data);
            if (profile != null && !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.CYBERARM)) {
                return;
            }

            boolean leftDead = isCyberarmUnpowered(player, data, CyberwareSlot.LARM);
            boolean rightDead = isCyberarmUnpowered(player, data, CyberwareSlot.RARM);

            if (!leftDead && !rightDead) return;

            dropIfHandDisabled(player, leftDead, rightDead, InteractionHand.MAIN_HAND);
            dropIfHandDisabled(player, leftDead, rightDead, InteractionHand.OFF_HAND);

            if (player.isUsingItem()) {
                InteractionHand usingHand = player.getUsedItemHand();
                if (isHandDisabled(player, leftDead, rightDead, usingHand)) {
                    player.stopUsingItem();
                }
            }
        }

        private static void dropIfHandDisabled(Player player, boolean leftDead, boolean rightDead, InteractionHand hand) {
            if (!isHandDisabled(player, leftDead, rightDead, hand)) return;

            ItemStack held = player.getItemInHand(hand);
            if (held.isEmpty()) return;

            ItemStack toDrop = held.copy();
            player.setItemInHand(hand, ItemStack.EMPTY);
            player.drop(toDrop, true);
            player.inventoryMenu.broadcastChanges();
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            Player player = event.getEntity();

            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            if (data == null) return;

            boolean leftDead = isCyberarmUnpowered(player, data, CyberwareSlot.LARM);
            boolean rightDead = isCyberarmUnpowered(player, data, CyberwareSlot.RARM);

            if (!isHandDisabled(player, leftDead, rightDead, event.getHand())) return;

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            Player player = event.getEntity();

            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            if (data == null) return;

            boolean leftDead = isCyberarmUnpowered(player, data, CyberwareSlot.LARM);
            boolean rightDead = isCyberarmUnpowered(player, data, CyberwareSlot.RARM);

            if (!isHandDisabled(player, leftDead, rightDead, event.getHand())) return;

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onUseItemStart(LivingEntityUseItemEvent.Start event) {
            if (!(event.getEntity() instanceof Player player)) return;

            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            if (data == null) return;

            boolean leftDead = isCyberarmUnpowered(player, data, CyberwareSlot.LARM);
            boolean rightDead = isCyberarmUnpowered(player, data, CyberwareSlot.RARM);

            if (!leftDead && !rightDead) return;

            InteractionHand hand = handHolding(player, event.getItem());
            if (hand != null && isHandDisabled(player, leftDead, rightDead, hand)) {
                event.setCanceled(true);
            }
        }

        private static InteractionHand handHolding(Player player, ItemStack stack) {
            if (ItemStack.isSameItemSameComponents(stack, player.getMainHandItem())) return InteractionHand.MAIN_HAND;
            if (ItemStack.isSameItemSameComponents(stack, player.getOffhandItem())) return InteractionHand.OFF_HAND;
            return null;
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onAttack(AttackEntityEvent event) {
            Player player = event.getEntity();

            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            if (data == null) return;

            boolean leftDead = isCyberarmUnpowered(player, data, CyberwareSlot.LARM);
            boolean rightDead = isCyberarmUnpowered(player, data, CyberwareSlot.RARM);

            if (!leftDead && !rightDead) return;

            if (isHandDisabled(player, leftDead, rightDead, InteractionHand.MAIN_HAND)) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
            Player player = event.getEntity();

            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            if (data == null) return;

            boolean leftDead = isCyberarmUnpowered(player, data, CyberwareSlot.LARM);
            boolean rightDead = isCyberarmUnpowered(player, data, CyberwareSlot.RARM);

            if (!isHandDisabled(player, leftDead, rightDead, InteractionHand.MAIN_HAND)) return;

            event.setNewSpeed(0.0F);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onBreakBlock(BlockEvent.BreakEvent event) {
            Player player = event.getPlayer();
            if (player == null) return;

            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            if (data == null) return;

            boolean leftDead = isCyberarmUnpowered(player, data, CyberwareSlot.LARM);
            boolean rightDead = isCyberarmUnpowered(player, data, CyberwareSlot.RARM);

            if (!isHandDisabled(player, leftDead, rightDead, InteractionHand.MAIN_HAND)) return;

            event.setCanceled(true);
        }

        private static boolean isHandDisabled(Player player, boolean leftDead, boolean rightDead, InteractionHand hand) {
            HumanoidArm arm = armForHand(player, hand);
            return (arm == HumanoidArm.LEFT && leftDead) || (arm == HumanoidArm.RIGHT && rightDead);
        }

        private static HumanoidArm armForHand(Player player, InteractionHand hand) {
            HumanoidArm main = player.getMainArm();
            if (hand == InteractionHand.MAIN_HAND) return main;
            return (main == HumanoidArm.RIGHT) ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        }

        private static boolean isCyberarmUnpowered(Player player, PlayerCyberwareData data, CyberwareSlot armSlot) {
            var arr = data.getAll().get(armSlot);
            if (arr == null) return false;

            for (int idx = 0; idx < arr.length; idx++) {
                var installed = arr[idx];
                if (installed == null) continue;

                ItemStack st = installed.getItem();
                if (st == null || st.isEmpty()) continue;

                Item item = st.getItem();
                if (!(item instanceof CyberarmItem cyberarm)) continue;
                if (cyberarm.side != armSlot) continue;
                if (!data.isEnabled(armSlot, idx)) continue;
                if (!cyberarm.requiresEnergyToFunction(player, st, armSlot)) return false;

                return !readInstalledPowered(installed);
            }

            return false;
        }

        private static boolean readInstalledPowered(Object installedCyberware) {
            try {
                Class<?> cls = installedCyberware.getClass();

                Method m = POWER_METHOD_CACHE.computeIfAbsent(cls, c -> {
                    Method found = findBoolMethod(c, "isPowered", "getPowered", "powered");
                    if (found != null) found.setAccessible(true);
                    return found;
                });

                if (m != null) {
                    Object v = m.invoke(installedCyberware);
                    if (v instanceof Boolean b) return b;
                }

                Field f = POWER_FIELD_CACHE.computeIfAbsent(cls, c -> {
                    Field found = findBoolField(c, "powered", "isPowered", "poweredFlag");
                    if (found != null) found.setAccessible(true);
                    return found;
                });

                if (f != null) {
                    return f.getBoolean(installedCyberware);
                }

                return true;
            } catch (Throwable t) {
                return true;
            }
        }

        private static Method findBoolMethod(Class<?> cls, String... names) {
            for (String n : names) {
                try {
                    Method m = cls.getDeclaredMethod(n);
                    if (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class) return m;
                } catch (NoSuchMethodException ignored) {}

                try {
                    Method m = cls.getMethod(n);
                    if (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class) return m;
                } catch (NoSuchMethodException ignored) {}
            }
            return null;
        }

        private static Field findBoolField(Class<?> cls, String... names) {
            for (String n : names) {
                try {
                    Field f = cls.getDeclaredField(n);
                    if (f.getType() == boolean.class || f.getType() == Boolean.class) return f;
                } catch (NoSuchFieldException ignored) {}
            }
            return null;
        }
    }
}