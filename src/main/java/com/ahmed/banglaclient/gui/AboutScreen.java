package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.identity.BcIdentityManager;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

/**
 * 2D Aura Dark Developer & Client Info Screen.
 */
public final class AboutScreen extends Screen {
    private final Screen parentScreen;
    private final Animation openAnim = new Animation(0.0f, 150L, Easing.EASE_OUT_CUBIC);
    private boolean closing = false;
    private int panelX, panelY, panelW, panelH;

    public AboutScreen(Screen parentScreen) {
        super(Text.literal("Info"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        this.panelW = Math.min(310, this.width - 30);
        this.panelH = Math.min(185, this.height - 24);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;
        this.closing = false;
        this.openAnim.snapTo(0.0f);
        this.openAnim.animateTo(1.0f);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float anim = openAnim.getValue();
        if (closing && anim <= 0.02f) {
            if (this.client != null) this.client.setScreen(parentScreen);
            return;
        }

        BlurRenderer.renderScreenBackdrop(context, this.width, this.height, anim);
        Theme theme = GlassRenderer.getTheme();

        Matrix3x2fStack matrices = context.getMatrices();
        float scale = 0.94f + 0.06f * anim;
        matrices.pushMatrix();
        matrices.translate(this.width * 0.5f, this.height * 0.5f);
        matrices.scale(scale, scale);
        matrices.translate(-this.width * 0.5f, -this.height * 0.5f);

        try {
            GlassRenderer.drawGlassPanel(context, panelX, panelY, panelW, panelH, true, false, true);

            int cx = panelX + 16;
            int cy = panelY + 14;

            BcIdentityManager.drawIdentityIcon(context, cx, cy, 16, 16);
            context.drawTextWithShadow(textRenderer, BanglaClient.CLIENT_NAME + " v" + BanglaClient.CLIENT_VERSION, cx + 22, cy + 1, 0xFFFFFFFF);
            context.drawTextWithShadow(textRenderer, "Developed by Ahmed (Minecraft Player & Developer)", cx + 22, cy + 11, theme.getTextSecondary());

            GlassRenderer.drawHorizontalDivider(context, cx, cy + 25, panelW - 32);

            context.drawTextWithShadow(textRenderer, "• Support: All Minecraft Versions Compatible", cx, cy + 34, 0xFFFFFFFF);
            context.drawTextWithShadow(textRenderer, "• Modules: 136 High-Performance Utility & PvP Mods", cx, cy + 47, theme.getTextSecondary());
            context.drawTextWithShadow(textRenderer, "• Architecture: 2D Aura Dark Edition (Zero-Lag HD)", cx, cy + 60, theme.getTextSecondary());
            context.drawTextWithShadow(textRenderer, "• Official Server: play.banglasmp.net", cx, cy + 76, 0xFFFFFFFF);

            int cpX = cx, cpY = cy + 92, cpW = 90, cpH = 17;
            boolean cpHov = MathUtils.isHovered(mouseX, mouseY, cpX, cpY, cpW, cpH);
            GlassRenderer.drawPillBadge(context, cpX, cpY, cpW, cpH, 0xFFFFFFFF, cpHov || true);
            context.drawTextWithShadow(textRenderer, "Copy Server IP", cpX + (cpW - textRenderer.getWidth("Copy Server IP")) / 2, cpY + 5, 0xFFFFFFFF);

            int btnW = 84, btnH = 18;
            int btnX = panelX + (panelW - btnW) / 2;
            int btnY = panelY + panelH - 26;
            boolean hovered = MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH);
            GlassRenderer.drawGlassPanel(context, btnX, btnY, btnW, btnH, true, hovered, hovered);
            String closeLabel = "← Return";
            context.drawTextWithShadow(textRenderer, closeLabel, btnX + (btnW - textRenderer.getWidth(closeLabel)) / 2, btnY + 5, 0xFFFFFFFF);
        } finally {
            matrices.popMatrix();
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (closing) return false;
        int cx = panelX + 16;
        int cy = panelY + 14;
        if (button == 0 && MathUtils.isHovered(mouseX, mouseY, cx, cy + 92, 90, 17)) {
            if (this.client != null && this.client.keyboard != null) {
                this.client.keyboard.setClipboard("play.banglasmp.net");
                if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
                    BanglaClient.getInstance().getNotificationManager().pushSuccess("Bangla SMP", "Copied play.banglasmp.net");
                }
            }
            return true;
        }
        int btnW = 84, btnH = 18;
        int btnX = panelX + (panelW - btnW) / 2;
        int btnY = panelY + panelH - 26;
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

    @Override
    public void close() {
        if (!closing) {
            closing = true;
            openAnim.animateTo(0.0f);
        }
    }

    @Override public boolean mouseClicked(net.minecraft.client.gui.Click c, boolean d) { return mouseClicked(c.x(), c.y(), c.button()) || super.mouseClicked(c, d); }
    @Override public boolean mouseDragged(net.minecraft.client.gui.Click c, double dx, double dy) { return false; }
    @Override public boolean mouseReleased(net.minecraft.client.gui.Click c) { return false; }
    @Override public boolean keyPressed(net.minecraft.client.input.KeyInput k) { return keyPressed(k.key(), k.scancode(), k.modifiers()) || super.keyPressed(k); }
    @Override public boolean charTyped(net.minecraft.client.input.CharInput ch) { return false; }
    public boolean mouseDragged(double mx, double my, int b, double dx, double dy) { return false; }
    public boolean mouseReleased(double mx, double my, int b) { return false; }
    public boolean charTyped(char c, int m) { return false; }
    @Override public boolean shouldPause() { return false; }
}
