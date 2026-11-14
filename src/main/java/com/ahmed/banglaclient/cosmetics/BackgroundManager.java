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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Manages custom Main Menu backgrounds in banglaclient/backgrounds/ supporting PNG, JPG, JPEG, and animated GIFs.
 */
public final class BackgroundManager {
    public static final class BackgroundEntry {
        private String id;
        private String displayName;
        private Path filePath;
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
    private String selectedId = ""; //Empty = Default Minecraft Panorama

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
            refreshLibrary();
            loadSelection();
        } catch (IOException e) {
            BanglaClient.LOGGER.error("Failed to create backgrounds directory", e);
        }
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
            String title = dot > 0 ? fileName.substring(0, dot) : fileName;
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

    public List<BackgroundEntry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<>(entries.values()));
    }

    public BackgroundEntry getActiveBackground() {
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
    }

    public void saveSelection() {
        JsonObject root = new JsonObject();
        root.addProperty("selectedId", selectedId != null ? selectedId : "");
        ConfigManager.writeAtomically(configFile, root);
    }

    public Path getBackgroundsDir() { return backgroundsDir; }
}
