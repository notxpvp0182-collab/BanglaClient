package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.world.World;

/**
 * Module 14: World Information
 * Displays current world dimension (Overworld, Nether, The End), difficulty, and loaded chunk count.
 */
public final class WorldInfoModule extends HudModule {
    private final BooleanSetting showDifficultySetting;
    private final BooleanSetting showLoadedChunksSetting;

    public WorldInfoModule() {
        super(
                "world_info",
                "World Information",
                "Displays current dimension, difficulty level, and loaded client chunk count.",
                ModuleCategory.HUD,
                160.0f,
                84.0f,
                130.0f,
                20.0f,
                false
        );
        this.showDifficultySetting = addSetting(new BooleanSetting(
                "show_difficulty", "Show Difficulty", "Include world difficulty (Peaceful/Easy/Normal/Hard)", true
        ));
        this.showLoadedChunksSetting = addSetting(new BooleanSetting(
                "show_chunks", "Show Loaded Chunks", "Include client chunk manager loaded chunk count", false
        ));
        addAliases("dimension", "difficulty", "overworld", "nether", "end");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        String dimension = "Overworld";
        String difficulty = "Normal";
        int loadedChunks = 289;

        if (client.world != null) {
            var regKey = client.world.getRegistryKey();
            if (regKey == World.OVERWORLD) {
                dimension = "Overworld";
            } else if (regKey == World.NETHER) {
                dimension = "The Nether";
            } else if (regKey == World.END) {
                dimension = "The End";
            } else {
                dimension = regKey.getValue().getPath();
            }

            difficulty = client.world.getDifficulty().getTranslatableName().getString();
            loadedChunks = client.world.getChunkManager().getLoadedChunkCount();
        }

        StringBuilder sb = new StringBuilder(dimension);
        if (showDifficultySetting.isEnabled()) {
            sb.append(" • ").append(difficulty);
        }
        if (showLoadedChunksSetting.isEnabled()) {
            sb.append(" (").append(loadedChunks).append(" chunks)");
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
