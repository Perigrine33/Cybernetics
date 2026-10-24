package com.perigrine3.createcybernetics.common.organs;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.block.ModBlocks;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.common.damage.ModDamageSources;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class MissingOrganController {

    private MissingOrganController() {}

    private static final String HOLO_SNAPSHOT_FLAG = "cc_holo_snapshot";

    private static final ResourceLocation NO_BONE_SPEED = ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "missing_bone_speed");
    private static final ResourceLocation NO_BONE_JUMP = ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "missing_bone_jump");
    private static final ResourceLocation NO_LEFT_LEG_SPEED = ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "missing_left_leg_speed");
    private static final ResourceLocation NO_RIGHT_LEG_SPEED = ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "missing_right_leg_speed");
    private static final ResourceLocation NO_BOTH_LEGS_JUMP = ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "missing_both_legs_jump");
    private static final String NO_LUNGS_AIR = "cc_no_lungs_air";
    private static final String FORCED_PRONE = "cc_forced_prone";
    private static final ResourceLocation NO_MUSCLE_SPEED = ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "missing_muscle_speed");
    private static final ResourceLocation NO_MUSCLE_ATTACK = ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "missing_muscle_attack");
    private static final ResourceLocation NO_MUSCLE_JUMP = ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "missing_muscle_jump");

    private static final String DEFAULTS_PATCHED = "cc_defaults_patched";
    private static final String FORCED_MAIN_ARM = "cc_forced_main_arm";
    private static final String ORIGINAL_MAIN_ARM = "cc_original_main_arm";
    private static final String PENALTIES_ACTIVE = "cc_missing_organ_penalties_active";


    private static void clearAllMissingOrganPenalties(Player player) {
        applyOrRemoveModifier(player, Attributes.MOVEMENT_SPEED, NO_BONE_SPEED, false, 0);
        applyOrRemoveModifier(player, Attributes.JUMP_STRENGTH, NO_BONE_JUMP, false, 0);
        applyOrRemoveModifier(player, Attributes.MOVEMENT_SPEED, NO_MUSCLE_SPEED, false, 0);
        applyOrRemoveModifier(player, Attributes.ATTACK_SPEED, NO_MUSCLE_ATTACK, false, 0);
        applyOrRemoveModifier(player, Attributes.JUMP_STRENGTH, NO_MUSCLE_JUMP, false, 0);
        applyOrRemoveModifier(player, Attributes.MOVEMENT_SPEED, NO_LEFT_LEG_SPEED, false, 0);
        applyOrRemoveModifier(player, Attributes.MOVEMENT_SPEED, NO_RIGHT_LEG_SPEED, false, 0);
        applyOrRemoveModifier(player, Attributes.JUMP_STRENGTH, NO_BOTH_LEGS_JUMP, false, 0);
        player.getPersistentData().remove(NO_LUNGS_AIR);
        clearProneLike(player);
        updateMainArmForMissingArms(player, true, true);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        if (player.getPersistentData().getBoolean(HOLO_SNAPSHOT_FLAG)) return;

        ensureFakePlayerDefaultOrgans(player);

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile profile =
                com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(player, data);
        if (profile != null && profile.allPrimaryOrgansFunctional() && !profile.hasGills()) {
            if (player.getPersistentData().getBoolean(PENALTIES_ACTIVE)) {
                clearAllMissingOrganPenalties(player);
                player.getPersistentData().remove(PENALTIES_ACTIVE);
            }
            return;
        }
        player.getPersistentData().putBoolean(PENALTIES_ACTIVE, true);


// Resolve organ presence
        boolean hasBrain = hasFunctionalOrgan(data, ModTags.Items.BRAIN_ITEMS, CyberwareSlot.BRAIN);
        boolean hasEyes = hasFunctionalOrgan(data, ModTags.Items.EYE_ITEMS, CyberwareSlot.EYES);
        boolean hasHeart = hasFunctionalOrgan(data, ModTags.Items.HEART_ITEMS, CyberwareSlot.HEART);
        boolean hasLungs = hasFunctionalOrgan(data, ModTags.Items.LUNGS_ITEMS, CyberwareSlot.LUNGS);

        boolean hasLiver = hasFunctionalOrgan(data, ModTags.Items.LIVER_ITEMS, CyberwareSlot.ORGANS);
        boolean hasIntestines = hasFunctionalOrgan(data, ModTags.Items.INTESTINES_ITEMS, CyberwareSlot.ORGANS);

        boolean hasBone = hasFunctionalOrgan(data, ModTags.Items.BONE_ITEMS, CyberwareSlot.BONE);
        boolean hasMuscle = hasFunctionalOrgan(data, ModTags.Items.MUSCLE_ITEMS, CyberwareSlot.MUSCLE);
        boolean hasSkin = hasFunctionalOrgan(data, ModTags.Items.SKIN_ITEMS, CyberwareSlot.SKIN);

        boolean hasLeftArm = hasFunctionalOrgan(data, ModTags.Items.LEFTARM_ITEMS, CyberwareSlot.LARM);
        boolean hasRightArm = hasFunctionalOrgan(data, ModTags.Items.RIGHTARM_ITEMS, CyberwareSlot.RARM);
        boolean hasLeftLeg = hasFunctionalOrgan(data, ModTags.Items.LEFTLEG_ITEMS, CyberwareSlot.LLEG);
        boolean hasRightLeg = hasFunctionalOrgan(data, ModTags.Items.RIGHTLEG_ITEMS, CyberwareSlot.RLEG);

        boolean hasArms = hasLeftArm || hasRightArm;
        boolean hasLegs = hasLeftLeg || hasRightLeg;


        /* -------------------- BRAIN -------------------- */
        if (!hasBrain) {
            player.hurt(ModDamageSources.brainDamage(player.level(), player, null), 500000);
            return;
        }

        /* -------------------- EYES -------------------- */
        if (!hasEyes) {

        }


        /* -------------------- HEART -------------------- */
        if (!hasHeart && player.tickCount % 20 == 0) {
            player.hurt(ModDamageSources.heartAttack(player.level(), player, null), 4);
        }

        /* -------------------- LUNGS -------------------- */
        boolean hasGills = hasFunctionalSpecificItem(data, ModItems.WETWARE_WATERBREATHINGLUNGS.get(), CyberwareSlot.LUNGS);
        boolean inWater = player.isUnderWater() || player.isInWaterOrRain();

        // Cases:
        // 1) Normal lungs only -> breathe air, vanilla underwater drowning applies.
        // 2) Gills only in water -> breathe underwater.
        // 3) Gills only out of water -> suffocate.
        // 4) No lungs and no gills -> suffocate.
        // 5) Normal lungs + gills -> breathe in air and underwater.

        boolean canBreatheAir = hasLungs;
        boolean canBreatheWater = hasGills && inWater;
        boolean canBreatheBoth = canBreatheAir || canBreatheWater;

        if (canBreatheBoth) {
            player.getPersistentData().remove(NO_LUNGS_AIR);

            if (canBreatheWater) {
                player.setAirSupply(player.getMaxAirSupply());
            }
        } else {
            CompoundTag pd = player.getPersistentData();

            int air = pd.contains(NO_LUNGS_AIR, Tag.TAG_INT)
                    ? pd.getInt(NO_LUNGS_AIR)
                    : player.getAirSupply();

            air -= 1;

            if (air <= -20) {
                player.hurt(ModDamageSources.missingLungs(player.level(), player, null), 2);
                air = 0;
            }

            pd.putInt(NO_LUNGS_AIR, air);
            player.setAirSupply(air);
        }

        /* -------------------- LIVER -------------------- */
        if (!hasLiver) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 1, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, true, false));
            player.hurt(ModDamageSources.liverFailure(player.level(), player, null), 6);
        }

        /* -------------------- INTESTINES -------------------- */
        // This is enforced by onItemUse

        /* -------------------- BONE -------------------- */
        if (!hasBone) {
            applyOrRemoveModifier(player, Attributes.MOVEMENT_SPEED, NO_BONE_SPEED, true, -0.85);
            applyOrRemoveModifier(player, Attributes.JUMP_STRENGTH, NO_BONE_JUMP, true, -0.7);

            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 4, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1, true, false));

            player.hurt(ModDamageSources.boneless(player.level(), player, null), 8);

            forceProneLike(player);
        } else {
            applyOrRemoveModifier(player, Attributes.MOVEMENT_SPEED, NO_BONE_SPEED, false, 0);
            applyOrRemoveModifier(player, Attributes.JUMP_STRENGTH, NO_BONE_JUMP, false, 0);

            clearProneLike(player);
        }

        /* -------------------- MUSCLE -------------------- */
        if (!hasMuscle) {
            applyOrRemoveModifier(player, Attributes.MOVEMENT_SPEED, NO_MUSCLE_SPEED, true, -1.0);
            applyOrRemoveModifier(player, Attributes.ATTACK_SPEED, NO_MUSCLE_ATTACK, true, -1.0);
            applyOrRemoveModifier(player, Attributes.JUMP_STRENGTH, NO_MUSCLE_JUMP, true, -1.0);

            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 4, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1, true, false));

            player.hurt(ModDamageSources.missingMuscle(player.level(), player, null), 11);

            forceProneLike(player);
        } else {
            applyOrRemoveModifier(player, Attributes.MOVEMENT_SPEED, NO_MUSCLE_SPEED, false, 0);
            applyOrRemoveModifier(player, Attributes.ATTACK_SPEED, NO_MUSCLE_ATTACK, false, 0);
            applyOrRemoveModifier(player, Attributes.JUMP_STRENGTH, NO_MUSCLE_JUMP, false, 0);

            clearProneLike(player);
        }

        /* -------------------- SKIN -------------------- */
        if (!hasSkin) {
            if (player.horizontalCollision || player.verticalCollision || player.tickCount % 100 == 0) {
                player.hurt(ModDamageSources.missingSkin(player.level(), player, null), 2f);
            }
        }

        /* -------------------- ARMS -------------------- */
        updateMainArmForMissingArms(player, hasLeftArm, hasRightArm);

        if (!player.getAbilities().instabuild) {
            if (!handWorks(player, InteractionHand.MAIN_HAND, hasLeftArm, hasRightArm)) {
                dropHeldItem(player, InteractionHand.MAIN_HAND);
            }

            if (!handWorks(player, InteractionHand.OFF_HAND, hasLeftArm, hasRightArm)) {
                dropHeldItem(player, InteractionHand.OFF_HAND);
            }
        }


        /* -------------------- LEGS -------------------- */
        applyOrRemoveModifier(player, Attributes.MOVEMENT_SPEED, NO_LEFT_LEG_SPEED, !hasLeftLeg, -1.0);
        applyOrRemoveModifier(player, Attributes.MOVEMENT_SPEED, NO_RIGHT_LEG_SPEED, !hasRightLeg, -1.0);

        applyOrRemoveModifier(player, Attributes.JUMP_STRENGTH, NO_BOTH_LEGS_JUMP, (!hasLeftLeg && !hasRightLeg), -0.35);

        if (!hasLegs) {
            player.setSprinting(false);
        }

    }


    /* -------------------- HELPERS -------------------- */

    private static boolean hasFunctionalOrgan(PlayerCyberwareData data, TagKey<Item> tag, CyberwareSlot slot) {
        InstalledCyberware[] arr = data.getAll().get(slot);
        if (arr == null) return false;

        for (InstalledCyberware installed : arr) {
            if (installed == null) continue;
            if (installed.isBroken()) continue;

            ItemStack stack = installed.getItem();
            if (stack == null || stack.isEmpty()) continue;

            if (stack.is(tag)) return true;

            if (stack.getItem() instanceof ICyberwareItem cyberwareItem
                    && cyberwareItem.replacesOrgan()
                    && cyberwareItem.getReplacedOrgans().contains(slot)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasFunctionalSpecificItem(PlayerCyberwareData data, Item item, CyberwareSlot slot) {
        InstalledCyberware[] arr = data.getAll().get(slot);
        if (arr == null) return false;

        for (InstalledCyberware installed : arr) {
            if (installed == null) continue;
            if (installed.isBroken()) continue;

            ItemStack stack = installed.getItem();
            if (stack == null || stack.isEmpty()) continue;

            if (stack.is(item)) return true;
        }

        return false;
    }

    private static void forceProneLike(Player player) {
        CompoundTag pd = player.getPersistentData();
        pd.putBoolean(FORCED_PRONE, true);

        player.setSprinting(false);
        player.setSwimming(true);

        try {
            player.setPose(Pose.SWIMMING);
        } catch (Throwable ignored) {}
    }

    private static void clearProneLike(Player player) {
        CompoundTag pd = player.getPersistentData();
        if (!pd.getBoolean(FORCED_PRONE)) return;

        pd.remove(FORCED_PRONE);
        player.setSwimming(false);
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        ensureFakePlayerDefaultOrgans(player);

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        boolean hasMuscle = hasFunctionalOrgan(data, ModTags.Items.MUSCLE_ITEMS, CyberwareSlot.MUSCLE);
        if (!hasMuscle) {
            event.setNewSpeed(0.0F);
        }
    }

    @SubscribeEvent
    public static void onBreakBlock(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        if (player.level().isClientSide) return;
        if (player instanceof FakePlayer) return;

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        boolean hasMuscle = hasFunctionalOrgan(data, ModTags.Items.MUSCLE_ITEMS, CyberwareSlot.MUSCLE);
        if (!hasMuscle) {
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public static void onItemUse(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ensureFakePlayerDefaultOrgans(player);

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        if (!data.hasAnyTagged(ModTags.Items.INTESTINES_ITEMS, CyberwareSlot.ORGANS)
                && event.getItem().getFoodProperties(player) != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        ensureFakePlayerDefaultOrgans(player);

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        boolean hasMuscle = hasFunctionalOrgan(data, ModTags.Items.MUSCLE_ITEMS, CyberwareSlot.MUSCLE);
        if (!hasMuscle) {
            event.setCanceled(true);
            return;
        }

        boolean hasLeftArm = hasFunctionalOrgan(data, ModTags.Items.LEFTARM_ITEMS, CyberwareSlot.LARM);
        boolean hasRightArm = hasFunctionalOrgan(data, ModTags.Items.RIGHTARM_ITEMS, CyberwareSlot.RARM);

        if (!handWorks(player, InteractionHand.MAIN_HAND, hasLeftArm, hasRightArm)) {
            event.setCanceled(true);
        }
    }

    private static boolean hasAny(PlayerCyberwareData data, CyberwareSlot... slots) {
        for (CyberwareSlot slot : slots) {
            var arr = data.getAll().get(slot);
            if (arr == null) continue;

            for (var installed : arr) {
                if (installed != null && !installed.getItem().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasAnyTagged(PlayerCyberwareData data, TagKey<Item> tag, CyberwareSlot... slots) {
        for (CyberwareSlot slot : slots) {
            var arr = data.getAll().get(slot);
            if (arr == null) continue;

            for (var installed : arr) {
                if (installed == null) continue;
                if (installed.getItem().isEmpty()) continue;
                if (installed.getItem().is(tag)) return true;
            }
        }
        return false;
    }

    private static void dropHeldItem(Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.isEmpty()) {
            return;
        }

        ItemStack droppedStack = heldStack.copy();

        player.setItemInHand(hand, ItemStack.EMPTY);
        player.drop(droppedStack, false);
        player.inventoryMenu.broadcastChanges();
    }


    private static void applyOrRemoveModifier(Player player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, ResourceLocation id, boolean apply, double amount) {
        var attr = player.getAttribute(attribute);
        if (attr == null) return;

        if (apply) {
            if (attr.getModifier(id) == null) {
                attr.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        } else {
            attr.removeModifier(id);
        }
    }

    private static void updateMainArmForMissingArms(Player player, boolean hasLeftArm, boolean hasRightArm) {
        CompoundTag pd = player.getPersistentData();

        if (pd.getBoolean(FORCED_MAIN_ARM)) {
            HumanoidArm originalMainArm = pd.getBoolean(ORIGINAL_MAIN_ARM)
                    ? HumanoidArm.LEFT
                    : HumanoidArm.RIGHT;

            boolean hasOriginalMainArm =
                    (originalMainArm == HumanoidArm.LEFT && hasLeftArm)
                            || (originalMainArm == HumanoidArm.RIGHT && hasRightArm);

            if (hasOriginalMainArm) {
                if (player.getMainArm() != originalMainArm) {
                    player.setMainArm(originalMainArm);
                }

                pd.remove(FORCED_MAIN_ARM);
                pd.remove(ORIGINAL_MAIN_ARM);
                return;
            }
        }

        HumanoidArm currentMainArm = player.getMainArm();

        if (currentMainArm == HumanoidArm.RIGHT && !hasRightArm && hasLeftArm) {
            if (!pd.getBoolean(FORCED_MAIN_ARM)) {
                pd.putBoolean(FORCED_MAIN_ARM, true);
                pd.putBoolean(ORIGINAL_MAIN_ARM, false);
            }

            player.setMainArm(HumanoidArm.LEFT);
            return;
        }

        if (currentMainArm == HumanoidArm.LEFT && !hasLeftArm && hasRightArm) {
            if (!pd.getBoolean(FORCED_MAIN_ARM)) {
                pd.putBoolean(FORCED_MAIN_ARM, true);
                pd.putBoolean(ORIGINAL_MAIN_ARM, true);
            }

            player.setMainArm(HumanoidArm.RIGHT);
        }
    }

    private static boolean handWorks(Player player, InteractionHand hand, boolean hasLeftArm, boolean hasRightArm) {
        HumanoidArm mainArm = player.getMainArm();

        boolean usingRightArm =
                (hand == InteractionHand.MAIN_HAND && mainArm == HumanoidArm.RIGHT)
                        || (hand == InteractionHand.OFF_HAND && mainArm == HumanoidArm.LEFT);

        return usingRightArm ? hasRightArm : hasLeftArm;
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        ensureFakePlayerDefaultOrgans(player);

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        boolean hasMuscle = hasFunctionalOrgan(data, ModTags.Items.MUSCLE_ITEMS, CyberwareSlot.MUSCLE);
        if (!hasMuscle) {
            event.setCanceled(true);
            return;
        }

        boolean hasLeftArm = hasFunctionalOrgan(data, ModTags.Items.LEFTARM_ITEMS, CyberwareSlot.LARM);
        boolean hasRightArm = hasFunctionalOrgan(data, ModTags.Items.RIGHTARM_ITEMS, CyberwareSlot.RARM);

        updateMainArmForMissingArms(player, hasLeftArm, hasRightArm);

        if (!handWorks(player, event.getHand(), hasLeftArm, hasRightArm)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        var clickedState = event.getLevel().getBlockState(event.getPos());

        if (clickedState.is(ModBlocks.SURGERY_TABLE.get())
                || clickedState.is(ModBlocks.ROBOSURGEON.get())
                || clickedState.is(ModBlocks.SURGERY_CHAMBER_TOP.get())
                || clickedState.is(ModBlocks.SURGERY_CHAMBER_BOTTOM.get())) {
            return;
        }

        ensureFakePlayerDefaultOrgans(player);

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        boolean hasMuscle = hasFunctionalOrgan(data, ModTags.Items.MUSCLE_ITEMS, CyberwareSlot.MUSCLE);
        if (!hasMuscle) {
            event.setCanceled(true);
            return;
        }

        boolean hasLeftArm = hasFunctionalOrgan(data, ModTags.Items.LEFTARM_ITEMS, CyberwareSlot.LARM);
        boolean hasRightArm = hasFunctionalOrgan(data, ModTags.Items.RIGHTARM_ITEMS, CyberwareSlot.RARM);

        updateMainArmForMissingArms(player, hasLeftArm, hasRightArm);

        if (!handWorks(player, event.getHand(), hasLeftArm, hasRightArm)) {
            event.setCanceled(true);
        }
    }

    private static boolean ensureFakePlayerDefaultOrgans(Player player) {
        if (!(player instanceof FakePlayer)) {
            return false;
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) {
            return true;
        }

        if (!hasCompleteDefaultOrganBaseline(data)) {
            data.resetToDefaultOrgans();
            data.setDirty();
            player.syncData(ModAttachments.CYBERWARE);
        }

        player.getPersistentData().putBoolean(DEFAULTS_PATCHED, true);
        return true;
    }

    private static boolean hasCompleteDefaultOrganBaseline(PlayerCyberwareData data) {
        return hasAnyTagged(data, ModTags.Items.BRAIN_ITEMS, CyberwareSlot.BRAIN)
                && hasAnyTagged(data, ModTags.Items.EYE_ITEMS, CyberwareSlot.EYES)
                && hasAnyTagged(data, ModTags.Items.HEART_ITEMS, CyberwareSlot.HEART)
                && hasAnyTagged(data, ModTags.Items.LUNGS_ITEMS, CyberwareSlot.LUNGS)
                && hasAnyTagged(data, ModTags.Items.LIVER_ITEMS, CyberwareSlot.ORGANS)
                && hasAnyTagged(data, ModTags.Items.INTESTINES_ITEMS, CyberwareSlot.ORGANS)
                && hasAnyTagged(data, ModTags.Items.BONE_ITEMS, CyberwareSlot.BONE)
                && hasAnyTagged(data, ModTags.Items.MUSCLE_ITEMS, CyberwareSlot.MUSCLE)
                && hasAnyTagged(data, ModTags.Items.SKIN_ITEMS, CyberwareSlot.SKIN)
                && hasAnyTagged(data, ModTags.Items.LEFTARM_ITEMS, CyberwareSlot.LARM)
                && hasAnyTagged(data, ModTags.Items.RIGHTARM_ITEMS, CyberwareSlot.RARM)
                && hasAnyTagged(data, ModTags.Items.LEFTLEG_ITEMS, CyberwareSlot.LLEG)
                && hasAnyTagged(data, ModTags.Items.RIGHTLEG_ITEMS, CyberwareSlot.RLEG);
    }
}