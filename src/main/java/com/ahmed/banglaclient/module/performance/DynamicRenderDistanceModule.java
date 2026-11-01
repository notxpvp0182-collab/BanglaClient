package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Module 107: Dynamic Render Distance
 * Provides configurable render-distance convenience controls within vanilla client limits
 * (e.g., automatically lowering render distance when the game window is unfocused to save power/GPU heat,
 * and restoring the user's normal render distance upon re-focusing).
 */
public final class DynamicRenderDistanceModule extends Module {
    private final NumberSetting activeChunksSetting;
    private final BooleanSetting reduceWhenUnfocusedSetting;
    private final NumberSetting unfocusedChunksSetting;

    private boolean currentlyThrottled = false;

    public DynamicRenderDistanceModule() {
        super(
                "dynamic_render_distance",
                "Dynamic Render Distance",
                "Manages client render distance within vanilla limits and lowers it when unfocused.",
                ModuleCategory.PERFORMANCE
        );
        this.activeChunksSetting = addSetting(new NumberSetting(
                "normal_distance", "Active Render Distance", "Normal render distance while playing",
                12.0, 2.0, 32.0, 1.0, " chunks"
        ));
        this.reduceWhenUnfocusedSetting = addSetting(new BooleanSetting(
                "unfocus_reduce", "Lower When Window Unfocused", "Reduce render distance while Alt-Tabbed to save system resources", true
        ));
        this.unfocusedChunksSetting = addSetting(new NumberSetting(
                "unfocus_distance", "Unfocused Distance", "Render distance while the Minecraft window is unfocused",
                4.0, 2.0, 12.0, 1.0, " chunks"
        )).visibleWhen(reduceWhenUnfocusedSetting::isEnabled) instanceof NumberSetting ns ? ns : null;
        addAliases("renderdistance", "viewdistance", "chunks", "dynamicchunks");
    }

    @Override
    protected void onDisable() {
        if (currentlyThrottled && client != null && client.options != null) {
            client.options.getViewDistance().setValue(activeChunksSetting.getIntValue());
        }
        currentlyThrottled = false;
    }

    @Override
    public void onTick() {
        if (client == null || client.options == null) {
            return;
        }
        int targetChunks = activeChunksSetting.getIntValue();
        if (reduceWhenUnfocusedSetting.isEnabled() && !client.isWindowFocused()) {
            targetChunks = unfocusedChunksSetting != null ? unfocusedChunksSetting.getIntValue() : 4;
            currentlyThrottled = true;
        } else {
            currentlyThrottled = false;
        }

        if (client.options.getViewDistance().getValue() != targetChunks) {
            client.options.getViewDistance().setValue(targetChunks);
        }
    }
}
