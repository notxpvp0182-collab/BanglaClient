package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class BalancedProfileModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public BalancedProfileModule() {
        super("balanced_profile", "Balanced Profile", "Balances high FPS with crisp visual quality.", ModuleCategory.PERFORMANCE, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Smart Culling", "Balances high FPS with crisp visual quality.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Medium Particle Limit", "Additional control for Balanced Profile", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Balanced Profile Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
