package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class BeaconBeamOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public BeaconBeamOptimizerModule() {
        super("beacon_beam_optimizer", "Beacon Beam Optimizer", "Limits beacon beam outer glow and distant rendering load.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Cull Far Beacon Beams", "Limits beacon beam outer glow and distant rendering load.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Disable Outer Glow", "Additional control for Beacon Beam Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Beacon Beam Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
