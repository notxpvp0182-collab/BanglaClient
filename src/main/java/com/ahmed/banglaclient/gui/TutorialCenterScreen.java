package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.gui.components.SmoothScrollContainer;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.ClipRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.tutorial.TutorialManager;
import com.ahmed.banglaclient.tutorial.TutorialStep;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.List;

public final class TutorialCenterScreen extends Screen {
    private final Screen parentScreen;
    private final SmoothScrollContainer sidebarScroll = new SmoothScrollContainer();
    private int winX, winY, winW, winH;

    public TutorialCenterScreen(Screen parentScreen) {
        super(Text.literal("BANGLA CLIENT Tutorial Center"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        this.winW = Math.min(600, this.width - 36);
        this.winH = Math.min(350, this.height - 32);
        this.winX = (this.width - this.winW) / 2;
        this.winY = (this.height - this.winH) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        BlurRenderer.renderScreenBackdrop(context, this.width, this.height, 1.0f);
        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary();
        TutorialManager tm = BanglaClient.getInstance().getTutorialManager();
        GlassRenderer.drawGlassPanel(context, winX, winY, winW, winH, false, false, true);
        context.drawTextWithShadow(textRenderer, "Tutorial Center — Step-by-Step Client Guide", winX + 14, winY + 12, theme.getTextPrimary());
        int closeX = winX + winW - 66;
        boolean closeHover = MathUtils.isHovered(mouseX, mouseY, closeX, winY + 8, 56, 18);
        GlassRenderer.drawPillBadge(context, closeX, winY + 8, 56, 18, closeHover ? accent : theme.getTextSecondary(), closeHover);
        context.drawTextWithShadow(textRenderer, "Close", closeX + 14, winY + 13, closeHover ? accent : theme.getTextPrimary());
        GlassRenderer.drawHorizontalDivider(context, winX + 10, winY + 32, winW - 20);

        int sideX = winX + 10, sideY = winY + 38, sideW = 180, sideH = winH - 48;
        GlassRenderer.drawGlassPanel(context, sideX, sideY, sideW, sideH, true, false, false);
        List<TutorialStep> steps = tm.getSteps();
        sidebarScroll.tickAndInterpolate();
        sidebarScroll.updateDimensions(sideH - 8, steps.size() * 22 + 8);
        ClipRenderer.renderClipped(context, sideX + 2, sideY + 4, sideW - 6, sideH - 8, () -> {
            int rowY = Math.round(sideY + 4 - sidebarScroll.getScrollOffset());
            for (int i = 0; i < steps.size(); i++) {
                boolean active = (i == tm.getCurrentStepIndex());
                boolean hovered = MathUtils.isHovered(mouseX, mouseY, sideX + 4, rowY, sideW - 12, 20);
                if (active || hovered) GlassRenderer.drawPillBadge(context, sideX + 4, rowY, sideW - 12, 20, accent, active);
                context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(steps.get(i).title(), sideW - 20), sideX + 8, rowY + 6, active ? accent : theme.getTextSecondary());
                rowY += 22;
            }
        });

        int contentX = sideX + sideW + 10, contentW = winW - sideW - 30;
        GlassRenderer.drawGlassPanel(context, contentX, sideY, contentW, sideH, true, false, false);
        TutorialStep current = tm.getCurrentStep();
        int textX = contentX + 14, textY = sideY + 14;
        context.drawTextWithShadow(textRenderer, current.title(), textX, textY, accent);
        textY += 18;
        for (OrderedText line : textRenderer.wrapLines(Text.literal(current.summary()), contentW - 28)) {
            context.drawTextWithShadow(textRenderer, line, textX, textY, theme.getTextSecondary());
            textY += 11;
        }
        textY += 8;
        for (String bullet : current.bulletInstructions()) {
            for (OrderedText line : textRenderer.wrapLines(Text.literal("• " + bullet), contentW - 32)) {
                context.drawTextWithShadow(textRenderer, line, textX + 4, textY, theme.getTextPrimary());
                textY += 12;
            }
        }
        int navY = sideY + sideH - 26;
        GlassRenderer.drawPillBadge(context, contentX + 12, navY, 68, 18, accent, false);
        context.drawTextWithShadow(textRenderer, "← Prev", contentX + 26, navY + 5, theme.getTextPrimary());
        GlassRenderer.drawPillBadge(context, contentX + contentW - 84, navY, 72, 18, accent, true);
        context.drawTextWithShadow(textRenderer, "Next →", contentX + contentW - 66, navY + 5, accent);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x(), mouseY = click.y();
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        TutorialManager tm = BanglaClient.getInstance().getTutorialManager();
        if (MathUtils.isHovered(mouseX, mouseY, winX + winW - 66, winY + 8, 56, 18)) { close(); return true; }
        int sideX = winX + 10, sideY = winY + 38, sideW = 180, sideH = winH - 48;
        int contentX = sideX + sideW + 10, contentW = winW - sideW - 30, navY = sideY + sideH - 26;
        if (MathUtils.isHovered(mouseX, mouseY, contentX + 12, navY, 68, 18)) { tm.previousStep(); return true; }
        if (MathUtils.isHovered(mouseX, mouseY, contentX + contentW - 84, navY, 72, 18)) { tm.nextStep(); return true; }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        return super.keyPressed(input);
    }

    @Override
    public void close() { if (this.client != null) this.client.setScreen(parentScreen); }
    @Override
    public boolean shouldPause() { return false; }
}
