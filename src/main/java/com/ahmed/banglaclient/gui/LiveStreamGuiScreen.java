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

public final class LiveStreamGuiScreen extends Screen {
    public enum Category {
        STREAM("Stream", "REPLAY"),
        HUD("HUD", "HUD"),
        CHANNEL("Channel", "ACCOUNT"),
        CHAT("Chat", "CHAT"),
        PROFILES("Profiles", "SETTINGS");

        private final String title;
        private final String iconKey;
        Category(String title, String iconKey) {
            this.title = title;
            this.iconKey = iconKey;
        }
        public String title() { return title; }
        public String iconKey() { return iconKey; }
    }

    private static final String[] PROFILES = {"Default", "Streaming", "Gaming", "Minimal"};

    private final Screen parentScreen;
    private final Animation openAnim = new Animation(0.0f, 200L, Easing.EASE_OUT_QUART);
    private final Animation expandAnim = new Animation(0.0f, 190L, Easing.EASE_OUT_CUBIC);
    private final SmoothScrollContainer panelScroll = new SmoothScrollContainer();

    private Category selectedCategory = null;
    private boolean workspaceModalOpen = false;
    private boolean closing = false;
    private String urlInput = "";
    private boolean urlFocused = false;
    private boolean draggingDelaySlider = false;
    private String activeProfile = "Default";

    public LiveStreamGuiScreen(Screen parentScreen) {
        super(Text.literal("LiveStream GUI"));
        this.parentScreen = parentScreen;
        this.urlInput = LiveStreamManager.get().getStreamUrl();
    }

    @Override
    protected void init() {
        this.closing = false;
        this.openAnim.snapTo(0.0f);
        this.openAnim.animateTo(1.0f);
    }

    private LiveStreamHudModule getLsModule(String id) {
        ModuleManager mm = BanglaClient.getInstance().getModuleManager();
        return mm.getModuleById(id).filter(m -> m instanceof LiveStreamHudModule).map(m -> (LiveStreamHudModule) m).orElse(null);
    }

