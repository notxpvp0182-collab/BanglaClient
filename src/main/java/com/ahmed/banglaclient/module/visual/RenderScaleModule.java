package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class RenderScaleModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public RenderScaleModule() {
        super("render_scale", "Render Scale", "Adjusts 3D viewport rendering scale to boost FPS on low-end GPUs.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Performance Scale", "Adjusts 3D viewport rendering scale to boost FPS on low-end GPUs.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Sharp Upscale", "Additional control for Render Scale", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Render Scale Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
