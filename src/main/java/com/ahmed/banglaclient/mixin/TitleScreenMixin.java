package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.gui.AboutScreen;
import com.ahmed.banglaclient.gui.ClickGuiScreen;
import com.ahmed.banglaclient.gui.HudEditorScreen;
import com.ahmed.banglaclient.gui.TutorialCenterScreen;
import com.ahmed.banglaclient.integration.MainMenuButtonBridge;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.ColorUtils;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void banglaclient$onRenderTitleScreen(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getConfigManager() == null) return;
        if (!bangla.getConfigManager().isCustomMainMenuEnabled()) return;

        renderCinematicMinecraftLandscape(context, this.width, this.height);
        BlurRenderer.renderScreenBackdrop(context, this.width, this.height, 0.55f);

        Theme theme = GlassRenderer.getTheme();
        int accent = bangla.getThemeManager().getEffectiveAccentPrimary();

        // 1. FastClient-Style Top-Right Utility Dock: [👤 User] [⊞ GUI] [HUD] [🎨 Theme] [⚙ Opt] [ℹ SMP] [BC]
        int topY = 8, sq = 18, gap = 4;
        int rightCursor = this.width - 8;

        // [BC] Switch to Vanilla button (Rightmost accent square)
        rightCursor -= sq;
        boolean bcHov = MathUtils.isHovered(mouseX, mouseY, rightCursor, topY, sq, sq);
        context.fill(rightCursor, topY, rightCursor + sq, topY + sq, bcHov ? ColorUtils.scaleBrightness(accent, 1.2f) : accent);
        context.drawTextWithShadow(this.textRenderer, "BC", rightCursor + 3, topY + 5, 0xFFFFFFFF);

        String[] topIcons = {"ℹ", "⚙", "🎨", "⊞", "★"};
        for (String ic : topIcons) {
            rightCursor -= (sq + gap);
            boolean hov = MathUtils.isHovered(mouseX, mouseY, rightCursor, topY, sq, sq);
            GlassRenderer.drawGlassPanel(context, rightCursor, topY, sq, sq, true, hov, hov);
            int iw = this.textRenderer.getWidth(ic);
            context.drawTextWithShadow(this.textRenderer, ic, rightCursor + (sq - iw) / 2, topY + 5, hov ? accent : theme.getTextPrimary());
        }

        // Username Pill
        String username = this.client != null && this.client.getSession() != null ? this.client.getSession().getUsername() : "Player";
        int userW = Math.max(54, this.textRenderer.getWidth(username) + 18);
        rightCursor -= (userW + gap);
        GlassRenderer.drawGlassPanel(context, rightCursor, topY, userW, sq, true, false, false);
        context.drawTextWithShadow(this.textRenderer, "• " + username, rightCursor + 6, topY + 5, theme.getTextPrimary());

        // 2. Center 3D Brand Header ("বাংলা CLIENT")
        int centerX = this.width / 2;
        int startY = Math.max(30, (this.height - 168) / 2);

        org.joml.Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(centerX, startY - 8);
        matrices.scale(2.2f, 2.2f);
        String w1 = "Bangla ";
        String w2 = "Client";
        int totalLogoW = this.textRenderer.getWidth(w1 + w2);
        int lx = -totalLogoW / 2;
        int w1W = this.textRenderer.getWidth(w1);
        // 3D Extruded Depth Layers for "Bangla" (Smooth Premium Red)
        context.drawText(this.textRenderer, w1, lx + 2, 2, 0xFF450A0A, false);
        context.drawText(this.textRenderer, w1, lx + 1, 1, 0xFF991B1B, false);
        context.drawText(this.textRenderer, w1, lx, 0, 0xFFEF4444, false);
        // 3D Extruded Depth Layers for "Client" (Clean White)
        context.drawText(this.textRenderer, w2, lx + w1W + 2, 2, 0xFF1E293B, false);
        context.drawText(this.textRenderer, w2, lx + w1W + 1, 1, 0xFF64748B, false);
        context.drawText(this.textRenderer, w2, lx + w1W, 0, 0xFFFFFFFF, false);
        matrices.popMatrix();

        // 3. FastClient-Style Centered Vertical Button Stack
        int btnW = Math.min(156, this.width - 40);
        int btnH = 20;
        int btnX = centerX - btnW / 2;
        int btnY = startY + 22;

        drawFastButton(context, "Singleplayer", btnX, btnY, btnW, btnH, mouseX, mouseY, false, accent);
        btnY += 23;
        drawFastButton(context, "Multiplayer", btnX, btnY, btnW, btnH, mouseX, mouseY, false, accent);
        btnY += 23;
        drawFastButton(context, "Realms", btnX, btnY, btnW, btnH, mouseX, mouseY, false, accent);
        btnY += 23;
        drawFastButton(context, "Mods & Click GUI", btnX, btnY, btnW, btnH, mouseX, mouseY, false, accent);
        btnY += 23;
        drawFastButton(context, "Options", btnX, btnY, btnW, btnH, mouseX, mouseY, false, accent);
        btnY += 28;

        // Bottom Separate Accent-Filled Quit Button (FastClient signature style)
        int quitW = Math.min(118, btnW);
        int quitX = centerX - quitW / 2;
        drawFastButton(context, "Quit Game", quitX, btnY, quitW, btnH, mouseX, mouseY, true, accent);

        String verFooter = "BANGLA CLIENT " + BanglaClient.CLIENT_VERSION + " (Developed by Ahmed)";
        int vfW = this.textRenderer.getWidth(verFooter);
        context.drawTextWithShadow(this.textRenderer, verFooter, centerX - vfW / 2, btnY + 24, theme.getTextSecondary());

        // Bottom-Right Bangla SMP Badge (matching FastClient corner card placement)
        int smpW = 118, smpH = 28;
        int smpX = this.width - smpW - 8, smpY = this.height - smpH - 8;
        boolean smpHov = MathUtils.isHovered(mouseX, mouseY, smpX, smpY, smpW, smpH);
        GlassRenderer.drawGlassPanel(context, smpX, smpY, smpW, smpH, true, smpHov, smpHov);
        context.drawTextWithShadow(this.textRenderer, "BANGLA SMP", smpX + 8, smpY + 5, accent);
        context.drawTextWithShadow(this.textRenderer, "play.banglasmp.net", smpX + 8, smpY + 15, theme.getTextPrimary());

        // Third-party Mod Extensions (top-left compact)
        List<ClickableWidget> extList = MainMenuButtonBridge.collectThirdPartyButtons(this);
        int extY = 8;
        for (ClickableWidget ext : extList) {
            String lbl = ext.getMessage() != null ? ext.getMessage().getString() : "Mod";
            if (lbl.isBlank()) continue;
            int ew = Math.min(110, this.textRenderer.getWidth(lbl) + 12);
            drawFastButton(context, this.textRenderer.trimToWidth(lbl, 98), 8, extY, ew, 18, mouseX, mouseY, false, accent);
            extY += 20;
        }

        if (bangla.getNotificationManager() != null) {
            bangla.getNotificationManager().render(context, this.width, this.height);
        }
        ci.cancel();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void banglaclient$onRenderVanillaTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getConfigManager() != null && !bangla.getConfigManager().isCustomMainMenuEnabled()) {
            int accent = bangla.getThemeManager().getEffectiveAccentPrimary();
            drawFastButton(context, "BC Theme", this.width - 68, 8, 60, 18, mouseX, mouseY, true, accent);
        }
    }

    private void drawFastButton(DrawContext context, String text, int x, int y, int w, int h, int mx, int my, boolean solidAccent, int accent) {
        Theme theme = GlassRenderer.getTheme();
        boolean hov = MathUtils.isHovered(mx, my, x, y, w, h);
        if (solidAccent) {
            int fill = hov ? ColorUtils.withAlpha(accent, 0.88f) : ColorUtils.withAlpha(accent, 0.68f);
            context.fill(x + 1, y, x + w - 1, y + h, fill);
            context.fill(x, y + 1, x + 1, y + h - 1, fill);
            context.fill(x + w - 1, y + 1, x + w, y + h - 1, fill);
            GlassRenderer.drawOutlineBorder(context, x, y, w, h, ColorUtils.withAlpha(0xFFFFFFFF, hov ? 0.55f : 0.28f));
        } else {
            GlassRenderer.drawGlassPanel(context, x, y, w, h, true, hov, hov);
        }
        int tw = this.textRenderer.getWidth(text);
        context.drawTextWithShadow(this.textRenderer, text, x + (w - tw) / 2, y + (h - 8) / 2, 0xFFFFFFFF);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void banglaclient$onMouseClickedTitleScreen(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        double mouseX = click.x(), mouseY = click.y();
        if (click.button() != 0 || this.client == null) return;
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getConfigManager() == null) return;

        if (!bangla.getConfigManager().isCustomMainMenuEnabled()) {
            if (MathUtils.isHovered(mouseX, mouseY, this.width - 68, 8, 60, 18)) {
                bangla.getConfigManager().setCustomMainMenuEnabled(true);
                cir.setReturnValue(true);
            }
            return;
        }

        // Top-right icon bar clicks
        int topY = 8, sq = 18, gap = 4;
        int rc = this.width - 8 - sq;
        if (MathUtils.isHovered(mouseX, mouseY, rc, topY, sq, sq)) {
            bangla.getConfigManager().setCustomMainMenuEnabled(false);
            cir.setReturnValue(true); return;
        }
        rc -= (sq + gap);
        if (MathUtils.isHovered(mouseX, mouseY, rc, topY, sq, sq)) {
            this.client.setScreen(new AboutScreen(this));
            cir.setReturnValue(true); return;
        }
        rc -= (sq + gap);
        if (MathUtils.isHovered(mouseX, mouseY, rc, topY, sq, sq)) {
            this.client.setScreen(new OptionsScreen(this, this.client.options));
            cir.setReturnValue(true); return;
        }
        rc -= (sq + gap);
        if (MathUtils.isHovered(mouseX, mouseY, rc, topY, sq, sq)) {
            bangla.getThemeManager().cycleNextTheme();
            cir.setReturnValue(true); return;
        }
        rc -= (sq + gap);
        if (MathUtils.isHovered(mouseX, mouseY, rc, topY, sq, sq)) {
            this.client.setScreen(new ClickGuiScreen(false));
            cir.setReturnValue(true); return;
        }
        rc -= (sq + gap);
        if (MathUtils.isHovered(mouseX, mouseY, rc, topY, sq, sq)) {
            this.client.setScreen(new HudEditorScreen(this));
            cir.setReturnValue(true); return;
        }

        // Center stack clicks
        int centerX = this.width / 2;
        int startY = Math.max(30, (this.height - 168) / 2);
        int btnW = Math.min(156, this.width - 40);
        int btnH = 20;
        int btnX = centerX - btnW / 2;
        int btnY = startY + 22;

        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
            this.client.setScreen(new SelectWorldScreen(this));
            cir.setReturnValue(true); return;
        }
        btnY += 23;
        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
            this.client.setScreen(new MultiplayerScreen(this));
            cir.setReturnValue(true); return;
        }
        btnY += 23;
        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
            this.client.setScreen(new RealmsMainScreen(this));
            cir.setReturnValue(true); return;
        }
        btnY += 23;
        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
            this.client.setScreen(new ClickGuiScreen(false));
            cir.setReturnValue(true); return;
        }
        btnY += 23;
        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
            this.client.setScreen(new OptionsScreen(this, this.client.options));
            cir.setReturnValue(true); return;
        }
        btnY += 28;
        int quitW = Math.min(118, btnW);
        if (MathUtils.isHovered(mouseX, mouseY, centerX - quitW / 2, btnY, quitW, btnH)) {
            this.client.scheduleStop();
            cir.setReturnValue(true); return;
        }

        // Bangla SMP Corner Card Click -> Copy IP
        if (MathUtils.isHovered(mouseX, mouseY, this.width - 126, this.height - 36, 118, 28)) {
            if (this.client.keyboard != null) {
                this.client.keyboard.setClipboard("play.banglasmp.net");
                bangla.getNotificationManager().pushSuccess("Bangla SMP", "Copied play.banglasmp.net");
            }
            cir.setReturnValue(true); return;
        }

        // Third-party buttons
        List<ClickableWidget> extList = MainMenuButtonBridge.collectThirdPartyButtons(this);
        int extY = 8;
        for (ClickableWidget ext : extList) {
            String lbl = ext.getMessage() != null ? ext.getMessage().getString() : "Mod";
            if (lbl.isBlank()) continue;
            int ew = Math.min(110, this.textRenderer.getWidth(lbl) + 12);
            if (MathUtils.isHovered(mouseX, mouseY, 8, extY, ew, 18)) {
                ext.onClick(new Click(mouseX, mouseY, new MouseInput(0, 0)), false);
                cir.setReturnValue(true); return;
            }
            extY += 20;
        }
        cir.setReturnValue(true);
    }
}
