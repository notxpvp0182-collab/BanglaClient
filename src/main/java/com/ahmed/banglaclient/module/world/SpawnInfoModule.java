package com.ahmed.banglaclient.module.world;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

/**
 * Module 76: Spawn Information
 * Displays known world spawn coordinates and horizontal Euclidean distance from the player.
 */
public final class SpawnInfoModule extends HudModule {
    private final BooleanSetting showDistanceSetting;

    public SpawnInfoModule() {
        super(
                "spawn_info",
                "Spawn Information",
                "Displays world spawn coordinates and horizontal distance to spawn.",
                ModuleCategory.WORLD,
                590.0f,
                306.0f,
                138.0f,
                20.0f,
                false
        );
        this.showDistanceSetting = addSetting(new BooleanSetting(
                "show_distance", "Show Distance to Spawn", "Append horizontal distance in blocks to world spawn", true
        ));
        addAliases("worldspawn", "spawn", "spawnpoint");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        BlockPos spawnPos = new BlockPos(0, 64, 0);
        double dist = 180.0;

        if (client.world != null) {
            spawnPos = client.world.getLevelProperties().getSpawnPoint().getPos();
            if (client.player != null) {
                double dx = client.player.getX() - (spawnPos.getX() + 0.5);
                double dz = client.player.getZ() - (spawnPos.getZ() + 0.5);
                dist = Math.hypot(dx, dz);
            }
        }

        String text = String.format("Spawn: %d, %d, %d", spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        if (showDistanceSetting.isEnabled()) {
            text += String.format(" (%.0fm)", dist);
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
