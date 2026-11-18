package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class CustomTablistModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public CustomTablistModule() {
        super("custom_tablist", "Custom Tablist", "Customizes Tablist background glass, numerical ping, and header/footer.", ModuleCategory.HUD, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Show Numerical Ping", "Customizes Tablist background glass, numerical ping, and header/footer.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "AMOLED Glass Tablist", "Additional control for Custom Tablist", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Custom Tablist Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
