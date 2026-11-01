package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ServerInfo;

/**
 * Module 13: Server Address
 * Displays the current server address when connected to multiplayer, with a streamer privacy mask option.
 * Never exposes hidden local network or private credentials.
 */
public final class ServerAddressModule extends HudModule {
    private final BooleanSetting privacyMaskSetting;

    public ServerAddressModule() {
        super(
                "server_address",
                "Server Address",
                "Displays the current multiplayer server address with an optional privacy mask.",
                ModuleCategory.HUD,
                160.0f,
                60.0f,
                110.0f,
                20.0f,
                false
        );
        this.privacyMaskSetting = addSetting(new BooleanSetting(
                "privacy_mask", "Privacy Mode (Hide IP)", "Mask numeric IP addresses to protect private servers while streaming", false
        ));
        addAliases("ip", "server", "host", "domain");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        String addressText = "Singleplayer";
        if (client != null && !client.isInSingleplayer()) {
            ServerInfo info = client.getCurrentServerEntry();
            if (info != null && info.address != null && !info.address.isBlank()) {
                addressText = privacyMaskSetting.isEnabled() ? "Hidden Server" : info.address;
            } else {
                addressText = "Multiplayer";
            }
        } else if (preview) {
            addressText = privacyMaskSetting.isEnabled() ? "Hidden Server" : "play.example.net";
        }

        String text = "Server: " + addressText;
        int pad = paddingSetting.getIntValue();
        int boxW = client.textRenderer.getWidth(text) + pad * 2;
        int boxH = client.textRenderer.fontHeight + pad * 2;

        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(
                    context, x, y, boxW, boxH,
                    backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
            );
        }

        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, textColorSetting.getArgb());
    }
}
