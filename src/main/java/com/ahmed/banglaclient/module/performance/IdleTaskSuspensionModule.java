package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class IdleTaskSuspensionModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public IdleTaskSuspensionModule() {
        super("idle_task_suspension", "Idle Task Suspension", "Suspends non-essential background tasks when idle.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Suspend Idle Workers", "Suspends non-essential background tasks when idle.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Wake on Input", "Additional control for Idle Task Suspension", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Idle Task Suspension Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
