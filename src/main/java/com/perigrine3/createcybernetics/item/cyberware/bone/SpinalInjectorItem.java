package com.perigrine3.createcybernetics.item.cyberware.bone;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.ISpinalInjectableItem;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.effect.ModEffects;
import com.perigrine3.createcybernetics.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Set;

public class SpinalInjectorItem extends Item implements ICyberwareItem {

    public static final int SLOT_COUNT = 4;
    public static final int STACK_FACTOR = 4;

    private static final String ENTRY_ITEM  = "item";
    private static final String ENTRY_COUNT = "count";

    private static final String STACK_ROOT = "cc_spinal_injector";
    private static final String STACK_INV = "inv";

    private static final int ONE_MINECRAFT_DAY_TICKS = 24000;

    private final int humanityCost;

    public SpinalInjectorItem(Properties props, int humanityCost) {
        super(props);
        this.humanityCost = humanityCost;
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

    @Override
    public Set<Item> requiresCyberware(ItemStack installedStack, CyberwareSlot slot) {
        return Set.of(ModItems.BASECYBERWARE_LINEARFRAME.get());
    }

    public static boolean isInjectable(ItemStack stack) {
        return !stack.isEmpty() && stack.is(Tags.Items.POTIONS);
    }

    public static int maxStackFor(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        int base = Math.max(1, stack.getMaxStackSize());
        return Math.min(64, base * STACK_FACTOR);
    }

    /* ---------------- STACK STORAGE (CUSTOM_DATA) ---------------- */

    private static CompoundTag getOrCreateRoot(ItemStack injectorStack) {
        CustomData cd = injectorStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag all = cd.copyTag();

        if (!all.contains(STACK_ROOT, Tag.TAG_COMPOUND)) {
            all.put(STACK_ROOT, new CompoundTag());
        }

        return all;
    }

    private static CompoundTag getRootView(ItemStack injectorStack) {
        CustomData cd = injectorStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag all = cd.copyTag();
        return all.contains(STACK_ROOT, Tag.TAG_COMPOUND) ? all.getCompound(STACK_ROOT) : new CompoundTag();
    }

    private static void writeBack(ItemStack injectorStack, CompoundTag all) {
        injectorStack.set(DataComponents.CUSTOM_DATA, CustomData.of(all));
    }

    public static void loadFromInstalledStack(ItemStack injectorStack, HolderLookup.Provider provider, Container intoInv, int[] counts) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            intoInv.setItem(i, ItemStack.EMPTY);
            if (counts != null && i < counts.length) counts[i] = 0;
        }

        if (injectorStack == null || injectorStack.isEmpty()) return;

        CompoundTag root = getRootView(injectorStack);
        if (!root.contains(STACK_INV, Tag.TAG_LIST)) return;

        ListTag list = root.getList(STACK_INV, Tag.TAG_COMPOUND);

