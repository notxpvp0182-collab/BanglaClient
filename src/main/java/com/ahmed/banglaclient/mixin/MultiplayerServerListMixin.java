package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.cosmetics.ServerFlagManager;
import com.ahmed.banglaclient.cosmetics.ServerFlagManager.CountryData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Renders the Server Country Flag right before the Server Name in the Multiplayer Server List (just like Feather Client).
 */
@Mixin(MultiplayerServerListWidget.ServerEntry.class)
public abstract class MultiplayerServerListMixin {

    @Shadow @Final private ServerInfo server;

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V", ordinal = 0),
            require = 0
    )
    private void banglaclient$drawFlagBeforeServerName(DrawContext context, TextRenderer textRenderer, String serverName, int x, int y, int color) {
        if (this.server != null && this.server.address != null) {
            CountryData data = ServerFlagManager.getOrResolveCountry(this.server.address);
            if (data != null) {
                ServerFlagManager.drawCountryFlag(context, x, y, data.countryCode());
                context.drawTextWithShadow(textRenderer, serverName, x + 17, y, color);
                return;
            }
        }
        context.drawTextWithShadow(textRenderer, serverName, x, y, color);
    }
}
