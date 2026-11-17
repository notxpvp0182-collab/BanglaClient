package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ContainerPreviewStandaloneModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ContainerPreviewStandaloneModule() {
        super("container_preview", "Container Preview", "Previews contents of Ender Chests, Barrels, and Chests in tooltips.", ModuleCategory.INVENTORY, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Preview Ender Chest", "Previews contents of Ender Chests, Barrels, and Chests in tooltips.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Compact Grid", "Additional control for Container Preview", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Container Preview Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
