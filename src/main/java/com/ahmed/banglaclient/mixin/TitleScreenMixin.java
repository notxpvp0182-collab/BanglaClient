package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.account.AccountManager;
import com.ahmed.banglaclient.cosmetics.BackgroundManager;
import com.ahmed.banglaclient.cosmetics.BackgroundManager.BackgroundEntry;
import com.ahmed.banglaclient.cosmetics.PlayerPreviewRenderer;
import com.ahmed.banglaclient.cosmetics.SkinCapeManager;
import com.ahmed.banglaclient.gui.AboutScreen;
import com.ahmed.banglaclient.gui.AccountSwitcherScreen;
import com.ahmed.banglaclient.gui.BackgroundSelectorScreen;
import com.ahmed.banglaclient.gui.ClickGuiScreen;
import com.ahmed.banglaclient.gui.HudEditorScreen;
import com.ahmed.banglaclient.gui.SkinCustomizationScreen;
import com.ahmed.banglaclient.integration.MainMenuButtonBridge;
import com.ahmed.banglaclient.rendering.GlassRenderer;
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

/**
 * 2D Aura Dark Main Menu:
 * - Default Minecraft Panorama background (or user-selected GIF/PNG/JPG/JPEG from BackgroundManager)
 * - Left-side ICON-ONLY mod dock (Account Switcher, Mod Menu, Replay Mod, Flashback, IAS, etc.)
 * - Narrower & taller center buttons (126x22)
 * - Larger, clearer top-right icon buttons (22x22 at y=14) including Background Customizer
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
    private int nextLeftSlotY = 14;

    @Unique
    private PlayerSkinWidget mainSkinWidget;

    @Unique
    private final java.util.Map<ClickableWidget, int[]> widgetBaseCoords = new java.util.HashMap<>();

    @Unique
    private long entranceStartMs = -1L;

    @Unique
    private boolean wasOverlayPresent = true;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Unique
    private void registerBanglaButton(ButtonWidget btn) {
        banglaMenuWidgets.add(btn);
        widgetBaseCoords.put(btn, new int[]{btn.getX(), btn.getY()});
        this.addDrawableChild(btn);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void banglaclient$setup2DAuraMainMenu(CallbackInfo ci) {
        banglaMenuWidgets.clear();
        widgetBaseCoords.clear();
        this.entranceStartMs = System.currentTimeMillis();
        this.wasOverlayPresent = (this.client != null && this.client.getOverlay() != null);
        capturedModWidgets.clear();
        bridgedExternalWidgets.clear();

        for (ClickableWidget ext : MainMenuButtonBridge.collectThirdPartyButtons(this)) {
            capturedModWidgets.add(ext);
        }
        this.clearChildren();

        int w = this.width;
        int h = this.height;
        int centerX = w / 2;

        // 1. Left-Side ICON-ONLY Mod Dock (22x22 square buttons, NO text labels!)
        int leftX = 12, leftSq = 22;
        this.nextLeftSlotY = 14;

        // Icon 1: Account Switcher
        registerBanglaButton(ButtonWidget.builder(Text.literal("__MODICON__:ACCOUNT"), b -> openAccountSwitcher())
                .dimensions(leftX, nextLeftSlotY, leftSq, leftSq)
                .tooltip(Tooltip.of(Text.literal("In-Game Account Switcher"))).build());
        nextLeftSlotY += 26;

        // Icon 2: Mod Menu
        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            registerBanglaButton(ButtonWidget.builder(Text.literal("__MODICON__:DASHBOARD"), b -> openModMenuScreen())
                    .dimensions(leftX, nextLeftSlotY, leftSq, leftSq)
                    .tooltip(Tooltip.of(Text.literal("Mod Menu"))).build());
            nextLeftSlotY += 26;
        }

        // Auto-detect and add icons for Replay Mod, Flashback, IAS, or any other installed mod buttons
        bridgeCapturedModButtons();

        // 2. Top-Right Utility Dock (Larger 22x22 buttons at y = 14, with Background Customizer button)
        int topY = 14;
        int trSq = 22, trGap = 5;
        int curRight = w - 12;

        curRight -= trSq;
        registerBanglaButton(ButtonWidget.builder(Text.literal("__ICON_INFO__"), b -> {
            if (this.client != null) this.client.setScreen(new AboutScreen(this));
        }).dimensions(curRight, topY, trSq, trSq).tooltip(Tooltip.of(Text.literal("Client & Developer Info"))).build());

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
        registerBanglaButton(ButtonWidget.builder(Text.literal("__ICON_BG__"), b -> {
            if (this.client != null) this.client.setScreen(new BackgroundSelectorScreen(this));
        }).dimensions(curRight, topY, trSq, trSq).tooltip(Tooltip.of(Text.literal("Custom Menu Backgrounds (GIF / PNG / JPG)"))).build());

        curRight -= (trSq + trGap);
        registerBanglaButton(ButtonWidget.builder(Text.literal("__ICON_HUD__"), b -> {
            if (this.client != null) this.client.setScreen(new HudEditorScreen(this));
        }).dimensions(curRight, topY, trSq, trSq).tooltip(Tooltip.of(Text.literal("HUD Editor"))).build());

        String username = this.client != null && this.client.getSession() != null ? this.client.getSession().getUsername() : "Player";
        int userW = Math.max(64, 26 + this.textRenderer.getWidth(username));
        curRight -= (userW + trGap);
        registerBanglaButton(ButtonWidget.builder(Text.literal("__USER__:" + username), b -> openAccountSwitcher())
                .dimensions(curRight, topY, userW, trSq).tooltip(Tooltip.of(Text.literal("Switch Account"))).build());

        // 3. Center Premium 2D Aura Button Stack (Narrower width: 126px, Taller height: 22px)
        int btnW = Math.min(126, w - 130);
        int btnH = 22;
        int btnStep = 25;
        int btnX = centerX - btnW / 2;
        int btnY = Math.max(52, h / 2 - 48);

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

        registerBanglaButton(ButtonWidget.builder(Text.literal("Quit"), b -> {
            if (this.client != null) this.client.scheduleStop();
        }).dimensions(btnX + halfW + 4, btnY, btnW - halfW - 4, btnH).build());

        // 4. Right Side: 3D PlayerSkinWidget & Bedrock-Style Wardrobe Button
        int charCenterX = w - 64;
        int charW = 72;
        int charH = Math.min(108, h - 94);
        int charTopY = Math.max(44, (h - charH) / 2 - 6);

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
        }).dimensions(charCenterX - 38, charTopY + charH + 4, 76, 18).tooltip(Tooltip.of(Text.literal("Bedrock-Style Skin & Cape Wardrobe"))).build());

        // 5. Bottom-Right BANGLA SMP Card (Well-spaced and neatly aligned)
        registerBanglaButton(ButtonWidget.builder(Text.literal("__BANGLA_SMP__"), b -> {
            if (this.client != null && this.client.keyboard != null) {
                this.client.keyboard.setClipboard("play.banglasmp.net");
                if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
                    BanglaClient.getInstance().getNotificationManager().pushSuccess("Bangla SMP", "Copied play.banglasmp.net");
                }
            }
        }).dimensions(w - 130, h - 34, 118, 25).tooltip(Tooltip.of(Text.literal("Click to copy play.banglasmp.net"))).build());
    }

    @Unique
    private void bridgeCapturedModButtons() {
        int leftX = 12, leftSq = 22;
        for (ClickableWidget cw : capturedModWidgets) {
            if (bridgedExternalWidgets.contains(cw)) continue;
            bridgedExternalWidgets.add(cw);

            String rawLabel = cw.getMessage() != null ? cw.getMessage().getString().trim() : "";
            String clsName = cw.getClass().getSimpleName();
            String lower = (rawLabel + " " + clsName).toLowerCase(Locale.ROOT);

            if (lower.contains("modmenu") || lower.equals("mods")) {
                cw.visible = false;
                cw.active = false;
                continue;
            }

            String displayTitle = !rawLabel.isEmpty() ? rawLabel : deriveModTitleFromClass(cw.getClass().getName());
            String iconKey = resolveAutoIconForMod(lower);

            final ClickableWidget targetWidget = cw;
            targetWidget.visible = false;
            targetWidget.active = false;

            if (nextLeftSlotY + leftSq < this.height - 28) {
                registerBanglaButton(ButtonWidget.builder(Text.literal("__MODICON__:" + iconKey), b -> {
                    targetWidget.active = true;
                    targetWidget.visible = true;
                    targetWidget.onClick(new Click(targetWidget.getX() + 2, targetWidget.getY() + 2, new MouseInput(0, 0)), false);
                    targetWidget.visible = false;
                    targetWidget.active = false;
                }).dimensions(leftX, nextLeftSlotY, leftSq, leftSq).tooltip(Tooltip.of(Text.literal(displayTitle))).build());
                nextLeftSlotY += 26;
            }
        }
    }

    @Unique
    private static String resolveAutoIconForMod(String lowerText) {
        if (lowerText.contains("account") || lowerText.contains("ias")) return "ACCOUNT";
        if (lowerText.contains("replay") || lowerText.contains("flashback") || lowerText.contains("camera")) return "REPLAY";
        if (lowerText.contains("shader") || lowerText.contains("iris") || lowerText.contains("sodium") || lowerText.contains("visual")) return "VISUAL";
        if (lowerText.contains("voice") || lowerText.contains("sound") || lowerText.contains("audio")) return "AUDIO";
        if (lowerText.contains("via") || lowerText.contains("server") || lowerText.contains("world") || lowerText.contains("map")) return "WORLD";
        if (lowerText.contains("chat") || lowerText.contains("social")) return "CHAT";
        return "UTILITY";
    }

    @Unique
    private static String deriveModTitleFromClass(String fullClassName) {
        if (fullClassName == null || fullClassName.isEmpty()) return "Mod Tool";
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

        // 1. Render Selected Custom Background (GIF/PNG/JPG) OR Default Minecraft Panorama
        BackgroundEntry customBg = BackgroundManager.getInstance().getActiveBackground();
        boolean renderedCustom = false;
        if (customBg != null) {
            Identifier frameTex = customBg.getCurrentFrameTexture();
            if (frameTex != null) {
                try {
                    context.drawTexture(RenderPipelines.GUI_TEXTURED, frameTex, 0, 0, 0.0f, 0.0f, w, h, w, h);
                    context.fill(0, 0, w, h, 0x38060709);
                    renderedCustom = true;
                } catch (Throwable ignored) {}
            }
        }
        if (!renderedCustom) {
            float pSpeed = 1.0f; if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getModuleManager() != null) { var panOpt = BanglaClient.getInstance().getModuleManager().getModule(com.ahmed.banglaclient.module.visual.CustomPanoramaModule.class); if (panOpt.isPresent()) pSpeed = panOpt.get().getRotationSpeedMultiplier(); } this.renderPanoramaBackground(context, delta * pSpeed);
            context.fill(0, 0, w, h, 0x44060709);
        }

        // Smooth Entrance Animation Timing (re-triggers cleanly the moment SplashOverlay finishes!)
        boolean overlayNow = (this.client != null && this.client.getOverlay() != null);
        if (wasOverlayPresent && !overlayNow) {
            entranceStartMs = System.currentTimeMillis();
        }
        wasOverlayPresent = overlayNow;
        if (entranceStartMs < 0L) entranceStartMs = System.currentTimeMillis();

        float elapsedSec = (System.currentTimeMillis() - entranceStartMs) / 1000.0f;
        float globalAnim = com.ahmed.banglaclient.animation.Easing.EASE_OUT_QUART.apply(
                com.ahmed.banglaclient.util.MathUtils.clamp(elapsedSec / 0.72f, 0.0f, 1.0f)
        );

        // 2. Animated 2D "Bangla Client" Title at Top-Center (glides down & scales in)
        int baseTitleY = Math.max(20, h / 2 - 76);
        float titleSlideY = baseTitleY - (1.0f - globalAnim) * 18.0f;
        float titleScale = 1.65f + 0.20f * globalAnim;
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(centerX, titleSlideY);
        matrices.scale(titleScale, titleScale);
        String titleStr = "Bangla Client";
        int tw = this.textRenderer.getWidth(titleStr);
        context.drawTextWithShadow(this.textRenderer, titleStr, -tw / 2, 0, 0xFFFFFFFF);
        matrices.popMatrix();

        // 3. Animated Right-Side Username Pill & 3D PlayerSkinWidget (glides in from right)
        int rightSlideOffset = Math.round((1.0f - globalAnim) * 36.0f);
        String liveUser = this.client != null && this.client.getSession() != null ? this.client.getSession().getUsername() : "Player";
        int charCenterX = w - 64 + rightSlideOffset;
        int nameW = this.textRenderer.getWidth(liveUser);
        int charW = 72;
        int charH = Math.min(108, h - 94);
        int charTopY = Math.max(44, (h - charH) / 2 - 6);
        int badgeY = Math.max(26, (h - charH) / 2 - 22);
        GlassRenderer.drawPillBadge(context, charCenterX - nameW / 2 - 6, badgeY, nameW + 12, 14, 0xFFFFFFFF, false);
        context.drawTextWithShadow(this.textRenderer, liveUser, charCenterX - nameW / 2, badgeY + 3, 0xFFFFFFFF);

        if (this.mainSkinWidget != null) {
            try {
                this.mainSkinWidget.setPosition(charCenterX - charW / 2, charTopY);
                this.mainSkinWidget.render(context, mouseX, mouseY, delta);
            } catch (Throwable ignored) {}
        }

        // 4. Staggered Choreographed Entrance for All Main Menu Buttons
        int centerBtnIndex = 0;
        for (ClickableWidget cw : banglaMenuWidgets) {
            if (!cw.visible) continue;
            int[] base = widgetBaseCoords.get(cw);
            if (base != null) {
                int bx = base[0], by = base[1];
                if (bx <= 24) {
                    // Left-side icon dock slides in from left
                    cw.setX(bx - Math.round((1.0f - globalAnim) * 28.0f));
                    cw.setY(by);
                } else if (by <= 20 && bx > w / 2) {
                    // Top-right utility dock slides down from top
                    cw.setX(bx);
                    cw.setY(by - Math.round((1.0f - globalAnim) * 24.0f));
                } else if (bx > w - 150 && by > 40) {
                    // Right-side Wardrobe & BANGLA SMP card slide in from right
                    cw.setX(bx + rightSlideOffset);
                    cw.setY(by);
                } else {
                    // Center buttons cascade upward with staggered timing
                    float delay = centerBtnIndex * 0.045f;
                    float btnProgress = com.ahmed.banglaclient.animation.Easing.EASE_OUT_CUBIC.apply(
                            com.ahmed.banglaclient.util.MathUtils.clamp((elapsedSec - delay) / 0.55f, 0.0f, 1.0f)
                    );
                    cw.setX(bx);
                    cw.setY(by + Math.round((1.0f - btnProgress) * 22.0f));
                    centerBtnIndex++;
                }
            }
            cw.render(context, mouseX, mouseY, delta);
        }

        int verSlideY = Math.round((1.0f - globalAnim) * 14.0f);
        context.drawTextWithShadow(this.textRenderer, "Bangla Client v1.0.0", 12, h - 14 + verSlideY, 0xFF94A3B8);

        // Seamless post-splash dark dissolve veil so there is zero sudden flash
        if (!overlayNow && globalAnim < 0.99f) {
            int veilAlpha = Math.round((1.0f - globalAnim) * 165.0f);
            if (veilAlpha > 2) {
                context.fill(0, 0, w, h, (veilAlpha << 24) | 0x040507);
            }
        }

        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getNotificationManager() != null) {
            BanglaClient.getInstance().getNotificationManager().render(context, w, h);
        }
        ci.cancel();
    }

    @Unique
    private void openAccountSwitcher() {
        if (this.client != null) {
            this.client.setScreen(new AccountSwitcherScreen(this, AccountManager.getInstance(), SkinCapeManager.getInstance()));
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
