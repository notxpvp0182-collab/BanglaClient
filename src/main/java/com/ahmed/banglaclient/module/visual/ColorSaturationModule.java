package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.EnumSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

/**
 * Built-in Color Saturation, Vibrance, Contrast, Brightness & Hue Customizer Module with 16 Presets and Reset.
 */
public final class ColorSaturationModule extends Module {
    public enum ColorPreset {
        DEFAULT("Default", 1.0f, 1.0f, 1.0f, 0.0f),
        VIBRANT("Vibrant", 1.65f, 1.08f, 1.04f, 0.0f),
        ULTRA_VIBRANT("Ultra Vibrant", 2.60f, 1.15f, 1.06f, 0.0f),
        NATURAL("Natural", 1.25f, 1.04f, 1.03f, 0.0f),
        CINEMATIC("Cinematic", 0.85f, 1.28f, 0.96f, -6.0f),
        BLACK_WHITE("Black & White", 0.0f, 1.15f, 1.02f, 0.0f),
        ULTRA_BW("Ultra B&W", -0.20f, 1.55f, 0.98f, 0.0f),
        HIGH_CONTRAST("High Contrast", 1.35f, 1.50f, 1.04f, 0.0f),
        PVP_BOOST("PvP Boost", 1.85f, 1.22f, 1.18f, 0.0f),
        WARM_SUNSET("Warm Sunset", 1.40f, 1.12f, 1.02f, -14.0f),
        COLD_CYBER("Cold Cyber", 1.50f, 1.18f, 1.00f, 18.0f),
        DARK_MOODY("Dark Moody", 0.80f, 1.30f, 0.72f, 0.0f),
        NIGHT_VISION("Night Vision", 1.20f, 1.10f, 1.55f, 0.0f),
        RETRO_VINTAGE("Retro Vintage", 0.65f, 1.15f, 1.05f, -12.0f),
        PASTEL_SOFT("Pastel Soft", 0.75f, 0.85f, 1.12f, 5.0f),
        DEEP_GOLDEN("Deep Golden", 1.75f, 1.16f, 1.06f, -8.0f);

        private final String label;
        private final float saturation, contrast, brightness, hue;

        ColorPreset(String label, float saturation, float contrast, float brightness, float hue) {
            this.label = label;
            this.saturation = saturation;
            this.contrast = contrast;
            this.brightness = brightness;
            this.hue = hue;
        }

        public String getActivePresetName() { return presetSetting.getValue().toString(); }
    public void applyPreset(ColorPreset p) { if (p != null) presetSetting.setValue(p); }
    public void setSaturation(float v) { saturationSetting.setValue((double) v); }
    public void setContrast(float v) { contrastSetting.setValue((double) v); }
    public void setBrightness(float v) { brightnessSetting.setValue((double) v); }
    public void setHue(float v) { hueSetting.setValue((double) v); }
    public float getSaturation() { return saturation; }
        public float getContrast() { return contrast; }
        public float getBrightness() { return brightness; }
        public float getHue() { return hue; }
        @Override public String toString() { return label; }
    }

    private final EnumSetting<ColorPreset> presetSetting;
    private final NumberSetting saturationSetting;
    private final NumberSetting contrastSetting;
    private final NumberSetting brightnessSetting;
    private final NumberSetting hueSetting;
    private final BooleanSetting resetNowSetting;
    private boolean applyingPreset = false;

