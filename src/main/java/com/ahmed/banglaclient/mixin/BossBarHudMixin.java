package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.utility.CustomBossBarModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Connects `CustomBossBarModule` visibility controls to Minecraft's `BossBarHud`.
 */
@Mixin(BossBarHud.class)
public abstract class BossBarHudMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void banglaclient$controlBossBarRendering(DrawContext context, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var bossBarOpt = bangla.getModuleManager().getModule(CustomBossBarModule.class);
            if (bossBarOpt.isPresent() && bossBarOpt.get().shouldHideBossBars()) {
                ci.cancel();
            }
        }
    }
}
