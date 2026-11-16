package com.ahmed.banglaclient.cosmetics;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.config.ConfigManager;
import com.ahmed.banglaclient.config.ConfigValidator;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Manages custom Main Menu backgrounds in banglaclient/backgrounds/.
 * Automatically downloads curated HD backgrounds ONLY on the very first launch if internet is available.
 */
public final class BackgroundManager {
    private static final String[][] CURATED_FIRST_LAUNCH_WALLPAPERS = {
            {"Aurora_Night_Sky.jpg", "https://images.unsplash.com/photo-1531366936337-7c912a4589a7?w=1280&q=80"},
            {"Dark_Cyber_Mountain.jpg", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1280&q=80"},
            {"Crimson_Sunset_Horizon.jpg", "https://i.postimg.cc/05X5w9Vm/0b436142caf939e1091ed9c1b48bd323.jpg"},
            {"Midnight_Nebula_Stars.jpg", "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?w=1280&q=80"},
            {"Dark_Minimal_Peaks.jpg", "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1280&q=80"}
    };

    public static final class BackgroundEntry {
        private final String id;
        private final String displayName;
        private final Path filePath;
        private final int width;
        private final int height;
        private final boolean animatedGif;
        private final List<Identifier> frameIds;
        private final int frameDelayMs;

        public BackgroundEntry(String id, String displayName, Path filePath, int width, int height, boolean animatedGif, List<Identifier> frameIds, int frameDelayMs) {
            this.id = id;
            this.displayName = displayName;
            this.filePath = filePath;
            this.width = width;
            this.height = height;
            this.animatedGif = animatedGif;
            this.frameIds = frameIds;
            this.frameDelayMs = Math.max(40, frameDelayMs);
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public Path getFilePath() { return filePath; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public boolean isAnimatedGif() { return animatedGif; }

        public Identifier getCurrentFrameTexture() {
            if (frameIds == null || frameIds.isEmpty()) return null;
            if (frameIds.size() == 1) return frameIds.get(0);
            int idx = (int) ((System.currentTimeMillis() / frameDelayMs) % frameIds.size());
            return frameIds.get(idx);
        }

        public List<Identifier> getAllFrameIds() { return frameIds; }
    }

    private static BackgroundManager instance;
    private final Path backgroundsDir;
    private final Path configFile;
    private final Map<String, BackgroundEntry> entries = new LinkedHashMap<>();
    private String selectedId = "";
    private boolean firstLaunchSeeded = false;

    public static synchronized BackgroundManager getInstance() {
        if (instance == null) {
            instance = new BackgroundManager();
        }
        return instance;
    }

    public BackgroundManager() {
        Path root = FabricLoader.getInstance().getGameDir().resolve("banglaclient");
        this.backgroundsDir = root.resolve("backgrounds");
        this.configFile = backgroundsDir.resolve("selected_bg.json");
        initialize();
    }

    private void initialize() {
        try {
            Files.createDirectories(backgroundsDir);
            loadSelection();
            refreshLibrary();
            if (!firstLaunchSeeded) {
                firstLaunchSeeded = true;
                saveSelection();
                downloadCuratedBackgroundsOnFirstLaunchAsync();
            }
        } catch (IOException e) {
            BanglaClient.LOGGER.error("Failed to create backgrounds directory", e);
        }
    }

    private void downloadCuratedBackgroundsOnFirstLaunchAsync() {
        CompletableFuture.runAsync(() -> {
            boolean downloadedAny = false;
            for (String[] pair : CURATED_FIRST_LAUNCH_WALLPAPERS) {
                String fileName = pair[0];
                String urlStr = pair[1];
                Path target = backgroundsDir.resolve(fileName);
                if (Files.exists(target)) continue;
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(7000);
                    if (conn.getResponseCode() == 200) {
                        try (InputStream in = conn.getInputStream()) {
                            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                            downloadedAny = true;
                        }
                    }
                } catch (Throwable ignored) {
                    // No internet/data available -> silently skip without error
                }
            }
            if (downloadedAny) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc != null) {
                    mc.execute(this::refreshLibrary);
                }
            }
        });
    }

