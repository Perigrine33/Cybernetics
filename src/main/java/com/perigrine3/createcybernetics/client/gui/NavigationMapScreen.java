package com.perigrine3.createcybernetics.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.client.MinimapWaypointClient;
import com.perigrine3.createcybernetics.client.navigation.SharedNavigationClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class NavigationMapScreen extends Screen {

    private static final int BACKGROUND_COLOR = 0xF0020607;
    private static final int MAP_BACKGROUND_COLOR = 0xFF050B0C;
    private static final int GRID_COLOR = 0x2600E5FF;
    private static final int FRAME_COLOR = 0xFF00E5FF;
    private static final int TEXT_COLOR = 0xFF00E5FF;
    private static final int ACTIVE_COLOR = 0xFFFFFF55;

    private static final int MAP_LEFT = 8;
    private static final int MAP_TOP = 24;
    private static final int MAP_RIGHT_PADDING = 8;
    private static final int MAP_BOTTOM_PADDING = 8;

    private static final int CONTEXT_MENU_WIDTH = 112;
    private static final int CONTEXT_MENU_HEIGHT = 20;
    private static final int CONTEXT_MENU_BACKGROUND = 0xEE050505;
    private static final int CONTEXT_MENU_BORDER = 0xFF00E5FF;
    private static final int CONTEXT_MENU_HOVER = 0xAA553333;
    private static final int CONTEXT_MENU_TEXT = 0xFFFF5555;

    private static final float HEADER_TITLE_SCALE = 0.75f;
    private static final float HEADER_GUIDE_SCALE = 0.55f;

    private static final double MIN_ZOOM = 0.05D;
    private static final double MAX_ZOOM = 8.0D;
    private static final double DEFAULT_ZOOM = 1.0D;
    private static final double ZOOM_STEP = 1.20D;

    private static final int DATA_INDEX_REFRESH_INTERVAL = 40;

    private final Screen parent;

    private final Map<TileKey, MinimapWaypointClient.ExploredTile> tileIndex = new HashMap<>();
    private final Map<MinimapWaypointClient.ExploredTile, TileTextureCache> tileTextureCaches = new IdentityHashMap<>();
    private final Map<String, List<MinimapWaypointClient.Waypoint>> waypointsByDimension = new HashMap<>();

    private double cameraX;
    private double cameraZ;
    private double zoom = DEFAULT_ZOOM;

    private boolean initializedCamera;
    private boolean dragging;
    private double lastMouseX;
    private double lastMouseY;

    private int lastDataIndexRefreshTick = Integer.MIN_VALUE;

    private @Nullable MinimapWaypointClient.Waypoint hoveredWaypoint;
    private @Nullable MinimapWaypointClient.Waypoint contextWaypoint;

    private int contextMenuX;
    private int contextMenuY;
    private boolean contextMenuOpen;
    private int contextWorldX;
    private int contextWorldZ;
    private int textureUploadsRemaining;

    public NavigationMapScreen(Screen parent) {
        super(Component.translatable("screen.createcybernetics.navigation_map"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null && !initializedCamera) {
            cameraX = player.getX();
            cameraZ = player.getZ();
            initializedCamera = true;
        }

        if (player != null) {
            rebuildDataIndexes(player);
        }
    }

    @Override
    public void tick() {
        super.tick();

        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) return;

        if (lastDataIndexRefreshTick == Integer.MIN_VALUE || player.tickCount - lastDataIndexRefreshTick >= DATA_INDEX_REFRESH_INTERVAL) {
            rebuildDataIndexes(player);
        }
    }

    @Override
    public void renderBackground(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        LocalPlayer player = Minecraft.getInstance().player;

        gg.fill(0, 0, width, height, BACKGROUND_COLOR);
        textureUploadsRemaining = 4;
        gg.fill(MAP_LEFT, MAP_TOP, mapRight(), mapBottom(), MAP_BACKGROUND_COLOR);

        if (player != null) {
            gg.enableScissor(MAP_LEFT, MAP_TOP, mapRight(), mapBottom());

            renderExploredTerrain(gg, player);
            renderGrid(gg);
            renderGuidanceLine(gg, player);
            renderWaypoints(gg, player, mouseX, mouseY);
            renderSharedPlayers(gg, player);
            renderPlayer(gg, player);

            gg.disableScissor();
        }

        renderFrame(gg);
        renderHeader(gg, player);
        renderContextMenu(gg, mouseX, mouseY);

        super.render(gg, mouseX, mouseY, partialTick);
    }

    private void rebuildDataIndexes(LocalPlayer player) {
        tileIndex.clear();
        waypointsByDimension.clear();

        Set<MinimapWaypointClient.ExploredTile> currentTiles = new HashSet<>();

        for (MinimapWaypointClient.ExploredTile tile : MinimapWaypointClient.getExploredTiles(player)) {
            tileIndex.put(new TileKey(tile.dimension(), tile.tileX(), tile.tileZ()), tile);
            currentTiles.add(tile);
        }

        tileTextureCaches.entrySet().removeIf(entry -> {
            if (currentTiles.contains(entry.getKey())) return false;

            entry.getValue().close();
            return true;
        });

        for (MinimapWaypointClient.Waypoint waypoint : MinimapWaypointClient.getWaypoints(player)) {
            waypointsByDimension.computeIfAbsent(waypoint.dimension(), ignored -> new ArrayList<>()).add(waypoint);
        }

        lastDataIndexRefreshTick = player.tickCount;
    }

    private void renderExploredTerrain(GuiGraphics gg, LocalPlayer player) {
        String dimension = player.level().dimension().location().toString();
        double minWorldX = Math.min(screenToWorldX(MAP_LEFT), screenToWorldX(mapRight()));
        double maxWorldX = Math.max(screenToWorldX(MAP_LEFT), screenToWorldX(mapRight()));
        double minWorldZ = Math.min(screenToWorldZ(MAP_TOP), screenToWorldZ(mapBottom()));
        double maxWorldZ = Math.max(screenToWorldZ(MAP_TOP), screenToWorldZ(mapBottom()));

        int tileSize = MinimapWaypointClient.EXPLORATION_TILE_SIZE;
        int minTileX = Math.floorDiv(Mth.floor(minWorldX), tileSize);
        int maxTileX = Math.floorDiv(Mth.floor(maxWorldX), tileSize);
        int minTileZ = Math.floorDiv(Mth.floor(minWorldZ), tileSize);
        int maxTileZ = Math.floorDiv(Mth.floor(maxWorldZ), tileSize);

        // At minimum zoom a large display can span hundreds of thousands of empty
        // tile coordinates. Visit existing tiles instead when that is cheaper.
        if ((long) (maxTileX - minTileX + 1) * (maxTileZ - minTileZ + 1) > tileIndex.size()) {
            for (var tile : tileIndex.values()) {
                if (!dimension.equals(tile.dimension()) || tile.tileX() < minTileX || tile.tileX() > maxTileX
                        || tile.tileZ() < minTileZ || tile.tileZ() > maxTileZ) continue;
                renderExploredTile(gg, tile, minWorldX, maxWorldX, minWorldZ, maxWorldZ);
            }
        } else for (int tileZ = minTileZ; tileZ <= maxTileZ; tileZ++) {
            for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
                MinimapWaypointClient.ExploredTile tile = tileIndex.get(new TileKey(dimension, tileX, tileZ));

                if (tile == null) continue;

                renderExploredTile(gg, tile, minWorldX, maxWorldX, minWorldZ, maxWorldZ);
            }
        }

        // Panning must not retain GPU textures for every tile ever viewed.
        tileTextureCaches.entrySet().removeIf(entry -> {
            var tile = entry.getKey();
            if (dimension.equals(tile.dimension()) && tile.tileX() >= minTileX - 1 && tile.tileX() <= maxTileX + 1
                    && tile.tileZ() >= minTileZ - 1 && tile.tileZ() <= maxTileZ + 1) return false;
            entry.getValue().close();
            return true;
        });
    }

    private void renderExploredTile(GuiGraphics gg, MinimapWaypointClient.ExploredTile tile, double minWorldX, double maxWorldX, double minWorldZ, double maxWorldZ) {
        TileTextureCache textureCache = tileTextureCaches.get(tile);
        if (textureCache == null) {
            if (textureUploadsRemaining == 0) return;
            textureCache = new TileTextureCache(tile);
            tileTextureCaches.put(tile, textureCache);
        }
        if (textureCache.uploadedRevision != tile.revision() && textureUploadsRemaining > 0) {
            textureCache.ensureCurrent();
            textureUploadsRemaining--;
        }

        int screenX0 = worldToScreenX(tile.minWorldX());
        int screenY0 = worldToScreenY(tile.minWorldZ());
        int screenX1 = worldToScreenX(tile.minWorldX() + MinimapWaypointClient.EXPLORATION_TILE_SIZE);
        int screenY1 = worldToScreenY(tile.minWorldZ() + MinimapWaypointClient.EXPLORATION_TILE_SIZE);

        int drawX0 = Math.max(MAP_LEFT, Math.min(screenX0, screenX1));
        int drawY0 = Math.max(MAP_TOP, Math.min(screenY0, screenY1));
        int drawX1 = Math.min(mapRight(), Math.max(screenX0, screenX1));
        int drawY1 = Math.min(mapBottom(), Math.max(screenY0, screenY1));

        if (drawX1 <= drawX0 || drawY1 <= drawY0) return;

        double tileScreenWidth = Math.max(1.0D, Math.abs(screenX1 - screenX0));
        double tileScreenHeight = Math.max(1.0D, Math.abs(screenY1 - screenY0));
        float u = (float) ((drawX0 - Math.min(screenX0, screenX1)) / tileScreenWidth * MinimapWaypointClient.EXPLORATION_TILE_SIZE);
        float v = (float) ((drawY0 - Math.min(screenY0, screenY1)) / tileScreenHeight * MinimapWaypointClient.EXPLORATION_TILE_SIZE);
        int sourceWidth = Math.max(1, Mth.ceil((drawX1 - drawX0) / tileScreenWidth * MinimapWaypointClient.EXPLORATION_TILE_SIZE));
        int sourceHeight = Math.max(1, Mth.ceil((drawY1 - drawY0) / tileScreenHeight * MinimapWaypointClient.EXPLORATION_TILE_SIZE));

        gg.blit(textureCache.location(), drawX0, drawY0, drawX1 - drawX0, drawY1 - drawY0, u, v, sourceWidth, sourceHeight, MinimapWaypointClient.EXPLORATION_TILE_SIZE, MinimapWaypointClient.EXPLORATION_TILE_SIZE);
    }

    private void renderGrid(GuiGraphics gg) {
        double gridSize = chooseGridSize();
        double minWorldX = Math.min(screenToWorldX(MAP_LEFT), screenToWorldX(mapRight()));
        double maxWorldX = Math.max(screenToWorldX(MAP_LEFT), screenToWorldX(mapRight()));
        double minWorldZ = Math.min(screenToWorldZ(MAP_TOP), screenToWorldZ(mapBottom()));
        double maxWorldZ = Math.max(screenToWorldZ(MAP_TOP), screenToWorldZ(mapBottom()));
        double firstX = Math.floor(minWorldX / gridSize) * gridSize;

        for (double worldX = firstX; worldX <= maxWorldX; worldX += gridSize) {
            int screenX = worldToScreenX(worldX);

            if (screenX >= MAP_LEFT && screenX < mapRight()) {
                gg.fill(screenX, MAP_TOP, screenX + 1, mapBottom(), GRID_COLOR);
            }
        }

        double firstZ = Math.floor(minWorldZ / gridSize) * gridSize;

        for (double worldZ = firstZ; worldZ <= maxWorldZ; worldZ += gridSize) {
            int screenY = worldToScreenY(worldZ);

            if (screenY >= MAP_TOP && screenY < mapBottom()) {
                gg.fill(MAP_LEFT, screenY, mapRight(), screenY + 1, GRID_COLOR);
            }
        }
    }

    private double chooseGridSize() {
        double[] sizes = {16.0D, 32.0D, 64.0D, 128.0D, 256.0D, 512.0D, 1024.0D, 2048.0D};

        for (double size : sizes) {
            if (size * zoom >= 36.0D) {
                return size;
            }
        }

        return 4096.0D;
    }

    private void renderWaypoints(GuiGraphics gg, LocalPlayer player, int mouseX, int mouseY) {
        String dimension = player.level().dimension().location().toString();
        List<MinimapWaypointClient.Waypoint> waypoints = waypointsByDimension.getOrDefault(dimension, List.of());
        MinimapWaypointClient.Waypoint activeWaypoint = MinimapWaypointClient.getActiveWaypoint(player);
        hoveredWaypoint = null;
        double closestHoverDistance = 64.0D;
        double minWorldX = Math.min(screenToWorldX(MAP_LEFT), screenToWorldX(mapRight()));
        double maxWorldX = Math.max(screenToWorldX(MAP_LEFT), screenToWorldX(mapRight()));
        double minWorldZ = Math.min(screenToWorldZ(MAP_TOP), screenToWorldZ(mapBottom()));
        double maxWorldZ = Math.max(screenToWorldZ(MAP_TOP), screenToWorldZ(mapBottom()));

        for (MinimapWaypointClient.Waypoint waypoint : waypoints) {
            double waypointWorldX = waypoint.x() + 0.5D;
            double waypointWorldZ = waypoint.z() + 0.5D;

            if (waypointWorldX < minWorldX || waypointWorldX > maxWorldX || waypointWorldZ < minWorldZ || waypointWorldZ > maxWorldZ) continue;

            int x = worldToScreenX(waypointWorldX);
            int y = worldToScreenY(waypointWorldZ);

            if (x < MAP_LEFT || x >= mapRight() || y < MAP_TOP || y >= mapBottom()) continue;

            boolean active = activeWaypoint != null && activeWaypoint.id().equals(waypoint.id());
            int color = active ? ACTIVE_COLOR : waypoint.color();

            renderWaypointMarker(gg, x, y, color, active);

            double mouseDistance = square(mouseX - x) + square(mouseY - y);

            if (mouseDistance <= closestHoverDistance) {
                closestHoverDistance = mouseDistance;
                hoveredWaypoint = waypoint;
            }
        }

        if (hoveredWaypoint != null && !contextMenuOpen) {
            int x = worldToScreenX(hoveredWaypoint.x() + 0.5D);
            int y = worldToScreenY(hoveredWaypoint.z() + 0.5D);
            int distance = Mth.floor(Math.sqrt(square(player.getX() - hoveredWaypoint.x()) + square(player.getZ() - hoveredWaypoint.z())));
            Component label = Component.translatable("gui.createcybernetics.navigation.waypoint_distance", waypointName(hoveredWaypoint), distance);

            gg.renderTooltip(font, label, x + 6, y + 6);
        }
    }

    private void renderWaypointMarker(GuiGraphics gg, int x, int y, int color, boolean active) {
        int size = active ? 4 : 3;

        gg.fill(x - size, y, x + size + 1, y + 1, color);
        gg.fill(x, y - size, x + 1, y + size + 1, color);
        gg.fill(x - 1, y - 1, x + 2, y + 2, color);

        if (active) {
            gg.fill(x - 5, y - 5, x + 6, y - 4, 0x88FFFF55);
            gg.fill(x - 5, y + 5, x + 6, y + 6, 0x88FFFF55);
            gg.fill(x - 5, y - 5, x - 4, y + 6, 0x88FFFF55);
            gg.fill(x + 5, y - 5, x + 6, y + 6, 0x88FFFF55);
        }
    }

    private void renderSharedPlayers(GuiGraphics gg, LocalPlayer player) {
        String currentDimension = player.level().dimension().location().toString();

        for (SharedNavigationClientState.SharedPlayer sharedPlayer : SharedNavigationClientState.getPlayers()) {
            if (sharedPlayer.playerId().equals(player.getUUID())) continue;
            if (!sharedPlayer.dimension().equals(currentDimension)) continue;

            int x = worldToScreenX(sharedPlayer.x());
            int y = worldToScreenY(sharedPlayer.z());

            if (x < MAP_LEFT || x >= mapRight() || y < MAP_TOP || y >= mapBottom()) continue;

            gg.fill(x - 3, y, x + 4, y + 1, 0xFF00E5FF);
            gg.fill(x, y - 3, x + 1, y + 4, 0xFF00E5FF);
            gg.fill(x - 1, y - 1, x + 2, y + 2, 0xFFFFFFFF);

            int nameX = x - font.width(sharedPlayer.name()) / 2;
            int nameY = y - font.lineHeight - 5;

            gg.drawString(font, sharedPlayer.name(), nameX, nameY, 0xFF00E5FF, true);
        }
    }

    private void renderPlayer(GuiGraphics gg, LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        int x = worldToScreenX(player.getX());
        int y = worldToScreenY(player.getZ());

        if (x < MAP_LEFT || x >= mapRight() || y < MAP_TOP || y >= mapBottom()) return;

        int markerSize = 12;
        MapDecoration decoration = new MapDecoration(MapDecorationTypes.PLAYER, (byte) 0, (byte) 0, (byte) 0, Optional.empty());
        TextureAtlasSprite sprite = mc.getMapDecorationTextures().get(decoration);

        gg.pose().pushPose();
        gg.pose().translate(x + 0.5f, y + 0.5f, 100);
        gg.pose().mulPose(Axis.ZP.rotationDegrees(player.getYRot() + 180.0f));
        gg.blit(-markerSize / 2, -markerSize / 2, 0, markerSize, markerSize, sprite);
        gg.pose().popPose();
    }

    private void renderGuidanceLine(GuiGraphics gg, LocalPlayer player) {
        MinimapWaypointClient.Waypoint activeWaypoint = MinimapWaypointClient.getActiveWaypoint(player);

        if (activeWaypoint == null) return;
        if (!activeWaypoint.dimension().equals(player.level().dimension().location().toString())) return;

        int playerX = worldToScreenX(player.getX());
        int playerY = worldToScreenY(player.getZ());
        int waypointX = worldToScreenX(activeWaypoint.x() + 0.5D);
        int waypointY = worldToScreenY(activeWaypoint.z() + 0.5D);

        drawClippedLine(gg, playerX, playerY, waypointX, waypointY, 0xAAFFFF55);
    }

    private void renderContextMenu(GuiGraphics gg, int mouseX, int mouseY) {
        if (!contextMenuOpen) return;

        int x0 = contextMenuX;
        int y0 = contextMenuY;
        int x1 = x0 + CONTEXT_MENU_WIDTH;
        int y1 = y0 + CONTEXT_MENU_HEIGHT;
        boolean hovered = mouseX >= x0 && mouseX < x1 && mouseY >= y0 && mouseY < y1;

        gg.fill(x0, y0, x1, y1, hovered ? CONTEXT_MENU_HOVER : CONTEXT_MENU_BACKGROUND);
        gg.fill(x0, y0, x1, y0 + 1, CONTEXT_MENU_BORDER);
        gg.fill(x0, y1 - 1, x1, y1, CONTEXT_MENU_BORDER);
        gg.fill(x0, y0, x0 + 1, y1, CONTEXT_MENU_BORDER);
        gg.fill(x1 - 1, y0, x1, y1, CONTEXT_MENU_BORDER);
        String action = contextWaypoint == null ? "add" : SharedNavigationClientState.isSharedWaypoint(contextWaypoint.id()) ? "hide" : "delete";
        gg.drawCenteredString(font, Component.translatable("gui.createcybernetics.navigation." + action), x0 + CONTEXT_MENU_WIDTH / 2, y0 + 6, CONTEXT_MENU_TEXT);
    }

    private void renderFrame(GuiGraphics gg) {
        gg.fill(0, 0, width, 1, FRAME_COLOR);
        gg.fill(0, height - 1, width, height, FRAME_COLOR);
        gg.fill(0, 0, 1, height, FRAME_COLOR);
        gg.fill(width - 1, 0, width, height, FRAME_COLOR);
        gg.fill(0, 22, width, 24, FRAME_COLOR);
    }

    private void renderHeader(GuiGraphics gg, LocalPlayer player) {
        Component title = Component.translatable("gui.createcybernetics.navigation.title");
        Component guide = Component.translatable("gui.createcybernetics.navigation.instructions");

        drawScaledString(gg, title, 8, 7, HEADER_TITLE_SCALE, TEXT_COLOR, false);
        drawScaledString(gg, guide, 130, 8, HEADER_GUIDE_SCALE, 0xFFAAAAAA, false);

        if (player != null) {
            Component position = Component.translatable("gui.createcybernetics.navigation.position_zoom", Mth.floor(cameraX), Mth.floor(cameraZ), String.format("%.2f", zoom));
            int positionX = width - font.width(position) - 8;
            int positionY = 28;
            gg.drawString(font, position, positionX, positionY, TEXT_COLOR, true);
        }
    }

    private void drawScaledString(GuiGraphics gg, Component text, int x, int y, float scale, int color, boolean shadow) {
        gg.pose().pushPose();
        gg.pose().translate(x, y, 0);
        gg.pose().scale(scale, scale, 1.0f);
        gg.drawString(font, text, 0, 0, color, shadow);
        gg.pose().popPose();
    }

    private Component waypointName(MinimapWaypointClient.Waypoint waypoint) {
        return MinimapWaypointClient.getWaypointDisplayName(waypoint);
    }

    private void drawClippedLine(GuiGraphics gg, int x0, int y0, int x1, int y1, int color) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double[] range = {0.0D, 1.0D};

        if (!clipLine(-dx, x0 - MAP_LEFT, range)) return;
        if (!clipLine(dx, mapRight() - 1 - x0, range)) return;
        if (!clipLine(-dy, y0 - MAP_TOP, range)) return;
        if (!clipLine(dy, mapBottom() - 1 - y0, range)) return;

        int clippedX0 = Mth.floor(x0 + range[0] * dx);
        int clippedY0 = Mth.floor(y0 + range[0] * dy);
        int clippedX1 = Mth.floor(x0 + range[1] * dx);
        int clippedY1 = Mth.floor(y0 + range[1] * dy);

        drawLine(gg, clippedX0, clippedY0, clippedX1, clippedY1, color);
    }

    private boolean clipLine(double p, double q, double[] range) {
        if (p == 0.0D) {
            return q >= 0.0D;
        }

        double ratio = q / p;

        if (p < 0.0D) {
            if (ratio > range[1]) return false;
            if (ratio > range[0]) range[0] = ratio;
        } else {
            if (ratio < range[0]) return false;
            if (ratio < range[1]) range[1] = ratio;
        }

        return true;
    }

    private void drawLine(GuiGraphics gg, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int stepX = x0 < x1 ? 1 : -1;
        int stepY = y0 < y1 ? 1 : -1;
        int error = dx - dy;

        while (true) {
            gg.fill(x0, y0, x0 + 1, y0 + 1, color);

            if (x0 == x1 && y0 == y1) {
                break;
            }

            int doubledError = error * 2;

            if (doubledError > -dy) {
                error -= dy;
                x0 += stepX;
            }

            if (doubledError < dx) {
                error += dx;
                y0 += stepY;
            }
        }
    }

    private int worldToScreenX(double worldX) {
        return Mth.floor(width / 2.0D + (worldX - cameraX) * zoom);
    }

    private int worldToScreenY(double worldZ) {
        return Mth.floor(height / 2.0D + (worldZ - cameraZ) * zoom);
    }

    private double screenToWorldX(double screenX) {
        return cameraX + (screenX - width / 2.0D) / zoom;
    }

    private double screenToWorldZ(double screenY) {
        return cameraZ + (screenY - height / 2.0D) / zoom;
    }

    private int mapRight() {
        return width - MAP_RIGHT_PADDING;
    }

    private int mapBottom() {
        return height - MAP_BOTTOM_PADDING;
    }

    private boolean contextMenuContains(double mouseX, double mouseY) {
        return mouseX >= contextMenuX && mouseX < contextMenuX + CONTEXT_MENU_WIDTH && mouseY >= contextMenuY && mouseY < contextMenuY + CONTEXT_MENU_HEIGHT;
    }

    private void openContextMenu(@Nullable MinimapWaypointClient.Waypoint waypoint, double mouseX, double mouseY) {
        contextMenuOpen = true;
        contextWaypoint = waypoint;
        contextWorldX = Mth.floor(screenToWorldX(mouseX));
        contextWorldZ = Mth.floor(screenToWorldZ(mouseY));
        contextMenuX = Mth.clamp(Mth.floor(mouseX), 0, Math.max(0, width - CONTEXT_MENU_WIDTH));
        contextMenuY = Mth.clamp(Mth.floor(mouseY), 0, Math.max(0, height - CONTEXT_MENU_HEIGHT));
        dragging = false;
    }

    private void closeContextMenu() {
        contextMenuOpen = false;
        contextWaypoint = null;
    }

    private boolean isInsideMap(double mouseX, double mouseY) {
        return mouseX >= MAP_LEFT && mouseX < mapRight() && mouseY >= MAP_TOP && mouseY < mapBottom();
    }

    private @Nullable MinimapWaypointClient.Waypoint waypointAt(LocalPlayer player, double mouseX, double mouseY) {
        if (player == null || !isInsideMap(mouseX, mouseY)) return null;
        MinimapWaypointClient.Waypoint closest = null;
        double distance = 64.0D;
        String dimension = player.level().dimension().location().toString();
        for (var waypoint : waypointsByDimension.getOrDefault(dimension, List.of())) {
            int x = worldToScreenX(waypoint.x() + 0.5D);
            int y = worldToScreenY(waypoint.z() + 0.5D);
            if (!isInsideMap(x, y)) continue;
            double candidate = square(mouseX - x) + square(mouseY - y);
            if (candidate <= distance) { closest = waypoint; distance = candidate; }
        }
        return closest;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (contextMenuOpen) {
            if (button == 0 && contextMenuContains(mouseX, mouseY)) {
                if (player != null) {
                    if (contextWaypoint == null) {
                        Minecraft.getInstance().setScreen(new NavigationWaypointScreen(this, contextWorldX, contextWorldZ));
                    } else {
                        MinimapWaypointClient.removeWaypoint(player, contextWaypoint);
                        rebuildDataIndexes(player);
                    }
                }

                closeContextMenu();
                return true;
            }

            closeContextMenu();
        }

        hoveredWaypoint = waypointAt(player, mouseX, mouseY);
        if (button == 1 && isInsideMap(mouseX, mouseY)) {
            openContextMenu(hoveredWaypoint, mouseX, mouseY);
            return true;
        }

        if (button == 0 && hoveredWaypoint != null) {
            if (player != null) {
                MinimapWaypointClient.Waypoint activeWaypoint = MinimapWaypointClient.getActiveWaypoint(player);

                if (activeWaypoint != null && activeWaypoint.id().equals(hoveredWaypoint.id())) {
                    MinimapWaypointClient.clearActiveWaypoint(player);
                } else {
                    MinimapWaypointClient.setActiveWaypoint(player, hoveredWaypoint);
                }
            }

            return true;
        }

        if (button == 0 && isInsideMap(mouseX, mouseY)) {
            dragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && dragging) {
            cameraX -= (mouseX - lastMouseX) / zoom;
            cameraZ -= (mouseY - lastMouseY) / zoom;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY == 0.0D) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        closeContextMenu();

        double worldXBefore = screenToWorldX(mouseX);
        double worldZBefore = screenToWorldZ(mouseY);

        if (scrollY > 0.0D) {
            zoom = Math.min(MAX_ZOOM, zoom * ZOOM_STEP);
        } else {
            zoom = Math.max(MIN_ZOOM, zoom / ZOOM_STEP);
        }

        double worldXAfter = screenToWorldX(mouseX);
        double worldZAfter = screenToWorldZ(mouseY);

        cameraX += worldXBefore - worldXAfter;
        cameraZ += worldZBefore - worldZAfter;

        return true;
    }

    @Override
    public void onClose() {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null) {
            MinimapWaypointClient.saveIfDirty(player.getUUID());
        }

        tileIndex.clear();
        for (TileTextureCache cache : tileTextureCaches.values()) {
            cache.close();
        }

        tileTextureCaches.clear();
        waypointsByDimension.clear();

        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    private static double square(double value) {
        return value * value;
    }

    private record TileKey(String dimension, int tileX, int tileZ) {}

    private static final class TileTextureCache {

        private static long nextTextureId;

        private final MinimapWaypointClient.ExploredTile tile;
        private final DynamicTexture texture;
        private final ResourceLocation location;

        private long uploadedRevision = Long.MIN_VALUE;

        private TileTextureCache(MinimapWaypointClient.ExploredTile tile) {
            this.tile = tile;
            this.texture = new DynamicTexture(MinimapWaypointClient.EXPLORATION_TILE_SIZE, MinimapWaypointClient.EXPLORATION_TILE_SIZE, true);
            this.location = ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "navigation/tile_" + nextTextureId++);
            Minecraft.getInstance().getTextureManager().register(location, texture);
            texture.setFilter(false, false);
        }

        private ResourceLocation location() {
            return location;
        }

        private void ensureCurrent() {
            if (uploadedRevision == tile.revision()) return;

            NativeImage pixels = texture.getPixels();

            if (pixels == null) return;

            for (int localZ = 0; localZ < MinimapWaypointClient.EXPLORATION_TILE_SIZE; localZ++) {
                for (int localX = 0; localX < MinimapWaypointClient.EXPLORATION_TILE_SIZE; localX++) {
                    int color = tile.isExplored(localX, localZ) ? tile.color(localX, localZ) : 0;
                    pixels.setPixelRGBA(localX, localZ, argbToAbgr(color));
                }
            }

            texture.upload();
            uploadedRevision = tile.revision();
        }

        private void close() {
            Minecraft.getInstance().getTextureManager().release(location);
        }

        private static int argbToAbgr(int color) {
            return color & 0xFF00FF00 | color >> 16 & 0x000000FF | color << 16 & 0x00FF0000;
        }
    }

}
