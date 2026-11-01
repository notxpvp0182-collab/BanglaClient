package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.StringSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 01: FPS Counter
 * Displays the current Minecraft frames per second with customizable prefix, frame-time display,
 * position, scale, text color, and Liquid Glass background.
 */
public final class FpsCounterModule extends HudModule {
    private final StringSetting prefixSetting;
    private final BooleanSetting showFrameTimeSetting;

    public FpsCounterModule() {
        super(
                "fps_counter",
                "FPS Counter",
                "Displays current frames per second and optional frame time in milliseconds.",
                ModuleCategory.HUD,
                10.0f,
                10.0f,
                64.0f,
                20.0f,
                true
        );
        this.prefixSetting = addSetting(new StringSetting(
                "prefix", "Label Prefix", "Text prefix shown next to the FPS value", "FPS", 16
        ));
        this.showFrameTimeSetting = addSetting(new BooleanSetting(
                "show_frame_time", "Show Frame Time (ms)", "Append estimated frame time in milliseconds", false
        ));
        addAliases("framerate", "frames", "performance");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int fps = client != null ? client.getCurrentFps() : 60;
        String prefix = prefixSetting.getValue().trim();
        String text = prefix.isEmpty() ? String.valueOf(fps) : (fps + " " + prefix);

        if (showFrameTimeSetting.isEnabled() && fps > 0) {
            float frameMs = 1000.0f / (float) fps;
            text += String.format(" (%.1f ms)", frameMs);
        }

        int pad = paddingSetting.getIntValue();
        int textW = client.textRenderer.getWidth(text);
        int boxW = textW + pad * 2;
        int boxH = client.textRenderer.fontHeight + pad * 2;

        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(
                    context, x, y, boxW, boxH,
                    backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
            );
        }

        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, textColorSetting.getArgb());
    }
}
