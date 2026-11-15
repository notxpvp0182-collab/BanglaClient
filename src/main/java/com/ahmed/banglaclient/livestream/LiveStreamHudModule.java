package com.ahmed.banglaclient.livestream;

import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.livestream.LiveStreamManager.ChatAnimationTemplate;
import com.ahmed.banglaclient.livestream.LiveStreamManager.ChatMessage;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.rendering.ClipRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.util.ColorUtils;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;

import java.awt.Color;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Bridges every LiveStream HUD element directly into BANGLA CLIENT's HudManager & HudEditorScreen!
 * All elements are DISABLED (false) by default, and appear in HudEditorScreen as soon as enabled!
 */
public final class LiveStreamHudModule extends HudModule {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());
    private static final int[] PALETTE = {
            0xFFFFB84D, 0xFFFF6B8B, 0xFF5CE1E6, 0xFF9ECE6A,
            0xFFBB9AF7, 0xFFFF757F, 0xFF7DCFFF, 0xFFFFD166
    };

    private final boolean isLiveChatBox;
    private final Supplier<String> metricSupplier;

    public LiveStreamHudModule(String id, String name, float defaultX, float defaultY, float defaultW, float defaultH,
                               boolean isLiveChatBox, Supplier<String> metricSupplier) {
        // Default enabled = FALSE so nothing shows on screen until turned ON in LiveStream GUI!
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

        // Live Chat HUD Box with 5 Selectable Animation Templates & Smooth Vertical Glide
        mgr.tickChatPacing();
        int boxW = Math.max(180, Math.round(getWidth()));
        int boxH = Math.max(95, Math.round(getHeight()));
        setWidth(boxW);
        setHeight(boxH);

        if (showBackgroundSetting.isEnabled() || preview) {
            GlassRenderer.drawCustomGlassPanel(context, x, y, boxW, boxH, backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled());
        }

        List<ChatMessage> snapshot = new ArrayList<>(mgr.chatMessages());
        if (snapshot.isEmpty()) {
            if (preview) {
                context.drawTextWithShadow(client.textRenderer, "▶ @Viewer: LiveStream Chat Preview!", x + 6, y + boxH - 14, 0xFFFFFFFF);
            }
            return;
        }

        long nowNs = System.nanoTime();
        float listGlideAge = (nowNs - mgr.getLastReleaseNanoTime()) / 1_000_000_000.0f;
        float listGlideProgress = Easing.EASE_OUT_CUBIC.apply(Math.min(1.0f, listGlideAge / 0.28f));
        float listVerticalSlide = (1.0f - listGlideProgress) * 11.0f;

        ChatAnimationTemplate template = mgr.getAnimationTemplate();

        ClipRenderer.renderClipped(context, x + 2, y + 2, boxW - 4, boxH - 4, () -> {
            int stepY = 12;
            int maxRows = Math.max(1, (boxH - 8) / stepY);
            int offset = Math.min(mgr.getScrollLineOffset(), Math.max(0, snapshot.size() - maxRows));
            int startIdx = snapshot.size() - 1 - offset;
            float drawY = mgr.newestAtTop ? (y + 5 - listVerticalSlide) : (y + boxH - 13 + listVerticalSlide);

            for (int i = startIdx; i >= 0; i--) {
                if (!mgr.newestAtTop && drawY < y + 2) break;
                if (mgr.newestAtTop && drawY > y + boxH - 12) break;

                ChatMessage msg = snapshot.get(i);
                float ageSec = (nowNs - msg.releasedNanoTime()) / 1_000_000_000.0f;
                float anim = Easing.EASE_OUT_CUBIC.apply(Math.min(1.0f, ageSec / 0.34f));

                float offsetX = 0.0f;
                float offsetY = 0.0f;
                float scale = 1.0f;

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

                int cx = 0;
                if (mgr.chatShadow) {
                    context.fill(-2, -1, boxW - 12, 10, ColorUtils.multiplyAlpha(0x77000000, anim));
                }
                if (mgr.showYouTubeIcon) {
                    context.fill(0, 1, 9, 8, ColorUtils.multiplyAlpha(0xFFFF0033, anim));
                    context.fill(3, 3, 6, 6, ColorUtils.multiplyAlpha(0xFFFFFFFF, anim));
                    cx += 12;
                }
                if (mgr.showTimestamp) {
                    String ts = TIME_FMT.format(Instant.ofEpochMilli(msg.timestampMs())) + " ";
                    context.drawText(client.textRenderer, ts, cx, 0, ColorUtils.multiplyAlpha(0xFF94A3B8, anim), mgr.textShadow);
                    cx += client.textRenderer.getWidth(ts);
                }
                if (mgr.showUsername) {
                    String author = "@" + (msg.author().length() > 13 ? msg.author().substring(0, 11) + ".." : msg.author()) + ": ";
                    int uCol = mgr.rgbMode
                            ? (0xFF000000 | (Color.HSBtoRGB(((System.currentTimeMillis() % 3000L) / 3000.0f), 0.7f, 1.0f) & 0x00FFFFFF))
                            : (mgr.coloredUsernames ? PALETTE[Math.abs(msg.author().hashCode()) % PALETTE.length] : 0xFFE2E8F0);
                    context.drawText(client.textRenderer, author, cx, 0, ColorUtils.multiplyAlpha(uCol, anim), mgr.textShadow);
                    cx += client.textRenderer.getWidth(author);
                }

                int mCol = mgr.coloredMessages ? PALETTE[Math.abs(msg.id().hashCode()) % PALETTE.length] : 0xFFFFFFFF;
                String body = client.textRenderer.trimToWidth(msg.content(), Math.max(30, boxW - cx - 12));
                context.drawText(client.textRenderer, body, cx, 0, ColorUtils.multiplyAlpha(mCol, anim), mgr.textShadow);

                matrices.popMatrix();
                drawY += mgr.newestAtTop ? stepY : -stepY;
            }
        });
    }
}
