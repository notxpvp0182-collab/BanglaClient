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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {

    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true, require = 0)
    private void banglaclient$prependBcIdentityInTabList(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        if (entry == null || entry.getProfile() == null) return;
        Text original = cir.getReturnValue();
        if (original == null) return;

        BanglaClient bangla = BanglaClient.getInstance();
        boolean showBadge = true;
        if (bangla != null && bangla.getModuleManager() != null) {
            var tabOpt = bangla.getModuleManager().getModule(TabListHeadsModule.class);
            if (tabOpt.isPresent()) {
                showBadge = tabOpt.get().shouldShowBcTabBadge();
            }
        }

        if (showBadge && (BcIdentityManager.isVerifiedBcUser(entry.getProfile().id())
                || BcIdentityManager.isVerifiedBcUserByName(entry.getProfile().name()))) {
            if (!original.getString().contains("[BC]")) {
                cir.setReturnValue(BcIdentityManager.createBcPrefixBadge().append(original));
            }
        }
    }
}
