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
        if (mc == null || getProfile() == null) return;

        // Strictly verify that this entry belongs to the local client player
        boolean isLocal = (mc.player != null && getProfile().id().equals(mc.player.getUuid()))
                || (mc.getSession() != null && getProfile().name().equalsIgnoreCase(mc.getSession().getUsername()));

        if (isLocal) {
            SkinCapeManager scm = SkinCapeManager.getInstance();
            if (scm.isCustomSkinOverride() || scm.isCustomCapeOverride()) {
                cir.setReturnValue(scm.getActiveSkinTextures());
            }
        }
    }
}
