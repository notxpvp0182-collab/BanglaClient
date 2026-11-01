package com.ahmed.banglaclient.module.chat;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.StringSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Text;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * Module 87: Chat Search
 * Maintains a searchable local history of recent chat messages and displays matching lines
 * when a search query is entered or while `ChatScreen` is open.
 */
public final class ChatSearchModule extends HudModule {
    private static final int MAX_LOGGED_LINES = 300;

    private final StringSetting searchQuerySetting;
    private final Deque<String> messageHistory = new ArrayDeque<>();

    private static boolean historyHookRegistered = false;
    private static ChatSearchModule instance;

    public ChatSearchModule() {
        super(
                "chat_search",
                "Chat Search",
                "Searches available chat history by keyword and displays matching messages.",
                ModuleCategory.CHAT,
                10.0f,
                350.0f,
                210.0f,
                56.0f,
                false
        );
        this.searchQuerySetting = addSetting(new StringSetting(
                "query", "Search Keyword", "Filter chat history for lines containing this keyword", "", 64
        ));
        instance = this;
        registerHistoryHookOnce();
        addAliases("searchchat", "chatlog", "findchat");
    }

    private static synchronized void registerHistoryHookOnce() {
        if (historyHookRegistered) {
            return;
        }
        historyHookRegistered = true;
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (instance != null) {
                instance.recordLine(message);
            }
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay && instance != null) {
                instance.recordLine(message);
            }
        });
    }

    private void recordLine(Text message) {
        if (message == null) {
            return;
        }
        String text = message.getString();
        if (text == null || text.isBlank()) {
            return;
        }
        while (messageHistory.size() >= MAX_LOGGED_LINES) {
            messageHistory.pollFirst();
        }
        messageHistory.addLast(text);
    }

    public List<String> searchHistory(String query, int maxResults) {
        List<String> matches = new ArrayList<>();
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        for (String line : messageHistory) {
            if (q.isEmpty() || line.toLowerCase(Locale.ROOT).contains(q)) {
                matches.add(line);
            }
        }
        if (matches.size() > maxResults) {
            return matches.subList(matches.size() - maxResults, matches.size());
        }
        return matches;
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        String query = searchQuerySetting.getValue().trim();
        boolean inChatScreen = client != null && client.currentScreen instanceof ChatScreen;
        if (!preview && !inChatScreen && query.isEmpty()) {
            return;
        }

        List<String> results = searchHistory(query, 4);
        int pad = paddingSetting.getIntValue();
        int boxW = 210;
        int boxH = Math.max(28, (results.size() + 1) * 12 + pad * 2);

        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());
        Theme theme = GlassRenderer.getTheme();

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(
                    context, x, y, boxW, boxH,
                    backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
            );
        }

        String header = query.isEmpty() ? "Chat History (All)" : ("Chat Search: \"" + query + "\" (" + results.size() + ")");
        context.drawTextWithShadow(client.textRenderer, client.textRenderer.trimToWidth(header, boxW - pad * 2),
                x + pad, y + pad, textColorSetting.getArgb());

        int drawY = y + pad + 13;
        if (results.isEmpty()) {
            context.drawTextWithShadow(client.textRenderer, "No matching chat messages.", x + pad, drawY, theme.getTextSecondary());
        } else {
            for (String line : results) {
                String trimmed = client.textRenderer.trimToWidth(line, boxW - pad * 2);
                context.drawTextWithShadow(client.textRenderer, trimmed, x + pad, drawY, theme.getTextSecondary());
                drawY += 12;
            }
        }
    }
}
