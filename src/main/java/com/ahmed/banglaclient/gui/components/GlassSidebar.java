package com.ahmed.banglaclient.gui.components;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.identity.BcIdentityManager;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.rendering.ClipRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.util.ColorUtils;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * PYES-structured, Aura-styled Left Navigation Sidebar:
 * - Brand Header ("BANGLA CLIENT" + "MODS & HUD MANAGER")
 * - Smooth sliding active tab indicator + monochrome vector icons
 * - Bottom Connection / Version & Performance Preset status area
 */
public final class GlassSidebar {
    public enum SidebarTab {
        HOME("DASHBOARD", "OVERVIEW", null, "DASHBOARD"),
        HUD("HUD", "MODULES", ModuleCategory.HUD, "HUD"),
        COMBAT("PvP / Combat", "MODULES", ModuleCategory.COMBAT, "COMBAT"),
        VISUAL("Visuals", "MODULES", ModuleCategory.VISUAL, "VISUAL"),
        INVENTORY("Inventory", "MODULES", ModuleCategory.INVENTORY, "INVENTORY"),
        MOVEMENT("Movement", "MODULES", ModuleCategory.MOVEMENT, "MOVEMENT"),
        WORLD("World", "MODULES", ModuleCategory.WORLD, "WORLD"),
        CHAT("Chat", "MODULES", ModuleCategory.CHAT, "CHAT"),
        AUDIO("Audio", "MODULES", ModuleCategory.AUDIO, "AUDIO"),
        PERFORMANCE("Performance", "MODULES", ModuleCategory.PERFORMANCE, "PERFORMANCE"),
        UTILITY("Utility", "MODULES", ModuleCategory.UTILITY, "UTILITY"),
        THEMES("Themes", "CLIENT", null, "VISUAL"),
        SETTINGS("Settings", "CLIENT", null, "SETTINGS"),
        ABOUT("About", "CLIENT", null, "DASHBOARD");

        private final String label;
        private final String sectionHeader;
        private final ModuleCategory moduleCategory;
        private final String iconKey;

        SidebarTab(String label, String sectionHeader, ModuleCategory moduleCategory, String iconKey) {
            this.label = label;
            this.sectionHeader = sectionHeader;
            this.moduleCategory = moduleCategory;
            this.iconKey = iconKey;
        }

        public String getLabel() { return label; }
        public String getSectionHeader() { return sectionHeader; }
        public ModuleCategory getModuleCategory() { return moduleCategory; }
        public String getIconKey() { return iconKey; }
    }

    private final SmoothScrollContainer scrollContainer = new SmoothScrollContainer();
    private SidebarTab selectedTab = SidebarTab.HOME;
    private final Animation activeIndicatorY = new Animation(0.0f, 170L, Easing.EASE_OUT_CUBIC);
    private boolean indicatorInitialized = false;

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