    public synchronized void refreshLibrary() {
        for (BackgroundEntry old : entries.values()) {
            for (Identifier tex : old.getAllFrameIds()) {
                try { MinecraftClient.getInstance().getTextureManager().destroyTexture(tex); } catch (Throwable ignored) {}
            }
        }
        entries.clear();
        if (!Files.exists(backgroundsDir)) return;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(backgroundsDir)) {
            for (Path file : stream) {
                if (Files.isDirectory(file)) continue;
                String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
                if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif")) {
                    BackgroundEntry loaded = loadEntryFromDisk(file);
                    if (loaded != null) {
                        entries.put(loaded.getId(), loaded);
                    }
                }
            }
        } catch (IOException e) {
            BanglaClient.LOGGER.warn("Error scanning backgrounds directory", e);
        }
    }

    private BackgroundEntry loadEntryFromDisk(Path file) {
        try {
            String fileName = file.getFileName().toString();
            int dot = fileName.lastIndexOf('.');
            String title = dot > 0 ? fileName.substring(0, dot).replace('_', ' ') : fileName;
            String lower = fileName.toLowerCase(Locale.ROOT);
            String safeBase = lower.replaceAll("[^a-z0-9_.-]", "_");

            if (lower.endsWith(".gif")) {
                List<Identifier> frames = new ArrayList<>();
                int w = 0, h = 0;
                try (InputStream in = Files.newInputStream(file);
                     ImageInputStream iis = ImageIO.createImageInputStream(in)) {
                    Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
                    if (readers.hasNext()) {
                        ImageReader reader = readers.next();
                        reader.setInput(iis, false);
                        int num = Math.min(36, reader.getNumImages(true));
                        for (int i = 0; i < num; i++) {
                            BufferedImage frameImg = reader.read(i);
                            if (frameImg == null) continue;
                            if (w == 0) { w = frameImg.getWidth(); h = frameImg.getHeight(); }
                            Identifier frameId = Identifier.of("banglaclient", "backgrounds/" + safeBase + "_f" + i);
                            registerBufferedImageAsTexture(frameImg, frameId, safeBase + "_f" + i);
                            frames.add(frameId);
                        }
                        reader.dispose();
                    }
                }
                if (!frames.isEmpty()) {
                    return new BackgroundEntry(fileName, title, file, w, h, true, frames, 90);
                }
            }

            try (InputStream in = Files.newInputStream(file)) {
                BufferedImage img = ImageIO.read(in);
                if (img == null) return null;
                Identifier texId = Identifier.of("banglaclient", "backgrounds/" + safeBase);
                registerBufferedImageAsTexture(img, texId, safeBase);
                return new BackgroundEntry(fileName, title, file, img.getWidth(), img.getHeight(), false, Collections.singletonList(texId), 100);
            }
        } catch (Throwable t) {
            return null;
        }
    }

    private static void registerBufferedImageAsTexture(BufferedImage src, Identifier texId, String debugName) throws IOException {
        BufferedImage argb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        var g = argb.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(argb, "PNG", baos);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
            NativeImage nativeImg = NativeImage.read(bais);
            NativeImageBackedTexture tex = new NativeImageBackedTexture(() -> debugName, nativeImg);
            MinecraftClient.getInstance().getTextureManager().registerTexture(texId, tex);
        }
    }

    public synchronized String importFile(Path source) {
        if (source == null || !Files.exists(source)) return "File not found.";
        String name = source.getFileName().toString();
        String lower = name.toLowerCase(Locale.ROOT);
        if (!lower.endsWith(".png") && !lower.endsWith(".jpg") && !lower.endsWith(".jpeg") && !lower.endsWith(".gif")) {
            return "Supported formats: PNG, JPG, JPEG, GIF";
        }
        try {
            String safeName = name.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path dest = backgroundsDir.resolve(safeName);
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
            refreshLibrary();
            setSelectedId(safeName);
            return "Imported & applied: " + safeName;
        } catch (IOException e) {
            return "Failed to import file.";
        }
    }

    public synchronized boolean renameEntry(BackgroundEntry entry, String newTitle) {
        if (entry == null || newTitle == null || newTitle.isBlank()) return false;
        String clean = newTitle.trim().replaceAll("[^a-zA-Z0-9 _-]", "").trim();
        if (clean.isEmpty()) return false;

        String oldFileName = entry.getId();
        int dot = oldFileName.lastIndexOf('.');
        String ext = dot >= 0 ? oldFileName.substring(dot) : ".png";
        String newFileName = clean.replace(' ', '_') + ext;

        Path newPath = backgroundsDir.resolve(newFileName);
        try {
            boolean wasSelected = oldFileName.equals(selectedId);
            Files.move(entry.getFilePath(), newPath, StandardCopyOption.REPLACE_EXISTING);
            refreshLibrary();
            if (wasSelected) {
                setSelectedId(newFileName);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public synchronized boolean deleteEntry(BackgroundEntry entry) {
        if (entry == null) return false;
        try {
            for (Identifier tex : entry.getAllFrameIds()) {
                try { MinecraftClient.getInstance().getTextureManager().destroyTexture(tex); } catch (Throwable ignored) {}
            }
            boolean deleted = Files.deleteIfExists(entry.getFilePath());
            if (entry.getId().equals(selectedId)) {
                selectedId = "";
            }
            refreshLibrary();
            saveSelection();
            return deleted;
        } catch (IOException e) {
            return false;
        }
    }

    private long lastBgWatchMs = 0L;
    private String lastBgSignature = "";

    public synchronized void checkAndLiveReload() {
        long now = System.currentTimeMillis();
        if (now - lastBgWatchMs < 1500L) return;
        lastBgWatchMs = now;
        try {
            if (MinecraftClient.getInstance() == null || MinecraftClient.getInstance().getTextureManager() == null) return;
            StringBuilder sig = new StringBuilder();
            if (Files.exists(backgroundsDir)) {
                try (var s = Files.list(backgroundsDir)) {
                    s.sorted().forEach(p -> {
                        String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
                        if (n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".gif")) {
                            try { sig.append(n).append(':').append(Files.size(p)).append(';'); } catch (Throwable ignored) {}
                        }
                    });
                }
            }
            String cur = sig.toString();
            if (!cur.equals(lastBgSignature) || (entries.isEmpty() && !cur.isEmpty())) {
                lastBgSignature = cur;
                refreshLibrary();
                loadSelection();
            }
        } catch (Throwable ignored) {}
    }

    public List<BackgroundEntry> getEntries() {
        checkAndLiveReload();
        return Collections.unmodifiableList(new ArrayList<>(entries.values()));
    }

    public BackgroundEntry getActiveBackground() {
        checkAndLiveReload();
        if (selectedId == null || selectedId.isEmpty()) return null;
        return entries.get(selectedId);
    }

    public String getSelectedId() { return selectedId; }

    public void setSelectedId(String id) {
        this.selectedId = id != null ? id : "";
        saveSelection();
    }

    public void loadSelection() {
        JsonObject root = ConfigValidator.readSafeJsonObject(configFile);
        if (root.has("selectedId")) {
            this.selectedId = root.get("selectedId").getAsString();
        }
        if (root.has("firstLaunchSeeded")) {
            this.firstLaunchSeeded = root.get("firstLaunchSeeded").getAsBoolean();
        }
    }

    public void saveSelection() {
        JsonObject root = new JsonObject();
        root.addProperty("selectedId", selectedId != null ? selectedId : "");
        root.addProperty("firstLaunchSeeded", firstLaunchSeeded);
        ConfigManager.writeAtomically(configFile, root);
    }

    public Path getBackgroundsDir() { return backgroundsDir; }
}
