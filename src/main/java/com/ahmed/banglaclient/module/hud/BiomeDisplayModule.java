package com.ahmed.banglaclient.module.hud;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

/**
 * Module 09: Biome Display
 * Displays the current biome name formatted cleanly from the world biome registry key.
 */
public final class BiomeDisplayModule extends HudModule {
    private final BooleanSetting showNamespaceSetting;

    public BiomeDisplayModule() {
        super(
                "biome_display",
                "Biome Display",
                "Displays the formatted name of the biome the player is currently standing in.",
                ModuleCategory.HUD,
                10.0f,
                220.0f,
                108.0f,
                20.0f,
                false
        );
        this.showNamespaceSetting = addSetting(new BooleanSetting(
                "show_namespace", "Show Registry ID", "Show raw identifier (e.g., minecraft:plains) instead of formatted title", false
        ));
        addAliases("biome", "region", "environment");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        String biomeText = "Plains";
        if (client.world != null && client.player != null) {
            Optional<RegistryKey<Biome>> keyOpt = client.world.getBiome(client.player.getBlockPos()).getKey();
            if (keyOpt.isPresent()) {
                Identifier id = keyOpt.get().getValue();
                biomeText = showNamespaceSetting.isEnabled() ? id.toString() : formatBiomePath(id.getPath());
            }
        }

        String text = "Biome: " + biomeText;
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

    private static String formatBiomePath(String path) {
        if (path == null || path.isEmpty()) {
            return "Unknown";
        }
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    sb.append(part.substring(1));
                }
                if (i < parts.length - 1) {
                    sb.append(' ');
                }
            }
        }
        return sb.toString();
    }
}
