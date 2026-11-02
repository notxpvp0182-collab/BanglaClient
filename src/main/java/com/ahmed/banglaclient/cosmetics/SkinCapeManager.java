package com.ahmed.banglaclient.cosmetics;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.config.ConfigManager;
import com.ahmed.banglaclient.config.ConfigValidator;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Manages .minecraft/banglaclient/skins/, .minecraft/banglaclient/capes/,
 * and .minecraft/banglaclient/cosmetics/selected.json with PNG validation and caching.
 */
public final class SkinCapeManager {
    private final Path rootDir;
    private final Path skinsDir;
    private final Path capesDir;
    private final Path selectedFile;

    private final Map<String, SkinEntry> skinLibrary = new LinkedHashMap<>();
    private final Map<String, SkinEntry> capeLibrary = new LinkedHashMap<>();

    private String selectedSkinId = "skin_001.png";
    private String selectedCapeId = "cape_001.png";
    private boolean selectedSlimModel = false;

    public SkinCapeManager() {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        this.rootDir = gameDir.resolve("banglaclient");
        this.skinsDir = rootDir.resolve("skins");
        this.capesDir = rootDir.resolve("capes");
        this.selectedFile = rootDir.resolve("cosmetics").resolve("selected.json");
        initializeStorage();
    }

    public synchronized void initializeStorage() {
        try {
            Files.createDirectories(skinsDir);
            Files.createDirectories(capesDir);
            Files.createDirectories(selectedFile.getParent());
            ensureStarterAssets();
            refreshLibraries();
            loadSelection();
        } catch (IOException e) {
            BanglaClient.LOGGER.error("Failed to initialize SkinCapeManager directories", e);
        }
    }

    private void ensureStarterAssets() throws IOException {
        Path skin1 = skinsDir.resolve("skin_001.png");
        if (!Files.exists(skin1)) {
            createStarterSkinPng(skin1, new Color(220, 38, 38), new Color(24, 28, 38), new Color(245, 195, 160));
        }
        Path skin2 = skinsDir.resolve("skin_002.png");
        if (!Files.exists(skin2)) {
            createStarterSkinPng(skin2, new Color(16, 185, 129), new Color(17, 24, 39), new Color(235, 185, 150));
        }
        Path cape1 = capesDir.resolve("cape_001.png");
        if (!Files.exists(cape1)) {
            createStarterCapePng(cape1, new Color(185, 28, 28), new Color(251, 191, 36));
        }
        Path cape2 = capesDir.resolve("cape_002.png");
        if (!Files.exists(cape2)) {
            createStarterCapePng(cape2, new Color(15, 23, 42), new Color(239, 68, 68));
        }
    }

    private static void createStarterSkinPng(Path target, Color accent, Color suit, Color skinTone) throws IOException {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        // Head (8x8x8 at UV 0,0..32,16)
        g.setColor(skinTone);
        g.fillRect(0, 0, 32, 16);
        // Hair & headband on head + hat layer
        g.setColor(new Color(20, 20, 26));
        g.fillRect(8, 0, 16, 10);
        g.setColor(accent);
        g.fillRect(8, 9, 8, 2);
        // Eyes
        g.setColor(Color.WHITE);
        g.fillRect(9, 12, 2, 1);
        g.fillRect(13, 12, 2, 1);
        g.setColor(accent);
        g.fillRect(10, 12, 1, 1);
        g.fillRect(13, 12, 1, 1);
        // Body (16,16..40,32)
        g.setColor(suit);
        g.fillRect(16, 16, 24, 16);
        g.setColor(accent);
        g.fillRect(22, 20, 4, 10);
        // Right & Left Arms
        g.setColor(suit);
        g.fillRect(40, 16, 16, 16);
        g.fillRect(32, 48, 16, 16);
        g.setColor(accent);
        g.fillRect(40, 26, 16, 2);
        g.fillRect(32, 58, 16, 2);
        // Right & Left Legs
        g.setColor(new Color(15, 18, 26));
        g.fillRect(0, 16, 16, 16);
        g.fillRect(16, 48, 16, 16);
        g.dispose();
        ImageIO.write(img, "PNG", target.toFile());
    }

