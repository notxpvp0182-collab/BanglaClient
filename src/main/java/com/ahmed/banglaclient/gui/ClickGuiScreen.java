package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.gui.components.GlassSidebar;
import com.ahmed.banglaclient.gui.components.GlassTopBar;
import com.ahmed.banglaclient.gui.components.ModuleCardWidget;
import com.ahmed.banglaclient.gui.components.SmoothScrollContainer;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.ModuleManager;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.ClipRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Primary application-style Click GUI for BANGLA CLIENT.
 * Replicates the visual hierarchy, KPI dashboard cards, sidebar navigation, search bar,
 * and Liquid Glass surface quality of the supplied visual reference image.
 */
public final class ClickGuiScreen extends Screen {
    private final GlassSidebar sidebar = new GlassSidebar();
    private final GlassTopBar topBar = new GlassTopBar();
    private final SmoothScrollContainer mainScroll = new SmoothScrollContainer();
    private final Animation openAnimation = new Animation(0.0f, 200L, Easing.EASE_OUT_CUBIC);

    private final Map<Module, ModuleCardWidget> cardWidgets = new LinkedHashMap<>();
    private List<Module> filteredModules = new ArrayList<>();

    private int windowX;
    private int windowY;
    private int windowWidth;
    private int windowHeight;
    private final boolean autoFocusSearch;

    public ClickGuiScreen(boolean autoFocusSearch) {
        super(Text.literal(BanglaClient.CLIENT_NAME));
        this.autoFocusSearch = autoFocusSearch;
    }

    public ClickGuiScreen() {
        this(false);
    }

    
    protected void init() {
        this.windowWidth = Math.min(720, Math.max(460, this.width - 40));
        this.windowHeight = Math.min(440, Math.max(280, this.height - 36));
        this.windowX = (this.width - this.windowWidth) / 2;
        this.windowY = (this.height - this.windowHeight) / 2;

        this.openAnimation.snapTo(0.0f);
        this.openAnimation.animateTo(1.0f);

        if (autoFocusSearch) {
            this.topBar.setSearchFocused(true);
        }

        rebuildFilteredModules();
    }

    private void rebuildFilteredModules() {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getModuleManager() == null) {
            return;
        }
        ModuleManager mm = bangla.getModuleManager();
        for (Module m : mm.getAllModules()) {
            cardWidgets.computeIfAbsent(m, ModuleCardWidget::new);
        }

        GlassSidebar.SidebarTab tab = sidebar.getSelectedTab();
        ModuleCategory categoryFilter = tab.getModuleCategory();
        if (!topBar.getSearchQuery().isBlank()) {
            categoryFilter = null;
        }

