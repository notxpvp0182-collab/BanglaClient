package com.ahmed.banglaclient.module;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.module.audio.AmbientSoundControlModule;
import com.ahmed.banglaclient.module.audio.MusicDisplayModule;
import com.ahmed.banglaclient.module.audio.MusicNotificationsModule;
import com.ahmed.banglaclient.module.audio.MusicVolumeControlModule;
import com.ahmed.banglaclient.module.audio.SoundCategoryHudModule;
import com.ahmed.banglaclient.module.audio.SoundCustomizerModule;
import com.ahmed.banglaclient.module.audio.SoundMuteToggleModule;
import com.ahmed.banglaclient.module.audio.SoundVolumeHudModule;
import com.ahmed.banglaclient.module.chat.AutoGgModule;
import com.ahmed.banglaclient.module.chat.AutoRespawnModule;
import com.ahmed.banglaclient.module.chat.ChatBackgroundModule;
import com.ahmed.banglaclient.module.chat.ChatCopyModule;
import com.ahmed.banglaclient.module.chat.ChatCustomizerModule;
import com.ahmed.banglaclient.module.chat.ChatFilterModule;
import com.ahmed.banglaclient.module.chat.ChatNotificationsModule;
import com.ahmed.banglaclient.module.chat.ChatSearchModule;
import com.ahmed.banglaclient.module.chat.ChatTimestampsModule;
import com.ahmed.banglaclient.module.chat.QuickMessagesModule;
import com.ahmed.banglaclient.module.combat.ArmorStatusModule;
import com.ahmed.banglaclient.module.combat.ArrowCounterModule;
import com.ahmed.banglaclient.module.combat.AttackCooldownModule;
import com.ahmed.banglaclient.module.combat.CombatTimerModule;
import com.ahmed.banglaclient.module.combat.ComboCounterModule;
import com.ahmed.banglaclient.module.combat.CrosshairCustomizerModule;
import com.ahmed.banglaclient.module.combat.HitIndicatorModule;
import com.ahmed.banglaclient.module.combat.ItemDurabilityModule;
import com.ahmed.banglaclient.module.combat.PotionHudModule;
import com.ahmed.banglaclient.module.combat.ShieldStatusModule;
import com.ahmed.banglaclient.module.combat.TargetInfoModule;
import com.ahmed.banglaclient.module.combat.TotemCounterModule;
import com.ahmed.banglaclient.module.hud.BiomeDisplayModule;
import com.ahmed.banglaclient.module.hud.ClockModule;
import com.ahmed.banglaclient.module.hud.CoordinatesModule;
import com.ahmed.banglaclient.module.hud.CpsCounterModule;
import com.ahmed.banglaclient.module.hud.DayCounterModule;
import com.ahmed.banglaclient.module.hud.DirectionHudModule;
import com.ahmed.banglaclient.module.hud.FpsCounterModule;
import com.ahmed.banglaclient.module.hud.KeystrokesModule;
import com.ahmed.banglaclient.module.hud.MemoryUsageModule;
import com.ahmed.banglaclient.module.hud.PingDisplayModule;
import com.ahmed.banglaclient.module.hud.ServerAddressModule;
import com.ahmed.banglaclient.module.hud.SessionTimerModule;
import com.ahmed.banglaclient.module.hud.SpeedometerModule;
import com.ahmed.banglaclient.module.hud.WeatherHudModule;
import com.ahmed.banglaclient.module.hud.WorldInfoModule;
import com.ahmed.banglaclient.module.inventory.BlockCounterModule;
import com.ahmed.banglaclient.module.inventory.ContainerPreviewModule;
import com.ahmed.banglaclient.module.inventory.DurabilityViewerModule;
import com.ahmed.banglaclient.module.inventory.EquipmentHudModule;
import com.ahmed.banglaclient.module.inventory.ExperienceHudModule;
import com.ahmed.banglaclient.module.inventory.FoodStatusModule;
import com.ahmed.banglaclient.module.inventory.HealthHudModule;
import com.ahmed.banglaclient.module.inventory.HeldItemDisplayModule;
import com.ahmed.banglaclient.module.inventory.HotbarHudModule;
import com.ahmed.banglaclient.module.inventory.InventoryHudModule;
import com.ahmed.banglaclient.module.inventory.InventoryStatusModule;
import com.ahmed.banglaclient.module.inventory.ItemCounterModule;
import com.ahmed.banglaclient.module.inventory.ItemPickupNotificationsModule;
import com.ahmed.banglaclient.module.inventory.LowDurabilityAlertModule;
import com.ahmed.banglaclient.module.inventory.ShulkerPreviewModule;
import com.ahmed.banglaclient.module.movement.AutoSprintModule;
import com.ahmed.banglaclient.module.movement.BlockBreakingInfoModule;
import com.ahmed.banglaclient.module.movement.BlockSpeedDisplayModule;
import com.ahmed.banglaclient.module.movement.FallDistanceModule;
import com.ahmed.banglaclient.module.movement.FlightStatusModule;
import com.ahmed.banglaclient.module.movement.JumpCounterModule;
import com.ahmed.banglaclient.module.movement.MovementStatusModule;
import com.ahmed.banglaclient.module.movement.SprintIndicatorModule;
import com.ahmed.banglaclient.module.movement.StepCounterModule;
import com.ahmed.banglaclient.module.movement.SwimmingIndicatorModule;
import com.ahmed.banglaclient.module.movement.ToggleSneakModule;
import com.ahmed.banglaclient.module.movement.ToggleSprintModule;
import com.ahmed.banglaclient.module.performance.AnimationOptimizerModule;
import com.ahmed.banglaclient.module.performance.DynamicRenderDistanceModule;
import com.ahmed.banglaclient.module.performance.EntityDistanceInfoModule;
import com.ahmed.banglaclient.module.performance.FpsGraphModule;
import com.ahmed.banglaclient.module.performance.FpsWarningModule;
import com.ahmed.banglaclient.module.performance.MemoryGraphModule;
import com.ahmed.banglaclient.module.performance.ParticleLimiterModule;
import com.ahmed.banglaclient.module.performance.PerformanceMonitorModule;
import com.ahmed.banglaclient.module.performance.PingGraphModule;
import com.ahmed.banglaclient.module.performance.ResourcePackMonitorModule;
import com.ahmed.banglaclient.module.utility.ConfigManagerModule;
import com.ahmed.banglaclient.module.utility.CoordinateClipboardModule;
import com.ahmed.banglaclient.module.utility.CustomBossBarModule;
import com.ahmed.banglaclient.module.utility.CustomNotificationsModule;
import com.ahmed.banglaclient.module.utility.CustomScoreboardModule;
import com.ahmed.banglaclient.module.utility.KeybindManagerModule;
import com.ahmed.banglaclient.module.utility.ModuleSearchModule;
import com.ahmed.banglaclient.module.utility.QuickSettingsModule;
import com.ahmed.banglaclient.module.utility.RecipeQuickAccessModule;
import com.ahmed.banglaclient.module.utility.ScreenshotManagerModule;
import com.ahmed.banglaclient.module.utility.WaypointManagerModule;
import com.ahmed.banglaclient.module.visual.ClearWaterModule;
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
import com.ahmed.banglaclient.module.visual.DynamicCrosshairModule;
import com.ahmed.banglaclient.module.visual.FullbrightModule;
import com.ahmed.banglaclient.module.visual.ZoomModule;
import com.ahmed.banglaclient.module.world.BlockInfoModule;
import com.ahmed.banglaclient.module.world.ChunkInfoModule;
import com.ahmed.banglaclient.module.world.CompassHudModule;
import com.ahmed.banglaclient.module.world.EntityCounterModule;
import com.ahmed.banglaclient.module.world.LightLevelModule;
import com.ahmed.banglaclient.module.world.LocalDifficultyModule;
import com.ahmed.banglaclient.module.world.MobInfoModule;
import com.ahmed.banglaclient.module.world.MoonPhaseModule;
import com.ahmed.banglaclient.module.world.SlimeChunkInfoModule;
import com.ahmed.banglaclient.module.world.SpawnInfoModule;
import com.ahmed.banglaclient.module.world.TimeDisplayModule;
import com.ahmed.banglaclient.module.world.WorldBorderInfoModule;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Central registry and lifecycle controller for all 120 BANGLA CLIENT modules.
 */
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
            if (client.player == null || client.world == null) {
                return;
            }
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
        // Category A — HUD & Information (01–15)
        register(new FpsCounterModule());
        register(new CpsCounterModule());
        register(new KeystrokesModule());
        register(new CoordinatesModule());
        register(new DirectionHudModule());
        register(new PingDisplayModule());
        register(new ClockModule());
        register(new SpeedometerModule());
        register(new BiomeDisplayModule());
        register(new WeatherHudModule());
        register(new DayCounterModule());
        register(new MemoryUsageModule());
        register(new ServerAddressModule());
        register(new WorldInfoModule());
        register(new SessionTimerModule());

        // Category B — PvP & Combat Information (16–27)
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

        // Category C — Visual Enhancement (28–42)
        register(new FullbrightModule());
        register(new ZoomModule());
        register(new CustomFogModule());
        register(new CustomSkyModule());
        register(new CustomWeatherModule());
        register(new DamageTintModule());
        register(new CustomParticlesModule());
        register(new DynamicCrosshairModule());
        register(new CustomPanoramaModule());
        register(new ClearWaterModule());
        register(new CustomFireOverlayModule());
        register(new CustomPumpkinOverlayModule());
        register(new CustomVignetteModule());
        register(new CustomHurtCameraModule());
        register(new CustomGlintModule());

        // Category D — Inventory & Item Information (43–57)
        register(new InventoryHudModule());
        register(new HotbarHudModule());
        register(new ItemCounterModule());
        register(new BlockCounterModule());
        register(new ContainerPreviewModule());
        register(new ShulkerPreviewModule());
        register(new EquipmentHudModule());
        register(new DurabilityViewerModule());
        register(new FoodStatusModule());
        register(new HealthHudModule());
        register(new ExperienceHudModule());
        register(new HeldItemDisplayModule());
        register(new ItemPickupNotificationsModule());
        register(new LowDurabilityAlertModule());
        register(new InventoryStatusModule());

        // Category E — Movement & Player Information (58–69)
        register(new ToggleSprintModule());
        register(new ToggleSneakModule());
        register(new AutoSprintModule());
        register(new SprintIndicatorModule());
        register(new BlockBreakingInfoModule());
        register(new BlockSpeedDisplayModule());
        register(new MovementStatusModule());
        register(new JumpCounterModule());
        register(new FallDistanceModule());
        register(new StepCounterModule());
        register(new SwimmingIndicatorModule());
        register(new FlightStatusModule());

        // Category F — World & Environment (70–81)
        register(new TimeDisplayModule());
        register(new MoonPhaseModule());
        register(new LightLevelModule());
        register(new LocalDifficultyModule());
        register(new ChunkInfoModule());
        register(new SlimeChunkInfoModule());
        register(new SpawnInfoModule());
        register(new EntityCounterModule());
        register(new MobInfoModule());
        register(new BlockInfoModule());
        register(new CompassHudModule());
        register(new WorldBorderInfoModule());

        // Category G — Chat & Communication (82–91)
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

        // Category H — Audio & Sound (92–99)
        register(new SoundVolumeHudModule());
        register(new SoundCustomizerModule());
        register(new MusicDisplayModule());
        register(new MusicNotificationsModule());
        register(new SoundMuteToggleModule());
        register(new AmbientSoundControlModule());
        register(new MusicVolumeControlModule());
        register(new SoundCategoryHudModule());

        // Category I — Performance & Optimization (100–109)
        register(new FpsGraphModule());
        register(new FpsWarningModule());
        register(new PingGraphModule());
        register(new MemoryGraphModule());
        register(new EntityDistanceInfoModule());
        register(new ParticleLimiterModule());
        register(new AnimationOptimizerModule());
        register(new DynamicRenderDistanceModule());
        register(new PerformanceMonitorModule());
        register(new ResourcePackMonitorModule());

        // Category J — Utility & Convenience (110–120)
        register(new ScreenshotManagerModule());
        register(new CustomNotificationsModule());
        register(new QuickSettingsModule());
        register(new CustomScoreboardModule());
        register(new CustomBossBarModule());
        register(new WaypointManagerModule());
        register(new CoordinateClipboardModule());
        register(new RecipeQuickAccessModule());
        register(new ConfigManagerModule());
        register(new KeybindManagerModule());
        register(new ModuleSearchModule());
    }

    /**
     * Registers a single module instance, preventing duplicate IDs or classes.
     */
    public synchronized <M extends Module> M register(M module) {
        if (module == null) {
            throw new IllegalArgumentException("Cannot register a null module");
        }
        String key = module.getId().toLowerCase(Locale.ROOT);
        if (modulesById.containsKey(key) || modulesByClass.containsKey(module.getClass())) {
            BanglaClient.LOGGER.warn("Skipping duplicate module registration for '{}'", module.getId());
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

    public List<Module> getAllModules() {
        return Collections.unmodifiableList(new ArrayList<>(modulesById.values()));
    }

    public List<HudModule> getHudModules() {
        return Collections.unmodifiableList(hudModules);
    }

    public List<Module> getModulesByCategory(ModuleCategory category) {
        if (category == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(modulesByCategory.getOrDefault(category, Collections.emptyList()));
    }

    public Optional<Module> getModuleById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(modulesById.get(id.toLowerCase(Locale.ROOT)));
    }

    @SuppressWarnings("unchecked")
    public <M extends Module> Optional<M> getModule(Class<M> moduleClass) {
        if (moduleClass == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((M) modulesByClass.get(moduleClass));
    }

    public List<Module> getEnabledModules() {
        return modulesById.values().stream()
                .filter(Module::isEnabled)
                .collect(Collectors.toList());
    }

    public List<Module> getFavoriteModules() {
        return modulesById.values().stream()
                .filter(Module::isFavorite)
                .collect(Collectors.toList());
    }

    public List<Module> getRecentlyUsedModules(int limit) {
        return modulesById.values().stream()
                .filter(m -> m.getLastUsedTimestamp() > 0L)
                .sorted(Comparator.comparingLong(Module::getLastUsedTimestamp).reversed())
                .limit(Math.max(1, limit))
                .collect(Collectors.toList());
    }

    public List<Module> searchModules(String query, ModuleCategory optionalCategoryFilter, boolean onlyEnabled, boolean onlyFavorites) {
        return modulesById.values().stream()
                .filter(m -> optionalCategoryFilter == null || m.getCategory() == optionalCategoryFilter)
                .filter(m -> !onlyEnabled || m.isEnabled())
                .filter(m -> !onlyFavorites || m.isFavorite())
                .filter(m -> m.matchesSearch(query))
                .sorted(Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public int getTotalModuleCount() {
        return modulesById.size();
    }

    public int getEnabledModuleCount() {
        int count = 0;
        for (Module m : modulesById.values()) {
            if (m.isEnabled()) {
                count++;
            }
        }
        return count;
    }
}
