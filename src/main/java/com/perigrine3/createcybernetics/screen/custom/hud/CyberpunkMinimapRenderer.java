package com.perigrine3.createcybernetics.screen.custom.hud;

import com.mojang.math.Axis;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.ICyberwareItem;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.client.HudConfigClient;
import com.perigrine3.createcybernetics.client.MinimapWaypointClient;
import com.perigrine3.createcybernetics.client.navigation.SharedNavigationClientState;
import com.perigrine3.createcybernetics.client.navigation.NavigationTerrainGrid;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;

import java.util.Optional;

public final class CyberpunkMinimapRenderer {

    private CyberpunkMinimapRenderer() {}

    public static final int MAP_SIZE = 80;
    public static final int FRAME_PADDING = 5;
    public static final int HEADER_HEIGHT = 12;
    public static final int TOTAL_WIDTH = MAP_SIZE + FRAME_PADDING * 2;
    public static final int TOTAL_HEIGHT = MAP_SIZE + FRAME_PADDING * 2 + HEADER_HEIGHT;

    private static final float BASE_RENDER_SCALE = 2.25f;
    private static final int BLOCKS_PER_PIXEL = 1;
    private static final int ENTITY_SCAN_RADIUS = MAP_SIZE * BLOCKS_PER_PIXEL / 2;

    private static final int BACKGROUND_COLOR = 0xE0020607;
    private static final int MAP_BACKGROUND_COLOR = 0xFF030A0B;
    private static final int FRAME_COLOR = 0xFF00E5FF;
    private static final int FRAME_DARK_COLOR = 0xAA007A88;
    private static final int GRID_COLOR = 0x2800E5FF;
    private static final int SCANLINE_COLOR = 0x2800FFFF;
    private static final int HOSTILE_COLOR = 0xFFFF3B55;
    private static final int PLAYER_CONTACT_COLOR = 0xFF00E5FF;
    private static final int NEUTRAL_COLOR = 0xFFFFC857;
    private static final int TEXT_COLOR = 0xFF00E5FF;
    private static final int NORTH_COLOR = 0xFFFF5555;

    private static NavigationTerrainGrid terrainGrid = new NavigationTerrainGrid(MAP_SIZE);
    private static final DynamicTexture[] TERRAIN_TEXTURES = new DynamicTexture[3];
    private static final ResourceLocation[] TERRAIN_LOCATIONS = new ResourceLocation[3];
    private static int textureSlot = -1;
    private static ResourceLocation terrainLocation;
    private static Level cachedLevel;
    private static int lastRenderTick = Integer.MIN_VALUE;
    private static java.util.List<Entity> cachedEntities = java.util.List.of();
    private static int entityScanTick = Integer.MIN_VALUE;
    private static boolean terrainUploadPending;

    public static void clearTerrainCache() {
        for (int slot = 0; slot < TERRAIN_TEXTURES.length; slot++) {
            if (TERRAIN_LOCATIONS[slot] != null) Minecraft.getInstance().getTextureManager().release(TERRAIN_LOCATIONS[slot]);
            TERRAIN_TEXTURES[slot] = null;
            TERRAIN_LOCATIONS[slot] = null;
        }
        textureSlot = -1;
        terrainLocation = null;
        cachedLevel = null;
        lastRenderTick = Integer.MIN_VALUE;
        cachedEntities = java.util.List.of();
        entityScanTick = Integer.MIN_VALUE;
        terrainGrid = new NavigationTerrainGrid(MAP_SIZE);
        terrainUploadPending = true;
    }

    public static boolean hasNavigationChip(LocalPlayer player) {
        PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);
        if (data == null) return false;

        InstalledCyberware[] installedEyes = data.getAll().get(CyberwareSlot.EYES);
        if (installedEyes == null) return false;

        for (int index = 0; index < installedEyes.length; index++) {
            InstalledCyberware installed = installedEyes[index];
            if (installed == null) continue;

            ItemStack stack = installed.getItem();
            if (stack == null || stack.isEmpty()) continue;
            if (!stack.is(ModItems.EYEUPGRADES_NAVIGATIONCHIP.get())) continue;
            if (!data.isEnabled(CyberwareSlot.EYES, index)) continue;

            if (stack.getItem() instanceof ICyberwareItem cyberwareItem && cyberwareItem.requiresEnergyToFunction(player, stack, CyberwareSlot.EYES) && !installed.isPowered()) continue;

            return true;
        }

