package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class MenuFpsLimiterModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public MenuFpsLimiterModule() {
        super("menu_fps_limiter", "Menu FPS Limiter", "Applies a dedicated smooth FPS limit while in menus.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Cap Menu at 60 FPS", "Applies a dedicated smooth FPS limit while in menus.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Uncap in Game", "Additional control for Menu FPS Limiter", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Menu FPS Limiter Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
