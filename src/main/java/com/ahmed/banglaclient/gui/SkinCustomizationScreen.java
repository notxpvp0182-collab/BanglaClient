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
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Dedicated Bedrock-inspired Skin & Cape Customization Screen with interactive 3D Character Preview,
 * Skin Library, Cape Library, Classic/Slim model switcher, and Drag-and-Drop PNG import.
 */
public final class SkinCustomizationScreen extends Screen {
    private final Screen parentScreen;
    private final SkinCapeManager manager;
    private final SmoothScrollContainer libraryScroll = new SmoothScrollContainer();

    private boolean viewingCapesTab = false;
    private boolean draggingModel = false;
    private float previewYaw = 22.0f;
    private float previewPitch = 8.0f;

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
        this.winW = Math.min(540, Math.max(280, this.width - 20));
        this.winH = Math.min(320, Math.max(190, this.height - 20));
        this.winX = (this.width - this.winW) / 2;
        this.winY = (this.height - this.winH) / 2;
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

        // Top Bar: [← Return] | Title | [Skins Library] [Capes Library]
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

        // Left Panel: Interactive 3D Character Preview + Model Type Toggle
        int leftW = Math.min(156, winW / 3 + 18);
        int bodyY = winY + 34;
        int bodyH = winH - 64;
        GlassRenderer.drawGlassPanel(context, winX + 8, bodyY, leftW, bodyH, true, false, false);

        String activeUser = this.client != null && this.client.getSession() != null ? this.client.getSession().getUsername() : "Player";
        int uw = textRenderer.getWidth(activeUser);
        context.drawTextWithShadow(textRenderer, activeUser, winX + 8 + (leftW - uw) / 2, bodyY + 8, accent);

        SkinEntry selSkin = manager.getSelectedSkin();
        SkinEntry selCape = manager.getSelectedCape();
        boolean slim = manager.isSelectedSlimModel();

        int modelCx = winX + 8 + leftW / 2;
        int modelCy = bodyY + bodyH / 2 + 2;
        float modelScale = Math.max(2.0f, Math.min(3.4f, bodyH / 52.0f));
        PlayerPreviewRenderer.renderPlayerModel3D(context, modelCx, modelCy, modelScale, previewYaw, previewPitch, selSkin, selCape, slim);

        // Model Arm Type Button: [Model: Classic / Slim]
        int armBtnY = bodyY + bodyH - 22;
        String armLabel = slim ? "Model: Slim (3px)" : "Model: Classic (4px)";
        boolean armHov = MathUtils.isHovered(mouseX, mouseY, winX + 14, armBtnY, leftW - 12, 16);
        GlassRenderer.drawPillBadge(context, winX + 14, armBtnY, leftW - 12, 16, accent, armHov);
        context.drawTextWithShadow(textRenderer, armLabel, winX + 20, armBtnY + 4, 0xFFFFFFFF);

        // Right Panel: Skin / Cape Library Grid + Action Toolbar
        int rightX = winX + leftW + 14;
        int rightW = winW - leftW - 22;
        GlassRenderer.drawGlassPanel(context, rightX, bodyY, rightW, bodyH, true, false, false);

        // Action Row inside Right Panel: [+ Import File] [Open Folder] [Unequip Cape / Delete]
        int actY = bodyY + 6;
        int btn1W = 78, btn2W = 76, btn3W = 76;
        GlassRenderer.drawPillBadge(context, rightX + 6, actY, btn1W, 16, accent, MathUtils.isHovered(mouseX, mouseY, rightX + 6, actY, btn1W, 16));
        context.drawTextWithShadow(textRenderer, "+ Import PNG", rightX + 12, actY + 4, 0xFFFFFFFF);

        GlassRenderer.drawPillBadge(context, rightX + 88, actY, btn2W, 16, accent, MathUtils.isHovered(mouseX, mouseY, rightX + 88, actY, btn2W, 16));
        context.drawTextWithShadow(textRenderer, "Open Folder", rightX + 95, actY + 4, 0xFFFFFFFF);

        GlassRenderer.drawPillBadge(context, rightX + 168, actY, btn3W, 16, accent, MathUtils.isHovered(mouseX, mouseY, rightX + 168, actY, btn3W, 16));
        context.drawTextWithShadow(textRenderer, viewingCapesTab ? "No Cape" : "Delete", rightX + 182, actY + 4, 0xFFFFFFFF);

        // Optional Path Input Box when [+ Import PNG] is clicked on Android/Termux
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
        int rows = (int) Math.ceil((double) items.size() / cols);

        libraryScroll.tickAndInterpolate();
        libraryScroll.updateDimensions(listH, rows * (cardH + cardGap) + 6);

