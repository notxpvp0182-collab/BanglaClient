package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.combat.CrosshairCustomizerModule;
import com.ahmed.banglaclient.module.utility.CustomScoreboardModule;
import com.ahmed.banglaclient.module.visual.CustomPumpkinOverlayModule;
import com.ahmed.banglaclient.module.visual.CustomVignetteModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow
    @Final
    private static Identifier PUMPKIN_BLUR;

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true, require = 0)
    private void banglaclient$overrideVanillaCrosshairWhenCustomActive(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var crosshairOpt = bangla.getModuleManager().getModule(CrosshairCustomizerModule.class);
            if (crosshairOpt.isPresent() && crosshairOpt.get().isEnabled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private void banglaclient$controlVignetteOverlay(DrawContext context, Entity entity, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var vignetteOpt = bangla.getModuleManager().getModule(CustomVignetteModule.class);
            if (vignetteOpt.isPresent() && vignetteOpt.get().shouldDisableVignette()) {
                ci.cancel();
            }
        }
    }

    @ModifyVariable(
            method = "renderOverlay",
            at = @At("HEAD"),
            argsOnly = true,
            require = 0
    )
    private float banglaclient$adjustPumpkinOverlayOpacity(float opacity, DrawContext context, Identifier texture) {
        if (texture != null && texture.equals(PUMPKIN_BLUR)) {
            BanglaClient bangla = BanglaClient.getInstance();
            if (bangla != null && bangla.getModuleManager() != null) {
                var pumpkinOpt = bangla.getModuleManager().getModule(CustomPumpkinOverlayModule.class);
                if (pumpkinOpt.isPresent() && pumpkinOpt.get().isEnabled()) {
                    return opacity * pumpkinOpt.get().getOverlayOpacity();
                }
            }
        }
        return opacity;
    }

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void banglaclient$interceptLiveServerScoreboard(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var sbOpt = bangla.getModuleManager().getModule(CustomScoreboardModule.class);
            if (sbOpt.isPresent() && sbOpt.get().isEnabled()) {
                CustomScoreboardModule mod = sbOpt.get();
                if (mod.shouldHideEntirely()) {
                    ci.cancel();
                    return;
                }
                mod.renderLiveScoreboard(context, objective);
                ci.cancel();
            }
        }
    }
}
