package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.cosmetics.BackgroundManager;
import com.ahmed.banglaclient.cosmetics.BackgroundManager.BackgroundEntry;
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
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.List;

/**
 * 2D Aura Dark Custom Main Menu Background Selector (Supports GIF, PNG, JPG, JPEG, Rename, Delete, Drag & Drop).
 */
public final class BackgroundSelectorScreen extends Screen {
    private final Screen parentScreen;
    private final BackgroundManager manager = BackgroundManager.getInstance();
    private final SmoothScrollContainer gridScroll = new SmoothScrollContainer();
    private final Animation openAnim = new Animation(0.0f, 160L, Easing.EASE_OUT_CUBIC);
    private boolean closing = false;

    private BackgroundEntry highlightedEntry = null;
    private boolean renaming = false;
    private String renameInput = "";

    private int winX, winY, winW, winH;

    public BackgroundSelectorScreen(Screen parentScreen) {
        super(Text.literal("Background Customizer"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        this.winW = Math.min(420, Math.max(260, (int) (this.width * 0.72f)));
        this.winH = Math.min(240, Math.max(165, (int) (this.height * 0.72f)));
        this.winX = (this.width - this.winW) / 2;
        this.winY = (this.height - this.winH) / 2;
        this.closing = false;
        this.openAnim.snapTo(0.0f);
        this.openAnim.animateTo(1.0f);
        this.highlightedEntry = manager.getActiveBackground();
    }

    @Override
    public void onFilesDropped(List<Path> paths) {
        if (paths == null || paths.isEmpty()) return;
        for (Path p : paths) {
            manager.importFile(p);
        }
        this.highlightedEntry = manager.getActiveBackground();
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

            // Header: [← Back] | Menu Backgrounds | [Folder] [Refresh]
            boolean backHov = MathUtils.isHovered(mouseX, mouseY, winX + 8, winY + 7, 46, 16);
            GlassRenderer.drawPillBadge(context, winX + 8, winY + 7, 46, 16, 0xFFFFFFFF, backHov);
            context.drawTextWithShadow(textRenderer, "← Back", winX + 14, winY + 11, 0xFFFFFFFF);

            context.drawTextWithShadow(textRenderer, "Menu Backgrounds (GIF / PNG / JPG)", winX + 60, winY + 11, 0xFFFFFFFF);

            int folderX = winX + winW - 96;
            boolean folHov = MathUtils.isHovered(mouseX, mouseY, folderX, winY + 7, 44, 16);
            GlassRenderer.drawPillBadge(context, folderX, winY + 7, 44, 16, 0xFFFFFFFF, folHov);
            context.drawTextWithShadow(textRenderer, "Folder", folderX + 8, winY + 11, 0xFFFFFFFF);

            int refX = winX + winW - 48;
            boolean refHov = MathUtils.isHovered(mouseX, mouseY, refX, winY + 7, 40, 16);
            GlassRenderer.drawPillBadge(context, refX, winY + 7, 40, 16, 0xFFFFFFFF, refHov);
            context.drawTextWithShadow(textRenderer, "Reload", refX + 6, winY + 11, 0xFFFFFFFF);

            GlassRenderer.drawHorizontalDivider(context, winX + 8, winY + 27, winW - 16);

            // Grid Area
            int gridX = winX + 8;
            int gridY = winY + 32;
            int gridW = winW - 16;
            int gridH = winH - 64;

            List<BackgroundEntry> items = manager.getEntries();
            int cols = gridW >= 320 ? 3 : 2;
            int gap = 6;
            int cardW = (gridW - 8 - (cols - 1) * gap) / cols;
            int cardH = 64;
            int totalSlots = items.size() + 1; // +1 for Default Minecraft Panorama
            int rows = (int) Math.ceil((double) totalSlots / cols);

            gridScroll.tickAndInterpolate();
            gridScroll.updateDimensions(gridH, rows * (cardH + gap) + 6);

            ClipRenderer.renderClipped(context, gridX, gridY, gridW - 4, gridH, () -> {
                int startY = Math.round(gridY - gridScroll.getScrollOffset());

                // Slot 0: Default Minecraft Background
                boolean defActive = manager.getActiveBackground() == null;
                boolean defHov = MathUtils.isHovered(mouseX, mouseY, gridX, startY, cardW, cardH);
                GlassRenderer.drawGlassPanel(context, gridX, startY, cardW, cardH, true, defHov, defActive);
                context.fill(gridX + 4, startY + 4, gridX + cardW - 4, startY + cardH - 18, 0xFF141820);
                context.drawTextWithShadow(textRenderer, "Default", gridX + 8, startY + cardH - 13, 0xFFFFFFFF);
                if (defActive) {
                    GlassRenderer.drawPillBadge(context, gridX + cardW - 16, startY + 5, 12, 12, 0xFF34D399, true); context.drawTextWithShadow(textRenderer, "✓", gridX + cardW - 13, startY + 7, 0xFF34D399);
                }

                for (int i = 0; i < items.size(); i++) {
                    BackgroundEntry entry = items.get(i);
                    int slot = i + 1;
                    int c = slot % cols;
                    int r = slot / cols;
                    int cx = gridX + c * (cardW + gap);
                    int cy = startY + r * (cardH + gap);
                    if (cy + cardH < gridY || cy > gridY + gridH) continue;

                    boolean isSelected = manager.getActiveBackground() == entry || highlightedEntry == entry;
                    boolean hov = MathUtils.isHovered(mouseX, mouseY, cx, cy, cardW, cardH);
                    GlassRenderer.drawGlassPanel(context, cx, cy, cardW, cardH, true, hov, isSelected);

                    Identifier tex = entry.getCurrentFrameTexture();
                    if (tex != null) {
                        try {
                            context.drawTexture(RenderPipelines.GUI_TEXTURED, tex, cx + 3, cy + 3, 0.0f, 0.0f, cardW - 6, cardH - 18, cardW - 6, cardH - 18);
                        } catch (Throwable ignored) {}
                    }

                    String label = (entry.isAnimatedGif() ? "[GIF] " : "") + entry.getDisplayName();
                    context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(label, cardW - 16), cx + 5, cy + cardH - 13, 0xFFFFFFFF); if (manager.getActiveBackground() == entry) { GlassRenderer.drawPillBadge(context, cx + cardW - 16, cy + 5, 12, 12, 0xFF34D399, true); context.drawTextWithShadow(textRenderer, "✓", cx + cardW - 13, cy + 7, 0xFF34D399); }
                }
            });

            gridScroll.renderScrollbar(context, gridX + gridW - 3, gridY, 2, gridH, mouseX, mouseY);

            // Bottom Action Bar: [Apply] [Rename] [Delete]
            int botY = winY + winH - 24;
            int btnW = (winW - 24) / 3;

            boolean appHov = MathUtils.isHovered(mouseX, mouseY, winX + 8, botY, btnW, 18);
            GlassRenderer.drawPillBadge(context, winX + 8, botY, btnW, 18, 0xFFFFFFFF, appHov);
            context.drawTextWithShadow(textRenderer, "Select", winX + 8 + (btnW - textRenderer.getWidth("Select")) / 2, botY + 5, 0xFFFFFFFF);

            int renX = winX + 12 + btnW;
            boolean renHov = MathUtils.isHovered(mouseX, mouseY, renX, botY, btnW, 18);
            GlassRenderer.drawPillBadge(context, renX, botY, btnW, 18, 0xFFFFFFFF, renHov);
            context.drawTextWithShadow(textRenderer, "Rename", renX + (btnW - textRenderer.getWidth("Rename")) / 2, botY + 5, 0xFFFFFFFF);

            int delX = winX + 16 + btnW * 2;
            boolean delHov = MathUtils.isHovered(mouseX, mouseY, delX, botY, btnW, 18);
            GlassRenderer.drawPillBadge(context, delX, botY, btnW, 18, ThemeTokens.STATUS_ERROR, delHov);
            context.drawTextWithShadow(textRenderer, "Delete", delX + (btnW - textRenderer.getWidth("Delete")) / 2, botY + 5, 0xFFFFFFFF);

            // Rename Modal
            if (renaming && highlightedEntry != null) {
                context.fill(0, 0, this.width, this.height, 0x99000000);
                int mw = 220, mh = 76;
                int mx = (this.width - mw) / 2, my = (this.height - mh) / 2;
                GlassRenderer.drawGlassPanel(context, mx, my, mw, mh, true, false, true);
                context.drawTextWithShadow(textRenderer, "Rename Background", mx + 10, my + 8, 0xFFFFFFFF);
                GlassRenderer.drawGlassPanel(context, mx + 10, my + 24, mw - 20, 18, false, false, true);
                context.drawTextWithShadow(textRenderer, renameInput + "|", mx + 14, my + 29, 0xFFFFFFFF);

                int hw = (mw - 26) / 2;
                GlassRenderer.drawPillBadge(context, mx + 10, my + 48, hw, 18, 0xFFFFFFFF, true);
                context.drawTextWithShadow(textRenderer, "Save", mx + 10 + (hw - textRenderer.getWidth("Save")) / 2, my + 53, 0xFFFFFFFF);

                GlassRenderer.drawPillBadge(context, mx + 16 + hw, my + 48, hw, 18, 0xFFFFFFFF, false);
                context.drawTextWithShadow(textRenderer, "Cancel", mx + 16 + hw + (hw - textRenderer.getWidth("Cancel")) / 2, my + 53, theme.getTextSecondary());
            }
        } finally {
            matrices.popMatrix();
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x(), mouseY = click.y();
        if (click.button() != 0 || closing) return super.mouseClicked(click, doubled);

        if (renaming && highlightedEntry != null) {
            int mw = 220, mh = 76;
            int mx = (this.width - mw) / 2, my = (this.height - mh) / 2;
            int hw = (mw - 26) / 2;
            if (MathUtils.isHovered(mouseX, mouseY, mx + 10, my + 48, hw, 18)) {
                manager.renameEntry(highlightedEntry, renameInput);
                highlightedEntry = manager.getActiveBackground();
                renaming = false;
                return true;
            }
            if (MathUtils.isHovered(mouseX, mouseY, mx + 16 + hw, my + 48, hw, 18)) {
                renaming = false;
                return true;
            }
            return true;
        }

        if (MathUtils.isHovered(mouseX, mouseY, winX + 8, winY + 7, 46, 16)) {
            close();
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, winX + winW - 96, winY + 7, 44, 16)) {
            try { Util.getOperatingSystem().open(manager.getBackgroundsDir().toFile()); } catch (Throwable ignored) {}
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, winX + winW - 48, winY + 7, 40, 16)) {
            manager.refreshLibrary();
            return true;
        }

