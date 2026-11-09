package com.perigrine3.createcybernetics.screen.custom.toggle_wheel;

import com.perigrine3.createcybernetics.client.RadialMenuController;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.perigrine3.createcybernetics.CreateCybernetics;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.common.capabilities.ModAttachments;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import com.perigrine3.createcybernetics.item.cyberware.leg.PneumaticCalvesItem;
import com.perigrine3.createcybernetics.network.payload.CyberwareTogglePayloads;
import com.perigrine3.createcybernetics.util.ModTags;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class CyberwareToggleWheelScreen extends Screen {

    private static final ResourceLocation LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "cyberware_toggle_wheel");

    private static boolean OPEN = false;
    private static int SELECTED_INDEX = 0;

    private record SlotIndex(CyberwareSlot slot, int index) {}
    private record Entry(ItemStack icon, List<SlotIndex> targets) {}

    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static PlayerCyberwareData LAST_DATA = null;

    public CyberwareToggleWheelScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        RadialMenuController.prepareToOpen(RadialMenuController.Wheel.IMPLANTS);
        OPEN = true;
        SELECTED_INDEX = 0;

        PacketDistributor.sendToServer(new CyberwareTogglePayloads.RequestToggleStatesPayload());

        LAST_DATA = rebuildEntries(Minecraft.getInstance());

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
        LAST_DATA = null;
        ENTRIES.clear();
    }

    public static void scrollSelection(double scrollDelta) {
        if (!OPEN) return;
        if (scrollDelta == 0.0D) return;

        LAST_DATA = rebuildEntries(Minecraft.getInstance());

        if (ENTRIES.isEmpty()) {
            SELECTED_INDEX = 0;
            return;
        }

        int direction = scrollDelta > 0.0D ? -1 : 1;
        SELECTED_INDEX = Math.floorMod(SELECTED_INDEX + direction, ENTRIES.size());
    }

    public static void toggleSelected() {
        if (!OPEN) return;

        LAST_DATA = rebuildEntries(Minecraft.getInstance());

        if (ENTRIES.isEmpty()) return;

        SELECTED_INDEX = Mth.clamp(SELECTED_INDEX, 0, ENTRIES.size() - 1);

        Entry entry = ENTRIES.get(SELECTED_INDEX);
        if (entry.targets().isEmpty()) return;

        PlayerCyberwareData data = LAST_DATA;

        if (data == null) {
            SlotIndex target = entry.targets().get(0);
            PacketDistributor.sendToServer(new CyberwareTogglePayloads.ToggleCyberwarePayload(target.slot().name(), target.index()));
            return;
        }

        boolean currentlyEnabled = isEntryEnabled(data, entry);
        boolean desiredEnabled = !currentlyEnabled;

        for (SlotIndex target : entry.targets()) {
            boolean nowEnabled = data.isEnabled(target.slot(), target.index());

            if (nowEnabled != desiredEnabled) {
                PacketDistributor.sendToServer(new CyberwareTogglePayloads.ToggleCyberwarePayload(target.slot().name(), target.index()));
            }
        }
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
            event.registerAboveAll(LAYER_ID, CyberwareToggleWheelScreen::renderHudLayer);
        }
    }

    private static void renderHudLayer(GuiGraphics graphics, DeltaTracker delta) {
        if (!OPEN) return;

        Minecraft mc = Minecraft.getInstance();

        if (mc.screen != null) {
            closeWheel();
            return;
        }

        PlayerCyberwareData data = rebuildEntries(mc);
        LAST_DATA = data;

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

        final int nameColor = 0xFFFFFFFF;
        final int enabledColor = 0xFF55FF55;
        final int disabledColor = 0xFFFF5555;

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

            var poseStack = graphics.pose();
            poseStack.pushPose();

            final float nameScale = 0.55f;

            poseStack.scale(nameScale, nameScale, 1.0f);

            int nameWidth = mc.font.width(name);
            int scaledCenterX = (int) (centerX / nameScale);
            int scaledNameX = scaledCenterX - nameWidth / 2;
            int scaledNameY = (int) ((iconY - mc.font.lineHeight - 2) / nameScale);

            graphics.drawString(mc.font, name, scaledNameX, scaledNameY, nameColor, true);

            poseStack.popPose();

            boolean enabled = data != null && isEntryEnabled(data, entry);
            String stateText = enabled ? "ENABLED" : "DISABLED";

            int stateWidth = mc.font.width(stateText);
            int stateX = centerX - stateWidth / 2;
            int stateY = iconY + 18;

            graphics.drawString(mc.font, stateText, stateX, stateY, enabled ? enabledColor : disabledColor, true);
        }

        renderSelectedEntry(graphics, mc, data, ENTRIES.get(selected), cx, cy);
    }

    private static void renderInstructions(GuiGraphics graphics, Minecraft mc, int screenWidth, int screenHeight, int centerX, int centerY, float outerRadius) {
        final String line1 = "SCROLL = Select";
        final String line2 = "L-MB = Toggle";
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

    private static void renderSelectedEntry(GuiGraphics graphics, Minecraft mc, PlayerCyberwareData data, Entry entry, int centerX, int centerY) {
        String name = entry.icon().getHoverName().getString();
        boolean enabled = data != null && isEntryEnabled(data, entry);
        String state = enabled ? "ENABLED" : "DISABLED";

        int nameWidth = mc.font.width(name);
        int stateWidth = mc.font.width(state);

        graphics.drawString(mc.font, name, centerX - nameWidth / 2, centerY - mc.font.lineHeight - 2, 0xFFFFFFFF, true);
        graphics.drawString(mc.font, state, centerX - stateWidth / 2, centerY + 2, enabled ? 0xFF55FF55 : 0xFFFF5555, true);
    }

    private static void renderEmptyWheel(GuiGraphics graphics, Minecraft mc, int centerX, int centerY, float innerRadius, float outerRadius) {
        drawDonutSegment(graphics, centerX, centerY, innerRadius, outerRadius, 1, 0, 48, 0x88000000);

        String text = "NO TOGGLEABLE CYBERWARE";
        int textWidth = mc.font.width(text);

        graphics.drawString(mc.font, text, centerX - textWidth / 2, centerY - mc.font.lineHeight / 2, 0xFFFF5555, true);
    }

    private static boolean isEntryEnabled(PlayerCyberwareData data, Entry entry) {
        if (data == null) return false;

        for (SlotIndex target : entry.targets()) {
            if (data.isEnabled(target.slot(), target.index())) {
                return true;
            }
        }

        return false;
    }

    private static PlayerCyberwareData rebuildEntries(Minecraft mc) {
        ENTRIES.clear();

        if (mc.player == null) {
            SELECTED_INDEX = 0;
            return null;
        }

        if (!mc.player.hasData(ModAttachments.CYBERWARE)) {
            SELECTED_INDEX = 0;
            return null;
        }

        PlayerCyberwareData data = mc.player.getData(ModAttachments.CYBERWARE);

        if (data == null) {
            SELECTED_INDEX = 0;
            return null;
        }

        List<SlotIndex> pneumaticCalvesTargets = new ArrayList<>();
        ItemStack pneumaticCalvesIcon = ItemStack.EMPTY;

        for (var entry : data.getAll().entrySet()) {
            CyberwareSlot slot = entry.getKey();
            var installedCyberware = entry.getValue();

            if (installedCyberware == null) continue;

            for (int i = 0; i < installedCyberware.length; i++) {
                var installed = installedCyberware[i];

                if (installed == null) continue;

                ItemStack stack = installed.getItem();

                if (stack == null || stack.isEmpty()) continue;
                if (!stack.is(ModTags.Items.TOGGLEABLE_CYBERWARE)) continue;

                if (stack.getItem() instanceof PneumaticCalvesItem) {
                    pneumaticCalvesTargets.add(new SlotIndex(slot, i));

                    if (pneumaticCalvesIcon.isEmpty()) {
                        pneumaticCalvesIcon = stack.copy();
                    }

                    continue;
                }

                ENTRIES.add(new Entry(stack.copy(), List.of(new SlotIndex(slot, i))));
            }
        }

        if (pneumaticCalvesTargets.size() >= 2) {
            ENTRIES.add(new Entry(pneumaticCalvesIcon.isEmpty() ? ItemStack.EMPTY : pneumaticCalvesIcon, pneumaticCalvesTargets));
        }

        if (ENTRIES.isEmpty()) {
            SELECTED_INDEX = 0;
        } else {
            SELECTED_INDEX = Mth.clamp(SELECTED_INDEX, 0, ENTRIES.size() - 1);
        }

        return data;
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
