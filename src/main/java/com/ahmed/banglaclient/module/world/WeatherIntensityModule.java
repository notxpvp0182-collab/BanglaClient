package com.ahmed.banglaclient.module.world;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class WeatherIntensityModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public WeatherIntensityModule() {
        super("weather_intensity", "Weather Intensity", "Controls rain and snow density from 0% to 100%.", ModuleCategory.WORLD, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Light Rain Mode", "Controls rain and snow density from 0% to 100%.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Mute Rain Sound", "Additional control for Weather Intensity", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Weather Intensity Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
