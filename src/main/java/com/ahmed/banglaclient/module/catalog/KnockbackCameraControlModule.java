package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class KnockbackCameraControlModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public KnockbackCameraControlModule() {
        super("knockback_camera_control", "Knockback Camera Control", "Stabilizes camera view during heavy knockback.", ModuleCategory.COMBAT, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Dampen Vertical Jolt", "Stabilizes camera view during heavy knockback.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Keep Crosshair Steady", "Additional control for Knockback Camera Control", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Knockback Camera Control Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