        ClipRenderer.renderClipped(context, rightX + 4, listStartY, rightW - 8, listH, () -> {
            int startY = Math.round(listStartY - libraryScroll.getScrollOffset());
            for (int i = 0; i < items.size(); i++) {
                SkinEntry entry = items.get(i);
                int c = i % cols;
                int r = i / cols;
                int cx = rightX + 6 + c * (cardW + cardGap);
                int cy = startY + r * (cardH + cardGap);
                if (cy + cardH < listStartY || cy > listStartY + listH) continue;

                boolean selected = viewingCapesTab
                        ? (selCape != null && selCape.getId().equals(entry.getId()))
                        : (selSkin != null && selSkin.getId().equals(entry.getId()));
                boolean hov = MathUtils.isHovered(mouseX, mouseY, cx, cy, cardW, cardH);

                GlassRenderer.drawGlassPanel(context, cx, cy, cardW, cardH, true, hov, selected);
                if (entry.isCape()) {
                    PlayerPreviewRenderer.drawCapeThumbnail(context, entry, cx + 6, cy + 4, 18, 28);
                } else {
                    PlayerPreviewRenderer.drawPlayerHead(context, entry, cx + 6, cy + 6, 24);
                }

                String title = textRenderer.trimToWidth(entry.getDisplayName(), cardW - 38);
                context.drawTextWithShadow(textRenderer, title, cx + 34, cy + 7, 0xFFFFFFFF);
                String meta = entry.getWidth() + "x" + entry.getHeight() + (selected ? " • ACTIVE" : "");
                context.drawTextWithShadow(textRenderer, meta, cx + 34, cy + 19, selected ? accent : theme.getTextSecondary());
            }
        });
        libraryScroll.renderScrollbar(context, rightX + rightW - 5, listStartY, 3, listH, mouseX, mouseY);

        // Bottom Status & Apply Bar
        int footY = winY + winH - 24;
        context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(statusBanner, winW - 96), winX + 10, footY + 6, theme.getTextSecondary());

        int applyX = winX + winW - 78;
        boolean appHov = MathUtils.isHovered(mouseX, mouseY, applyX, footY, 70, 18);
        GlassRenderer.drawPillBadge(context, applyX, footY, 70, 18, accent, true);
        context.drawTextWithShadow(textRenderer, "Apply & Save", applyX + 6, footY + 5, appHov ? 0xFFFFFFFF : accent);
    }

    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        // Back button
        if (MathUtils.isHovered(mouseX, mouseY, winX + 8, winY + 7, 56, 18)) {
            close();
            return true;
        }
        // Apply & Save button
        if (MathUtils.isHovered(mouseX, mouseY, winX + winW - 78, winY + winH - 24, 70, 18)) {
            manager.saveSelection();
            close();
            return true;
        }
        // Tabs
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

        // Classic / Slim toggle
        if (MathUtils.isHovered(mouseX, mouseY, winX + 14, bodyY + bodyH - 22, leftW - 12, 16)) {
            manager.setSelectedSlimModel(!manager.isSelectedSlimModel());
            return true;
        }

        // Start 3D character rotation drag
        if (MathUtils.isHovered(mouseX, mouseY, winX + 8, bodyY, leftW, bodyH - 24)) {
            draggingModel = true;
            return true;
        }

        int rightX = winX + leftW + 14;
        int rightW = winW - leftW - 22;
        int actY = bodyY + 6;

        // [+ Import PNG]
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 6, actY, 78, 16)) {
            typingImportPath = !typingImportPath;
            manager.refreshLibraries();
            statusBanner = "Refreshed library (" + manager.getSkins().size() + " skins, " + manager.getCapes().size() + " capes).";
            return true;
        }
        // [Open Folder]
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 88, actY, 76, 16)) {
            Path folder = viewingCapesTab ? manager.getCapesDir() : manager.getSkinsDir();
            try {
                Util.getOperatingSystem().open(folder.toFile());
            } catch (Throwable ignored) {}
            statusBanner = "Folder: " + folder.toAbsolutePath();
            return true;
        }
        // [No Cape / Delete]
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 168, actY, 76, 16)) {
            if (viewingCapesTab) {
                manager.setSelectedCapeId("");
                statusBanner = "Unequipped Cape.";
            } else {
                SkinEntry sel = manager.getSelectedSkin();
                if (sel != null && manager.getSkins().size() > 1) {
                    manager.removeEntry(sel);
                    statusBanner = "Removed skin: " + sel.getDisplayName();
                }
            }
            return true;
        }

        // Library Card Clicks
        int listStartY = bodyY + 26 + (typingImportPath ? 22 : 0);
        int listH = Math.max(40, (bodyY + bodyH - 6) - listStartY);
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 4, listStartY, rightW - 8, listH)) {
            List<SkinEntry> items = viewingCapesTab ? manager.getCapes() : manager.getSkins();
            int cols = rightW >= 240 ? 2 : 1;
            int cardGap = 6;
            int cardW = (rightW - 16 - (cols - 1) * cardGap) / cols;
            int cardH = 36;
            int startY = Math.round(listStartY - libraryScroll.getScrollOffset());

            for (int i = 0; i < items.size(); i++) {
                SkinEntry entry = items.get(i);
                int c = i % cols;
                int r = i / cols;
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
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (draggingModel) {
            previewYaw += (float) deltaX * 1.6f;
            previewPitch = MathUtils.clamp(previewPitch + (float) deltaY * 0.8f, -35.0f, 35.0f);
            return true;
        }
        libraryScroll.onMouseScrolled(click.x(), click.y(), deltaY * 0.12, 0, 0, this.width, this.height);
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        draggingModel = false;
        libraryScroll.onMouseReleased(click.button());
        return super.mouseReleased(click);
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
