package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.cosmetics.PlayerPreviewRenderer;
import com.ahmed.banglaclient.cosmetics.SkinCapeManager;
import com.ahmed.banglaclient.cosmetics.SkinEntry;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.List;

public final class SkinCustomizationScreen extends Screen {
    private final Screen parentScreen;
    private final SkinCapeManager manager;
    private final Animation openAnim = new Animation(0.0f, 170L, Easing.EASE_OUT_CUBIC);
    private final Animation carouselSlideAnim = new Animation(0.0f, 150L, Easing.EASE_OUT_CUBIC);
    private boolean closing = false;

    private PlayerSkinWidget capePreviewWidget;
    private boolean viewingCapesPage = false;
    private int skinCarouselIndex = 0;
    private int capeCarouselIndex = 0;

    private int winX, winY, winW, winH;

    public SkinCustomizationScreen(Screen parentScreen, SkinCapeManager manager) {
        super(Text.literal("Wardrobe"));
        this.parentScreen = parentScreen;
        this.manager = manager;
    }

    @Override
    protected void init() {
        this.winW = Math.min(430, Math.max(270, (int) (this.width * 0.74f)));
        this.winH = Math.min(245, Math.max(175, (int) (this.height * 0.76f)));
        this.winX = (this.width - this.winW) / 2;
        this.winY = (this.height - this.winH) / 2;
        this.closing = false;
        this.openAnim.snapTo(0.0f);
        this.openAnim.animateTo(1.0f);

        syncCarouselIndicesWithActiveSelection();

        this.capePreviewWidget = PlayerPreviewRenderer.createSkinWidget(
                96,
                114,
                () -> manager.getActiveSkinTextures()
        );
        this.capePreviewWidget.setPosition(winX + (winW - 96) / 2, winY + 34);
        this.addDrawableChild(this.capePreviewWidget);
    }

    private void syncCarouselIndicesWithActiveSelection() {
        List<SkinEntry> skins = manager.getSkins();
        SkinEntry selSkin = manager.getSelectedSkin();
        skinCarouselIndex = 0;
        if (selSkin != null) {
            for (int i = 0; i < skins.size(); i++) {
                if (skins.get(i).getId().equals(selSkin.getId())) {
                    skinCarouselIndex = i + 1;
                    break;
                }
            }
        }

        List<SkinEntry> capes = manager.getCapes();
        SkinEntry selCape = manager.getSelectedCape();
        capeCarouselIndex = 0;
        if (selCape != null) {
            for (int i = 0; i < capes.size(); i++) {
                if (capes.get(i).getId().equals(selCape.getId())) {
                    capeCarouselIndex = i + 1;
                    break;
                }
            }
        }
    }

    @Override
    public void onFilesDropped(List<Path> paths) {
        if (paths == null || paths.isEmpty() || manager == null) return;
        for (Path p : paths) {
            manager.importExternalFile(p, viewingCapesPage ? Boolean.TRUE : Boolean.FALSE);
        }
        GlassRenderer.playClickSound();
        syncCarouselIndicesWithActiveSelection();
    }

