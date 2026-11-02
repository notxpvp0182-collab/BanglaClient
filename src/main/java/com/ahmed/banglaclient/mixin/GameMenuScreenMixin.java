package com.ahmed.banglaclient.mixin;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.gui.ClickGuiScreen;
import com.ahmed.banglaclient.rendering.BlurRenderer;
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
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
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

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Unique
    private void registerPauseBtn(ButtonWidget btn) {
        pauseButtons.add(btn);
        this.addDrawableChild(btn);
    }

    @Inject(method = "initWidgets", at = @At("TAIL"))
    private void banglaclient$setupCleanImage4PauseMenu(CallbackInfo ci) {
        pauseButtons.clear();
        // Remove vanilla's GridWidget and "Game Menu" text so zero ghost widgets or overlapping titles exist
        this.clearChildren();

        int w = this.width;
        int h = this.height;
        int centerX = w / 2;

        boolean isSingle = this.client != null && this.client.isInSingleplayer();
        boolean canOpenLan = this.client != null
                && this.client.isIntegratedServerRunning()
                && (this.client.getServer() == null || !this.client.getServer().isRemote());

        int totalW = Math.min(208, w - 36);
        int colGap = 6;
        int halfW = (totalW - colGap) / 2;
        int btnH = 20;
        int rowStep = 23;

        int logoCenterY = Math.max(26, (h - 200) / 2);
        int leftX = centerX - totalW / 2;
        int rightX = leftX + halfW + colGap;
        int rowY = logoCenterY + 48;

        // Row 1 (Full Width): [▶] Back to Game
        registerPauseBtn(ButtonWidget.builder(Text.literal("__PAUSE_PLAY:Back to Game"), b -> {
            if (this.client != null) {
                this.client.setScreen(null);
                this.client.mouse.lockCursor();
            }
        }).dimensions(leftX, rowY, totalW, btnH).build());
        rowY += rowStep;

        // Row 2 (2 Columns): [🏆] Advancements | [📊] Statistics
        registerPauseBtn(ButtonWidget.builder(Text.literal("__PAUSE_TROPHY:Advancements"), b -> {
            if (this.client != null && this.client.player != null) {
                this.client.setScreen(new AdvancementsScreen(this.client.player.networkHandler.getAdvancementHandler(), this));
            }
        }).dimensions(leftX, rowY, halfW, btnH).build());

        registerPauseBtn(ButtonWidget.builder(Text.literal("__PAUSE_STATS:Statistics"), b -> {
            if (this.client != null && this.client.player != null) {
                this.client.setScreen(new StatsScreen(this, this.client.player.getStatHandler()));
            }
        }).dimensions(rightX, rowY, halfW, btnH).build());
        rowY += rowStep;

        // Row 3 (2 Columns): [🧩] Mods | [↖] Click GUI
        registerPauseBtn(ButtonWidget.builder(Text.literal("__PAUSE_MODS:Mods"), b -> openModMenuScreen())
                .dimensions(leftX, rowY, halfW, btnH).build());

        registerPauseBtn(ButtonWidget.builder(Text.literal("__PAUSE_GUI:Click GUI"), b -> {
            if (this.client != null) {
                this.client.setScreen(new ClickGuiScreen(false));
            }
        }).dimensions(rightX, rowY, halfW, btnH).build());
        rowY += rowStep;

        // Row 4 (2 Columns): [⚙] Options... | [👥] Open to LAN
        registerPauseBtn(ButtonWidget.builder(Text.literal("__PAUSE_OPT:Options..."), b -> {
            if (this.client != null) {
                this.client.setScreen(new OptionsScreen(this, this.client.options));
            }
        }).dimensions(leftX, rowY, halfW, btnH).build());

        ButtonWidget lanBtn = ButtonWidget.builder(Text.literal("__PAUSE_LAN:Open to LAN"), b -> {
            if (this.client != null && canOpenLan) {
                this.client.setScreen(new OpenToLanScreen(this));
            }
        }).dimensions(rightX, rowY, halfW, btnH).build();
        lanBtn.active = canOpenLan;
        registerPauseBtn(lanBtn);
        rowY += rowStep + 2;

        // Row 5 (Full Width): [↪] Save and Quit to Title (or Disconnect)
        String exitLabel = isSingle ? "__PAUSE_EXIT:Save and Quit to Title" : "__PAUSE_EXIT:Disconnect";
        registerPauseBtn(ButtonWidget.builder(Text.literal(exitLabel), b -> {
            if (this.client != null) {
                this.client.disconnectWithProgressScreen();
            }
        }).dimensions(leftX, rowY, totalW, btnH).build());
    }

    @Unique
    private void sanitizePauseWidgets() {
        // Suppress any non-button title TextWidget ("Game Menu") or external overlay widgets
        for (Element child : this.children()) {
            if (child instanceof ClickableWidget cw && !pauseButtons.contains(cw)) {
                cw.visible = false;
                cw.active = false;
                cw.setX(-2000);
                cw.setY(-2000);
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void banglaclient$renderCleanImage4PauseMenu(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        sanitizePauseWidgets();

        BanglaClient bangla = BanglaClient.getInstance();
        int w = this.width;
        int h = this.height;
        int centerX = w / 2;
        int accent = bangla != null && bangla.getThemeManager() != null
                ? bangla.getThemeManager().getEffectiveAccentPrimary()
                : 0xFFEF4444;

        // 1. Live Minecraft Gameplay World Behind (NO static background image!)
        BlurRenderer.renderScreenBackdrop(context, w, h, 0.58f);

        // 2. Centered 3D "Bangla Client" Logo (Zero "Game Menu" text overlap)
        int logoCenterY = Math.max(26, (h - 200) / 2);
        drawGoldenCrown(context, centerX + 4, logoCenterY - 18);
        drawRedWings(context, centerX, logoCenterY + 12, accent);

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(centerX, logoCenterY - 2);
        matrices.scale(2.7f, 2.7f);
        String txtBangla = "Bangla";
        int bx = -this.textRenderer.getWidth(txtBangla) / 2;
        context.drawText(this.textRenderer, txtBangla, bx + 2, 2, 0xFF280404, false);
        context.drawText(this.textRenderer, txtBangla, bx + 1, 1, 0xFF7F1D1D, false);
        context.drawTextWithShadow(this.textRenderer, txtBangla, bx, 0, 0xFFEF4444);
        matrices.popMatrix();

        matrices.pushMatrix();
        matrices.translate(centerX, logoCenterY + 20);
        matrices.scale(2.3f, 2.3f);
        String txtClient = "Client";
        int cx = -this.textRenderer.getWidth(txtClient) / 2;
        context.drawText(this.textRenderer, txtClient, cx + 2, 2, 0xFF0F172A, false);
        context.drawText(this.textRenderer, txtClient, cx + 1, 1, 0xFF475569, false);
        context.drawTextWithShadow(this.textRenderer, txtClient, cx, 0, 0xFFFFFFFF);
        matrices.popMatrix();

        // 3. Render ONLY our 5 clean pause button rows
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
    private void drawRedWings(DrawContext context, int cx, int cy, int accent) {
        int span = 76;
        context.fill(cx - span, cy, cx - 42, cy + 3, 0xFFEF4444);
        context.fill(cx - span + 8, cy + 4, cx - 42, cy + 6, ColorUtils.withAlpha(accent, 0.65f));
        context.fill(cx + 42, cy, cx + span, cy + 3, 0xFFEF4444);
        context.fill(cx + 42, cy + 4, cx + span - 8, cy + 6, ColorUtils.withAlpha(accent, 0.65f));
    }
}
