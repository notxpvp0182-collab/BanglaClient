package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class LoadingIndicatorModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public LoadingIndicatorModule() {
        super("loading_indicator", "Loading Indicator", "Displays a sleek progress indicator when chunks or assets are loading.", ModuleCategory.HUD, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Show Chunk Load Spinner", "Displays a sleek progress indicator when chunks or assets are loading.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Compact Corner Badge", "Additional control for Loading Indicator", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Loading Indicator Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
