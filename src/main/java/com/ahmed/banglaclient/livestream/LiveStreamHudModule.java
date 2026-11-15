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
import java.util.function.Supplier;

/**
 * Full-featured LiveStream HUD & Multi-line Live Chat Module with YouTube Emojis,
 * Owner/Moderator/Member Role Badges, Bengali/World Language Shaping, 5 Animation Templates,
 * and History Scroll Indicator.
 */
public final class LiveStreamHudModule extends HudModule {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());
    private static final int[] PALETTE = {
            0xFFFFB84D, 0xFFFF6B8B, 0xFF5CE1E6, 0xFF9ECE6A,
            0xFFBB9AF7, 0xFFFF757F, 0xFF7DCFFF, 0xFFFFD166,
            0xFF73DACA, 0xFFFF9E64, 0xFF8AB4F8, 0xFFF472B6
    };

    private record WrappedChatLine(
            ChatMessage msg,
            String timestampPart,
            String authorPart,
            String contentPart,
            boolean isFirstLine,
            int msgIndex,
            int prefixWidth
    ) {}

    private final boolean isLiveChatBox;
    private final Supplier<String> metricSupplier;

    public LiveStreamHudModule(String id, String name, float defaultX, float defaultY, float defaultW, float defaultH,
                               boolean isLiveChatBox, Supplier<String> metricSupplier) {
        super(id, name, "LiveStream HUD overlay element (" + name + ").", ModuleCategory.HUD, defaultX, defaultY, defaultW, defaultH, false);
        this.isLiveChatBox = isLiveChatBox;
        this.metricSupplier = metricSupplier;
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        LiveStreamManager mgr = LiveStreamManager.get();
        int x = Math.round(getPosX());
        int y = Math.round(getPosY());

        if (!isLiveChatBox) {
            String text = metricSupplier != null ? metricSupplier.get() : getName();
            int pad = paddingSetting.getIntValue();
            int w = Math.max(58, client.textRenderer.getWidth(text) + pad * 2 + (getId().equals("ls_live_indicator") ? 10 : 0));
            int h = client.textRenderer.fontHeight + pad * 2;
            setWidth(w);
            setHeight(h);

            if (showBackgroundSetting.isEnabled()) {
                GlassRenderer.drawCustomGlassPanel(context, x, y, w, h, backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled());
            }

            int tx = x + pad;
            if (getId().equals("ls_live_indicator")) {
                boolean live = mgr.snapshot().state() == LiveStreamManager.ConnectionState.LIVE;
                context.fill(tx, y + (h - 5) / 2, tx + 5, y + (h - 5) / 2 + 5, live ? 0xFFEF4444 : 0xFF64748B);
                tx += 9;
            }
            context.drawTextWithShadow(client.textRenderer, text, tx, y + pad + 1, textColorSetting.getArgb());
            return;
        }

        // Full Live Chat Box
        mgr.tickChatPacing();
        int boxW = Math.max(200, Math.round(getWidth()));
        int boxH = Math.max(105, Math.round(getHeight()));
        setWidth(boxW);
        setHeight(boxH);

        if (showBackgroundSetting.isEnabled() || preview) {
            GlassRenderer.drawCustomGlassPanel(context, x, y, boxW, boxH, backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled());
        }

        List<ChatMessage> snapshot = new ArrayList<>(mgr.chatMessages());
        if (snapshot.isEmpty()) {
            if (preview) {
                EmojiRenderer.drawYouTubeBadge(context, x + 6, y + boxH - 15, 255);
                EmojiRenderer.drawRichText(context, client.textRenderer, "@Viewer  Live chat waiting for messages... ❤ 🔥", x + 20, y + boxH - 14, 0xFFFFFFFF, false, true);
            }
            return;
        }

        TextRenderer tr = client.textRenderer;
        int availWidth = Math.max(80, boxW - 14);
        List<WrappedChatLine> allLines = new ArrayList<>(snapshot.size() * 2);

        for (int mIdx = 0; mIdx < snapshot.size(); mIdx++) {
            ChatMessage msg = snapshot.get(mIdx);
            String ts = mgr.showTimestamp ? (TIME_FMT.format(Instant.ofEpochMilli(msg.timestampMs())) + " ") : "";
            String author = mgr.showUsername ? formatAuthor(msg.author()) : "";
            int badgeW = (mgr.showYouTubeIcon ? 14 : 0) + ((msg.owner() || msg.moderator() || msg.member()) ? 10 : 0);
            int prefixW = badgeW + tr.getWidth(ts + author);
            int lineAvail = Math.max(40, availWidth - prefixW);

            List<String> wrapped = wrapTextToWidth(tr, msg.content(), lineAvail);
            for (int l = 0; l < wrapped.size(); l++) {
                allLines.add(new WrappedChatLine(msg, l == 0 ? ts : "", l == 0 ? author : "", wrapped.get(l), l == 0, mIdx, prefixW));
            }
        }

        if (allLines.isEmpty()) return;

        int stepY = 12;
        int maxVisibleRows = Math.max(1, (boxH - 10) / stepY);
        int scrollOffset = Math.min(mgr.getScrollLineOffset(), Math.max(0, allLines.size() - maxVisibleRows));

        long nowNs = System.nanoTime();
        float listGlideAge = (nowNs - mgr.getLastReleaseNanoTime()) / 1_000_000_000.0f;
        float listGlideProgress = Easing.EASE_OUT_CUBIC.apply(Math.min(1.0f, listGlideAge / 0.28f));
        float listVerticalSlide = (scrollOffset == 0) ? ((1.0f - listGlideProgress) * 11.0f) : 0.0f;

        ChatAnimationTemplate template = mgr.getAnimationTemplate();

        ClipRenderer.renderClipped(context, x + 2, y + 2, boxW - 4, boxH - 4, () -> {
            int startIdx = allLines.size() - 1 - scrollOffset;
            float drawY = mgr.newestAtTop
                    ? (y + 4 + (scrollOffset > 0 ? 12 : 0) - listVerticalSlide)
                    : (y + boxH - 13 - (scrollOffset > 0 ? 12 : 0) + listVerticalSlide);

            for (int i = startIdx; i >= 0; i--) {
                if (!mgr.newestAtTop && drawY < y + 2) break;
                if (mgr.newestAtTop && drawY > y + boxH - 12) break;

                WrappedChatLine line = allLines.get(i);
                ChatMessage msg = line.msg();
                float ageSec = (nowNs - msg.releasedNanoTime()) / 1_000_000_000.0f;
                float anim = (scrollOffset == 0 && ageSec < 0.34f)
                        ? Easing.EASE_OUT_CUBIC.apply(Math.min(1.0f, ageSec / 0.34f))
                        : 1.0f;
                int alpha255 = Math.max(25, Math.min(255, Math.round(255 * anim)));

                float offsetX = 0.0f, offsetY = 0.0f, scale = 1.0f;
                switch (template) {
                    case SLIDE_UP -> offsetY = (1.0f - anim) * 10.0f;
                    case SLIDE_DOWN -> offsetY = -(1.0f - anim) * 10.0f;
                    case SLIDE_LEFT -> offsetX = -(1.0f - anim) * 18.0f;
                    case SCALE_POP -> scale = 0.80f + 0.20f * anim;
                    case SMOOTH_FADE -> offsetY = (1.0f - anim) * 3.0f;
                }

                Matrix3x2fStack matrices = context.getMatrices();
                matrices.pushMatrix();
                matrices.translate(x + 6 + offsetX, drawY + offsetY);
                if (Math.abs(scale - 1.0f) > 0.001f) {
                    matrices.scale(scale, scale);
                }

                if (mgr.chatShadow) {
                    context.fill(-2, -1, boxW - 12, 10, ColorUtils.multiplyAlpha(0x77000000, anim));
                }

                float cursorX = 0.0f;
                if (line.isFirstLine()) {
                    if (mgr.showYouTubeIcon) {
                        cursorX += EmojiRenderer.drawYouTubeBadge(context, cursorX, 0, alpha255);
                    }
                    if (msg.owner() || msg.moderator() || msg.member()) {
                        cursorX += EmojiRenderer.drawRoleBadge(context, cursorX, 0, msg.owner(), msg.moderator(), msg.member(), alpha255);
                    }
                    if (!line.timestampPart().isEmpty()) {
                        context.drawText(tr, line.timestampPart(), Math.round(cursorX), 0, EmojiRenderer.withAlpha(0xFF94A3B8, alpha255), mgr.textShadow);
                        cursorX += tr.getWidth(line.timestampPart());
                    }
                    if (!line.authorPart().isEmpty()) {
                        int aCol = resolveAuthorColor(mgr, msg, line.msgIndex());
                        context.drawText(tr, line.authorPart(), Math.round(cursorX), 0, EmojiRenderer.withAlpha(aCol, alpha255), mgr.textShadow);
                    }
                }

                int mCol = EmojiRenderer.withAlpha(resolveMessageColor(mgr, msg, line.msgIndex()), alpha255);
                EmojiRenderer.drawRichText(context, tr, line.contentPart(), line.prefixWidth(), 0, mCol, false, mgr.textShadow);

                matrices.popMatrix();
                drawY += mgr.newestAtTop ? stepY : -stepY;
            }

            if (scrollOffset > 0) {
                int barY = mgr.newestAtTop ? (y + 2) : (y + boxH - 13);
                context.fill(x + 4, barY, x + boxW - 4, barY + 11, 0xDD08090D);
                String histMsg = "▲ History (-" + scrollOffset + ") | Hold Down 2s for Live";
                context.drawTextWithShadow(tr, histMsg, x + 8, barY + 2, 0xFF7DCFFF);
            }
        });
    }

    private static String formatAuthor(String author) {
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
            return PALETTE[Math.abs(msg.author().hashCode()) % PALETTE.length];
        }
        return 0xFFE2E8F0;
    }

    private static int resolveMessageColor(LiveStreamManager mgr, ChatMessage msg, int idx) {
        if (mgr.rgbMode) {
            float hue = ((System.currentTimeMillis() % 3500L) / 3500.0f + idx * 0.07f + 0.18f) % 1.0f;
            return 0xFF000000 | (Color.HSBtoRGB(hue, 0.55f, 1.0f) & 0x00FFFFFF);
        }
        if (mgr.coloredMessages) {
            return PALETTE[Math.abs(msg.id().hashCode() + idx * 31) % PALETTE.length];
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
