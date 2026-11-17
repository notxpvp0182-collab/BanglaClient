package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class OffhandHudModule extends HudModule {
    public OffhandHudModule() {
        super("offhand_hud", "Offhand HUD & Highlighter", "Displays your offhand item icon, stack count, and rarity highlight.",
                ModuleCategory.INVENTORY, 210.0f, 310.0f, 48.0f, 22.0f, false);
        addAliases("offhand", "itemhighlighter", "rarityhighlight", "tooltipcustomizer", "compacttooltip");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        ItemStack stack = (client.player != null) ? client.player.getOffHandStack() : ItemStack.EMPTY;
        if (stack.isEmpty() && preview) stack = new ItemStack(Items.SHIELD);
        if (stack.isEmpty()) return;

        int w = 48, h = 22;
        setWidth(w);
        setHeight(h);
        int x = Math.round(getPosX()), y = Math.round(getPosY());
        if (showBackgroundSetting.isEnabled()) {
            GlassRenderer.drawCustomGlassPanel(context, x, y, w, h, backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled());
        }
        context.drawItem(stack, x + 4, y + 3);
        String cnt = String.valueOf(stack.getCount());
        context.drawTextWithShadow(client.textRenderer, cnt, x + 24, y + 7, textColorSetting.getArgb());
    }
}
