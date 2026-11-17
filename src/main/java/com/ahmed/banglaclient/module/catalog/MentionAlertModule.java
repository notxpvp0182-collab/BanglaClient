package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class MentionAlertModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public MentionAlertModule() {
        super("mention_alert", "Mention Alert", "Plays a ping sound and toast alert when someone mentions your name.", ModuleCategory.CHAT, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Play Ping Sound", "Plays a ping sound and toast alert when someone mentions your name.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Show Toast Alert", "Additional control for Mention Alert", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Mention Alert Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
