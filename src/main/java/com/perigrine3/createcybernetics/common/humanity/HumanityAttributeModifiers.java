package com.perigrine3.createcybernetics.common.humanity;

import com.perigrine3.createcybernetics.Config;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.attributes.ModAttributes;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.util.CyberwareAttributeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class HumanityAttributeModifiers {
    public static final ResourceLocation HUMANITY_MOD_ID =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "humanity_mod");

    private static final String CYBERWARE_COST_PREFIX = "humanity_cyberware_cost_";
    private static final String BONUS_PREFIX = "humanity_bonus_";
    private static final String PENALTY_PREFIX = "humanity_penalty_";

    private HumanityAttributeModifiers() {}

    public static int getConfiguredBaseHumanity() {
        return Math.max(1, Config.HUMANITY.get());
    }

    public static int get(Player player) {
        return CyberwareAttributeHelper.getIntValue(
                player,
                ModAttributes.HUMANITY,
                getConfiguredBaseHumanity()
        );
    }

    public static int getBase(Player player) {
        if (player == null) {
            return getConfiguredBaseHumanity();
        }

        var instance = player.getAttribute(ModAttributes.HUMANITY);
        if (instance == null) {
            return getConfiguredBaseHumanity();
        }

        return Mth.floor(instance.getBaseValue());
    }

    public static void syncConfiguredBase(Player player) {
        if (player == null) {
            return;
        }

        CyberwareAttributeHelper.setBaseValue(
                player,
                ModAttributes.HUMANITY,
                getConfiguredBaseHumanity()
        );
    }

    public static void rebuildCyberwareCostModifiers(Player player) {
        if (player == null) {
            return;
        }

        syncConfiguredBase(player);
        clearCyberwareCostModifiers(player);

        if (DataIntegrityHandler.usesDataIntegrity(player)) {
            return;
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) {
            return;
        }

        for (CyberwareSlot slot : CyberwareSlot.values()) {
            InstalledCyberware[] installed = data.getAll().get(slot);
            if (installed == null) {
                continue;
            }

            for (int i = 0; i < installed.length; i++) {
                InstalledCyberware cyberware = installed[i];
                if (cyberware == null) {
                    continue;
                }

                ItemStack stack = cyberware.getItem();
                if (stack == null || stack.isEmpty()) {
                    continue;
                }

                if (!(stack.getItem() instanceof ICyberwareItem item)) {
                    continue;
                }

                int cost = item.getHumanityCost();
                if (cost == 0) {
                    continue;
                }

                CyberwareAttributeHelper.setPermanentModifier(
                        player,
                        ModAttributes.HUMANITY,
                        cyberwareCostId(slot, i),
                        -cost,
                        AttributeModifier.Operation.ADD_VALUE
                );
            }
        }
    }

    public static void setHumanityMod(Player player, int value) {
        if (player == null) {
            return;
        }

        if (value == 0) {
            clearHumanityMod(player);
            return;
        }

        CyberwareAttributeHelper.setPermanentModifier(
                player,
                ModAttributes.HUMANITY,
                HUMANITY_MOD_ID,
                value,
                AttributeModifier.Operation.ADD_VALUE
        );
    }

    public static void clearHumanityMod(Player player) {
        if (player == null) {
            return;
        }

        CyberwareAttributeHelper.removePermanentModifier(
                player,
                ModAttributes.HUMANITY,
                HUMANITY_MOD_ID
        );
    }

    public static boolean setBonus(Player player, String key, int value) {
        if (player == null) {
            return false;
        }

        if (value <= 0) {
            return clearBonus(player, key);
        }

        return CyberwareAttributeHelper.setPermanentModifier(
                player,
                ModAttributes.HUMANITY,
                keyedId(BONUS_PREFIX, key),
                value,
                AttributeModifier.Operation.ADD_VALUE
        );
    }

    public static boolean clearBonus(Player player, String key) {
        if (player == null) {
            return false;
        }

        return CyberwareAttributeHelper.removePermanentModifier(
                player,
                ModAttributes.HUMANITY,
                keyedId(BONUS_PREFIX, key)
        );
    }

    public static boolean setPenalty(Player player, String key, int value) {
        if (player == null) {
            return false;
        }

        if (value <= 0) {
            return clearPenalty(player, key);
        }

        return CyberwareAttributeHelper.setPermanentModifier(
                player,
                ModAttributes.HUMANITY,
                keyedId(PENALTY_PREFIX, key),
                -value,
                AttributeModifier.Operation.ADD_VALUE
        );
    }

    public static boolean clearPenalty(Player player, String key) {
        if (player == null) {
            return false;
        }

        return CyberwareAttributeHelper.removePermanentModifier(
                player,
                ModAttributes.HUMANITY,
                keyedId(PENALTY_PREFIX, key)
        );
    }

    private static void clearCyberwareCostModifiers(Player player) {
        for (CyberwareSlot slot : CyberwareSlot.values()) {
            for (int i = 0; i < slot.size; i++) {
                CyberwareAttributeHelper.removePermanentModifier(
                        player,
                        ModAttributes.HUMANITY,
                        cyberwareCostId(slot, i)
                );
            }
        }
    }

    private static ResourceLocation cyberwareCostId(CyberwareSlot slot, int index) {
        return ResourceLocation.fromNamespaceAndPath(
                CreateCybernetics.MODID,
                CYBERWARE_COST_PREFIX + slot.name().toLowerCase() + "_" + index
        );
    }

    private static ResourceLocation keyedId(String prefix, String key) {
        String safe = key == null || key.isBlank()
                ? "unknown"
                : key.toLowerCase()
                .replaceAll("[^a-z0-9_./-]", "_")
                .replace('.', '_')
                .replace('/', '_');

        return ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, prefix + safe);
    }
}
