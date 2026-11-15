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
import com.ahmed.banglaclient.identity.BcIdentityManager;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.ModuleManager;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.ClipRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
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
 * 2D Aura Dark ClickGUI with smooth page transitions, dedicated ENABLED & FAVORITES views,
 * Tutorial access in Settings, and enhanced About page.
 */
public final class ClickGuiScreen extends Screen {
    private final GlassSidebar sidebar = new GlassSidebar();
    private final GlassTopBar topBar = new GlassTopBar();
    private final SmoothScrollContainer mainScroll = new SmoothScrollContainer();
    private final Animation openAnimation = new Animation(0.0f, 160L, Easing.EASE_OUT_CUBIC);
    private final Animation pageTransitionAnim = new Animation(1.0f, 150L, Easing.EASE_OUT_CUBIC);

    private final Map<Module, ModuleCardWidget> cardWidgets = new LinkedHashMap<>();
    private List<Module> filteredModules = new ArrayList<>();

    private int windowX, windowY, windowWidth, windowHeight;
    private final boolean autoFocusSearch;
    private boolean closing = false;

    public ClickGuiScreen(boolean autoFocusSearch) {
        super(Text.literal(BanglaClient.CLIENT_NAME));
        this.autoFocusSearch = autoFocusSearch;
    }

    public ClickGuiScreen() {
        this(false);
    }

    @Override
    protected void init() {
        this.windowWidth = Math.min(410, Math.max(250, (int) (this.width * 0.70f)));
        this.windowHeight = Math.min(230, Math.max(155, (int) (this.height * 0.70f)));
        this.windowX = (this.width - this.windowWidth) / 2;
        this.windowY = (this.height - this.windowHeight) / 2;

        this.closing = false;
        this.openAnimation.snapTo(0.0f);
        this.openAnimation.animateTo(1.0f);
        this.pageTransitionAnim.snapTo(0.0f);
        this.pageTransitionAnim.animateTo(1.0f);

        if (autoFocusSearch) {
            this.topBar.setSearchFocused(true);
        }
        rebuildFilteredModules();
    }

