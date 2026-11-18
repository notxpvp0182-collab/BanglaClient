package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;

public final class ItemRenderingOptimizerModule extends Module {
    private final BooleanSetting activeOpt;

    public ItemRenderingOptimizerModule() {
        super("item_rendering_optimizer", "Item Rendering Optimizer", "Optimizes dropped and held item rendering.", ModuleCategory.PERFORMANCE);
        this.activeOpt = addSetting(new BooleanSetting("active_opt", "Enable Item Rendering Optimizer", "Optimizes dropped and held item rendering.", true));
    }
}
