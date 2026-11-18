package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class NoCameraShakeModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public NoCameraShakeModule() {
        super("no_camera_shake", "No Camera Shake", "Disables explosion, warden sonic boom, and damage camera shake.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "No Explosion Shake", "Disables explosion, warden sonic boom, and damage camera shake.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "No Warden Shake", "Additional control for No Camera Shake", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "No Camera Shake Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
