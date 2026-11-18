package com.ahmed.banglaclient.module.chat;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class KeywordHighlightModule extends Module {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting levelSetting;

    public KeywordHighlightModule() {
        super("keyword_highlight", "Keyword Highlight", "Highlights custom keywords and your username in chat.", ModuleCategory.CHAT, -1, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Highlight Own Name", "Highlights custom keywords and your username in chat.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Bold Matched Words", "Additional control for Keyword Highlight", true));
        this.levelSetting = addSetting(new NumberSetting("strength", "Keyword Highlight Level", "Intensity / threshold percentage", 80.0, 10.0, 150.0, 5.0, "%"));
    }

    public float getLevelMultiplier() {
        return isEnabled() ? (levelSetting.getFloatValue() / 100.0f) : 1.0f;
    }
}
