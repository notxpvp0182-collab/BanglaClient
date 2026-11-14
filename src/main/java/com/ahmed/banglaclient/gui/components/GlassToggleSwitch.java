package com.ahmed.banglaclient.gui.components;

import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.gui.DrawContext;

/**
 * Crisp 2D Aura Toggle Switch with high-contrast white sliding knob and distinct ON/OFF track.
 */
public final class GlassToggleSwitch {
    public static final int DEFAULT_WIDTH = 26;
    public static final int DEFAULT_HEIGHT = 13;

    private final Animation toggleAnimation;
    private boolean lastKnownState;

    public GlassToggleSwitch(boolean initialState) {
        this.lastKnownState = initialState;
        this.toggleAnimation = new Animation(initialState ? 1.0f : 0.0f, 150L, Easing.EASE_OUT_CUBIC);
    }

    public void render(DrawContext context, int x, int y, boolean state, boolean hovered) {
        if (state != lastKnownState) {
            lastKnownState = state;
            toggleAnimation.animateTo(state ? 1.0f : 0.0f);
        }

        float progress = toggleAnimation.getValue();
        int offTrack = hovered ? 0xFF161920 : 0xFF0A0C10;
        int onTrack = 0x48FFFFFF;
        int trackFill = ColorUtils.lerpColor(offTrack, onTrack, progress);
        int borderCol = ColorUtils.lerpColor(0x38FFFFFF, 0xEEFFFFFF, progress);

        context.fill(x + 1, y, x + DEFAULT_WIDTH - 1, y + DEFAULT_HEIGHT, trackFill);
        context.fill(x, y + 1, x + 1, y + DEFAULT_HEIGHT - 1, trackFill);
        context.fill(x + DEFAULT_WIDTH - 1, y + 1, x + DEFAULT_WIDTH, y + DEFAULT_HEIGHT - 1, trackFill);
        GlassRenderer.drawOutlineBorder(context, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, borderCol);

        int knobSize = 7;
        int minKnobX = x + 3;
        int maxKnobX = x + DEFAULT_WIDTH - knobSize - 3;
        int knobX = Math.round(minKnobX + (maxKnobX - minKnobX) * progress);
        int knobY = y + 3;

        int knobColor = ColorUtils.lerpColor(0xFF788296, 0xFFFFFFFF, progress);
        context.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, knobColor);
    }
}
