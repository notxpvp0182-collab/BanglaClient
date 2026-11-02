package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.gui.ClickGuiScreen;
import com.ahmed.banglaclient.gui.HudEditorScreen;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.util.ColorUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Constructor;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    @Unique
    private boolean layoutAdjusted = false;

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("TAIL"))
    private void banglaclient$addClientButtonsToPauseMenu(CallbackInfo ci) {
        layoutAdjusted = false;
        int centerX = this.width / 2;
        int totalW = 204;
        int halfW = 98;
        int leftX = centerX - totalW / 2;
        int rightX = leftX + halfW + 8;

        // Add Cosmetics (opens Click GUI) and HUD Editor (opens HUD Editor) buttons into the Pause Menu
        this.addDrawableChild(ButtonWidget.builder(Text.literal("__PAUSE_GUI:Cosmetics"), b -> {
            if (this.client != null) {
                this.client.setScreen(new ClickGuiScreen(false));
            }
        }).dimensions(leftX, this.height - 28, halfW, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("__PAUSE_OPT:HUD Editor"), b -> {
            if (this.client != null) {
                this.client.setScreen(new HudEditorScreen(this));
            }
        }).dimensions(rightX, this.height - 28, halfW, 20).build());
    }

    @Unique
    private void adjustDynamicPauseMenuLayoutOnce() {
        if (layoutAdjusted) return;
        layoutAdjusted = true;

        int centerX = this.width / 2;
        int totalW = 204;
        int halfW = 98;
        int leftX = centerX - totalW / 2;
        int rightX = leftX + halfW + 8;

        ClickableWidget quitBtn = null;
        ClickableWidget cosmeticsBtn = null;
        ClickableWidget hudEditorBtn = null;
        boolean hasModsBtn = false;
        int maxOtherY = 60;

        for (Element child : this.children()) {
            if (child instanceof ClickableWidget cw) {
                // Hide non-button text widgets like vanilla "Game Menu" title
                if (!(cw instanceof PressableWidget)) {
                    cw.visible = false;
                    cw.active = false;
                    continue;
                }
                String msg = cw.getMessage() != null ? cw.getMessage().getString() : "";
                if (msg.equals("__PAUSE_GUI:Cosmetics")) {
                    cosmeticsBtn = cw;
                } else if (msg.equals("__PAUSE_OPT:HUD Editor")) {
                    hudEditorBtn = cw;
                } else if (msg.contains("Quit") || msg.contains("Disconnect")) {
                    quitBtn = cw;
                } else {
                    if (msg.toLowerCase().contains("mod")) {
                        hasModsBtn = true;
                    }
                    // Shift standard grid slightly down if too close to top logo
                    if (cw.getY() < 56) {
                        int shift = 56 - cw.getY();
                        for (Element e2 : this.children()) {
                            if (e2 instanceof PressableWidget pw) {
                                pw.setY(pw.getY() + shift);
                            }
                        }
                    }
                    maxOtherY = Math.max(maxOtherY, cw.getY());
                }
            }
        }

        // If Mod Menu did not inject a Mods button, add a fallback Mods button
        int nextRowY = maxOtherY + 24;
        if (!hasModsBtn && FabricLoader.getInstance().isModLoaded("modmenu")) {
            ButtonWidget modsFallback = ButtonWidget.builder(Text.literal("__PAUSE_MODS:Mods"), b -> {
                try {
                    Class<?> cls = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
                    Constructor<?> ctor = cls.getConstructor(Screen.class);
                    if (this.client != null) this.client.setScreen((Screen) ctor.newInstance(this));
                } catch (Throwable ignored) {}
            }).dimensions(leftX, nextRowY, totalW, 20).build();
            this.addDrawableChild(modsFallback);
            nextRowY += 24;
        }

        // Place [Cosmetics] | [HUD Editor] cleanly right above [Disconnect / Save and Quit]
        if (cosmeticsBtn != null && hudEditorBtn != null) {
            cosmeticsBtn.setX(leftX);
            cosmeticsBtn.setY(nextRowY);
            cosmeticsBtn.setWidth(halfW);
            cosmeticsBtn.setHeight(20);

            hudEditorBtn.setX(rightX);
            hudEditorBtn.setY(nextRowY);
            hudEditorBtn.setWidth(halfW);
            hudEditorBtn.setHeight(20);
            nextRowY += 24;
        }

        if (quitBtn != null) {
            quitBtn.setX(leftX);
            quitBtn.setY(nextRowY);
            quitBtn.setWidth(totalW);
            quitBtn.setHeight(20);
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void banglaclient$renderDynamicPauseMenu(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        adjustDynamicPauseMenuLayoutOnce();

        BanglaClient bangla = BanglaClient.getInstance();
        int w = this.width;
        int h = this.height;
        int centerX = w / 2;
        int accent = bangla != null && bangla.getThemeManager() != null
                ? bangla.getThemeManager().getEffectiveAccentPrimary()
                : 0xFFEF4444;

        // 1. Live Minecraft Gameplay World Remains Visible Behind Translucent Blur/Dimming
        BlurRenderer.renderScreenBackdrop(context, w, h, 0.55f);

        // 2. Centered 3D "Bangla Client" Logo at top
        int logoCenterY = 18;
        drawGoldenCrown(context, centerX + 4, logoCenterY - 14);
        drawRedWings(context, centerX, logoCenterY + 10, accent);

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(centerX, logoCenterY - 2);
        matrices.scale(2.2f, 2.2f);
        String txtBangla = "Bangla";
        int bx = -this.textRenderer.getWidth(txtBangla) / 2;
        context.drawText(this.textRenderer, txtBangla, bx + 2, 2, 0xFF280404, false);
        context.drawText(this.textRenderer, txtBangla, bx + 1, 1, 0xFF7F1D1D, false);
        context.drawTextWithShadow(this.textRenderer, txtBangla, bx, 0, 0xFFEF4444);
        matrices.popMatrix();

        matrices.pushMatrix();
        matrices.translate(centerX, logoCenterY + 16);
        matrices.scale(1.9f, 1.9f);
        String txtClient = "Client";
        int cx = -this.textRenderer.getWidth(txtClient) / 2;
        context.drawText(this.textRenderer, txtClient, cx + 2, 2, 0xFF0F172A, false);
        context.drawText(this.textRenderer, txtClient, cx + 1, 1, 0xFF475569, false);
        context.drawTextWithShadow(this.textRenderer, txtClient, cx, 0, 0xFFFFFFFF);
        matrices.popMatrix();

        // 3. Render all active Pause Menu buttons (Singleplayer / Server buttons + Mod Menu's Mods button + Cosmetics + HUD Editor)
        for (Element child : this.children()) {
            if (child instanceof PressableWidget pw && pw.visible) {
                pw.render(context, mouseX, mouseY, delta);
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
    private void drawRedWings(DrawContext context, int cx, int cy, int accent) {
        int span = 68;
        context.fill(cx - span, cy, cx - 36, cy + 2, 0xFFEF4444);
        context.fill(cx - span + 6, cy + 3, cx - 36, cy + 5, ColorUtils.withAlpha(accent, 0.65f));
        context.fill(cx + 36, cy, cx + span, cy + 2, 0xFFEF4444);
        context.fill(cx + 36, cy + 3, cx + span - 6, cy + 5, ColorUtils.withAlpha(accent, 0.65f));
    }
}
