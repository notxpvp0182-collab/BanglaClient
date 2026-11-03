package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.account.AccountManager;
import com.ahmed.banglaclient.cosmetics.PlayerPreviewRenderer;
import com.ahmed.banglaclient.cosmetics.SkinCapeManager;
import com.ahmed.banglaclient.gui.AboutScreen;
import com.ahmed.banglaclient.gui.AccountSwitcherScreen;
import com.ahmed.banglaclient.gui.ClickGuiScreen;
import com.ahmed.banglaclient.gui.HudEditorScreen;
import com.ahmed.banglaclient.gui.SkinCustomizationScreen;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.util.ColorUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
import java.util.Locale;
import java.util.Set;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique
    private static final Identifier BG_IMAGE = Identifier.of("banglaclient", "textures/gui/main_menu_background.png");

    @Unique
    private final Set<ClickableWidget> banglaMenuWidgets = new HashSet<>();

    @Unique
    private final List<ClickableWidget> capturedModWidgets = new ArrayList<>();

    @Unique
    private PlayerSkinWidget mainSkinWidget;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Unique
    private void registerBanglaButton(ButtonWidget btn) {
        banglaMenuWidgets.add(btn);
        this.addDrawableChild(btn);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void banglaclient$setupMainMenu(CallbackInfo ci) {
        banglaMenuWidgets.clear();
        capturedModWidgets.clear();

        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getConfigManager() == null || !bangla.getConfigManager().isCustomMainMenuEnabled()) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("__ICON_BC__"), b -> {
                if (bangla != null && bangla.getConfigManager() != null) {
                    bangla.getConfigManager().setCustomMainMenuEnabled(true);
                    if (this.client != null) this.client.setScreen(new TitleScreen());
                }
            }).dimensions(this.width - 28, 14, 20, 20).tooltip(Tooltip.of(Text.literal("Switch to BANGLA CLIENT Theme"))).build());
            return;
        }

        for (Element child : this.children()) {
            if (child instanceof ClickableWidget cw) {
                capturedModWidgets.add(cw);
            }
        }
        this.clearChildren();

        int w = this.width;
        int h = this.height;
        int centerX = w / 2;

        // 1. Left-Side Installed-Mod Shortcuts (Only present if mod is loaded)
        int leftX = 8, leftW = 78, leftH = 20;
        int leftY = Math.max(64, (h - 96) / 2);

        if (FabricLoader.getInstance().isModLoaded("ias")) {
            registerBanglaButton(ButtonWidget.builder(Text.literal("__LEFT_IAS__:Account"), b -> openAccountSwitcher())
                    .dimensions(leftX, leftY, leftW, leftH).tooltip(Tooltip.of(Text.literal("Account Switcher"))).build());
            leftY += 24;
        }
        if (FabricLoader.getInstance().isModLoaded("replaymod")) {
            registerBanglaButton(ButtonWidget.builder(Text.literal("__LEFT_REPLAY__:Replay Mod"), b -> triggerExternalMod("replay", "replay"))
                    .dimensions(leftX, leftY, leftW, leftH).tooltip(Tooltip.of(Text.literal("Replay Mod Viewer"))).build());
            leftY += 24;
        }
        if (FabricLoader.getInstance().isModLoaded("flashback")) {
            registerBanglaButton(ButtonWidget.builder(Text.literal("__LEFT_FLASHBACK__:Flashback"), b -> triggerExternalMod("flashback", "flashback"))
                    .dimensions(leftX, leftY, leftW, leftH).tooltip(Tooltip.of(Text.literal("Flashback Replays"))).build());
            leftY += 24;
        }
        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            registerBanglaButton(ButtonWidget.builder(Text.literal("__LEFT_MODS__:More Mods"), b -> openModMenuScreen())
                    .dimensions(leftX, leftY, leftW, leftH).tooltip(Tooltip.of(Text.literal("Open Mod Menu"))).build());
        }

        // 2. Top-Right Toolbar (y = 14): [• Username] [Color] [HUD Editor] [Packs] [Info] [BC]
        int topY = 14;
        int trSq = 20, trGap = 4;
        int curRight = w - 10;

        curRight -= trSq;
        registerBanglaButton(ButtonWidget.builder(Text.literal("__ICON_BC__"), b -> {
            bangla.getConfigManager().setCustomMainMenuEnabled(false);
            if (this.client != null) this.client.setScreen(new TitleScreen());
        }).dimensions(curRight, topY, trSq, trSq).tooltip(Tooltip.of(Text.literal("Switch to Default Minecraft Theme"))).build());

        curRight -= (trSq + trGap);
        registerBanglaButton(ButtonWidget.builder(Text.literal("__ICON_INFO__"), b -> {
            if (this.client != null) this.client.setScreen(new AboutScreen(this));
        }).dimensions(curRight, topY, trSq, trSq).tooltip(Tooltip.of(Text.literal("Info & About"))).build());

        curRight -= (trSq + trGap);
        registerBanglaButton(ButtonWidget.builder(Text.literal("__ICON_PACKS__"), b -> {
            if (this.client != null) {
                this.client.setScreen(new PackScreen(
                        this.client.getResourcePackManager(),
                        manager -> {
                            this.client.options.refreshResourcePacks(manager);
                            this.client.setScreen(this);
                        },
                        this.client.getResourcePackDir(),
                        Text.translatable("resourcePack.title")
                ));
            }
        }).dimensions(curRight, topY, trSq, trSq).tooltip(Tooltip.of(Text.literal("Resource Packs"))).build());

        curRight -= (trSq + trGap);
        registerBanglaButton(ButtonWidget.builder(Text.literal("__ICON_HUD__"), b -> {
            if (this.client != null) this.client.setScreen(new HudEditorScreen(this));
        }).dimensions(curRight, topY, trSq, trSq).tooltip(Tooltip.of(Text.literal("HUD Editor"))).build());

        curRight -= (trSq + trGap);
        registerBanglaButton(ButtonWidget.builder(Text.literal("__ICON_COLOR__"), b -> {
            bangla.getThemeManager().cycleNextTheme();
        }).dimensions(curRight, topY, trSq, trSq).tooltip(Tooltip.of(Text.literal("Switch Red / Custom Theme"))).build());

        // Username Pill with strictly non-overlapping width
        String username = this.client != null && this.client.getSession() != null ? this.client.getSession().getUsername() : "Player";
        int headSize = 12, pad = 4;
        int userW = Math.max(62, pad + headSize + pad + this.textRenderer.getWidth(username) + pad + 4);
        curRight -= (userW + trGap);
        registerBanglaButton(ButtonWidget.builder(Text.literal("__USER__:" + username), b -> openAccountSwitcher())
                .dimensions(curRight, topY, userW, trSq).tooltip(Tooltip.of(Text.literal("Account: " + username))).build());

        // 3. Center Main Menu Button Stack (Narrower: 128px, Thicker: 21px, Step: 23px)
        int logoCenterY = Math.max(24, (h - 210) / 2);
        int btnW = Math.min(128, w - 60);
        int btnH = 21;
        int btnStep = 23;
        int btnX = centerX - btnW / 2;
        int btnY = logoCenterY + 56;

        registerBanglaButton(ButtonWidget.builder(Text.literal("Singleplayer"), b -> {
            if (this.client != null) this.client.setScreen(new SelectWorldScreen(this));
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnStep;

        registerBanglaButton(ButtonWidget.builder(Text.literal("Multiplayer"), b -> {
            if (this.client != null) this.client.setScreen(new MultiplayerScreen(this));
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnStep;

        registerBanglaButton(ButtonWidget.builder(Text.literal("Realms"), b -> {
            if (this.client != null) this.client.setScreen(new RealmsMainScreen(this));
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnStep;

        // Cosmetics -> Opens Click GUI
        registerBanglaButton(ButtonWidget.builder(Text.literal("Cosmetics"), b -> {
            if (this.client != null) this.client.setScreen(new ClickGuiScreen(false));
        }).dimensions(btnX, btnY, btnW, btnH).tooltip(Tooltip.of(Text.literal("Open Click GUI & Modules"))).build());
        btnY += btnStep;

        registerBanglaButton(ButtonWidget.builder(Text.literal("Options"), b -> {
            if (this.client != null) this.client.setScreen(new OptionsScreen(this, this.client.options));
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += btnStep + 1;

        registerBanglaButton(ButtonWidget.builder(Text.literal("Quit Game"), b -> {
            if (this.client != null) this.client.scheduleStop();
        }).dimensions(btnX, btnY, btnW, btnH).build());

        // 4. Right Side: 3D PlayerSkinWidget & CUSTOMIZE button
        int charCenterX = w - 68;
        int charW = 76;
        int charH = Math.min(120, h - 90);
        int charTopY = Math.max(38, (h - charH) / 2 - 12);

        try {
            this.mainSkinWidget = PlayerPreviewRenderer.createSkinWidget(
                    charW,
                    charH,
                    () -> SkinCapeManager.getInstance().getActiveSkinTextures()
            );
            this.mainSkinWidget.setPosition(charCenterX - charW / 2, charTopY);
            this.addDrawableChild(this.mainSkinWidget);
        } catch (Throwable t) {
            this.mainSkinWidget = null;
        }

        registerBanglaButton(ButtonWidget.builder(Text.literal("CUSTOMIZE"), b -> {
            if (this.client != null) {
                this.client.setScreen(new SkinCustomizationScreen(this, SkinCapeManager.getInstance()));
            }
        }).dimensions(charCenterX - 42, charTopY + charH + 4, 84, 18).tooltip(Tooltip.of(Text.literal("Customize 3D Skin & Cape"))).build());

        // 5. Bottom-Right BANGLA SMP Card
        registerBanglaButton(ButtonWidget.builder(Text.literal("__BANGLA_SMP__"), b -> {
            if (this.client != null && this.client.keyboard != null) {
                this.client.keyboard.setClipboard("play.banglasmp.net");
                bangla.getNotificationManager().pushSuccess("Bangla SMP", "Copied play.banglasmp.net");
            }
        }).dimensions(w - 122, h - 34, 114, 26).tooltip(Tooltip.of(Text.literal("Click to copy play.banglasmp.net"))).build());
    }

    @Unique
    private void sanitizePostInitWidgets() {
        List<Element> snapshot = new ArrayList<>(this.children());
        for (Element child : snapshot) {
            if (child instanceof ClickableWidget cw && !banglaMenuWidgets.contains(cw) && cw != mainSkinWidget) {
                if (!capturedModWidgets.contains(cw)) {
                    capturedModWidgets.add(cw);
                }
                cw.visible = false;
                cw.active = false;
                cw.setX(-2000);
                cw.setY(-2000);
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void banglaclient$renderImage3MainMenu(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getConfigManager() == null || !bangla.getConfigManager().isCustomMainMenuEnabled()) {
            return;
        }

        sanitizePostInitWidgets();

        int w = this.width;
        int h = this.height;
        int centerX = w / 2;
        int accent = bangla.getThemeManager().getEffectiveAccentPrimary();

        // 1. Fullscreen Custom Cherry Blossom Sunset PNG with Cover Scaling
        int texW = 1280, texH = 720;
        float scale = Math.max((float) w / (float) texW, (float) h / (float) texH);
        int drawW = Math.round(texW * scale);
        int drawH = Math.round(texH * scale);
        int drawX = (w - drawW) / 2;
        int drawY = (h - drawH) / 2;

        try {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, BG_IMAGE, drawX, drawY, 0.0f, 0.0f, drawW, drawH, drawW, drawH);
        } catch (Throwable t) {
            this.renderPanoramaBackground(context, delta);
        }

        context.fillGradient(0, 0, w, h, 0x18000000, 0x44080A10);

        // 2. 3D "Bangla Client" Logo
        int logoCenterY = Math.max(24, (h - 210) / 2);
        drawGoldenCrown(context, centerX - 54, logoCenterY - 18);
        drawRedEnergyRings(context, centerX, logoCenterY + 5, accent);

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(centerX, logoCenterY - 2);
        matrices.scale(2.8f, 2.8f);
        String txtBangla = "Bangla";
        int bx = -this.textRenderer.getWidth(txtBangla) / 2;
        context.drawText(this.textRenderer, txtBangla, bx + 2, 2, 0xFF280404, false);
        context.drawText(this.textRenderer, txtBangla, bx + 1, 1, 0xFF7F1D1D, false);
        context.drawTextWithShadow(this.textRenderer, txtBangla, bx, 0, 0xFFEF4444);
        matrices.popMatrix();

        matrices.pushMatrix();
        matrices.translate(centerX, logoCenterY + 21);
        matrices.scale(2.35f, 2.35f);
        String txtClient = "Client";
        int cx = -this.textRenderer.getWidth(txtClient) / 2;
        context.drawText(this.textRenderer, txtClient, cx + 2, 2, 0xFF0F172A, false);
        context.drawText(this.textRenderer, txtClient, cx + 1, 1, 0xFF475569, false);
        context.drawTextWithShadow(this.textRenderer, txtClient, cx, 0, 0xFFFFFFFF);
        matrices.popMatrix();

        // 3. Username badge above right-side 3D player model
        String liveUser = this.client != null && this.client.getSession() != null ? this.client.getSession().getUsername() : "Player";
        int charCenterX = w - 68;
        int nameW = this.textRenderer.getWidth(liveUser);
        int badgeY = Math.max(22, (h - Math.min(120, h - 90)) / 2 - 28);
        GlassRenderer.drawPillBadge(context, charCenterX - nameW / 2 - 6, badgeY, nameW + 12, 14, accent, false);
        context.drawTextWithShadow(this.textRenderer, liveUser, charCenterX - nameW / 2, badgeY + 3, 0xFFFFFFFF);

        // 4. Render native 3D PlayerSkinWidget
        if (this.mainSkinWidget != null) {
            try {
                this.mainSkinWidget.render(context, mouseX, mouseY, delta);
            } catch (Throwable ignored) {}
        }

        // 5. Render ButtonWidgets (Zero duplicate bottom text!)
        for (ClickableWidget cw : banglaMenuWidgets) {
            if (cw.visible) {
                cw.render(context, mouseX, mouseY, delta);
            }
        }

        if (bangla.getNotificationManager() != null) {
            bangla.getNotificationManager().render(context, w, h);
        }
        ci.cancel();
    }

    @Unique
    private void openAccountSwitcher() {
        if (this.client != null) {
            this.client.setScreen(new AccountSwitcherScreen(this, new AccountManager(), SkinCapeManager.getInstance()));
        }
    }

    @Unique
    private void triggerExternalMod(String classHint, String labelHint) {
        for (ClickableWidget cw : capturedModWidgets) {
            String msg = cw.getMessage() != null ? cw.getMessage().getString().toLowerCase(Locale.ROOT) : "";
            String cls = cw.getClass().getName().toLowerCase(Locale.ROOT);
            if (cls.contains(classHint) || msg.contains(labelHint)) {
                cw.active = true;
                cw.visible = true;
                cw.onClick(new Click(cw.getX() + 2, cw.getY() + 2, new MouseInput(0, 0)), false);
                cw.visible = false;
                cw.active = false;
                return;
            }
        }
        openModMenuScreen();
    }

    @Unique
    private void openModMenuScreen() {
        for (ClickableWidget cw : capturedModWidgets) {
            String msg = cw.getMessage() != null ? cw.getMessage().getString().toLowerCase(Locale.ROOT) : "";
            String cls = cw.getClass().getName().toLowerCase(Locale.ROOT);
            if (msg.contains("mod") || cls.contains("modmenu")) {
                cw.active = true;
                cw.visible = true;
                cw.onClick(new Click(cw.getX() + 2, cw.getY() + 2, new MouseInput(0, 0)), false);
                cw.visible = false;
                cw.active = false;
                return;
            }
        }
        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            try {
                Class<?> cls = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
                Constructor<?> ctor = cls.getConstructor(Screen.class);
                if (this.client != null) this.client.setScreen((Screen) ctor.newInstance(this));
                return;
            } catch (Throwable ignored) {}
        }
        if (this.client != null) this.client.setScreen(new ClickGuiScreen(false));
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
        int rx = 78;
        context.fill(cx - rx, cy - 2, cx + rx, cy, ColorUtils.withAlpha(accent, 0.55f));
        context.fill(cx - rx + 6, cy + 4, cx + rx - 6, cy + 6, ColorUtils.withAlpha(accent, 0.35f));
        context.fill(cx - rx - 4, cy - 1, cx - rx + 4, cy + 2, 0xFFEF4444);
        context.fill(cx + rx - 4, cy - 1, cx + rx + 4, cy + 2, 0xFFEF4444);
    }
}
