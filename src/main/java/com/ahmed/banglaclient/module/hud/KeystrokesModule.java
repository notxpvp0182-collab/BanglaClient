package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.util.ColorUtils;
import com.ahmed.banglaclient.util.MathUtils;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 03: Keystrokes
 * Displays WASD movement keys, Spacebar jump bar, and LMB/RMB mouse buttons with smooth press animations.
 */
public final class KeystrokesModule extends HudModule {
    private final BooleanSetting showMouseButtonsSetting;
    private final BooleanSetting showSpacebarSetting;
    private final BooleanSetting showCpsInButtonsSetting;

    private float wAlpha, aAlpha, sAlpha, dAlpha, lmbAlpha, rmbAlpha, spaceAlpha;

    public KeystrokesModule() {
        super(
                "keystrokes",
                "Keystrokes",
                "Displays WASD, spacebar, and mouse button activity with smooth Liquid Glass animations.",
                ModuleCategory.HUD,
                10.0f,
                60.0f,
                76.0f,
                78.0f,
                false
        );
        this.showMouseButtonsSetting = addSetting(new BooleanSetting(
                "show_mouse", "Show LMB / RMB", "Render left and right mouse button boxes below WASD", true
        ));
        this.showSpacebarSetting = addSetting(new BooleanSetting(
                "show_space", "Show Spacebar", "Render the jump bar at the bottom of the widget", true
        ));
        this.showCpsInButtonsSetting = addSetting(new BooleanSetting(
                "show_cps", "Show CPS on Mouse Keys", "Display live CPS count inside LMB/RMB boxes", true
         instanceof BooleanSetting b ? (BooleanSetting) b.visibleWhen(showMouseButtonsSetting::isEnabled) : null;
        addAliases("wasd", "keys", "input");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        boolean wDown = client.options != null && client.options.forwardKey.isPressed();
        boolean aDown = client.options != null && client.options.leftKey.isPressed();
        boolean sDown = client.options != null && client.options.backKey.isPressed();
        boolean dDown = client.options != null && client.options.rightKey.isPressed();
        boolean lmbDown = client.options != null && client.options.attackKey.isPressed();
        boolean rmbDown = client.options != null && client.options.useKey.isPressed();
        boolean spaceDown = client.options != null && client.options.jumpKey.isPressed();

        float step = 0.22f;
        wAlpha = MathUtils.lerp(wAlpha, wDown ? 1.0f : 0.0f, step);
        aAlpha = MathUtils.lerp(aAlpha, aDown ? 1.0f : 0.0f, step);
        sAlpha = MathUtils.lerp(sAlpha, sDown ? 1.0f : 0.0f, step);
        dAlpha = MathUtils.lerp(dAlpha, dDown ? 1.0f : 0.0f, step);
        lmbAlpha = MathUtils.lerp(lmbAlpha, lmbDown ? 1.0f : 0.0f, step);
        rmbAlpha = MathUtils.lerp(rmbAlpha, rmbDown ? 1.0f : 0.0f, step);
        spaceAlpha = MathUtils.lerp(spaceAlpha, spaceDown ? 1.0f : 0.0f, step);

        int keySize = 24;
        int gap = 2;
        int totalW = keySize * 3 + gap * 2;
        int baseX = Math.round(getPosX());
        int curY = Math.round(getPosY());

        // Row 1: [W] centered
        renderKeyBox(context, baseX + keySize + gap, curY, keySize, keySize, "W", null, wAlpha);
        curY += keySize + gap;

        // Row 2: [A] [S] [D]
        renderKeyBox(context, baseX, curY, keySize, keySize, "A", null, aAlpha);
        renderKeyBox(context, baseX + keySize + gap, curY, keySize, keySize, "S", null, sAlpha);
        renderKeyBox(context, baseX + (keySize + gap) * 2, curY, keySize, keySize, "D", null, dAlpha);
        curY += keySize + gap;

        // Row 3: [LMB] [RMB]
        if (showMouseButtonsSetting.isEnabled()) {
            int mouseW = (totalW - gap) / 2;
            int mouseH = showCpsInButtonsSetting.isEnabled() ? 22 : 18;
            String lmbSub = null;
            String rmbSub = null;

            if (showCpsInButtonsSetting.isEnabled() && BanglaClient.getInstance() != null) {
                var cpsMod = BanglaClient.getInstance().getModuleManager().getModule(CpsCounterModule.class);
                if (cpsMod.isPresent()) {
                    lmbSub = cpsMod.get().getLeftCps() + " CPS";
                    rmbSub = cpsMod.get().getRightCps() + " CPS";
                }
            }

            renderKeyBox(context, baseX, curY, mouseW, mouseH, "LMB", lmbSub, lmbAlpha);
            renderKeyBox(context, baseX + mouseW + gap, curY, totalW - mouseW - gap, mouseH, "RMB", rmbSub, rmbAlpha);
            curY += mouseH + gap;
        }

        // Row 4: [Spacebar]
        if (showSpacebarSetting.isEnabled()) {
            int spaceH = 12;
            renderKeyBox(context, baseX, curY, totalW, spaceH, "---", null, spaceAlpha);
            curY += spaceH;
        } else {
            curY -= gap;
        }

        setWidth(totalW);
        setHeight(curY - Math.round(getPosY()));
    }

    private void renderKeyBox(DrawContext context, int x, int y, int w, int h, String label, String subLabel, float pressProgress) {
        int accent = BanglaClient.getInstance() != null && BanglaClient.getInstance().getThemeManager() != null
                ? BanglaClient.getInstance().getThemeManager().getEffectiveAccentPrimary()
                : 0xFF38BDF8;

        int baseBg = backgroundColorSetting.getArgb();
        int pressedBg = ColorUtils.withAlpha(accent, 0.72f);
        int fill = ColorUtils.lerpColor(baseBg, pressedBg, pressProgress);

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(context, x, y, w, h, fill, pressProgress > 0.3f, pressProgress > 0.5f, showBorderSetting.isEnabled());
        }

        int textColor = ColorUtils.lerpColor(textColorSetting.getArgb(), 0xFFFFFFFF, pressProgress);
        int lw = client.textRenderer.getWidth(label);
        if (subLabel == null) {
            context.drawTextWithShadow(client.textRenderer, label, x + (w - lw) / 2, y + (h - 8) / 2, textColor);
        } else {
            context.drawTextWithShadow(client.textRenderer, label, x + (w - lw) / 2, y + 3, textColor);
            int sw = client.textRenderer.getWidth(subLabel);
            context.drawTextWithShadow(client.textRenderer, subLabel, x + (w - sw) / 2, y + 12,
                    ColorUtils.multiplyAlpha(textColor, 0.80f));
        }
    }
}
