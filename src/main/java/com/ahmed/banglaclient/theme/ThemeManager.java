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
    private String activeThemeId = "aura_dark";

    private float customPrimaryHue = 0.58f;
    private float customSecondaryHue = 0.60f;
    private float customGlassAlpha = 0.80f;
    private float customTintBrightness = 0.09f;
    private float customBorderAlpha = 0.22f;
    private float customTextBrightness = 0.98f;
    private boolean useCustomAccent = false;
    private int customAccentOverride = ThemeTokens.DEFAULT_ACCENT_PRIMARY;

    public ThemeManager() {
        rebuildThemes();
    }

    public void rebuildThemes() {
        themes.clear();

        themes.put("aura_dark", new Theme(
                "aura_dark",
                "Aura Dark",
                0xEE08090C,
                0xF20D0F14,
                0xCC12151C,
                0xDC181C26,
                0x20FFFFFF,
                0x05FFFFFF,
                0xFFE2E8F0,
                0xFF94A3B8,
                0xFFF8FAFC,
                0xFF94A3B8,
                0xFF64748B,
                0x1FFFFFFF,
                0x45FFFFFF
        ));

        themes.put("aura_light", new Theme(
                "aura_light",
                "Aura Light",
                0xEEEDF2F7,
                0xF2E2E8F0,
                0xCCF8FAFC,
                0xDCEFF6FF,
                0x40FFFFFF,
                0x10FFFFFF,
                0xFF1E293B,
                0xFF475569,
                0xFF0F172A,
                0xFF475569,
                0xFF64748B,
                0x240F172A,
                0x480F172A
        ));

        themes.put("red_theme", new Theme(
                "red_theme",
                "Crimson Dark",
                0xEE09080C,
                0xF2120E14,
                0xCC161118,
                0xDC1F1722,
                0x24FFFFFF,
                0x06FFFFFF,
                0xFFEF4444,
                0xFFF87171,
                0xFFF8FAFC,
                0xFFCBD5E1,
                0xFF64748B,
                0x22FFFFFF,
                0x50EF4444
        ));

        int priRgb = (Color.HSBtoRGB(customPrimaryHue, 0.35f, 0.95f) & 0x00FFFFFF) | 0xFF000000;
        int secRgb = (Color.HSBtoRGB(customSecondaryHue, 0.30f, 0.85f) & 0x00FFFFFF) | 0xFF000000;
        int tintBase = Color.HSBtoRGB(customPrimaryHue, 0.20f, Math.max(0.05f, customTintBrightness)) & 0x00FFFFFF;
        int surfaceArgb = ColorUtils.withAlpha(tintBase, customGlassAlpha);
        int elevatedArgb = ColorUtils.withAlpha(tintBase, Math.min(0.92f, customGlassAlpha + 0.08f));
        int borderSub = ColorUtils.withAlpha(0xFFFFFF, customBorderAlpha);
        int borderStr = ColorUtils.withAlpha(priRgb, Math.min(0.85f, customBorderAlpha + 0.20f));
        int txtChannel = Math.max(180, Math.min(255, Math.round(customTextBrightness * 255.0f)));
        int txtPrimary = (0xFF << 24) | (txtChannel << 16) | (txtChannel << 8) | txtChannel;

        themes.put("custom_theme", new Theme(
                "custom_theme",
                "Custom Theme",
                ColorUtils.withAlpha(tintBase, Math.min(0.94f, customGlassAlpha)),
                ColorUtils.withAlpha(tintBase, Math.min(0.96f, customGlassAlpha + 0.04f)),
                surfaceArgb,
                elevatedArgb,
                0x22FFFFFF,
                0x05FFFFFF,
                priRgb,
                secRgb,
                txtPrimary,
                0xFF94A3B8,
                0xFF64748B,
                borderSub,
                borderStr
        ));
    }

    public void register(Theme theme) {
        if (theme != null) themes.put(theme.getId(), theme);
    }

    public Theme getActiveTheme() {
        rebuildThemes();
        return themes.getOrDefault(activeThemeId, themes.get("aura_dark"));
    }

    public void setActiveThemeById(String themeId) {
        if (themeId != null && themes.containsKey(themeId)) {
            this.activeThemeId = themeId;
        } else {
            this.activeThemeId = "aura_dark";
        }
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getConfigManager() != null) {
            BanglaClient.getInstance().getConfigManager().setActiveThemeId(this.activeThemeId);
        }
    }

    public void cycleNextTheme() {
        List<String> keys = new ArrayList<>(themes.keySet());
        int idx = keys.indexOf(activeThemeId);
        int nextIdx = (idx + 1) % keys.size();
        setActiveThemeById(keys.get(nextIdx));
    }

    public List<Theme> getAllThemes() {
        rebuildThemes();
        return Collections.unmodifiableList(new ArrayList<>(themes.values()));
    }

    public int getEffectiveAccentPrimary() {
        return useCustomAccent ? customAccentOverride : getActiveTheme().getAccentPrimary();
    }

    public void resetCustomThemeToRedDefaults() {
        this.customPrimaryHue = 0.58f;
        this.customSecondaryHue = 0.60f;
        this.customGlassAlpha = 0.80f;
        this.customTintBrightness = 0.09f;
        this.customBorderAlpha = 0.22f;
        this.customTextBrightness = 0.98f;
        rebuildThemes();
    }

    public float getCustomPrimaryHue() { return customPrimaryHue; }
    public void setCustomPrimaryHue(float v) { this.customPrimaryHue = Math.max(0f, Math.min(1f, v)); rebuildThemes(); }
    public float getCustomSecondaryHue() { return customSecondaryHue; }
    public void setCustomSecondaryHue(float v) { this.customSecondaryHue = Math.max(0f, Math.min(1f, v)); rebuildThemes(); }
    public float getCustomGlassAlpha() { return customGlassAlpha; }
    public void setCustomGlassAlpha(float v) { this.customGlassAlpha = Math.max(0.06f, Math.min(0.92f, v)); rebuildThemes(); }
    public float getCustomTintBrightness() { return customTintBrightness; }
    public void setCustomTintBrightness(float v) { this.customTintBrightness = Math.max(0.02f, Math.min(0.50f, v)); rebuildThemes(); }
    public float getCustomBorderAlpha() { return customBorderAlpha; }
    public void setCustomBorderAlpha(float v) { this.customBorderAlpha = Math.max(0.08f, Math.min(0.90f, v)); rebuildThemes(); }
    public float getCustomTextBrightness() { return customTextBrightness; }
    public void setCustomTextBrightness(float v) { this.customTextBrightness = Math.max(0.70f, Math.min(1.0f, v)); rebuildThemes(); }

    public boolean isUseCustomAccent() { return useCustomAccent; }
    public void setUseCustomAccent(boolean u) { this.useCustomAccent = u; }
    public int getCustomAccentOverride() { return customAccentOverride; }
    public void setCustomAccentOverride(int c) { this.customAccentOverride = c | 0xFF000000; }
}
