package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class XpOptimizerModule extends Module {
    private final NumberSetting maxXpDistanceSetting;

    public XpOptimizerModule() {
        super("xp_optimizer", "XP Optimizer",
                "Limits rendering of distant Experience Orbs to eliminate XP farm frame drops.",
                ModuleCategory.PERFORMANCE);
        this.maxXpDistanceSetting = addSetting(new NumberSetting("xp_dist", "Max XP Orb Distance", "Max distance in blocks to render XP orbs", 32.0, 10.0, 80.0, 4.0, "m"));
        addAliases("xporboptimizer", "xpfarm", "xplag");
    }

    public double getMaxDistanceSq() {
        double d = maxXpDistanceSetting.getValue();
        return d * d;
    }
}
