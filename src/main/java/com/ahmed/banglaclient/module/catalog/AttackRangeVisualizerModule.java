package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class AttackRangeVisualizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public AttackRangeVisualizerModule() {
        super("attack_range_visualizer", "Attack Range Visualizer", "Helps visualize the 3.0-block combat attack range.", ModuleCategory.COMBAT, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Highlight In-Range", "Helps visualize the 3.0-block combat attack range.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Show 3.0m Ring", "Additional control for Attack Range Visualizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Attack Range Visualizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
