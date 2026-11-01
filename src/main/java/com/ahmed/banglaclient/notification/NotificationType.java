package com.ahmed.banglaclient.notification;

import com.ahmed.banglaclient.theme.ThemeTokens;

/**
 * Defines semantic categories and accent colors for client toast notifications.
 */
public enum NotificationType {
    INFO("INFO", ThemeTokens.DEFAULT_ACCENT_PRIMARY),
    SUCCESS("OK", ThemeTokens.STATUS_SUCCESS),
    WARNING("WARN", ThemeTokens.STATUS_WARNING),
    ERROR("ALERT", ThemeTokens.STATUS_ERROR);

    private final String badgeText;
    private final int accentColor;

    NotificationType(String badgeText, int accentColor) {
        this.badgeText = badgeText;
        this.accentColor = accentColor;
    }

    public String getBadgeText() {
        return badgeText;
    }

    public int getAccentColor() {
        return accentColor;
    }
}
