package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.account.AccountManager;
import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.cosmetics.SkinCapeManager;
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
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BANGLA CLIENT Main ClickGUI:
 * Structurally modeled on PYES Client (Sidebar + TopBar + Multi-Column Card Grid + Bottom Status Bar)
 * Visually styled on AURA Client (Dark Frosted Charcoal Glass, Thin Silver Borders, Staggered Cascade Motion).
 */
public final class ClickGuiScreen extends Screen {
    private final GlassSidebar sidebar = new GlassSidebar();
    private final GlassTopBar topBar = new GlassTopBar();
    private final SmoothScrollContainer mainScroll = new SmoothScrollContainer();
    private final Animation openAnimation = new Animation(0.0f, 190L, Easing.EASE_OUT_QUART);

    private final Map<Module, ModuleCardWidget> cardWidgets = new LinkedHashMap<>();
    private List<Module> filteredModules = new ArrayList<>();

    private int windowX, windowY, windowWidth, windowHeight;
    private final boolean autoFocusSearch;
    private int activeCustomSlider = -1;
    private long lastCategorySwitchMillis = System.currentTimeMillis();

    public ClickGuiScreen(boolean autoFocusSearch) {
        super(Text.literal(BanglaClient.CLIENT_NAME));
        this.autoFocusSearch = autoFocusSearch;
    }

    public ClickGuiScreen() {
        this(false);
    }

    @Override
    protected void init() {
        this.windowWidth = Math.min(760, Math.max(300, this.width - 20));
        this.windowHeight = Math.min(430, Math.max(200, this.height - 20));
        this.windowX = (this.width - this.windowWidth) / 2;
        this.windowY = (this.height - this.windowHeight) / 2;

        this.openAnimation.snapTo(0.0f);
        this.openAnimation.animateTo(1.0f);
        this.lastCategorySwitchMillis = System.currentTimeMillis();

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
        float openVal = openAnimation.getValue();
        BlurRenderer.renderScreenBackdrop(context, this.width, this.height, openVal);

        // Subtle Aura-style scale-in transition (96.5% -> 100%)
        Matrix3x2fStack matrices = context.getMatrices();
        float scale = 0.965f + (0.035f * openVal);
        boolean scaling = Math.abs(scale - 1.0f) > 0.001f;
        if (scaling) {
            float cx = this.width * 0.5f;
            float cy = this.height * 0.5f;
            matrices.pushMatrix();
            matrices.translate(cx, cy);
            matrices.scale(scale, scale);
            matrices.translate(-cx, -cy);
        }

        try {
            GlassRenderer.drawGlassPanel(context, windowX, windowY, windowWidth, windowHeight, false, false, false);

            int sidebarW = Math.min(ThemeTokens.SIDEBAR_WIDTH, Math.max(92, this.windowWidth / 5));
            int topBarH = ThemeTokens.TOPBAR_HEIGHT;
            int footerH = ThemeTokens.FOOTER_HEIGHT;

            int mainX = windowX + sidebarW + 6;
            int mainY = windowY + topBarH + 6;
            int mainW = windowWidth - sidebarW - 10;
            int mainH = windowHeight - topBarH - footerH - 10;

            sidebar.render(context, textRenderer, windowX + 4, windowY + 4, sidebarW - 2, windowHeight - footerH - 6, mouseX, mouseY);
            topBar.render(context, textRenderer, mainX, windowY + 4, mainW, topBarH - 2, mouseX, mouseY);

            GlassSidebar.SidebarTab activeTab = sidebar.getSelectedTab();
            if (!topBar.getSearchQuery().isBlank() || activeTab.getModuleCategory() != null || activeTab == GlassSidebar.SidebarTab.HOME) {
                renderModulesGridView(context, mainX, mainY, mainW, mainH, mouseX, mouseY);
            } else if (activeTab == GlassSidebar.SidebarTab.THEMES) {
                renderThemesView(context, mainX, mainY, mainW, mainH, mouseX, mouseY);
            } else if (activeTab == GlassSidebar.SidebarTab.SETTINGS) {
                renderClientSettingsView(context, mainX, mainY, mainW, mainH, mouseX, mouseY);
            } else if (activeTab == GlassSidebar.SidebarTab.ABOUT) {
                renderAboutView(context, mainX, mainY, mainW, mainH, mouseX, mouseY);
            }

            renderBottomStatusBar(context, windowX + 4, windowY + windowHeight - footerH - 2, windowWidth - 8, footerH, mouseX, mouseY);
        } finally {
            if (scaling) {
                matrices.popMatrix();
            }
        }
    }

