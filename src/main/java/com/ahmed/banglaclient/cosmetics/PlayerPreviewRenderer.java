package com.ahmed.banglaclient.cosmetics;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.entity.player.SkinTextures;

import java.util.function.Supplier;

public final class PlayerPreviewRenderer {

    private PlayerPreviewRenderer() {}

    public static PlayerSkinWidget createSkinWidget(int width, int height, Supplier<SkinTextures> skinSupplier) {
        MinecraftClient mc = MinecraftClient.getInstance();
        LoadedEntityModels models = mc.getLoadedEntityModels();
        return new PlayerSkinWidget(width, height, models, skinSupplier);
    }

    public static void drawPlayerHead(DrawContext context, SkinTextures textures, int x, int y, int size) {
        if (context == null || textures == null || size <= 0) return;
        PlayerSkinDrawer.draw(context, textures, x, y, size);
    }
}
