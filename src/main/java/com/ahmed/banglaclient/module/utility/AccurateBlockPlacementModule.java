package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class AccurateBlockPlacementModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public AccurateBlockPlacementModule() {
        super("accurate_block_placement", "Accurate Block Placement", "Prevents misplacing blocks when bridging or fast-placing.", ModuleCategory.UTILITY, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Lock Placement Face", "Prevents misplacing blocks when bridging or fast-placing.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Safe Edge Bridging", "Additional control for Accurate Block Placement", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Accurate Block Placement Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
