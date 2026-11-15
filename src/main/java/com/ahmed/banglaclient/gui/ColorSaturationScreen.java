package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.module.visual.ColorSaturationModule;
import com.ahmed.banglaclient.module.visual.ColorSaturationModule.ColorPreset;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

/**
 * Exact Aurora-inspired Frosted-Glass Interface from color-saturation-main:
 * - Central Frosted-Glass Panel (Visuals Sliders / 16 Presets Grid)
 * - Flanking Left/Right Preview Cards ([Visuals] and [Presets])
 * - Bottom Floating Glass Dock: [Visuals] [Presets] [Reset] [Back]
 * - 100% Small-Caps typography & smooth cubic interpolation
 */
public final class ColorSaturationScreen extends Screen {
    private final Screen parent;
    private final ColorSaturationModule module;
    private final Animation openAnim = new Animation(0.0f, 170L, Easing.EASE_OUT_CUBIC);
    private boolean closing = false;
    private boolean viewingPresets = false;
    private int presetPage = 0;
    private int draggingSliderIndex = -1;

    private int panelX, panelY, panelW, panelH, dockY;

    public ColorSaturationScreen(Screen parent, ColorSaturationModule module) {
        super(Text.literal(GlassRenderer.toSmallCaps("Color Saturation")));
        this.parent = parent;
        this.module = module;
    }

    @Override
    protected void init() {
        this.panelW = Math.min(266, Math.max(216, (int) (this.width * 0.58f)));
        this.panelH = Math.min(168, Math.max(136, this.height - 64));
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = Math.max(20, (this.height - this.panelH - 30) / 2);
        this.dockY = this.panelY + this.panelH + 6;
        this.closing = false;
        this.openAnim.snapTo(0.0f);
        this.openAnim.animateTo(1.0f);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float anim = openAnim.getValue();
        if (closing && anim <= 0.02f) {
            BanglaClient.getInstance().getConfigManager().saveModulesConfig();
            if (this.client != null) this.client.setScreen(parent);
            return;
        }

        if (draggingSliderIndex >= 0) {
            updateDraggedSlider(mouseX);
        }

        BlurRenderer.renderScreenBackdrop(context, this.width, this.height, anim);

        Matrix3x2fStack matrices = context.getMatrices();
        float scale = 0.94f + 0.06f * anim;
        matrices.pushMatrix();
        matrices.translate(this.width * 0.5f, this.height * 0.5f);
        matrices.scale(scale, scale);
        matrices.translate(-this.width * 0.5f, -this.height * 0.5f);

        try {
            // 1. Top Centered Title ("ᴄᴏʟᴏʀ ꜱᴀᴛᴜʀᴀᴛɪᴏɴ")
            String topTitle = GlassRenderer.toSmallCaps("Color Saturation");
            context.drawTextWithShadow(textRenderer, topTitle, (this.width - textRenderer.getWidth(topTitle)) / 2, panelY - 13, 0xFFF4F4F5);

            // 2. Main Central Frosted-Glass Container
            GlassRenderer.drawGlassPanel(context, panelX, panelY, panelW, panelH, true, false, true);

            int centerX = panelX + panelW / 2;
            String headerLabel = GlassRenderer.toSmallCaps(viewingPresets ? "Presets" : "Visuals");
            context.drawTextWithShadow(textRenderer, headerLabel, centerX - textRenderer.getWidth(headerLabel) / 2, panelY + 8, 0xFFF4F4F5);
            context.fill(centerX - 10, panelY + 19, centerX + 10, panelY + 20, 0x88FFFFFF);

            // 3. Side Flanking Cards ([Visuals] on left, [Presets] on right)
            if (this.width - this.panelW >= 130) {
                int sideW = 48, sideH = 54;
                int sideY = panelY + (panelH - sideH) / 2;

                int leftX = panelX - sideW - 10;
                boolean lHov = MathUtils.isHovered(mouseX, mouseY, leftX, sideY, sideW, sideH);
                GlassRenderer.drawGlassPanel(context, leftX, sideY, sideW, sideH, true, lHov, !viewingPresets);
                GlassRenderer.drawVectorIcon(context, leftX + (sideW - 10) / 2, sideY + 12, 10, "VISUAL", 0xFFFFFFFF);
                String vLbl = GlassRenderer.toSmallCaps("Visuals");
                context.drawTextWithShadow(textRenderer, vLbl, leftX + (sideW - textRenderer.getWidth(vLbl)) / 2, sideY + 30, 0xFFA1A1AA);

                int rightX = panelX + panelW + 10;
                boolean rHov = MathUtils.isHovered(mouseX, mouseY, rightX, sideY, sideW, sideH);
                GlassRenderer.drawGlassPanel(context, rightX, sideY, sideW, sideH, true, rHov, viewingPresets);
                GlassRenderer.drawVectorIcon(context, rightX + (sideW - 10) / 2, sideY + 12, 10, "DASHBOARD", 0xFFFFFFFF);
                String pLbl = GlassRenderer.toSmallCaps("Presets");
                context.drawTextWithShadow(textRenderer, pLbl, rightX + (sideW - textRenderer.getWidth(pLbl)) / 2, sideY + 30, 0xFFA1A1AA);
            }

            int innerX = panelX + 8;
            int innerW = panelW - 16;

            if (!viewingPresets) {
                renderVisualsContent(context, innerX, panelY + 24, innerW, mouseX, mouseY);
            } else {
                renderPresetsContent(context, innerX, panelY + 24, innerW, mouseX, mouseY);
            }

            // 4. Bottom Floating Glass Dock: [Visuals] [Presets] [Reset] [Back]
            int dockW = 50, dockH = 22, gap = 5;
            int totalDockW = dockW * 4 + gap * 3;
            int dockStartX = (this.width - totalDockW) / 2;
            String[] dockLabels = {"Visuals", "Presets", "Reset", "← Back"};
            for (int i = 0; i < 4; i++) {
                int dx = dockStartX + i * (dockW + gap);
                boolean active = (i == 0 && !viewingPresets) || (i == 1 && viewingPresets);
                boolean hov = MathUtils.isHovered(mouseX, mouseY, dx, dockY, dockW, dockH);
                GlassRenderer.drawGlassPanel(context, dx, dockY, dockW, dockH, true, hov, active);
                String dl = GlassRenderer.toSmallCaps(dockLabels[i]);
                context.drawTextWithShadow(textRenderer, dl, dx + (dockW - textRenderer.getWidth(dl)) / 2, dockY + 7, active || hov ? 0xFFFFFFFF : 0xFFA1A1AA);
            }
        } finally {
            matrices.popMatrix();
        }
    }