    private void applyHudProfile(String profile) {
        this.activeProfile = profile;
        String[] allIds = {
                "ls_live_indicator", "ls_viewer_count", "ls_like_count", "ls_channel_name",
                "ls_channel_handle", "ls_subscriber_count", "ls_stream_duration",
                "ls_stream_title", "ls_connection_status", "ls_live_chat"
        };
        for (String id : allIds) {
            LiveStreamHudModule m = getLsModule(id);
            if (m == null) continue;
            switch (profile) {
                case "Streaming" -> m.setEnabled(true);
                case "Minimal" -> m.setEnabled(id.equals("ls_live_indicator") || id.equals("ls_viewer_count") || id.equals("ls_live_chat"));
                case "Gaming" -> m.setEnabled(id.equals("ls_live_indicator") || id.equals("ls_viewer_count"));
                default -> m.setEnabled(!id.equals("ls_connection_status"));
            }
        }
        BanglaClient.getInstance().getConfigManager().saveModulesConfig();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        float openProgress = openAnim.getValue();
        if (closing && openProgress <= 0.02f) {
            LiveStreamManager.get().saveConfig();
            BanglaClient.getInstance().getConfigManager().saveAll();
            if (this.client != null) this.client.setScreen(parentScreen);
            return;
        }

        expandAnim.animateTo(selectedCategory != null ? 1.0f : 0.0f);
        float expand = expandAnim.getValue();

        BlurRenderer.renderScreenBackdrop(ctx, this.width, this.height, openProgress);

        Matrix3x2fStack matrices = ctx.getMatrices();
        float animScale = 0.94f + 0.06f * openProgress;
        matrices.pushMatrix();
        matrices.translate(this.width * 0.5f, this.height * 0.5f);
        matrices.scale(animScale, animScale);
        matrices.translate(-this.width * 0.5f, -this.height * 0.5f);

        try {
            if (workspaceModalOpen) {
                renderWorkspaceModal(ctx, mouseX, mouseY);
                return;
            }

            int centerX = this.width / 2;
            int centerY = this.height / 2;

            String title = GlassRenderer.toSmallCaps("L I V E S T R E A M");
            int titleW = textRenderer.getWidth(title);
            ctx.drawTextWithShadow(textRenderer, title, centerX - titleW / 2, Math.max(10, centerY - 98 - Math.round(expand * 16)), 0xFFFFFFFF);

            Category[] cats = Category.values();
            int cardW = 58, cardH = 64, gap = 8;
            int totalRowW = cats.length * cardW + (cats.length - 1) * gap;
            int startX = centerX - totalRowW / 2;
            int cardY = centerY - cardH / 2;
            int selIndex = selectedCategory != null ? selectedCategory.ordinal() : -1;

            for (int i = 0; i < cats.length; i++) {
                if (selectedCategory == cats[i] && expand > 0.15f) continue;

                int cx = startX + i * (cardW + gap);
                if (selectedCategory != null) {
                    int remIdx = (i < selIndex) ? i : (i - 1);
                    int slot = (remIdx < 2) ? remIdx : (remIdx + 1);
                    cx = startX + slot * (cardW + gap) + Math.round((remIdx < 2 ? -86.0f : 86.0f) * expand);
                }

                boolean hovered = MathUtils.isHovered(mouseX, mouseY, cx, cardY, cardW, cardH);
                int grow = hovered ? 2 : 0;
                GlassRenderer.drawGlassPanel(ctx, cx - grow, cardY - grow, cardW + grow * 2, cardH + grow * 2, true, hovered, hovered);
                GlassRenderer.drawVectorIcon(ctx, cx + (cardW - 14) / 2, cardY + 14, 14, cats[i].iconKey(), 0xFFFFFFFF);

                String lbl = GlassRenderer.toSmallCaps(cats[i].title());
                ctx.drawTextWithShadow(textRenderer, lbl, cx + (cardW - textRenderer.getWidth(lbl)) / 2, cardY + cardH - 20, 0xFFFFFFFF);
                ctx.fill(cx + cardW / 3, cardY + cardH - 8, cx + (cardW * 2) / 3, cardY + cardH - 7, hovered ? 0xFFFFFFFF : 0x55FFFFFF);
            }

            int dockY = Math.min(this.height - 24, centerY + 42 + Math.round(expand * 54));
            drawDockBtn(ctx, centerX - 134, dockY, 88, 20, "Stream Connect", mouseX, mouseY);
            drawDockBtn(ctx, centerX - 42, dockY, 84, 20, "Edit HUD", mouseX, mouseY);
            drawDockBtn(ctx, centerX + 46, dockY, 88, 20, "← Back to GUI", mouseX, mouseY);

            if (expand > 0.05f && selectedCategory != null) {
                int panelW = 228;
                int panelH = Math.min(this.height - 52, Math.round(190 * expand));
                int px = centerX - panelW / 2;
                int py = centerY - panelH / 2;

                GlassRenderer.drawGlassPanel(ctx, px, py, panelW, panelH, true, false, true);
                GlassRenderer.drawVectorIcon(ctx, centerX - 5, py + 6, 10, selectedCategory.iconKey(), 0xFFFFFFFF);
                String catTitle = GlassRenderer.toSmallCaps(selectedCategory.title());
                ctx.drawTextWithShadow(textRenderer, catTitle, centerX - textRenderer.getWidth(catTitle) / 2, py + 17, 0xFFFFFFFF);

                int editBtnY = py + 29;
                boolean editHov = MathUtils.isHovered(mouseX, mouseY, px + 8, editBtnY, panelW - 16, 16);
                GlassRenderer.drawPillBadge(ctx, px + 8, editBtnY, panelW - 16, 16, 0xFFFFFFFF, editHov || true);
                String editTxt = GlassRenderer.toSmallCaps("Edit HUD Layout");
                ctx.drawTextWithShadow(textRenderer, editTxt, centerX - textRenderer.getWidth(editTxt) / 2, editBtnY + 4, 0xFFFFFFFF);

                int listY = py + 49;
                int listH = Math.max(20, panelH - 55);
                renderCategoryList(ctx, px + 8, listY, panelW - 16, listH, mouseX, mouseY);
            }
        } finally {
            matrices.popMatrix();
        }
    }

