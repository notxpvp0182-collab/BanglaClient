package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ViewmodelEditorModule extends Module {
    private final NumberSetting posX, posY, posZ;
    private final NumberSetting rotX, rotY, rotZ;
    private final NumberSetting scale;

    public ViewmodelEditorModule() {
        super("viewmodel_editor", "Viewmodel Editor", "Customizes held-item X/Y/Z position, X/Y/Z rotation angles, and scale.", ModuleCategory.VISUAL);
        this.posX = addSetting(new NumberSetting("pos_x", "Position X", "Horizontal hand offset", 0.0, -1.0, 1.0, 0.05, ""));
        this.posY = addSetting(new NumberSetting("pos_y", "Position Y", "Vertical hand offset", 0.0, -1.0, 1.0, 0.05, ""));
        this.posZ = addSetting(new NumberSetting("pos_z", "Position Z", "Depth hand offset", 0.0, -1.0, 1.0, 0.05, ""));
        this.rotX = addSetting(new NumberSetting("rot_x", "Rotation X", "Pitch rotation in degrees", 0.0, -180.0, 180.0, 5.0, "°"));
        this.rotY = addSetting(new NumberSetting("rot_y", "Rotation Y", "Yaw rotation in degrees", 0.0, -180.0, 180.0, 5.0, "°"));
        this.rotZ = addSetting(new NumberSetting("rot_z", "Rotation Z", "Roll rotation in degrees", 0.0, -180.0, 180.0, 5.0, "°"));
        this.scale = addSetting(new NumberSetting("scale", "Viewmodel Scale", "Global held item scale", 100.0, 25.0, 160.0, 5.0, "%"));
    }

    public float getPosX() { return posX.getFloatValue(); }
    public float getPosY() { return posY.getFloatValue(); }
    public float getPosZ() { return posZ.getFloatValue(); }
    public float getRotX() { return rotX.getFloatValue(); }
    public float getRotY() { return rotY.getFloatValue(); }
    public float getRotZ() { return rotZ.getFloatValue(); }
    public float getScale() { return scale.getFloatValue() / 100.0f; }
}