    private void triggerPageTransition() {
        this.pageTransitionAnim.snapTo(0.0f);
        this.pageTransitionAnim.animateTo(1.0f);
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
        if (!topBar.getSearchQuery().isBlank() || topBar.isFilterOnlyEnabled() || topBar.isFilterOnlyFavorites()) {
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
        if (closing && openVal <= 0.02f) {
            performActualClose();
            return;
        }

        BlurRenderer.renderScreenBackdrop(context, this.width, this.height, openVal);

        Matrix3x2fStack matrices = context.getMatrices();
        float scale = 0.94f + (0.06f * openVal);
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

            int sidebarW = ThemeTokens.SIDEBAR_WIDTH;
            int topBarH = ThemeTokens.TOPBAR_HEIGHT;
            int mainX = windowX + sidebarW + 6;
            int mainY = windowY + topBarH + 6;
            int mainW = windowWidth - sidebarW - 10;
            int mainH = windowHeight - topBarH - 10;

            sidebar.render(context, textRenderer, windowX + 3, windowY + 3, sidebarW, windowHeight - 6, mouseX, mouseY); 
            topBar.render(context, textRenderer, mainX, windowY + 3, mainW, topBarH, mouseX, mouseY);

            float pageTrans = pageTransitionAnim.getValue();
            int slideOffsetY = Math.round((1.0f - pageTrans) * 6.0f);

            GlassSidebar.SidebarTab activeTab = sidebar.getSelectedTab();
            if (topBar.isFilterOnlyEnabled() || topBar.isFilterOnlyFavorites() || !topBar.getSearchQuery().isBlank() || activeTab.getModuleCategory() != null || activeTab == GlassSidebar.SidebarTab.HOME) {
                renderModulesGridView(context, mainX, mainY + slideOffsetY, mainW, mainH, mouseX, mouseY);
            } else if (activeTab == GlassSidebar.SidebarTab.SETTINGS) {
                renderClientSettingsView(context, mainX, mainY + slideOffsetY, mainW, mainH, mouseX, mouseY);
            } else if (activeTab == GlassSidebar.SidebarTab.ABOUT) {
                renderAboutView(context, mainX, mainY + slideOffsetY, mainW, mainH, mouseX, mouseY);
            }

            // Floating Category Hover Tooltip Centered Directly Above Mouse Cursor (Top-most layer!)
            GlassSidebar.SidebarTab hovTab = sidebar.getHoveredTab(mouseX, mouseY, windowX + 3, windowY + 3, sidebarW, windowHeight - 6);
            if (hovTab != null) {
                String hText = GlassRenderer.toSmallCaps(hovTab.getLabel());
                int htw = textRenderer.getWidth(hText) + 12;
                int tipX = Math.max(4, Math.min(this.width - htw - 4, mouseX - (htw / 2)));
                int tipY = Math.max(4, mouseY - 18);
                context.fill(tipX - 1, tipY - 1, tipX + htw + 1, tipY + 16 + 1, 0xEE030406);
                GlassRenderer.drawGlassPanel(context, tipX, tipY, htw, 16, true, true, true);
                context.drawTextWithShadow(textRenderer, hText, tipX + 6, tipY + 4, 0xFFFFFFFF);
            }

            // Floating Description Screen Modal when [?] is clicked
            Module descMod = ModuleCardWidget.getActiveDescriptionPopupModule();
            if (descMod != null) {
                context.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, 0xB8030406);
                int mw = 230, mh = 96;
                int mx = windowX + (windowWidth - mw) / 2;
                int my = windowY + (windowHeight - mh) / 2;
                GlassRenderer.drawGlassPanel(context, mx, my, mw, mh, true, false, true);
                GlassRenderer.drawModuleIcon(context, mx + 10, my + 10, 12, descMod.getId(), 0xFFFFFFFF);
                context.drawTextWithShadow(textRenderer, GlassRenderer.toSmallCaps(descMod.getName()), mx + 26, my + 12, 0xFFFFFFFF);
                GlassRenderer.drawHorizontalDivider(context, mx + 8, my + 26, mw - 16);

                var wrapped = textRenderer.wrapLines(Text.literal(descMod.getDescription()), mw - 20);
                int ly = my + 32;
                for (var line : wrapped) {
                    context.drawTextWithShadow(textRenderer, line, mx + 10, ly, 0xFFCBD5E1);
                    ly += 10;
                }

                int closeW = 64, closeH = 16;
                int closeX = mx + (mw - closeW) / 2;
                int closeY = my + mh - 22;
                boolean cHov = MathUtils.isHovered(mouseX, mouseY, closeX, closeY, closeW, closeH);
                GlassRenderer.drawPillBadge(context, closeX, closeY, closeW, closeH, 0xFFFFFFFF, cHov || true);
                String clLbl = GlassRenderer.toSmallCaps("Close");
                context.drawTextWithShadow(textRenderer, clLbl, closeX + (closeW - textRenderer.getWidth(clLbl)) / 2, closeY + 4, 0xFFFFFFFF);
            }
        } finally {
            if (scaling) {
                matrices.popMatrix();
            }
        }
    }

    private void renderModulesGridView(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        int columns = width >= 250 ? 2 : 1;
        int gap = 5;
        int cardW = (width - 8 - (gap * (columns - 1))) / columns;
        int cardH = 44;
        int rows = (int) Math.ceil((double) filteredModules.size() / columns);
        int totalContentHeight = rows * (cardH + gap) + 4;

        filteredModules.removeIf(m -> { ModuleCardWidget w = cardWidgets.get(m); if (w != null && w.isExitAnimationFinished()) { w.resetExitAnimation(); return true; } return false; }); mainScroll.tickAndInterpolate();
        mainScroll.updateDimensions(height, totalContentHeight);

        ClipRenderer.renderClipped(context, x, y, width - 6, height, () -> {
            if (filteredModules.isEmpty()) {
                String emptyMsg = topBar.isFilterOnlyFavorites()
                        ? "No favorite modules yet. Click ☆ on any module!"
                        : (topBar.isFilterOnlyEnabled() ? "No enabled modules." : "No matching modules found.");
                context.drawTextWithShadow(textRenderer, emptyMsg, x + 8, y + 12, 0xFF94A3B8);
                return;
            }
            int startY = Math.round(y - mainScroll.getScrollOffset());
            for (int i = 0; i < filteredModules.size(); i++) {
                Module module = filteredModules.get(i);
                ModuleCardWidget widget = cardWidgets.computeIfAbsent(module, ModuleCardWidget::new);
                int col = i % columns;
                int row = i / columns;
                int cx = x + col * (cardW + gap);
                int cy = startY + row * (cardH + gap);
                if (cy + cardH < y || cy > y + height) continue;
                widget.render(context, textRenderer, cx, cy, cardW, cardH, mouseX, mouseY, true);
            }
        });

        mainScroll.renderScrollbar(context, x + width - 3, y, 2, height, mouseX, mouseY);
    }

    private void renderClientSettingsView(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        Theme theme = GlassRenderer.getTheme();
        var cfg = BanglaClient.getInstance().getConfigManager();

        filteredModules.removeIf(m -> { ModuleCardWidget w = cardWidgets.get(m); if (w != null && w.isExitAnimationFinished()) { w.resetExitAnimation(); return true; } return false; }); mainScroll.tickAndInterpolate();
        mainScroll.updateDimensions(height, 235);

        ClipRenderer.renderClipped(context, x, y, width - 6, height, () -> {
            int rowY = Math.round(y - mainScroll.getScrollOffset()) + 2;
            String[] rows = {
                    "Open Interactive Tutorial Guide (14 Steps) →",
                    "Open Interactive HUD Editor (Corner Resize & Move) →",
                    "Open Bedrock-Style Skin & Cape Wardrobe →",
                    "Open In-Game Account Switcher →",
                    "Open Menu Background Customizer (GIF / PNG / JPG) →",
                    "Open Keybind Manager & Conflict Inspector →",
                    "Performance Mode: " + BanglaClient.getInstance().getPerformancePreset().getDisplayName(),
                    "Reduced Motion: " + (cfg.isReducedMotion() ? "ON" : "OFF"),
                    "Reset All Client Settings to Default"
            };

            for (int i = 0; i < rows.length; i++) {
                boolean hov = MathUtils.isHovered(mouseX, mouseY, x + 2, rowY, width - 10, 22);
                GlassRenderer.drawGlassPanel(context, x + 2, rowY, width - 10, 22, true, hov, i == 0 || hov);
                int col = (i == 8) ? ThemeTokens.STATUS_ERROR : (hov || i == 0 ? 0xFFFFFFFF : theme.getTextSecondary());
                context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(rows[i], width - 20), x + 8, rowY + 7, col);
                rowY += 25;
            }
        });
        mainScroll.renderScrollbar(context, x + width - 3, y, 2, height, mouseX, mouseY);
    }

    private void renderAboutView(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        Theme theme = GlassRenderer.getTheme();
        filteredModules.removeIf(m -> { ModuleCardWidget w = cardWidgets.get(m); if (w != null && w.isExitAnimationFinished()) { w.resetExitAnimation(); return true; } return false; }); mainScroll.tickAndInterpolate();
        mainScroll.updateDimensions(height, 145);

        ClipRenderer.renderClipped(context, x, y, width - 6, height, () -> {
            int cy = Math.round(y - mainScroll.getScrollOffset()) + 4;
            GlassRenderer.drawGlassPanel(context, x + 2, cy, width - 10, 134, true, false, true);
            int cx = x + 12;
            cy += 10;

            BcIdentityManager.drawIdentityIcon(context, cx, cy, 14, 14);
            context.drawTextWithShadow(textRenderer, BanglaClient.CLIENT_NAME + " v" + BanglaClient.CLIENT_VERSION, cx + 20, cy + 3, 0xFFFFFFFF);
            context.drawTextWithShadow(textRenderer, "Developer: Ahmed (Minecraft Player & Client Creator)", cx, cy + 19, 0xFFFFFFFF);
            GlassRenderer.drawHorizontalDivider(context, cx, cy + 31, width - 30);

            context.drawTextWithShadow(textRenderer, "• Support: Compatible with All Minecraft Versions", cx, cy + 38, theme.getTextSecondary());
            context.drawTextWithShadow(textRenderer, "• Modules: 136 HD Utility, HUD & PvP Modules", cx, cy + 50, theme.getTextSecondary());
            context.drawTextWithShadow(textRenderer, "• Official Server: play.banglasmp.net", cx, cy + 64, 0xFFFFFFFF);

            int btnY = cy + 80;
            boolean copyHov = MathUtils.isHovered(mouseX, mouseY, cx, btnY, 106, 18);
            GlassRenderer.drawPillBadge(context, cx, btnY, 106, 18, 0xFFFFFFFF, copyHov || true);
            context.drawTextWithShadow(textRenderer, "Copy Server IP", cx + 14, btnY + 5, 0xFFFFFFFF);
        });
        mainScroll.renderScrollbar(context, x + width - 3, y, 2, height, mouseX, mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (closing) return false;
        if (ModuleCardWidget.getActiveDescriptionPopupModule() != null) { GlassRenderer.playClickSound(); ModuleCardWidget.clearDescriptionPopup(); return true; }
        int sidebarW = ThemeTokens.SIDEBAR_WIDTH;
        int topBarH = ThemeTokens.TOPBAR_HEIGHT;
        int mainX = windowX + sidebarW + 6;
        int mainY = windowY + topBarH + 6;
        int mainW = windowWidth - sidebarW - 10;
        int mainH = windowHeight - topBarH - 10;

        if (sidebar.mouseClicked(mouseX, mouseY, button, windowX + 3, windowY + 3, sidebarW, windowHeight - 6, () -> {
            if (sidebar.getSelectedTab() == GlassSidebar.SidebarTab.LIVESTREAM) {
                sidebar.setSelectedTab(GlassSidebar.SidebarTab.HOME);
                if (this.client != null) this.client.setScreen(new LiveStreamGuiScreen(this));
                return;
            }
            topBar.clearQuickFilters();
            mainScroll.resetScroll();
            triggerPageTransition();
            rebuildFilteredModules();
        })) {
            return true;
        }

        if (topBar.mouseClicked(mouseX, mouseY, button, mainX, windowY + 3, mainW, topBarH, () -> {
            mainScroll.resetScroll();
            triggerPageTransition();
            rebuildFilteredModules();
        })) {
            return true;
        }

        if (mainScroll.onMouseClicked(mouseX, mouseY, button, mainX + mainW - 4, mainY, 4, mainH)) {
            return true;
        }

        GlassSidebar.SidebarTab activeTab = sidebar.getSelectedTab();
        boolean showingModules = topBar.isFilterOnlyEnabled() || topBar.isFilterOnlyFavorites()
                || !topBar.getSearchQuery().isBlank() || activeTab.getModuleCategory() != null || activeTab == GlassSidebar.SidebarTab.HOME;

        if (!showingModules && activeTab == GlassSidebar.SidebarTab.SETTINGS) {
            var cfg = BanglaClient.getInstance().getConfigManager();
            int rowY = Math.round(mainY - mainScroll.getScrollOffset()) + 2;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 2, rowY, mainW - 10, 22)) {
                if (this.client != null) this.client.setScreen(new TutorialCenterScreen(this));
                return true;
            }
            rowY += 25;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 2, rowY, mainW - 10, 22)) {
                if (this.client != null) this.client.setScreen(new HudEditorScreen(this));
                return true;
            }
            rowY += 25;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 2, rowY, mainW - 10, 22)) {
                if (this.client != null) this.client.setScreen(new SkinCustomizationScreen(this, SkinCapeManager.getInstance()));
                return true;
            }
            rowY += 25;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 2, rowY, mainW - 10, 22)) {
                if (this.client != null) this.client.setScreen(new AccountSwitcherScreen(this, AccountManager.getInstance(), SkinCapeManager.getInstance()));
                return true;
            }
            rowY += 25;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 2, rowY, mainW - 10, 22)) {
                if (this.client != null) this.client.setScreen(new BackgroundSelectorScreen(this));
                return true;
            }
            rowY += 25;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 2, rowY, mainW - 10, 22)) {
                if (this.client != null) this.client.setScreen(new KeybindManagerScreen(this));
                return true;
            }
            rowY += 25;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 2, rowY, mainW - 10, 22)) {
                BanglaClient.getInstance().setPerformancePreset(BanglaClient.getInstance().getPerformancePreset().next());
                cfg.saveGuiAndSystemConfig();
                return true;
            }
            rowY += 25;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 2, rowY, mainW - 10, 22)) {
                cfg.setReducedMotion(!cfg.isReducedMotion());
                return true;
            }
            rowY += 25;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 2, rowY, mainW - 10, 22)) {
                cfg.resetAllToDefaults();
                rebuildFilteredModules();
                return true;
            }
            return true;
        }

        if (!showingModules && activeTab == GlassSidebar.SidebarTab.ABOUT) {
            int cy = Math.round(mainY - mainScroll.getScrollOffset()) + 14;
            int btnY = cy + 80;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 12, btnY, 106, 18)) {
                if (this.client != null && this.client.keyboard != null) {
                    this.client.keyboard.setClipboard("play.banglasmp.net");
                    BanglaClient.getInstance().getNotificationManager().pushSuccess("Bangla SMP", "Copied play.banglasmp.net");
                }
            }
            return true;
        }

        if (MathUtils.isHovered(mouseX, mouseY, mainX, mainY, mainW - 6, mainH)) {
            int columns = mainW >= 250 ? 2 : 1;
            int gap = 5;
            int cardW = (mainW - 8 - (gap * (columns - 1))) / columns;
            int cardH = 44;
            int startY = Math.round(mainY - mainScroll.getScrollOffset());

            for (int i = 0; i < filteredModules.size(); i++) {
                Module module = filteredModules.get(i);
                ModuleCardWidget widget = cardWidgets.get(module);
                int col = i % columns;
                int row = i / columns;
                int cx = mainX + col * (cardW + gap);
                int cy = startY + row * (cardH + gap);

                if (widget != null && widget.mouseClicked(mouseX, mouseY, button, cx, cy, cardW, cardH, true, m -> {
                    if (this.client != null) {
                        if (m instanceof com.ahmed.banglaclient.module.visual.ColorSaturationModule csm) { this.client.setScreen(new ColorSaturationScreen(this, csm)); } else { this.client.setScreen(new ModuleSettingsScreen(this, m)); }
                    }
                })) {
                    if ((topBar.isFilterOnlyEnabled() && !module.isEnabled()) || (topBar.isFilterOnlyFavorites() && !module.isFavorite())) { widget.startExitAnimation(); } else if (false) {
                        rebuildFilteredModules();
                    }
                    return true;
                }
            }
        }
        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (sidebar.mouseDragged(mouseX, mouseY, button, windowHeight - 6)) {
            return true;
        }
        int mainH = windowHeight - ThemeTokens.TOPBAR_HEIGHT - 10;
        if (mainScroll.onMouseDragged(mouseX, mouseY, button, mainH)) {
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        sidebar.mouseReleased(button);
        mainScroll.onMouseReleased(button);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int sidebarW = ThemeTokens.SIDEBAR_WIDTH;
        int topBarH = ThemeTokens.TOPBAR_HEIGHT;
        int mainX = windowX + sidebarW + 6;
        int mainY = windowY + topBarH + 6;
        int mainW = windowWidth - sidebarW - 10;
        int mainH = windowHeight - topBarH - 10;

        if (sidebar.mouseScrolled(mouseX, mouseY, verticalAmount, windowX + 3, windowY + 3, sidebarW, windowHeight - 6)) {
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
            if (ModuleCardWidget.getActiveDescriptionPopupModule() != null) { ModuleCardWidget.clearDescriptionPopup(); return true; }
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
        if (!closing) {
            closing = true;
            openAnimation.animateTo(0.0f);
        }
    }

    private void performActualClose() {
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getConfigManager() != null) {
            BanglaClient.getInstance().getConfigManager().saveAll();
        }
        super.close();
    }

    @Override
    public boolean shouldPause() { return false; }
}
