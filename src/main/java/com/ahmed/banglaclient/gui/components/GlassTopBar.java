package com.ahmed.banglaclient.gui.components;

import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

/**
 * 2D Aura Top Bar: [BANGLA CLIENT] | [ENABLED] [★ FAV] | [Search...]
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
        GlassRenderer.drawGlassPanel(context, x, y, width, height, false, false, false);

        int innerY = y + 4;
        int innerH = height - 8;

        context.drawTextWithShadow(textRenderer, "BANGLA CLIENT", x + 8, y + (height - 8) / 2, 0xFFFFFFFF);

        int pillX = x + 88;
        pillX += renderFilterPill(context, textRenderer, "ENABLED", pillX, innerY, innerH, filterOnlyEnabled, mouseX, mouseY) + 4;
        pillX += renderFilterPill(context, textRenderer, "★ FAV", pillX, innerY, innerH, filterOnlyFavorites, mouseX, mouseY) + 4;

        int searchW = Math.max(76, Math.min(138, (x + width - 6) - pillX));
        int searchX = x + width - searchW - 6;
        boolean searchHov = MathUtils.isHovered(mouseX, mouseY, searchX, innerY, searchW, innerH);
        GlassRenderer.drawGlassPanel(context, searchX, innerY, searchW, innerH, true, searchHov, searchFocused);

        String displaySearch;
        int searchTextColor;
        if (searchQuery.isEmpty() && !searchFocused) {
            displaySearch = "Search...";
            searchTextColor = theme.getTextMuted();
        } else {
            String cursor = (searchFocused && (System.currentTimeMillis() / 500L) % 2L == 0L) ? "|" : "";
            displaySearch = textRenderer.trimToWidth(searchQuery, searchW - 20) + cursor;
            searchTextColor = 0xFFFFFFFF;
        }
        context.drawTextWithShadow(textRenderer, displaySearch, searchX + 6, innerY + (innerH - 8) / 2, searchTextColor);

        if (!searchQuery.isEmpty()) {
            int clearX = searchX + searchW - 12;
            context.drawTextWithShadow(textRenderer, "×", clearX, innerY + (innerH - 8) / 2, theme.getTextSecondary());
        }
    }

    private int renderFilterPill(DrawContext context, TextRenderer tr, String text, int x, int y, int height, boolean active, double mouseX, double mouseY) {
        Theme theme = GlassRenderer.getTheme();
        int width = tr.getWidth(text) + 12;
        boolean hovered = MathUtils.isHovered(mouseX, mouseY, x, y, width, height);
        GlassRenderer.drawPillBadge(context, x, y, width, height, 0xFFFFFFFF, active || hovered);
        int textColor = active ? 0xFFFFFFFF : (hovered ? 0xFFFFFFFF : theme.getTextSecondary());
        context.drawTextWithShadow(tr, text, x + 6, y + (height - 8) / 2, textColor);
        return width;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height, Runnable onFilterChanged) {
        if (button != 0) return false;
        int innerY = y + 4;
        int innerH = height - 8;

        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer tr = mc != null ? mc.textRenderer : null;
        int pillX = x + 88;
        int enW = (tr != null ? tr.getWidth("ENABLED") : 42) + 12;
        if (MathUtils.isHovered(mouseX, mouseY, pillX, innerY, enW, innerH)) {
            filterOnlyEnabled = !filterOnlyEnabled;
            if (filterOnlyEnabled) filterOnlyFavorites = false;
            if (onFilterChanged != null) onFilterChanged.run();
            return true;
        }
        pillX += enW + 4;

        int favW = (tr != null ? tr.getWidth("★ FAV") : 30) + 12;
        if (MathUtils.isHovered(mouseX, mouseY, pillX, innerY, favW, innerH)) {
            filterOnlyFavorites = !filterOnlyFavorites;
            if (filterOnlyFavorites) filterOnlyEnabled = false;
            if (onFilterChanged != null) onFilterChanged.run();
            return true;
        }
        pillX += favW + 4;

        int searchW = Math.max(76, Math.min(138, (x + width - 6) - pillX));
        int searchX = x + width - searchW - 6;

        if (!searchQuery.isEmpty() && MathUtils.isHovered(mouseX, mouseY, searchX + searchW - 14, innerY, 14, innerH)) {
            searchQuery = "";
            if (onFilterChanged != null) onFilterChanged.run();
            return true;
        }

        if (MathUtils.isHovered(mouseX, mouseY, searchX, innerY, searchW, innerH)) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }
        return false;
    }

    public void clearQuickFilters() {
        this.filterOnlyEnabled = false;
        this.filterOnlyFavorites = false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers, Runnable onQueryChanged) {
        if (!searchFocused) return false;
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
            searchFocused = false;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                if (onQueryChanged != null) onQueryChanged.run();
            }
            return true;
        }
        return false;
    }

    public boolean charTyped(char chr, int modifiers, Runnable onQueryChanged) {
        if (!searchFocused) return false;
        if (chr >= 32 && chr != 127 && searchQuery.length() < 40) {
            searchQuery += chr;
            if (onQueryChanged != null) onQueryChanged.run();
            return true;
        }
        return false;
    }

    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery != null ? searchQuery : ""; }
    public boolean isSearchFocused() { return searchFocused; }
    public void setSearchFocused(boolean searchFocused) { this.searchFocused = searchFocused; }
    public boolean isFilterOnlyEnabled() { return filterOnlyEnabled; }
    public boolean isFilterOnlyFavorites() { return filterOnlyFavorites; }
    public boolean isCompactDensity() { return compactDensity; }
}
