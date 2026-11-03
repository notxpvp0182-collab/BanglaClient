package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.cosmetics.SkinCapeManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {

    @Shadow public abstract GameProfile getProfile();

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true, require = 0)
    private void banglaclient$useLocalCustomSkinInTab(CallbackInfoReturnable<SkinTextures> cir) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.player != null && getProfile() != null && getProfile().id().equals(mc.player.getUuid())) {
            SkinCapeManager scm = SkinCapeManager.getInstance();
            if (scm.isCustomSkinOverride() || scm.isCustomCapeOverride()) {
                cir.setReturnValue(scm.getActiveSkinTextures());
            }
        }
    }
}
