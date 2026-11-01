package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

/**
 * Module 54: Held Item Display
 * Displays the currently held item's icon, custom/localized name, stack count, and optional registry ID metadata.
 */
public final class HeldItemDisplayModule extends HudModule {
    private final BooleanSetting showRegistryIdSetting;

    public HeldItemDisplayModule() {
        super(
                "held_item_display",
                "Held Item Display",
                "Displays the held item's icon, name, count, and optional registry metadata.",
                ModuleCategory.INVENTORY,
                590.0f,
                150.0f,
                132.0f,
                26.0f,
                false
        );
        this.showRegistryIdSetting = addSetting(new BooleanSetting(
                "show_id", "Show Registry ID", "Display the item's Minecraft identifier below its name", false
        ));
        addAliases("helditem", "mainhand", "itemname");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        ItemStack stack = client.player != null ? client.player.getMainHandStack() : ItemStack.EMPTY;
        if (stack.isEmpty() && preview) {
            stack = new ItemStack(Items.DIAMOND_PICKAXE);
        }
        if (stack.isEmpty()) {
            return;
        }

        String name = stack.getName().getString();
        if (stack.getCount() > 1) {
            name += " (x" + stack.getCount() + ")";
        }
        String regId = Registries.ITEM.getId(stack.getItem()).toString();

        int pad = paddingSetting.getIntValue();
        Theme theme = GlassRenderer.getTheme();

        int textW = client.textRenderer.getWidth(name);
        if (showRegistryIdSetting.isEnabled()) {
            textW = Math.max(textW, client.textRenderer.getWidth(regId));
        }

        int boxW = 22 + textW + pad * 2;
        int boxH = (showRegistryIdSetting.isEnabled() ? 22 : 16) + pad * 2;

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

        context.drawItem(stack, x + pad, y + (boxH - 16) / 2);
        if (showRegistryIdSetting.isEnabled()) {
            context.drawTextWithShadow(client.textRenderer, name, x + pad + 20, y + pad + 1, textColorSetting.getArgb());
            context.drawTextWithShadow(client.textRenderer, regId, x + pad + 20, y + pad + 12, theme.getTextSecondary());
        } else {
            context.drawTextWithShadow(client.textRenderer, name, x + pad + 20, y + pad + 4, textColorSetting.getArgb());
        }
    }
}
