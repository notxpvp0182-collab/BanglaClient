package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.account.AccountEntry;
import com.ahmed.banglaclient.account.AccountManager;
import com.ahmed.banglaclient.animation.Animation;
import com.ahmed.banglaclient.animation.Easing;
import com.ahmed.banglaclient.cosmetics.PlayerPreviewRenderer;
import com.ahmed.banglaclient.cosmetics.SkinCapeManager;
import com.ahmed.banglaclient.gui.components.SmoothScrollContainer;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.ClipRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 2D Aura Dark Account Switcher with smooth animations, zero extra descriptions,
 * direct Skin Wardrobe access, and Microsoft / Offline account workflows.
 */
public final class AccountSwitcherScreen extends Screen {
    private final Screen parentScreen;
    private final AccountManager accountManager;
    private final SkinCapeManager skinManager;
    private final SmoothScrollContainer listScroll = new SmoothScrollContainer();
    private final Animation openAnim = new Animation(0.0f, 160L, Easing.EASE_OUT_CUBIC);
    private final Animation modalAnim = new Animation(0.0f, 140L, Easing.EASE_OUT_CUBIC);
    private boolean closing = false;

    private PlayerSkinWidget previewSkinWidget;
    private AccountEntry selectedAccount = null;

    private String searchFilter = "";
    private boolean typingSearch = false;

    // 0 = none, 1 = Choose Account Type (Microsoft / Offline), 2 = Offline Name Input, 3 = Edit Name, 4 = Delete Confirm
    private int modalMode = 0;
    private AccountEntry modalTarget = null;
    private String modalInput = "";

    private int winX, winY, winW, winH;

    public AccountSwitcherScreen(Screen parentScreen, AccountManager accountManager, SkinCapeManager skinManager) {
        super(Text.literal("Accounts"));
        this.parentScreen = parentScreen;
        this.accountManager = accountManager;
        this.skinManager = skinManager;
    }

    @Override
    protected void init() {
        this.winW = Math.min(440, Math.max(280, (int) (this.width * 0.76f)));
        this.winH = Math.min(250, Math.max(175, (int) (this.height * 0.76f)));
        this.winX = (this.width - this.winW) / 2;
        this.winY = (this.height - this.winH) / 2;
        this.closing = false;
        this.openAnim.snapTo(0.0f);
        this.openAnim.animateTo(1.0f);

        if (selectedAccount == null) {
            for (AccountEntry a : accountManager.getAccounts()) {
                if (accountManager.isAccountActive(a)) {
                    selectedAccount = a;
                    break;
                }
            }
            if (selectedAccount == null && !accountManager.getAccounts().isEmpty()) {
                selectedAccount = accountManager.getAccounts().get(0);
            }
        }

        int previewW = Math.min(120, winW / 3);
        int bodyH = winH - 36;
        this.previewSkinWidget = PlayerPreviewRenderer.createSkinWidget(
                previewW - 12,
                bodyH - 26,
                this::resolveSkinTexturesForSelected
        );
        this.previewSkinWidget.setPosition(winX + 12, winY + 34);
        this.addDrawableChild(this.previewSkinWidget);
    }

    private SkinTextures resolveSkinTexturesForSelected() {
        if (selectedAccount == null) {
            return DefaultSkinHelper.getSkinTextures(new java.util.UUID(0L, 0L));
        }
        if (accountManager.isAccountActive(selectedAccount)) {
            return skinManager.getActiveSkinTextures();
        }
        return DefaultSkinHelper.getSkinTextures(selectedAccount.getUuid());
    }