    public ColorSaturationModule() {
        super(
                "color_saturation",
                "Color Saturation",
                "Real-time color saturation, contrast, brightness, and hue customizer with 16 visual presets.",
                ModuleCategory.VISUAL
        );
        this.presetSetting = addSetting(new EnumSetting<>("preset", "Color Preset", "Select a built-in color grading preset", ColorPreset.VIBRANT));
        this.saturationSetting = addSetting(new NumberSetting("saturation", "Saturation", "Color saturation intensity (-1.0 to 5.0)", 1.65, -1.0, 5.0, 0.05, "x"));
        this.contrastSetting = addSetting(new NumberSetting("contrast", "Contrast", "Visual contrast balance (0.0 to 2.0)", 1.08, 0.0, 2.0, 0.05, "x"));
        this.brightnessSetting = addSetting(new NumberSetting("brightness", "Brightness", "Visual brightness level (0.0 to 2.0)", 1.04, 0.0, 2.0, 0.05, "x"));
        this.hueSetting = addSetting(new NumberSetting("hue", "Hue Shift", "Color hue rotation in degrees (-180° to 180°)", 0.0, -180.0, 180.0, 1.0, "°"));
        this.resetNowSetting = addSetting(new BooleanSetting("reset_color", "Action: Reset to Default", "Click to reset all color sliders to 1.0x default", false));

        this.presetSetting.onChange(preset -> {
            if (preset != null && !applyingPreset) {
                applyingPreset = true;
                saturationSetting.setValue((double) preset.getSaturation());
                contrastSetting.setValue((double) preset.getContrast());
                brightnessSetting.setValue((double) preset.getBrightness());
                hueSetting.setValue((double) preset.getHue());
                applyingPreset = false;
            }
        });

        this.resetNowSetting.onChange(val -> {
            if (Boolean.TRUE.equals(val)) {
                presetSetting.setValue(ColorPreset.DEFAULT);
                saturationSetting.setValue(1.0);
                contrastSetting.setValue(1.0);
                brightnessSetting.setValue(1.0);
                hueSetting.setValue(0.0);
                resetNowSetting.setValue(false);
            }
        });

        addAliases("saturation", "colorsaturation", "vibrance", "contrast", "hue", "color");
    }

    /**
     * Renders the real-time color grading pass (Saturation, Contrast, Brightness, Hue) over the game viewport.
     */
    public String getActivePresetName() { return presetSetting.getValue().toString(); }
    public void applyPreset(ColorPreset p) { if (p != null) presetSetting.setValue(p); }
    public void setSaturation(float v) { saturationSetting.setValue((double) v); }
    public void setContrast(float v) { contrastSetting.setValue((double) v); }
    public void setBrightness(float v) { brightnessSetting.setValue((double) v); }
    public void setHue(float v) { hueSetting.setValue((double) v); }
    public float getSaturation() { return saturationSetting.getFloatValue(); }
    public float getContrast() { return contrastSetting.getFloatValue(); }
    public float getBrightness() { return brightnessSetting.getFloatValue(); }
    public float getHue() { return hueSetting.getFloatValue(); }
    public boolean isActiveNonIdentity() { return isEnabled() && (Math.abs(getSaturation() - 1.0f) > 0.005f || Math.abs(getContrast() - 1.0f) > 0.005f || Math.abs(getBrightness() - 1.0f) > 0.005f || Math.abs(getHue()) > 0.1f); }
    public void renderColorGradingOverlay(DrawContext context, int screenWidth, int screenHeight) {
        
        if (!isEnabled() || context == null) return;
        float sat = saturationSetting.getFloatValue();
        float con = contrastSetting.getFloatValue();
        float bri = brightnessSetting.getFloatValue();
        float hueDeg = hueSetting.getFloatValue();

        // 1. Brightness & Contrast pass
        if (Math.abs(bri - 1.0f) > 0.02f) {
            if (bri > 1.0f) {
                int alpha = Math.min(95, Math.round((bri - 1.0f) * 110.0f));
                context.fill(0, 0, screenWidth, screenHeight, (alpha << 24) | 0xFFFFFF);
            } else {
                int alpha = Math.min(160, Math.round((1.0f - bri) * 180.0f));
                context.fill(0, 0, screenWidth, screenHeight, (alpha << 24));
            }
        }

        // 2. Saturation & Hue vibrancy pass
        float satDelta = sat - 1.0f;
        if (Math.abs(satDelta) > 0.03f || Math.abs(hueDeg) > 1.0f || Math.abs(con - 1.0f) > 0.03f) {
            float normalizedHue = ((hueDeg + 360.0f) % 360.0f) / 360.0f;
            if (Math.abs(hueDeg) <= 1.0f) {
                normalizedHue = satDelta >= 0 ? 0.08f : 0.60f;
            }
            float tintSat = Math.min(1.0f, Math.abs(satDelta) * 0.35f + Math.abs(hueDeg) / 180.0f * 0.5f);
            int rgb = Color.HSBtoRGB(normalizedHue, tintSat, satDelta >= 0 ? 1.0f : 0.25f) & 0x00FFFFFF;
            int alpha = Math.min(72, Math.round((Math.abs(satDelta) * 18.0f) + (Math.abs(con - 1.0f) * 28.0f) + (Math.abs(hueDeg) * 0.18f)));
            if (alpha > 2) {
                context.fill(0, 0, screenWidth, screenHeight, (alpha << 24) | rgb);
            }
        }
    }
}
