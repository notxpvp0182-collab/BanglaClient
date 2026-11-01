package com.ahmed.banglaclient.theme;

/**
 * Shared design tokens for the BANGLA CLIENT Liquid Glass design system.
 * Centralizes colors, corner radii, spacing, and typography metrics.
 */
public final class ThemeTokens {
    // Core Dark Liquid Glass Palette (Default Reference Tokens)
    public static final int DEFAULT_BG_PRIMARY = 0x22080A0F;
    public static final int DEFAULT_BG_SECONDARY = 0x3610141B;
    public static final int DEFAULT_GLASS_SURFACE = 0x3A12161F;
    public static final int DEFAULT_GLASS_ELEVATED = 0x52181E29;
    public static final int DEFAULT_SPECULAR_TOP = 0x38FFFFFF;
    public static final int DEFAULT_SPECULAR_BOTTOM = 0x05FFFFFF;

    // Accents
    public static final int DEFAULT_ACCENT_PRIMARY = 0xFFEF4444;   // Sky Blue
    public static final int DEFAULT_ACCENT_SECONDARY = 0xFFF87171; // Cool Blue
    public static final int DEFAULT_ACCENT_GLOW = 0x3838BDF8;

    // Typography Colors
    public static final int DEFAULT_TEXT_PRIMARY = 0xFFF4F7FA;
    public static final int DEFAULT_TEXT_SECONDARY = 0xFFA5AFBC;
    public static final int DEFAULT_TEXT_MUTED = 0xFF64748B;

    // Borders & Shadows
    public static final int DEFAULT_BORDER_SUBTLE = 0x2CFFFFFF;
    public static final int DEFAULT_BORDER_STRONG = 0x52FFFFFF;
    public static final int DEFAULT_SHADOW_AMBIENT = 0x66000000;

    // Semantic Status Colors
    public static final int STATUS_SUCCESS = 0xFF34D399;
    public static final int STATUS_WARNING = 0xFFFBBF24;
    public static final int STATUS_ERROR = 0xFFF87171;

    // Layout & Geometry Constants
    public static final float RADIUS_WINDOW = 10.0f;
    public static final float RADIUS_CARD = 6.0f;
    public static final float RADIUS_BUTTON = 4.0f;
    public static final float RADIUS_PILL = 8.0f;

    public static final int PADDING_COMFORTABLE = 12;
    public static final int PADDING_COMPACT = 8;
    public static final int SIDEBAR_WIDTH = 118;
    public static final int TOPBAR_HEIGHT = 34;

    private ThemeTokens() {
    }
}