        int gridX = winX + 8, gridY = winY + 32, gridW = winW - 16, gridH = winH - 64;
        if (gridScroll.onMouseClicked(mouseX, mouseY, 0, gridX + gridW - 4, gridY, 4, gridH)) {
            return true;
        }

        if (MathUtils.isHovered(mouseX, mouseY, gridX, gridY, gridW - 4, gridH)) {
            List<BackgroundEntry> items = manager.getEntries();
            int cols = gridW >= 320 ? 3 : 2;
            int gap = 6;
            int cardW = (gridW - 8 - (cols - 1) * gap) / cols;
            int cardH = 64;
            int startY = Math.round(gridY - gridScroll.getScrollOffset());

            if (MathUtils.isHovered(mouseX, mouseY, gridX, startY, cardW, cardH)) {
                highlightedEntry = null;
                manager.setSelectedId("");
                return true;
            }

            for (int i = 0; i < items.size(); i++) {
                BackgroundEntry entry = items.get(i);
                int slot = i + 1;
                int c = slot % cols;
                int r = slot / cols;
                int cx = gridX + c * (cardW + gap);
                int cy = startY + r * (cardH + gap);
                if (MathUtils.isHovered(mouseX, mouseY, cx, cy, cardW, cardH)) {
                    highlightedEntry = entry;
                    manager.setSelectedId(entry.getId());
                    return true;
                }
            }
        }

