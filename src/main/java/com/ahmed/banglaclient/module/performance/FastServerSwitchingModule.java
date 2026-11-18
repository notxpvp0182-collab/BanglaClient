package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class FastServerSwitchingModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public FastServerSwitchingModule() {
        super("fast_server_switching", "Fast Server Switching", "Clears network state quickly when switching multiplayer servers.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Instant Socket Reset", "Clears network state quickly when switching multiplayer servers.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Reuse Texture Cache", "Additional control for Fast Server Switching", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Fast Server Switching Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
