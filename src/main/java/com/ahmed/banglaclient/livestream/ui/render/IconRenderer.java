package com.ahmed.banglaclient.livestream.ui.render;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;

public final class IconRenderer {
    public enum Icon {
        SETTINGS, CLOSE, BACK, SEARCH, EDIT, MOVE, LOCK, UNLOCK,
        EYE, EYE_OFF, RESET, ADD, REMOVE, CONNECT, DISCONNECT,
        LIVE, CHAT, STREAM, PROFILE, SERVER, THEME, DISPLAY, KEYBOARD, SAVE,
        SHIELD, SPARKLES, SLIDERS
    }

    private IconRenderer() {}

    public static void drawIcon(DrawContext ctx, Icon icon, float x, float y, float size, int color) {
        int alpha = (color >>> 24) & 0xFF;
        int softCol = UIRenderer.withAlpha(color, (int) (alpha * 0.35f));

        switch (icon) {
            case BACK -> {
                Matrix3x2fStack m = ctx.getMatrices();
                m.pushMatrix();
                m.translate(x, y);
                m.scale(0.5f, 0.5f);
                int s = Math.round(size * 2.0f);
                int cy = s / 2;
                ctx.fill(5, cy - 1, s - 4, cy + 1, color);
                for (int i = 0; i < 6; i++) {
                    ctx.fill(4 + i, cy - i, 6 + i, cy - i + 2, color);
                    ctx.fill(4 + i, cy + i - 1, 6 + i, cy + i + 1, color);
                }
                m.popMatrix();
            }
            case LIVE -> {
                float w = size, h = size * 0.78f, oy = y + (size - h) * 0.5f;
                UIRenderer.drawRoundedRect(ctx, x + 1.0f, oy + 1.0f, w - 2.0f, h - 2.0f, 5.0f, softCol);
                UIRenderer.drawRoundedBorder(ctx, x, oy, w, h, 5.5f, 1.0f, color);
                Matrix3x2fStack m = ctx.getMatrices();
                m.pushMatrix();
                m.translate(x + w * 0.38f, oy + h * 0.26f);
                m.scale(0.5f, 0.5f);
                int th = Math.round(h * 0.96f);
                for (int row = 0; row < th; row++) {
                    int half = th / 2;
                    int dist = half - Math.abs(half - row);
                    ctx.fill(0, row, Math.max(1, dist + 1), row + 1, color);
                }
                m.popMatrix();
            }
            case DISPLAY, STREAM -> {
                float w = size, h = size * 0.76f, oy = y + 1.0f;
                UIRenderer.drawRoundedBorder(ctx, x, oy, w, h, 4.5f, 1.0f, color);
                UIRenderer.drawRoundedRect(ctx, x + 3.0f, oy + 3.0f, w * 0.35f, h * 0.32f, 1.5f, color);
                UIRenderer.drawRoundedRect(ctx, x + w * 0.48f, oy + 3.0f, w - w * 0.48f - 3.0f, h * 0.32f, 1.5f, softCol);
                UIRenderer.drawRoundedRect(ctx, x + 3.0f, oy + h * 0.55f, w - 6.0f, h * 0.22f, 1.5f, softCol);
                UIRenderer.drawRoundedRect(ctx, x + w * 0.30f, oy + h + 2.0f, w * 0.40f, 1.5f, 1.0f, color);
            }
            case PROFILE -> {
                UIRenderer.drawRoundedBorder(ctx, x + 1.0f, y + 1.0f, size - 2.0f, size - 2.0f, (size - 2.0f) * 0.5f, 1.0f, softCol);
                float headR = size * 0.32f;
                UIRenderer.drawRoundedRect(ctx, x + (size - headR) * 0.5f, y + size * 0.18f, headR, headR, headR * 0.5f, color);
                float bodyW = size * 0.58f, bodyH = size * 0.25f;
                UIRenderer.drawRoundedRect(ctx, x + (size - bodyW) * 0.5f, y + size * 0.56f, bodyW, bodyH, bodyH * 0.5f, color);
            }
            case CHAT -> {
                float w = size, h = size * 0.76f, oy = y + 1.0f;
                UIRenderer.drawRoundedRect(ctx, x + 1.0f, oy + 1.0f, w - 2.0f, h - 2.0f, 5.0f, softCol);
                UIRenderer.drawRoundedBorder(ctx, x, oy, w, h, 5.5f, 1.0f, color);
                UIRenderer.drawRoundedRect(ctx, x + 4.0f, oy + h * 0.30f, w - 8.0f, 1.8f, 1.0f, color);
                UIRenderer.drawRoundedRect(ctx, x + 4.0f, oy + h * 0.58f, w * 0.52f, 1.8f, 1.0f, color);
                UIRenderer.drawRoundedRect(ctx, x + 4.0f, oy + h - 0.5f, 4.0f, 3.0f, 1.5f, color);
            }
            case SLIDERS, SETTINGS -> {
                float y1 = y + size * 0.22f, y2 = y + size * 0.50f, y3 = y + size * 0.78f;
                UIRenderer.drawRoundedRect(ctx, x + 1.0f, y1, size - 2.0f, 1.5f, 1.0f, softCol);
                UIRenderer.drawRoundedRect(ctx, x + 1.0f, y2, size - 2.0f, 1.5f, 1.0f, softCol);
                UIRenderer.drawRoundedRect(ctx, x + 1.0f, y3, size - 2.0f, 1.5f, 1.0f, softCol);
                float kw = 4.5f;
                UIRenderer.drawRoundedRect(ctx, x + size * 0.22f, y1 - 1.5f, kw, kw, kw * 0.5f, color);
                UIRenderer.drawRoundedRect(ctx, x + size * 0.58f, y2 - 1.5f, kw, kw, kw * 0.5f, color);
                UIRenderer.drawRoundedRect(ctx, x + size * 0.35f, y3 - 1.5f, kw, kw, kw * 0.5f, color);
            }
            default -> UIRenderer.drawRoundedBorder(ctx, x + 1.0f, y + 1.0f, size - 2.0f, size - 2.0f, 4.0f, 1.0f, color);
        }
    }
}
