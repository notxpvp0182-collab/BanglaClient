package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * "Item Physics" module providing smooth dropped-item tumbling and flat ground settling
 * without altering server-authoritative item positions, collision, or pickup rules.
 */
public final class ItemPhysicsModule extends Module {
    private final NumberSetting rotationSpeedSetting;
    private final BooleanSetting settleFlatOnGroundSetting;
    private final NumberSetting groundOffsetSetting;

    public ItemPhysicsModule() {
        super(
                "item_physics",
                "Item Physics",
                "Renders dropped items with natural physics rotation and flat surface settling.",
                ModuleCategory.VISUAL
        );
        this.rotationSpeedSetting = addSetting(new NumberSetting(
                "rotation_speed", "Tumble Speed", "Rotation speed multiplier while item is airborne",
                1.0, 0.2, 3.0, 0.1, "x"
        ));
        this.settleFlatOnGroundSetting = addSetting(new BooleanSetting(
                "settle_flat", "Settle Flat on Ground", "Rest dropped items flat on the surface instead of floating upright", true
        ));
        this.groundOffsetSetting = addSetting(new NumberSetting(
                "ground_offset", "Surface Height Offset", "Vertical offset so settled items never look buried in the ground",
                0.04, 0.0, 0.15, 0.01, "m"
        ));
        addAliases("itemphysics", "droppeditems", "realisticitems", "physics");
    }

    public float getRotationSpeed() {
        return rotationSpeedSetting.getFloatValue();
    }

    public boolean shouldSettleFlat() {
        return settleFlatOnGroundSetting.isEnabled();
    }

    public float getGroundOffset() {
        return groundOffsetSetting.getFloatValue();
    }
}
