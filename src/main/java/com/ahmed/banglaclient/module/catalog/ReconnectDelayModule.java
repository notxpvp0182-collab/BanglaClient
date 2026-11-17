package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ReconnectDelayModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ReconnectDelayModule() {
        super("reconnect_delay", "Reconnect Delay", "Configures delay (5s) before Auto Reconnect rejoins.", ModuleCategory.UTILITY, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "5 Second Delay", "Configures delay (5s) before Auto Reconnect rejoins.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Cancel on ESC", "Additional control for Reconnect Delay", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Reconnect Delay Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
