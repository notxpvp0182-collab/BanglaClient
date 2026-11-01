package com.ahmed.banglaclient.theme;

import com.ahmed.banglaclient.BanglaClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages available Liquid Glass themes and provides the active theme to all UI renderers.
 */
public final class ThemeManager {
    private final Map<String, Theme> themes = new LinkedHashMap<>();
    private Theme activeTheme;
    private int customAccentOverride = 0;
    private boolean useCustomAccent = false;

    public ThemeManager() {
        registerBuiltInThemes();
        this.activeTheme = themes.get("liquid_dark_sky");
    }

    private void registerBuiltInThemes() {
        // 1. Primary Reference Theme: Liquid Dark Sky (Sky Blue & Cool Blue on Obsidian Glass)
        register(new Theme(
                "liquid_dark_sky",
                "Liquid Dark Sky",
                ThemeTokens.DEFAULT_BG_PRIMARY,
                ThemeTokens.DEFAULT_BG_SECONDARY,
                ThemeTokens.DEFAULT_GLASS_SURFACE,
                ThemeTokens.DEFAULT_GLASS_ELEVATED,
                ThemeTokens.DEFAULT_SPECULAR_TOP,
                ThemeTokens.DEFAULT_SPECULAR_BOTTOM,
                ThemeTokens.DEFAULT_ACCENT_PRIMARY,
                ThemeTokens.DEFAULT_ACCENT_SECONDARY,
                ThemeTokens.DEFAULT_TEXT_PRIMARY,
                ThemeTokens.DEFAULT_TEXT_SECONDARY,
                ThemeTokens.DEFAULT_TEXT_MUTED,
                ThemeTokens.DEFAULT_BORDER_SUBTLE,
                ThemeTokens.DEFAULT_BORDER_STRONG
        ));

        // 2. Reference Dashboard Ember: Smoked Charcoal with Warm Amber/Coral Accents
        register(new Theme(
                "smoked_ember_glass",
                "Smoked Ember Glass",
                0xFF09090C,
                0xFF121217,
                0xD8101116,
                0xE2181A20,
                0x24FFFFFF,
                0x05FFFFFF,
                0xFFF97316,
                0xFFFB923C,
                0xFFF4F7FA,
                0xFFA5AFBC,
                0xFF64748B,
                0x18FFFFFF,
                0x2EFFFFFF
        ));

        // 3. Bengal Emerald Glass: Deep Forest Obsidian with Crisp Emerald Accents
        register(new Theme(
                "bengal_emerald_glass",
                "Bengal Emerald Glass",
                0xFF060B09,
                0xFF0D1613,
                0xD60B1310,
                0xE0121E1A,
                0x24FFFFFF,
                0x05FFFFFF,
                0xFF10B981,
                0xFF34D399,
                0xFFF4F7FA,
                0xFFA3B8B0,
                0xFF5E7A70,
                0x18FFFFFF,
                0x2EFFFFFF
        ));

        // 4. Frost Crystal Glass: High-Clarity Midnight Blue Glass with Ice Cyan Accents
        register(new Theme(
                "frost_crystal_glass",
                "Frost Crystal Glass",
                0xFF070B12,
                0xFF0E1624,
                0xD20C1320,
                0xDC142033,
                0x28FFFFFF,
                0x06FFFFFF,
                0xFF22D3EE,
                0xFF38BDF8,
                0xFFF8FAFC,
                0xFF94A3B8,
                0xFF64748B,
                0x1CFFFFFF,
                0x33FFFFFF
        ));
    }

    public void register(Theme theme) {
        if (theme != null) {
            themes.put(theme.getId(), theme);
        }
    }

    public Theme getActiveTheme() {
        return activeTheme;
    }

    public void setActiveThemeById(String themeId) {
        if (themeId != null && themes.containsKey(themeId)) {
            this.activeTheme = themes.get(themeId);
            if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getConfigManager() != null) {
                BanglaClient.getInstance().getConfigManager().setActiveThemeId(themeId);
            }
        }
    }

    public void cycleNextTheme() {
        List<String> keys = new ArrayList<>(themes.keySet());
        int idx = keys.indexOf(activeTheme.getId());
        int nextIdx = (idx + 1) % keys.size();
        setActiveThemeById(keys.get(nextIdx));
    }

    public List<Theme> getAllThemes() {
        return Collections.unmodifiableList(new ArrayList<>(themes.values()));
    }

    public int getEffectiveAccentPrimary() {
        return useCustomAccent ? customAccentOverride : activeTheme.getAccentPrimary();
    }

    public boolean isUseCustomAccent() {
        return useCustomAccent;
    }

    public void setUseCustomAccent(boolean useCustomAccent) {
        this.useCustomAccent = useCustomAccent;
    }

    public int getCustomAccentOverride() {
        return customAccentOverride;
    }

    public void setCustomAccentOverride(int customAccentOverride) {
        this.customAccentOverride = customAccentOverride | 0xFF000000;
    }
}
