package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.gui.AboutScreen;
import com.ahmed.banglaclient.gui.ClickGuiScreen;
import com.ahmed.banglaclient.gui.HudEditorScreen;
import com.ahmed.banglaclient.integration.MainMenuButtonBridge;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.ColorUtils;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.MouseInput;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Locale;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void banglaclient$renderCustomInGameMenu(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null) return;

        int w = this.width;
        int h = this.height;

        // In-game world remains visible through blurred transparent darkening (NO static background image)
        BlurRenderer.renderScreenBackdrop(context, w, h, 0.72f);

        Theme theme = GlassRenderer.getTheme();
        int accent = bangla.getThemeManager().getEffectiveAccentPrimary();
        int centerX = w / 2;

        // 1. Top-Left Mod Tiles (Non-Emoji Vector Icons)
        List<ClickableWidget> externalButtons = MainMenuButtonBridge.collectThirdPartyButtons(this);
        int tlX = 10, tlY = 10, iconSq = 20, iconGap = 5;
        for (int i = 0; i < externalButtons.size(); i++) {
            ClickableWidget ext = externalButtons.get(i);
            int btnX = tlX + (i * (iconSq + iconGap));
            boolean hov = MathUtils.isHovered(mouseX, mouseY, btnX, tlY, iconSq, iconSq);
            drawPillCapsule(context, btnX, tlY, iconSq, iconSq, 0x40121622, 0x55080A10, hov ? 0xFFFFFFFF : 0x77FFFFFF, hov);
            String label = ext.getMessage() != null ? ext.getMessage().getString().toLowerCase(Locale.ROOT) : "";
            int iconColor = hov ? accent : 0xFFFFFFFF;
            if (label.contains("mod")) {
                drawVectorPuzzleIcon(context, btnX + 3, tlY + 3, iconColor);
            } else if (label.contains("account") || label.contains("ias")) {
                drawVectorUserIcon(context, btnX + 2, tlY + 2, iconColor);
            } else {
                drawVectorGearIcon(context, btnX + 2, tlY + 2, iconColor);
            }
        }

        // 2. Top-Right Buttons: [• Username] [Color] [Cosmetics] [Resource Pack] [Info] [Theme]
        int trSq = 18, trGap = 4;
        int curRight = w - 10;

        // Theme Switcher Button
        curRight -= trSq;
        boolean bcHov = MathUtils.isHovered(mouseX, mouseY, curRight, 10, trSq, trSq);
        context.fill(curRight, 10, curRight + trSq, 10 + trSq, bcHov ? ColorUtils.scaleBrightness(accent, 1.2f) : accent);
        drawVectorThemeIcon(context, curRight + 2, 12, 0xFFFFFFFF);

        // Info Button
        curRight -= (trSq + trGap);
        boolean infoHov = MathUtils.isHovered(mouseX, mouseY, curRight, 10, trSq, trSq);
        drawPillCapsule(context, curRight, 10, trSq, trSq, 0x40121622, 0x55080A10, infoHov ? 0xFFFFFFFF : 0x77FFFFFF, infoHov);
        drawVectorInfoIcon(context, curRight + 1, 11, infoHov ? accent : 0xFFFFFFFF);

        // In-Game Resource Pack Screen Button
        curRight -= (trSq + trGap);
        boolean packHov = MathUtils.isHovered(mouseX, mouseY, curRight, 10, trSq, trSq);
        drawPillCapsule(context, curRight, 10, trSq, trSq, 0x40121622, 0x55080A10, packHov ? 0xFFFFFFFF : 0x77FFFFFF, packHov);
        drawVectorFolderIcon(context, curRight + 1, 11, packHov ? accent : 0xFFFFFFFF);

        // Cosmetics / HUD Editor Button
        curRight -= (trSq + trGap);
        boolean cosmHov = MathUtils.isHovered(mouseX, mouseY, curRight, 10, trSq, trSq);
        drawPillCapsule(context, curRight, 10, trSq, trSq, 0x40121622, 0x55080A10, cosmHov ? 0xFFFFFFFF : 0x77FFFFFF, cosmHov);
        drawVectorCosmeticsIcon(context, curRight + 1, 11, cosmHov ? accent : 0xFFFFFFFF);

        // Color Changer / Click GUI Button
        curRight -= (trSq + trGap);
        boolean colorHov = MathUtils.isHovered(mouseX, mouseY, curRight, 10, trSq, trSq);
        drawPillCapsule(context, curRight, 10, trSq, trSq, 0x40121622, 0x55080A10, colorHov ? 0xFFFFFFFF : 0x77FFFFFF, colorHov);
        drawVectorPaletteIcon(context, curRight + 1, 11, accent, colorHov);

        // Username Pill
        String username = this.client != null && this.client.getSession() != null ? this.client.getSession().getUsername() : "Player";
        String userLabel = "• " + username;
        int userW = this.textRenderer.getWidth(userLabel) + 16;
        curRight -= (userW + trGap);
        drawPillCapsule(context, curRight, 10, userW, trSq, 0x40121622, 0x55080A10, 0x66FFFFFF, false);
        context.drawTextWithShadow(this.textRenderer, userLabel, curRight + 8, 15, 0xFFFFFFFF);

        // 3. Centered 3D Bangla Client Logo (NO 'Game Menu' text anywhere!)
        int logoCenterY = Math.max(34, (h - 200) / 2);
        drawGoldenCrown(context, centerX - 56, logoCenterY - 18);
        drawRedEnergyRings(context, centerX, logoCenterY + 4, accent);

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

        // 4. In-Game Serial Liquid Glass Capsule Button Stack
        int btnW = Math.min(182, w - 40);
        int btnH = 20;
        int btnX = centerX - btnW / 2;
        int btnY = logoCenterY + 54;

        drawPillMenuButton(context, "Back to Game", btnX, btnY, btnW, btnH, mouseX, mouseY, false, accent);
        btnY += 22;
        drawPillMenuButton(context, "Advancements", btnX, btnY, btnW, btnH, mouseX, mouseY, false, accent);
        btnY += 22;
        drawPillMenuButton(context, "Statistics", btnX, btnY, btnW, btnH, mouseX, mouseY, false, accent);
        btnY += 22;
        drawPillMenuButton(context, "Mods & Click GUI", btnX, btnY, btnW, btnH, mouseX, mouseY, false, accent);
        btnY += 22;
        drawPillMenuButton(context, "Options", btnX, btnY, btnW, btnH, mouseX, mouseY, false, accent);
        btnY += 22;

        boolean isSingleplayer = this.client != null && this.client.isInSingleplayer();
        if (isSingleplayer) {
            drawPillMenuButton(context, "Open to LAN", btnX, btnY, btnW, btnH, mouseX, mouseY, false, accent);
            btnY += 25;
        } else {
            btnY += 3;
        }

        // Save and Quit / Disconnect Button (Solid Glowing Red Glass Capsule)
        int quitW = Math.min(142, btnW);
        int quitX = centerX - quitW / 2;
        String quitText = isSingleplayer ? "Save and Quit to Title" : "Disconnect";
        drawPillMenuButton(context, quitText, quitX, btnY, quitW, btnH, mouseX, mouseY, true, accent);

        // 5. Bottom Center Credits & Account
        String verStr = "BANGLA CLIENT " + BanglaClient.CLIENT_VERSION + " (Developed by Ahmed)";
        int vw = this.textRenderer.getWidth(verStr);
        context.drawTextWithShadow(this.textRenderer, verStr, centerX - vw / 2, h - 23, 0xFFFFFFFF);

        String currentAcct = "Current Account: " + username;
        int aw = this.textRenderer.getWidth(currentAcct);
        context.drawTextWithShadow(this.textRenderer, currentAcct, centerX - aw / 2, h - 12, 0xFFEF4444);

        // 6. Bottom Right: BANGLA SMP Server Card
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
        context.fill(x + 2, y, x + 5, y + 3, 0xFFF59E0B);
        context.fill(x + 9, y - 3, x + 12, y + 3, 0xFFF59E0B);
        context.fill(x + 16, y, x + 19, y + 3, 0xFFF59E0B);
        context.fill(x, y + 3, x + 21, y + 9, 0xFFD97706);
        context.fill(x + 2, y + 4, x + 19, y + 8, 0xFFFBBF24);
        context.fill(x + 3, y + 5, x + 5, y + 7, 0xFFEF4444);
        context.fill(x + 9, y + 4, x + 12, y + 7, 0xFF38BDF8);
        context.fill(x + 16, y + 5, x + 18, y + 7, 0xFFEF4444);
    }

    private void drawRedEnergyRings(DrawContext context, int cx, int cy, int accent) {
        int rx = 76;
        context.fill(cx - rx, cy - 1, cx + rx, cy + 1, ColorUtils.withAlpha(accent, 0.40f));
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
        context.fill(x + 3, y + 1, x + w - 3, y + 2, 0x44FFFFFF);
        GlassRenderer.drawOutlineBorder(context, x, y, w, h, borderCol);
    }

    private void drawPillMenuButton(DrawContext context, String text, int x, int y, int w, int h, int mx, int my, boolean isQuit, int accent) {
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
        int tw = this.textRenderer.getWidth(text);
        context.drawTextWithShadow(this.textRenderer, text, x + (w - tw) / 2, y + (h - 8) / 2, 0xFFFFFFFF);
    }

    private void drawVectorFolderIcon(DrawContext context, int x, int y, int color) {
        context.fill(x + 2, y + 3, x + 6, y + 5, color);
        context.fill(x + 2, y + 5, x + 14, y + 13, color);
        context.fill(x + 4, y + 7, x + 12, y + 11, 0xFF141A24);
    }

    private void drawVectorPaletteIcon(DrawContext context, int x, int y, int accent, boolean hov) {
        context.fill(x + 3, y + 3, x + 13, y + 13, hov ? accent : 0xFFFFFFFF);
        context.fill(x + 4, y + 4, x + 12, y + 12, 0xFF141A24);
        context.fill(x + 5, y + 5, x + 7, y + 7, 0xFFEF4444);
        context.fill(x + 9, y + 5, x + 11, y + 7, 0xFF38BDF8);
        context.fill(x + 5, y + 9, x + 7, y + 11, 0xFF10B981);
        context.fill(x + 9, y + 9, x + 11, y + 11, 0xFFFBBF24);
    }

    private void drawVectorCosmeticsIcon(DrawContext context, int x, int y, int color) {
        context.fill(x + 7, y + 2, x + 9, y + 4, color);
        context.fill(x + 3, y + 5, x + 13, y + 7, color);
        context.fill(x + 4, y + 7, x + 12, y + 13, color);
        context.fill(x + 6, y + 8, x + 10, y + 12, 0xFF141A24);
    }

    private void drawVectorInfoIcon(DrawContext context, int x, int y, int color) {
        context.fill(x + 7, y + 3, x + 9, y + 5, color);
        context.fill(x + 7, y + 6, x + 9, y + 12, color);
        context.fill(x + 5, y + 12, x + 11, y + 13, color);
    }

    private void drawVectorThemeIcon(DrawContext context, int x, int y, int color) {
        context.fill(x + 3, y + 3, x + 7, y + 13, color);
        context.fill(x + 7, y + 3, x + 13, y + 5, color);
        context.fill(x + 7, y + 7, x + 11, y + 9, color);
        context.fill(x + 7, y + 11, x + 13, y + 13, color);
    }

    private void drawVectorPuzzleIcon(DrawContext context, int x, int y, int color) {
        context.fill(x + 2, y + 2, x + 6, y + 6, color);
        context.fill(x + 8, y + 2, x + 12, y + 6, color);
        context.fill(x + 2, y + 8, x + 6, y + 12, color);
        context.fill(x + 8, y + 8, x + 12, y + 12, color);
        context.fill(x + 5, y + 4, x + 9, y + 10, color);
    }

    private void drawVectorUserIcon(DrawContext context, int x, int y, int color) {
        context.fill(x + 6, y + 2, x + 10, y + 6, color);
        context.fill(x + 3, y + 8, x + 13, y + 13, color);
    }

    private void drawVectorGearIcon(DrawContext context, int x, int y, int color) {
        context.fill(x + 6, y + 2, x + 10, y + 14, color);
        context.fill(x + 2, y + 6, x + 14, y + 10, color);
        context.fill(x + 4, y + 4, x + 12, y + 12, color);
        context.fill(x + 6, y + 6, x + 10, y + 10, 0xFF141A24);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void banglaclient$onInGameMenuClick(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        double mouseX = click.x(), mouseY = click.y();
        if (click.button() != 0 || this.client == null) return;
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null) return;

        int w = this.width;
        int h = this.height;
        int centerX = w / 2;

        // Top-Left Mod Tiles Click Handling
        List<ClickableWidget> externalButtons = MainMenuButtonBridge.collectThirdPartyButtons(this);
        int tlX = 10, tlY = 10, iconSq = 20, iconGap = 5;
        for (int i = 0; i < externalButtons.size(); i++) {
            ClickableWidget ext = externalButtons.get(i);
            int btnX = tlX + (i * (iconSq + iconGap));
            if (MathUtils.isHovered(mouseX, mouseY, btnX, tlY, iconSq, iconSq)) {
                ext.onClick(new Click(mouseX, mouseY, new MouseInput(0, 0)), false);
                cir.setReturnValue(true);
                return;
            }
        }

        // Top-Right Icons Click Handling
        int trSq = 18, trGap = 4;
        int curRight = w - 10 - trSq;

        // Theme Switcher
        if (MathUtils.isHovered(mouseX, mouseY, curRight, 10, trSq, trSq)) {
            bangla.getThemeManager().cycleNextTheme();
            cir.setReturnValue(true); return;
        }

        // Info Button
        curRight -= (trSq + trGap);
        if (MathUtils.isHovered(mouseX, mouseY, curRight, 10, trSq, trSq)) {
            this.client.setScreen(new AboutScreen(this));
            cir.setReturnValue(true); return;
        }

        // Resource Pack Screen Button
        curRight -= (trSq + trGap);
        if (MathUtils.isHovered(mouseX, mouseY, curRight, 10, trSq, trSq)) {
            this.client.setScreen(new PackScreen(
                    this.client.getResourcePackManager(),
                    manager -> {
                        this.client.options.refreshResourcePacks(manager);
                        this.client.setScreen(this);
                    },
                    this.client.getResourcePackDir(),
                    Text.translatable("resourcePack.title")
            ));
            cir.setReturnValue(true); return;
        }

        // Cosmetics / HUD Editor
        curRight -= (trSq + trGap);
        if (MathUtils.isHovered(mouseX, mouseY, curRight, 10, trSq, trSq)) {
            this.client.setScreen(new HudEditorScreen(this));
            cir.setReturnValue(true); return;
        }

        // Color Changer / Click GUI
        curRight -= (trSq + trGap);
        if (MathUtils.isHovered(mouseX, mouseY, curRight, 10, trSq, trSq)) {
            this.client.setScreen(new ClickGuiScreen(false));
            cir.setReturnValue(true); return;
        }

        // Center Buttons Click Handling
        int logoCenterY = Math.max(34, (h - 200) / 2);
        int btnW = Math.min(182, w - 40);
        int btnH = 20;
        int btnX = centerX - btnW / 2;
        int btnY = logoCenterY + 54;

        // Back to Game
        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
            this.client.setScreen(null);
            this.client.mouse.lockCursor();
            cir.setReturnValue(true); return;
        }
        btnY += 22;

        // Advancements
        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
            if (this.client.player != null) {
                this.client.setScreen(new AdvancementsScreen(this.client.player.networkHandler.getAdvancementHandler(), this));
            }
            cir.setReturnValue(true); return;
        }
        btnY += 22;

        // Statistics
        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
            if (this.client.player != null) {
                this.client.setScreen(new StatsScreen(this, this.client.player.getStatHandler()));
            }
            cir.setReturnValue(true); return;
        }
        btnY += 22;

        // Mods & Click GUI
        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
            this.client.setScreen(new ClickGuiScreen(false));
            cir.setReturnValue(true); return;
        }
        btnY += 22;

        // Options
        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
            this.client.setScreen(new OptionsScreen(this, this.client.options));
            cir.setReturnValue(true); return;
        }
        btnY += 22;

        boolean isSingleplayer = this.client.isInSingleplayer();
        if (isSingleplayer) {
            if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
                this.client.setScreen(new OpenToLanScreen(this));
                cir.setReturnValue(true); return;
            }
            btnY += 25;
        } else {
            btnY += 3;
        }

        // Save & Quit / Disconnect
        int quitW = Math.min(142, btnW);
        if (MathUtils.isHovered(mouseX, mouseY, centerX - quitW / 2, btnY, quitW, btnH)) {
            if (this.client.world != null) {
                this.client.world.disconnectClient(Text.translatable("menu.savingLevel"));
            }
            this.client.disconnect();
            this.client.setScreen(new TitleScreen());
            cir.setReturnValue(true); return;
        }

        // Bottom-Right Bangla SMP Card
        if (MathUtils.isHovered(mouseX, mouseY, w - 130, h - 38, 120, 28)) {
            if (this.client.keyboard != null) {
                this.client.keyboard.setClipboard("play.banglasmp.net");
                bangla.getNotificationManager().pushSuccess("Bangla SMP", "Copied play.banglasmp.net");
            }
            cir.setReturnValue(true); return;
        }

        cir.setReturnValue(true);
    }
}
