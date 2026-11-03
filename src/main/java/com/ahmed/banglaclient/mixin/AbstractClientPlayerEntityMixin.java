package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.cosmetics.SkinCapeManager;
import com.ahmed.banglaclient.cosmetics.SkinEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
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
            boolean overrideSkin = scm.isCustomSkinOverride();
            boolean overrideCape = scm.isCustomCapeOverride();

            if (overrideSkin || overrideCape) {
                SkinTextures orig = cir.getReturnValue();
                SkinEntry skin = overrideSkin ? scm.getSelectedSkin() : null;
                SkinEntry cape = overrideCape ? scm.getSelectedCape() : null;

                AssetInfo.TextureAsset body = (skin != null)
                        ? new AssetInfo.TextureAssetInfo(skin.getTextureId(), skin.getTextureId())
                        : (orig != null ? orig.body() : null);
                AssetInfo.TextureAsset capeAsset = (cape != null)
                        ? new AssetInfo.TextureAssetInfo(cape.getTextureId(), cape.getTextureId())
                        : (orig != null ? orig.cape() : null);
                PlayerSkinType model = (skin != null)
                        ? (skin.isSlimModel() ? PlayerSkinType.SLIM : PlayerSkinType.WIDE)
                        : (orig != null ? orig.model() : PlayerSkinType.WIDE);

                if (body != null) {
                    cir.setReturnValue(new SkinTextures(
                            body,
                            capeAsset,
                            orig != null ? orig.elytra() : null,
                            model,
                            true
                    ));
                }
            }
        }
    }
}
