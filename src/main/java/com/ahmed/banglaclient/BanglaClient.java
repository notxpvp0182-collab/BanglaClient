package com.ahmed.banglaclient;

import com.ahmed.banglaclient.config.ConfigManager;
import com.ahmed.banglaclient.config.ProfileManager;
import com.ahmed.banglaclient.core.PerformancePreset;
import com.ahmed.banglaclient.core.SessionTracker;
import com.ahmed.banglaclient.hud.HudManager;
import com.ahmed.banglaclient.input.KeybindManager;
import com.ahmed.banglaclient.module.ModuleManager;
import com.ahmed.banglaclient.notification.NotificationManager;
import com.ahmed.banglaclient.rendering.BlurRenderer;
import com.ahmed.banglaclient.theme.ThemeManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Primary Fabric client entrypoint for BANGLA CLIENT.
 * Developed by Ahmed.
 */
public final class BanglaClient implements ClientModInitializer {
    public static final String MOD_ID = "banglaclient";
    public static final String CLIENT_NAME = "BANGLA CLIENT";
    public static final String CLIENT_VERSION = "1.0.0";
    public static final String DEVELOPER = "Ahmed";

    public static final Logger LOGGER = LoggerFactory.getLogger(CLIENT_NAME);

    private static BanglaClient instance;

    private Path configDirectory;
    private SessionTracker sessionTracker;
    private ThemeManager themeManager;
    private NotificationManager notificationManager;
    private ModuleManager moduleManager;
    private KeybindManager keybindManager;
    private HudManager hudManager;
    private ConfigManager configManager;
    private ProfileManager profileManager;
    private PerformancePreset performancePreset = PerformancePreset.BALANCED;

    @Override
    public void onInitializeClient() {
        instance = this;
        long initStart = System.currentTimeMillis();

        LOGGER.info("Initializing {} v{} (Developed by {})...", CLIENT_NAME, CLIENT_VERSION, DEVELOPER);

        this.configDirectory = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
        ensureConfigDirectories();

        this.sessionTracker = new SessionTracker();
        this.themeManager = new ThemeManager();
        this.notificationManager = new NotificationManager();
        this.moduleManager = new ModuleManager();
        this.moduleManager.initialize();

        this.keybindManager = new KeybindManager(this.moduleManager);
        this.keybindManager.initialize();

        this.hudManager = new HudManager(this.moduleManager);

        this.configManager = new ConfigManager(this.configDirectory, this.moduleManager);
        this.profileManager = new ProfileManager(this.configDirectory.resolve("profiles"), this.moduleManager, this.configManager);

        this.configManager.loadAll();
        this.themeManager.setActiveThemeById(this.configManager.getActiveThemeId());

        registerLifecycleHooks();

        long elapsed = System.currentTimeMillis() - initStart;
        LOGGER.info("{} initialized in {} ms [Preset: {}, Theme: {}, Modules Registered: {}].",
                CLIENT_NAME,
                elapsed,
                performancePreset.getDisplayName(),
                themeManager.getActiveTheme().getDisplayName(),
                moduleManager.getTotalModuleCount());
    }

    private void ensureConfigDirectories() {
        try {
            Files.createDirectories(this.configDirectory);
            Files.createDirectories(this.configDirectory.resolve("profiles"));
        } catch (IOException e) {
            LOGGER.error("Failed to create BANGLA CLIENT configuration directory at {}", this.configDirectory, e);
        }
    }

    private void registerLifecycleHooks() {
        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            BlurRenderer.beginFrame();
            if (this.hudManager != null) {
                this.hudManager.renderInGameHud(drawContext, 1.0f);
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (this.sessionTracker != null) {
                this.sessionTracker.onWorldJoin();
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            if (this.configManager != null) {
                LOGGER.info("Saving {} configurations before exit...", CLIENT_NAME);
                this.configManager.saveAll();
            }
        });
    }

    public static BanglaClient getInstance() {
        return instance;
    }

    public Path getConfigDirectory() {
        return configDirectory;
    }

    public SessionTracker getSessionTracker() {
        return sessionTracker;
    }

    public ThemeManager getThemeManager() {
        return themeManager;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public KeybindManager getKeybindManager() {
        return keybindManager;
    }

    public HudManager getHudManager() {
        return hudManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public PerformancePreset getPerformancePreset() {
        return performancePreset;
    }

    public void setPerformancePreset(PerformancePreset performancePreset) {
        if (performancePreset != null) {
            this.performancePreset = performancePreset;
        }
    }
}
