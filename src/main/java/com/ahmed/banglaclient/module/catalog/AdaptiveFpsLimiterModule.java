package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class AdaptiveFpsLimiterModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public AdaptiveFpsLimiterModule() {
        super("adaptive_fps_limiter", "Adaptive FPS Limiter", "Dynamically adjusts FPS cap based on activity and combat.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Boost in Combat", "Dynamically adjusts FPS cap based on activity and combat.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Save Power When Still", "Additional control for Adaptive FPS Limiter", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Adaptive FPS Limiter Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
