package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

/**
 * Module 117: Recipe Quick Access
 * Provides a convenient one-key shortcut to open the player's inventory screen for immediate vanilla Recipe Book access.
 */
public final class RecipeQuickAccessModule extends Module {

    public RecipeQuickAccessModule() {
        super(
                "recipe_quick_access",
                "Recipe Quick Access",
                "Provides a direct shortcut to open the inventory and vanilla Recipe Book.",
                ModuleCategory.UTILITY
        );
        addAliases("recipes", "recipebook", "crafting", "craftguide");
    }

    @Override
    protected void onEnable() {
        if (client != null && client.player != null && client.currentScreen == null) {
            client.setScreen(new InventoryScreen(client.player));
        }
        setEnabled(false);
    }
}
