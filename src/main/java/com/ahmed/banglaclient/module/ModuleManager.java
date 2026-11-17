package com.ahmed.banglaclient.module;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.audio.SoundMuteToggleModule;
import com.ahmed.banglaclient.module.chat.AutoGgModule;
import com.ahmed.banglaclient.module.chat.AutoRespawnModule;
import com.ahmed.banglaclient.module.chat.ChatBackgroundModule;
import com.ahmed.banglaclient.module.chat.ChatCopyModule;
import com.ahmed.banglaclient.module.chat.ChatCustomizerModule;
import com.ahmed.banglaclient.module.chat.ChatFilterModule;
import com.ahmed.banglaclient.module.chat.ChatHeadsModule;
import com.ahmed.banglaclient.module.chat.ChatNotificationsModule;
import com.ahmed.banglaclient.module.chat.ChatSearchModule;
import com.ahmed.banglaclient.module.chat.ChatTimestampsModule;
import com.ahmed.banglaclient.module.chat.QuickMessagesModule;
import com.ahmed.banglaclient.module.combat.AnchorOptimizerModule;
import com.ahmed.banglaclient.module.combat.ArmorStatusModule;
import com.ahmed.banglaclient.module.combat.ArrowCounterModule;
import com.ahmed.banglaclient.module.combat.AttackCooldownModule;
import com.ahmed.banglaclient.module.combat.CombatTimerModule;
import com.ahmed.banglaclient.module.combat.ComboCounterModule;
import com.ahmed.banglaclient.module.combat.CrosshairCustomizerModule;
import com.ahmed.banglaclient.module.combat.CrystalOptimizerModule;
import com.ahmed.banglaclient.module.combat.HitIndicatorModule;
import com.ahmed.banglaclient.module.combat.ItemDurabilityModule;
import com.ahmed.banglaclient.module.combat.PearlOptimizerModule;
import com.ahmed.banglaclient.module.combat.PotionHudModule;
import com.ahmed.banglaclient.module.combat.ShieldOptimizerModule;
import com.ahmed.banglaclient.module.combat.ShieldStatusModule;
import com.ahmed.banglaclient.module.combat.TargetInfoModule;
import com.ahmed.banglaclient.module.combat.TotemCounterModule;
import com.ahmed.banglaclient.module.hud.BiomeDisplayModule;
import com.ahmed.banglaclient.module.hud.ClockModule;
import com.ahmed.banglaclient.module.hud.CoordinatesModule;
import com.ahmed.banglaclient.module.hud.CpsCounterModule;
import com.ahmed.banglaclient.module.hud.DayCounterModule;
import com.ahmed.banglaclient.module.hud.FpsCounterModule;
import com.ahmed.banglaclient.module.hud.KeystrokesModule;
import com.ahmed.banglaclient.module.hud.MemoryUsageModule;
import com.ahmed.banglaclient.module.hud.PingDisplayModule;
import com.ahmed.banglaclient.module.hud.ServerAddressModule;
import com.ahmed.banglaclient.module.hud.SessionTimerModule;
import com.ahmed.banglaclient.module.hud.SpeedometerModule;
import com.ahmed.banglaclient.module.hud.TabListHeadsModule;
import com.ahmed.banglaclient.module.hud.WorldInfoModule;
import com.ahmed.banglaclient.module.inventory.BlockCounterModule;
import com.ahmed.banglaclient.module.inventory.DurabilityViewerModule;
import com.ahmed.banglaclient.module.inventory.ExperienceHudModule;
import com.ahmed.banglaclient.module.inventory.FoodStatusModule;
import com.ahmed.banglaclient.module.inventory.HealthHudModule;
import com.ahmed.banglaclient.module.inventory.HotbarHudModule;
import com.ahmed.banglaclient.module.inventory.InventoryHudModule;
import com.ahmed.banglaclient.module.inventory.InventoryStatusModule;
import com.ahmed.banglaclient.module.inventory.ItemCounterModule;
import com.ahmed.banglaclient.module.inventory.ItemPickupNotificationsModule;
import com.ahmed.banglaclient.module.inventory.LowDurabilityAlertModule;
import com.ahmed.banglaclient.module.inventory.ShulkerPreviewModule;
import com.ahmed.banglaclient.module.movement.ToggleSneakModule;
import com.ahmed.banglaclient.module.movement.ToggleSprintModule;
import com.ahmed.banglaclient.module.performance.AnimationOptimizerModule;
import com.ahmed.banglaclient.module.performance.DynamicRenderDistanceModule;
import com.ahmed.banglaclient.module.performance.ParticleLimiterModule;
import com.ahmed.banglaclient.module.performance.PerformanceMonitorModule;
import com.ahmed.banglaclient.module.utility.BreakDelayModule;
import com.ahmed.banglaclient.module.utility.ConfigManagerModule;
import com.ahmed.banglaclient.module.utility.CoordinateClipboardModule;
import com.ahmed.banglaclient.module.utility.CustomBossBarModule;
import com.ahmed.banglaclient.module.utility.CustomNotificationsModule;
import com.ahmed.banglaclient.module.utility.CustomScoreboardModule;
import com.ahmed.banglaclient.module.utility.FastBlockPlacementModule;
import com.ahmed.banglaclient.module.utility.LatencyOptimizerModule;
import com.ahmed.banglaclient.module.utility.LeftClickDelayModule;
import com.ahmed.banglaclient.module.utility.MiddleClickPearlModule;
import com.ahmed.banglaclient.module.utility.RightClickDelayModule;
import com.ahmed.banglaclient.module.utility.ScreenshotManagerModule;
import com.ahmed.banglaclient.module.utility.WaypointManagerModule;
import com.ahmed.banglaclient.livestream.LiveStreamHudModule;
import com.ahmed.banglaclient.livestream.LiveStreamManager;
import com.ahmed.banglaclient.module.visual.ClearWaterModule;
import com.ahmed.banglaclient.module.visual.ColorSaturationModule;
import com.ahmed.banglaclient.module.visual.CustomFireOverlayModule;
import com.ahmed.banglaclient.module.visual.CustomFogModule;
import com.ahmed.banglaclient.module.visual.CustomGlintModule;
import com.ahmed.banglaclient.module.visual.CustomHurtCameraModule;
import com.ahmed.banglaclient.module.visual.CustomPanoramaModule;
import com.ahmed.banglaclient.module.visual.CustomParticlesModule;
import com.ahmed.banglaclient.module.visual.CustomPumpkinOverlayModule;
import com.ahmed.banglaclient.module.visual.CustomSkyModule;
import com.ahmed.banglaclient.module.visual.CustomVignetteModule;
import com.ahmed.banglaclient.module.visual.CustomWeatherModule;
import com.ahmed.banglaclient.module.visual.DamageTintModule;
import com.ahmed.banglaclient.module.visual.FullbrightModule;
import com.ahmed.banglaclient.module.visual.ItemPhysicsModule;
import com.ahmed.banglaclient.module.visual.NametagsModule;
import com.ahmed.banglaclient.module.visual.SmallHeldItemModule;
import com.ahmed.banglaclient.module.visual.ZoomModule;
import com.ahmed.banglaclient.module.world.BlockInfoModule;
import com.ahmed.banglaclient.module.world.ChunkInfoModule;
import com.ahmed.banglaclient.module.world.CompassHudModule;
import com.ahmed.banglaclient.module.world.LightLevelModule;
import com.ahmed.banglaclient.module.world.SlimeChunkInfoModule;
import com.ahmed.banglaclient.module.world.TimeDisplayModule;
import com.ahmed.banglaclient.module.combat.ReachDisplayModule;
import com.ahmed.banglaclient.module.combat.TntCrystalHudModule;
import com.ahmed.banglaclient.module.hud.ServerTpsNetworkHudModule;
import com.ahmed.banglaclient.module.inventory.MouseTweaksModule;
import com.ahmed.banglaclient.module.inventory.OffhandHudModule;
import com.ahmed.banglaclient.module.movement.FlySpeedControlModule;
import com.ahmed.banglaclient.module.performance.MasterOptimizerModule;
import com.ahmed.banglaclient.module.utility.MidClickActionsModule;
import com.ahmed.banglaclient.module.visual.ClearLavaModule;
import com.ahmed.banglaclient.module.visual.CustomFovModule;
import com.ahmed.banglaclient.module.visual.FreelookModule;
import com.ahmed.banglaclient.module.visual.SmallItemsSystemModule;
import com.ahmed.banglaclient.module.catalog.InputOptimizerModule;
import com.ahmed.banglaclient.module.catalog.RenderOptimizerModule;
import com.ahmed.banglaclient.module.catalog.EntityCullingModule;
import com.ahmed.banglaclient.module.catalog.CaveCullingModule;
import com.ahmed.banglaclient.module.catalog.BlockRenderingOptimizerModule;
import com.ahmed.banglaclient.module.catalog.ItemRenderingOptimizerModule;
import com.ahmed.banglaclient.module.catalog.MobRenderingOptimizerModule;
import com.ahmed.banglaclient.module.catalog.ChunkLoadingOptimizerModule;
import com.ahmed.banglaclient.module.catalog.SmartRenderDistanceModule;
import com.ahmed.banglaclient.module.catalog.ChunkUpdateBatchingModule;
import com.ahmed.banglaclient.module.catalog.ChunkMeshCacheModule;
import com.ahmed.banglaclient.module.catalog.BlockFaceCullingModule;
import com.ahmed.banglaclient.module.catalog.OcclusionCullingModule;
import com.ahmed.banglaclient.module.catalog.TransparentRenderOptimizerModule;
import com.ahmed.banglaclient.module.catalog.RenderLayerOptimizerModule;
import com.ahmed.banglaclient.module.catalog.SignRenderingOptimizerModule;
import com.ahmed.banglaclient.module.catalog.NametagRenderingOptimizerModule;
import com.ahmed.banglaclient.module.catalog.WeatherRenderingOptimizerModule;
import com.ahmed.banglaclient.module.catalog.CloudRenderingOptimizerModule;
import com.ahmed.banglaclient.module.catalog.SkyRenderingOptimizerModule;
import com.ahmed.banglaclient.module.catalog.WaterRenderingOptimizerModule;
import com.ahmed.banglaclient.module.catalog.LavaRenderingOptimizerModule;
import com.ahmed.banglaclient.module.catalog.FireRenderingOptimizerModule;
import com.ahmed.banglaclient.module.catalog.BeaconBeamOptimizerModule;
import com.ahmed.banglaclient.module.catalog.XpOptimizerModule;
import com.ahmed.banglaclient.module.catalog.TntOptimizerModule;
import com.ahmed.banglaclient.module.catalog.ItemEntityOptimizerModule;
import com.ahmed.banglaclient.module.catalog.FastHotbarOptimizerModule;
import com.ahmed.banglaclient.module.catalog.PotionOptimizerModule;
import com.ahmed.banglaclient.module.catalog.ElytraOptimizerModule;
import com.ahmed.banglaclient.module.catalog.MaceOptimizerModule;
import com.ahmed.banglaclient.module.catalog.TotemOptimizerModule;
import com.ahmed.banglaclient.module.catalog.CrossbowOptimizerModule;
import com.ahmed.banglaclient.module.catalog.BowOptimizerModule;
import com.ahmed.banglaclient.module.catalog.ProjectileOptimizerModule;
import com.ahmed.banglaclient.module.catalog.KnockbackOptimizerModule;
import com.ahmed.banglaclient.module.catalog.WindChargeOptimizerModule;
import com.ahmed.banglaclient.module.catalog.StringOptimizerModule;
import com.ahmed.banglaclient.module.catalog.SweetBerryOptimizerModule;
import com.ahmed.banglaclient.module.catalog.ConsumableOptimizerModule;
import com.ahmed.banglaclient.module.catalog.CartOptimizerModule;
import com.ahmed.banglaclient.module.catalog.NoShadowBlocksModule;
import com.ahmed.banglaclient.module.catalog.OnePercentLowFpsModule;
import com.ahmed.banglaclient.module.catalog.FrameTimeDisplayModule;
import com.ahmed.banglaclient.module.catalog.FrameTimeGraphModule;
import com.ahmed.banglaclient.module.catalog.GpuUsageHudModule;
import com.ahmed.banglaclient.module.catalog.CpuUsageHudModule;
import com.ahmed.banglaclient.module.catalog.EntityCounterHudModule;
import com.ahmed.banglaclient.module.catalog.ParticleCounterHudModule;
import com.ahmed.banglaclient.module.catalog.FpsBottleneckDetectorModule;
import com.ahmed.banglaclient.module.catalog.FramePacingOptimizerModule;
import com.ahmed.banglaclient.module.catalog.AdaptiveFpsLimiterModule;
import com.ahmed.banglaclient.module.catalog.UnfocusedFpsLimiterModule;
import com.ahmed.banglaclient.module.catalog.MenuFpsLimiterModule;
import com.ahmed.banglaclient.module.catalog.WorldFpsLimiterModule;
import com.ahmed.banglaclient.module.catalog.SmartVsyncModule;
import com.ahmed.banglaclient.module.catalog.DynamicSimulationDistanceModule;
import com.ahmed.banglaclient.module.catalog.LoadingScreenOptimizerModule;
import com.ahmed.banglaclient.module.catalog.StartupOptimizerModule;
import com.ahmed.banglaclient.module.catalog.FastWorldLoadingModule;
import com.ahmed.banglaclient.module.catalog.FastServerSwitchingModule;
import com.ahmed.banglaclient.module.catalog.FastQuitModule;
import com.ahmed.banglaclient.module.catalog.RamUsageOptimizerModule;
import com.ahmed.banglaclient.module.catalog.MemoryCleanupModule;
import com.ahmed.banglaclient.module.catalog.ManualMemoryCleanupKeybindModule;
import com.ahmed.banglaclient.module.catalog.SmartMemoryCleanupModule;
import com.ahmed.banglaclient.module.catalog.TextureMemoryOptimizerModule;
import com.ahmed.banglaclient.module.catalog.ModelCacheOptimizerModule;
import com.ahmed.banglaclient.module.catalog.ResourceCacheOptimizerModule;
import com.ahmed.banglaclient.module.catalog.CpuThreadOptimizerModule;
import com.ahmed.banglaclient.module.catalog.RenderThreadOptimizerModule;
import com.ahmed.banglaclient.module.catalog.BackgroundTaskOptimizerModule;
import com.ahmed.banglaclient.module.catalog.IdleTaskSuspensionModule;
import com.ahmed.banglaclient.module.catalog.AsyncResourceLoadingModule;
import com.ahmed.banglaclient.module.catalog.AsyncChunkPreparationModule;
import com.ahmed.banglaclient.module.catalog.CrashSafeCleanupModule;
import com.ahmed.banglaclient.module.catalog.MidClickElytraModule;
import com.ahmed.banglaclient.module.catalog.MidClickRocketModule;
import com.ahmed.banglaclient.module.catalog.MidClickXpModule;
import com.ahmed.banglaclient.module.catalog.FastXpBottleModule;
import com.ahmed.banglaclient.module.catalog.AutoSprintModule;
import com.ahmed.banglaclient.module.catalog.AccurateBlockPlacementModule;
import com.ahmed.banglaclient.module.catalog.LeftRightCpsModule;
import com.ahmed.banglaclient.module.catalog.CpsGraphModule;
import com.ahmed.banglaclient.module.catalog.WeaponCooldownHudModule;
import com.ahmed.banglaclient.module.catalog.AttackIndicatorCustomizerModule;
import com.ahmed.banglaclient.module.catalog.HitCounterModule;
import com.ahmed.banglaclient.module.catalog.KnockbackCameraControlModule;
import com.ahmed.banglaclient.module.catalog.ProjectileIndicatorModule;
import com.ahmed.banglaclient.module.catalog.TntFuseHudStandaloneModule;
import com.ahmed.banglaclient.module.catalog.CrystalStatusHudStandaloneModule;
import com.ahmed.banglaclient.module.catalog.AttackRangeVisualizerModule;
import com.ahmed.banglaclient.module.catalog.ProjectileTrajectoryPreviewModule;
import com.ahmed.banglaclient.module.catalog.HudScaleControlModule;
import com.ahmed.banglaclient.module.catalog.HudOpacityControlModule;
import com.ahmed.banglaclient.module.catalog.HudPresetsModule;
import com.ahmed.banglaclient.module.catalog.DirectionHudStandaloneModule;
import com.ahmed.banglaclient.module.catalog.DistanceDisplayModule;
import com.ahmed.banglaclient.module.catalog.PingGraphStandaloneModule;
import com.ahmed.banglaclient.module.catalog.NetworkStatusHudModule;
import com.ahmed.banglaclient.module.catalog.PacketRateDisplayModule;
import com.ahmed.banglaclient.module.catalog.EffectTimerModule;
import com.ahmed.banglaclient.module.catalog.MoonPhaseHudModule;
import com.ahmed.banglaclient.module.catalog.CameraSmoothingModule;
import com.ahmed.banglaclient.module.catalog.CameraDistanceModule;
import com.ahmed.banglaclient.module.catalog.CameraCollisionFixModule;
import com.ahmed.banglaclient.module.catalog.SprintFovControlModule;
import com.ahmed.banglaclient.module.catalog.SpeedFovControlModule;
import com.ahmed.banglaclient.module.catalog.ItemFovControlModule;
import com.ahmed.banglaclient.module.catalog.DamageFovControlModule;
import com.ahmed.banglaclient.module.catalog.NoViewBobbingModule;
import com.ahmed.banglaclient.module.catalog.NoCameraShakeModule;
import com.ahmed.banglaclient.module.catalog.DamageTiltControlModule;
import com.ahmed.banglaclient.module.catalog.MotionBlurControlModule;
import com.ahmed.banglaclient.module.catalog.PotionCameraEffectOffModule;
import com.ahmed.banglaclient.module.catalog.DeathCameraEffectOffModule;
import com.ahmed.banglaclient.module.catalog.ViewmodelEditorModule;
import com.ahmed.banglaclient.module.catalog.ThirdPerson3DF5ViewModule;
import com.ahmed.banglaclient.module.catalog.RenderScaleModule;
import com.ahmed.banglaclient.module.catalog.ResolutionControlModule;
import com.ahmed.banglaclient.module.catalog.FxaaAntiAliasingModule;
import com.ahmed.banglaclient.module.catalog.HdrToggleModule;
import com.ahmed.banglaclient.module.catalog.SharpeningFilterModule;
import com.ahmed.banglaclient.module.catalog.BrightnessControlModule;
import com.ahmed.banglaclient.module.catalog.NoFogModule;
import com.ahmed.banglaclient.module.catalog.SkyColorWheelModule;
import com.ahmed.banglaclient.module.catalog.DarkGuiModeModule;
import com.ahmed.banglaclient.module.catalog.WeatherIntensityModule;
import com.ahmed.banglaclient.module.catalog.CloudControlModule;
import com.ahmed.banglaclient.module.catalog.CloudDistanceModule;
import com.ahmed.banglaclient.module.catalog.WaterQualityControlModule;
import com.ahmed.banglaclient.module.catalog.LavaQualityControlModule;
import com.ahmed.banglaclient.module.catalog.FireRenderingControlModule;
import com.ahmed.banglaclient.module.catalog.ParticleVisibilityControlModule;
import com.ahmed.banglaclient.module.catalog.PotionParticleOffModule;
import com.ahmed.banglaclient.module.catalog.PotionFovParticleOffModule;
import com.ahmed.banglaclient.module.catalog.ContainerPreviewStandaloneModule;
import com.ahmed.banglaclient.module.catalog.ItemHighlighterModule;
import com.ahmed.banglaclient.module.catalog.RarityHighlightModule;
import com.ahmed.banglaclient.module.catalog.StackCounterModule;
import com.ahmed.banglaclient.module.catalog.DurabilityBarCustomizerModule;
import com.ahmed.banglaclient.module.catalog.ItemTooltipCustomizerModule;
import com.ahmed.banglaclient.module.catalog.CompactTooltipModule;
import com.ahmed.banglaclient.module.catalog.EnchantmentTooltipModule;
import com.ahmed.banglaclient.module.catalog.PotionTooltipModule;
import com.ahmed.banglaclient.module.catalog.SmallSwordModule;
import com.ahmed.banglaclient.module.catalog.SmallAxeModule;
import com.ahmed.banglaclient.module.catalog.SmallPickaxeModule;
import com.ahmed.banglaclient.module.catalog.SmallShovelModule;
import com.ahmed.banglaclient.module.catalog.SmallFoodModule;
import com.ahmed.banglaclient.module.catalog.SmallBlockModule;
import com.ahmed.banglaclient.module.catalog.SmallShieldModule;
import com.ahmed.banglaclient.module.catalog.SmallTotemModule;
import com.ahmed.banglaclient.module.catalog.TotemResizerModule;
import com.ahmed.banglaclient.module.catalog.ChatClearModule;
import com.ahmed.banglaclient.module.catalog.KeywordHighlightModule;
import com.ahmed.banglaclient.module.catalog.MentionAlertModule;
import com.ahmed.banglaclient.module.catalog.ChatScaleModule;
import com.ahmed.banglaclient.module.catalog.ChatWidthHeightModule;
import com.ahmed.banglaclient.module.catalog.NetworkOptimizerModule;
import com.ahmed.banglaclient.module.catalog.FastIpPingModule;
import com.ahmed.banglaclient.module.catalog.ConnectionStatusModule;
import com.ahmed.banglaclient.module.catalog.AutoReconnectModule;
import com.ahmed.banglaclient.module.catalog.ReconnectDelayModule;
import com.ahmed.banglaclient.module.catalog.ServerBrandDisplayModule;
import com.ahmed.banglaclient.module.catalog.VoiceChatSupportModule;
import com.ahmed.banglaclient.module.catalog.DeathLocationModule;
import com.ahmed.banglaclient.module.catalog.DeathWaypointModule;
import com.ahmed.banglaclient.module.catalog.ChunkBorderDisplayModule;
import com.ahmed.banglaclient.module.catalog.SpawnLocationModule;
import com.ahmed.banglaclient.module.catalog.StructureLocationHelperModule;
import com.ahmed.banglaclient.module.catalog.CustomTablistModule;
import com.ahmed.banglaclient.module.catalog.NametagDistanceControlModule;
import com.ahmed.banglaclient.module.catalog.BcNametagIconModule;
import com.ahmed.banglaclient.module.catalog.ProfileManagerModule;
import com.ahmed.banglaclient.module.catalog.ServerSpecificProfileModule;
import com.ahmed.banglaclient.module.catalog.WorldSpecificProfileModule;
import com.ahmed.banglaclient.module.catalog.PvpProfileModule;
import com.ahmed.banglaclient.module.catalog.SurvivalProfileModule;
import com.ahmed.banglaclient.module.catalog.LowEndProfileModule;
import com.ahmed.banglaclient.module.catalog.BalancedProfileModule;
import com.ahmed.banglaclient.module.catalog.QualityProfileModule;
import com.ahmed.banglaclient.module.catalog.ExtremeFpsProfileModule;
import com.ahmed.banglaclient.module.catalog.OneClickOptimizationModule;
import com.ahmed.banglaclient.module.catalog.HardwareDetectionModule;
import com.ahmed.banglaclient.module.catalog.RecommendedSettingsModule;
import com.ahmed.banglaclient.module.catalog.SafeModeModule;
import com.ahmed.banglaclient.module.catalog.CompatibilityModeModule;
import com.ahmed.banglaclient.module.catalog.CrashProtectionModule;
import com.ahmed.banglaclient.module.catalog.VanillaModeModule;
import com.ahmed.banglaclient.module.catalog.LoadingIndicatorModule;
import com.ahmed.banglaclient.module.catalog.PerformanceDashboardModule;
import com.ahmed.banglaclient.module.catalog.AutomaticPerformanceTuningModule;
import com.ahmed.banglaclient.module.catalog.TargetFpsModeModule;
import com.ahmed.banglaclient.module.catalog.BatterySaverModule;
import com.ahmed.banglaclient.module.catalog.ThermalModeModule;
import com.ahmed.banglaclient.module.catalog.BackgroundModeModule;
import com.ahmed.banglaclient.module.catalog.LowPowerModeModule;
import com.ahmed.banglaclient.module.catalog.AfkOptimizationModule;
import com.ahmed.banglaclient.module.catalog.SessionStatisticsModule;
import com.ahmed.banglaclient.module.catalog.PerformanceHistoryModule;
import com.ahmed.banglaclient.module.catalog.OptimizationLogModule;
import com.ahmed.banglaclient.module.catalog.ResetOptimizationModule;
import com.ahmed.banglaclient.module.catalog.MasterPerformanceSwitchModule;
import com.ahmed.banglaclient.module.catalog.GuiAnimationControlModule;
import com.ahmed.banglaclient.module.catalog.GuiRenderingOptimizerModule;
import com.ahmed.banglaclient.module.catalog.ResourceReloadOptimizerModule;
import com.ahmed.banglaclient.module.catalog.StartupCacheModule;
import com.ahmed.banglaclient.module.catalog.FastResourceReloadModule;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.*;
import java.util.stream.Collectors;

