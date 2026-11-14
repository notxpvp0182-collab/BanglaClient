package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.cosmetics.PlayerPreviewRenderer;
import com.ahmed.banglaclient.cosmetics.SkinCapeManager;
import com.ahmed.banglaclient.gui.ClickGuiScreen;
import com.ahmed.banglaclient.gui.HudEditorScreen;
import com.ahmed.banglaclient.gui.SkinCustomizationScreen;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Strictly Dead-Center Serial Vertical Pause Menu in 2D Aura Dark style.
 */
@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    @Unique
    private final Set<ClickableWidget> pauseButtons = new HashSet<>();

    @Unique
    private PlayerSkinWidget pauseSkinWidget;

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Unique
    private void registerPauseBtn(ButtonWidget btn) {
        pauseButtons.add(btn);
        this.addDrawableChild(btn);
    }

    @Inject(method = "initWidgets", at = @At("TAIL"))
    private void banglaclient$setupCenteredSerialPauseMenu(CallbackInfo ci) {
        pauseButtons.clear();
        this.clearChildren();

        int w = this.width;
        int h = this.height;

        boolean isSingle = this.client != null && this.client.isInSingleplayer();
        boolean canOpenLan = this.client != null
                && this.client.isIntegratedServerRunning()
                && (this.client.getServer() == null || !this.client.getServer().isRemote());

        boolean wideScreen = (w >= 340);
        int btnW = Math.min(130, w - 40);
        int btnH = 19;
        int btnStep = 21;

        int totalRows = isSingle ? 9 : 8;
        int totalMenuH = totalRows * btnStep;

        // Strictly dead-center (w / 2) on all screen resolutions!
        int centerX = w / 2;
        int logoCenterY = Math.max(14, (h - (totalMenuH + 28)) / 2);
        int btnX = centerX - (btnW / 2);
        int btnY = logoCenterY + 24;

        registerPauseBtn(ButtonWidget.builder(Text.literal("Back to Game"), b -> {
            if (this.client != null) {
                this.client.setScreen(null);
                this.client.mouse.lockCursor();
            }
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnStep;

        registerPauseBtn(ButtonWidget.builder(Text.literal("Advancements"), b -> {
            if (this.client != null && this.client.player != null) {
                this.client.setScreen(new AdvancementsScreen(this.client.player.networkHandler.getAdvancementHandler(), this));
            }
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnStep;

        registerPauseBtn(ButtonWidget.builder(Text.literal("Statistics"), b -> {
            if (this.client != null && this.client.player != null) {
                this.client.setScreen(new StatsScreen(this, this.client.player.getStatHandler()));
            }
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnStep;

        registerPauseBtn(ButtonWidget.builder(Text.literal("Mods"), b -> openModMenuScreen())
                .dimensions(btnX, btnY, btnW, btnH).tooltip(Tooltip.of(Text.literal("Open Mod Menu"))).build());
        btnY += btnStep;

        registerPauseBtn(ButtonWidget.builder(Text.literal("Cosmetics"), b -> {
            if (this.client != null) this.client.setScreen(new ClickGuiScreen(false));
        }).dimensions(btnX, btnY, btnW, btnH).tooltip(Tooltip.of(Text.literal("Open BANGLA CLIENT ClickGUI"))).build());
        btnY += btnStep;

        registerPauseBtn(ButtonWidget.builder(Text.literal("HUD Editor"), b -> {
            if (this.client != null) this.client.setScreen(new HudEditorScreen(this));
        }).dimensions(btnX, btnY, btnW, btnH).tooltip(Tooltip.of(Text.literal("Open Interactive HUD Editor"))).build());
        btnY += btnStep;

        registerPauseBtn(ButtonWidget.builder(Text.literal("Options..."), b -> {
            if (this.client != null) this.client.setScreen(new OptionsScreen(this, this.client.options));
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnStep;

        if (isSingle) {
            ButtonWidget lanBtn = ButtonWidget.builder(Text.literal("Open to LAN"), b -> {
                if (this.client != null && canOpenLan) {
                    this.client.setScreen(new OpenToLanScreen(this));
                }
            }).dimensions(btnX, btnY, btnW, btnH).build();
            lanBtn.active = canOpenLan;
            registerPauseBtn(lanBtn);
            btnY += btnStep;
        }

        String exitLabel = isSingle ? "Save and Quit to Title" : "Disconnect";
        registerPauseBtn(ButtonWidget.builder(Text.literal(exitLabel), b -> performCleanWorldExit())
                .dimensions(btnX, btnY, btnW, btnH).build());

        if (wideScreen) {
            int charCenterX = w - 62;
            int charW = 68;
            int charH = Math.min(108, h - 90);
            int charTopY = Math.max(34, (h - charH) / 2 - 10);

            try {
                this.pauseSkinWidget = PlayerPreviewRenderer.createSkinWidget(
                        charW,
                        charH,
                        () -> SkinCapeManager.getInstance().getActiveSkinTextures()
                );
                this.pauseSkinWidget.setPosition(charCenterX - charW / 2, charTopY);
                this.addDrawableChild(this.pauseSkinWidget);
            } catch (Throwable t) {
                this.pauseSkinWidget = null;
            }

            registerPauseBtn(ButtonWidget.builder(Text.literal("Wardrobe"), b -> {
                if (this.client != null) {
                    this.client.setScreen(new SkinCustomizationScreen(this, SkinCapeManager.getInstance()));
                }
            }).dimensions(charCenterX - 38, charTopY + charH + 4, 76, 17).build());
        }
    }

    @Unique
    private void sanitizePauseWidgets() {
        List<Element> snapshot = new ArrayList<>(this.children());
        for (Element child : snapshot) {
            if (child instanceof ClickableWidget cw && !pauseButtons.contains(cw) && cw != pauseSkinWidget) {
                cw.visible = false;
                cw.active = false;
                cw.setX(-2000);
                cw.setY(-2000);
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void banglaclient$renderCentered2DAuraPauseMenu(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        sanitizePauseWidgets();

        int w = this.width;
        int h = this.height;
        boolean isSingle = this.client != null && this.client.isInSingleplayer();
        boolean wideScreen = (w >= 340);
        int centerX = w / 2;

        BlurRenderer.renderScreenBackdrop(context, w, h, 1.0f);

        int totalRows = isSingle ? 9 : 8;
        int totalMenuH = totalRows * 21;
        int logoCenterY = Math.max(14, (h - (totalMenuH + 28)) / 2);

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(centerX, logoCenterY + 4);
        matrices.scale(1.65f, 1.65f);
        String titleStr = "Bangla Client";
        int tw = this.textRenderer.getWidth(titleStr);
        context.drawTextWithShadow(this.textRenderer, titleStr, -tw / 2, 0, 0xFFFFFFFF);
        matrices.popMatrix();

        if (wideScreen) {
            String liveUser = this.client != null && this.client.getSession() != null ? this.client.getSession().getUsername() : "Player";
            int charCenterX = w - 62;
            int nameW = this.textRenderer.getWidth(liveUser);
            int charH = Math.min(108, h - 90);
            int badgeY = Math.max(20, (h - charH) / 2 - 24);

            GlassRenderer.drawPillBadge(context, charCenterX - nameW / 2 - 6, badgeY, nameW + 12, 14, 0xFFFFFFFF, false);
            context.drawTextWithShadow(this.textRenderer, liveUser, charCenterX - nameW / 2, badgeY + 3, 0xFFFFFFFF);

            if (this.pauseSkinWidget != null) {
                try {
                    this.pauseSkinWidget.render(context, mouseX, mouseY, delta);
                } catch (Throwable ignored) {}
            }
        }

        for (ClickableWidget cw : pauseButtons) {
            if (cw.visible) {
                cw.render(context, mouseX, mouseY, delta);
            }
        }

        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
            BanglaClient.getInstance().getNotificationManager().render(context, w, h);
        }
        ci.cancel();
    }

    @Unique
    private void openModMenuScreen() {
        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            try {
                Class<?> cls = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
                Constructor<?> ctor = cls.getConstructor(Screen.class);
                if (this.client != null) {
                    this.client.setScreen((Screen) ctor.newInstance(this));
                    return;
                }
            } catch (Throwable ignored) {}
        }
        if (this.client != null) {
            this.client.setScreen(new ClickGuiScreen(false));
        }
    }

    @Unique
    private void performCleanWorldExit() {
        if (this.client == null) return;
        try {
            if (this.client.world != null) {
                this.client.world.disconnect(net.minecraft.text.Text.translatable("menu.savingLevel"));
            }
            this.client.disconnect(new TitleScreen(), false);
        } catch (Throwable t) {
            this.client.setScreen(new TitleScreen());
        }
    }
}
