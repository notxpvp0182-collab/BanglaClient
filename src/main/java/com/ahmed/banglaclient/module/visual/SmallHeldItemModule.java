package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * "Small Held Item" module providing generalized first-person held-item scaling and X/Y/Z positioning
 * for all Minecraft items without breaking swing or item-use animations.
 */
public final class SmallHeldItemModule extends Module {
    private final NumberSetting mainHandScaleSetting;
    private final NumberSetting offHandScaleSetting;
    private final NumberSetting offsetXSetting;
    private final NumberSetting offsetYSetting;
    private final NumberSetting offsetZSetting;
    private final BooleanSetting resetDefaultsActionSetting;

    public SmallHeldItemModule() {
        super(
                "small_held_item",
                "Small Held Item",
                "Reduces and customizes the size and position of held items in first-person view.",
                ModuleCategory.VISUAL
        );
        this.mainHandScaleSetting = addSetting(new NumberSetting(
                "item_size", "Main Hand Item Size", "Scale multiplier for main-hand held items",
                0.65, 0.20, 1.50, 0.05, "x"
        ));
        this.offHandScaleSetting = addSetting(new NumberSetting(
                "offhand_size", "Offhand Item Size", "Scale multiplier for offhand held items",
                0.65, 0.20, 1.50, 0.05, "x"
        ));
        this.offsetXSetting = addSetting(new NumberSetting(
                "offset_x", "Horizontal X Offset", "Shift held item left or right",
                0.0, -0.60, 0.60, 0.02, ""
        ));
        this.offsetYSetting = addSetting(new NumberSetting(
                "offset_y", "Vertical Y Offset", "Shift held item down or up",
                -0.06, -0.60, 0.60, 0.02, ""
        ));
        this.offsetZSetting = addSetting(new NumberSetting(
                "offset_z", "Depth Z Offset", "Shift held item forward or backward",
                0.0, -0.60, 0.60, 0.02, ""
        ));
        this.resetDefaultsActionSetting = addSetting(new BooleanSetting(
                "reset_action", "Action: Reset to Default", "Toggle ON to reset held item size and offsets to defaults", false
        ));
        this.resetDefaultsActionSetting.onChange(val -> {
            if (Boolean.TRUE.equals(val)) {
                resetSettingsToDefault();
                resetDefaultsActionSetting.setValue(false);
            }
        });
        addAliases("smallitems", "viewmodel", "handscale", "itemsize", "miniitems");
    }

    public float getMainHandScale() { return mainHandScaleSetting.getFloatValue(); }
    public float getOffHandScale() { return offHandScaleSetting.getFloatValue(); }
    public float getOffsetX() { return offsetXSetting.getFloatValue(); }
    public float getOffsetY() { return offsetYSetting.getFloatValue(); }
    public float getOffsetZ() { return offsetZSetting.getFloatValue(); }
}
