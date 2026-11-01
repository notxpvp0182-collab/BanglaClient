package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.core.PerformancePreset;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 101: FPS Warning
 * Notifies the player when rolling average FPS drops below a configurable threshold and optionally
 * switches BANGLA CLIENT's rendering preset to `LOW` automatically to preserve smooth gameplay.
 */
public final class FpsWarningModule extends HudModule {
    private static final long ALERT_COOLDOWN_MS = 15_000L;

    private final NumberSetting minFpsThresholdSetting;
    private final BooleanSetting autoSwitchToLowPresetSetting;

    private int consecutiveLowTicks = 0;
    private long lastAlertTimeMillis = 0L;
    private boolean currentlyBelowThreshold = false;

    public FpsWarningModule() {
        super(
                "fps_warning",
                "FPS Warning",
                "Alerts the player when FPS drops below a threshold and optionally activates Low preset.",
                ModuleCategory.PERFORMANCE,
                440.0f,
                182.0f,
                138.0f,
                20.0f,
                false
        );
        this.minFpsThresholdSetting = addSetting(new NumberSetting(
                "threshold", "FPS Warning Threshold", "Trigger alert when sustained FPS falls below this value",
                30.0, 15.0, 90.0, 5.0, " FPS"
        ));
        this.autoSwitchToLowPresetSetting = addSetting(new BooleanSetting(
                "auto_low_preset", "Auto-Switch to LOW Glass Mode", "Automatically switch UI preset to LOW during heavy frame drops", false
        ));
        addAliases("lowfps", "framedrop", "lagwarning");
    }

    @Override
    public void onTick() {
        if (client == null || client.player == null) {
            consecutiveLowTicks = 0;
            currentlyBelowThreshold = false;
            return;
        }

        int fps = client.getCurrentFps();
        int threshold = minFpsThresholdSetting.getIntValue();

        if (fps > 0 && fps < threshold) {
            consecutiveLowTicks++;
            if (consecutiveLowTicks >= 40) { // Sustained for 2 seconds (40 ticks)
                currentlyBelowThreshold = true;
                long now = System.currentTimeMillis();
                if (now - lastAlertTimeMillis > ALERT_COOLDOWN_MS) {
                    lastAlertTimeMillis = now;
                    BanglaClient bangla = BanglaClient.getInstance();
                    if (bangla != null) {
                        if (autoSwitchToLowPresetSetting.isEnabled() && bangla.getPerformancePreset() != PerformancePreset.LOW) {
                            bangla.setPerformancePreset(PerformancePreset.LOW);
                            if (bangla.getNotificationManager() != null) {
                                bangla.getNotificationManager().pushWarning(
                                        "Low FPS Detected (" + fps + " FPS)",
                                        "Switched Glass Rendering to LOW preset."
                                );
                            }
                        } else if (bangla.getNotificationManager() != null) {
                            bangla.getNotificationManager().pushWarning(
                                    "Low FPS Warning",
                                    "Current FPS (" + fps + ") is below threshold (" + threshold + ")."
                            );
                        }
                    }
                }
            }
        } else {
            consecutiveLowTicks = 0;
            currentlyBelowThreshold = false;
        }
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        if (!currentlyBelowThreshold && !preview) {
            return;
        }

        int fps = client != null ? client.getCurrentFps() : 22;
        String text = "⚠ LOW FPS: " + (preview ? 24 : fps) + " FPS";

        int pad = paddingSetting.getIntValue();
        int boxW = client.textRenderer.getWidth(text) + pad * 2;
        int boxH = client.textRenderer.fontHeight + pad * 2;

        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(
                    context, x, y, boxW, boxH,
                    backgroundColorSetting.getArgb(), false, true, showBorderSetting.isEnabled()
            );
        }

        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, ThemeTokens.STATUS_ERROR);
    }
}
