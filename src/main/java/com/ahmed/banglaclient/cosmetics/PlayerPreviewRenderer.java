package com.ahmed.banglaclient.cosmetics;

import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Renders an interactive, lit 3D Minecraft Player Model (Classic & Slim, 2nd layer Hat/Jacket/Sleeves,
 * and animated 3D Cape) plus 2D Skin Head icons directly via DrawContext with zero GPU framebuffer leaks.
 */
public final class PlayerPreviewRenderer {

    private record Quad3D(
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            int u0, int v0, int u1, int v1,
            int virtualW, int virtualH,
            SkinEntry texture,
            float shade,
            float avgZ
    ) {}

    private PlayerPreviewRenderer() {}

    /**
     * Renders a 2D player face + hat overlay at (x, y) of size (size x size).
     */
    public static void drawPlayerHead(DrawContext context, SkinEntry skin, int x, int y, int size) {
        if (context == null || size <= 0) return;
        if (skin == null) {
            context.fill(x, y, x + size, y + size, 0xFFEF4444);
            return;
        }
        // Base face: UV (8,8) to (16,16)
        drawUvRegion2D(context, skin, 8, 8, 8, 8, 64, 64, x, y, size, size, 1.0f);
        // Hat overlay: UV (40,8) to (48,16)
        if (skin.getHeight() == skin.getWidth()) {
            drawUvRegion2D(context, skin, 40, 8, 8, 8, 64, 64, x, y, size, size, 1.0f);
        }
    }

    public static void drawCapeThumbnail(DrawContext context, SkinEntry cape, int x, int y, int w, int h) {
        if (context == null || cape == null) return;
        drawUvRegion2D(context, cape, 1, 1, 10, 16, 64, 32, x, y, w, h, 1.0f);
    }

    private static void drawUvRegion2D(
            DrawContext context, SkinEntry entry,
            int uStart, int vStart, int uSpan, int vSpan,
            int virtW, int virtH,
            int screenX, int screenY, int screenW, int screenH,
            float brightness
    ) {
        for (int py = 0; py < vSpan; py++) {
            int sy0 = screenY + (py * screenH) / vSpan;
            int sy1 = screenY + ((py + 1) * screenH) / vSpan;
            if (sy1 <= sy0) sy1 = sy0 + 1;
            for (int px = 0; px < uSpan; px++) {
                int argb = entry.sampleUv(uStart + px, vStart + py, virtW, virtH);
                if (((argb >>> 24) & 0xFF) < 20) continue;
                int sx0 = screenX + (px * screenW) / uSpan;
                int sx1 = screenX + ((px + 1) * screenW) / uSpan;
                if (sx1 <= sx0) sx1 = sx0 + 1;
                int shaded = brightness == 1.0f ? argb : ColorUtils.scaleBrightness(argb, brightness);
                context.fill(sx0, sy0, sx1, sy1, shaded);
            }
        }
    }

