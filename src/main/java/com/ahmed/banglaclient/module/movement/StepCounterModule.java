package com.ahmed.banglaclient.module.movement;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 67: Step Counter
 * Tracks and displays cumulative horizontal steps/blocks traveled during the play session.
 */
public final class StepCounterModule extends HudModule {
    private final BooleanSetting showKilometersSetting;

    public StepCounterModule() {
        super(
                "step_counter",
                "Step Counter",
                "Counts player movement steps and distance traveled during the session.",
                ModuleCategory.MOVEMENT,
                160.0f,
                206.0f,
                104.0f,
                20.0f,
                false
        );
        this.showKilometersSetting = addSetting(new BooleanSetting(
                "km_format", "Show Kilometers (>1000m)", "Automatically format distances above 1,000 blocks in kilometers", true
        ));
        addAliases("steps", "pedometer", "distancetraveled");
    }

    @Override
    public void onTick() {
        if (client.player == null) {
            return;
        }
        double dx = client.player.getX() - client.player.prevX;
        double dz = client.player.getZ() - client.player.prevZ;
        double dist = Math.hypot(dx, dz);
        if (dist > 0.001 && BanglaClient.getInstance() != null && BanglaClient.getInstance().getSessionTracker() != null) {
            BanglaClient.getInstance().getSessionTracker().addDistance(dist);
        }
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        double blocks = 0.0;
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getSessionTracker() != null) {
            blocks = BanglaClient.getInstance().getSessionTracker().getSessionDistanceBlocks();
        }
        if (preview && blocks < 1.0) {
            blocks = 1420.5;
        }

        String text;
        if (showKilometersSetting.isEnabled() && blocks >= 1000.0) {
            text = String.format("Steps: %d (%.2f km)", (int) Math.round(blocks), blocks / 1000.0);
        } else {
            text = String.format("Steps: %d m", (int) Math.round(blocks));
        }

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
                    backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
            );
        }

        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, textColorSetting.getArgb());
    }
}
