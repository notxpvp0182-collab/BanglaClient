package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;

public final class PingGraphStandaloneModule extends Module {
    private final BooleanSetting activeOpt;

    public PingGraphStandaloneModule() {
        super("ping_graph", "Ping Graph", "Displays real-time server latency stability.", ModuleCategory.HUD);
        this.activeOpt = addSetting(new BooleanSetting("active_opt", "Enable Ping Graph", "Displays real-time server latency stability.", true));
    }
}
