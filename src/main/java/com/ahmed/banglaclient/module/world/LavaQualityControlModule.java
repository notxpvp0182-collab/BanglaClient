package com.ahmed.banglaclient.module.world;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class LavaQualityControlModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public LavaQualityControlModule() {
        super("lava_quality_control", "Lava Quality Control", "Adjusts lava brightness and animation smoothness.", ModuleCategory.WORLD, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Low-Glare Lava", "Adjusts lava brightness and animation smoothness.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Disable Lava Pop", "Additional control for Lava Quality Control", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Lava Quality Control Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
