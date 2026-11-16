package com.ahmed.banglaclient.livestream.ui.render;

import com.ahmed.banglaclient.livestream.ui.theme.GlassQuality;
import com.ahmed.banglaclient.livestream.ui.theme.ThemeManager;
import com.ahmed.banglaclient.livestream.ui.theme.ThemeTokens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;

public final class UIRenderer {
    private static final int MAX_R = 64;
    private static final int[][][] ARC_BANDS = new int[MAX_R + 1][][];
    private static final int[][] ARC_SOLID_INSET = new int[MAX_R + 1][];

    static {
        for (int r = 1; r <= MAX_R; r++) {
            ARC_SOLID_INSET[r] = new int[r];
            int[] aa = new int[r];
            for (int dy = 0; dy < r; dy++) {
                double py = r - dy - 0.5;
                double dxExact = Math.sqrt(Math.max(0.0, r * r - py * py));
                int dxInt = (int) Math.floor(dxExact);
                double frac = dxExact - dxInt;
                ARC_SOLID_INSET[r][dy] = Math.max(0, r - dxInt);
                aa[dy] = (int) Math.round(frac * 255.0);
            }

            List<int[]> bands = new ArrayList<>();
            int dy = 0;
            while (dy < r) {
                int in = ARC_SOLID_INSET[r][dy];
                int sumAa = aa[dy];
                int yEnd = dy + 1;
                while (yEnd < r && ARC_SOLID_INSET[r][yEnd] == in) {
                    sumAa += aa[yEnd];
                    yEnd++;
                }
                int avgAa = sumAa / (yEnd - dy);
                bands.add(new int[]{dy, yEnd, in, avgAa});
                dy = yEnd;
            }
            ARC_BANDS[r] = bands.toArray(new int[0][]);
        }
    }

    private UIRenderer() {}

    public static void drawGlassPanel(
        DrawContext ctx,
        float x, float y, float w, float h,
        float radius,
        int surfaceColor,
        int borderColor,
        float hoverProgress,
        float mouseX, float mouseY
    ) {
        ThemeTokens t = ThemeManager.get().tokens();
        GlassQuality q = ThemeManager.get().quality();
        float smoothRadius = Math.max(radius, 7.5f);

        if (q.shadowEnabled() && ((surfaceColor >>> 24) & 0xFF) > 10) {
            drawShadow(ctx, x, y + 2.0f, w, h, smoothRadius, 5.0f, t.shadowColor());
        }
        drawRoundedRect(ctx, x, y, w, h, smoothRadius, surfaceColor);

        if (q.innerHighlightEnabled() && h > 16.0f) {
            int sheenAlpha = q.liquidRefractionEnabled() ? (int) (14 + 18 * hoverProgress) : (int) (9 + 10 * hoverProgress);
            int topSheen = withAlpha(0xFFFFFFFF, sheenAlpha);
            drawRoundedRect(ctx, x + 1.0f, y + 0.75f, w - 2.0f, Math.min(h * 0.38f, 16.0f), Math.max(2.0f, smoothRadius - 1.0f), topSheen);
        }

        if (((borderColor >>> 24) & 0xFF) > 2) {
            int effectiveBorder = lerpColor(borderColor, t.borderActive(), hoverProgress);
            drawRoundedBorder(ctx, x, y, w, h, smoothRadius, 1.0f, effectiveBorder);
        }
    }

    public static void drawShadow(DrawContext ctx, float x, float y, float w, float h, float radius, float spread, int shadowColor) {
        int baseAlpha = (shadowColor >>> 24) & 0xFF;
        if (baseAlpha <= 4) return;
        int rgb = shadowColor & 0x00FFFFFF;
        GlassQuality q = ThemeManager.get().quality();

        drawRoundedRectFast(ctx, x - 1.25f, y - 0.5f, w + 2.5f, h + 2.0f, radius + 1.25f, (Math.max(4, baseAlpha / 4) << 24) | rgb, false);
        if (q.liquidRefractionEnabled()) {
            drawRoundedRectFast(ctx, x - 2.5f, y - 1.0f, w + 5.0f, h + 3.75f, radius + 2.5f, (Math.max(2, baseAlpha / 8) << 24) | rgb, false);
        }
    }

    public static void drawRoundedRect(DrawContext ctx, float x, float y, float w, float h, float radius, int color) {
        boolean feather = ThemeManager.get().quality().innerHighlightEnabled();
        drawRoundedRectFast(ctx, x, y, w, h, radius, color, feather);
    }

