package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class SmartRenderDistanceModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public SmartRenderDistanceModule() {
        super("smart_render_distance", "Smart Render Distance", "Automatically lowers render distance during sudden FPS drops.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Auto Drop Recovery", "Automatically lowers render distance during sudden FPS drops.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Smooth Distance Step", "Additional control for Smart Render Distance", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Smart Render Distance Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
