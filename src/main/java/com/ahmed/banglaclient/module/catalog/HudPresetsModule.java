package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class HudPresetsModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public HudPresetsModule() {
        super("hud_presets", "HUD Presets", "Quickly switch between PvP, Minimal, Survival, and Streamer HUD layouts.", ModuleCategory.HUD, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Apply PvP Layout", "Quickly switch between PvP, Minimal, Survival, and Streamer HUD layouts.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Apply Minimal Layout", "Additional control for HUD Presets", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "HUD Presets Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
