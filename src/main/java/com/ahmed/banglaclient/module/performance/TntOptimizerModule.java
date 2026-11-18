package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class TntOptimizerModule extends Module {
    private final NumberSetting maxTntDistanceSetting;
    private final BooleanSetting reduceExplosionSmokeSetting;

    public TntOptimizerModule() {
        super("tnt_optimizer", "TNT Optimizer",
                "Reduces primed TNT rendering distance and explosion smoke particles.",
                ModuleCategory.PERFORMANCE);
        this.maxTntDistanceSetting = addSetting(new NumberSetting("tnt_dist", "Max TNT Render Distance", "Max distance to render primed TNT", 56.0, 16.0, 128.0, 4.0, "m"));
        this.reduceExplosionSmokeSetting = addSetting(new BooleanSetting("reduce_smoke", "Reduce Explosion Smoke", "Skip heavy explosion smoke particles", true));
        addAliases("tntlag", "explosionoptimizer");
    }

    public double getMaxDistanceSq() {
        double d = maxTntDistanceSetting.getValue();
        return d * d;
    }
    public boolean shouldReduceSmoke() { return isEnabled() && reduceExplosionSmokeSetting.isEnabled(); }
}
