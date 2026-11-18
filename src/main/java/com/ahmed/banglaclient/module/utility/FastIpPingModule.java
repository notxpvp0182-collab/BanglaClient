package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class FastIpPingModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public FastIpPingModule() {
        super("fast_ip_ping", "Fast IP Ping", "Pings all multiplayer servers in parallel for instant server list loading.", ModuleCategory.UTILITY, -1, true);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Parallel Server Ping", "Pings all multiplayer servers in parallel for instant server list loading.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Fast Timeout 2s", "Additional control for Fast IP Ping", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Fast IP Ping Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
