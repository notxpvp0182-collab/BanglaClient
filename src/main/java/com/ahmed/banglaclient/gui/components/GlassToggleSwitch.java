package com.ahmed.banglaclient.gui.components;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.gui.DrawContext;

/**
 * Animated Liquid Glass ON/OFF pill toggle switch used on module cards and boolean settings.
 */
public final class GlassToggleSwitch {
    public static final int DEFAULT_WIDTH = 28;
    public static final int DEFAULT_HEIGHT = 14;

    private final Animation toggleAnimation;
    private boolean lastKnownState;

    public GlassToggleSwitch(boolean initialState) {
        this.lastKnownState = initialState;
        this.toggleAnimation = new Animation(initialState ? 1.0f : 0.0f, 160L, Easing.EASE_OUT_CUBIC);
    }

    public void render(DrawContext context, int x, int y, boolean state, boolean hovered) {
        if (state != lastKnownState) {
            lastKnownState = state;
            toggleAnimation.animateTo(state ? 1.0f : 0.0f);
        }

        float progress = toggleAnimation.getValue();
        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : theme.getAccentPrimary();

        int offTrackColor = hovered ? ColorUtils.scaleBrightness(theme.getBackgroundSecondary(), 1.25f) : theme.getBackgroundSecondary();
        int onTrackColor = ColorUtils.withAlpha(accent, 0.85f);
        int trackFill = ColorUtils.lerpColor(offTrackColor, onTrackColor, progress);
        int borderColor = ColorUtils.lerpColor(theme.getBorderSubtle(), accent, progress);

        // Rounded pill track
        context.fill(x + 1, y, x + DEFAULT_WIDTH - 1, y + DEFAULT_HEIGHT, trackFill);
        context.fill(x, y + 1, x + 1, y + DEFAULT_HEIGHT - 1, trackFill);
        context.fill(x + DEFAULT_WIDTH - 1, y + 1, x + DEFAULT_WIDTH, y + DEFAULT_HEIGHT - 1, trackFill);

        // Top & bottom subtle rim
        context.fill(x + 1, y, x + DEFAULT_WIDTH - 1, y + 1, borderColor);
        context.fill(x + 1, y + DEFAULT_HEIGHT - 1, x + DEFAULT_WIDTH - 1, y + DEFAULT_HEIGHT, ColorUtils.multiplyAlpha(borderColor, 0.55f));

        // Sliding knob
        int knobSize = 10;
        int minKnobX = x + 2;
        int maxKnobX = x + DEFAULT_WIDTH - knobSize - 2;
        int knobX = Math.round(minKnobX + (maxKnobX - minKnobX) * progress);
        int knobY = y + 2;

        int knobColor = ColorUtils.lerpColor(theme.getTextSecondary(), 0xFFFFFFFF, progress);
        context.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, knobColor);
    }
}
