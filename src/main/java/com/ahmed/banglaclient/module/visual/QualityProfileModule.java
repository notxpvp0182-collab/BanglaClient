package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class QualityProfileModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public QualityProfileModule() {
        super("quality_profile", "Quality Profile", "Prioritizes maximum visual fidelity, smooth animations, and HD glass.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Ultra Glass Quality", "Prioritizes maximum visual fidelity, smooth animations, and HD glass.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Full Particle Fidelity", "Additional control for Quality Profile", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Quality Profile Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
