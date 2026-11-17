package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.cosmetics.BackgroundManager;
import com.ahmed.banglaclient.cosmetics.BackgroundManager.BackgroundEntry;
import com.ahmed.banglaclient.identity.BcIdentityManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import com.ahmed.banglaclient.gui.ServerBannerScreen;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies the selected custom background across ALL Minecraft menu screens
 * and renders the large transparent BANGLA CLIENT watermark on all Inventory/Container screens.
 */
@Mixin(Screen.class)
public abstract class ScreenBackgroundMixin {

    @Shadow public int width;
    @Shadow public int height;
    @Shadow protected abstract <T extends net.minecraft.client.gui.Element & net.minecraft.client.gui.Drawable & net.minecraft.client.gui.Selectable> T addDrawableChild(T drawableElement);

    @Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At("TAIL"), require = 0)
    private void banglaclient$addServerBannerButtonOnMultiplayer(MinecraftClient client, int width, int height, CallbackInfo ci) {
        if ((Object) this instanceof MultiplayerScreen mpScreen) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Server Banner"), b -> {
                client.setScreen(new ServerBannerScreen(mpScreen));
            }).dimensions(width - 96, 6, 88, 18).build());
        }
    }


    @Inject(method = "renderPanoramaBackground", at = @At("HEAD"), cancellable = true, require = 0)
    private void banglaclient$renderGlobalCustomMenuBackground(DrawContext context, float delta, CallbackInfo ci) {
        BackgroundEntry customBg = BackgroundManager.getInstance().getActiveBackground();
        if (customBg != null) {
            Identifier frameTex = customBg.getCurrentFrameTexture();
            if (frameTex != null) {
                try {
                    context.drawTexture(RenderPipelines.GUI_TEXTURED, frameTex, 0, 0, 0.0f, 0.0f, this.width, this.height, this.width, this.height);
                    context.fill(0, 0, this.width, this.height, 0x38050608);
                    ci.cancel();
                } catch (Throwable ignored) {}
            }
        }
    }

    @Inject(method = "renderWithTooltip", at = @At("TAIL"), require = 0)
    private void banglaclient$renderWatermarkOnAllContainerScreens(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if ((Object) this instanceof HandledScreen<?>) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.textRenderer != null) {
                BcIdentityManager.drawWatermark(context, this.width, this.height, mc.textRenderer);
            }
        }
    }
}
