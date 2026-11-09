package com.perigrine3.createcybernetics.screen.custom.cyberdeck;

import com.perigrine3.createcybernetics.client.RadialMenuController;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.item.macroprogram.MacroprogramItem;
import com.perigrine3.createcybernetics.network.payload.ExecuteMacroprogramPayload;
import com.perigrine3.createcybernetics.util.ModTags;
import com.perigrine3.createcybernetics.world.dimension.ModDimensions;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class MacroprogramWheelScreen extends Screen {
    private static final ResourceLocation LAYER_ID = ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "macroprogram_wheel");

    private static final int RAM_AFFORDABLE_COLOR = 0xFF7FDBFF;
    private static final int RAM_UNAFFORDABLE_COLOR = 0xFFFF5555;

    private static boolean OPEN = false;
    private static int SELECTED_INDEX = 0;

    private record Entry(int cyberdeckSlot, ItemStack icon, int ramCost) {}

    private static final List<Entry> ENTRIES = new ArrayList<>();

    public MacroprogramWheelScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || !mc.player.level().dimension().equals(ModDimensions.CYBERSPACE_LEVEL)) {
            OPEN = false;

            if (this.minecraft != null && this.minecraft.screen == this) {
                this.minecraft.setScreen(null);
            }

            return;
        }

        RadialMenuController.prepareToOpen(RadialMenuController.Wheel.MACROPROGRAMS);
        OPEN = true;
        SELECTED_INDEX = 0;
        rebuildEntries(mc);

        if (this.minecraft != null && this.minecraft.screen == this) {
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static boolean isWheelOpen() {
        return OPEN;
    }

    public static void closeWheel() {
        OPEN = false;
        ENTRIES.clear();
    }

    public static void scrollSelection(double scrollDelta) {
        if (!OPEN) return;
        if (scrollDelta == 0.0D) return;

        rebuildEntries(Minecraft.getInstance());

        if (ENTRIES.isEmpty()) {
            SELECTED_INDEX = 0;
            return;
        }

        int direction = scrollDelta > 0.0D ? -1 : 1;
        SELECTED_INDEX = Math.floorMod(SELECTED_INDEX + direction, ENTRIES.size());
    }

    public static void activateSelected() {
        if (!OPEN) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!mc.player.level().dimension().equals(ModDimensions.CYBERSPACE_LEVEL)) return;

        rebuildEntries(mc);

        if (ENTRIES.isEmpty()) return;

        SELECTED_INDEX = Mth.clamp(SELECTED_INDEX, 0, ENTRIES.size() - 1);

        Entry entry = ENTRIES.get(SELECTED_INDEX);
        PacketDistributor.sendToServer(new ExecuteMacroprogramPayload(entry.cyberdeckSlot()));

        closeWheel();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    public boolean shouldBlurBackground() {
        return false;
    }

    @Override
    public void onClose() {
        closeWheel();
        super.onClose();
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ClientModBus {
        @SubscribeEvent
        public static void registerGuiLayers(RegisterGuiLayersEvent event) {
            event.registerAboveAll(LAYER_ID, MacroprogramWheelScreen::renderHudLayer);
        }
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static final class ClientGameBus {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
            if (!OPEN) return;

            event.setCanceled(true);
            scrollSelection(event.getScrollDeltaY());
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onMouseButton(InputEvent.MouseButton.Pre event) {
            if (!OPEN) return;
            if (event.getAction() != GLFW.GLFW_PRESS) return;

            Minecraft mc = Minecraft.getInstance();

            if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                event.setCanceled(true);

                KeyMapping attack = mc.options.keyAttack;
                if (attack != null) attack.setDown(false);

                activateSelected();
                return;
            }

            if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                event.setCanceled(true);
                closeWheel();
            }
        }
    }

    private static void renderHudLayer(GuiGraphics graphics, DeltaTracker delta) {
        if (!OPEN) return;

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || !mc.player.level().dimension().equals(ModDimensions.CYBERSPACE_LEVEL)) {
            closeWheel();
            return;
        }

        if (mc.screen != null) {
            closeWheel();
            return;
        }

        rebuildEntries(mc);

        PlayerCyberwareData data = mc.player.getData(ModAttachments.CYBERWARE);
        if (data == null) {
            closeWheel();
            return;
        }

        int currentRam = data.getCyberdeckRam();

        var window = mc.getWindow();

        int w = window.getGuiScaledWidth();
        int h = window.getGuiScaledHeight();
        int cx = w / 2;
        int cy = h / 2;

        int screenWidth = window.getScreenWidth();
        int screenHeight = window.getScreenHeight();
        double guiScale = window.getGuiScale();

        float outerRadiusPixels = Math.min(screenWidth, screenHeight) * 0.37f;
        float outerRadius = (float) (outerRadiusPixels / guiScale);

        float innerRadius = outerRadius * 0.40f;
        float middleRadius = (innerRadius + outerRadius) * 0.5f;

        int entryCount = ENTRIES.size();

        if (entryCount <= 0) {
            SELECTED_INDEX = 0;
            renderEmptyWheel(graphics, mc, cx, cy, innerRadius, outerRadius);
            return;
        }

        SELECTED_INDEX = Mth.clamp(SELECTED_INDEX, 0, entryCount - 1);
        int selected = SELECTED_INDEX;

        final int baseArgb = 0x88000000;
        final int selectedArgb = 0xAA2E7BFF;

        for (int i = 0; i < entryCount; i++) {
            int argb = i == selected ? selectedArgb : baseArgb;
            drawDonutSegment(graphics, cx, cy, innerRadius, outerRadius, entryCount, i, 24, argb);
        }

        renderInstructions(graphics, mc, w, h, cx, cy, outerRadius);

        RenderSystem.enableDepthTest();

        for (int i = 0; i < ENTRIES.size(); i++) {
            Entry entry = ENTRIES.get(i);

            double angle = angleForIndex(entryCount, i) + ((Math.PI * 2.0) / entryCount) * 0.5;

            int centerX = (int) Math.round(cx + Math.cos(angle) * middleRadius);
            int centerY = (int) Math.round(cy + Math.sin(angle) * middleRadius);

            int iconX = centerX - 8;
            int iconY = centerY - 8;

            graphics.renderItem(entry.icon(), iconX, iconY);

            String rawName = entry.icon().getHoverName().getString();
            String name = rawName.length() > 22 ? rawName.substring(0, 21) + "…" : rawName;
            String ramText = entry.ramCost() + " RAM";

            int ramColor = currentRam >= entry.ramCost() ? RAM_AFFORDABLE_COLOR : RAM_UNAFFORDABLE_COLOR;

            var poseStack = graphics.pose();
            poseStack.pushPose();

            final float nameScale = 0.55f;

            poseStack.scale(nameScale, nameScale, 1.0f);

            int nameWidth = mc.font.width(name);
            int scaledCenterX = (int) (centerX / nameScale);
            int scaledNameX = scaledCenterX - nameWidth / 2;
            int scaledNameY = (int) ((iconY - mc.font.lineHeight - 2) / nameScale);

            graphics.drawString(mc.font, name, scaledNameX, scaledNameY, 0xFFFFFFFF, true);

            poseStack.popPose();

            poseStack.pushPose();

            final float ramScale = 0.50f;

            poseStack.scale(ramScale, ramScale, 1.0f);

            int ramWidth = mc.font.width(ramText);
            int scaledRamCenterX = (int) (centerX / ramScale);
            int scaledRamX = scaledRamCenterX - ramWidth / 2;
            int scaledRamY = (int) ((iconY + 18) / ramScale);

            graphics.drawString(mc.font, ramText, scaledRamX, scaledRamY, ramColor, true);

            poseStack.popPose();
        }

        renderSelectedEntry(graphics, mc, ENTRIES.get(selected), currentRam, cx, cy);
    }

    private static void renderInstructions(GuiGraphics graphics, Minecraft mc, int screenWidth, int screenHeight, int centerX, int centerY, float outerRadius) {
        final String line1 = "SCROLL = Select";
        final String line2 = "L-MB = Execute";
        final String line3 = "R-MB = Close";

        int textX = (int) (centerX + outerRadius + 12);
        int textY = centerY - mc.font.lineHeight;

        int maxX = screenWidth - 4;
        int maxY = screenHeight - 4;

        int line1Width = mc.font.width(line1);
        int line2Width = mc.font.width(line2);
        int line3Width = mc.font.width(line3);
        int maxLineWidth = Math.max(line1Width, Math.max(line2Width, line3Width));

        if (textX + maxLineWidth > maxX) {
            textX = maxX - maxLineWidth;
        }

        if (textX < 4) {
            textX = 4;
        }

        if (textY < 4) {
            textY = 4;
        }

        if (textY + mc.font.lineHeight * 3 + 4 > maxY) {
            textY = maxY - mc.font.lineHeight * 3 - 4;
        }

        graphics.drawString(mc.font, line1, textX, textY, 0xFFFFFFFF, true);
        graphics.drawString(mc.font, line2, textX, textY + mc.font.lineHeight + 2, 0xFFFFFFFF, true);
        graphics.drawString(mc.font, line3, textX, textY + (mc.font.lineHeight + 2) * 2, 0xFFFFFFFF, true);
    }

    private static void renderSelectedEntry(GuiGraphics graphics, Minecraft mc, Entry entry, int currentRam, int centerX, int centerY) {
        String name = entry.icon().getHoverName().getString();
        int nameWidth = mc.font.width(name);

        graphics.drawString(mc.font, name, centerX - nameWidth / 2, centerY - mc.font.lineHeight - 2, 0xFFFFFFFF, true);

        String ramText = "RAM: " + entry.ramCost();
        int ramWidth = mc.font.width(ramText);
        int ramColor = currentRam >= entry.ramCost() ? RAM_AFFORDABLE_COLOR : RAM_UNAFFORDABLE_COLOR;

        graphics.drawString(mc.font, ramText, centerX - ramWidth / 2, centerY + 2, ramColor, true);
    }

    private static void renderEmptyWheel(GuiGraphics graphics, Minecraft mc, int centerX, int centerY, float innerRadius, float outerRadius) {
        drawDonutSegment(graphics, centerX, centerY, innerRadius, outerRadius, 1, 0, 48, 0x88000000);

        String text = "NO MACROPROGRAMS INSTALLED";
        int textWidth = mc.font.width(text);

        graphics.drawString(mc.font, text, centerX - textWidth / 2, centerY - mc.font.lineHeight / 2, 0xFFFF5555, true);
    }

    private static void rebuildEntries(Minecraft mc) {
        ENTRIES.clear();

        if (mc.player == null) {
            SELECTED_INDEX = 0;
            return;
        }

        if (!mc.player.hasData(ModAttachments.CYBERWARE)) {
            SELECTED_INDEX = 0;
            return;
        }

        PlayerCyberwareData data = mc.player.getData(ModAttachments.CYBERWARE);

        if (data == null) {
            SELECTED_INDEX = 0;
            return;
        }

        for (int i = 0; i < PlayerCyberwareData.CYBERDECK_MACROPROGRAM_SLOT_COUNT; i++) {
            int slot = PlayerCyberwareData.CYBERDECK_MACROPROGRAM_SLOT_START + i;
            ItemStack stack = data.getCyberdeckStack(slot);

            if (stack.isEmpty()) continue;
            if (!stack.is(ModTags.Items.MACROPROGRAMS)) continue;
            if (!(stack.getItem() instanceof MacroprogramItem macroprogramItem)) continue;

            int ramCost = Math.max(0, macroprogramItem.getDefinition().ramCost());

            ENTRIES.add(new Entry(slot, stack.copy(), ramCost));
        }

        if (ENTRIES.isEmpty()) {
            SELECTED_INDEX = 0;
        } else {
            SELECTED_INDEX = Mth.clamp(SELECTED_INDEX, 0, ENTRIES.size() - 1);
        }
    }

    private static double angleForIndex(int entryCount, int index) {
        double step = Math.PI * 2.0 / entryCount;
        return -Math.PI / 2.0 + step * index;
    }

    private static void drawDonutSegment(GuiGraphics graphics, int centerX, int centerY, float innerRadius, float outerRadius, int entryCount, int index, int arcSteps, int argb) {
        float alpha = ((argb >>> 24) & 0xFF) / 255.0f;
        float red = ((argb >>> 16) & 0xFF) / 255.0f;
        float green = ((argb >>> 8) & 0xFF) / 255.0f;
        float blue = (argb & 0xFF) / 255.0f;

        double step = Math.PI * 2.0 / entryCount;
        double startAngle = -Math.PI / 2.0 + step * index;
        double endAngle = startAngle + step;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f pose = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= arcSteps; i++) {
            double progress = i / (double) arcSteps;
            double angle = startAngle + (endAngle - startAngle) * progress;

            float cosine = (float) Math.cos(angle);
            float sine = (float) Math.sin(angle);

            float outerX = centerX + cosine * outerRadius;
            float outerY = centerY + sine * outerRadius;

            float innerX = centerX + cosine * innerRadius;
            float innerY = centerY + sine * innerRadius;

            buffer.addVertex(pose, outerX, outerY, 0.0f).setColor(red, green, blue, alpha);
            buffer.addVertex(pose, innerX, innerY, 0.0f).setColor(red, green, blue, alpha);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
