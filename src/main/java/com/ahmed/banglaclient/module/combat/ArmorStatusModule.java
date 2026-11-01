package com.ahmed.banglaclient.module.combat;

import com.ahmed.banglaclient.module.HudModule;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.EnumSetting;
import com.ahmed.banglaclient.rendering.GlassRenderer;
import com.ahmed.banglaclient.theme.ThemeTokens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Module 16: Armor Status
 * Displays equipped armor items and remaining durability (numeric or percentage) in vertical or horizontal layout.
 */
public final class ArmorStatusModule extends HudModule {
    public enum Orientation {
        VERTICAL("Vertical"),
        HORIZONTAL("Horizontal");

        private final String label;

        Orientation(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private final EnumSetting<Orientation> orientationSetting;
    private final BooleanSetting showPercentageSetting;
    private final BooleanSetting includeHeldItemSetting;

    public ArmorStatusModule() {
        super(
                "armor_status",
                "Armor Status",
                "Displays equipped armor icons and remaining durability.",
                ModuleCategory.COMBAT,
                300.0f,
                10.0f,
                64.0f,
                76.0f,
                false
        );
        this.orientationSetting = addSetting(new EnumSetting<>(
                "orientation", "Orientation", "Vertical stacked or horizontal row layout", Orientation.VERTICAL
        ));
        this.showPercentageSetting = addSetting(new BooleanSetting(
                "show_percent", "Percentage Mode", "Display remaining durability as a percentage instead of raw points", false
        ));
        this.includeHeldItemSetting = addSetting(new BooleanSetting(
                "include_hand", "Include Main Hand", "Also display the currently held main-hand item", false
        ));
        addAliases("armor", "durability", "helmet", "chestplate", "leggings", "boots");
    }

    @Override
    public void renderHud(DrawContext context, float tickDelta, boolean preview) {
        List<ItemStack> stacks = new ArrayList<>(5);
        if (client.player != null) {
            for (EquipmentSlot slot : ARMOR_SLOTS) {
                ItemStack stack = client.player.getEquippedStack(slot);
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
            if (includeHeldItemSetting.isEnabled()) {
                ItemStack mainHand = client.player.getMainHandStack();
                if (!mainHand.isEmpty()) {
                    stacks.add(mainHand);
                }
            }
        }

        if (stacks.isEmpty() && preview) {
            stacks.add(new ItemStack(Items.NETHERITE_HELMET));
            stacks.add(new ItemStack(Items.NETHERITE_CHESTPLATE));
            stacks.add(new ItemStack(Items.NETHERITE_LEGGINGS));
            stacks.add(new ItemStack(Items.NETHERITE_BOOTS));
        }

        int pad = paddingSetting.getIntValue();
        int x = Math.round(getPosX());
        int y = Math.round(getPosY());

        if (stacks.isEmpty()) {
            setWidth(64);
            setHeight(24);
            return;
        }

        if (orientationSetting.getValue() == Orientation.VERTICAL) {
            int maxTextW = 24;
            for (ItemStack s : stacks) {
                maxTextW = Math.max(maxTextW, client.textRenderer.getWidth(formatDurabilityText(s)));
            }
            int boxW = 20 + maxTextW + pad * 2;
            int boxH = stacks.size() * 18 + pad * 2 - 2;
            setWidth(boxW);
            setHeight(boxH);

            if (showBackgroundSetting.isEnabled()) {
                GlassRenderer.drawCustomGlassPanel(
                        context, x, y, boxW, boxH,
                        backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
                );
            }

            int rowY = y + pad;
            for (ItemStack stack : stacks) {
                context.drawItem(stack, x + pad, rowY);
                String text = formatDurabilityText(stack);
                int color = getDurabilityColor(stack);
                context.drawTextWithShadow(client.textRenderer, text, x + pad + 20, rowY + 4, color);
                rowY += 18;
            }
        } else {
            int slotW = 46;
            int boxW = stacks.size() * slotW + pad * 2;
            int boxH = 30 + pad * 2;
            setWidth(boxW);
            setHeight(boxH);

            if (showBackgroundSetting.isEnabled()) {
                GlassRenderer.drawCustomGlassPanel(
                        context, x, y, boxW, boxH,
                        backgroundColorSetting.getArgb(), false, false, showBorderSetting.isEnabled()
                );
            }

            int colX = x + pad;
            for (ItemStack stack : stacks) {
                context.drawItem(stack, colX + (slotW - 16) / 2, y + pad);
                String text = formatDurabilityText(stack);
                int tw = client.textRenderer.getWidth(text);
                int color = getDurabilityColor(stack);
                context.drawTextWithShadow(client.textRenderer, text, colX + (slotW - tw) / 2, y + pad + 18, color);
                colX += slotW;
            }
        }
    }

    private String formatDurabilityText(ItemStack stack) {
        if (!stack.isDamageable() || stack.getMaxDamage() <= 0) {
            return stack.getCount() > 1 ? ("x" + stack.getCount()) : "MAX";
        }
        int remaining = stack.getMaxDamage() - stack.getDamage();
        if (showPercentageSetting.isEnabled()) {
            int pct = Math.round((remaining * 100.0f) / stack.getMaxDamage());
            return pct + "%";
        }
        return String.valueOf(remaining);
    }

    private int getDurabilityColor(ItemStack stack) {
        if (!stack.isDamageable() || stack.getMaxDamage() <= 0) {
            return textColorSetting.getArgb();
        }
        float ratio = (float) (stack.getMaxDamage() - stack.getDamage()) / (float) stack.getMaxDamage();
        if (ratio <= 0.15f) {
            return ThemeTokens.STATUS_ERROR;
        } else if (ratio <= 0.40f) {
            return ThemeTokens.STATUS_WARNING;
        }
        return ThemeTokens.STATUS_SUCCESS;
    }
}
