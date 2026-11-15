package com.ahmed.banglaclient.livestream;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;

import java.io.DataInputStream;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BengaliFontEngine {
    private static final float HD_SCALE = 0.285f;
    public record AksharGlyph(int advanceSubPx, int[] rects) {}

    private static final Map<String, AksharGlyph> ATLAS = new HashMap<>(2048);
    private static final Map<String, List<AksharGlyph>> TOKEN_CACHE = new ConcurrentHashMap<>(512);
    private static final Map<String, Integer> WIDTH_CACHE = new ConcurrentHashMap<>(512);
    private static boolean loaded = false;

    private BengaliFontEngine() {}

    private static synchronized void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        try (InputStream in = BengaliFontEngine.class.getResourceAsStream("/assets/banglaclient/font/bengali_atlas.dat")) {
            if (in == null) return;
            DataInputStream dis = new DataInputStream(in);
            int count = dis.readInt();
            for (int i = 0; i < count; i++) {
                String key = dis.readUTF();
                int adv = dis.readUnsignedByte();
                int rectCount = dis.readUnsignedShort();
                int[] rects = new int[rectCount];
                for (int s = 0; s < rectCount; s++) {
                    rects[s] = dis.readInt();
                }
                ATLAS.put(key, new AksharGlyph(adv, rects));
            }
        } catch (Exception ignored) {}
    }

    public static boolean isBengaliChar(int cp) {
        return cp >= 0x0980 && cp <= 0x09FF;
    }

    public static String normalizeBengali(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        return Normalizer.normalize(raw, Normalizer.Form.NFC)
            .replace("\u09AF\u09BC", "\u09DF")
            .replace("\u09A1\u09BC", "\u09DC")
            .replace("\u09A2\u09BC", "\u09DD")
            .replace("\u09C7\u09BE", "\u09CB")
            .replace("\u09C7\u09D7", "\u09CC")
            .replace("\u200C", "")
            .replace("\u200D", "");
    }

    public static List<AksharGlyph> tokenizeToGlyphs(String bnWord) {
        if (bnWord == null || bnWord.isEmpty()) return List.of();
        ensureLoaded();
        if (TOKEN_CACHE.size() > 1024) TOKEN_CACHE.clear();
        return TOKEN_CACHE.computeIfAbsent(bnWord, key -> {
            String norm = normalizeBengali(key);
            List<AksharGlyph> out = new ArrayList<>(norm.length());
            int n = norm.length();
            int i = 0;
            while (i < n) {
                AksharGlyph matched = null;
                int matchedLen = 0;
                int maxLen = Math.min(8, n - i);
                for (int len = maxLen; len >= 1; len--) {
                    String sub = norm.substring(i, i + len);
                    AksharGlyph g = ATLAS.get(sub);
                    if (g != null) {
                        matched = g;
                        matchedLen = len;
                        break;
                    }
                }
                if (matched != null) {
                    out.add(matched);
                    i += matchedLen;
                } else {
                    i++;
                }
            }
            return Collections.unmodifiableList(out);
        });
    }

    public static int measureBengaliWidth(String bnSegment) {
        if (bnSegment == null || bnSegment.isEmpty()) return 0;
        if (WIDTH_CACHE.size() > 1024) WIDTH_CACHE.clear();
        return WIDTH_CACHE.computeIfAbsent(bnSegment, seg -> {
            List<AksharGlyph> glyphs = tokenizeToGlyphs(seg);
            int subPx = 0;
            for (AksharGlyph g : glyphs) {
                subPx += g.advanceSubPx();
            }
            return Math.round(subPx * HD_SCALE);
        });
    }

    public static float drawBengaliSegment(DrawContext ctx, String bnSegment, float x, float y, int color, boolean shadow) {
        List<AksharGlyph> glyphs = tokenizeToGlyphs(bnSegment);
        if (glyphs.isEmpty()) return 0.0f;

        int baseAlpha = (color >>> 24) & 0xFF;
        int rgb = color & 0x00FFFFFF;

        Matrix3x2fStack m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(x, y - 1.8f);
        m.scale(HD_SCALE, HD_SCALE);

        int cursorSubX = 0;
        if (shadow) {
            int shadowCol = (Math.max(12, (int) (baseAlpha * 0.55f)) << 24) | 0x06080C;
            for (AksharGlyph g : glyphs) {
                for (int sp : g.rects()) {
                    int a6 = sp & 0x3F;
                    if (a6 < 32) continue;
                    int y1 = (sp >>> 26) & 0x3F;
                    int y2 = (sp >>> 20) & 0x3F;
                    int x1 = ((sp >>> 13) & 0x7F) - 4;
                    int x2 = ((sp >>> 6) & 0x7F) - 4;
                    ctx.fill(cursorSubX + x1 + 2, y1 + 2, cursorSubX + x2 + 2, y2 + 2, shadowCol);
                }
                cursorSubX += g.advanceSubPx();
            }
            cursorSubX = 0;
        }

        for (AksharGlyph g : glyphs) {
            for (int sp : g.rects()) {
                int y1 = (sp >>> 26) & 0x3F;
                int y2 = (sp >>> 20) & 0x3F;
                int x1 = ((sp >>> 13) & 0x7F) - 4;
                int x2 = ((sp >>> 6) & 0x7F) - 4;
                int a8 = Math.min(255, (sp & 0x3F) << 2);
                int effA = (a8 * baseAlpha) / 255;
                if (effA > 10) {
                    ctx.fill(cursorSubX + x1, y1, cursorSubX + x2, y2, (effA << 24) | rgb);
                }
            }
            cursorSubX += g.advanceSubPx();
        }

        m.popMatrix();
        return cursorSubX * HD_SCALE;
    }
}
