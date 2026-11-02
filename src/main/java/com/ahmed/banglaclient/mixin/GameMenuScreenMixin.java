package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.gui.ClickGuiScreen;
import com.ahmed.banglaclient.gui.HudEditorScreen;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("TAIL"))
    private void banglaclient$setupCleanPauseMenuButtons(CallbackInfo ci) {
        // Clear all default stone buttons & header widgets so only our aligned Pause Menu buttons exist
        this.clearChildren();

        int w = this.width;
        int h = this.height;
        int centerX = w / 2;
        boolean isSingle = this.client != null && this.client.isInSingleplayer();

        int logoCenterY = Math.max(24, (h - 208) / 2);
        int btnW = Math.min(182, w - 40);
        int btnH = 20;
        int btnX = centerX - btnW / 2;
        int btnY = logoCenterY + 48;

        // 1. Back to Game
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back to Game"), b -> {
            if (this.client != null) {
                this.client.setScreen(null);
                this.client.mouse.lockCursor();
            }
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += 22;

        // 2. Advancements
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Advancements"), b -> {
            if (this.client != null && this.client.player != null) {
                this.client.setScreen(new AdvancementsScreen(this.client.player.networkHandler.getAdvancementHandler(), this));
            }
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += 22;

        // 3. Statistics
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Statistics"), b -> {
            if (this.client != null && this.client.player != null) {
                this.client.setScreen(new StatsScreen(this, this.client.player.getStatHandler()));
            }
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += 22;

        // 4. Mods / Click GUI
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Mods / Click GUI"), b -> {
            if (this.client != null) {
                this.client.setScreen(new ClickGuiScreen(false));
            }
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += 22;

        // 5. Options
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Options"), b -> {
            if (this.client != null) {
                this.client.setScreen(new OptionsScreen(this, this.client.options));
            }
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += 22;

        // 6. Open to LAN (Only when available in singleplayer)
        if (isSingle) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Open to LAN"), b -> {
                if (this.client != null) {
                    this.client.setScreen(new OpenToLanScreen(this));
                }
            }).dimensions(btnX, btnY, btnW, btnH).build());
            btnY += 24;
        } else {
            btnY += 2;
        }

        // 7. Save and Quit to Title / Disconnect (Red Capsule)
        int quitW = Math.min(152, btnW);
        String quitLabel = isSingle ? "Save and Quit to Title" : "Disconnect";
        this.addDrawableChild(ButtonWidget.builder(Text.literal(quitLabel), b -> {
            if (this.client != null) {
                this.client.disconnectWithProgressScreen();
            }
        }).dimensions(centerX - quitW / 2, btnY, quitW, btnH).build());
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void banglaclient$renderCleanPauseMenu(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        int w = this.width;
        int h = this.height;
        int centerX = w / 2;
        int accent = bangla != null && bangla.getThemeManager() != null
                ? bangla.getThemeManager().getEffectiveAccentPrimary()
                : 0xFFEF4444;

        // 1. Blurred translucent in-game world backdrop
        BlurRenderer.renderScreenBackdrop(context, w, h, 0.70f);

        // 2. Centered 3D "Bangla Client" Logo (NO "Game Menu" text, NO Main Menu side panels!)
        int logoCenterY = Math.max(24, (h - 208) / 2);
        drawGoldenCrown(context, centerX - 56, logoCenterY - 18);
        drawRedEnergyRings(context, centerX, logoCenterY + 4, accent);

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(centerX, logoCenterY);
        matrices.scale(2.6f, 2.6f);
        String txtBangla = "Bangla";
        int bx = -this.textRenderer.getWidth(txtBangla) / 2;
        context.drawText(this.textRenderer, txtBangla, bx + 2, 2, 0xFF2D0404, false);
        context.drawText(this.textRenderer, txtBangla, bx + 1, 1, 0xFF7F1D1D, false);
        context.drawTextWithShadow(this.textRenderer, txtBangla, bx, 0, 0xFFEF4444);
        matrices.popMatrix();

        matrices.pushMatrix();
        matrices.translate(centerX, logoCenterY + 21);
        matrices.scale(2.2f, 2.2f);
        String txtClient = "Client";
        int cx = -this.textRenderer.getWidth(txtClient) / 2;
        context.drawText(this.textRenderer, txtClient, cx + 2, 2, 0xFF0F172A, false);
        context.drawText(this.textRenderer, txtClient, cx + 1, 1, 0xFF475569, false);
        context.drawTextWithShadow(this.textRenderer, txtClient, cx, 0, 0xFFFFFFFF);
        matrices.popMatrix();

        // 3. Render ONLY our registered Pause Menu ButtonWidgets
        for (Element child : this.children()) {
            if (child instanceof ClickableWidget cw && cw.visible) {
                cw.render(context, mouseX, mouseY, delta);
            }
        }

        if (bangla != null && bangla.getNotificationManager() != null) {
            bangla.getNotificationManager().render(context, w, h);
        }
        ci.cancel();
    }

    @Unique
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

    @Unique
    private void drawRedEnergyRings(DrawContext context, int cx, int cy, int accent) {
        int rx = 72;
        context.fill(cx - rx, cy - 1, cx + rx, cy + 1, ColorUtils.withAlpha(accent, 0.40f));
        context.fill(cx - rx + 4, cy - 2, cx + rx - 4, cy + 2, ColorUtils.withAlpha(accent, 0.22f));
        context.fill(cx - rx - 3, cy, cx - rx + 3, cy + 2, 0xFFEF4444);
        context.fill(cx + rx - 3, cy, cx + rx + 3, cy + 2, 0xFFEF4444);
    }
}
