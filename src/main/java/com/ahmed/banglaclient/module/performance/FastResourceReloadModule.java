package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class FastResourceReloadModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public FastResourceReloadModule() {
        super("fast_resource_reload", "Fast Resource Reload", "Reduces freeze time when applying or removing resource packs.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Async Sound Reload", "Reduces freeze time when applying or removing resource packs.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Fast Mipmap Gen", "Additional control for Fast Resource Reload", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Fast Resource Reload Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