    private static void createStarterCapePng(Path target, Color baseCol, Color emblemCol) throws IOException {
        BufferedImage img = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(baseCol);
        g.fillRect(1, 1, 20, 16);
        g.setColor(emblemCol);
        g.drawRect(2, 2, 8, 14);
        // Crown emblem in center of cape front (UV 1..11, 1..17)
        g.fillRect(4, 6, 4, 4);
        g.fillRect(3, 5, 1, 2);
        g.fillRect(8, 5, 1, 2);
        g.dispose();
        ImageIO.write(img, "PNG", target.toFile());
    }

    public synchronized void refreshLibraries() {
        skinLibrary.clear();
        capeLibrary.clear();
        scanDirectory(skinsDir, false, skinLibrary);
        scanDirectory(capesDir, true, capeLibrary);
    }

    private void scanDirectory(Path dir, boolean isCape, Map<String, SkinEntry> targetMap) {
        if (!Files.exists(dir)) return;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.png")) {
            for (Path entry : stream) {
                SkinEntry loaded = decodeAndValidatePng(entry, isCape);
                if (loaded != null) {
                    targetMap.put(loaded.getId(), loaded);
                }
            }
        } catch (IOException e) {
            BanglaClient.LOGGER.warn("Error scanning cosmetics directory {}", dir, e);
        }
    }

    public SkinEntry decodeAndValidatePng(Path file, boolean asCape) {
        if (file == null || !Files.exists(file)) return null;
        try (InputStream in = Files.newInputStream(file)) {
            BufferedImage img = ImageIO.read(in);
            if (img == null) return null;
            int w = img.getWidth();
            int h = img.getHeight();
            if (asCape) {
                boolean validCape = (w == 64 && h == 32) || (w == 128 && h == 64) || (w >= 22 && h >= 17 && w <= 512 && h <= 256);
                if (!validCape) return null;
            } else {
                boolean validSkin = (w == 64 && (h == 64 || h == 32)) || (w == 128 && (h == 128 || h == 64));
                if (!validSkin) return null;
            }
            int[] pixels = new int[w * h];
            img.getRGB(0, 0, w, h, pixels, 0, w);
            String fileName = file.getFileName().toString();
            String cleanTitle = fileName.substring(0, fileName.length() - 4).replace('_', ' ');
            boolean autoSlim = !asCape && h == w && (((pixels[(20 * (h / 64)) * w + (54 * (w / 64))] >>> 24) & 0xFF) == 0);
            return new SkinEntry(fileName, cleanTitle, file, w, h, asCape, autoSlim, pixels);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Imports a dropped or selected PNG file into skins/ or capes/ after validating dimensions.
     */
    public synchronized String importExternalFile(Path sourceFile, Boolean forceCapeMode) {
        if (sourceFile == null || !Files.exists(sourceFile)) {
            return "File does not exist.";
        }
        String nameLower = sourceFile.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!nameLower.endsWith(".png")) {
            return "Rejected: Only .png textures are supported.";
        }
        try (InputStream in = Files.newInputStream(sourceFile)) {
            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                return "Rejected: Corrupted or unreadable PNG image.";
            }
            int w = img.getWidth();
            int h = img.getHeight();
            boolean isSkinDim = (w == 64 && (h == 64 || h == 32)) || (w == 128 && (h == 128 || h == 64));
            boolean isCapeDim = (w == 64 && h == 32) || (w == 128 && h == 64) || (w == 22 && h == 17);

            boolean importAsCape;
            if (forceCapeMode != null) {
                importAsCape = forceCapeMode;
                if (importAsCape && !isCapeDim) return "Invalid Cape dimensions (" + w + "x" + h + "). Expected 64x32.";
                if (!importAsCape && !isSkinDim) return "Invalid Skin dimensions (" + w + "x" + h + "). Expected 64x64 or 64x32.";
            } else {
                if (w == h && isSkinDim) importAsCape = false;
                else if (w == h * 2 && nameLower.contains("cape")) importAsCape = true;
                else if (isSkinDim) importAsCape = false;
                else if (isCapeDim) importAsCape = true;
                else return "Rejected: Unsupported PNG dimensions (" + w + "x" + h + ").";
            }

            Path targetFolder = importAsCape ? capesDir : skinsDir;
            String safeFileName = sourceFile.getFileName().toString().replaceAll("[^a-zA-Z0-9._\-]", "_");
            Path destination = targetFolder.resolve(safeFileName);
            Files.copy(sourceFile, destination, StandardCopyOption.REPLACE_EXISTING);
            refreshLibraries();
            if (importAsCape) {
                this.selectedCapeId = safeFileName;
            } else {
                this.selectedSkinId = safeFileName;
            }
            saveSelection();
            return "Imported " + (importAsCape ? "Cape" : "Skin") + ": " + safeFileName;
        } catch (IOException e) {
            return "Import error: " + e.getMessage();
        }
    }

    public synchronized boolean removeEntry(SkinEntry entry) {
        if (entry == null) return false;
        try {
            boolean deleted = Files.deleteIfExists(entry.getFilePath());
            refreshLibraries();
            if (entry.isCape() && entry.getId().equals(selectedCapeId)) {
                selectedCapeId = capeLibrary.isEmpty() ? "" : capeLibrary.keySet().iterator().next();
            } else if (!entry.isCape() && entry.getId().equals(selectedSkinId)) {
                selectedSkinId = skinLibrary.isEmpty() ? "" : skinLibrary.keySet().iterator().next();
            }
            saveSelection();
            return deleted;
        } catch (IOException e) {
            return false;
        }
    }

    public void loadSelection() {
        JsonObject root = ConfigValidator.readSafeJsonObject(selectedFile);
        if (root.has("selectedSkinId")) this.selectedSkinId = root.get("selectedSkinId").getAsString();
        if (root.has("selectedCapeId")) this.selectedCapeId = root.get("selectedCapeId").getAsString();
        if (root.has("selectedSlimModel")) this.selectedSlimModel = root.get("selectedSlimModel").getAsBoolean();
    }

    public void saveSelection() {
        JsonObject root = new JsonObject();
        root.addProperty("selectedSkinId", selectedSkinId != null ? selectedSkinId : "");
        root.addProperty("selectedCapeId", selectedCapeId != null ? selectedCapeId : "");
        root.addProperty("selectedSlimModel", selectedSlimModel);
        ConfigManager.writeAtomically(selectedFile, root);
    }

    public List<SkinEntry> getSkins() { return Collections.unmodifiableList(new ArrayList<>(skinLibrary.values())); }
    public List<SkinEntry> getCapes() { return Collections.unmodifiableList(new ArrayList<>(capeLibrary.values())); }

    public SkinEntry getSelectedSkin() {
        if (selectedSkinId != null && skinLibrary.containsKey(selectedSkinId)) {
            return skinLibrary.get(selectedSkinId);
        }
        return skinLibrary.values().stream().findFirst().orElse(null);
    }

    public SkinEntry getSelectedCape() {
        if (selectedCapeId == null || selectedCapeId.isEmpty()) return null;
        return capeLibrary.get(selectedCapeId);
    }

    public void setSelectedSkinId(String id) {
        this.selectedSkinId = id != null ? id : "";
        SkinEntry s = getSelectedSkin();
        if (s != null) this.selectedSlimModel = s.isSlimModel();
        saveSelection();
    }

    public void setSelectedCapeId(String id) {
        this.selectedCapeId = id != null ? id : "";
        saveSelection();
    }

    public boolean isSelectedSlimModel() { return selectedSlimModel; }
    public void setSelectedSlimModel(boolean slim) {
        this.selectedSlimModel = slim;
        SkinEntry s = getSelectedSkin();
        if (s != null) s.setSlimModel(slim);
        saveSelection();
    }

    public Path getSkinsDir() { return skinsDir; }
    public Path getCapesDir() { return capesDir; }
}
