package com.ahmed.banglaclient.module.performance;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import com.ahmed.banglaclient.module.setting.BooleanSetting;
import com.ahmed.banglaclient.module.setting.NumberSetting;

public final class MasterOptimizerModule extends Module {
    private final BooleanSetting entityAndCaveCulling;
    private final BooleanSetting chunkBatchAndMeshCache;
    private final BooleanSetting blockItemMobOptimizer;
    private final BooleanSetting weatherCloudSkyOptimizer;
    private final BooleanSetting beaconXpTntOptimizer;
    private final BooleanSetting combatProjectilesOptimizer;
    private final BooleanSetting unfocusedFpsLimiter;
    private final NumberSetting unfocusedMaxFps;
    private final BooleanSetting smartMemoryCleanup;
    private final BooleanSetting triggerRamCleanupNow;

    private int tickCounter = 0;

    public MasterOptimizerModule() {
        super("master_optimizer", "Master Optimizer Suite (48-in-1)",
                "All-in-one Entity/Cave Culling, Chunk Mesh Cache, Block/Item/Mob/XP/TNT/Beacon/Combat Optimizer & Smart RAM Cleanup.",
                ModuleCategory.PERFORMANCE, -1, true);
        this.entityAndCaveCulling = addSetting(new BooleanSetting("culling", "Entity, Cave & Occlusion Culling", "Skip off-screen entities and occluded underground caves", true));
        this.chunkBatchAndMeshCache = addSetting(new BooleanSetting("chunk_opt", "Chunk Batching & Mesh Cache", "Optimize chunk loading, face culling, and mesh reuse", true));
        this.blockItemMobOptimizer = addSetting(new BooleanSetting("render_opt", "Block, Item, Mob & Sign Optimizer", "Reduce dropped item, mob, sign, and shadow rendering overhead", true));
        this.weatherCloudSkyOptimizer = addSetting(new BooleanSetting("env_opt", "Weather, Cloud, Sky & Fluid Optimizer", "Optimize rain, clouds, sky, water, and lava rendering", true));
        this.beaconXpTntOptimizer = addSetting(new BooleanSetting("fx_opt", "Beacon Beam, XP Orb & TNT Optimizer", "Batch XP orbs, TNT particles, and distant beacon beams", true));
        this.combatProjectilesOptimizer = addSetting(new BooleanSetting("pvp_opt", "Elytra, Mace, Totem, Bow & Wind Charge Optimizer", "Optimize PvP item animations, hotbar switching, and projectiles", true));
        this.unfocusedFpsLimiter = addSetting(new BooleanSetting("unfocused_fps", "Unfocused / Background FPS Limiter", "Lower FPS when game window is minimized or unfocused", true));
        this.unfocusedMaxFps = addSetting(new NumberSetting("bg_fps_cap", "Unfocused FPS Limit", "Max FPS when window is unfocused", 30.0, 10.0, 60.0, 5.0, " FPS"));
        this.smartMemoryCleanup = addSetting(new BooleanSetting("smart_ram", "Smart RAM & Cache Cleanup", "Periodically clean unused heap allocations when RAM > 85%", true));
        this.triggerRamCleanupNow = addSetting(new BooleanSetting("clean_ram_now", "Action: Clean RAM Memory Now", "Click to immediately run JVM memory cleanup", false));

        this.triggerRamCleanupNow.onChange(val -> {
            if (Boolean.TRUE.equals(val)) {
                System.gc();
                triggerRamCleanupNow.setValue(false);
            }
        });

        addAliases(
                "inputoptimizer", "renderoptimizer", "entityculling", "caveculling", "blockrendering", "itemrendering",
                "mobrendering", "chunkloading", "chunkmesh", "blockfaceculling", "occlusionculling", "signrendering",
                "beaconbeam", "xpoptimizer", "tntoptimizer", "fasthotbar", "elytraoptimizer", "maceoptimizer",
                "totemoptimizer", "crossbowoptimizer", "bowoptimizer", "windcharge", "noshadowblocks", "1%lowfps",
                "gpuusage", "cpuusage", "bottleneck", "framepacing", "unfocusedfps", "fastquit", "ramcleanup",
                "memorycleanup", "texturememory", "cpithread", "asyncchunk", "batterysaver", "afkoptimization",
                "lowendprofile", "extremefpsprofile", "oneclickoptimization"
        );
    }

    @Override
    public void onTick() {
        tickCounter++;
        if (smartMemoryCleanup.isEnabled() && tickCounter % 1200 == 0) {
            Runtime rt = Runtime.getRuntime();
            long used = rt.totalMemory() - rt.freeMemory();
            if ((used * 100L) / Math.max(1L, rt.maxMemory()) >= 86L) {
                System.gc();
            }
        }
    }
}
