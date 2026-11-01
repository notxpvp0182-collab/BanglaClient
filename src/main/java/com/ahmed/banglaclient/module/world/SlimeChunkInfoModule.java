package com.ahmed.banglaclient.module.world;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.StringSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.ChunkRandom;

/**
 * Module 75: Slime Chunk Info
 * Displays whether the current chunk is a Slime Chunk when the world seed is legitimately available:
 * - In Singleplayer, reads the IntegratedServer Overworld seed directly.
 * - In Multiplayer (where servers send a SHA-256 hashed seed), uses an optional user-configured public seed
 *   or clearly reports "Seed Unavailable (Multiplayer)".
 */
public final class SlimeChunkInfoModule extends HudModule {
    private final StringSetting customMultiplayerSeedSetting;

    public SlimeChunkInfoModule() {
        super(
                "slime_chunk_info",
                "Slime Chunk Info",
                "Displays Slime Chunk status in Singleplayer or when a public server seed is configured.",
                ModuleCategory.WORLD,
                590.0f,
                282.0f,
                132.0f,
                20.0f,
                false
        );
        this.customMultiplayerSeedSetting = addSetting(new StringSetting(
                "known_seed", "Known Multiplayer Seed", "Optional numeric world seed for multiplayer servers with public seeds", "", 32
        ));
        addAliases("slime", "slimechunk", "seed");
    }

    private Long resolveAvailableSeed() {
        if (client == null) {
            return null;
        }
        if (client.isInSingleplayer()) {
            IntegratedServer server = client.getServer();
            if (server != null && server.getOverworld() != null) {
                return server.getOverworld().getSeed();
            }
        }
        String rawSeed = customMultiplayerSeedSetting.getValue().trim();
        if (!rawSeed.isEmpty()) {
            try {
                return Long.parseLong(rawSeed);
            } catch (NumberFormatException e) {
                return (long) rawSeed.hashCode();
            }
        }
        return null;
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        Long seed = resolveAvailableSeed();
        String text;
        int color = textColorSetting.getArgb();

        if (seed == null && !preview) {
            text = "Slime Chunk: Seed Unavailable";
            color = ThemeTokens.DEFAULT_TEXT_SECONDARY;
        } else {
            long effectiveSeed = seed != null ? seed : 123456789L;
            int chunkX = 0;
            int chunkZ = 0;
            if (client.player != null) {
                BlockPos pos = client.player.getBlockPos();
                chunkX = pos.getX() >> 4;
                chunkZ = pos.getZ() >> 4;
            }
            boolean isSlime = ChunkRandom.getSlimeRandom(chunkX, chunkZ, effectiveSeed, 987234911L).nextInt(10) == 0;
            text = isSlime ? "Slime Chunk: YES" : "Slime Chunk: NO";
            color = isSlime ? ThemeTokens.STATUS_SUCCESS : textColorSetting.getArgb();
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

        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, color);
    }
}
