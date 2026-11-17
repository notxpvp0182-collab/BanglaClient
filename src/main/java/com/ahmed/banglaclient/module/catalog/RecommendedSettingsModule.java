package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class RecommendedSettingsModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public RecommendedSettingsModule() {
        super("recommended_settings", "Recommended Settings", "Applies optimal settings tailored to your detected hardware.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Auto Apply Best Preset", "Applies optimal settings tailored to your detected hardware.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Notify on Bottleneck", "Additional control for Recommended Settings", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Recommended Settings Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
