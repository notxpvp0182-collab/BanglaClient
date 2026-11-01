package com.ahmed.banglaclient.rendering;

import net.minecraft.client.gui.DrawContext;

/**
 * Provides safe, nestable hardware scissor clipping around scrollable panels and cards.
 * Guarantees scissor state cleanup via try/finally blocks.
 */
public final class ClipRenderer {

    private ClipRenderer() {
    }

    /**
     * Enables scissor clipping for the specified rectangle and guarantees disableScissor() after execution.
     */
    public static void renderClipped(DrawContext context, int x, int y, int width, int height, Runnable drawAction) {
        if (context == null || drawAction == null || width <= 0 || height <= 0) {
            return;
        }
        context.enableScissor(x, y, x + width, y + height);
        try {
            drawAction.run();
        } finally {
            context.disableScissor();
        }
    }

    /**
     * Float overload for convenience when working with animated panel coordinates.
     */
    public static void renderClipped(DrawContext context, float x, float y, float width, float height, Runnable drawAction) {
        renderClipped(
                context,
                Math.round(x),
                Math.round(y),
                Math.max(0, Math.round(width)),
                Math.max(0, Math.round(height)),
                drawAction
        );
    }
}
