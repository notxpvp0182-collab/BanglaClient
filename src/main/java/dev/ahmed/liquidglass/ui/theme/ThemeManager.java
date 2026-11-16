package dev.ahmed.liquidglass.ui.theme;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ThemeManager {
    private static final ThemeManager INSTANCE = new ThemeManager();
    private final Map<String, ThemeTokens> registry = new LinkedHashMap<>();
    private ThemeTokens activeTheme;
    private GlassQuality glassQuality = GlassQuality.ULTRA;

    private ThemeManager() {
        register(ThemeTokens.darkLiquidGlass());
        register(ThemeTokens.amoledGlass());
        this.activeTheme = ThemeTokens.amoledGlass();
    }

    public static ThemeManager get() { return INSTANCE; }
    public void register(ThemeTokens tokens) { registry.put(tokens.id(), tokens); }
    public ThemeTokens tokens() { return activeTheme; }
    public void setActiveTheme(String id) {
        ThemeTokens found = registry.get(id);
        if (found != null) this.activeTheme = found;
    }
    public GlassQuality quality() { return glassQuality; }
    public void setQuality(GlassQuality quality) {
        if (quality != null) this.glassQuality = quality;
    }
}
