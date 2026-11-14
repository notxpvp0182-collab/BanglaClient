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
import com.ahmed.banglaclient.integration.MainMenuButtonBridge;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.fabricmc.loader.api.FabricLoader;
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

/**
 * Pure 2D Aura Dark Main Menu:
 * - No background wallpaper image (clean dark Aura canvas)
 * - Auto-detects any installed third-party mod buttons and builds custom 2D icon buttons on the left
 * - 2D Aura center buttons & top-right utility dock (without default-theme/color switcher)
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique
    private final Set<ClickableWidget> banglaMenuWidgets = new HashSet<>();

    @Unique
    private final List<ClickableWidget> capturedModWidgets = new ArrayList<>();

    @Unique
    private final Set<ClickableWidget> bridgedExternalWidgets = new HashSet<>();

    @Unique
    private int nextLeftSlotY = 36;

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
    private void banglaclient$setup2DAuraMainMenu(CallbackInfo ci) {
        banglaMenuWidgets.clear();
        capturedModWidgets.clear();
        bridgedExternalWidgets.clear();

        for (ClickableWidget ext : MainMenuButtonBridge.collectThirdPartyButtons(this)) {
            capturedModWidgets.add(ext);
        }
        this.clearChildren();

        int w = this.width;
        int h = this.height;
        int centerX = w / 2;

        // 1. Left-Side Serial Mod Dock (Account Switcher + Mod Menu + Any Auto-Detected Mod Buttons)
        int leftX = 8, leftW = 84, leftH = 18;
        this.nextLeftSlotY = 36;

        registerBanglaButton(ButtonWidget.builder(Text.literal("__MODBTN__:ACCOUNT:Accounts"), b -> openAccountSwitcher())
                .dimensions(leftX, nextLeftSlotY, leftW, leftH)
                .tooltip(Tooltip.of(Text.literal("In-Game Account Switcher"))).build());
        nextLeftSlotY += 21;

        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            registerBanglaButton(ButtonWidget.builder(Text.literal("__MODBTN__:DASHBOARD:Mods"), b -> openModMenuScreen())
                    .dimensions(leftX, nextLeftSlotY, leftW, leftH)
                    .tooltip(Tooltip.of(Text.literal("Open Mod Menu"))).build());
            nextLeftSlotY += 21;
        }

        bridgeCapturedModButtons();

        // 2. Top-Right Utility Dock (No Switch-to-Default-Theme, No Theme Color Switcher)
        int topY = 10;
        int trSq = 18, trGap = 4;
        int curRight = w - 8;

        curRight -= trSq;
        registerBanglaButton(ButtonWidget.builder(Text.literal("__ICON_INFO__"), b -> {
            if (this.client != null) this.client.setScreen(new AboutScreen(this));
        }).dimensions(curRight, topY, trSq, trSq).tooltip(Tooltip.of(Text.literal("About BANGLA CLIENT"))).build());

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

        String username = this.client != null && this.client.getSession() != null ? this.client.getSession().getUsername() : "Player";
        int userW = Math.max(58, 24 + this.textRenderer.getWidth(username));
        curRight -= (userW + trGap);
        registerBanglaButton(ButtonWidget.builder(Text.literal("__USER__:" + username), b -> openAccountSwitcher())
                .dimensions(curRight, topY, userW, trSq).tooltip(Tooltip.of(Text.literal("Account: " + username))).build());

        // 3. Center 2D Aura Button Stack (Singleplayer, Multiplayer, Realms, Cosmetics, and [Options... | Quit Game])
        int btnW = Math.min(148, w - 120);
        int btnH = 19;
        int btnStep = 22;
        int btnX = centerX - btnW / 2;
        int btnY = Math.max(54, h / 2 - 42);

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

        registerBanglaButton(ButtonWidget.builder(Text.literal("Cosmetics"), b -> {
            if (this.client != null) this.client.setScreen(new ClickGuiScreen(false));
        }).dimensions(btnX, btnY, btnW, btnH).tooltip(Tooltip.of(Text.literal("Open BANGLA CLIENT ClickGUI"))).build());
        btnY += btnStep + 2;

        int halfW = (btnW - 4) / 2;
        registerBanglaButton(ButtonWidget.builder(Text.literal("Options..."), b -> {
            if (this.client != null) this.client.setScreen(new OptionsScreen(this, this.client.options));
        }).dimensions(btnX, btnY, halfW, btnH).build());

        registerBanglaButton(ButtonWidget.builder(Text.literal("Quit Game"), b -> {
            if (this.client != null) this.client.scheduleStop();
        }).dimensions(btnX + halfW + 4, btnY, btnW - halfW - 4, btnH).build());

        // 4. Right Side: 3D PlayerSkinWidget & Wardrobe Button
        int charCenterX = w - 64;
        int charW = 72;
        int charH = Math.min(110, h - 90);
        int charTopY = Math.max(38, (h - charH) / 2 - 8);

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

        registerBanglaButton(ButtonWidget.builder(Text.literal("Wardrobe"), b -> {
            if (this.client != null) {
                this.client.setScreen(new SkinCustomizationScreen(this, SkinCapeManager.getInstance()));
            }
        }).dimensions(charCenterX - 38, charTopY + charH + 4, 76, 17).tooltip(Tooltip.of(Text.literal("Customize Skin & Cape"))).build());

        // 5. Bottom-Right BANGLA SMP Card
        registerBanglaButton(ButtonWidget.builder(Text.literal("__BANGLA_SMP__"), b -> {
            if (this.client != null && this.client.keyboard != null) {
                this.client.keyboard.setClipboard("play.banglasmp.net");
                if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
                    BanglaClient.getInstance().getNotificationManager().pushSuccess("Bangla SMP", "Copied play.banglasmp.net");
                }
            }
        }).dimensions(w - 114, h - 30, 106, 24).tooltip(Tooltip.of(Text.literal("Click to copy play.banglasmp.net"))).build());
    }

    @Unique
    private void bridgeCapturedModButtons() {
        int leftX = 8, leftW = 84, leftH = 18;
        for (ClickableWidget cw : capturedModWidgets) {
            if (bridgedExternalWidgets.contains(cw)) continue;
            bridgedExternalWidgets.add(cw);

            String rawLabel = cw.getMessage() != null ? cw.getMessage().getString().trim() : "";
            String clsName = cw.getClass().getSimpleName();
            String lower = (rawLabel + " " + clsName).toLowerCase(Locale.ROOT);

            // Skip ModMenu or IAS if already added as primary buttons
            if (lower.contains("modmenu") || lower.equals("mods") || lower.contains("account")) {
                cw.visible = false;
                cw.active = false;
                continue;
            }

            String displayTitle = !rawLabel.isEmpty() ? rawLabel : deriveModTitleFromClass(cw.getClass().getName());
            String iconKey = resolveAutoIconForMod(lower);

            final ClickableWidget targetWidget = cw;
            targetWidget.visible = false;
            targetWidget.active = false;

            if (nextLeftSlotY + leftH < this.height - 28) {
                registerBanglaButton(ButtonWidget.builder(Text.literal("__MODBTN__:" + iconKey + ":" + displayTitle), b -> {
                    targetWidget.active = true;
                    targetWidget.visible = true;
                    targetWidget.onClick(new Click(targetWidget.getX() + 2, targetWidget.getY() + 2, new MouseInput(0, 0)), false);
                    targetWidget.visible = false;
                    targetWidget.active = false;
                }).dimensions(leftX, nextLeftSlotY, leftW, leftH).tooltip(Tooltip.of(Text.literal(displayTitle))).build());
                nextLeftSlotY += 21;
            }
        }
    }

    @Unique
    private static String resolveAutoIconForMod(String lowerText) {
        if (lowerText.contains("replay") || lowerText.contains("flashback") || lowerText.contains("camera")) return "REPLAY";
        if (lowerText.contains("shader") || lowerText.contains("iris") || lowerText.contains("sodium") || lowerText.contains("visual")) return "VISUAL";
        if (lowerText.contains("voice") || lowerText.contains("sound") || lowerText.contains("audio")) return "AUDIO";
        if (lowerText.contains("via") || lowerText.contains("server") || lowerText.contains("world") || lowerText.contains("map")) return "WORLD";
        if (lowerText.contains("chat") || lowerText.contains("social")) return "CHAT";
        return "UTILITY";
    }

    @Unique
    private static String deriveModTitleFromClass(String fullClassName) {
        if (fullClassName == null || fullClassName.isEmpty()) return "Mod";
        String[] parts = fullClassName.split("\\.");
        for (String p : parts) {
            if (!p.equals("com") && !p.equals("net") && !p.equals("org") && !p.equals("me") && !p.equals("gui") && !p.equals("client") && p.length() > 2) {
                return Character.toUpperCase(p.charAt(0)) + p.substring(1);
            }
        }
        return "Mod Tool";
    }

    @Unique
    private void sanitizePostInitWidgets() {
        List<Element> snapshot = new ArrayList<>(this.children());
        boolean foundNewModWidget = false;
        for (Element child : snapshot) {
            if (child instanceof ClickableWidget cw && !banglaMenuWidgets.contains(cw) && cw != mainSkinWidget) {
                if (!capturedModWidgets.contains(cw)) {
                    capturedModWidgets.add(cw);
                    foundNewModWidget = true;
                }
                cw.visible = false;
                cw.active = false;
                cw.setX(-2000);
                cw.setY(-2000);
            }
        }
        if (foundNewModWidget) {
            bridgeCapturedModButtons();
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void banglaclient$render2DAuraMainMenu(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        sanitizePostInitWidgets();

        int w = this.width;
        int h = this.height;
        int centerX = w / 2;

        // 1. Pure 2D Aura Dark Canvas (NO background image!)
        context.fill(0, 0, w, h, 0xFF07080B);
        context.fill(0, h - 38, w, h, 0xFF0A0C10);
        context.fill(0, h - 39, w, h - 38, 0x16FFFFFF);

        // 2. Clean 2D "Bangla Client" Title at Top-Center (Zero 3D extrusion, Zero crown)
        int titleY = Math.max(22, h / 2 - 72);
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(centerX, titleY);
        matrices.scale(1.85f, 1.85f);
        String titleStr = "Bangla Client";
        int tw = this.textRenderer.getWidth(titleStr);
        context.drawTextWithShadow(this.textRenderer, titleStr, -tw / 2, 0, 0xFFFFFFFF);
        matrices.popMatrix();

        // 3. Right-Side Username Pill & 3D PlayerSkinWidget
        String liveUser = this.client != null && this.client.getSession() != null ? this.client.getSession().getUsername() : "Player";
        int charCenterX = w - 64;
        int nameW = this.textRenderer.getWidth(liveUser);
        int charH = Math.min(110, h - 90);
        int badgeY = Math.max(22, (h - charH) / 2 - 24);
        GlassRenderer.drawPillBadge(context, charCenterX - nameW / 2 - 6, badgeY, nameW + 12, 14, 0xFFFFFFFF, false);
        context.drawTextWithShadow(this.textRenderer, liveUser, charCenterX - nameW / 2, badgeY + 3, 0xFFFFFFFF);

        if (this.mainSkinWidget != null) {
            try {
                this.mainSkinWidget.render(context, mouseX, mouseY, delta);
            } catch (Throwable ignored) {}
        }

        // 4. Render All 2D Aura Buttons
        for (ClickableWidget cw : banglaMenuWidgets) {
            if (cw.visible) {
                cw.render(context, mouseX, mouseY, delta);
            }
        }

        // Bottom-left subtle version text (like Aura Beta 3.2.3 Minecraft 1.21.11)
        context.drawTextWithShadow(this.textRenderer, "Bangla Client v1.0.0  Minecraft 1.21.11", 8, h - 12, 0xFF64748B);

        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
            BanglaClient.getInstance().getNotificationManager().render(context, w, h);
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
}