        this.filteredModules = mm.searchModules(
                topBar.getSearchQuery(),
                categoryFilter,
                topBar.isFilterOnlyEnabled(),
                topBar.isFilterOnlyFavorites()
        );
    }

    
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float openProgress = openAnimation.getValue();
        BlurRenderer.renderScreenBackdrop(context, this.width, this.height, openProgress);

        GlassRenderer.drawGlassPanel(context, windowX, windowY, windowWidth, windowHeight, false, false, false);

        int sidebarW = ThemeTokens.SIDEBAR_WIDTH;
        int topBarH = ThemeTokens.TOPBAR_HEIGHT;
        int mainX = windowX + sidebarW + 6;
        int mainY = windowY + topBarH + 6;
        int mainW = windowWidth - sidebarW - 10;
        int mainH = windowHeight - topBarH - 10;

        sidebar.render(context, textRenderer, windowX + 4, windowY + 4, sidebarW - 2, windowHeight - 8, mouseX, mouseY);
        topBar.render(context, textRenderer, mainX, windowY + 4, mainW, topBarH - 2, mouseX, mouseY);

        GlassSidebar.SidebarTab activeTab = sidebar.getSelectedTab();
        if (!topBar.getSearchQuery().isBlank() || activeTab.getModuleCategory() != null) {
            renderModulesGridView(context, mainX, mainY, mainW, mainH, mouseX, mouseY);
        } else if (activeTab == GlassSidebar.SidebarTab.HOME) {
            renderHomeDashboardView(context, mainX, mainY, mainW, mainH, mouseX, mouseY);
        } else if (activeTab == GlassSidebar.SidebarTab.THEMES) {
            renderThemesView(context, mainX, mainY, mainW, mainH, mouseX, mouseY);
        } else if (activeTab == GlassSidebar.SidebarTab.SETTINGS) {
            renderClientSettingsView(context, mainX, mainY, mainW, mainH, mouseX, mouseY);
        } else if (activeTab == GlassSidebar.SidebarTab.ABOUT) {
            renderAboutView(context, mainX, mainY, mainW, mainH);
        }
    }

    private void renderHomeDashboardView(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : theme.getAccentPrimary();

        int kpiGap = 6;
        int kpiW = (width - (kpiGap * 3)) / 4;
        int kpiH = 54;

        int fps = this.client != null ? this.client.getCurrentFps() : 0;
        ModuleManager mm = BanglaClient.getInstance().getModuleManager();
        int enabledCount = mm.getEnabledModuleCount();
        int totalCount = mm.getTotalModuleCount();

        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024L * 1024L);
        long maxMb = rt.maxMemory() / (1024L * 1024L);
        int memPct = maxMb > 0 ? (int) ((usedMb * 100L) / maxMb) : 0;

        String uptime = BanglaClient.getInstance().getSessionTracker().formatClientUptime();

        renderKpiCard(context, x, y, kpiW, kpiH, "Client FPS", fps + " FPS", "Smooth Frame Pacing", ThemeTokens.STATUS_SUCCESS);
        renderKpiCard(context, x + (kpiW + kpiGap), y, kpiW, kpiH, "Active Modules", enabledCount + " / " + totalCount, "Modules Enabled", accent);
        renderKpiCard(context, x + (kpiW + kpiGap) * 2, y, kpiW, kpiH, "JVM Heap", usedMb + " MB", memPct + "% Allocated", ThemeTokens.STATUS_WARNING);
        renderKpiCard(context, x + (kpiW + kpiGap) * 3, y, kpiW, kpiH, "Session Time", uptime, "Profile: " + BanglaClient.getInstance().getProfileManager().getActiveProfileName(), accent);

        int gridY = y + kpiH + 8;
        int gridH = height - kpiH - 8;
        renderModulesGridView(context, x, gridY, width, gridH, mouseX, mouseY);
    }

    private void renderKpiCard(DrawContext context, int x, int y, int w, int h, String label, String bigValue, String subText, int statusColor) {
        Theme theme = GlassRenderer.getTheme();
        GlassRenderer.drawGlassPanel(context, x, y, w, h, true, false, false);
        context.drawTextWithShadow(textRenderer, label, x + 8, y + 7, theme.getTextSecondary());
        context.drawTextWithShadow(textRenderer, bigValue, x + 8, y + 21, theme.getTextPrimary());
        context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(subText, w - 14), x + 8, y + 37, statusColor);
    }

    private void renderModulesGridView(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        Theme theme = GlassRenderer.getTheme();
        boolean compact = topBar.isCompactDensity();

        String heading = !topBar.getSearchQuery().isBlank()
                ? "Search Results (\"" + topBar.getSearchQuery() + "\")"
                : (sidebar.getSelectedTab().getModuleCategory() != null
                        ? sidebar.getSelectedTab().getModuleCategory().getFullTitle()
                        : "All Utility Modules");
        context.drawTextWithShadow(textRenderer, heading + " (" + filteredModules.size() + ")", x + 4, y + 2, theme.getTextPrimary());

        int viewY = y + 16;
        int viewH = Math.max(40, height - 16);

        if (filteredModules.isEmpty()) {
            GlassRenderer.drawGlassPanel(context, x, viewY, width - 6, viewH, false, false, false);
            String emptyMsg = "No modules matched your search or filter criteria.";
            int msgW = textRenderer.getWidth(emptyMsg);
            context.drawTextWithShadow(textRenderer, emptyMsg, x + (width - msgW) / 2, viewY + viewH / 2 - 6, theme.getTextSecondary());
            return;
        }

        int columns = width >= 420 ? 2 : 1;
        int gap = 6;
        int cardW = (width - 10 - (gap * (columns - 1))) / columns;
        int cardH = compact ? 42 : 56;
        int rows = (int) Math.ceil((double) filteredModules.size() / columns);
        int totalContentHeight = rows * (cardH + gap) + 6;

        mainScroll.tickAndInterpolate();
        mainScroll.updateDimensions(viewH, totalContentHeight);

        ClipRenderer.renderClipped(context, x, viewY, width - 8, viewH, () -> {
            int startY = Math.round(viewY - mainScroll.getScrollOffset());
            for (int i = 0; i < filteredModules.size(); i++) {
                Module module = filteredModules.get(i);
                ModuleCardWidget widget = cardWidgets.computeIfAbsent(module, ModuleCardWidget::new);
                int col = i % columns;
                int row = i / columns;
                int cx = x + col * (cardW + gap);
                int cy = startY + row * (cardH + gap);

                if (cy + cardH < viewY || cy > viewY + viewH) {
                    continue;
                }
                widget.render(context, textRenderer, cx, cy, cardW, cardH, mouseX, mouseY, compact);
            }
        });

        mainScroll.renderScrollbar(context, x + width - 5, viewY, 3, viewH, mouseX, mouseY);
    }

    private void renderThemesView(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        Theme active = GlassRenderer.getTheme();
        context.drawTextWithShadow(textRenderer, "Liquid Glass Theme Selection", x + 4, y + 4, active.getTextPrimary());

        List<Theme> themes = BanglaClient.getInstance().getThemeManager().getAllThemes();
        int cardY = y + 20;
        for (Theme t : themes) {
            boolean isSelected = t.getId().equals(active.getId());
            boolean hovered = MathUtils.isHovered(mouseX, mouseY, x + 4, cardY, width - 12, 38);
            GlassRenderer.drawGlassPanel(context, x + 4, cardY, width - 12, 38, true, hovered, isSelected);
            context.fill(x + 12, cardY + 9, x + 32, cardY + 29, t.getAccentPrimary());
            context.drawTextWithShadow(textRenderer, t.getDisplayName(), x + 42, cardY + 10, t.getTextPrimary());
            context.drawTextWithShadow(textRenderer, isSelected ? "Active Theme" : "Click to activate", x + 42, cardY + 22,
                    isSelected ? t.getAccentPrimary() : t.getTextSecondary());
            cardY += 44;
        }
    }

    private void renderClientSettingsView(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary();
        var cfg = BanglaClient.getInstance().getConfigManager();
        context.drawTextWithShadow(textRenderer, "Client Tools, Onboarding & Accessibility", x + 4, y + 4, theme.getTextPrimary());

        int rowY = y + 22;
        // 1. Open Interactive Tutorial Center
        GlassRenderer.drawGlassPanel(context, x + 4, rowY, width - 12, 30, true,
                MathUtils.isHovered(mouseX, mouseY, x + 4, rowY, width - 12, 30), true);
        context.drawTextWithShadow(textRenderer, "Open Interactive Tutorial Center (14-Step Client Guide) →",
                x + 14, rowY + 11, accent);
        rowY += 36;

        // 2. Open Keybind Manager & Conflict Inspector
        GlassRenderer.drawGlassPanel(context, x + 4, rowY, width - 12, 30, true,
                MathUtils.isHovered(mouseX, mouseY, x + 4, rowY, width - 12, 30), false);
        context.drawTextWithShadow(textRenderer, "Open Keybind Manager & Conflict Inspector →",
                x + 14, rowY + 11, theme.getTextPrimary());
        rowY += 36;

        // 3. Open Advanced HUD Editor
        GlassRenderer.drawGlassPanel(context, x + 4, rowY, width - 12, 30, true,
                MathUtils.isHovered(mouseX, mouseY, x + 4, rowY, width - 12, 30), false);
        context.drawTextWithShadow(textRenderer, "Open Interactive HUD Editor (Drag, Resize & Snap Widgets) →",
                x + 14, rowY + 11, theme.getTextPrimary());
        rowY += 36;

        // 4. Reduced Motion Toggle
        GlassRenderer.drawGlassPanel(context, x + 4, rowY, width - 12, 30, true,
                MathUtils.isHovered(mouseX, mouseY, x + 4, rowY, width - 12, 30), cfg.isReducedMotion());
        context.drawTextWithShadow(textRenderer, "Reduced Motion (Instant UI Transitions): " + (cfg.isReducedMotion() ? "ON" : "OFF"),
                x + 14, rowY + 11, theme.getTextPrimary());
        rowY += 36;

        // 5. Custom Main Menu Toggle
        GlassRenderer.drawGlassPanel(context, x + 4, rowY, width - 12, 30, true,
                MathUtils.isHovered(mouseX, mouseY, x + 4, rowY, width - 12, 30), cfg.isCustomMainMenuEnabled());
        context.drawTextWithShadow(textRenderer, "BANGLA CLIENT Custom Main Menu: " + (cfg.isCustomMainMenuEnabled() ? "ENABLED" : "VANILLA"),
                x + 14, rowY + 11, theme.getTextPrimary());
        rowY += 36;

        // 6. Reset All Settings Button
        GlassRenderer.drawGlassPanel(context, x + 4, rowY, width - 12, 30, true,
                MathUtils.isHovered(mouseX, mouseY, x + 4, rowY, width - 12, 30), false);
        context.drawTextWithShadow(textRenderer, "Reset All Client Configurations to Factory Defaults",
                x + 14, rowY + 11, ThemeTokens.STATUS_ERROR);
    }

    private void renderAboutView(DrawContext context, int x, int y, int width, int height) {
        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary();
        GlassRenderer.drawGlassPanel(context, x + 4, y + 4, width - 12, height - 10, true, false, true);

        int cx = x + 24;
        int cy = y + 26;
        context.drawTextWithShadow(textRenderer, BanglaClient.CLIENT_NAME, cx, cy, accent);
        context.drawTextWithShadow(textRenderer, "Version: " + BanglaClient.CLIENT_VERSION, cx, cy + 16, theme.getTextPrimary());
        context.drawTextWithShadow(textRenderer, "Developed by " + BanglaClient.DEVELOPER, cx, cy + 32, theme.getTextPrimary());
        GlassRenderer.drawHorizontalDivider(context, cx, cy + 48, width - 56);
        context.drawTextWithShadow(textRenderer, "Minecraft Java Edition 1.21.11 (Fabric Utility Client)", cx, cy + 58, theme.getTextSecondary());
        context.drawTextWithShadow(textRenderer, "Design System: Premium Dark Transparent Liquid Glass", cx, cy + 72, theme.getTextSecondary());
        context.drawTextWithShadow(textRenderer, "Legitimate HUD, Visual, Audio, Performance & QoL Suite.", cx, cy + 86, theme.getTextMuted());
    }

    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int sidebarW = ThemeTokens.SIDEBAR_WIDTH;
        int topBarH = ThemeTokens.TOPBAR_HEIGHT;
        int mainX = windowX + sidebarW + 6;
        int mainY = windowY + topBarH + 6;
        int mainW = windowWidth - sidebarW - 10;
        int mainH = windowHeight - topBarH - 10;

        if (sidebar.mouseClicked(mouseX, mouseY, button, windowX + 4, windowY + 4, sidebarW - 2, windowHeight - 8, () -> {
            mainScroll.resetScroll();
            rebuildFilteredModules();
        })) {
            return true;
        }

        if (topBar.mouseClicked(mouseX, mouseY, button, mainX, windowY + 4, mainW, topBarH - 2, () -> {
            mainScroll.resetScroll();
            rebuildFilteredModules();
        })) {
            return true;
        }

        GlassSidebar.SidebarTab activeTab = sidebar.getSelectedTab();
        if (activeTab == GlassSidebar.SidebarTab.THEMES && topBar.getSearchQuery().isBlank()) {
            int cardY = mainY + 20;
            for (Theme t : BanglaClient.getInstance().getThemeManager().getAllThemes()) {
                if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, cardY, mainW - 12, 38)) {
                    BanglaClient.getInstance().getThemeManager().setActiveThemeById(t.getId());
                    return true;
                }
                cardY += 44;
            }
            return false;
        }

        if (activeTab == GlassSidebar.SidebarTab.SETTINGS && topBar.getSearchQuery().isBlank()) {
            var cfg = BanglaClient.getInstance().getConfigManager();
            int rowY = mainY + 22;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 30)) {
                if (this.client != null) {
                    this.client.setScreen(new TutorialCenterScreen(this));
                }
                return true;
            }
            rowY += 36;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 30)) {
                if (this.client != null) {
                    this.client.setScreen(new KeybindManagerScreen(this));
                }
                return true;
            }
            rowY += 36;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 30)) {
                if (this.client != null) {
                    this.client.setScreen(new HudEditorScreen(this));
                }
                return true;
            }
            rowY += 36;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 30)) {
                cfg.setReducedMotion(!cfg.isReducedMotion());
                return true;
            }
            rowY += 36;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 30)) {
                cfg.setCustomMainMenuEnabled(!cfg.isCustomMainMenuEnabled());
                return true;
            }
            rowY += 36;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 30)) {
                cfg.resetAllToDefaults();
                rebuildFilteredModules();
                return true;
            }
            return false;
        }

        int gridY = (activeTab == GlassSidebar.SidebarTab.HOME && topBar.getSearchQuery().isBlank()) ? mainY + 62 : mainY;
        int gridH = (activeTab == GlassSidebar.SidebarTab.HOME && topBar.getSearchQuery().isBlank()) ? mainH - 62 : mainH;
        int viewY = gridY + 16;
        int viewH = Math.max(40, gridH - 16);

        if (mainScroll.onMouseClicked(mouseX, mouseY, button, mainX + mainW - 5, viewY, 3, viewH)) {
            return true;
        }

        if (MathUtils.isHovered(mouseX, mouseY, mainX, viewY, mainW - 8, viewH)) {
            boolean compact = topBar.isCompactDensity();
            int columns = mainW >= 420 ? 2 : 1;
            int gap = 6;
            int cardW = (mainW - 10 - (gap * (columns - 1))) / columns;
            int cardH = compact ? 42 : 56;
            int startY = Math.round(viewY - mainScroll.getScrollOffset());

            for (int i = 0; i < filteredModules.size(); i++) {
                Module module = filteredModules.get(i);
                ModuleCardWidget widget = cardWidgets.get(module);
                int col = i % columns;
                int row = i / columns;
                int cx = mainX + col * (cardW + gap);
                int cy = startY + row * (cardH + gap);

                if (widget != null && widget.mouseClicked(mouseX, mouseY, button, cx, cy, cardW, cardH, compact, m -> {
                    if (this.client != null) {
                        this.client.setScreen(new ModuleSettingsScreen(this, m));
                    }
                })) {
                    return true;
                }
            }
        }

        return false;
    }

    
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (sidebar.mouseDragged(mouseX, mouseY, button, windowHeight - 8)) {
            return true;
        }
        int mainH = windowHeight - ThemeTokens.TOPBAR_HEIGHT - 10;
        if (mainScroll.onMouseDragged(mouseX, mouseY, button, mainH - 16)) {
            return true;
        }
        return false;
    }

    
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        sidebar.mouseReleased(button);
        mainScroll.onMouseReleased(button);
        return false;
    }

    
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int sidebarW = ThemeTokens.SIDEBAR_WIDTH;
        int topBarH = ThemeTokens.TOPBAR_HEIGHT;
        int mainX = windowX + sidebarW + 6;
        int mainY = windowY + topBarH + 6;
        int mainW = windowWidth - sidebarW - 10;
        int mainH = windowHeight - topBarH - 10;

        if (sidebar.mouseScrolled(mouseX, mouseY, verticalAmount, windowX + 4, windowY + 4, sidebarW - 2, windowHeight - 8)) {
            return true;
        }
        if (mainScroll.onMouseScrolled(mouseX, mouseY, verticalAmount, mainX, mainY, mainW, mainH)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (topBar.keyPressed(keyCode, scanCode, modifiers, () -> {
            mainScroll.resetScroll();
            rebuildFilteredModules();
        })) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return false;
    }

    
    public boolean charTyped(char chr, int modifiers) {
        if (topBar.charTyped(chr, modifiers, () -> {
            mainScroll.resetScroll();
            rebuildFilteredModules();
        })) {
            return true;
        }
        return false;
    }

    
    public void close() {
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getConfigManager() != null) {
            BanglaClient.getInstance().getConfigManager().saveAll();
        }
        super.close();
    }

    
         public boolean mouseClicked(net.minecraft.client.gui.Click c, boolean d) { return mouseClicked(c.x(), c.y(), c.button()) || super.mouseClicked(c, d); }
     public boolean mouseDragged(net.minecraft.client.gui.Click c, double dx, double dy) { return mouseDragged(c.x(), c.y(), c.button(), dx, dy) || super.mouseDragged(c, dx, dy); }
     public boolean mouseReleased(net.minecraft.client.gui.Click c) { return mouseReleased(c.x(), c.y(), c.button()) || super.mouseReleased(c); }
     public boolean keyPressed(net.minecraft.client.input.KeyInput k) { return keyPressed(k.key(), k.scancode(), k.modifiers()) || super.keyPressed(k); }
     public boolean charTyped(net.minecraft.client.input.CharInput ch) { return charTyped((char) ch.codepoint(), ch.modifiers()) || super.charTyped(ch); }
    public boolean shouldPause() {
        return false;
    }
}
