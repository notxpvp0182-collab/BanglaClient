package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class EntityCullingModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public EntityCullingModule() {
        super("entity_culling", "Entity Culling", "Skips rendering entities outside the camera frustum or behind walls.", ModuleCategory.PERFORMANCE, -1, true);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Frustum Culling", "Skips rendering entities outside the camera frustum or behind walls.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Wall Occlusion Check", "Additional control for Entity Culling", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Entity Culling Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
