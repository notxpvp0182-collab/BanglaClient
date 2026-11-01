package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.hud.HudManager;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.ColorUtils;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Interactive HUD Editor screen supporting drag-and-drop widget positioning,
 * center/edge alignment snap guides, mouse-wheel widget scaling, and right-click settings access.
 */
public final class HudEditorScreen extends Screen {
    private static final float SNAP_THRESHOLD = 5.0f;

    private final Screen parentScreen;
    private HudModule selectedWidget = null;
    private HudModule draggingWidget = null;
    private float dragOffsetX;
    private float dragOffsetY;
    private boolean snappingEnabled = true;

    public HudEditorScreen(Screen parentScreen) {
        super(Text.literal("BANGLA CLIENT HUD Editor"));
        this.parentScreen = parentScreen;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary();

        // Subtle dark translucent canvas overlay so player can still see their world behind widgets
        context.fill(0, 0, this.width, this.height, 0x55080A0F);

        // Center alignment cross-guides when dragging
        if (draggingWidget != null && snappingEnabled) {
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            context.fill(centerX, 0, centerX + 1, this.height, ColorUtils.withAlpha(accent, 0.30f));
            context.fill(0, centerY, this.width, centerY + 1, ColorUtils.withAlpha(accent, 0.30f));
        }

        // Render all enabled HUD widgets with selection & hover outlines
        HudManager hudManager = BanglaClient.getInstance().getHudManager();
        List<HudModule> widgets = BanglaClient.getInstance().getModuleManager().getHudModules();

        for (HudModule widget : widgets) {
            if (!widget.isEnabled()) {
                continue;
            }
            HudManager.clampWidgetToScreen(widget, this.width, this.height);
            hudManager.renderScaledWidget(context, widget, delta, true);

            int wx = Math.round(widget.getPosX());
            int wy = Math.round(widget.getPosY());
            int ww = Math.round(widget.getScaledWidth());
            int wh = Math.round(widget.getScaledHeight());
            boolean hovered = widget.isMouseOver(mouseX, mouseY);
            boolean selected = (widget == selectedWidget || widget == draggingWidget);

            if (selected || hovered) {
                int outlineColor = selected ? accent : theme.getBorderStrong();
                GlassRenderer.drawOutlineBorder(context, wx - 1, wy - 1, ww + 2, wh + 2, outlineColor);
            }
        }

        // Top Floating Control Toolbar
        int barW = 340;
        int barH = 32;
        int barX = (this.width - barW) / 2;
        int barY = 10;
        GlassRenderer.drawGlassPanel(context, barX, barY, barW, barH, true, false, false);

        context.drawTextWithShadow(textRenderer, "HUD Editor", barX + 12, barY + 7, accent);
        context.drawTextWithShadow(textRenderer, "Drag to Move • Scroll to Scale • Right-Click to Configure",
                barX + 12, barY + 18, theme.getTextSecondary());

        // Reset All button inside toolbar
        int resetW = 58;
        int resetX = barX + barW - resetW - 8;
        boolean resetHover = MathUtils.isHovered(mouseX, mouseY, resetX, barY + 7, resetW, 18);
        GlassRenderer.drawPillBadge(context, resetX, barY + 7, resetW, 18, resetHover ? accent : theme.getTextSecondary(), resetHover);
        context.drawTextWithShadow(textRenderer, "Reset All", resetX + 7, barY + 12, resetHover ? accent : theme.getTextPrimary());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int barW = 340;
        int barX = (this.width - barW) / 2;
        int resetX = barX + barW - 66;
        if (button == 0 && MathUtils.isHovered(mouseX, mouseY, resetX, 17, 58, 18)) {
            BanglaClient.getInstance().getHudManager().resetAllPositions();
            return true;
        }

        List<HudModule> widgets = BanglaClient.getInstance().getModuleManager().getHudModules();
        for (int i = widgets.size() - 1; i >= 0; i--) {
            HudModule widget = widgets.get(i);
            if (!widget.isEnabled() || !widget.isMouseOver(mouseX, mouseY)) {
                continue;
            }
            this.selectedWidget = widget;
            if (button == 0) {
                this.draggingWidget = widget;
                this.dragOffsetX = (float) mouseX - widget.getPosX();
                this.dragOffsetY = (float) mouseY - widget.getPosY();
                return true;
            } else if (button == 1 && this.client != null) {
                this.client.setScreen(new ModuleSettingsScreen(this, widget));
                return true;
            }
        }

        this.selectedWidget = null;
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingWidget != null && button == 0) {
            float newX = (float) mouseX - dragOffsetX;
            float newY = (float) mouseY - dragOffsetY;

            if (snappingEnabled) {
                float centerX = (this.width - draggingWidget.getScaledWidth()) * 0.5f;
                float centerY = (this.height - draggingWidget.getScaledHeight()) * 0.5f;
                if (Math.abs(newX - centerX) < SNAP_THRESHOLD) {
                    newX = centerX;
                }
                if (Math.abs(newY - centerY) < SNAP_THRESHOLD) {
                    newY = centerY;
                }
                if (Math.abs(newX - 6.0f) < SNAP_THRESHOLD) {
                    newX = 6.0f;
                }
                if (Math.abs(newY - 6.0f) < SNAP_THRESHOLD) {
                    newY = 6.0f;
                }
            }

            draggingWidget.setPosition(newX, newY);
            HudManager.clampWidgetToScreen(draggingWidget, this.width, this.height);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingWidget != null) {
            draggingWidget = null;
            BanglaClient.getInstance().getConfigManager().saveHudLayoutConfig();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (HudModule widget : BanglaClient.getInstance().getModuleManager().getHudModules()) {
            if (widget.isEnabled() && widget.isMouseOver(mouseX, mouseY)) {
                double nextScale = widget.getScale() + (verticalAmount > 0 ? 0.05 : -0.05);
                widget.getSettings().stream()
                        .filter(s -> s.getId().equals("hud_scale"))
                        .findFirst()
                        .ifPresent(s -> {
                            if (s instanceof com.ahmed.banglaclient.module.setting.NumberSetting ns) {
                                ns.setValue(nextScale);
                            }
                        });
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        BanglaClient.getInstance().getConfigManager().saveHudLayoutConfig();
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
