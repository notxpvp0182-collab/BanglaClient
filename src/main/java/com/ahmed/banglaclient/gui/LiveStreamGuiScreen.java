package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.gui.components.SmoothScrollContainer;
import com.ahmed.banglaclient.livestream.LiveStreamHudModule;
import com.ahmed.banglaclient.livestream.LiveStreamManager;
import com.ahmed.banglaclient.module.ModuleManager;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.ClipRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

/**
 * BANGLA CLIENT Built-in LiveStream GUI styled in 2D AMOLED Aura Dark & White:
 * - Tabs: [Stream Connect] [HUD Widgets] [Channel Info] [Live Chat & Animations]
 * - Chat Delay slider from 0.0s to 5.0s + 5 Chat Animation Templates
 * - Direct button to open BANGLA CLIENT's HUD Editor when widgets are turned ON!
 */
public final class LiveStreamGuiScreen extends Screen {
    private enum Tab {
        STREAM("Stream"),
        HUD("HUD"),
        CHANNEL("Channel"),
        CHAT("Chat");

        private final String label;
        Tab(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final Screen parentScreen;
    private final Animation openAnim = new Animation(0.0f, 170L, Easing.EASE_OUT_CUBIC);
    private final SmoothScrollContainer scroll = new SmoothScrollContainer();
    private boolean closing = false;

    private Tab activeTab = Tab.STREAM;
    private String urlInput = "";
    private boolean urlFocused = false;
    private boolean draggingDelaySlider = false;

    private int winX, winY, winW, winH;

    public LiveStreamGuiScreen(Screen parentScreen) {
        super(Text.literal("LiveStream"));
        this.parentScreen = parentScreen;
        this.urlInput = LiveStreamManager.get().getStreamUrl();
    }

    @Override
    protected void init() {
        this.winW = Math.min(410, Math.max(260, (int) (this.width * 0.72f)));
        this.winH = Math.min(235, Math.max(170, (int) (this.height * 0.72f)));
        this.winX = (this.width - this.winW) / 2;
        this.winY = (this.height - this.winH) / 2;
        this.closing = false;
        this.openAnim.snapTo(0.0f);
        this.openAnim.animateTo(1.0f);
    }

    private LiveStreamHudModule getLsModule(String id) {
        ModuleManager mm = BanglaClient.getInstance().getModuleManager();
        return mm.getModuleById(id).filter(m -> m instanceof LiveStreamHudModule).map(m -> (LiveStreamHudModule) m).orElse(null);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float anim = openAnim.getValue();
        if (closing && anim <= 0.02f) {
            if (this.client != null) this.client.setScreen(parentScreen);
            return;
        }

        BlurRenderer.renderScreenBackdrop(context, this.width, this.height, anim);
        Theme theme = GlassRenderer.getTheme();

        Matrix3x2fStack matrices = context.getMatrices();
        float scale = 0.94f + 0.06f * anim;
        matrices.pushMatrix();
        matrices.translate(this.width * 0.5f, this.height * 0.5f);
        matrices.scale(scale, scale);
        matrices.translate(-this.width * 0.5f, -this.height * 0.5f);

        try {
            GlassRenderer.drawGlassPanel(context, winX, winY, winW, winH, false, false, true);

            // Top Header: [← Back] | [Stream] [HUD] [Channel] [Chat] | [Edit HUD]
            boolean backHov = MathUtils.isHovered(mouseX, mouseY, winX + 6, winY + 6, 44, 16);
            GlassRenderer.drawPillBadge(context, winX + 6, winY + 6, 44, 16, 0xFFFFFFFF, backHov);
            GlassRenderer.drawSmallCapsText(context, textRenderer, "← Back", winX + 11, winY + 10, 0xFFFFFFFF);

            int tabStartX = winX + 56;
            int tabW = 52;
            for (Tab t : Tab.values()) {
                boolean sel = (t == activeTab);
                boolean hov = MathUtils.isHovered(mouseX, mouseY, tabStartX, winY + 6, tabW, 16);
                GlassRenderer.drawPillBadge(context, tabStartX, winY + 6, tabW, 16, 0xFFFFFFFF, sel || hov);
                String lbl = GlassRenderer.toSmallCaps(t.getLabel());
                context.drawTextWithShadow(textRenderer, lbl, tabStartX + (tabW - textRenderer.getWidth(lbl)) / 2, winY + 10, 0xFFFFFFFF);
                tabStartX += tabW + 4;
            }

            int hudBtnW = 54;
            int hudBtnX = winX + winW - hudBtnW - 6;
            boolean hudHov = MathUtils.isHovered(mouseX, mouseY, hudBtnX, winY + 6, hudBtnW, 16);
            GlassRenderer.drawPillBadge(context, hudBtnX, winY + 6, hudBtnW, 16, 0xFFFFFFFF, hudHov || true);
            GlassRenderer.drawSmallCapsText(context, textRenderer, "Edit HUD", hudBtnX + 6, winY + 10, 0xFFFFFFFF);

            GlassRenderer.drawHorizontalDivider(context, winX + 6, winY + 26, winW - 12);

            int bodyX = winX + 8;
            int bodyY = winY + 32;
            int bodyW = winW - 16;
            int bodyH = winH - 38;

            switch (activeTab) {
                case STREAM -> renderStreamTab(context, bodyX, bodyY, bodyW, bodyH, mouseX, mouseY, theme);
                case HUD -> renderHudTab(context, bodyX, bodyY, bodyW, bodyH, mouseX, mouseY);
                case CHANNEL -> renderChannelTab(context, bodyX, bodyY, bodyW, bodyH, mouseX, mouseY);
                case CHAT -> renderChatTab(context, bodyX, bodyY, bodyW, bodyH, mouseX, mouseY);
            }
        } finally {
            matrices.popMatrix();
        }
    }

    private void renderStreamTab(DrawContext context, int x, int y, int w, int h, int mouseX, int mouseY, Theme theme) {
        LiveStreamManager mgr = LiveStreamManager.get();
        var snap = mgr.snapshot();

        GlassRenderer.drawSmallCapsText(context, textRenderer, "YouTube Live Stream URL:", x + 4, y + 4, 0xFFFFFFFF);

        // URL Input Box with Paste button
        int pasteW = 46;
        int boxW = w - pasteW - 12;
        boolean boxHov = MathUtils.isHovered(mouseX, mouseY, x + 4, y + 16, boxW, 20);
        GlassRenderer.drawGlassPanel(context, x + 4, y + 16, boxW, 20, true, boxHov, urlFocused);
        String dispUrl = urlInput.isEmpty() && !urlFocused
                ? "Paste YouTube Live link (watch?v= or /live/)..."
                : textRenderer.trimToWidth(urlInput, boxW - 14) + (urlFocused && (System.currentTimeMillis() / 500L) % 2L == 0L ? "|" : "");
        context.drawTextWithShadow(textRenderer, dispUrl, x + 10, y + 22, urlInput.isEmpty() && !urlFocused ? theme.getTextMuted() : 0xFFFFFFFF);

        int pasteX = x + 8 + boxW;
        boolean pHov = MathUtils.isHovered(mouseX, mouseY, pasteX, y + 16, pasteW, 20);
        GlassRenderer.drawPillBadge(context, pasteX, y + 16, pasteW, 20, 0xFFFFFFFF, pHov || true);
        GlassRenderer.drawSmallCapsText(context, textRenderer, "Paste", pasteX + 10, y + 22, 0xFFFFFFFF);

        // [Connect] [Disconnect] [Refresh]
        int btnY = y + 42;
        int btnW = (w - 16) / 3;
        String[] actions = {"Connect", "Disconnect", "Refresh"};
        for (int i = 0; i < 3; i++) {
            int bx = x + 4 + i * (btnW + 4);
            boolean hov = MathUtils.isHovered(mouseX, mouseY, bx, btnY, btnW, 18);
            int col = (i == 1) ? ThemeTokens.STATUS_ERROR : 0xFFFFFFFF;
            GlassRenderer.drawPillBadge(context, bx, btnY, btnW, 18, col, hov || i == 0);
            String lbl = GlassRenderer.toSmallCaps(actions[i]);
            context.drawTextWithShadow(textRenderer, lbl, bx + (btnW - textRenderer.getWidth(lbl)) / 2, btnY + 5, 0xFFFFFFFF);
        }

        // Status & Live Telemetry Cards
        GlassRenderer.drawSmallCapsText(context, textRenderer, "Status: " + snap.state().name() + " (" + snap.statusMessage() + ")", x + 4, y + 66, 0xFFE2E8F0);

        int cardW = (w - 12) / 2;
        int cardH = 34;
        drawMetricCard(context, x + 4, y + 80, cardW, cardH, "Channel", snap.channelName());
        drawMetricCard(context, x + 8 + cardW, y + 80, cardW, cardH, "Subscribers", LiveStreamManager.formatCompactNumber(snap.subscriberCount()));
        drawMetricCard(context, x + 4, y + 118, cardW, cardH, "Live Viewers", LiveStreamManager.formatCompactNumber(snap.viewerCount()));
        drawMetricCard(context, x + 8 + cardW, y + 118, cardW, cardH, "Likes", LiveStreamManager.formatCompactNumber(snap.likeCount()));
    }

    private void drawMetricCard(DrawContext context, int x, int y, int w, int h, String title, String value) {
        GlassRenderer.drawGlassPanel(context, x, y, w, h, true, false, false);
        GlassRenderer.drawSmallCapsText(context, textRenderer, title, x + 8, y + 6, 0xFF94A3B8);
        context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(value != null ? value : "--", w - 14), x + 8, y + 19, 0xFFFFFFFF);
    }

