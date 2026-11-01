package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.gui.AboutScreen;
import com.ahmed.banglaclient.gui.ClickGuiScreen;
import com.ahmed.banglaclient.gui.HudEditorScreen;
import com.ahmed.banglaclient.gui.TutorialCenterScreen;
import com.ahmed.banglaclient.integration.MainMenuButtonBridge;
import com.ahmed.banglaclient.integration.ReplayModIntegration;
import com.ahmed.banglaclient.module.visual.CustomPanoramaModule;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Integrates the BANGLA CLIENT Liquid Glass Main Menu directly into Minecraft's TitleScreen.
 * Preserves third-party mod buttons, provides a top-right Theme Switcher ("Switch to Default Minecraft Theme" /
 * "Switch to BANGLA CLIENT Theme"), and renders the custom Liquid Glass layout when active.
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void banglaclient$onRenderTitleScreen(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getConfigManager() == null) {
            return;
        }

        boolean customActive = bangla.getConfigManager().isCustomMainMenuEnabled();
        if (!customActive) {
            return;
        }

        this.renderPanoramaBackground(context, delta);

        var panoMod = bangla.getModuleManager().getModule(CustomPanoramaModule.class);
        if (panoMod.isPresent() && panoMod.get().shouldRenderGradientTint()) {
            context.fillGradient(0, 0, this.width, this.height,
                    panoMod.get().getTopGradientArgb(), panoMod.get().getBottomGradientArgb());
        } else {
            context.fillGradient(0, 0, this.width, this.height, 0x88080A0F, 0xD0080A0F);
        }

        Theme theme = GlassRenderer.getTheme();
        int accent = bangla.getThemeManager().getEffectiveAccentPrimary();

        // Top-Left Session Status Pill & Top-Right Theme Switcher
        String sessionUser = this.client != null && this.client.getSession() != null
                ? "Account: " + this.client.getSession().getUsername()
                : "Offline Session";
        int userW = this.textRenderer.getWidth(sessionUser) + 16;
        GlassRenderer.drawGlassPanel(context, 10, 10, userW, 20, true, false, false);
        context.drawTextWithShadow(this.textRenderer, sessionUser, 18, 16, theme.getTextSecondary());

        renderCornerThemeSwitcher(context, mouseX, mouseY, true);

        // Centerpiece BANGLA CLIENT Liquid Glass Card
        int cardW = 248;
        int cardH = 236;
        int cardX = (this.width - cardW) / 2;
        int cardY = (this.height - cardH) / 2 - 4;

        GlassRenderer.drawGlassPanel(context, cardX, cardY, cardW, cardH, false, false, true);

        String brand = BanglaClient.CLIENT_NAME;
        int brandW = this.textRenderer.getWidth(brand);
        context.drawTextWithShadow(this.textRenderer, brand, cardX + (cardW - brandW) / 2, cardY + 14, accent);

        String subBrand = "Minecraft 1.21.11 • v" + BanglaClient.CLIENT_VERSION;
        int subW = this.textRenderer.getWidth(subBrand);
        context.drawTextWithShadow(this.textRenderer, subBrand, cardX + (cardW - subW) / 2, cardY + 26, theme.getTextSecondary());

        GlassRenderer.drawHorizontalDivider(context, cardX + 16, cardY + 40, cardW - 32);

        int btnX = cardX + 16;
        int btnW = cardW - 32;
        int btnY = cardY + 48;

        renderGlassMenuButton(context, "Singleplayer", btnX, btnY, btnW, 22, mouseX, mouseY, true);
        btnY += 26;
        renderGlassMenuButton(context, "Multiplayer", btnX, btnY, btnW, 22, mouseX, mouseY, false);
        btnY += 26;
        renderGlassMenuButton(context, "Minecraft Realms", btnX, btnY, btnW, 22, mouseX, mouseY, false);
        btnY += 26;

        // Split row: [Click GUI] | [HUD Editor] | [Tutorial]
        int thirdW = (btnW - 8) / 3;
        renderGlassMenuButton(context, "Click GUI", btnX, btnY, thirdW, 20, mouseX, mouseY, true);
        renderGlassMenuButton(context, "HUD Editor", btnX + thirdW + 4, btnY, thirdW, 20, mouseX, mouseY, false);
        renderGlassMenuButton(context, "Tutorials", btnX + (thirdW + 4) * 2, btnY, btnW - (thirdW + 4) * 2, 20, mouseX, mouseY, false);
        btnY += 24;

        // Split row: [Options] | [About] | [Quit]
        renderGlassMenuButton(context, "Options", btnX, btnY, thirdW, 20, mouseX, mouseY, false);
        renderGlassMenuButton(context, "About", btnX + thirdW + 4, btnY, thirdW, 20, mouseX, mouseY, false);
        renderGlassMenuButton(context, "Quit Game", btnX + (thirdW + 4) * 2, btnY, btnW - (thirdW + 4) * 2, 20, mouseX, mouseY, false);
        btnY += 24;

        // Bottom Utility Row: [Lang] | [Access] | [Replay / Stats]
        boolean replayLoaded = ReplayModIntegration.isReplayModLoaded();
        renderGlassMenuButton(context, "Language", btnX, btnY, thirdW, 18, mouseX, mouseY, false);
        renderGlassMenuButton(context, "Access", btnX + thirdW + 4, btnY, thirdW, 18, mouseX, mouseY, false);
        renderGlassMenuButton(context, replayLoaded ? "Replay" : "Stats", btnX + (thirdW + 4) * 2, btnY, btnW - (thirdW + 4) * 2, 18, mouseX, mouseY, false);

        // Dynamic Third-Party Mod Buttons Dock (Mod Menu, Iris, ReplayMod, etc.)
        List<ClickableWidget> externalButtons = MainMenuButtonBridge.collectThirdPartyButtons(this);
        if (!externalButtons.isEmpty()) {
            int dockX = 10;
            int dockY = 38;
            context.drawTextWithShadow(this.textRenderer, "Mod Extensions:", dockX + 2, dockY, theme.getTextMuted());
            dockY += 12;
            for (ClickableWidget ext : externalButtons) {
                String extLabel = ext.getMessage() != null && !ext.getMessage().getString().isBlank()
                        ? ext.getMessage().getString()
                        : "Mod Button";
                int extW = Math.max(92, this.textRenderer.getWidth(extLabel) + 16);
                renderGlassMenuButton(context, extLabel, dockX, dockY, extW, 20, mouseX, mouseY, false);
                dockY += 24;
            }
        }

        // Render active notifications on Main Menu if any
        if (bangla.getNotificationManager() != null) {
            bangla.getNotificationManager().render(context, this.width, this.height);
        }

        String footer = BanglaClient.CLIENT_NAME + " v" + BanglaClient.CLIENT_VERSION + " — Developed by " + BanglaClient.DEVELOPER;
        context.drawTextWithShadow(this.textRenderer, footer, 10, this.height - 14, theme.getTextSecondary());

        ci.cancel();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void banglaclient$onRenderVanillaTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla != null && bangla.getConfigManager() != null && !bangla.getConfigManager().isCustomMainMenuEnabled()) {
            renderCornerThemeSwitcher(context, mouseX, mouseY, false);
        }
    }

    private void renderCornerThemeSwitcher(DrawContext context, int mouseX, int mouseY, boolean customActive) {
        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary();
        String label = customActive
                ? "Switch to Default Minecraft Theme"
                : "Switch to BANGLA CLIENT Theme";
        int btnW = this.textRenderer.getWidth(label) + 16;
        int btnH = 20;
        int btnX = this.width - btnW - 10;
        int btnY = 10;
        boolean hovered = MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH);

        GlassRenderer.drawGlassPanel(context, btnX, btnY, btnW, btnH, true, hovered, hovered);
        context.drawTextWithShadow(this.textRenderer, label, btnX + 8, btnY + 6,
                hovered ? accent : theme.getTextPrimary());
    }

    private void renderGlassMenuButton(
            DrawContext context,
            String label,
            int x,
            int y,
            int w,
            int h,
            int mouseX,
            int mouseY,
            boolean primaryHighlight
    ) {
        Theme theme = GlassRenderer.getTheme();
        int accent = BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary();
        boolean hovered = MathUtils.isHovered(mouseX, mouseY, x, y, w, h);

        GlassRenderer.drawGlassPanel(context, x, y, w, h, true, hovered, primaryHighlight || hovered);
        int lw = this.textRenderer.getWidth(label);
        int textColor = hovered ? accent : theme.getTextPrimary();
        context.drawTextWithShadow(this.textRenderer, label, x + (w - lw) / 2, y + (h - 8) / 2, textColor);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void banglaclient$onMouseClickedTitleScreen(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0 || this.client == null) {
            return;
        }
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getConfigManager() == null) {
            return;
        }

        boolean customActive = bangla.getConfigManager().isCustomMainMenuEnabled();
        String switchLabel = customActive
                ? "Switch to Default Minecraft Theme"
                : "Switch to BANGLA CLIENT Theme";
        int switchW = this.textRenderer.getWidth(switchLabel) + 16;
        if (MathUtils.isHovered(mouseX, mouseY, this.width - switchW - 10, 10, switchW, 20)) {
            bangla.getConfigManager().setCustomMainMenuEnabled(!customActive);
            cir.setReturnValue(true);
            return;
        }

        if (!customActive) {
            return;
        }

        int cardW = 248;
        int cardH = 236;
        int cardX = (this.width - cardW) / 2;
        int cardY = (this.height - cardH) / 2 - 4;
        int btnX = cardX + 16;
        int btnW = cardW - 32;
        int btnY = cardY + 48;

        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, 22)) {
            this.client.setScreen(new SelectWorldScreen(this));
            cir.setReturnValue(true);
            return;
        }
        btnY += 26;

        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, 22)) {
            this.client.setScreen(new MultiplayerScreen(this));
            cir.setReturnValue(true);
            return;
        }
        btnY += 26;

        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, btnW, 22)) {
            this.client.setScreen(new RealmsMainScreen(this));
            cir.setReturnValue(true);
            return;
        }
        btnY += 26;

        int thirdW = (btnW - 8) / 3;
        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, thirdW, 20)) {
            this.client.setScreen(new ClickGuiScreen(false));
            cir.setReturnValue(true);
            return;
        }
        if (MathUtils.isHovered(mouseX, mouseY, btnX + thirdW + 4, btnY, thirdW, 20)) {
            this.client.setScreen(new HudEditorScreen(this));
            cir.setReturnValue(true);
            return;
        }
        if (MathUtils.isHovered(mouseX, mouseY, btnX + (thirdW + 4) * 2, btnY, btnW - (thirdW + 4) * 2, 20)) {
            this.client.setScreen(new TutorialCenterScreen(this));
            cir.setReturnValue(true);
            return;
        }
        btnY += 24;

        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, thirdW, 20)) {
            this.client.setScreen(new OptionsScreen(this, this.client.options));
            cir.setReturnValue(true);
            return;
        }
        if (MathUtils.isHovered(mouseX, mouseY, btnX + thirdW + 4, btnY, thirdW, 20)) {
            this.client.setScreen(new AboutScreen(this));
            cir.setReturnValue(true);
            return;
        }
        if (MathUtils.isHovered(mouseX, mouseY, btnX + (thirdW + 4) * 2, btnY, btnW - (thirdW + 4) * 2, 20)) {
            this.client.scheduleStop();
            cir.setReturnValue(true);
            return;
        }
        btnY += 24;

        if (MathUtils.isHovered(mouseX, mouseY, btnX, btnY, thirdW, 18)) {
            this.client.setScreen(new LanguageOptionsScreen(this, this.client.options, this.client.getLanguageManager()));
            cir.setReturnValue(true);
            return;
        }
        if (MathUtils.isHovered(mouseX, mouseY, btnX + thirdW + 4, btnY, thirdW, 18)) {
            this.client.setScreen(new AccessibilityOptionsScreen(this, this.client.options));
            cir.setReturnValue(true);
            return;
        }
        if (MathUtils.isHovered(mouseX, mouseY, btnX + (thirdW + 4) * 2, btnY, btnW - (thirdW + 4) * 2, 18)) {
            if (ReplayModIntegration.isReplayModLoaded()) {
                ReplayModIntegration.openReplayViewerIfAvailable(this);
            } else {
                ReplayModIntegration.openStatisticsScreen(this);
            }
            cir.setReturnValue(true);
            return;
        }

        List<ClickableWidget> externalButtons = MainMenuButtonBridge.collectThirdPartyButtons(this);
        if (!externalButtons.isEmpty()) {
            int dockX = 10;
            int dockY = 50;
            for (ClickableWidget ext : externalButtons) {
                String extLabel = ext.getMessage() != null && !ext.getMessage().getString().isBlank()
                        ? ext.getMessage().getString()
                        : "Mod Button";
                int extW = Math.max(92, this.textRenderer.getWidth(extLabel) + 16);
                if (MathUtils.isHovered(mouseX, mouseY, dockX, dockY, extW, 20)) {
                    ext.onClick(mouseX, mouseY);
                    cir.setReturnValue(true);
                    return;
                }
                dockY += 24;
            }
        }

        cir.setReturnValue(true);
    }
}
