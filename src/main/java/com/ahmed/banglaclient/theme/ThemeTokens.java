package com.ahmed.banglaclient.theme;

/**
 * Centralized design tokens for the BANGLA CLIENT Aura-inspired Frosted Glass design system.
 */
public final class ThemeTokens {
    // Aura Dark Primary Palette (Near-Black Charcoal & Restrained Cool-White/Silver)
    public static final int DEFAULT_BG_PRIMARY = 0xEE08090C;
    public static final int DEFAULT_BG_SECONDARY = 0xF20D0F14;
    public static final int DEFAULT_GLASS_SURFACE = 0xCC12151C;
    public static final int DEFAULT_GLASS_ELEVATED = 0xDC181C26;
    public static final int DEFAULT_SPECULAR_TOP = 0x22FFFFFF;
    public static final int DEFAULT_SPECULAR_BOTTOM = 0x06FFFFFF;

    // Restrained Aura Accents (Cool White / Soft Silver instead of harsh neon)
    public static final int DEFAULT_ACCENT_PRIMARY = 0xFFE2E8F0;
    public static final int DEFAULT_ACCENT_SECONDARY = 0xFF94A3B8;
    public static final int DEFAULT_ACCENT_GLOW = 0x24E2E8F0;

    // Typography Colors
    public static final int DEFAULT_TEXT_PRIMARY = 0xFFF8FAFC;
    public static final int DEFAULT_TEXT_SECONDARY = 0xFF94A3B8;
    public static final int DEFAULT_TEXT_MUTED = 0xFF64748B;

    // Subtle Thin Borders & Shadows
    public static final int DEFAULT_BORDER_SUBTLE = 0x1FFFFFFF;
    public static final int DEFAULT_BORDER_STRONG = 0x45FFFFFF;
    public static final int DEFAULT_SHADOW_AMBIENT = 0x66000000;

    // Semantic Status Colors (Restrained & Clean)
    public static final int STATUS_SUCCESS = 0xFF34D399;
    public static final int STATUS_WARNING = 0xFFFBBF24;
    public static final int STATUS_ERROR = 0xFFF87171;

    // Geometry & Layout Tokens
    public static final float RADIUS_WINDOW = 8.0f;
    public static final float RADIUS_CARD = 4.0f;
    public static final float RADIUS_BUTTON = 3.0f;
    public static final float RADIUS_PILL = 6.0f;

    public static final int PADDING_COMFORTABLE = 12;
    public static final int PADDING_COMPACT = 8;
    public static final int SIDEBAR_WIDTH = 126;
    public static final int TOPBAR_HEIGHT = 36;
    public static final int FOOTER_HEIGHT = 20;

    private ThemeTokens() {
    }
}