    /**
     * Renders the interactive 3D Minecraft player character with idle animation, Classic/Slim arms,
     * outer skin layers, and 3D back cape.
     */
    public static void renderPlayerModel3D(
            DrawContext context,
            int centerX,
            int centerY,
            float scale,
            float yawDegrees,
            float pitchDegrees,
            SkinEntry skin,
            SkinEntry cape,
            boolean slimArms
    ) {
        if (context == null || skin == null) return;

        double time = (System.currentTimeMillis() % 60000L) / 1000.0;
        float idleSwing = (float) Math.sin(time * 2.1) * 4.5f;
        float yawRad = (float) Math.toRadians(yawDegrees);
        float pitchRad = (float) Math.toRadians(Math.max(-35.0f, Math.min(35.0f, pitchDegrees)));

        List<Quad3D> quads = new ArrayList<>(48);
        int armW = slimArms ? 3 : 4;
        boolean has64x64 = skin.getHeight() == skin.getWidth();

        // 1. Head (-4..4, -16..-8, -4..4)
        addBoxFaces(quads, -4, -16, -4, 8, 8, 8, 0, 0, 64, 64, skin, yawRad, pitchRad, 0f);
        // Hat Layer
        if (has64x64) {
            addBoxFaces(quads, -4.35f, -16.35f, -4.35f, 8.7f, 8.7f, 8.7f, 32, 0, 64, 64, skin, yawRad, pitchRad, 0f);
        }

        // 2. Torso (-4..4, -8..4, -2..2)
        addBoxFaces(quads, -4, -8, -2, 8, 12, 4, 16, 16, 64, 64, skin, yawRad, pitchRad, 0f);
        if (has64x64) {
            addBoxFaces(quads, -4.25f, -8.25f, -2.25f, 8.5f, 12.5f, 4.5f, 16, 32, 64, 64, skin, yawRad, pitchRad, 0f);
        }

        // 3. Right Arm (-4 - armW .. -4, -8..4, -2..2)
        addBoxFaces(quads, -4 - armW, -8, -2, armW, 12, 4, 40, 16, 64, 64, skin, yawRad, pitchRad, idleSwing * 0.04f);
        // 4. Left Arm (4 .. 4 + armW, -8..4, -2..2)
        int leftArmU = has64x64 ? 32 : 40;
        int leftArmV = has64x64 ? 48 : 16;
        addBoxFaces(quads, 4, -8, -2, armW, 12, 4, leftArmU, leftArmV, 64, 64, skin, yawRad, pitchRad, -idleSwing * 0.04f);

        // 5. Right Leg (-4..0, 4..16, -2..2)
        addBoxFaces(quads, -4, 4, -2, 4, 12, 4, 0, 16, 64, 64, skin, yawRad, pitchRad, -idleSwing * 0.02f);
        // 6. Left Leg (0..4, 4..16, -2..2)
        int leftLegU = has64x64 ? 16 : 0;
        int leftLegV = has64x64 ? 48 : 16;
        addBoxFaces(quads, 0, 4, -2, 4, 12, 4, leftLegU, leftLegV, 64, 64, skin, yawRad, pitchRad, idleSwing * 0.02f);

        // 7. 3D Equipped Cape on Back (-5..5, -8..8, -3.2..-2.2)
        if (cape != null) {
            float capeWave = (float) Math.sin(time * 2.6) * 0.35f;
            addBoxFaces(quads, -5, -8, -3.1f - capeWave, 10, 16, 1, 0, 0, 64, 32, cape, yawRad, pitchRad, 0f);
        }

        // Sort faces back-to-front by rotated average Z depth (Painter's Algorithm)
        quads.sort(Comparator.comparingDouble(Quad3D::avgZ));

        // Render sorted 3D texel faces
        for (Quad3D q : quads) {
            renderProjectedFace(context, centerX, centerY, scale, q, yawRad, pitchRad);
        }
    }

    private static void addBoxFaces(
            List<Quad3D> list,
            float x, float y, float z,
            float w, float h, float d,
            int u, int v,
            int virtW, int virtH,
            SkinEntry tex,
            float yaw, float pitch,
            float zSwingOffset
    ) {
        int iw = Math.max(1, Math.round(w));
        int ih = Math.max(1, Math.round(h));
        int id = Math.max(1, Math.round(d));
        float zOff = z + zSwingOffset;

        // Front face (Z = z + d)
        addFaceIfVisible(list, x, y, zOff + d, x + w, y + h, zOff + d,
                u + id, v + id, u + id + iw, v + id + ih, virtW, virtH, tex, 1.0f, 0, 0, 1, yaw, pitch);
        // Back face (Z = z)
        addFaceIfVisible(list, x + w, y, zOff, x, y + h, zOff,
                u + id + iw + id, v + id, u + id + iw + id + iw, v + id + ih, virtW, virtH, tex, 0.72f, 0, 0, -1, yaw, pitch);
        // Right face (X = x)
        addFaceIfVisible(list, x, y, zOff, x, y + h, zOff + d,
                u, v + id, u + id, v + id + ih, virtW, virtH, tex, 0.84f, -1, 0, 0, yaw, pitch);
        // Left face (X = x + w)
        addFaceIfVisible(list, x + w, y, zOff + d, x + w, y + h, zOff,
                u + id + iw, v + id, u + id + iw + id, v + id + ih, virtW, virtH, tex, 0.84f, 1, 0, 0, yaw, pitch);
        // Top face (Y = y)
        addFaceIfVisible(list, x, y, zOff, x + w, y, zOff + d,
                u + id, v, u + id + iw, v + id, virtW, virtH, tex, 1.08f, 0, -1, 0, yaw, pitch);
    }

