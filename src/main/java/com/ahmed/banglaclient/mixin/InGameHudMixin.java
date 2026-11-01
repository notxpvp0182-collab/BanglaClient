package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.combat.CrosshairCustomizerModule;
import com.ahmed.banglaclient.module.utility.CustomScoreboardModule;
import com.ahmed.banglaclient.module.visual.CustomVignetteModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void banglaclient$overrideVanillaCrosshairWhenCustomActive(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var crosshairOpt = bangla.getModuleManager().getModule(CrosshairCustomizerModule.class);
            if (crosshairOpt.isPresent() && crosshairOpt.get().isEnabled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
    private void banglaclient$controlVignetteOverlay(DrawContext context, Entity entity, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var vignetteOpt = bangla.getModuleManager().getModule(CustomVignetteModule.class);
            if (vignetteOpt.isPresent() && vignetteOpt.get().shouldDisableVignette()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At("HEAD"), cancellable = true)
    private void banglaclient$controlScoreboardSidebar(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var sbOpt = bangla.getModuleManager().getModule(CustomScoreboardModule.class);
            if (sbOpt.isPresent() && sbOpt.get().shouldHideEntirely()) {
                ci.cancel();
            }
        }
    }
}
