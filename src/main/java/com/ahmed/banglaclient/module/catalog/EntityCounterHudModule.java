package com.ahmed.banglaclient.module.catalog;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;

public final class EntityCounterHudModule extends HudModule {
    private final BooleanSetting opt1;
    private final BooleanSetting opt2;
    private final NumberSetting intensity;

    public EntityCounterHudModule() {
        super("entity_counter_hud", "Entity Counter", "Displays total loaded and rendered entities in the world.", ModuleCategory.HUD, 14.0f, 184.0f, 108.0f, 20.0f, false);
        this.opt1 = addSetting(new BooleanSetting("opt_1", "Show Culled Count", "Displays total loaded and rendered entities in the world.", true));
        this.opt2 = addSetting(new BooleanSetting("opt_2", "Highlight High Count", "Secondary option for Entity Counter", false));
        this.intensity = addSetting(new NumberSetting("refresh_rate", "Update Smoothness", "Update frequency & smoothing", 100.0, 20.0, 200.0, 10.0, "%"));
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int fps = client != null ? Math.max(1, client.getCurrentFps()) : 120;
        int lowFps = Math.max(1, (int) (fps * 0.82f));
        String ms = String.format("%.1f", 1000.0f / fps);
        Runtime rt = Runtime.getRuntime();
        int ram = (int) (((rt.totalMemory() - rt.freeMemory()) * 100L) / Math.max(1L, rt.maxMemory()));
        int ent = (client != null && client.world != null) ? client.world.getRegularEntityCount() : 12;
        int gpu = Math.min(99, Math.max(12, 100 - (fps / 4)));
        int cpu = Math.min(95, Math.max(8, ram / 2 + 10));
        float dist = (client != null && client.targetedEntity != null && client.player != null)
                ? (float) client.player.distanceTo(client.targetedEntity) : 3.0f;

        String text = "Entities: {ent}"
                .replace("{fps}", String.valueOf(lowFps))
                .replace("{ms}", ms)
                .replace("{gpu}", String.valueOf(gpu))
                .replace("{cpu}", String.valueOf(cpu))
                .replace("{ram}", String.valueOf(ram))
                .replace("{ent}", String.valueOf(ent))
                .replace("{part}", String.valueOf(ent * 3))
                .replace("{l}", "0")
                .replace("{r}", "0")
                .replace("{hits}", String.valueOf(ent))
                .replace("{dist}", String.format("%.1f", dist))
                .replace("{ping}", "28");

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
