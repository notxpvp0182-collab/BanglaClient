package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class BackgroundModeModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public BackgroundModeModule() {
        super("background_mode", "Background Mode", "Minimizes RAM and CPU usage when Minecraft is in the background.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Drop to 10 FPS Minimized", "Minimizes RAM and CPU usage when Minecraft is in the background.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Pause Background Animations", "Additional control for Background Mode", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Background Mode Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
