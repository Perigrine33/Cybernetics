package com.perigrine3.createcybernetics.item.cyberware.leg;

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
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class CyberlegItem extends Item implements ICyberwareItem {

    private final int humanityCost;
    private final CyberwareSlot side;

    public CyberlegItem(Properties props, int humanityCost, CyberwareSlot side) {
        super(props);
        this.humanityCost = humanityCost;
        this.side = side;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.createcybernetics.humanity", humanityCost)
                    .withStyle(ChatFormatting.GOLD));

            tooltip.add(Component.translatable("tooltip.basecyberware_cyberleg.energy").withStyle(ChatFormatting.RED));
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
    public CyberwareRepairType getRepairType(ItemStack installedStack, CyberwareSlot slot) {
        return CyberwareRepairType.CYBERLIMB;
    }

    @Override
    public int getMaxCyberwareDurability(ItemStack installedStack, CyberwareSlot slot) {
        return slot == CyberwareSlot.RLEG || slot == CyberwareSlot.LLEG ? 2200 : 2000;
    }

    @Override
    public void onInstalled(LivingEntity entity) {
        boolean hasRight;
        boolean hasLeft;

        if (entity instanceof Player player) {
            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            hasRight = data.hasAnyTagged(ModTags.Items.RIGHT_CYBERLEG, CyberwareSlot.RLEG);
            hasLeft = data.hasAnyTagged(ModTags.Items.LEFT_CYBERLEG, CyberwareSlot.LLEG);
        } else {
            EntityCyberwareData data = entity.getData(ModMobAttachments.CYBERENTITY_CYBERWARE);
            hasRight = data.hasAnyTagged(ModTags.Items.RIGHT_CYBERLEG, CyberwareSlot.RLEG);
            hasLeft = data.hasAnyTagged(ModTags.Items.LEFT_CYBERLEG, CyberwareSlot.LLEG);
        }

        if (hasRight && hasLeft) {
            CyberwareAttributeHelper.applyModifier(entity, "cyberleg_speed1");
            CyberwareAttributeHelper.applyModifier(entity, "cyberleg_jump1");

            CyberwareAttributeHelper.applyModifier(entity, "cyberleg_speed2");
            CyberwareAttributeHelper.applyModifier(entity, "cyberleg_jump2");
        } else if (hasRight || hasLeft) {
            CyberwareAttributeHelper.applyModifier(entity, "cyberleg_speed1");
            CyberwareAttributeHelper.applyModifier(entity, "cyberleg_jump2");

            CyberwareAttributeHelper.removeModifier(entity, "cyberleg_speed2");
            CyberwareAttributeHelper.removeModifier(entity, "cyberleg_jump2");
        }
    }

    @Override
    public void onRemoved(LivingEntity entity) {
        CyberwareAttributeHelper.removeModifier(entity, "cyberleg_speed1");
        CyberwareAttributeHelper.removeModifier(entity, "cyberleg_jump1");

        CyberwareAttributeHelper.removeModifier(entity, "cyberleg_speed2");
        CyberwareAttributeHelper.removeModifier(entity, "cyberleg_jump2");

        onInstalled(entity);
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class PowerFailHooks {
        private PowerFailHooks() {}

        private static final ResourceLocation UNPOWERED_LEG_SPEED =
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "cyberleg_unpowered_speed");
        private static final ResourceLocation UNPOWERED_LEG_JUMP =
                ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "cyberleg_unpowered_jump");

        @SubscribeEvent
        public static void onCyberwareStateChange(com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent event) {
            Player player = event.getPlayer();
            if (player == null || player.level().isClientSide) return;
            var profile = event.getCurrentProfile();
            if (profile == null || !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.CYBERLEG)) {
                setOrClearModifier(player, Attributes.MOVEMENT_SPEED, UNPOWERED_LEG_SPEED, null);
                setOrClearModifier(player, Attributes.JUMP_STRENGTH, UNPOWERED_LEG_JUMP, null);
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();
            if (player.level().isClientSide) return;

            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            if (data == null) return;

            var profile = com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(player, data);
            if (profile != null && !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.CYBERLEG)) {
                return;
            }

            boolean leftDead = isCyberlegUnpowered(player, data, CyberwareSlot.LLEG);
            boolean rightDead = isCyberlegUnpowered(player, data, CyberwareSlot.RLEG);

            int deadCount = (leftDead ? 1 : 0) + (rightDead ? 1 : 0);

            if (deadCount <= 0) {
                setOrClearModifier(player, Attributes.MOVEMENT_SPEED, UNPOWERED_LEG_SPEED, null);
                setOrClearModifier(player, Attributes.JUMP_STRENGTH, UNPOWERED_LEG_JUMP, null);
                return;
            }

            if (deadCount == 1) {
                setOrClearModifier(player, Attributes.MOVEMENT_SPEED, UNPOWERED_LEG_SPEED, -0.5D);
                setOrClearModifier(player, Attributes.JUMP_STRENGTH, UNPOWERED_LEG_JUMP, -0.5D);
                return;
            }

            setOrClearModifier(player, Attributes.MOVEMENT_SPEED, UNPOWERED_LEG_SPEED, -1.0D);
            setOrClearModifier(player, Attributes.JUMP_STRENGTH, UNPOWERED_LEG_JUMP, -0.75D);

            player.setSprinting(false);
            Vec3 v = player.getDeltaMovement();
            if (v.x != 0.0D || v.z != 0.0D) {
                player.setDeltaMovement(0.0D, v.y, 0.0D);
                player.hurtMarked = true;
            }
        }

        private static void setOrClearModifier(Player player, Holder<Attribute> attrHolder, ResourceLocation id, Double amountOrNull) {
            var attr = player.getAttribute(attrHolder);
            if (attr == null) return;

            if (amountOrNull == null) {
                attr.removeModifier(id);
                return;
            }

            var existing = attr.getModifier(id);
            if (existing != null) {
                if (Double.compare(existing.amount(), amountOrNull) == 0
                        && existing.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                    return;
                }
                attr.removeModifier(id);
            }

            attr.addTransientModifier(new AttributeModifier(
                    id,
                    amountOrNull,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }

        private static boolean isCyberlegUnpowered(Player player, PlayerCyberwareData data, CyberwareSlot legSlot) {
            var arr = data.getAll().get(legSlot);
            if (arr == null) return false;

            for (int idx = 0; idx < arr.length; idx++) {
                var installed = arr[idx];
                if (installed == null) continue;

                ItemStack st = installed.getItem();
                if (st == null || st.isEmpty()) continue;

                Item item = st.getItem();
                if (!(item instanceof CyberlegItem cyberleg)) continue;

                if (cyberleg.side != legSlot) continue;
                if (!data.isEnabled(legSlot, idx)) continue;
                if (!cyberleg.requiresEnergyToFunction(player, st, legSlot)) return false;

                boolean powered = readInstalledPowered(installed);
                return !powered;
            }

            return false;
        }

        private static boolean readInstalledPowered(Object installedCyberware) {
            try {
                Method m;

                try {
                    m = installedCyberware.getClass().getMethod("isPowered");
                    Object v = m.invoke(installedCyberware);
                    if (v instanceof Boolean b) return b;
                } catch (NoSuchMethodException ignored) {}

                try {
                    m = installedCyberware.getClass().getMethod("getPowered");
                    Object v = m.invoke(installedCyberware);
                    if (v instanceof Boolean b) return b;
                } catch (NoSuchMethodException ignored) {}

                try {
                    m = installedCyberware.getClass().getMethod("powered");
                    Object v = m.invoke(installedCyberware);
                    if (v instanceof Boolean b) return b;
                } catch (NoSuchMethodException ignored) {}

                return true;
            } catch (Throwable t) {
                return true;
            }
        }
    }
}