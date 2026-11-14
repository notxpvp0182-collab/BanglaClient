package com.ahmed.banglaclient.gui.components;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.ColorUtils;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

/**
 * PYES-structured, Aura-styled Top Header Bar:
 * [SETTINGS] button | [Search modules... 🔍] | [Filter: Enabled / ★] | [□□□ Grid Density]
 */
public final class GlassTopBar {
    private String searchQuery = "";
    private boolean searchFocused = false;
    private boolean filterOnlyEnabled = false;
    private boolean filterOnlyFavorites = false;
    private boolean compactDensity = false;
    private boolean settingsShortcutClicked = false;

    private final Animation searchFocusAnim = new Animation(0.0f, 160L, Easing.EASE_OUT_CUBIC);

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

        searchFocusAnim.animateTo(searchFocused ? 1.0f : 0.0f);

        GlassRenderer.drawGlassPanel(context, x, y, width, height, false, false, false);

        int innerY = y + 5;
        int innerH = height - 10;

        // 1. Left: PYES-style [⚙ SETTINGS] quick button
        int settingsBtnW = 76;
        int settingsBtnX = x + 8;
        boolean settingsHov = MathUtils.isHovered(mouseX, mouseY, settingsBtnX, innerY, settingsBtnW, innerH);
        GlassRenderer.drawGlassPanel(context, settingsBtnX, innerY, settingsBtnW, innerH, true, settingsHov, false);
        GlassRenderer.drawVectorIcon(context, settingsBtnX + 7, innerY + (innerH - 10) / 2, 10, "SETTINGS",
                settingsHov ? theme.getTextPrimary() : theme.getTextSecondary());
        context.drawTextWithShadow(textRenderer, "SETTINGS", settingsBtnX + 21, innerY + (innerH - 8) / 2,
                settingsHov ? theme.getTextPrimary() : theme.getTextSecondary());

        // 2. Center: Filter Pills ([ACTIVE] [★])
        int pillX = settingsBtnX + settingsBtnW + 8;
        pillX += renderFilterPill(context, textRenderer, "ACTIVE", pillX, innerY + 1, innerH - 2, filterOnlyEnabled, accent, mouseX, mouseY) + 5;
        pillX += renderFilterPill(context, textRenderer, "★ FAV", pillX, innerY + 1, innerH - 2, filterOnlyFavorites, accent, mouseX, mouseY) + 5;

        // 3. Right Side: Grid Density Icon [□□□] + [Search modules...] input box
        int gridBtnW = 28;
        int gridBtnX = x + width - gridBtnW - 8;
        boolean gridHov = MathUtils.isHovered(mouseX, mouseY, gridBtnX, innerY, gridBtnW, innerH);
        GlassRenderer.drawGlassPanel(context, gridBtnX, innerY, gridBtnW, innerH, true, gridHov, compactDensity);
        // Draw 3 mini squares (□□□) like PYES top-right
        int sqY = innerY + (innerH - 4) / 2;
        int sqCol = compactDensity || gridHov ? theme.getTextPrimary() : theme.getTextMuted();
        context.fill(gridBtnX + 5, sqY, gridBtnX + 9, sqY + 4, sqCol);
        context.fill(gridBtnX + 12, sqY, gridBtnX + 16, sqY + 4, sqCol);
        context.fill(gridBtnX + 19, sqY, gridBtnX + 23, sqY + 4, sqCol);

        int searchW = Math.max(96, Math.min(180, gridBtnX - pillX - 8));
        int searchX = gridBtnX - searchW - 6;
        boolean searchHov = MathUtils.isHovered(mouseX, mouseY, searchX, innerY, searchW, innerH);
        GlassRenderer.drawGlassPanel(context, searchX, innerY, searchW, innerH, true, searchHov, searchFocused);

        float focusVal = searchFocusAnim.getValue();
        if (focusVal > 0.02f) {
            GlassRenderer.drawOutlineBorder(context, searchX, innerY, searchW, innerH,
                    ColorUtils.withAlpha(accent, 0.35f * focusVal));
        }

        String displaySearch;
        int searchTextColor;
        if (searchQuery.isEmpty() && !searchFocused) {
            displaySearch = "Search modules...";
            searchTextColor = theme.getTextMuted();
        } else {
            String cursor = (searchFocused && (System.currentTimeMillis() / 500L) % 2L == 0L) ? "|" : "";
            displaySearch = textRenderer.trimToWidth(searchQuery, searchW - 26) + cursor;
            searchTextColor = theme.getTextPrimary();
        }
        context.drawTextWithShadow(textRenderer, displaySearch, searchX + 8, innerY + (innerH - 8) / 2, searchTextColor);

