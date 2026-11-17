package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class SkyColorWheelModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public SkyColorWheelModule() {
        super("sky_color_wheel", "Sky Color Wheel", "Customizes horizon and zenith sky colors with RGB precision.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Custom Horizon Tint", "Customizes horizon and zenith sky colors with RGB precision.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Blend Sunset Colors", "Additional control for Sky Color Wheel", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Sky Color Wheel Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
