package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;

public final class DirectionHudStandaloneModule extends Module {
    private final BooleanSetting activeOpt;

    public DirectionHudStandaloneModule() {
        super("direction_hud", "Direction HUD", "Displays facing cardinal direction.", ModuleCategory.HUD);
        this.activeOpt = addSetting(new BooleanSetting("active_opt", "Enable Direction HUD", "Displays facing cardinal direction.", true));
    }
}
