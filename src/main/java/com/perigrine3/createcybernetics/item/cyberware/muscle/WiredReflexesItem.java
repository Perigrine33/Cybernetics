package com.perigrine3.createcybernetics.item.cyberware.muscle;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
public class WiredReflexesItem extends Item implements ICyberwareItem {
    private final int humanityCost;

    private static final String NBT_INSTALLED = "cc_wired_reflexes_installed";

    public WiredReflexesItem(Properties props, int humanityCost) {
        super(props);
        this.humanityCost = humanityCost;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.createcybernetics.humanity", humanityCost)
                    .withStyle(ChatFormatting.GOLD));

            tooltip.add(Component.translatable("tooltip.createcybernetics.muscleupgrades_wiredreflexes.energy").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public Map<Item, Integer> getAdditionalAnvilRepairMaterials(ItemStack cyberwareStack) {
        return Map.of(
                ModItems.COMPONENT_SSD.get(), 250,
                ModItems.COMPONENT_SYNTHNERVES.get(), 350,
                ModItems.TITANIUMNUGGET.get(), 100
        );
    }

    @Override
    public int getEnergyUsedPerTick(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        return 3;
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
    public Set<TagKey<Item>> requiresCyberwareTags(ItemStack installedStack, CyberwareSlot slot) {
        return Set.of(ModTags.Items.MUSCLE_ITEMS);
    }

    @Override
    public Set<CyberwareSlot> getSupportedSlots() {
        return Set.of(CyberwareSlot.MUSCLE);
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
        if (entity.level().isClientSide) return;
        entity.getPersistentData().putBoolean(NBT_INSTALLED, true);
    }

    @Override
    public void onRemoved(LivingEntity entity) {
        if (entity.level().isClientSide) return;
        entity.getPersistentData().remove(NBT_INSTALLED);
    }

    @Override
    public void onTick(LivingEntity entity) {
    }

    @SubscribeEvent
    public static void onCyberwareStateChange(com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent event) {
        var player = event.getPlayer();
        if (player.level().isClientSide) return;
        var profile = event.getCurrentProfile();
        if (profile != null && profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.WIRED_REFLEXES)) {
            player.getPersistentData().putBoolean(NBT_INSTALLED, true);
        } else {
            player.getPersistentData().remove(NBT_INSTALLED);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide) return;

        if (!entity.getPersistentData().getBoolean(NBT_INSTALLED)) return;

        DamageSource source = event.getSource();

        Entity attacker = source.getEntity();
        if (attacker == null) attacker = source.getDirectEntity();
        if (attacker == null || attacker == entity) return;

        double px = entity.getX();
        double py = entity.getEyeY();
        double pz = entity.getZ();

        double ax = attacker.getX();
        double ay = attacker.getY() + attacker.getBbHeight() * 0.5D;
        double az = attacker.getZ();

        double dx = ax - px;
        double dy = ay - py;
        double dz = az - pz;

        double horiz = Math.sqrt(dx * dx + dz * dz);
        if (horiz < 1.0e-6) return;

        float yaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        float pitch = (float) (-(Mth.atan2(dy, horiz) * (180.0D / Math.PI)));

        yaw = Mth.wrapDegrees(yaw);
        pitch = Mth.clamp(pitch, -90.0F, 90.0F);

        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.setYBodyRot(yaw);
        entity.setYHeadRot(yaw);

        if (entity instanceof ServerPlayer player) {
            player.connection.send(new ClientboundPlayerLookAtPacket(
                    EntityAnchorArgument.Anchor.EYES,
                    attacker,
                    EntityAnchorArgument.Anchor.EYES
            ));

            byte headByte = (byte) Mth.floor(yaw * 256.0F / 360.0F);
            player.connection.send(new ClientboundRotateHeadPacket(player, headByte));
        }
    }
}