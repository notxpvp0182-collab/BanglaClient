package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.cosmetics.PlayerPreviewRenderer;
import com.ahmed.banglaclient.cosmetics.SkinCapeManager;
import com.ahmed.banglaclient.cosmetics.SkinEntry;
import com.ahmed.banglaclient.gui.components.SmoothScrollContainer;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.ClipRenderer;
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

/**
 * Bedrock Edition Style Skin & Cape Customizer:
 * Renders each uploaded skin as a standing full-body character model card one after another.
 * Zero extra description clutter.
 */
public final class SkinCustomizationScreen extends Screen {
    private final Screen parentScreen;
    private final SkinCapeManager manager;
    private final SmoothScrollContainer libraryScroll = new SmoothScrollContainer();
    private final Animation openAnim = new Animation(0.0f, 160L, Easing.EASE_OUT_CUBIC);
    private boolean closing = false;

    private PlayerSkinWidget playerSkinWidget;
    private boolean viewingCapesTab = false;
    private boolean confirmingDelete = false;
    private SkinEntry deleteTarget = null;

    private int winX, winY, winW, winH;

    public SkinCustomizationScreen(Screen parentScreen, SkinCapeManager manager) {
        super(Text.literal("Wardrobe"));
        this.parentScreen = parentScreen;
        this.manager = manager;
    }

    @Override
    protected void init() {
        this.winW = Math.min(460, Math.max(280, (int) (this.width * 0.78f)));
        this.winH = Math.min(260, Math.max(180, (int) (this.height * 0.78f)));
        this.winX = (this.width - this.winW) / 2;
        this.winY = (this.height - this.winH) / 2;
        this.closing = false;
        this.openAnim.snapTo(0.0f);
        this.openAnim.animateTo(1.0f);

        int leftW = Math.min(130, winW / 3);
        int bodyY = winY + 30;
        int bodyH = winH - 36;

        this.playerSkinWidget = PlayerPreviewRenderer.createSkinWidget(
                leftW - 12,
                bodyH - 42,
                () -> manager.getActiveSkinTextures()
        );
        this.playerSkinWidget.setPosition(winX + 12, bodyY + 4);
        this.addDrawableChild(this.playerSkinWidget);
    }

    @Override
    public void onFilesDropped(List<Path> paths) {
        if (paths == null || paths.isEmpty() || manager == null) return;
        for (Path p : paths) {
            manager.importExternalFile(p, viewingCapesTab ? Boolean.TRUE : null);
        }
    }