    private static void addFaceIfVisible(
            List<Quad3D> list,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            int u0, int v0, int u1, int v1,
            int virtW, int virtH,
            SkinEntry tex,
            float baseShade,
            float nx, float ny, float nz,
            float yaw, float pitch
    ) {
        // Rotate normal vector to check back-face culling
        float rnx = (float) (nx * Math.cos(yaw) + nz * Math.sin(yaw));
        float rnz = (float) (-nx * Math.sin(yaw) + nz * Math.cos(yaw));
        float rny = (float) (ny * Math.cos(pitch) - rnz * Math.sin(pitch));
        float finalNz = (float) (ny * Math.sin(pitch) + rnz * Math.cos(pitch));

        if (finalNz < -0.15f) return;

        float cx = (x0 + x1) * 0.5f;
        float cy = (y0 + y1) * 0.5f;
        float cz = (z0 + z1) * 0.5f;
        float rotZ = (float) (-cx * Math.sin(yaw) + cz * Math.cos(yaw));
        float finalZ = (float) (cy * Math.sin(pitch) + rotZ * Math.cos(pitch));

        list.add(new Quad3D(x0, y0, z0, x1, y1, z1, u0, v0, u1, v1, virtW, virtH, tex, baseShade, finalZ));
    }

    private static void renderProjectedFace(
            DrawContext context,
            int centerX,
            int centerY,
            float scale,
            Quad3D q,
            float yaw,
            float pitch
    ) {
        int uSteps = Math.max(1, Math.abs(q.u1 - q.u0));
        int vSteps = Math.max(1, Math.abs(q.v1 - q.v0));
        float cosY = (float) Math.cos(yaw), sinY = (float) Math.sin(yaw);
        float cosP = (float) Math.cos(pitch), sinP = (float) Math.sin(pitch);

        for (int vi = 0; vi < vSteps; vi++) {
            float vt0 = (float) vi / vSteps;
            float vt1 = (float) (vi + 1) / vSteps;
            float wy0 = q.y0 + (q.y1 - q.y0) * vt0;
            float wy1 = q.y0 + (q.y1 - q.y0) * vt1;

            for (int ui = 0; ui < uSteps; ui++) {
                int sampleU = q.u0 + (q.u1 >= q.u0 ? ui : -ui);
                int sampleV = q.v0 + (q.v1 >= q.v0 ? vi : -vi);
                int argb = q.texture.sampleUv(sampleU, sampleV, q.virtualW, q.virtualH);
                if (((argb >>> 24) & 0xFF) < 25) continue;

                float ut0 = (float) ui / uSteps;
                float ut1 = (float) (ui + 1) / uSteps;
                float wx0 = q.x0 + (q.x1 - q.x0) * ut0;
                float wx1 = q.x0 + (q.x1 - q.x0) * ut1;
                float wz0 = q.z0 + (q.z1 - q.z0) * ut0;
                float wz1 = q.z0 + (q.z1 - q.z0) * ut1;

                float rx0 = wx0 * cosY + wz0 * sinY;
                float rz0 = -wx0 * sinY + wz0 * cosY;
                float ry0 = wy0 * cosP - rz0 * sinP;

                float rx1 = wx1 * cosY + wz1 * sinY;
                float rz1 = -wx1 * sinY + wz1 * cosY;
                float ry1 = wy1 * cosP - rz1 * sinP;

                int sx0 = Math.round(centerX + Math.min(rx0, rx1) * scale);
                int sx1 = Math.round(centerX + Math.max(rx0, rx1) * scale);
                int sy0 = Math.round(centerY + Math.min(ry0, ry1) * scale);
                int sy1 = Math.round(centerY + Math.max(ry0, ry1) * scale);

                if (sx1 <= sx0) sx1 = sx0 + Math.max(1, Math.round(scale * 0.85f));
                if (sy1 <= sy0) sy1 = sy0 + Math.max(1, Math.round(scale * 0.85f));

                int litColor = ColorUtils.scaleBrightness(argb, q.shade);
                context.fill(sx0, sy0, sx1, sy1, litColor);
            }
        }
    }
}