    private void renderCategoryList(DrawContext ctx, int x, int y, int w, int h, int mouseX, int mouseY) {
        LiveStreamManager mgr = LiveStreamManager.get();
        if (draggingDelaySlider) {
            float norm = MathUtils.clamp((float) (mouseX - (x + 8)) / (float) (w - 24), 0.0f, 1.0f);
            mgr.setChatDelayMs(Math.round(norm * 5000.0f));
        }

        int contentH = switch (selectedCategory) {
            case HUD -> 6 * 24 + 6;
            case CHANNEL -> 4 * 24 + 6;
            case CHAT -> 12 * 24 + 12;
            case PROFILES -> 4 * 24 + 6;
            default -> 40;
        };

        panelScroll.tickAndInterpolate();
        panelScroll.updateDimensions(h, contentH);

        ClipRenderer.renderClipped(ctx, x, y, w - 4, h, () -> {
            int ry = Math.round(y - panelScroll.getScrollOffset()) + 2;
            int rw = w - 8;

            if (selectedCategory == Category.HUD) {
                String[] ids = {"ls_live_indicator", "ls_viewer_count", "ls_like_count", "ls_stream_duration", "ls_stream_title", "ls_connection_status"};
                for (String id : ids) {
                    LiveStreamHudModule m = getLsModule(id);
                    if (m != null) {
                        drawToggleRow(ctx, x, ry, rw, 21, m.getName().replace("LS: ", ""), m.isEnabled(), mouseX, mouseY);
                        ry += 24;
                    }
                }
            } else if (selectedCategory == Category.CHANNEL) {
                String[] ids = {"ls_channel_name", "ls_channel_handle", "ls_subscriber_count"};
                for (String id : ids) {
                    LiveStreamHudModule m = getLsModule(id);
                    if (m != null) {
                        drawToggleRow(ctx, x, ry, rw, 21, m.getName().replace("LS: ", ""), m.isEnabled(), mouseX, mouseY);
                        ry += 24;
                    }
                }
                drawToggleRow(ctx, x, ry, rw, 21, "Auto Connect on Launch", mgr.isAutoConnect(), mouseX, mouseY);
            } else if (selectedCategory == Category.CHAT) {
                LiveStreamHudModule chatMod = getLsModule("ls_live_chat");
                if (chatMod != null) {
                    drawToggleRow(ctx, x, ry, rw, 21, "Enable Live Chat", chatMod.isEnabled(), mouseX, mouseY);
                    ry += 24;
                }
                GlassRenderer.drawGlassPanel(ctx, x, ry, rw, 24, true, false, draggingDelaySlider);
                String dStr = String.format(Locale.ROOT, "Chat Delay: %.1fs", mgr.getChatDelayMs() / 1000.0f);
                GlassRenderer.drawSmallCapsText(ctx, textRenderer, dStr, x + 8, ry + 4, 0xFFFFFFFF);
                int tx = x + 8, tw = rw - 16, ty = ry + 16;
                ctx.fill(tx, ty, tx + tw, ty + 2, 0x33FFFFFF);
                int fw = Math.round((mgr.getChatDelayMs() / 5000.0f) * tw);
                ctx.fill(tx, ty, tx + fw, ty + 2, 0xFFFFFFFF);
                ctx.fill(tx + fw - 2, ty - 2, tx + fw + 2, ty + 4, 0xFFFFFFFF);
                ry += 27;

                boolean aHov = MathUtils.isHovered(mouseX, mouseY, x, ry, rw, 21);
                GlassRenderer.drawGlassPanel(ctx, x, ry, rw, 21, true, aHov, true);
                GlassRenderer.drawSmallCapsText(ctx, textRenderer, "Animation: " + mgr.getAnimationTemplate().getLabel(), x + 8, ry + 6, 0xFFFFFFFF);
                ry += 24;

                drawToggleRow(ctx, x, ry, rw, 21, "Text Shadow", mgr.textShadow, mouseX, mouseY); ry += 24;
                drawToggleRow(ctx, x, ry, rw, 21, "Line Background", mgr.chatShadow, mouseX, mouseY); ry += 24;
                drawToggleRow(ctx, x, ry, rw, 21, "Newest Chat at Top", mgr.newestAtTop, mouseX, mouseY); ry += 24;
                drawToggleRow(ctx, x, ry, rw, 21, "YouTube Icon Badge", mgr.showYouTubeIcon, mouseX, mouseY); ry += 24;
                drawToggleRow(ctx, x, ry, rw, 21, "Colored Usernames", mgr.coloredUsernames, mouseX, mouseY); ry += 24;
                drawToggleRow(ctx, x, ry, rw, 21, "Multi-Color Chat", mgr.coloredMessages, mouseX, mouseY); ry += 24;
                drawToggleRow(ctx, x, ry, rw, 21, "RGB Chroma Mode", mgr.rgbMode, mouseX, mouseY); ry += 24;
                drawToggleRow(ctx, x, ry, rw, 21, "Show Timestamp", mgr.showTimestamp, mouseX, mouseY); ry += 24;
                drawToggleRow(ctx, x, ry, rw, 21, "Show Username", mgr.showUsername, mouseX, mouseY);
            } else if (selectedCategory == Category.PROFILES) {
                for (String p : PROFILES) {
                    boolean act = p.equals(activeProfile);
                    drawToggleRow(ctx, x, ry, rw, 21, "Profile: " + p, act, mouseX, mouseY);
                    ry += 24;
                }
            }
        });
        panelScroll.renderScrollbar(ctx, x + w - 3, y, 2, h, mouseX, mouseY);
    }

