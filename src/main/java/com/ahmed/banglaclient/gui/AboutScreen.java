package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.identity.BcIdentityManager;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

/**
 * Redesigned Premium AMOLED Aura Info Screen (100% Small-Caps, zero version clutter).
 */
public final class AboutScreen extends Screen {
    private final Screen parent;
    private final Animation openAnim = new Animation(0.0f, 180L, Easing.EASE_OUT_CUBIC);
    private boolean closing = false;
    private int winX, winY, winW, winH;

    public AboutScreen(Screen parent) {
        super(Text.literal("Client Info"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.winW = 286;
        this.winH = 182;
        this.winX = (this.width - this.winW) / 2;
        this.winY = (this.height - this.winH) / 2;
        this.closing = false;
        this.openAnim.snapTo(0.0f);
        this.openAnim.animateTo(1.0f);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float anim = openAnim.getValue();
        if (closing && anim <= 0.02f) {
            if (this.client != null) this.client.setScreen(parent);
            return;
        }

        BlurRenderer.renderScreenBackdrop(context, this.width, this.height, anim);

        Matrix3x2fStack matrices = context.getMatrices();
        float scale = 0.92f + 0.08f * anim;
        matrices.pushMatrix();
        matrices.translate(this.width * 0.5f, this.height * 0.5f);
        matrices.scale(scale, scale);
        matrices.translate(-this.width * 0.5f, -this.height * 0.5f);

        try {
            GlassRenderer.drawGlassPanel(context, winX, winY, winW, winH, false, false, true);

            int cx = winX + winW / 2;
            BcIdentityManager.drawIdentityIcon(context, cx - 12, winY + 10, 24, 24);

            String brand = GlassRenderer.toSmallCaps("BANGLA CLIENT");
            context.drawTextWithShadow(textRenderer, brand, cx - textRenderer.getWidth(brand) / 2, winY + 37, 0xFFFFFFFF);

            String dev = GlassRenderer.toSmallCaps("Developed by Ahmed");
            context.drawTextWithShadow(textRenderer, dev, cx - textRenderer.getWidth(dev) / 2, winY + 48, 0xFF94A3B8);

            GlassRenderer.drawHorizontalDivider(context, winX + 14, winY + 61, winW - 28);

            // Clean Info Cards
            int cardW = (winW - 34) / 2;
            GlassRenderer.drawGlassPanel(context, winX + 14, winY + 67, cardW, 32, true, false, false);
            GlassRenderer.drawSmallCapsText(context, textRenderer, "Platform", winX + 20, winY + 72, 0xFF94A3B8);
            GlassRenderer.drawSmallCapsText(context, textRenderer, "Fabric 1.21.11", winX + 20, winY + 84, 0xFFFFFFFF);

            GlassRenderer.drawGlassPanel(context, winX + 20 + cardW, winY + 67, cardW, 32, true, false, false);
            GlassRenderer.drawSmallCapsText(context, textRenderer, "Architecture", winX + 26 + cardW, winY + 72, 0xFF94A3B8);
            GlassRenderer.drawSmallCapsText(context, textRenderer, "AMOLED Aura Engine", winX + 26 + cardW, winY + 84, 0xFFFFFFFF);

            // Official Server Card
            int srvY = winY + 105;
            GlassRenderer.drawGlassPanel(context, winX + 14, srvY, winW - 28, 36, true, false, true);
            GlassRenderer.drawSmallCapsText(context, textRenderer, "Official Community Server", winX + 22, srvY + 6, 0xFF94A3B8);
            GlassRenderer.drawSmallCapsText(context, textRenderer, "play.banglasmp.net", winX + 22, srvY + 19, 0xFFFFFFFF);

            int copyW = 68, copyH = 18;
            int copyX = winX + winW - 22 - copyW;
            boolean copyHov = MathUtils.isHovered(mouseX, mouseY, copyX, srvY + 9, copyW, copyH);
            GlassRenderer.drawPillBadge(context, copyX, srvY + 9, copyW, copyH, 0xFFFFFFFF, copyHov || true);
            String cpTxt = GlassRenderer.toSmallCaps("Copy IP");
            context.drawTextWithShadow(textRenderer, cpTxt, copyX + (copyW - textRenderer.getWidth(cpTxt)) / 2, srvY + 14, 0xFFFFFFFF);

            // Bottom Return Button
            int retW = 104, retH = 20;
            int retX = cx - retW / 2;
            int retY = winY + winH - 28;
            boolean retHov = MathUtils.isHovered(mouseX, mouseY, retX, retY, retW, retH);
            GlassRenderer.drawPillBadge(context, retX, retY, retW, retH, 0xFFFFFFFF, retHov);
            String retTxt = GlassRenderer.toSmallCaps("← Return");
            context.drawTextWithShadow(textRenderer, retTxt, cx - textRenderer.getWidth(retTxt) / 2, retY + 6, 0xFFFFFFFF);
        } finally {
            matrices.popMatrix();
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x(), mouseY = click.y();
        if (click.button() != 0 || closing) return super.mouseClicked(click, doubled);

        int srvY = winY + 105;
        int copyW = 68, copyH = 18;
        int copyX = winX + winW - 22 - copyW;
        if (MathUtils.isHovered(mouseX, mouseY, copyX, srvY + 9, copyW, copyH)) {
            GlassRenderer.playClickSound();
            if (this.client != null && this.client.keyboard != null) {
                this.client.keyboard.setClipboard("play.banglasmp.net");
            }
            if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
                BanglaClient.getInstance().getNotificationManager().pushSuccess("Server IP Copied", "play.banglasmp.net");
            }
            return true;
        }

        int cx = winX + winW / 2;
        int retW = 104, retH = 20;
        if (MathUtils.isHovered(mouseX, mouseY, cx - retW / 2, winY + winH - 28, retW, retH)) {
            GlassRenderer.playClickSound();
            close();
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        if (!closing) {
            closing = true;
            openAnim.animateTo(0.0f);
        }
    }

    @Override
    public boolean shouldPause() { return false; }
}
