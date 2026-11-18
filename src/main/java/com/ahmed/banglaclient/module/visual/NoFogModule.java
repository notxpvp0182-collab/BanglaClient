package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class NoFogModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public NoFogModule() {
        super("no_fog", "No Fog", "Completely disables Overworld, Nether, and underwater distance fog.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Disable Nether Fog", "Completely disables Overworld, Nether, and underwater distance fog.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Disable Terrain Fog", "Additional control for No Fog", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "No Fog Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
