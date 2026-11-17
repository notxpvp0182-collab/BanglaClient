package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class AutoReconnectModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public AutoReconnectModule() {
        super("auto_reconnect", "Auto Reconnect", "Automatically reconnects to the server if disconnected.", ModuleCategory.UTILITY, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Auto Reconnect on Kick", "Automatically reconnects to the server if disconnected.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Show Countdown", "Additional control for Auto Reconnect", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Auto Reconnect Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