    private void drawFullLayered2DSkinModel(DrawContext context, Identifier skinTex, int centerX, int topY, float modelScale) {
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(centerX, topY);
        matrices.scale(modelScale, modelScale);
        try {
            // Head + Hat Outer Layer
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTex, -8, 0, 8.0f, 8.0f, 16, 16, 64, 64);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTex, -9, -1, 40.0f, 8.0f, 18, 18, 64, 64);
            // Body + Jacket Outer Layer
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTex, -8, 16, 20.0f, 20.0f, 16, 24, 64, 64);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTex, -8, 16, 20.0f, 36.0f, 16, 24, 64, 64);
            // Right Arm + Sleeve Outer Layer
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTex, -16, 16, 44.0f, 20.0f, 8, 24, 64, 64);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTex, -16, 16, 44.0f, 36.0f, 8, 24, 64, 64);
            // Left Arm + Sleeve Outer Layer
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTex, 8, 16, 36.0f, 52.0f, 8, 24, 64, 64);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTex, 8, 16, 52.0f, 52.0f, 8, 24, 64, 64);
            // Right Leg + Pants Outer Layer
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTex, -8, 40, 4.0f, 20.0f, 8, 24, 64, 64);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTex, -8, 40, 4.0f, 36.0f, 8, 24, 64, 64);
            // Left Leg + Pants Outer Layer
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTex, 0, 40, 20.0f, 52.0f, 8, 24, 64, 64);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTex, 0, 40, 4.0f, 52.0f, 8, 24, 64, 64);
        } catch (Throwable ignored) {}
        matrices.popMatrix();
    }

    private Identifier getSkinTextureAt(int index) {
        List<SkinEntry> skins = manager.getSkins();
        if (index <= 0 || index > skins.size()) {
            return SkinCapeManager.getAuthenticatedAccountDefaultSkin(client).body().texturePath();
        }
        return skins.get(index - 1).getTextureId();
    }

    private String getSkinTitleAt(int index) {
        List<SkinEntry> skins = manager.getSkins();
        if (index <= 0 || index > skins.size()) {
            return "Default Account Skin";
        }
        return skins.get(index - 1).getDisplayName();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float anim = openAnim.getValue();
        if (closing && anim <= 0.02f) {
            manager.saveSelection();
            if (this.client != null) this.client.setScreen(parentScreen);
            return;
        }

        if (this.capePreviewWidget != null) {
            this.capePreviewWidget.visible = viewingCapesPage;
            this.capePreviewWidget.active = viewingCapesPage;
        }

        BlurRenderer.renderScreenBackdrop(context, this.width, this.height, anim);
        Theme theme = GlassRenderer.getTheme();

        Matrix3x2fStack matrices = context.getMatrices();
        float scale = 0.92f + 0.08f * anim;
        matrices.pushMatrix();
        matrices.translate(this.width * 0.5f, this.height * 0.5f);
        matrices.scale(scale, scale);
        matrices.translate(-this.width * 0.5f, -this.height * 0.5f);

        try {
            GlassRenderer.drawGlassPanel(context, winX, winY, winW, winH, false, false, true);

            boolean backHov = MathUtils.isHovered(mouseX, mouseY, winX + 6, winY + 6, 46, 16);
            GlassRenderer.drawPillBadge(context, winX + 6, winY + 6, 46, 16, 0xFFFFFFFF, backHov);
            GlassRenderer.drawSmallCapsText(context, textRenderer, "← Back", winX + 12, winY + 10, 0xFFFFFFFF);

            int tabW = 68;
            int skinsTabX = winX + (winW / 2) - tabW - 3;
            int capesTabX = winX + (winW / 2) + 3;

            GlassRenderer.drawPillBadge(context, skinsTabX, winY + 6, tabW, 16, 0xFFFFFFFF, !viewingCapesPage);
            GlassRenderer.drawSmallCapsText(context, textRenderer, "Skins (" + manager.getSkins().size() + ")", skinsTabX + 10, winY + 10, 0xFFFFFFFF);

            GlassRenderer.drawPillBadge(context, capesTabX, winY + 6, tabW, 16, 0xFFFFFFFF, viewingCapesPage);
            GlassRenderer.drawSmallCapsText(context, textRenderer, "Capes (" + manager.getCapes().size() + ")", capesTabX + 10, winY + 10, 0xFFFFFFFF);

            int folderX = winX + winW - 54;
            boolean folHov = MathUtils.isHovered(mouseX, mouseY, folderX, winY + 6, 48, 16);
            GlassRenderer.drawPillBadge(context, folderX, winY + 6, 48, 16, 0xFFFFFFFF, folHov);
            GlassRenderer.drawSmallCapsText(context, textRenderer, "Folder", folderX + 9, winY + 10, 0xFFFFFFFF);

            GlassRenderer.drawHorizontalDivider(context, winX + 6, winY + 26, winW - 12);

            if (!viewingCapesPage) {
                renderSkinsCarouselPage(context, mouseX, mouseY, anim, theme);
            } else {
                renderCapesPage(context, mouseX, mouseY, delta, anim, theme);
            }
        } finally {
            matrices.popMatrix();
        }
    }

    private void renderSkinsCarouselPage(DrawContext context, int mouseX, int mouseY, float anim, Theme theme) {
        int totalSkins = manager.getSkins().size() + 1;
        skinCarouselIndex = MathUtils.clamp(skinCarouselIndex, 0, totalSkins - 1);

        int centerX = winX + winW / 2;
        int stageY = winY + 34;
        int stageH = winH - 72;

        float slideOffset = carouselSlideAnim.getValue();

        if (totalSkins > 1) {
            int prevIdx = (skinCarouselIndex - 1 + totalSkins) % totalSkins;
            GlassRenderer.drawGlassPanel(context, centerX - 128, stageY + 18, 52, stageH - 32, false, false, false);
            drawFullLayered2DSkinModel(context, getSkinTextureAt(prevIdx), centerX - 102, stageY + 26, 1.0f * anim);

            int nextIdx = (skinCarouselIndex + 1) % totalSkins;
            GlassRenderer.drawGlassPanel(context, centerX + 76, stageY + 18, 52, stageH - 32, false, false, false);
            drawFullLayered2DSkinModel(context, getSkinTextureAt(nextIdx), centerX + 102, stageY + 26, 1.0f * anim);
        }

        int cardW = 104;
        int cardX = centerX - cardW / 2 + Math.round(slideOffset * 14.0f);
        GlassRenderer.drawGlassPanel(context, cardX, stageY, cardW, stageH, true, false, true);

        Identifier currentTex = getSkinTextureAt(skinCarouselIndex);
        drawFullLayered2DSkinModel(context, currentTex, cardX + cardW / 2, stageY + 10, 1.65f * anim);

        String skinName = GlassRenderer.toSmallCaps(getSkinTitleAt(skinCarouselIndex));
        String trimmedName = textRenderer.trimToWidth(skinName, cardW - 10);
        context.drawTextWithShadow(textRenderer, trimmedName, cardX + (cardW - textRenderer.getWidth(trimmedName)) / 2, stageY + stageH - 14, 0xFFFFFFFF);

        int arrowSize = 24;
        int arrowY = stageY + (stageH - arrowSize) / 2;
        int leftArrowX = centerX - 66 - arrowSize;
        int rightArrowX = centerX + 66;

        boolean lHov = MathUtils.isHovered(mouseX, mouseY, leftArrowX, arrowY, arrowSize, arrowSize);
        GlassRenderer.drawGlassPanel(context, leftArrowX, arrowY, arrowSize, arrowSize, true, lHov, lHov);
        context.drawTextWithShadow(textRenderer, "◀", leftArrowX + 9, arrowY + 8, 0xFFFFFFFF);

        boolean rHov = MathUtils.isHovered(mouseX, mouseY, rightArrowX, arrowY, arrowSize, arrowSize);
        GlassRenderer.drawGlassPanel(context, rightArrowX, arrowY, arrowSize, arrowSize, true, rHov, rHov);
        context.drawTextWithShadow(textRenderer, "▶", rightArrowX + 9, arrowY + 8, 0xFFFFFFFF);

        int botY = winY + winH - 26;
        int btnW = (winW - 30) / 4;

        String armLbl = manager.isSelectedSlimModel() ? "Model: Slim" : "Model: Classic";
        boolean b0 = MathUtils.isHovered(mouseX, mouseY, winX + 6, botY, btnW, 18);
        GlassRenderer.drawPillBadge(context, winX + 6, botY, btnW, 18, 0xFFFFFFFF, b0);
        GlassRenderer.drawSmallCapsText(context, textRenderer, armLbl, winX + 6 + (btnW - textRenderer.getWidth(armLbl)) / 2, botY + 5, 0xFFFFFFFF);

        int bx1 = winX + 12 + btnW;
        boolean b1 = MathUtils.isHovered(mouseX, mouseY, bx1, botY, btnW, 18);
        GlassRenderer.drawPillBadge(context, bx1, botY, btnW, 18, 0xFFFFFFFF, true);
        GlassRenderer.drawSmallCapsText(context, textRenderer, "Select Skin", bx1 + (btnW - textRenderer.getWidth("Select Skin")) / 2, botY + 5, b1 ? 0xFFFFFFFF : 0xFFE2E8F0);

        int bx2 = winX + 18 + btnW * 2;
        String ovLbl = manager.isCustomSkinOverride() ? "Skin: ON" : "Skin: OFF";
        GlassRenderer.drawPillBadge(context, bx2, botY, btnW, 18, 0xFFFFFFFF, manager.isCustomSkinOverride());
        GlassRenderer.drawSmallCapsText(context, textRenderer, ovLbl, bx2 + (btnW - textRenderer.getWidth(ovLbl)) / 2, botY + 5, 0xFFFFFFFF);

        int bx3 = winX + 24 + btnW * 3;
        boolean b3 = MathUtils.isHovered(mouseX, mouseY, bx3, botY, btnW, 18);
        GlassRenderer.drawPillBadge(context, bx3, botY, btnW, 18, ThemeTokens.STATUS_ERROR, b3);
        GlassRenderer.drawSmallCapsText(context, textRenderer, "Delete", bx3 + (btnW - textRenderer.getWidth("Delete")) / 2, botY + 5, 0xFFFFFFFF);
    }

    private void renderCapesPage(DrawContext context, int mouseX, int mouseY, float delta, float anim, Theme theme) {
        int centerX = winX + winW / 2;
        int stageY = winY + 30;
        int stageH = 122;

        int boxW = 116;
        int boxX = centerX - boxW / 2;
        GlassRenderer.drawGlassPanel(context, boxX, stageY, boxW, stageH, true, false, true);

        if (this.capePreviewWidget != null) {
            int scaledW = Math.max(20, Math.round(96 * anim));
            int scaledH = Math.max(20, Math.round(112 * anim));
            this.capePreviewWidget.setWidth(scaledW);
            this.capePreviewWidget.setHeight(scaledH);
            this.capePreviewWidget.setPosition(centerX - scaledW / 2, stageY + (stageH - scaledH) / 2);
            this.capePreviewWidget.render(context, mouseX, mouseY, delta);
        }

        int stripY = stageY + stageH + 6;
        int stripH = 54;
        List<SkinEntry> capes = manager.getCapes();
        int totalCapes = capes.size() + 1;
        capeCarouselIndex = MathUtils.clamp(capeCarouselIndex, 0, totalCapes - 1);

        int cardW = 46;
        int gap = 6;
        int maxVisible = Math.min(totalCapes, Math.max(3, (winW - 60) / (cardW + gap)));
        int stripTotalW = maxVisible * cardW + (maxVisible - 1) * gap;
        int startX = centerX - stripTotalW / 2;
        int startIdx = Math.max(0, Math.min(capeCarouselIndex - maxVisible / 2, totalCapes - maxVisible));

        for (int v = 0; v < maxVisible; v++) {
            int idx = startIdx + v;
            if (idx >= totalCapes) break;
            int cx = startX + v * (cardW + gap);
            boolean active = (idx == 0 && !manager.hasCustomCapeSelected())
                    || (idx > 0 && manager.getSelectedCape() != null && manager.getSelectedCape().getId().equals(capes.get(idx - 1).getId()));
            boolean hov = MathUtils.isHovered(mouseX, mouseY, cx, stripY, cardW, stripH);

            GlassRenderer.drawGlassPanel(context, cx, stripY, cardW, stripH, true, hov, active);
            if (idx == 0) {
                GlassRenderer.drawSmallCapsText(context, textRenderer, "None", cx + 10, stripY + 22, active ? 0xFFFFFFFF : theme.getTextSecondary());
            } else {
                SkinEntry cape = capes.get(idx - 1);
                try {
                    context.drawTexture(RenderPipelines.GUI_TEXTURED, cape.getTextureId(), cx + (cardW - 22) / 2, stripY + 4, 1.0f, 1.0f, 22, 34, 64, 32);
                } catch (Throwable ignored) {}
                String nm = textRenderer.trimToWidth(GlassRenderer.toSmallCaps(cape.getDisplayName()), cardW - 6);
                context.drawTextWithShadow(textRenderer, nm, cx + (cardW - textRenderer.getWidth(nm)) / 2, stripY + stripH - 11, 0xFFFFFFFF);
            }
        }

        boolean lHov = MathUtils.isHovered(mouseX, mouseY, winX + 8, stripY + 15, 20, 22);
        GlassRenderer.drawGlassPanel(context, winX + 8, stripY + 15, 20, 22, true, lHov, lHov);
        context.drawTextWithShadow(textRenderer, "◀", winX + 15, stripY + 22, 0xFFFFFFFF);

        boolean rHov = MathUtils.isHovered(mouseX, mouseY, winX + winW - 28, stripY + 15, 20, 22);
        GlassRenderer.drawGlassPanel(context, winX + winW - 28, stripY + 15, 20, 22, true, rHov, rHov);
        context.drawTextWithShadow(textRenderer, "▶", winX + winW - 21, stripY + 22, 0xFFFFFFFF);
    }

    private void applySkinAtCarouselIndex(int index) {
        List<SkinEntry> skins = manager.getSkins();
        if (index <= 0 || index > skins.size()) {
            manager.setSelectedSkinId("");
        } else {
            manager.setSelectedSkinId(skins.get(index - 1).getId());
            manager.setCustomSkinOverride(true);
        }
        GlassRenderer.playClickSound();
    }

    private void applyCapeAtCarouselIndex(int index) {
        List<SkinEntry> capes = manager.getCapes();
        if (index <= 0 || index > capes.size()) {
            manager.setSelectedCapeId("");
        } else {
            manager.setSelectedCapeId(capes.get(index - 1).getId());
            manager.setCustomCapeOverride(true);
        }
        GlassRenderer.playClickSound();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x(), mouseY = click.y();
        if (click.button() != 0 || closing) return super.mouseClicked(click, doubled);

        if (MathUtils.isHovered(mouseX, mouseY, winX + 6, winY + 6, 46, 16)) {
            GlassRenderer.playClickSound();
            close();
            return true;
        }

        int tabW = 68;
        int skinsTabX = winX + (winW / 2) - tabW - 3;
        int capesTabX = winX + (winW / 2) + 3;
        if (MathUtils.isHovered(mouseX, mouseY, skinsTabX, winY + 6, tabW, 16)) {
            viewingCapesPage = false;
            GlassRenderer.playClickSound();
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, capesTabX, winY + 6, tabW, 16)) {
            viewingCapesPage = true;
            GlassRenderer.playClickSound();
            return true;
        }

        int folderX = winX + winW - 54;
        if (MathUtils.isHovered(mouseX, mouseY, folderX, winY + 6, 48, 16)) {
            GlassRenderer.playClickSound();
            manager.refreshLibraries();
            Path folder = viewingCapesPage ? manager.getCapesDir() : manager.getSkinsDir();
            try { Util.getOperatingSystem().open(folder.toFile()); } catch (Throwable ignored) {}
            return true;
        }

        int centerX = winX + winW / 2;

        if (!viewingCapesPage) {
            int totalSkins = manager.getSkins().size() + 1;
            int stageY = winY + 34;
            int stageH = winH - 72;
            int arrowSize = 24;
            int arrowY = stageY + (stageH - arrowSize) / 2;
            int leftArrowX = centerX - 66 - arrowSize;
            int rightArrowX = centerX + 66;

            if (MathUtils.isHovered(mouseX, mouseY, leftArrowX, arrowY, arrowSize, arrowSize)) {
                skinCarouselIndex = (skinCarouselIndex - 1 + totalSkins) % totalSkins;
                carouselSlideAnim.snapTo(-1.0f);
                carouselSlideAnim.animateTo(0.0f);
                applySkinAtCarouselIndex(skinCarouselIndex);
                return true;
            }
            if (MathUtils.isHovered(mouseX, mouseY, rightArrowX, arrowY, arrowSize, arrowSize)) {
                skinCarouselIndex = (skinCarouselIndex + 1) % totalSkins;
                carouselSlideAnim.snapTo(1.0f);
                carouselSlideAnim.animateTo(0.0f);
                applySkinAtCarouselIndex(skinCarouselIndex);
                return true;
            }

            int botY = winY + winH - 26;
            int btnW = (winW - 30) / 4;
            if (MathUtils.isHovered(mouseX, mouseY, winX + 6, botY, btnW, 18)) {
                manager.setSelectedSlimModel(!manager.isSelectedSlimModel());
                GlassRenderer.playClickSound();
                return true;
            }
            if (MathUtils.isHovered(mouseX, mouseY, winX + 12 + btnW, botY, btnW, 18)) {
                applySkinAtCarouselIndex(skinCarouselIndex);
                return true;
            }
            if (MathUtils.isHovered(mouseX, mouseY, winX + 18 + btnW * 2, botY, btnW, 18)) {
                manager.setCustomSkinOverride(!manager.isCustomSkinOverride());
                GlassRenderer.playClickSound();
                return true;
            }
            if (MathUtils.isHovered(mouseX, mouseY, winX + 24 + btnW * 3, botY, btnW, 18)) {
                if (skinCarouselIndex > 0 && skinCarouselIndex <= manager.getSkins().size()) {
                    manager.removeEntry(manager.getSkins().get(skinCarouselIndex - 1));
                    skinCarouselIndex = 0;
                    GlassRenderer.playClickSound();
                }
                return true;
            }
        } else {
            int stageY = winY + 30;
            int stageH = 122;
            int stripY = stageY + stageH + 6;
            int stripH = 54;
            List<SkinEntry> capes = manager.getCapes();
            int totalCapes = capes.size() + 1;

            if (MathUtils.isHovered(mouseX, mouseY, winX + 8, stripY + 15, 20, 22)) {
                capeCarouselIndex = (capeCarouselIndex - 1 + totalCapes) % totalCapes;
                applyCapeAtCarouselIndex(capeCarouselIndex);
                return true;
            }
            if (MathUtils.isHovered(mouseX, mouseY, winX + winW - 28, stripY + 15, 20, 22)) {
                capeCarouselIndex = (capeCarouselIndex + 1) % totalCapes;
                applyCapeAtCarouselIndex(capeCarouselIndex);
                return true;
            }

            int cardW = 46;
            int gap = 6;
            int maxVisible = Math.min(totalCapes, Math.max(3, (winW - 60) / (cardW + gap)));
            int stripTotalW = maxVisible * cardW + (maxVisible - 1) * gap;
            int startX = centerX - stripTotalW / 2;
            int startIdx = Math.max(0, Math.min(capeCarouselIndex - maxVisible / 2, totalCapes - maxVisible));

            for (int v = 0; v < maxVisible; v++) {
                int idx = startIdx + v;
                if (idx >= totalCapes) break;
                int cx = startX + v * (cardW + gap);
                if (MathUtils.isHovered(mouseX, mouseY, cx, stripY, cardW, stripH)) {
                    capeCarouselIndex = idx;
                    applyCapeAtCarouselIndex(idx);
                    return true;
                }
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        if (!closing) {
            closing = true;
            openAnim.animateTo(0.0f);
        }
    }

    @Override
    public boolean shouldPause() { return false; }
}
