package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.livestream.LiveStreamHudModule;
import com.ahmed.banglaclient.livestream.LiveStreamManager;
import com.ahmed.banglaclient.module.ModuleManager;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
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

public final class WorkspaceModalScreen extends Screen {
    private static final String[] PROFILES = {"Default", "Streaming", "Gaming", "Minimal"};
    private final Screen parent;
    private final Animation openAnim = new Animation(0.0f, 180L, Easing.EASE_OUT_QUART);
    private boolean closing = false;
    private String urlText = "";
    private boolean urlFocused = false;

    public WorkspaceModalScreen(Screen parent) {
        super(Text.literal("Stream Workspace"));
        this.parent = parent;
        this.urlText = LiveStreamManager.get().getStreamUrl();
    }

    @Override
    protected void init() {
        this.closing = false;
        this.openAnim.snapTo(0.0f);
        this.openAnim.animateTo(1.0f);
    }

    private void applyProfilePreset(String profileName) {
        LiveStreamManager.get().setActiveProfile(profileName);
        ModuleManager mm = BanglaClient.getInstance().getModuleManager();
        String[] allIds = {
                "ls_live_indicator", "ls_viewer_count", "ls_like_count", "ls_channel_name",
                "ls_channel_handle", "ls_subscriber_count", "ls_stream_duration",
                "ls_stream_title", "ls_connection_status", "ls_live_chat"
        };
        for (String id : allIds) {
            mm.getModuleById(id).filter(m -> m instanceof LiveStreamHudModule).ifPresent(m -> {
                switch (profileName) {
                    case "Streaming" -> m.setEnabled(true);
                    case "Minimal" -> m.setEnabled(id.equals("ls_live_indicator") || id.equals("ls_viewer_count") || id.equals("ls_live_chat"));
                    case "Gaming" -> m.setEnabled(id.equals("ls_live_indicator") || id.equals("ls_viewer_count"));
                    default -> m.setEnabled(!id.equals("ls_connection_status"));
                }
            });
        }
        BanglaClient.getInstance().getConfigManager().saveModulesConfig();
        LiveStreamManager.get().saveConfig();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float deltaTicks) {
        float progress = openAnim.getValue();
        if (closing && progress <= 0.02f) {
            if (this.client != null) this.client.setScreen(parent);
            return;
        }

        BlurRenderer.renderScreenBackdrop(ctx, this.width, this.height, progress);

        Matrix3x2fStack matrices = ctx.getMatrices();
        float scale = 0.94f + 0.06f * progress;
        matrices.pushMatrix();
        matrices.translate(this.width * 0.5f, this.height * 0.5f);
        matrices.scale(scale, scale);
        matrices.translate(-this.width * 0.5f, -this.height * 0.5f);

        try {
            int mw = Math.min(440, this.width - 24);
            int mh = Math.min(228, this.height - 20);
            int mx = (this.width - mw) / 2;
            int my = (this.height - mh) / 2;

            GlassRenderer.drawGlassPanel(ctx, mx, my, mw, mh, false, false, true);

            boolean backHov = MathUtils.isHovered(mouseX, mouseY, mx + 8, my + 7, 46, 16);
            GlassRenderer.drawPillBadge(ctx, mx + 8, my + 7, 46, 16, 0xFFFFFFFF, backHov);
            GlassRenderer.drawSmallCapsText(ctx, textRenderer, "← Back", mx + 14, my + 11, 0xFFFFFFFF);
            GlassRenderer.drawSmallCapsText(ctx, textRenderer, "Stream Workspace", mx + 62, my + 11, 0xFFFFFFFF);

            var snap = LiveStreamManager.get().snapshot();
            String stateText = snap.state().name();
            int badgeW = textRenderer.getWidth(stateText) + 14;
            GlassRenderer.drawPillBadge(ctx, mx + mw - badgeW - 8, my + 7, badgeW, 16, 0xFFFFFFFF, true);
            ctx.drawTextWithShadow(textRenderer, stateText, mx + mw - badgeW - 1, my + 11, 0xFFFFFFFF);

            GlassRenderer.drawHorizontalDivider(ctx, mx + 8, my + 27, mw - 16);

            // Left Sidebar: HUD Profiles
            int sideX = mx + 8, sideY = my + 32, sideW = 100, sideH = mh - 40;
            GlassRenderer.drawGlassPanel(ctx, sideX, sideY, sideW, sideH, true, false, false);
            GlassRenderer.drawSmallCapsText(ctx, textRenderer, "HUD Profiles", sideX + 8, sideY + 6, 0xFF94A3B8);
            for (int i = 0; i < PROFILES.length; i++) {
                int py = sideY + 20 + i * 22;
                boolean active = PROFILES[i].equals(LiveStreamManager.get().getActiveProfile());
                boolean hov = MathUtils.isHovered(mouseX, mouseY, sideX + 5, py, sideW - 10, 18);
                GlassRenderer.drawPillBadge(ctx, sideX + 5, py, sideW - 10, 18, 0xFFFFFFFF, active || hov);
                GlassRenderer.drawSmallCapsText(ctx, textRenderer, PROFILES[i], sideX + 12, py + 5, 0xFFFFFFFF);
            }

            // Right Main Workspace Area
            int mainX = sideX + sideW + 6, mainY = sideY, mainW = mw - sideW - 22;
            GlassRenderer.drawGlassPanel(ctx, mainX, mainY, mainW, sideH, true, false, false);

            GlassRenderer.drawSmallCapsText(ctx, textRenderer, "Stream URL", mainX + 8, mainY + 6, 0xFFFFFFFF);
            int pasteW = 44;
            int fieldW = mainW - pasteW - 20;
            GlassRenderer.drawGlassPanel(ctx, mainX + 8, mainY + 18, fieldW, 18, false, false, urlFocused);
            String disp = urlText.isEmpty() && !urlFocused
                    ? "Paste YouTube Live link..."
                    : textRenderer.trimToWidth(urlText, fieldW - 22) + (urlFocused && (System.currentTimeMillis() / 500L) % 2L == 0L ? "|" : "");
            ctx.drawTextWithShadow(textRenderer, disp, mainX + 12, mainY + 23, urlText.isEmpty() && !urlFocused ? 0xFF64748B : 0xFFFFFFFF);
            if (!urlText.isEmpty()) {
                ctx.drawTextWithShadow(textRenderer, "×", mainX + 8 + fieldW - 10, mainY + 23, 0xFF94A3B8);
            }

            int pasteX = mainX + 12 + fieldW;
            boolean pHov = MathUtils.isHovered(mouseX, mouseY, pasteX, mainY + 18, pasteW, 18);
            GlassRenderer.drawPillBadge(ctx, pasteX, mainY + 18, pasteW, 18, 0xFFFFFFFF, pHov || true);
            GlassRenderer.drawSmallCapsText(ctx, textRenderer, "Paste", pasteX + 8, mainY + 23, 0xFFFFFFFF);

            int btnY = mainY + 41;
            int bw = (mainW - 24) / 3;
            String[] bLabels = {"Connect", "Disconnect", "Refresh"};
            for (int i = 0; i < 3; i++) {
                int bx = mainX + 8 + i * (bw + 4);
                boolean hov = MathUtils.isHovered(mouseX, mouseY, bx, btnY, bw, 17);
                GlassRenderer.drawPillBadge(ctx, bx, btnY, bw, 17, i == 1 ? ThemeTokens.STATUS_ERROR : 0xFFFFFFFF, hov || i == 0);
                String bl = GlassRenderer.toSmallCaps(bLabels[i]);
                ctx.drawTextWithShadow(textRenderer, bl, bx + (bw - textRenderer.getWidth(bl)) / 2, btnY + 4, 0xFFFFFFFF);
            }

            GlassRenderer.drawSmallCapsText(ctx, textRenderer, textRenderer.trimToWidth(snap.statusMessage(), mainW - 16), mainX + 8, mainY + 63, 0xFF94A3B8);

            int cardW = (mainW - 20) / 2;
            int cardH = 30;
            drawTelemetryCard(ctx, mainX + 8, mainY + 76, cardW, cardH, "Channel", snap.channelName());
            drawTelemetryCard(ctx, mainX + 12 + cardW, mainY + 76, cardW, cardH, "Subscribers", LiveStreamManager.formatCompactNumber(snap.subscriberCount()));
            drawTelemetryCard(ctx, mainX + 8, mainY + 110, cardW, cardH, "Live Viewers", LiveStreamManager.formatCompactNumber(snap.viewerCount()));
            drawTelemetryCard(ctx, mainX + 12 + cardW, mainY + 110, cardW, cardH, "Likes", LiveStreamManager.formatCompactNumber(snap.likeCount()));
        } finally {
            matrices.popMatrix();
        }
    }

