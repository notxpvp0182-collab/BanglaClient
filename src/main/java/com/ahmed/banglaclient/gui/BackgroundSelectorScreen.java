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
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.List;

/**
 * 2D AMOLED Aura Background Selector:
 * - Left-clicking an image only highlights it; clicking [Select] applies it and shows the ✓ checkmark.
 * - No filenames under images (full-bleed clean image cards).
 * - Only [Select] and [Delete] buttons at the bottom (no Rename button).
 * - Clicking [Delete] opens a Confirm / Cancel modal dialog.
 */
public final class BackgroundSelectorScreen extends Screen {
    private final Screen parentScreen;
    private final BackgroundManager manager = BackgroundManager.getInstance();
    private final SmoothScrollContainer gridScroll = new SmoothScrollContainer();
    private final Animation openAnim = new Animation(0.0f, 160L, Easing.EASE_OUT_CUBIC);
    private boolean closing = false;

    private BackgroundEntry highlightedEntry = null;
    private boolean confirmingDelete = false;

    private int winX, winY, winW, winH;

    public BackgroundSelectorScreen(Screen parentScreen) {
        super(Text.literal("Backgrounds"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        this.winW = Math.min(420, Math.max(260, (int) (this.width * 0.72f)));
        this.winH = Math.min(240, Math.max(165, (int) (this.height * 0.72f)));
        this.winX = (this.width - this.winW) / 2;
        this.winY = (this.height - this.winH) / 2;
        this.closing = false;
        this.confirmingDelete = false;
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
        GlassRenderer.playClickSound();
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

            // Header: [← Back] | Backgrounds | [Folder] [Reload]
            boolean backHov = MathUtils.isHovered(mouseX, mouseY, winX + 8, winY + 7, 46, 16);
            GlassRenderer.drawPillBadge(context, winX + 8, winY + 7, 46, 16, 0xFFFFFFFF, backHov);
            GlassRenderer.drawSmallCapsText(context, textRenderer, "← Back", winX + 14, winY + 11, 0xFFFFFFFF);

            GlassRenderer.drawSmallCapsText(context, textRenderer, "Backgrounds", winX + 60, winY + 11, 0xFFFFFFFF);

            int folderX = winX + winW - 96;
            boolean folHov = MathUtils.isHovered(mouseX, mouseY, folderX, winY + 7, 44, 16);
            GlassRenderer.drawPillBadge(context, folderX, winY + 7, 44, 16, 0xFFFFFFFF, folHov);
            GlassRenderer.drawSmallCapsText(context, textRenderer, "Folder", folderX + 8, winY + 11, 0xFFFFFFFF);

            int refX = winX + winW - 48;
            boolean refHov = MathUtils.isHovered(mouseX, mouseY, refX, winY + 7, 40, 16);
            GlassRenderer.drawPillBadge(context, refX, winY + 7, 40, 16, 0xFFFFFFFF, refHov);
            GlassRenderer.drawSmallCapsText(context, textRenderer, "Reload", refX + 6, winY + 11, 0xFFFFFFFF);

            GlassRenderer.drawHorizontalDivider(context, winX + 8, winY + 27, winW - 16);

            // Image Grid Area (Full-bleed image cards with NO filename text)
            int gridX = winX + 8;
            int gridY = winY + 32;
            int gridW = winW - 16;
            int gridH = winH - 64;

            List<BackgroundEntry> items = manager.getEntries();
            int cols = gridW >= 320 ? 3 : 2;
            int gap = 6;
            int cardW = (gridW - 8 - (cols - 1) * gap) / cols;
            int cardH = 60;
            int totalSlots = items.size() + 1;
            int rows = (int) Math.ceil((double) totalSlots / cols);

            gridScroll.tickAndInterpolate();
            gridScroll.updateDimensions(gridH, rows * (cardH + gap) + 6);

            ClipRenderer.renderClipped(context, gridX, gridY, gridW - 4, gridH, () -> {
                int startY = Math.round(gridY - gridScroll.getScrollOffset());

                // Slot 0: Default Minecraft Background Card
                boolean defApplied = (manager.getActiveBackground() == null);
                boolean defHighlighted = (highlightedEntry == null);
                boolean defHov = MathUtils.isHovered(mouseX, mouseY, gridX, startY, cardW, cardH);
                GlassRenderer.drawGlassPanel(context, gridX, startY, cardW, cardH, true, defHov, defHighlighted);
                context.fillGradient(gridX + 3, startY + 3, gridX + cardW - 3, startY + cardH - 3, 0xFF141822, 0xFF07090E);
                GlassRenderer.drawVectorIcon(context, gridX + (cardW - 12) / 2, startY + (cardH - 12) / 2, 12, "WORLD", 0xFF94A3B8);

                if (defApplied) {
                    GlassRenderer.drawPillBadge(context, gridX + cardW - 16, startY + 5, 12, 12, 0xFF34D399, true);
                    context.drawTextWithShadow(textRenderer, "✓", gridX + cardW - 13, startY + 7, 0xFF34D399);
                }

                // Uploaded / Ready-made Image Cards (Pure image, NO text below)
                for (int i = 0; i < items.size(); i++) {
                    BackgroundEntry entry = items.get(i);
                    int slot = i + 1;
                    int c = slot % cols;
                    int r = slot / cols;
                    int cx = gridX + c * (cardW + gap);
                    int cy = startY + r * (cardH + gap);
                    if (cy + cardH < gridY || cy > gridY + gridH) continue;

                    boolean isHighlighted = (highlightedEntry == entry);
                    boolean isApplied = (manager.getActiveBackground() == entry);
                    boolean hov = MathUtils.isHovered(mouseX, mouseY, cx, cy, cardW, cardH);
                    GlassRenderer.drawGlassPanel(context, cx, cy, cardW, cardH, true, hov, isHighlighted);

                    Identifier tex = entry.getCurrentFrameTexture();
                    if (tex != null) {
                        try {
                            context.drawTexture(RenderPipelines.GUI_TEXTURED, tex, cx + 3, cy + 3, 0.0f, 0.0f, cardW - 6, cardH - 6, cardW - 6, cardH - 6);
                        } catch (Throwable ignored) {}
                    }

                    if (isApplied) {
                        GlassRenderer.drawPillBadge(context, cx + cardW - 16, cy + 5, 12, 12, 0xFF34D399, true);
                        context.drawTextWithShadow(textRenderer, "✓", cx + cardW - 13, cy + 7, 0xFF34D399);
                    }
                }
            });

            gridScroll.renderScrollbar(context, gridX + gridW - 3, gridY, 2, gridH, mouseX, mouseY);

            // Bottom Action Bar: ONLY [Select] and [Delete]
            int botY = winY + winH - 24;
            int btnW = (winW - 22) / 2;

            boolean selHov = MathUtils.isHovered(mouseX, mouseY, winX + 8, botY, btnW, 18);
            GlassRenderer.drawPillBadge(context, winX + 8, botY, btnW, 18, 0xFFFFFFFF, selHov || true);
            String selLbl = GlassRenderer.toSmallCaps("Select");
            context.drawTextWithShadow(textRenderer, selLbl, winX + 8 + (btnW - textRenderer.getWidth(selLbl)) / 2, botY + 5, 0xFFFFFFFF);

            int delX = winX + 14 + btnW;
            boolean delHov = MathUtils.isHovered(mouseX, mouseY, delX, botY, btnW, 18);
            GlassRenderer.drawPillBadge(context, delX, botY, btnW, 18, ThemeTokens.STATUS_ERROR, delHov);
            String delLbl = GlassRenderer.toSmallCaps("Delete");
            context.drawTextWithShadow(textRenderer, delLbl, delX + (btnW - textRenderer.getWidth(delLbl)) / 2, botY + 5, 0xFFFFFFFF);

            // Delete Confirmation Modal
            if (confirmingDelete && highlightedEntry != null) {
                context.fill(0, 0, this.width, this.height, 0xB0000000);
                int mw = 210, mh = 68;
                int mx = (this.width - mw) / 2, my = (this.height - mh) / 2;
                GlassRenderer.drawGlassPanel(context, mx, my, mw, mh, true, false, true);

                String title = GlassRenderer.toSmallCaps("Delete this background?");
                context.drawTextWithShadow(textRenderer, title, mx + (mw - textRenderer.getWidth(title)) / 2, my + 14, 0xFFFFFFFF);

                int hw = (mw - 26) / 2;
                boolean cfHov = MathUtils.isHovered(mouseX, mouseY, mx + 10, my + 40, hw, 18);
                GlassRenderer.drawPillBadge(context, mx + 10, my + 40, hw, 18, ThemeTokens.STATUS_ERROR, cfHov || true);
                String cfLbl = GlassRenderer.toSmallCaps("Confirm");
                context.drawTextWithShadow(textRenderer, cfLbl, mx + 10 + (hw - textRenderer.getWidth(cfLbl)) / 2, my + 45, 0xFFFFFFFF);

                boolean cnHov = MathUtils.isHovered(mouseX, mouseY, mx + 16 + hw, my + 40, hw, 18);
                GlassRenderer.drawPillBadge(context, mx + 16 + hw, my + 40, hw, 18, 0xFFFFFFFF, cnHov);
                String cnLbl = GlassRenderer.toSmallCaps("Cancel");
                context.drawTextWithShadow(textRenderer, cnLbl, mx + 16 + hw + (hw - textRenderer.getWidth(cnLbl)) / 2, my + 45, theme.getTextSecondary());
            }
        } finally {
            matrices.popMatrix();
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x(), mouseY = click.y();
        if (click.button() != 0 || closing) return super.mouseClicked(click, doubled);

        // 1. Delete Confirmation Modal Clicks
        if (confirmingDelete && highlightedEntry != null) {
            int mw = 210, mh = 68;
            int mx = (this.width - mw) / 2, my = (this.height - mh) / 2;
            int hw = (mw - 26) / 2;
            if (MathUtils.isHovered(mouseX, mouseY, mx + 10, my + 40, hw, 18)) {
                GlassRenderer.playClickSound();
                manager.deleteEntry(highlightedEntry);
                highlightedEntry = manager.getActiveBackground();
                confirmingDelete = false;
                return true;
            }
            if (MathUtils.isHovered(mouseX, mouseY, mx + 16 + hw, my + 40, hw, 18)) {
                GlassRenderer.playClickSound();
                confirmingDelete = false;
                return true;
            }
            return true;
        }

        if (MathUtils.isHovered(mouseX, mouseY, winX + 8, winY + 7, 46, 16)) {
            GlassRenderer.playClickSound();
            close();
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, winX + winW - 96, winY + 7, 44, 16)) {
            GlassRenderer.playClickSound();
            try { Util.getOperatingSystem().open(manager.getBackgroundsDir().toFile()); } catch (Throwable ignored) {}
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, winX + winW - 48, winY + 7, 40, 16)) {
            GlassRenderer.playClickSound();
            manager.refreshLibrary();
            return true;
        }

        int gridX = winX + 8, gridY = winY + 32, gridW = winW - 16, gridH = winH - 64;
        if (gridScroll.onMouseClicked(mouseX, mouseY, 0, gridX + gridW - 4, gridY, 4, gridH)) {
            return true;
        }

        // 2. Left-clicking an image ONLY highlights it (does NOT select/apply it yet!)
        if (MathUtils.isHovered(mouseX, mouseY, gridX, gridY, gridW - 4, gridH)) {
            List<BackgroundEntry> items = manager.getEntries();
            int cols = gridW >= 320 ? 3 : 2;
            int gap = 6;
            int cardW = (gridW - 8 - (cols - 1) * gap) / cols;
            int cardH = 60;
            int startY = Math.round(gridY - gridScroll.getScrollOffset());

            if (MathUtils.isHovered(mouseX, mouseY, gridX, startY, cardW, cardH)) {
                highlightedEntry = null;
                GlassRenderer.playClickSound();
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
                    GlassRenderer.playClickSound();
                    return true;
                }
            }
        }

        // 3. Bottom [Select] and [Delete] Buttons
        int botY = winY + winH - 24;
        int btnW = (winW - 22) / 2;

        // [Select] -> Applies the highlighted background and shows the ✓ checkmark!
        if (MathUtils.isHovered(mouseX, mouseY, winX + 8, botY, btnW, 18)) {
            GlassRenderer.playClickSound();
            manager.setSelectedId(highlightedEntry != null ? highlightedEntry.getId() : "");
            return true;
        }

        // [Delete] -> Opens Confirm / Cancel popup for the highlighted image
        if (MathUtils.isHovered(mouseX, mouseY, winX + 14 + btnW, botY, btnW, 18)) {
            if (highlightedEntry != null) {
                GlassRenderer.playClickSound();
                confirmingDelete = true;
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
        if (confirmingDelete) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { confirmingDelete = false; return true; }
            if (key == GLFW.GLFW_KEY_ENTER && highlightedEntry != null) {
                manager.deleteEntry(highlightedEntry);
                highlightedEntry = manager.getActiveBackground();
                confirmingDelete = false;
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
    public void close() {
        if (!closing) {
            closing = true;
            openAnim.animateTo(0.0f);
        }
    }

    @Override
    public boolean shouldPause() { return false; }
}