    private void renderWorkspaceModal(DrawContext ctx, int mouseX, int mouseY) {
        LiveStreamManager mgr = LiveStreamManager.get();
        var snap = mgr.snapshot();

        int mw = Math.min(430, this.width - 24);
        int mh = Math.min(220, this.height - 20);
        int mx = (this.width - mw) / 2;
        int my = (this.height - mh) / 2;

        GlassRenderer.drawGlassPanel(ctx, mx, my, mw, mh, false, false, true);

        boolean backHov = MathUtils.isHovered(mouseX, mouseY, mx + 8, my + 7, 46, 16);
        GlassRenderer.drawPillBadge(ctx, mx + 8, my + 7, 46, 16, 0xFFFFFFFF, backHov);
        GlassRenderer.drawSmallCapsText(ctx, textRenderer, "← Back", mx + 14, my + 11, 0xFFFFFFFF);
        GlassRenderer.drawSmallCapsText(ctx, textRenderer, "Stream Workspace", mx + 62, my + 11, 0xFFFFFFFF);

        String stateStr = snap.state().name();
        int sw = textRenderer.getWidth(stateStr) + 14;
        GlassRenderer.drawPillBadge(ctx, mx + mw - sw - 8, my + 7, sw, 16, 0xFFFFFFFF, true);
        ctx.drawTextWithShadow(textRenderer, stateStr, mx + mw - sw - 1, my + 11, 0xFFFFFFFF);

        GlassRenderer.drawHorizontalDivider(ctx, mx + 8, my + 27, mw - 16);

        int sideX = mx + 8, sideY = my + 32, sideW = 96, sideH = mh - 40;
        GlassRenderer.drawGlassPanel(ctx, sideX, sideY, sideW, sideH, true, false, false);
        GlassRenderer.drawSmallCapsText(ctx, textRenderer, "HUD Profiles", sideX + 8, sideY + 6, 0xFF94A3B8);
        for (int i = 0; i < PROFILES.length; i++) {
            int py = sideY + 20 + i * 22;
            boolean act = PROFILES[i].equals(activeProfile);
            boolean hov = MathUtils.isHovered(mouseX, mouseY, sideX + 5, py, sideW - 10, 18);
            GlassRenderer.drawPillBadge(ctx, sideX + 5, py, sideW - 10, 18, 0xFFFFFFFF, act || hov);
            GlassRenderer.drawSmallCapsText(ctx, textRenderer, PROFILES[i], sideX + 12, py + 5, 0xFFFFFFFF);
        }

        int mainX = sideX + sideW + 6, mainY = sideY, mainW = mw - sideW - 22;
        GlassRenderer.drawGlassPanel(ctx, mainX, mainY, mainW, sideH, true, false, false);

        GlassRenderer.drawSmallCapsText(ctx, textRenderer, "Stream URL", mainX + 8, mainY + 6, 0xFFFFFFFF);
        int pasteW = 42;
        int fieldW = mainW - pasteW - 20;
        GlassRenderer.drawGlassPanel(ctx, mainX + 8, mainY + 18, fieldW, 18, false, false, urlFocused);
        String disp = urlInput.isEmpty() && !urlFocused
                ? "Paste YouTube Live link..."
                : textRenderer.trimToWidth(urlInput, fieldW - 12) + (urlFocused && (System.currentTimeMillis() / 500L) % 2L == 0L ? "|" : "");
        ctx.drawTextWithShadow(textRenderer, disp, mainX + 12, mainY + 23, urlInput.isEmpty() && !urlFocused ? 0xFF64748B : 0xFFFFFFFF);

        int pasteX = mainX + 12 + fieldW;
        GlassRenderer.drawPillBadge(ctx, pasteX, mainY + 18, pasteW, 18, 0xFFFFFFFF, true);
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
        int cardH = 28;
        drawMiniTelemetry(ctx, mainX + 8, mainY + 76, cardW, cardH, "Channel", snap.channelName());
        drawMiniTelemetry(ctx, mainX + 12 + cardW, mainY + 76, cardW, cardH, "Subscribers", LiveStreamManager.formatCompactNumber(snap.subscriberCount()));
        drawMiniTelemetry(ctx, mainX + 8, mainY + 108, cardW, cardH, "Live Viewers", LiveStreamManager.formatCompactNumber(snap.viewerCount()));
        drawMiniTelemetry(ctx, mainX + 12 + cardW, mainY + 108, cardW, cardH, "Likes", LiveStreamManager.formatCompactNumber(snap.likeCount()));
    }

