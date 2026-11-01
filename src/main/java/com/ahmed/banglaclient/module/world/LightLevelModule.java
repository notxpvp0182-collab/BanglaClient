package com.ahmed.banglaclient.module.world;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

/**
 * Module 72: Light Level
 * Displays Block Light and Sky Light at the player's current block position,
 * warning in red when Block Light is `0` (the hostile mob spawn threshold in modern Minecraft).
 */
public final class LightLevelModule extends HudModule {
    private final BooleanSetting showSkyLightSetting;
    private final BooleanSetting warnMobSpawnThresholdSetting;

    public LightLevelModule() {
        super(
                "light_level",
                "Light Level",
                "Displays Block Light and Sky Light levels and warns when hostile mobs can spawn.",
                ModuleCategory.WORLD,
                160.0f,
                326.0f,
                118.0f,
                20.0f,
                false
        );
        this.showSkyLightSetting = addSetting(new BooleanSetting(
                "show_sky", "Show Sky Light", "Include Sky Light level alongside Block Light", true
        ));
        this.warnMobSpawnThresholdSetting = addSetting(new BooleanSetting(
                "spawn_warning", "Hostile Spawn Alert", "Highlight in red when Block Light is 0", true
        ));
        addAliases("light", "blocklight", "skylight", "mobspawn");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int blockLight = 12;
        int skyLight = 15;

        if (client.world != null && client.player != null) {
            BlockPos pos = client.player.getBlockPos();
            blockLight = client.world.getLightLevel(LightType.BLOCK, pos);
            skyLight = client.world.getLightLevel(LightType.SKY, pos);
        }

        StringBuilder sb = new StringBuilder("Light: ").append(blockLight).append(" Block");
        if (showSkyLightSetting.isEnabled()) {
            sb.append(" | ").append(skyLight).append(" Sky");
        }
        if (warnMobSpawnThresholdSetting.isEnabled() && blockLight == 0) {
            sb.append(" [SPAWN]");
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

        int color = (warnMobSpawnThresholdSetting.isEnabled() && blockLight == 0)
                ? ThemeTokens.STATUS_ERROR
                : textColorSetting.getArgb();
        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, color);
    }
}
