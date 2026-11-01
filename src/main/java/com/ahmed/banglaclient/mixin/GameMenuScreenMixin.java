package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.gui.ClickGuiScreen;
import com.ahmed.banglaclient.gui.HudEditorScreen;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void banglaclient$renderPauseMenuHeaderAndGlass(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null) return;
        Theme theme = GlassRenderer.getTheme();
        int accent = bangla.getThemeManager().getEffectiveAccentPrimary();

        // Top-Right Quick Client Buttons on Pause Menu: [BANGLA GUI] [HUD Editor] [Theme]
        int bx = this.width - 78, by = 8;
        boolean gHov = MathUtils.isHovered(mouseX, mouseY, bx, by, 70, 18);
        GlassRenderer.drawGlassPanel(context, bx, by, 70, 18, true, gHov, true);
        context.drawTextWithShadow(this.textRenderer, "Click GUI", bx + 11, by + 5, gHov ? accent : theme.getTextPrimary());

        int hx = bx - 74;
        boolean hHov = MathUtils.isHovered(mouseX, mouseY, hx, by, 70, 18);
        GlassRenderer.drawGlassPanel(context, hx, by, 70, 18, true, hHov, false);
        context.drawTextWithShadow(this.textRenderer, "HUD Editor", hx + 8, by + 5, hHov ? accent : theme.getTextPrimary());

        // Style all vanilla Pause Menu buttons with Liquid Glass overlay
        for (Element el : this.children()) {
            if (el instanceof ClickableWidget w && w.visible) {
                boolean hov = w.isHovered();
                GlassRenderer.drawGlassPanel(context, w.getX(), w.getY(), w.getWidth(), w.getHeight(), true, hov, hov);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void banglaclient$onPauseMenuClick(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (click.button() != 0 || this.client == null) return;
        double mx = click.x(), my = click.y();
        int bx = this.width - 78, by = 8;
        if (MathUtils.isHovered(mx, my, bx, by, 70, 18)) {
            this.client.setScreen(new ClickGuiScreen(false));
            cir.setReturnValue(true);
            return;
        }
        if (MathUtils.isHovered(mx, my, bx - 74, by, 70, 18)) {
            this.client.setScreen(new HudEditorScreen(this));
            cir.setReturnValue(true);
        }
    }
}
