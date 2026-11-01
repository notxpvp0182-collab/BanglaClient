package com.ahmed.banglaclient.module.chat;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Module 83: Chat Customizer
 * Customizes chat scale, line spacing, vertical chat offset, and preserves chat history across world switches.
 */
public final class ChatCustomizerModule extends Module {
    private final NumberSetting chatScaleSetting;
    private final NumberSetting lineSpacingSetting;
    private final NumberSetting verticalOffsetSetting;
    private final BooleanSetting keepHistoryOnDisconnectSetting;

    public ChatCustomizerModule() {
        super(
                "chat_customizer",
                "Chat Customizer",
                "Customizes chat scale, line spacing, vertical offset, and chat history retention.",
                ModuleCategory.CHAT
        );
        this.chatScaleSetting = addSetting(new NumberSetting(
                "chat_scale", "Chat Text Scale", "Scale percentage for chat font size",
                100.0, 50.0, 150.0, 5.0, "%"
        ));
        this.lineSpacingSetting = addSetting(new NumberSetting(
                "line_spacing", "Line Spacing", "Spacing percentage between consecutive chat lines",
                10.0, 0.0, 100.0, 5.0, "%"
        ));
        this.verticalOffsetSetting = addSetting(new NumberSetting(
                "y_offset", "Vertical Offset", "Shift the chat box upward in pixels (useful above armor/status bars)",
                0.0, 0.0, 40.0, 2.0, "px"
        ));
        this.keepHistoryOnDisconnectSetting = addSetting(new BooleanSetting(
                "keep_history", "Keep History Across Worlds", "Prevent clearing local chat history when switching worlds or servers", true
        ));
        addAliases("chatsettings", "chatfont", "chatsize", "chathistory");
    }

    @Override
    public void onTick() {
        if (client == null || client.options == null) {
            return;
        }
        double targetScale = chatScaleSetting.getValue() / 100.0;
        double targetSpacing = lineSpacingSetting.getValue() / 100.0;

        if (Math.abs(client.options.getChatScale().getValue() - targetScale) > 0.01) {
            client.options.getChatScale().setValue(targetScale);
        }
        if (Math.abs(client.options.getChatLineSpacing().getValue() - targetSpacing) > 0.01) {
            client.options.getChatLineSpacing().setValue(targetSpacing);
        }
    }

    public int getVerticalOffsetPx() {
        return isEnabled() ? verticalOffsetSetting.getIntValue() : 0;
    }

    public boolean shouldKeepHistoryOnDisconnect() {
        return isEnabled() && keepHistoryOnDisconnectSetting.isEnabled();
    }
}
