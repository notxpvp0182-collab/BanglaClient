package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class DarkGuiModeModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public DarkGuiModeModule() {
        super("dark_gui_mode", "Dark GUI Mode", "Applies AMOLED dark glass styling across client menus.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "AMOLED Pitch Black", "Applies AMOLED dark glass styling across client menus.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Crisp White Borders", "Additional control for Dark GUI Mode", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Dark GUI Mode Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
