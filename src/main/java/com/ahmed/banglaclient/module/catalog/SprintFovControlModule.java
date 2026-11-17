package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class SprintFovControlModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public SprintFovControlModule() {
        super("sprint_fov_control", "Sprint FOV Control", "Controls or disables FOV widening when sprinting.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Lock Sprint FOV", "Controls or disables FOV widening when sprinting.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Custom Sprint Boost", "Additional control for Sprint FOV Control", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Sprint FOV Control Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
