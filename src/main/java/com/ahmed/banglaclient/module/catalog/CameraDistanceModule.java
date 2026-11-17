package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class CameraDistanceModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public CameraDistanceModule() {
        super("camera_distance", "Camera Distance", "Customizes third-person (F5) camera distance from 2m to 12m.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Smooth Zoom Out", "Customizes third-person (F5) camera distance from 2m to 12m.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Ignore Leaves", "Additional control for Camera Distance", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Camera Distance Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
