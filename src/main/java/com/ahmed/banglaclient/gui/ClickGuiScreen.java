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
 * Compact 2D Aura Dark ClickGUI with smooth open/close animations and scrollbar/wheel-only scrolling.
 */
public final class ClickGuiScreen extends Screen {
    private final GlassSidebar sidebar = new GlassSidebar();
    private final GlassTopBar topBar = new GlassTopBar();
    private final SmoothScrollContainer mainScroll = new SmoothScrollContainer();
    private final Animation openAnimation = new Animation(0.0f, 160L, Easing.EASE_OUT_CUBIC);

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
        // Compact Aura window dimensions (never eats the full screen)
        this.windowWidth = Math.min(410, Math.max(250, (int) (this.width * 0.70f)));
        this.windowHeight = Math.min(230, Math.max(155, (int) (this.height * 0.70f)));
        this.windowX = (this.width - this.windowWidth) / 2;
        this.windowY = (this.height - this.windowHeight) / 2;

        this.closing = false;
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

            GlassSidebar.SidebarTab activeTab = sidebar.getSelectedTab();
            if (!topBar.getSearchQuery().isBlank() || activeTab.getModuleCategory() != null || activeTab == GlassSidebar.SidebarTab.HOME) {
                renderModulesGridView(context, mainX, mainY, mainW, mainH, mouseX, mouseY);
            } else if (activeTab == GlassSidebar.SidebarTab.SETTINGS) {
                renderClientSettingsView(context, mainX, mainY, mainW, mainH, mouseX, mouseY);
            } else if (activeTab == GlassSidebar.SidebarTab.ABOUT) {
                renderAboutView(context, mainX, mainY, mainW, mainH, mouseX, mouseY);
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
        int cardH = 26;
        int rows = (int) Math.ceil((double) filteredModules.size() / columns);
        int totalContentHeight = rows * (cardH + gap) + 4;

        mainScroll.tickAndInterpolate();
        mainScroll.updateDimensions(height, totalContentHeight);

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
                widget.render(context, textRenderer, cx, cy, cardW, cardH, mouseX, mouseY, true);
            }
        });

        // Right-side scrollbar line (supports left-click drag)
        mainScroll.renderScrollbar(context, x + width - 3, y, 2, height, mouseX, mouseY);
    }

    private void renderClientSettingsView(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        Theme theme = GlassRenderer.getTheme();
        var cfg = BanglaClient.getInstance().getConfigManager();

        mainScroll.tickAndInterpolate();
        mainScroll.updateDimensions(height, 210);

        ClipRenderer.renderClipped(context, x, y, width - 6, height, () -> {
            int rowY = Math.round(y - mainScroll.getScrollOffset()) + 2;
            String[] rows = {
                    "Open Interactive HUD Editor →",
                    "Open 3D Skin & Cape Wardrobe →",
                    "Open Account Switcher →",
                    "Open Keybind Manager →",
                    "Open Tutorial Guide →",
                    "Performance Mode: " + BanglaClient.getInstance().getPerformancePreset().getDisplayName(),
                    "Reduced Motion: " + (cfg.isReducedMotion() ? "ON" : "OFF"),
                    "Reset All Settings to Default"
            };

            for (int i = 0; i < rows.length; i++) {
                boolean hov = MathUtils.isHovered(mouseX, mouseY, x + 2, rowY, width - 10, 22);
                GlassRenderer.drawGlassPanel(context, x + 2, rowY, width - 10, 22, true, hov, hov);
                int col = (i == 7) ? ThemeTokens.STATUS_ERROR : (hov ? 0xFFFFFFFF : theme.getTextSecondary());
                context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(rows[i], width - 20), x + 8, rowY + 7, col);
                rowY += 25;
            }
        });
        mainScroll.renderScrollbar(context, x + width - 3, y, 2, height, mouseX, mouseY);
    }

    private void renderAboutView(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        Theme theme = GlassRenderer.getTheme();
        mainScroll.tickAndInterpolate();
        mainScroll.updateDimensions(height, 130);

        ClipRenderer.renderClipped(context, x, y, width - 6, height, () -> {
            int cy = Math.round(y - mainScroll.getScrollOffset()) + 4;
            GlassRenderer.drawGlassPanel(context, x + 2, cy, width - 10, 120, true, false, true);
            int cx = x + 12;
            cy += 10;
            context.drawTextWithShadow(textRenderer, BanglaClient.CLIENT_NAME + " v" + BanglaClient.CLIENT_VERSION, cx, cy, 0xFFFFFFFF);
            context.drawTextWithShadow(textRenderer, "Developed by " + BanglaClient.DEVELOPER, cx, cy + 13, theme.getTextSecondary());
            GlassRenderer.drawHorizontalDivider(context, cx, cy + 26, width - 30);
            context.drawTextWithShadow(textRenderer, "Minecraft 1.21.11 • 2D Aura Dark Edition", cx, cy + 34, theme.getTextSecondary());
            context.drawTextWithShadow(textRenderer, "Bangla SMP: play.banglasmp.net", cx, cy + 48, 0xFFFFFFFF);

            int btnY = cy + 64;
            boolean copyHov = MathUtils.isHovered(mouseX, mouseY, cx, btnY, 110, 18);
            GlassRenderer.drawGlassPanel(context, cx, btnY, 110, 18, true, copyHov, true);
            context.drawTextWithShadow(textRenderer, "Copy Server IP", cx + 16, btnY + 5, 0xFFFFFFFF);
        });
        mainScroll.renderScrollbar(context, x + width - 3, y, 2, height, mouseX, mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (closing) return false;
        int sidebarW = ThemeTokens.SIDEBAR_WIDTH;
        int topBarH = ThemeTokens.TOPBAR_HEIGHT;
        int mainX = windowX + sidebarW + 6;
        int mainY = windowY + topBarH + 6;
        int mainW = windowWidth - sidebarW - 10;
        int mainH = windowHeight - topBarH - 10;

        if (sidebar.mouseClicked(mouseX, mouseY, button, windowX + 3, windowY + 3, sidebarW, windowHeight - 6, () -> {
            mainScroll.resetScroll();
            rebuildFilteredModules();
        })) {
            return true;
        }

        if (topBar.mouseClicked(mouseX, mouseY, button, mainX, windowY + 3, mainW, topBarH, () -> {
            mainScroll.resetScroll();
            rebuildFilteredModules();
        })) {
            return true;
        }

        // Right-side scrollbar line click/drag
        if (mainScroll.onMouseClicked(mouseX, mouseY, button, mainX + mainW - 4, mainY, 4, mainH)) {
            return true;
        }

        GlassSidebar.SidebarTab activeTab = sidebar.getSelectedTab();

        if (topBar.getSearchQuery().isBlank() && activeTab == GlassSidebar.SidebarTab.SETTINGS) {
            var cfg = BanglaClient.getInstance().getConfigManager();
            int rowY = Math.round(mainY - mainScroll.getScrollOffset()) + 2;
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
                if (this.client != null) this.client.setScreen(new AccountSwitcherScreen(this, new AccountManager(), SkinCapeManager.getInstance()));
                return true;
            }
            rowY += 25;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 2, rowY, mainW - 10, 22)) {
                if (this.client != null) this.client.setScreen(new KeybindManagerScreen(this));
                return true;
            }
            rowY += 25;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 2, rowY, mainW - 10, 22)) {
                if (this.client != null) this.client.setScreen(new TutorialCenterScreen(this));
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

        if (topBar.getSearchQuery().isBlank() && activeTab == GlassSidebar.SidebarTab.ABOUT) {
            int cy = Math.round(mainY - mainScroll.getScrollOffset()) + 14;
            int btnY = cy + 64;
            if (button == 0 && MathUtils.isHovered(mouseX, mouseY, mainX + 12, btnY, 110, 18)) {
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
            int cardH = 26;
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
        // ONLY drag when holding the scrollbar thumb (NO drag-scrolling on content area)
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
    public boolean shouldPause() {
        return false;
    }
}
