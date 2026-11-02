package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.gui.AboutScreen;
import com.ahmed.banglaclient.gui.ClickGuiScreen;
import com.ahmed.banglaclient.gui.HudEditorScreen;
import com.ahmed.banglaclient.util.ColorUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gl.RenderPipelines;
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
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.gui.Click;
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
import java.util.List;
import java.util.Locale;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique
    private static final Identifier BG_IMAGE = Identifier.of("banglaclient", "textures/gui/title_background.png");

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void banglaclient$setupNativeMainMenuButtons(CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getConfigManager() == null) return;

        if (!bangla.getConfigManager().isCustomMainMenuEnabled()) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("__ICON_BC__"), b -> {
                bangla.getConfigManager().setCustomMainMenuEnabled(true);
                if (this.client != null) this.client.setScreen(new TitleScreen());
            }).dimensions(this.width - 28, 8, 20, 20).tooltip(Tooltip.of(Text.literal("Switch to BANGLA CLIENT Theme"))).build());
            return;
        }

        // Capture any third-party mod buttons (ModMenu / IAS) before clearing vanilla buttons & duplicate IAS text labels
        List<ClickableWidget> modButtons = new ArrayList<>();
        for (Element child : this.children()) {
            if (child instanceof ClickableWidget cw && cw.visible) {
                String msg = cw.getMessage() != null ? cw.getMessage().getString().toLowerCase(Locale.ROOT) : "";
                String cls = cw.getClass().getName().toLowerCase(Locale.ROOT);
                if (msg.contains("mod") || cls.contains("modmenu") || msg.contains("account") || cls.contains("ias")) {
                    modButtons.add(cw);
                }
            }
        }

        // Remove all vanilla buttons & duplicate bottom account labels so there is zero hitbox conflict or overlapping text
        this.clearChildren();

        int w = this.width;
        int h = this.height;
        int centerX = w / 2;

        // 1. Top-Left Compact Mod Icon Buttons (Mods & In-Game Account Switcher)
        int tlX = 10, tlY = 10, sq = 20, gap = 5;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("__ICON_MODS__"), b -> openModMenuOrFallback(modButtons, "mod"))
                .dimensions(tlX, tlY, sq, sq).tooltip(Tooltip.of(Text.literal("Mods"))).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("__ICON_IAS__"), b -> openIasOrFallback(modButtons))
                .dimensions(tlX + sq + gap, tlY, sq, sq).tooltip(Tooltip.of(Text.literal("Account Switcher"))).build());

        // 2. Top-Right Toolbar: [• Username] [Color Changer] [HUD Editor] [Resource Packs] [Info] [Theme Switcher]
        int trSq = 20, trGap = 4;
        int curRight = w - 10;

        // [1] Theme Switcher (BC)
        curRight -= trSq;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("__ICON_BC__"), b -> {
            bangla.getConfigManager().setCustomMainMenuEnabled(false);
            if (this.client != null) this.client.setScreen(new TitleScreen());
        }).dimensions(curRight, 10, trSq, trSq).tooltip(Tooltip.of(Text.literal("Switch Menu Theme"))).build());

        // [2] Info / About Button
        curRight -= (trSq + trGap);
        this.addDrawableChild(ButtonWidget.builder(Text.literal("__ICON_INFO__"), b -> {
            if (this.client != null) this.client.setScreen(new AboutScreen(this));
        }).dimensions(curRight, 10, trSq, trSq).tooltip(Tooltip.of(Text.literal("About Bangla Client"))).build());

        // [3] Resource Packs Button
        curRight -= (trSq + trGap);
        this.addDrawableChild(ButtonWidget.builder(Text.literal("__ICON_PACKS__"), b -> {
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
        }).dimensions(curRight, 10, trSq, trSq).tooltip(Tooltip.of(Text.literal("Resource Packs"))).build());

        // [4] Dedicated HUD Editor Button (Requirement #2)
        curRight -= (trSq + trGap);
        this.addDrawableChild(ButtonWidget.builder(Text.literal("__ICON_HUD__"), b -> {
            if (this.client != null) this.client.setScreen(new HudEditorScreen(this));
        }).dimensions(curRight, 10, trSq, trSq).tooltip(Tooltip.of(Text.literal("HUD Editor"))).build());

        // [5] Color / Theme Changer Button
        curRight -= (trSq + trGap);
        this.addDrawableChild(ButtonWidget.builder(Text.literal("__ICON_COLOR__"), b -> {
            bangla.getThemeManager().cycleNextTheme();
        }).dimensions(curRight, 10, trSq, trSq).tooltip(Tooltip.of(Text.literal("Switch Red / Custom Theme"))).build());

        // [6] Top-Right Account Display Pill
        String username = this.client != null && this.client.getSession() != null ? this.client.getSession().getUsername() : "Player";
        int userW = Math.max(58, this.textRenderer.getWidth("• " + username) + 16);
        curRight -= (userW + trGap);
        this.addDrawableChild(ButtonWidget.builder(Text.literal("__USER__:" + username), b -> openIasOrFallback(modButtons))
                .dimensions(curRight, 10, userW, trSq).tooltip(Tooltip.of(Text.literal("Logged in as " + username))).build());

        // 3. Center Main Menu Buttons (Real ButtonWidgets -> 100% Exact Hitboxes!)
        int logoCenterY = Math.max(32, (h - 196) / 2);
        int btnW = Math.min(182, w - 40);
        int btnH = 20;
        int btnX = centerX - btnW / 2;
        int btnY = logoCenterY + 52;

        // Singleplayer
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Singleplayer"), b -> {
            if (this.client != null) this.client.setScreen(new SelectWorldScreen(this));
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += 22;

        // Multiplayer
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Multiplayer"), b -> {
            if (this.client != null) this.client.setScreen(new MultiplayerScreen(this));
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += 22;

        // Realms
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Realms"), b -> {
            if (this.client != null) this.client.setScreen(new RealmsMainScreen(this));
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += 22;

        // Cosmetics -> Opens actual Click GUI screen (Requirement #1)
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cosmetics"), b -> {
            if (this.client != null) this.client.setScreen(new ClickGuiScreen(false));
        }).dimensions(btnX, btnY, btnW, btnH).tooltip(Tooltip.of(Text.literal("Open BANGLA CLIENT Click GUI"))).build());
        btnY += 22;

        // Options
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Options"), b -> {
            if (this.client != null) this.client.setScreen(new OptionsScreen(this, this.client.options));
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += 25;

        // Quit Game (Red Capsule)
        int quitW = Math.min(142, btnW);
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Quit Game"), b -> {
            if (this.client != null) this.client.scheduleStop();
        }).dimensions(centerX - quitW / 2, btnY, quitW, btnH).build());

        // 4. Bottom-Right BANGLA SMP Server Panel Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("__BANGLA_SMP__"), b -> {
            if (this.client != null && this.client.keyboard != null) {
                this.client.keyboard.setClipboard("play.banglasmp.net");
                bangla.getNotificationManager().pushSuccess("Bangla SMP", "Copied play.banglasmp.net");
            }
        }).dimensions(w - 130, h - 38, 120, 28).tooltip(Tooltip.of(Text.literal("Click to copy play.banglasmp.net"))).build());
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void banglaclient$renderMainMenu(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getConfigManager() == null || !bangla.getConfigManager().isCustomMainMenuEnabled()) {
            return;
        }

        int w = this.width;
        int h = this.height;
        int centerX = w / 2;
        int accent = bangla.getThemeManager().getEffectiveAccentPrimary();

        // 1. Render Valid Custom Cherry Blossom Sunset PNG Texture Across Entire Screen
        try {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, BG_IMAGE, 0, 0, 0.0f, 0.0f, w, h, w, h);
        } catch (Throwable t) {
            this.renderPanoramaBackground(context, delta);
        }
        context.fillGradient(0, 0, w, h, 0x26000000, 0x55080A10);

        // 2. Render Centered 3D "Bangla Client" Logo with Golden Crown & Red Energy Rings
        int logoCenterY = Math.max(32, (h - 196) / 2);
        drawGoldenCrown(context, centerX - 56, logoCenterY - 18);
        drawRedEnergyRings(context, centerX, logoCenterY + 4, accent);

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(centerX, logoCenterY);
        matrices.scale(2.8f, 2.8f);
        String txtBangla = "Bangla";
        int bx = -this.textRenderer.getWidth(txtBangla) / 2;
        context.drawText(this.textRenderer, txtBangla, bx + 2, 2, 0xFF2D0404, false);
        context.drawText(this.textRenderer, txtBangla, bx + 1, 1, 0xFF7F1D1D, false);
        context.drawTextWithShadow(this.textRenderer, txtBangla, bx, 0, 0xFFEF4444);
        matrices.popMatrix();

        matrices.pushMatrix();
        matrices.translate(centerX, logoCenterY + 23);
        matrices.scale(2.4f, 2.4f);
        String txtClient = "Client";
        int cx = -this.textRenderer.getWidth(txtClient) / 2;
        context.drawText(this.textRenderer, txtClient, cx + 2, 2, 0xFF0F172A, false);
        context.drawText(this.textRenderer, txtClient, cx + 1, 1, 0xFF475569, false);
        context.drawTextWithShadow(this.textRenderer, txtClient, cx, 0, 0xFFFFFFFF);
        matrices.popMatrix();

        // 3. Render all registered native ButtonWidgets (Zero duplicate bottom text!)
        for (Element child : this.children()) {
            if (child instanceof ClickableWidget cw && cw.visible) {
                cw.render(context, mouseX, mouseY, delta);
            }
        }

        if (bangla.getNotificationManager() != null) {
            bangla.getNotificationManager().render(context, w, h);
        }
        ci.cancel();
    }

    @Unique
    private void openModMenuOrFallback(List<ClickableWidget> captured, String keyword) {
        for (ClickableWidget cw : captured) {
            String msg = cw.getMessage() != null ? cw.getMessage().getString().toLowerCase(Locale.ROOT) : "";
            if (msg.contains(keyword) || cw.getClass().getName().toLowerCase(Locale.ROOT).contains("modmenu")) {
                cw.onClick(new Click(cw.getX() + 2, cw.getY() + 2, new MouseInput(0, 0)), false);
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
    private void openIasOrFallback(List<ClickableWidget> captured) {
        for (ClickableWidget cw : captured) {
            String msg = cw.getMessage() != null ? cw.getMessage().getString().toLowerCase(Locale.ROOT) : "";
            String cls = cw.getClass().getName().toLowerCase(Locale.ROOT);
            if (msg.contains("account") || cls.contains("ias") || cls.contains("account")) {
                cw.onClick(new Click(cw.getX() + 2, cw.getY() + 2, new MouseInput(0, 0)), false);
                return;
            }
        }
        String user = this.client != null && this.client.getSession() != null ? this.client.getSession().getUsername() : "Player";
        BanglaClient.getInstance().getNotificationManager().pushInfo("Account", "Active Account: " + user);
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
        int rx = 76;
        context.fill(cx - rx, cy - 1, cx + rx, cy + 1, ColorUtils.withAlpha(accent, 0.40f));
        context.fill(cx - rx + 4, cy - 2, cx + rx - 4, cy + 2, ColorUtils.withAlpha(accent, 0.22f));
        context.fill(cx - rx - 3, cy, cx - rx + 3, cy + 2, 0xFFEF4444);
        context.fill(cx + rx - 3, cy, cx + rx + 3, cy + 2, 0xFFEF4444);
    }
}