    private void renderVisualsContent(DrawContext context, int x, int y, int w, int mouseX, int mouseY) {
        // Active Preset Bar
        boolean pHov = MathUtils.isHovered(mouseX, mouseY, x, y, w, 15);
        GlassRenderer.drawGlassPanel(context, x, y, w, 15, false, pHov, false);
        String pBar = GlassRenderer.toSmallCaps("Preset: " + module.getActivePresetName() + " (Click to Switch)");
        context.drawTextWithShadow(textRenderer, pBar, x + (w - textRenderer.getWidth(pBar)) / 2, y + 4, 0xFFF4F4F5);
        y += 19;

        int rowH = 20, gap = 3;

        // 1. Enabled Toggle Row
        boolean enHov = MathUtils.isHovered(mouseX, mouseY, x, y, w, rowH);
        GlassRenderer.drawGlassPanel(context, x, y, w, rowH, false, enHov, module.isEnabled());
        GlassRenderer.drawSmallCapsText(context, textRenderer, "Enabled", x + 8, y + 6, 0xFFF4F4F5);
        int swX = x + w - 28, swY = y + 5;
        context.fill(swX + 1, swY, swX + 21, swY + 10, module.isEnabled() ? 0x48FFFFFF : 0xFF08090C);
        GlassRenderer.drawOutlineBorder(context, swX, swY, 22, 10, module.isEnabled() ? 0xEEFFFFFF : 0x38FFFFFF);
        int kx = module.isEnabled() ? swX + 13 : swX + 2;
        context.fill(kx, swY + 2, kx + 6, swY + 8, module.isEnabled() ? 0xFFFFFFFF : 0xFF788296);
        y += rowH + gap;

        // 2..5 Sliders: Saturation, Contrast, Brightness, Hue Shift (All in Small-Caps!)
        String[] titles = {"Saturation", "Contrast", "Brightness", "Hue Shift"};
        float[] vals = {module.getSaturation(), module.getContrast(), module.getBrightness(), module.getHue()};
        float[] mins = {-1.0f, 0.0f, 0.0f, -180.0f};
        float[] maxs = {5.0f, 2.0f, 2.0f, 180.0f};
        String[] sufs = {"x", "x", "x", "°"};

        for (int i = 0; i < 4; i++) {
            boolean rHov = MathUtils.isHovered(mouseX, mouseY, x, y, w, rowH);
            GlassRenderer.drawGlassPanel(context, x, y, w, rowH, false, rHov, draggingSliderIndex == i);
            GlassRenderer.drawSmallCapsText(context, textRenderer, titles[i], x + 8, y + 6, 0xFFF4F4F5);

            int trackX = x + 88;
            int trackW = w - 134;
            int trackY = y + 9;
            context.fill(trackX, trackY, trackX + trackW, trackY + 2, 0x44FFFFFF);

            float norm = MathUtils.clamp((vals[i] - mins[i]) / (maxs[i] - mins[i]), 0.0f, 1.0f);
            int fillW = Math.round(norm * trackW);
            context.fill(trackX, trackY, trackX + fillW, trackY + 2, 0xFFFFFFFF);
            context.fill(trackX + fillW - 2, trackY - 2, trackX + fillW + 2, trackY + 4, 0xFFFFFFFF);

            String vStr = (i == 3) ? String.format(Locale.ROOT, "%.0f%s", vals[i], sufs[i]) : String.format(Locale.ROOT, "%.2f%s", vals[i], sufs[i]);
            context.drawTextWithShadow(textRenderer, vStr, x + w - textRenderer.getWidth(vStr) - 6, y + 6, 0xFFA1A1AA);
            y += rowH + gap;
        }
    }

