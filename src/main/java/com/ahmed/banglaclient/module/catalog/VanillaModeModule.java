package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class VanillaModeModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public VanillaModeModule() {
        super("vanilla_mode", "Vanilla Mode", "Temporarily hides custom HUDs and overlays for a clean vanilla look.", ModuleCategory.UTILITY, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Hide Custom HUDs", "Temporarily hides custom HUDs and overlays for a clean vanilla look.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Keep FPS Optimizations", "Additional control for Vanilla Mode", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Vanilla Mode Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
