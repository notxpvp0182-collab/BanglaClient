package com.ahmed.banglaclient.module.chat;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ChatScaleModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ChatScaleModule() {
        super("chat_scale", "Chat Scale", "Customizes chat text scale independently of GUI scale.", ModuleCategory.CHAT, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Crisp Small Text", "Customizes chat text scale independently of GUI scale.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Auto Wrap Lines", "Additional control for Chat Scale", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Chat Scale Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