    private void renderPresetsContent(DrawContext context, int x, int y, int w, int mouseX, int mouseY) {
        ColorPreset[] presets = ColorPreset.values();
        int cols = 3, rows = 4, perPage = 12;
        int gap = 4;
        int cardW = (w - gap * (cols - 1)) / cols;
        int cardH = 22;

        int startIdx = presetPage * perPage;
        for (int i = 0; i < perPage && (startIdx + i) < presets.length; i++) {
            ColorPreset p = presets[startIdx + i];
            int c = i % cols, r = i / cols;
            int bx = x + c * (cardW + gap);
            int by = y + 4 + r * (cardH + gap);
            boolean sel = p.toString().equalsIgnoreCase(module.getActivePresetName());
            boolean hov = MathUtils.isHovered(mouseX, mouseY, bx, by, cardW, cardH);

            GlassRenderer.drawGlassPanel(context, bx, by, cardW, cardH, true, hov, sel);
            String lbl = textRenderer.trimToWidth(GlassRenderer.toSmallCaps(p.toString()), cardW - 6);
            context.drawTextWithShadow(textRenderer, lbl, bx + (cardW - textRenderer.getWidth(lbl)) / 2, by + 7, sel || hov ? 0xFFFFFFFF : 0xFFA1A1AA);
        }

        int botY = panelY + panelH - 18;
        GlassRenderer.drawPillBadge(context, x + w / 2 - 26, botY, 24, 14, 0xFFFFFFFF, false);
        context.drawTextWithShadow(textRenderer, "<", x + w / 2 - 17, botY + 3, 0xFFFFFFFF);
        GlassRenderer.drawPillBadge(context, x + w / 2 + 2, botY, 24, 14, 0xFFFFFFFF, false);
        context.drawTextWithShadow(textRenderer, ">", x + w / 2 + 11, botY + 3, 0xFFFFFFFF);
    }

