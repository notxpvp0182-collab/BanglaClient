package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.EnumSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Module 32: Custom Weather
 * Provides client-only visual weather customization (Clear, Rain, Thunderstorm) without modifying server world state.
 */
public final class CustomWeatherModule extends Module {
    public enum WeatherPreset {
        VANILLA("Server Default"),
        CLEAR("Always Clear"),
        LIGHT_RAIN("Rain"),
        THUNDERSTORM("Storm");

        private final String label;

        WeatherPreset(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final EnumSetting<WeatherPreset> presetSetting;
    private final NumberSetting strengthSetting;

    public CustomWeatherModule() {
        super(
                "custom_weather",
                "Custom Weather",
                "Customizes visual weather appearance locally without altering server world state.",
                ModuleCategory.VISUAL
        );
        this.presetSetting = addSetting(new EnumSetting<>(
                "preset", "Weather Mode", "Client-side visual weather override", WeatherPreset.CLEAR
        ));
        this.strengthSetting = addSetting(new NumberSetting(
                "strength", "Precipitation Strength", "Visual intensity when Rain or Storm is selected",
                0.8, 0.1, 1.0, 0.1, ""
        )).visibleWhen(() -> presetSetting.getValue() == WeatherPreset.LIGHT_RAIN || presetSetting.getValue() == WeatherPreset.THUNDERSTORM) instanceof NumberSetting ns ? ns : null;
        addAliases("weatherchanger", "norain", "clearweather");
    }

    public WeatherPreset getActivePreset() {
        return isEnabled() ? presetSetting.getValue() : WeatherPreset.VANILLA;
    }

    public float getOverrideRainGradient(float vanillaGradient) {
        if (!isEnabled()) {
            return vanillaGradient;
        }
        return switch (presetSetting.getValue()) {
            case VANILLA -> vanillaGradient;
            case CLEAR -> 0.0f;
            case LIGHT_RAIN, THUNDERSTORM -> strengthSetting != null ? strengthSetting.getFloatValue() : 0.8f;
        };
    }

    public float getOverrideThunderGradient(float vanillaThunder) {
        if (!isEnabled()) {
            return vanillaThunder;
        }
        return switch (presetSetting.getValue()) {
            case VANILLA -> vanillaThunder;
            case CLEAR, LIGHT_RAIN -> 0.0f;
            case THUNDERSTORM -> strengthSetting != null ? strengthSetting.getFloatValue() : 0.8f;
        };
    }
}
