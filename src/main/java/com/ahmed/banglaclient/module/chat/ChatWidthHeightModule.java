package com.ahmed.banglaclient.module.chat;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ChatWidthHeightModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ChatWidthHeightModule() {
        super("chat_width_height", "Chat Width/Height", "Customizes chat box width and unfocused/focused height.", ModuleCategory.CHAT, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Wide Chat Box", "Customizes chat box width and unfocused/focused height.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Tall History View", "Additional control for Chat Width/Height", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Chat Width/Height Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