    private void openModal(int mode) {
        this.modalMode = mode;
        this.modalAnim.snapTo(0.0f);
        this.modalAnim.animateTo(1.0f);
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

            // Header: [← Back] | Accounts | Active Badge
            boolean backHov = MathUtils.isHovered(mouseX, mouseY, winX + 6, winY + 6, 46, 16);
            GlassRenderer.drawPillBadge(context, winX + 6, winY + 6, 46, 16, 0xFFFFFFFF, backHov);
            context.drawTextWithShadow(textRenderer, "← Back", winX + 12, winY + 10, 0xFFFFFFFF);

            context.drawTextWithShadow(textRenderer, "Account Switcher", winX + 58, winY + 10, 0xFFFFFFFF);

            String activeBadge = accountManager.getActiveUsername();
            int aw = textRenderer.getWidth(activeBadge) + 12;
            GlassRenderer.drawPillBadge(context, winX + winW - aw - 6, winY + 6, aw, 16, 0xFFFFFFFF, true);
            context.drawTextWithShadow(textRenderer, activeBadge, winX + winW - aw, winY + 10, 0xFFFFFFFF);

            GlassRenderer.drawHorizontalDivider(context, winX + 6, winY + 25, winW - 12);

            // Left Panel: 3D Preview + [Select Skin] Button (NO extra description text!)
            int leftW = Math.min(120, winW / 3);
            int bodyY = winY + 30;
            int bodyH = winH - 36;
            GlassRenderer.drawGlassPanel(context, winX + 6, bodyY, leftW, bodyH, true, false, false);

            if (this.previewSkinWidget != null) {
                int sw = Math.max(16, Math.round((leftW - 12) * anim)); int sh = Math.max(16, Math.round((bodyH - 26) * anim)); this.previewSkinWidget.setWidth(sw); this.previewSkinWidget.setHeight(sh); this.previewSkinWidget.setPosition(winX + 6 + (leftW - sw) / 2, bodyY + 4 + (bodyH - 26 - sh) / 2); this.previewSkinWidget.render(context, mouseX, mouseY, delta);
            }

            int skinBtnY = bodyY + bodyH - 18;
            boolean skinHov = MathUtils.isHovered(mouseX, mouseY, winX + 10, skinBtnY, leftW - 8, 14);
            GlassRenderer.drawPillBadge(context, winX + 10, skinBtnY, leftW - 8, 14, 0xFFFFFFFF, skinHov);
            context.drawTextWithShadow(textRenderer, "Select Skin", winX + 10 + (leftW - 8 - textRenderer.getWidth("Select Skin")) / 2, skinBtnY + 3, 0xFFFFFFFF);

            // Right Panel
            int rightX = winX + leftW + 10;
            int rightW = winW - leftW - 16;
            GlassRenderer.drawGlassPanel(context, rightX, bodyY, rightW, bodyH, true, false, false);

            int searchBarY = bodyY + 5;
            int searchW = rightW - 10;
            boolean sHov = MathUtils.isHovered(mouseX, mouseY, rightX + 5, searchBarY, searchW, 15);
            GlassRenderer.drawGlassPanel(context, rightX + 5, searchBarY, searchW, 15, true, sHov, typingSearch);
            String sText = searchFilter.isEmpty() && !typingSearch ? "Search accounts..." : searchFilter + (typingSearch ? "|" : "");
            context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(sText, searchW - 10), rightX + 10, searchBarY + 4, typingSearch ? 0xFFFFFFFF : theme.getTextSecondary());

            int listStartY = searchBarY + 19;
            int listH = Math.max(40, (bodyY + bodyH - 22) - listStartY);

            List<AccountEntry> filtered = new ArrayList<>();
            String q = searchFilter.trim().toLowerCase(Locale.ROOT);
            for (AccountEntry a : accountManager.getAccounts()) {
                if (q.isEmpty() || a.getUsername().toLowerCase(Locale.ROOT).contains(q)) {
                    filtered.add(a);
                }
            }

            listScroll.tickAndInterpolate();
            listScroll.updateDimensions(listH, filtered.size() * 26 + 4);

