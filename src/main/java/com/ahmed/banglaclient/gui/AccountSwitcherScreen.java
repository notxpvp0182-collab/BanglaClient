package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.account.AccountEntry;
import com.ahmed.banglaclient.account.AccountManager;
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
import org.lwjgl.glfw.GLFW;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class AccountSwitcherScreen extends Screen {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yy", Locale.US);

    private final Screen parentScreen;
    private final AccountManager accountManager;
    private final SkinCapeManager skinManager;
    private final SmoothScrollContainer listScroll = new SmoothScrollContainer();

    private PlayerSkinWidget previewSkinWidget;
    private AccountEntry selectedAccount = null;

    private String searchFilter = "";
    private boolean typingSearch = false;

    // Modal Dialog States: 0 = none, 1 = Add Account, 2 = Delete Confirmation, 3 = Edit Account
    private int modalMode = 0;
    private AccountEntry modalTarget = null;
    private String modalInput = "";
    private String statusMessage = "Select an account to login or add a new profile.";

    private int winX, winY, winW, winH;

    public AccountSwitcherScreen(Screen parentScreen, AccountManager accountManager, SkinCapeManager skinManager) {
        super(Text.literal("BANGLA CLIENT Account Switcher"));
        this.parentScreen = parentScreen;
        this.accountManager = accountManager;
        this.skinManager = skinManager;
    }

    @Override
    protected void init() {
        this.winW = Math.min(560, Math.max(300, this.width - 24));
        this.winH = Math.min(320, Math.max(190, this.height - 24));
        this.winX = (this.width - this.winW) / 2;
        this.winY = (this.height - this.winH) / 2;

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

        int previewW = Math.min(136, winW / 3);
        int previewH = winH - 96;
        this.previewSkinWidget = PlayerPreviewRenderer.createSkinWidget(
                previewW - 12,
                previewH - 24,
                this::resolveSkinTexturesForSelected
        );
        this.previewSkinWidget.setPosition(winX + 14, winY + 38);
        this.addDrawableChild(this.previewSkinWidget);
    }

    private SkinTextures resolveSkinTexturesForSelected() {
        if (selectedAccount == null) {
            return DefaultSkinHelper.getSkinTextures(DefaultSkinHelper.ZERO_UUID);
        }
        if (accountManager.isAccountActive(selectedAccount)) {
            return skinManager.getActiveSkinTextures();
        }
        return DefaultSkinHelper.getSkinTextures(selectedAccount.getUuid());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        BlurRenderer.renderScreenBackdrop(context, this.width, this.height, 0.85f);
        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : 0xFFEF4444;

        GlassRenderer.drawGlassPanel(context, winX, winY, winW, winH, false, false, true);

        // Header: [← Back] | Title | Active Account Badge
        boolean backHov = MathUtils.isHovered(mouseX, mouseY, winX + 8, winY + 7, 54, 18);
        GlassRenderer.drawPillBadge(context, winX + 8, winY + 7, 54, 18, accent, backHov);
        context.drawTextWithShadow(textRenderer, "← Back", winX + 15, winY + 12, 0xFFFFFFFF);

        context.drawTextWithShadow(textRenderer, "In-Game Account Switcher", winX + 68, winY + 12, theme.getTextPrimary());

        String activeBadge = "Active: " + accountManager.getActiveUsername();
        int aw = textRenderer.getWidth(activeBadge) + 14;
        GlassRenderer.drawPillBadge(context, winX + winW - aw - 8, winY + 7, aw, 18, ThemeTokens.STATUS_SUCCESS, true);
        context.drawTextWithShadow(textRenderer, activeBadge, winX + winW - aw - 1, winY + 12, 0xFFFFFFFF);

        GlassRenderer.drawHorizontalDivider(context, winX + 8, winY + 29, winW - 16);

        // Left Panel (IAS Style 3D Player Preview & Account Meta Card)
        int leftW = Math.min(136, winW / 3);
        int bodyY = winY + 34;
        int bodyH = winH - 64;
        GlassRenderer.drawGlassPanel(context, winX + 8, bodyY, leftW, bodyH, true, false, false);

        if (this.previewSkinWidget != null) {
            this.previewSkinWidget.render(context, mouseX, mouseY, delta);
        }

        // Selected Account Details Box at bottom of left panel
        int metaY = bodyY + bodyH - 38;
        if (selectedAccount != null) {
            String accTypeStr = selectedAccount.getType() == AccountEntry.AccountType.MICROSOFT ? "Premium" : "Offline";
            int typeCol = selectedAccount.getType() == AccountEntry.AccountType.MICROSOFT ? ThemeTokens.STATUS_SUCCESS : 0xFF38BDF8;
            context.drawTextWithShadow(textRenderer, accTypeStr, winX + 14, metaY, typeCol);
            context.drawTextWithShadow(textRenderer, "Used: " + selectedAccount.getUseCount() + "x", winX + 14, metaY + 11, 0xFFFFFFFF);
            context.drawTextWithShadow(textRenderer, DATE_FORMAT.format(new Date(selectedAccount.getLastUsed())), winX + 14, metaY + 22, theme.getTextSecondary());
        }

        // Right Panel (Search, Reload Skins, Account Cards List, and Bottom Actions)
        int rightX = winX + leftW + 14;
        int rightW = winW - leftW - 22;
        GlassRenderer.drawGlassPanel(context, rightX, bodyY, rightW, bodyH, true, false, false);

        // Search Bar & Reload Skins Row
        int searchBarY = bodyY + 6;
        int reloadW = 74;
        int searchW = rightW - reloadW - 18;
        boolean sHov = MathUtils.isHovered(mouseX, mouseY, rightX + 6, searchBarY, searchW, 16);
        GlassRenderer.drawGlassPanel(context, rightX + 6, searchBarY, searchW, 16, true, sHov, typingSearch);
        String sText = searchFilter.isEmpty() && !typingSearch ? "Search..." : searchFilter + (typingSearch ? "|" : "");
        context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(sText, searchW - 10), rightX + 11, searchBarY + 4, typingSearch ? 0xFFFFFFFF : theme.getTextSecondary());

        boolean relHov = MathUtils.isHovered(mouseX, mouseY, rightX + 10 + searchW, searchBarY, reloadW, 16);
        GlassRenderer.drawPillBadge(context, rightX + 10 + searchW, searchBarY, reloadW, 16, accent, relHov);
        context.drawTextWithShadow(textRenderer, "Reload Skins", rightX + 15 + searchW, searchBarY + 4, 0xFFFFFFFF);

        // Filtered Account List
        int listStartY = searchBarY + 22;
        int listH = Math.max(40, (bodyY + bodyH - 28) - listStartY);

        List<AccountEntry> allAccs = accountManager.getAccounts();
        List<AccountEntry> filtered = new ArrayList<>();
        String q = searchFilter.trim().toLowerCase(Locale.ROOT);
        for (AccountEntry a : allAccs) {
            if (q.isEmpty() || a.getUsername().toLowerCase(Locale.ROOT).contains(q)) {
                filtered.add(a);
            }
        }

        listScroll.tickAndInterpolate();
        listScroll.updateDimensions(listH, filtered.size() * 32 + 6);

        final int finalListY = listStartY;
        ClipRenderer.renderClipped(context, rightX + 4, finalListY, rightW - 8, listH, () -> {
            int rowY = Math.round(finalListY - listScroll.getScrollOffset()) + 2;
            for (AccountEntry acc : filtered) {
                boolean isActive = accountManager.isAccountActive(acc);
                boolean isSelected = (selectedAccount == acc);
                boolean hov = MathUtils.isHovered(mouseX, mouseY, rightX + 6, rowY, rightW - 16, 28);

                GlassRenderer.drawGlassPanel(context, rightX + 6, rowY, rightW - 16, 28, true, hov, isSelected);

                // Render 2D Player Head Icon
                SkinTextures tex = isActive ? skinManager.getActiveSkinTextures() : DefaultSkinHelper.getSkinTextures(acc.getUuid());
                PlayerPreviewRenderer.drawPlayerHead(context, tex, rightX + 11, rowY + 4, 20);

                // Username with fixed non-overlapping margin (strictly at rightX + 36)
                int nameMaxW = Math.max(20, rightW - 100);
                String trimmedName = textRenderer.trimToWidth(acc.getUsername(), nameMaxW);
                context.drawTextWithShadow(textRenderer, trimmedName, rightX + 36, rowY + 5, 0xFFFFFFFF);

                // Type badge & Active Indicator
                String typeStr = acc.getType() == AccountEntry.AccountType.MICROSOFT ? "Premium" : "Offline";
                int typeCol = acc.getType() == AccountEntry.AccountType.MICROSOFT ? ThemeTokens.STATUS_SUCCESS : 0xFF38BDF8;
                context.drawTextWithShadow(textRenderer, typeStr, rightX + 36, rowY + 16, typeCol);

                if (isActive) {
                    GlassRenderer.drawPillBadge(context, rightX + rightW - 58, rowY + 6, 44, 14, ThemeTokens.STATUS_SUCCESS, true);
                    context.drawTextWithShadow(textRenderer, "ACTIVE", rightX + rightW - 52, rowY + 9, 0xFFFFFFFF);
                }
                rowY += 32;
            }
        });
        listScroll.renderScrollbar(context, rightX + rightW - 5, finalListY, 3, listH, mouseX, mouseY);

        // Bottom IAS Action Buttons: [Login / Select] [Edit] [+ Add Account] [Delete]
        int bActY = bodyY + bodyH - 22;
        int btnW = (rightW - 20) / 4;

        boolean logHov = MathUtils.isHovered(mouseX, mouseY, rightX + 6, bActY, btnW, 16);
        GlassRenderer.drawPillBadge(context, rightX + 6, bActY, btnW, 16, accent, logHov);
        context.drawTextWithShadow(textRenderer, "Login", rightX + 6 + (btnW - textRenderer.getWidth("Login")) / 2, bActY + 4, 0xFFFFFFFF);

        boolean editHov = MathUtils.isHovered(mouseX, mouseY, rightX + 6 + (btnW + 3), bActY, btnW, 16);
        GlassRenderer.drawPillBadge(context, rightX + 6 + (btnW + 3), bActY, btnW, 16, 0x55FFFFFF, editHov);
        context.drawTextWithShadow(textRenderer, "Edit", rightX + 6 + (btnW + 3) + (btnW - textRenderer.getWidth("Edit")) / 2, bActY + 4, 0xFFFFFFFF);

        boolean addHov = MathUtils.isHovered(mouseX, mouseY, rightX + 6 + (btnW + 3) * 2, bActY, btnW, 16);
        GlassRenderer.drawPillBadge(context, rightX + 6 + (btnW + 3) * 2, bActY, btnW, 16, accent, addHov);
        context.drawTextWithShadow(textRenderer, "+ Add", rightX + 6 + (btnW + 3) * 2 + (btnW - textRenderer.getWidth("+ Add")) / 2, bActY + 4, 0xFFFFFFFF);

        boolean delHov = MathUtils.isHovered(mouseX, mouseY, rightX + 6 + (btnW + 3) * 3, bActY, btnW, 16);
        GlassRenderer.drawPillBadge(context, rightX + 6 + (btnW + 3) * 3, bActY, btnW, 16, ThemeTokens.STATUS_ERROR, delHov);
        context.drawTextWithShadow(textRenderer, "Delete", rightX + 6 + (btnW + 3) * 3 + (btnW - textRenderer.getWidth("Delete")) / 2, bActY + 4, 0xFFFFFFFF);

        // Status bar
        context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(statusMessage, winW - 20), winX + 10, winY + winH - 18, theme.getTextSecondary());

        // Floating Modal Dialogs
        renderModalDialog(context, mouseX, mouseY, theme, accent);
    }

    private void renderModalDialog(DrawContext context, int mouseX, int mouseY, Theme theme, int accent) {
        if (modalMode == 0) return;
        context.fill(0, 0, this.width, this.height, 0x88000000);

        int mW = 270, mH = 100;
        int mX = (this.width - mW) / 2;
        int mY = (this.height - mH) / 2;
        GlassRenderer.drawGlassPanel(context, mX, mY, mW, mH, false, false, true);

        if (modalMode == 1) { // Add Account Dialog
            context.drawTextWithShadow(textRenderer, "Add Account", mX + 14, mY + 10, accent);
            GlassRenderer.drawGlassPanel(context, mX + 14, mY + 26, mW - 28, 18, true, false, true);
            String inputDisp = modalInput.isEmpty() ? "Type username (3-16 chars)..." : modalInput + "|";
            context.drawTextWithShadow(textRenderer, inputDisp, mX + 20, mY + 31, 0xFFFFFFFF);

            int btnHalfW = (mW - 34) / 2;
            boolean addHov = MathUtils.isHovered(mouseX, mouseY, mX + 14, mY + 48, btnHalfW, 18);
            GlassRenderer.drawPillBadge(context, mX + 14, mY + 48, btnHalfW, 18, ThemeTokens.STATUS_SUCCESS, true);
            context.drawTextWithShadow(textRenderer, "+ Add Offline", mX + 14 + (btnHalfW - textRenderer.getWidth("+ Add Offline")) / 2, mY + 53, 0xFFFFFFFF);

            boolean msHov = MathUtils.isHovered(mouseX, mouseY, mX + 20 + btnHalfW, mY + 48, btnHalfW, 18);
            GlassRenderer.drawPillBadge(context, mX + 20 + btnHalfW, mY + 48, btnHalfW, 18, accent, msHov);
            context.drawTextWithShadow(textRenderer, "Microsoft Auth", mX + 20 + btnHalfW + (btnHalfW - textRenderer.getWidth("Microsoft Auth")) / 2, mY + 53, 0xFFFFFFFF);

            boolean canHov = MathUtils.isHovered(mouseX, mouseY, mX + 14, mY + 72, mW - 28, 16);
            GlassRenderer.drawPillBadge(context, mX + 14, mY + 72, mW - 28, 16, 0x44FFFFFF, canHov);
            context.drawTextWithShadow(textRenderer, "Cancel", mX + (mW - textRenderer.getWidth("Cancel")) / 2, mY + 76, 0xFFFFFFFF);
        } else if (modalMode == 2) { // Delete Confirmation Dialog
            context.drawTextWithShadow(textRenderer, "Delete Confirmation", mX + 14, mY + 10, ThemeTokens.STATUS_ERROR);
            String u = modalTarget != null ? modalTarget.getUsername() : "";
            context.drawTextWithShadow(textRenderer, "Delete profile '" + u + "'?", mX + 14, mY + 28, 0xFFFFFFFF);
            context.drawTextWithShadow(textRenderer, "Deleting locally will never delete your Microsoft account.", mX + 14, mY + 42, theme.getTextSecondary());

            int btnHalfW = (mW - 34) / 2;
            boolean canHov = MathUtils.isHovered(mouseX, mouseY, mX + 14, mY + 66, btnHalfW, 18);
            GlassRenderer.drawPillBadge(context, mX + 14, mY + 66, btnHalfW, 18, 0x44FFFFFF, canHov);
            context.drawTextWithShadow(textRenderer, "Cancel", mX + 14 + (btnHalfW - textRenderer.getWidth("Cancel")) / 2, mY + 71, 0xFFFFFFFF);

            boolean delHov = MathUtils.isHovered(mouseX, mouseY, mX + 20 + btnHalfW, mY + 66, btnHalfW, 18);
            GlassRenderer.drawPillBadge(context, mX + 20 + btnHalfW, mY + 66, btnHalfW, 18, ThemeTokens.STATUS_ERROR, true);
            context.drawTextWithShadow(textRenderer, "Confirm Delete", mX + 20 + btnHalfW + (btnHalfW - textRenderer.getWidth("Confirm Delete")) / 2, mY + 71, 0xFFFFFFFF);
        } else if (modalMode == 3) { // Edit Account Dialog
            context.drawTextWithShadow(textRenderer, "Rename Profile", mX + 14, mY + 10, accent);
            GlassRenderer.drawGlassPanel(context, mX + 14, mY + 30, mW - 28, 18, true, false, true);
            context.drawTextWithShadow(textRenderer, modalInput + "|", mX + 20, mY + 35, 0xFFFFFFFF);

            int btnHalfW = (mW - 34) / 2;
            boolean canHov = MathUtils.isHovered(mouseX, mouseY, mX + 14, mY + 66, btnHalfW, 18);
            GlassRenderer.drawPillBadge(context, mX + 14, mY + 66, btnHalfW, 18, 0x44FFFFFF, canHov);
            context.drawTextWithShadow(textRenderer, "Cancel", mX + 14 + (btnHalfW - textRenderer.getWidth("Cancel")) / 2, mY + 71, 0xFFFFFFFF);

            boolean saveHov = MathUtils.isHovered(mouseX, mouseY, mX + 20 + btnHalfW, mY + 66, btnHalfW, 18);
            GlassRenderer.drawPillBadge(context, mX + 20 + btnHalfW, mY + 66, btnHalfW, 18, ThemeTokens.STATUS_SUCCESS, true);
            context.drawTextWithShadow(textRenderer, "Save", mX + 20 + btnHalfW + (btnHalfW - textRenderer.getWidth("Save")) / 2, mY + 71, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x(), mouseY = click.y();
        if (click.button() != 0) return super.mouseClicked(click, doubled);

        // Modal Dialog Clicks
        if (modalMode != 0) {
            int mW = 270, mH = 100;
            int mX = (this.width - mW) / 2;
            int mY = (this.height - mH) / 2;
            int btnHalfW = (mW - 34) / 2;

            if (modalMode == 1) { // Add Account
                if (MathUtils.isHovered(mouseX, mouseY, mX + 14, mY + 48, btnHalfW, 18)) {
                    statusMessage = accountManager.addOfflineAccount(modalInput);
                    modalMode = 0;
                    modalInput = "";
                    return true;
                }
                if (MathUtils.isHovered(mouseX, mouseY, mX + 20 + btnHalfW, mY + 48, btnHalfW, 18)) {
                    try { Util.getOperatingSystem().open("https://www.microsoft.com/link"); } catch (Throwable ignored) {}
                    statusMessage = "Opened Microsoft Device Auth in browser.";
                    modalMode = 0;
                    return true;
                }
                if (MathUtils.isHovered(mouseX, mouseY, mX + 14, mY + 72, mW - 28, 16)) {
                    modalMode = 0;
                    return true;
                }
            } else if (modalMode == 2) { // Delete
                if (MathUtils.isHovered(mouseX, mouseY, mX + 14, mY + 66, btnHalfW, 18)) {
                    modalMode = 0;
                    modalTarget = null;
                    return true;
                }
                if (MathUtils.isHovered(mouseX, mouseY, mX + 20 + btnHalfW, mY + 66, btnHalfW, 18)) {
                    if (modalTarget != null) {
                        String un = modalTarget.getUsername();
                        accountManager.removeAccount(modalTarget);
                        statusMessage = "Removed profile: " + un;
                        if (selectedAccount == modalTarget) {
                            selectedAccount = accountManager.getAccounts().isEmpty() ? null : accountManager.getAccounts().get(0);
                        }
                    }
                    modalMode = 0;
                    modalTarget = null;
                    return true;
                }
            } else if (modalMode == 3) { // Edit
                if (MathUtils.isHovered(mouseX, mouseY, mX + 14, mY + 66, btnHalfW, 18)) {
                    modalMode = 0;
                    modalTarget = null;
                    return true;
                }
                if (MathUtils.isHovered(mouseX, mouseY, mX + 20 + btnHalfW, mY + 66, btnHalfW, 18)) {
                    if (modalTarget != null) {
                        if (accountManager.editOfflineAccount(modalTarget, modalInput)) {
                            statusMessage = "Renamed profile to " + modalInput.trim();
                        } else {
                            statusMessage = "Invalid name! Use 3-16 characters.";
                        }
                    }
                    modalMode = 0;
                    modalTarget = null;
                    return true;
                }
            }
            return true;
        }

        // Back Button
        if (MathUtils.isHovered(mouseX, mouseY, winX + 8, winY + 7, 54, 18)) {
            close();
            return true;
        }

        int leftW = Math.min(136, winW / 3);
        int bodyY = winY + 34;
        int bodyH = winH - 64;
        int rightX = winX + leftW + 14;
        int rightW = winW - leftW - 22;

        // Search Bar Focus
        int searchBarY = bodyY + 6;
        int reloadW = 74;
        int searchW = rightW - reloadW - 18;
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 6, searchBarY, searchW, 16)) {
            typingSearch = true;
            return true;
        } else {
            typingSearch = false;
        }

        // Reload Skins
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 10 + searchW, searchBarY, reloadW, 16)) {
            accountManager.reloadSkins();
            statusMessage = "Reloaded skin library and account textures.";
            return true;
        }

        // Account List Selection Clicks
        int listStartY = searchBarY + 22;
        int listH = Math.max(40, (bodyY + bodyH - 28) - listStartY);
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 4, listStartY, rightW - 8, listH)) {
            List<AccountEntry> allAccs = accountManager.getAccounts();
            List<AccountEntry> filtered = new ArrayList<>();
            String q = searchFilter.trim().toLowerCase(Locale.ROOT);
            for (AccountEntry a : allAccs) {
                if (q.isEmpty() || a.getUsername().toLowerCase(Locale.ROOT).contains(q)) filtered.add(a);
            }
            int rowY = Math.round(listStartY - listScroll.getScrollOffset()) + 2;
            for (AccountEntry acc : filtered) {
                if (MathUtils.isHovered(mouseX, mouseY, rightX + 6, rowY, rightW - 16, 28)) {
                    selectedAccount = acc;
                    if (doubled) {
                        accountManager.switchToAccount(acc);
                        statusMessage = "Switched active account to: " + acc.getUsername();
                    }
                    return true;
                }
                rowY += 32;
            }
        }

        // Bottom Action Buttons
        int bActY = bodyY + bodyH - 22;
        int btnW = (rightW - 20) / 4;

        // [Login / Select]
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 6, bActY, btnW, 16)) {
            if (selectedAccount != null) {
                accountManager.switchToAccount(selectedAccount);
                statusMessage = "Active account: " + selectedAccount.getUsername();
            }
            return true;
        }
        // [Edit]
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 6 + (btnW + 3), bActY, btnW, 16)) {
            if (selectedAccount != null && selectedAccount.getType() == AccountEntry.AccountType.OFFLINE) {
                modalMode = 3;
                modalTarget = selectedAccount;
                modalInput = selectedAccount.getUsername();
            } else {
                statusMessage = "Microsoft profiles cannot be renamed offline.";
            }
            return true;
        }
        // [+ Add Account]
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 6 + (btnW + 3) * 2, bActY, btnW, 16)) {
            modalMode = 1;
            modalInput = "";
            return true;
        }
        // [Delete]
        if (MathUtils.isHovered(mouseX, mouseY, rightX + 6 + (btnW + 3) * 3, bActY, btnW, 16)) {
            if (selectedAccount != null) {
                modalMode = 2;
                modalTarget = selectedAccount;
            }
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        listScroll.onMouseScrolled(mouseX, mouseY, verticalAmount, 0, 0, this.width, this.height);
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (modalMode != 0) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                modalMode = 0;
                modalTarget = null;
                return true;
            }
            if (key == GLFW.GLFW_KEY_ENTER) {
                if (modalMode == 1 && !modalInput.isBlank()) {
                    statusMessage = accountManager.addOfflineAccount(modalInput);
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
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) {
                typingSearch = false;
                return true;
            }
            if (key == GLFW.GLFW_KEY_BACKSPACE && !searchFilter.isEmpty()) {
                searchFilter = searchFilter.substring(0, searchFilter.length() - 1);
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
        if (modalMode == 1 || modalMode == 3) {
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
        if (this.client != null) {
            this.client.setScreen(parentScreen);
        }
    }

    @Override
    public boolean shouldPause() { return false; }
}
