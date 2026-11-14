package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.hud.HudManager;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * 2D Aura HUD Editor supporting:
 * - Left-click dragging to move widgets
 * - Left-click dragging the bottom-right corner handle to resize/scale widgets
 * - Mouse wheel scaling & Right-click settings
 */
public final class HudEditorScreen extends Screen {
    private static final float SNAP_THRESHOLD = 5.0f;

    private final Screen parentScreen;
    private HudModule selectedWidget = null;
    private HudModule draggingWidget = null;
    private HudModule resizingWidget = null;
    private float dragOffsetX;
    private float dragOffsetY;
    private float resizeStartMouseX;
    private float resizeStartScale;

    public HudEditorScreen(Screen parentScreen) {
        super(Text.literal("HUD Editor"));
        this.parentScreen = parentScreen;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Theme theme = GlassRenderer.getTheme();
        context.fill(0, 0, this.width, this.height, 0x66060709);

        if (draggingWidget != null) {
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            context.fill(centerX, 0, centerX + 1, this.height, 0x44FFFFFF);
            context.fill(0, centerY, this.width, centerY + 1, 0x44FFFFFF);
        }

        HudManager hudManager = BanglaClient.getInstance().getHudManager();
        List<HudModule> widgets = BanglaClient.getInstance().getModuleManager().getHudModules();

        for (HudModule widget : widgets) {
            if (!widget.isEnabled()) continue;
            HudManager.clampWidgetToScreen(widget, this.width, this.height);
            hudManager.renderScaledWidget(context, widget, delta, true);

            int wx = Math.round(widget.getPosX());
            int wy = Math.round(widget.getPosY());
            int ww = Math.round(widget.getScaledWidth());
            int wh = Math.round(widget.getScaledHeight());
            boolean hovered = widget.isMouseOver(mouseX, mouseY) || isOverCornerHandle(widget, mouseX, mouseY);
            boolean selected = (widget == selectedWidget || widget == draggingWidget || widget == resizingWidget);

            if (selected || hovered) {
                GlassRenderer.drawOutlineBorder(context, wx - 1, wy - 1, ww + 2, wh + 2, selected ? 0xFFFFFFFF : 0x88FFFFFF);
                // Bottom-Right Corner Resize Handle (6x6 crisp white square)
                int hx = wx + ww - 3;
                int hy = wy + wh - 3;
                context.fill(hx, hy, hx + 5, hy + 5, 0xFFFFFFFF);
            }
        }

        // Clean Compact Top Toolbar (NO overlapping subtitle text!)
        int barW = 200;
        int barH = 24;
        int barX = (this.width - barW) / 2;
        int barY = 8;
        GlassRenderer.drawGlassPanel(context, barX, barY, barW, barH, true, false, true);

        context.drawTextWithShadow(textRenderer, "HUD Editor", barX + 10, barY + 8, 0xFFFFFFFF);

        int resetW = 54;
        int resetX = barX + barW - resetW - 6;
        boolean resetHover = MathUtils.isHovered(mouseX, mouseY, resetX, barY + 4, resetW, 16);
        GlassRenderer.drawPillBadge(context, resetX, barY + 4, resetW, 16, 0xFFFFFFFF, resetHover);
        context.drawTextWithShadow(textRenderer, "Reset All", resetX + 6, barY + 8, resetHover ? 0xFFFFFFFF : theme.getTextSecondary());

        int doneW = 42;
        int doneX = resetX - doneW - 5;
        boolean doneHov = MathUtils.isHovered(mouseX, mouseY, doneX, barY + 4, doneW, 16);
        GlassRenderer.drawPillBadge(context, doneX, barY + 4, doneW, 16, 0xFFFFFFFF, true);
        context.drawTextWithShadow(textRenderer, "Done", doneX + 9, barY + 8, doneHov ? 0xFFFFFFFF : 0xFFE2E8F0);
    }

    private boolean isOverCornerHandle(HudModule widget, double mouseX, double mouseY) {
        float hx = widget.getPosX() + widget.getScaledWidth();
        float hy = widget.getPosY() + widget.getScaledHeight();
        return mouseX >= hx - 6 && mouseX <= hx + 6 && mouseY >= hy - 6 && mouseY <= hy + 6;
    }

