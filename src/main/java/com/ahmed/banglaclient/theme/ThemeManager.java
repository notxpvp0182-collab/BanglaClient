package com.ahmed.banglaclient.theme;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.util.ColorUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ThemeManager {
    private final Map<String, Theme> themes = new LinkedHashMap<>();
    private String activeThemeId = "red_theme";

    // Custom Theme configurable parameters (0.0f - 1.0f)
    private float customPrimaryHue = 0.0f;       // Red by default
    private float customSecondaryHue = 0.03f;
    private float customGlassAlpha = 0.22f;      // Genuine clear transparency
    private float customTintBrightness = 0.10f;
    private float customBorderAlpha = 0.35f;
    private float customTextBrightness = 0.98f;
    private boolean useCustomAccent = false;
    private int customAccentOverride = ThemeTokens.DEFAULT_ACCENT_PRIMARY;

    public ThemeManager() {
        rebuildThemes();
    }

    public void rebuildThemes() {
        themes.clear();
        // 1. Default Red Theme
        themes.put("red_theme", new Theme(
                "red_theme",
                "Red Theme",
                0x10080A0F,
                0x1E10141B,
                0x2412161F,
                0x32181E29,
                0x38FFFFFF,
                0x06FFFFFF,
                0xFFEF4444,
                0xFFF87171,
                0xFFF4F7FA,
                0xFFD0D7E2,
                0xFF94A3B8,
                0x2EFFFFFF,
                0x58EF4444
        ));

        // 2. User-Customizable Theme
        int priRgb = (Color.HSBtoRGB(customPrimaryHue, 0.82f, 0.96f) & 0x00FFFFFF) | 0xFF000000;
        int secRgb = (Color.HSBtoRGB(customSecondaryHue, 0.72f, 0.98f) & 0x00FFFFFF) | 0xFF000000;
        int tintBase = Color.HSBtoRGB(customPrimaryHue, 0.35f, Math.max(0.05f, customTintBrightness)) & 0x00FFFFFF;
        int surfaceArgb = ColorUtils.withAlpha(tintBase, customGlassAlpha);
        int elevatedArgb = ColorUtils.withAlpha(tintBase, Math.min(0.75f, customGlassAlpha + 0.10f));
        int borderSub = ColorUtils.withAlpha(0xFFFFFF, customBorderAlpha);
        int borderStr = ColorUtils.withAlpha(priRgb, Math.min(0.95f, customBorderAlpha + 0.25f));
        int txtChannel = Math.max(180, Math.min(255, Math.round(customTextBrightness * 255.0f)));
        int txtPrimary = (0xFF << 24) | (txtChannel << 16) | (txtChannel << 8) | txtChannel;

        themes.put("custom_theme", new Theme(
                "custom_theme",
                "Custom Theme",
                ColorUtils.withAlpha(tintBase, customGlassAlpha * 0.5f),
                ColorUtils.withAlpha(tintBase, customGlassAlpha * 0.8f),
                surfaceArgb,
                elevatedArgb,
                0x38FFFFFF,
                0x06FFFFFF,
                priRgb,
                secRgb,
                txtPrimary,
                0xFFD0D7E2,
                0xFF94A3B8,
                borderSub,
                borderStr
        ));
    }

    public void register(Theme theme) {
        if (theme != null) {
            themes.put(theme.getId(), theme);
        }
    }

    public Theme getActiveTheme() {
        rebuildThemes();
        return themes.getOrDefault(activeThemeId, themes.get("red_theme"));
    }

    public void setActiveThemeById(String themeId) {
        if (themeId != null && (themeId.equals("red_theme") || themeId.equals("custom_theme"))) {
            this.activeThemeId = themeId;
        } else {
            this.activeThemeId = "red_theme";
        }
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getConfigManager() != null) {
            BanglaClient.getInstance().getConfigManager().setActiveThemeId(this.activeThemeId);
        }
    }

    public void cycleNextTheme() {
        setActiveThemeById("red_theme".equals(activeThemeId) ? "custom_theme" : "red_theme");
    }

    public List<Theme> getAllThemes() {
        rebuildThemes();
        return Collections.unmodifiableList(new ArrayList<>(themes.values()));
    }

    public int getEffectiveAccentPrimary() {
        return useCustomAccent ? customAccentOverride : getActiveTheme().getAccentPrimary();
    }

    public void resetCustomThemeToRedDefaults() {
        this.customPrimaryHue = 0.0f;
        this.customSecondaryHue = 0.03f;
        this.customGlassAlpha = 0.22f;
        this.customTintBrightness = 0.10f;
        this.customBorderAlpha = 0.35f;
        this.customTextBrightness = 0.98f;
        rebuildThemes();
    }

    public float getCustomPrimaryHue() { return customPrimaryHue; }
    public void setCustomPrimaryHue(float v) { this.customPrimaryHue = Math.max(0f, Math.min(1f, v)); rebuildThemes(); }
    public float getCustomSecondaryHue() { return customSecondaryHue; }
    public void setCustomSecondaryHue(float v) { this.customSecondaryHue = Math.max(0f, Math.min(1f, v)); rebuildThemes(); }
    public float getCustomGlassAlpha() { return customGlassAlpha; }
    public void setCustomGlassAlpha(float v) { this.customGlassAlpha = Math.max(0.06f, Math.min(0.75f, v)); rebuildThemes(); }
    public float getCustomTintBrightness() { return customTintBrightness; }
    public void setCustomTintBrightness(float v) { this.customTintBrightness = Math.max(0.02f, Math.min(0.50f, v)); rebuildThemes(); }
    public float getCustomBorderAlpha() { return customBorderAlpha; }
    public void setCustomBorderAlpha(float v) { this.customBorderAlpha = Math.max(0.10f, Math.min(0.90f, v)); rebuildThemes(); }
    public float getCustomTextBrightness() { return customTextBrightness; }
    public void setCustomTextBrightness(float v) { this.customTextBrightness = Math.max(0.70f, Math.min(1.0f, v)); rebuildThemes(); }

    public boolean isUseCustomAccent() { return useCustomAccent; }
    public void setUseCustomAccent(boolean u) { this.useCustomAccent = u; }
    public int getCustomAccentOverride() { return customAccentOverride; }
    public void setCustomAccentOverride(int c) { this.customAccentOverride = c | 0xFF000000; }
}
