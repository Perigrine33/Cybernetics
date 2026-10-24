package com.perigrine3.createcybernetics.item.cyberware.arm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.client.model.AttachmentAnchor;
import com.perigrine3.createcybernetics.client.model.PlayerAttachmentManager;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.sound.ModSounds;
import com.perigrine3.createcybernetics.util.CyberwareAttributeHelper;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MantisBladeItem extends Item implements ICyberwareItem {

    private static final int ENERGY_USED_PER_TICK = 15;

    private static final int COPPER_ENERGY_DRAIN_MIN = 1;
    private static final int COPPER_ENERGY_DRAIN_MAX = 5_000;
    private static final int IRON_SHIELD_DISABLE_TICKS = 40;
    private static final int DIAMOND_ARMOR_DAMAGE = 2;

    private static final float GOLD_DAMAGE_MULTIPLIER = 0.75F;
    private static final int GOLD_EXTRA_DROP_ROLLS = 1;

    private static final float NETHERITE_EXECUTION_HEALTH_FRACTION = 0.5F;
    private static final float NETHERITE_EXECUTION_BONUS_DAMAGE = 3.0F;

    public enum Variant {
        IRON("iron"),
        COPPER("copper"),
        TITANIUM("titanium"),
        GOLD("gold"),
        DIAMOND("diamond"),
        NETHERITE("netherite");

        private final String id;

        Variant(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        public String damageModifierKey(int index) {
            return id + "mantis_damage" + index;
        }

        public String speedModifierKey(int index) {
            return id + "mantis_speed" + index;
        }

        public boolean hasLatentAbility() {
            return this != TITANIUM;
        }
    }

    private final Variant variant;
    private final int humanityCost;

    public MantisBladeItem(Properties props, Variant variant, int humanityCost) {
        super(props);
        this.variant = variant;
        this.humanityCost = humanityCost;
    }

    public Variant getVariant() {
        return variant;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
            tooltip.add(Component.translatable("tooltip.createcybernetics.armupgrades_mantisblade.tooltip1"));
            tooltip.add(Component.translatable("tooltip.createcybernetics.armupgrades_mantisblade.tooltip2"));
            tooltip.add(Component.translatable("tooltip.createcybernetics.armupgrades_mantisblade.energy"));

            tooltip.add(Component.translatable("tooltip.createcybernetics.armupgrades_mantisblade_" + variant.id() + ".damage")
                    .withStyle(ChatFormatting.GREEN));

            tooltip.add(Component.translatable("tooltip.createcybernetics.armupgrades_mantisblade_" + variant.id() + ".speed")
                    .withStyle(ChatFormatting.GREEN));

            if (variant.hasLatentAbility()) {
                tooltip.add(Component.translatable("tooltip.createcybernetics.armupgrades_mantisblade_" + variant.id() + ".latent")
                        .withStyle(ChatFormatting.AQUA));
            }

            tooltip.add(Component.translatable("tooltip.createcybernetics.humanity", humanityCost)
                    .withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(Component.translatable("tooltip.createcybernetics.armupgrades_tooltip"));
            tooltip.add(Component.translatable("tooltip.createcybernetics.hold_shift_down"));
        }

        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public int getHumanityCost() {
        return humanityCost;
    }

    @Override
    public Set<CyberwareSlot> getSupportedSlots() {
        return Set.of(CyberwareSlot.LARM, CyberwareSlot.RARM);
    }

    @Override
    public Set<TagKey<Item>> requiresCyberwareTags(ItemStack installedStack, CyberwareSlot slot) {
        return switch (slot) {
            case RARM -> Set.of(ModTags.Items.RIGHTARM_REPLACEMENTS);
            case LARM -> Set.of(ModTags.Items.LEFTARM_REPLACEMENTS);
            default -> Set.of();
        };
    }

    @Override
    public Set<TagKey<Item>> sameSlotIncompatibleCyberwareTags(ItemStack installedStack, CyberwareSlot slot) {
        return Set.of(ModTags.Items.MANTIS_BLADES);
    }

    @Override
    public Set<Item> incompatibleCyberware(ItemStack installedStack, CyberwareSlot slot) {
        return Set.of(
                ModItems.ARMUPGRADES_CLAWS.get(),
                ModItems.ARMUPGRADES_RIPPERCLAW.get(),
                ModItems.ARMUPGRADES_DRILLFIST.get()
        );
    }

    @Override
    public int getEnergyUsedPerTick(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        return isInstalledBladeEnabled(entity, installedStack, slot) ? ENERGY_USED_PER_TICK : 0;
    }

    private static boolean isInstalledBladeEnabled(LivingEntity entity, ItemStack installedStack, CyberwareSlot slot) {
        if (entity == null || installedStack == null || installedStack.isEmpty() || slot == null) {
            return false;
        }

        if (!entity.hasData(ModAttachments.CYBERWARE)) {
            return false;
        }

        PlayerCyberwareData data = entity.getData(ModAttachments.CYBERWARE);

        if (data == null) {
            return false;
        }

        InstalledCyberware[] arr = data.getAll().get(slot);

        if (arr == null) {
            return false;
        }

        for (int i = 0; i < arr.length; i++) {
            InstalledCyberware cyberware = arr[i];

            if (cyberware == null) {
                continue;
            }

            ItemStack stack = cyberware.getItem();

            if (stack == null || stack.isEmpty()) {
                continue;
            }

            if (stack != installedStack && !ItemStack.isSameItemSameComponents(stack, installedStack)) {
                continue;
            }

            return data.isEnabled(slot, i);
        }

        return false;
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
    public void onTick(LivingEntity entity) {
    }

    private static void applyVariantModifiers(Player player, Variant variant, int index) {
        CyberwareAttributeHelper.applyModifier(player, variant.damageModifierKey(index));
        CyberwareAttributeHelper.applyModifier(player, variant.speedModifierKey(index));
    }

    private static void removeVariantModifiers(Player player, Variant variant, int index) {
        CyberwareAttributeHelper.removeModifier(player, variant.damageModifierKey(index));
        CyberwareAttributeHelper.removeModifier(player, variant.speedModifierKey(index));
    }

    private static void removeAllMantisBladeModifiers(Player player) {
        for (Variant variant : Variant.values()) {
            removeVariantModifiers(player, variant, 1);
            removeVariantModifiers(player, variant, 2);
        }
    }

    private static boolean canUseMantisBladeCombat(Player player) {
        if (player == null) {
            return false;
        }

        if (player.level().isClientSide) {
            return false;
        }

        return !isHoldingWeapon(player);
    }

    private static boolean isHoldingWeapon(Player player) {
        if (player == null) {
            return false;
        }

        return isWeaponLike(player.getMainHandItem()) || isWeaponLike(player.getOffhandItem());
    }

    private static boolean isWeaponLike(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        Item item = stack.getItem();

        if (item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem) {
            return true;
        }

        if (item instanceof SwordItem || item instanceof AxeItem || item instanceof MaceItem || item instanceof DiggerItem) {
            return true;
        }

        ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);

        if (modifiers == null) {
            return false;
        }

        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            Holder<Attribute> attribute = entry.attribute();

            boolean attackDamage = attribute != null && attribute.value() == Attributes.ATTACK_DAMAGE.value();

            boolean mainhand = entry.slot() == EquipmentSlotGroup.MAINHAND
                    || entry.slot().test(EquipmentSlot.MAINHAND);

            if (attackDamage && mainhand && entry.modifier().amount() != 0.0D) {
                return true;
            }
        }

        return false;
    }

    private static EnumSet<Variant> getEnabledBladeVariants(Player player) {
        EnumSet<Variant> variants = EnumSet.noneOf(Variant.class);

        if (player == null || !player.hasData(ModAttachments.CYBERWARE)) {
            return variants;
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);

        if (data == null) {
            return variants;
        }

        addEnabledBladeVariantsInSlot(data, CyberwareSlot.LARM, variants);
        addEnabledBladeVariantsInSlot(data, CyberwareSlot.RARM, variants);

        return variants;
    }

    private static void addEnabledBladeVariantsInSlot(PlayerCyberwareData data, CyberwareSlot slot, EnumSet<Variant> variants) {
        InstalledCyberware[] arr = data.getAll().get(slot);

        if (arr == null) {
            return;
        }

        for (int i = 0; i < arr.length; i++) {
            InstalledCyberware cyberware = arr[i];

            if (cyberware == null) {
                continue;
            }

            ItemStack stack = cyberware.getItem();

            if (stack == null || stack.isEmpty()) {
                continue;
            }

            if (!(stack.getItem() instanceof MantisBladeItem blade)) {
                continue;
            }

            if (!data.isEnabled(slot, i)) {
                continue;
            }

            variants.add(blade.getVariant());
        }
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class ServerHandler {

        private static final Map<UUID, Boolean> LAST_LEFT = new HashMap<>();
        private static final Map<UUID, Boolean> LAST_RIGHT = new HashMap<>();

        @SubscribeEvent
        public static void onCyberwareStateChange(com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent event) {
            syncMantisBlades(event.getPlayer(), event.getData(), event.getCurrentProfile());
        }

        @SubscribeEvent
        public static void onPowerStateChange(com.perigrine3.createcybernetics.event.custom.CyberwarePowerStateChangeEvent event) {
            if (event.getSlot() != CyberwareSlot.LARM && event.getSlot() != CyberwareSlot.RARM) return;
            Player player = event.getPlayer();
            PlayerCyberwareData data = event.getData();
            syncMantisBlades(player, data, com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(player, data));
        }

        @SubscribeEvent
        public static void onEquipmentChange(net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent event) {
            if (!(event.getEntity() instanceof Player player)) return;
            if (player.level().isClientSide) return;
            if (event.getSlot() != net.minecraft.world.entity.EquipmentSlot.MAINHAND
                    && event.getSlot() != net.minecraft.world.entity.EquipmentSlot.OFFHAND) {
                return;
            }
            if (!player.hasData(ModAttachments.CYBERWARE)) return;
            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
            var profile = com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(player, data);
            if (profile == null || !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.MANTIS_BLADES)) {
                return;
            }
            syncMantisBlades(player, data, profile);
        }

        private static void syncMantisBlades(
                Player player,
                PlayerCyberwareData data,
                com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile profile
        ) {
            if (player == null || player.level().isClientSide) {
                return;
            }

            if (data == null || (profile != null && !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.MANTIS_BLADES))) {
                removeAllMantisBladeModifiers(player);
                clearTrackedState(player);
                return;
            }

            EnabledBlade leftBlade = getEnabledBladeInSlot(data, CyberwareSlot.LARM);
            EnabledBlade rightBlade = getEnabledBladeInSlot(data, CyberwareSlot.RARM);

            boolean leftEnabled = leftBlade != null;
            boolean rightEnabled = rightBlade != null;

            updateToggleSound(player, leftEnabled, rightEnabled);

            removeAllMantisBladeModifiers(player);

            boolean anyEnabled = leftEnabled || rightEnabled;
            boolean weaponEquipped = isHoldingWeapon(player);

            if (!anyEnabled || weaponEquipped) {
                return;
            }

            if (rightBlade != null && leftBlade != null) {
                applyVariantModifiers(player, rightBlade.variant(), 1);
                applyVariantModifiers(player, leftBlade.variant(), 2);
            } else if (rightBlade != null) {
                applyVariantModifiers(player, rightBlade.variant(), 1);
            } else if (leftBlade != null) {
                applyVariantModifiers(player, leftBlade.variant(), 1);
            }
        }

        private static void updateToggleSound(Player player, boolean leftEnabled, boolean rightEnabled) {
            UUID id = player.getUUID();

            boolean previousLeft = LAST_LEFT.getOrDefault(id, false);

            if (leftEnabled != previousLeft) {
                LAST_LEFT.put(id, leftEnabled);
                playToggleSound(player, leftEnabled);
            }

            boolean previousRight = LAST_RIGHT.getOrDefault(id, false);

            if (rightEnabled != previousRight) {
                LAST_RIGHT.put(id, rightEnabled);
                playToggleSound(player, rightEnabled);
            }
        }

        private static void clearTrackedState(Player player) {
            if (player == null) {
                return;
            }

            UUID id = player.getUUID();
            LAST_LEFT.remove(id);
            LAST_RIGHT.remove(id);
        }

        @SubscribeEvent
        public static void onIncomingDamage(LivingIncomingDamageEvent event) {
            LivingEntity target = event.getEntity();

            if (target == null || target.level().isClientSide) {
                return;
            }

            DamageSource source = event.getSource();

            if (!(source.getEntity() instanceof Player attacker)) {
                return;
            }

            if (!canUseMantisBladeCombat(attacker)) {
                return;
            }

            EnumSet<Variant> variants = getEnabledBladeVariants(attacker);

            if (variants.isEmpty()) {
                return;
            }

            float amount = event.getAmount();

            if (variants.contains(Variant.GOLD)) {
                amount *= GOLD_DAMAGE_MULTIPLIER;
            }

            if (variants.contains(Variant.NETHERITE)
                    && target.getHealth() <= target.getMaxHealth() * NETHERITE_EXECUTION_HEALTH_FRACTION) {
                amount += NETHERITE_EXECUTION_BONUS_DAMAGE;
            }

            event.setAmount(amount);
        }

        @SubscribeEvent
        public static void onLivingDamaged(LivingDamageEvent.Post event) {
            LivingEntity target = event.getEntity();

            if (target == null || target.level().isClientSide) {
                return;
            }

            DamageSource source = event.getSource();

            if (!(source.getEntity() instanceof Player attacker)) {
                return;
            }

            if (!canUseMantisBladeCombat(attacker)) {
                return;
            }

            EnumSet<Variant> variants = getEnabledBladeVariants(attacker);

            if (variants.isEmpty()) {
                return;
            }

            if (variants.contains(Variant.IRON)) {
                disableShieldIfBlocking(target);
            }

            if (variants.contains(Variant.COPPER)) {
                drainTargetEnergy(attacker, target);
            }

            if (variants.contains(Variant.DIAMOND)) {
                damageTargetArmor(target, DIAMOND_ARMOR_DAMAGE);
            }
        }

        @SubscribeEvent
        public static void onLivingDrops(LivingDropsEvent event) {
            LivingEntity target = event.getEntity();

            if (target == null || target.level().isClientSide) {
                return;
            }

            DamageSource source = event.getSource();

            if (!(source.getEntity() instanceof Player attacker)) {
                return;
            }

            if (!canUseMantisBladeCombat(attacker)) {
                return;
            }

            EnumSet<Variant> variants = getEnabledBladeVariants(attacker);

            if (!variants.contains(Variant.GOLD)) {
                return;
            }

            duplicateDropsForGold(target, event.getDrops());
        }

        private static void duplicateDropsForGold(LivingEntity target, Collection<ItemEntity> drops) {
            if (target == null || drops == null || drops.isEmpty()) {
                return;
            }

            java.util.List<ItemEntity> extraDrops = new java.util.ArrayList<>();

            for (ItemEntity drop : drops) {
                if (drop == null) {
                    continue;
                }

                ItemStack original = drop.getItem();

                if (original == null || original.isEmpty()) {
                    continue;
                }

                ItemStack copy = original.copy();
                copy.setCount(Math.max(1, Math.min(original.getCount(), GOLD_EXTRA_DROP_ROLLS)));

                ItemEntity extra = new ItemEntity(
                        target.level(),
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        copy
                );

                extra.setPickUpDelay(10);
                extraDrops.add(extra);
            }

            drops.addAll(extraDrops);
        }

        private static void disableShieldIfBlocking(LivingEntity target) {
            if (!(target instanceof Player playerTarget)) {
                return;
            }

            if (!playerTarget.isBlocking()) {
                return;
            }

            ItemStack useStack = playerTarget.getUseItem();

            if (useStack != null && !useStack.isEmpty() && useStack.is(Items.SHIELD)) {
                playerTarget.getCooldowns().addCooldown(useStack.getItem(), IRON_SHIELD_DISABLE_TICKS);
                playerTarget.stopUsingItem();
                return;
            }

            ItemStack main = playerTarget.getMainHandItem();

            if (main != null && !main.isEmpty() && main.is(Items.SHIELD)) {
                playerTarget.getCooldowns().addCooldown(main.getItem(), IRON_SHIELD_DISABLE_TICKS);
                playerTarget.stopUsingItem();
                return;
            }

            ItemStack off = playerTarget.getOffhandItem();

            if (off != null && !off.isEmpty() && off.is(Items.SHIELD)) {
                playerTarget.getCooldowns().addCooldown(off.getItem(), IRON_SHIELD_DISABLE_TICKS);
                playerTarget.stopUsingItem();
            }
        }

        private static void drainTargetEnergy(Player attacker, LivingEntity target) {
            if (attacker == null || target == null) {
                return;
            }

            if (!(target instanceof Player playerTarget)) {
                return;
            }

            if (!playerTarget.hasData(ModAttachments.CYBERWARE)) {
                return;
            }

            if (!attacker.hasData(ModAttachments.CYBERWARE)) {
                return;
            }

            PlayerCyberwareData targetData = playerTarget.getData(ModAttachments.CYBERWARE);
            PlayerCyberwareData attackerData = attacker.getData(ModAttachments.CYBERWARE);

            if (targetData == null || attackerData == null) {
                return;
            }

            int rolledDrain = COPPER_ENERGY_DRAIN_MIN
                    + attacker.getRandom().nextInt(COPPER_ENERGY_DRAIN_MAX - COPPER_ENERGY_DRAIN_MIN + 1);

            int drained = drainUpTo(targetData, rolledDrain);

            if (drained <= 0) {
                return;
            }

            attackerData.receiveEnergy(attacker, drained);
            attackerData.clampEnergyToCapacity(attacker);

            targetData.setDirty();
            attackerData.setDirty();
        }

        private static int drainUpTo(PlayerCyberwareData targetData, int maxAmount) {
            if (targetData == null || maxAmount <= 0) {
                return 0;
            }

            int drained = 0;
            int remaining = maxAmount;
            int step = Integer.highestOneBit(maxAmount);

            while (step > 0 && remaining > 0) {
                if (step <= remaining && targetData.tryConsumeEnergy(step)) {
                    drained += step;
                    remaining -= step;
                } else {
                    step >>= 1;
                }
            }

            return drained;
        }

        private static void damageTargetArmor(LivingEntity target, int amount) {
            if (target == null || amount <= 0) {
                return;
            }

            damageArmorSlot(target, EquipmentSlot.HEAD, amount);
            damageArmorSlot(target, EquipmentSlot.CHEST, amount);
            damageArmorSlot(target, EquipmentSlot.LEGS, amount);
            damageArmorSlot(target, EquipmentSlot.FEET, amount);
        }

        private static void damageArmorSlot(LivingEntity target, EquipmentSlot slot, int amount) {
            ItemStack stack = target.getItemBySlot(slot);

            if (stack == null || stack.isEmpty()) {
                return;
            }

            stack.hurtAndBreak(amount, target, slot);
        }

        private static EnabledBlade getEnabledBladeInSlot(PlayerCyberwareData data, CyberwareSlot slot) {
            InstalledCyberware[] arr = data.getAll().get(slot);

            if (arr == null) {
                return null;
            }

            for (int i = 0; i < arr.length; i++) {
                InstalledCyberware cyberware = arr[i];

                if (cyberware == null) {
                    continue;
                }

                ItemStack stack = cyberware.getItem();

                if (stack == null || stack.isEmpty()) {
                    continue;
                }

                if (!(stack.getItem() instanceof MantisBladeItem blade)) {
                    continue;
                }

                if (!data.isEnabled(slot, i)) {
                    continue;
                }

                return new EnabledBlade(blade.getVariant());
            }

            return null;
        }

        private static void playToggleSound(Player player, boolean enabledNow) {
            SoundEvent sound = mantisBladeToggleSound();

            if (sound == null) {
                return;
            }

            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    sound,
                    SoundSource.PLAYERS,
                    0.9F,
                    enabledNow ? 1.15F : 0.95F
            );
        }

        private static SoundEvent mantisBladeToggleSound() {
            return ModSounds.MANTIS_OPEN.get();
        }

        @SubscribeEvent
        public static void onLogout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
            clearTrackedState(event.getEntity());
        }

        private record EnabledBlade(Variant variant) {
        }

        private ServerHandler() {}
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static final class ClientFirstPerson {

        private static final float RIGHT_X = -0.05F;
        private static final float RIGHT_Y = 0F;
        private static final float RIGHT_Z = 0F;

        private static final float LEFT_X = 0.05F;
        private static final float LEFT_Y = 0F;
        private static final float LEFT_Z = 0F;

        private static final float RIGHT_X_ROT = 0.0F;
        private static final float RIGHT_Y_ROT = 0.0F;
        private static final float RIGHT_Z_ROT = 0.0F;

        private static final float LEFT_X_ROT = 0.0F;
        private static final float LEFT_Y_ROT = 0.0F;
        private static final float LEFT_Z_ROT = 0.0F;

        private static final float SCALE_X = 1.0F;
        private static final float SCALE_Y = 1.0F;
        private static final float SCALE_Z = 1.0F;

        @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
        public static void onRenderArm(RenderArmEvent event) {
            AbstractClientPlayer player = event.getPlayer();

            if (player == null) {
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();
            Player viewer = minecraft.player;

            if (viewer != null) {
                if (player.isInvisibleTo(viewer)) {
                    return;
                }
            } else if (player.isInvisible()) {
                return;
            }

            if (minecraft.player == null || !player.getUUID().equals(minecraft.player.getUUID())) {
                return;
            }

            if (!player.hasData(ModAttachments.CYBERWARE)) {
                return;
            }

            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);

            if (data == null) {
                return;
            }

            HumanoidArm arm = event.getArm();
            CyberwareSlot slot = arm == HumanoidArm.LEFT ? CyberwareSlot.LARM : CyberwareSlot.RARM;

            MantisBladeRenderState renderState = getMantisBladeRenderStateInSlot(data, slot);

            if (renderState == null) {
                return;
            }

            PoseStack pose = event.getPoseStack();
            MultiBufferSource buffers = event.getMultiBufferSource();
            int light = event.getPackedLight();

            var model = PlayerAttachmentManager.mantisBladeModel();
            var texture = PlayerAttachmentManager.mantisBladeTextureFor(renderState.stack(), renderState.enabled());

            model.setBladeVisible(renderState.enabled());

            pose.pushPose();

            try {
                AttachmentAnchor anchor = arm == HumanoidArm.LEFT
                        ? AttachmentAnchor.LEFT_ARM
                        : AttachmentAnchor.RIGHT_ARM;

                applyFirstPersonMantisBladeTransform(pose, anchor);

                var vertexConsumer = buffers.getBuffer(model.renderType(texture));
                model.renderToBuffer(pose, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            } finally {
                model.setBladeVisible(true);
                pose.popPose();
            }
        }

        private static void applyFirstPersonMantisBladeTransform(PoseStack pose, AttachmentAnchor armAnchor) {
            pose.scale(SCALE_X, SCALE_Y, SCALE_Z);

            if (armAnchor == AttachmentAnchor.LEFT_ARM) {
                pose.translate(LEFT_X, LEFT_Y, LEFT_Z);
                pose.mulPose(Axis.XP.rotationDegrees(LEFT_X_ROT));
                pose.mulPose(Axis.YP.rotationDegrees(LEFT_Y_ROT));
                pose.mulPose(Axis.ZP.rotationDegrees(LEFT_Z_ROT));
                pose.scale(-1.0F, 1.0F, 1.0F);
            } else if (armAnchor == AttachmentAnchor.RIGHT_ARM) {
                pose.translate(RIGHT_X, RIGHT_Y, RIGHT_Z);
                pose.mulPose(Axis.XP.rotationDegrees(RIGHT_X_ROT));
                pose.mulPose(Axis.YP.rotationDegrees(RIGHT_Y_ROT));
                pose.mulPose(Axis.ZP.rotationDegrees(RIGHT_Z_ROT));
            }
        }

        private ClientFirstPerson() {}
    }

    private static MantisBladeRenderState getMantisBladeRenderStateInSlot(PlayerCyberwareData data, CyberwareSlot slot) {
        InstalledCyberware[] arr = data.getAll().get(slot);

        if (arr == null) {
            return null;
        }

        for (int i = 0; i < arr.length; i++) {
            InstalledCyberware cyberware = arr[i];

            if (cyberware == null) {
                continue;
            }

            ItemStack stack = cyberware.getItem();

            if (stack == null || stack.isEmpty()) {
                continue;
            }

            if (!(stack.getItem() instanceof MantisBladeItem)) {
                continue;
            }

            return new MantisBladeRenderState(stack, data.isEnabled(slot, i));
        }

        return null;
    }

    private record MantisBladeRenderState(ItemStack stack, boolean enabled) {
    }
}