    private void setWidgetScale(HudModule widget, double nextScale) {
        double clamped = MathUtils.clamp(nextScale, 0.5, 2.5);
        widget.getSettings().stream()
                .filter(s -> s.getId().equals("hud_scale"))
                .findFirst()
                .ifPresent(s -> {
                    if (s instanceof NumberSetting ns) {
                        ns.setValue(clamped);
                    }
                });
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int barW = 200;
        int barX = (this.width - barW) / 2;
        int resetX = barX + barW - 60;
        int doneX = resetX - 47;

        if (button == 0 && MathUtils.isHovered(mouseX, mouseY, doneX, 12, 42, 16)) { close(); return true; }
        if (button == 0 && MathUtils.isHovered(mouseX, mouseY, resetX, 12, 54, 16)) {
            BanglaClient.getInstance().getHudManager().resetAllPositions();
            return true;
        }

        List<HudModule> widgets = BanglaClient.getInstance().getModuleManager().getHudModules();
        for (int i = widgets.size() - 1; i >= 0; i--) {
            HudModule widget = widgets.get(i);
            if (!widget.isEnabled()) continue;

            // 1. Check Corner Resize Handle first
            if (button == 0 && isOverCornerHandle(widget, mouseX, mouseY)) {
                this.selectedWidget = widget;
                this.resizingWidget = widget;
                this.resizeStartMouseX = (float) mouseX;
                this.resizeStartScale = widget.getScale();
                return true;
            }

            // 2. Check Widget Body Drag or Right-Click Settings
            if (widget.isMouseOver(mouseX, mouseY)) {
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
        }

        this.selectedWidget = null;
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (resizingWidget != null && button == 0) {
            float deltaPixels = (float) mouseX - resizeStartMouseX;
            double newScale = resizeStartScale + (deltaPixels / Math.max(40.0f, resizingWidget.getWidth()));
            setWidgetScale(resizingWidget, newScale);
            HudManager.clampWidgetToScreen(resizingWidget, this.width, this.height);
            return true;
        }
        if (draggingWidget != null && button == 0) {
            float newX = (float) mouseX - dragOffsetX;
            float newY = (float) mouseY - dragOffsetY;

            float centerX = (this.width - draggingWidget.getScaledWidth()) * 0.5f;
            float centerY = (this.height - draggingWidget.getScaledHeight()) * 0.5f;
            if (Math.abs(newX - centerX) < SNAP_THRESHOLD) newX = centerX;
            if (Math.abs(newY - centerY) < SNAP_THRESHOLD) newY = centerY;
            if (Math.abs(newX - 6.0f) < SNAP_THRESHOLD) newX = 6.0f;
            if (Math.abs(newY - 6.0f) < SNAP_THRESHOLD) newY = 6.0f;

            draggingWidget.setPosition(newX, newY);
            HudManager.clampWidgetToScreen(draggingWidget, this.width, this.height);
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && (draggingWidget != null || resizingWidget != null)) {
            draggingWidget = null;
            resizingWidget = null;
            BanglaClient.getInstance().getConfigManager().saveHudLayoutConfig();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (HudModule widget : BanglaClient.getInstance().getModuleManager().getHudModules()) {
            if (widget.isEnabled() && widget.isMouseOver(mouseX, mouseY)) {
                setWidgetScale(widget, widget.getScale() + (verticalAmount > 0 ? 0.05 : -0.05));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

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

    @Override public boolean mouseClicked(net.minecraft.client.gui.Click c, boolean d) { return mouseClicked(c.x(), c.y(), c.button()) || super.mouseClicked(c, d); }
    @Override public boolean mouseDragged(net.minecraft.client.gui.Click c, double dx, double dy) { return mouseDragged(c.x(), c.y(), c.button(), dx, dy) || super.mouseDragged(c, dx, dy); }
    @Override public boolean mouseReleased(net.minecraft.client.gui.Click c) { return mouseReleased(c.x(), c.y(), c.button()) || super.mouseReleased(c); }
    @Override public boolean keyPressed(net.minecraft.client.input.KeyInput k) { return keyPressed(k.key(), k.scancode(), k.modifiers()) || super.keyPressed(k); }
    @Override public boolean charTyped(net.minecraft.client.input.CharInput ch) { return charTyped((char) ch.codepoint(), ch.modifiers()) || super.charTyped(ch); }
    public boolean charTyped(char c, int m) { return false; }
    @Override public boolean shouldPause() { return false; }
}
