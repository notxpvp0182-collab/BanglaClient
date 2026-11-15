package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.cosmetics.PlayerPreviewRenderer;
import com.ahmed.banglaclient.cosmetics.SkinCapeManager;
import com.ahmed.banglaclient.cosmetics.SkinEntry;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.MouseInput;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.List;

/**
 * Wardrobe Screen with ONE Single Rotatable 3D Character Model in the Center:
 * - Skins Page: Browse uploaded skins with ◀ / ▶ on the 3D rotatable character model.
 * - Capes Page: Automatically rotates the 3D character to show the back so you see your Cape on the character's back, and browse capes with ◀ / ▶!
 */
public final class SkinCustomizationScreen extends Screen {
    private final Screen parentScreen;
    private final SkinCapeManager manager;
    private final Animation openAnim = new Animation(0.0f, 170L, Easing.EASE_OUT_CUBIC);
    private boolean closing = false;

    private PlayerSkinWidget center3DCharacterWidget;
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
        this.winW = Math.min(410, Math.max(260, (int) (this.width * 0.72f)));
        this.winH = Math.min(240, Math.max(175, (int) (this.height * 0.75f)));
        this.winX = (this.width - this.winW) / 2;
        this.winY = (this.height - this.winH) / 2;
        this.closing = false;
        this.openAnim.snapTo(0.0f);
        this.openAnim.animateTo(1.0f);

        syncCarouselIndicesWithActiveSelection();

