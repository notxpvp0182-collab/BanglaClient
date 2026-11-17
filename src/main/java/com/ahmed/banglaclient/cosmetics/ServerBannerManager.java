package com.ahmed.banglaclient.cosmetics;

import com.ahmed.banglaclient.config.ConfigManager;
import com.ahmed.banglaclient.config.ConfigValidator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;

/**
 * Manages Custom Per-Server Banner Backgrounds in banglaclient/banners/ (like Feather Client's Server Banner system).
 */
public final class ServerBannerManager {
    private static ServerBannerManager instance;
    private final Path bannersDir;
    private final Path mapFile;
    private final Map<String, Identifier> bannerTextures = new LinkedHashMap<>();
    private final Map<String, String> serverToBannerFile = new LinkedHashMap<>();
    private boolean loaded = false;

    public static synchronized ServerBannerManager getInstance() {
        if (instance == null) instance = new ServerBannerManager();
        return instance;
    }

    private ServerBannerManager() {
        Path root = FabricLoader.getInstance().getGameDir().resolve("banglaclient");
        this.bannersDir = root.resolve("banners");
        this.mapFile = bannersDir.resolve("server_banners.json");
        try {
            Files.createDirectories(bannersDir);
            loadMapping();
        } catch (IOException ignored) {}
    }

    public synchronized void refreshBanners() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getTextureManager() == null) return;
        for (Identifier id : bannerTextures.values()) {
            try { mc.getTextureManager().destroyTexture(id); } catch (Throwable ignored) {}
        }
        bannerTextures.clear();
        if (!Files.exists(bannersDir)) return;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(bannersDir)) {
            for (Path p : stream) {
                if (Files.isDirectory(p)) continue;
                String name = p.getFileName().toString();
                String lower = name.toLowerCase(Locale.ROOT);
                if (!lower.endsWith(".png") && !lower.endsWith(".jpg") && !lower.endsWith(".jpeg")) continue;
                Identifier tex = loadBannerTexture(p, name);
                if (tex != null) {
                    bannerTextures.put(name, tex);
                }
            }
            loaded = true;
        } catch (Throwable ignored) {}
    }

    private Identifier loadBannerTexture(Path file, String fileName) {
        try {
            String safe = fileName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
            Identifier texId = Identifier.of("banglaclient", "banners/" + safe);
            if (fileName.toLowerCase(Locale.ROOT).endsWith(".png")) {
                try (InputStream in = Files.newInputStream(file)) {
                    NativeImage ni = NativeImage.read(in);
                    MinecraftClient.getInstance().getTextureManager().registerTexture(texId, new NativeImageBackedTexture(() -> "banner_" + safe, ni));
                    return texId;
                }
            }
            try (InputStream in = Files.newInputStream(file)) {
                BufferedImage bi = ImageIO.read(in);
                if (bi == null) return null;
                int w = bi.getWidth(), h = bi.getHeight();
                int[] rgb = bi.getRGB(0, 0, w, h, null, 0, w);
                NativeImage ni = new NativeImage(w, h, true);
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        ni.setColorArgb(x, y, rgb[y * w + x]);
                    }
                }
                MinecraftClient.getInstance().getTextureManager().registerTexture(texId, new NativeImageBackedTexture(() -> "banner_" + safe, ni));
                return texId;
            }
        } catch (Throwable t) {
            return null;
        }
    }

    public Identifier getBannerForServer(String serverAddress) {
        if (serverAddress == null || serverAddress.isBlank()) return null;
        if (!loaded) refreshBanners();
        String key = serverAddress.trim().toLowerCase(Locale.ROOT);
        String file = serverToBannerFile.get(key);
        return file != null ? bannerTextures.get(file) : null;
    }

    public void setBannerForServer(String serverAddress, String bannerFileName) {
        if (serverAddress == null || serverAddress.isBlank()) return;
        String key = serverAddress.trim().toLowerCase(Locale.ROOT);
        if (bannerFileName == null || bannerFileName.isEmpty()) {
            serverToBannerFile.remove(key);
        } else {
            serverToBannerFile.put(key, bannerFileName);
        }
        saveMapping();
    }

    public Map<String, Identifier> getBannerTextures() {
        if (!loaded) refreshBanners();
        return Collections.unmodifiableMap(bannerTextures);
    }

    public Path getBannersDir() { return bannersDir; }

    private void loadMapping() {
        JsonObject root = ConfigValidator.readSafeJsonObject(mapFile);
        for (Map.Entry<String, JsonElement> e : root.entrySet()) {
            if (e.getValue().isJsonPrimitive()) {
                serverToBannerFile.put(e.getKey(), e.getValue().getAsString());
            }
        }
    }

    public void saveMapping() {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, String> e : serverToBannerFile.entrySet()) {
            root.addProperty(e.getKey(), e.getValue());
        }
        ConfigManager.writeAtomically(mapFile, root);
    }
}