        return false;
    }

    public static CyberwareHudLayer.HudRect computeRect(HudConfigClient.ComponentLayout layout, int screenPxW, int screenPxH) {
        float renderScale = effectiveScale(layout);
        int width = Math.round(TOTAL_WIDTH * renderScale);
        int height = Math.round(TOTAL_HEIGHT * renderScale);
        int x = Mth.clamp(layout.pixelX(screenPxW), 0, Math.max(0, screenPxW - width));
        int y = Mth.clamp(layout.pixelY(screenPxH), 0, Math.max(0, screenPxH - height));
        return new CyberwareHudLayer.HudRect(x, y, width, height);
    }

    public static void render(GuiGraphics gg, Minecraft mc, LocalPlayer player, int screenPxW, int screenPxH, HudConfigClient.HudConfig cfg) {
        HudConfigClient.ComponentLayout layout = cfg.minimap;
        if (!layout.enabled) return;
        if (!hasNavigationChip(player)) return;

        uploadTerrainIfNeeded();

        CyberwareHudLayer.HudRect rect = computeRect(layout, screenPxW, screenPxH);
        float renderScale = effectiveScale(layout);

        gg.pose().pushPose();
        gg.pose().translate(rect.x(), rect.y(), 0);
        gg.pose().scale(renderScale, renderScale, 1.0f);

        renderBackground(gg);
        renderHeader(gg, mc, player);
        renderTerrain(gg);
        renderGrid(gg);
        renderWaypoints(gg, mc, player);
        renderEntities(gg, player);
        renderSharedPlayers(gg, mc, player);
        renderPlayerMarker(gg, player);
        renderNorthMarker(gg, mc);
        renderScanline(gg, player);
        renderFrame(gg);

        gg.pose().popPose();
    }

    private static float effectiveScale(HudConfigClient.ComponentLayout layout) {
        return BASE_RENDER_SCALE * layout.scale;
    }

    /** Client tick only: the HUD draw path must never query world terrain. */
    public static void updateTerrain(LocalPlayer player) {
        Level level = player.level();
        if (cachedLevel != level) {
            terrainGrid = new NavigationTerrainGrid(MAP_SIZE);
            lastRenderTick = Integer.MIN_VALUE;
            cachedEntities = java.util.List.of();
            entityScanTick = Integer.MIN_VALUE;
            cachedLevel = level;
            terrainUploadPending = true;
        }
        if (lastRenderTick == player.tickCount) return;
        lastRenderTick = player.tickCount;
        terrainGrid.moveTo(Mth.floor(player.getX()), Mth.floor(player.getZ()));
        terrainGrid.update((x, z) -> MinimapWaypointClient.sampleTerrainColor(level, x, z), 256);
        terrainUploadPending |= terrainGrid.consumeChanged();
    }

    private static void uploadTerrainIfNeeded() {
        if (!terrainUploadPending) return;
        // Do not overwrite the same texture the GPU used for the preceding frame.
        // Three tiny textures keep updates bounded without accumulating resources.
        int nextSlot = (textureSlot + 1) % TERRAIN_TEXTURES.length;
        DynamicTexture terrainTexture = TERRAIN_TEXTURES[nextSlot];
        if (terrainTexture == null) {
            terrainTexture = new DynamicTexture(MAP_SIZE, MAP_SIZE, true);
            TERRAIN_TEXTURES[nextSlot] = terrainTexture;
            TERRAIN_LOCATIONS[nextSlot] = Minecraft.getInstance().getTextureManager().register("navigation_minimap", terrainTexture);
            terrainTexture.setFilter(false, false);
        }
        var pixels = terrainTexture.getPixels();
        if (pixels == null) return;
        for (int z = 0; z < MAP_SIZE; z++) {
            for (int x = 0; x < MAP_SIZE; x++) {
                int color = terrainGrid.color(x, z);
                if (color == 0) color = MAP_BACKGROUND_COLOR;
                pixels.setPixelRGBA(x, z, (color & 0xFF00FF00) | ((color >> 16) & 0xFF) | ((color & 0xFF) << 16));
            }
        }
        terrainTexture.upload();
        textureSlot = nextSlot;
        terrainLocation = TERRAIN_LOCATIONS[nextSlot];
        terrainUploadPending = false;
    }

    private static void renderBackground(GuiGraphics gg) {
        gg.fill(0, 0, TOTAL_WIDTH, TOTAL_HEIGHT, BACKGROUND_COLOR);
        gg.fill(FRAME_PADDING, FRAME_PADDING + HEADER_HEIGHT, FRAME_PADDING + MAP_SIZE, FRAME_PADDING + HEADER_HEIGHT + MAP_SIZE, MAP_BACKGROUND_COLOR);
    }

    private static void renderHeader(GuiGraphics gg, Minecraft mc, LocalPlayer player) {
        Component heading = getCardinalDirection(player.getYRot());
        Component text = Component.translatable("gui.createcybernetics.navigation.minimap_header", heading);
        int textX = FRAME_PADDING + 2;
        int textY = FRAME_PADDING + 2;

        gg.drawString(mc.font, text, textX, textY, TEXT_COLOR, false);
    }

    private static void renderTerrain(GuiGraphics gg) {
        int startX = FRAME_PADDING;
        int startY = FRAME_PADDING + HEADER_HEIGHT;

        if (terrainLocation != null) gg.blit(terrainLocation, startX, startY, 0, 0, MAP_SIZE, MAP_SIZE, MAP_SIZE, MAP_SIZE);
    }

    private static void renderGrid(GuiGraphics gg) {
        int startX = FRAME_PADDING;
        int startY = FRAME_PADDING + HEADER_HEIGHT;

        for (int x = 0; x <= MAP_SIZE; x += 20) {
            gg.fill(startX + x, startY, startX + x + 1, startY + MAP_SIZE, GRID_COLOR);
        }

        for (int y = 0; y <= MAP_SIZE; y += 20) {
            gg.fill(startX, startY + y, startX + MAP_SIZE, startY + y + 1, GRID_COLOR);
        }

        gg.fill(startX + MAP_SIZE / 2, startY, startX + MAP_SIZE / 2 + 1, startY + MAP_SIZE, 0x4400E5FF);
        gg.fill(startX, startY + MAP_SIZE / 2, startX + MAP_SIZE, startY + MAP_SIZE / 2 + 1, 0x4400E5FF);
    }

    private static void renderWaypoints(GuiGraphics gg, Minecraft mc, LocalPlayer player) {
        int centerX = FRAME_PADDING + MAP_SIZE / 2;
        int centerY = FRAME_PADDING + HEADER_HEIGHT + MAP_SIZE / 2;
        int minX = FRAME_PADDING + 2;
        int maxX = FRAME_PADDING + MAP_SIZE - 3;
        int minY = FRAME_PADDING + HEADER_HEIGHT + 2;
        int maxY = FRAME_PADDING + HEADER_HEIGHT + MAP_SIZE - 3;

        for (MinimapWaypointClient.Waypoint waypoint : MinimapWaypointClient.getWaypoints(player)) {
            if (!waypoint.dimension().equals(player.level().dimension().location().toString())) continue;

            double relativeX = waypoint.x() + 0.5D - player.getX();
            double relativeZ = waypoint.z() + 0.5D - player.getZ();

            int mapX = centerX + Mth.floor(relativeX / BLOCKS_PER_PIXEL);
            int mapY = centerY + Mth.floor(relativeZ / BLOCKS_PER_PIXEL);

            boolean inside = mapX >= minX && mapX <= maxX && mapY >= minY && mapY <= maxY;

            if (!inside) {
                double angle = Math.atan2(relativeZ, relativeX);
                double radius = MAP_SIZE / 2.0D - 4.0D;
                mapX = centerX + Mth.floor(Math.cos(angle) * radius);
                mapY = centerY + Mth.floor(Math.sin(angle) * radius);
            }

            renderWaypointMarker(gg, mapX, mapY, waypoint.color());

            if (inside) {
                int labelX = mapX + 4;
                int labelY = mapY - 3;
                int maxLabelWidth = Math.max(0, FRAME_PADDING + MAP_SIZE - labelX - 2);
                Component displayName = MinimapWaypointClient.getWaypointDisplayName(waypoint);
                String label = trimToWidth(mc, displayName.getString(), maxLabelWidth);

                if (!label.isEmpty()) {
                    gg.drawString(mc.font, label, labelX, labelY, waypoint.color(), true);
                }
            }
        }
    }

    private static void renderWaypointMarker(GuiGraphics gg, int x, int y, int color) {
        gg.fill(x, y - 3, x + 1, y + 4, color);
        gg.fill(x - 3, y, x + 4, y + 1, color);
        gg.fill(x - 2, y - 2, x + 3, y + 3, color & 0x77FFFFFF);
        gg.fill(x - 1, y - 1, x + 2, y + 2, color);
    }

    private static String trimToWidth(Minecraft mc, String text, int maxWidth) {
        if (text == null || text.isEmpty() || maxWidth <= 0) return "";
        if (mc.font.width(text) <= maxWidth) return text;

        String ellipsis = "...";
        int targetWidth = Math.max(0, maxWidth - mc.font.width(ellipsis));
        String trimmed = mc.font.plainSubstrByWidth(text, targetWidth);
        return trimmed.isEmpty() ? "" : trimmed + ellipsis;
    }

    private static void renderSharedPlayers(GuiGraphics gg, Minecraft mc, LocalPlayer player) {
        int centerX = FRAME_PADDING + MAP_SIZE / 2;
        int centerY = FRAME_PADDING + HEADER_HEIGHT + MAP_SIZE / 2;
        int minX = FRAME_PADDING + 3;
        int maxX = FRAME_PADDING + MAP_SIZE - 4;
        int minY = FRAME_PADDING + HEADER_HEIGHT + 3;
        int maxY = FRAME_PADDING + HEADER_HEIGHT + MAP_SIZE - 4;
        String currentDimension = player.level().dimension().location().toString();

        for (SharedNavigationClientState.SharedPlayer sharedPlayer : SharedNavigationClientState.getPlayers()) {
            if (sharedPlayer.playerId().equals(player.getUUID())) continue;
            if (!sharedPlayer.dimension().equals(currentDimension)) continue;

            double relativeX = sharedPlayer.x() - player.getX();
            double relativeZ = sharedPlayer.z() - player.getZ();

            int mapX = centerX + Mth.floor(relativeX / BLOCKS_PER_PIXEL);
            int mapY = centerY + Mth.floor(relativeZ / BLOCKS_PER_PIXEL);
            boolean inside = mapX >= minX && mapX <= maxX && mapY >= minY && mapY <= maxY;

            if (!inside) {
                double angle = Math.atan2(relativeZ, relativeX);
                double radius = MAP_SIZE / 2.0D - 5.0D;
                mapX = centerX + Mth.floor(Math.cos(angle) * radius);
                mapY = centerY + Mth.floor(Math.sin(angle) * radius);
            }

            gg.fill(mapX - 2, mapY - 2, mapX + 3, mapY + 3, PLAYER_CONTACT_COLOR);
            gg.fill(mapX - 1, mapY - 1, mapX + 2, mapY + 2, 0xFFFFFFFF);

            int nameWidth = mc.font.width(sharedPlayer.name());
            int nameX = Mth.clamp(mapX - nameWidth / 2, FRAME_PADDING + 1, FRAME_PADDING + MAP_SIZE - nameWidth - 1);
            int nameY = Math.max(FRAME_PADDING + HEADER_HEIGHT + 1, mapY - mc.font.lineHeight - 3);

            gg.drawString(mc.font, sharedPlayer.name(), nameX, nameY, PLAYER_CONTACT_COLOR, true);
        }
    }

    private static void renderEntities(GuiGraphics gg, LocalPlayer player) {
        int centerX = FRAME_PADDING + MAP_SIZE / 2;
        int centerY = FRAME_PADDING + HEADER_HEIGHT + MAP_SIZE / 2;

        if (entityScanTick == Integer.MIN_VALUE || player.tickCount - entityScanTick >= 5) {
            cachedEntities = player.level().getEntities(player, player.getBoundingBox().inflate(ENTITY_SCAN_RADIUS));
            entityScanTick = player.tickCount;
        }
        for (Entity entity : cachedEntities) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (!living.isAlive()) continue;
            if (living.isInvisible()) continue;

            int mapX = centerX + Mth.floor((entity.getX() - player.getX()) / BLOCKS_PER_PIXEL);
            int mapY = centerY + Mth.floor((entity.getZ() - player.getZ()) / BLOCKS_PER_PIXEL);
            int minX = FRAME_PADDING + 1;
            int maxX = FRAME_PADDING + MAP_SIZE - 2;
            int minY = FRAME_PADDING + HEADER_HEIGHT + 1;
            int maxY = FRAME_PADDING + HEADER_HEIGHT + MAP_SIZE - 2;

            if (mapX < minX || mapX > maxX || mapY < minY || mapY > maxY) continue;

            int color = living instanceof Enemy ? HOSTILE_COLOR : living instanceof Player ? PLAYER_CONTACT_COLOR : NEUTRAL_COLOR;

            gg.fill(mapX - 1, mapY - 1, mapX + 2, mapY + 2, color);
        }
    }

    private static void renderPlayerMarker(GuiGraphics gg, LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        int centerX = FRAME_PADDING + MAP_SIZE / 2;
        int centerY = FRAME_PADDING + HEADER_HEIGHT + MAP_SIZE / 2;
        int markerSize = 8;

        MapDecoration decoration = new MapDecoration(MapDecorationTypes.PLAYER, (byte) 0, (byte) 0, (byte) 0, Optional.empty());
        TextureAtlasSprite sprite = mc.getMapDecorationTextures().get(decoration);

        gg.pose().pushPose();
        gg.pose().translate(centerX + 0.5f, centerY + 0.5f, 0);
        gg.pose().mulPose(Axis.ZP.rotationDegrees(player.getYRot() + 180.0f));

        gg.blit(-markerSize / 2, -markerSize / 2, 0, markerSize, markerSize, sprite);

        gg.pose().popPose();
    }

    private static void renderNorthMarker(GuiGraphics gg, Minecraft mc) {
        int centerX = FRAME_PADDING + MAP_SIZE / 2;
        int topY = FRAME_PADDING + HEADER_HEIGHT + 2;
        Component north = Component.translatable("gui.createcybernetics.navigation.direction.north_short");
        int northX = centerX - mc.font.width(north) / 2;

        gg.drawString(mc.font, north, northX, topY, NORTH_COLOR, true);
        gg.fill(centerX, topY + mc.font.lineHeight, centerX + 1, topY + mc.font.lineHeight + 4, NORTH_COLOR);
    }

    private static void renderScanline(GuiGraphics gg, LocalPlayer player) {
        int startX = FRAME_PADDING;
        int startY = FRAME_PADDING + HEADER_HEIGHT;
        int scanY = startY + player.tickCount % MAP_SIZE;

        gg.fill(startX, scanY, startX + MAP_SIZE, scanY + 1, SCANLINE_COLOR);
    }

    private static void renderFrame(GuiGraphics gg) {
        gg.fill(0, 0, TOTAL_WIDTH, 1, FRAME_COLOR);
        gg.fill(0, TOTAL_HEIGHT - 1, TOTAL_WIDTH, TOTAL_HEIGHT, FRAME_COLOR);
        gg.fill(0, 0, 1, TOTAL_HEIGHT, FRAME_COLOR);
        gg.fill(TOTAL_WIDTH - 1, 0, TOTAL_WIDTH, TOTAL_HEIGHT, FRAME_COLOR);

        gg.fill(3, 3, TOTAL_WIDTH - 3, 4, FRAME_DARK_COLOR);
        gg.fill(3, TOTAL_HEIGHT - 4, TOTAL_WIDTH - 3, TOTAL_HEIGHT - 3, FRAME_DARK_COLOR);

        gg.fill(0, 0, 9, 2, FRAME_COLOR);
        gg.fill(0, 0, 2, 9, FRAME_COLOR);
        gg.fill(TOTAL_WIDTH - 9, 0, TOTAL_WIDTH, 2, FRAME_COLOR);
        gg.fill(TOTAL_WIDTH - 2, 0, TOTAL_WIDTH, 9, FRAME_COLOR);

        gg.fill(0, TOTAL_HEIGHT - 2, 9, TOTAL_HEIGHT, FRAME_COLOR);
        gg.fill(0, TOTAL_HEIGHT - 9, 2, TOTAL_HEIGHT, FRAME_COLOR);
        gg.fill(TOTAL_WIDTH - 9, TOTAL_HEIGHT - 2, TOTAL_WIDTH, TOTAL_HEIGHT, FRAME_COLOR);
        gg.fill(TOTAL_WIDTH - 2, TOTAL_HEIGHT - 9, TOTAL_WIDTH, TOTAL_HEIGHT, FRAME_COLOR);
    }

    private static Component getCardinalDirection(float yaw) {
        int index = Mth.floor(yaw / 45.0f + 0.5f) & 7;

        return Component.translatable(switch (index) {
            case 0 -> "gui.createcybernetics.navigation.direction.south_short";
            case 1 -> "gui.createcybernetics.navigation.direction.southwest_short";
            case 2 -> "gui.createcybernetics.navigation.direction.west_short";
            case 3 -> "gui.createcybernetics.navigation.direction.northwest_short";
            case 4 -> "gui.createcybernetics.navigation.direction.north_short";
            case 5 -> "gui.createcybernetics.navigation.direction.northeast_short";
            case 6 -> "gui.createcybernetics.navigation.direction.east_short";
            default -> "gui.createcybernetics.navigation.direction.southeast_short";
        });
    }
}
