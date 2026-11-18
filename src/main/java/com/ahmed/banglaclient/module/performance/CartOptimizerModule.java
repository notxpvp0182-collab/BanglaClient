package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class CartOptimizerModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public CartOptimizerModule() {
        super("cart_optimizer", "Cart Optimizer", "Optimizes minecart entity rendering and stacked cart culling.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Cull Stacked Minecarts", "Optimizes minecart entity rendering and stacked cart culling.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Skip Far Wheel Roll", "Additional control for Cart Optimizer", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Cart Optimizer Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
