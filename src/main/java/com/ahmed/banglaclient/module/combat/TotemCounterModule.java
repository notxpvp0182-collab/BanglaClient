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
 * Module 18: Totem Counter
 * Displays the total number of Totems of Undying in the player's inventory and offhand.
 */
public final class TotemCounterModule extends HudModule {
    private static final ItemStack TOTEM_ICON = new ItemStack(Items.TOTEM_OF_UNDYING);

    private final BooleanSetting hideWhenZeroSetting;

    public TotemCounterModule() {
        super(
                "totem_counter",
                "Totem Counter",
                "Displays the total count of Totems of Undying in inventory and offhand.",
                ModuleCategory.COMBAT,
                300.0f,
                146.0f,
                54.0f,
                24.0f,
                false
        );
        this.hideWhenZeroSetting = addSetting(new BooleanSetting(
                "hide_when_zero", "Hide When 0", "Only render on HUD when holding at least 1 Totem of Undying", false
        ));
        addAliases("totem", "totems", "undying", "pop");
    }

    public int countTotems() {
        if (client.player == null) {
            return 0;
        }
        int total = 0;
        var inv = client.player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.isOf(Items.TOTEM_OF_UNDYING)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int count = countTotems();
        if (count == 0 && hideWhenZeroSetting.isEnabled() && !preview) {
            return;
        }

        String text = "x" + (preview && count == 0 ? 3 : count);
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

        context.drawItem(TOTEM_ICON, x + pad, y + pad);
        int color = count == 0 && !preview ? ThemeTokens.STATUS_ERROR : textColorSetting.getArgb();
        context.drawTextWithShadow(client.textRenderer, text, x + pad + 19, y + pad + 4, color);
    }
}