            ClipRenderer.renderClipped(context, rightX + 4, listStartY, rightW - 8, listH, () -> {
                int rowY = Math.round(listStartY - listScroll.getScrollOffset()) + 2;
                for (AccountEntry acc : filtered) {
                    boolean isActive = accountManager.isAccountActive(acc);
                    boolean isSelected = (selectedAccount == acc);
                    boolean hov = MathUtils.isHovered(mouseX, mouseY, rightX + 5, rowY, rightW - 12, 23);

                    GlassRenderer.drawGlassPanel(context, rightX + 5, rowY, rightW - 12, 23, true, hov, isSelected);
                    SkinTextures tex = isActive ? skinManager.getActiveSkinTextures() : DefaultSkinHelper.getSkinTextures(acc.getUuid());
                    PlayerPreviewRenderer.drawPlayerHead(context, tex, rightX + 9, rowY + 3, 16);

                    context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(acc.getUsername(), rightW - 85), rightX + 30, rowY + 8, 0xFFFFFFFF);

                    if (isActive) {
                        GlassRenderer.drawPillBadge(context, rightX + rightW - 50, rowY + 5, 38, 13, 0xFFFFFFFF, true);
                        context.drawTextWithShadow(textRenderer, "ACTIVE", rightX + rightW - 46, rowY + 8, 0xFFFFFFFF);
                    }
                    rowY += 26;
                }
            });
            listScroll.renderScrollbar(context, rightX + rightW - 4, listStartY, 2, listH, mouseX, mouseY);

            // Bottom Action Buttons: [Login] [Rename] [+ Add] [Delete]
            int bActY = bodyY + bodyH - 19;
            int btnW = (rightW - 19) / 4;

            String[] labels = {"Login", "Rename", "+ Add", "Delete"};
            for (int i = 0; i < 4; i++) {
                int bx = rightX + 5 + i * (btnW + 3);
                boolean hov = MathUtils.isHovered(mouseX, mouseY, bx, bActY, btnW, 15);
                int col = (i == 3) ? ThemeTokens.STATUS_ERROR : 0xFFFFFFFF;
                GlassRenderer.drawPillBadge(context, bx, bActY, btnW, 15, col, hov || i == 2);
                context.drawTextWithShadow(textRenderer, labels[i], bx + (btnW - textRenderer.getWidth(labels[i])) / 2, bActY + 4, 0xFFFFFFFF);
            }

            renderModalDialog(context, mouseX, mouseY, theme);
        } finally {
            matrices.popMatrix();
        }
    }

    private void renderModalDialog(DrawContext context, int mouseX, int mouseY, Theme theme) {
        if (modalMode == 0) return;
        float mVal = modalAnim.getValue();
        context.fill(0, 0, this.width, this.height, ((int) (140 * mVal) << 24));

        int mW = 220, mH = 82;
        int mX = (this.width - mW) / 2;
        int mY = (this.height - mH) / 2;
        GlassRenderer.drawGlassPanel(context, mX, mY, mW, mH, true, false, true);

        if (modalMode == 1) { // Step 1: Choose Microsoft or Offline
            context.drawTextWithShadow(textRenderer, "Add Account", mX + 12, mY + 10, 0xFFFFFFFF);
            int hw = (mW - 28) / 2;
            boolean msHov = MathUtils.isHovered(mouseX, mouseY, mX + 10, mY + 28, hw, 20);
            GlassRenderer.drawPillBadge(context, mX + 10, mY + 28, hw, 20, 0xFFFFFFFF, msHov || true);
            context.drawTextWithShadow(textRenderer, "Microsoft", mX + 10 + (hw - textRenderer.getWidth("Microsoft")) / 2, mY + 34, 0xFFFFFFFF);

            boolean offHov = MathUtils.isHovered(mouseX, mouseY, mX + 18 + hw, mY + 28, hw, 20);
            GlassRenderer.drawPillBadge(context, mX + 18 + hw, mY + 28, hw, 20, 0xFFFFFFFF, offHov || true);
            context.drawTextWithShadow(textRenderer, "Offline", mX + 18 + hw + (hw - textRenderer.getWidth("Offline")) / 2, mY + 34, 0xFFFFFFFF);

            GlassRenderer.drawPillBadge(context, mX + 10, mY + 54, mW - 20, 16, 0xFFFFFFFF, false);
            context.drawTextWithShadow(textRenderer, "Cancel", mX + (mW - textRenderer.getWidth("Cancel")) / 2, mY + 58, theme.getTextSecondary());
        } else if (modalMode == 2 || modalMode == 3) { // Offline Name Input OR Rename Account
            String header = (modalMode == 2) ? "Enter Offline Username" : "Rename Account";
            context.drawTextWithShadow(textRenderer, header, mX + 12, mY + 10, 0xFFFFFFFF);
            GlassRenderer.drawGlassPanel(context, mX + 10, mY + 26, mW - 20, 18, false, false, true);
            String disp = modalInput.isEmpty() ? "Username..." : modalInput + "|";
            context.drawTextWithShadow(textRenderer, disp, mX + 15, mY + 31, 0xFFFFFFFF);

            int hw = (mW - 28) / 2;
            GlassRenderer.drawPillBadge(context, mX + 10, mY + 50, hw, 18, 0xFFFFFFFF, true);
            context.drawTextWithShadow(textRenderer, "Save", mX + 10 + (hw - textRenderer.getWidth("Save")) / 2, mY + 55, 0xFFFFFFFF);

            GlassRenderer.drawPillBadge(context, mX + 18 + hw, mY + 50, hw, 18, 0xFFFFFFFF, false);
            context.drawTextWithShadow(textRenderer, "Cancel", mX + 18 + hw + (hw - textRenderer.getWidth("Cancel")) / 2, mY + 55, theme.getTextSecondary());
        } else if (modalMode == 4) { // Delete Confirmation (No extra description!)
            String u = modalTarget != null ? modalTarget.getUsername() : "";
            context.drawTextWithShadow(textRenderer, "Delete '" + u + "'?", mX + 12, mY + 18, 0xFFFFFFFF);

            int hw = (mW - 28) / 2;
            GlassRenderer.drawPillBadge(context, mX + 10, mY + 46, hw, 18, 0xFFFFFFFF, false);
            context.drawTextWithShadow(textRenderer, "Cancel", mX + 10 + (hw - textRenderer.getWidth("Cancel")) / 2, mY + 51, 0xFFFFFFFF);

            GlassRenderer.drawPillBadge(context, mX + 18 + hw, mY + 46, hw, 18, ThemeTokens.STATUS_ERROR, true);
            context.drawTextWithShadow(textRenderer, "Delete", mX + 18 + hw + (hw - textRenderer.getWidth("Delete")) / 2, mY + 51, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x(), mouseY = click.y();
        if (click.button() != 0 || closing) return super.mouseClicked(click, doubled); GlassRenderer.playClickSound();

        if (modalMode != 0) {
            int mW = 220, mH = 82;
            int mX = (this.width - mW) / 2, mY = (this.height - mH) / 2;
            int hw = (mW - 28) / 2;

            if (modalMode == 1) {
                if (MathUtils.isHovered(mouseX, mouseY, mX + 10, mY + 28, hw, 20)) {
                    try { Util.getOperatingSystem().open("https://www.microsoft.com/link"); } catch (Throwable ignored) {}
                    modalMode = 0;
                    return true;
                }
                if (MathUtils.isHovered(mouseX, mouseY, mX + 18 + hw, mY + 28, hw, 20)) {
                    modalInput = "";
                    openModal(2);
                    return true;
                }
                if (MathUtils.isHovered(mouseX, mouseY, mX + 10, mY + 54, mW - 20, 16)) {
                    modalMode = 0;
                    return true;
                }
            } else if (modalMode == 2 || modalMode == 3) {
                if (MathUtils.isHovered(mouseX, mouseY, mX + 10, mY + 50, hw, 18)) {
                    if (modalMode == 2) {
                        accountManager.addOfflineAccount(modalInput);
                    } else if (modalTarget != null) {
                        accountManager.editOfflineAccount(modalTarget, modalInput);
                    }
                    modalMode = 0;
                    modalInput = "";
                    return true;
                }
                if (MathUtils.isHovered(mouseX, mouseY, mX + 18 + hw, mY + 50, hw, 18)) {
                    modalMode = 0;
                    return true;
                }
            } else if (modalMode == 4) {
                if (MathUtils.isHovered(mouseX, mouseY, mX + 10, mY + 46, hw, 18)) {
                    modalMode = 0;
                    modalTarget = null;
                    return true;
                }
                if (MathUtils.isHovered(mouseX, mouseY, mX + 18 + hw, mY + 46, hw, 18)) {
                    if (modalTarget != null) {
                        accountManager.removeAccount(modalTarget);
                        if (selectedAccount == modalTarget) {
                            selectedAccount = accountManager.getAccounts().isEmpty() ? null : accountManager.getAccounts().get(0);
                        }
                    }
                    modalMode = 0;
                    modalTarget = null;
                    return true;
                }
            }
            return true;
        }

        if (MathUtils.isHovered(mouseX, mouseY, winX + 6, winY + 6, 46, 16)) {
            close();
            return true;
        }

        int leftW = Math.min(120, winW / 3);
        int bodyY = winY + 30;
        int bodyH = winH - 36;

        // [Select Skin] button on left panel
        if (MathUtils.isHovered(mouseX, mouseY, winX + 10, bodyY + bodyH - 18, leftW - 8, 14)) {
            if (selectedAccount != null) {
                accountManager.switchToAccount(selectedAccount);
            }
            if (this.client != null) {
                this.client.setScreen(new SkinCustomizationScreen(this, skinManager));
            }
            return true;
        }

        int rightX = winX + leftW + 10;
        int rightW = winW - leftW - 16;
        int searchBarY = bodyY + 5;
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 5, searchBarY, rightW - 10, 15)) {
            typingSearch = true;
            return true;
        } else {
            typingSearch = false;
        }

        int listStartY = searchBarY + 19;
        int listH = Math.max(40, (bodyY + bodyH - 22) - listStartY);
        if (listScroll.onMouseClicked(mouseX, mouseY, 0, rightX + rightW - 5, listStartY, 4, listH)) {
            return true;
        }

        if (MathUtils.isHovered(mouseX, mouseY, rightX + 4, listStartY, rightW - 8, listH)) {
            List<AccountEntry> filtered = new ArrayList<>();
            String q = searchFilter.trim().toLowerCase(Locale.ROOT);
            for (AccountEntry a : accountManager.getAccounts()) {
                if (q.isEmpty() || a.getUsername().toLowerCase(Locale.ROOT).contains(q)) filtered.add(a);
            }
            int rowY = Math.round(listStartY - listScroll.getScrollOffset()) + 2;
            for (AccountEntry acc : filtered) {
                if (MathUtils.isHovered(mouseX, mouseY, rightX + 5, rowY, rightW - 12, 23)) {
                    selectedAccount = acc;
                    if (doubled) accountManager.switchToAccount(acc);
                    return true;
                }
                rowY += 26;
            }
        }

        int bActY = bodyY + bodyH - 19;
        int btnW = (rightW - 19) / 4;
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 5, bActY, btnW, 15)) {
            if (selectedAccount != null) accountManager.switchToAccount(selectedAccount);
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 5 + (btnW + 3), bActY, btnW, 15)) {
            if (selectedAccount != null && selectedAccount.getType() == AccountEntry.AccountType.OFFLINE) {
                modalTarget = selectedAccount;
                modalInput = selectedAccount.getUsername();
                openModal(3);
            }
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 5 + (btnW + 3) * 2, bActY, btnW, 15)) {
            openModal(1);
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 5 + (btnW + 3) * 3, bActY, btnW, 15)) {
            if (selectedAccount != null) {
                modalTarget = selectedAccount;
                openModal(4);
            }
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click c, double dx, double dy) {
        return listScroll.onMouseDragged(c.x(), c.y(), c.button(), winH - 76) || super.mouseDragged(c, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click c) {
        listScroll.onMouseReleased(c.button());
        return super.mouseReleased(c);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        listScroll.onMouseScrolled(mouseX, mouseY, verticalAmount, winX, winY + 50, winW, winH - 76);
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (modalMode != 0) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { modalMode = 0; modalTarget = null; return true; }
            if (key == GLFW.GLFW_KEY_ENTER) {
                if (modalMode == 2 && !modalInput.isBlank()) {
                    accountManager.addOfflineAccount(modalInput);
                    modalMode = 0;
                    modalInput = "";
                } else if (modalMode == 3 && modalTarget != null && !modalInput.isBlank()) {
                    accountManager.editOfflineAccount(modalTarget, modalInput);
                    modalMode = 0;
                }
                return true;
            }
            if (key == GLFW.GLFW_KEY_BACKSPACE && !modalInput.isEmpty()) {
                modalInput = modalInput.substring(0, modalInput.length() - 1);
                return true;
            }
            return true;
        }
        if (typingSearch) {
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) { typingSearch = false; return true; }
            if (key == GLFW.GLFW_KEY_BACKSPACE && !searchFilter.isEmpty()) {
                searchFilter = searchFilter.substring(0, searchFilter.length() - 1);
                return true;
            }
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        char chr = (char) input.codepoint();
        if (modalMode == 2 || modalMode == 3) {
            if (modalInput.length() < 16 && ((chr >= 'a' && chr <= 'z') || (chr >= 'A' && chr <= 'Z') || (chr >= '0' && chr <= '9') || chr == '_')) {
                modalInput += chr;
                return true;
            }
        }
        if (typingSearch && chr >= 32 && chr != 127) {
            searchFilter += chr;
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