    private void updateDraggedSlider(double mouseX) {
        int innerX = panelX + 8;
        int innerW = panelW - 16;
        int trackX = innerX + 88;
        int trackW = innerW - 134;
        float norm = MathUtils.clamp((float) (mouseX - trackX) / (float) Math.max(1, trackW), 0.0f, 1.0f);
        module.setEnabled(true);
        switch (draggingSliderIndex) {
            case 0 -> module.setSaturation(Math.round((-1.0f + norm * 6.0f) * 20.0f) / 20.0f);
            case 1 -> module.setContrast(Math.round((norm * 2.0f) * 20.0f) / 20.0f);
            case 2 -> module.setBrightness(Math.round((norm * 2.0f) * 20.0f) / 20.0f);
            case 3 -> module.setHue(Math.round(-180.0f + norm * 360.0f));
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x(), mouseY = click.y();
        if (click.button() != 0 || closing) return super.mouseClicked(click, doubled);

        // Side Flanking Cards
        if (this.width - this.panelW >= 130) {
            int sideW = 48, sideH = 54;
            int sideY = panelY + (panelH - sideH) / 2;
            if (MathUtils.isHovered(mouseX, mouseY, panelX - sideW - 10, sideY, sideW, sideH)) {
                viewingPresets = false;
                GlassRenderer.playClickSound();
                return true;
            }
            if (MathUtils.isHovered(mouseX, mouseY, panelX + panelW + 10, sideY, sideW, sideH)) {
                viewingPresets = true;
                GlassRenderer.playClickSound();
                return true;
            }
        }

        // Bottom Dock
        int dockW = 50, dockH = 22, gap = 5;
        int totalDockW = dockW * 4 + gap * 3;
        int dockStartX = (this.width - totalDockW) / 2;
        for (int i = 0; i < 4; i++) {
            int dx = dockStartX + i * (dockW + gap);
            if (MathUtils.isHovered(mouseX, mouseY, dx, dockY, dockW, dockH)) {
                GlassRenderer.playClickSound();
                if (i == 0) viewingPresets = false;
                else if (i == 1) viewingPresets = true;
                else if (i == 2) module.resetSettingsToDefault();
                else close();
                return true;
            }
        }

        int innerX = panelX + 8, innerW = panelW - 16, y = panelY + 24;
        if (!viewingPresets) {
            if (MathUtils.isHovered(mouseX, mouseY, innerX, y, innerW, 15)) {
                viewingPresets = true;
                GlassRenderer.playClickSound();
                return true;
            }
            y += 19;
            int rowH = 20, rGap = 3;
            if (MathUtils.isHovered(mouseX, mouseY, innerX, y, innerW, rowH)) {
                module.toggle();
                GlassRenderer.playClickSound();
                return true;
            }
            y += rowH + rGap;
            for (int i = 0; i < 4; i++) {
                if (MathUtils.isHovered(mouseX, mouseY, innerX, y, innerW, rowH)) {
                    draggingSliderIndex = i;
                    updateDraggedSlider(mouseX);
                    return true;
                }
                y += rowH + rGap;
            }
        } else {
            ColorPreset[] presets = ColorPreset.values();
            int cols = 3, perPage = 12, pGap = 4;
            int cardW = (innerW - pGap * (cols - 1)) / cols, cardH = 22;
            int startIdx = presetPage * perPage;
            for (int i = 0; i < perPage && (startIdx + i) < presets.length; i++) {
                int c = i % cols, r = i / cols;
                int bx = innerX + c * (cardW + pGap);
                int by = y + 4 + r * (cardH + pGap);
                if (MathUtils.isHovered(mouseX, mouseY, bx, by, cardW, cardH)) {
                    module.applyPreset(presets[startIdx + i]);
                    module.setEnabled(true);
                    GlassRenderer.playClickSound();
                    return true;
                }
            }
            int botY = panelY + panelH - 18;
            if (MathUtils.isHovered(mouseX, mouseY, innerX + innerW / 2 - 26, botY, 52, 14)) {
                presetPage = (presetPage + 1) % 2;
                GlassRenderer.playClickSound();
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click c, double dx, double dy) {
        if (draggingSliderIndex >= 0) {
            updateDraggedSlider(c.x());
            return true;
        }
        return super.mouseDragged(c, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click c) {
        draggingSliderIndex = -1;
        return super.mouseReleased(c);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        if (!closing) {
            closing = true;
            openAnim.animateTo(0.0f);
        }
    }

    @Override
    public boolean shouldPause() { return false; }
}