    private static void drawRoundedRectFast(DrawContext ctx, float x, float y, float w, float h, float radius, int color, boolean featherAA) {
        int baseAlpha = (color >>> 24) & 0xFF;
        if (baseAlpha <= 1 || w <= 0 || h <= 0) return;
        int rgb = color & 0x00FFFFFF;

        Matrix3x2fStack m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(x, y);
        m.scale(0.25f, 0.25f);

        int iw = Math.round(w * 4.0f);
        int ih = Math.round(h * 4.0f);
        int r = Math.min(MAX_R, Math.min(Math.round(radius * 4.0f), Math.min(iw, ih) / 2));

        if (r <= 1) {
            ctx.fill(0, 0, iw, ih, color);
            m.popMatrix();
            return;
        }

        int[][] bands = ARC_BANDS[r];
        for (int i = 0; i < bands.length; i++) {
            int[] b = bands[i];
            int y1 = b[0], y2 = b[1], in = b[2];
            int botY1 = ih - y2, botY2 = ih - y1;

            if (iw - in > in) {
                ctx.fill(in, y1, iw - in, y2, color);
                if (botY1 >= r) ctx.fill(in, botY1, iw - in, botY2, color);
            }
            if (featherAA && in > 0) {
                int aa = (baseAlpha * b[3]) >> 8;
                if (aa > 10) {
                    int aaCol = (aa << 24) | rgb;
                    ctx.fill(in - 1, y1, in, y2, aaCol);
                    ctx.fill(iw - in, y1, iw - in + 1, y2, aaCol);
                    if (botY1 >= r) {
                        ctx.fill(in - 1, botY1, in, botY2, aaCol);
                        ctx.fill(iw - in, botY1, iw - in + 1, botY2, aaCol);
                    }
                }
            }
        }
        if (ih - r > r) ctx.fill(0, r, iw, ih - r, color);
        m.popMatrix();
    }

    public static void drawRoundedBorder(DrawContext ctx, float x, float y, float w, float h, float radius, float thickness, int color) {
        int baseAlpha = (color >>> 24) & 0xFF;
        if (baseAlpha <= 2 || w <= 2 || h <= 2) return;

        Matrix3x2fStack m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(x, y);
        m.scale(0.25f, 0.25f);

        int iw = Math.round(w * 4.0f);
        int ih = Math.round(h * 4.0f);
        int r = Math.min(MAX_R, Math.min(Math.round(radius * 4.0f), Math.min(iw, ih) / 2));
        int t = Math.max(3, Math.round(thickness * 3.0f));

        if (r <= 2) {
            ctx.fill(0, 0, iw, t, color);
            ctx.fill(0, ih - t, iw, ih, color);
            ctx.fill(0, t, t, ih - t, color);
            ctx.fill(iw - t, t, iw, ih - t, color);
            m.popMatrix();
            return;
        }

        int[] outerInsets = ARC_SOLID_INSET[r];
        int innerR = Math.max(1, r - t);
        int[] innerInsets = ARC_SOLID_INSET[innerR];

        ctx.fill(r, 0, iw - r, t, color);
        ctx.fill(r, ih - t, iw - r, ih, color);
        ctx.fill(0, r, t, ih - r, color);
        ctx.fill(iw - t, r, iw, ih - r, color);

        int dy = 0;
        while (dy < r) {
            int outIn = outerInsets[dy];
            int inIn = (dy < t) ? r : (t + innerInsets[Math.min(innerR - 1, dy - t)]);
            int strokeEnd = Math.max(outIn + t, inIn);
            int yEnd = dy + 1;
            while (yEnd < r) {
                int nextOut = outerInsets[yEnd];
                int nextIn = (yEnd < t) ? r : (t + innerInsets[Math.min(innerR - 1, yEnd - t)]);
                int nextEnd = Math.max(nextOut + t, nextIn);
                if (nextOut != outIn || nextEnd != strokeEnd) break;
                yEnd++;
            }
            int botY1 = ih - yEnd, botY2 = ih - dy;
            ctx.fill(outIn, dy, strokeEnd, yEnd, color);
            ctx.fill(iw - strokeEnd, dy, iw - outIn, yEnd, color);
            if (botY1 >= r) {
                ctx.fill(outIn, botY1, strokeEnd, botY2, color);
                ctx.fill(iw - strokeEnd, botY1, iw - outIn, botY2, color);
            }
            dy = yEnd;
        }
        m.popMatrix();
    }

    public static void drawText(DrawContext ctx, String text, float x, float y, int color, float scale, boolean smallCaps, boolean shadow) {
        if (text == null || text.isEmpty()) return;
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        Matrix3x2fStack matrices = ctx.getMatrices();
        matrices.pushMatrix();
        matrices.translate(x, y);
        if (Math.abs(scale - 1.0f) > 0.001f) matrices.scale(scale, scale);
        EmojiRenderer.drawRichText(ctx, tr, text, 0, 0, color, smallCaps, shadow);
        matrices.popMatrix();
    }

    public static int withAlpha(int argb, int alpha255) {
        int clamped = Math.max(0, Math.min(255, alpha255));
        return (clamped << 24) | (argb & 0x00FFFFFF);
    }

    public static int lerpColor(int c1, int c2, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        int a = (int) (((c1 >>> 24) & 0xFF) + (((c2 >>> 24) & 0xFF) - ((c1 >>> 24) & 0xFF)) * t);
        int r = (int) (((c1 >>> 16) & 0xFF) + (((c2 >>> 16) & 0xFF) - ((c1 >>> 16) & 0xFF)) * t);
        int g = (int) (((c1 >>> 8) & 0xFF) + (((c2 >>> 8) & 0xFF) - ((c1 >>> 8) & 0xFF)) * t);
        int b = (int) ((c1 & 0xFF) + ((c2 & 0xFF) - (c1 & 0xFF)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
