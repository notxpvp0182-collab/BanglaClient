package com.ahmed.banglaclient.gui.components;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.setting.EnumSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.ColorUtils;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Expandable Liquid Glass dropdown selector for EnumSetting instances.
 */
public final class GlassDropdownWidget<E extends Enum<E>> {
    private static final int HEADER_HEIGHT = 22;
    private static final int OPTION_HEIGHT = 18;

    private final EnumSetting<E> setting;
    private boolean expanded;

    public GlassDropdownWidget(EnumSetting<E> setting) {
        this.setting = setting;
    }

    public int getTotalHeight() {
        if (!expanded) {
            return HEADER_HEIGHT;
        }
        return HEADER_HEIGHT + (setting.getConstants().length * OPTION_HEIGHT) + 4;
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
        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : theme.getAccentPrimary();

        boolean headerHovered = MathUtils.isHovered(mouseX, mouseY, x, y, width, HEADER_HEIGHT);
        GlassRenderer.drawGlassPanel(context, x, y, width, getTotalHeight(), true, headerHovered, expanded);

        // Header label & active selection
        context.drawTextWithShadow(textRenderer, setting.getName(), x + 8, y + 7, theme.getTextPrimary());
        String currentVal = setting.getValue().toString() + (expanded ? " ▲" : " ▼");
        int valWidth = textRenderer.getWidth(currentVal);
        context.drawTextWithShadow(textRenderer, currentVal, x + width - valWidth - 8, y + 7, accent);

        if (expanded) {
            GlassRenderer.drawHorizontalDivider(context, x + 4, y + HEADER_HEIGHT, width - 8);
            E[] constants = setting.getConstants();
            int optY = y + HEADER_HEIGHT + 2;
            for (E constant : constants) {
                boolean optHovered = MathUtils.isHovered(mouseX, mouseY, x + 2, optY, width - 4, OPTION_HEIGHT);
                boolean selected = constant == setting.getValue();

                if (optHovered || selected) {
                    int highlight = selected
                            ? ColorUtils.withAlpha(accent, 0.22f)
                            : ColorUtils.withAlpha(0xFFFFFFFF, 0.08f);
                    context.fill(x + 3, optY, x + width - 3, optY + OPTION_HEIGHT, highlight);
                }

                int textColor = selected ? accent : (optHovered ? theme.getTextPrimary() : theme.getTextSecondary());
                context.drawTextWithShadow(textRenderer, constant.toString(), x + 12, optY + 5, textColor);
                optY += OPTION_HEIGHT;
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
        if (MathUtils.isHovered(mouseX, mouseY, x, y, width, HEADER_HEIGHT)) {
            if (button == 0) {
                expanded = !expanded;
                return true;
            } else if (button == 1) {
                setting.cycleNext();
                return true;
            }
        }
        if (expanded && button == 0) {
            E[] constants = setting.getConstants();
            int optY = y + HEADER_HEIGHT + 2;
            for (E constant : constants) {
                if (MathUtils.isHovered(mouseX, mouseY, x + 2, optY, width - 4, OPTION_HEIGHT)) {
                    setting.setValue(constant);
                    expanded = false;
                    return true;
                }
                optY += OPTION_HEIGHT;
            }
        }
        return false;
    }

    public EnumSetting<E> getSetting() {
        return setting;
    }

    public boolean isExpanded() {
        return expanded;
    }
}
