package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.cosmetics.SkinCapeManager;
import com.ahmed.banglaclient.cosmetics.SkinEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true, require = 0)
    private void banglaclient$applyCustomSkinAndCapeInGame(CallbackInfoReturnable<SkinTextures> cir) {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.player != null && player.getUuid().equals(mc.player.getUuid())) {
            SkinCapeManager scm = SkinCapeManager.getInstance();
            if (scm.hasCustomSkinSelected() || scm.hasCustomCapeSelected()) {
                SkinTextures orig = cir.getReturnValue();
                SkinEntry skin = scm.getSelectedSkin();
                SkinEntry cape = scm.getSelectedCape();

                Identifier skinId = (skin != null) ? skin.getTextureId() : (orig != null ? orig.texture() : null);
                Identifier capeId = (cape != null) ? cape.getTextureId() : (orig != null ? orig.capeTexture() : null);
                SkinTextures.Model model = (skin != null)
                        ? (skin.isSlimModel() ? SkinTextures.Model.SLIM : SkinTextures.Model.WIDE)
                        : (orig != null ? orig.model() : SkinTextures.Model.WIDE);

                if (skinId != null) {
                    cir.setReturnValue(new SkinTextures(
                            skinId,
                            orig != null ? orig.textureUrl() : null,
                            capeId,
                            orig != null ? orig.elytraTexture() : null,
                            model,
                            true
                    ));
                }
            }
        }
    }
}
