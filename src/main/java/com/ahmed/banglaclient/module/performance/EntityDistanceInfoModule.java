package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;

/**
 * Module 104: Entity Distance Info
 * Displays the exact Euclidean distance to a visible targeted entity and the current client Entity Distance scaling percentage.
 */
public final class EntityDistanceInfoModule extends HudModule {
    private final BooleanSetting showRenderScaleSetting;

    public EntityDistanceInfoModule() {
        super(
                "entity_distance_info",
                "Entity Distance Info",
                "Displays distance to the targeted entity and active entity render distance scale.",
                ModuleCategory.PERFORMANCE,
                440.0f,
                312.0f,
                132.0f,
                20.0f,
                false
        );
        this.showRenderScaleSetting = addSetting(new BooleanSetting(
                "show_scale", "Show Entity Distance %", "Include client Entity Distance scaling percentage", true
        ));
        addAliases("entitydistance", "reachdisplay", "targetdistance");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        Entity target = client != null ? client.targetedEntity : null;
        double dist = -1.0;
        if (target != null && client.player != null) {
            dist = client.player.distanceTo(target);
        } else if (preview) {
            dist = 2.84;
        }

        int scalePct = 100;
        if (client != null && client.options != null) {
            scalePct = (int) Math.round(client.options.getEntityDistanceScaling().getValue() * 100.0);
        }

        StringBuilder sb = new StringBuilder();
        if (dist >= 0.0) {
            sb.append(String.format("Target: %.2fm", dist));
        } else {
            sb.append("Target: None");
        }
        if (showRenderScaleSetting.isEnabled()) {
            sb.append(" (Scale: ").append(scalePct).append("%)");
        }

        String text = sb.toString();
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