    private void renderBottomStatusBar(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
        Theme theme = GlassRenderer.getTheme();
        GlassRenderer.drawGlassPanel(context, x, y, width, height, false, false, false);

        ModuleManager mm = BanglaClient.getInstance().getModuleManager();
        int fps = this.client != null ? this.client.getCurrentFps() : 60;
        String leftStatus = "STATUS: CONNECTED  •  " + mm.getEnabledModuleCount() + "/" + mm.getTotalModuleCount() + " ACTIVE  •  " + fps + " FPS";
        context.drawTextWithShadow(textRenderer, leftStatus, x + 8, y + (height - 8) / 2, theme.getTextSecondary());

        String centerBrand = BanglaClient.CLIENT_NAME;
        int cbW = textRenderer.getWidth(centerBrand);
        if (width > 360) {
            context.drawTextWithShadow(textRenderer, centerBrand, x + (width - cbW) / 2, y + (height - 8) / 2, theme.getTextMuted());
        }

        // Quick Aura-style bottom dock shortcuts: [HUD Editor] [Wardrobe] [Accounts]
        String rightVer = "VERSION: " + BanglaClient.CLIENT_VERSION;
        int rvW = textRenderer.getWidth(rightVer);
        context.drawTextWithShadow(textRenderer, rightVer, x + width - rvW - 8, y + (height - 8) / 2, theme.getTextSecondary());
    }

    private void renderModulesGridView(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        boolean compact = topBar.isCompactDensity();
        int columns = width >= 540 ? 4 : (width >= 380 ? 3 : (width >= 230 ? 2 : 1));
        int gap = 6;
        int cardW = (width - 8 - (gap * (columns - 1))) / columns;
        int cardH = compact ? 48 : 66;
        int rows = (int) Math.ceil((double) filteredModules.size() / columns);
        int totalContentHeight = rows * (cardH + gap) + 6;

        mainScroll.tickAndInterpolate();
        mainScroll.updateDimensions(height, totalContentHeight);

        long elapsedSinceSwitch = System.currentTimeMillis() - lastCategorySwitchMillis;

        ClipRenderer.renderClipped(context, x, y, width - 6, height, () -> {
            int startY = Math.round(y - mainScroll.getScrollOffset());
            for (int i = 0; i < filteredModules.size(); i++) {
                Module module = filteredModules.get(i);
                ModuleCardWidget widget = cardWidgets.computeIfAbsent(module, ModuleCardWidget::new);
                int col = i % columns;
                int row = i / columns;
                int cx = x + col * (cardW + gap);
                int cy = startY + row * (cardH + gap);
                if (cy + cardH < y || cy > y + height) continue;

                // Subtle Staggered Cascade Entrance Animation (Aura style)
                long cardDelay = Math.min(i, 16) * 14L;
                float staggerProgress = MathUtils.clamp((elapsedSinceSwitch - cardDelay) / 160.0f, 0.0f, 1.0f);
                float easedStagger = Easing.EASE_OUT_CUBIC.apply(staggerProgress);
                int staggerOffsetY = Math.round((1.0f - easedStagger) * 7.0f);

                widget.render(context, textRenderer, cx, cy + staggerOffsetY, cardW, cardH, mouseX, mouseY, compact);
            }
        });
        mainScroll.renderScrollbar(context, x + width - 4, y, 2, height, mouseX, mouseY);
    }