    private void renderHudTab(DrawContext context, int x, int y, int w, int h, int mouseX, int mouseY) {
        String[] ids = {"ls_live_indicator", "ls_viewer_count", "ls_like_count", "ls_stream_duration", "ls_stream_title", "ls_connection_status"};
        int rowY = y + 4;
        for (String id : ids) {
            LiveStreamHudModule m = getLsModule(id);
            if (m == null) continue;
            renderToggleRow(context, x + 4, rowY, w - 8, 22, m.getName(), m.isEnabled(), mouseX, mouseY);
            rowY += 25;
        }
    }

    private void renderChannelTab(DrawContext context, int x, int y, int w, int h, int mouseX, int mouseY) {
        String[] ids = {"ls_channel_name", "ls_channel_handle", "ls_subscriber_count"};
        int rowY = y + 4;
        for (String id : ids) {
            LiveStreamHudModule m = getLsModule(id);
            if (m == null) continue;
            renderToggleRow(context, x + 4, rowY, w - 8, 22, m.getName(), m.isEnabled(), mouseX, mouseY);
            rowY += 25;
        }
        renderToggleRow(context, x + 4, rowY, w - 8, 22, "Auto Connect on Launch", LiveStreamManager.get().isAutoConnect(), mouseX, mouseY);
    }