        // Search icon or clear 'x' on right side of search box
        if (!searchQuery.isEmpty()) {
            int clearX = searchX + searchW - 14;
            boolean clearHover = MathUtils.isHovered(mouseX, mouseY, clearX - 2, innerY, 14, innerH);
            context.drawTextWithShadow(textRenderer, "×", clearX + 2, innerY + (innerH - 8) / 2,
                    clearHover ? theme.getTextPrimary() : theme.getTextSecondary());
        } else {
            int magX = searchX + searchW - 14;
            int magY = innerY + (innerH - 8) / 2;
            GlassRenderer.drawOutlineBorder(context, magX, magY, 6, 6, theme.getTextMuted());
            context.fill(magX + 5, magY + 5, magX + 8, magY + 8, theme.getTextMuted());
        }
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
        int width = tr.getWidth(text) + 12;
        boolean hovered = MathUtils.isHovered(mouseX, mouseY, x, y, width, height);
        GlassRenderer.drawPillBadge(context, x, y, width, height, active ? accent : theme.getTextSecondary(), active || hovered);
        int textColor = active ? theme.getTextPrimary() : (hovered ? theme.getTextPrimary() : theme.getTextSecondary());
        context.drawTextWithShadow(tr, text, x + 6, y + (height - 8) / 2, textColor);
        return width;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height, Runnable onFilterChanged) {
        if (button != 0) {
            return false;
        }
        int innerY = y + 5;
        int innerH = height - 10;

        // 1. Settings button click
        int settingsBtnW = 76;
        int settingsBtnX = x + 8;
        if (MathUtils.isHovered(mouseX, mouseY, settingsBtnX, innerY, settingsBtnW, innerH)) {
            settingsShortcutClicked = true;
            if (onFilterChanged != null) onFilterChanged.run();
            return true;
        }

        // 2. Filter Pills
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer tr = mc != null ? mc.textRenderer : null;
        int pillX = settingsBtnX + settingsBtnW + 8;
        int actW = (tr != null ? tr.getWidth("ACTIVE") : 36) + 12;
        if (MathUtils.isHovered(mouseX, mouseY, pillX, innerY + 1, actW, innerH - 2)) {
            filterOnlyEnabled = !filterOnlyEnabled;
            if (onFilterChanged != null) onFilterChanged.run();
            return true;
        }
        pillX += actW + 5;

        int favW = (tr != null ? tr.getWidth("★ FAV") : 30) + 12;
        if (MathUtils.isHovered(mouseX, mouseY, pillX, innerY + 1, favW, innerH - 2)) {
            filterOnlyFavorites = !filterOnlyFavorites;
            if (onFilterChanged != null) onFilterChanged.run();
            return true;
        }
        pillX += favW + 5;

        // 3. Grid Density Toggle [□□□]
        int gridBtnW = 28;
        int gridBtnX = x + width - gridBtnW - 8;
        if (MathUtils.isHovered(mouseX, mouseY, gridBtnX, innerY, gridBtnW, innerH)) {
            compactDensity = !compactDensity;
            if (onFilterChanged != null) onFilterChanged.run();
            return true;
        }

        // 4. Search Box
        int searchW = Math.max(96, Math.min(180, gridBtnX - pillX - 8));
        int searchX = gridBtnX - searchW - 6;

        if (!searchQuery.isEmpty()) {
            int clearX = searchX + searchW - 14;
            if (MathUtils.isHovered(mouseX, mouseY, clearX - 2, innerY, 14, innerH)) {
                searchQuery = "";
                if (onFilterChanged != null) onFilterChanged.run();
                return true;
            }
        }

        if (MathUtils.isHovered(mouseX, mouseY, searchX, innerY, searchW, innerH)) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        return false;
    }

    public boolean consumeSettingsShortcutClick() {
        if (settingsShortcutClicked) {
            settingsShortcutClicked = false;
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

    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery != null ? searchQuery : ""; }
    public boolean isSearchFocused() { return searchFocused; }
    public void setSearchFocused(boolean searchFocused) { this.searchFocused = searchFocused; }
    public boolean isFilterOnlyEnabled() { return filterOnlyEnabled; }
    public boolean isFilterOnlyFavorites() { return filterOnlyFavorites; }
    public boolean isCompactDensity() { return compactDensity; }
}