    private void renderThemesView(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        Theme active = GlassRenderer.getTheme();
        ThemeManager tm = BanglaClient.getInstance().getThemeManager();
        int accent = tm.getEffectiveAccentPrimary();

        if (activeCustomSlider >= 0) {
            updateCustomThemeFromMouse(mouseX, x + 8, width - 24);
        }

        mainScroll.tickAndInterpolate();
        mainScroll.updateDimensions(height, 440);

        ClipRenderer.renderClipped(context, x, y, width - 6, height, () -> {
            int curY = Math.round(y - mainScroll.getScrollOffset()) + 4;
            context.drawTextWithShadow(textRenderer, "Visual Themes (Aura Dark, Aura Light, Crimson & Custom)", x + 4, curY, active.getTextPrimary());
            curY += 14;

            for (Theme t : tm.getAllThemes()) {
                boolean isSelected = t.getId().equals(active.getId());
                boolean hovered = MathUtils.isHovered(mouseX, mouseY, x + 4, curY, width - 12, 28);
                GlassRenderer.drawGlassPanel(context, x + 4, curY, width - 12, 28, true, hovered, isSelected);
                context.fill(x + 10, curY + 6, x + 26, curY + 22, t.getAccentPrimary());
                context.drawTextWithShadow(textRenderer, t.getDisplayName(), x + 34, curY + 5, active.getTextPrimary());
                context.drawTextWithShadow(textRenderer, isSelected ? "ACTIVE THEME" : "Click to apply", x + 34, curY + 16,
                        isSelected ? active.getTextPrimary() : active.getTextSecondary());
                curY += 32;
            }

            curY += 6;
            context.drawTextWithShadow(textRenderer, "Custom Theme Live Adjuster:", x + 4, curY, active.getTextPrimary());
            curY += 14;

            String[] labels = {
                    "Primary Accent Hue",
                    "Secondary Accent Hue",
                    "Glass Opacity",
                    "Surface Brightness",
                    "Border Intensity",
                    "Text Brightness"
            };
            float[] vals = {
                    tm.getCustomPrimaryHue(),
                    tm.getCustomSecondaryHue(),
                    (tm.getCustomGlassAlpha() - 0.06f) / 0.86f,
                    tm.getCustomTintBrightness() / 0.50f,
                    (tm.getCustomBorderAlpha() - 0.08f) / 0.82f,
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

            int halfW = (width - 18) / 2;
            GlassRenderer.drawGlassPanel(context, x + 4, curY, halfW, 20, true, MathUtils.isHovered(mouseX, mouseY, x + 4, curY, halfW, 20), true);
            context.drawTextWithShadow(textRenderer, "Save Custom Theme", x + 10, curY + 6, active.getTextPrimary());

            int rx = x + 4 + halfW + 6;
            GlassRenderer.drawGlassPanel(context, rx, curY, halfW, 20, true, MathUtils.isHovered(mouseX, mouseY, rx, curY, halfW, 20), false);
            context.drawTextWithShadow(textRenderer, "Reset to Aura Dark", rx + 10, curY + 6, active.getTextSecondary());
        });
        mainScroll.renderScrollbar(context, x + width - 4, y, 2, height, mouseX, mouseY);
    }

    private void updateCustomThemeFromMouse(double mouseX, int barX, int barW) {
        if (barW <= 0) return;
        float norm = MathUtils.clamp((float) ((mouseX - barX) / (double) barW), 0.0f, 1.0f);
        ThemeManager tm = BanglaClient.getInstance().getThemeManager();
        tm.setActiveThemeById("custom_theme");
        switch (activeCustomSlider) {
            case 0 -> tm.setCustomPrimaryHue(norm);
            case 1 -> tm.setCustomSecondaryHue(norm);
            case 2 -> tm.setCustomGlassAlpha(0.06f + norm * 0.86f);
            case 3 -> tm.setCustomTintBrightness(norm * 0.50f);
            case 4 -> tm.setCustomBorderAlpha(0.08f + norm * 0.82f);
            case 5 -> tm.setCustomTextBrightness(0.70f + norm * 0.30f);
        }
    }

    private void renderClientSettingsView(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        Theme theme = GlassRenderer.getTheme();
        var cfg = BanglaClient.getInstance().getConfigManager();

        mainScroll.tickAndInterpolate();
        mainScroll.updateDimensions(height, 280);

        ClipRenderer.renderClipped(context, x, y, width - 6, height, () -> {
            int rowY = Math.round(y - mainScroll.getScrollOffset()) + 4;
            context.drawTextWithShadow(textRenderer, "Client Tools, Wardrobe, Accounts & System", x + 4, rowY, theme.getTextPrimary());
            rowY += 16;

            String[] rows = {
                    "Open Interactive HUD Editor (Drag, Resize & Snap Widgets) →",
                    "Open 3D Skin & Cape Wardrobe Customizer →",
                    "Open In-Game Account Switcher (Microsoft & Offline) →",
                    "Open Keybind Manager & Conflict Inspector →",
                    "Open Interactive Tutorial Center (14-Step Guide) →",
                    "Reduced Motion (Instant UI Transitions): " + (cfg.isReducedMotion() ? "ENABLED" : "DISABLED"),
                    "BANGLA CLIENT Custom Main Menu: " + (cfg.isCustomMainMenuEnabled() ? "ENABLED" : "VANILLA"),
                    "Reset All Client Configurations to Factory Defaults"
            };

            for (int i = 0; i < rows.length; i++) {
                boolean hov = MathUtils.isHovered(mouseX, mouseY, x + 4, rowY, width - 12, 26);
                GlassRenderer.drawGlassPanel(context, x + 4, rowY, width - 12, 26, true, hov, i == 0 || hov);
                int col = (i == 7) ? ThemeTokens.STATUS_ERROR : (hov ? theme.getTextPrimary() : theme.getTextSecondary());
                context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(rows[i], width - 24), x + 10, rowY + 9, col);
                rowY += 30;
            }
        });
        mainScroll.renderScrollbar(context, x + width - 4, y, 2, height, mouseX, mouseY);
    }

