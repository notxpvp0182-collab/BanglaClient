package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.gui.AboutScreen;
import com.ahmed.banglaclient.gui.ClickGuiScreen;
import com.ahmed.banglaclient.gui.HudEditorScreen;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.ColorUtils;
import com.ahmed.banglaclient.util.MathUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    private static final Identifier BG_IMAGE = Identifier.of("banglaclient", "textures/gui/title_background.png");

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void banglaclient$onRenderTitleScreen(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getConfigManager() == null) return;
        if (!bangla.getConfigManager().isCustomMainMenuEnabled()) return;

        int w = this.width;
        int h = this.height;

        // 1. Fullscreen Cherry Blossom Japanese Temple Landscape Background
        try {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, BG_IMAGE, 0, 0, 0.0f, 0.0f, w, h, w, h);
        } catch (Throwable t) {
            this.renderPanoramaBackground(context, delta);
        }

        // Subtle dark vignette to maximize contrast behind Liquid Glass
        context.fillGradient(0, 0, w, h, 0x44000000, 0x77080A10);

        Theme theme = GlassRenderer.getTheme();
        int accent = bangla.getThemeManager().getEffectiveAccentPrimary();
        int centerX = w / 2;

        // 2. Top-Left Icons: [🧩 Mods] and [👤 In-Game Account Switcher] (NO TEXT, ICONS ONLY)
        int iconSq = 20, iconGap = 5;
        int tlX = 10, tlY = 10;

        boolean modsHov = MathUtils.isHovered(mouseX, mouseY, tlX, tlY, iconSq, iconSq);
        drawPillCapsule(context, tlX, tlY, iconSq, iconSq, 0x40121622, 0x55080A10, modsHov ? 0xFFFFFFFF : 0x77FFFFFF, modsHov);
        context.drawTextWithShadow(this.textRenderer, "🧩", tlX + 5, tlY + 6, modsHov ? accent : 0xFFFFFFFF);

        boolean acctHov = MathUtils.isHovered(mouseX, mouseY, tlX + iconSq + iconGap, tlY, iconSq, iconSq);
        drawPillCapsule(context, tlX + iconSq + iconGap, tlY, iconSq, iconSq, 0x40121622, 0x55080A10, acctHov ? 0xFFFFFFFF : 0x77FFFFFF, acctHov);
        context.drawTextWithShadow(this.textRenderer, "👤", tlX + iconSq + iconGap + 5, tlY + 6, acctHov ? accent : 0xFFFFFFFF);

        // 3. Top-Right Dock: • <Username> and [★] [⊞] [👁] [⚙] [BC]
        int trSq = 18, trGap = 4;
        int curRight = w - 10;

        // [BC] Toggle
        curRight -= trSq;
        boolean bcHov = MathUtils.isHovered(mouseX, mouseY, curRight, 10, trSq, trSq);
        context.fill(curRight, 10, curRight + trSq, 10 + trSq, bcHov ? ColorUtils.scaleBrightness(accent, 1.2f) : accent);
        context.drawTextWithShadow(this.textRenderer, "BC", curRight + 3, 15, 0xFFFFFFFF);

        String[] trIcons = {"⚙", "👁", "⊞", "★"};
        for (String ic : trIcons) {
            curRight -= (trSq + trGap);
            boolean hov = MathUtils.isHovered(mouseX, mouseY, curRight, 10, trSq, trSq);
            drawPillCapsule(context, curRight, 10, trSq, trSq, 0x40121622, 0x55080A10, hov ? 0xFFFFFFFF : 0x77FFFFFF, hov);
            int iw = this.textRenderer.getWidth(ic);
            context.drawTextWithShadow(this.textRenderer, ic, curRight + (trSq - iw) / 2, 15, hov ? accent : 0xFFFFFFFF);
        }

        String username = this.client != null && this.client.getSession() != null ? this.client.getSession().getUsername() : "Player";
        String userLabel = "• " + username;
        int userW = this.textRenderer.getWidth(userLabel) + 16;
        curRight -= (userW + trGap);
        drawPillCapsule(context, curRight, 10, userW, trSq, 0x40121622, 0x55080A10, 0x66FFFFFF, false);
        context.drawTextWithShadow(this.textRenderer, userLabel, curRight + 8, 15, 0xFFFFFFFF);

        // 4. Large 3D Centered "Bangla Client" Logo with Golden Crown & Glowing Red Rings
        int logoCenterY = Math.max(34, (h - 200) / 2);

        // Golden Crown over "Bangla"
        drawGoldenCrown(context, centerX - 56, logoCenterY - 18);

        // Orbiting glowing red energy rings around "Bangla"
        drawRedEnergyRings(context, centerX, logoCenterY + 4, accent);

        // 3D "Bangla" in Vibrant Extruded Red
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(centerX, logoCenterY);
        matrices.scale(2.8f, 2.8f);
        String txtBangla = "Bangla";
        int bw = this.textRenderer.getWidth(txtBangla);
        int bx = -bw / 2;
        context.drawText(this.textRenderer, txtBangla, bx + 2, 2, 0xFF2D0404, false);
        context.drawText(this.textRenderer, txtBangla, bx + 1, 1, 0xFF7F1D1D, false);
        context.drawTextWithShadow(this.textRenderer, txtBangla, bx, 0, 0xFFEF4444);
        matrices.popMatrix();

        // 3D "Client" in Bold Extruded White
        matrices.pushMatrix();
        matrices.translate(centerX, logoCenterY + 23);
        matrices.scale(2.4f, 2.4f);
        String txtClient = "Client";
        int cw = this.textRenderer.getWidth(txtClient);
        int cx = -cw / 2;
        context.drawText(this.textRenderer, txtClient, cx + 2, 2, 0xFF0F172A, false);
        context.drawText(this.textRenderer, txtClient, cx + 1, 1, 0xFF475569, false);
        context.drawTextWithShadow(this.textRenderer, txtClient, cx, 0, 0xFFFFFFFF);
        matrices.popMatrix();

        // 5. Centered Rounded Glass Capsule Button Stack
        int btnW = Math.min(182, w - 40);
        int btnH = 20;
        int btnX = centerX - btnW / 2;
        int btnY = logoCenterY + 54;

        drawPillMenuButton(context, "👥", "Singleplayer", btnX, btnY, btnW, btnH, mouseX, mouseY, false, accent);
        btnY += 22;
        drawPillMenuButton(context, "📖", "Multiplayer", btnX, btnY, btnW, btnH, mouseX, mouseY, false, accent);
        btnY += 22;
        drawPillMenuButton(context, "🏰", "Realms", btnX, btnY, btnW, btnH, mouseX, mouseY, false, accent);
        btnY += 22;
        drawPillMenuButton(context, "🥼", "Cosmetics", btnX, btnY, btnW, btnH, mouseX, mouseY, false, accent);
        btnY += 22;
        drawPillMenuButton(context, "⚙", "Options", btnX, btnY, btnW, btnH, mouseX, mouseY, false, accent);
        btnY += 25;

        // Quit Game Button (Solid Glowing Red Glass Capsule)
        int quitW = Math.min(142, btnW);
        int quitX = centerX - quitW / 2;
        drawPillMenuButton(context, "", "Quit Game", quitX, btnY, quitW, btnH, mouseX, mouseY, true, accent);

        // 6. Bottom Center Credits & Account
        String verStr = "BANGLA CLIENT " + BanglaClient.CLIENT_VERSION + " (Developed by Ahmed)";
        int vw = this.textRenderer.getWidth(verStr);
        context.drawTextWithShadow(this.textRenderer, verStr, centerX - vw / 2, h - 23, 0xFFFFFFFF);

        String currentAcct = "Current Account: " + username;
        int aw = this.textRenderer.getWidth(currentAcct);
        context.drawTextWithShadow(this.textRenderer, currentAcct, centerX - aw / 2, h - 12, 0xFFEF4444);

        // 7. Bottom Right: BANGLA SMP Server Card
        int smpW = 120, smpH = 28;
        int smpX = w - smpW - 10, smpY = h - smpH - 10;
        boolean smpHov = MathUtils.isHovered(mouseX, mouseY, smpX, smpY, smpW, smpH);
        drawPillCapsule(context, smpX, smpY, smpW, smpH, 0x55080A10, 0x66080A10, smpHov ? 0xFFEF4444 : 0x88EF4444, smpHov);
        context.drawTextWithShadow(this.textRenderer, "BANGLA SMP", smpX + 10, smpY + 5, 0xFFEF4444);
        context.drawTextWithShadow(this.textRenderer, "play.banglasmp.net", smpX + 10, smpY + 16, 0xFFFFFFFF);

        if (bangla.getNotificationManager() != null) {
            bangla.getNotificationManager().render(context, w, h);
        }
        ci.cancel();
    }

    private void drawGoldenCrown(DrawContext context, int x, int y) {
        // Pixel-art golden crown matching the reference image over 'Bangla'
        context.fill(x + 2, y, x + 5, y + 3, 0xFFF59E0B);
        context.fill(x + 9, y - 3, x + 12, y + 3, 0xFFF59E0B);
        context.fill(x + 16, y, x + 19, y + 3, 0xFFF59E0B);
        context.fill(x, y + 3, x + 21, y + 9, 0xFFD97706);
        context.fill(x + 2, y + 4, x + 19, y + 8, 0xFFFBBF24);
        // Crown jewels
        context.fill(x + 3, y + 5, x + 5, y + 7, 0xFFEF4444);
        context.fill(x + 9, y + 4, x + 12, y + 7, 0xFF38BDF8);
        context.fill(x + 16, y + 5, x + 18, y + 7, 0xFFEF4444);
    }

    private void drawRedEnergyRings(DrawContext context, int cx, int cy, int accent) {
        // Red glowing orbital aura ellipses
        int rx = 76, ry = 18;
        int ringColor = ColorUtils.withAlpha(accent, 0.40f);
        context.fill(cx - rx, cy - 1, cx + rx, cy + 1, ringColor);
        context.fill(cx - rx + 4, cy - 2, cx + rx - 4, cy + 2, ColorUtils.withAlpha(accent, 0.22f));
        context.fill(cx - rx - 3, cy, cx - rx + 3, cy + 2, 0xFFEF4444);
        context.fill(cx + rx - 3, cy, cx + rx + 3, cy + 2, 0xFFEF4444);
    }

    private void drawPillCapsule(DrawContext context, int x, int y, int w, int h, int bgTop, int bgBot, int borderCol, boolean hov) {
        context.fillGradient(x + 2, y, x + w - 2, y + h, bgTop, bgBot);
        context.fillGradient(x + 1, y + 1, x + 2, y + h - 1, bgTop, bgBot);
        context.fillGradient(x + w - 2, y + 1, x + w - 1, y + h - 1, bgTop, bgBot);
        context.fill(x, y + 2, x + 1, y + h - 2, bgTop);
        context.fill(x + w - 1, y + 2, x + w, y + h - 2, bgTop);
        // Top specular reflection
        context.fill(x + 3, y + 1, x + w - 3, y + 2, 0x44FFFFFF);
        GlassRenderer.drawOutlineBorder(context, x, y, w, h, borderCol);
    }

    private void drawPillMenuButton(DrawContext context, String icon, String text, int x, int y, int w, int h, int mx, int my, boolean isQuit, int accent) {
        boolean hov = MathUtils.isHovered(mx, my, x, y, w, h);
        int bgTop, bgBot, borderCol;
        if (isQuit) {
            bgTop = hov ? 0xE8EF4444 : 0xCFDC2626;
            bgBot = hov ? 0xE8B91C1C : 0xCF991B1B;
            borderCol = hov ? 0xFFFFFFFF : 0x99FFFFFF;
        } else {
            bgTop = hov ? 0x55FFFFFF : 0x38181A24;
            bgBot = hov ? 0x35FFFFFF : 0x4A0C0E14;
            borderCol = hov ? 0xFFFFFFFF : 0x66FFFFFF;
        }

        drawPillCapsule(context, x, y, w, h, bgTop, bgBot, borderCol, hov);

        if (!icon.isEmpty()) {
            context.drawTextWithShadow(this.textRenderer, icon, x + 14, y + (h - 8) / 2, 0xFFFFFFFF);
        }
        int tw = this.textRenderer.getWidth(text);
        context.drawTextWithShadow(this.textRenderer, text, x + (w - tw) / 2, y + (h - 8) / 2, 0xFFFFFFFF);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void banglaclient$onRenderVanillaTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getConfigManager() != null && !bangla.getConfigManager().isCustomMainMenuEnabled()) {
            int accent = bangla.getThemeManager().getEffectiveAccentPrimary();
            drawPillMenuButton(context, "", "BC Theme", this.width - 68, 8, 60, 18, mouseX, mouseY, true, accent);
        }
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

        int w = this.width;
        int h = this.height;
        int centerX = w / 2;

        // 1. Top-Left Icons: [🧩 Mods] and [👤 IAS]
        int iconSq = 20, iconGap = 5;
        if (MathUtils.isHovered(mouseX, mouseY, 10, 10, iconSq, iconSq)) {
            openModMenuScreen();
            cir.setReturnValue(true);
            return;
        }
        if (MathUtils.isHovered(mouseX, mouseY, 10 + iconSq + iconGap, 10, iconSq, iconSq)) {
            openAccountSwitcherScreen();
            cir.setReturnValue(true);
            return;
        }

        // 2. Top-Right Icons: [BC] [⚙] [👁] [⊞] [★]
        int trSq = 18, trGap = 4;
        int curRight = w - 10 - trSq;
        if (MathUtils.isHovered(mouseX, mouseY, curRight, 10, trSq, trSq)) {
            bangla.getConfigManager().setCustomMainMenuEnabled(false);
            cir.setReturnValue(true);
            return;
        }
        curRight -= (trSq + trGap);
        if (MathUtils.isHovered(mouseX, mouseY, curRight, 10, trSq, trSq)) {
            this.client.setScreen(new OptionsScreen(this, this.client.options));
            cir.setReturnValue(true);
            return;
        }
        curRight -= (trSq + trGap);
        if (MathUtils.isHovered(mouseX, mouseY, curRight, 10, trSq, trSq)) {
            this.client.setScreen(new HudEditorScreen(this));
            cir.setReturnValue(true);
            return;
        }
        curRight -= (trSq + trGap);
        if (MathUtils.isHovered(mouseX, mouseY, curRight, 10, trSq, trSq)) {
            this.client.setScreen(new ClickGuiScreen(false));
            cir.setReturnValue(true);
            return;
        }
        curRight -= (trSq + trGap);
        if (MathUtils.isHovered(mouseX, mouseY, curRight, 10, trSq, trSq)) {
            this.client.setScreen(new ClickGuiScreen(false));
            cir.setReturnValue(true);
            return;
        }

        // 3. Center Capsule Buttons
        int logoCenterY = Math.max(34, (h - 200) / 2);
        int btnW = Math.min(182, w - 40);
        int btnH = 20;
        int btnX = centerX - btnW / 2;
        int btnY = logoCenterY + 54;

        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
            this.client.setScreen(new SelectWorldScreen(this));
            cir.setReturnValue(true); return;
        }
        btnY += 22;
        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
            this.client.setScreen(new MultiplayerScreen(this));
            cir.setReturnValue(true); return;
        }
        btnY += 22;
        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
            this.client.setScreen(new RealmsMainScreen(this));
            cir.setReturnValue(true); return;
        }
        btnY += 22;
        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
            this.client.setScreen(new HudEditorScreen(this));
            cir.setReturnValue(true); return;
        }
        btnY += 22;
        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
            this.client.setScreen(new OptionsScreen(this, this.client.options));
            cir.setReturnValue(true); return;
        }
        btnY += 25;

        // Quit Button
        int quitW = Math.min(142, btnW);
        if (MathUtils.isHovered(mouseX, mouseY, centerX - quitW / 2, btnY, quitW, btnH)) {
            this.client.scheduleStop();
            cir.setReturnValue(true); return;
        }

        // 4. Bottom-Right BANGLA SMP Server Card Click
        if (MathUtils.isHovered(mouseX, mouseY, w - 130, h - 38, 120, 28)) {
            if (this.client.keyboard != null) {
                this.client.keyboard.setClipboard("play.banglasmp.net");
                bangla.getNotificationManager().pushSuccess("Bangla SMP", "Copied play.banglasmp.net");
            }
            cir.setReturnValue(true); return;
        }

        cir.setReturnValue(true);
    }

    private void openModMenuScreen() {
        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            try {
                Class<?> screenClass = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
                Constructor<?> ctor = screenClass.getConstructor(Screen.class);
                this.client.setScreen((Screen) ctor.newInstance(this));
                return;
            } catch (Throwable ignored) {}
        }
        this.client.setScreen(new ClickGuiScreen(false));
    }

    private void openAccountSwitcherScreen() {
        if (FabricLoader.getInstance().isModLoaded("ias")) {
            try {
                Class<?> iasClass = Class.forName("com.github.theodore_m.in_game_account_switcher.client.gui.GuiAccountSwitcher");
                Constructor<?> ctor = iasClass.getConstructor(Screen.class);
                this.client.setScreen((Screen) ctor.newInstance(this));
                return;
            } catch (Throwable ignored) {}
        }
        String user = this.client.getSession() != null ? this.client.getSession().getUsername() : "Player";
        BanglaClient.getInstance().getNotificationManager().pushInfo("Account Switcher", "Logged in as: " + user);
    }
}
