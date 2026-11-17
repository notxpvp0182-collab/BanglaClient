package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class BlockFaceCullingModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public BlockFaceCullingModule() {
        super("block_face_culling", "Block Face Culling", "Does not render interior or adjacent hidden block faces.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Strict Adjacent Culling", "Does not render interior or adjacent hidden block faces.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Cull Leaves Interior", "Additional control for Block Face Culling", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Block Face Culling Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
