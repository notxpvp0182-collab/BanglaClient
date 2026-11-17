package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class NoViewBobbingModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public NoViewBobbingModule() {
        super("no_view_bobbing", "No View Bobbing", "Disables screen walk bobbing while keeping first-person hand steady.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Disable Screen Bob", "Disables screen walk bobbing while keeping first-person hand steady.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Keep Hand Bobbing", "Additional control for No View Bobbing", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "No View Bobbing Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