        for (int i = 0; i < SLOT_COUNT; i++) {
            if (i >= list.size()) break;

            CompoundTag entry = list.getCompound(i);

            if (entry.contains(ENTRY_ITEM, Tag.TAG_COMPOUND)) {
                ItemStack base = ItemStack.parseOptional(provider, entry.getCompound(ENTRY_ITEM));
                if (base.isEmpty() || !isInjectable(base)) continue;

                int cap = maxStackFor(base);
                int c = entry.contains(ENTRY_COUNT, Tag.TAG_INT) ? entry.getInt(ENTRY_COUNT) : 0;
                c = Math.max(0, Math.min(cap, c));

                if (c <= 0) continue;

                base.setCount(c);
                intoInv.setItem(i, base);

                if (counts != null && i < counts.length) {
                    counts[i] = c;
                }

                continue;
            }

            ItemStack legacy = ItemStack.parseOptional(provider, entry);
            if (legacy.isEmpty() || !isInjectable(legacy)) continue;

            int cap = maxStackFor(legacy);
            int c = legacy.getCount();
            c = Math.max(0, Math.min(cap, c));

            if (c <= 0) continue;

            legacy.setCount(c);
            intoInv.setItem(i, legacy);

            if (counts != null && i < counts.length) {
                counts[i] = c;
            }
        }
    }

    public static void saveIntoInstalledStack(ItemStack injectorStack, HolderLookup.Provider provider, Container fromInv, int[] counts) {
        if (injectorStack == null || injectorStack.isEmpty()) return;

        ListTag list = new ListTag();

        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack base = fromInv.getItem(i);

            if (!base.isEmpty() && isInjectable(base)) {
                int cap = maxStackFor(base);
                int c = Math.max(0, Math.min(cap, base.getCount()));

                if (c > 0) {
                    ItemStack rep = base.copy();
                    rep.setCount(1);

                    CompoundTag entry = new CompoundTag();
                    entry.put(ENTRY_ITEM, rep.save(provider));
                    entry.putInt(ENTRY_COUNT, c);

                    list.add(entry);
                    continue;
                }
            }

            list.add(new CompoundTag());
        }

        CompoundTag all = getOrCreateRoot(injectorStack);
        CompoundTag root = all.getCompound(STACK_ROOT);
        root.put(STACK_INV, list);
        all.put(STACK_ROOT, root);

        writeBack(injectorStack, all);
    }

    public static void dropAndClearInstalledStack(ServerPlayer sp, HolderLookup.Provider provider, ItemStack injectorStack) {
        if (injectorStack == null || injectorStack.isEmpty()) return;

        SimpleContainer tmp = new SimpleContainer(SLOT_COUNT);
        int[] counts = new int[SLOT_COUNT];

        loadFromInstalledStack(injectorStack, provider, tmp, counts);

        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack base = tmp.getItem(i);
            int c = counts[i];

            if (base.isEmpty() || c <= 0) continue;

            ItemStack one = base.copy();
            one.setCount(1);

            for (int n = 0; n < c; n++) {
                sp.drop(one.copy(), false);
            }
        }

        CustomData cd = injectorStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag all = cd.copyTag();
        all.remove(STACK_ROOT);
        injectorStack.set(DataComponents.CUSTOM_DATA, CustomData.of(all));
    }

    /* ---------------- uninstall behavior ---------------- */

    @Override
    public void onRemoved(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer sp)) return;
        if (!sp.hasData(ModAttachments.CYBERWARE)) return;

        PlayerCyberwareData data = sp.getData(ModAttachments.CYBERWARE);
        if (data == null) return;

        for (var entry : data.getAll().entrySet()) {
            var arr = entry.getValue();
            if (arr == null) continue;

            for (int i = 0; i < arr.length; i++) {
                var inst = arr[i];
                if (inst == null) continue;

                ItemStack st = inst.getItem();
                if (st == null || st.isEmpty()) continue;

                if (st.getItem() == this) {
                    dropAndClearInstalledStack(sp, sp.level().registryAccess(), st);
                }
            }
        }

        data.setDirty();
        sp.syncData(ModAttachments.CYBERWARE);
    }

    /* ---------------- AUTO-INJECT FUNCTIONALITY ---------------- */

    private static final String INJECT_COOLDOWN_TAG = "cc_spinal_injector_cd";
    private static final int INJECT_COOLDOWN_TICKS = 20;

    private static int getCooldown(ServerPlayer sp) {
        CompoundTag pd = sp.getPersistentData();
        return pd.contains(INJECT_COOLDOWN_TAG, Tag.TAG_INT) ? pd.getInt(INJECT_COOLDOWN_TAG) : 0;
    }

    private static void setCooldown(ServerPlayer sp, int ticks) {
        sp.getPersistentData().putInt(INJECT_COOLDOWN_TAG, Math.max(0, ticks));
    }

    private static void tickDownCooldown(ServerPlayer sp) {
        int cd = getCooldown(sp);
        if (cd > 0) setCooldown(sp, cd - 1);
    }

    private static void applyNeuropozyneDay(ServerPlayer sp) {
        var neuropozyne = ModEffects.NEUROPOZYNE;
        sp.addEffect(new MobEffectInstance(neuropozyne, ONE_MINECRAFT_DAY_TICKS, 0, false, true, true));
    }

    private static void applyNeuropozyneBoostDay(ServerPlayer sp) {
        var neuropozyne = ModEffects.NEUROPOZYNE;

        MobEffectInstance cur = sp.getEffect(neuropozyne);
        int amp = (cur == null) ? 0 : Math.min(255, cur.getAmplifier() + 1);

        sp.addEffect(new MobEffectInstance(neuropozyne, ONE_MINECRAFT_DAY_TICKS, amp, false, true, true));
    }

    private static void applyPotionContentsTo(ServerPlayer sp, PotionContents pc, float durationFactor) {
        if (pc == null || pc == PotionContents.EMPTY) return;

        pc.forEachEffect(inst -> {
            if (inst == null) return;

            var effectHolder = inst.getEffect();
            var effect = effectHolder.value();

            if (effect.isInstantenous()) {
                effect.applyInstantenousEffect(sp, sp, sp, inst.getAmplifier(), 1.0D);
                return;
            }

            int dur = net.minecraft.util.Mth.floor(inst.getDuration() * durationFactor);
            if (dur <= 0) return;

            sp.addEffect(new MobEffectInstance(
                    effectHolder,
                    dur,
                    inst.getAmplifier(),
                    inst.isAmbient(),
                    inst.isVisible(),
                    inst.showIcon()
            ));
        });
    }

    private static boolean consumeStoredInjection(
            ServerPlayer sp,
            PlayerCyberwareData data,
            ItemStack injectorStack,
            HolderLookup.Provider provider,
            SimpleContainer tmp,
            int[] counts,
            int slot,
            boolean dropsAutoInjector
    ) {
        counts[slot] = Math.max(0, counts[slot] - 1);

        ItemStack stored = tmp.getItem(slot);

        if (counts[slot] <= 0) {
            tmp.setItem(slot, ItemStack.EMPTY);
        } else if (!stored.isEmpty()) {
            stored.setCount(counts[slot]);
            tmp.setItem(slot, stored);
        }

        saveIntoInstalledStack(injectorStack, provider, tmp, counts);

        data.setDirty();
        sp.syncData(ModAttachments.CYBERWARE);

        if (dropsAutoInjector) {
            dropEmptyAutoInjectorBehind(sp);
        } else {
            dropEmptyBottleBehind(sp);
        }

        return true;
    }

    private static boolean tryInjectFromStack(ServerPlayer sp, PlayerCyberwareData data, ItemStack injectorStack) {
        if (injectorStack == null || injectorStack.isEmpty()) return false;
        if (!(injectorStack.getItem() instanceof SpinalInjectorItem)) return false;

        HolderLookup.Provider provider = sp.level().registryAccess();

        SimpleContainer tmp = new SimpleContainer(SLOT_COUNT);
        int[] counts = new int[SLOT_COUNT];
        loadFromInstalledStack(injectorStack, provider, tmp, counts);

        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack base = tmp.getItem(i);
            int c = counts[i];

            if (base.isEmpty() || c <= 0) continue;
            if (!isInjectable(base)) continue;

            if (base.is(ModItems.NEUROPOZYNE_AUTOINJECTOR.get())) {
                var cyberwareRejection = ModEffects.CYBERWARE_REJECTION;

                if (!sp.hasEffect(cyberwareRejection)) {
                    continue;
                }

                if (sp.hasEffect(ModEffects.NEUROPOZYNE)) {
                    applyNeuropozyneBoostDay(sp);
                } else {
                    applyNeuropozyneDay(sp);
                }

                return consumeStoredInjection(
                        sp,
                        data,
                        injectorStack,
                        provider,
                        tmp,
                        counts,
                        i,
                        true
                );
            }

            if (base.getItem() instanceof ISpinalInjectableItem injectable) {
                if (!injectable.shouldSpinalInjectorInject(sp, base)) {
                    continue;
                }

                injectable.applySpinalInjection(sp, base);

                return consumeStoredInjection(
                        sp,
                        data,
                        injectorStack,
                        provider,
                        tmp,
                        counts,
                        i,
                        true
                );
            }

            PotionContents pc = base.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            if (pc == null || !pc.hasEffects()) continue;

            boolean missingNonInstant = false;

            for (MobEffectInstance inst : pc.getAllEffects()) {
                if (inst == null) continue;

                Holder<MobEffect> eff = inst.getEffect();
                if (eff == null) continue;

                MobEffect effValue = eff.value();
                if (effValue != null && effValue.isInstantenous()) continue;

                if (!sp.hasEffect(eff)) {
                    missingNonInstant = true;
                    break;
                }
            }

            if (!missingNonInstant) continue;

            applyPotionContentsTo(sp, pc, 2.0F);

            return consumeStoredInjection(
                    sp,
                    data,
                    injectorStack,
                    provider,
                    tmp,
                    counts,
                    i,
                    false
            );
        }

        return false;
    }

    private static void dropItemBehind(ServerPlayer sp, ItemStack drop) {
        if (drop == null || drop.isEmpty()) return;

        Vec3 look = sp.getLookAngle();
        Vec3 back = new Vec3(-look.x, 0.0, -look.z);

        if (back.lengthSqr() < 1.0E-6) {
            back = new Vec3(0.0, 0.0, 1.0);
        } else {
            back = back.normalize();
        }

        Vec3 offset = back.scale(0.35);
        double x = sp.getX() + offset.x;
        double y = sp.getY() + sp.getBbHeight() * 0.65;
        double z = sp.getZ() + offset.z;

        ItemEntity ent = new ItemEntity(sp.level(), x, y, z, drop.copy());
        ent.setDefaultPickUpDelay();
        ent.setDeltaMovement(offset.x * 0.25, 0.10, offset.z * 0.25);

        sp.level().addFreshEntity(ent);
    }

    private static void dropEmptyBottleBehind(ServerPlayer sp) {
        dropItemBehind(sp, new ItemStack(Items.GLASS_BOTTLE));
    }

    private static void dropEmptyAutoInjectorBehind(ServerPlayer sp) {
        dropItemBehind(sp, new ItemStack(ModItems.EMPTY_AUTOINJECTOR.get()));
    }

    private static boolean tryInjectFromAnyInstalled(ServerPlayer sp, PlayerCyberwareData data) {
        var arr = data.getAll().get(CyberwareSlot.BONE);
        if (arr == null) return false;

        for (int i = 0; i < arr.length; i++) {
            var inst = arr[i];
            if (inst == null) continue;

            ItemStack st = inst.getItem();
            if (st == null || st.isEmpty()) continue;

            if (!data.isEnabled(CyberwareSlot.BONE, i)) {
                continue;
            }

            if (st.getItem() instanceof SpinalInjectorItem) {
                if (tryInjectFromStack(sp, data, st)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* ---------------- EVENT HOOK ---------------- */

    @EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class AutoInjectHandler {

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            if (!(event.getEntity() instanceof ServerPlayer sp)) return;
            if (sp.level().isClientSide()) return;

            if (!sp.hasData(ModAttachments.CYBERWARE)) return;
            PlayerCyberwareData data = sp.getData(ModAttachments.CYBERWARE);
            if (data == null) return;

            var profile = com.perigrine3.createcybernetics.common.scheduling.CyberwareProfileManager.getProfile(sp, data);
            if (profile == null || !profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.SPINAL_INJECTOR)) {
                return;
            }

            int cd = getCooldown(sp);
            if (cd > 0) {
                tickDownCooldown(sp);
                return;
            }

            boolean injected = tryInjectFromAnyInstalled(sp, data);

            if (injected) {
                setCooldown(sp, INJECT_COOLDOWN_TICKS);
            }
        }
    }
}