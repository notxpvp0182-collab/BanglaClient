package com.ahmed.banglaclient.module.world;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Module 78: Mob Information
 * Summarizes visible nearby mobs (where `player.canSee(mob)` is true) within a configurable radius.
 * Never reveals hidden entities through walls.
 */
public final class MobInfoModule extends HudModule {
    private final NumberSetting scanRadiusSetting;

    public MobInfoModule() {
        super(
                "mob_info",
                "Mob Information",
                "Summarizes visible nearby mobs in direct line-of-sight.",
                ModuleCategory.WORLD,
                590.0f,
                354.0f,
                126.0f,
                36.0f,
                false
        );
        this.scanRadiusSetting = addSetting(new NumberSetting(
                "radius", "Visible Range", "Maximum line-of-sight distance in blocks to summarize visible mobs",
                24.0, 8.0, 48.0, 2.0, "m"
        ));
        addAliases("nearby", "visiblemobs", "mobcounter");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        Map<String, Integer> visibleMobCounts = new LinkedHashMap<>();
        double maxDist = scanRadiusSetting.getValue();

        if (client.world != null && client.player != null) {
            for (Entity entity : client.world.getEntities()) {
                if (entity instanceof MobEntity mob && mob.isAlive()) {
                    if (client.player.distanceTo(mob) <= maxDist && client.player.canSee(mob)) {
                        String typeName = mob.getType().getName().getString();
                        visibleMobCounts.merge(typeName, 1, Integer::sum);
                    }
                }
            }
        } else if (preview) {
            visibleMobCounts.put("Zombie", 2);
            visibleMobCounts.put("Cow", 3);
        }

        if (visibleMobCounts.isEmpty() && !preview) {
            return;
        }

        List<String> lines = new ArrayList<>();
        lines.add("Visible Mobs:");
        int shown = 0;
        for (Map.Entry<String, Integer> entry : visibleMobCounts.entrySet()) {
            if (shown >= 4) {
                break;
            }
            lines.add("• " + entry.getKey() + " x" + entry.getValue());
            shown++;
        }

        int pad = paddingSetting.getIntValue();
        int maxW = 84;
        for (String line : lines) {
            maxW = Math.max(maxW, client.textRenderer.getWidth(line));
        }

        int boxW = maxW + pad * 2;
        int boxH = lines.size() * 11 + pad * 2;
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

        int drawY = y + pad + 1;
        for (String line : lines) {
            context.drawTextWithShadow(client.textRenderer, line, x + pad, drawY, textColorSetting.getArgb());
            drawY += 11;
        }
    }
}
