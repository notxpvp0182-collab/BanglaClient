package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;

public final class TntFuseHudStandaloneModule extends Module {
    private final BooleanSetting activeOpt;

    public TntFuseHudStandaloneModule() {
        super("tnt_fuse_hud", "TNT Fuse HUD", "Displays countdown timer of nearby primed TNT.", ModuleCategory.COMBAT);
        this.activeOpt = addSetting(new BooleanSetting("active_opt", "Enable TNT Fuse HUD", "Displays countdown timer of nearby primed TNT.", true));
    }
}
