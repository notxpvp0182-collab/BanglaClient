package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class MotionBlurControlModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public MotionBlurControlModule() {
        super("motion_blur_control", "Motion Blur Control", "Applies smooth frame-blended motion blur during fast camera turns.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Smooth Blur Shader", "Applies smooth frame-blended motion blur during fast camera turns.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Adaptive High-FPS Blur", "Additional control for Motion Blur Control", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Motion Blur Control Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
