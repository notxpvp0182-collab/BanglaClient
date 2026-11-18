package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class NoShadowBlocksModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public NoShadowBlocksModule() {
        super("no_shadow_blocks", "No Shadow Blocks", "Flattens dark block side shading for a cleaner look and higher FPS.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Flat Block Shading", "Flattens dark block side shading for a cleaner look and higher FPS.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Disable Entity Shadows", "Additional control for No Shadow Blocks", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "No Shadow Blocks Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
