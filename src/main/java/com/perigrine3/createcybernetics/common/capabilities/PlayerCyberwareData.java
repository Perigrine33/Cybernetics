package com.perigrine3.createcybernetics.common.capabilities;

import com.perigrine3.createcybernetics.ConfigValues;
import com.perigrine3.createcybernetics.api.*;
import com.perigrine3.createcybernetics.client.TrimColorPresets;
import com.perigrine3.createcybernetics.common.attributes.ModAttributes;
import com.perigrine3.createcybernetics.common.durability.CyberwareDurabilityManager;
import com.perigrine3.createcybernetics.common.humanity.HumanityAttributeModifiers;
import com.perigrine3.createcybernetics.common.surgery.DefaultOrgans;
import com.perigrine3.createcybernetics.item.cyberware.arm.ArmCannonItem;
import com.perigrine3.createcybernetics.item.cyberware.bone.SpinalInjectorItem;
import com.perigrine3.createcybernetics.item.cyberware.brain.ChipwareSlotsItem;
import com.perigrine3.createcybernetics.item.cyberware.brain.CyberdeckItem;
import com.perigrine3.createcybernetics.item.cyberware.lungs.VampyresItem;
import com.perigrine3.createcybernetics.item.cyberware.organs.HeatEngineItem;
import com.perigrine3.createcybernetics.util.ModTags;
import com.perigrine3.createcybernetics.util.SecondaryDyeColor;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class PlayerCyberwareData implements ICyberwareData {

    public static final String HOLO_SNAPSHOT_FLAG = "cc_holo_snapshot";
    public static final String HOLO_SNAPSHOT_CYBERWARE = "cc_holo_snapshot_cyberware";

    private static final String NBT_CYBERWARE = "Cyberware";
    private static final String NBT_HUMANITY = "Humanity";
    private static final String NBT_HUMANITY_BONUS = "HumanityBonus";
    private static final String NBT_HUMANITY_PENALTIES = "HumanityPenalties";
    private static final String NBT_ENERGY = "Energy";
    private static final String NBT_CYBERDECK_RAM = "CyberdeckRam";
    private static final String NBT_CYBERDECK_RAM_REGEN_TICKS = "CyberdeckRamRegenTicks";
    private static final String NBT_CYBERDECK_MAX_RAM = "CyberdeckMaxRam";
    private static final String NBT_DATA_INTEGRITY = "DataIntegrity";
    private static final String NBT_TICKS_SINCE_INTEGRITY_BOOT_DOWN = "TicksSinceIntegrityBootDown";
    private static final String NBT_INTEGRITY_BED_TICKS = "IntegrityBedTicks";

    private boolean forcedChamberCrouch = false;

    private static final String NBT_NEUROPOZYNE_APPLY_COUNT = "NeuropozyneApplyCount";
    private int neuropozyneApplyCount = 0;

    private static final String NBT_SPINAL_INJECTOR_INV = "SpinalInjectorInv";
    private final ItemStack[] spinalInjectorInv = new ItemStack[SpinalInjectorItem.SLOT_COUNT];

    private static final String NBT_ARM_CANNON_INV = "ArmCannonInv";
    private final ItemStack[] armCannonInv = new ItemStack[ArmCannonItem.SLOT_COUNT];

    private static final String NBT_ARM_CANNON_SELECTED = "ArmCannonSelected";
    private int armCannonSelected = 0;

    // ---- Copernicus oxygen (display units 0..maxDisplayUnits) ----
    private static final String NBT_COPERNICUS_OXYGEN = "CopernicusOxygen";
    private int copernicusOxygen = 0;
    private boolean copernicusOxygenatedEnvironment = false;
    private int copernicusOxygenSecondTicker = 0;

    public static final int CHIPWARE_SLOT_COUNT = 2;
    private static final String NBT_CHIPWARE_INV = "ChipwareInv";
    private static final String NBT_CHIPWARE_QUICKHACK_FIRED = "ChipwareQuickhackFired";
    private final ItemStack[] chipwareInv = new ItemStack[CHIPWARE_SLOT_COUNT];
    private int chipwareQuickhackFiredMask = 0;

    public static final int CYBERDECK_QUICKHACK_SLOT_COUNT = 4;
    public static final int CYBERDECK_MICROPROGRAM_CHIP_SLOT = 4;
    public static final int CYBERDECK_MACROPROGRAM_SLOT_START = 5;
    public static final int CYBERDECK_MACROPROGRAM_SLOT_COUNT = 10;
    public static final int CYBERDECK_SLOT_COUNT = CYBERDECK_MACROPROGRAM_SLOT_START + CYBERDECK_MACROPROGRAM_SLOT_COUNT;
    private static final String NBT_CYBERDECK_INV = "CyberdeckInv";
    private final ItemStack[] cyberdeckInv = new ItemStack[CYBERDECK_SLOT_COUNT];

    public static final int VAMPYRES_SLOT_COUNT = 2;
    private static final String NBT_VAMPYRES_INV = "VampyresInv";
    private final ItemStack[] vampyresInv = new ItemStack[VAMPYRES_SLOT_COUNT];

    public static final int FACEPLATE_MASK_SLOT_COUNT = 1;
    private static final String NBT_FACEPLATE_MASK_INV = "FaceplateMaskInv";
    private final ItemStack[] faceplateMaskInv = new ItemStack[FACEPLATE_MASK_SLOT_COUNT];

    // ---- Heat Engine (fuel/input/output + timers) ----
    public static final int HEAT_ENGINE_SLOT_COUNT = 3;
    public static final int HEAT_ENGINE_FUEL = 0;
    public static final int HEAT_ENGINE_INPUT = 1;
    public static final int HEAT_ENGINE_OUTPUT = 2;

    private static final String NBT_HEAT_ENGINE_INV = "HeatEngineInv";
    private static final String NBT_HEAT_ENGINE_BURN = "HeatEngineBurn";
    private static final String NBT_HEAT_ENGINE_BURN_TOTAL = "HeatEngineBurnTotal";
    private static final String NBT_HEAT_ENGINE_COOK = "HeatEngineCook";
    private static final String NBT_HEAT_ENGINE_COOK_TOTAL = "HeatEngineCookTotal";

    private final ItemStack[] heatEngineInv = new ItemStack[HEAT_ENGINE_SLOT_COUNT];

    private int heatEngineBurnTime = 0;
    private int heatEngineBurnTimeTotal = 0;
    private int heatEngineCookTime = 0;
    private int heatEngineCookTimeTotal = 200;

    private final EnumMap<CyberwareSlot, InstalledCyberware[]> slots =
            new EnumMap<>(CyberwareSlot.class);

    private final EnumMap<CyberwareSlot, boolean[]> enabled =
            new EnumMap<>(CyberwareSlot.class);

    private boolean dirty = false;

    private int legacyHumanity = ConfigValues.BASE_HUMANITY;
    private int legacyHumanityBonus = 0;
    private final Map<String, Integer> legacyHumanityPenalties = new HashMap<>();

    private double dataIntegrity = -1.0D;
    private int ticksSinceIntegrityBootDown = 0;
    private int integrityBedTicks = 0;

    private int energyStored = 0;
    private int cyberdeckRam = -1;
    private int cyberdeckRamRegenTicks = 0;
    private int cyberdeckMaxRam = -1;

    public PlayerCyberwareData() {
        for (CyberwareSlot slot : CyberwareSlot.values()) {
            slots.put(slot, new InstalledCyberware[slot.size]);

            boolean[] en = new boolean[slot.size];
            for (int i = 0; i < en.length; i++) {
                en[i] = true;
            }
            enabled.put(slot, en);
        }

        for (int i = 0; i < spinalInjectorInv.length; i++) {
            spinalInjectorInv[i] = ItemStack.EMPTY;
        }
        for (int i = 0; i < armCannonInv.length; i++) {
            armCannonInv[i] = ItemStack.EMPTY;
        }
        for (int i = 0; i < chipwareInv.length; i++) {
            chipwareInv[i] = ItemStack.EMPTY;
        }
        chipwareQuickhackFiredMask = 0;
        for (int i = 0; i < cyberdeckInv.length; i++) {
            cyberdeckInv[i] = ItemStack.EMPTY;
        }
        for (int i = 0; i < vampyresInv.length; i++) {
            vampyresInv[i] = ItemStack.EMPTY;
        }
        for (int i = 0; i < heatEngineInv.length; i++) {
            heatEngineInv[i] = ItemStack.EMPTY;
        }
        for (int i = 0; i < faceplateMaskInv.length; i++) {
            faceplateMaskInv[i] = ItemStack.EMPTY;
        }

        armCannonSelected = 0;
    }

    @Override
    public InstalledCyberware get(CyberwareSlot slot, int index) {
        InstalledCyberware[] arr = slots.get(slot);
        if (arr == null) return null;
        if (index < 0 || index >= arr.length) return null;
        return arr[index];
    }

    @Override
    public void set(CyberwareSlot slot, int index, InstalledCyberware cyberware) {
        InstalledCyberware[] arr = slots.get(slot);
        if (arr == null) return;
        if (index < 0 || index >= arr.length) return;

        arr[index] = cyberware;

        boolean[] en = enabled.get(slot);
        if (en != null && index >= 0 && index < en.length) {
            boolean enabledByDefault = true;

            if (cyberware != null) {
                ItemStack st = cyberware.getItem();
                if (st != null && !st.isEmpty() && st.getItem() instanceof ICyberwareItem cwItem) {
                    if (cwItem.isToggleableByWheel(st, slot)) {
                        enabledByDefault = false;
                    }
                }
            }

            en[index] = enabledByDefault;
        }

        refreshLegacyHumanitySnapshot();
        dirty = true;
    }

    @Override
    public InstalledCyberware remove(CyberwareSlot slot, int index) {
        InstalledCyberware[] arr = slots.get(slot);
        if (arr == null) return null;
        if (index < 0 || index >= arr.length) return null;

        InstalledCyberware old = arr[index];
        arr[index] = null;

        boolean[] en = enabled.get(slot);
        if (en != null && index >= 0 && index < en.length) {
            en[index] = true;
        }

        refreshLegacyHumanitySnapshot();
        dirty = true;
        return old;
    }

    @Override
    public Map<CyberwareSlot, InstalledCyberware[]> getAll() {
        return slots;
    }

    @Override
    public int getHumanity() {
        return legacyHumanity + legacyHumanityBonus - getHumanityPenaltySum();
    }

    public int getHumanity(Player player) {
        if (player == null) {
            return getHumanity();
        }
        return HumanityAttributeModifiers.get(player);
    }

    @Override
    public void setHumanity(int value) {
        legacyHumanity = Mth.clamp(value, -1000, 1000);
        dirty = true;
    }

    public int getHumanityBase() {
        return legacyHumanity;
    }

    public int getHumanityBase(Player player) {
        return HumanityAttributeModifiers.getBase(player);
    }

    public int getHumanityBonus() {
        return legacyHumanityBonus;
    }

    public void setHumanityBonus(int bonus) {
        legacyHumanityBonus = Mth.clamp(bonus, -1000, 1000);
        dirty = true;
    }

    public void clearHumanityBonus() {
        if (legacyHumanityBonus != 0) {
            legacyHumanityBonus = 0;
            dirty = true;
        }
    }

    public void setHumanityBonus(Player player, String key, int bonus) {
        if (player == null) {
            setHumanityBonus(bonus);
            return;
        }

        HumanityAttributeModifiers.setBonus(player, key, bonus);
        dirty = true;
    }

    public void clearHumanityBonus(Player player, String key) {
        if (player == null) {
            clearHumanityBonus();
            return;
        }

        HumanityAttributeModifiers.clearBonus(player, key);
        dirty = true;
    }

    public void setHumanityPenalty(String key, int penalty) {
        if (key == null || key.isBlank()) return;

        int clamped = Mth.clamp(penalty, 0, 1000);
        Integer cur = legacyHumanityPenalties.get(key);
        if (cur != null && cur == clamped) return;

        if (clamped <= 0) {
            legacyHumanityPenalties.remove(key);
        } else {
            legacyHumanityPenalties.put(key, clamped);
        }

        dirty = true;
    }

    public void clearHumanityPenalty(String key) {
        if (key == null || key.isBlank()) return;
        if (legacyHumanityPenalties.remove(key) != null) {
            dirty = true;
        }
    }

    public void setHumanityPenalty(Player player, String key, int penalty) {
        if (player == null) {
            setHumanityPenalty(key, penalty);
            return;
        }

        HumanityAttributeModifiers.setPenalty(player, key, penalty);
        setHumanityPenalty(key, penalty);
    }

    public void clearHumanityPenalty(Player player, String key) {
        if (player == null) {
            clearHumanityPenalty(key);
            return;
        }

        HumanityAttributeModifiers.clearPenalty(player, key);
        clearHumanityPenalty(key);
    }

    public int getHumanityPenaltySum() {
        if (legacyHumanityPenalties.isEmpty()) return 0;

        int sum = 0;
        for (int v : legacyHumanityPenalties.values()) {
            sum += Math.max(0, v);
        }
        return sum;
    }

    public boolean hasInitializedDataIntegrity() {
        return dataIntegrity >= 0.0D;
    }

    public int getDataIntegrity() {
        return Mth.ceil((float) getDataIntegrityPrecise());
    }

    public double getDataIntegrityPrecise() {
        if (dataIntegrity < 0.0D) {
            return HumanityAttributeModifiers.getConfiguredBaseHumanity();
        }

        return dataIntegrity;
    }

    public void setDataIntegrity(int value) {
        setDataIntegrityPrecise(value);
    }

    public void setDataIntegrityPrecise(double value) {
        int maxIntegrity = HumanityAttributeModifiers.getConfiguredBaseHumanity();
        double clamped = Mth.clamp(value, 0.0D, maxIntegrity);

        if (Double.compare(dataIntegrity, clamped) == 0) {
            return;
        }

        dataIntegrity = clamped;
        dirty = true;
    }

    public int getTicksSinceIntegrityBootDown() {
        return Math.max(0, ticksSinceIntegrityBootDown);
    }

    public void setTicksSinceIntegrityBootDown(int value) {
        int clamped = Math.max(0, value);

        if (ticksSinceIntegrityBootDown == clamped) {
            return;
        }

        ticksSinceIntegrityBootDown = clamped;
        dirty = true;
    }

    public int getIntegrityBedTicks() {
        return Math.max(0, integrityBedTicks);
    }

    public void setIntegrityBedTicks(int value) {
        int clamped = Math.max(0, value);

        if (integrityBedTicks == clamped) {
            return;
        }

        integrityBedTicks = clamped;
        dirty = true;
    }

    public void recomputeHumanityBaseFromInstalled() {
        refreshLegacyHumanitySnapshot();
        dirty = true;
    }

    public void recomputeHumanityBaseFromInstalled(Player player) {
        refreshLegacyHumanitySnapshot();

        if (player != null) {
            HumanityAttributeModifiers.rebuildCyberwareCostModifiers(player);

            for (Map.Entry<String, Integer> entry : legacyHumanityPenalties.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                if (key != null && !key.isBlank() && value != null && value > 0) {
                    HumanityAttributeModifiers.setPenalty(player, key, value);
                }
            }

            if (legacyHumanityBonus > 0) {
                HumanityAttributeModifiers.setBonus(player, "legacy_humanity_bonus", legacyHumanityBonus);
            } else {
                HumanityAttributeModifiers.clearBonus(player, "legacy_humanity_bonus");
            }
        }

        dirty = true;
    }

    private void refreshLegacyHumanitySnapshot() {
        int base = HumanityAttributeModifiers.getConfiguredBaseHumanity();

        for (var entry : slots.entrySet()) {
            InstalledCyberware[] arr = entry.getValue();
            if (arr == null) continue;

            for (InstalledCyberware cw : arr) {
                if (cw == null) continue;

                ItemStack stack = cw.getItem();
                if (stack == null || stack.isEmpty()) continue;

                if (stack.getItem() instanceof ICyberwareItem item) {
                    base -= item.getHumanityCost();
                }
            }
        }

        legacyHumanity = Mth.clamp(base, -1000, 1000);
    }

    /* ---------------- ARM CANNON SELECTED SLOT ---------------- */

    public int getArmCannonSelected() {
        return Mth.clamp(armCannonSelected, 0, ArmCannonItem.SLOT_COUNT - 1);
    }

    public void setArmCannonSelected(int idx) {
        int clamped = Mth.clamp(idx, 0, ArmCannonItem.SLOT_COUNT - 1);
        if (clamped != armCannonSelected) {
            armCannonSelected = clamped;
            dirty = true;
        }
    }

    /* ---------------- FACEPLATE MASK INVENTORY ---------------- */

    public ItemStack getFaceplateMaskStack() {
        ItemStack stack = faceplateMaskInv[0];
        return stack == null ? ItemStack.EMPTY : stack;
    }

    public void setFaceplateMaskStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            faceplateMaskInv[0] = ItemStack.EMPTY;
            dirty = true;
            return;
        }

        ItemStack copy = stack.copy();
        copy.setCount(1);

        faceplateMaskInv[0] = copy;
        dirty = true;
    }

    public ItemStack removeFaceplateMaskStack() {
        ItemStack stored = getFaceplateMaskStack();
        if (stored.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = stored.copy();
        faceplateMaskInv[0] = ItemStack.EMPTY;
        dirty = true;

        return removed;
    }

    public void clearFaceplateMaskInventory() {
        faceplateMaskInv[0] = ItemStack.EMPTY;
        dirty = true;
    }

    /* ---------------- CHIPWARE INVENTORY (2 SLOTS) ---------------- */

    public ItemStack getChipwareStack(int slot) {
        if (slot < 0 || slot >= chipwareInv.length) return ItemStack.EMPTY;
        ItemStack st = chipwareInv[slot];
        return st == null ? ItemStack.EMPTY : st;
    }

    public void setChipwareStack(int slot, ItemStack stack) {
        if (slot < 0 || slot >= chipwareInv.length) return;

        ItemStack current = getChipwareStack(slot);

        if (stack == null || stack.isEmpty()) {
            chipwareInv[slot] = ItemStack.EMPTY;
            setChipwareQuickhackFired(slot, false);
            dirty = true;
            return;
        }

        if (!stack.is(ModTags.Items.DATA_SHARDS)) {
            chipwareInv[slot] = ItemStack.EMPTY;
            setChipwareQuickhackFired(slot, false);
            dirty = true;
            return;
        }

        ItemStack copy = stack.copy();
        copy.setCount(1);

        boolean sameStack = !current.isEmpty() && ItemStack.isSameItemSameComponents(current, copy);

        chipwareInv[slot] = copy;

        if (!sameStack) {
            setChipwareQuickhackFired(slot, false);
        }

        dirty = true;
    }

    public boolean hasChipwareQuickhackFired(int slot) {
        if (slot < 0 || slot >= chipwareInv.length) return false;
        return (chipwareQuickhackFiredMask & (1 << slot)) != 0;
    }

    public void setChipwareQuickhackFired(int slot, boolean fired) {
        if (slot < 0 || slot >= chipwareInv.length) return;

        int mask = 1 << slot;
        int next = fired ? chipwareQuickhackFiredMask | mask : chipwareQuickhackFiredMask & ~mask;

        if (next != chipwareQuickhackFiredMask) {
            chipwareQuickhackFiredMask = next;
            dirty = true;
        }
    }

    public void clearChipwareInventory() {
        for (int i = 0; i < chipwareInv.length; i++) {
            chipwareInv[i] = ItemStack.EMPTY;
        }

        chipwareQuickhackFiredMask = 0;
        dirty = true;
    }

    public boolean hasChipwareShard(TagKey<Item> tag) {
        for (int i = 0; i < CHIPWARE_SLOT_COUNT; i++) {
            ItemStack st = getChipwareStack(i);
            if (!st.isEmpty() && st.is(tag)) return true;
        }
        return false;
    }

    public boolean hasChipwareShardExact(Item shardItem) {
        if (shardItem == null) return false;

        for (int i = 0; i < CHIPWARE_SLOT_COUNT; i++) {
            ItemStack st = getChipwareStack(i);
            if (!st.isEmpty() && st.is(shardItem)) {
                return true;
            }
        }

        return false;
    }

    /* ---------------- CYBERDECK QUICKHACK INVENTORY (4 SLOTS) ---------------- */

    public ItemStack getCyberdeckStack(int slot) {
        if (slot < 0 || slot >= cyberdeckInv.length) return ItemStack.EMPTY;
        ItemStack st = cyberdeckInv[slot];
        return st == null ? ItemStack.EMPTY : st;
    }

    public void setCyberdeckStack(int slot, ItemStack stack) {
        if (slot < 0 || slot >= cyberdeckInv.length) return;

        if (stack == null || stack.isEmpty()) {
            cyberdeckInv[slot] = ItemStack.EMPTY;
            dirty = true;
            return;
        }

        if (slot < CYBERDECK_QUICKHACK_SLOT_COUNT) {
            if (!stack.is(ModTags.Items.QUICKHACK_SHARDS)) {
                cyberdeckInv[slot] = ItemStack.EMPTY;
                dirty = true;
                return;
            }
        } else if (slot == CYBERDECK_MICROPROGRAM_CHIP_SLOT) {
            if (!stack.is(ModTags.Items.MICROPROGRAM_SHARDS)) {
                cyberdeckInv[slot] = ItemStack.EMPTY;
                dirty = true;
                return;
            }
        } else if (slot >= CYBERDECK_MACROPROGRAM_SLOT_START && slot < CYBERDECK_SLOT_COUNT) {
            if (!stack.is(ModTags.Items.MACROPROGRAMS)) {
                cyberdeckInv[slot] = ItemStack.EMPTY;
                dirty = true;
                return;
            }
        } else {
            return;
        }

        ItemStack copy = stack.copy();
        copy.setCount(1);

        cyberdeckInv[slot] = copy;
        dirty = true;
    }

    public void clearCyberdeckInventory() {
        for (int i = 0; i < cyberdeckInv.length; i++) {
            cyberdeckInv[i] = ItemStack.EMPTY;
        }
        dirty = true;
    }

    /* ---------------- VAMPYRES INVENTORY ---------------- */

    public ItemStack getVampyresStack(int slot) {
        if (slot < 0 || slot >= vampyresInv.length) return ItemStack.EMPTY;

        ItemStack stack = vampyresInv[slot];
        return stack == null ? ItemStack.EMPTY : stack;
    }

    public void setVampyresStack(int slot, ItemStack stack) {
        if (slot < 0 || slot >= vampyresInv.length) return;

        if (stack == null || stack.isEmpty()) {
            vampyresInv[slot] = ItemStack.EMPTY;
            dirty = true;
            return;
        }

        if (!VampyresItem.isInjectable(stack)) {
            vampyresInv[slot] = ItemStack.EMPTY;
            dirty = true;
            return;
        }

        ItemStack copy = stack.copy();
        copy.setCount(Math.min(VampyresItem.SLOT_STACK_LIMIT, copy.getCount()));

        vampyresInv[slot] = copy;
        dirty = true;
    }

    public ItemStack removeVampyresStack(int slot, int amount) {
        if (slot < 0 || slot >= vampyresInv.length) return ItemStack.EMPTY;
        if (amount <= 0) return ItemStack.EMPTY;

        ItemStack current = getVampyresStack(slot);
        if (current.isEmpty()) return ItemStack.EMPTY;

        int removedCount = Math.min(amount, current.getCount());

        ItemStack removed = current.copy();
        removed.setCount(removedCount);

        current.shrink(removedCount);

        if (current.isEmpty()) {
            vampyresInv[slot] = ItemStack.EMPTY;
        } else {
            vampyresInv[slot] = current;
        }

        dirty = true;
        return removed;
    }

    public void clearVampyresInventory() {
        for (int i = 0; i < vampyresInv.length; i++) {
            vampyresInv[i] = ItemStack.EMPTY;
        }

        dirty = true;
    }

    public void ejectVampyresInventory(Player player) {
        for (int slot = 0; slot < vampyresInv.length; slot++) {
            giveOrDropStoredStack(player, vampyresInv[slot]);
            vampyresInv[slot] = ItemStack.EMPTY;
        }

        dirty = true;
    }

    /* ---------------- ENABLED HELPERS ---------------- */

    public boolean isEnabled(CyberwareSlot slot, int index) {
        boolean[] arr = enabled.get(slot);
        if (arr == null) return true;
        if (index < 0 || index >= arr.length) return true;
        return arr[index];
    }

    public void setEnabled(CyberwareSlot slot, int index, boolean value) {
        boolean[] arr = enabled.get(slot);
        if (arr == null) return;
        if (index < 0 || index >= arr.length) return;

        if (arr[index] != value) {
            arr[index] = value;
            dirty = true;
        }
    }

    public boolean toggleEnabled(CyberwareSlot slot, int index) {
        boolean next = !isEnabled(slot, index);
        setEnabled(slot, index, next);
        return next;
    }

    public boolean hasOrgan(CyberwareSlot slot) {
        InstalledCyberware[] arr = slots.get(slot);
        if (arr == null) return false;

        for (InstalledCyberware cw : arr) {
            if (cw != null && cw.getItem() != null && !cw.getItem().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean isOrganReplaced(CyberwareSlot slot) {
        InstalledCyberware[] arr = slots.get(slot);
        if (arr == null) return false;

        for (InstalledCyberware cw : arr) {
            if (cw != null && !cw.getItem().isEmpty() && cw.getItem().getItem() instanceof ICyberwareItem item) {
                if (item.replacesOrgan() && item.getReplacedOrgans().contains(slot)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasAnyInSlots(CyberwareSlot slot) {
        InstalledCyberware[] arr = slots.get(slot);
        if (arr == null) return false;

        for (InstalledCyberware cw : arr) {
            if (cw == null) continue;
            if (!cw.getItem().isEmpty()) return true;
        }

        return false;
    }

    public boolean hasAnyTagged(TagKey<Item> tag, CyberwareSlot... slotsToCheck) {
        for (CyberwareSlot slot : slotsToCheck) {
            InstalledCyberware[] arr = slots.get(slot);
            if (arr == null) continue;

            for (InstalledCyberware cw : arr) {
                if (cw == null) continue;

                ItemStack stack = cw.getItem();
                if (stack.isEmpty()) continue;

                if (stack.is(tag)) return true;
            }
        }
        return false;
    }

    public boolean hasSpecificItem(Item item, CyberwareSlot... slotsToCheck) {
        for (CyberwareSlot slot : slotsToCheck) {
            InstalledCyberware[] arr = slots.get(slot);
            if (arr == null) continue;

            for (InstalledCyberware cw : arr) {
                if (cw == null) continue;

                ItemStack stack = cw.getItem();
                if (stack.isEmpty()) continue;

                if (stack.is(item)) return true;
            }
        }
        return false;
    }

    public boolean hasMultipleSpecificItem(Item item, CyberwareSlot slotToCheck, int requiredCount) {
        if (item == null) return false;
        if (requiredCount <= 0) return true;

        InstalledCyberware[] arr = slots.get(slotToCheck);
        if (arr == null) return false;

        int found = 0;
        for (InstalledCyberware cw : arr) {
            if (cw == null) continue;

            ItemStack stack = cw.getItem();
            if (stack == null || stack.isEmpty()) continue;

            if (stack.is(item)) {
                found++;
                if (found >= requiredCount) return true;
            }
        }

        return false;
    }

    public boolean hasMultipleSpecificItem(Item item, int requiredCount, CyberwareSlot... slotsToCheck) {
        if (item == null) return false;
        if (requiredCount <= 0) return true;
        if (slotsToCheck == null || slotsToCheck.length == 0) return false;

        int found = 0;

        for (CyberwareSlot slot : slotsToCheck) {
            InstalledCyberware[] arr = slots.get(slot);
            if (arr == null) continue;

            for (InstalledCyberware cw : arr) {
                if (cw == null) continue;

                ItemStack stack = cw.getItem();
                if (stack == null || stack.isEmpty()) continue;

                if (stack.is(item)) {
                    found++;
                    if (found >= requiredCount) return true;
                }
            }
        }

        return false;
    }

    public boolean isInstalled(Item item, CyberwareSlot slot, int index) {
        InstalledCyberware[] arr = slots.get(slot);
        if (arr == null) return false;
        if (index < 0 || index >= arr.length) return false;

        InstalledCyberware cw = arr[index];
        if (cw == null) return false;

        ItemStack stack = cw.getItem();
        if (stack == null || stack.isEmpty()) return false;

        return stack.is(item);
    }

    public boolean isInstalled(Item item, CyberwareSlot slot) {
        return hasSpecificItem(item, slot);
    }

    public boolean isDyed(CyberwareSlot slot, int index) {
        InstalledCyberware installed = get(slot, index);
        if (installed == null) return false;

        ItemStack st = installed.getItem();
        if (st == null || st.isEmpty()) return false;

        if (!(st.getItem() instanceof ICyberwareItem item)) return false;

        return item.isDyed(st, slot);
    }

    public int dyeColor(CyberwareSlot slot, int index) {
        InstalledCyberware installed = get(slot, index);
        if (installed == null) return 0xFFFFFFFF;

        ItemStack st = installed.getItem();
        if (st == null || st.isEmpty()) return 0xFFFFFFFF;

        if (!(st.getItem() instanceof ICyberwareItem item)) return 0xFFFFFFFF;

        return item.dyeColor(st, slot);
    }

    public boolean isDyed(Item item, CyberwareSlot slotToCheck) {
        InstalledCyberware[] arr = slots.get(slotToCheck);
        if (arr == null) return false;

        for (InstalledCyberware installed : arr) {
            if (installed == null) continue;

            ItemStack st = installed.getItem();
            if (st == null || st.isEmpty()) continue;

            if (!st.is(item)) continue;

            if (st.getItem() instanceof ICyberwareItem cw) {
                return cw.isDyed(st, slotToCheck);
            }
            return false;
        }
        return false;
    }

    public int dyeColor(Item item, CyberwareSlot slotToCheck) {
        InstalledCyberware[] arr = slots.get(slotToCheck);
        if (arr == null) return 0xFFFFFFFF;

        for (InstalledCyberware installed : arr) {
            if (installed == null) continue;

            ItemStack st = installed.getItem();
            if (st == null || st.isEmpty()) continue;

            if (!st.is(item)) continue;

            if (st.getItem() instanceof ICyberwareItem cw) {
                return cw.dyeColor(st, slotToCheck);
            }
            return 0xFFFFFFFF;
        }
        return 0xFFFFFFFF;
    }

    public boolean isSecondaryDyed(CyberwareSlot slot, int index) {
        InstalledCyberware installed = get(slot, index);
        if (installed == null) return false;

        ItemStack st = installed.getItem();
        if (st == null || st.isEmpty()) return false;

        return SecondaryDyeColor.hasColor(st);
    }

    public int secondaryDyeColor(CyberwareSlot slot, int index) {
        InstalledCyberware installed = get(slot, index);
        if (installed == null) return 0xFFFFFFFF;

        ItemStack st = installed.getItem();
        if (st == null || st.isEmpty()) return 0xFFFFFFFF;

        return SecondaryDyeColor.getColor(st);
    }

    public boolean isSecondaryDyed(Item item, CyberwareSlot slotToCheck) {
        if (item == null) return false;

        InstalledCyberware[] arr = slots.get(slotToCheck);
        if (arr == null) return false;

        for (InstalledCyberware installed : arr) {
            if (installed == null) continue;

            ItemStack st = installed.getItem();
            if (st == null || st.isEmpty()) continue;

            if (!st.is(item)) continue;

            return SecondaryDyeColor.hasColor(st);
        }

        return false;
    }

    public int secondaryDyeColor(Item item, CyberwareSlot slotToCheck) {
        if (item == null) return 0xFFFFFFFF;

        InstalledCyberware[] arr = slots.get(slotToCheck);
        if (arr == null) return 0xFFFFFFFF;

        for (InstalledCyberware installed : arr) {
            if (installed == null) continue;

            ItemStack st = installed.getItem();
            if (st == null || st.isEmpty()) continue;

            if (!st.is(item)) continue;

            return SecondaryDyeColor.getColor(st);
        }

        return 0xFFFFFFFF;
    }

    public boolean isTrimmed(CyberwareSlot slot, int index) {
        InstalledCyberware installed = get(slot, index);
        if (installed == null) return false;

        ItemStack st = installed.getItem();
        if (st == null || st.isEmpty()) return false;

        return st.get(DataComponents.TRIM) != null;
    }

    public boolean isTrimmed(Item item, CyberwareSlot slotToCheck) {
        if (item == null) return false;

        InstalledCyberware[] arr = slots.get(slotToCheck);
        if (arr == null) return false;

        for (InstalledCyberware installed : arr) {
            if (installed == null) continue;

            ItemStack st = installed.getItem();
            if (st == null || st.isEmpty()) continue;
            if (!st.is(item)) continue;

            return st.get(DataComponents.TRIM) != null;
        }

        return false;
    }

    public ResourceLocation trimMaterialId(Item item, CyberwareSlot slotToCheck) {
        if (item == null) return null;

        InstalledCyberware[] arr = slots.get(slotToCheck);
        if (arr == null) return null;

        for (InstalledCyberware installed : arr) {
            if (installed == null) continue;

            ItemStack st = installed.getItem();
            if (st == null || st.isEmpty()) continue;
            if (!st.is(item)) continue;

            ArmorTrim trim = st.get(DataComponents.TRIM);
            if (trim == null) return null;

            return trim.material().unwrapKey().map(k -> k.location()).orElse(null);
        }

        return null;
    }

    public ResourceLocation trimPatternId(Item item, CyberwareSlot slotToCheck) {
        if (item == null) return null;

        InstalledCyberware[] arr = slots.get(slotToCheck);
        if (arr == null) return null;

        for (InstalledCyberware installed : arr) {
            if (installed == null) continue;

            ItemStack st = installed.getItem();
            if (st == null || st.isEmpty()) continue;
            if (!st.is(item)) continue;

            ArmorTrim trim = st.get(DataComponents.TRIM);
            if (trim == null) return null;

            return trim.pattern().unwrapKey().map(k -> k.location()).orElse(null);
        }

        return null;
    }

    public int trimColor(Item item, CyberwareSlot slotToCheck) {
        if (item == null) return 0xFFFFFFFF;

        InstalledCyberware[] arr = slots.get(slotToCheck);
        if (arr == null) return 0xFFFFFFFF;

        for (InstalledCyberware installed : arr) {
            if (installed == null) continue;

            ItemStack st = installed.getItem();
            if (st == null || st.isEmpty()) continue;
            if (!st.is(item)) continue;

            ArmorTrim trim = st.get(DataComponents.TRIM);
            if (trim == null) return 0xFFFFFFFF;

            return TrimColorPresets.colorFor(trim.material());
        }

        return 0xFFFFFFFF;
    }

    public int trimColor(CyberwareSlot slot, int index) {
        InstalledCyberware installed = get(slot, index);
        if (installed == null) return 0xFFFFFFFF;

        ItemStack st = installed.getItem();
        if (st == null || st.isEmpty()) return 0xFFFFFFFF;

        ArmorTrim trim = st.get(DataComponents.TRIM);
        if (trim == null) return 0xFFFFFFFF;

        return TrimColorPresets.colorFor(trim.material());
    }


    /* ---------------- DURABILITY ---------------- */

    public InstalledCyberware getInstalledSpecificItem(Item item, CyberwareSlot... slotsToCheck) {
        if (item == null || slotsToCheck == null) return null;

        for (CyberwareSlot slot : slotsToCheck) {
            if (slot == null) continue;

            InstalledCyberware[] arr = slots.get(slot);
            if (arr == null) continue;

            for (InstalledCyberware installed : arr) {
                if (installed == null) continue;

                ItemStack stack = installed.getItem();
                if (stack == null || stack.isEmpty()) continue;
                if (!stack.is(item)) continue;

                return installed;
            }
        }

        return null;
    }

    public int getDurability(Item item, CyberwareSlot... slotsToCheck) {
        InstalledCyberware installed = getInstalledSpecificItem(item, slotsToCheck);
        if (installed == null) return -1;

        return installed.getDurability();
    }

    public int getMaxDurability(Item item, CyberwareSlot... slotsToCheck) {
        InstalledCyberware installed = getInstalledSpecificItem(item, slotsToCheck);
        if (installed == null) return -1;

        return installed.getMaxDurability();
    }

    public float getDurabilityPercent(Item item, CyberwareSlot... slotsToCheck) {
        InstalledCyberware installed = getInstalledSpecificItem(item, slotsToCheck);
        if (installed == null) return -1.0F;

        return installed.getDurabilityPercent();
    }

    public boolean hasDurabilityAtOrBelow(Item item, float percent, CyberwareSlot... slotsToCheck) {
        float durability = getDurabilityPercent(item, slotsToCheck);
        if (durability < 0.0F) return false;

        return durability <= Mth.clamp(percent, 0.0F, 1.0F);
    }

    public boolean hasDurabilityBelow(Item item, float percent, CyberwareSlot... slotsToCheck) {
        float durability = getDurabilityPercent(item, slotsToCheck);
        if (durability < 0.0F) return false;

        return durability < Mth.clamp(percent, 0.0F, 1.0F);
    }

    public boolean isBroken(Item item, CyberwareSlot... slotsToCheck) {
        InstalledCyberware installed = getInstalledSpecificItem(item, slotsToCheck);
        if (installed == null) return false;

        return installed.isBroken();
    }

    public boolean isFunctional(Player player, CyberwareSlot slot, int index) {
        InstalledCyberware installed = get(slot, index);
        return CyberwareDurabilityManager.isFunctional(player, installed);
    }

    public boolean hasFunctionalSpecificItem(Player player, Item item, CyberwareSlot... slotsToCheck) {
        if (item == null || slotsToCheck == null) return false;

        for (CyberwareSlot slot : slotsToCheck) {
            InstalledCyberware[] arr = slots.get(slot);
            if (arr == null) continue;

            for (InstalledCyberware installed : arr) {
                if (installed == null) continue;

                ItemStack stack = installed.getItem();
                if (stack == null || stack.isEmpty()) continue;
                if (!stack.is(item)) continue;
                if (!CyberwareDurabilityManager.isFunctional(player, installed)) continue;

                return true;
            }
        }

        return false;
    }

    public boolean hasAnyFunctionalTagged(Player player, TagKey<Item> tag, CyberwareSlot... slotsToCheck) {
        if (tag == null || slotsToCheck == null) return false;

        for (CyberwareSlot slot : slotsToCheck) {
            InstalledCyberware[] arr = slots.get(slot);
            if (arr == null) continue;

            for (InstalledCyberware installed : arr) {
                if (installed == null) continue;

                ItemStack stack = installed.getItem();
                if (stack == null || stack.isEmpty()) continue;
                if (!stack.is(tag)) continue;
                if (!CyberwareDurabilityManager.isFunctional(player, installed)) continue;

                return true;
            }
        }

        return false;
    }

    public boolean hasFunctionalOrgan(Player player, CyberwareSlot slot) {
        InstalledCyberware[] arr = slots.get(slot);
        if (arr == null) return false;

        for (InstalledCyberware installed : arr) {
            if (installed == null) continue;

            ItemStack stack = installed.getItem();
            if (stack == null || stack.isEmpty()) continue;
            if (!CyberwareDurabilityManager.isFunctional(player, installed)) continue;

            return true;
        }

        return false;
    }

    public boolean isOrganFunctionallyReplaced(Player player, CyberwareSlot slot) {
        InstalledCyberware[] arr = slots.get(slot);
        if (arr == null) return false;

        for (InstalledCyberware installed : arr) {
            if (installed == null) continue;

            ItemStack stack = installed.getItem();
            if (stack == null || stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof ICyberwareItem item)) continue;
            if (!item.replacesOrgan()) continue;
            if (!item.getReplacedOrgans().contains(slot)) continue;
            if (!CyberwareDurabilityManager.isFunctional(player, installed)) continue;

            return true;
        }

        return false;
    }

    /* ---------------- SPINAL INJECTOR INVENTORY ---------------- */

    public ItemStack getSpinalInjectorStack(int slot) {
        if (slot < 0 || slot >= spinalInjectorInv.length) return ItemStack.EMPTY;
        ItemStack st = spinalInjectorInv[slot];
        return st == null ? ItemStack.EMPTY : st;
    }

    public void setSpinalInjectorStack(int slot, ItemStack stack) {
        if (slot < 0 || slot >= spinalInjectorInv.length) return;

        if (stack == null || stack.isEmpty() || !SpinalInjectorItem.isInjectable(stack)) {
            spinalInjectorInv[slot] = ItemStack.EMPTY;
            dirty = true;
            return;
        }

        ItemStack copy = stack.copy();
        int cap = SpinalInjectorItem.maxStackFor(copy);
        if (copy.getCount() > cap) copy.setCount(cap);

        spinalInjectorInv[slot] = copy;
        dirty = true;
    }

    public void clearSpinalInjectorInventory() {
        for (int i = 0; i < spinalInjectorInv.length; i++) {
            spinalInjectorInv[i] = ItemStack.EMPTY;
        }
        dirty = true;
    }

    /* ---------------- ARM CANNON 4-SLOT INVENTORY ---------------- */

    public ItemStack getArmCannonStack(int slot) {
        if (slot < 0 || slot >= armCannonInv.length) return ItemStack.EMPTY;
        ItemStack st = armCannonInv[slot];
        return st == null ? ItemStack.EMPTY : st;
    }

    public void setArmCannonStack(int slot, ItemStack stack) {
        if (slot < 0 || slot >= armCannonInv.length) return;

        if (stack == null || stack.isEmpty() || !ArmCannonItem.isValidStoredItem(stack)) {
            armCannonInv[slot] = ItemStack.EMPTY;
            dirty = true;
            return;
        }

        ItemStack copy = stack.copy();

        int cap = Math.max(1, copy.getMaxStackSize());
        if (copy.getCount() > cap) copy.setCount(cap);

        armCannonInv[slot] = copy;
        dirty = true;
    }

    public void clearArmCannonInventory() {
        for (int i = 0; i < armCannonInv.length; i++) {
            armCannonInv[i] = ItemStack.EMPTY;
        }
        dirty = true;
    }

    @Override
    public void clear() {
        for (CyberwareSlot slot : CyberwareSlot.values()) {
            slots.put(slot, new InstalledCyberware[slot.size]);

            boolean[] en = enabled.get(slot);
            if (en == null || en.length != slot.size) {
                en = new boolean[slot.size];
                enabled.put(slot, en);
            }
            for (int i = 0; i < en.length; i++) {
                en[i] = true;
            }
        }

        for (int i = 0; i < spinalInjectorInv.length; i++) {
            spinalInjectorInv[i] = ItemStack.EMPTY;
        }
        for (int i = 0; i < armCannonInv.length; i++) {
            armCannonInv[i] = ItemStack.EMPTY;
        }
        for (int i = 0; i < chipwareInv.length; i++) {
            chipwareInv[i] = ItemStack.EMPTY;
        }
        for (int i = 0; i < cyberdeckInv.length; i++) {
            cyberdeckInv[i] = ItemStack.EMPTY;
        }
        for (int i = 0; i < vampyresInv.length; i++) {
            vampyresInv[i] = ItemStack.EMPTY;
        }
        for (int i = 0; i < heatEngineInv.length; i++) {
            heatEngineInv[i] = ItemStack.EMPTY;
        }

        heatEngineBurnTime = 0;
        heatEngineBurnTimeTotal = 0;
        heatEngineCookTime = 0;
        heatEngineCookTimeTotal = 200;

        armCannonSelected = 0;
        copernicusOxygen = 0;
        copernicusOxygenatedEnvironment = false;
        copernicusOxygenSecondTicker = 0;

        legacyHumanity = ConfigValues.BASE_HUMANITY;
        legacyHumanityBonus = 0;
        legacyHumanityPenalties.clear();

        dataIntegrity = -1.0D;
        ticksSinceIntegrityBootDown = 0;
        integrityBedTicks = 0;

        energyStored = 0;
        cyberdeckRam = -1;
        cyberdeckRamRegenTicks = 0;
        neuropozyneApplyCount = 0;

        dirty = true;
    }

    /* ---------------- COPERNICUS BREATHING STUFF ---------------- */

    public int getCopernicusOxygen() {
        return Math.max(0, copernicusOxygen);
    }

    public void setCopernicusOxygen(int value, int maxDisplayUnits) {
        int max = Math.max(0, maxDisplayUnits);
        int clamped = Mth.clamp(value, 0, max);
        if (clamped != copernicusOxygen) {
            copernicusOxygen = clamped;
            dirty = true;
        }
    }

    public boolean getCopernicusOxygenatedEnvironment() {
        return copernicusOxygenatedEnvironment;
    }

    public void setCopernicusOxygenatedEnvironment(boolean oxygenated) {
        copernicusOxygenatedEnvironment = oxygenated;
    }

    public void tickCopernicusOxygen(boolean oxygenatedEnvironment, int depletionPerSecond, int rechargePerSecond, int maxDisplayUnits) {
        copernicusOxygenSecondTicker++;
        if (copernicusOxygenSecondTicker < 20) return;
        copernicusOxygenSecondTicker = 0;

        int oxy = getCopernicusOxygen();

        if (oxygenatedEnvironment) {
            oxy = Math.min(maxDisplayUnits, oxy + Math.max(0, rechargePerSecond));
        } else {
            oxy = Math.max(0, oxy - Math.max(0, depletionPerSecond));
        }

        setCopernicusOxygen(oxy, maxDisplayUnits);
    }

    /* ---------------- HEAT ENGINE ---------------- */

    public boolean isHeatEngineActive() {
        return heatEngineBurnTime > 0;
    }

    public int getHeatEngineBurnTime() {
        return Math.max(0, heatEngineBurnTime);
    }

    public int getHeatEngineBurnTimeTotal() {
        return Math.max(0, heatEngineBurnTimeTotal);
    }

    public int getHeatEngineCookTime() {
        return Math.max(0, heatEngineCookTime);
    }

    public int getHeatEngineCookTimeTotal() {
        return Math.max(1, heatEngineCookTimeTotal);
    }

    public ItemStack getHeatEngineStack(int slot) {
        if (slot < 0 || slot >= heatEngineInv.length) return ItemStack.EMPTY;
        ItemStack st = heatEngineInv[slot];
        return st == null ? ItemStack.EMPTY : st;
    }

    public void setHeatEngineStack(int slot, ItemStack stack) {
        if (slot < 0 || slot >= heatEngineInv.length) return;

        if (stack == null || stack.isEmpty()) {
            heatEngineInv[slot] = ItemStack.EMPTY;
            dirty = true;
            return;
        }

        ItemStack copy = stack.copy();
        int cap = Math.max(1, copy.getMaxStackSize());
        if (copy.getCount() > cap) copy.setCount(cap);

        heatEngineInv[slot] = copy;
        dirty = true;
    }

    public ItemStack removeHeatEngineStack(int slot, int amount) {
        if (amount <= 0) return ItemStack.EMPTY;

        ItemStack cur = getHeatEngineStack(slot);
        if (cur.isEmpty()) return ItemStack.EMPTY;

        int taken = Math.min(amount, cur.getCount());
        ItemStack out = cur.copy();
        out.setCount(taken);

        cur.shrink(taken);
        if (cur.isEmpty()) {
            heatEngineInv[slot] = ItemStack.EMPTY;
        }

        dirty = true;
        return out;
    }

    public void tickHeatEngine(ServerPlayer player) {
        if (player == null) return;

        Level level = player.level();
        if (level.isClientSide) return;

        if (heatEngineBurnTime <= 0) {
            ItemStack fuel = getHeatEngineStack(HEAT_ENGINE_FUEL);
            if (!fuel.isEmpty() && AbstractFurnaceBlockEntity.isFuel(fuel)) {
                int burn = fuel.getBurnTime(RecipeType.SMELTING);
                if (burn > 0) {
                    ItemStack remainder = ItemStack.EMPTY;
                    Item remainderItem = fuel.getItem().getCraftingRemainingItem();
                    if (remainderItem != null) {
                        remainder = new ItemStack(remainderItem);
                    }

                    removeHeatEngineStack(HEAT_ENGINE_FUEL, 1);

                    if (!remainder.isEmpty()) {
                        if (!player.getInventory().add(remainder)) {
                            player.drop(remainder, false);
                        }
                    }

                    heatEngineBurnTime = burn;
                    heatEngineBurnTimeTotal = burn;
                    dirty = true;
                }
            }
        }

        if (heatEngineBurnTime > 0) {
            heatEngineBurnTime--;
            receiveEnergy(player, 50);
            dirty = true;

            tickHeatEngineSmelt(level);
        } else {
            if (heatEngineCookTime != 0) {
                heatEngineCookTime = 0;
                dirty = true;
            }
        }
    }

    private void tickHeatEngineSmelt(Level level) {
        ItemStack in = getHeatEngineStack(HEAT_ENGINE_INPUT);
        if (in.isEmpty()) {
            if (heatEngineCookTime != 0) {
                heatEngineCookTime = 0;
                dirty = true;
            }
            return;
        }

        SingleRecipeInput input = new SingleRecipeInput(in);
        var opt = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, level);
        if (opt.isEmpty()) {
            if (heatEngineCookTime != 0) {
                heatEngineCookTime = 0;
                dirty = true;
            }
            return;
        }

        RecipeHolder<SmeltingRecipe> holder = opt.get();
        ItemStack result = holder.value().assemble(input, level.registryAccess());
        if (result.isEmpty() || !canAcceptSmeltResult(result)) {
            if (heatEngineCookTime != 0) {
                heatEngineCookTime = 0;
                dirty = true;
            }
            return;
        }

        heatEngineCookTimeTotal = Math.max(1, holder.value().getCookingTime());
        heatEngineCookTime++;
        dirty = true;

        if (heatEngineCookTime >= heatEngineCookTimeTotal) {
            removeHeatEngineStack(HEAT_ENGINE_INPUT, 1);

            ItemStack out = getHeatEngineStack(HEAT_ENGINE_OUTPUT);
            if (out.isEmpty()) {
                setHeatEngineStack(HEAT_ENGINE_OUTPUT, result);
            } else {
                out.grow(result.getCount());
                setHeatEngineStack(HEAT_ENGINE_OUTPUT, out);
            }

            heatEngineCookTime = 0;
            dirty = true;
        }
    }

    private boolean canAcceptSmeltResult(ItemStack result) {
        ItemStack out = getHeatEngineStack(HEAT_ENGINE_OUTPUT);
        if (out.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(out, result)) return false;

        int max = out.getMaxStackSize();
        return out.getCount() + result.getCount() <= max;
    }

    public void setHeatEngineBurnTime(int v) {
        heatEngineBurnTime = Math.max(0, v);
        dirty = true;
    }

    public void setHeatEngineBurnTimeTotal(int v) {
        heatEngineBurnTimeTotal = Math.max(0, v);
        dirty = true;
    }

    public void setHeatEngineCookTime(int v) {
        heatEngineCookTime = Math.max(0, v);
        dirty = true;
    }

    public void setHeatEngineCookTimeTotal(int v) {
        heatEngineCookTimeTotal = Math.max(1, v);
        dirty = true;
    }

    /* ---------------- IMPLANT INVENTORY EJECTION ---------------- */

    public void ejectInventoryForRemovedCyberware(Player player, ItemStack removedCyberware) {
        if (player == null) return;
        if (removedCyberware == null || removedCyberware.isEmpty()) return;
        if (player.level().isClientSide) return;

        Item item = removedCyberware.getItem();

        if (item instanceof ChipwareSlotsItem) {
            ejectChipwareInventory(player);
            return;
        }

        if (item instanceof CyberdeckItem) {
            ejectCyberdeckInventory(player);
            return;
        }

        if (item instanceof SpinalInjectorItem) {
            if (player instanceof ServerPlayer serverPlayer) {
                SpinalInjectorItem.dropAndClearInstalledStack(
                        serverPlayer,
                        serverPlayer.level().registryAccess(),
                        removedCyberware
                );
            }

            clearSpinalInjectorInventory();
            return;
        }

        if (item instanceof ArmCannonItem) {
            ejectArmCannonInventory(player);
            return;
        }

        if (item instanceof VampyresItem) {
            ejectVampyresInventory(player);
            return;
        }

        if (item instanceof HeatEngineItem) {
            ejectHeatEngineInventory(player);
        }
    }

    public void ejectChipwareInventory(Player player) {
        for (int slot = 0; slot < chipwareInv.length; slot++) {
            giveOrDropStoredStack(player, chipwareInv[slot]);
            chipwareInv[slot] = ItemStack.EMPTY;
        }

        chipwareQuickhackFiredMask = 0;
        dirty = true;
    }

    public void ejectCyberdeckInventory(Player player) {
        for (int slot = 0; slot < cyberdeckInv.length; slot++) {
            giveOrDropStoredStack(player, cyberdeckInv[slot]);
            cyberdeckInv[slot] = ItemStack.EMPTY;
        }

        dirty = true;
    }

    public void ejectSpinalInjectorInventory(Player player) {
        for (int slot = 0; slot < spinalInjectorInv.length; slot++) {
            giveOrDropStoredStack(player, spinalInjectorInv[slot]);
            spinalInjectorInv[slot] = ItemStack.EMPTY;
        }

        dirty = true;
    }

    public void ejectArmCannonInventory(Player player) {
        for (int slot = 0; slot < armCannonInv.length; slot++) {
            giveOrDropStoredStack(player, armCannonInv[slot]);
            armCannonInv[slot] = ItemStack.EMPTY;
        }

        armCannonSelected = 0;
        dirty = true;
    }

    public void ejectHeatEngineInventory(Player player) {
        for (int slot = 0; slot < heatEngineInv.length; slot++) {
            giveOrDropStoredStack(player, heatEngineInv[slot]);
            heatEngineInv[slot] = ItemStack.EMPTY;
        }

        heatEngineBurnTime = 0;
        heatEngineBurnTimeTotal = 0;
        heatEngineCookTime = 0;
        heatEngineCookTimeTotal = 200;

        dirty = true;
    }

    private static void giveOrDropStoredStack(Player player, ItemStack storedStack) {
        if (player == null) return;
        if (storedStack == null || storedStack.isEmpty()) return;

        ItemStack remaining = storedStack.copy();

        if (!player.getInventory().add(remaining)) {
            player.drop(remaining, false);
        }
    }

    /* ---------------- DATA AND RESET ---------------- */

    @Override
    public void setDirty() {
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clean() {
        dirty = false;
    }

    public void resetToDefaultOrgans() {
        for (CyberwareSlot slot : CyberwareSlot.values()) {
            InstalledCyberware[] arr = getAll().get(slot);
            if (arr == null) continue;

            for (int i = 0; i < arr.length; i++) {
                ItemStack def = DefaultOrgans.get(slot, i);

                if (def == null || def.isEmpty()) {
                    arr[i] = null;
                    setEnabled(slot, i, true);
                    continue;
                }

                int humanityCost = 0;

                if (def.getItem() instanceof ICyberwareItem cyberwareItem) {
                    humanityCost = cyberwareItem.getHumanityCost();
                }

                arr[i] = new InstalledCyberware(def, slot, i, humanityCost);
                arr[i].setPowered(true);
                setEnabled(slot, i, true);
            }
        }

        refreshLegacyHumanitySnapshot();
        dirty = true;
    }

    /* ---------------- CYBERDECK RAM ---------------- */

    public int getCyberdeckRam() {
        return Math.max(0, cyberdeckRam);
    }

    public boolean hasInitializedCyberdeckRam() {
        return cyberdeckRam >= 0;
    }

    public int getMaxCyberdeckRam(Player player) {
        if (player == null) return 0;
        return Math.max(0, Mth.floor(player.getAttributeValue(ModAttributes.RAM)));
    }

    public int getCyberdeckRamRegen(Player player) {
        if (player == null) return 0;
        return Math.max(0, Mth.floor(player.getAttributeValue(ModAttributes.RAM_REGEN)));
    }

    public void initializeCyberdeckRam(Player player) {
        int maxRam = getMaxCyberdeckRam(player);
        int nextRam = maxRam > 0 ? maxRam : -1;

        if (cyberdeckRam != nextRam || cyberdeckMaxRam != maxRam || cyberdeckRamRegenTicks != 0) {
            cyberdeckRam = nextRam;
            cyberdeckMaxRam = maxRam;
            cyberdeckRamRegenTicks = 0;
            dirty = true;
        }
    }

    public void setCyberdeckRam(Player player, int value) {
        int maxRam = getMaxCyberdeckRam(player);

        cyberdeckMaxRam = maxRam;

        if (maxRam <= 0) {
            if (cyberdeckRam != -1 || cyberdeckRamRegenTicks != 0) {
                cyberdeckRam = -1;
                cyberdeckRamRegenTicks = 0;
                dirty = true;
            }

            return;
        }

        int clamped = Mth.clamp(value, 0, maxRam);

        if (cyberdeckRam != clamped) {
            cyberdeckRam = clamped;
            dirty = true;
        }
    }

    public boolean tryConsumeCyberdeckRam(Player player, int amount) {
        if (amount <= 0) return true;

        int maxRam = getMaxCyberdeckRam(player);
        if (maxRam <= 0) return false;

        cyberdeckMaxRam = maxRam;

        if (!hasInitializedCyberdeckRam()) {
            cyberdeckRam = maxRam;
            cyberdeckRamRegenTicks = 0;
            dirty = true;
        } else if (cyberdeckRam > maxRam) {
            cyberdeckRam = maxRam;
            dirty = true;
        }

        if (cyberdeckRam < amount) return false;

        cyberdeckRam -= amount;
        cyberdeckRamRegenTicks = 0;
        dirty = true;

        return true;
    }

    public int receiveCyberdeckRam(Player player, int amount) {
        if (amount <= 0) return 0;

        int maxRam = getMaxCyberdeckRam(player);
        if (maxRam <= 0) return 0;

        cyberdeckMaxRam = maxRam;

        if (!hasInitializedCyberdeckRam()) {
            cyberdeckRam = maxRam;
            cyberdeckRamRegenTicks = 0;
            dirty = true;
            return 0;
        }

        int accepted = Math.min(amount, Math.max(0, maxRam - cyberdeckRam));
        if (accepted <= 0) return 0;

        cyberdeckRam += accepted;
        dirty = true;

        return accepted;
    }

    public boolean tickCyberdeckRam(Player player) {
        if (player == null || player.level().isClientSide) return false;

        int maxRam = getMaxCyberdeckRam(player);

        if (maxRam <= 0) {
            boolean changed = cyberdeckRam != -1 || cyberdeckMaxRam != 0;

            cyberdeckRam = -1;
            cyberdeckMaxRam = 0;
            cyberdeckRamRegenTicks = 0;

            if (changed) {
                dirty = true;
            }

            return changed;
        }

        if (!hasInitializedCyberdeckRam()) {
            cyberdeckRam = maxRam;
            cyberdeckMaxRam = maxRam;
            cyberdeckRamRegenTicks = 0;
            dirty = true;
            return true;
        }

        if (cyberdeckMaxRam < 0) {
            cyberdeckMaxRam = maxRam;
            dirty = true;
            return true;
        }

        if (maxRam != cyberdeckMaxRam) {
            int oldMaxRam = cyberdeckMaxRam;

            cyberdeckMaxRam = maxRam;
            cyberdeckRamRegenTicks = 0;

            if (maxRam < oldMaxRam) {
                cyberdeckRam = Math.min(cyberdeckRam, maxRam);
            } else if (maxRam > oldMaxRam && cyberdeckRam >= oldMaxRam) {
                cyberdeckRam = maxRam;
            }

            dirty = true;
            return true;
        }

        if (cyberdeckRam > maxRam) {
            cyberdeckRam = maxRam;
            cyberdeckRamRegenTicks = 0;
            dirty = true;
            return true;
        }

        if (cyberdeckRam >= maxRam) {
            cyberdeckRamRegenTicks = 0;
            return false;
        }

        cyberdeckRamRegenTicks++;

        if (cyberdeckRamRegenTicks < 100) {
            return false;
        }

        cyberdeckRamRegenTicks = 0;

        int regenAmount = getCyberdeckRamRegen(player);
        if (regenAmount <= 0) return false;

        int oldRam = cyberdeckRam;
        cyberdeckRam = Math.min(maxRam, cyberdeckRam + regenAmount);

        if (cyberdeckRam == oldRam) return false;

        dirty = true;
        return true;
    }

    public void resetCyberdeckRam() {
        if (cyberdeckRam != -1 || cyberdeckMaxRam != -1 || cyberdeckRamRegenTicks != 0) {
            cyberdeckRam = -1;
            cyberdeckMaxRam = -1;
            cyberdeckRamRegenTicks = 0;
            dirty = true;
        }
    }

    /* ---------------- ENERGY HELPERS ---------------- */

    public int getEnergyStored() {
        return energyStored;
    }

    public void setEnergyStored(Player player, int value) {
        int cap = getTotalEnergyCapacity(player);
        int clamped = Mth.clamp(value, 0, cap);
        if (clamped != energyStored) {
            energyStored = clamped;
            dirty = true;
        }
    }

    public int getTotalEnergyCapacity(Player player) {
        int total = 0;

        for (var entry : slots.entrySet()) {
            CyberwareSlot slot = entry.getKey();
            InstalledCyberware[] arr = entry.getValue();
            if (arr == null) continue;

            for (InstalledCyberware cw : arr) {
                if (cw == null) continue;

                ItemStack stack = cw.getItem();
                if (stack == null || stack.isEmpty()) continue;

                if (!(stack.getItem() instanceof ICyberwareItem item)) continue;
                if (!CyberwareDurabilityManager.isFunctional(player, cw)) continue;

                int cap = item.getEnergyCapacity(player, stack, slot);
                if (cap > 0) total += cap;
            }
        }

        return Math.max(0, total);
    }

    public int receiveEnergy(Player player, int amount) {
        if (amount <= 0) return 0;

        int cap = getTotalEnergyCapacity(player);
        if (cap <= 0) return 0;

        int before = energyStored;
        int after = Mth.clamp(before + amount, 0, cap);

        if (after != before) {
            energyStored = after;
            dirty = true;
        }

        int received = after - before;

        if (received > 0 && player != null && !player.level().isClientSide) {
            CyberwareDurabilityManager.recordBatteryEnergyReceived(player, this, received);
        }

        return received;
    }

    public int extractEnergy(Player player, int amount) {
        if (amount <= 0) return 0;

        int before = energyStored;
        int after = Math.max(0, before - amount);

        if (after != before) {
            energyStored = after;
            dirty = true;
        }

        int extracted = before - after;

        if (extracted > 0 && player != null && !player.level().isClientSide) {
            CyberwareDurabilityManager.recordBatteryEnergyExtracted(player, this, extracted);
        }

        return extracted;
    }

    public int extractEnergy(int amount) {
        return extractEnergy(null, amount);
    }

    public boolean tryConsumeEnergy(Player player, int amount) {
        if (amount <= 0) return true;
        if (energyStored < amount) return false;

        energyStored -= amount;
        dirty = true;

        if (player != null && !player.level().isClientSide) {
            CyberwareDurabilityManager.recordBatteryEnergyExtracted(player, this, amount);
        }

        return true;
    }

    public boolean tryConsumeEnergy(int amount) {
        return tryConsumeEnergy(null, amount);
    }

    public void clampEnergyToCapacity(Player player) {
        int cap = getTotalEnergyCapacity(player);
        int clamped = Mth.clamp(energyStored, 0, cap);
        if (clamped != energyStored) {
            energyStored = clamped;
            dirty = true;
        }
    }

    public int getNeuropozyneApplyCount() {
        return Math.max(0, neuropozyneApplyCount);
    }

    public int incrementNeuropozyneApplyCount() {
        neuropozyneApplyCount = Math.max(0, neuropozyneApplyCount) + 1;
        dirty = true;
        return neuropozyneApplyCount;
    }

    public void resetNeuropozyneApplyCount() {
        neuropozyneApplyCount = 0;
        dirty = true;
    }

    /* ---------------- NBT ---------------- */

    private static CompoundTag cc$saveStackToCompound(HolderLookup.Provider provider, ItemStack stack) {
        Tag t = stack.save(provider);
        return (t instanceof CompoundTag ct) ? ct : new CompoundTag();
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();

        for (var entry : slots.entrySet()) {
            CyberwareSlot slot = entry.getKey();
            InstalledCyberware[] arr = entry.getValue();
            if (arr == null) continue;

            for (int i = 0; i < arr.length; i++) {
                InstalledCyberware installed = arr[i];
                if (installed == null) continue;

                ItemStack stack = installed.getItem();
                if (stack == null || stack.isEmpty()) continue;

                CompoundTag c = installed.save(provider);

                c.putString("SlotGroup", slot.name());
                c.putInt("Index", i);
                c.putBoolean("Enabled", isEnabled(slot, i));

                list.add(c);
            }
        }

        tag.put(NBT_CYBERWARE, list);
        tag.putInt(NBT_HUMANITY, legacyHumanity);
        tag.putInt(NBT_HUMANITY_BONUS, legacyHumanityBonus);
        tag.putInt(NBT_ENERGY, energyStored);
        tag.putInt(NBT_CYBERDECK_RAM, cyberdeckRam);
        tag.putInt(NBT_CYBERDECK_MAX_RAM, cyberdeckMaxRam);
        tag.putInt(NBT_CYBERDECK_RAM_REGEN_TICKS, cyberdeckRamRegenTicks);
        tag.putDouble(NBT_DATA_INTEGRITY, dataIntegrity);
        tag.putInt(NBT_TICKS_SINCE_INTEGRITY_BOOT_DOWN, ticksSinceIntegrityBootDown);
        tag.putInt(NBT_INTEGRITY_BED_TICKS, integrityBedTicks);

        CompoundTag penalties = new CompoundTag();
        for (Map.Entry<String, Integer> e : legacyHumanityPenalties.entrySet()) {
            String k = e.getKey();
            if (k == null || k.isBlank()) continue;
            penalties.putInt(k, Math.max(0, e.getValue() == null ? 0 : e.getValue()));
        }
        tag.put(NBT_HUMANITY_PENALTIES, penalties);

        tag.putInt(NBT_ARM_CANNON_SELECTED, getArmCannonSelected());

        ListTag inj = new ListTag();
        for (int i = 0; i < spinalInjectorInv.length; i++) {
            CompoundTag c = new CompoundTag();
            ItemStack st = spinalInjectorInv[i];
            if (st != null && !st.isEmpty() && SpinalInjectorItem.isInjectable(st)) {
                ItemStack copy = st.copy();
                int cap = SpinalInjectorItem.maxStackFor(copy);
                if (copy.getCount() > cap) copy.setCount(cap);
                copy.save(provider, c);
            }
            inj.add(c);
        }
        tag.put(NBT_SPINAL_INJECTOR_INV, inj);

        ListTag arm = new ListTag();
        for (int i = 0; i < armCannonInv.length; i++) {
            CompoundTag c = new CompoundTag();
            ItemStack st = armCannonInv[i];

            if (st != null && !st.isEmpty()) {
                ItemStack copy = st.copy();
                int cap = Math.max(1, copy.getMaxStackSize());
                if (copy.getCount() > cap) copy.setCount(cap);
                copy.save(provider, c);
            }

            arm.add(c);
        }
        tag.put(NBT_ARM_CANNON_INV, arm);

        ListTag chip = new ListTag();
        for (int i = 0; i < chipwareInv.length; i++) {
            ItemStack st = chipwareInv[i];

            if (st != null && !st.isEmpty() && st.is(ModTags.Items.DATA_SHARDS)) {
                ItemStack copy = st.copy();
                copy.setCount(1);

                chip.add(cc$saveStackToCompound(provider, copy));
            } else {
                chip.add(new CompoundTag());
            }
        }

        ListTag cyberdeck = new ListTag();
        for (int i = 0; i < cyberdeckInv.length; i++) {
            ItemStack st = cyberdeckInv[i];

            boolean valid = st != null && !st.isEmpty()
                    && (i < CYBERDECK_QUICKHACK_SLOT_COUNT
                    ? st.is(ModTags.Items.QUICKHACK_SHARDS)
                    : i == CYBERDECK_MICROPROGRAM_CHIP_SLOT
                    ? st.is(ModTags.Items.MICROPROGRAM_SHARDS)
                    : i >= CYBERDECK_MACROPROGRAM_SLOT_START && i < CYBERDECK_SLOT_COUNT && st.is(ModTags.Items.MACROPROGRAMS));

            if (valid) {
                ItemStack copy = st.copy();
                copy.setCount(1);

                cyberdeck.add(cc$saveStackToCompound(provider, copy));
            } else {
                cyberdeck.add(new CompoundTag());
            }
        }

        ListTag vampyres = new ListTag();

        for (int i = 0; i < vampyresInv.length; i++) {
            ItemStack stack = vampyresInv[i];

            if (stack != null && !stack.isEmpty() && VampyresItem.isInjectable(stack)) {
                ItemStack copy = stack.copy();
                copy.setCount(Math.min(VampyresItem.SLOT_STACK_LIMIT, copy.getCount()));
                vampyres.add(cc$saveStackToCompound(provider, copy));
            } else {
                vampyres.add(new CompoundTag());
            }
        }

        ListTag faceplateMaskList = new ListTag();

        for (int i = 0; i < faceplateMaskInv.length; i++) {
            ItemStack stack = faceplateMaskInv[i];
            if (stack == null || stack.isEmpty()) {
                continue;
            }

            CompoundTag entry = new CompoundTag();
            entry.putInt("Slot", i);
            entry.put("Stack", stack.save(provider));

            faceplateMaskList.add(entry);
        }

        tag.put(NBT_FACEPLATE_MASK_INV, faceplateMaskList);

        ListTag heat = new ListTag();
        for (int i = 0; i < heatEngineInv.length; i++) {
            ItemStack st = heatEngineInv[i];
            heat.add((st != null && !st.isEmpty()) ? cc$saveStackToCompound(provider, st) : new CompoundTag());
        }
        tag.put(NBT_HEAT_ENGINE_INV, heat);

        tag.putInt(NBT_HEAT_ENGINE_BURN, heatEngineBurnTime);
        tag.putInt(NBT_HEAT_ENGINE_BURN_TOTAL, heatEngineBurnTimeTotal);
        tag.putInt(NBT_HEAT_ENGINE_COOK, heatEngineCookTime);
        tag.putInt(NBT_HEAT_ENGINE_COOK_TOTAL, heatEngineCookTimeTotal);

        tag.put(NBT_CHIPWARE_INV, chip);
        tag.putInt(NBT_CHIPWARE_QUICKHACK_FIRED, chipwareQuickhackFiredMask);
        tag.put(NBT_CYBERDECK_INV, cyberdeck);

        tag.put(NBT_VAMPYRES_INV, vampyres);

        tag.putInt(NBT_NEUROPOZYNE_APPLY_COUNT, neuropozyneApplyCount);

        tag.putInt(NBT_COPERNICUS_OXYGEN, copernicusOxygen);

        return tag;
    }

    public void deserializeNBT(CompoundTag tag, HolderLookup.Provider provider) {
        clear();

        ListTag list = tag.getList(NBT_CYBERWARE, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);

            if (!c.contains("SlotGroup", Tag.TAG_STRING)) continue;

            CyberwareSlot slot = CyberwareSlot.valueOf(c.getString("SlotGroup"));
            int index = c.getInt("Index");

            InstalledCyberware loaded = InstalledCyberware.load(c, provider);

            InstalledCyberware[] arr = slots.get(slot);
            if (arr == null) continue;
            if (index < 0 || index >= arr.length) continue;

            arr[index] = loaded;

            boolean en = c.contains("Enabled", Tag.TAG_BYTE) ? c.getBoolean("Enabled") : true;
            setEnabled(slot, index, en);
        }

        legacyHumanity = tag.contains(NBT_HUMANITY, Tag.TAG_INT)
                ? Mth.clamp(tag.getInt(NBT_HUMANITY), -1000, 1000)
                : ConfigValues.BASE_HUMANITY;

        legacyHumanityBonus = tag.contains(NBT_HUMANITY_BONUS, Tag.TAG_INT)
                ? Mth.clamp(tag.getInt(NBT_HUMANITY_BONUS), -1000, 1000)
                : 0;

        refreshLegacyHumanitySnapshot();

        energyStored = tag.contains(NBT_ENERGY, Tag.TAG_INT) ? tag.getInt(NBT_ENERGY) : 0;
        cyberdeckRam = tag.contains(NBT_CYBERDECK_RAM, Tag.TAG_INT) ? tag.getInt(NBT_CYBERDECK_RAM) : -1;
        cyberdeckMaxRam = tag.contains(NBT_CYBERDECK_MAX_RAM, Tag.TAG_INT) ? tag.getInt(NBT_CYBERDECK_MAX_RAM) : -1;
        cyberdeckRamRegenTicks = tag.contains(NBT_CYBERDECK_RAM_REGEN_TICKS, Tag.TAG_INT) ? Mth.clamp(tag.getInt(NBT_CYBERDECK_RAM_REGEN_TICKS), 0, 99) : 0;

        dataIntegrity = tag.contains(NBT_DATA_INTEGRITY, Tag.TAG_DOUBLE)
                ? Mth.clamp(tag.getDouble(NBT_DATA_INTEGRITY), 0.0D, HumanityAttributeModifiers.getConfiguredBaseHumanity())
                : -1.0D;

        ticksSinceIntegrityBootDown = tag.contains(NBT_TICKS_SINCE_INTEGRITY_BOOT_DOWN, Tag.TAG_INT)
                ? Math.max(0, tag.getInt(NBT_TICKS_SINCE_INTEGRITY_BOOT_DOWN))
                : 0;

        integrityBedTicks = tag.contains(NBT_INTEGRITY_BED_TICKS, Tag.TAG_INT)
                ? Math.max(0, tag.getInt(NBT_INTEGRITY_BED_TICKS))
                : 0;

        legacyHumanityPenalties.clear();
        if (tag.contains(NBT_HUMANITY_PENALTIES, Tag.TAG_COMPOUND)) {
            CompoundTag penalties = tag.getCompound(NBT_HUMANITY_PENALTIES);
            for (String k : penalties.getAllKeys()) {
                int v = penalties.getInt(k);
                if (k != null && !k.isBlank() && v > 0) {
                    legacyHumanityPenalties.put(k, Mth.clamp(v, 0, 1000));
                }
            }
        }

        armCannonSelected = tag.contains(NBT_ARM_CANNON_SELECTED, Tag.TAG_INT)
                ? Mth.clamp(tag.getInt(NBT_ARM_CANNON_SELECTED), 0, ArmCannonItem.SLOT_COUNT - 1)
                : 0;

        for (int i = 0; i < spinalInjectorInv.length; i++) {
            spinalInjectorInv[i] = ItemStack.EMPTY;
        }

        if (tag.contains(NBT_SPINAL_INJECTOR_INV, Tag.TAG_LIST)) {
            ListTag inj = tag.getList(NBT_SPINAL_INJECTOR_INV, Tag.TAG_COMPOUND);
            for (int i = 0; i < spinalInjectorInv.length && i < inj.size(); i++) {
                CompoundTag c = inj.getCompound(i);
                ItemStack st = ItemStack.parseOptional(provider, c);

                if (st.isEmpty() || !SpinalInjectorItem.isInjectable(st)) {
                    spinalInjectorInv[i] = ItemStack.EMPTY;
                    continue;
                }

                int cap = SpinalInjectorItem.maxStackFor(st);
                if (st.getCount() > cap) st.setCount(cap);

                spinalInjectorInv[i] = st;
            }
        }

        for (int i = 0; i < armCannonInv.length; i++) {
            armCannonInv[i] = ItemStack.EMPTY;
        }

        if (tag.contains(NBT_ARM_CANNON_INV, Tag.TAG_LIST)) {
            ListTag arm = tag.getList(NBT_ARM_CANNON_INV, Tag.TAG_COMPOUND);

            for (int i = 0; i < armCannonInv.length && i < arm.size(); i++) {
                CompoundTag c = arm.getCompound(i);
                ItemStack st = ItemStack.parseOptional(provider, c);

                if (st.isEmpty()) {
                    armCannonInv[i] = ItemStack.EMPTY;
                    continue;
                }

                int cap = Math.max(1, st.getMaxStackSize());
                if (st.getCount() > cap) st.setCount(cap);

                armCannonInv[i] = st;
            }
        }

        for (int i = 0; i < chipwareInv.length; i++) {
            chipwareInv[i] = ItemStack.EMPTY;
        }

        if (tag.contains(NBT_CHIPWARE_INV, Tag.TAG_LIST)) {
            ListTag chip = tag.getList(NBT_CHIPWARE_INV, Tag.TAG_COMPOUND);
            for (int i = 0; i < chipwareInv.length && i < chip.size(); i++) {
                CompoundTag c = chip.getCompound(i);
                ItemStack st = ItemStack.parseOptional(provider, c);

                if (st.isEmpty() || !st.is(ModTags.Items.DATA_SHARDS)) {
                    chipwareInv[i] = ItemStack.EMPTY;
                    continue;
                }

                st.setCount(1);
                chipwareInv[i] = st;
            }
        }

        chipwareQuickhackFiredMask = tag.contains(NBT_CHIPWARE_QUICKHACK_FIRED, Tag.TAG_INT)
                ? tag.getInt(NBT_CHIPWARE_QUICKHACK_FIRED)
                : 0;

        int validChipwareMask = (1 << CHIPWARE_SLOT_COUNT) - 1;
        chipwareQuickhackFiredMask &= validChipwareMask;

        for (int i = 0; i < chipwareInv.length; i++) {
            if (chipwareInv[i].isEmpty()) {
                chipwareQuickhackFiredMask &= ~(1 << i);
            }
        }

        for (int i = 0; i < cyberdeckInv.length; i++) {
            cyberdeckInv[i] = ItemStack.EMPTY;
        }

        if (tag.contains(NBT_CYBERDECK_INV, Tag.TAG_LIST)) {
            ListTag cyberdeck = tag.getList(NBT_CYBERDECK_INV, Tag.TAG_COMPOUND);
            for (int i = 0; i < cyberdeckInv.length && i < cyberdeck.size(); i++) {
                CompoundTag c = cyberdeck.getCompound(i);
                ItemStack st = ItemStack.parseOptional(provider, c);

                boolean valid = !st.isEmpty()
                        && (i < CYBERDECK_QUICKHACK_SLOT_COUNT
                        ? st.is(ModTags.Items.QUICKHACK_SHARDS)
                        : i == CYBERDECK_MICROPROGRAM_CHIP_SLOT
                        ? st.is(ModTags.Items.MICROPROGRAM_SHARDS)
                        : i >= CYBERDECK_MACROPROGRAM_SLOT_START && i < CYBERDECK_SLOT_COUNT && st.is(ModTags.Items.MACROPROGRAMS));

                if (!valid) {
                    cyberdeckInv[i] = ItemStack.EMPTY;
                    continue;
                }

                st.setCount(1);
                cyberdeckInv[i] = st;
            }
        }

        if (tag.contains(NBT_FACEPLATE_MASK_INV, Tag.TAG_LIST)) {
            ListTag faceplateMaskList = tag.getList(NBT_FACEPLATE_MASK_INV, Tag.TAG_COMPOUND);

            for (int i = 0; i < faceplateMaskList.size(); i++) {
                CompoundTag entry = faceplateMaskList.getCompound(i);
                int slot = entry.getInt("Slot");

                if (slot < 0 || slot >= faceplateMaskInv.length) {
                    continue;
                }

                faceplateMaskInv[slot] = ItemStack.parseOptional(
                        provider,
                        entry.getCompound("Stack")
                );
            }
        }

        for (int i = 0; i < vampyresInv.length; i++) {
            vampyresInv[i] = ItemStack.EMPTY;
        }

        if (tag.contains(NBT_VAMPYRES_INV, Tag.TAG_LIST)) {
            ListTag vampyres = tag.getList(NBT_VAMPYRES_INV, Tag.TAG_COMPOUND);

            for (int i = 0; i < vampyresInv.length && i < vampyres.size(); i++) {
                ItemStack stack = ItemStack.parseOptional(provider, vampyres.getCompound(i));

                if (stack.isEmpty() || !VampyresItem.isInjectable(stack)) {
                    vampyresInv[i] = ItemStack.EMPTY;
                    continue;
                }

                stack.setCount(Math.min(VampyresItem.SLOT_STACK_LIMIT, stack.getCount()));
                vampyresInv[i] = stack;
            }
        }

        for (int i = 0; i < heatEngineInv.length; i++) {
            heatEngineInv[i] = ItemStack.EMPTY;
        }

        if (tag.contains(NBT_HEAT_ENGINE_INV, Tag.TAG_LIST)) {
            ListTag heat = tag.getList(NBT_HEAT_ENGINE_INV, Tag.TAG_COMPOUND);
            for (int i = 0; i < heatEngineInv.length && i < heat.size(); i++) {
                heatEngineInv[i] = ItemStack.parseOptional(provider, heat.getCompound(i));
            }
        }

        heatEngineBurnTime = tag.contains(NBT_HEAT_ENGINE_BURN, Tag.TAG_INT) ? Math.max(0, tag.getInt(NBT_HEAT_ENGINE_BURN)) : 0;
        heatEngineBurnTimeTotal = tag.contains(NBT_HEAT_ENGINE_BURN_TOTAL, Tag.TAG_INT) ? Math.max(0, tag.getInt(NBT_HEAT_ENGINE_BURN_TOTAL)) : 0;
        heatEngineCookTime = tag.contains(NBT_HEAT_ENGINE_COOK, Tag.TAG_INT) ? Math.max(0, tag.getInt(NBT_HEAT_ENGINE_COOK)) : 0;
        heatEngineCookTimeTotal = tag.contains(NBT_HEAT_ENGINE_COOK_TOTAL, Tag.TAG_INT) ? Math.max(1, tag.getInt(NBT_HEAT_ENGINE_COOK_TOTAL)) : 200;

        neuropozyneApplyCount = tag.contains(NBT_NEUROPOZYNE_APPLY_COUNT, Tag.TAG_INT)
                ? Math.max(0, tag.getInt(NBT_NEUROPOZYNE_APPLY_COUNT))
                : 0;

        copernicusOxygen = tag.contains(NBT_COPERNICUS_OXYGEN, Tag.TAG_INT)
                ? Math.max(0, tag.getInt(NBT_COPERNICUS_OXYGEN))
                : 0;

        dirty = false;
    }

    /* ---------------- COMMANDS ---------------- */

    public boolean commandInstall(Player player, ItemStack stack) {
        if (player == null) return false;
        if (stack == null || stack.isEmpty()) return false;

        Item item = stack.getItem();
        if (!(item instanceof ICyberwareItem cw)) return false;

        ItemStack installStack = stack.copy();
        installStack.setCount(1);

        InstallTarget target = findInstallTargetForCommand(installStack, cw);
        if (target == null) return false;

        if (cw.replacesOrgan()) {
            for (CyberwareSlot replaced : cw.getReplacedOrgans()) {
                removeFirstNonEmptyInSlotGroup(player, replaced);
            }
        }

        int humanityCost = cw.getHumanityCost();
        InstalledCyberware installed = new InstalledCyberware(installStack, target.slot, target.index, humanityCost);
        installed.setPowered(true);

        set(target.slot, target.index, installed);

        cw.onInstalled(player);

        recomputeHumanityBaseFromInstalled(player);
        clampEnergyToCapacity(player);

        return true;
    }

    public boolean commandRemove(Player player, Item item) {
        if (player == null) return false;
        if (item == null) return false;

        for (CyberwareSlot slot : CyberwareSlot.values()) {
            InstalledCyberware[] arr = slots.get(slot);
            if (arr == null) continue;

            for (int i = 0; i < arr.length; i++) {
                InstalledCyberware installed = arr[i];
                if (installed == null) continue;

                ItemStack st = installed.getItem();
                if (st == null || st.isEmpty()) continue;
                if (!st.is(item)) continue;

                remove(slot, i);

                if (item instanceof ICyberwareItem cw) {
                    cw.onRemoved(player);
                }

                recomputeHumanityBaseFromInstalled(player);
                clampEnergyToCapacity(player);
                return true;
            }
        }

        return false;
    }

    public Component commandListComponent() {
        MutableComponent root = Component.literal("Installed Implants:");

        boolean any = false;

        for (CyberwareSlot slot : CyberwareSlot.values()) {
            InstalledCyberware[] arr = slots.get(slot);
            if (arr == null) continue;

            StringBuilder sb = new StringBuilder();
            for (InstalledCyberware installed : arr) {
                if (installed == null) continue;

                ItemStack st = installed.getItem();
                if (st == null || st.isEmpty()) continue;

                boolean isDefault = false;
                for (int i = 0; i < arr.length; i++) {
                    ItemStack def = DefaultOrgans.get(slot, i);
                    if (def != null && !def.isEmpty() && ItemStack.isSameItemSameComponents(st, def)) {
                        isDefault = true;
                        break;
                    }
                }

                if (isDefault) continue;

                if (sb.length() > 0) sb.append(", ");
                sb.append(st.getHoverName().getString());
            }

            if (sb.isEmpty()) continue;

            any = true;
            root.append(Component.literal("\n" + slot.name() + ": " + sb));
        }

        if (!any) {
            root.append(Component.literal("\n(X)"));
        }

        return root;
    }

    /* ---------------- internal helpers for commands ---------------- */

    private record InstallTarget(CyberwareSlot slot, int index) {}

    private InstallTarget findInstallTargetForCommand(ItemStack stack, ICyberwareItem cw) {
        if (cw.replacesOrgan()) {
            for (CyberwareSlot replaced : cw.getReplacedOrgans()) {
                InstallTarget t = findFirstValidSpaceInSlotGroup(stack, cw, replaced);
                if (t != null) return t;
            }
        }

        for (CyberwareSlot slot : CyberwareSlot.values()) {
            if (!cw.supportsSlot(slot)) continue;

            InstallTarget t = findFirstValidSpaceInSlotGroup(stack, cw, slot);
            if (t != null) return t;
        }

        return null;
    }

    private InstallTarget findFirstValidSpaceInSlotGroup(ItemStack incoming, ICyberwareItem cw, CyberwareSlot slot) {
        InstalledCyberware[] arr = slots.get(slot);
        if (arr == null) return null;
        if (incoming == null || incoming.isEmpty()) return null;

        int max = Math.max(1, cw.maxStacksPerSlotType(incoming, slot));
        int already = 0;

        for (InstalledCyberware installed : arr) {
            if (installed == null) continue;

            ItemStack st = installed.getItem();
            if (st == null || st.isEmpty()) continue;

            if (st.is(incoming.getItem())) {
                already++;
            }
        }

        if (already >= max) return null;

        for (int i = 0; i < arr.length; i++) {
            InstalledCyberware installed = arr[i];

            if (installed == null || installed.getItem() == null || installed.getItem().isEmpty()) {
                return new InstallTarget(slot, i);
            }
        }

        return null;
    }

    private InstalledCyberware removeFirstNonEmptyInSlotGroup(Player player, CyberwareSlot slot) {
        InstalledCyberware[] arr = slots.get(slot);
        if (arr == null) return null;

        for (int i = 0; i < arr.length; i++) {
            InstalledCyberware installed = arr[i];
            if (installed == null) continue;

            ItemStack st = installed.getItem();
            if (st == null || st.isEmpty()) continue;

            InstalledCyberware removed = remove(slot, i);

            Item removedItem = st.getItem();
            if (removedItem instanceof ICyberwareItem cw) {
                cw.onRemoved(player);
            }

            return removed;
        }

        return null;
    }

    public static CompoundTag createSnapshotTagFor(Player player, HolderLookup.Provider provider) {
        PlayerCyberwareData data = PlayerCyberwareData.getForVisual(player, player.registryAccess());
        if (data == null) return new CompoundTag();
        return data.serializeNBT(provider);
    }

    public static PlayerCyberwareData fromSnapshotTag(CompoundTag tag, HolderLookup.Provider provider) {
        PlayerCyberwareData d = new PlayerCyberwareData();
        if (tag != null && !tag.isEmpty()) {
            d.deserializeNBT(tag, provider);
        }
        return d;
    }

    public static PlayerCyberwareData getForVisual(Player player, HolderLookup.Provider provider) {
        if (player == null) return null;

        if (player instanceof ICyberwareVisualDataHolder holder) {
            PlayerCyberwareData visualData = holder.createcybernetics$getCyberwareVisualData(provider);
            if (visualData != null) {
                return visualData;
            }
        }

        CompoundTag pd = player.getPersistentData();
        if (pd.getBoolean(HOLO_SNAPSHOT_FLAG) && pd.contains(HOLO_SNAPSHOT_CYBERWARE, Tag.TAG_COMPOUND)) {
            CompoundTag snap = pd.getCompound(HOLO_SNAPSHOT_CYBERWARE);
            return fromSnapshotTag(snap, provider);
        }

        return player.getData(ModAttachments.CYBERWARE);
    }
}