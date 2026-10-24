package com.perigrine3.createcybernetics.common.durability;

import com.perigrine3.createcybernetics.common.scheduling.StaggeredTickScheduler;
import com.perigrine3.createcybernetics.ConfigValues;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class CriticalOrganDamageController {

    private static final String CRITICAL_EYE_BLINDNESS = "cc_critical_eye_blindness";
    private static final String CRITICAL_HEART_WEAKNESS = "cc_critical_heart_weakness";
    private static final String PREVIOUS_SATURATION = "cc_critical_intestine_previous_saturation";

    private static final float EYE_THRESHOLD = 0.5F;
    private static final float HEART_THRESHOLD = 0.25F;
    private static final float LUNG_THRESHOLD = 0.25F;
    private static final float LIVER_THRESHOLD = 0.25F;
    private static final float BONE_THRESHOLD = 0.25F;
    private static final float MUSCLE_THRESHOLD = 0.25F;
    private static final float SKIN_THRESHOLD = 0.1F;

    private static final Map<UUID, Map<Holder<MobEffect>, StoredHarmfulEffect>> LIVER_EFFECTS = new HashMap<>();

    private CriticalOrganDamageController() {}

    public static void onDurabilityDamaged(LivingEntity entity, InstalledCyberware installed, int previousDurability, int currentDurability) {
        if (!ConfigValues.CRITICAL_DURABILITY_DEBUFFS) return;
        if (!(entity instanceof Player player)) return;
        if (installed == null) return;

        ItemStack stack = installed.getItem();
        if (stack == null || stack.isEmpty()) return;

        int maxDurability = installed.getMaxDurability();
        if (maxDurability <= 0) return;

        float previousPercent = (float) previousDurability / (float) maxDurability;
        float currentPercent = (float) currentDurability / (float) maxDurability;
        CyberwareSlot slot = installed.getSlot();
        CompoundTag persistentData = player.getPersistentData();

        if (slot == CyberwareSlot.BRAIN && isOrgan(stack, CyberwareSlot.BRAIN, ModTags.Items.BRAIN_ITEMS)) {
            if (player.getRandom().nextFloat() < 0.25F) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 0));
            }
        }

        if (slot == CyberwareSlot.EYES && isOrgan(stack, CyberwareSlot.EYES, ModTags.Items.EYE_ITEMS)) {
            if (previousPercent >= EYE_THRESHOLD && currentPercent < EYE_THRESHOLD && player.getRandom().nextFloat() < 0.1F) {
                persistentData.putBoolean(CRITICAL_EYE_BLINDNESS, true);
            }
        }

        if (slot == CyberwareSlot.HEART && isOrgan(stack, CyberwareSlot.HEART, ModTags.Items.HEART_ITEMS)) {
            if (previousPercent >= HEART_THRESHOLD && currentPercent < HEART_THRESHOLD && player.getRandom().nextFloat() < 0.25F) {
                persistentData.putBoolean(CRITICAL_HEART_WEAKNESS, true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!StaggeredTickScheduler.shouldRun(player, 20, StaggeredTickScheduler.TickPhase.CRITICAL_ORGANS)) {
            return;
        }

        if (!ConfigValues.CRITICAL_DURABILITY_DEBUFFS) {
            clearCriticalState(player);
            return;
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        handleEyes(player, data);
        handleHeart(player, data);
        handleLungs(player, data);
        handleLiver(player, data);
        handleBonesAndMuscles(player, data);
        handleSkin(player, data);
    }

    @SubscribeEvent
    public static void onFoodUseStarted(LivingEntityUseItemEvent.Start event) {
        if (!ConfigValues.CRITICAL_DURABILITY_DEBUFFS) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (event.getItem().getFoodProperties(player) == null) return;

        player.getPersistentData().putFloat(PREVIOUS_SATURATION, player.getFoodData().getSaturationLevel());
    }

    @SubscribeEvent
    public static void onFoodFinished(LivingEntityUseItemEvent.Finish event) {
        if (!ConfigValues.CRITICAL_DURABILITY_DEBUFFS) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (event.getItem().getFoodProperties(player) == null) return;

        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(PREVIOUS_SATURATION)) return;

        float previousSaturation = persistentData.getFloat(PREVIOUS_SATURATION);
        persistentData.remove(PREVIOUS_SATURATION);

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        InstalledCyberware intestines = findLowestDurabilityOrgan(data, CyberwareSlot.ORGANS, ModTags.Items.INTESTINES_ITEMS);
        if (intestines == null) return;
        if (!CyberwareDurabilityManager.usesDurability(intestines)) return;

        float durabilityPercent = CyberwareDurabilityManager.getEffectiveDurabilityPercent(intestines);
        FoodData foodData = player.getFoodData();
        float currentSaturation = foodData.getSaturationLevel();
        float gainedSaturation = Math.max(0.0F, currentSaturation - previousSaturation);
        float adjustedSaturation = previousSaturation + gainedSaturation * durabilityPercent;

        foodData.setSaturation(Math.min(currentSaturation, adjustedSaturation));
    }

    private static void handleEyes(Player player, PlayerCyberwareData data) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.getBoolean(CRITICAL_EYE_BLINDNESS)) return;

        InstalledCyberware eyes = findLowestDurabilityOrgan(data, CyberwareSlot.EYES, ModTags.Items.EYE_ITEMS);

        if (eyes == null || CyberwareDurabilityManager.getEffectiveDurabilityPercent(eyes) >= EYE_THRESHOLD) {
            persistentData.remove(CRITICAL_EYE_BLINDNESS);
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, true, false, true));
    }

    private static void handleHeart(Player player, PlayerCyberwareData data) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.getBoolean(CRITICAL_HEART_WEAKNESS)) return;

        InstalledCyberware heart = findLowestDurabilityOrgan(data, CyberwareSlot.HEART, ModTags.Items.HEART_ITEMS);

        if (heart == null || CyberwareDurabilityManager.getEffectiveDurabilityPercent(heart) >= HEART_THRESHOLD) {
            persistentData.remove(CRITICAL_HEART_WEAKNESS);
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, true, false, true));
    }

    private static void handleLungs(Player player, PlayerCyberwareData data) {
        InstalledCyberware lungs = findLowestDurabilityOrgan(data, CyberwareSlot.LUNGS, ModTags.Items.LUNGS_ITEMS);
        if (!isBelowThreshold(lungs, LUNG_THRESHOLD)) return;

        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, true, false, true));
    }

    private static void handleLiver(Player player, PlayerCyberwareData data) {
        InstalledCyberware liver = findLowestDurabilityOrgan(data, CyberwareSlot.ORGANS, ModTags.Items.LIVER_ITEMS);

        if (!isBelowThreshold(liver, LIVER_THRESHOLD)) {
            LIVER_EFFECTS.remove(player.getUUID());
            return;
        }

        Map<Holder<MobEffect>, StoredHarmfulEffect> storedEffects = LIVER_EFFECTS.computeIfAbsent(player.getUUID(), uuid -> new HashMap<>());

        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (effect.getEffect().value().getCategory() != MobEffectCategory.HARMFUL) continue;

            storedEffects.put(effect.getEffect(), new StoredHarmfulEffect(effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon()));
        }

        for (var entry : storedEffects.entrySet()) {
            Holder<MobEffect> effect = entry.getKey();
            StoredHarmfulEffect stored = entry.getValue();

            player.addEffect(new MobEffectInstance(effect, 60, stored.amplifier(), stored.ambient(), stored.visible(), stored.showIcon()));
        }
    }

    private static void handleBonesAndMuscles(Player player, PlayerCyberwareData data) {
        InstalledCyberware bones = findLowestDurabilityOrgan(data, CyberwareSlot.BONE, ModTags.Items.BONE_ITEMS);
        InstalledCyberware muscles = findLowestDurabilityOrgan(data, CyberwareSlot.MUSCLE, ModTags.Items.MUSCLE_ITEMS);

        boolean criticalBones = isBelowThreshold(bones, BONE_THRESHOLD);
        boolean criticalMuscles = isBelowThreshold(muscles, MUSCLE_THRESHOLD);

        if (!criticalBones && !criticalMuscles) return;

        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, true, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, true, false, true));
    }

    private static void handleSkin(Player player, PlayerCyberwareData data) {
        if (!StaggeredTickScheduler.shouldRun(player, 1200, StaggeredTickScheduler.TickPhase.CRITICAL_ORGANS)) {
            return;
        }

        InstalledCyberware skin = findLowestDurabilityOrgan(data, CyberwareSlot.SKIN, ModTags.Items.SKIN_ITEMS);
        if (!isBelowThreshold(skin, SKIN_THRESHOLD)) return;
        if (player.getRandom().nextFloat() >= 0.25F) return;

        player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
    }

    private static InstalledCyberware findLowestDurabilityOrgan(PlayerCyberwareData data, CyberwareSlot slot, TagKey<Item> tag) {
        InstalledCyberware[] installedCyberware = data.getAll().get(slot);
        if (installedCyberware == null) return null;

        InstalledCyberware lowest = null;
        float lowestPercent = Float.MAX_VALUE;

        for (InstalledCyberware installed : installedCyberware) {
            if (installed == null) continue;

            ItemStack stack = installed.getItem();
            if (stack == null || stack.isEmpty()) continue;
            if (!isOrgan(stack, slot, tag)) continue;

            float durabilityPercent = CyberwareDurabilityManager.getEffectiveDurabilityPercent(installed);

            if (durabilityPercent < lowestPercent) {
                lowest = installed;
                lowestPercent = durabilityPercent;
            }
        }

        return lowest;
    }

    private static boolean isOrgan(ItemStack stack, CyberwareSlot slot, TagKey<Item> tag) {
        if (stack.is(tag)) return true;
        if (!(stack.getItem() instanceof ICyberwareItem cyberwareItem)) return false;

        return cyberwareItem.replacesOrgan() && cyberwareItem.getReplacedOrgans().contains(slot);
    }

    private static boolean isBelowThreshold(InstalledCyberware installed, float threshold) {
        if (installed == null) return false;
        if (!CyberwareDurabilityManager.usesDurability(installed)) return false;
        if (CyberwareDurabilityManager.isBroken(installed)) return false;

        return CyberwareDurabilityManager.getEffectiveDurabilityPercent(installed) < threshold;
    }

    private static void clearCriticalState(Player player) {
        CompoundTag persistentData = player.getPersistentData();

        persistentData.remove(CRITICAL_EYE_BLINDNESS);
        persistentData.remove(CRITICAL_HEART_WEAKNESS);
        persistentData.remove(PREVIOUS_SATURATION);

        LIVER_EFFECTS.remove(player.getUUID());
    }

    private record StoredHarmfulEffect(int amplifier, boolean ambient, boolean visible, boolean showIcon) {}
}