    private void renderChatTab(DrawContext context, int x, int y, int w, int h, int mouseX, int mouseY) {
        LiveStreamManager mgr = LiveStreamManager.get();
        if (draggingDelaySlider) {
            float norm = MathUtils.clamp((float) (mouseX - (x + 12)) / (float) (w - 30), 0.0f, 1.0f);
            mgr.setChatDelayMs(Math.round(norm * 5000.0f));
        }

        scroll.tickAndInterpolate();
        scroll.updateDimensions(h, 290);

        ClipRenderer.renderClipped(context, x, y, w - 4, h, () -> {
            int ry = Math.round(y - scroll.getScrollOffset()) + 2;
            int rw = w - 10;

            LiveStreamHudModule chatMod = getLsModule("ls_live_chat");
            if (chatMod != null) {
                renderToggleRow(context, x + 2, ry, rw, 22, "Enable Live Chat Overlay (Shows in HUD Editor)", chatMod.isEnabled(), mouseX, mouseY);
                ry += 25;
            }

            // Chat Delay Slider (0.0s to 5.0s)
            GlassRenderer.drawGlassPanel(context, x + 2, ry, rw, 26, true, false, draggingDelaySlider);
            String delayStr = String.format(Locale.ROOT, "Chat Delay: %.1fs (Max 5.0s)", mgr.getChatDelayMs() / 1000.0f);
            GlassRenderer.drawSmallCapsText(context, textRenderer, delayStr, x + 10, ry + 4, 0xFFFFFFFF);
            int trackX = x + 10, trackW = rw - 20, trackY = ry + 17;
            context.fill(trackX, trackY, trackX + trackW, trackY + 3, 0x33FFFFFF);
            int fillW = Math.round((mgr.getChatDelayMs() / 5000.0f) * trackW);
            context.fill(trackX, trackY, trackX + fillW, trackY + 3, 0xFFFFFFFF);
            context.fill(trackX + fillW - 2, trackY - 2, trackX + fillW + 2, trackY + 5, 0xFFFFFFFF);
            ry += 29;

            // Chat Animation Template Selector
            boolean animHov = MathUtils.isHovered(mouseX, mouseY, x + 2, ry, rw, 22);
            GlassRenderer.drawGlassPanel(context, x + 2, ry, rw, 22, true, animHov, true);
            String animText = "Chat Animation Template: " + mgr.getAnimationTemplate().getLabel();
            GlassRenderer.drawSmallCapsText(context, textRenderer, animText, x + 10, ry + 7, 0xFFFFFFFF);
            ry += 25;

            renderToggleRow(context, x + 2, ry, rw, 22, "YouTube Icon Badge", mgr.showYouTubeIcon, mouseX, mouseY); ry += 25;
            renderToggleRow(context, x + 2, ry, rw, 22, "Colored Usernames", mgr.coloredUsernames, mouseX, mouseY); ry += 25;
            renderToggleRow(context, x + 2, ry, rw, 22, "Multi-Color Chat Text", mgr.coloredMessages, mouseX, mouseY); ry += 25;
            renderToggleRow(context, x + 2, ry, rw, 22, "RGB Chroma Mode", mgr.rgbMode, mouseX, mouseY); ry += 25;
            renderToggleRow(context, x + 2, ry, rw, 22, "Newest Chat at Top", mgr.newestAtTop, mouseX, mouseY); ry += 25;
            renderToggleRow(context, x + 2, ry, rw, 22, "Chat Line Background", mgr.chatShadow, mouseX, mouseY); ry += 25;
            renderToggleRow(context, x + 2, ry, rw, 22, "Show Timestamp", mgr.showTimestamp, mouseX, mouseY);
        });
        scroll.renderScrollbar(context, x + w - 3, y, 2, h, mouseX, mouseY);
    }

