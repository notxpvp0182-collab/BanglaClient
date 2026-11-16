package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.livestream.LiveStreamHudModule;
import com.ahmed.banglaclient.livestream.LiveStreamManager;
import com.ahmed.banglaclient.module.ModuleManager;
import dev.ahmed.liquidglass.ui.animation.Animation;
import dev.ahmed.liquidglass.ui.components.GlassSlider;
import dev.ahmed.liquidglass.ui.components.GlassToggle;
import dev.ahmed.liquidglass.ui.render.IconRenderer;
import dev.ahmed.liquidglass.ui.render.UIRenderer;
import dev.ahmed.liquidglass.ui.theme.GlassQuality;
import dev.ahmed.liquidglass.ui.theme.ThemeManager;
import dev.ahmed.liquidglass.ui.theme.ThemeTokens;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class LiveStreamGuiScreen extends Screen {
    public enum Category {
        STREAM("Stream", IconRenderer.Icon.LIVE),
        HUD("HUD", IconRenderer.Icon.DISPLAY),
        CHANNEL("Channel", IconRenderer.Icon.PROFILE),
        CHAT("Chat", IconRenderer.Icon.CHAT),
        THEME("Theme", IconRenderer.Icon.SLIDERS);

        private final String title;
        private final IconRenderer.Icon icon;
        Category(String title, IconRenderer.Icon icon) {
            this.title = title;
            this.icon = icon;
        }
        public String title() { return title; }
        public IconRenderer.Icon icon() { return icon; }
    }

    private final Screen parentScreen;
    private final Animation openAnim = new Animation(0.0f, 0.25f, Animation.Easing.EASE_OUT_QUART);
    private final Animation expandAnim = new Animation(0.0f, 0.24f, Animation.Easing.EASE_OUT_CUBIC);
    private final Animation panelSwitchAnim = new Animation(1.0f, 0.22f, Animation.Easing.EASE_OUT_QUART);
    private final Animation[] cardHoverAnims = new Animation[Category.values().length];
    private final Animation[] cardXAnims = new Animation[Category.values().length];
    private boolean cardPositionsInitialized = false;

    private Category selectedCategory = null;
    private boolean closing = false;
    private boolean draggingScrollbar = false;
    private long lastFrameNs = System.nanoTime();
    private float scrollOffset = 0.0f;
    private final List<GlassToggle> activeToggles = new ArrayList<>();
    private final List<GlassSlider> activeSliders = new ArrayList<>();

    public LiveStreamGuiScreen(Screen parentScreen) {
        super(Text.literal("LiveStream GUI"));
        this.parentScreen = parentScreen;
        for (int i = 0; i < Category.values().length; i++) {
            cardHoverAnims[i] = new Animation(0.0f, 0.15f, Animation.Easing.EASE_OUT_CUBIC);
            cardXAnims[i] = new Animation(0.0f, 0.24f, Animation.Easing.EASE_OUT_QUART);
        }
        openAnim.animateTo(1.0f);
    }

    private float getResponsiveScale() {
        float sx = this.width / 620.0f;
        float sy = this.height / 345.0f;
        return Math.max(0.58f, Math.min(1.25f, Math.min(sx, sy)));
    }

    private float toDesignX(double screenX) {
        float s = getResponsiveScale();
        return (float) ((screenX - this.width * 0.5) / s + this.width * 0.5);
    }

    private float toDesignY(double screenY) {
        float s = getResponsiveScale();
        return (float) ((screenY - this.height * 0.5) / s + this.height * 0.5);
    }

    private LiveStreamHudModule getLsModule(String id) {
        ModuleManager mm = BanglaClient.getInstance().getModuleManager();
        return mm.getModuleById(id).filter(m -> m instanceof LiveStreamHudModule).map(m -> (LiveStreamHudModule) m).orElse(null);
    }

    private void selectCategoryWithAnimation(Category cat) {
        this.selectedCategory = cat;
        this.scrollOffset = 0.0f;
        this.panelSwitchAnim.animateTo(0.0f);
        this.panelSwitchAnim.update(1.0f);
        this.panelSwitchAnim.animateTo(1.0f);
        rebuildCategoryControls(cat);
    }

    private void rebuildCategoryControls(Category cat) {
        activeToggles.clear();
        activeSliders.clear();
        if (cat == null) return;
        LiveStreamManager mgr = LiveStreamManager.get();

        switch (cat) {
            case HUD -> {
                String[] ids = {"ls_live_indicator", "ls_viewer_count", "ls_like_count", "ls_stream_duration", "ls_stream_title", "ls_connection_status"};
                for (String id : ids) {
                    LiveStreamHudModule el = getLsModule(id);
                    if (el != null) {
                        activeToggles.add(new GlassToggle(el.getName().replace("LS: ", ""), el::isEnabled, el::setEnabled));
                    }
                }
            }
            case CHANNEL -> {
                String[] ids = {"ls_channel_name", "ls_channel_handle", "ls_subscriber_count"};
                for (String id : ids) {
                    LiveStreamHudModule el = getLsModule(id);
                    if (el != null) {
                        activeToggles.add(new GlassToggle(el.getName().replace("LS: ", ""), el::isEnabled, el::setEnabled));
                    }
                }
                activeToggles.add(new GlassToggle("Auto Connect on Launch", mgr::isAutoConnect, mgr::setAutoConnect));
            }
            case CHAT -> {
                LiveStreamHudModule chat = getLsModule("ls_live_chat");
                if (chat != null) {
                    activeToggles.add(new GlassToggle("Enable Live Chat", chat::isEnabled, chat::setEnabled));
                }
                activeSliders.add(new GlassSlider("Chat Delay", 0.0, 5.0, 0.1, "s",
                    () -> mgr.getChatDelayMs() / 1000.0,
                    sec -> mgr.setChatDelayMs((int) Math.round(sec * 1000.0))));
                activeSliders.add(new GlassSlider("Animation (1..5)", 1.0, 5.0, 1.0, "",
                    () -> mgr.getAnimationTemplate().ordinal() + 1.0,
                    val -> {
                        int idx = Math.max(0, Math.min(4, (int) Math.round(val) - 1));
                        mgr.setAnimationTemplate(LiveStreamManager.ChatAnimationTemplate.values()[idx]);
                    }));
                activeToggles.add(new GlassToggle("Text Shadow", () -> mgr.textShadow, v -> mgr.textShadow = v));
                activeToggles.add(new GlassToggle("Line Background", () -> mgr.chatShadow, v -> mgr.chatShadow = v));
                activeToggles.add(new GlassToggle("Newest Chat at Top", () -> mgr.newestAtTop, v -> mgr.newestAtTop = v));
                activeToggles.add(new GlassToggle("YouTube Icon Badge", () -> mgr.showYouTubeIcon, v -> mgr.showYouTubeIcon = v));
                activeToggles.add(new GlassToggle("Colored Usernames", () -> mgr.coloredUsernames, v -> mgr.coloredUsernames = v));
                activeToggles.add(new GlassToggle("Multi-Color Chat", () -> mgr.coloredMessages, v -> mgr.coloredMessages = v));
                activeToggles.add(new GlassToggle("RGB Chroma Mode", () -> mgr.rgbMode, v -> mgr.rgbMode = v));
                activeToggles.add(new GlassToggle("Show Timestamp", () -> mgr.showTimestamp, v -> mgr.showTimestamp = v));
                activeToggles.add(new GlassToggle("Show Username", () -> mgr.showUsername, v -> mgr.showUsername = v));
            }
            case THEME -> {
                activeSliders.add(new GlassSlider("Quality (1=Low..4=Ultra)", 1.0, 4.0, 1.0, "",
                    () -> ThemeManager.get().quality().ordinal() + 1.0,
                    val -> {
                        int idx = Math.max(0, Math.min(3, (int) Math.round(val) - 1));
                        ThemeManager.get().setQuality(GlassQuality.values()[idx]);
                    }));
                activeToggles.add(new GlassToggle("AMOLED Obsidian Theme",
                    () -> "amoled_glass".equals(ThemeManager.get().tokens().id()),
                    on -> ThemeManager.get().setActiveTheme(on ? "amoled_glass" : "dark_liquid_glass")));
            }
            default -> {}
        }
    }

    private float getContentHeight() {
        return activeSliders.size() * 28.0f + activeToggles.size() * 26.0f;
    }

    private float getMaxScroll(float viewHeight) {
        return Math.max(0.0f, getContentHeight() - viewHeight);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float deltaTicks) {
        long now = System.nanoTime();
        float dt = Math.min(0.05f, (now - lastFrameNs) / 1_000_000_000.0f);
        lastFrameNs = now;

        float openProgress = openAnim.update(dt);
        if (closing && openAnim.isFinished()) {
            LiveStreamManager.get().saveConfig();
            BanglaClient.getInstance().getConfigManager().saveAll();
            if (this.client != null) this.client.setScreen(parentScreen);
            return;
        }

        ThemeTokens t = ThemeManager.get().tokens();
        expandAnim.animateTo(selectedCategory != null ? 1.0f : 0.0f);
        float expand = expandAnim.update(dt);
        float switchAnim = panelSwitchAnim.update(dt);

        ctx.fill(0, 0, this.width, this.height, UIRenderer.withAlpha(t.bgOverlay(), (int) (175 * openProgress)));

        float respScale = getResponsiveScale();
        float animScale = respScale * (0.95f + 0.05f * openProgress);
        float dmx = toDesignX(mouseX);
        float dmy = toDesignY(mouseY);

        Matrix3x2fStack matrices = ctx.getMatrices();
        matrices.pushMatrix();
        matrices.translate(this.width * 0.5f, this.height * 0.5f);
        matrices.scale(animScale, animScale);
        matrices.translate(-this.width * 0.5f, -this.height * 0.5f);

        float centerX = this.width * 0.5f;
        float centerY = this.height * 0.5f;

        String title = "L I V E S T R E A M";
        int titleW = this.textRenderer.getWidth(title);
        UIRenderer.drawText(ctx, title, centerX - titleW * 0.5f, centerY - 125 - expand * 32,
            UIRenderer.withAlpha(t.textPrimary(), (int) (255 * openProgress)), 1.0f, false, true);

        Category[] cats = Category.values();
        float cardW = 68.0f;
        float cardH = 74.0f;
        float gap = 10.0f;
        float totalRowW = cats.length * cardW + (cats.length - 1) * gap;
        float startX = centerX - totalRowW * 0.5f;
        float cardY = centerY - cardH * 0.5f;

        int selIndex = selectedCategory != null ? selectedCategory.ordinal() : -1;

        for (int i = 0; i < cats.length; i++) {
            float baseCx = startX + i * (cardW + gap);
            float targetCx = baseCx;
            if (selectedCategory != null) {
                if (i == selIndex) {
                    targetCx = centerX - cardW * 0.5f;
                } else {
                    int remIdx = (i < selIndex) ? i : (i - 1);
                    int slot = (remIdx < 2) ? remIdx : (remIdx + 1);
                    targetCx = startX + slot * (cardW + gap) + (remIdx < 2 ? -108.0f : 108.0f);
                }
            }

            if (!cardPositionsInitialized) {
                cardXAnims[i] = new Animation(targetCx, 0.24f, Animation.Easing.EASE_OUT_QUART);
            } else {
                cardXAnims[i].animateTo(targetCx);
            }
            float cx = cardXAnims[i].update(dt);

            boolean hovered = dmx >= cx && dmx <= cx + cardW && dmy >= cardY && dmy <= cardY + cardH;
            cardHoverAnims[i].animateTo(hovered ? 1.0f : 0.0f);
            float hv = cardHoverAnims[i].update(dt);

            if (selectedCategory == cats[i] && expand > 0.20f) {
                continue;
            }

            float grow = 4.0f * hv;
            UIRenderer.drawGlassPanel(
                ctx,
                cx - grow * 0.5f, cardY - grow * 0.5f,
                cardW + grow, cardH + grow,
                10.0f,
                t.bgGlass(),
                t.borderBase(),
                hv, dmx, dmy
            );

            int iconColor = UIRenderer.lerpColor(t.textSecondary(), t.accentActive(), hv);
            IconRenderer.drawIcon(ctx, cats[i].icon(), cx + (cardW - 22) * 0.5f, cardY + 14, 22, iconColor);

            String label = cats[i].title();
            int lw = this.textRenderer.getWidth(label);
            UIRenderer.drawText(ctx, label, cx + (cardW - lw) * 0.5f, cardY + cardH - 22,
                t.textPrimary(), 1.0f, false, false);
            UIRenderer.drawRoundedRect(ctx, cx + cardW * 0.34f, cardY + cardH - 10, cardW * 0.32f, 1.5f, 1.0f,
                UIRenderer.lerpColor(t.textMuted(), t.accentPrimary(), hv));
        }
        cardPositionsInitialized = true;

        float dockY = centerY + 54 + expand * 74;
        drawDockButton(ctx, centerX - 105, dockY, 100, 22, "Stream Connect", dmx, dmy);
        drawDockButton(ctx, centerX + 5, dockY, 100, 22, "Edit HUD", dmx, dmy);

        if (expand > 0.01f && selectedCategory != null) {
            float effectiveAnim = expand * (0.85f + 0.15f * switchAnim);
            float panelW = 236.0f;
            float panelH = 236.0f * effectiveAnim;
            float px = centerX - panelW * 0.5f;
            float py = centerY - panelH * 0.5f + (1.0f - switchAnim) * 8.0f;

            UIRenderer.drawGlassPanel(ctx, px, py, panelW, panelH, 13.0f,
                t.bgGlassStrong(), t.borderActive(), 0.4f, dmx, dmy);

            int scX1 = Math.round(centerX + (px - centerX) * animScale);
            int scY1 = Math.round(centerY + (py - centerY) * animScale);
            int scX2 = Math.round(centerX + (px + panelW - centerX) * animScale);
            int scY2 = Math.round(centerY + (py + panelH - centerY) * animScale);
            ctx.enableScissor(scX1, scY1, scX2, scY2);

            IconRenderer.drawIcon(ctx, selectedCategory.icon(), centerX - 5, py + 7, 10, t.accentPrimary());
            String catTitle = selectedCategory.title();
            int ctw = this.textRenderer.getWidth(catTitle);
            UIRenderer.drawText(ctx, catTitle, centerX - ctw * 0.5f, py + 19, t.textPrimary(), 1.0f, false, false);

            float btnX = px + 10;
            float btnY = py + 33;
            float btnW = panelW - 20;
            float btnH = 18;
            boolean editHover = dmx >= btnX && dmx <= btnX + btnW && dmy >= btnY && dmy <= btnY + btnH;
            UIRenderer.drawRoundedRect(ctx, btnX, btnY, btnW, btnH, 7.0f,
                editHover ? t.bgGlassStrong() : t.bgElevated());
            UIRenderer.drawRoundedBorder(ctx, btnX, btnY, btnW, btnH, 7.0f, 1.0f,
                editHover ? t.borderActive() : t.borderBase());
            String topBtnText = "Edit HUD Layout";
            UIRenderer.drawText(ctx, topBtnText, centerX - this.textRenderer.getWidth(topBtnText) * 0.5f,
                btnY + 5, t.textPrimary(), 1.0f, false, false);

            float listTop = py + 56;
            float listBottom = py + panelH - 10;
            float viewH = Math.max(10.0f, listBottom - listTop);
            float maxScroll = getMaxScroll(viewH);
            scrollOffset = Math.max(0.0f, Math.min(maxScroll, scrollOffset));

            float rowWidth = maxScroll > 0.0f ? (panelW - 28.0f) : (panelW - 20.0f);
            int lScX1 = Math.round(centerX + (px + 6 - centerX) * animScale);
            int lScY1 = Math.round(centerY + (listTop - centerY) * animScale);
            int lScX2 = Math.round(centerX + (px + panelW - 6 - centerX) * animScale);
            int lScY2 = Math.round(centerY + (listBottom - centerY) * animScale);
            ctx.enableScissor(lScX1, lScY1, lScX2, lScY2);

            float rowY = listTop - scrollOffset;
            for (GlassSlider slider : activeSliders) {
                slider.render(ctx, px + 10, rowY, rowWidth, 24, dmx, dmy, dt);
                rowY += 28;
            }
            for (GlassToggle toggle : activeToggles) {
                toggle.render(ctx, px + 10, rowY, rowWidth, 22, dmx, dmy, dt);
                rowY += 26;
            }
            ctx.disableScissor();

            if (maxScroll > 0.0f) {
                float sbX = px + panelW - 11.0f;
                float sbW = 4.5f;
                UIRenderer.drawRoundedRect(ctx, sbX, listTop, sbW, viewH, 2.25f, 0x6608090C);
                float contentH = getContentHeight();
                float thumbH = Math.max(22.0f, viewH * (viewH / Math.max(viewH, contentH)));
                float thumbY = listTop + (viewH - thumbH) * (scrollOffset / maxScroll);
                boolean sbHover = draggingScrollbar || (dmx >= sbX - 3 && dmx <= sbX + sbW + 3 && dmy >= listTop && dmy <= listBottom);
                UIRenderer.drawRoundedRect(ctx, sbX, thumbY, sbW, thumbH, 2.25f,
                    sbHover ? t.accentActive() : t.accentPrimary());
            }

            ctx.disableScissor();
        }

        matrices.popMatrix();
    }

    private void drawDockButton(DrawContext ctx, float x, float y, float w, float h, String label, float dmx, float dmy) {
        ThemeTokens t = ThemeManager.get().tokens();
        boolean hover = dmx >= x && dmx <= x + w && dmy >= y && dmy <= y + h;
        UIRenderer.drawGlassPanel(ctx, x, y, w, h, 8.0f,
            hover ? t.bgGlassStrong() : t.bgGlass(), hover ? t.borderActive() : t.borderBase(),
            hover ? 1.0f : 0.0f, dmx, dmy);
        int lw = this.textRenderer.getWidth(label);
        UIRenderer.drawText(ctx, label, x + (w - lw) * 0.5f, y + (h - 8) * 0.5f, t.textPrimary(), 1.0f, false, false);
    }

    private void updateScrollFromMouse(double designMouseY) {
        float panelH = 236.0f;
        float py = this.height * 0.5f - panelH * 0.5f;
        float listTop = py + 56.0f;
        float listBottom = py + panelH - 10.0f;
        float viewH = Math.max(10.0f, listBottom - listTop);
        float maxScroll = getMaxScroll(viewH);
        if (maxScroll <= 0.0f) return;

        float contentH = getContentHeight();
        float thumbH = Math.max(22.0f, viewH * (viewH / Math.max(viewH, contentH)));
        float trackTravel = Math.max(1.0f, viewH - thumbH);
        float ratio = ((float) designMouseY - listTop - thumbH * 0.5f) / trackTravel;
        scrollOffset = Math.max(0.0f, Math.min(maxScroll, ratio * maxScroll));
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = toDesignX(click.x());
        double my = toDesignY(click.y());
        int button = click.button();

        float centerX = this.width * 0.5f;
        float centerY = this.height * 0.5f;
        float expand = expandAnim.value();
        float dockY = centerY + 54 + expand * 74;

        if (button == 0 && my >= dockY && my <= dockY + 22) {
            if (mx >= centerX - 105 && mx <= centerX - 5) {
                if (this.client != null) this.client.setScreen(new WorkspaceModalScreen(this));
                return true;
            }
            if (mx >= centerX + 5 && mx <= centerX + 105) {
                if (this.client != null) this.client.setScreen(new HudEditorScreen(this));
                return true;
            }
        }

        Category[] cats = Category.values();
        float cardW = 68.0f;
        float cardH = 74.0f;
        float cardY = centerY - cardH * 0.5f;

        if (selectedCategory != null) {
            float panelW = 236.0f;
            float panelH = 236.0f;
            float px = centerX - panelW * 0.5f;
            float py = centerY - panelH * 0.5f;

            if (button == 0 && mx >= px + 10 && mx <= px + panelW - 10 && my >= py + 33 && my <= py + 51) {
                if (this.client != null) {
                    this.client.setScreen(new HudEditorScreen(this));
                }
                return true;
            }

            float listTop = py + 56;
            float listBottom = py + panelH - 10;
            float viewH = Math.max(10.0f, listBottom - listTop);
            float maxScroll = getMaxScroll(viewH);

            if (button == 0 && maxScroll > 0.0f && mx >= px + panelW - 15.0f && mx <= px + panelW - 4.0f
                && my >= listTop && my <= listBottom) {
                draggingScrollbar = true;
                updateScrollFromMouse(my);
                return true;
            }

            float rowWidth = maxScroll > 0.0f ? (panelW - 28.0f) : (panelW - 20.0f);
            if (my >= listTop && my <= listBottom) {
                float rowY = listTop - scrollOffset;
                for (GlassSlider slider : activeSliders) {
                    if (slider.mouseClicked(mx, my, button, px + 10, rowY, rowWidth, 24)) {
                        return true;
                    }
                    rowY += 28;
                }
                for (GlassToggle toggle : activeToggles) {
                    if (toggle.mouseClicked(mx, my, button, px + 10, rowY, rowWidth, 22)) {
                        return true;
                    }
                    rowY += 26;
                }
            }

            if (mx < px || mx > px + panelW || my < py || my > py + panelH) {
                for (int i = 0; i < cats.length; i++) {
                    if (cats[i] == selectedCategory) continue;
                    float cx = cardXAnims[i].value();
                    if (button == 0 && mx >= cx && mx <= cx + cardW && my >= cardY && my <= cardY + cardH) {
                        if (cats[i] == Category.STREAM) {
                            if (this.client != null) this.client.setScreen(new WorkspaceModalScreen(this));
                            return true;
                        }
                        selectCategoryWithAnimation(cats[i]);
                        return true;
                    }
                }
                selectedCategory = null;
                return true;
            }
        } else {
            for (int i = 0; i < cats.length; i++) {
                float cx = cardXAnims[i].value();
                if (button == 0 && mx >= cx && mx <= cx + cardW && my >= cardY && my <= cardY + cardH) {
                    if (cats[i] == Category.STREAM) {
                        if (this.client != null) this.client.setScreen(new WorkspaceModalScreen(this));
                        return true;
                    }
                    selectCategoryWithAnimation(cats[i]);
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        double dmx = toDesignX(click.x());
        double dmy = toDesignY(click.y());
        if (draggingScrollbar) {
            updateScrollFromMouse(dmy);
            return true;
        }
        if (selectedCategory != null) {
            float panelW = 236.0f;
            float px = this.width * 0.5f - panelW * 0.5f;
            float viewH = 170.0f;
            float rowWidth = getMaxScroll(viewH) > 0.0f ? (panelW - 28.0f) : (panelW - 20.0f);
            for (GlassSlider slider : activeSliders) {
                if (slider.mouseDragged(dmx, px + 10, rowWidth)) {
                    return true;
                }
            }
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        draggingScrollbar = false;
        for (GlassSlider slider : activeSliders) {
            slider.mouseReleased();
        }
        LiveStreamManager.get().saveConfig();
        BanglaClient.getInstance().getConfigManager().saveAll();
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (selectedCategory != null) {
            float maxScroll = getMaxScroll(170.0f);
            scrollOffset = Math.max(0.0f, Math.min(maxScroll, scrollOffset - (float) verticalAmount * 18.0f));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            if (selectedCategory != null) {
                selectedCategory = null;
                return true;
            }
            this.closing = true;
            this.openAnim.animateTo(0.0f);
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void removed() {
        LiveStreamManager.get().saveConfig();
        BanglaClient.getInstance().getConfigManager().saveAll();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
