package com.ahmed.banglaclient.module.utility;

import com.ahmed.banglaclient.gui.ClickGuiScreen;
import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;

/**
 * Module 120: Module Search
 * Opens the Click GUI with the top search bar immediately focused for instant Spotlight-style module lookup.
 */
public final class ModuleSearchModule extends Module {

    public ModuleSearchModule() {
        super(
                "module_search",
                "Module Search",
                "Opens the Click GUI with the search bar automatically focused for instant lookup.",
                ModuleCategory.UTILITY
        );
        addAliases("search", "spotlight", "findmodule", "commandpalette");
    }

    @Override
    protected void onEnable() {
        if (client != null) {
            client.setScreen(new ClickGuiScreen(true));
        }
        setEnabled(false);
    }
}
