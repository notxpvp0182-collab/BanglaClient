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

import org.jcodec.api.FrameGrab;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.ColorUtil;
import org.jcodec.scale.Transform;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * High-Performance Background Manager supporting Full-HD PNG, JPG, JPEG, Animated GIF, and MP4 Video frames
 * with zero startup lag and zero render-thread FPS drops.
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
        private final boolean animated;
        private final List<Identifier> frameIds;
        private final int frameDelayMs;

        public BackgroundEntry(String id, String displayName, Path filePath, int width, int height, boolean animated, List<Identifier> frameIds, int frameDelayMs) {
            this.id = id;
            this.displayName = displayName;
            this.filePath = filePath;
            this.width = width;
            this.height = height;
            this.animated = animated;
            this.frameIds = frameIds;
            this.frameDelayMs = Math.max(33, frameDelayMs);
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public Path getFilePath() { return filePath; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public boolean isAnimatedGif() { return animated; }

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
    private boolean initializedTextures = false;
    private String lastBgSignature = "";

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
        try {
            Files.createDirectories(backgroundsDir);
            loadSelection();
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
            for (String[] pair : CURATED_FIRST_LAUNCH_WALLPAPERS) {
                Path target = backgroundsDir.resolve(pair[0]);
                if (Files.exists(target)) continue;
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(pair[1]).openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    conn.setConnectTimeout(4000);
                    conn.setReadTimeout(6000);
                    if (conn.getResponseCode() == 200) {
                        try (InputStream in = conn.getInputStream()) {
                            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (Throwable ignored) {}
            }
        });
    }

    public synchronized void checkAndLiveReload() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getTextureManager() == null) return;
        try {
            StringBuilder sig = new StringBuilder();
            if (Files.exists(backgroundsDir)) {
                try (var s = Files.list(backgroundsDir)) {
                    s.sorted().forEach(p -> {
                        String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
                        if (isSupportedFile(n)) {
                            try { sig.append(n).append(':').append(Files.size(p)).append(';'); } catch (Throwable ignored) {}
                        }
                    });
                }
            }
            String cur = sig.toString();
            if (!cur.equals(lastBgSignature)) {
                lastBgSignature = cur;
                refreshLibrary();
            }
        } catch (Throwable ignored) {}
    }

    private static boolean isSupportedFile(String lower) {
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".gif") || lower.endsWith(".mp4");
    }

    public synchronized void refreshLibrary() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getTextureManager() == null) return;
        for (BackgroundEntry old : entries.values()) {
            for (Identifier tex : old.getAllFrameIds()) {
                try { mc.getTextureManager().destroyTexture(tex); } catch (Throwable ignored) {}
            }
        }
        entries.clear();
        if (!Files.exists(backgroundsDir)) return;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(backgroundsDir)) {
            for (Path file : stream) {
                if (Files.isDirectory(file)) continue;
                String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
                if (isSupportedFile(name)) {
                    BackgroundEntry loaded = loadEntryFromDisk(file);
                    if (loaded != null) {
                        entries.put(loaded.getId(), loaded);
                    }
                }
            }
            initializedTextures = true;
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

            if (lower.endsWith(".mp4")) {
                return loadMp4Entry(file, fileName, title, safeBase);
            }

            if (lower.endsWith(".gif")) {
                List<Identifier> frames = new ArrayList<>();
                int canvasW = 0, canvasH = 0;
                try (InputStream in = Files.newInputStream(file);
                     ImageInputStream iis = ImageIO.createImageInputStream(in)) {
                    Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
                    if (readers.hasNext()) {
                        ImageReader reader = readers.next();
                        reader.setInput(iis, false);
                        int num = Math.min(48, reader.getNumImages(true));
                        BufferedImage masterCanvas = null;
                        for (int i = 0; i < num; i++) {
                            BufferedImage rawFrame = reader.read(i);
                            if (rawFrame == null) continue;
                            if (masterCanvas == null) {
                                canvasW = rawFrame.getWidth();
                                canvasH = rawFrame.getHeight();
                                masterCanvas = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);
                            }
                            // Composite delta frame onto master canvas so Full-HD GIFs never look ghosted or blurry!
                            var g = masterCanvas.createGraphics();
                            g.drawImage(rawFrame, 0, 0, null);
                            g.dispose();

                            Identifier frameId = Identifier.of("banglaclient", "backgrounds/" + safeBase + "_f" + i);
                            registerBufferedImageDirect(masterCanvas, frameId, safeBase + "_f" + i);
                            frames.add(frameId);
                        }
                        reader.dispose();
                    }
                }
                if (!frames.isEmpty()) {
                    return new BackgroundEntry(fileName, title, file, canvasW, canvasH, true, frames, 65);
                }
            }

            if (lower.endsWith(".png")) {
                try (InputStream in = Files.newInputStream(file)) {
                    NativeImage nativeImg = NativeImage.read(in);
                    int w = nativeImg.getWidth(), h = nativeImg.getHeight();
                    Identifier texId = Identifier.of("banglaclient", "backgrounds/" + safeBase);
                    NativeImageBackedTexture tex = new NativeImageBackedTexture(() -> safeBase, nativeImg);
                    MinecraftClient.getInstance().getTextureManager().registerTexture(texId, tex);
                    return new BackgroundEntry(fileName, title, file, w, h, false, Collections.singletonList(texId), 100);
                }
            }

            try (InputStream in = Files.newInputStream(file)) {
                BufferedImage img = ImageIO.read(in);
                if (img == null) return null;
                Identifier texId = Identifier.of("banglaclient", "backgrounds/" + safeBase);
                registerBufferedImageDirect(img, texId, safeBase);
                return new BackgroundEntry(fileName, title, file, img.getWidth(), img.getHeight(), false, Collections.singletonList(texId), 100);
            }
        } catch (Throwable t) {
            return null;
        }
    }

    private BackgroundEntry loadMp4Entry(Path mp4File, String fileName, String title, String safeBase) {
        try {
            Path cacheDir = backgroundsDir.resolve(".mp4_" + safeBase);
            Files.createDirectories(cacheDir);

            // Check if frames were already decoded and cached
            List<Path> existingPngs;
            try (var s = Files.list(cacheDir)) {
                existingPngs = s.filter(p -> p.toString().endsWith(".png")).sorted().toList();
            }

            if (existingPngs.isEmpty()) {
                // Pure-Java JCodec H.264/MP4 Decoder (Works on Windows & Android without ffmpeg!)
                try (SeekableByteChannel channel = NIOUtils.readableChannel(mp4File.toFile())) {
                    FrameGrab grab = FrameGrab.createFrameGrab(channel);
                    Picture pic;
                    int frameIdx = 0;
                    int maxFrames = 60;
                    while (frameIdx < maxFrames && (pic = grab.getNativeFrame()) != null) {
                        Picture rgb = Picture.create(pic.getWidth(), pic.getHeight(), ColorSpace.RGB);
                        Transform transform = ColorUtil.getTransform(pic.getColor(), ColorSpace.RGB);
                        if (transform != null) {
                            transform.transform(pic, rgb);
                            int srcW = rgb.getWidth();
                            int srcH = rgb.getHeight();
                            // Keep Full-HD aspect ratio (up to 1280x720 for fast GPU texture upload)
                            int outW = srcW > 1280 ? 1280 : srcW;
                            int outH = srcW > 1280 ? Math.max(1, (srcH * 1280) / srcW) : srcH;
                            byte[] plane = rgb.getPlaneData(0);
                            try (NativeImage ni = new NativeImage(outW, outH, true)) {
                                for (int y = 0; y < outH; y++) {
                                    int sy = (y * srcH) / outH;
                                    for (int x = 0; x < outW; x++) {
                                        int sx = (x * srcW) / outW;
                                        int off = (sy * srcW + sx) * 3;
                                        if (off + 2 < plane.length) {
                                            int r = Math.max(0, Math.min(255, plane[off] + 128));
                                            int g = Math.max(0, Math.min(255, plane[off + 1] + 128));
                                            int b = Math.max(0, Math.min(255, plane[off + 2] + 128));
                                            ni.setColorArgb(x, y, (0xFF << 24) | (r << 16) | (g << 8) | b);
                                        }
                                    }
                                }
                                Path outPng = cacheDir.resolve(String.format(Locale.ROOT, "frame_%03d.png", frameIdx));
                                ni.writeTo(outPng);
                            }
                        }
                        frameIdx++;
                    }
                } catch (Throwable e) {
                    BanglaClient.LOGGER.warn("JCodec MP4 decode warning for {}: {}", fileName, e.getMessage());
                }
            }

            List<Identifier> frames = new ArrayList<>();
            int w = 1280, h = 720;
            try (var stream = Files.list(cacheDir)) {
                List<Path> pngs = stream.filter(p -> p.toString().endsWith(".png")).sorted().toList();
                for (int i = 0; i < pngs.size(); i++) {
                    try (InputStream in = Files.newInputStream(pngs.get(i))) {
                        NativeImage ni = NativeImage.read(in);
                        w = ni.getWidth();
                        h = ni.getHeight();
                        Identifier fid = Identifier.of("banglaclient", "backgrounds/" + safeBase + "_v" + i);
                        MinecraftClient.getInstance().getTextureManager().registerTexture(fid, new NativeImageBackedTexture(() -> safeBase, ni));
                        frames.add(fid);
                    }
                }
            }
            if (!frames.isEmpty()) {
                return new BackgroundEntry(fileName, title, mp4File, w, h, true, frames, 50);
            }
        } catch (Throwable ignored) {}
        return null;
    }

    /**
     * Direct fast pixel transfer from BufferedImage to NativeImage without slow PNG re-encoding!
     */
    private static void registerBufferedImageDirect(BufferedImage src, Identifier texId, String debugName) {
        int w = src.getWidth();
        int h = src.getHeight();
        int[] rgbArray = src.getRGB(0, 0, w, h, null, 0, w);
        NativeImage nativeImg = new NativeImage(w, h, true);
        for (int y = 0; y < h; y++) {
            int rowOff = y * w;
            for (int x = 0; x < w; x++) {
                nativeImg.setColorArgb(x, y, rgbArray[rowOff + x]);
            }
        }
        NativeImageBackedTexture tex = new NativeImageBackedTexture(() -> debugName, nativeImg);
        MinecraftClient.getInstance().getTextureManager().registerTexture(texId, tex);
    }

    public synchronized String importFile(Path source) {
        if (source == null || !Files.exists(source)) return "File not found.";
        String name = source.getFileName().toString();
        if (!isSupportedFile(name.toLowerCase(Locale.ROOT))) {
            return "Supported: PNG, JPG, JPEG, GIF, MP4";
        }
        try {
            String safeName = name.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path dest = backgroundsDir.resolve(safeName);
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
            lastBgSignature = "";
            checkAndLiveReload();
            setSelectedId(safeName);
            return "Imported: " + safeName;
        } catch (IOException e) {
            return "Failed to import file.";
        }
    }

    public synchronized boolean deleteEntry(BackgroundEntry entry) {
        if (entry == null) return false;
        try {
            for (Identifier tex : entry.getAllFrameIds()) {
                try { MinecraftClient.getInstance().getTextureManager().destroyTexture(tex); } catch (Throwable ignored) {}
            }
            boolean deleted = Files.deleteIfExists(entry.getFilePath());
            if (entry.getId().equals(selectedId)) selectedId = "";
            lastBgSignature = "";
            checkAndLiveReload();
            saveSelection();
            return deleted;
        } catch (IOException e) {
            return false;
        }
    }

    public List<BackgroundEntry> getEntries() {
        checkAndLiveReload();
        return Collections.unmodifiableList(new ArrayList<>(entries.values()));
    }

    public BackgroundEntry getActiveBackground() {
        if (selectedId == null || selectedId.isEmpty()) return null;
        if (!initializedTextures) {
            checkAndLiveReload();
        }
        return entries.get(selectedId);
    }

    public String getSelectedId() { return selectedId; }

    public void setSelectedId(String id) {
        this.selectedId = id != null ? id : "";
        saveSelection();
    }

    public void loadSelection() {
        JsonObject root = ConfigValidator.readSafeJsonObject(configFile);
        if (root.has("selectedId")) this.selectedId = root.get("selectedId").getAsString();
        if (root.has("firstLaunchSeeded")) this.firstLaunchSeeded = root.get("firstLaunchSeeded").getAsBoolean();
    }

    public void saveSelection() {
        JsonObject root = new JsonObject();
        root.addProperty("selectedId", selectedId != null ? selectedId : "");
        root.addProperty("firstLaunchSeeded", firstLaunchSeeded);
        ConfigManager.writeAtomically(configFile, root);
    }

    public Path getBackgroundsDir() { return backgroundsDir; }
}
