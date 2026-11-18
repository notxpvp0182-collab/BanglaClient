package com.ahmed.banglaclient.gui.components;

import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.identity.BcIdentityManager;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.rendering.ClipRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.ColorUtils;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Pure 2D Aura Icon-Only Sidebar (No text labels, no bottom status box, scrollbar/wheel scrolling only).
 */
public final class GlassSidebar {
    public enum SidebarTab {
        LIVESTREAM("LiveStream", "CLIENT", null, "REPLAY"),
        HOME("All Modules", "OVERVIEW", null, "DASHBOARD"),
        HUD("HUD", "MODULES", ModuleCategory.HUD, "HUD"),
        COMBAT("PvP & Combat", "MODULES", ModuleCategory.COMBAT, "COMBAT"),
        VISUAL("Visuals", "MODULES", ModuleCategory.VISUAL, "VISUAL"),
        INVENTORY("Inventory", "MODULES", ModuleCategory.INVENTORY, "INVENTORY"),
        MOVEMENT("Movement", "MODULES", ModuleCategory.MOVEMENT, "MOVEMENT"),
        WORLD("World", "MODULES", ModuleCategory.WORLD, "WORLD"),
        CHAT("Chat", "MODULES", ModuleCategory.CHAT, "CHAT"),
        AUDIO("Audio", "MODULES", ModuleCategory.AUDIO, "AUDIO"),
        PERFORMANCE("Performance", "MODULES", ModuleCategory.PERFORMANCE, "PERFORMANCE"),
        UTILITY("Utility", "MODULES", ModuleCategory.UTILITY, "UTILITY"),
        SETTINGS("Settings", "CLIENT", null, "SETTINGS"),
        ABOUT("About", "CLIENT", null, "ABOUT");

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
    private final Animation activeIndicatorY = new Animation(0.0f, 150L, Easing.EASE_OUT_CUBIC);
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
        GlassRenderer.drawGlassPanel(context, x, y, width, height, false, false, false);

        // Top BC Emblem Icon centered in the icon dock
        int logoSize = 22;
        BcIdentityManager.drawIdentityIcon(context, x + (width - logoSize) / 2, y + 3, logoSize, logoSize);
        GlassRenderer.drawHorizontalDivider(context, x + 5, y + 25, width - 10);

        int listY = y + 29;
        int listHeight = Math.max(30, height - 33);
        int btnSize = 20;
        int btnStep = 23;

        scrollContainer.tickAndInterpolate();
        int totalContentHeight = SidebarTab.values().length * btnStep + 4;
        scrollContainer.updateDimensions(listHeight, totalContentHeight);

        ClipRenderer.renderClipped(context, x + 1, listY, width - 2, listHeight, () -> {
            int drawY = Math.round(listY - scrollContainer.getScrollOffset()) + 2;
            int btnX = x + (width - btnSize) / 2 - 1;

            for (SidebarTab tab : SidebarTab.values()) {
                boolean active = (tab == selectedTab);
                boolean hovered = MathUtils.isHovered(mouseX, mouseY, btnX, drawY, btnSize, btnSize)
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
                    int fill = active ? 0x33FFFFFF : 0x16FFFFFF;
                    GlassRenderer.drawCustomGlassPanel(context, btnX, drawY, btnSize, btnSize, fill, hovered, active, true);
                }

                int iconCol = active ? 0xFFFFFFFF : (hovered ? theme.getTextPrimary() : theme.getTextMuted());
                GlassRenderer.drawVectorIcon(context, btnX + (btnSize - 10) / 2, drawY + (btnSize - 10) / 2, 10, tab.getIconKey(), iconCol);

                drawY += btnStep;
            }

            int indY = Math.round(activeIndicatorY.getValue());
            context.fill(x + 2, indY + 4, x + 4, indY + btnSize - 4, 0xFFFFFFFF);
        });

        // Right-side scrollbar line (supports left-click drag)
        scrollContainer.renderScrollbar(context, x + width - 3, listY, 2, listHeight, mouseX, mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height, Runnable onTabChanged) {
        int listY = y + 29;
        int listHeight = Math.max(30, height - 33);

        // 1. Check right-side scrollbar click/drag first
        if (scrollContainer.onMouseClicked(mouseX, mouseY, button, x + width - 4, listY, 4, listHeight)) {
            return true;
        }

        // 2. Check icon button clicks
        if (button == 0 && MathUtils.isHovered(mouseX, mouseY, x + 1, listY, width - 5, listHeight)) {
            int btnSize = 20;
            int btnStep = 23;
            int drawY = Math.round(listY - scrollContainer.getScrollOffset()) + 2;
            int btnX = x + (width - btnSize) / 2 - 1;

            for (SidebarTab tab : SidebarTab.values()) {
                if (MathUtils.isHovered(mouseX, mouseY, btnX, drawY, btnSize, btnSize)) {
                    this.selectedTab = tab;
                    if (onTabChanged != null) {
                        onTabChanged.run();
                    }
                    return true;
                }
                drawY += btnStep;
            }
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, int height) {
        int listHeight = Math.max(30, height - 33);
        return scrollContainer.onMouseDragged(mouseX, mouseY, button, listHeight);
    }

    public void mouseReleased(int button) {
        scrollContainer.onMouseReleased(button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount, int x, int y, int width, int height) {
        int listY = y + 29;
        int listHeight = Math.max(30, height - 33);
        return scrollContainer.onMouseScrolled(mouseX, mouseY, verticalAmount, x, listY, width, listHeight);
    }

    public SidebarTab getHoveredTab(double mouseX, double mouseY, int x, int y, int width, int height) { int listY = y + 29; int listHeight = Math.max(30, height - 33); if (!MathUtils.isHovered(mouseX, mouseY, x + 1, listY, width - 5, listHeight)) return null; int btnSize = 20, btnStep = 23; int drawY = Math.round(listY - scrollContainer.getScrollOffset()) + 2; int btnX = x + (width - btnSize) / 2 - 1; for (SidebarTab tab : SidebarTab.values()) { if (MathUtils.isHovered(mouseX, mouseY, btnX, drawY, btnSize, btnSize)) return tab; drawY += btnStep; } return null; }
    public SidebarTab getSelectedTab() { return selectedTab; }
    public void setSelectedTab(SidebarTab selectedTab) {
        if (selectedTab != null) {
            this.selectedTab = selectedTab;
        }
    }
}
