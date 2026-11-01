package com.ahmed.banglaclient.module.chat;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.StringSetting;
import net.minecraft.client.gui.screen.ChatScreen;

/**
 * Module 91: Quick Messages
 * Allows the user to save predefined text messages and quickly insert them into the chat input box
 * (so the user can review or edit before sending, preventing accidental spam).
 */
public final class QuickMessagesModule extends Module {
    private final StringSetting presetOneSetting;
    private final StringSetting presetTwoSetting;
    private final StringSetting presetThreeSetting;
    private final BooleanSetting includeCoordinatesTagSetting;

    public QuickMessagesModule() {
        super(
                "quick_messages",
                "Quick Messages",
                "Saves user-defined messages and inserts them into the chat input bar on trigger.",
                ModuleCategory.CHAT
        );
        this.presetOneSetting = addSetting(new StringSetting(
                "preset_1", "Primary Quick Message", "Text inserted into chat when triggered",
                "Hello everyone!", 100
        ));
        this.presetTwoSetting = addSetting(new StringSetting(
                "preset_2", "Secondary Quick Message", "Second saved message template",
                "On my way!", 100
        ));
        this.presetThreeSetting = addSetting(new StringSetting(
                "preset_3", "Tertiary Quick Message", "Third saved message template",
                "Good game!", 100
        ));
        this.includeCoordinatesTagSetting = addSetting(new BooleanSetting(
                "append_coords", "Replace {pos} with Coordinates", "Automatically replace {pos} with current X, Y, Z coordinates", true
        ));
        addAliases("macros", "chatpresets", "quickchat");
    }

    @Override
    protected void onEnable() {
        if (client != null && client.player != null && client.currentScreen == null) {
            String draft = formatTemplate(presetOneSetting.getValue());
            client.setScreen(new ChatScreen(draft));
            // Automatically return to standby so it acts as a clean one-shot action when bound to a key
            setEnabled(false);
        }
    }

    public String formatTemplate(String rawTemplate) {
        if (rawTemplate == null) {
            return "";
        }
        if (includeCoordinatesTagSetting.isEnabled() && client != null && client.player != null && rawTemplate.contains("{pos}")) {
            String posStr = String.format("%d, %d, %d",
                    client.player.getBlockX(), client.player.getBlockY(), client.player.getBlockZ());
            return rawTemplate.replace("{pos}", posStr);
        }
        return rawTemplate;
    }

    public String getPresetTwo() {
        return formatTemplate(presetTwoSetting.getValue());
    }

    public String getPresetThree() {
        return formatTemplate(presetThreeSetting.getValue());
    }
}