public final class ModuleManager {
    private final Map<String, Module> modulesById = new LinkedHashMap<>();
    private final Map<Class<? extends Module>, Module> modulesByClass = new LinkedHashMap<>();
    private final Map<ModuleCategory, List<Module>> modulesByCategory = new EnumMap<>(ModuleCategory.class);
    private final List<HudModule> hudModules = new ArrayList<>();

    public ModuleManager() {
        for (ModuleCategory category : ModuleCategory.values()) {
            modulesByCategory.put(category, new ArrayList<>());
        }
    }

    public void initialize() {
        registerBuiltInModules();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            for (Module module : modulesById.values()) {
                if (module.isEnabled()) {
                    try {
                        module.onTick();
                    } catch (Exception e) {
                        BanglaClient.LOGGER.error("Error ticking module '{}'", module.getName(), e);
                    }
                }
            }
        });
    }

    private void registerBuiltInModules() {
        // HUD (14 Essential Modules)
        register(new FpsCounterModule());
        register(new CpsCounterModule());
        register(new KeystrokesModule());
        register(new CoordinatesModule());
        register(new PingDisplayModule());
        register(new ClockModule());
        register(new SpeedometerModule());
        register(new BiomeDisplayModule());
        register(new DayCounterModule());
        register(new MemoryUsageModule());
        register(new ServerAddressModule());
        register(new WorldInfoModule());
        register(new SessionTimerModule());
        register(new TabListHeadsModule());
        register(new LiveStreamHudModule("ls_live_indicator", "LS: Live Indicator", 12f, 12f, 68f, 20f, false, () -> LiveStreamManager.get().snapshot().state() == LiveStreamManager.ConnectionState.LIVE ? "LIVE" : LiveStreamManager.get().snapshot().state().name()));
        register(new LiveStreamHudModule("ls_viewer_count", "LS: Viewer Count", 84f, 12f, 92f, 20f, false, () -> "Viewers: " + LiveStreamManager.formatCompactNumber(LiveStreamManager.get().snapshot().viewerCount())));
        register(new LiveStreamHudModule("ls_like_count", "LS: Like Count", 180f, 12f, 82f, 20f, false, () -> "Likes: " + LiveStreamManager.formatCompactNumber(LiveStreamManager.get().snapshot().likeCount())));
        register(new LiveStreamHudModule("ls_channel_name", "LS: Channel Name", 12f, 36f, 128f, 20f, false, () -> "Channel: " + LiveStreamManager.get().snapshot().channelName()));
        register(new LiveStreamHudModule("ls_channel_handle", "LS: Channel Handle", 144f, 36f, 102f, 20f, false, () -> "Handle: " + LiveStreamManager.get().snapshot().channelHandle()));
        register(new LiveStreamHudModule("ls_subscriber_count", "LS: Subscribers", 12f, 60f, 118f, 20f, false, () -> "Subs: " + LiveStreamManager.formatCompactNumber(LiveStreamManager.get().snapshot().subscriberCount())));
        register(new LiveStreamHudModule("ls_stream_duration", "LS: Stream Duration", 134f, 60f, 112f, 20f, false, () -> { var s = LiveStreamManager.get().snapshot(); if (s.streamStartEpochMs() <= 0L || s.state() != LiveStreamManager.ConnectionState.LIVE) return "Live: --:--"; long sec = Math.max(0L, (System.currentTimeMillis() - s.streamStartEpochMs()) / 1000L); return String.format("Live: %02d:%02d:%02d", sec / 3600, (sec % 3600) / 60, sec % 60); }));
        register(new LiveStreamHudModule("ls_stream_title", "LS: Stream Title", 12f, 84f, 220f, 20f, false, () -> "Title: " + LiveStreamManager.get().snapshot().streamTitle()));
        register(new LiveStreamHudModule("ls_connection_status", "LS: Stream Status", 12f, 108f, 190f, 20f, false, () -> "Status: " + LiveStreamManager.get().snapshot().statusMessage()));
        register(new LiveStreamHudModule("ls_live_chat", "LS: YouTube Live Chat", 12f, 136f, 260f, 140f, true, null));

        // PvP / Combat (16 Essential Modules)
        register(new ArmorStatusModule());
        register(new PotionHudModule());
        register(new TotemCounterModule());
        register(new ComboCounterModule());
        register(new HitIndicatorModule());
        register(new CrosshairCustomizerModule());
        register(new ItemDurabilityModule());
        register(new TargetInfoModule());
        register(new AttackCooldownModule());
        register(new ShieldStatusModule());
        register(new ArrowCounterModule());
        register(new CombatTimerModule());
        register(new CrystalOptimizerModule());
        register(new AnchorOptimizerModule());
        register(new ShieldOptimizerModule());
        register(new PearlOptimizerModule());

        // Visual (17 Working Modules)
        register(new FullbrightModule());
        register(new ColorSaturationModule());
        register(new ZoomModule());
        register(new CustomFogModule());
        register(new CustomSkyModule());
        register(new CustomWeatherModule());
        register(new DamageTintModule());
        register(new CustomParticlesModule());
        register(new CustomPanoramaModule());
        register(new ClearWaterModule());
        register(new CustomFireOverlayModule());
        register(new CustomPumpkinOverlayModule());
        register(new CustomVignetteModule());
        register(new CustomHurtCameraModule());
        register(new CustomGlintModule());
        register(new NametagsModule());
        register(new ItemPhysicsModule());
        register(new SmallHeldItemModule());
        register(new SmallItemsSystemModule());
        register(new ClearLavaModule());
        register(new FreelookModule());
        register(new CustomFovModule());
        register(new ReachDisplayModule());
        register(new TntCrystalHudModule());
        register(new ServerTpsNetworkHudModule());
        register(new OffhandHudModule());
        register(new MouseTweaksModule());
        register(new FlySpeedControlModule());
        register(new MidClickActionsModule());
        register(new MasterOptimizerModule());

        // Inventory (12 Essential Modules)
        register(new InventoryHudModule());
        register(new HotbarHudModule());
        register(new ItemCounterModule());
        register(new BlockCounterModule());
        register(new ShulkerPreviewModule());
        register(new DurabilityViewerModule());
        register(new FoodStatusModule());
        register(new HealthHudModule());
        register(new ExperienceHudModule());
        register(new ItemPickupNotificationsModule());
        register(new LowDurabilityAlertModule());
        register(new InventoryStatusModule());

        // Movement (2 Unified Essential Modules)
        register(new ToggleSprintModule());
        register(new ToggleSneakModule());

        // World (5 Essential Modules)
        register(new TimeDisplayModule());
        register(new LightLevelModule());
        register(new ChunkInfoModule());
        register(new SlimeChunkInfoModule());
        register(new BlockInfoModule());
        register(new CompassHudModule());

        // Chat (11 Essential Modules)
        register(new ChatTimestampsModule());
        register(new ChatCustomizerModule());
        register(new ChatBackgroundModule());
        register(new ChatNotificationsModule());
        register(new ChatFilterModule());
        register(new ChatSearchModule());
        register(new ChatCopyModule());
        register(new AutoGgModule());
        register(new AutoRespawnModule());
        register(new QuickMessagesModule());
        register(new ChatHeadsModule());

        // Audio (1 Essential Module)
        register(new SoundMuteToggleModule());

        // Performance (4 Essential Modules)
        register(new ParticleLimiterModule());
        register(new AnimationOptimizerModule());
        register(new DynamicRenderDistanceModule());
        register(new PerformanceMonitorModule());

        // Utility (13 Essential Modules)
        register(new ScreenshotManagerModule());
        register(new CustomNotificationsModule());
        register(new CustomScoreboardModule());
        register(new CustomBossBarModule());
        register(new WaypointManagerModule());
        register(new CoordinateClipboardModule());
        register(new ConfigManagerModule());
        register(new MiddleClickPearlModule());
        register(new FastBlockPlacementModule());
        register(new BreakDelayModule());
        register(new RightClickDelayModule());
        register(new LeftClickDelayModule());
        register(new LatencyOptimizerModule());
        register(new InputOptimizerModule());
        register(new RenderOptimizerModule());
        register(new EntityCullingModule());
        register(new CaveCullingModule());
        register(new BlockRenderingOptimizerModule());
        register(new ItemRenderingOptimizerModule());
        register(new MobRenderingOptimizerModule());
        register(new ChunkLoadingOptimizerModule());
        register(new SmartRenderDistanceModule());
        register(new ChunkUpdateBatchingModule());
        register(new ChunkMeshCacheModule());
        register(new BlockFaceCullingModule());
        register(new OcclusionCullingModule());
        register(new TransparentRenderOptimizerModule());
        register(new RenderLayerOptimizerModule());
        register(new SignRenderingOptimizerModule());
        register(new NametagRenderingOptimizerModule());
        register(new WeatherRenderingOptimizerModule());
        register(new CloudRenderingOptimizerModule());
        register(new SkyRenderingOptimizerModule());
        register(new WaterRenderingOptimizerModule());
        register(new LavaRenderingOptimizerModule());
        register(new FireRenderingOptimizerModule());
        register(new BeaconBeamOptimizerModule());
        register(new XpOptimizerModule());
        register(new TntOptimizerModule());
        register(new ItemEntityOptimizerModule());
        register(new FastHotbarOptimizerModule());
        register(new PotionOptimizerModule());
        register(new ElytraOptimizerModule());
        register(new MaceOptimizerModule());
        register(new TotemOptimizerModule());
        register(new CrossbowOptimizerModule());
        register(new BowOptimizerModule());
        register(new ProjectileOptimizerModule());
        register(new KnockbackOptimizerModule());
        register(new WindChargeOptimizerModule());
        register(new StringOptimizerModule());
        register(new SweetBerryOptimizerModule());
        register(new ConsumableOptimizerModule());
        register(new CartOptimizerModule());
        register(new NoShadowBlocksModule());
        register(new OnePercentLowFpsModule());
        register(new FrameTimeDisplayModule());
        register(new FrameTimeGraphModule());
        register(new GpuUsageHudModule());
        register(new CpuUsageHudModule());
        register(new EntityCounterHudModule());
        register(new ParticleCounterHudModule());
        register(new FpsBottleneckDetectorModule());
        register(new FramePacingOptimizerModule());
        register(new AdaptiveFpsLimiterModule());
        register(new UnfocusedFpsLimiterModule());
        register(new MenuFpsLimiterModule());
        register(new WorldFpsLimiterModule());
        register(new SmartVsyncModule());
        register(new DynamicSimulationDistanceModule());
        register(new LoadingScreenOptimizerModule());
        register(new StartupOptimizerModule());
        register(new FastWorldLoadingModule());
        register(new FastServerSwitchingModule());
        register(new FastQuitModule());
        register(new RamUsageOptimizerModule());
        register(new MemoryCleanupModule());
        register(new ManualMemoryCleanupKeybindModule());
        register(new SmartMemoryCleanupModule());
        register(new TextureMemoryOptimizerModule());
        register(new ModelCacheOptimizerModule());
        register(new ResourceCacheOptimizerModule());
        register(new CpuThreadOptimizerModule());
        register(new RenderThreadOptimizerModule());
        register(new BackgroundTaskOptimizerModule());
        register(new IdleTaskSuspensionModule());
        register(new AsyncResourceLoadingModule());
        register(new AsyncChunkPreparationModule());
        register(new CrashSafeCleanupModule());
        register(new MidClickElytraModule());
        register(new MidClickRocketModule());
        register(new MidClickXpModule());
        register(new FastXpBottleModule());
        register(new AutoSprintModule());
        register(new AccurateBlockPlacementModule());
        register(new LeftRightCpsModule());
        register(new CpsGraphModule());
        register(new WeaponCooldownHudModule());
        register(new AttackIndicatorCustomizerModule());
        register(new HitCounterModule());
        register(new KnockbackCameraControlModule());
        register(new ProjectileIndicatorModule());
        register(new TntFuseHudStandaloneModule());
        register(new CrystalStatusHudStandaloneModule());
        register(new AttackRangeVisualizerModule());
        register(new ProjectileTrajectoryPreviewModule());
        register(new HudScaleControlModule());
        register(new HudOpacityControlModule());
        register(new HudPresetsModule());
        register(new DirectionHudStandaloneModule());
        register(new DistanceDisplayModule());
        register(new PingGraphStandaloneModule());
        register(new NetworkStatusHudModule());
        register(new PacketRateDisplayModule());
        register(new EffectTimerModule());
        register(new MoonPhaseHudModule());
        register(new CameraSmoothingModule());
        register(new CameraDistanceModule());
        register(new CameraCollisionFixModule());
        register(new SprintFovControlModule());
        register(new SpeedFovControlModule());
        register(new ItemFovControlModule());
        register(new DamageFovControlModule());
        register(new NoViewBobbingModule());
        register(new NoCameraShakeModule());
        register(new DamageTiltControlModule());
        register(new MotionBlurControlModule());
        register(new PotionCameraEffectOffModule());
        register(new DeathCameraEffectOffModule());
        register(new ViewmodelEditorModule());
        register(new ThirdPerson3DF5ViewModule());
        register(new RenderScaleModule());
        register(new ResolutionControlModule());
        register(new FxaaAntiAliasingModule());
        register(new HdrToggleModule());
        register(new SharpeningFilterModule());
        register(new BrightnessControlModule());
        register(new NoFogModule());
        register(new SkyColorWheelModule());
        register(new DarkGuiModeModule());
        register(new WeatherIntensityModule());
        register(new CloudControlModule());
        register(new CloudDistanceModule());
        register(new WaterQualityControlModule());
        register(new LavaQualityControlModule());
        register(new FireRenderingControlModule());
        register(new ParticleVisibilityControlModule());
        register(new PotionParticleOffModule());
        register(new PotionFovParticleOffModule());
        register(new ContainerPreviewStandaloneModule());
        register(new ItemHighlighterModule());
        register(new RarityHighlightModule());
        register(new StackCounterModule());
        register(new DurabilityBarCustomizerModule());
        register(new ItemTooltipCustomizerModule());
        register(new CompactTooltipModule());
        register(new EnchantmentTooltipModule());
        register(new PotionTooltipModule());
        register(new SmallSwordModule());
        register(new SmallAxeModule());
        register(new SmallPickaxeModule());
        register(new SmallShovelModule());
        register(new SmallFoodModule());
        register(new SmallBlockModule());
        register(new SmallShieldModule());
        register(new SmallTotemModule());
        register(new TotemResizerModule());
        register(new ChatClearModule());
        register(new KeywordHighlightModule());
        register(new MentionAlertModule());
        register(new ChatScaleModule());
        register(new ChatWidthHeightModule());
        register(new NetworkOptimizerModule());
        register(new FastIpPingModule());
        register(new ConnectionStatusModule());
        register(new AutoReconnectModule());
        register(new ReconnectDelayModule());
        register(new ServerBrandDisplayModule());
        register(new VoiceChatSupportModule());
        register(new DeathLocationModule());
        register(new DeathWaypointModule());
        register(new ChunkBorderDisplayModule());
        register(new SpawnLocationModule());
        register(new StructureLocationHelperModule());
        register(new CustomTablistModule());
        register(new NametagDistanceControlModule());
        register(new BcNametagIconModule());
        register(new ProfileManagerModule());
        register(new ServerSpecificProfileModule());
        register(new WorldSpecificProfileModule());
        register(new PvpProfileModule());
        register(new SurvivalProfileModule());
        register(new LowEndProfileModule());
        register(new BalancedProfileModule());
        register(new QualityProfileModule());
        register(new ExtremeFpsProfileModule());
        register(new OneClickOptimizationModule());
        register(new HardwareDetectionModule());
        register(new RecommendedSettingsModule());
        register(new SafeModeModule());
        register(new CompatibilityModeModule());
        register(new CrashProtectionModule());
        register(new VanillaModeModule());
        register(new LoadingIndicatorModule());
        register(new PerformanceDashboardModule());
        register(new AutomaticPerformanceTuningModule());
        register(new TargetFpsModeModule());
        register(new BatterySaverModule());
        register(new ThermalModeModule());
        register(new BackgroundModeModule());
        register(new LowPowerModeModule());
        register(new AfkOptimizationModule());
        register(new SessionStatisticsModule());
        register(new PerformanceHistoryModule());
        register(new OptimizationLogModule());
        register(new ResetOptimizationModule());
        register(new MasterPerformanceSwitchModule());
        register(new GuiAnimationControlModule());
        register(new GuiRenderingOptimizerModule());
        register(new ResourceReloadOptimizerModule());
        register(new StartupCacheModule());
        register(new FastResourceReloadModule());

    }

    public synchronized <M extends Module> M register(M module) {
        if (module == null) throw new IllegalArgumentException("Cannot register a null module");
        String key = module.getId().toLowerCase(Locale.ROOT);
        if (modulesById.containsKey(key)) {
            return module;
        }
        modulesById.put(key, module);
        modulesByClass.put(module.getClass(), module);
        modulesByCategory.get(module.getCategory()).add(module);
        if (module instanceof HudModule hudModule) {
            hudModules.add(hudModule);
        }
        return module;
    }

    public List<Module> getAllModules() { return Collections.unmodifiableList(new ArrayList<>(modulesById.values())); }
    public List<HudModule> getHudModules() { return Collections.unmodifiableList(hudModules); }
    public List<Module> getModulesByCategory(ModuleCategory category) {
        if (category == null) return Collections.emptyList();
        return Collections.unmodifiableList(modulesByCategory.getOrDefault(category, Collections.emptyList()));
    }
    public Optional<Module> getModuleById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(modulesById.get(id.toLowerCase(Locale.ROOT)));
    }
    @SuppressWarnings("unchecked")
    public <M extends Module> Optional<M> getModule(Class<M> moduleClass) {
        if (moduleClass == null) return Optional.empty();
        return Optional.ofNullable((M) modulesByClass.get(moduleClass));
    }
    public List<Module> getEnabledModules() { return modulesById.values().stream().filter(Module::isEnabled).collect(Collectors.toList()); }
    public List<Module> getFavoriteModules() { return modulesById.values().stream().filter(Module::isFavorite).collect(Collectors.toList()); }
    public List<Module> getRecentlyUsedModules(int limit) {
        return modulesById.values().stream()
                .filter(m -> m.getLastUsedTimestamp() > 0L)
                .sorted(Comparator.comparingLong(Module::getLastUsedTimestamp).reversed())
                .limit(Math.max(1, limit))
                .collect(Collectors.toList());
    }
    public List<Module> searchModules(String query, ModuleCategory optionalCategoryFilter, boolean onlyEnabled, boolean onlyFavorites) {
        return modulesById.values().stream()
                .filter(m -> !(m instanceof LiveStreamHudModule))
                .filter(m -> optionalCategoryFilter == null || m.getCategory() == optionalCategoryFilter)
                .filter(m -> !onlyEnabled || m.isEnabled())
                .filter(m -> !onlyFavorites || m.isFavorite())
                .filter(m -> m.matchesSearch(query))
                .sorted(Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }
    public int getTotalModuleCount() { return modulesById.size(); }
    public int getEnabledModuleCount() {
        int count = 0;
        for (Module m : modulesById.values()) {
            if (m.isEnabled()) count++;
        }
        return count;
    }
}