    private void drawMiniTelemetry(DrawContext ctx, int x, int y, int w, int h, String label, String val) {
        GlassRenderer.drawGlassPanel(ctx, x, y, w, h, false, false, false);
        GlassRenderer.drawSmallCapsText(ctx, textRenderer, label, x + 6, y + 4, 0xFF94A3B8);
        ctx.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(val != null ? val : "--", w - 12), x + 6, y + 15, 0xFFFFFFFF);
    }

    private void drawDockBtn(DrawContext ctx, int x, int y, int w, int h, String label, int mouseX, int mouseY) {
        boolean hov = MathUtils.isHovered(mouseX, mouseY, x, y, w, h);
        GlassRenderer.drawGlassPanel(ctx, x, y, w, h, true, hov, hov);
        String s = GlassRenderer.toSmallCaps(label);
        ctx.drawTextWithShadow(textRenderer, s, x + (w - textRenderer.getWidth(s)) / 2, y + (h - 8) / 2, 0xFFFFFFFF);
    }

    private void drawToggleRow(DrawContext ctx, int x, int y, int w, int h, String label, boolean state, int mouseX, int mouseY) {
        boolean hov = MathUtils.isHovered(mouseX, mouseY, x, y, w, h);
        GlassRenderer.drawGlassPanel(ctx, x, y, w, h, true, hov, state);
        GlassRenderer.drawSmallCapsText(ctx, textRenderer, label, x + 8, y + (h - 8) / 2, state ? 0xFFFFFFFF : 0xFF94A3B8);

        int swW = 20, swH = 10, swX = x + w - swW - 6, swY = y + (h - swH) / 2;
        ctx.fill(swX + 1, swY, swX + swW - 1, swY + swH, state ? 0x4CFFFFFF : 0xFF07080B);
        GlassRenderer.drawOutlineBorder(ctx, swX, swY, swW, swH, state ? 0xEEFFFFFF : 0x38FFFFFF);
        int kx = state ? (swX + swW - 8) : (swX + 2);
        ctx.fill(kx, swY + 2, kx + 6, swY + swH - 2, state ? 0xFFFFFFFF : 0xFF64748B);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        if (click.button() != 0 || closing) return super.mouseClicked(click, doubled);

        LiveStreamManager mgr = LiveStreamManager.get();

        if (workspaceModalOpen) {
            int mw = Math.min(430, this.width - 24);
            int mh = Math.min(220, this.height - 20);
            int winX = (this.width - mw) / 2;
            int winY = (this.height - mh) / 2;

            if (MathUtils.isHovered(mx, my, winX + 8, winY + 7, 46, 16)) {
                GlassRenderer.playClickSound();
                workspaceModalOpen = false;
                return true;
            }

            int sideX = winX + 8, sideY = winY + 32, sideW = 96;
            for (int i = 0; i < PROFILES.length; i++) {
                int py = sideY + 20 + i * 22;
                if (MathUtils.isHovered(mx, my, sideX + 5, py, sideW - 10, 18)) {
                    GlassRenderer.playClickSound();
                    applyHudProfile(PROFILES[i]);
                    return true;
                }
            }

            int mainX = sideX + sideW + 6, mainY = sideY, mainW = mw - sideW - 22;
            int pasteW = 42, fieldW = mainW - pasteW - 20;
            if (MathUtils.isHovered(mx, my, mainX + 8, mainY + 18, fieldW, 18)) {
                urlFocused = true;
                return true;
            } else {
                urlFocused = false;
            }
            if (MathUtils.isHovered(mx, my, mainX + 12 + fieldW, mainY + 18, pasteW, 18)) {
                GlassRenderer.playClickSound();
                if (this.client != null && this.client.keyboard != null) {
                    String clip = this.client.keyboard.getClipboard();
                    if (clip != null) urlInput = clip.trim();
                }
                return true;
            }
            int btnY = mainY + 41, bw = (mainW - 24) / 3;
            if (MathUtils.isHovered(mx, my, mainX + 8, btnY, bw, 17)) {
                GlassRenderer.playClickSound();
                mgr.setStreamUrl(urlInput);
                mgr.saveConfig();
                mgr.connect(urlInput);
                return true;
            }
            if (MathUtils.isHovered(mx, my, mainX + 12 + bw, btnY, bw, 17)) {
                GlassRenderer.playClickSound();
                mgr.disconnect();
                return true;
            }
            if (MathUtils.isHovered(mx, my, mainX + 16 + bw * 2, btnY, bw, 17)) {
                GlassRenderer.playClickSound();
                mgr.refresh();
                return true;
            }
            return true;
        }

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        float expand = expandAnim.getValue();
        int dockY = Math.min(this.height - 24, centerY + 42 + Math.round(expand * 54));

        if (MathUtils.isHovered(mx, my, centerX - 134, dockY, 88, 20)) {
            GlassRenderer.playClickSound();
            workspaceModalOpen = true;
            return true;
        }
        if (MathUtils.isHovered(mx, my, centerX - 42, dockY, 84, 20)) {
            GlassRenderer.playClickSound();
            if (this.client != null) this.client.setScreen(new HudEditorScreen(this));
            return true;
        }
        if (MathUtils.isHovered(mx, my, centerX + 46, dockY, 88, 20)) {
            GlassRenderer.playClickSound();
            close();
            return true;
        }

        Category[] cats = Category.values();
        int cardW = 58, cardH = 64, gap = 8;
        int totalRowW = cats.length * cardW + (cats.length - 1) * gap;
        int startX = centerX - totalRowW / 2;
        int cardY = centerY - cardH / 2;
        int selIndex = selectedCategory != null ? selectedCategory.ordinal() : -1;

        if (selectedCategory != null) {
            int panelW = 228;
            int panelH = Math.min(this.height - 52, 190);
            int px = centerX - panelW / 2;
            int py = centerY - panelH / 2;

            if (MathUtils.isHovered(mx, my, px + 8, py + 29, panelW - 16, 16)) {
                GlassRenderer.playClickSound();
                if (this.client != null) this.client.setScreen(new HudEditorScreen(this));
                return true;
            }

            int listY = py + 49, listH = Math.max(20, panelH - 55);
            if (panelScroll.onMouseClicked(mx, my, 0, px + panelW - 12, listY, 4, listH)) return true;

            if (MathUtils.isHovered(mx, my, px + 8, listY, panelW - 20, listH)) {
                int ry = Math.round(listY - panelScroll.getScrollOffset()) + 2;
                int rw = panelW - 24;

                if (selectedCategory == Category.HUD) {
                    String[] ids = {"ls_live_indicator", "ls_viewer_count", "ls_like_count", "ls_stream_duration", "ls_stream_title", "ls_connection_status"};
                    for (String id : ids) {
                        LiveStreamHudModule m = getLsModule(id);
                        if (m != null && MathUtils.isHovered(mx, my, px + 8, ry, rw, 21)) {
                            GlassRenderer.playClickSound();
                            m.toggle();
                            return true;
                        }
                        ry += 24;
                    }
                } else if (selectedCategory == Category.CHANNEL) {
                    String[] ids = {"ls_channel_name", "ls_channel_handle", "ls_subscriber_count"};
                    for (String id : ids) {
                        LiveStreamHudModule m = getLsModule(id);
                        if (m != null && MathUtils.isHovered(mx, my, px + 8, ry, rw, 21)) {
                            GlassRenderer.playClickSound();
                            m.toggle();
                            return true;
                        }
                        ry += 24;
                    }
                    if (MathUtils.isHovered(mx, my, px + 8, ry, rw, 21)) {
                        GlassRenderer.playClickSound();
                        mgr.setAutoConnect(!mgr.isAutoConnect());
                        mgr.saveConfig();
                        return true;
                    }
                } else if (selectedCategory == Category.CHAT) {
                    LiveStreamHudModule chatMod = getLsModule("ls_live_chat");
                    if (chatMod != null) {
                        if (MathUtils.isHovered(mx, my, px + 8, ry, rw, 21)) {
                            GlassRenderer.playClickSound();
                            chatMod.toggle();
                            return true;
                        }
                        ry += 24;
                    }
                    if (MathUtils.isHovered(mx, my, px + 8, ry, rw, 24)) {
                        draggingDelaySlider = true;
                        float norm = MathUtils.clamp((float) (mx - (px + 16)) / (float) (rw - 16), 0.0f, 1.0f);
                        mgr.setChatDelayMs(Math.round(norm * 5000.0f));
                        return true;
                    }
                    ry += 27;
                    if (MathUtils.isHovered(mx, my, px + 8, ry, rw, 21)) {
                        GlassRenderer.playClickSound();
                        mgr.setAnimationTemplate(mgr.getAnimationTemplate().next());
                        mgr.saveConfig();
                        return true;
                    }
                    ry += 24;
                    if (MathUtils.isHovered(mx, my, px + 8, ry, rw, 21)) { mgr.textShadow = !mgr.textShadow; GlassRenderer.playClickSound(); return true; } ry += 24;
                    if (MathUtils.isHovered(mx, my, px + 8, ry, rw, 21)) { mgr.chatShadow = !mgr.chatShadow; GlassRenderer.playClickSound(); return true; } ry += 24;
                    if (MathUtils.isHovered(mx, my, px + 8, ry, rw, 21)) { mgr.newestAtTop = !mgr.newestAtTop; GlassRenderer.playClickSound(); return true; } ry += 24;
                    if (MathUtils.isHovered(mx, my, px + 8, ry, rw, 21)) { mgr.showYouTubeIcon = !mgr.showYouTubeIcon; GlassRenderer.playClickSound(); return true; } ry += 24;
                    if (MathUtils.isHovered(mx, my, px + 8, ry, rw, 21)) { mgr.coloredUsernames = !mgr.coloredUsernames; GlassRenderer.playClickSound(); return true; } ry += 24;
                    if (MathUtils.isHovered(mx, my, px + 8, ry, rw, 21)) { mgr.coloredMessages = !mgr.coloredMessages; GlassRenderer.playClickSound(); return true; } ry += 24;
                    if (MathUtils.isHovered(mx, my, px + 8, ry, rw, 21)) { mgr.rgbMode = !mgr.rgbMode; GlassRenderer.playClickSound(); return true; } ry += 24;
                    if (MathUtils.isHovered(mx, my, px + 8, ry, rw, 21)) { mgr.showTimestamp = !mgr.showTimestamp; GlassRenderer.playClickSound(); return true; } ry += 24;
                    if (MathUtils.isHovered(mx, my, px + 8, ry, rw, 21)) { mgr.showUsername = !mgr.showUsername; GlassRenderer.playClickSound(); return true; }
                } else if (selectedCategory == Category.PROFILES) {
                    for (String p : PROFILES) {
                        if (MathUtils.isHovered(mx, my, px + 8, ry, rw, 21)) {
                            GlassRenderer.playClickSound();
                            applyHudProfile(p);
                            return true;
                        }
                        ry += 24;
                    }
                }
                return true;
            }

            if (!MathUtils.isHovered(mx, my, px, py, panelW, panelH)) {
                for (int i = 0; i < cats.length; i++) {
                    if (cats[i] == selectedCategory) continue;
                    int remIdx = (i < selIndex) ? i : (i - 1);
                    int slot = (remIdx < 2) ? remIdx : (remIdx + 1);
                    int cx = startX + slot * (cardW + gap) + Math.round((remIdx < 2 ? -86.0f : 86.0f) * expand);
                    if (MathUtils.isHovered(mx, my, cx, cardY, cardW, cardH)) {
                        GlassRenderer.playClickSound();
                        if (cats[i] == Category.STREAM) {
                            workspaceModalOpen = true;
                        } else {
                            selectedCategory = cats[i];
                            panelScroll.resetScroll();
                        }
                        return true;
                    }
                }
                selectedCategory = null;
                return true;
            }
        } else {
            for (int i = 0; i < cats.length; i++) {
                int cx = startX + i * (cardW + gap);
                if (MathUtils.isHovered(mx, my, cx, cardY, cardW, cardH)) {
                    GlassRenderer.playClickSound();
                    if (cats[i] == Category.STREAM) {
                        workspaceModalOpen = true;
                    } else {
                        selectedCategory = cats[i];
                        panelScroll.resetScroll();
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click c, double dx, double dy) {
        if (draggingDelaySlider) return true;
        return panelScroll.onMouseDragged(c.x(), c.y(), c.button(), 135) || super.mouseDragged(c, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click c) {
        if (draggingDelaySlider) {
            draggingDelaySlider = false;
            LiveStreamManager.get().saveConfig();
        }
        panelScroll.onMouseReleased(c.button());
        return super.mouseReleased(c);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (selectedCategory != null) {
            panelScroll.onMouseScrolled(mouseX, mouseY, verticalAmount, 0, 0, this.width, this.height);
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
            if (workspaceModalOpen) { workspaceModalOpen = false; return true; }
            if (selectedCategory != null) { selectedCategory = null; return true; }
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