    private void renderAboutView(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        Theme theme = GlassRenderer.getTheme();

        mainScroll.tickAndInterpolate();
        mainScroll.updateDimensions(height, 165);

        ClipRenderer.renderClipped(context, x, y, width - 6, height, () -> {
            int cy = Math.round(y - mainScroll.getScrollOffset()) + 6;
            GlassRenderer.drawGlassPanel(context, x + 4, cy, width - 12, 150, true, false, true);
            int cx = x + 16;
            cy += 14;
            context.drawTextWithShadow(textRenderer, BanglaClient.CLIENT_NAME, cx, cy, theme.getTextPrimary());
            context.drawTextWithShadow(textRenderer, "Version: " + BanglaClient.CLIENT_VERSION, cx, cy + 14, theme.getTextSecondary());
            context.drawTextWithShadow(textRenderer, "Developed by " + BanglaClient.DEVELOPER, cx, cy + 28, theme.getTextSecondary());
            GlassRenderer.drawHorizontalDivider(context, cx, cy + 42, width - 36);
            context.drawTextWithShadow(textRenderer, "Minecraft Java Edition 1.21.11 (Fabric Utility Client)", cx, cy + 50, theme.getTextSecondary());
            context.drawTextWithShadow(textRenderer, "Architecture: PYES Layout Structure + AURA Frosted Glass", cx, cy + 63, theme.getTextSecondary());
            context.drawTextWithShadow(textRenderer, "Bangla SMP — Server Address: play.banglasmp.net", cx, cy + 80, theme.getTextPrimary());

            int btnY = cy + 96;
            boolean copyHov = MathUtils.isHovered(mouseX, mouseY, cx, btnY, 128, 18);
            GlassRenderer.drawGlassPanel(context, cx, btnY, 128, 18, true, copyHov, true);
            context.drawTextWithShadow(textRenderer, "Copy Server IP", cx + 22, btnY + 5, theme.getTextPrimary());
        });
        mainScroll.renderScrollbar(context, x + width - 4, y, 2, height, mouseX, mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int sidebarW = Math.min(ThemeTokens.SIDEBAR_WIDTH, Math.max(92, this.windowWidth / 5));
        int topBarH = ThemeTokens.TOPBAR_HEIGHT;
        int footerH = ThemeTokens.FOOTER_HEIGHT;
        int mainX = windowX + sidebarW + 6;
        int mainY = windowY + topBarH + 6;
        int mainW = windowWidth - sidebarW - 10;
        int mainH = windowHeight - topBarH - footerH - 10;

        if (sidebar.mouseClicked(mouseX, mouseY, button, windowX + 4, windowY + 4, sidebarW - 2, windowHeight - footerH - 6, () -> {
            mainScroll.resetScroll();
            lastCategorySwitchMillis = System.currentTimeMillis();
            rebuildFilteredModules();
        })) {
            return true;
        }

        if (topBar.mouseClicked(mouseX, mouseY, button, mainX, windowY + 4, mainW, topBarH - 2, () -> {
            if (topBar.consumeSettingsShortcutClick()) {
                sidebar.setSelectedTab(GlassSidebar.SidebarTab.SETTINGS);
            }
            mainScroll.resetScroll();
            lastCategorySwitchMillis = System.currentTimeMillis();
            rebuildFilteredModules();
        })) {
            return true;
        }

        if (mainScroll.onMouseClicked(mouseX, mouseY, button, mainX + mainW - 4, mainY, 4, mainH)) {
            return true;
        }

        GlassSidebar.SidebarTab activeTab = sidebar.getSelectedTab();

        if (topBar.getSearchQuery().isBlank() && activeTab == GlassSidebar.SidebarTab.THEMES) {
            int curY = Math.round(mainY - mainScroll.getScrollOffset()) + 18;
            ThemeManager tm = BanglaClient.getInstance().getThemeManager();
            for (Theme t : tm.getAllThemes()) {
                if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, curY, mainW - 12, 28)) {
                    tm.setActiveThemeById(t.getId());
                    return true;
                }
                curY += 32;
            }
            curY += 20;
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
                BanglaClient.getInstance().getNotificationManager().pushSuccess("Theme Saved", "Saved Custom Theme settings.");
                return true;
            }
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 10 + halfW, curY, halfW, 20)) {
                tm.resetCustomThemeToRedDefaults();
                tm.setActiveThemeById("aura_dark");
                BanglaClient.getInstance().getConfigManager().saveThemeConfig();
                return true;
            }
            return true;
        }

        if (topBar.getSearchQuery().isBlank() && activeTab == GlassSidebar.SidebarTab.SETTINGS) {
            var cfg = BanglaClient.getInstance().getConfigManager();
            int rowY = Math.round(mainY - mainScroll.getScrollOffset()) + 20;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 26)) {
                if (this.client != null) this.client.setScreen(new HudEditorScreen(this));
                return true;
            }
            rowY += 30;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 26)) {
                if (this.client != null) this.client.setScreen(new SkinCustomizationScreen(this, SkinCapeManager.getInstance()));
                return true;
            }
            rowY += 30;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 26)) {
                if (this.client != null) this.client.setScreen(new AccountSwitcherScreen(this, new AccountManager(), SkinCapeManager.getInstance()));
                return true;
            }
            rowY += 30;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 26)) {
                if (this.client != null) this.client.setScreen(new KeybindManagerScreen(this));
                return true;
            }
            rowY += 30;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 26)) {
                if (this.client != null) this.client.setScreen(new TutorialCenterScreen(this));
                return true;
            }
            rowY += 30;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 26)) {
                cfg.setReducedMotion(!cfg.isReducedMotion());
                return true;
            }
            rowY += 30;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 26)) {
                cfg.setCustomMainMenuEnabled(!cfg.isCustomMainMenuEnabled());
                return true;
            }
            rowY += 30;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 4, rowY, mainW - 12, 26)) {
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

        // Module Grid Clicks
        if (MathUtils.isHovered(mouseX, mouseY, mainX, mainY, mainW - 6, mainH)) {
            boolean compact = topBar.isCompactDensity();
            int columns = mainW >= 540 ? 4 : (mainW >= 380 ? 3 : (mainW >= 230 ? 2 : 1));
            int gap = 6;
            int cardW = (mainW - 8 - (gap * (columns - 1))) / columns;
            int cardH = compact ? 48 : 66;
            int startY = Math.round(mainY - mainScroll.getScrollOffset());

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
            int sidebarW = Math.min(ThemeTokens.SIDEBAR_WIDTH, Math.max(92, this.windowWidth / 5));
            int mainX = windowX + sidebarW + 6;
            int mainW = windowWidth - sidebarW - 10;
            updateCustomThemeFromMouse(mouseX, mainX + 8, mainW - 24);
            return true;
        }
        if (sidebar.mouseDragged(mouseX, mouseY, button, windowHeight - ThemeTokens.FOOTER_HEIGHT - 6)) {
            return true;
        }
        int mainH = windowHeight - ThemeTokens.TOPBAR_HEIGHT - ThemeTokens.FOOTER_HEIGHT - 10;
        if (mainScroll.onMouseDragged(mouseX, mouseY, button, mainH)) {
            return true;
        }
        int sidebarW = Math.min(ThemeTokens.SIDEBAR_WIDTH, Math.max(92, this.windowWidth / 5));
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
        int sidebarW = Math.min(ThemeTokens.SIDEBAR_WIDTH, Math.max(92, this.windowWidth / 5));
        int topBarH = ThemeTokens.TOPBAR_HEIGHT;
        int footerH = ThemeTokens.FOOTER_HEIGHT;
        int mainX = windowX + sidebarW + 6;
        int mainY = windowY + topBarH + 6;
        int mainW = windowWidth - sidebarW - 10;
        int mainH = windowHeight - topBarH - footerH - 10;

        if (sidebar.mouseScrolled(mouseX, mouseY, verticalAmount, windowX + 4, windowY + 4, sidebarW - 2, windowHeight - footerH - 6)) {
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
