package com.ahmed.banglaclient.module.audio;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class VoiceChatSupportModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public VoiceChatSupportModule() {
        super("voice_chat", "Voice Chat", "Integrates voice chat status indicator on supported servers.", ModuleCategory.AUDIO, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Show Mic Mute Icon", "Integrates voice chat status indicator on supported servers.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Show Speaking Players", "Additional control for Voice Chat", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Voice Chat Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
