package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.gui.ClickGuiScreen;
import com.ahmed.banglaclient.gui.HudEditorScreen;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("TAIL"), require = 0)
    private void banglaclient$arrangePauseMenuVerticallyAndAddClientButtons(CallbackInfo ci) {
        List<ClickableWidget> vanillaButtons = new ArrayList<>();
        for (Element el : this.children()) {
            if (el instanceof ClickableWidget cw && cw.visible) {
                vanillaButtons.add(cw);
            }
        }

        int btnW = Math.min(168, this.width - 40);
        int btnH = 20;
        int centerX = this.width / 2;
        int totalRows = vanillaButtons.size();
        int startY = Math.max(38, (this.height - (totalRows * 22)) / 2);

        for (int i = 0; i < vanillaButtons.size(); i++) {
            ClickableWidget w = vanillaButtons.get(i);
            w.setX(centerX - btnW / 2);
            w.setY(startY + i * 22);
            w.setWidth(btnW);
            w.setHeight(btnH);
        }

        // Register real, native ButtonWidgets for Click GUI and HUD Editor so touch/mouse clicks ALWAYS work
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Click GUI"), btn -> {
            if (this.client != null) {
                this.client.setScreen(new ClickGuiScreen(false));
            }
        }).dimensions(this.width - 78, 8, 70, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("HUD Editor"), btn -> {
            if (this.client != null) {
                this.client.setScreen(new HudEditorScreen(this));
            }
        }).dimensions(this.width - 152, 8, 70, 20).build());
    }

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true, require = 0)
    private void banglaclient$renderPauseMenuGlassBackdrop(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BlurRenderer.renderScreenBackdrop(context, this.width, this.height, 0.75f);
        Theme theme = GlassRenderer.getTheme();

        int cx = this.width / 2;
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(cx, 12);
        matrices.scale(1.5f, 1.5f);
        String b1 = "Bangla ";
        String b2 = "Client";
        int tw = this.textRenderer.getWidth(b1 + b2);
        int lx = -tw / 2;
        context.drawTextWithShadow(this.textRenderer, b1, lx, 0, theme.getAccentPrimary());
        context.drawTextWithShadow(this.textRenderer, b2, lx + this.textRenderer.getWidth(b1), 0, 0xFFFFFFFF);
        matrices.popMatrix();
        ci.cancel();
    }
}
