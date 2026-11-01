package com.ahmed.banglaclient.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Represents an ARGB color setting (`0xAARRGGBB`) with optional rainbow/chroma oscillation.
 */
public final class ColorSetting extends Setting<Integer> {
    private final boolean allowAlpha;
    private boolean chroma;
    private float chromaSpeed = 1.0f;

    public ColorSetting(String id, String name, String description, int defaultArgb, boolean allowAlpha) {
        super(id, name, description, allowAlpha ? defaultArgb : (defaultArgb | 0xFF000000));
        this.allowAlpha = allowAlpha;
        this.chroma = false;
    }

    public ColorSetting(String id, String name, String description, int defaultArgb) {
        this(id, name, description, defaultArgb, true);
    }

    public boolean isAllowAlpha() {
        return allowAlpha;
    }

    public boolean isChroma() {
        return chroma;
    }

    public void setChroma(boolean chroma) {
        this.chroma = chroma;
    }

    public float getChromaSpeed() {
        return chromaSpeed;
    }

    public void setChromaSpeed(float chromaSpeed) {
        this.chromaSpeed = Math.max(0.1f, Math.min(5.0f, chromaSpeed));
    }

    /**
     * Returns the effective ARGB integer, applying smooth HSB cycle if chroma is active.
     */
    public int getArgb() {
        int raw = getValue();
        if (!chroma) {
            return allowAlpha ? raw : (raw | 0xFF000000);
        }
        int alpha = allowAlpha ? ((raw >>> 24) & 0xFF) : 0xFF;
        float hue = ((System.currentTimeMillis() % (long) (4000.0f / chromaSpeed)) / (4000.0f / chromaSpeed));
        int rgb = java.awt.Color.HSBtoRGB(hue, 0.75f, 0.95f) & 0x00FFFFFF;
        return (alpha << 24) | rgb;
    }

    public int getAlpha() {
        return (getValue() >>> 24) & 0xFF;
    }

    public int getRed() {
        return (getValue() >>> 16) & 0xFF;
    }

    public int getGreen() {
        return (getValue() >>> 8) & 0xFF;
    }

    public int getBlue() {
        return getValue() & 0xFF;
    }

    public void setRgba(int r, int g, int b, int a) {
        int clampedA = allowAlpha ? Math.max(0, Math.min(255, a)) : 255;
        int clampedR = Math.max(0, Math.min(255, r));
        int clampedG = Math.max(0, Math.min(255, g));
        int clampedB = Math.max(0, Math.min(255, b));
        setValue((clampedA << 24) | (clampedR << 16) | (clampedG << 8) | clampedB);
    }

    public String toHexString() {
        if (allowAlpha) {
            return String.format("#%08X", getValue());
        }
        return String.format("#%06X", getValue() & 0x00FFFFFF);
    }

    @Override
    protected Integer validate(Integer candidate) {
        if (candidate == null) {
            return getDefaultValue();
        }
        return allowAlpha ? candidate : (candidate | 0xFF000000);
    }

    @Override
    public JsonElement toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("argb", getValue());
        obj.addProperty("hex", toHexString());
        obj.addProperty("chroma", chroma);
        obj.addProperty("chromaSpeed", chromaSpeed);
        return obj;
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            return;
        }
        JsonObject obj = element.getAsJsonObject();
        if (obj.has("argb") && obj.get("argb").isJsonPrimitive()) {
            setValue(obj.get("argb").getAsInt());
        }
        if (obj.has("chroma") && obj.get("chroma").isJsonPrimitive()) {
            this.chroma = obj.get("chroma").getAsBoolean();
        }
        if (obj.has("chromaSpeed") && obj.get("chromaSpeed").isJsonPrimitive()) {
            setChromaSpeed(obj.get("chromaSpeed").getAsFloat());
        }
    }
}
