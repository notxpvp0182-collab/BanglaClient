package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.cosmetics.PlayerPreviewRenderer;
import com.ahmed.banglaclient.cosmetics.SkinCapeManager;
import com.ahmed.banglaclient.gui.ClickGuiScreen;
import com.ahmed.banglaclient.gui.HudEditorScreen;
import com.ahmed.banglaclient.gui.SkinCustomizationScreen;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.util.ColorUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsScreen;
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
import java.util.HashSet;
import java.util.Set;

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
    private void banglaclient$setupSerialVerticalPauseMenu(CallbackInfo ci) {
        pauseButtons.clear();
        this.clearChildren();

        int w = this.width;
        int h = this.height;

        boolean isSingle = this.client != null && this.client.isInSingleplayer();
        boolean canOpenLan = this.client != null
                && this.client.isIntegratedServerRunning()
                && (this.client.getServer() == null || !this.client.getServer().isRemote());

        boolean wideScreen = (w >= 340);
        int btnW = Math.min(126, w - 40);
        int btnH = 18;
        int btnStep = 20;

        // In singleplayer, 9 buttons; in multiplayer, 8 buttons (Open to LAN omitted)
        int totalRows = isSingle ? 9 : 8;
        int totalMenuH = totalRows * btnStep;

        int logoCenterY = Math.max(16, (h - (totalMenuH + 48)) / 2);
        int btnX = wideScreen ? (w / 2 - 36) : (w / 2 - btnW / 2);
        int btnY = logoCenterY + 44;

        // 1. Back to Game
        registerPauseBtn(ButtonWidget.builder(Text.literal("Back to Game"), b -> {
            if (this.client != null) {
                this.client.setScreen(null);
                this.client.mouse.lockCursor();
            }
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnStep;

        // 2. Advancements
        registerPauseBtn(ButtonWidget.builder(Text.literal("Advancements"), b -> {
            if (this.client != null && this.client.player != null) {
                this.client.setScreen(new AdvancementsScreen(this.client.player.networkHandler.getAdvancementHandler(), this));
            }
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnStep;

        // 3. Statistics
        registerPauseBtn(ButtonWidget.builder(Text.literal("Statistics"), b -> {
            if (this.client != null && this.client.player != null) {
                this.client.setScreen(new StatsScreen(this, this.client.player.getStatHandler()));
            }
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnStep;

        // 4. Mods (Opens Mod Menu)
        registerPauseBtn(ButtonWidget.builder(Text.literal("Mods"), b -> openModMenuScreen())
                .dimensions(btnX, btnY, btnW, btnH).tooltip(Tooltip.of(Text.literal("Open Mod Menu"))).build());
        btnY += btnStep;

        // 5. Cosmetics (Opens Click GUI)
        registerPauseBtn(ButtonWidget.builder(Text.literal("Cosmetics"), b -> {
            if (this.client != null) {
                this.client.setScreen(new ClickGuiScreen(false));
            }
        }).dimensions(btnX, btnY, btnW, btnH).tooltip(Tooltip.of(Text.literal("Open BANGLA CLIENT Click GUI"))).build());
        btnY += btnStep;

        // 6. HUD Editor (Opens HUD Editor)
        registerPauseBtn(ButtonWidget.builder(Text.literal("HUD Editor"), b -> {
            if (this.client != null) {
                this.client.setScreen(new HudEditorScreen(this));
            }
        }).dimensions(btnX, btnY, btnW, btnH).tooltip(Tooltip.of(Text.literal("Open Interactive HUD Editor"))).build());
        btnY += btnStep;

        // 7. Options...
        registerPauseBtn(ButtonWidget.builder(Text.literal("Options..."), b -> {
            if (this.client != null) {
                this.client.setScreen(new OptionsScreen(this, this.client.options));
            }
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnStep;

        // 8. Open to LAN (Singleplayer only)
        if (isSingle) {
            ButtonWidget lanBtn = ButtonWidget.builder(Text.literal("Open to LAN"), b -> {
                if (this.client != null && canOpenLan) {
                    this.client.setScreen(new OpenToLanScreen(this));
                }
            }).dimensions(btnX, btnY, btnW, btnH).build();
            lanBtn.active = canOpenLan;
            registerPauseBtn(lanBtn);
            btnY += btnStep + 1;
        } else {
            btnY += 1;
        }

        // 9. Save and Quit to Title / Disconnect (Red Capsule Button)
        String exitLabel = isSingle ? "Save and Quit to Title" : "Disconnect";
        registerPauseBtn(ButtonWidget.builder(Text.literal(exitLabel), b -> {
            if (this.client != null) {
                this.client.disconnectWithProgressScreen();
            }
        }).dimensions(btnX, btnY, btnW, btnH).build());

        // Right-Side 3D Player Profile Panel (Only rendered if screen is wide enough)
        if (wideScreen) {
            int charCenterX = w - 62;
            int charW = 68;
            int charH = Math.min(115, h - 90);
            int charTopY = Math.max(34, (h - charH) / 2 - 10);

            this.pauseSkinWidget = PlayerPreviewRenderer.createSkinWidget(
                    charW,
                    charH,
                    () -> SkinCapeManager.getInstance().getActiveSkinTextures()
            );
            this.pauseSkinWidget.setPosition(charCenterX - charW / 2, charTopY);
            this.addDrawableChild(this.pauseSkinWidget);

            registerPauseBtn(ButtonWidget.builder(Text.literal("CUSTOMIZE"), b -> {
                if (this.client != null) {
                    this.client.setScreen(new SkinCustomizationScreen(this, SkinCapeManager.getInstance()));
                }
            }).dimensions(charCenterX - 40, charTopY + charH + 4, 80, 18).tooltip(Tooltip.of(Text.literal("Customize 3D Skin & Cape"))).build());
        }
    }

    @Unique
    private void sanitizePauseWidgets() {
        for (Element child : this.children()) {
            if (child instanceof ClickableWidget cw && !pauseButtons.contains(cw) && cw != pauseSkinWidget) {
                cw.visible = false;
                cw.active = false;
                cw.setX(-2000);
                cw.setY(-2000);
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void banglaclient$renderCleanPauseMenu(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        sanitizePauseWidgets();

        BanglaClient bangla = BanglaClient.getInstance();
        int w = this.width;
        int h = this.height;
        boolean isSingle = this.client != null && this.client.isInSingleplayer();
        boolean wideScreen = (w >= 340);
        int centerX = wideScreen ? (w / 2 - 36) : (w / 2);

        int accent = bangla != null && bangla.getThemeManager() != null
                ? bangla.getThemeManager().getEffectiveAccentPrimary()
                : 0xFFEF4444;

        // 1. Live Minecraft Gameplay World Behind (NO static background image)
        BlurRenderer.renderScreenBackdrop(context, w, h, 0.58f);

        // 2. Centered 3D "Bangla Client" Logo (Zero "Game Menu" text overlap)
        int totalRows = isSingle ? 9 : 8;
        int totalMenuH = totalRows * 20;
        int logoCenterY = Math.max(16, (h - (totalMenuH + 48)) / 2);

        drawGoldenCrown(context, centerX - 50, logoCenterY - 16);
        drawRedEnergyRings(context, centerX, logoCenterY + 4, accent);

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(centerX, logoCenterY - 2);
        matrices.scale(2.6f, 2.6f);
        String txtBangla = "Bangla";
        int bx = -this.textRenderer.getWidth(txtBangla) / 2;
        context.drawText(this.textRenderer, txtBangla, bx + 2, 2, 0xFF280404, false);
        context.drawText(this.textRenderer, txtBangla, bx + 1, 1, 0xFF7F1D1D, false);
        context.drawTextWithShadow(this.textRenderer, txtBangla, bx, 0, 0xFFEF4444);
        matrices.popMatrix();

        matrices.pushMatrix();
        matrices.translate(centerX, logoCenterY + 19);
        matrices.scale(2.2f, 2.2f);
        String txtClient = "Client";
        int cx = -this.textRenderer.getWidth(txtClient) / 2;
        context.drawText(this.textRenderer, txtClient, cx + 2, 2, 0xFF0F172A, false);
        context.drawText(this.textRenderer, txtClient, cx + 1, 1, 0xFF475569, false);
        context.drawTextWithShadow(this.textRenderer, txtClient, cx, 0, 0xFFFFFFFF);
        matrices.popMatrix();

        // 3. Right-Side 3D Character & Username Badge
        if (wideScreen) {
            String liveUser = this.client != null && this.client.getSession() != null ? this.client.getSession().getUsername() : "Player";
            int charCenterX = w - 62;
            int nameW = this.textRenderer.getWidth(liveUser);
            int charH = Math.min(115, h - 90);
            int badgeY = Math.max(20, (h - charH) / 2 - 24);

            GlassRenderer.drawPillBadge(context, charCenterX - nameW / 2 - 6, badgeY, nameW + 12, 14, accent, false);
            context.drawTextWithShadow(this.textRenderer, liveUser, charCenterX - nameW / 2, badgeY + 3, 0xFFFFFFFF);

            if (this.pauseSkinWidget != null) {
                this.pauseSkinWidget.render(context, mouseX, mouseY, delta);
            }
        }

        // 4. Render All Serial Pause ButtonWidgets
        for (ClickableWidget cw : pauseButtons) {
            if (cw.visible) {
                cw.render(context, mouseX, mouseY, delta);
            }
        }

        if (bangla != null && bangla.getNotificationManager() != null) {
            bangla.getNotificationManager().render(context, w, h);
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
    private void drawGoldenCrown(DrawContext context, int x, int y) {
        context.fill(x + 2, y, x + 5, y + 3, 0xFFF59E0B);
        context.fill(x + 9, y - 3, x + 12, y + 3, 0xFFF59E0B);
        context.fill(x + 16, y, x + 19, y + 3, 0xFFF59E0B);
        context.fill(x, y + 3, x + 21, y + 9, 0xFFD97706);
        context.fill(x + 2, y + 4, x + 19, y + 8, 0xFFFBBF24);
        context.fill(x + 3, y + 5, x + 5, y + 7, 0xFFEF4444);
        context.fill(x + 9, y + 4, x + 12, y + 7, 0xFF38BDF8);
        context.fill(x + 16, y + 5, x + 18, y + 7, 0xFFEF4444);
    }

    @Unique
    private void drawRedEnergyRings(DrawContext context, int cx, int cy, int accent) {
        int rx = 72;
        context.fill(cx - rx, cy - 2, cx + rx, cy, ColorUtils.withAlpha(accent, 0.50f));
        context.fill(cx - rx + 5, cy + 3, cx + rx - 5, cy + 5, ColorUtils.withAlpha(accent, 0.30f));
        context.fill(cx - rx - 3, cy - 1, cx - rx + 3, cy + 2, 0xFFEF4444);
        context.fill(cx + rx - 3, cy - 1, cx + rx + 3, cy + 2, 0xFFEF4444);
    }
}
