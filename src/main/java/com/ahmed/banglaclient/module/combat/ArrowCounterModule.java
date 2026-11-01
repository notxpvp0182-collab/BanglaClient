package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;

/**
 * Module 26: Arrow Counter
 * Displays available arrow count (regular, spectral, and tipped arrows) in the player's inventory.
 */
public final class ArrowCounterModule extends HudModule {
    private static final ItemStack ARROW_ICON = new ItemStack(Items.ARROW);

    private final BooleanSetting onlyWhenHoldingRangedSetting;

    public ArrowCounterModule() {
        super(
                "arrow_counter",
                "Arrow Counter",
                "Displays total arrow count available in the player's inventory.",
                ModuleCategory.COMBAT,
                415.0f,
                10.0f,
                58.0f,
                24.0f,
                false
        );
        this.onlyWhenHoldingRangedSetting = addSetting(new BooleanSetting(
                "only_with_bow", "Only With Bow/Crossbow", "Only display when holding a Bow or Crossbow", false
        ));
        addAliases("arrows", "ammo", "bow", "crossbow", "quiver");
    }

    private boolean isHoldingRangedWeapon() {
        if (client.player == null) {
            return false;
        }
        ItemStack main = client.player.getMainHandStack();
        ItemStack off = client.player.getOffHandStack();
        return main.getItem() instanceof BowItem
                || main.getItem() instanceof CrossbowItem
                || off.getItem() instanceof BowItem
                || off.getItem() instanceof CrossbowItem;
    }

    public int countArrows() {
        if (client.player == null) {
            return 0;
        }
        int total = 0;
        var inv = client.player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.isIn(ItemTags.ARROWS)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        if (!preview && onlyWhenHoldingRangedSetting.isEnabled() && !isHoldingRangedWeapon()) {
            return;
        }

        int count = preview && client.player == null ? 64 : countArrows();
        String text = "x" + count;

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

        context.drawItem(ARROW_ICON, x + pad, y + pad);
        int color = count == 0 ? ThemeTokens.STATUS_ERROR : textColorSetting.getArgb();
        context.drawTextWithShadow(client.textRenderer, text, x + pad + 19, y + pad + 4, color);
    }
}
