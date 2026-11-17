package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ClearLavaModule extends Module {
    private final BooleanSetting removeLavaFogSetting;
    private final NumberSetting lavaClaritySetting;

    public ClearLavaModule() {
        super("clear_lava", "Clear Lava", "Removes thick orange lava fog to dramatically improve visibility in lava.", ModuleCategory.VISUAL);
        this.removeLavaFogSetting = addSetting(new BooleanSetting("no_lava_fog", "Remove Submerged Lava Fog", "Bypass dense lava fog when submerged", true));
        this.lavaClaritySetting = addSetting(new NumberSetting("lava_clarity", "Lava Clarity Level", "Visibility percentage inside lava", 100.0, 20.0, 100.0, 5.0, "%"));
        addAliases("lava", "lavaclarity", "lavaquality", "nofoglava");
    }

    public boolean shouldBypassLavaFog() {
        return isEnabled() && removeLavaFogSetting.isEnabled() && lavaClaritySetting.getFloatValue() >= 50.0f;
    }
}
