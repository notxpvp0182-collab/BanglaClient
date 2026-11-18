package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class HdrToggleModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public HdrToggleModule() {
        super("hdr_toggle", "HDR Toggle", "Enhances dynamic range and highlight contrast on supported displays.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Vivid Highlights", "Enhances dynamic range and highlight contrast on supported displays.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Deep Shadow Tone", "Additional control for HDR Toggle", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "HDR Toggle Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
