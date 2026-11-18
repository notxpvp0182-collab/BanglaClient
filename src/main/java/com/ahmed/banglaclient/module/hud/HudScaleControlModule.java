package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class HudScaleControlModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public HudScaleControlModule() {
        super("hud_scale_control", "HUD Scale Control", "Global master scale multiplier for all HUD widgets.", ModuleCategory.HUD, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Lock Aspect Ratio", "Global master scale multiplier for all HUD widgets.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "HiDPI Sharp Text", "Additional control for HUD Scale Control", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "HUD Scale Control Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
