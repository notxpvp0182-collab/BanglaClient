package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.EnumSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import net.minecraft.client.gui.DrawContext;

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

        public float getPresetSaturation() { return saturation; }
        public float getPresetContrast() { return contrast; }
        public float getPresetBrightness() { return brightness; }
        public float getPresetHue() { return hue; }
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
                "Real-time GPU shader color saturation, contrast, brightness, and hue customizer with 16 visual presets.",
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
                saturationSetting.setValue((double) preset.getPresetSaturation());
                contrastSetting.setValue((double) preset.getPresetContrast());
                brightnessSetting.setValue((double) preset.getPresetBrightness());
                hueSetting.setValue((double) preset.getPresetHue());
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

    public String getActivePresetName() { return presetSetting.getValue().toString(); }
    public void applyPreset(ColorPreset p) { if (p != null) presetSetting.setValue(p); }
    public float getSaturation() { return saturationSetting.getFloatValue(); }
    public void setSaturation(float v) { saturationSetting.setValue((double) v); }
    public float getContrast() { return contrastSetting.getFloatValue(); }
    public void setContrast(float v) { contrastSetting.setValue((double) v); }
    public float getBrightness() { return brightnessSetting.getFloatValue(); }
    public void setBrightness(float v) { brightnessSetting.setValue((double) v); }
    public float getHue() { return hueSetting.getFloatValue(); }
    public void setHue(float v) { hueSetting.setValue((double) v); }

    public boolean isActiveNonIdentity() {
        return isEnabled() && (Math.abs(getSaturation() - 1.0f) > 0.005f
                || Math.abs(getContrast() - 1.0f) > 0.005f
                || Math.abs(getBrightness() - 1.0f) > 0.005f
                || Math.abs(getHue()) > 0.1f);
    }

    public void renderColorGradingOverlay(DrawContext context, int screenWidth, int screenHeight) {}
}
