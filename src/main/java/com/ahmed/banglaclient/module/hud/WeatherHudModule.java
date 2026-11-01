package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

/**
 * Module 10: Weather HUD
 * Displays current world weather conditions (Clear, Raining, Snowing, Thunderstorm) and rain gradient intensity.
 */
public final class WeatherHudModule extends HudModule {
    private final BooleanSetting showIntensitySetting;

    public WeatherHudModule() {
        super(
                "weather_hud",
                "Weather HUD",
                "Displays current world weather conditions and precipitation intensity.",
                ModuleCategory.HUD,
                10.0f,
                244.0f,
                96.0f,
                20.0f,
                false
        );
        this.showIntensitySetting = addSetting(new BooleanSetting(
                "show_intensity", "Show Rain Intensity %", "Append precipitation gradient percentage when raining", false
        ));
        addAliases("weather", "rain", "thunder", "snow", "storm");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        String condition = "Clear";
        float rainGradient = 0.0f;

        if (client.world != null && client.player != null) {
            rainGradient = client.world.getRainGradient(tickDelta);
            BlockPos pos = client.player.getBlockPos();
            Biome biome = client.world.getBiome(pos).value();
            Biome.Precipitation precipitation = biome.getPrecipitation(pos, client.world.getSeaLevel());

            if (client.world.isThundering()) {
                condition = precipitation == Biome.Precipitation.SNOW ? "Blizzard" : "Thunderstorm";
            } else if (client.world.isRaining()) {
                if (precipitation == Biome.Precipitation.SNOW) {
                    condition = "Snowing";
                } else if (precipitation == Biome.Precipitation.NONE) {
                    condition = "Overcast";
                } else {
                    condition = "Raining";
                }
            }
        }

        String text = "Weather: " + condition;
        if (showIntensitySetting.isEnabled() && rainGradient > 0.01f) {
            text += String.format(" (%.0f%%)", rainGradient * 100.0f);
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
