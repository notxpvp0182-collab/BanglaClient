package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.cosmetics.ServerFlagManager;
import com.ahmed.banglaclient.cosmetics.ServerFlagManager.CountryData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MultiplayerServerListWidget.ServerEntry.class)
public abstract class MultiplayerServerListMixin {

    @Shadow @Final private ServerInfo server;

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V", ordinal = 0),
            require = 0
    )
    private void banglaclient$drawFlagOrQuestionMarkBeforeServerName(DrawContext context, TextRenderer textRenderer, String serverName, int x, int y, int color) {
        String address = (this.server != null && this.server.address != null) ? this.server.address : "";
        CountryData data = ServerFlagManager.getOrResolveCountry(address);
        String code = data != null ? data.countryCode() : "unknown";
        ServerFlagManager.drawCountryFlag(context, x, y, code);
        context.drawTextWithShadow(textRenderer, serverName, x + 17, y, color);
    }
}
