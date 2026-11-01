package com.ahmed.banglaclient.gui.components;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.ColorUtils;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Interactive Liquid Glass numeric slider widget for NumberSetting instances.
 */
public final class GlassSliderWidget {
    private final NumberSetting setting;
    private boolean dragging;

    public GlassSliderWidget(NumberSetting setting) {
        this.setting = setting;
    }

    public void render(
            DrawContext context,
            TextRenderer textRenderer,
            int x,
            int y,
            int width,
            int height,
            double mouseX,
            double mouseY
    ) {
        if (dragging) {
            updateValueFromMouse(mouseX, x, width);
        }

        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : theme.getAccentPrimary();

        // Label and formatted value
        String label = setting.getName();
        String valueText = setting.getFormattedValue();
        context.drawTextWithShadow(textRenderer, label, x, y + 2, theme.getTextPrimary());
        int valWidth = textRenderer.getWidth(valueText);
        context.drawTextWithShadow(textRenderer, valueText, x + width - valWidth, y + 2, accent);

        // Slider Bar Track
        int trackY = y + 15;
        int trackHeight = 4;
        context.fill(x, trackY, x + width, trackY + trackHeight, theme.getBackgroundSecondary());
        context.fill(x, trackY, x + width, trackY + 1, theme.getBorderSubtle());

        // Filled Progress
        float progress = (float) setting.getNormalizedProgress();
        int fillWidth = Math.round(width * progress);
        if (fillWidth > 0) {
            context.fillGradient(x, trackY, x + fillWidth, trackY + trackHeight, accent, theme.getAccentSecondary());
        }

        // Handle Knob
        int knobX = MathUtils.clamp(x + fillWidth - 3, x, x + width - 6);
        boolean hovered = MathUtils.isHovered(mouseX, mouseY, x, trackY - 4, width, trackHeight + 8);
        int knobColor = (dragging || hovered) ? 0xFFFFFFFF : theme.getTextPrimary();
        context.fill(knobX, trackY - 2, knobX + 6, trackY + trackHeight + 2, knobColor);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height) {
        if (button == 0 && MathUtils.isHovered(mouseX, mouseY, x, y, width, height)) {
            this.dragging = true;
            updateValueFromMouse(mouseX, x, width);
            return true;
        }
        return false;
    }

    public void mouseReleased(int button) {
        if (button == 0) {
            this.dragging = false;
        }
    }

    private void updateValueFromMouse(double mouseX, int x, int width) {
        if (width <= 0) {
            return;
        }
        double norm = MathUtils.clamp((mouseX - x) / (double) width, 0.0, 1.0);
        setting.setFromNormalizedProgress(norm);
    }

    public NumberSetting getSetting() {
        return setting;
    }

    public boolean isDragging() {
        return dragging;
    }
}
