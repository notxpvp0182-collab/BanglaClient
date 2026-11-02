package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.gui.ClickGuiScreen;
import com.ahmed.banglaclient.gui.HudEditorScreen;
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
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
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

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Unique
    private void registerBanglaButton(ButtonWidget btn) {
        banglaMenuWidgets.add(btn);
        this.addDrawableChild(btn);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void banglaclient$setupImage3MainMenu(CallbackInfo ci) {
        banglaMenuWidgets.clear();
        capturedModWidgets.clear();

        BanglaClient bangla = BanglaClient.getInstance();
        if (bangla == null || bangla.getConfigManager() == null || !bangla.getConfigManager().isCustomMainMenuEnabled()) {
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

        // 1. Left-Side Dynamic Installed-Mod Shortcuts (IMAGE 3: Only shown if the mod is actually installed!)
        int leftX = 8;
        int leftW = 78;
        int leftH = 20;
        int leftY = Math.max(58, (h - 96) / 2);

        if (FabricLoader.getInstance().isModLoaded("ias")) {
            registerBanglaButton(ButtonWidget.builder(Text.literal("__LEFT_IAS__:Account"), b -> triggerExternalMod("ias", "account"))
                    .dimensions(leftX, leftY, leftW, leftH).tooltip(Tooltip.of(Text.literal("In-Game Account Switcher"))).build());
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

        // 2. Top-Right Toolbar (IMAGE 3: [• Username] [Click GUI] [HUD Editor] [Theme] [Options] [Cosmetics/GUI])
        int trSq = 18, trGap = 4;
        int curRight = w - 8;

        // Rightmost: Cosmetics / Quick GUI shortcut
        curRight -= trSq;
        registerBanglaButton(ButtonWidget.builder(Text.literal("__ICON_COSM__"), b -> {
            if (this.client != null) this.client.setScreen(new ClickGuiScreen(false));
        }).dimensions(curRight, 8, trSq, trSq).tooltip(Tooltip.of(Text.literal("Cosmetics & Modules"))).build());

        // Settings / Options shortcut
        curRight -= (trSq + trGap);
        registerBanglaButton(ButtonWidget.builder(Text.literal("__ICON_OPT__"), b -> {
            if (this.client != null) this.client.setScreen(new OptionsScreen(this, this.client.options));
        }).dimensions(curRight, 8, trSq, trSq).tooltip(Tooltip.of(Text.literal("Minecraft Options"))).build());

        // Theme / Color Changer shortcut
        curRight -= (trSq + trGap);
        registerBanglaButton(ButtonWidget.builder(Text.literal("__ICON_COLOR__"), b -> {
            bangla.getThemeManager().cycleNextTheme();
        }).dimensions(curRight, 8, trSq, trSq).tooltip(Tooltip.of(Text.literal("Switch Red / Custom Theme"))).build());

        // Dedicated HUD Editor shortcut (Requirement #2)
        curRight -= (trSq + trGap);
        registerBanglaButton(ButtonWidget.builder(Text.literal("__ICON_HUD__"), b -> {
            if (this.client != null) this.client.setScreen(new HudEditorScreen(this));
        }).dimensions(curRight, 8, trSq, trSq).tooltip(Tooltip.of(Text.literal("HUD Editor"))).build());

        // Click GUI shortcut
        curRight -= (trSq + trGap);
        registerBanglaButton(ButtonWidget.builder(Text.literal("__ICON_GUI__"), b -> {
            if (this.client != null) this.client.setScreen(new ClickGuiScreen(false));
        }).dimensions(curRight, 8, trSq, trSq).tooltip(Tooltip.of(Text.literal("Open Click GUI"))).build());

        // Account Name Pill
        String username = this.client != null && this.client.getSession() != null ? this.client.getSession().getUsername() : "Player";
        int userW = Math.max(54, this.textRenderer.getWidth("• " + username) + 14);
        curRight -= (userW + trGap);
        registerBanglaButton(ButtonWidget.builder(Text.literal("__USER__:" + username), b -> triggerExternalMod("ias", "account"))
                .dimensions(curRight, 8, userW, trSq).tooltip(Tooltip.of(Text.literal("Account: " + username))).build());

        // 3. Compact Centered Main Menu Button Stack (IMAGE 3)
        int logoCenterY = Math.max(30, (h - 190) / 2);
        int btnW = Math.min(144, w - 60);
        int btnH = 18;
        int btnX = centerX - btnW / 2;
        int btnY = logoCenterY + 50;

        registerBanglaButton(ButtonWidget.builder(Text.literal("Singleplayer"), b -> {
            if (this.client != null) this.client.setScreen(new SelectWorldScreen(this));
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += 20;

        registerBanglaButton(ButtonWidget.builder(Text.literal("Multiplayer"), b -> {
            if (this.client != null) this.client.setScreen(new MultiplayerScreen(this));
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += 20;

        registerBanglaButton(ButtonWidget.builder(Text.literal("Realms"), b -> {
            if (this.client != null) this.client.setScreen(new RealmsMainScreen(this));
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += 20;

        // Cosmetics -> Opens actual Click GUI screen
        registerBanglaButton(ButtonWidget.builder(Text.literal("Cosmetics"), b -> {
            if (this.client != null) this.client.setScreen(new ClickGuiScreen(false));
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += 20;

        registerBanglaButton(ButtonWidget.builder(Text.literal("Options"), b -> {
            if (this.client != null) this.client.setScreen(new OptionsScreen(this, this.client.options));
        }).dimensions(btnX, btnY, btnW, btnH).build());
        btnY += 21;

        registerBanglaButton(ButtonWidget.builder(Text.literal("Quit Game"), b -> {
            if (this.client != null) this.client.scheduleStop();
        }).dimensions(btnX, btnY, btnW, btnH).build());

        // 4. Bottom-Right BANGLA SMP Server Card
        registerBanglaButton(ButtonWidget.builder(Text.literal("__BANGLA_SMP__"), b -> {
            if (this.client != null && this.client.keyboard != null) {
                this.client.keyboard.setClipboard("play.banglasmp.net");
                bangla.getNotificationManager().pushSuccess("Bangla SMP", "Copied play.banglasmp.net");
            }
        }).dimensions(w - 122, h - 34, 114, 26).tooltip(Tooltip.of(Text.literal("Click to copy play.banglasmp.net"))).build());
    }

    @Unique
    private void sanitizePostInitWidgets() {
        // Intercept and hide any widgets added after init() by ScreenEvents.AFTER_INIT (such as IAS's [In] button & bottom label)
        for (Element child : this.children()) {
            if (child instanceof ClickableWidget cw && !banglaMenuWidgets.contains(cw)) {
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

        // 1. Aspect-Ratio-Preserving Cover Scaling of Verified Cherry Blossom Background PNG
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

        // Subtle atmospheric vignette so Liquid Glass buttons pop with crisp contrast
        context.fillGradient(0, 0, w, h, 0x18000000, 0x44080A10);

        // 2. Large 3D "Bangla Client" Logo with Golden Crown & Red Energy Rings (IMAGE 3)
        int logoCenterY = Math.max(30, (h - 190) / 2);
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

        // 3. Render ONLY our owned BANGLA CLIENT widgets (Zero stray [In] or bottom Current Account text!)
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
    private void triggerExternalMod(String classHint, String labelHint) {
        for (ClickableWidget cw : capturedModWidgets) {
            String msg = cw.getMessage() != null ? cw.getMessage().getString().toLowerCase(Locale.ROOT) : "";
            String cls = cw.getClass().getName().toLowerCase(Locale.ROOT);
            if (cls.contains(classHint) || msg.contains(labelHint) || cls.contains("account")) {
                cw.active = true;
                cw.visible = true;
                cw.onClick(new Click(cw.getX() + 2, cw.getY() + 2, new MouseInput(0, 0)), false);
                return;
            }
        }
        openModMenuScreen();
    }

    @Unique
    private void openModMenuScreen() {
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
