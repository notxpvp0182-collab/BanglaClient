package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * Module 22: Item Durability
 * Displays the held item's remaining durability points and remaining percentage.
 */
public final class ItemDurabilityModule extends HudModule {
    private final BooleanSetting showMaxDurabilitySetting;
    private final BooleanSetting showPercentageSetting;

    public ItemDurabilityModule() {
        super(
                "item_durability",
                "Item Durability",
                "Displays held-item durability points and remaining percentage.",
                ModuleCategory.COMBAT,
                300.0f,
                200.0f,
                110.0f,
                24.0f,
                false
        );
        this.showMaxDurabilitySetting = addSetting(new BooleanSetting(
                "show_max", "Show Max Durability", "Display remaining/maximum points (e.g., 1420/2031)", true
        ));
        this.showPercentageSetting = addSetting(new BooleanSetting(
                "show_pct", "Show Percentage", "Append remaining durability percentage", true
        ));
        addAliases("helddurability", "tooldurability", "weapondurability");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        ItemStack stack = client.player != null ? client.player.getMainHandStack() : ItemStack.EMPTY;
        if (stack.isEmpty() && preview) {
            stack = new ItemStack(Items.NETHERITE_SWORD);
        }

        if (stack.isEmpty() || !stack.isDamageable() || stack.getMaxDamage() <= 0) {
            if (!preview) {
                return;
            }
        }

        int max = Math.max(1, stack.getMaxDamage());
        int rem = max - stack.getDamage();
        int pct = Math.round((rem * 100.0f) / max);

        StringBuilder sb = new StringBuilder();
        sb.append(rem);
        if (showMaxDurabilitySetting.isEnabled()) {
            sb.append("/").append(max);
        }
        if (showPercentageSetting.isEnabled()) {
            sb.append(" (").append(pct).append("%)");
        }

        String text = sb.toString();
        int pad = paddingSetting.getIntValue();
        int boxW = 20 + client.textRenderer.getWidth(text) + pad * 2;
        int boxH = 16 + pad * 2;

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

        context.drawItem(stack, x + pad, y + pad);
        int color = pct <= 15
                ? ThemeTokens.STATUS_ERROR
                : (pct <= 40 ? ThemeTokens.STATUS_WARNING : textColorSetting.getArgb());
        context.drawTextWithShadow(client.textRenderer, text, x + pad + 20, y + pad + 4, color);
    }
}
