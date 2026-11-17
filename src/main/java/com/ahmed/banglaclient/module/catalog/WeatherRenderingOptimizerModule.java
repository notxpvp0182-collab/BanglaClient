package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class WeatherRenderingOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public WeatherRenderingOptimizerModule() {
        super("weather_rendering_optimizer", "Weather Rendering Optimizer", "Reduces rain and snow particle/quad rendering load.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Low-Density Rain Quads", "Reduces rain and snow particle/quad rendering load.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Skip Distant Precip", "Additional control for Weather Rendering Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Weather Rendering Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
