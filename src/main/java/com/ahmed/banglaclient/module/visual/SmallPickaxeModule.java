package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class SmallPickaxeModule extends Module {
    private final NumberSetting scaleSetting;
    private final NumberSetting offsetXSetting;
    private final NumberSetting offsetYSetting;
    private final NumberSetting offsetZSetting;

    public SmallPickaxeModule() {
        super("small_pickaxe", "Small Pickaxe", "Independently scales and positions held Small Pickaxe in first-person view.", ModuleCategory.VISUAL);
        this.scaleSetting = addSetting(new NumberSetting("scale", "Small Pickaxe Scale", "Size percentage", 75.0, 20.0, 150.0, 5.0, "%"));
        this.offsetXSetting = addSetting(new NumberSetting("offset_x", "Position X", "Horizontal offset", 0.0, -0.6, 0.6, 0.05, ""));
        this.offsetYSetting = addSetting(new NumberSetting("offset_y", "Position Y", "Vertical offset", -0.05, -0.6, 0.6, 0.05, ""));
        this.offsetZSetting = addSetting(new NumberSetting("offset_z", "Position Z", "Depth offset", 0.0, -0.6, 0.6, 0.05, ""));
    }

    public float getScale() { return scaleSetting.getFloatValue() / 100.0f; }
    public float getOffsetX() { return offsetXSetting.getFloatValue(); }
    public float getOffsetY() { return offsetYSetting.getFloatValue(); }
    public float getOffsetZ() { return offsetZSetting.getFloatValue(); }
}
