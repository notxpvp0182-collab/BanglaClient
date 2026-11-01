package com.ahmed.banglaclient.gui.components;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

/**
 * Renders the Click GUI Top Utility Bar: interactive real-time Search Input, Clear button,
 * Filter pills (All / Enabled / Favorites), Density toggle, HUD Editor shortcut, and Version pill.
 */
public final class GlassTopBar {
    private String searchQuery = "";
    private boolean searchFocused = false;
    private boolean filterOnlyEnabled = false;
    private boolean filterOnlyFavorites = false;
    private boolean compactDensity = false;

    public void render(
            DrawContext context,
            TextRenderer textRenderer,
            int x,
            int y,
            int width,
            int height,
            double mouseX,
            double mouseY
    ) {
        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : theme.getAccentPrimary();

        GlassRenderer.drawGlassPanel(context, x, y, width, height, false, false, false);

        // 1. Search Box
        int searchX = x + 10;
        int searchY = y + 7;
        int searchW = Math.min(210, width / 3);
        int searchH = height - 14;
        boolean searchHovered = MathUtils.isHovered(mouseX, mouseY, searchX, searchY, searchW, searchH);
        GlassRenderer.drawGlassPanel(context, searchX, searchY, searchW, searchH, true, searchHovered, searchFocused);

        String displaySearch;
        int searchTextColor;
        if (searchQuery.isEmpty() && !searchFocused) {
            displaySearch = "Search modules, categories...";
            searchTextColor = theme.getTextMuted();
        } else {
            String cursor = (searchFocused && (System.currentTimeMillis() / 500L) % 2L == 0L) ? "|" : "";
            displaySearch = textRenderer.trimToWidth(searchQuery, searchW - 28) + cursor;
            searchTextColor = theme.getTextPrimary();
        }
        context.drawTextWithShadow(textRenderer, displaySearch, searchX + 8, searchY + 7, searchTextColor);

        // Clear 'X' button inside search bar when non-empty
        if (!searchQuery.isEmpty()) {
            int clearX = searchX + searchW - 16;
            boolean clearHover = MathUtils.isHovered(mouseX, mouseY, clearX - 2, searchY + 2, 14, searchH - 4);
            context.drawTextWithShadow(textRenderer, "x", clearX + 3, searchY + 7,
                    clearHover ? accent : theme.getTextSecondary());
        }

        // 2. Filter Pills: [Enabled] [Favorites] [Compact]
        int pillX = searchX + searchW + 10;
        int pillY = y + 9;
        int pillH = height - 18;

        pillX += renderFilterPill(context, textRenderer, "Enabled", pillX, pillY, pillH, filterOnlyEnabled, accent, mouseX, mouseY) + 6;
        pillX += renderFilterPill(context, textRenderer, "Favorites ★", pillX, pillY, pillH, filterOnlyFavorites, accent, mouseX, mouseY) + 6;
        renderFilterPill(context, textRenderer, compactDensity ? "Compact" : "Comfortable", pillX, pillY, pillH, compactDensity, accent, mouseX, mouseY);

        // 3. Right-hand Version Badge ("v1.0.0")
        String verText = "v" + BanglaClient.CLIENT_VERSION;
        int verW = textRenderer.getWidth(verText) + 12;
        int verX = x + width - verW - 10;
        GlassRenderer.drawPillBadge(context, verX, pillY, verW, pillH, accent, true);
        context.drawTextWithShadow(textRenderer, verText, verX + 6, pillY + 5, accent);
    }

    private int renderFilterPill(
            DrawContext context,
            TextRenderer tr,
            String text,
            int x,
            int y,
            int height,
            boolean active,
            int accent,
            double mouseX,
            double mouseY
    ) {
        Theme theme = GlassRenderer.getTheme();
        int width = tr.getWidth(text) + 14;
        boolean hovered = MathUtils.isHovered(mouseX, mouseY, x, y, width, height);
        GlassRenderer.drawPillBadge(context, x, y, width, height, active ? accent : theme.getTextSecondary(), active || hovered);
        int textColor = active ? accent : (hovered ? theme.getTextPrimary() : theme.getTextSecondary());
        context.drawTextWithShadow(tr, text, x + 7, y + 5, textColor);
        return width;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height, Runnable onFilterChanged) {
        if (button != 0) {
            return false;
        }
        int searchX = x + 10;
        int searchY = y + 7;
        int searchW = Math.min(210, width / 3);
        int searchH = height - 14;

        // Clear search button
        if (!searchQuery.isEmpty()) {
            int clearX = searchX + searchW - 16;
            if (MathUtils.isHovered(mouseX, mouseY, clearX - 2, searchY + 2, 14, searchH - 4)) {
                searchQuery = "";
                if (onFilterChanged != null) {
                    onFilterChanged.run();
                }
                return true;
            }
        }

        // Focus search bar
        if (MathUtils.isHovered(mouseX, mouseY, searchX, searchY, searchW, searchH)) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        // Filter pills hit testing
        MinecraftClientTextWidthHelper helper = new MinecraftClientTextWidthHelper();
        int pillX = searchX + searchW + 10;
        int pillY = y + 9;
        int pillH = height - 18;

        int enabledW = helper.getWidth("Enabled") + 14;
        if (MathUtils.isHovered(mouseX, mouseY, pillX, pillY, enabledW, pillH)) {
            filterOnlyEnabled = !filterOnlyEnabled;
            if (onFilterChanged != null) {
                onFilterChanged.run();
            }
            return true;
        }
        pillX += enabledW + 6;

        int favW = helper.getWidth("Favorites ★") + 14;
        if (MathUtils.isHovered(mouseX, mouseY, pillX, pillY, favW, pillH)) {
            filterOnlyFavorites = !filterOnlyFavorites;
            if (onFilterChanged != null) {
                onFilterChanged.run();
            }
            return true;
        }
        pillX += favW + 6;

        String densityLabel = compactDensity ? "Compact" : "Comfortable";
        int densityW = helper.getWidth(densityLabel) + 14;
        if (MathUtils.isHovered(mouseX, mouseY, pillX, pillY, densityW, pillH)) {
            compactDensity = !compactDensity;
            if (onFilterChanged != null) {
                onFilterChanged.run();
            }
            return true;
        }

        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers, Runnable onQueryChanged) {
        if (!searchFocused) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
            searchFocused = false;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                if (onQueryChanged != null) {
                    onQueryChanged.run();
                }
            }
            return true;
        }
        return false;
    }

    public boolean charTyped(char chr, int modifiers, Runnable onQueryChanged) {
        if (!searchFocused) {
            return false;
        }
        if (chr >= 32 && chr != 127 && searchQuery.length() < 48) {
            searchQuery += chr;
            if (onQueryChanged != null) {
                onQueryChanged.run();
            }
            return true;
        }
        return false;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery != null ? searchQuery : "";
    }

    public boolean isSearchFocused() {
        return searchFocused;
    }

    public void setSearchFocused(boolean searchFocused) {
        this.searchFocused = searchFocused;
    }

    public boolean isFilterOnlyEnabled() {
        return filterOnlyEnabled;
    }

    public boolean isFilterOnlyFavorites() {
        return filterOnlyFavorites;
    }

    public boolean isCompactDensity() {
        return compactDensity;
    }

    private static final class MinecraftClientTextWidthHelper {
        int getWidth(String text) {
            net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
            return mc != null && mc.textRenderer != null ? mc.textRenderer.getWidth(text) : text.length() * 6;
        }
    }
}
