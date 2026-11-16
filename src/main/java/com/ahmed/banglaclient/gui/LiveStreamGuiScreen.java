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
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

/**
 * Full-Featured LiveStream ClickGUI in BANGLA CLIENT's Aura Dark Design:
 * - 5 Category Cards: [Stream] [HUD] [Channel] [Chat] [Theme/Dev]
 * - Bottom Dock: [Stream Connect] [Edit HUD] [← Back]
 * - Every slider and toggle from LiveStreem-main (Chat Delay 0-5s, Chat Text Size 0.7x-1.8x,
 *   Animation Template, Text Shadow, Line Bg, Newest at Top, YouTube Badge, Colored Usernames,
 *   Multi-Color Chat, RGB Chroma, Timestamp, Username, Chat Box Frame, Dev Telemetry, Bounding Boxes).
 */
public final class LiveStreamGuiScreen extends Screen {
    public enum Category {
        STREAM("Stream", "REPLAY"),
        HUD("HUD", "HUD"),
        CHANNEL("Channel", "ACCOUNT"),
        CHAT("Chat", "CHAT"),
        THEME("Telemetry", "SETTINGS");

        private final String title;
        private final String iconKey;
        Category(String title, String iconKey) {
            this.title = title;
            this.iconKey = iconKey;
        }
        public String title() { return title; }
        public String iconKey() { return iconKey; }
    }

    private final Screen parentScreen;
    private final Animation openAnim = new Animation(0.0f, 190L, Easing.EASE_OUT_QUART);
    private final Animation expandAnim = new Animation(0.0f, 180L, Easing.EASE_OUT_CUBIC);
    private final SmoothScrollContainer panelScroll = new SmoothScrollContainer();

    private Category selectedCategory = null;
    private boolean closing = false;
    private int activeChatSlider = -1; // 0 = Delay, 1 = Text Size

    public LiveStreamGuiScreen(Screen parentScreen) {
        super(Text.literal("LiveStream GUI"));
        this.parentScreen = parentScreen;
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

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float deltaTicks) {
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
                    cx = startX + slot * (cardW + gap) + Math.round((remIdx < 2 ? -88.0f : 88.0f) * expand);
                }

                boolean hovered = MathUtils.isHovered(mouseX, mouseY, cx, cardY, cardW, cardH);
                int grow = hovered ? 2 : 0;
                GlassRenderer.drawGlassPanel(ctx, cx - grow, cardY - grow, cardW + grow * 2, cardH + grow * 2, true, hovered, hovered);
                GlassRenderer.drawVectorIcon(ctx, cx + (cardW - 14) / 2, cardY + 14, 14, cats[i].iconKey(), 0xFFFFFFFF);

