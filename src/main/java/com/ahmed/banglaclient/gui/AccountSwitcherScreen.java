package com.ahmed.banglaclient.gui;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.account.AccountEntry;
import com.ahmed.banglaclient.account.AccountManager;
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
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Dedicated BANGLA CLIENT Account Switcher Screen supporting Offline profiles
 * and legitimate Microsoft session restoration/OAuth browser flow.
 */
public final class AccountSwitcherScreen extends Screen {
    private final Screen parentScreen;
    private final AccountManager accountManager;
    private final SkinCapeManager skinManager;
    private final SmoothScrollContainer listScroll = new SmoothScrollContainer();

    private boolean typingUsername = false;
    private AccountEntry editingEntry = null;
    private String inputUsername = "";
    private String statusMessage = "Select an account to switch or add an Offline / Microsoft profile.";

    private int winX, winY, winW, winH;

    public AccountSwitcherScreen(Screen parentScreen, AccountManager accountManager, SkinCapeManager skinManager) {
        super(Text.literal("BANGLA CLIENT Account Switcher"));
        this.parentScreen = parentScreen;
        this.accountManager = accountManager;
        this.skinManager = skinManager;
    }

    @Override
    protected void init() {
        this.winW = Math.min(460, Math.max(260, this.width - 24));
        this.winH = Math.min(290, Math.max(180, this.height - 24));
        this.winX = (this.width - this.winW) / 2;
        this.winY = (this.height - this.winH) / 2;
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

        context.drawTextWithShadow(textRenderer, "Account Switcher", winX + 70, winY + 12, 0xFFFFFFFF);

        String activeLabel = "Active: " + accountManager.getActiveUsername();
        int aw = textRenderer.getWidth(activeLabel) + 14;
        GlassRenderer.drawPillBadge(context, winX + winW - aw - 8, winY + 7, aw, 18, ThemeTokens.STATUS_SUCCESS, true);
        context.drawTextWithShadow(textRenderer, activeLabel, winX + winW - aw - 1, winY + 12, 0xFFFFFFFF);

        GlassRenderer.drawHorizontalDivider(context, winX + 8, winY + 29, winW - 16);

        // Top Action Bar: [+ Add Offline Account] [Microsoft OAuth Link] [Refresh]
        int barY = winY + 34;
        GlassRenderer.drawPillBadge(context, winX + 8, barY, 122, 18, accent, typingUsername || MathUtils.isHovered(mouseX, mouseY, winX + 8, barY, 122, 18));
        context.drawTextWithShadow(textRenderer, "+ Add Offline Account", winX + 15, barY + 5, 0xFFFFFFFF);

        GlassRenderer.drawPillBadge(context, winX + 136, barY, 126, 18, accent, MathUtils.isHovered(mouseX, mouseY, winX + 136, barY, 126, 18));
        context.drawTextWithShadow(textRenderer, "Microsoft Device Auth", winX + 143, barY + 5, 0xFFFFFFFF);

        GlassRenderer.drawPillBadge(context, winX + 268, barY, 64, 18, accent, MathUtils.isHovered(mouseX, mouseY, winX + 268, barY, 64, 18));
        context.drawTextWithShadow(textRenderer, "Refresh", winX + 281, barY + 5, 0xFFFFFFFF);

        int listY = barY + 22;
        if (typingUsername) {
            GlassRenderer.drawGlassPanel(context, winX + 8, listY, winW - 16, 20, true, false, true);
            String prompt = (editingEntry != null ? "Rename Offline Account: " : "Enter Offline Username (3-16 chars): ")
                    + inputUsername + ((System.currentTimeMillis() / 500L) % 2L == 0L ? "|" : "");
            context.drawTextWithShadow(textRenderer, prompt, winX + 14, listY + 6, 0xFFFFFFFF);

            int okX = winX + winW - 56;
            GlassRenderer.drawPillBadge(context, okX, listY + 2, 44, 16, ThemeTokens.STATUS_SUCCESS, true);
            context.drawTextWithShadow(textRenderer, "Save", okX + 10, listY + 6, 0xFFFFFFFF);
            listY += 24;
        }

        int listH = Math.max(40, (winY + winH - 24) - listY);
        List<AccountEntry> accounts = accountManager.getAccounts();
        net.minecraft.entity.player.SkinTextures activeTextures = skinManager != null ? skinManager.getActiveSkinTextures() : SkinCapeManager.getAuthenticatedAccountDefaultSkin(client);

        listScroll.tickAndInterpolate();
        listScroll.updateDimensions(listH, accounts.size() * 38 + 6);

        int finalListY = listY;
        ClipRenderer.renderClipped(context, winX + 6, finalListY, winW - 14, listH, () -> {
            int rowY = Math.round(finalListY - listScroll.getScrollOffset()) + 2;
            for (AccountEntry acc : accounts) {
                boolean active = accountManager.isAccountActive(acc);
                boolean hov = MathUtils.isHovered(mouseX, mouseY, winX + 8, rowY, winW - 20, 34);
                GlassRenderer.drawGlassPanel(context, winX + 8, rowY, winW - 20, 34, true, hov, active);

                PlayerPreviewRenderer.drawPlayerHead(context, activeTextures, winX + 14, rowY + 5, 24);
                context.drawTextWithShadow(textRenderer, acc.getUsername(), winX + 44, rowY + 6, 0xFFFFFFFF);

                String typeStr = acc.getType().getLabel() + (active ? " • ACTIVE" : "");
                context.drawTextWithShadow(textRenderer, typeStr, winX + 44, rowY + 18,
                        active ? ThemeTokens.STATUS_SUCCESS : theme.getTextSecondary());

                int rightBtnX = winX + winW - 64;
                if (acc.getType() == AccountEntry.AccountType.OFFLINE) {
                    GlassRenderer.drawPillBadge(context, rightBtnX - 88, rowY + 8, 40, 16, accent, false);
                    context.drawTextWithShadow(textRenderer, "Edit", rightBtnX - 78, rowY + 12, 0xFFFFFFFF);

                    GlassRenderer.drawPillBadge(context, rightBtnX - 44, rowY + 8, 40, 16, ThemeTokens.STATUS_ERROR, false);
                    context.drawTextWithShadow(textRenderer, "Del", rightBtnX - 33, rowY + 12, 0xFFFFFFFF);
                }

                GlassRenderer.drawPillBadge(context, rightBtnX, rowY + 8, 48, 16, active ? ThemeTokens.STATUS_SUCCESS : accent, active);
                context.drawTextWithShadow(textRenderer, active ? "Active" : "Switch", rightBtnX + 8, rowY + 12, 0xFFFFFFFF);
                rowY += 38;
            }
        });
        listScroll.renderScrollbar(context, winX + winW - 6, finalListY, 3, listH, mouseX, mouseY);

        context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(statusMessage, winW - 20),
                winX + 10, winY + winH - 16, theme.getTextSecondary());
    }

    private void commitUsernameInput() {
        if (editingEntry != null) {
            if (accountManager.editOfflineAccount(editingEntry, inputUsername)) {
                statusMessage = "Updated account name to " + inputUsername.trim();
            } else {
                statusMessage = "Invalid username! Use 3-16 alphanumeric/underscore characters.";
            }
        } else {
            statusMessage = accountManager.addOfflineAccount(inputUsername);
        }
        typingUsername = false;
        editingEntry = null;
        inputUsername = "";
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x(), mouseY = click.y();
        if (click.button() != 0) return super.mouseClicked(click, doubled);

        if (MathUtils.isHovered(mouseX, mouseY, winX + 8, winY + 7, 54, 18)) {
            close();
            return true;
        }

        int barY = winY + 34;
        if (MathUtils.isHovered(mouseX, mouseY, winX + 8, barY, 122, 18)) {
            typingUsername = !typingUsername;
            editingEntry = null;
            inputUsername = "";
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, winX + 136, barY, 126, 18)) {
            try {
                Util.getOperatingSystem().open("https://www.microsoft.com/link");
            } catch (Throwable ignored) {}
            statusMessage = "Opened official Microsoft OAuth Device Login in browser.";
            return true;
        }
        if (MathUtils.isHovered(mouseX, mouseY, winX + 268, barY, 64, 18)) {
            statusMessage = "Refreshed active session: " + accountManager.getActiveUsername();
            return true;
        }

        int listY = barY + 22;
        if (typingUsername) {
            int okX = winX + winW - 56;
            if (MathUtils.isHovered(mouseX, mouseY, okX, listY + 2, 44, 16)) {
                commitUsernameInput();
                return true;
            }
            listY += 24;
        }

        int listH = Math.max(40, (winY + winH - 24) - listY);
        if (MathUtils.isHovered(mouseX, mouseY, winX + 6, listY, winW - 14, listH)) {
            int rowY = Math.round(listY - listScroll.getScrollOffset()) + 2;
            List<AccountEntry> snapshot = List.copyOf(accountManager.getAccounts());
            int rightBtnX = winX + winW - 64;

            for (AccountEntry acc : snapshot) {
                if (MathUtils.isHovered(mouseX, mouseY, winX + 8, rowY, winW - 20, 34)) {
                    if (acc.getType() == AccountEntry.AccountType.OFFLINE) {
                        if (MathUtils.isHovered(mouseX, mouseY, rightBtnX - 88, rowY + 8, 40, 16)) {
                            editingEntry = acc;
                            inputUsername = acc.getUsername();
                            typingUsername = true;
                            return true;
                        }
                        if (MathUtils.isHovered(mouseX, mouseY, rightBtnX - 44, rowY + 8, 40, 16)) {
                            accountManager.removeAccount(acc);
                            statusMessage = "Removed offline profile: " + acc.getUsername();
                            return true;
                        }
                    }
                    accountManager.switchToAccount(acc);
                    statusMessage = "Switched active account to: " + acc.getUsername();
                    return true;
                }
                rowY += 38;
            }
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        listScroll.onMouseScrolled(mouseX, mouseY, verticalAmount, 0, 0, this.width, this.height);
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (typingUsername) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                typingUsername = false;
                editingEntry = null;
                return true;
            }
            if (key == GLFW.GLFW_KEY_ENTER) {
                commitUsernameInput();
                return true;
            }
            if (key == GLFW.GLFW_KEY_BACKSPACE && !inputUsername.isEmpty()) {
                inputUsername = inputUsername.substring(0, inputUsername.length() - 1);
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
        if (typingUsername && inputUsername.length() < 16 && ((chr >= 'a' && chr <= 'z') || (chr >= 'A' && chr <= 'Z') || (chr >= '0' && chr <= '9') || chr == '_')) {
            inputUsername += chr;
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
