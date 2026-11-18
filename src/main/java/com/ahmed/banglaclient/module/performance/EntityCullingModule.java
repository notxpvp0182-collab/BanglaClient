package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class EntityCullingModule extends Module {
    private final NumberSetting cullingDistanceSetting;
    private final BooleanSetting ignorePlayersSetting;
    private final BooleanSetting ignoreNamedEntitiesSetting;

    public EntityCullingModule() {
        super("entity_culling", "Entity Culling",
                "Prevents distant and non-visible entities from being rendered unnecessarily.",
                ModuleCategory.PERFORMANCE, -1, true);
        this.cullingDistanceSetting = addSetting(new NumberSetting("cull_dist", "Culling Distance", "Max distance in blocks to render non-player entities", 64.0, 24.0, 128.0, 4.0, "m"));
        this.ignorePlayersSetting = addSetting(new BooleanSetting("keep_players", "Always Render Players", "Never cull player entities", true));
        this.ignoreNamedEntitiesSetting = addSetting(new BooleanSetting("keep_named", "Always Render Named Entities", "Never cull entities with custom nametags", true));
        addAliases("entityculling", "cullentities");
    }

    public double getMaxDistanceSq() {
        double d = cullingDistanceSetting.getValue();
        return d * d;
    }
    public boolean shouldKeepPlayers() { return ignorePlayersSetting.isEnabled(); }
    public boolean shouldKeepNamed() { return ignoreNamedEntitiesSetting.isEnabled(); }
}
