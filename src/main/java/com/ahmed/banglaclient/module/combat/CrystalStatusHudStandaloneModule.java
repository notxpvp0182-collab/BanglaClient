package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;

public final class CrystalStatusHudStandaloneModule extends Module {
    private final BooleanSetting activeOpt;

    public CrystalStatusHudStandaloneModule() {
        super("crystal_status_hud", "Crystal Status HUD", "Displays nearby End Crystal count and status.", ModuleCategory.COMBAT);
        this.activeOpt = addSetting(new BooleanSetting("active_opt", "Enable Crystal Status HUD", "Displays nearby End Crystal count and status.", true));
    }
}