    /**
     * Draws a full standing Bedrock-style character model (Head + Hat + Body + Arms + Legs) from a 64x64 skin texture.
     */
    private void drawStandingSkinModel(DrawContext context, Identifier skinTex, int centerX, int topY) {
        try {
            // Head (8x8 -> 16x16) + Hat overlay
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTex, centerX - 8, topY, 8.0f, 8.0f, 16, 16, 64, 64);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTex, centerX - 8, topY, 40.0f, 8.0f, 16, 16, 64, 64);
            // Body (8x12 -> 16x20)
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTex, centerX - 8, topY + 16, 20.0f, 20.0f, 16, 20, 64, 64);
            // Left & Right Arms (4x12 -> 7x20)
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTex, centerX - 15, topY + 16, 44.0f, 20.0f, 7, 20, 64, 64);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTex, centerX + 8, topY + 16, 36.0f, 52.0f, 7, 20, 64, 64);
            // Left & Right Legs (4x12 -> 8x20)
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTex, centerX - 8, topY + 36, 4.0f, 20.0f, 8, 20, 64, 64);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTex, centerX, topY + 36, 20.0f, 52.0f, 8, 20, 64, 64);
        } catch (Throwable ignored) {}
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
        Theme theme = GlassRenderer.getTheme();

        Matrix3x2fStack matrices = context.getMatrices();
        float scale = 0.94f + 0.06f * anim;
        matrices.pushMatrix();
        matrices.translate(this.width * 0.5f, this.height * 0.5f);
        matrices.scale(scale, scale);
        matrices.translate(-this.width * 0.5f, -this.height * 0.5f);

        try {
            GlassRenderer.drawGlassPanel(context, winX, winY, winW, winH, false, false, true);

            // Header: [← Back] | Wardrobe | [Skins] [Capes]
            boolean backHov = MathUtils.isHovered(mouseX, mouseY, winX + 6, winY + 6, 46, 16);
            GlassRenderer.drawPillBadge(context, winX + 6, winY + 6, 46, 16, 0xFFFFFFFF, backHov);
            context.drawTextWithShadow(textRenderer, "← Back", winX + 12, winY + 10, 0xFFFFFFFF);

            context.drawTextWithShadow(textRenderer, "Wardrobe", winX + 58, winY + 10, 0xFFFFFFFF);

            int tabSkinsX = winX + winW - 124;
            int tabCapesX = winX + winW - 62;
            GlassRenderer.drawPillBadge(context, tabSkinsX, winY + 6, 58, 16, 0xFFFFFFFF, !viewingCapesTab);
            context.drawTextWithShadow(textRenderer, "Skins (" + manager.getSkins().size() + ")", tabSkinsX + 6, winY + 10, 0xFFFFFFFF);

            GlassRenderer.drawPillBadge(context, tabCapesX, winY + 6, 56, 16, 0xFFFFFFFF, viewingCapesTab);
            context.drawTextWithShadow(textRenderer, "Capes (" + manager.getCapes().size() + ")", tabCapesX + 6, winY + 10, 0xFFFFFFFF);

            GlassRenderer.drawHorizontalDivider(context, winX + 6, winY + 25, winW - 12);

            // Left Panel: 3D Player Preview + Model Type
            int leftW = Math.min(130, winW / 3);
            int bodyY = winY + 30;
            int bodyH = winH - 36;
            GlassRenderer.drawGlassPanel(context, winX + 6, bodyY, leftW, bodyH, true, false, false);

            if (this.playerSkinWidget != null) {
                this.playerSkinWidget.render(context, mouseX, mouseY, delta);
            }

            boolean slim = manager.isSelectedSlimModel();
            int armBtnY = bodyY + bodyH - 34;
            String armLabel = slim ? "Model: Slim" : "Model: Classic";
            boolean armHov = MathUtils.isHovered(mouseX, mouseY, winX + 10, armBtnY, leftW - 8, 14);
            GlassRenderer.drawPillBadge(context, winX + 10, armBtnY, leftW - 8, 14, 0xFFFFFFFF, armHov);
            context.drawTextWithShadow(textRenderer, armLabel, winX + 10 + (leftW - 8 - textRenderer.getWidth(armLabel)) / 2, armBtnY + 3, 0xFFFFFFFF);

            int overBtnY = bodyY + bodyH - 17;
            int halfW = (leftW - 10) / 2;
            boolean skinOver = manager.isCustomSkinOverride();
            boolean capeOver = manager.isCustomCapeOverride();

            GlassRenderer.drawPillBadge(context, winX + 10, overBtnY, halfW, 13, 0xFFFFFFFF, skinOver);
            context.drawTextWithShadow(textRenderer, skinOver ? "Skin: ON" : "Skin: OFF", winX + 14, overBtnY + 3, 0xFFFFFFFF);

            GlassRenderer.drawPillBadge(context, winX + 12 + halfW, overBtnY, halfW, 13, 0xFFFFFFFF, capeOver);
            context.drawTextWithShadow(textRenderer, capeOver ? "Cape: ON" : "Cape: OFF", winX + 16 + halfW, overBtnY + 3, 0xFFFFFFFF);

            // Right Panel: Bedrock Edition Standing Skin Model Grid
            int rightX = winX + leftW + 10;
            int rightW = winW - leftW - 16;
            GlassRenderer.drawGlassPanel(context, rightX, bodyY, rightW, bodyH, true, false, false);

            int actY = bodyY + 5;
            int bW = (rightW - 20) / 3;
            GlassRenderer.drawPillBadge(context, rightX + 5, actY, bW, 15, 0xFFFFFFFF, MathUtils.isHovered(mouseX, mouseY, rightX + 5, actY, bW, 15));
            context.drawTextWithShadow(textRenderer, "Open Folder", rightX + 5 + (bW - textRenderer.getWidth("Open Folder")) / 2, actY + 4, 0xFFFFFFFF);

            GlassRenderer.drawPillBadge(context, rightX + 10 + bW, actY, bW, 15, 0xFFFFFFFF, MathUtils.isHovered(mouseX, mouseY, rightX + 10 + bW, actY, bW, 15));
            context.drawTextWithShadow(textRenderer, "Refresh", rightX + 10 + bW + (bW - textRenderer.getWidth("Refresh")) / 2, actY + 4, 0xFFFFFFFF);

            GlassRenderer.drawPillBadge(context, rightX + 15 + bW * 2, actY, bW, 15, ThemeTokens.STATUS_ERROR, MathUtils.isHovered(mouseX, mouseY, rightX + 15 + bW * 2, actY, bW, 15));
            context.drawTextWithShadow(textRenderer, "Delete", rightX + 15 + bW * 2 + (bW - textRenderer.getWidth("Delete")) / 2, actY + 4, 0xFFFFFFFF);

            int listStartY = bodyY + 24;
            int listH = bodyH - 28;
            List<SkinEntry> items = viewingCapesTab ? manager.getCapes() : manager.getSkins();
            int cols = rightW >= 240 ? 4 : 3;
            int cardGap = 6;
            int cardW = (rightW - 14 - (cols - 1) * cardGap) / cols;
            int cardH = 82;
            int rows = Math.max(1, (int) Math.ceil((double) (items.size() + 1) / cols));

            libraryScroll.tickAndInterpolate();
            libraryScroll.updateDimensions(listH, rows * (cardH + cardGap) + 6);

            ClipRenderer.renderClipped(context, rightX + 4, listStartY, rightW - 8, listH, () -> {
                int startY = Math.round(listStartY - libraryScroll.getScrollOffset());

                // Slot 0: Default Skin/Cape Card
                boolean isDefaultActive = viewingCapesTab ? !manager.hasCustomCapeSelected() : !manager.hasCustomSkinSelected();
                int defCx = rightX + 5;
                boolean defHov = MathUtils.isHovered(mouseX, mouseY, defCx, startY, cardW, cardH);
                GlassRenderer.drawGlassPanel(context, defCx, startY, cardW, cardH, true, defHov, isDefaultActive);

                Identifier defTex = SkinCapeManager.getAuthenticatedAccountDefaultSkin(client).body().texturePath();
                drawStandingSkinModel(context, defTex, defCx + cardW / 2, startY + 6);
                String defLbl = "Default";
                context.drawTextWithShadow(textRenderer, defLbl, defCx + (cardW - textRenderer.getWidth(defLbl)) / 2, startY + cardH - 12, 0xFFFFFFFF);

                // Uploaded Skins/Capes one after another in Bedrock model format
                for (int i = 0; i < items.size(); i++) {
                    SkinEntry entry = items.get(i);
                    int slotIndex = i + 1;
                    int c = slotIndex % cols;
                    int r = slotIndex / cols;
                    int cx = rightX + 5 + c * (cardW + cardGap);
                    int cy = startY + r * (cardH + cardGap);
                    if (cy + cardH < listStartY || cy > listStartY + listH) continue;

                    boolean selected = viewingCapesTab
                            ? (manager.getSelectedCape() != null && manager.getSelectedCape().getId().equals(entry.getId()))
                            : (manager.getSelectedSkin() != null && manager.getSelectedSkin().getId().equals(entry.getId()));
                    boolean hov = MathUtils.isHovered(mouseX, mouseY, cx, cy, cardW, cardH);

                    GlassRenderer.drawGlassPanel(context, cx, cy, cardW, cardH, true, hov, selected);
                    if (entry.isCape()) {
                        try {
                            context.drawTexture(RenderPipelines.GUI_TEXTURED, entry.getTextureId(), cx + (cardW - 28) / 2, cy + 10, 1.0f, 1.0f, 28, 44, 64, 32);
                        } catch (Throwable ignored) {}
                    } else {
                        drawStandingSkinModel(context, entry.getTextureId(), cx + cardW / 2, cy + 6);
                    }

                    String title = textRenderer.trimToWidth(entry.getDisplayName(), cardW - 6);
                    context.drawTextWithShadow(textRenderer, title, cx + (cardW - textRenderer.getWidth(title)) / 2, cy + cardH - 12,
                            selected ? 0xFFFFFFFF : theme.getTextSecondary());
                }
            });
            libraryScroll.renderScrollbar(context, rightX + rightW - 4, listStartY, 2, listH, mouseX, mouseY);

            if (confirmingDelete && deleteTarget != null) {
                context.fill(0, 0, this.width, this.height, 0x99000000);
                int mWidth = 220, mHeight = 72;
                int mx = (this.width - mWidth) / 2, my = (this.height - mHeight) / 2;
                GlassRenderer.drawGlassPanel(context, mx, my, mWidth, mHeight, true, false, true);
                context.drawTextWithShadow(textRenderer, "Delete '" + deleteTarget.getDisplayName() + "'?", mx + 12, my + 14, 0xFFFFFFFF);

                int btnHalfW = (mWidth - 28) / 2;
                GlassRenderer.drawPillBadge(context, mx + 10, my + 42, btnHalfW, 18, 0xFFFFFFFF, false);
                context.drawTextWithShadow(textRenderer, "Cancel", mx + 10 + (btnHalfW - textRenderer.getWidth("Cancel")) / 2, my + 47, 0xFFFFFFFF);

                GlassRenderer.drawPillBadge(context, mx + 18 + btnHalfW, my + 42, btnHalfW, 18, ThemeTokens.STATUS_ERROR, true);
                context.drawTextWithShadow(textRenderer, "Delete", mx + 18 + btnHalfW + (btnHalfW - textRenderer.getWidth("Delete")) / 2, my + 47, 0xFFFFFFFF);
            }
        } finally {
            matrices.popMatrix();
        }
    }

    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (button != 0 || closing) return false;

        if (confirmingDelete && deleteTarget != null) {
            int mWidth = 220, mHeight = 72;
            int mx = (this.width - mWidth) / 2, my = (this.height - mHeight) / 2;
            int btnHalfW = (mWidth - 28) / 2;
            if (MathUtils.isHovered(mouseX, mouseY, mx + 10, my + 42, btnHalfW, 18)) {
                confirmingDelete = false;
                deleteTarget = null;
                return true;
            }
            if (MathUtils.isHovered(mouseX, mouseY, mx + 18 + btnHalfW, my + 42, btnHalfW, 18)) {
                manager.removeEntry(deleteTarget);
                confirmingDelete = false;
                deleteTarget = null;
                return true;
            }
            return true;
        }

        if (MathUtils.isHovered(mouseX, mouseY, winX + 6, winY + 6, 46, 16)) {
            close();
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, winX + winW - 124, winY + 6, 58, 16)) {
            viewingCapesTab = false;
            libraryScroll.resetScroll();
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, winX + winW - 62, winY + 6, 56, 16)) {
            viewingCapesTab = true;
            libraryScroll.resetScroll();
            return true;
        }

        int leftW = Math.min(130, winW / 3);
        int bodyY = winY + 30;
        int bodyH = winH - 36;

        if (MathUtils.isHovered(mouseX, mouseY, winX + 10, bodyY + bodyH - 34, leftW - 8, 14)) {
            manager.setSelectedSlimModel(!manager.isSelectedSlimModel());
            return true;
        }

        int overBtnY = bodyY + bodyH - 17;
        int halfW = (leftW - 10) / 2;
        if (MathUtils.isHovered(mouseX, mouseY, winX + 10, overBtnY, halfW, 13)) {
            manager.setCustomSkinOverride(!manager.isCustomSkinOverride());
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, winX + 12 + halfW, overBtnY, halfW, 13)) {
            manager.setCustomCapeOverride(!manager.isCustomCapeOverride());
            return true;
        }

        int rightX = winX + leftW + 10;
        int rightW = winW - leftW - 16;
        int actY = bodyY + 5;
        int bW = (rightW - 20) / 3;

        if (MathUtils.isHovered(mouseX, mouseY, rightX + 5, actY, bW, 15)) {
            Path folder = viewingCapesTab ? manager.getCapesDir() : manager.getSkinsDir();
            try { Util.getOperatingSystem().open(folder.toFile()); } catch (Throwable ignored) {}
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 10 + bW, actY, bW, 15)) {
            manager.refreshLibraries();
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 15 + bW * 2, actY, bW, 15)) {
            SkinEntry sel = viewingCapesTab ? manager.getSelectedCape() : manager.getSelectedSkin();
            if (sel != null) {
                confirmingDelete = true;
                deleteTarget = sel;
            }
            return true;
        }

        int listStartY = bodyY + 24;
        int listH = bodyH - 28;
        if (libraryScroll.onMouseClicked(mouseX, mouseY, 0, rightX + rightW - 5, listStartY, 4, listH)) {
            return true;
        }

        if (MathUtils.isHovered(mouseX, mouseY, rightX + 4, listStartY, rightW - 8, listH)) {
            List<SkinEntry> items = viewingCapesTab ? manager.getCapes() : manager.getSkins();
            int cols = rightW >= 240 ? 4 : 3;
            int cardGap = 6;
            int cardW = (rightW - 14 - (cols - 1) * cardGap) / cols;
            int cardH = 82;
            int startY = Math.round(listStartY - libraryScroll.getScrollOffset());

            if (MathUtils.isHovered(mouseX, mouseY, rightX + 5, startY, cardW, cardH)) {
                if (viewingCapesTab) manager.setSelectedCapeId("");
                else manager.setSelectedSkinId("");
                return true;
            }

            for (int i = 0; i < items.size(); i++) {
                SkinEntry entry = items.get(i);
                int slotIndex = i + 1;
                int c = slotIndex % cols;
                int r = slotIndex / cols;
                int cx = rightX + 5 + c * (cardW + cardGap);
                int cy = startY + r * (cardH + cardGap);
                if (MathUtils.isHovered(mouseX, mouseY, cx, cy, cardW, cardH)) {
                    if (viewingCapesTab) manager.setSelectedCapeId(entry.getId());
                    else manager.setSelectedSkinId(entry.getId());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        return handleMouseClick(click.x(), click.y(), click.button()) || super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click c, double dx, double dy) {
        return libraryScroll.onMouseDragged(c.x(), c.y(), c.button(), winH - 64) || super.mouseDragged(c, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click c) {
        libraryScroll.onMouseReleased(c.button());
        return super.mouseReleased(c);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        libraryScroll.onMouseScrolled(mouseX, mouseY, verticalAmount, winX, winY + 54, winW, winH - 64);
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            if (confirmingDelete) { confirmingDelete = false; deleteTarget = null; return true; }
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
