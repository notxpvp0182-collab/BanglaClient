package com.ahmed.banglaclient.module.world;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Module 77: Entity Counter
 * Displays the number of loaded client-side entities, with an optional breakdown by
 * Players, Hostiles, Passives, and Item Drops.
 */
public final class EntityCounterModule extends HudModule {
    private final BooleanSetting showDetailedBreakdownSetting;

    public EntityCounterModule() {
        super(
                "entity_counter",
                "Entity Counter",
                "Displays the number of loaded client entities and optional category breakdown.",
                ModuleCategory.WORLD,
                590.0f,
                330.0f,
                148.0f,
                20.0f,
                false
        );
        this.showDetailedBreakdownSetting = addSetting(new BooleanSetting(
                "detailed_breakdown", "Show Category Breakdown", "Append counts of Players, Hostiles, Passives, and Items", true
        ));
        addAliases("entities", "entitycount", "mobs", "loadedentities");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int total = 48;
        int players = 2;
        int hostiles = 11;
        int passives = 19;
        int items = 6;

        if (client.world != null) {
            total = 0;
            players = 0;
            hostiles = 0;
            passives = 0;
            items = 0;
            for (Entity entity : client.world.getEntities()) {
                total++;
                if (entity instanceof PlayerEntity) {
                    players++;
                } else if (entity instanceof Monster) {
                    hostiles++;
                } else if (entity instanceof PassiveEntity) {
                    passives++;
                } else if (entity instanceof ItemEntity) {
                    items++;
                }
            }
        }

        String text = showDetailedBreakdownSetting.isEnabled()
                ? String.format("Entities: %d (P:%d H:%d M:%d I:%d)", total, players, hostiles, passives, items)
                : ("Entities: " + total);

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
