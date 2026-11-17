package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;

public final class MouseTweaksModule extends Module {
    private final BooleanSetting wheelTransferSetting;
    private final BooleanSetting dragMoveSetting;

    public MouseTweaksModule() {
        super("mouse_tweaks", "Mouse Tweaks", "Enhances mouse dragging and scroll-wheel item transfers in inventories.", ModuleCategory.INVENTORY, -1, true);
        this.wheelTransferSetting = addSetting(new BooleanSetting("wheel_transfer", "Scroll Wheel Item Transfer", "Scroll over slots to move items", true));
        this.dragMoveSetting = addSetting(new BooleanSetting("lmb_drag", "Fast Shift-LMB Dragging", "Hold Shift + Left Click and drag across slots", true));
        addAliases("mousetweaks", "inventorytweaks", "accurateblockplacement");
    }
}
