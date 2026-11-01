package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.visual.ZoomModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Routes mouse-wheel scroll events to `ZoomModule` while the zoom keybind is held so
 * scrolling adjusts magnification instead of changing the hotbar slot.
 */
@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void banglaclient$interceptZoomScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.currentScreen != null) {
            return;
        }
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var zoomOpt = bangla.getModuleManager().getModule(ZoomModule.class);
            if (zoomOpt.isPresent() && zoomOpt.get().onMouseScroll(vertical)) {
                ci.cancel();
            }
        }
    }
}
