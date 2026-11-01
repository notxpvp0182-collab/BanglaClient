package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 12: Memory Usage
 * Displays current JVM heap memory usage, percentage, and optional maximum allocated memory.
 */
public final class MemoryUsageModule extends HudModule {
    private final BooleanSetting showMaxMemorySetting;
    private final BooleanSetting showProgressBarSetting;

    public MemoryUsageModule() {
        super(
                "memory_usage",
                "Memory Usage",
                "Displays current JVM heap memory usage and optional maximum allocated memory.",
                ModuleCategory.HUD,
                160.0f,
                34.0f,
                110.0f,
                22.0f,
                false
        );
        this.showMaxMemorySetting = addSetting(new BooleanSetting(
                "show_max_mb", "Show Max Allocated MB", "Display used/max megabytes in addition to percentage", true
        ));
        this.showProgressBarSetting = addSetting(new BooleanSetting(
                "show_bar", "Show Usage Bar", "Render a mini progress bar at the bottom of the widget", true
        ));
        addAliases("ram", "heap", "jvm", "mem");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        Runtime rt = Runtime.getRuntime();
        long maxBytes = rt.maxMemory();
        long usedBytes = rt.totalMemory() - rt.freeMemory();

        long usedMb = usedBytes / (1024L * 1024L);
        long maxMb = Math.max(1L, maxBytes / (1024L * 1024L));
        int pct = (int) Math.min(100L, (usedMb * 100L) / maxMb);

        String text = showMaxMemorySetting.isEnabled()
                ? String.format("MEM: %d%% (%d/%d MB)", pct, usedMb, maxMb)
                : String.format("MEM: %d%%", pct);

        int pad = paddingSetting.getIntValue();
        int barExtraH = showProgressBarSetting.isEnabled() ? 5 : 0;
        int boxW = client.textRenderer.getWidth(text) + pad * 2;
        int boxH = client.textRenderer.fontHeight + pad * 2 + barExtraH;

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

        if (showProgressBarSetting.isEnabled()) {
            int barX = x + pad;
            int barY = y + boxH - pad - 2;
            int barW = boxW - pad * 2;
            context.fill(barX, barY, barX + barW, barY + 2, 0x44FFFFFF);

            int fillW = Math.round((pct / 100.0f) * barW);
            int barColor = pct >= 85
                    ? ThemeTokens.STATUS_ERROR
                    : (pct >= 70 ? ThemeTokens.STATUS_WARNING : ThemeTokens.STATUS_SUCCESS);
            if (fillW > 0) {
                context.fill(barX, barY, barX + fillW, barY + 2, barColor);
            }
        }
    }
}
