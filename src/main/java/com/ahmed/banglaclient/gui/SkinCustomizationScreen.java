package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.BanglaClient;
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
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class SkinCustomizationScreen extends Screen {
    private final Screen parentScreen;
    private final SkinCapeManager manager;
    private final SmoothScrollContainer libraryScroll = new SmoothScrollContainer();

    private PlayerSkinWidget playerSkinWidget;
    private boolean viewingCapesTab = false;

    // Delete Confirmation Dialog state
    private boolean confirmingDelete = false;
    private SkinEntry deleteTarget = null;

    private boolean typingImportPath = false;
    private String manualImportPath = "";
    private String statusBanner = "Drag & drop any .png Skin or Cape file onto this window to import!";

    private int winX, winY, winW, winH;

    public SkinCustomizationScreen(Screen parentScreen, SkinCapeManager manager) {
        super(Text.literal("Skin & Cape Customizer"));
        this.parentScreen = parentScreen;
        this.manager = manager;
    }

    @Override
    protected void init() {
        this.winW = Math.min(560, Math.max(280, this.width - 20));
        this.winH = Math.min(320, Math.max(190, this.height - 20));
        this.winX = (this.width - this.winW) / 2;
        this.winY = (this.height - this.winH) / 2;

        int leftW = Math.min(156, winW / 3 + 18);
        int bodyY = winY + 34;
        int bodyH = winH - 64;

        // Native 1.21.11 GPU-accelerated PlayerSkinWidget with mouse drag rotation & authentic cape
        this.playerSkinWidget = PlayerPreviewRenderer.createSkinWidget(
                leftW - 12,
                bodyH - 30,
                () -> manager.getActiveSkinTextures()
        );
        this.playerSkinWidget.setPosition(winX + 14, bodyY + 6);
        this.addDrawableChild(this.playerSkinWidget);
    }

    @Override
    public void onFilesDropped(List<Path> paths) {
        if (paths == null || paths.isEmpty() || manager == null) return;
        for (Path p : paths) {
            String res = manager.importExternalFile(p, viewingCapesTab ? Boolean.TRUE : null);
            this.statusBanner = res;
            if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
                BanglaClient.getInstance().getNotificationManager().pushInfo("Cosmetics Import", res);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        BlurRenderer.renderScreenBackdrop(context, this.width, this.height, 0.85f);
        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : 0xFFEF4444;

        GlassRenderer.drawGlassPanel(context, winX, winY, winW, winH, false, false, true);

        // Header
        boolean backHov = MathUtils.isHovered(mouseX, mouseY, winX + 8, winY + 7, 56, 18);
        GlassRenderer.drawPillBadge(context, winX + 8, winY + 7, 56, 18, accent, backHov);
        context.drawTextWithShadow(textRenderer, "← Back", winX + 16, winY + 12, 0xFFFFFFFF);

        context.drawTextWithShadow(textRenderer, "3D Skin & Cape Customizer", winX + 72, winY + 12, theme.getTextPrimary());

        int tabSkinsX = winX + winW - 148;
        int tabCapesX = winX + winW - 76;
        GlassRenderer.drawPillBadge(context, tabSkinsX, winY + 7, 68, 18, accent, !viewingCapesTab);
        context.drawTextWithShadow(textRenderer, "Skins (" + manager.getSkins().size() + ")", tabSkinsX + 8, winY + 12, 0xFFFFFFFF);

        GlassRenderer.drawPillBadge(context, tabCapesX, winY + 7, 68, 18, accent, viewingCapesTab);
        context.drawTextWithShadow(textRenderer, "Capes (" + manager.getCapes().size() + ")", tabCapesX + 8, winY + 12, 0xFFFFFFFF);

        GlassRenderer.drawHorizontalDivider(context, winX + 8, winY + 29, winW - 16);

        // Left Panel: 3D Preview Frame
        int leftW = Math.min(156, winW / 3 + 18);
        int bodyY = winY + 34;
        int bodyH = winH - 64;
        GlassRenderer.drawGlassPanel(context, winX + 8, bodyY, leftW, bodyH, true, false, false);

        if (this.playerSkinWidget != null) {
            this.playerSkinWidget.render(context, mouseX, mouseY, delta);
        }

        // Arm Model Type Button: [Model: Classic / Slim]
        boolean slim = manager.isSelectedSlimModel();
        int armBtnY = bodyY + bodyH - 22;
        String armLabel = slim ? "Model: Slim (3px)" : "Model: Classic (4px)";
        boolean armHov = MathUtils.isHovered(mouseX, mouseY, winX + 14, armBtnY, leftW - 12, 16);
        GlassRenderer.drawPillBadge(context, winX + 14, armBtnY, leftW - 12, 16, accent, armHov);
        context.drawTextWithShadow(textRenderer, armLabel, winX + 20, armBtnY + 4, 0xFFFFFFFF);

        // Right Panel: Library Grid
        int rightX = winX + leftW + 14;
        int rightW = winW - leftW - 22;
        GlassRenderer.drawGlassPanel(context, rightX, bodyY, rightW, bodyH, true, false, false);

        // Action Toolbar: [+ Import PNG] [Open Folder] [Export] [No Cape / Delete]
        int actY = bodyY + 6;
        int bW = (rightW - 28) / 4;
        GlassRenderer.drawPillBadge(context, rightX + 6, actY, bW, 16, accent, MathUtils.isHovered(mouseX, mouseY, rightX + 6, actY, bW, 16));
        context.drawTextWithShadow(textRenderer, "+ Import", rightX + 10, actY + 4, 0xFFFFFFFF);

        GlassRenderer.drawPillBadge(context, rightX + 6 + (bW + 4), actY, bW, 16, accent, MathUtils.isHovered(mouseX, mouseY, rightX + 6 + (bW + 4), actY, bW, 16));
        context.drawTextWithShadow(textRenderer, "Folder", rightX + 14 + bW, actY + 4, 0xFFFFFFFF);

        GlassRenderer.drawPillBadge(context, rightX + 6 + (bW + 4) * 2, actY, bW, 16, accent, MathUtils.isHovered(mouseX, mouseY, rightX + 6 + (bW + 4) * 2, actY, bW, 16));
        context.drawTextWithShadow(textRenderer, "Export", rightX + 14 + bW * 2, actY + 4, 0xFFFFFFFF);

        GlassRenderer.drawPillBadge(context, rightX + 6 + (bW + 4) * 3, actY, bW, 16, ThemeTokens.STATUS_ERROR, MathUtils.isHovered(mouseX, mouseY, rightX + 6 + (bW + 4) * 3, actY, bW, 16));
        context.drawTextWithShadow(textRenderer, viewingCapesTab ? "No Cape" : "Delete", rightX + 10 + bW * 3, actY + 4, 0xFFFFFFFF);

        int listStartY = bodyY + 26;
        if (typingImportPath) {
            GlassRenderer.drawGlassPanel(context, rightX + 6, listStartY, rightW - 12, 18, false, false, true);
            String prompt = manualImportPath.isEmpty() ? "Type .png path & press ENTER..." : manualImportPath + "|";
            context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(prompt, rightW - 20), rightX + 10, listStartY + 5, 0xFFFFFFFF);
            listStartY += 22;
        }

        int listH = Math.max(40, (bodyY + bodyH - 6) - listStartY);
        List<SkinEntry> items = viewingCapesTab ? manager.getCapes() : manager.getSkins();
        int cols = rightW >= 240 ? 2 : 1;
        int cardGap = 6;
        int cardW = (rightW - 16 - (cols - 1) * cardGap) / cols;
        int cardH = 36;
        int rows = Math.max(1, (int) Math.ceil((double) (items.size() + 1) / cols));

        libraryScroll.tickAndInterpolate();
        libraryScroll.updateDimensions(listH, rows * (cardH + cardGap) + 6);

        final int finalListY = listStartY;
        ClipRenderer.renderClipped(context, rightX + 4, finalListY, rightW - 8, listH, () -> {
            int startY = Math.round(finalListY - libraryScroll.getScrollOffset());

            // First entry: Default Authenticated Account entry
            boolean isDefaultActive = viewingCapesTab ? !manager.hasCustomCapeSelected() : !manager.hasCustomSkinSelected();
            int defCx = rightX + 6;
            int defCy = startY;
            boolean defHov = MathUtils.isHovered(mouseX, mouseY, defCx, defCy, cardW, cardH);
            GlassRenderer.drawGlassPanel(context, defCx, defCy, cardW, cardH, true, defHov, isDefaultActive);

            PlayerPreviewRenderer.drawPlayerHead(context, SkinCapeManager.getAuthenticatedAccountDefaultSkin(client), defCx + 6, defCy + 6, 24);
            context.drawTextWithShadow(textRenderer, viewingCapesTab ? "Default Account Cape" : "Default Account Skin", defCx + 34, defCy + 7, 0xFFFFFFFF);
            context.drawTextWithShadow(textRenderer, isDefaultActive ? "ACTIVE (UUID)" : "Selectable", defCx + 34, defCy + 19, isDefaultActive ? accent : theme.getTextSecondary());

            for (int i = 0; i < items.size(); i++) {
                SkinEntry entry = items.get(i);
                int slotIndex = i + 1;
                int c = slotIndex % cols;
                int r = slotIndex / cols;
                int cx = rightX + 6 + c * (cardW + cardGap);
                int cy = startY + r * (cardH + cardGap);
                if (cy + cardH < finalListY || cy > finalListY + listH) continue;

                boolean selected = viewingCapesTab
                        ? (manager.getSelectedCape() != null && manager.getSelectedCape().getId().equals(entry.getId()))
                        : (manager.getSelectedSkin() != null && manager.getSelectedSkin().getId().equals(entry.getId()));
                boolean hov = MathUtils.isHovered(mouseX, mouseY, cx, cy, cardW, cardH);

                GlassRenderer.drawGlassPanel(context, cx, cy, cardW, cardH, true, hov, selected);
                if (entry.isCape()) {
                    try {
                        context.drawTexture(RenderPipelines.GUI_TEXTURED, entry.getTextureId(), cx + 6, cy + 4, 1.0f, 1.0f, 18, 28, 64, 32);
                    } catch (Throwable t) {
                        context.fill(cx + 6, cy + 4, cx + 24, cy + 32, 0xFFEF4444);
                    }
                } else {
                    PlayerPreviewRenderer.drawPlayerHead(context, manager.getActiveSkinTextures(), cx + 6, cy + 6, 24);
                }

                String title = textRenderer.trimToWidth(entry.getDisplayName(), cardW - 38);
                context.drawTextWithShadow(textRenderer, title, cx + 34, cy + 7, 0xFFFFFFFF);
                String meta = entry.getWidth() + "x" + entry.getHeight() + (selected ? " • ACTIVE" : "");
                context.drawTextWithShadow(textRenderer, meta, cx + 34, cy + 19, selected ? accent : theme.getTextSecondary());
            }
        });
        libraryScroll.renderScrollbar(context, rightX + rightW - 5, finalListY, 3, listH, mouseX, mouseY);

        // Bottom Bar
        int footY = winY + winH - 24;
        context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(statusBanner, winW - 96), winX + 10, footY + 6, theme.getTextSecondary());

        int applyX = winX + winW - 78;
        boolean appHov = MathUtils.isHovered(mouseX, mouseY, applyX, footY, 70, 18);
        GlassRenderer.drawPillBadge(context, applyX, footY, 70, 18, accent, true);
        context.drawTextWithShadow(textRenderer, "Apply & Save", applyX + 6, footY + 5, appHov ? 0xFFFFFFFF : accent);

        // Delete Confirmation Modal Dialog
        if (confirmingDelete && deleteTarget != null) {
            context.fill(0, 0, this.width, this.height, 0x88000000);
            int mWidth = 260, mHeight = 90;
            int mx = (this.width - mWidth) / 2;
            int my = (this.height - mHeight) / 2;
            GlassRenderer.drawGlassPanel(context, mx, my, mWidth, mHeight, false, false, true);

            context.drawTextWithShadow(textRenderer, "Delete Confirmation", mx + 14, my + 10, ThemeTokens.STATUS_ERROR);
            context.drawTextWithShadow(textRenderer, "Permanently delete '" + deleteTarget.getDisplayName() + "'?", mx + 14, my + 28, 0xFFFFFFFF);
            context.drawTextWithShadow(textRenderer, "This file will be deleted from disk.", mx + 14, my + 42, theme.getTextSecondary());

            int btnHalfW = (mWidth - 36) / 2;
            boolean canHov = MathUtils.isHovered(mouseX, mouseY, mx + 14, my + 60, btnHalfW, 18);
            GlassRenderer.drawPillBadge(context, mx + 14, my + 60, btnHalfW, 18, 0x44FFFFFF, canHov);
            context.drawTextWithShadow(textRenderer, "Cancel", mx + 14 + (btnHalfW - textRenderer.getWidth("Cancel")) / 2, my + 65, 0xFFFFFFFF);

            boolean delHov = MathUtils.isHovered(mouseX, mouseY, mx + 22 + btnHalfW, my + 60, btnHalfW, 18);
            GlassRenderer.drawPillBadge(context, mx + 22 + btnHalfW, my + 60, btnHalfW, 18, ThemeTokens.STATUS_ERROR, true);
            context.drawTextWithShadow(textRenderer, "Delete", mx + 22 + btnHalfW + (btnHalfW - textRenderer.getWidth("Delete")) / 2, my + 65, 0xFFFFFFFF);
        }
    }

    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        // Modal Dialog interactions
        if (confirmingDelete && deleteTarget != null) {
            int mWidth = 260, mHeight = 90;
            int mx = (this.width - mWidth) / 2;
            int my = (this.height - mHeight) / 2;
            int btnHalfW = (mWidth - 36) / 2;

            if (MathUtils.isHovered(mouseX, mouseY, mx + 14, my + 60, btnHalfW, 18)) {
                confirmingDelete = false;
                deleteTarget = null;
                return true;
            }
            if (MathUtils.isHovered(mouseX, mouseY, mx + 22 + btnHalfW, my + 60, btnHalfW, 18)) {
                String name = deleteTarget.getDisplayName();
                manager.removeEntry(deleteTarget);
                statusBanner = "Deleted: " + name;
                confirmingDelete = false;
                deleteTarget = null;
                return true;
            }
            return true;
        }

        if (MathUtils.isHovered(mouseX, mouseY, winX + 8, winY + 7, 56, 18)) {
            close();
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, winX + winW - 78, winY + winH - 24, 70, 18)) {
            manager.saveSelection();
            close();
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, winX + winW - 148, winY + 7, 68, 18)) {
            viewingCapesTab = false;
            libraryScroll.resetScroll();
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, winX + winW - 76, winY + 7, 68, 18)) {
            viewingCapesTab = true;
            libraryScroll.resetScroll();
            return true;
        }

        int leftW = Math.min(156, winW / 3 + 18);
        int bodyY = winY + 34;
        int bodyH = winH - 64;

        if (MathUtils.isHovered(mouseX, mouseY, winX + 14, bodyY + bodyH - 22, leftW - 12, 16)) {
            manager.setSelectedSlimModel(!manager.isSelectedSlimModel());
            return true;
        }

        int rightX = winX + leftW + 14;
        int rightW = winW - leftW - 22;
        int actY = bodyY + 6;
        int bW = (rightW - 28) / 4;

        if (MathUtils.isHovered(mouseX, mouseY, rightX + 6, actY, bW, 16)) {
            typingImportPath = !typingImportPath;
            manager.refreshLibraries();
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 6 + (bW + 4), actY, bW, 16)) {
            Path folder = viewingCapesTab ? manager.getCapesDir() : manager.getSkinsDir();
            try { Util.getOperatingSystem().open(folder.toFile()); } catch (Throwable ignored) {}
            statusBanner = "Folder: " + folder.toAbsolutePath();
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 6 + (bW + 4) * 2, actY, bW, 16)) {
            SkinEntry sel = viewingCapesTab ? manager.getSelectedCape() : manager.getSelectedSkin();
            if (sel != null && client != null && client.keyboard != null) {
                client.keyboard.setClipboard(sel.getFilePath().toAbsolutePath().toString());
                statusBanner = "Exported path: " + sel.getDisplayName();
            } else {
                statusBanner = "No imported file selected to export.";
            }
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 6 + (bW + 4) * 3, actY, bW, 16)) {
            if (viewingCapesTab) {
                manager.setSelectedCapeId("");
                statusBanner = "Unequipped Cape.";
            } else {
                SkinEntry sel = manager.getSelectedSkin();
                if (sel != null) {
                    confirmingDelete = true;
                    deleteTarget = sel;
                } else {
                    statusBanner = "Default account skin cannot be deleted.";
                }
            }
            return true;
        }

        int listStartY = bodyY + 26 + (typingImportPath ? 22 : 0);
        int listH = Math.max(40, (bodyY + bodyH - 6) - listStartY);
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 4, listStartY, rightW - 8, listH)) {
            List<SkinEntry> items = viewingCapesTab ? manager.getCapes() : manager.getSkins();
            int cols = rightW >= 240 ? 2 : 1;
            int cardGap = 6;
            int cardW = (rightW - 16 - (cols - 1) * cardGap) / cols;
            int cardH = 36;
            int startY = Math.round(listStartY - libraryScroll.getScrollOffset());

            // Default account card
            if (MathUtils.isHovered(mouseX, mouseY, rightX + 6, startY, cardW, cardH)) {
                if (viewingCapesTab) {
                    manager.setSelectedCapeId("");
                    statusBanner = "Using official account cape (if equipped).";
                } else {
                    manager.setSelectedSkinId("");
                    statusBanner = "Using official authenticated account skin.";
                }
                return true;
            }

            for (int i = 0; i < items.size(); i++) {
                SkinEntry entry = items.get(i);
                int slotIndex = i + 1;
                int c = slotIndex % cols;
                int r = slotIndex / cols;
                int cx = rightX + 6 + c * (cardW + cardGap);
                int cy = startY + r * (cardH + cardGap);
                if (MathUtils.isHovered(mouseX, mouseY, cx, cy, cardW, cardH)) {
                    if (viewingCapesTab) {
                        manager.setSelectedCapeId(entry.getId());
                        statusBanner = "Selected Cape: " + entry.getDisplayName();
                    } else {
                        manager.setSelectedSkinId(entry.getId());
                        statusBanner = "Selected Skin: " + entry.getDisplayName();
                    }
                    return true;
                }
            }
        }
        return true;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        return handleMouseClick(click.x(), click.y(), click.button()) || super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        libraryScroll.onMouseScrolled(mouseX, mouseY, verticalAmount, 0, 0, this.width, this.height);
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (typingImportPath) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                typingImportPath = false;
                return true;
            }
            if (key == GLFW.GLFW_KEY_ENTER) {
                if (!manualImportPath.isBlank()) {
                    statusBanner = manager.importExternalFile(Paths.get(manualImportPath.trim()), viewingCapesTab ? Boolean.TRUE : null);
                }
                typingImportPath = false;
                return true;
            }
            if (key == GLFW.GLFW_KEY_BACKSPACE && !manualImportPath.isEmpty()) {
                manualImportPath = manualImportPath.substring(0, manualImportPath.length() - 1);
                return true;
            }
            return true;
        }
        if (confirmingDelete) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                confirmingDelete = false;
                deleteTarget = null;
                return true;
            }
            if (key == GLFW.GLFW_KEY_ENTER && deleteTarget != null) {
                manager.removeEntry(deleteTarget);
                confirmingDelete = false;
                deleteTarget = null;
                return true;
            }
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
        if (typingImportPath && chr >= 32 && chr != 127) {
            manualImportPath += chr;
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public void close() {
        manager.saveSelection();
        if (this.client != null) {
            this.client.setScreen(parentScreen);
        }
    }

    @Override
    public boolean shouldPause() { return false; }
}
