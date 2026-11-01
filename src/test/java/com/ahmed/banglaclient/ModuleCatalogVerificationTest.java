package com.ahmed.banglaclient;

import com.ahmed.banglaclient.module.Module;
import com.ahmed.banglaclient.module.ModuleCategory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Automated JUnit 5 verification test confirming that all 10 module categories and all 120
 * standalone module classes exist on the compiled classpath and extend `Module`.
 */
public final class ModuleCatalogVerificationTest {

    private static final String[] ALL_120_MODULE_CLASSES = {
            // Category A — HUD & Information (01–15)
            "com.ahmed.banglaclient.module.hud.FpsCounterModule",
            "com.ahmed.banglaclient.module.hud.CpsCounterModule",
            "com.ahmed.banglaclient.module.hud.KeystrokesModule",
            "com.ahmed.banglaclient.module.hud.CoordinatesModule",
            "com.ahmed.banglaclient.module.hud.DirectionHudModule",
            "com.ahmed.banglaclient.module.hud.PingDisplayModule",
            "com.ahmed.banglaclient.module.hud.ClockModule",
            "com.ahmed.banglaclient.module.hud.SpeedometerModule",
            "com.ahmed.banglaclient.module.hud.BiomeDisplayModule",
            "com.ahmed.banglaclient.module.hud.WeatherHudModule",
            "com.ahmed.banglaclient.module.hud.DayCounterModule",
            "com.ahmed.banglaclient.module.hud.MemoryUsageModule",
            "com.ahmed.banglaclient.module.hud.ServerAddressModule",
            "com.ahmed.banglaclient.module.hud.WorldInfoModule",
            "com.ahmed.banglaclient.module.hud.SessionTimerModule",

            // Category B — PvP & Combat Information (16–27)
            "com.ahmed.banglaclient.module.combat.ArmorStatusModule",
            "com.ahmed.banglaclient.module.combat.PotionHudModule",
            "com.ahmed.banglaclient.module.combat.TotemCounterModule",
            "com.ahmed.banglaclient.module.combat.ComboCounterModule",
            "com.ahmed.banglaclient.module.combat.HitIndicatorModule",
            "com.ahmed.banglaclient.module.combat.CrosshairCustomizerModule",
            "com.ahmed.banglaclient.module.combat.ItemDurabilityModule",
            "com.ahmed.banglaclient.module.combat.TargetInfoModule",
            "com.ahmed.banglaclient.module.combat.AttackCooldownModule",
            "com.ahmed.banglaclient.module.combat.ShieldStatusModule",
            "com.ahmed.banglaclient.module.combat.ArrowCounterModule",
            "com.ahmed.banglaclient.module.combat.CombatTimerModule",

            // Category C — Visual Enhancement (28–42)
            "com.ahmed.banglaclient.module.visual.FullbrightModule",
            "com.ahmed.banglaclient.module.visual.ZoomModule",
            "com.ahmed.banglaclient.module.visual.CustomFogModule",
            "com.ahmed.banglaclient.module.visual.CustomSkyModule",
            "com.ahmed.banglaclient.module.visual.CustomWeatherModule",
            "com.ahmed.banglaclient.module.visual.DamageTintModule",
            "com.ahmed.banglaclient.module.visual.CustomParticlesModule",
            "com.ahmed.banglaclient.module.visual.DynamicCrosshairModule",
            "com.ahmed.banglaclient.module.visual.CustomPanoramaModule",
            "com.ahmed.banglaclient.module.visual.ClearWaterModule",
            "com.ahmed.banglaclient.module.visual.CustomFireOverlayModule",
            "com.ahmed.banglaclient.module.visual.CustomPumpkinOverlayModule",
            "com.ahmed.banglaclient.module.visual.CustomVignetteModule",
            "com.ahmed.banglaclient.module.visual.CustomHurtCameraModule",
            "com.ahmed.banglaclient.module.visual.CustomGlintModule",

            // Category D — Inventory & Item Information (43–57)
            "com.ahmed.banglaclient.module.inventory.InventoryHudModule",
            "com.ahmed.banglaclient.module.inventory.HotbarHudModule",
            "com.ahmed.banglaclient.module.inventory.ItemCounterModule",
            "com.ahmed.banglaclient.module.inventory.BlockCounterModule",
            "com.ahmed.banglaclient.module.inventory.ContainerPreviewModule",
            "com.ahmed.banglaclient.module.inventory.ShulkerPreviewModule",
            "com.ahmed.banglaclient.module.inventory.EquipmentHudModule",
            "com.ahmed.banglaclient.module.inventory.DurabilityViewerModule",
            "com.ahmed.banglaclient.module.inventory.FoodStatusModule",
            "com.ahmed.banglaclient.module.inventory.HealthHudModule",
            "com.ahmed.banglaclient.module.inventory.ExperienceHudModule",
            "com.ahmed.banglaclient.module.inventory.HeldItemDisplayModule",
            "com.ahmed.banglaclient.module.inventory.ItemPickupNotificationsModule",
            "com.ahmed.banglaclient.module.inventory.LowDurabilityAlertModule",
            "com.ahmed.banglaclient.module.inventory.InventoryStatusModule",

            // Category E — Movement & Player Information (58–69)
            "com.ahmed.banglaclient.module.movement.ToggleSprintModule",
            "com.ahmed.banglaclient.module.movement.ToggleSneakModule",
            "com.ahmed.banglaclient.module.movement.AutoSprintModule",
            "com.ahmed.banglaclient.module.movement.SprintIndicatorModule",
            "com.ahmed.banglaclient.module.movement.BlockBreakingInfoModule",
            "com.ahmed.banglaclient.module.movement.BlockSpeedDisplayModule",
            "com.ahmed.banglaclient.module.movement.MovementStatusModule",
            "com.ahmed.banglaclient.module.movement.JumpCounterModule",
            "com.ahmed.banglaclient.module.movement.FallDistanceModule",
            "com.ahmed.banglaclient.module.movement.StepCounterModule",
            "com.ahmed.banglaclient.module.movement.SwimmingIndicatorModule",
            "com.ahmed.banglaclient.module.movement.FlightStatusModule",

            // Category F — World & Environment (70–81)
            "com.ahmed.banglaclient.module.world.TimeDisplayModule",
            "com.ahmed.banglaclient.module.world.MoonPhaseModule",
            "com.ahmed.banglaclient.module.world.LightLevelModule",
            "com.ahmed.banglaclient.module.world.LocalDifficultyModule",
            "com.ahmed.banglaclient.module.world.ChunkInfoModule",
            "com.ahmed.banglaclient.module.world.SlimeChunkInfoModule",
            "com.ahmed.banglaclient.module.world.SpawnInfoModule",
            "com.ahmed.banglaclient.module.world.EntityCounterModule",
            "com.ahmed.banglaclient.module.world.MobInfoModule",
            "com.ahmed.banglaclient.module.world.BlockInfoModule",
            "com.ahmed.banglaclient.module.world.CompassHudModule",
            "com.ahmed.banglaclient.module.world.WorldBorderInfoModule",

            // Category G — Chat & Communication (82–91)
            "com.ahmed.banglaclient.module.chat.ChatTimestampsModule",
            "com.ahmed.banglaclient.module.chat.ChatCustomizerModule",
            "com.ahmed.banglaclient.module.chat.ChatBackgroundModule",
            "com.ahmed.banglaclient.module.chat.ChatNotificationsModule",
            "com.ahmed.banglaclient.module.chat.ChatFilterModule",
            "com.ahmed.banglaclient.module.chat.ChatSearchModule",
            "com.ahmed.banglaclient.module.chat.ChatCopyModule",
            "com.ahmed.banglaclient.module.chat.AutoGgModule",
            "com.ahmed.banglaclient.module.chat.AutoRespawnModule",
            "com.ahmed.banglaclient.module.chat.QuickMessagesModule",

            // Category H — Audio & Sound (92–99)
            "com.ahmed.banglaclient.module.audio.SoundVolumeHudModule",
            "com.ahmed.banglaclient.module.audio.SoundCustomizerModule",
            "com.ahmed.banglaclient.module.audio.MusicDisplayModule",
            "com.ahmed.banglaclient.module.audio.MusicNotificationsModule",
            "com.ahmed.banglaclient.module.audio.SoundMuteToggleModule",
            "com.ahmed.banglaclient.module.audio.AmbientSoundControlModule",
            "com.ahmed.banglaclient.module.audio.MusicVolumeControlModule",
            "com.ahmed.banglaclient.module.audio.SoundCategoryHudModule",

            // Category I — Performance & Optimization (100–109)
            "com.ahmed.banglaclient.module.performance.FpsGraphModule",
            "com.ahmed.banglaclient.module.performance.FpsWarningModule",
            "com.ahmed.banglaclient.module.performance.PingGraphModule",
            "com.ahmed.banglaclient.module.performance.MemoryGraphModule",
            "com.ahmed.banglaclient.module.performance.EntityDistanceInfoModule",
            "com.ahmed.banglaclient.module.performance.ParticleLimiterModule",
            "com.ahmed.banglaclient.module.performance.AnimationOptimizerModule",
            "com.ahmed.banglaclient.module.performance.DynamicRenderDistanceModule",
            "com.ahmed.banglaclient.module.performance.PerformanceMonitorModule",
            "com.ahmed.banglaclient.module.performance.ResourcePackMonitorModule",

            // Category J — Utility & Convenience (110–120)
            "com.ahmed.banglaclient.module.utility.ScreenshotManagerModule",
            "com.ahmed.banglaclient.module.utility.CustomNotificationsModule",
            "com.ahmed.banglaclient.module.utility.QuickSettingsModule",
            "com.ahmed.banglaclient.module.utility.CustomScoreboardModule",
            "com.ahmed.banglaclient.module.utility.CustomBossBarModule",
            "com.ahmed.banglaclient.module.utility.WaypointManagerModule",
            "com.ahmed.banglaclient.module.utility.CoordinateClipboardModule",
            "com.ahmed.banglaclient.module.utility.RecipeQuickAccessModule",
            "com.ahmed.banglaclient.module.utility.ConfigManagerModule",
            "com.ahmed.banglaclient.module.utility.KeybindManagerModule",
            "com.ahmed.banglaclient.module.utility.ModuleSearchModule"
    };

    @Test
    public void testAll120ModuleClassesExistAndExtendModule() throws ClassNotFoundException {
        Assertions.assertEquals(10, ModuleCategory.values().length);
        Assertions.assertEquals(120, ALL_120_MODULE_CLASSES.length);

        for (String className : ALL_120_MODULE_CLASSES) {
            Class<?> clazz = Class.forName(className, false, getClass().getClassLoader());
            Assertions.assertTrue(Module.class.isAssignableFrom(clazz),
                    className + " must extend com.ahmed.banglaclient.module.Module");
        }
    }
}
