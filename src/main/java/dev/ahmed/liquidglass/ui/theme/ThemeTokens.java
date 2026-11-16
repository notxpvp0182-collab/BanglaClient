package dev.ahmed.liquidglass.ui.theme;

public record ThemeTokens(
    String id,
    String displayName,
    int bgBase,
    int bgElevated,
    int bgGlass,
    int bgGlassStrong,
    int bgGlassWeak,
    int bgOverlay,
    float glassOpacity,
    float glassBlurRadius,
    float glassSaturation,
    float glassBrightness,
    float glassRefractionStrength,
    float glassEdgeSoftness,
    float glassHighlightIntensity,
    float glassNoiseIntensity,
    int borderBase,
    int borderInner,
    int borderOuter,
    int borderActive,
    float borderWidth,
    int shadowColor,
    float shadowRadius,
    float shadowOffsetX,
    float shadowOffsetY,
    float radiusSmall,
    float radiusMedium,
    float radiusLarge,
    float radiusPill,
    int textPrimary,
    int textSecondary,
    int textMuted,
    int textDisabled,
    int textAccent,
    float fontTitle,
    float fontHeading,
    float fontBody,
    float fontSmall,
    float fontTiny,
    int accentPrimary,
    int accentSecondary,
    int accentHover,
    int accentActive,
    int statusSuccess,
    int statusWarning,
    int statusError,
    int statusInfo
) {
    public static ThemeTokens darkLiquidGlass() {
        return new ThemeTokens(
            "dark_liquid_glass", "Dark Liquid Glass",
            0xFF08090C, 0xFF101218, 0xCC12141B, 0xE6141720, 0x99101218, 0xB3060709,
            0.82f, 14.0f, 1.08f, 0.94f, 0.012f, 1.25f, 0.18f, 0.015f,
            0x26FFFFFF, 0x1AFFFFFF, 0x4D000000, 0x73D8DEE9, 1.0f,
            0x80000000, 12.0f, 0.0f, 4.0f,
            5.0f, 8.0f, 12.0f, 999.0f,
            0xFFF4F5F7, 0xFFA7ABB5, 0xFF707581, 0xFF4B4F58, 0xFFE5E9F0,
            18.0f, 13.0f, 10.0f, 8.5f, 7.5f,
            0xFFD8DEE9, 0xFF88C0D0, 0xFFECEFF4, 0xFFFFFFFF,
            0xFF68D391, 0xFFF6AD55, 0xFFFC8181, 0xFF63B3ED
        );
    }

    public static ThemeTokens amoledGlass() {
        return new ThemeTokens(
            "amoled_glass", "AMOLED Obsidian",
            0xFF030304, 0xFF090A0D, 0xD90A0B0F, 0xF00C0D12, 0x8C08090C, 0xCC020203,
            0.86f, 16.0f, 1.04f, 0.90f, 0.010f, 1.25f, 0.14f, 0.010f,
            0x1FFFFFFF, 0x14FFFFFF, 0x66000000, 0x66E5E9F0, 1.0f,
            0x99000000, 14.0f, 0.0f, 5.0f,
            5.0f, 8.0f, 12.0f, 999.0f,
            0xFFF8F9FA, 0xFF9CA3AF, 0xFF6B7280, 0xFF374151, 0xFFF3F4F6,
            18.0f, 13.0f, 10.0f, 8.5f, 7.5f,
            0xFFE5E7EB, 0xFF93C5FD, 0xFFFFFFFF, 0xFFFFFFFF,
            0xFF34D399, 0xFFFBBF24, 0xFFF87171, 0xFF60A5FA
        );
    }
}
