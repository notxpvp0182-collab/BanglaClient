package com.ahmed.banglaclient.theme;

/**
 * Immutable definition of a Liquid Glass visual theme in BANGLA CLIENT.
 */
public final class Theme {
    private final String id;
    private final String displayName;
    private final int backgroundPrimary;
    private final int backgroundSecondary;
    private final int glassSurface;
    private final int glassElevated;
    private final int specularTop;
    private final int specularBottom;
    private final int accentPrimary;
    private final int accentSecondary;
    private final int textPrimary;
    private final int textSecondary;
    private final int textMuted;
    private final int borderSubtle;
    private final int borderStrong;

    public Theme(
            String id,
            String displayName,
            int backgroundPrimary,
            int backgroundSecondary,
            int glassSurface,
            int glassElevated,
            int specularTop,
            int specularBottom,
            int accentPrimary,
            int accentSecondary,
            int textPrimary,
            int textSecondary,
            int textMuted,
            int borderSubtle,
            int borderStrong
    ) {
        this.id = id;
        this.displayName = displayName;
        this.backgroundPrimary = backgroundPrimary;
        this.backgroundSecondary = backgroundSecondary;
        this.glassSurface = glassSurface;
        this.glassElevated = glassElevated;
        this.specularTop = specularTop;
        this.specularBottom = specularBottom;
        this.accentPrimary = accentPrimary;
        this.accentSecondary = accentSecondary;
        this.textPrimary = textPrimary;
        this.textSecondary = textSecondary;
        this.textMuted = textMuted;
        this.borderSubtle = borderSubtle;
        this.borderStrong = borderStrong;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBackgroundPrimary() {
        return backgroundPrimary;
    }

    public int getBackgroundSecondary() {
        return backgroundSecondary;
    }

    public int getGlassSurface() {
        return glassSurface;
    }

    public int getGlassElevated() {
        return glassElevated;
    }

    public int getSpecularTop() {
        return specularTop;
    }

    public int getSpecularBottom() {
        return specularBottom;
    }

    public int getAccentPrimary() {
        return accentPrimary;
    }

    public int getAccentSecondary() {
        return accentSecondary;
    }

    public int getTextPrimary() {
        return textPrimary;
    }

    public int getTextSecondary() {
        return textSecondary;
    }

    public int getTextMuted() {
        return textMuted;
    }

    public int getBorderSubtle() {
        return borderSubtle;
    }

    public int getBorderStrong() {
        return borderStrong;
    }
}
