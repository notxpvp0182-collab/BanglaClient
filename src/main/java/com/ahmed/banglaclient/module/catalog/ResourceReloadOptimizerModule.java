package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ResourceReloadOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ResourceReloadOptimizerModule() {
        super("resource_reload_optimizer", "Resource Reload Optimizer", "Speeds up F3+T and resource pack switching.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Parallel Pack Scan", "Speeds up F3+T and resource pack switching.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Reuse Unchanged Atlases", "Additional control for Resource Reload Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Resource Reload Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
