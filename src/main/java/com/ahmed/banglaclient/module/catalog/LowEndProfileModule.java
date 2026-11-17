package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class LowEndProfileModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public LowEndProfileModule() {
        super("low_end_profile", "Low-End Profile", "Optimizes settings for 4GB RAM and integrated graphics for maximum FPS.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Aggressive Entity/Particle Cull", "Optimizes settings for 4GB RAM and integrated graphics for maximum FPS.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Disable Heavy Shaders", "Additional control for Low-End Profile", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Low-End Profile Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
