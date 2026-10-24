package com.perigrine3.createcybernetics.common.capabilities;

import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareData;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.client.TrimColorPresets;
import com.perigrine3.createcybernetics.common.surgery.DefaultOrgans;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

import java.util.EnumMap;
import java.util.Map;

public class EntityCyberwareData implements ICyberwareData {

    private static final String NBT_CYBERWARE = "Cyberware";
    private static final String NBT_ENERGY = "Energy";
    private static final String NBT_CHIPWARE = "Chipware";

    private final EnumMap<CyberwareSlot, InstalledCyberware[]> slots =
            new EnumMap<>(CyberwareSlot.class);

    private final EnumMap<CyberwareSlot, boolean[]> enabled =
            new EnumMap<>(CyberwareSlot.class);

    private final ItemStack[] chipware = new ItemStack[PlayerCyberwareData.CHIPWARE_SLOT_COUNT];

    private boolean dirty = false;
    private int energyStored = 0;

    private int loadoutVersion = 1;
    private transient int cachedTickableVersion = 0;
    private transient java.util.List<com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.InstalledSlotRef> cachedTickableSlots = java.util.List.of();
    private transient boolean cachedEyeOfDefender = false;

    public int getLoadoutVersion() {
        return loadoutVersion;
    }

    public void incrementLoadoutVersion() {
        loadoutVersion++;
    }

    public int getCachedTickableVersion() {
        return cachedTickableVersion;
    }

    public java.util.List<com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.InstalledSlotRef> getCachedTickableSlots() {
        return cachedTickableSlots;
    }

    public boolean hasCachedEyeOfDefender() {
        return cachedEyeOfDefender;
    }

    public void updateEntityCache(
            int version,
            java.util.List<com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.InstalledSlotRef> tickableSlots,
            boolean hasEyeOfDefender
    ) {
        this.cachedTickableVersion = version;
        this.cachedTickableSlots = tickableSlots;
        this.cachedEyeOfDefender = hasEyeOfDefender;
    }

    public EntityCyberwareData() {
        for (CyberwareSlot slot : CyberwareSlot.values()) {
            slots.put(slot, new InstalledCyberware[slot.size]);

            boolean[] en = new boolean[slot.size];
            for (int i = 0; i < en.length; i++) {
                en[i] = true;
            }
            enabled.put(slot, en);
        }

        for (int i = 0; i < chipware.length; i++) {
            chipware[i] = ItemStack.EMPTY;
        }
    }

    @Override
    public InstalledCyberware get(CyberwareSlot slot, int index) {
        InstalledCyberware[] arr = slots.get(slot);
        if (arr == null || index < 0 || index >= arr.length) return null;
        return arr[index];
    }

    @Override
    public void set(CyberwareSlot slot, int index, InstalledCyberware cyberware) {
        InstalledCyberware[] arr = slots.get(slot);
        if (arr == null || index < 0 || index >= arr.length) return;

        arr[index] = cyberware;

        boolean[] en = enabled.get(slot);
        if (en != null && index < en.length) {
            en[index] = true;
        }

        incrementLoadoutVersion();
        dirty = true;
    }

    @Override
    public InstalledCyberware remove(CyberwareSlot slot, int index) {
        InstalledCyberware[] arr = slots.get(slot);
        if (arr == null || index < 0 || index >= arr.length) return null;

        InstalledCyberware old = arr[index];
        arr[index] = null;

        boolean[] en = enabled.get(slot);
        if (en != null && index >= 0 && index < en.length) {
            en[index] = true;
        }

        incrementLoadoutVersion();
        dirty = true;
        return old;
    }

    @Override
    public Map<CyberwareSlot, InstalledCyberware[]> getAll() {
        return slots;
    }

    @Override
    public int getHumanity() {
        return 0;
    }

