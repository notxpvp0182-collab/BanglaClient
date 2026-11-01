package com.ahmed.banglaclient.module.movement;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 68: Swimming Indicator
 * Displays underwater/swimming state and remaining oxygen air supply in seconds.
 */
public final class SwimmingIndicatorModule extends HudModule {
    private final BooleanSetting onlyWhenInWaterSetting;

    public SwimmingIndicatorModule() {
        super(
                "swimming_indicator",
                "Swimming Indicator",
                "Displays swimming state and remaining underwater oxygen timer in seconds.",
                ModuleCategory.MOVEMENT,
                160.0f,
                230.0f,
                114.0f,
                20.0f,
                false
        );
        this.onlyWhenInWaterSetting = addSetting(new BooleanSetting(
                "only_in_water", "Only Show In Water", "Hide the widget when the player is on dry land with full oxygen", false
        ));
        addAliases("swim", "oxygen", "air", "breath", "diving");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        boolean inWater = false;
        boolean swimming = false;
        int airTicks = 300;
        int maxAirTicks = 300;

        if (client.player != null) {
            inWater = client.player.isTouchingWater() || client.player.isSubmergedInWater();
            swimming = client.player.isSwimming();
            airTicks = Math.max(0, client.player.getAir());
            maxAirTicks = Math.max(1, client.player.getMaxAir());
        } else if (preview) {
            inWater = true;
            swimming = true;
            airTicks = 180;
        }

        if (!preview && onlyWhenInWaterSetting.isEnabled() && !inWater && airTicks >= maxAirTicks) {
            return;
        }

        float airSec = airTicks / 20.0f;
        float maxSec = maxAirTicks / 20.0f;
        String mode = swimming ? "Swimming" : (inWater ? "In Water" : "Dry Land");
        String text = String.format("%s • Air: %.1f/%.0fs", mode, airSec, maxSec);

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

        int color = airSec <= 3.0f
                ? ThemeTokens.STATUS_ERROR
                : (airSec <= 7.0f ? ThemeTokens.STATUS_WARNING : textColorSetting.getArgb());
        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, color);
    }
}
