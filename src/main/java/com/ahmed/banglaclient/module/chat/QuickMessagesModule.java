package com.ahmed.banglaclient.module.chat;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.StringSetting;
import net.minecraft.client.gui.screen.ChatScreen;

public final class QuickMessagesModule extends Module {
    private final StringSetting presetOneSetting;
    private final StringSetting presetTwoSetting;
    private final StringSetting presetThreeSetting;
    private final BooleanSetting includeCoordinatesTagSetting;

    public QuickMessagesModule() {
        super(
                "quick_messages",
                "Quick Messages",
                "Inserts saved quick messages into the chat input bar when pressing its keybind.",
                ModuleCategory.CHAT
        );
        setTriggerKeybindWhileEnabledOnly(true);

        this.presetOneSetting = addSetting(new StringSetting("preset_1", "Primary Quick Message", "Text inserted into chat when keybind is pressed", "Hello everyone!", 100));
        this.presetTwoSetting = addSetting(new StringSetting("preset_2", "Secondary Quick Message", "Second saved message template", "On my way! {pos}", 100));
        this.presetThreeSetting = addSetting(new StringSetting("preset_3", "Tertiary Quick Message", "Third saved message template", "Good game!", 100));
        this.includeCoordinatesTagSetting = addSetting(new BooleanSetting("append_coords", "Replace {pos} with Coordinates", "Automatically replace {pos} with current X, Y, Z coordinates", true));
        addAliases("macros", "chatpresets", "quickchat");
    }

    @Override
    public void onKeybindPressed() {
        if (isEnabled() && client != null && client.player != null && client.currentScreen == null) {
            String draft = formatTemplate(presetOneSetting.getValue());
            client.setScreen(new ChatScreen(draft, true));
        }
    }

    public String formatTemplate(String rawTemplate) {
        if (rawTemplate == null) return "";
        if (includeCoordinatesTagSetting.isEnabled() && client != null && client.player != null && rawTemplate.contains("{pos}")) {
            String posStr = String.format("%d, %d, %d",
                    client.player.getBlockX(), client.player.getBlockY(), client.player.getBlockZ());
            return rawTemplate.replace("{pos}", posStr);
        }
        return rawTemplate;
    }

    public String getPresetTwo() { return formatTemplate(presetTwoSetting.getValue()); }
    public String getPresetThree() { return formatTemplate(presetThreeSetting.getValue()); }
}
