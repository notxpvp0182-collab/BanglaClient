package com.ahmed.banglaclient.module.chat;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.ColorSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Module 84: Chat Background
 * Customizes chat background transparency, allows completely transparent chat, or applies a smoked glass tint.
 */
public final class ChatBackgroundModule extends Module {
    private final BooleanSetting transparentChatSetting;
    private final NumberSetting opacityPercentSetting;
    private final ColorSetting tintColorSetting;

    public ChatBackgroundModule() {
        super(
                "chat_background",
                "Chat Background",
                "Customizes chat background transparency and smoked-glass tint.",
                ModuleCategory.CHAT
        );
        this.transparentChatSetting = addSetting(new BooleanSetting(
                "clear_background", "Completely Transparent", "Remove the dark rectangle behind chat messages completely", false
        ));
        this.opacityPercentSetting = addSetting(new NumberSetting(
                "opacity", "Background Opacity", "Opacity percentage of the chat background when not completely transparent",
                45.0, 0.0, 100.0, 5.0, "%"
        )).visibleWhen(() -> !transparentChatSetting.isEnabled()) instanceof NumberSetting ns ? ns : null;
        this.tintColorSetting = addSetting(new ColorSetting(
                "tint_color", "Glass Tint Color", "Base RGB tint for the chat background",
                0xCC080A0F, false
        )).visibleWhen(() -> !transparentChatSetting.isEnabled()) instanceof ColorSetting cs ? cs : null;
        addAliases("clearchat", "chatbg", "chatopacity", "fastchat");
    }

    @Override
    protected void onDisable() {
        if (client != null && client.options != null) {
            client.options.getChatOpacity().setValue(1.0);
        }
    }

    @Override
    public void onTick() {
        if (client == null || client.options == null) {
            return;
        }
        double targetOpacity = transparentChatSetting.isEnabled()
                ? 0.0
                : (opacityPercentSetting != null ? opacityPercentSetting.getValue() / 100.0 : 0.45);
        if (Math.abs(client.options.getChatOpacity().getValue() - targetOpacity) > 0.01) {
            client.options.getChatOpacity().setValue(targetOpacity);
        }
    }

    public boolean isTransparentChat() {
        return isEnabled() && transparentChatSetting.isEnabled();
    }

    public int getCustomTintArgb() {
        return tintColorSetting != null ? tintColorSetting.getArgb() : 0xCC080A0F;
    }
}
