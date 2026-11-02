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
import com.ahmed.banglaclient.theme.ThemeManager;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ClickGuiScreen extends Screen {
    private final GlassSidebar sidebar = new GlassSidebar();
    private final GlassTopBar topBar = new GlassTopBar();
    private final SmoothScrollContainer mainScroll = new SmoothScrollContainer();
    private final Animation openAnimation = new Animation(0.0f, 180L, Easing.EASE_OUT_CUBIC);

    private final Map<Module, ModuleCardWidget> cardWidgets = new LinkedHashMap<>();
    private List<Module> filteredModules = new ArrayList<>();

    private int windowX, windowY, windowWidth, windowHeight;
    private final boolean autoFocusSearch;
    private int activeCustomSlider = -1;

    public ClickGuiScreen(boolean autoFocusSearch) {
        super(Text.literal(BanglaClient.CLIENT_NAME));
        this.autoFocusSearch = autoFocusSearch;
    }

    public ClickGuiScreen() {
        this(false);
    }

    @Override
    protected void init() {
        this.windowWidth = Math.min(680, Math.max(240, this.width - 16));
        this.windowHeight = Math.min(400, Math.max(170, this.height - 16));
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
        if (bangla == null || bangla.getModuleManager() == null) return;
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

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        BlurRenderer.renderScreenBackdrop(context, this.width, this.height, openAnimation.getValue());
        GlassRenderer.drawGlassPanel(context, windowX, windowY, windowWidth, windowHeight, false, false, false);

        int sidebarW = Math.min(ThemeTokens.SIDEBAR_WIDTH, Math.max(82, this.windowWidth / 4));
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
            renderAboutView(context, mainX, mainY, mainW, mainH, mouseX, mouseY);
        }
    }

    private void renderHomeDashboardView(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary();
        int kpiGap = 4;
        int kpiW = (width - (kpiGap * 3)) / 4;
        int kpiH = 44;

        int fps = this.client != null ? this.client.getCurrentFps() : 0;
        ModuleManager mm = BanglaClient.getInstance().getModuleManager();
        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024L * 1024L);
        long maxMb = Math.max(1L, rt.maxMemory() / (1024L * 1024L));
        int memPct = (int) ((usedMb * 100L) / maxMb);
        String uptime = BanglaClient.getInstance().getSessionTracker().formatClientUptime();

        renderKpiCard(context, x, y, kpiW, kpiH, "Client FPS", fps + " FPS", "Smooth Pacing", ThemeTokens.STATUS_SUCCESS);
        renderKpiCard(context, x + (kpiW + kpiGap), y, kpiW, kpiH, "Modules", mm.getEnabledModuleCount() + "/" + mm.getTotalModuleCount(), "Active", accent);
        renderKpiCard(context, x + (kpiW + kpiGap) * 2, y, kpiW, kpiH, "JVM Heap", usedMb + " MB", memPct + "% Used", ThemeTokens.STATUS_WARNING);
        renderKpiCard(context, x + (kpiW + kpiGap) * 3, y, kpiW, kpiH, "Session", uptime, BanglaClient.getInstance().getProfileManager().getActiveProfileName(), accent);

        renderModulesGridView(context, x, y + kpiH + 6, width, height - kpiH - 6, mouseX, mouseY);
    }

    private void renderKpiCard(DrawContext context, int x, int y, int w, int h, String label, String bigValue, String subText, int statusColor) {
        Theme theme = GlassRenderer.getTheme();
        GlassRenderer.drawGlassPanel(context, x, y, w, h, true, false, false);
        context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(label, w - 8), x + 4, y + 4, theme.getTextSecondary());
        context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(bigValue, w - 8), x + 4, y + 16, theme.getTextPrimary());
        context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(subText, w - 8), x + 4, y + 29, statusColor);
    }

    private void renderModulesGridView(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        Theme theme = GlassRenderer.getTheme();
        boolean compact = topBar.isCompactDensity();
        String heading = !topBar.getSearchQuery().isBlank() ? ("Search: " + topBar.getSearchQuery()) : (sidebar.getSelectedTab().getModuleCategory() != null ? sidebar.getSelectedTab().getModuleCategory().getFullTitle() : "All Utility Modules");
        context.drawTextWithShadow(textRenderer, heading + " (" + filteredModules.size() + ")", x + 2, y + 2, theme.getTextPrimary());

        int viewY = y + 14;
        int viewH = Math.max(30, height - 14);
        int columns = width >= 260 ? 2 : 1;
        int gap = 5;
        int cardW = (width - 8 - (gap * (columns - 1))) / columns;
        int cardH = compact ? 40 : 52;
        int rows = (int) Math.ceil((double) filteredModules.size() / columns);
        int totalContentHeight = rows * (cardH + gap) + 6;

        mainScroll.tickAndInterpolate();
        mainScroll.updateDimensions(viewH, totalContentHeight);

        ClipRenderer.renderClipped(context, x, viewY, width - 6, viewH, () -> {
            int startY = Math.round(viewY - mainScroll.getScrollOffset());
            for (int i = 0; i < filteredModules.size(); i++) {
                Module module = filteredModules.get(i);
                ModuleCardWidget widget = cardWidgets.computeIfAbsent(module, ModuleCardWidget::new);
                int col = i % columns;
                int row = i / columns;
                int cx = x + col * (cardW + gap);
                int cy = startY + row * (cardH + gap);
                if (cy + cardH < viewY || cy > viewY + viewH) continue;
                widget.render(context, textRenderer, cx, cy, cardW, cardH, mouseX, mouseY, compact);
            }
        });
        mainScroll.renderScrollbar(context, x + width - 4, viewY, 3, viewH, mouseX, mouseY);
    }

    private void renderThemesView(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        Theme active = GlassRenderer.getTheme();
        ThemeManager tm = BanglaClient.getInstance().getThemeManager();
        int accent = tm.getEffectiveAccentPrimary();

        if (activeCustomSlider >= 0) {
            updateCustomThemeFromMouse(mouseX, x + 8, width - 24);
        }

        mainScroll.tickAndInterpolate();
        mainScroll.updateDimensions(height, 420);

        ClipRenderer.renderClipped(context, x, y, width - 6, height, () -> {
            int curY = Math.round(y - mainScroll.getScrollOffset()) + 4;
            context.drawTextWithShadow(textRenderer, "Theme Selection (Red Default & Custom Theme)", x + 4, curY, active.getTextPrimary());
            curY += 14;

            for (Theme t : tm.getAllThemes()) {
                boolean isSelected = t.getId().equals(active.getId());
                boolean hovered = MathUtils.isHovered(mouseX, mouseY, x + 4, curY, width - 12, 30);
                GlassRenderer.drawGlassPanel(context, x + 4, curY, width - 12, 30, true, hovered, isSelected);
                context.fill(x + 10, curY + 7, x + 26, curY + 23, t.getAccentPrimary());
                context.drawTextWithShadow(textRenderer, t.getDisplayName(), x + 34, curY + 6, t.getTextPrimary());
                context.drawTextWithShadow(textRenderer, isSelected ? "Active Theme" : "Click to activate", x + 34, curY + 17,
                        isSelected ? t.getAccentPrimary() : t.getTextSecondary());
                curY += 34;
            }

            curY += 4;
            context.drawTextWithShadow(textRenderer, "Custom Theme Live Editor:", x + 4, curY, accent);
            curY += 14;

            String[] labels = {
                    "Primary Accent Hue",
                    "Secondary Accent Hue",
                    "Glass Transparency (Opacity)",
                    "Glass Surface Brightness",
                    "Glass Border Intensity",
                    "Primary Text Brightness"
            };
            float[] vals = {
                    tm.getCustomPrimaryHue(),
                    tm.getCustomSecondaryHue(),
                    (tm.getCustomGlassAlpha() - 0.06f) / 0.69f,
                    tm.getCustomTintBrightness() / 0.50f,
                    (tm.getCustomBorderAlpha() - 0.10f) / 0.80f,
                    (tm.getCustomTextBrightness() - 0.70f) / 0.30f
            };

            int barX = x + 8, barW = width - 24;
            for (int i = 0; i < labels.length; i++) {
                GlassRenderer.drawGlassPanel(context, x + 4, curY, width - 12, 22, false, false, false);
                context.drawTextWithShadow(textRenderer, labels[i], barX, curY + 3, active.getTextPrimary());
                context.fill(barX, curY + 14, barX + barW, curY + 18, 0x33FFFFFF);
                int fw = Math.round(MathUtils.clamp(vals[i], 0f, 1f) * barW);
                context.fill(barX, curY + 14, barX + fw, curY + 18, accent);
                curY += 25;
            }

            // [Save Custom Theme] | [Restore Default Red Theme]
            int halfW = (width - 18) / 2;
            GlassRenderer.drawGlassPanel(context, x + 4, curY, halfW, 20, true, MathUtils.isHovered(mouseX, mouseY, x + 4, curY, halfW, 20), true);
            context.drawTextWithShadow(textRenderer, "Save Custom Theme", x + 10, curY + 6, accent);

            int rx = x + 4 + halfW + 6;
            GlassRenderer.drawGlassPanel(context, rx, curY, halfW, 20, true, MathUtils.isHovered(mouseX, mouseY, rx, curY, halfW, 20), false);
            context.drawTextWithShadow(textRenderer, "Reset to Red Theme", rx + 8, curY + 6, active.getTextPrimary());
        });
        mainScroll.renderScrollbar(context, x + width - 4, y, 3, height, mouseX, mouseY);
    }

    private void updateCustomThemeFromMouse(double mouseX, int barX, int barW) {
        if (barW <= 0) return;
        float norm = MathUtils.clamp((float) ((mouseX - barX) / (double) barW), 0.0f, 1.0f);
        ThemeManager tm = BanglaClient.getInstance().getThemeManager();
        tm.setActiveThemeById("custom_theme");
        switch (activeCustomSlider) {
            case 0 -> tm.setCustomPrimaryHue(norm);
            case 1 -> tm.setCustomSecondaryHue(norm);
            case 2 -> tm.setCustomGlassAlpha(0.06f + norm * 0.69f);
            case 3 -> tm.setCustomTintBrightness(norm * 0.50f);
            case 4 -> tm.setCustomBorderAlpha(0.10f + norm * 0.80f);
            case 5 -> tm.setCustomTextBrightness(0.70f + norm * 0.30f);
        }
    }

    private void renderClientSettingsView(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary();
        var cfg = BanglaClient.getInstance().getConfigManager();

        mainScroll.tickAndInterpolate();
        mainScroll.updateDimensions(height, 240);

        ClipRenderer.renderClipped(context, x, y, width - 6, height, () -> {
            int rowY = Math.round(y - mainScroll.getScrollOffset()) + 4;
            context.drawTextWithShadow(textRenderer, "Client Tools, Onboarding & Accessibility", x + 4, rowY, theme.getTextPrimary());
            rowY += 16;

            String[] rows = {
                    "Open Interactive Tutorial Center (14-Step Guide) →",
                    "Open Keybind Manager & Conflict Inspector →",
                    "Open Interactive HUD Editor (Drag, Resize & Snap) →",
                    "Reduced Motion (Instant UI Transitions): " + (cfg.isReducedMotion() ? "ON" : "OFF"),
                    "BANGLA CLIENT Custom Main Menu: " + (cfg.isCustomMainMenuEnabled() ? "ENABLED" : "VANILLA"),
                    "Reset All Client Configurations to Factory Defaults"
            };

            for (int i = 0; i < rows.length; i++) {
                boolean hov = MathUtils.isHovered(mouseX, mouseY, x + 4, rowY, width - 12, 28);
                GlassRenderer.drawGlassPanel(context, x + 4, rowY, width - 12, 28, true, hov, i == 0 || hov);
                int col = (i == 0) ? accent : (i == 5 ? ThemeTokens.STATUS_ERROR : theme.getTextPrimary());
                context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(rows[i], width - 24), x + 10, rowY + 10, col);
                rowY += 32;
            }
        });
        mainScroll.renderScrollbar(context, x + width - 4, y, 3, height, mouseX, mouseY);
    }

    private void renderAboutView(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary();

        mainScroll.tickAndInterpolate();
        mainScroll.updateDimensions(height, 165);

        ClipRenderer.renderClipped(context, x, y, width - 6, height, () -> {
            int cy = Math.round(y - mainScroll.getScrollOffset()) + 6;
            GlassRenderer.drawGlassPanel(context, x + 4, cy, width - 12, 150, true, false, true);
            int cx = x + 16;
            cy += 14;
            context.drawTextWithShadow(textRenderer, BanglaClient.CLIENT_NAME, cx, cy, accent);
            context.drawTextWithShadow(textRenderer, "Version: " + BanglaClient.CLIENT_VERSION, cx, cy + 14, theme.getTextPrimary());
            context.drawTextWithShadow(textRenderer, "Developed by " + BanglaClient.DEVELOPER, cx, cy + 28, theme.getTextPrimary());
            GlassRenderer.drawHorizontalDivider(context, cx, cy + 42, width - 36);
            context.drawTextWithShadow(textRenderer, "Minecraft Java Edition 1.21.11 (Fabric Utility Client)", cx, cy + 50, theme.getTextSecondary());
            context.drawTextWithShadow(textRenderer, "Design System: Genuine Transparent Liquid Glass", cx, cy + 63, theme.getTextSecondary());
            context.drawTextWithShadow(textRenderer, "Bangla SMP — Server Address: play.banglasmp.net", cx, cy + 80, accent);

            int btnY = cy + 96;
            boolean copyHov = MathUtils.isHovered(mouseX, mouseY, cx, btnY, 128, 18);
            GlassRenderer.drawGlassPanel(context, cx, btnY, 128, 18, true, copyHov, true);
            context.drawTextWithShadow(textRenderer, "Copy Server IP", cx + 22, btnY + 5, copyHov ? accent : theme.getTextPrimary());
        });
        mainScroll.renderScrollbar(context, x + width - 4, y, 3, height, mouseX, mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int sidebarW = Math.min(ThemeTokens.SIDEBAR_WIDTH, Math.max(82, this.windowWidth / 4));
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

        if (mainScroll.onMouseClicked(mouseX, mouseY, button, mainX + mainW - 4, mainY, 4, mainH)) {
            return true;
        }

        GlassSidebar.SidebarTab activeTab = sidebar.getSelectedTab();

        // STRICT INPUT ISOLATION FOR THEMES, SETTINGS, AND ABOUT (Prevents Click-Through to Modules 100%)
        if (topBar.getSearchQuery().isBlank() && activeTab == GlassSidebar.SidebarTab.THEMES) {
            int curY = Math.round(mainY - mainScroll.getScrollOffset()) + 18;
            ThemeManager tm = BanglaClient.getInstance().getThemeManager();
            for (Theme t : tm.getAllThemes()) {
                if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, curY, mainW - 12, 30)) {
                    tm.setActiveThemeById(t.getId());
                    return true;
                }
                curY += 34;
            }
            curY += 18;
            for (int i = 0; i < 6; i++) {
                if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, curY, mainW - 12, 22)) {
                    activeCustomSlider = i;
                    updateCustomThemeFromMouse(mouseX, mainX + 8, mainW - 24);
                    return true;
                }
                curY += 25;
            }
            int halfW = (mainW - 18) / 2;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, curY, halfW, 20)) {
                BanglaClient.getInstance().getConfigManager().saveThemeConfig();
                BanglaClient.getInstance().getNotificationManager().pushSuccess("Custom Theme", "Saved Custom Theme settings.");
                return true;
            }
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 10 + halfW, curY, halfW, 20)) {
                tm.resetCustomThemeToRedDefaults();
                tm.setActiveThemeById("red_theme");
                BanglaClient.getInstance().getConfigManager().saveThemeConfig();
                return true;
            }
            return true;
        }

        if (topBar.getSearchQuery().isBlank() && activeTab == GlassSidebar.SidebarTab.SETTINGS) {
            var cfg = BanglaClient.getInstance().getConfigManager();
            int rowY = Math.round(mainY - mainScroll.getScrollOffset()) + 20;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 28)) {
                if (this.client != null) this.client.setScreen(new TutorialCenterScreen(this));
                return true;
            }
            rowY += 32;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 28)) {
                if (this.client != null) this.client.setScreen(new KeybindManagerScreen(this));
                return true;
            }
            rowY += 32;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 28)) {
                if (this.client != null) this.client.setScreen(new HudEditorScreen(this));
                return true;
            }
            rowY += 32;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 28)) {
                cfg.setReducedMotion(!cfg.isReducedMotion());
                return true;
            }
            rowY += 32;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 28)) {
                cfg.setCustomMainMenuEnabled(!cfg.isCustomMainMenuEnabled());
                return true;
            }
            rowY += 32;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 28)) {
                cfg.resetAllToDefaults();
                rebuildFilteredModules();
                return true;
            }
            return true;
        }

        if (topBar.getSearchQuery().isBlank() && activeTab == GlassSidebar.SidebarTab.ABOUT) {
            int cy = Math.round(mainY - mainScroll.getScrollOffset()) + 20;
            int btnY = cy + 96;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 16, btnY, 128, 18)) {
                if (this.client != null && this.client.keyboard != null) {
                    this.client.keyboard.setClipboard("play.banglasmp.net");
                    BanglaClient.getInstance().getNotificationManager().pushSuccess("Bangla SMP", "Copied play.banglasmp.net");
                }
            }
            return true;
        }

        // Module grid clicks (ONLY reached when on HOME or a Module Category tab)
        int gridY = (activeTab == GlassSidebar.SidebarTab.HOME && topBar.getSearchQuery().isBlank()) ? mainY + 50 : mainY;
        int gridH = (activeTab == GlassSidebar.SidebarTab.HOME && topBar.getSearchQuery().isBlank()) ? mainH - 50 : mainH;
        int viewY = gridY + 14;
        int viewH = Math.max(30, gridH - 14);

        if (MathUtils.isHovered(mouseX, mouseY, mainX, viewY, mainW - 6, viewH)) {
            boolean compact = topBar.isCompactDensity();
            int columns = mainW >= 260 ? 2 : 1;
            int gap = 5;
            int cardW = (mainW - 8 - (gap * (columns - 1))) / columns;
            int cardH = compact ? 40 : 52;
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
        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (activeCustomSlider >= 0) {
            int sidebarW = Math.min(ThemeTokens.SIDEBAR_WIDTH, Math.max(82, this.windowWidth / 4));
            int mainX = windowX + sidebarW + 6;
            int mainW = windowWidth - sidebarW - 10;
            updateCustomThemeFromMouse(mouseX, mainX + 8, mainW - 24);
            return true;
        }
        if (sidebar.mouseDragged(mouseX, mouseY, button, windowHeight - 8)) {
            return true;
        }
        int mainH = windowHeight - ThemeTokens.TOPBAR_HEIGHT - 10;
        if (mainScroll.onMouseDragged(mouseX, mouseY, button, mainH - 14)) {
            return true;
        }
        // Touch-drag scrolling support for mobile screens
        int sidebarW = Math.min(ThemeTokens.SIDEBAR_WIDTH, Math.max(82, this.windowWidth / 4));
        if (mouseX > windowX + sidebarW) {
            mainScroll.onMouseScrolled(mouseX, mouseY, deltaY * 0.12, 0, 0, this.width, this.height);
        } else {
            sidebar.mouseScrolled(mouseX, mouseY, deltaY * 0.12, 0, 0, this.width, this.height);
        }
        return true;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (activeCustomSlider >= 0) {
            activeCustomSlider = -1;
            BanglaClient.getInstance().getConfigManager().saveThemeConfig();
        }
        sidebar.mouseReleased(button);
        mainScroll.onMouseReleased(button);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int sidebarW = Math.min(ThemeTokens.SIDEBAR_WIDTH, Math.max(82, this.windowWidth / 4));
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
        return true;
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
        return topBar.charTyped(chr, modifiers, () -> {
            mainScroll.resetScroll();
            rebuildFilteredModules();
        });
    }

    @Override public boolean mouseClicked(Click c, boolean d) { return mouseClicked(c.x(), c.y(), c.button()); }
    @Override public boolean mouseDragged(Click c, double dx, double dy) { return mouseDragged(c.x(), c.y(), c.button(), dx, dy); }
    @Override public boolean mouseReleased(Click c) { return mouseReleased(c.x(), c.y(), c.button()); }
    @Override public boolean keyPressed(KeyInput k) { return keyPressed(k.key(), k.scancode(), k.modifiers()) || super.keyPressed(k); }
    @Override public boolean charTyped(CharInput ch) { return charTyped((char) ch.codepoint(), ch.modifiers()) || super.charTyped(ch); }

    @Override
    public void close() {
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getConfigManager() != null) {
            BanglaClient.getInstance().getConfigManager().saveAll();
        }
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
