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
