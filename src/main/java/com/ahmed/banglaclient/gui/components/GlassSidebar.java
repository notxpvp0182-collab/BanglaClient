package com.ahmed.banglaclient.gui.components;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.rendering.ClipRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.ColorUtils;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Renders the Left Navigation Sidebar of the Click GUI, modeled directly after the
 * reference dark liquid-glass dashboard ("BANGLA CLIENT" header, sectioned items, and bottom preset card).
 */
public final class GlassSidebar {
    public enum SidebarTab {
        HOME("Home", "OVERVIEW", null),
        HUD("HUD", "MODULES", ModuleCategory.HUD),
        COMBAT("PvP Information", "MODULES", ModuleCategory.COMBAT),
        VISUAL("Visual", "MODULES", ModuleCategory.VISUAL),
        INVENTORY("Inventory", "MODULES", ModuleCategory.INVENTORY),
        MOVEMENT("Movement", "MODULES", ModuleCategory.MOVEMENT),
        WORLD("World", "MODULES", ModuleCategory.WORLD),
        CHAT("Chat", "MODULES", ModuleCategory.CHAT),
        AUDIO("Audio", "MODULES", ModuleCategory.AUDIO),
        PERFORMANCE("Performance", "MODULES", ModuleCategory.PERFORMANCE),
        UTILITY("Utility", "MODULES", ModuleCategory.UTILITY),
        SETTINGS("Settings", "CLIENT & SYSTEM", null),
        THEMES("Themes", "CLIENT & SYSTEM", null),
        ABOUT("About", "CLIENT & SYSTEM", null);

        private final String label;
        private final String sectionHeader;
        private final ModuleCategory moduleCategory;

        SidebarTab(String label, String sectionHeader, ModuleCategory moduleCategory) {
            this.label = label;
            this.sectionHeader = sectionHeader;
            this.moduleCategory = moduleCategory;
        }

        public String getLabel() {
            return label;
        }

        public String getSectionHeader() {
            return sectionHeader;
        }

        public ModuleCategory getModuleCategory() {
            return moduleCategory;
        }
    }

    private final SmoothScrollContainer scrollContainer = new SmoothScrollContainer();
    private SidebarTab selectedTab = SidebarTab.HOME;

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

        // Sidebar container background
        GlassRenderer.drawGlassPanel(context, x, y, width, height, false, false, false);

