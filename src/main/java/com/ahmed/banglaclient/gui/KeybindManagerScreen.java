package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.config.ConfigManager;
import com.ahmed.banglaclient.gui.components.SmoothScrollContainer;
import com.ahmed.banglaclient.input.KeybindManager;
import com.ahmed.banglaclient.module.Module;
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

import java.util.List;

/**
 * Dedicated Keybind Manager screen allowing the user to view, assign, clear, and resolve
 * duplicate keybind conflicts across all client and module shortcuts.
 */
public final class KeybindManagerScreen extends Screen {
    private final Screen parentScreen;
    private final SmoothScrollContainer scrollContainer = new SmoothScrollContainer();

    private Module bindingModule = null;
    private boolean bindingClickGui = false;
    private boolean bindingHudEditor = false;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    public KeybindManagerScreen(Screen parentScreen) {
        super(Text.literal("Keybind Manager"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        this.panelWidth = Math.min(500, this.width - 40);
        this.panelHeight = Math.min(340, this.height - 36);
        this.panelX = (this.width - this.panelWidth) / 2;
        this.panelY = (this.height - this.panelHeight) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        BlurRenderer.renderScreenBackdrop(context, this.width, this.height, 1.0f);
        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary();

        GlassRenderer.drawGlassPanel(context, panelX, panelY, panelWidth, panelHeight, true, false, true);

        // Header
        boolean backHover = MathUtils.isHovered(mouseX, mouseY, panelX + 10, panelY + 10, 52, 18);
        GlassRenderer.drawPillBadge(context, panelX + 10, panelY + 10, 52, 18, backHover ? accent : theme.getTextSecondary(), backHover);
        context.drawTextWithShadow(textRenderer, "← Back", panelX + 18, panelY + 15, backHover ? accent : theme.getTextPrimary());

        context.drawTextWithShadow(textRenderer, "Keybind Manager & Conflict Inspector", panelX + 72, panelY + 14, theme.getTextPrimary());
        GlassRenderer.drawHorizontalDivider(context, panelX + 10, panelY + 34, panelWidth - 20);

        int viewY = panelY + 40;
        int viewH = panelHeight - 46;
        List<Module> modules = BanglaClient.getInstance().getModuleManager().getAllModules();
        int totalHeight = (modules.size() + 2) * 30 + 10;

        scrollContainer.tickAndInterpolate();
        scrollContainer.updateDimensions(viewH, totalHeight);

        ConfigManager cfg = BanglaClient.getInstance().getConfigManager();
        KeybindManager km = BanglaClient.getInstance().getKeybindManager();

        ClipRenderer.renderClipped(context, panelX + 6, viewY, panelWidth - 14, viewH, () -> {
            int rowY = Math.round(viewY - scrollContainer.getScrollOffset()) + 4;
            int rowX = panelX + 12;
            int rowW = panelWidth - 28;

            // System Bind 1: Open Click GUI
            renderBindRow(context, rowX, rowY, rowW, "Open Click GUI", "Global Client Shortcut",
                    bindingClickGui ? "Press key..." : KeybindManager.formatKeyName(cfg.getClickGuiKeyCode()),
                    bindingClickGui, false, accent);
            rowY += 30;

            // System Bind 2: Open HUD Editor
            renderBindRow(context, rowX, rowY, rowW, "Open HUD Editor", "Global Client Shortcut",
                    bindingHudEditor ? "Press key..." : KeybindManager.formatKeyName(cfg.getHudEditorKeyCode()),
                    bindingHudEditor, false, accent);
            rowY += 30;

            // Module Binds
            for (Module m : modules) {
                boolean listening = (bindingModule == m);
                List<Module> conflicts = km.findConflictsForKey(m.getKeyCode(), m);
                boolean hasConflict = !conflicts.isEmpty();
                String keyText = listening ? "Press key..." : KeybindManager.formatKeyName(m.getKeyCode());
                String sub = hasConflict
                        ? "Conflict with: " + conflicts.get(0).getName()
                        : m.getCategory().getSidebarLabel();

                if (rowY + 26 >= viewY && rowY <= viewY + viewH) {
                    renderBindRow(context, rowX, rowY, rowW, m.getName(), sub, keyText, listening, hasConflict, accent);
                }
                rowY += 30;
            }
        });

        scrollContainer.renderScrollbar(context, panelX + panelWidth - 6, viewY, 3, viewH, mouseX, mouseY);
    }

    private void renderBindRow(
            DrawContext context,
            int x,
            int y,
            int w,
            String title,
            String subtitle,
            String keyLabel,
            boolean listening,
            boolean conflict,
            int accent
    ) {
        Theme theme = GlassRenderer.getTheme();
        GlassRenderer.drawGlassPanel(context, x, y, w, 26, false, false, listening);
        context.drawTextWithShadow(textRenderer, title, x + 10, y + 5, theme.getTextPrimary());
        context.drawTextWithShadow(textRenderer, subtitle, x + 10, y + 15,
                conflict ? ThemeTokens.STATUS_WARNING : theme.getTextMuted());

        int badgeColor = conflict ? ThemeTokens.STATUS_WARNING : accent;
        int kw = textRenderer.getWidth(keyLabel) + 14;
        int kx = x + w - kw - 8;
        GlassRenderer.drawPillBadge(context, kx, y + 5, kw, 16, badgeColor, listening || conflict);
        context.drawTextWithShadow(textRenderer, keyLabel, kx + 7, y + 9, badgeColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && MathUtils.isHovered(mouseX, mouseY, panelX + 10, panelY + 10, 52, 18)) {
            close();
            return true;
        }
        int viewY = panelY + 40;
        int viewH = panelHeight - 46;
        if (scrollContainer.onMouseClicked(mouseX, mouseY, button, panelX + panelWidth - 6, viewY, 3, viewH)) {
            return true;
        }

        if (MathUtils.isHovered(mouseX, mouseY, panelX + 6, viewY, panelWidth - 14, viewH)) {
            int rowY = Math.round(viewY - scrollContainer.getScrollOffset()) + 4;
            int rowX = panelX + 12;
            int rowW = panelWidth - 28;

            bindingModule = null;
            bindingClickGui = false;
            bindingHudEditor = false;

            if (MathUtils.isHovered(mouseX, mouseY, rowX, rowY, rowW, 26)) {
                bindingClickGui = (button == 0);
                return true;
            }
            rowY += 30;

            if (MathUtils.isHovered(mouseX, mouseY, rowX, rowY, rowW, 26)) {
                bindingHudEditor = (button == 0);
                return true;
            }
            rowY += 30;

            for (Module m : BanglaClient.getInstance().getModuleManager().getAllModules()) {
                if (MathUtils.isHovered(mouseX, mouseY, rowX, rowY, rowW, 26)) {
                    if (button == 0) {
                        bindingModule = m;
                    } else if (button == 1) {
                        m.clearKeybind();
                        BanglaClient.getInstance().getConfigManager().saveKeybindsConfig();
                    }
                    return true;
                }
                rowY += 30;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (scrollContainer.onMouseDragged(mouseX, mouseY, button, panelHeight - 46)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        scrollContainer.onMouseReleased(button);
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (scrollContainer.onMouseScrolled(mouseX, mouseY, verticalAmount, panelX, panelY + 40, panelWidth, panelHeight - 46)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        ConfigManager cfg = BanglaClient.getInstance().getConfigManager();
        if (bindingClickGui) {
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                cfg.setClickGuiKeyCode(keyCode);
            }
            bindingClickGui = false;
            return true;
        }
        if (bindingHudEditor) {
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                cfg.setHudEditorKeyCode(keyCode);
            }
            bindingHudEditor = false;
            return true;
        }
        if (bindingModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                bindingModule.clearKeybind();
            } else {
                bindingModule.setKeyCode(keyCode);
            }
            bindingModule = null;
            cfg.saveKeybindsConfig();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parentScreen);
        }
    }

    @Override
        @Override public boolean mouseClicked(net.minecraft.client.gui.Click c, boolean d) { return mouseClicked(c.x(), c.y(), c.button()) || super.mouseClicked(c, d); }
    @Override public boolean mouseDragged(net.minecraft.client.gui.Click c, double dx, double dy) { return mouseDragged(c.x(), c.y(), c.button(), dx, dy) || super.mouseDragged(c, dx, dy); }
    @Override public boolean mouseReleased(net.minecraft.client.gui.Click c) { return mouseReleased(c.x(), c.y(), c.button()) || super.mouseReleased(c); }
    @Override public boolean keyPressed(net.minecraft.client.input.KeyInput k) { return keyPressed(k.key(), k.scancode(), k.modifiers()) || super.keyPressed(k); }
    @Override public boolean charTyped(net.minecraft.client.input.CharInput ch) { return charTyped((char) ch.codepoint(), ch.modifiers()) || super.charTyped(ch); }
    public boolean charTyped(char c, int m) { return false; }
    public boolean shouldPause() {
        return false;
    }
}
