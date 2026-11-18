package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.performance.PerformanceEngine;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import java.util.Locale;

public final class OnePercentLowFpsModule extends HudModule {
    private final BooleanSetting includeFrameTimeSetting;

    public OnePercentLowFpsModule() {
        super("one_percent_low_fps", "1% Low FPS & Frame Time",
                "Measures real 1% Low FPS and frame-time stability from a rolling 240-frame nanosecond window.",
                ModuleCategory.HUD, 10.0f, 34.0f, 96.0f, 20.0f, false);
        this.includeFrameTimeSetting = addSetting(new BooleanSetting("show_ms", "Show Frame Time (ms)", "Append real milliseconds per frame", true));
        addAliases("1%low", "frametime", "frametimegraph", "fpsbottleneck");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        PerformanceEngine eng = PerformanceEngine.get();
        String text = includeFrameTimeSetting.isEnabled()
                ? String.format(Locale.ROOT, "1%% Low: %d FPS (%.1f ms)", eng.getOnePercentLowFps(), eng.getCurrentFrameTimeMs())
                : ("1% Low: " + eng.getOnePercentLowFps() + " FPS");
        int pad = paddingSetting.getIntValue();
        int w = client.textRenderer.getWidth(text) + pad * 2;
        int h = client.textRenderer.fontHeight + pad * 2;
        setWidth(w);
        setHeight(h);
        int x = Math.round(getPosX()), y = Math.round(getPosY());
        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(context, x, y, w, h, backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled());
        }
        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, textColorSetting.getArgb());
    }
}
