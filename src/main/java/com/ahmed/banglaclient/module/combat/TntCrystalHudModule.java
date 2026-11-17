package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;

public final class TntCrystalHudModule extends HudModule {
    private final BooleanSetting showTntFuse;
    private final BooleanSetting showCrystalCount;

    public TntCrystalHudModule() {
        super("tnt_crystal_hud", "TNT Fuse & Crystal HUD", "Displays nearby primed TNT fuse countdown and active End Crystal status.",
                ModuleCategory.COMBAT, 10.0f, 184.0f, 118.0f, 20.0f, false);
        this.showTntFuse = addSetting(new BooleanSetting("tnt_fuse", "Show TNT Fuse Timer", "Display fuse countdown of nearest primed TNT", true));
        this.showCrystalCount = addSetting(new BooleanSetting("crystal_status", "Show Crystal Status", "Display count of nearby End Crystals", true));
        addAliases("tntfuse", "crystalstatus", "tnttimer", "crystaltimer");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int crystals = 0;
        int minFuse = -1;
        if (client.world != null && client.player != null) {
            for (Entity e : client.world.getEntities()) {
                if (e.squaredDistanceTo(client.player) > 256.0) continue;
                if (e instanceof EndCrystalEntity) crystals++;
                else if (e instanceof TntEntity tnt) {
                    int f = tnt.getFuse();
                    if (minFuse < 0 || f < minFuse) minFuse = f;
                }
            }
        }
        String text = minFuse >= 0 && showTntFuse.isEnabled()
                ? String.format("TNT: %.1fs | Crystals: %d", minFuse / 20.0f, crystals)
                : ("Crystals Nearby: " + crystals);
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
