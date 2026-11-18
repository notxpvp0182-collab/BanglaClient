package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ProjectileTrajectoryPreviewModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ProjectileTrajectoryPreviewModule() {
        super("projectile_trajectory_preview", "Projectile Trajectory Preview", "Previews landing trajectory for bows, pearls, and tridents.", ModuleCategory.COMBAT, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Show Landing Box", "Previews landing trajectory for bows, pearls, and tridents.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Color by Entity Hit", "Additional control for Projectile Trajectory Preview", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Projectile Trajectory Preview Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
