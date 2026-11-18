package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ExtremeFpsProfileModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ExtremeFpsProfileModule() {
        super("extreme_fps_profile", "Extreme FPS Profile", "Aggressively disables non-essential visuals for maximum competitive FPS.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Max Culling & Batching", "Aggressively disables non-essential visuals for maximum competitive FPS.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Minimal HUD Overhead", "Additional control for Extreme FPS Profile", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Extreme FPS Profile Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
