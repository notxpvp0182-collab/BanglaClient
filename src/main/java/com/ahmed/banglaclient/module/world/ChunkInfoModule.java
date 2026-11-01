package com.ahmed.banglaclient.module.world;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

/**
 * Module 74: Chunk Information
 * Displays current Chunk X, Y, Z coordinates, sub-chunk relative block offsets (`0–15`), and region file coordinates.
 */
public final class ChunkInfoModule extends HudModule {
    private final BooleanSetting showSubChunkRelativeSetting;
    private final BooleanSetting showRegionFileSetting;

    public ChunkInfoModule() {
        super(
                "chunk_info",
                "Chunk Information",
                "Displays current chunk coordinates and relative block position within the chunk.",
                ModuleCategory.WORLD,
                160.0f,
                374.0f,
                130.0f,
                20.0f,
                false
        );
        this.showSubChunkRelativeSetting = addSetting(new BooleanSetting(
                "show_relative", "Show Sub-Chunk Offset", "Append (0..15) block position inside the current 16x16x16 section", true
        ));
        this.showRegionFileSetting = addSetting(new BooleanSetting(
                "show_region", "Show Region File", "Append r.X.Z.mca region file identifier", false
        ));
        addAliases("chunk", "chunkcoords", "subchunk", "region");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int chunkX = 8;
        int chunkY = 4;
        int chunkZ = -16;
        int relX = 4;
        int relY = 0;
        int relZ = 12;

        if (client.player != null) {
            BlockPos pos = client.player.getBlockPos();
            chunkX = pos.getX() >> 4;
            chunkY = pos.getY() >> 4;
            chunkZ = pos.getZ() >> 4;
            relX = pos.getX() & 15;
            relY = pos.getY() & 15;
            relZ = pos.getZ() & 15;
        }

        StringBuilder sb = new StringBuilder(String.format("Chunk: %d, %d, %d", chunkX, chunkY, chunkZ));
        if (showSubChunkRelativeSetting.isEnabled()) {
            sb.append(String.format(" [%d, %d, %d]", relX, relY, relZ));
        }
        if (showRegionFileSetting.isEnabled()) {
            sb.append(String.format(" (r.%d.%d)", chunkX >> 5, chunkZ >> 5));
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