    private void renderToggleRow(DrawContext context, int x, int y, int w, int h, String label, boolean state, int mouseX, int mouseY) {
        boolean hov = MathUtils.isHovered(mouseX, mouseY, x, y, w, h);
        GlassRenderer.drawGlassPanel(context, x, y, w, h, true, hov, state);
        GlassRenderer.drawSmallCapsText(context, textRenderer, label, x + 8, y + (h - 8) / 2, state ? 0xFFFFFFFF : 0xFF94A3B8);

        int swW = 22, swH = 11;
        int swX = x + w - swW - 6;
        int swY = y + (h - swH) / 2;
        context.fill(swX + 1, swY, swX + swW - 1, swY + swH, state ? 0x4CFFFFFF : 0xFF07080B);
        GlassRenderer.drawOutlineBorder(context, swX, swY, swW, swH, state ? 0xEEFFFFFF : 0x38FFFFFF);
        int knobX = state ? (swX + swW - 8) : (swX + 2);
        context.fill(knobX, swY + 2, knobX + 6, swY + swH - 2, state ? 0xFFFFFFFF : 0xFF64748B);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x(), mouseY = click.y();
        if (click.button() != 0 || closing) return super.mouseClicked(click, doubled);

        if (MathUtils.isHovered(mouseX, mouseY, winX + 6, winY + 6, 44, 16)) {
            GlassRenderer.playClickSound();
            close();
            return true;
        }

        int tabStartX = winX + 56, tabW = 52;
        for (Tab t : Tab.values()) {
            if (MathUtils.isHovered(mouseX, mouseY, tabStartX, winY + 6, tabW, 16)) {
                GlassRenderer.playClickSound();
                activeTab = t;
                scroll.resetScroll();
                return true;
            }
            tabStartX += tabW + 4;
        }

        int hudBtnW = 54, hudBtnX = winX + winW - hudBtnW - 6;
        if (MathUtils.isHovered(mouseX, mouseY, hudBtnX, winY + 6, hudBtnW, 16)) {
            GlassRenderer.playClickSound();
            if (this.client != null) this.client.setScreen(new HudEditorScreen(this));
            return true;
        }

        int bodyX = winX + 8, bodyY = winY + 32, bodyW = winW - 16, bodyH = winH - 38;
        LiveStreamManager mgr = LiveStreamManager.get();

        if (activeTab == Tab.STREAM) {
            int pasteW = 46, boxW = bodyW - pasteW - 12;
            if (MathUtils.isHovered(mouseX, mouseY, bodyX + 4, bodyY + 16, boxW, 20)) {
                urlFocused = true;
                return true;
            } else {
                urlFocused = false;
            }
            if (MathUtils.isHovered(mouseX, mouseY, bodyX + 8 + boxW, bodyY + 16, pasteW, 20)) {
                GlassRenderer.playClickSound();
                if (this.client != null && this.client.keyboard != null) {
                    String clip = this.client.keyboard.getClipboard();
                    if (clip != null) urlInput = clip.trim();
                }
                return true;
            }
            int btnY = bodyY + 42, btnW = (bodyW - 16) / 3;
            if (MathUtils.isHovered(mouseX, mouseY, bodyX + 4, btnY, btnW, 18)) {
                GlassRenderer.playClickSound();
                mgr.connect(urlInput);
                return true;
            }
            if (MathUtils.isHovered(mouseX, mouseY, bodyX + 8 + btnW, btnY, btnW, 18)) {
                GlassRenderer.playClickSound();
                mgr.disconnect();
                return true;
            }
            if (MathUtils.isHovered(mouseX, mouseY, bodyX + 12 + btnW * 2, btnY, btnW, 18)) {
                GlassRenderer.playClickSound();
                mgr.refresh();
                return true;
            }
        } else if (activeTab == Tab.HUD) {
            String[] ids = {"ls_live_indicator", "ls_viewer_count", "ls_like_count", "ls_stream_duration", "ls_stream_title", "ls_connection_status"};
            int rowY = bodyY + 4;
            for (String id : ids) {
                LiveStreamHudModule m = getLsModule(id);
                if (m != null && MathUtils.isHovered(mouseX, mouseY, bodyX + 4, rowY, bodyW - 8, 22)) {
                    GlassRenderer.playClickSound();
                    m.toggle();
                    return true;
                }
                rowY += 25;
            }
        } else if (activeTab == Tab.CHANNEL) {
            String[] ids = {"ls_channel_name", "ls_channel_handle", "ls_subscriber_count"};
            int rowY = bodyY + 4;
            for (String id : ids) {
                LiveStreamHudModule m = getLsModule(id);
                if (m != null && MathUtils.isHovered(mouseX, mouseY, bodyX + 4, rowY, bodyW - 8, 22)) {
                    GlassRenderer.playClickSound();
                    m.toggle();
                    return true;
                }
                rowY += 25;
            }
            if (MathUtils.isHovered(mouseX, mouseY, bodyX + 4, rowY, bodyW - 8, 22)) {
                GlassRenderer.playClickSound();
                mgr.setAutoConnect(!mgr.isAutoConnect());
                return true;
            }
        } else if (activeTab == Tab.CHAT) {
            if (scroll.onMouseClicked(mouseX, mouseY, 0, bodyX + bodyW - 4, bodyY, 4, bodyH)) return true;
            int ry = Math.round(bodyY - scroll.getScrollOffset()) + 2;
            int rw = bodyW - 10;

            LiveStreamHudModule chatMod = getLsModule("ls_live_chat");
            if (chatMod != null) {
                if (MathUtils.isHovered(mouseX, mouseY, bodyX + 2, ry, rw, 22)) {
                    GlassRenderer.playClickSound();
                    chatMod.toggle();
                    return true;
                }
                ry += 25;
            }
            if (MathUtils.isHovered(mouseX, mouseY, bodyX + 2, ry, rw, 26)) {
                draggingDelaySlider = true;
                float norm = MathUtils.clamp((float) (mouseX - (bodyX + 12)) / (float) (bodyW - 30), 0.0f, 1.0f);
                mgr.setChatDelayMs(Math.round(norm * 5000.0f));
                return true;
            }
            ry += 29;
            if (MathUtils.isHovered(mouseX, mouseY, bodyX + 2, ry, rw, 22)) {
                GlassRenderer.playClickSound();
                mgr.setAnimationTemplate(mgr.getAnimationTemplate().next());
                return true;
            }
            ry += 25;
            if (MathUtils.isHovered(mouseX, mouseY, bodyX + 2, ry, rw, 22)) { mgr.showYouTubeIcon = !mgr.showYouTubeIcon; GlassRenderer.playClickSound(); return true; } ry += 25;
            if (MathUtils.isHovered(mouseX, mouseY, bodyX + 2, ry, rw, 22)) { mgr.coloredUsernames = !mgr.coloredUsernames; GlassRenderer.playClickSound(); return true; } ry += 25;
            if (MathUtils.isHovered(mouseX, mouseY, bodyX + 2, ry, rw, 22)) { mgr.coloredMessages = !mgr.coloredMessages; GlassRenderer.playClickSound(); return true; } ry += 25;
            if (MathUtils.isHovered(mouseX, mouseY, bodyX + 2, ry, rw, 22)) { mgr.rgbMode = !mgr.rgbMode; GlassRenderer.playClickSound(); return true; } ry += 25;
            if (MathUtils.isHovered(mouseX, mouseY, bodyX + 2, ry, rw, 22)) { mgr.newestAtTop = !mgr.newestAtTop; GlassRenderer.playClickSound(); return true; } ry += 25;
            if (MathUtils.isHovered(mouseX, mouseY, bodyX + 2, ry, rw, 22)) { mgr.chatShadow = !mgr.chatShadow; GlassRenderer.playClickSound(); return true; } ry += 25;
            if (MathUtils.isHovered(mouseX, mouseY, bodyX + 2, ry, rw, 22)) { mgr.showTimestamp = !mgr.showTimestamp; GlassRenderer.playClickSound(); return true; }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click c, double dx, double dy) {
        if (draggingDelaySlider) return true;
        return scroll.onMouseDragged(c.x(), c.y(), c.button(), winH - 38) || super.mouseDragged(c, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click c) {
        draggingDelaySlider = false;
        scroll.onMouseReleased(c.button());
        return super.mouseReleased(c);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (activeTab == Tab.CHAT) {
            scroll.onMouseScrolled(mouseX, mouseY, verticalAmount, winX + 8, winY + 32, winW - 16, winH - 38);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (urlFocused) {
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) { urlFocused = false; return true; }
            if (key == GLFW.GLFW_KEY_BACKSPACE && !urlInput.isEmpty()) {
                urlInput = urlInput.substring(0, urlInput.length() - 1);
                return true;
            }
            if (key == GLFW.GLFW_KEY_V && (input.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0 && this.client != null) {
                String clip = this.client.keyboard.getClipboard();
                if (clip != null) urlInput += clip.trim();
                return true;
            }
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        char chr = (char) input.codepoint();
        if (urlFocused && chr >= 32 && chr != 127) {
            urlInput += chr;
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public void close() {
        LiveStreamManager.get().setStreamUrl(urlInput);
        LiveStreamManager.get().saveConfig();
        if (!closing) {
            closing = true;
            openAnim.animateTo(0.0f);
        }
    }

    @Override
    public boolean shouldPause() { return false; }
}
