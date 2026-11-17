package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;

public final class ReachDisplayModule extends HudModule {
    private float lastReach = 0.0f;
    private long lastHitMs = 0L;

    public ReachDisplayModule() {
        super("reach_display", "Reach Display", "Displays exact attack distance in blocks when hitting a target.",
                ModuleCategory.COMBAT, 10.0f, 160.0f, 82.0f, 20.0f, false);
        addAliases("reach", "hitdistance", "attackrange", "trajectory");
    }

    @Override
    public void onTick() {
        if (client.player != null && client.targetedEntity != null && client.options.attackKey.isPressed()) {
            Entity target = client.targetedEntity;
            lastReach = (float) client.player.getEyePos().distanceTo(target.getBoundingBox().getCenter());
            lastHitMs = System.currentTimeMillis();
        }
        if (System.currentTimeMillis() - lastHitMs > 4000L) lastReach = 0.0f;
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        String text = (lastReach > 0.01f || preview)
                ? String.format("%.2f blocks", preview && lastReach == 0 ? 2.86f : lastReach)
                : "0.00 blocks";
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
