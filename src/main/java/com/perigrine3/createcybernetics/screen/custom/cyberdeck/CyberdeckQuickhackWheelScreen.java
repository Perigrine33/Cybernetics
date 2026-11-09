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
import com.perigrine3.createcybernetics.common.quickhack.QuickhackProgramData;
import com.perigrine3.createcybernetics.item.ModItems;
import com.perigrine3.createcybernetics.network.payload.CastCyberdeckQuickhackPayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CyberdeckQuickhackWheelScreen extends Screen {

    private static final ResourceLocation LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(CreateCybernetics.MODID, "cyberdeck_quickhack_wheel");

    private static final double MAX_TARGET_RANGE = 25.0D;
    private static final int CAST_COOLDOWN_TICKS = 200;

    private static final int RAM_AFFORDABLE_COLOR = 0xFF7FDBFF;
    private static final int RAM_UNAFFORDABLE_COLOR = 0xFFFF5555;

    private static boolean OPEN = false;
    private static int SELECTED_INDEX = 0;

    private record Entry(ItemStack icon, int cyberdeckSlot, int ramCost) {}
    private static final List<Entry> ENTRIES = new ArrayList<>();

    public CyberdeckQuickhackWheelScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        open();

        if (this.minecraft != null && this.minecraft.screen == this) {
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    }

    public boolean shouldBlurBackground() {
        return false;
    }

    @Override
    public void onClose() {
        OPEN = false;
        super.onClose();
    }

    public static boolean isOpen() {
        return OPEN;
    }

    public static void close() {
        OPEN = false;
    }

    public static void open() {
        RadialMenuController.prepareToOpen(RadialMenuController.Wheel.QUICKHACKS);
        OPEN = true;
        SELECTED_INDEX = 0;
    }

    private static String quickhackDisplayName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";

        if (QuickhackProgramData.hasProgram(stack)) {
            String programName = QuickhackProgramData.getName(stack);
            return programName.isBlank() ? "Quickhack.exe" : programName + ".exe";
        }

        return Component.translatable(stack.getDescriptionId() + ".desc").getString();
    }

    private static Entity getValidTarget(Minecraft mc) {
        if (mc.player == null || mc.level == null) {
            return null;
        }

        Entity viewer = mc.player;
        Vec3 eyePos = viewer.getEyePosition();
        Vec3 look = viewer.getViewVector(1.0F);
        Vec3 end = eyePos.add(look.scale(MAX_TARGET_RANGE));

        AABB searchBox = viewer.getBoundingBox()
                .expandTowards(look.scale(MAX_TARGET_RANGE))
                .inflate(1.0D);

        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                viewer,
                eyePos,
                end,
                searchBox,
                entity -> !entity.isSpectator() && entity.isPickable() && entity.isAlive(),
                MAX_TARGET_RANGE * MAX_TARGET_RANGE
        );

        if (hit == null) {
            return null;
        }

        Entity target = hit.getEntity();
        if (target == null || !target.isAlive()) {
            return null;
        }

        return target;
    }

    private static void castSelected() {
        if (!OPEN) return;
        if (ENTRIES.isEmpty()) return;

        int idx = Mth.clamp(SELECTED_INDEX, 0, ENTRIES.size() - 1);
        Entry e = ENTRIES.get(idx);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Entity target = getValidTarget(mc);
        if (target == null) {
            return;
        }

        PacketDistributor.sendToServer(new CastCyberdeckQuickhackPayload(e.cyberdeckSlot(), target.getId()));
        close();
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ClientModBus {
        @SubscribeEvent
        public static void registerGuiLayers(RegisterGuiLayersEvent event) {
            event.registerAboveAll(LAYER_ID, CyberdeckQuickhackWheelScreen::renderHudLayer);
        }
    }

    @EventBusSubscriber(modid = CreateCybernetics.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
    public static final class ClientGameBus {

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            if (!OPEN) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.screen != null) {
                close();
                return;
            }

            KeyMapping attack = mc.options.keyAttack;
            if (attack != null && attack.isDown()) {
                attack.setDown(false);
            }
        }

        @SubscribeEvent
        public static void onMouseButton(InputEvent.MouseButton.Pre event) {
            if (!OPEN) return;
            if (event.getAction() != GLFW.GLFW_PRESS) return;

            Minecraft mc = Minecraft.getInstance();

            if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                event.setCanceled(true);

                KeyMapping attack = mc.options.keyAttack;
                if (attack != null) attack.setDown(false);

                castSelected();
                return;
            }

            if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                event.setCanceled(true);
                close();
            }
        }

        @SubscribeEvent
        public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
            if (!OPEN) return;

            if (ENTRIES.isEmpty()) {
                event.setCanceled(true);
                return;
            }

            double delta = event.getScrollDeltaY();
            if (delta == 0.0D) return;

            event.setCanceled(true);

            int size = ENTRIES.size();
            if (delta > 0.0D) {
                SELECTED_INDEX--;
            } else {
                SELECTED_INDEX++;
            }

            if (SELECTED_INDEX < 0) {
                SELECTED_INDEX = size - 1;
            } else if (SELECTED_INDEX >= size) {
                SELECTED_INDEX = 0;
            }
        }
    }

    private static void renderHudLayer(GuiGraphics graphics, DeltaTracker delta) {
        if (!OPEN) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) {
            close();
            return;
        }

        PlayerCyberwareData data = rebuildEntries(mc);

        if (data == null) {
            close();
            return;
        }

        int currentRam = data.getCyberdeckRam();

        var window = mc.getWindow();
        int w = window.getGuiScaledWidth();
        int h = window.getGuiScaledHeight();
        int cx = w / 2;
        int cy = h / 2;

        int sw = window.getScreenWidth();
        int sh = window.getScreenHeight();
        double guiScale = window.getGuiScale();

        float outerR_px = Math.min(sw, sh) * 0.30f;
        float outerR = (float) (outerR_px / guiScale);

        float innerR = outerR * 0.48f;
        float midR = (innerR + outerR) * 0.5f;

        int n = Math.max(1, ENTRIES.size());
        SELECTED_INDEX = Mth.clamp(SELECTED_INDEX, 0, Math.max(0, ENTRIES.size() - 1));

        final int baseArgb = 0x88000000;
        final int hoverArgb = 0xAA00FFFF;

        for (int i = 0; i < n; i++) {
            int argb = (i == SELECTED_INDEX) ? hoverArgb : baseArgb;
            drawDonutSegment(graphics, cx, cy, innerR, outerR, n, i, 24, argb);
        }

        final String line1 = "Scroll = Select";
        final String line2 = "LMB = Cast";
        final String line3 = "RMB = Close";

        int textX = (int) (cx + outerR + 12);
        int textY = cy - mc.font.lineHeight;

        graphics.drawString(mc.font, line1, textX, textY, 0xFFFFFFFF, true);
        graphics.drawString(mc.font, line2, textX, textY + mc.font.lineHeight + 2, 0xFFFFFFFF, true);
        graphics.drawString(mc.font, line3, textX, textY + (mc.font.lineHeight + 2) * 2, 0xFFFFFFFF, true);

        RenderSystem.enableDepthTest();

        for (int i = 0; i < ENTRIES.size(); i++) {
            Entry e = ENTRIES.get(i);

            double ang = angleForIndex(n, i) + ((Math.PI * 2.0) / n) * 0.5;

            int centerX = (int) Math.round(cx + Math.cos(ang) * midR);
            int centerY = (int) Math.round(cy + Math.sin(ang) * midR);

            int ix = centerX - 8;
            int iy = centerY - 8;
            graphics.renderItem(e.icon(), ix, iy);

            String rawName = quickhackDisplayName(e.icon());
            String name = (rawName.length() > 22) ? (rawName.substring(0, 21) + "…") : rawName;
            String ramText = e.ramCost() + " RAM";
            int ramColor = currentRam >= e.ramCost() ? RAM_AFFORDABLE_COLOR : RAM_UNAFFORDABLE_COLOR;

            var poseStack = graphics.pose();
            poseStack.pushPose();

            final float nameScale = 0.55f;
            poseStack.scale(nameScale, nameScale, 1.0f);

            int nameW = mc.font.width(name);
            int scaledCenterX = (int) (centerX / nameScale);
            int scaledNameX = scaledCenterX - (nameW / 2);
            int scaledNameY = (int) ((iy - (mc.font.lineHeight + 2)) / nameScale);

            graphics.drawString(mc.font, name, scaledNameX, scaledNameY, 0xFFFFFFFF, true);

            poseStack.popPose();

            poseStack.pushPose();

            final float ramScale = 0.50f;
            poseStack.scale(ramScale, ramScale, 1.0f);

            int ramWidth = mc.font.width(ramText);
            int scaledRamCenterX = (int) (centerX / ramScale);
            int scaledRamX = scaledRamCenterX - ramWidth / 2;
            int scaledRamY = (int) ((iy + 18) / ramScale);

            graphics.drawString(mc.font, ramText, scaledRamX, scaledRamY, ramColor, true);

            poseStack.popPose();
        }

        drawCooldownAboveWheel(graphics, mc, cx, cy);
        drawTargetInfoAboveCrosshair(graphics, mc, cx, cy);
        drawSelectedQuickhackInfo(graphics, mc, currentRam, cx, cy);
    }

    private static void drawCooldownAboveWheel(GuiGraphics graphics, Minecraft mc, int cx, int cy) {
        if (mc.player == null) return;

        Item cyberdeckItem = ModItems.BRAINUPGRADES_CYBERDECK.get();
        float cooldownPercent = mc.player.getCooldowns().getCooldownPercent(cyberdeckItem, 0.0F);
        if (cooldownPercent <= 0.0F) return;

        int remainingTicks = Mth.ceil(cooldownPercent * CAST_COOLDOWN_TICKS);
        float remainingSeconds = remainingTicks / 20.0F;

        String text = String.format("Cooldown: %.1fs", remainingSeconds);
        int width = mc.font.width(text);

        graphics.drawString(mc.font, text, cx - (width / 2), cy - 95, 0xFFFF5555, true);
    }

    private static void drawTargetInfoAboveCrosshair(GuiGraphics graphics, Minecraft mc, int cx, int cy) {
        Entity target = getValidTarget(mc);
        String targetText = target != null ? target.getDisplayName().getString() : "No target";

        int textW = mc.font.width(targetText);
        int textX = cx - (textW / 2);
        int textY = cy - 28;

        graphics.drawString(mc.font, targetText, textX, textY, 0xFFFFFFFF, true);
    }

    private static void drawSelectedQuickhackInfo(GuiGraphics graphics, Minecraft mc, int currentRam, int cx, int cy) {
        if (ENTRIES.isEmpty()) {
            String empty = "No quickhacks loaded";
            int w = mc.font.width(empty);
            graphics.drawString(mc.font, empty, cx - (w / 2), cy + 12, 0xFFFF5555, true);
            return;
        }

        Entry selected = ENTRIES.get(Mth.clamp(SELECTED_INDEX, 0, ENTRIES.size() - 1));

        String name = quickhackDisplayName(selected.icon());
        int nameWidth = mc.font.width(name);

        graphics.drawString(mc.font, name, cx - (nameWidth / 2), cy + 7, 0xFF00FFFF, true);

        String ramText = "RAM: " + selected.ramCost();
        int ramWidth = mc.font.width(ramText);
        int ramColor = currentRam >= selected.ramCost() ? RAM_AFFORDABLE_COLOR : RAM_UNAFFORDABLE_COLOR;

        graphics.drawString(mc.font, ramText, cx - (ramWidth / 2), cy + 19, ramColor, true);
    }

    private static PlayerCyberwareData rebuildEntries(Minecraft mc) {
        ENTRIES.clear();

        if (mc.player == null) return null;
        if (!mc.player.hasData(ModAttachments.CYBERWARE)) return null;

        PlayerCyberwareData data = mc.player.getData(ModAttachments.CYBERWARE);
        if (data == null) return null;

        for (int i = 0; i < PlayerCyberwareData.CYBERDECK_QUICKHACK_SLOT_COUNT; i++) {
            ItemStack stack = data.getCyberdeckStack(i);
            if (stack == null || stack.isEmpty()) continue;

            int ramCost = QuickhackProgramData.getRamCost(stack);

            ENTRIES.add(new Entry(stack.copy(), i, ramCost));
        }

        if (SELECTED_INDEX >= ENTRIES.size()) {
            SELECTED_INDEX = Math.max(0, ENTRIES.size() - 1);
        }

        return data;
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
