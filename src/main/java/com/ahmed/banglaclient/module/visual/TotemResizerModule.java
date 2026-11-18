package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class TotemResizerModule extends Module {
    private final NumberSetting mainHandScaleSetting;
    private final NumberSetting offHandScaleSetting;
    private final NumberSetting verticalOffsetSetting;

    public TotemResizerModule() {
        super("totem_resizer", "Totem Resizer", "Fine-grained percentage size and height control for Mainhand & Offhand Totems.", ModuleCategory.VISUAL);
        this.mainHandScaleSetting = addSetting(new NumberSetting("main_scale", "Mainhand Totem Size", "Size when holding Totem in main hand", 60.0, 15.0, 140.0, 5.0, "%"));
        this.offHandScaleSetting = addSetting(new NumberSetting("off_scale", "Offhand Totem Size", "Size when holding Totem in offhand", 55.0, 15.0, 140.0, 5.0, "%"));
        this.verticalOffsetSetting = addSetting(new NumberSetting("y_offset", "Totem Vertical Offset", "Vertical height offset for Totem", -0.18, -0.60, 0.30, 0.02, ""));
    }

    public float getScale(boolean mainHand) {
        return (mainHand ? mainHandScaleSetting.getFloatValue() : offHandScaleSetting.getFloatValue()) / 100.0f;
    }

    public float getVerticalOffset() { return verticalOffsetSetting.getFloatValue(); }
}
