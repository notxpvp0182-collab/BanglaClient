package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Module 15: Session Timer
 * Displays elapsed play time since launching BANGLA CLIENT or joining the current world/server.
 */
public final class SessionTimerModule extends HudModule {
    private final BooleanSetting trackWorldJoinOnlySetting;

    public SessionTimerModule() {
        super(
                "session_timer",
                "Session Timer",
                "Displays elapsed time since the current play session began.",
                ModuleCategory.HUD,
                160.0f,
                108.0f,
                92.0f,
                20.0f,
                false
        );
        this.trackWorldJoinOnlySetting = addSetting(new BooleanSetting(
                "reset_per_world", "Reset on World/Server Join", "Track time in current world instead of total client launch time", false
        ));
        addAliases("playtime", "uptime", "session", "timer");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        String formatted = "00:00";
        if (BanglaClient.getInstance() != null && BanglaClient.getInstance().getSessionTracker() != null) {
            var tracker = BanglaClient.getInstance().getSessionTracker();
            formatted = trackWorldJoinOnlySetting.isEnabled()
                    ? tracker.formatWorldSession()
                    : tracker.formatClientUptime();
        }

        String text = "Session: " + formatted;
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