        // 1. PYES-style Brand Header with BC Identity Emblem + "BANGLA CLIENT" + "MODS & HUD MANAGER"
        BcIdentityManager.drawIdentityIcon(context, x + 8, y + 8, 16, 16);
        context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth("BANGLA CLIENT", width - 30),
                x + 28, y + 7, theme.getTextPrimary());
        context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth("MODS & HUD", width - 30),
                x + 28, y + 17, theme.getTextMuted());
        GlassRenderer.drawHorizontalDivider(context, x + 6, y + 30, width - 12);

        // 2. Scrollable Navigation List
        int listY = y + 34;
        int bottomStatusHeight = 44;
        int listHeight = Math.max(40, height - 38 - bottomStatusHeight);

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
                    context.drawTextWithShadow(textRenderer, lastSection, x + 10, drawY, theme.getTextMuted());
                    drawY += 12;
                }

                boolean active = (tab == selectedTab);
                boolean hovered = MathUtils.isHovered(mouseX, mouseY, x + 6, drawY, width - 12, 20)
                        && mouseY >= listY && mouseY <= (listY + listHeight);

                if (active) {
                    if (!indicatorInitialized) {
                        activeIndicatorY.snapTo(drawY);
                        indicatorInitialized = true;
                    } else {
                        activeIndicatorY.animateTo(drawY);
                    }
                }

                if (active || hovered) {
                    int fill = active
                            ? ColorUtils.withAlpha(accent, 0.14f)
                            : ColorUtils.withAlpha(0xFFFFFF, 0.05f);
                    GlassRenderer.drawCustomGlassPanel(context, x + 6, drawY, width - 12, 20, fill, hovered, active, true);
                }

                int iconCol = active ? theme.getTextPrimary() : (hovered ? theme.getTextPrimary() : theme.getTextSecondary());
                GlassRenderer.drawVectorIcon(context, x + 11, drawY + 5, 10, tab.getIconKey(), iconCol);

                int labelColor = active ? theme.getTextPrimary() : (hovered ? theme.getTextPrimary() : theme.getTextSecondary());
                context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(tab.getLabel(), width - 44),
                        x + 25, drawY + 6, labelColor);

                if (tab.getModuleCategory() != null && BanglaClient.getInstance() != null) {
                    int count = BanglaClient.getInstance().getModuleManager()
                            .getModulesByCategory(tab.getModuleCategory()).size();
                    String countStr = String.valueOf(count);
                    int cw = textRenderer.getWidth(countStr);
                    context.drawTextWithShadow(textRenderer, countStr, x + width - cw - 11, drawY + 6,
                            active ? theme.getTextSecondary() : theme.getTextMuted());
                }

                drawY += 22;
            }

            // Smooth sliding left indicator bar
            int indY = Math.round(activeIndicatorY.getValue());
            context.fill(x + 6, indY + 4, x + 8, indY + 16, accent);
        });

        scrollContainer.renderScrollbar(context, x + width - 4, listY, 2, listHeight, mouseX, mouseY);

        // 3. Bottom PYES-style Status & Quality Preset Card
        int statusY = y + height - bottomStatusHeight - 3;
        GlassRenderer.drawHorizontalDivider(context, x + 6, statusY - 3, width - 12);
        boolean cardHovered = MathUtils.isHovered(mouseX, mouseY, x + 6, statusY, width - 12, bottomStatusHeight);
        GlassRenderer.drawGlassPanel(context, x + 6, statusY, width - 12, bottomStatusHeight, true, cardHovered, false);

        // Status dot + "CONNECTED / READY"
        context.fill(x + 12, statusY + 7, x + 16, statusY + 11, ThemeTokens.STATUS_SUCCESS);
        context.drawTextWithShadow(textRenderer, "READY", x + 20, statusY + 5, theme.getTextPrimary());

        String presetName = BanglaClient.getInstance() != null
                ? BanglaClient.getInstance().getPerformancePreset().getDisplayName()
                : "Balanced";
        context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth("Quality: " + presetName, width - 20),
                x + 12, statusY + 17, theme.getTextSecondary());
        context.drawTextWithShadow(textRenderer, "VERSION " + BanglaClient.CLIENT_VERSION,
                x + 12, statusY + 29, theme.getTextMuted());
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height, Runnable onTabChanged) {
        int listY = y + 34;
        int bottomStatusHeight = 44;
        int listHeight = Math.max(40, height - 38 - bottomStatusHeight);

        if (scrollContainer.onMouseClicked(mouseX, mouseY, button, x + width - 4, listY, 3, listHeight)) {
            return true;
        }

        int statusY = y + height - bottomStatusHeight - 3;
        if (button == 0 && MathUtils.isHovered(mouseX, mouseY, x + 6, statusY, width - 12, bottomStatusHeight)) {
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

        if (button == 0 && MathUtils.isHovered(mouseX, mouseY, x + 4, listY, width - 8, listHeight)) {
            int drawY = Math.round(listY - scrollContainer.getScrollOffset());
            String lastSection = "";
            for (SidebarTab tab : SidebarTab.values()) {
                if (!tab.getSectionHeader().equals(lastSection)) {
                    lastSection = tab.getSectionHeader();
                    drawY += 16;
                }
                if (MathUtils.isHovered(mouseX, mouseY, x + 6, drawY, width - 12, 20)) {
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
        int listHeight = Math.max(40, height - 82);
        return scrollContainer.onMouseDragged(mouseX, mouseY, button, listHeight);
    }

    public void mouseReleased(int button) {
        scrollContainer.onMouseReleased(button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount, int x, int y, int width, int height) {
        int listY = y + 34;
        int listHeight = Math.max(40, height - 82);
        return scrollContainer.onMouseScrolled(mouseX, mouseY, verticalAmount, x, listY, width, listHeight);
    }

    private int calculateContentHeight() {
        int total = 0;
        String lastSection = "";
        for (SidebarTab tab : SidebarTab.values()) {
            if (!tab.getSectionHeader().equals(lastSection)) {
                lastSection = tab.getSectionHeader();
                total += 16;
            }
            total += 22;
        }
        return total + 6;
    }

    public SidebarTab getSelectedTab() { return selectedTab; }
    public void setSelectedTab(SidebarTab selectedTab) {
        if (selectedTab != null) {
            this.selectedTab = selectedTab;
        }
    }
}
