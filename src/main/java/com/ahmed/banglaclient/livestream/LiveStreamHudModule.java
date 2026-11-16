package com.ahmed.banglaclient.livestream;

import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.livestream.LiveStreamManager.ChatAnimationTemplate;
import com.ahmed.banglaclient.livestream.LiveStreamManager.ChatMessage;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.rendering.ClipRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;

import java.awt.Color;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class LiveStreamHudModule extends HudModule {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());
    private static final int[] VIBRANT_PALETTE = {
            0xFFFFB84D, 0xFFFF6B8B, 0xFF5CE1E6, 0xFF9ECE6A,
            0xFFBB9AF7, 0xFFFF757F, 0xFF7DCFFF, 0xFFFFD166,
            0xFF73DACA, 0xFFFF9E64, 0xFF8AB4F8, 0xFFF472B6
    };

    private record DisplayLine(
            ChatMessage msg,
            String timestampPart,
            String authorPart,
            String contentPart,
            boolean isFirstLine,
            int msgIndex,
            int prefixWidth,
            int contentWidth
    ) {}

    private final boolean isLiveChatBox;
    public boolean isLiveChatBox() { return isLiveChatBox; }
    private final Supplier<String> metricSupplier;
    private final Map<String, List<String>> wrapCache = new ConcurrentHashMap<>(512);
    private List<DisplayLine> cachedLines = List.of();
    private int cachedTotalReceived = -1;
    private int cachedAvailWidth = -1;
    private boolean cachedShowTs = false;
    private boolean cachedShowUser = true;
    private boolean cachedShowYt = true;

    public LiveStreamHudModule(String id, String name, float defaultX, float defaultY, float defaultW, float defaultH,
                               boolean isLiveChatBox, Supplier<String> metricSupplier) {
        super(id, name, "LiveStream HUD overlay element (" + name + ").", ModuleCategory.HUD, defaultX, defaultY, defaultW, defaultH, false);
        this.isLiveChatBox = isLiveChatBox;
        this.metricSupplier = metricSupplier;
        if (isLiveChatBox) {
            this.showBackgroundSetting.setValue(false);
        }
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        LiveStreamManager mgr = LiveStreamManager.get();
        int x = Math.round(getPosX());
        int y = Math.round(getPosY());

        if (!isLiveChatBox) {
            String text = metricSupplier != null ? metricSupplier.get() : getName();
            int pad = paddingSetting.getIntValue();
            boolean isLiveInd = getId().equals("ls_live_indicator");
            int w = Math.max(58, client.textRenderer.getWidth(text) + pad * 2 + (isLiveInd ? 11 : 0));
            int h = client.textRenderer.fontHeight + pad * 2;
            setWidth(w);
            setHeight(h);

            if (showBackgroundSetting.isEnabled()) {
                GlassRenderer.drawCustomGlassPanel(context, x, y, w, h, backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled());
            }

            int tx = x + pad;
            if (isLiveInd) {
                boolean isLive = mgr.snapshot().state() == LiveStreamManager.ConnectionState.LIVE;
                float pulse = isLive ? (float) (0.65 + 0.35 * Math.sin(System.currentTimeMillis() / 260.0)) : 0.4f;
                int dotColor = isLive ? EmojiRenderer.withAlpha(0xFFFC8181, (int) (255 * pulse)) : 0xFF64748B;
                context.fill(tx, y + (h - 6) / 2, tx + 6, y + (h - 6) / 2 + 6, dotColor);
                tx += 10;
            }
            context.drawTextWithShadow(client.textRenderer, text, tx, y + pad + 1, textColorSetting.getArgb());
            renderBoundingBoxIfEnabled(context, x, y, w, h);
            return;
        }

        // Full LiveChatElement rendering with wrapCache, Chat Text Scale, Role Badges, Bengali Font & Dev Telemetry
        mgr.tickChatPacing();
        int boxW = Math.max(200, Math.round(getWidth()));
        int boxH = Math.max(110, Math.round(getHeight()));
        setWidth(boxW);
        setHeight(boxH);

        if (mgr.chatBoxFrame || showBackgroundSetting.isEnabled() || preview) {
            GlassRenderer.drawCustomGlassPanel(context, x, y, boxW, boxH, backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled());
        }

        float textScale = mgr.getChatTextScale();
        TextRenderer tr = client.textRenderer;
        var queue = mgr.chatMessages();

        ClipRenderer.renderClipped(context, x + 2, y + 2, boxW - 4, boxH - 4, () -> {
            if (queue.isEmpty()) {
                if (true) {
                    EmojiRenderer.drawYouTubeBadge(context, x + 8, y + 8, 255);
                    EmojiRenderer.drawRichText(context, tr, "Live chat waiting for messages...", x + 24, y + 8, 0xFF94A3B8, false, mgr.textShadow);
                }
                return;
            }

            int availWidth = Math.max(80, Math.round((boxW - 14.0f) / Math.max(0.5f, textScale)));
            int currentTotal = mgr.getTotalChatReceived();

            if (currentTotal != cachedTotalReceived
                    || availWidth != cachedAvailWidth
                    || mgr.showTimestamp != cachedShowTs
                    || mgr.showUsername != cachedShowUser
                    || mgr.showYouTubeIcon != cachedShowYt) {

                if (availWidth != cachedAvailWidth) wrapCache.clear();
                cachedTotalReceived = currentTotal;
                cachedAvailWidth = availWidth;
                cachedShowTs = mgr.showTimestamp;
                cachedShowUser = mgr.showUsername;
                cachedShowYt = mgr.showYouTubeIcon;

                List<ChatMessage> snap = new ArrayList<>(queue);
                List<DisplayLine> built = new ArrayList<>(snap.size() * 2);

                for (int mIdx = 0; mIdx < snap.size(); mIdx++) {
                    ChatMessage msg = snap.get(mIdx);
                    String ts = mgr.showTimestamp ? (TIME_FMT.format(Instant.ofEpochMilli(msg.timestampMs())) + " ") : "";
                    String author = mgr.showUsername ? formatAuthorColumn(msg.author()) : "";

                    int badgeW = (mgr.showYouTubeIcon ? 14 : 0) + ((msg.owner() || msg.moderator() || msg.member()) ? 10 : 0);
                    int prefixW = badgeW + tr.getWidth(ts + author);
                    int lineAvail = Math.max(40, availWidth - prefixW);

                    List<String> wrapped = wrapCache.computeIfAbsent(msg.id(), id -> wrapTextToWidth(tr, msg.content(), lineAvail));
                    if (wrapCache.size() > 600) wrapCache.clear();

                    for (int l = 0; l < wrapped.size(); l++) {
                        String part = wrapped.get(l);
                        int cw = EmojiRenderer.measureRichTextWidth(tr, part, false);
                        built.add(new DisplayLine(msg, l == 0 ? ts : "", l == 0 ? author : "", part, l == 0, mIdx, prefixW, cw));
                    }
                }
                cachedLines = built;
            }

            List<DisplayLine> allWrappedLines = cachedLines;
            int totalLines = allWrappedLines.size();
            if (totalLines == 0) return;

            float lineSpacing = 12.5f * textScale;
            int maxLinesInBox = Math.max(1, (int) ((boxH - 12.0f) / lineSpacing));
            int maxScroll = Math.max(0, totalLines - maxLinesInBox);
            int scrollOffset = Math.min(mgr.getScrollLineOffset(), maxScroll);

            long nowNs = System.nanoTime();
            float listGlideAge = (nowNs - mgr.getLastReleaseNanoTime()) / 1_000_000_000.0f;
            float listGlide = (scrollOffset == 0) ? ((1.0f - Easing.EASE_OUT_CUBIC.apply(Math.min(1.0f, listGlideAge / 0.26f))) * lineSpacing) : 0.0f;

            int bottomLineIdx = totalLines - 1 - scrollOffset;
            float drawY = mgr.newestAtTop
                    ? (y + 4.0f + (scrollOffset > 0 ? 12.0f : 0.0f) - listGlide)
                    : (y + boxH - 13.0f - (scrollOffset > 0 ? 12.0f : 0.0f) + listGlide);

            ChatAnimationTemplate template = mgr.getAnimationTemplate();

            for (int i = bottomLineIdx; i >= 0; i--) {
                if (!mgr.newestAtTop && drawY < y + 2) break;
                if (mgr.newestAtTop && drawY > y + boxH - 12) break;

                DisplayLine line = allWrappedLines.get(i);
                ChatMessage msg = line.msg();
                float ageSec = (nowNs - msg.releasedNanoTime()) / 1_000_000_000.0f;
                float anim = (scrollOffset == 0 && ageSec < 0.30f)
                        ? Easing.EASE_OUT_CUBIC.apply(Math.min(1.0f, ageSec / 0.30f))
                        : 1.0f;
                int alpha = Math.max(25, Math.min(255, Math.round(255 * anim)));

                float slideX = 0.0f, slideY = 0.0f, popScale = 1.0f;
                switch (template) {
                    case SLIDE_UP -> slideY = (1.0f - anim) * 8.0f;
                    case SLIDE_DOWN -> slideY = -(1.0f - anim) * 8.0f;
                    case SLIDE_LEFT -> slideX = -(1.0f - anim) * 14.0f;
                    case SCALE_POP -> popScale = 0.82f + 0.18f * anim;
                    case SMOOTH_FADE -> slideY = (1.0f - anim) * 3.0f;
                }

                Matrix3x2fStack matrices = context.getMatrices();
                matrices.pushMatrix();
                matrices.translate(x + 6 + slideX, drawY + slideY);
                float totalScale = textScale * popScale;
                if (Math.abs(totalScale - 1.0f) > 0.001f) {
                    matrices.scale(totalScale, totalScale);
                }

                int prefixW = line.prefixWidth();
                if (mgr.chatShadow) {
                    int bgAlpha = Math.round(100 * (alpha / 255.0f));
                    int lineStartX = line.isFirstLine() ? -2 : (prefixW - 2);
                    int lineEndX = prefixW + line.contentWidth() + 4;
                    context.fill(lineStartX, -1, lineEndX, 10, (bgAlpha << 24));
                }

                float cursorX = 0.0f;
                if (line.isFirstLine()) {
                    if (mgr.showYouTubeIcon) {
                        cursorX += EmojiRenderer.drawYouTubeBadge(context, cursorX, 0, alpha);
                    }
                    if (msg.owner() || msg.moderator() || msg.member()) {
                        cursorX += EmojiRenderer.drawRoleBadge(context, cursorX, 0, msg.owner(), msg.moderator(), msg.member(), alpha);
                    }
                    if (!line.timestampPart().isEmpty()) {
                        context.drawText(tr, line.timestampPart(), Math.round(cursorX), 0, EmojiRenderer.withAlpha(0xFF8A909E, alpha), mgr.textShadow);
                        cursorX += tr.getWidth(line.timestampPart());
                    }
                    if (!line.authorPart().isEmpty()) {
                        int aCol = resolveAuthorColor(mgr, msg, line.msgIndex());
                        context.drawText(tr, line.authorPart(), Math.round(cursorX), 0, EmojiRenderer.withAlpha(aCol, alpha), mgr.textShadow);
                    }
                }

                int mCol = EmojiRenderer.withAlpha(resolveMessageColor(mgr, msg, line.msgIndex()), alpha);
                EmojiRenderer.drawRichText(context, tr, line.contentPart(), prefixW, 0, mCol, false, mgr.textShadow);

                matrices.popMatrix();
                drawY += mgr.newestAtTop ? lineSpacing : -lineSpacing;
            }

            if (scrollOffset > 0) {
                int barY = mgr.newestAtTop ? (y + 2) : (y + boxH - 13);
                context.fill(x + 4, barY, x + boxW - 4, barY + 11, 0xCC141822);
                String histMsg = "▲ History (-" + scrollOffset + ") | Hold Down 2s for Live";
                context.drawTextWithShadow(tr, histMsg, x + 8, barY + 2, 0xFF7DCFFF);
            }
        });

        renderBoundingBoxIfEnabled(context, x, y, boxW, boxH);

        if (mgr.devTelemetryEnabled) {
            int dx = context.getScaledWindowWidth() - 162;
            int dy = 10;
            GlassRenderer.drawGlassPanel(context, dx, dy, 152, 64, true, false, true);
            var snap = mgr.snapshot();
            GlassRenderer.drawSmallCapsText(context, tr, "DEV TELEMETRY", dx + 8, dy + 6, 0xFFFFFFFF);
            context.drawTextWithShadow(tr, "FPS: " + client.getCurrentFps(), dx + 8, dy + 19, 0xFF94A3B8);
            context.drawTextWithShadow(tr, "State: " + snap.state().name(), dx + 8, dy + 31, 0xFF94A3B8);
            context.drawTextWithShadow(tr, "Chat Queue: " + queue.size() + "/" + mgr.getMaxChatHistory(), dx + 8, dy + 43, 0xFF94A3B8);
        }
    }

    private void renderBoundingBoxIfEnabled(DrawContext context, int x, int y, int w, int h) {
        if (LiveStreamManager.get().hudBoundingBoxesEnabled) {
            GlassRenderer.drawOutlineBorder(context, x, y, w, h, 0xAA63B3ED);
            context.drawTextWithShadow(client.textRenderer, getId() + " [" + x + "," + y + "]", x + 2, Math.max(2, y - 9), 0xFF63B3ED);
        }
    }

    private static String formatAuthorColumn(String author) {
        String a = author.startsWith("@") ? author : ("@" + author);
        if (a.length() > 15) a = a.substring(0, 12) + "...";
        return a + "  ";
    }

    private static int resolveAuthorColor(LiveStreamManager mgr, ChatMessage msg, int idx) {
        if (mgr.rgbMode) {
            float hue = ((System.currentTimeMillis() % 3500L) / 3500.0f + idx * 0.07f) % 1.0f;
            return 0xFF000000 | (Color.HSBtoRGB(hue, 0.75f, 1.0f) & 0x00FFFFFF);
        }
        if (msg.owner()) return 0xFFFFD866;
        if (msg.moderator()) return 0xFF5E97F6;
        if (msg.member()) return 0xFF4ADE80;
        if (mgr.coloredUsernames) {
            return VIBRANT_PALETTE[Math.abs(msg.author().hashCode()) % VIBRANT_PALETTE.length];
        }
        return 0xFFD8DEE9;
    }

    private static int resolveMessageColor(LiveStreamManager mgr, ChatMessage msg, int idx) {
        if (mgr.rgbMode) {
            float hue = ((System.currentTimeMillis() % 3500L) / 3500.0f + idx * 0.07f + 0.18f) % 1.0f;
            return 0xFF000000 | (Color.HSBtoRGB(hue, 0.55f, 1.0f) & 0x00FFFFFF);
        }
        if (mgr.coloredMessages) {
            return VIBRANT_PALETTE[Math.abs(msg.id().hashCode() + idx * 31) % VIBRANT_PALETTE.length];
        }
        return 0xFFFFFFFF;
    }

    private static List<String> wrapTextToWidth(TextRenderer tr, String text, int maxW) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }
        String remaining = text.trim();
        while (!remaining.isEmpty()) {
            if (EmojiRenderer.measureRichTextWidth(tr, remaining, false) <= maxW) {
                lines.add(remaining);
                break;
            }
            int cut = findWrapCutIndex(tr, remaining, maxW);
            String part = remaining.substring(0, cut).trim();
            if (part.isEmpty()) {
                int step = Character.charCount(remaining.codePointAt(0));
                lines.add(remaining.substring(0, step));
                remaining = remaining.substring(step).trim();
            } else {
                lines.add(part);
                remaining = remaining.substring(cut).trim();
            }
        }
        if (lines.isEmpty()) lines.add("");
        return lines;
    }

    private static int findWrapCutIndex(TextRenderer tr, String str, int maxW) {
        int lastSpace = -1;
        int idx = 0;
        while (idx < str.length()) {
            int cp = str.codePointAt(idx);
            int nextIdx = idx + Character.charCount(cp);
            if (cp == ' ') lastSpace = idx;
            int w = EmojiRenderer.measureRichTextWidth(tr, str.substring(0, nextIdx), false);
            if (w > maxW) {
                if (lastSpace > idx / 3) return lastSpace;
                return Math.max(Character.charCount(str.codePointAt(0)), idx);
            }
            idx = nextIdx;
        }
        return str.length();
    }
}
