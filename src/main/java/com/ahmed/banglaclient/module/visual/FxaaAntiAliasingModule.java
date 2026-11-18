package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class FxaaAntiAliasingModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public FxaaAntiAliasingModule() {
        super("fxaa_anti_aliasing", "FXAA Anti-Aliasing", "Smooths jagged block and entity edges using fast FXAA.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Subpixel AA", "Smooths jagged block and entity edges using fast FXAA.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Preserve Text Sharpness", "Additional control for FXAA Anti-Aliasing", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "FXAA Anti-Aliasing Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
