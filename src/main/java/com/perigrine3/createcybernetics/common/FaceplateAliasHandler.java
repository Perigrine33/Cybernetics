package com.perigrine3.createcybernetics.common;

import com.mojang.authlib.GameProfile;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.item.ModItems;
import net.minecraft.core.component.DataComponents; // ADDED
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag; // ADDED
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent; // ADDED
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class FaceplateAliasHandler {

    private static final String NBT_ALIAS_ACTIVE = "cc_faceplate_active";
    private static final String NBT_ALIAS_TEXT   = "cc_faceplate_alias";

    private FaceplateAliasHandler() {}

    public static boolean hasActive(ServerPlayer player) {
        CompoundTag p = player.getPersistentData();

        if (!p.getBoolean(NBT_ALIAS_ACTIVE)) {
            return false;
        }

        if (!p.contains(NBT_ALIAS_TEXT, Tag.TAG_STRING)) {
            return false;
        }

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        return data != null && !data.getFaceplateMaskStack().isEmpty();
    }

    public static String getAlias(ServerPlayer player) {
        return player.getPersistentData().getString(NBT_ALIAS_TEXT);
    }

    public static boolean hasInterchangeableFaceplateInstalled(ServerPlayer player) {
        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return false;

        var required = ModItems.SKINUPGRADES_FACEPLATE.get();

        for (CyberwareSlot slot : CyberwareSlot.values()) {
            InstalledCyberware[] arr = data.getAll().get(slot);
            if (arr == null) continue;

            for (InstalledCyberware inst : arr) {
                if (inst == null) continue;
                ItemStack st = inst.getItem();
                if (st == null || st.isEmpty()) continue;
                if (st.is(required)) return true;
            }
        }
        return false;
    }

    public static boolean apply(ServerPlayer player, ItemStack faceplateOne) {
        if (faceplateOne.isEmpty()) return false;

        Component customName = faceplateOne.get(DataComponents.CUSTOM_NAME);
        if (customName == null) return false;

        if (!hasInterchangeableFaceplateInstalled(player)) return false;

        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return false;

        if (hasActive(player)) {
            clear(player, true);
        }

        String alias = customName.getString().trim();
        if (alias.isEmpty()) return false;

        ItemStack stored = faceplateOne.copy();
        stored.setCount(1);
        data.setFaceplateMaskStack(stored);

        CompoundTag p = player.getPersistentData();
        p.putBoolean(NBT_ALIAS_ACTIVE, true);
        p.putString(NBT_ALIAS_TEXT, alias);

        player.setCustomName(Component.literal(alias));
        player.setCustomNameVisible(true);

        player.refreshDisplayName();
        player.refreshTabListName();

        data.setDirty();
        player.syncData(ModAttachments.CYBERWARE);

        return true;
    }

    public static void clear(ServerPlayer player, boolean returnFaceplate) {
        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);

        if (data != null) {
            ItemStack stored = data.removeFaceplateMaskStack();

            if (returnFaceplate && !stored.isEmpty()) {
                if (!player.getInventory().add(stored.copy())) {
                    player.drop(stored.copy(), false);
                }
            }

            data.setDirty();
            player.syncData(ModAttachments.CYBERWARE);
        }

        CompoundTag p = player.getPersistentData();
        p.remove(NBT_ALIAS_TEXT);
        p.putBoolean(NBT_ALIAS_ACTIVE, false);

        player.setCustomName(null);
        player.setCustomNameVisible(false);

        player.refreshDisplayName();
        player.refreshTabListName();
    }

    @SubscribeEvent
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        Player p = event.getEntity();
        Component custom = p.getCustomName();
        if (custom != null) {
            event.setDisplayname(custom);
            return;
        }

        if (p instanceof ServerPlayer sp && hasActive(sp)) {
            event.setDisplayname(Component.literal(getAlias(sp)));
        }
    }

    @SubscribeEvent
    public static void onTabListName(PlayerEvent.TabListNameFormat event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (!hasActive(sp)) return;

        event.setDisplayName(Component.literal(getAlias(sp)));
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer sp = event.getPlayer();
        if (sp == null) return;
        if (!hasActive(sp)) return;

        event.setCanceled(true);

        Component name = Component.literal(getAlias(sp));
        Component content = Component.literal(event.getRawText());

        Component line = Component.translatable("chat.type.text", name, content);

        sp.server.getPlayerList().broadcastSystemMessage(line, false);
    }


    @SubscribeEvent
    public static void onRightClickAnvil(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        Level level = event.getLevel();
        if (level.isClientSide) return;

        if (!sp.isCrouching()) return;

        if (!level.getBlockState(event.getPos()).is(BlockTags.ANVIL)) return;

        if (!hasActive(sp)) return;

        clear(sp, true);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (!hasActive(sp)) return;

        if (!hasInterchangeableFaceplateInstalled(sp)) {
            clear(sp, true);
            return;
        }

        String alias = getAlias(sp);
        if (!alias.isBlank()) {
            sp.setCustomName(Component.literal(alias));
            sp.setCustomNameVisible(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (!hasActive(sp)) return;

        if (!hasInterchangeableFaceplateInstalled(sp)) {
            clear(sp, true);
            return;
        }

        String alias = getAlias(sp);
        if (!alias.isBlank()) {
            sp.setCustomName(Component.literal(alias));
            sp.setCustomNameVisible(true);
        }
    }

    @SubscribeEvent
    public static void onCyberwareStateChange(com.perigrine3.createcybernetics.event.custom.CyberwareStateChangeEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer sp)) return;
        if (sp.level().isClientSide) return;

        if (!hasActive(sp)) return;

        var profile = event.getCurrentProfile();
        boolean hasFaceplate = profile != null
                ? profile.hasCapability(com.perigrine3.createcybernetics.common.scheduling.CyberwareLoadoutProfile.CapabilityFlag.INTERCHANGEABLE_FACEPLATE)
                : hasInterchangeableFaceplateInstalled(sp);
        if (!hasFaceplate) {
            clear(sp, true);
        }
    }
}