                String lbl = GlassRenderer.toSmallCaps(cats[i].title());
                ctx.drawTextWithShadow(textRenderer, lbl, cx + (cardW - textRenderer.getWidth(lbl)) / 2, cardY + cardH - 20, 0xFFFFFFFF);
                ctx.fill(cx + cardW / 3, cardY + cardH - 8, cx + (cardW * 2) / 3, cardY + cardH - 7, hovered ? 0xFFFFFFFF : 0x55FFFFFF);
            }

            int dockY = Math.min(this.height - 24, centerY + 42 + Math.round(expand * 56));
            drawDockBtn(ctx, centerX - 134, dockY, 88, 20, "Stream Connect", mouseX, mouseY);
            drawDockBtn(ctx, centerX - 42, dockY, 84, 20, "Edit HUD", mouseX, mouseY);
            drawDockBtn(ctx, centerX + 46, dockY, 88, 20, "← Back to GUI", mouseX, mouseY);

            if (expand > 0.05f && selectedCategory != null) {
                int panelW = 232;
                int panelH = Math.min(this.height - 52, Math.round(194 * expand));
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
        if (activeChatSlider == 0) {
            float norm = MathUtils.clamp((float) (mouseX - (x + 8)) / (float) (w - 24), 0.0f, 1.0f);
            mgr.setChatDelayMs(Math.round(norm * 5000.0f));
        } else if (activeChatSlider == 1) {
            float norm = MathUtils.clamp((float) (mouseX - (x + 8)) / (float) (w - 24), 0.0f, 1.0f);
            mgr.setChatTextScale(Math.round((0.7f + norm * 1.1f) * 10.0f) / 10.0f);
        }

        int contentH = switch (selectedCategory) {
            case HUD -> 10 * 24 + 10;
            case CHANNEL -> 4 * 24 + 6;
            case CHAT -> 14 * 24 + 18;
            case THEME -> 3 * 24 + 6;
            default -> 40;
        };

        panelScroll.tickAndInterpolate();
        panelScroll.updateDimensions(h, contentH);

        ClipRenderer.renderClipped(ctx, x, y, w - 4, h, () -> {
            int ry = Math.round(y - panelScroll.getScrollOffset()) + 2;
            int rw = w - 8;

            if (selectedCategory == Category.HUD) {
                String[] ids = {"ls_live_chat", "ls_live_indicator", "ls_viewer_count", "ls_like_count", "ls_subscriber_count", "ls_channel_name", "ls_channel_handle", "ls_stream_duration", "ls_stream_title", "ls_connection_status"};
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
                // Slider 1: Chat Delay (0.0s to 5.0s)
                drawSliderRow(ctx, x, ry, rw, 24, String.format(Locale.ROOT, "Chat Delay: %.1fs", mgr.getChatDelayMs() / 1000.0f),
                        mgr.getChatDelayMs() / 5000.0f, activeChatSlider == 0);
                ry += 27;

                // Slider 2: Chat Text Size (0.7x to 1.8x)
                drawSliderRow(ctx, x, ry, rw, 24, String.format(Locale.ROOT, "Chat Text Size: %.1fx", mgr.getChatTextScale()),
                        (mgr.getChatTextScale() - 0.7f) / 1.1f, activeChatSlider == 1);
                ry += 27;

                // Animation Template Selector
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
                drawToggleRow(ctx, x, ry, rw, 21, "Show Username", mgr.showUsername, mouseX, mouseY); ry += 24;
                drawToggleRow(ctx, x, ry, rw, 21, "Chat Box Frame", mgr.chatBoxFrame, mouseX, mouseY);
            } else if (selectedCategory == Category.THEME) {
                drawToggleRow(ctx, x, ry, rw, 21, "Dev Telemetry Overlay", mgr.devTelemetryEnabled, mouseX, mouseY); ry += 24;
                drawToggleRow(ctx, x, ry, rw, 21, "Show HUD Bounding Boxes", mgr.hudBoundingBoxesEnabled, mouseX, mouseY);
            }
        });
        panelScroll.renderScrollbar(ctx, x + w - 3, y, 2, h, mouseX, mouseY);
    }

    private void drawSliderRow(DrawContext ctx, int x, int y, int w, int h, String label, float norm, boolean active) {
        GlassRenderer.drawGlassPanel(ctx, x, y, w, h, true, false, active);
        GlassRenderer.drawSmallCapsText(ctx, textRenderer, label, x + 8, y + 4, 0xFFFFFFFF);
        int tx = x + 8, tw = w - 16, ty = y + 16;
        ctx.fill(tx, ty, tx + tw, ty + 2, 0x33FFFFFF);
        int fw = Math.round(MathUtils.clamp(norm, 0.0f, 1.0f) * tw);
        ctx.fill(tx, ty, tx + fw, ty + 2, 0xFFFFFFFF);
        ctx.fill(tx + fw - 2, ty - 2, tx + fw + 2, ty + 4, 0xFFFFFFFF);
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
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        float expand = expandAnim.getValue();
        int dockY = Math.min(this.height - 24, centerY + 42 + Math.round(expand * 56));

        if (MathUtils.isHovered(mx, my, centerX - 134, dockY, 88, 20)) {
            GlassRenderer.playClickSound();
            if (this.client != null) this.client.setScreen(new WorkspaceModalScreen(this));
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
            int panelW = 232;
            int panelH = Math.min(this.height - 52, 194);
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
                    String[] ids = {"ls_live_chat", "ls_live_indicator", "ls_viewer_count", "ls_like_count", "ls_subscriber_count", "ls_channel_name", "ls_channel_handle", "ls_stream_duration", "ls_stream_title", "ls_connection_status"};
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
                        activeChatSlider = 0;
                        float norm = MathUtils.clamp((float) (mx - (px + 16)) / (float) (rw - 16), 0.0f, 1.0f);
                        mgr.setChatDelayMs(Math.round(norm * 5000.0f));
                        return true;
                    }
                    ry += 27;
                    if (MathUtils.isHovered(mx, my, px + 8, ry, rw, 24)) {
                        activeChatSlider = 1;
                        float norm = MathUtils.clamp((float) (mx - (px + 16)) / (float) (rw - 16), 0.0f, 1.0f);
                        mgr.setChatTextScale(Math.round((0.7f + norm * 1.1f) * 10.0f) / 10.0f);
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
                    if (MathUtils.isHovered(mx, my, px + 8, ry, rw, 21)) { mgr.showUsername = !mgr.showUsername; GlassRenderer.playClickSound(); return true; } ry += 24;
                    if (MathUtils.isHovered(mx, my, px + 8, ry, rw, 21)) { mgr.chatBoxFrame = !mgr.chatBoxFrame; GlassRenderer.playClickSound(); return true; }
                } else if (selectedCategory == Category.THEME) {
                    if (MathUtils.isHovered(mx, my, px + 8, ry, rw, 21)) { mgr.devTelemetryEnabled = !mgr.devTelemetryEnabled; GlassRenderer.playClickSound(); return true; } ry += 24;
                    if (MathUtils.isHovered(mx, my, px + 8, ry, rw, 21)) { mgr.hudBoundingBoxesEnabled = !mgr.hudBoundingBoxesEnabled; GlassRenderer.playClickSound(); return true; }
                }
                return true;
            }

            if (!MathUtils.isHovered(mx, my, px, py, panelW, panelH)) {
                for (int i = 0; i < cats.length; i++) {
                    if (cats[i] == selectedCategory) continue;
                    int remIdx = (i < selIndex) ? i : (i - 1);
                    int slot = (remIdx < 2) ? remIdx : (remIdx + 1);
                    int cx = startX + slot * (cardW + gap) + Math.round((remIdx < 2 ? -88.0f : 88.0f) * expand);
                    if (MathUtils.isHovered(mx, my, cx, cardY, cardW, cardH)) {
                        GlassRenderer.playClickSound();
                        if (cats[i] == Category.STREAM) {
                            if (this.client != null) this.client.setScreen(new WorkspaceModalScreen(this));
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
                        if (this.client != null) this.client.setScreen(new WorkspaceModalScreen(this));
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
        if (activeChatSlider >= 0) return true;
        return panelScroll.onMouseDragged(c.x(), c.y(), c.button(), 135) || super.mouseDragged(c, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click c) {
        if (activeChatSlider >= 0) {
            activeChatSlider = -1;
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
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            if (selectedCategory != null) { selectedCategory = null; return true; }
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        LiveStreamManager.get().saveConfig();
        BanglaClient.getInstance().getConfigManager().saveAll();
        if (!closing) {
            closing = true;
            openAnim.animateTo(0.0f);
        }
    }

    @Override
    public boolean shouldPause() { return false; }
}
