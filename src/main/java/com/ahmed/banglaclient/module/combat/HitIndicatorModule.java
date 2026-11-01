package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.ColorSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.util.ColorUtils;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;

/**
 * Module 20: Hit Indicator
 * Renders a brief, customizable visual hitmarker pulse around the screen center when the player lands an attack.
 */
public final class HitIndicatorModule extends HudModule {
    private final ColorSetting markerColorSetting;
    private final NumberSetting durationMsSetting;
    private final NumberSetting markerSizeSetting;

    private long lastHitTimestamp = 0L;
    private static boolean hookRegistered = false;
    private static HitIndicatorModule instance;

    public HitIndicatorModule() {
        super(
                "hit_indicator",
                "Hit Indicator",
                "Provides visual hitmarker feedback around the crosshair when attacking an entity.",
                ModuleCategory.COMBAT,
                200.0f,
                200.0f,
                24.0f,
                24.0f,
                false
        );
        this.markerColorSetting = addSetting(new ColorSetting(
                "marker_color", "Marker Color", "Color of the hitmarker ticks", 0xFF38BDF8, true
        ));
        this.durationMsSetting = addSetting(new NumberSetting(
                "duration_ms", "Pulse Duration", "Duration of the hitmarker fade in milliseconds",
                220.0, 80.0, 600.0, 20.0, "ms"
        ));
        this.markerSizeSetting = addSetting(new NumberSetting(
                "marker_size", "Marker Span", "Span of the hitmarker arms in pixels",
                6.0, 3.0, 14.0, 1.0, "px"
        ));
        instance = this;
        registerHookOnce();
        addAliases("hitmarker", "attackindicator", "hit");
    }

    private static synchronized void registerHookOnce() {
        if (hookRegistered) {
            return;
        }
        hookRegistered = true;
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() && instance != null && instance.isEnabled() && entity instanceof LivingEntity) {
                instance.lastHitTimestamp = System.currentTimeMillis();
            }
            return ActionResult.PASS;
        });
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        long elapsed = System.currentTimeMillis() - lastHitTimestamp;
        long maxDur = durationMsSetting.getIntValue();

        if (!preview && (elapsed < 0L || elapsed > maxDur)) {
            return;
        }

        float alphaFactor = preview ? 1.0f : (1.0f - ((float) elapsed / (float) maxDur));
        int color = ColorUtils.multiplyAlpha(markerColorSetting.getArgb(), alphaFactor);

        int cx = preview ? Math.round(getPosX() + 12) : client.getWindow().getScaledWidth() / 2;
        int cy = preview ? Math.round(getPosY() + 12) : client.getWindow().getScaledHeight() / 2;
        int gap = 4;
        int len = markerSizeSetting.getIntValue();

        // Draw 4 corner brackets around the crosshair center
        context.fill(cx - gap - len, cy - gap - 1, cx - gap, cy - gap, color);
        context.fill(cx - gap - 1, cy - gap - len, cx - gap, cy - gap, color);

        context.fill(cx + gap, cy - gap - 1, cx + gap + len, cy - gap, color);
        context.fill(cx + gap, cy - gap - len, cx + gap + 1, cy - gap, color);

        context.fill(cx - gap - len, cy + gap, cx - gap, cy + gap + 1, color);
        context.fill(cx - gap - 1, cy + gap, cx - gap, cy + gap + len, color);

        context.fill(cx + gap, cy + gap, cx + gap + len, cy + gap + 1, color);
        context.fill(cx + gap, cy + gap, cx + gap + 1, cy + gap + len, color);
    }
}
