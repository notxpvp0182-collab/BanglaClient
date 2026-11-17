package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class UnfocusedFpsLimiterModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public UnfocusedFpsLimiterModule() {
        super("unfocused_fps_limiter", "Unfocused FPS Limiter", "Reduces FPS to 15 when the game window is minimized or unfocused.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Limit When Unfocused", "Reduces FPS to 15 when the game window is minimized or unfocused.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Mute Audio Unfocused", "Additional control for Unfocused FPS Limiter", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Unfocused FPS Limiter Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