    private void drawTelemetryCard(DrawContext ctx, int x, int y, int w, int h, String label, String val) {
        GlassRenderer.drawGlassPanel(ctx, x, y, w, h, false, false, false);
        GlassRenderer.drawSmallCapsText(ctx, textRenderer, label, x + 6, y + 4, 0xFF94A3B8);
        ctx.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(val != null ? val : "--", w - 12), x + 6, y + 16, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double cx = click.x(), cy = click.y();
        if (click.button() != 0 || closing) return super.mouseClicked(click, doubled);

        int mw = Math.min(440, this.width - 24);
        int mh = Math.min(228, this.height - 20);
        int mx = (this.width - mw) / 2;
        int my = (this.height - mh) / 2;

        if (MathUtils.isHovered(cx, cy, mx + 8, my + 7, 46, 16)) {
            GlassRenderer.playClickSound();
            close();
            return true;
        }

        int sideX = mx + 8, sideY = my + 32, sideW = 100;
        for (int i = 0; i < PROFILES.length; i++) {
            int py = sideY + 20 + i * 22;
            if (MathUtils.isHovered(cx, cy, sideX + 5, py, sideW - 10, 18)) {
                GlassRenderer.playClickSound();
                applyProfilePreset(PROFILES[i]);
                return true;
            }
        }

        int mainX = sideX + sideW + 6, mainY = sideY, mainW = mw - sideW - 22;
        int pasteW = 44, fieldW = mainW - pasteW - 20;
        if (!urlText.isEmpty() && MathUtils.isHovered(cx, cy, mainX + 8 + fieldW - 14, mainY + 18, 14, 18)) {
            urlText = "";
            GlassRenderer.playClickSound();
            return true;
        }
        if (MathUtils.isHovered(cx, cy, mainX + 8, mainY + 18, fieldW, 18)) {
            urlFocused = true;
            return true;
        } else {
            urlFocused = false;
        }
        if (MathUtils.isHovered(cx, cy, mainX + 12 + fieldW, mainY + 18, pasteW, 18)) {
            GlassRenderer.playClickSound();
            if (this.client != null && this.client.keyboard != null) {
                String clip = this.client.keyboard.getClipboard();
                if (clip != null) urlText = clip.trim();
            }
            return true;
        }

        int btnY = mainY + 41, bw = (mainW - 24) / 3;
        if (MathUtils.isHovered(cx, cy, mainX + 8, btnY, bw, 17)) {
            GlassRenderer.playClickSound();
            LiveStreamManager.get().setStreamUrl(urlText);
            LiveStreamManager.get().saveConfig();
            LiveStreamManager.get().connect(urlText);
            return true;
        }
        if (MathUtils.isHovered(cx, cy, mainX + 12 + bw, btnY, bw, 17)) {
            GlassRenderer.playClickSound();
            LiveStreamManager.get().disconnect();
            return true;
        }
        if (MathUtils.isHovered(cx, cy, mainX + 16 + bw * 2, btnY, bw, 17)) {
            GlassRenderer.playClickSound();
            LiveStreamManager.get().refresh();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (urlFocused) {
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) { urlFocused = false; return true; }
            if (key == GLFW.GLFW_KEY_BACKSPACE && !urlText.isEmpty()) {
                urlText = urlText.substring(0, urlText.length() - 1);
                return true;
            }
            if (key == GLFW.GLFW_KEY_V && (input.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0 && this.client != null) {
                String clip = this.client.keyboard.getClipboard();
                if (clip != null) urlText += clip.trim();
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
            urlText += chr;
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public void close() {
        LiveStreamManager.get().setStreamUrl(urlText);
        LiveStreamManager.get().saveConfig();
        if (!closing) {
            closing = true;
            openAnim.animateTo(0.0f);
        }
    }

    @Override
    public boolean shouldPause() { return false; }
}
