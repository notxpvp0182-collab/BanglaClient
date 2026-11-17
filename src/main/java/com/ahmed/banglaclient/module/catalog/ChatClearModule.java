package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class ChatClearModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public ChatClearModule() {
        super("chat_clear", "Chat Clear", "Quickly clears all chat messages on screen when toggled.", ModuleCategory.CHAT, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Keep Sent History", "Quickly clears all chat messages on screen when toggled.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Confirm Before Clear", "Additional control for Chat Clear", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Chat Clear Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    @Override
    protected void onEnable() {
        if (client != null && client.inGameHud != null && client.inGameHud.getChatHud() != null) {
            client.inGameHud.getChatHud().clear(opt1.isEnabled());
        }
        setEnabled(false);
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
