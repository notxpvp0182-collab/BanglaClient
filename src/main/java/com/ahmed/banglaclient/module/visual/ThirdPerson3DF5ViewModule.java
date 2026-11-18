package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ThirdPerson3DF5ViewModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ThirdPerson3DF5ViewModule() {
        super("third_person_3d_view", "3D F5 View", "Enhances third-person F5 view with centered over-the-shoulder option.", ModuleCategory.VISUAL, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Shoulder Offset", "Enhances third-person F5 view with centered over-the-shoulder option.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Show Front Cape", "Additional control for 3D F5 View", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "3D F5 View Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
