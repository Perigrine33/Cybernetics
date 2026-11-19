package com.perigrine3.createcybernetics.client.gui;

import com.perigrine3.createcybernetics.client.MinimapWaypointClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Names a waypoint at the exact world position selected in the map. */
public final class NavigationWaypointScreen extends Screen {
    private final Screen parent;
    private final int worldX;
    private final int worldZ;
    private EditBox name;

    public NavigationWaypointScreen(Screen parent, int worldX, int worldZ) {
        super(Component.translatable("gui.createcybernetics.navigation.add"));
        this.parent = parent;
        this.worldX = worldX;
        this.worldZ = worldZ;
    }

    @Override
    protected void init() {
        String previous = name == null ? "" : name.getValue();
        name = addRenderableWidget(new EditBox(font, width / 2 - 100, height / 2 - 10, 200, 20,
                Component.translatable("gui.createcybernetics.navigation.waypoint_name")));
        name.setMaxLength(128);
        name.setValue(previous);
        name.setHint(Component.translatable("gui.createcybernetics.navigation.default_waypoint"));
        addRenderableWidget(Button.builder(Component.translatable("gui.createcybernetics.save"), button -> save())
                .bounds(width / 2 - 100, height / 2 + 20, 96, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(width / 2 + 4, height / 2 + 20, 96, 20).build());
        setInitialFocus(name);
    }

    private void save() {
        if (minecraft != null && minecraft.player != null) {
            MinimapWaypointClient.addCustomWaypoint(minecraft.player, worldX, worldZ, name.getValue());
        }
        onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            save();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xFF020607);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 45, 0xFF00E5FF);
        graphics.drawCenteredString(font, Component.literal(worldX + ", " + worldZ), width / 2, height / 2 - 28, 0xFFAAAAAA);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }
}
