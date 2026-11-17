package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class CameraSmoothingModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public CameraSmoothingModule() {
        super("camera_smoothing", "Camera Smoothing", "Adds smooth cinematic damping to camera rotation.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Smooth Pitch & Yaw", "Adds smooth cinematic damping to camera rotation.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Preserve PvP Flick", "Additional control for Camera Smoothing", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Camera Smoothing Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