    @Override
    public void setHumanity(int value) {
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

        for (int i = 0; i < chipware.length; i++) {
            chipware[i] = ItemStack.EMPTY;
        }

        energyStored = 0;
        incrementLoadoutVersion();
        dirty = true;
    }

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
            incrementLoadoutVersion();
            dirty = true;
        }
    }

    public boolean toggleEnabled(CyberwareSlot slot, int index) {
        boolean next = !isEnabled(slot, index);
        setEnabled(slot, index, next);
        return next;
    }

    public int getChipwareSlotCount() {
        return chipware.length;
    }

    public ItemStack getChipwareStack(int index) {
        if (index < 0 || index >= chipware.length) return ItemStack.EMPTY;
        return chipware[index];
    }

    public void setChipwareStack(int index, ItemStack stack) {
        if (index < 0 || index >= chipware.length) return;
        chipware[index] = (stack == null || stack.isEmpty()) ? ItemStack.EMPTY : stack.copyWithCount(1);
        dirty = true;
    }

    public boolean addChipwareStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        for (int i = 0; i < chipware.length; i++) {
            if (chipware[i] == null || chipware[i].isEmpty()) {
                chipware[i] = stack.copyWithCount(1);
                dirty = true;
                return true;
            }
        }

        return false;
    }

    public boolean hasAnyChipware() {
        for (ItemStack stack : chipware) {
            if (stack != null && !stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyInSlots(CyberwareSlot slot) {
        InstalledCyberware[] arr = slots.get(slot);
        if (arr == null) return false;

        for (InstalledCyberware cw : arr) {
            if (cw == null) continue;
            if (cw.getItem() != null && !cw.getItem().isEmpty()) return true;
        }

        return false;
    }

    public boolean hasSpecificItem(Item item, CyberwareSlot... slotsToCheck) {
        if (item == null || slotsToCheck == null) return false;

        for (CyberwareSlot slot : slotsToCheck) {
            InstalledCyberware[] arr = slots.get(slot);
            if (arr == null) continue;

            for (InstalledCyberware cw : arr) {
                if (cw == null) continue;

                ItemStack stack = cw.getItem();
                if (stack == null || stack.isEmpty()) continue;

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

    public boolean hasAnyTagged(TagKey<Item> tag, CyberwareSlot... slotsToCheck) {
        if (tag == null || slotsToCheck == null) return false;

        for (CyberwareSlot slot : slotsToCheck) {
            InstalledCyberware[] arr = slots.get(slot);
            if (arr == null) continue;

            for (InstalledCyberware cw : arr) {
                if (cw == null) continue;

                ItemStack stack = cw.getItem();
                if (stack == null || stack.isEmpty()) continue;

                if (stack.is(tag)) return true;
            }
        }

        return false;
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

    public boolean isTrimmed(CyberwareSlot slot, int index) {
        InstalledCyberware installed = get(slot, index);
        if (installed == null) return false;

        ItemStack st = installed.getItem();
        if (st == null || st.isEmpty()) return false;

        return st.get(DataComponents.TRIM) != null;
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

        for (int i = 0; i < chipware.length; i++) {
            chipware[i] = ItemStack.EMPTY;
        }

        incrementLoadoutVersion();
        dirty = true;
    }

    public int getEnergyStored() {
        return energyStored;
    }

    public void setEnergyStored(LivingEntity entity, int value) {
        int cap = getTotalEnergyCapacity(entity);
        int clamped = Mth.clamp(value, 0, cap);
        if (clamped != this.energyStored) {
            this.energyStored = clamped;
            dirty = true;
        }
    }

    public int getTotalEnergyCapacity(LivingEntity entity) {
        if (entity == null) return 0;

        int total = 0;

        for (var entry : slots.entrySet()) {
            CyberwareSlot slot = entry.getKey();
            InstalledCyberware[] arr = entry.getValue();
            if (arr == null) continue;

            for (int i = 0; i < arr.length; i++) {
                InstalledCyberware cw = arr[i];
                if (cw == null) continue;

                ItemStack stack = cw.getItem();
                if (stack == null || stack.isEmpty()) continue;
                if (!(stack.getItem() instanceof ICyberwareItem item)) continue;

                int cap = item.getEnergyCapacity(entity, stack, slot);
                if (cap > 0) total += cap;
            }
        }

        return Math.max(0, total);
    }

    public int receiveEnergy(LivingEntity entity, int amount) {
        if (entity == null) return 0;
        if (amount <= 0) return 0;

        int cap = getTotalEnergyCapacity(entity);
        if (cap <= 0) return 0;

        int before = energyStored;
        int after = Mth.clamp(before + amount, 0, cap);

        if (after != before) {
            energyStored = after;
            dirty = true;
        }

        return after - before;
    }

    public int extractEnergy(int amount) {
        if (amount <= 0) return 0;

        int before = energyStored;
        int after = Math.max(0, before - amount);

        if (after != before) {
            energyStored = after;
            dirty = true;
        }

        return before - after;
    }

    public boolean tryConsumeEnergy(int amount) {
        if (amount <= 0) return true;
        if (energyStored < amount) return false;

        energyStored -= amount;
        dirty = true;
        return true;
    }

    public void clampEnergyToCapacity(LivingEntity entity) {
        if (entity == null) return;

        int cap = getTotalEnergyCapacity(entity);
        int clamped = Mth.clamp(energyStored, 0, cap);
        if (clamped != energyStored) {
            energyStored = clamped;
            dirty = true;
        }
    }

    public boolean commandInstall(Entity target, ItemStack stack) {
        if (target != null && !(target instanceof LivingEntity)) return false;
        if (stack == null || stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof ICyberwareItem cw)) return false;

        ItemStack installStack = stack.copy();
        installStack.setCount(1);

        InstallTarget place = findInstallTarget(installStack, cw);
        if (place == null) return false;

        if (cw.replacesOrgan()) {
            for (CyberwareSlot replaced : cw.getReplacedOrgans()) {
                removeFirstDefaultOrNonEmptyInSlotGroup(replaced);
            }
        }

        int humanityCost = cw.getHumanityCost();
        InstalledCyberware installed = new InstalledCyberware(installStack, place.slot(), place.index(), humanityCost);
        installed.setPowered(true);

        set(place.slot(), place.index(), installed);

        if (target instanceof LivingEntity living) {
            cw.onInstalled(living);
        }

        return true;
    }

    public boolean commandRemove(Entity target, Item item) {
        if (target != null && !(target instanceof LivingEntity)) return false;
        if (item == null) return false;

        for (CyberwareSlot slot : CyberwareSlot.values()) {
            InstalledCyberware[] arr = slots.get(slot);
            if (arr == null) continue;

            for (int i = 0; i < arr.length; i++) {
                InstalledCyberware installed = arr[i];
                if (installed == null) continue;

                ItemStack stack = installed.getItem();
                if (stack == null || stack.isEmpty()) continue;

                if (stack.is(item)) {
                    if (target instanceof LivingEntity living && stack.getItem() instanceof ICyberwareItem cw) {
                        cw.onRemoved(living);
                    }
                    remove(slot, i);
                    return true;
                }
            }
        }

        return false;
    }

    public Component commandListComponent() {
        MutableComponent out = Component.literal("Installed cyberware:");
        boolean foundAny = false;

        for (CyberwareSlot slot : CyberwareSlot.values()) {
            InstalledCyberware[] arr = slots.get(slot);
            if (arr == null) continue;

            for (int i = 0; i < arr.length; i++) {
                InstalledCyberware installed = arr[i];
                if (installed == null) continue;

                ItemStack stack = installed.getItem();
                if (stack == null || stack.isEmpty()) continue;

                foundAny = true;

                MutableComponent line = Component.literal("\n- ")
                        .append(Component.literal(slot.name()))
                        .append(Component.literal("["))
                        .append(Component.literal(Integer.toString(i)))
                        .append(Component.literal("]: "))
                        .append(stack.getHoverName());

                if (!isEnabled(slot, i)) {
                    line.append(Component.literal(" (disabled)"));
                }

                out.append(line);
            }
        }

        boolean foundChipware = false;
        for (int i = 0; i < chipware.length; i++) {
            ItemStack stack = chipware[i];
            if (stack == null || stack.isEmpty()) continue;

            if (!foundChipware) {
                out.append(Component.literal("\nChipware:"));
                foundChipware = true;
            }

            foundAny = true;

            out.append(Component.literal("\n- CHIPWARE["))
                    .append(Component.literal(Integer.toString(i)))
                    .append(Component.literal("]: "))
                    .append(stack.getHoverName());
        }

        if (!foundAny) {
            out.append(Component.literal("\n- none"));
        }

        return out;
    }

    private InstallTarget findInstallTarget(ItemStack incoming, ICyberwareItem cw) {
        if (cw.replacesOrgan()) {
            for (CyberwareSlot replaced : cw.getReplacedOrgans()) {
                InstallTarget t = findFirstValidSpaceInSlotGroup(incoming, cw, replaced);
                if (t != null) return t;
            }
        }

        for (CyberwareSlot slot : CyberwareSlot.values()) {
            if (!cw.supportsSlot(slot)) continue;

            InstallTarget t = findFirstValidSpaceInSlotGroup(incoming, cw, slot);
            if (t != null) return t;
        }

        return null;
    }

    private InstalledCyberware removeFirstDefaultOrNonEmptyInSlotGroup(CyberwareSlot slot) {
        InstalledCyberware[] arr = slots.get(slot);
        if (arr == null) return null;

        for (int i = 0; i < arr.length; i++) {
            InstalledCyberware installed = arr[i];
            if (installed == null) continue;

            ItemStack st = installed.getItem();
            if (st == null || st.isEmpty()) continue;

            if (DefaultOrgans.isOrganForSlot(st, slot)) {
                return remove(slot, i);
            }
        }

        for (int i = 0; i < arr.length; i++) {
            InstalledCyberware installed = arr[i];
            if (installed == null) continue;

            ItemStack st = installed.getItem();
            if (st == null || st.isEmpty()) continue;

            return remove(slot, i);
        }

        return null;
    }

    private record InstallTarget(CyberwareSlot slot, int index) {}

    private InstallTarget findFirstValidSpaceInSlotGroup(ItemStack incoming, ICyberwareItem cw, CyberwareSlot slot) {
        InstalledCyberware[] arr = slots.get(slot);
        if (arr == null) return null;

        int max = Math.max(1, cw.maxStacksPerSlotType(incoming, slot));
        int already = 0;

        for (InstalledCyberware installed : arr) {
            if (installed == null) continue;
            ItemStack st = installed.getItem();
            if (st == null || st.isEmpty()) continue;
            if (ItemStack.isSameItemSameComponents(st, incoming)) already++;
        }

        if (already >= max) return null;

        for (int i = 0; i < arr.length; i++) {
            InstalledCyberware installed = arr[i];

            if (installed == null) {
                return new InstallTarget(slot, i);
            }

            ItemStack st = installed.getItem();
            if (st == null || st.isEmpty()) {
                return new InstallTarget(slot, i);
            }
        }

        return null;
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        ListTag chipwareList = new ListTag();

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

        for (int i = 0; i < chipware.length; i++) {
            ItemStack stack = chipware[i];
            if (stack == null || stack.isEmpty()) continue;

            CompoundTag c = new CompoundTag();
            c.putInt("Index", i);
            c.put("Stack", stack.save(provider));
            chipwareList.add(c);
        }

        tag.put(NBT_CYBERWARE, list);
        tag.put(NBT_CHIPWARE, chipwareList);
        tag.putInt(NBT_ENERGY, energyStored);
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

            boolean en = !c.contains("Enabled", Tag.TAG_BYTE) || c.getBoolean("Enabled");
            setEnabled(slot, index, en);
        }

        ListTag chipwareList = tag.getList(NBT_CHIPWARE, Tag.TAG_COMPOUND);
        for (int i = 0; i < chipwareList.size(); i++) {
            CompoundTag c = chipwareList.getCompound(i);
            int index = c.getInt("Index");
            if (index < 0 || index >= chipware.length) continue;
            if (!c.contains("Stack", Tag.TAG_COMPOUND)) continue;

            chipware[index] = ItemStack.parse(provider, c.getCompound("Stack")).orElse(ItemStack.EMPTY);
        }

        energyStored = tag.contains(NBT_ENERGY, Tag.TAG_INT) ? tag.getInt(NBT_ENERGY) : 0;
        incrementLoadoutVersion();
        dirty = false;
    }
}