        // Brand Identity Header ("BANGLA CLIENT" text, no oversized graphical logo)
        context.fill(x + 12, y + 14, x + 18, y + 24, accent);
        context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(BanglaClient.CLIENT_NAME, width - 26), x + 20, y + 12, theme.getTextPrimary());
        GlassRenderer.drawHorizontalDivider(context, x + 10, y + 34, width - 20);

        // Scrollable navigation list area
        int listY = y + 40;
        int bottomCardHeight = 52;
        int listHeight = Math.max(40, height - 46 - bottomCardHeight);

        scrollContainer.tickAndInterpolate();
        int totalContentHeight = calculateContentHeight();
        scrollContainer.updateDimensions(listHeight, totalContentHeight);

        ClipRenderer.renderClipped(context, x + 2, listY, width - 4, listHeight, () -> {
            int drawY = Math.round(listY - scrollContainer.getScrollOffset());
            String lastSection = "";

            for (SidebarTab tab : SidebarTab.values()) {
                if (!tab.getSectionHeader().equals(lastSection)) {
                    lastSection = tab.getSectionHeader();
                    drawY += 4;
                    context.drawTextWithShadow(textRenderer, lastSection, x + 12, drawY, theme.getTextMuted());
                    drawY += 13;
                }

                boolean active = (tab == selectedTab);
                boolean hovered = MathUtils.isHovered(mouseX, mouseY, x + 8, drawY, width - 16, 20)
                        && mouseY >= listY && mouseY <= (listY + listHeight);

                if (active || hovered) {
                    GlassRenderer.drawGlassPanel(
                            context, x + 8, drawY, width - 16, 20,
                            true, hovered, active
                    );
                    if (active) {
                        context.fill(x + 8, drawY + 4, x + 11, drawY + 16, accent);
                    }
                }

                int labelColor = active ? theme.getTextPrimary() : (hovered ? theme.getTextPrimary() : theme.getTextSecondary());
                context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(tab.getLabel(), width - 38), x + 12, drawY + 6, labelColor);

                // Count badge for module categories
                if (tab.getModuleCategory() != null && BanglaClient.getInstance() != null) {
                    int count = BanglaClient.getInstance().getModuleManager()
                            .getModulesByCategory(tab.getModuleCategory()).size();
                    String badge = String.valueOf(count);
                    int bw = textRenderer.getWidth(badge) + 8;
                    int bx = x + width - bw - 14;
                    GlassRenderer.drawPillBadge(context, bx, drawY + 4, bw, 12, accent, active);
                    context.drawTextWithShadow(textRenderer, badge, bx + 4, drawY + 6,
                            active ? accent : theme.getTextMuted());
                }

                drawY += 22;
            }
        });

        scrollContainer.renderScrollbar(context, x + width - 5, listY, 3, listHeight, mouseX, mouseY);

        // Bottom Pinned Performance Preset Card (inspired by bottom sidebar card in reference image)
        int cardY = y + height - bottomCardHeight - 4;
        boolean cardHovered = MathUtils.isHovered(mouseX, mouseY, x + 8, cardY, width - 16, bottomCardHeight - 4);
        GlassRenderer.drawGlassPanel(context, x + 8, cardY, width - 16, bottomCardHeight - 4, true, cardHovered, false);

        String presetName = BanglaClient.getInstance() != null
                ? BanglaClient.getInstance().getPerformancePreset().getDisplayName()
                : "Balanced";
        context.drawTextWithShadow(textRenderer, "Graphics Mode", x + 14, cardY + 7, theme.getTextSecondary());
        context.drawTextWithShadow(textRenderer, "Preset: " + presetName, x + 14, cardY + 19, accent);
        context.drawTextWithShadow(textRenderer, "Click to cycle preset", x + 14, cardY + 31, theme.getTextMuted());
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height, Runnable onTabChanged) {
        int listY = y + 40;
        int bottomCardHeight = 52;
        int listHeight = Math.max(40, height - 46 - bottomCardHeight);

        if (scrollContainer.onMouseClicked(mouseX, mouseY, button, x + width - 5, listY, 3, listHeight)) {
            return true;
        }

        // Bottom preset card click
        int cardY = y + height - bottomCardHeight - 4;
        if (button == 0 && MathUtils.isHovered(mouseX, mouseY, x + 8, cardY, width - 16, bottomCardHeight - 4)) {
            if (BanglaClient.getInstance() != null) {
                BanglaClient.getInstance().setPerformancePreset(
                        BanglaClient.getInstance().getPerformancePreset().next()
                );
                if (BanglaClient.getInstance().getConfigManager() != null) {
                    BanglaClient.getInstance().getConfigManager().saveGuiAndSystemConfig();
                }
            }
            return true;
        }

        if (button == 0 && MathUtils.isHovered(mouseX, mouseY, x + 4, listY, width - 10, listHeight)) {
            int drawY = Math.round(listY - scrollContainer.getScrollOffset());
            String lastSection = "";
            for (SidebarTab tab : SidebarTab.values()) {
                if (!tab.getSectionHeader().equals(lastSection)) {
                    lastSection = tab.getSectionHeader();
                    drawY += 17;
                }
                if (MathUtils.isHovered(mouseX, mouseY, x + 8, drawY, width - 16, 20)) {
                    this.selectedTab = tab;
                    if (onTabChanged != null) {
                        onTabChanged.run();
                    }
                    return true;
                }
                drawY += 22;
            }
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, int height) {
        int listHeight = Math.max(40, height - 98);
        return scrollContainer.onMouseDragged(mouseX, mouseY, button, listHeight);
    }

    public void mouseReleased(int button) {
        scrollContainer.onMouseReleased(button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount, int x, int y, int width, int height) {
        int listY = y + 40;
        int listHeight = Math.max(40, height - 98);
        return scrollContainer.onMouseScrolled(mouseX, mouseY, verticalAmount, x, listY, width, listHeight);
    }

    private int calculateContentHeight() {
        int total = 0;
        String lastSection = "";
        for (SidebarTab tab : SidebarTab.values()) {
            if (!tab.getSectionHeader().equals(lastSection)) {
                lastSection = tab.getSectionHeader();
                total += 17;
            }
            total += 22;
        }
        return total + 6;
    }

    public SidebarTab getSelectedTab() {
        return selectedTab;
    }

    public void setSelectedTab(SidebarTab selectedTab) {
        if (selectedTab != null) {
            this.selectedTab = selectedTab;
        }
    }
}
