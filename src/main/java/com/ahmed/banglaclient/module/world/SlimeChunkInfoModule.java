package com.ahmed.banglaclient.module.world;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.StringSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.LightType;

import java.util.ArrayList;
import java.util.List;

/**
 * Advanced Slime Spawn & Chunk Analyzer Module:
 * - Detects Slime Chunks (Singleplayer automatic seed or custom Multiplayer seed)
 * - Displays Y-Level (< 40) spawn eligibility
 * - Displays Lunar Phase spawn rate & Light Level (<= 7) for surface/swamp spawning
 * - Optional 5x5 Chunk Radar Grid in settings
 */
public final class SlimeChunkInfoModule extends HudModule {
    private final BooleanSetting showSpawnRulesSetting;
    private final BooleanSetting showMoonAndLightSetting;
    private final BooleanSetting showChunkRadarSetting;
    private final StringSetting customMultiplayerSeedSetting;

    public SlimeChunkInfoModule() {
        super(
                "slime_chunk_info",
                "Slime Spawn Info",
                "Displays Slime Chunk status, Y-level (<40) spawn rules, moon phase rate, and a 5x5 chunk radar.",
                ModuleCategory.WORLD,
                590.0f,
                282.0f,
                146.0f,
                54.0f,
                false
        );
        this.showSpawnRulesSetting = addSetting(new BooleanSetting(
                "show_y_rule", "Show Y-Level Spawn Rule", "Show whether your current Y-level (< 40) allows slime chunk spawning", true
        ));
        this.showMoonAndLightSetting = addSetting(new BooleanSetting(
                "show_moon_light", "Show Moon & Light Details", "Show lunar phase slime spawn chance % and block/sky light level", true
        ));
        this.showChunkRadarSetting = addSetting(new BooleanSetting(
                "show_radar", "Show 5x5 Slime Chunk Radar", "Render a mini 5x5 surrounding chunk map highlighting slime chunks in green", true
        ));
        this.customMultiplayerSeedSetting = addSetting(new StringSetting(
                "known_seed", "Multiplayer World Seed", "Enter server seed to calculate Slime Chunks on multiplayer servers", "", 32
        ));
        addAliases("slime", "slimechunk", "slimespawn", "slimeradar", "seed");
    }

    private Long resolveAvailableSeed() {
        if (client == null) return null;
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

    private static boolean isChunkSlime(int chunkX, int chunkZ, long seed) {
        return ChunkRandom.getSlimeRandom(chunkX, chunkZ, seed, 987234911L).nextInt(10) == 0;
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        Long seed = resolveAvailableSeed();
        int chunkX = 0, chunkZ = 0, playerY = 32, light = 0;
        int moonPhase = 0;

        if (client.player != null && client.world != null) {
            BlockPos pos = client.player.getBlockPos();
            chunkX = pos.getX() >> 4;
            chunkZ = pos.getZ() >> 4;
            playerY = pos.getY();
            light = Math.max(client.world.getLightLevel(LightType.BLOCK, pos), client.world.getLightLevel(LightType.SKY, pos));
            moonPhase = (int) ((client.world.getTimeOfDay() / 24000L) % 8L);
        }

        boolean hasSeed = (seed != null) || preview;
        long effectiveSeed = seed != null ? seed : 123456789L;
        boolean inSlimeChunk = hasSeed && isChunkSlime(chunkX, chunkZ, effectiveSeed);

        List<String> lines = new ArrayList<>(4);
        List<Integer> lineColors = new ArrayList<>(4);
        Theme theme = GlassRenderer.getTheme();

        // Line 1: Slime Chunk Status
        if (!hasSeed) {
            lines.add("Slime Chunk: Seed Needed [" + chunkX + ", " + chunkZ + "]");
            lineColors.add(theme.getTextSecondary());
        } else {
            lines.add("Slime Chunk: " + (inSlimeChunk ? "YES (Active)" : "NO") + " [" + chunkX + ", " + chunkZ + "]");
            lineColors.add(inSlimeChunk ? ThemeTokens.STATUS_SUCCESS : textColorSetting.getArgb());
        }

        // Line 2: Y-Level Eligibility (< 40 for Slime Chunks, 50-70 for Swamp)
        if (showSpawnRulesSetting.isEnabled()) {
            if (playerY < 40 && playerY > -64) {
                lines.add("Height Y=" + playerY + ": Valid Slime Depth (<40)");
                lineColors.add(ThemeTokens.STATUS_SUCCESS);
            } else if (playerY >= 50 && playerY <= 70) {
                lines.add("Height Y=" + playerY + ": Swamp Range (50-70)");
                lineColors.add(ThemeTokens.STATUS_WARNING);
            } else {
                lines.add("Height Y=" + playerY + ": Need Y < 40 for Chunk Spawn");
                lineColors.add( theme.getTextSecondary());
            }
        }

        // Line 3: Moon Phase Spawn Rate & Light Level
        if (showMoonAndLightSetting.isEnabled()) {
            int moonChancePct = switch (moonPhase) {
                case 0 -> 100;
                case 1, 7 -> 75;
                case 2, 6 -> 50;
                case 3, 5 -> 25;
                default -> 0;
            };
            lines.add("Moon Spawn: " + moonChancePct + "% • Light: " + light + (light <= 7 ? " (OK)" : ""));
            lineColors.add(moonChancePct > 0 ? theme.getTextSecondary() : ThemeTokens.STATUS_ERROR);
        }

        int pad = paddingSetting.getIntValue();
        int maxTextW = 100;
        for (String s : lines) {
            maxTextW = Math.max(maxTextW, client.textRenderer.getWidth(s));
        }

        boolean drawRadar = showChunkRadarSetting.isEnabled() && hasSeed;
        int radarSize = drawRadar ? 32 : 0;
        int boxW = maxTextW + (drawRadar ? radarSize + 8 : 0) + pad * 2;
        int boxH = Math.max(lines.size() * 11, radarSize) + pad * 2;

        setWidth(boxW);
        setHeight(boxH);

        int x = Math.round(getPosX());
        int y = Math.round(getPosY());

        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(context, x, y, boxW, boxH, backgroundColorSetting.getArgb(), false, inSlimeChunk, showBorderSetting.isEnabled());
        }

        int textY = y + pad + 1;
        for (int i = 0; i < lines.size(); i++) {
            context.drawTextWithShadow(client.textRenderer, lines.get(i), x + pad, textY, lineColors.get(i));
            textY += 11;
        }

        // 5x5 Mini Slime Chunk Radar on the right side of the card
        if (drawRadar) {
            int cell = 6;
            int rx = x + boxW - pad - (5 * cell);
            int ry = y + (boxH - (5 * cell)) / 2;
            GlassRenderer.drawOutlineBorder(context, rx - 1, ry - 1, 5 * cell + 2, 5 * cell + 2, 0x55FFFFFF);

            for (int dz = -2; dz <= 2; dz++) {
                for (int dx = -2; dx <= 2; dx++) {
                    boolean slime = isChunkSlime(chunkX + dx, chunkZ + dz, effectiveSeed);
                    int cx = rx + (dx + 2) * cell;
                    int cy = ry + (dz + 2) * cell;
                    int fill = slime ? 0xCC34D399 : 0x331E293B;
                    context.fill(cx, cy, cx + cell - 1, cy + cell - 1, fill);
                    if (dx == 0 && dz == 0) {
                        // Player center dot
                        context.fill(cx + 1, cy + 1, cx + cell - 2, cy + cell - 2, 0xFFFFFFFF);
                    }
                }
            }
        }
    }
}
