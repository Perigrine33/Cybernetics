package com.perigrine3.createcybernetics.item.cyberware.bone;

import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.common.damage.ModDamageSources;
import com.perigrine3.createcybernetics.effect.ModEffects;
import com.perigrine3.createcybernetics.sound.ModSounds;
import com.perigrine3.createcybernetics.util.CyberwareAttributeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Set;

public class SandevistanItem extends Item implements ICyberwareItem {

    private final int humanityCost;
    private final SoundEvent activateSound;

    private static final int ACTIVE_TICKS_TOTAL = 30;
    private static final int COOLDOWN_TICKS_TOTAL = 150 * 20;
    private static final int FORCED_COOLDOWN_TICKS_TOTAL = 3 * 20;

    private static final String TAG_ACTIVE_TICKS = "cc_sandevistan_active";
    private static final String TAG_COOLDOWN_TICKS = "cc_sandevistan_cd";
    private static final String TAG_FORCED_TICKS = "cc_sandevistan_forced_cd";

    private static final float OVERCLOCK_MIN_CHANCE = 0.05f;
    private static final float OVERCLOCK_MAX_CHANCE = 0.60f;
    private static final float OVERCLOCK_DAMAGE = 7.0f;

    public SandevistanItem(Properties props, int humanityCost) {
        super(props);
        this.humanityCost = humanityCost;
        this.activateSound = ModSounds.SANDY_STARTUP.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.createcybernetics.humanity", humanityCost).withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public int getHumanityCost() {
        return humanityCost;
    }

    @Override
    public Set<CyberwareSlot> getSupportedSlots() {
        return Set.of(CyberwareSlot.BONE);
    }

    @Override
    public boolean replacesOrgan() {
        return false;
    }

    @Override
    public Set<CyberwareSlot> getReplacedOrgans() {
        return Set.of();
    }

    public boolean isDyeable(ItemStack stack, CyberwareSlot slot) {
        return slot == CyberwareSlot.BONE;
    }

    @Override
    public boolean isDyeable(ItemStack stack) {
        return true;
    }

    @Override
    public void onInstalled(LivingEntity entity) {
    }

    @Override
    public void onRemoved(LivingEntity entity) {
        if (!(entity instanceof Player player)) return;
        forceStopAndStartCooldown(player);
        removeAll(player);
    }

    @Override
    public void onTick(LivingEntity entity) {
        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        CompoundTag pd = player.getPersistentData();

        int activeTicks = getInt(pd, TAG_ACTIVE_TICKS);
        int cooldownTicks = getInt(pd, TAG_COOLDOWN_TICKS);
        int forcedTicks = getInt(pd, TAG_FORCED_TICKS);

        if (cooldownTicks > 0) {
            cooldownTicks--;
            setInt(pd, TAG_COOLDOWN_TICKS, cooldownTicks);
        }
        if (forcedTicks > 0) {
            forcedTicks--;
            setInt(pd, TAG_FORCED_TICKS, forcedTicks);
        }

        if (!player.hasData(ModAttachments.CYBERWARE)) {
            if (activeTicks > 0) forceStopAndStartCooldown(player);
            removeAll(player);
            return;
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) {
            if (activeTicks > 0) forceStopAndStartCooldown(player);
            removeAll(player);
            return;
        }

        InstalledRef ref = findEnabledRefForThisItem(data);
        if (ref == null) {
            if (activeTicks > 0) forceStopAndStartCooldown(player);
            removeAll(player);
            return;
        }

        if (activeTicks > 0) {
            applyAll(player, activeTicks);

            activeTicks--;
            setInt(pd, TAG_ACTIVE_TICKS, activeTicks);

            if (activeTicks <= 0) {
                setInt(pd, TAG_ACTIVE_TICKS, 0);
                removeAll(player);

                setInt(pd, TAG_FORCED_TICKS, FORCED_COOLDOWN_TICKS_TOTAL);
                setInt(pd, TAG_COOLDOWN_TICKS, COOLDOWN_TICKS_TOTAL);
            }
            return;
        }

        removeAll(player);

        if (!player.isSprinting()) return;
        if (forcedTicks > 0) {
            return;
        }

        if (cooldownTicks > 0) {
            tryOverclockBacklash(player, cooldownTicks);
            setInt(pd, TAG_COOLDOWN_TICKS, COOLDOWN_TICKS_TOTAL);
        }

        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                activateSound,
                SoundSource.PLAYERS,
                5.0F,
                1.0F
        );

        setInt(pd, TAG_ACTIVE_TICKS, ACTIVE_TICKS_TOTAL);
        applyAll(player, ACTIVE_TICKS_TOTAL);
    }

    private void tryOverclockBacklash(Player player, int cooldownTicksRemaining) {
        float progress = Mth.clamp(cooldownTicksRemaining / (float) COOLDOWN_TICKS_TOTAL, 0.0f, 1.0f);
        progress = progress * progress;

        float chance = OVERCLOCK_MIN_CHANCE + (OVERCLOCK_MAX_CHANCE - OVERCLOCK_MIN_CHANCE) * progress;

        if (player.getRandom().nextFloat() < chance) {
            player.hurt(ModDamageSources.davidsDemise(player.level(), player, null), OVERCLOCK_DAMAGE);
        }
    }

    private void applyAll(Player player, int remainingTicks) {
        player.addEffect(new MobEffectInstance(ModEffects.SANDEVISTAN_EFFECT, Math.max(1, remainingTicks), 0, false, false, false));
    }


    private void removeAll(Player player) {
        if (player.level().isClientSide()) return;

        player.removeEffect(ModEffects.SANDEVISTAN_EFFECT);

        CyberwareAttributeHelper.removeModifier(player, "sandevistan_speed");
        CyberwareAttributeHelper.removeModifier(player, "sandevistan_stepheight");
        CyberwareAttributeHelper.removeModifier(player, "sandevistan_jump");
    }

    private void forceStopAndStartCooldown(Player player) {
        if (player.level().isClientSide()) return;

        CompoundTag pd = player.getPersistentData();
        int activeTicks = getInt(pd, TAG_ACTIVE_TICKS);
        if (activeTicks > 0) {
            setInt(pd, TAG_ACTIVE_TICKS, 0);
            removeAll(player);

            setInt(pd, TAG_FORCED_TICKS, FORCED_COOLDOWN_TICKS_TOTAL);

            int curCd = getInt(pd, TAG_COOLDOWN_TICKS);
            setInt(pd, TAG_COOLDOWN_TICKS, Math.max(curCd, COOLDOWN_TICKS_TOTAL));
        }
    }

    private static int getInt(CompoundTag tag, String key) {
        return tag.contains(key, CompoundTag.TAG_INT) ? tag.getInt(key) : 0;
    }

    private static void setInt(CompoundTag tag, String key, int value) {
        tag.putInt(key, Math.max(0, value));
    }

    private InstalledRef findEnabledRefForThisItem(PlayerCyberwareData data) {
        for (var entry : data.getAll().entrySet()) {
            CyberwareSlot slot = entry.getKey();
            var arr = entry.getValue();
            if (arr == null) continue;

            for (int i = 0; i < arr.length; i++) {
                var inst = arr[i];
                if (inst == null) continue;

                ItemStack st = inst.getItem();
                if (st == null || st.isEmpty()) continue;
                if (st.getItem() != this) continue;
                if (!data.isEnabled(slot, i)) continue;

                return new InstalledRef(slot, i);
            }
        }
        return null;
    }

    private record InstalledRef(CyberwareSlot slot, int index) {}
}