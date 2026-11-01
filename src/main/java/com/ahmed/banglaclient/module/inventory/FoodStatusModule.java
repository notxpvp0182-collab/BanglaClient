package com.ahmed.banglaclient.module.inventory;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;

/**
 * Module 51: Food Status
 * Displays the player's current hunger level, saturation level, and optional held food restoration preview
 * using Minecraft 1.21.11's `DataComponentTypes.FOOD` API.
 */
public final class FoodStatusModule extends HudModule {
    private final BooleanSetting showSaturationSetting;
    private final BooleanSetting showHeldFoodRestoreSetting;

    public FoodStatusModule() {
        super(
                "food_status",
                "Food Status",
                "Displays hunger, saturation, and held food restoration values.",
                ModuleCategory.INVENTORY,
                590.0f,
                66.0f,
                122.0f,
                20.0f,
                false
        );
        this.showSaturationSetting = addSetting(new BooleanSetting(
                "show_saturation", "Show Saturation", "Include exact saturation points next to hunger", true
        ));
        this.showHeldFoodRestoreSetting = addSetting(new BooleanSetting(
                "held_food_preview", "Held Food Preview", "Show (+Food / +Sat) when holding an edible item", true
        ));
        addAliases("hunger", "saturation", "appleskin", "food");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        int food = 20;
        float saturation = 5.0f;
        int heldBonusFood = 0;
        float heldBonusSat = 0.0f;

        if (client.player != null) {
            var hm = client.player.getHungerManager();
            food = hm.getFoodLevel();
            saturation = hm.getSaturationLevel();

            if (showHeldFoodRestoreSetting.isEnabled()) {
                ItemStack held = client.player.getMainHandStack();
                FoodComponent fc = held.get(DataComponentTypes.FOOD);
                if (fc == null) {
                    held = client.player.getOffHandStack();
                    fc = held.get(DataComponentTypes.FOOD);
                }
                if (fc != null) {
                    heldBonusFood = fc.nutrition();
                    heldBonusSat = fc.saturation();
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Food: ").append(food).append("/20");
        if (showSaturationSetting.isEnabled()) {
            sb.append(String.format(" (Sat: %.1f)", saturation));
        }
        if (heldBonusFood > 0) {
            sb.append(String.format(" [+%d | +%.1f]", heldBonusFood, heldBonusSat));
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

        int color = food <= 6 ? ThemeTokens.STATUS_WARNING : textColorSetting.getArgb();
        context.drawTextWithShadow(client.textRenderer, text, x + pad, y + pad + 1, color);
    }
}
