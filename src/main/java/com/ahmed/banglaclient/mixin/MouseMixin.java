package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.utility.MiddleClickPearlModule;
import com.ahmed.banglaclient.module.visual.ZoomModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Routes mouse-wheel scroll events to ZoomModule and middle-mouse button presses
 * to MiddleClickPearlModule.
 */
@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true, require = 0)
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

    @Inject(method = "onMouseButton", at = @At("HEAD"), require = 0)
    private void banglaclient$onMiddleClickPearl(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action != GLFW.GLFW_PRESS || button != GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            return;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.currentScreen != null) {
            return;
        }
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var pearlOpt = bangla.getModuleManager().getModule(MiddleClickPearlModule.class);
            pearlOpt.ifPresent(MiddleClickPearlModule::onMiddleMouseClick);
        }
    }
}
