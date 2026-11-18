package com.ahmed.banglaclient.module.chat;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;

public final class ChatClearModule extends Module {
    private final BooleanSetting clearSentHistorySetting;

    public ChatClearModule() {
        super("chat_clear", "Chat Clear", "Clears visible chat messages immediately when toggled or pressed.", ModuleCategory.CHAT);
        this.clearSentHistorySetting = addSetting(new BooleanSetting("clear_sent", "Clear Sent Command History", "Also clear up-arrow sent message history", false));
        addAliases("clearchat", "purgechat");
    }

    @Override
    protected void onEnable() {
        if (client != null && client.inGameHud != null && client.inGameHud.getChatHud() != null) {
            client.inGameHud.getChatHud().clear(clearSentHistorySetting.isEnabled());
            if (BanglaClient.getInstance().getNotificationManager() != null) {
                BanglaClient.getInstance().getNotificationManager().pushInfo("Chat Clear", "Cleared visible chat history");
            }
        }
        setEnabled(false);
    }
}
