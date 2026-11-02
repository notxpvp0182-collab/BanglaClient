package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.identity.BcIdentityManager;
import com.ahmed.banglaclient.module.hud.TabListHeadsModule;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {

    @ModifyVariable(method = "render", at = @At("STORE"), ordinal = 0, require = 0)
    private boolean banglaclient$forceShowPlayerHeadsInTabList(boolean showHeads) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getModuleManager() != null) {
            var tabOpt = bangla.getModuleManager().getModule(TabListHeadsModule.class);
            if (tabOpt.isPresent() && tabOpt.get().isEnabled()) {
                return true; // Force real player head rendering even on offline-mode/unencrypted servers
            }
        }
        return showHeads;
    }

    @Inject(method = "getPlayerName(Lnet/minecraft/client/network/PlayerListEntry;)Lnet/minecraft/text/Text;", at = @At("RETURN"), cancellable = true, require = 0)
    private void banglaclient$prependBcIdentityInTabList(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        if (entry == null || entry.getProfile() == null) return;
        Text original = cir.getReturnValue();
        if (original == null) return;

        if (BcIdentityManager.isVerifiedBcUser(entry.getProfile().id())
                || BcIdentityManager.isVerifiedBcUserByName(entry.getProfile().name())) {
            if (!original.getString().contains("✦")) {
                cir.setReturnValue(BcIdentityManager.createBcPrefixBadge().append(original));
            }
        }
    }
}
