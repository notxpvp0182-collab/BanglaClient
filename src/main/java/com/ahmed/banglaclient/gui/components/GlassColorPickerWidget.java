package com.ahmed.banglaclient.gui.components;

import com.ahmed.banglaclient.module.setting.ColorSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Interactive Liquid Glass RGBA and Chroma color picker widget for ColorSetting instances.
 */
public final class GlassColorPickerWidget {
    private static final int COLLAPSED_HEIGHT = 22;
    private static final int EXPANDED_HEIGHT = 98;

    private final ColorSetting setting;
    private boolean expanded;
    private int activeChannelDrag = -1; // 0=R, 1=G, 2=B, 3=A

    public GlassColorPickerWidget(ColorSetting setting) {
        this.setting = setting;
    }

    public int getTotalHeight() {
        return expanded ? EXPANDED_HEIGHT : COLLAPSED_HEIGHT;
    }

    public void render(
            DrawContext context,
            TextRenderer textRenderer,
            int x,
            int y,
            int width,
            double mouseX,
            double mouseY
    ) {
        if (activeChannelDrag >= 0) {
            updateChannelFromMouse(mouseX, x + 48, width - 56);
        }

        Theme theme = GlassRenderer.getTheme();
        boolean headerHovered = MathUtils.isHovered(mouseX, mouseY, x, y, width, COLLAPSED_HEIGHT);
        GlassRenderer.drawGlassPanel(context, x, y, width, getTotalHeight(), true, headerHovered, expanded);

        // Header label & Color preview swatch
        context.drawTextWithShadow(textRenderer, setting.getName(), x + 8, y + 7, theme.getTextPrimary());
        String hex = setting.toHexString();
        int hexWidth = textRenderer.getWidth(hex);
        context.drawTextWithShadow(textRenderer, hex, x + width - hexWidth - 28, y + 7, theme.getTextSecondary());

        int swatchX = x + width - 22;
        int swatchY = y + 4;
        context.fill(swatchX - 1, swatchY - 1, swatchX + 15, swatchY + 15, theme.getBorderStrong());
        context.fill(swatchX, swatchY, swatchX + 14, swatchY + 14, setting.getArgb());

        if (expanded) {
            GlassRenderer.drawHorizontalDivider(context, x + 6, y + COLLAPSED_HEIGHT, width - 12);
            int sliderX = x + 48;
            int sliderW = width - 56;

            renderChannelBar(context, textRenderer, "Red", setting.getRed(), 0xFFF87171, x + 8, y + 28, sliderX, sliderW);
            renderChannelBar(context, textRenderer, "Green", setting.getGreen(), 0xFF34D399, x + 8, y + 44, sliderX, sliderW);
            renderChannelBar(context, textRenderer, "Blue", setting.getBlue(), 0xFF38BDF8, x + 8, y + 60, sliderX, sliderW);

            if (setting.isAllowAlpha()) {
                renderChannelBar(context, textRenderer, "Alpha", setting.getAlpha(), 0xFFE2E8F0, x + 8, y + 76, sliderX, sliderW);
            } else {
                String chromaText = "Chroma Wave: " + (setting.isChroma() ? "ON" : "OFF");
                context.drawTextWithShadow(textRenderer, chromaText, x + 8, y + 78,
                        setting.isChroma() ? theme.getAccentPrimary() : theme.getTextSecondary());
            }
        }
    }

    private void renderChannelBar(
            DrawContext context,
            TextRenderer tr,
            String label,
            int channelValue,
            int barColor,
            int labelX,
            int rowY,
            int barX,
            int barWidth
    ) {
        Theme theme = GlassRenderer.getTheme();
        context.drawTextWithShadow(tr, label, labelX, rowY, theme.getTextSecondary());
        context.fill(barX, rowY + 3, barX + barWidth, rowY + 7, theme.getBackgroundSecondary());
        int fillW = Math.round((channelValue / 255.0f) * barWidth);
        if (fillW > 0) {
            context.fill(barX, rowY + 3, barX + fillW, rowY + 7, barColor);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
        if (MathUtils.isHovered(mouseX, mouseY, x, y, width, COLLAPSED_HEIGHT)) {
            if (button == 0 || button == 1) {
                expanded = !expanded;
                return true;
            }
        }
        if (expanded && button == 0) {
            int sliderX = x + 48;
            int sliderW = width - 56;
            for (int ch = 0; ch < 4; ch++) {
                int rowY = y + 28 + ch * 16;
                if (ch == 3 && !setting.isAllowAlpha()) {
                    if (MathUtils.isHovered(mouseX, mouseY, x + 6, rowY - 2, width - 12, 14)) {
                        setting.setChroma(!setting.isChroma());
                        return true;
                    }
                    break;
                }
                if (MathUtils.isHovered(mouseX, mouseY, sliderX - 4, rowY - 2, sliderW + 8, 14)) {
                    activeChannelDrag = ch;
                    updateChannelFromMouse(mouseX, sliderX, sliderW);
                    return true;
                }
            }
        }
        return false;
    }

    public void mouseReleased(int button) {
        if (button == 0) {
            activeChannelDrag = -1;
        }
    }

    private void updateChannelFromMouse(double mouseX, int barX, int barWidth) {
        if (barWidth <= 0) {
            return;
        }
        int val = MathUtils.clamp((int) Math.round(((mouseX - barX) / (double) barWidth) * 255.0), 0, 255);
        int r = setting.getRed();
        int g = setting.getGreen();
        int b = setting.getBlue();
        int a = setting.getAlpha();

        switch (activeChannelDrag) {
            case 0 -> r = val;
            case 1 -> g = val;
            case 2 -> b = val;
            case 3 -> a = val;
            default -> {
            }
        }
        setting.setRgba(r, g, b, a);
    }

    public ColorSetting getSetting() {
        return setting;
    }
}
