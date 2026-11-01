package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Dedicated About Screen displaying BANGLA CLIENT branding, version, and developer credit ("Developed by Ahmed").
 */
public final class AboutScreen extends Screen {
    private final Screen parentScreen;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;

    public AboutScreen(Screen parentScreen) {
        super(Text.literal("About " + BanglaClient.CLIENT_NAME));
        this.parentScreen = parentScreen;
    }

    
    protected void init() {
        this.panelW = 360;
        this.panelH = 210;
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;
    }

    
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        BlurRenderer.renderScreenBackdrop(context, this.width, this.height, 1.0f);
        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : theme.getAccentPrimary();

        GlassRenderer.drawGlassPanel(context, panelX, panelY, panelW, panelH, true, false, true);

        int cx = panelX + 24;
        int cy = panelY + 24;

        context.drawTextWithShadow(textRenderer, BanglaClient.CLIENT_NAME, cx, cy, accent);
        context.drawTextWithShadow(textRenderer, "Version: " + BanglaClient.CLIENT_VERSION, cx, cy + 16, theme.getTextPrimary());
        context.drawTextWithShadow(textRenderer, "Developed by " + BanglaClient.DEVELOPER, cx, cy + 32, theme.getTextPrimary());

        GlassRenderer.drawHorizontalDivider(context, cx, cy + 50, panelW - 48);

        context.drawTextWithShadow(textRenderer, "Target Edition: Minecraft Java 1.21.11 (Fabric)", cx, cy + 62, theme.getTextSecondary());
        context.drawTextWithShadow(textRenderer, "Design System: Dark Transparent Liquid Glass", cx, cy + 76, theme.getTextSecondary());
        context.drawTextWithShadow(textRenderer, "Modules Included: 120 Utility & Customization Modules", cx, cy + 90, theme.getTextSecondary());
        context.drawTextWithShadow(textRenderer, "Legitimate Client-Side HUD, Visual & QoL Suite.", cx, cy + 106, theme.getTextMuted());

        // Close Button
        int btnW = 88;
        int btnH = 20;
        int btnX = panelX + (panelW - btnW) / 2;
        int btnY = panelY + panelH - 30;
        boolean hovered = MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH);
        GlassRenderer.drawGlassPanel(context, btnX, btnY, btnW, btnH, true, hovered, hovered);
        String closeLabel = "← Return";
        int lw = textRenderer.getWidth(closeLabel);
        context.drawTextWithShadow(textRenderer, closeLabel, btnX + (btnW - lw) / 2, btnY + 6,
                hovered ? accent : theme.getTextPrimary());
    }

    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int btnW = 88;
        int btnH = 20;
        int btnX = panelX + (panelW - btnW) / 2;
        int btnY = panelY + panelH - 30;
        if (button == 0 && MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
            close();
            return true;
        }
        return false;
    }

    
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return false;
    }

    
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parentScreen);
        }
    }

    
         public boolean mouseClicked(net.minecraft.client.gui.Click c, boolean d) { return mouseClicked(c.x(), c.y(), c.button()) || super.mouseClicked(c, d); }
     public boolean mouseDragged(net.minecraft.client.gui.Click c, double dx, double dy) { return mouseDragged(c.x(), c.y(), c.button(), dx, dy) || super.mouseDragged(c, dx, dy); }
     public boolean mouseReleased(net.minecraft.client.gui.Click c) { return mouseReleased(c.x(), c.y(), c.button()) || super.mouseReleased(c); }
     public boolean keyPressed(net.minecraft.client.input.KeyInput k) { return keyPressed(k.key(), k.scancode(), k.modifiers()) || super.keyPressed(k); }
     public boolean charTyped(net.minecraft.client.input.CharInput ch) { return charTyped((char) ch.codepoint(), ch.modifiers()) || super.charTyped(ch); }
    public boolean mouseDragged(double mx, double my, int b, double dx, double dy) { return false; }
    public boolean mouseReleased(double mx, double my, int b) { return false; }
    public boolean charTyped(char c, int m) { return false; }
    public boolean shouldPause() {
        return false;
    }
}
