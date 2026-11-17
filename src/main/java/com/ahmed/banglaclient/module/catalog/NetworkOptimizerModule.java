package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class NetworkOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public NetworkOptimizerModule() {
        super("network_optimizer", "Network Optimizer", "Optimizes Netty packet buffer pooling and compression.", ModuleCategory.UTILITY, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Fast Packet Decode", "Optimizes Netty packet buffer pooling and compression.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Zero-Copy Buffers", "Additional control for Network Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Network Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
