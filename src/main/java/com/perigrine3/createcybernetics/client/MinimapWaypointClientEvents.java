package com.perigrine3.createcybernetics.client;

import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.network.payload.NavigationMapPayloads;
import com.perigrine3.createcybernetics.screen.custom.hud.CyberpunkMinimapRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = CreateCybernetics.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class MinimapWaypointClientEvents {

    private MinimapWaypointClientEvents() {}

    private static final int EXPLORATION_RADIUS = 32;
    private static final int EXPLORATION_REFRESH_INTERVAL = 200;
    private static final int SAVE_INTERVAL = 100;
    private static LocalPlayer navigationPlayer;
    private static int lastRefreshTick = Integer.MIN_VALUE;
    private static int lastSaveTick = Integer.MIN_VALUE;

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) return;
        if (event.getEntity() != player) return;
        if (!CyberpunkMinimapRenderer.hasNavigationChip(player)) return;

        BlockPos pos = event.getPos();
        BlockState state = event.getLevel().getBlockState(pos);

        if (!(state.getBlock() instanceof AbstractBannerBlock bannerBlock)) return;

        BannerBlockEntity bannerBlockEntity = event.getLevel().getBlockEntity(pos) instanceof BannerBlockEntity banner ? banner : null;
        Component waypointName = getBannerName(bannerBlockEntity, bannerBlock.getColor());
        int waypointColor = getWaypointColor(bannerBlock.getColor());
        boolean added = MinimapWaypointClient.toggleBannerWaypoint(player, pos, waypointName.getString(), waypointColor);

        if (added) {
            player.displayClientMessage(Component.translatable("gui.createcybernetics.navigation.waypoint_added", waypointName).withStyle(ChatFormatting.AQUA), true);
        } else {
            player.displayClientMessage(Component.translatable("gui.createcybernetics.navigation.waypoint_removed", waypointName).withStyle(ChatFormatting.RED), true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.getLevel().isClientSide()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) return;
        if (event.getEntity() != player) return;
        if (!CyberpunkMinimapRenderer.hasNavigationChip(player)) return;

        ItemStack stack = event.getItemStack();

        if (!stack.is(Items.FILLED_MAP)) return;

        PacketDistributor.sendToServer(new NavigationMapPayloads.RequestMapImportPayload(event.getHand() == InteractionHand.MAIN_HAND));
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        MinimapWaypointClient.beginNavigationFrame();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) return;
        if (!CyberpunkMinimapRenderer.hasNavigationChip(player)) return;
        // Minecraft may run several client ticks before displaying the next frame.
        // Navigation gets one job, not a fresh allowance for every catch-up tick.
        if (!MinimapWaypointClient.claimNavigationWork()) return;
        if (navigationPlayer != player) {
            navigationPlayer = player;
            lastRefreshTick = Integer.MIN_VALUE;
            lastSaveTick = Integer.MIN_VALUE;
        }

        // Persist nearby exploration first; the minimap can reuse those samples.
        MinimapWaypointClient.updateExploration(player, EXPLORATION_RADIUS);

        if (!mc.options.hideGui && mc.screen == null && mc.options.getCameraType().isFirstPerson()
                && HudConfigClient.get(player.getUUID()).minimap.enabled) {
            CyberpunkMinimapRenderer.updateTerrain(player);
        }

        if (lastRefreshTick == Integer.MIN_VALUE || player.tickCount - lastRefreshTick >= EXPLORATION_REFRESH_INTERVAL) {
            MinimapWaypointClient.refreshExploration(player, 12);
            lastRefreshTick = player.tickCount;
        }

        if (lastSaveTick == Integer.MIN_VALUE || player.tickCount - lastSaveTick >= SAVE_INTERVAL) {
            MinimapWaypointClient.saveIfDirty(player.getUUID());
            lastSaveTick = player.tickCount;
        }
    }

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null) {
            MinimapWaypointClient.reload(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        navigationPlayer = null;
        CyberpunkMinimapRenderer.clearTerrainCache();
        MinimapWaypointClient.clearTerrainCache();
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null) {
            MinimapWaypointClient.invalidate(player.getUUID());
        }
    }

    private static Component getBannerName(BannerBlockEntity bannerBlockEntity, DyeColor color) {
        if (bannerBlockEntity != null && bannerBlockEntity.hasCustomName()) {
            return bannerBlockEntity.getCustomName();
        }

        return Component.translatable("gui.createcybernetics.navigation.default_banner", Component.translatable("color.minecraft." + color.getName()));
    }

    private static int getWaypointColor(DyeColor color) {
        return switch (color) {
            case WHITE -> 0xFFFFFFFF;
            case ORANGE -> 0xFFFFA500;
            case MAGENTA -> 0xFFFF55FF;
            case LIGHT_BLUE -> 0xFF55AAFF;
            case YELLOW -> 0xFFFFFF55;
            case LIME -> 0xFF55FF55;
            case PINK -> 0xFFFF88AA;
            case GRAY -> 0xFF777777;
            case LIGHT_GRAY -> 0xFFAAAAAA;
            case CYAN -> 0xFF00FFFF;
            case PURPLE -> 0xFFAA55FF;
            case BLUE -> 0xFF5555FF;
            case BROWN -> 0xFFAA6633;
            case GREEN -> 0xFF00AA55;
            case RED -> 0xFFFF5555;
            case BLACK -> 0xFF333333;
        };
    }
}
