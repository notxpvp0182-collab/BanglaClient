package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ResolutionControlModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ResolutionControlModule() {
        super("resolution_control", "Resolution Control", "Controls window and fullscreen rendering resolution preset.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Lock Aspect Ratio", "Controls window and fullscreen rendering resolution preset.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Fast Window Resize", "Additional control for Resolution Control", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Resolution Control Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
