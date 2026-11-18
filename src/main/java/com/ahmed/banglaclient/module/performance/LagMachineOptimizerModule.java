package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

/**
 * Protects client FPS against Redstone Lag Machines, Piston spam, Armor Stand clusters,
 * stacked Minecarts/Boats, Falling Block columns, and firework/redstone particle floods.
 */
public final class LagMachineOptimizerModule extends Module {
    private final BooleanSetting blockRedstoneParticlesSetting;
    private final BooleanSetting limitMovingPistonsSetting;
    private final NumberSetting maxPistonsPerFrameSetting;
    private final BooleanSetting limitStackedEntitiesSetting;
    private final NumberSetting maxEntitiesPerCellSetting;
    private final BooleanSetting cullFallingBlocksSetting;

    public LagMachineOptimizerModule() {
        super(
                "lag_machine_optimizer",
                "Redstone & Lag Machine Shield",
                "Neutralizes redstone lag machines, mass moving pistons, stacked armor stands/minecarts, and falling block lag.",
                ModuleCategory.PERFORMANCE,
                -1,
                true
        );
        this.blockRedstoneParticlesSetting = addSetting(new BooleanSetting(
                "no_redstone_dust", "Block Redstone Wire Particles", "Cancel redstone dust particle spam from rapid clocks", true
        ));
        this.limitMovingPistonsSetting = addSetting(new BooleanSetting(
                "limit_pistons", "Throttle Mass Moving Pistons", "Limit how many moving piston block entities render per frame", true
        ));
        this.maxPistonsPerFrameSetting = addSetting(new NumberSetting(
                "max_pistons", "Max Pistons Rendered", "Maximum moving pistons rendered simultaneously",
                24.0, 4.0, 64.0, 4.0, ""
        ));
        this.limitStackedEntitiesSetting = addSetting(new BooleanSetting(
                "limit_stacked", "Cull Stacked Carts / Armor Stands", "Prevent 200+ stacked minecarts or armor stands from lagging FPS", true
        ));
        this.maxEntitiesPerCellSetting = addSetting(new NumberSetting(
                "max_per_cell", "Max Entities Per Block Cell", "Maximum entities rendered inside the same 2x2 block cell",
                5.0, 1.0, 20.0, 1.0, ""
        ));
        this.cullFallingBlocksSetting = addSetting(new BooleanSetting(
                "cull_falling", "Optimize Falling Sand / Gravel", "Throttle mass falling block rendering in sand/tnt cannons", true
        ));
        addAliases("redstoneoptimizer", "lagmachine", "pistonoptimizer", "armorstandlag", "cartoptimizer", "fallingblock");
    }

    public boolean shouldBlockRedstoneParticles() { return isEnabled() && blockRedstoneParticlesSetting.isEnabled(); }
    public boolean shouldLimitPistons() { return isEnabled() && limitMovingPistonsSetting.isEnabled(); }
    public int getMaxPistonsPerFrame() { return maxPistonsPerFrameSetting.getIntValue(); }
    public boolean shouldLimitStackedEntities() { return isEnabled() && limitStackedEntitiesSetting.isEnabled(); }
    public int getMaxEntitiesPerCell() { return maxEntitiesPerCellSetting.getIntValue(); }
    public boolean shouldCullFallingBlocks() { return isEnabled() && cullFallingBlocksSetting.isEnabled(); }
}
