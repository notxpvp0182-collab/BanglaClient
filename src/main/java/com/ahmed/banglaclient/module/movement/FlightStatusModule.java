package com.ahmed.banglaclient.module.movement;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 69: Flight Status
 * Displays legitimate Creative/Spectator flight status, fly speed multiplier, or Elytra gliding pitch angle.
 */
public final class FlightStatusModule extends HudModule {

    public FlightStatusModule() {
        super(
                "flight_status",
                "Flight Status",
                "Displays legitimate Creative flight status, fly speed, or Elytra gliding pitch.",
                ModuleCategory.MOVEMENT,
                160.0f,
                254.0f,
                116.0f,
                20.0f,
                false
        );
        addAliases("flight", "flyspeed", "elytra", "gliding");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        String text = "Flight: Grounded";
        if (client.player != null) {
            var abilities = client.player.getAbilities();
            if (abilities.flying) {
                float speedMultiplier = abilities.getFlySpeed() / 0.05f;
                text = String.format("Flying (%.1fx Speed)", speedMultiplier);
            } else if (client.player.isGliding()) {
                float pitch = -client.player.getPitch(tickDelta);
                text = String.format("Elytra Glide (Pitch: %+.0f°)", pitch);
            } else if (abilities.allowFlying) {
                text = "Flight: Standby";
            }
        } else if (preview) {
            text = "Flying (1.0x Speed)";
        }

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
