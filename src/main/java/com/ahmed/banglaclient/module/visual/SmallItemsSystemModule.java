package com.ahmed.banglaclient.module.visual;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.NumberSetting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.*;
import net.minecraft.registry.tag.ItemTags;

public final class SmallItemsSystemModule extends Module {
    private final NumberSetting swordScale;
    private final NumberSetting axeScale;
    private final NumberSetting pickaxeScale;
    private final NumberSetting shovelScale;
    private final NumberSetting foodScale;
    private final NumberSetting blockScale;
    private final NumberSetting shieldScale;
    private final NumberSetting totemScale;

    public SmallItemsSystemModule() {
        super("small_item_system", "Small Item System",
                "Individually resize held Sword, Axe, Pickaxe, Shovel, Food, Block, Shield, and Totem.",
                ModuleCategory.VISUAL);
        this.swordScale = addSetting(new NumberSetting("small_sword", "Small Sword Scale", "Held sword size percentage", 70.0, 25.0, 120.0, 5.0, "%"));
        this.axeScale = addSetting(new NumberSetting("small_axe", "Small Axe Scale", "Held axe size percentage", 70.0, 25.0, 120.0, 5.0, "%"));
        this.pickaxeScale = addSetting(new NumberSetting("small_pickaxe", "Small Pickaxe Scale", "Held pickaxe size percentage", 75.0, 25.0, 120.0, 5.0, "%"));
        this.shovelScale = addSetting(new NumberSetting("small_shovel", "Small Shovel Scale", "Held shovel size percentage", 75.0, 25.0, 120.0, 5.0, "%"));
        this.foodScale = addSetting(new NumberSetting("small_food", "Small Food Scale", "Held food size percentage", 65.0, 25.0, 120.0, 5.0, "%"));
        this.blockScale = addSetting(new NumberSetting("small_block", "Small Block Scale", "Held block size percentage", 70.0, 25.0, 120.0, 5.0, "%"));
        this.shieldScale = addSetting(new NumberSetting("small_shield", "Small Shield Scale", "Held shield size percentage", 65.0, 25.0, 120.0, 5.0, "%"));
        this.totemScale = addSetting(new NumberSetting("totem_resizer", "Small Totem Resizer", "Held Totem of Undying size percentage", 60.0, 20.0, 120.0, 5.0, "%"));
        addAliases("smallsword", "smallaxe", "smallpickaxe", "smallshovel", "smallfood", "smallblock", "smallshield", "smalltotem", "totemresizer");
    }

    public float getScaleForStack(ItemStack stack) {
        if (!isEnabled() || stack == null || stack.isEmpty()) return 1.0f;
        if (stack.isOf(Items.TOTEM_OF_UNDYING)) return totemScale.getFloatValue() / 100.0f;
        if (stack.isOf(Items.SHIELD)) return shieldScale.getFloatValue() / 100.0f;
        if (stack.isIn(ItemTags.SWORDS)) return swordScale.getFloatValue() / 100.0f;
        if (stack.isIn(ItemTags.AXES)) return axeScale.getFloatValue() / 100.0f;
        if (stack.isIn(ItemTags.PICKAXES)) return pickaxeScale.getFloatValue() / 100.0f;
        if (stack.isIn(ItemTags.SHOVELS)) return shovelScale.getFloatValue() / 100.0f;
        if (stack.contains(DataComponentTypes.FOOD)) return foodScale.getFloatValue() / 100.0f;
        if (stack.getItem() instanceof BlockItem) return blockScale.getFloatValue() / 100.0f;
        return 1.0f;
    }
}
