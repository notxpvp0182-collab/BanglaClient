package com.ahmed.banglaclient.gui.components;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.ColorUtils;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.DrawContext;

/**
 * Reusable scroll controller supporting mouse-wheel scrolling, click-and-drag scrollbar thumb,
 * track click-to-jump, smooth damping, and accurate thumb height calculation.
 */
public final class SmoothScrollContainer {
    private float targetScroll;
    private float currentScroll;
    private float maxScroll;
    private float viewportHeight;
    private float contentHeight;

    private boolean draggingThumb;
    private float dragStartMouseY;
    private float dragStartScroll;
    private long lastUpdateTimeNanos = System.nanoTime();

    public void updateDimensions(float viewportHeight, float contentHeight) {
        this.viewportHeight = Math.max(1.0f, viewportHeight);
        this.contentHeight = Math.max(0.0f, contentHeight);
        this.maxScroll = Math.max(0.0f, this.contentHeight - this.viewportHeight);
        this.targetScroll = MathUtils.clamp(this.targetScroll, 0.0f, this.maxScroll);
        this.currentScroll = MathUtils.clamp(this.currentScroll, 0.0f, this.maxScroll);
    }

    /**
     * Updates the smooth scroll position using frame-rate-independent exponential damping.
     */
    public void tickAndInterpolate() {
        long now = System.nanoTime();
        float deltaSeconds = MathUtils.clamp((now - lastUpdateTimeNanos) / 1_000_000_000.0f, 0.0005f, 0.1f);
        lastUpdateTimeNanos = now;

        BanglaClient client = BanglaClient.getInstance();
        boolean instant = client != null
                && client.getConfigManager() != null
                && client.getConfigManager().isReducedMotion();

        if (instant || draggingThumb) {
            this.currentScroll = this.targetScroll;
        } else {
            this.currentScroll = MathUtils.damp(this.currentScroll, this.targetScroll, 18.0f, deltaSeconds);
            if (Math.abs(this.currentScroll - this.targetScroll) < 0.25f) {
                this.currentScroll = this.targetScroll;
            }
        }
    }

    public boolean onMouseScrolled(double mouseX, double mouseY, double verticalAmount, int viewX, int viewY, int viewWidth, int viewHeight) {
        if (!MathUtils.isHovered(mouseX, mouseY, viewX, viewY, viewWidth, viewHeight) || maxScroll <= 0.0f) {
            return false;
        }
        float scrollStep = 28.0f;
        targetScroll = MathUtils.clamp(targetScroll - (float) verticalAmount * scrollStep, 0.0f, maxScroll);
        return true;
    }

    public boolean onMouseClicked(double mouseX, double mouseY, int button, int trackX, int trackY, int trackWidth, int trackHeight) {
        if (button != 0 || maxScroll <= 0.0f) {
            return false;
        }
        if (!MathUtils.isHovered(mouseX, mouseY, trackX - 2, trackY, trackWidth + 4, trackHeight)) {
            return false;
        }

        float thumbHeight = getThumbHeight(trackHeight);
        float thumbY = getThumbY(trackY, trackHeight, thumbHeight);

        if (mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
            // Clicked directly on the thumb: start dragging
            this.draggingThumb = true;
            this.dragStartMouseY = (float) mouseY;
            this.dragStartScroll = this.targetScroll;
            return true;
        } else {
            // Clicked on the track: jump to that proportional scroll position and start dragging
            float clickableTrack = Math.max(1.0f, trackHeight - thumbHeight);
            float clickRatio = MathUtils.clamp(((float) mouseY - trackY - thumbHeight * 0.5f) / clickableTrack, 0.0f, 1.0f);
            this.targetScroll = clickRatio * maxScroll;
            this.currentScroll = this.targetScroll;
            this.draggingThumb = true;
            this.dragStartMouseY = (float) mouseY;
            this.dragStartScroll = this.targetScroll;
            return true;
        }
    }

    public boolean onMouseDragged(double mouseX, double mouseY, int button, int trackHeight) {
        if (!draggingThumb || button != 0 || maxScroll <= 0.0f) {
            return false;
        }
        float thumbHeight = getThumbHeight(trackHeight);
        float scrollableTrack = Math.max(1.0f, trackHeight - thumbHeight);
        float deltaY = (float) mouseY - dragStartMouseY;
        float scrollDelta = (deltaY / scrollableTrack) * maxScroll;

        this.targetScroll = MathUtils.clamp(dragStartScroll + scrollDelta, 0.0f, maxScroll);
        this.currentScroll = this.targetScroll;
        return true;
    }

    public void onMouseReleased(int button) {
        if (button == 0) {
            this.draggingThumb = false;
        }
    }

    /**
     * Renders the Liquid Glass scrollbar track and thumb when content exceeds the viewport.
     */
    public void renderScrollbar(DrawContext context, int trackX, int trackY, int trackWidth, int trackHeight, double mouseX, double mouseY) {
        if (maxScroll <= 0.0f || trackHeight <= 8) {
            return;
        }
        Theme theme = GlassRenderer.getTheme();

        // Track background
        int trackColor = ColorUtils.withAlpha(theme.getBackgroundSecondary(), 0.55f);
        context.fill(trackX, trackY, trackX + trackWidth, trackY + trackHeight, trackColor);

        // Thumb position & size
        float thumbHeight = getThumbHeight(trackHeight);
        float thumbY = getThumbY(trackY, trackHeight, thumbHeight);
        boolean thumbHovered = draggingThumb || MathUtils.isHovered(mouseX, mouseY, trackX - 2, thumbY, trackWidth + 4, thumbHeight);

        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : theme.getAccentPrimary();

        int thumbColor = draggingThumb
                ? accent
                : (thumbHovered ? ColorUtils.withAlpha(accent, 0.85f) : theme.getBorderStrong());

        int ty1 = Math.round(thumbY);
        int ty2 = Math.round(thumbY + thumbHeight);
        context.fill(trackX, ty1, trackX + trackWidth, ty2, thumbColor);
    }

    private float getThumbHeight(int trackHeight) {
        if (contentHeight <= 0.0f) {
            return trackHeight;
        }
        float ratio = MathUtils.clamp(viewportHeight / contentHeight, 0.08f, 1.0f);
        return Math.max(20.0f, trackHeight * ratio);
    }

    private float getThumbY(int trackY, int trackHeight, float thumbHeight) {
        if (maxScroll <= 0.0f) {
            return trackY;
        }
        float progress = MathUtils.clamp(currentScroll / maxScroll, 0.0f, 1.0f);
        return trackY + progress * (trackHeight - thumbHeight);
    }

    public float getScrollOffset() {
        return currentScroll;
    }

    public void resetScroll() {
        this.targetScroll = 0.0f;
        this.currentScroll = 0.0f;
        this.draggingThumb = false;
    }

    public boolean isScrollable() {
        return maxScroll > 0.0f;
    }

    public boolean isDraggingThumb() {
        return draggingThumb;
    }
}