        int botY = winY + winH - 24;
        int btnW = (winW - 24) / 3;
        if (MathUtils.isHovered(mouseX, mouseY, winX + 8, botY, btnW, 18)) {
            manager.setSelectedId(highlightedEntry != null ? highlightedEntry.getId() : "");
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, winX + 12 + btnW, botY, btnW, 18)) {
            if (highlightedEntry != null) {
                renaming = true;
                renameInput = highlightedEntry.getDisplayName();
            }
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, winX + 16 + btnW * 2, botY, btnW, 18)) {
            if (highlightedEntry != null) {
                manager.deleteEntry(highlightedEntry);
                highlightedEntry = manager.getActiveBackground();
            }
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click c, double dx, double dy) {
        return gridScroll.onMouseDragged(c.x(), c.y(), c.button(), winH - 64) || super.mouseDragged(c, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click c) {
        gridScroll.onMouseReleased(c.button());
        return super.mouseReleased(c);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        gridScroll.onMouseScrolled(mouseX, mouseY, verticalAmount, winX, winY + 32, winW, winH - 64);
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (renaming) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { renaming = false; return true; }
            if (key == GLFW.GLFW_KEY_ENTER && highlightedEntry != null) {
                manager.renameEntry(highlightedEntry, renameInput);
                highlightedEntry = manager.getActiveBackground();
                renaming = false;
                return true;
            }
            if (key == GLFW.GLFW_KEY_BACKSPACE && !renameInput.isEmpty()) {
                renameInput = renameInput.substring(0, renameInput.length() - 1);
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
        if (renaming && chr >= 32 && chr != 127 && renameInput.length() < 28) {
            renameInput += chr;
            return true;
        }
        return super.charTyped(input);
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
