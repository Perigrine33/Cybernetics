package com.perigrine3.createcybernetics.screen.custom.arm_cannon;

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
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.item.cyberware.arm.ArmCannonItem;
import com.perigrine3.createcybernetics.network.payload.ArmCannonWheelPayloads;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class ArmCannonWheelScreen extends Screen {

    private static final ResourceLocation LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "radial_wheel_overlay");

    private static boolean OPEN = false;

    private static int SEGMENTS = 4;
    private static int SELECTED_INDEX = 0;

    private static final int BASE_ARGB = 0x88000000;
    private static final int HOVER_ARGB = 0xAA2E7BFF;

    // Reused temp inventory for decoding the installed stack's CUSTOM_DATA each frame
    private static final SimpleContainer TMP_INV = new SimpleContainer(ArmCannonItem.SLOT_COUNT);

    public ArmCannonWheelScreen() {
        super(Component.empty());
    }

    public static boolean isOpen() {
        return OPEN;
    }

    public static void open(int segments) {
        RadialMenuController.prepareToOpen(RadialMenuController.Wheel.ARM_CANNON);
        SEGMENTS = Math.max(1, segments);
        OPEN = true;
        SELECTED_INDEX = 0;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null && player.hasData(ModAttachments.CYBERWARE)) {
            PlayerCyberwareData data = player.getData(ModAttachments.CYBERWARE);

            setPreselectedIndex(data.getArmCannonSelected());
        }

        KeyMapping attack = mc.options.keyAttack;
        if (attack.isDown()) {
            attack.setDown(false);
        }
    }

    public static void close() {
        OPEN = false;
    }

    public static int getSelectedIndex() {
        if (!OPEN) return 0;
        return Mth.clamp(SELECTED_INDEX, 0, Math.max(0, SEGMENTS - 1));
    }

    public static void scrollSelection(double scrollDelta) {
        if (!OPEN) return;
        if (scrollDelta == 0.0D) return;

        int segments = Math.max(1, SEGMENTS);
        int direction = scrollDelta > 0.0D ? -1 : 1;

        SELECTED_INDEX = Math.floorMod(SELECTED_INDEX + direction, segments);
    }

    public static void setPreselectedIndex(int index) {
        int segments = Math.max(1, SEGMENTS);
        SELECTED_INDEX = Mth.clamp(index, 0, segments - 1);
    }

    @Override
    protected void init() {
        if (this.minecraft != null && this.minecraft.screen == this) {
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    }

    public boolean shouldBlurBackground() {
        return false;
    }

    @Override
    public void onClose() {
        close();
        super.onClose();
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ClientModBus {
        @SubscribeEvent
        public static void registerGuiLayers(RegisterGuiLayersEvent event) {
            event.registerAboveAll(LAYER_ID, ArmCannonWheelScreen::renderHudLayer);
        }
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static final class ClientGameBus {

        @SubscribeEvent
        public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
            if (!OPEN) return;

            event.setCanceled(true);
            scrollSelection(event.getScrollDeltaY());
        }

        @SubscribeEvent
        public static void onMouseButton(InputEvent.MouseButton.Pre event) {
            if (!OPEN) return;
            if (event.getAction() != GLFW.GLFW_PRESS) return;

            Minecraft mc = Minecraft.getInstance();

            if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                event.setCanceled(true);

                KeyMapping attack = mc.options.keyAttack;
                attack.setDown(false);

                int index = getSelectedIndex();
                PacketDistributor.sendToServer(new ArmCannonWheelPayloads.SelectArmCannonAmmoSlotPayload(index));

                close();
                return;
            }

            if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                event.setCanceled(true);
                close();
            }
        }

        private ClientGameBus() {
        }
    }

    private static void renderHudLayer(GuiGraphics graphics, DeltaTracker delta) {
        if (!OPEN) return;

        Minecraft mc = Minecraft.getInstance();

        if (mc.screen != null) {
            close();
            return;
        }

        var window = mc.getWindow();

        int w = window.getGuiScaledWidth();
        int h = window.getGuiScaledHeight();
        int cx = w / 2;
        int cy = h / 2;

        int sw = window.getScreenWidth();
        int sh = window.getScreenHeight();
        double guiScale = window.getGuiScale();

        float outerR_px = Math.min(sw, sh) * 0.37f;
        float outerR = (float) (outerR_px / guiScale);
        float innerR = outerR * 0.40f;
        float midR = (innerR + outerR) * 0.5f;

        int n = Math.max(1, SEGMENTS);

        int loadedIndex = -1;
        ItemStack[] stacks = new ItemStack[n];
        for (int i = 0; i < n; i++) stacks[i] = ItemStack.EMPTY;

        if (mc.player != null && mc.player.hasData(ModAttachments.CYBERWARE)) {
            PlayerCyberwareData data = mc.player.getData(ModAttachments.CYBERWARE);
            if (data != null) {
                loadedIndex = data.getArmCannonSelected();

                ItemStack installedCannon = findInstalledArmCannonStack(data);
                if (!installedCannon.isEmpty()) {
                    ArmCannonItem.loadFromInstalledStack(installedCannon, mc.player.level().registryAccess(), TMP_INV);

                    for (int i = 0; i < n && i < ArmCannonItem.SLOT_COUNT; i++) {
                        ItemStack st = TMP_INV.getItem(i);
                        stacks[i] = (st == null) ? ItemStack.EMPTY : st;
                    }
                }
            }
        }

        SELECTED_INDEX = Mth.clamp(SELECTED_INDEX, 0, n - 1);
        int selected = SELECTED_INDEX;

        for (int i = 0; i < n; i++) {
            int argb = i == selected ? HOVER_ARGB : BASE_ARGB;
            drawDonutSegment(graphics, cx, cy, innerR, outerR, n, i, 24, argb);
        }

        RenderSystem.enableDepthTest();

        final int nameColor = 0xFFFFFFFF;
        final int loadedColor = 0xFFFFFFFF;

        for (int i = 0; i < n; i++) {
            ItemStack st = stacks[i];
            if (st == null) st = ItemStack.EMPTY;

            double ang = angleForIndex(n, i) + ((Math.PI * 2.0) / n) * 0.5;
            int centerX = (int) Math.round(cx + Math.cos(ang) * midR);
            int centerY = (int) Math.round(cy + Math.sin(ang) * midR);

            if (!st.isEmpty()) {
                int ix = centerX - 8;
                int iy = centerY - 8;
                graphics.renderItem(st, ix, iy);

                String rawName = st.getHoverName().getString();
                String name = (rawName.length() > 22) ? (rawName.substring(0, 21) + "…") : rawName;

                var poseStack = graphics.pose();
                poseStack.pushPose();

                final float nameScale = 0.55f;
                poseStack.scale(nameScale, nameScale, 1.0f);

                int nameW = mc.font.width(name);
                int scaledCenterX = (int) (centerX / nameScale);
                int scaledNameX = scaledCenterX - (nameW / 2);
                int scaledNameY = (int) ((iy - (mc.font.lineHeight + 2)) / nameScale);

                graphics.drawString(mc.font, name, scaledNameX, scaledNameY, nameColor, true);

                poseStack.popPose();

                if (i == loadedIndex) {
                    String loaded = "LOADED";
                    int lw = mc.font.width(loaded);
                    int lx = centerX - (lw / 2);
                    int ly = iy + 16 + 2;
                    graphics.drawString(mc.font, loaded, lx, ly, loadedColor, true);
                }
            }
        }

        renderInstructions(graphics, mc, w, h, cx, cy, outerR);
    }

    private static void renderInstructions(GuiGraphics graphics, Minecraft mc, int screenWidth, int screenHeight, int centerX, int centerY, float outerRadius) {
        final String line1 = "SCROLL = Select";
        final String line2 = "L-MB = Load";
        final String line3 = "R-MB = Close";

        int textX = (int) (centerX + outerRadius + 12);
        int textY = centerY - mc.font.lineHeight;

        int maximumX = screenWidth - 4;
        int maximumY = screenHeight - 4;

        int line1Width = mc.font.width(line1);
        int line2Width = mc.font.width(line2);
        int line3Width = mc.font.width(line3);
        int maximumLineWidth = Math.max(line1Width, Math.max(line2Width, line3Width));

        if (textX + maximumLineWidth > maximumX) {
            textX = maximumX - maximumLineWidth;
        }

        if (textX < 4) {
            textX = 4;
        }

        if (textY < 4) {
            textY = 4;
        }

        if (textY + mc.font.lineHeight * 3 + 4 > maximumY) {
            textY = maximumY - mc.font.lineHeight * 3 - 4;
        }

        graphics.drawString(mc.font, line1, textX, textY, 0xFFFFFFFF, true);
        graphics.drawString(mc.font, line2, textX, textY + mc.font.lineHeight + 2, 0xFFFFFFFF, true);
        graphics.drawString(mc.font, line3, textX, textY + (mc.font.lineHeight + 2) * 2, 0xFFFFFFFF, true);
    }

    private static ItemStack findInstalledArmCannonStack(PlayerCyberwareData data) {
        for (var entry : data.getAll().entrySet()) {
            var arr = entry.getValue();
            if (arr == null) continue;

            for (int i = 0; i < arr.length; i++) {
                var inst = arr[i];
                if (inst == null) continue;

                ItemStack st = inst.getItem();
                if (st == null || st.isEmpty()) continue;

                if (st.getItem() == ModItems.ARMUPGRADES_ARMCANNON.get()) {
                    return st;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static double angleForIndex(int n, int i) {
        double step = (Math.PI * 2.0) / n;
        return -Math.PI / 2.0 + step * i;
    }

    private static void drawDonutSegment(
            GuiGraphics graphics,
            int cx, int cy,
            float innerR, float outerR,
            int n, int idx,
            int arcSteps,
            int argb
    ) {
        float a = ((argb >>> 24) & 0xFF) / 255.0f;
        float r = ((argb >>> 16) & 0xFF) / 255.0f;
        float g = ((argb >>> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;

        double step = (Math.PI * 2.0) / (double) n;
        double a0 = -Math.PI / 2.0 + step * (double) idx;
        double a1 = a0 + step;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f pose = graphics.pose().last().pose();
        BufferBuilder bb = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= arcSteps; i++) {
            double t = i / (double) arcSteps;
            double ang = a0 + (a1 - a0) * t;

            float cos = (float) Math.cos(ang);
            float sin = (float) Math.sin(ang);

            float xo = cx + cos * outerR;
            float yo = cy + sin * outerR;

            float xi = cx + cos * innerR;
            float yi = cy + sin * innerR;

            bb.addVertex(pose, xo, yo, 0.0f).setColor(r, g, b, a);
            bb.addVertex(pose, xi, yi, 0.0f).setColor(r, g, b, a);
        }

        BufferUploader.drawWithShader(bb.buildOrThrow());

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
