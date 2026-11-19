package com.perigrine3.createcybernetics.common.quickhack;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;

public final class QuickhackProgramData {
    private static final String ROOT = "CreateCyberneticsQuickhack";
    private static final String NBT_NAME = "Name";
    private static final String NBT_PROGRAM = "Program";
    private static final String NBT_ID = "Id";

    public static final int MAX_PROGRAM_NODES = 10;
    public static final int MAX_NAME_LENGTH = QuickhackNameSanitizer.MAX_LENGTH;

    private QuickhackProgramData() {}

    public static boolean hasProgram(ItemStack stack) {
        return !getNodes(stack).isEmpty();
    }

    public static String getName(ItemStack stack) {
        CompoundTag root = getRoot(stack);
        if (root == null) return "";
        return sanitizeName(root.getString(NBT_NAME));
    }

    public static List<MicroprogramDefinition> getNodes(ItemStack stack) {
        CompoundTag root = getRoot(stack);
        if (root == null || !root.contains(NBT_PROGRAM, Tag.TAG_LIST)) return List.of();

        ListTag program = root.getList(NBT_PROGRAM, Tag.TAG_COMPOUND);
        List<MicroprogramDefinition> nodes = new ArrayList<>();

        for (int i = 0; i < program.size() && nodes.size() < MAX_PROGRAM_NODES; i++) {
            CompoundTag nodeTag = program.getCompound(i);
            String id = nodeTag.getString(NBT_ID);
            if (id.isBlank()) continue;

            MicroprogramDefinition definition = MicroprogramRegistry.get(id);
            if (definition == null) continue;

            nodes.add(definition);
        }

        return List.copyOf(nodes);
    }

    public static int getRamCost(ItemStack stack) {
        return calculateRamCost(getNodes(stack));
    }

    public static int calculateRamCost(List<MicroprogramDefinition> nodes) {
        if (nodes == null || nodes.isEmpty()) return 0;

        int total = 0;

        for (MicroprogramDefinition node : nodes) {
            if (node == null) continue;
            total += node.ramCost();
        }

        return Math.max(0, total);
    }

    public static String getProgramDisplay(ItemStack stack) {
        List<MicroprogramDefinition> nodes = getNodes(stack);
        if (nodes.isEmpty()) return "";

        StringBuilder builder = new StringBuilder();

        for (MicroprogramDefinition node : nodes) {
            if (!builder.isEmpty()) builder.append(" > ");
            builder.append(node.displayName());
        }

        return builder.toString();
    }

    public static void setProgram(ItemStack stack, String name, List<MicroprogramDefinition> nodes) {
        if (stack == null || stack.isEmpty()) return;

        List<MicroprogramDefinition> sanitizedNodes = sanitizeNodes(nodes);
        CompoundTag root = new CompoundTag();

        root.putString(NBT_NAME, sanitizeName(name));

        ListTag program = new ListTag();

        for (MicroprogramDefinition node : sanitizedNodes) {
            CompoundTag nodeTag = new CompoundTag();
            nodeTag.putString(NBT_ID, node.id());
            program.add(nodeTag);
        }

        root.put(NBT_PROGRAM, program);

        CustomData existing = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag merged = existing.copyTag();
        merged.put(ROOT, root);

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(merged));
    }

    public static void clearProgram(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;

        CustomData existing = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = existing.copyTag();

        tag.remove(ROOT);

        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    public static boolean isValidProgram(List<MicroprogramDefinition> nodes) {
        if (nodes == null || nodes.isEmpty() || nodes.size() > MAX_PROGRAM_NODES) return false;

        MicroprogramDefinition currentTarget = null;
        boolean hasAnyFunction = false;
        boolean canAcceptModifier = false;

        for (MicroprogramDefinition node : nodes) {
            if (node == null || !MicroprogramRegistry.contains(node.id())) return false;

            if (node.category() == MicroprogramCategory.TARGET) {
                currentTarget = node;
                canAcceptModifier = false;
                continue;
            }

            if (node.category() == MicroprogramCategory.FUNCTION) {
                if (currentTarget == null) return false;

                hasAnyFunction = true;
                canAcceptModifier = true;
                continue;
            }

            if (node.category() == MicroprogramCategory.MODIFIER && !canAcceptModifier) return false;
        }

        return hasAnyFunction;
    }

    public static String sanitizeName(String name) {
        return QuickhackNameSanitizer.sanitize(name);
    }

    private static List<MicroprogramDefinition> sanitizeNodes(List<MicroprogramDefinition> nodes) {
        if (nodes == null || nodes.isEmpty()) return List.of();

        List<MicroprogramDefinition> sanitized = new ArrayList<>();

        for (MicroprogramDefinition node : nodes) {
            if (node == null) continue;

            MicroprogramDefinition registered = MicroprogramRegistry.get(node.id());
            if (registered == null) continue;

            sanitized.add(registered);

            if (sanitized.size() >= MAX_PROGRAM_NODES) break;
        }

        return List.copyOf(sanitized);
    }

    private static CompoundTag getRoot(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return null;

        CompoundTag tag = customData.copyTag();
        if (!tag.contains(ROOT, Tag.TAG_COMPOUND)) return null;

        return tag.getCompound(ROOT);
    }
}
