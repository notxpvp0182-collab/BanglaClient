package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class GuiRenderingOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public GuiRenderingOptimizerModule() {
        super("gui_rendering_optimizer", "GUI Rendering Optimizer", "Caches static UI geometry and text widths for zero-lag menus.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Cache Text Widths", "Caches static UI geometry and text widths for zero-lag menus.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Batch Glass Panels", "Additional control for GUI Rendering Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "GUI Rendering Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