        this.center3DCharacterWidget = PlayerPreviewRenderer.createSkinWidget(
                96,
                122,
                this::resolveLivePreviewSkinTextures
        );
        this.center3DCharacterWidget.setPosition(winX + (winW - 96) / 2, winY + 34);
        this.addDrawableChild(this.center3DCharacterWidget);
    }

    /**
     * Supplies the exact Skin + Cape textures for the currently browsed carousel item to the 3D character model!
     */
    private SkinTextures resolveLivePreviewSkinTextures() {
        SkinTextures vanillaDefault = SkinCapeManager.getAuthenticatedAccountDefaultSkin(client);
        List<SkinEntry> skins = manager.getSkins();
        List<SkinEntry> capes = manager.getCapes();

        SkinEntry previewSkin = (skinCarouselIndex > 0 && skinCarouselIndex <= skins.size())
                ? skins.get(skinCarouselIndex - 1)
                : null;
        SkinEntry previewCape = (capeCarouselIndex > 0 && capeCarouselIndex <= capes.size())
                ? capes.get(capeCarouselIndex - 1)
                : null;

        AssetInfo.TextureAsset body = (previewSkin != null)
                ? new AssetInfo.TextureAssetInfo(previewSkin.getTextureId(), previewSkin.getTextureId())
                : vanillaDefault.body();
        AssetInfo.TextureAsset capeAsset = (previewCape != null)
                ? new AssetInfo.TextureAssetInfo(previewCape.getTextureId(), previewCape.getTextureId())
                : vanillaDefault.cape();
        PlayerSkinType model = (previewSkin != null)
                ? (manager.isSelectedSlimModel() ? PlayerSkinType.SLIM : PlayerSkinType.WIDE)
                : vanillaDefault.model();

        return new SkinTextures(body, capeAsset, capeAsset != null ? capeAsset : vanillaDefault.elytra(), model, true);
    }

    private void rotateCharacterToShowBack(boolean showBack) {
        if (this.center3DCharacterWidget != null) {
            try {
                // Drag horizontally to turn the 3D model 180 degrees so the cape on the back faces the camera
                this.center3DCharacterWidget.mouseDragged(
                        new Click(center3DCharacterWidget.getX() + 20, center3DCharacterWidget.getY() + 20, new MouseInput(0, 0)),
                        showBack ? 95.0 : -95.0,
                        0.0
                );
            } catch (Throwable ignored) {}
        }
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

    private String getCurrentBrowsedTitle() {
        if (!viewingCapesPage) {
            List<SkinEntry> skins = manager.getSkins();
            if (skinCarouselIndex <= 0 || skinCarouselIndex > skins.size()) return "Default Skin";
            return skins.get(skinCarouselIndex - 1).getDisplayName();
        } else {
            List<SkinEntry> capes = manager.getCapes();
            if (capeCarouselIndex <= 0 || capeCarouselIndex > capes.size()) return "No Cape";
            return capes.get(capeCarouselIndex - 1).getDisplayName();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float anim = openAnim.getValue();
        if (closing && anim <= 0.02f) {
            manager.saveSelection();
            if (this.client != null) this.client.setScreen(parentScreen);
            return;
        }

        BlurRenderer.renderScreenBackdrop(context, this.width, this.height, anim);

        Matrix3x2fStack matrices = context.getMatrices();
        float scale = 0.92f + 0.08f * anim;
        matrices.pushMatrix();
        matrices.translate(this.width * 0.5f, this.height * 0.5f);
        matrices.scale(scale, scale);
        matrices.translate(-this.width * 0.5f, -this.height * 0.5f);

        try {
            GlassRenderer.drawGlassPanel(context, winX, winY, winW, winH, false, false, true);

            // Top Header: [← Back] | [SKINS] [CAPES] | [Folder]
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

            // Single Center Stage Box with Rotatable 3D Character Model (No duplicate side models!)
            int centerX = winX + winW / 2;
            int stageY = winY + 32;
            int stageW = 118;
            int stageH = winH - 66;
            int stageX = centerX - stageW / 2;

            GlassRenderer.drawGlassPanel(context, stageX, stageY, stageW, stageH, true, false, true);

            if (this.center3DCharacterWidget != null) {
                int w3d = Math.max(20, Math.round(92 * anim));
                int h3d = Math.max(20, Math.round((stageH - 24) * anim));
                this.center3DCharacterWidget.setWidth(w3d);
                this.center3DCharacterWidget.setHeight(h3d);
                this.center3DCharacterWidget.setPosition(centerX - w3d / 2, stageY + 4 + (stageH - 24 - h3d) / 2);
                this.center3DCharacterWidget.render(context, mouseX, mouseY, delta);
            }

            String itemTitle = GlassRenderer.toSmallCaps(getCurrentBrowsedTitle());
            String trimmedTitle = textRenderer.trimToWidth(itemTitle, stageW - 10);
            context.drawTextWithShadow(textRenderer, trimmedTitle, centerX - textRenderer.getWidth(trimmedTitle) / 2, stageY + stageH - 13, 0xFFFFFFFF);

            // Left [◀] and Right [▶] Carousel Arrows
            int arrowSize = 24;
            int arrowY = stageY + (stageH - arrowSize) / 2;
            int leftArrowX = stageX - arrowSize - 12;
            int rightArrowX = stageX + stageW + 12;

            boolean lHov = MathUtils.isHovered(mouseX, mouseY, leftArrowX, arrowY, arrowSize, arrowSize);
            GlassRenderer.drawGlassPanel(context, leftArrowX, arrowY, arrowSize, arrowSize, true, lHov, lHov);
            context.drawTextWithShadow(textRenderer, "◀", leftArrowX + 9, arrowY + 8, 0xFFFFFFFF);

            boolean rHov = MathUtils.isHovered(mouseX, mouseY, rightArrowX, arrowY, arrowSize, arrowSize);
            GlassRenderer.drawGlassPanel(context, rightArrowX, arrowY, arrowSize, arrowSize, true, rHov, rHov);
            context.drawTextWithShadow(textRenderer, "▶", rightArrowX + 9, arrowY + 8, 0xFFFFFFFF);

            // Bottom Action Bar
            int botY = winY + winH - 25;
            int btnW = (winW - 30) / 4;

            String armLbl = manager.isSelectedSlimModel() ? "Model: Slim" : "Model: Classic";
            boolean b0 = MathUtils.isHovered(mouseX, mouseY, winX + 6, botY, btnW, 18);
            GlassRenderer.drawPillBadge(context, winX + 6, botY, btnW, 18, 0xFFFFFFFF, b0);
            GlassRenderer.drawSmallCapsText(context, textRenderer, armLbl, winX + 6 + (btnW - textRenderer.getWidth(armLbl)) / 2, botY + 5, 0xFFFFFFFF);

            int bx1 = winX + 12 + btnW;
            boolean b1 = MathUtils.isHovered(mouseX, mouseY, bx1, botY, btnW, 18);
            String equipLbl = viewingCapesPage ? "Select Cape" : "Select Skin";
            GlassRenderer.drawPillBadge(context, bx1, botY, btnW, 18, 0xFFFFFFFF, true);
            GlassRenderer.drawSmallCapsText(context, textRenderer, equipLbl, bx1 + (btnW - textRenderer.getWidth(equipLbl)) / 2, botY + 5, b1 ? 0xFFFFFFFF : 0xFFE2E8F0);

            int bx2 = winX + 18 + btnW * 2;
            boolean isOv = viewingCapesPage ? manager.isCustomCapeOverride() : manager.isCustomSkinOverride();
            String ovLbl = (viewingCapesPage ? "Cape: " : "Skin: ") + (isOv ? "ON" : "OFF");
            GlassRenderer.drawPillBadge(context, bx2, botY, btnW, 18, 0xFFFFFFFF, isOv);
            GlassRenderer.drawSmallCapsText(context, textRenderer, ovLbl, bx2 + (btnW - textRenderer.getWidth(ovLbl)) / 2, botY + 5, 0xFFFFFFFF);

            int bx3 = winX + 24 + btnW * 3;
            boolean b3 = MathUtils.isHovered(mouseX, mouseY, bx3, botY, btnW, 18);
            GlassRenderer.drawPillBadge(context, bx3, botY, btnW, 18, ThemeTokens.STATUS_ERROR, b3);
            GlassRenderer.drawSmallCapsText(context, textRenderer, "Delete", bx3 + (btnW - textRenderer.getWidth("Delete")) / 2, botY + 5, 0xFFFFFFFF);
        } finally {
            matrices.popMatrix();
        }
    }

    private void applyCurrentSelection() {
        if (!viewingCapesPage) {
            List<SkinEntry> skins = manager.getSkins();
            if (skinCarouselIndex <= 0 || skinCarouselIndex > skins.size()) {
                manager.setSelectedSkinId("");
            } else {
                manager.setSelectedSkinId(skins.get(skinCarouselIndex - 1).getId());
                manager.setCustomSkinOverride(true);
            }
        } else {
            List<SkinEntry> capes = manager.getCapes();
            if (capeCarouselIndex <= 0 || capeCarouselIndex > capes.size()) {
                manager.setSelectedCapeId("");
            } else {
                manager.setSelectedCapeId(capes.get(capeCarouselIndex - 1).getId());
                manager.setCustomCapeOverride(true);
            }
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
            if (viewingCapesPage) {
                viewingCapesPage = false;
                rotateCharacterToShowBack(false);
            }
            GlassRenderer.playClickSound();
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, capesTabX, winY + 6, tabW, 16)) {
            if (!viewingCapesPage) {
                viewingCapesPage = true;
                rotateCharacterToShowBack(true);
            }
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
        int stageY = winY + 32;
        int stageW = 118;
        int stageH = winH - 66;
        int stageX = centerX - stageW / 2;
        int arrowSize = 24;
        int arrowY = stageY + (stageH - arrowSize) / 2;
        int leftArrowX = stageX - arrowSize - 12;
        int rightArrowX = stageX + stageW + 12;

        if (MathUtils.isHovered(mouseX, mouseY, leftArrowX, arrowY, arrowSize, arrowSize)) {
            if (!viewingCapesPage) {
                int total = manager.getSkins().size() + 1;
                skinCarouselIndex = (skinCarouselIndex - 1 + total) % total;
            } else {
                int total = manager.getCapes().size() + 1;
                capeCarouselIndex = (capeCarouselIndex - 1 + total) % total;
            }
            applyCurrentSelection();
            return true;
        }

        if (MathUtils.isHovered(mouseX, mouseY, rightArrowX, arrowY, arrowSize, arrowSize)) {
            if (!viewingCapesPage) {
                int total = manager.getSkins().size() + 1;
                skinCarouselIndex = (skinCarouselIndex + 1) % total;
            } else {
                int total = manager.getCapes().size() + 1;
                capeCarouselIndex = (capeCarouselIndex + 1) % total;
            }
            applyCurrentSelection();
            return true;
        }

        int botY = winY + winH - 25;
        int btnW = (winW - 30) / 4;
        if (MathUtils.isHovered(mouseX, mouseY, winX + 6, botY, btnW, 18)) {
            manager.setSelectedSlimModel(!manager.isSelectedSlimModel());
            GlassRenderer.playClickSound();
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, winX + 12 + btnW, botY, btnW, 18)) {
            applyCurrentSelection();
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, winX + 18 + btnW * 2, botY, btnW, 18)) {
            if (viewingCapesPage) manager.setCustomCapeOverride(!manager.isCustomCapeOverride());
            else manager.setCustomSkinOverride(!manager.isCustomSkinOverride());
            GlassRenderer.playClickSound();
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, winX + 24 + btnW * 3, botY, btnW, 18)) {
            if (!viewingCapesPage && skinCarouselIndex > 0 && skinCarouselIndex <= manager.getSkins().size()) {
                manager.removeEntry(manager.getSkins().get(skinCarouselIndex - 1));
                skinCarouselIndex = 0;
                GlassRenderer.playClickSound();
            } else if (viewingCapesPage && capeCarouselIndex > 0 && capeCarouselIndex <= manager.getCapes().size()) {
                manager.removeEntry(manager.getCapes().get(capeCarouselIndex - 1));
                capeCarouselIndex = 0;
                GlassRenderer.playClickSound();
            }
            return true;
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
