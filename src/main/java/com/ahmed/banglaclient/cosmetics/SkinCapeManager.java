package com.ahmed.banglaclient.cosmetics;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.config.ConfigManager;
import com.ahmed.banglaclient.config.ConfigValidator;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public final class SkinCapeManager {
    private static SkinCapeManager instance;

    private final Path skinsDir;
    private final Path capesDir;
    private final Path selectedFile;

    private final Map<String, SkinEntry> skinLibrary = new LinkedHashMap<>();
    private final Map<String, SkinEntry> capeLibrary = new LinkedHashMap<>();

    private String selectedSkinId = "";
    private String selectedCapeId = "";
    private boolean selectedSlimModel = false;

    public static synchronized SkinCapeManager getInstance() {
        if (instance == null) {
            instance = new SkinCapeManager();
        }
        return instance;
    }

    public SkinCapeManager() {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        Path rootDir = gameDir.resolve("banglaclient");
        this.skinsDir = rootDir.resolve("skins");
        this.capesDir = rootDir.resolve("capes");
        this.selectedFile = rootDir.resolve("cosmetics").resolve("selected.json");
        initializeStorage();
    }

    private synchronized void initializeStorage() {
        try {
            Files.createDirectories(skinsDir);
            Files.createDirectories(capesDir);
            Files.createDirectories(selectedFile.getParent());

            // Permanently remove any legacy auto-generated placeholder files
            Files.deleteIfExists(skinsDir.resolve("skin_001.png"));
            Files.deleteIfExists(skinsDir.resolve("skin_002.png"));
            Files.deleteIfExists(capesDir.resolve("cape_001.png"));
            Files.deleteIfExists(capesDir.resolve("cape_002.png"));

            refreshLibraries();
            loadSelection();
        } catch (IOException e) {
            BanglaClient.LOGGER.error("Failed to initialize SkinCapeManager directories", e);
        }
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
                SkinEntry loaded = loadAndRegisterTexture(entry, isCape);
                if (loaded != null) {
                    targetMap.put(loaded.getId(), loaded);
                }
            }
        } catch (IOException e) {
            BanglaClient.LOGGER.warn("Error scanning cosmetics directory {}", dir, e);
        }
    }

    public SkinEntry loadAndRegisterTexture(Path file, boolean asCape) {
        if (file == null || !Files.exists(file)) return null;
        try (InputStream in = Files.newInputStream(file)) {
            BufferedImage img = ImageIO.read(in);
            if (img == null) return null;
            int w = img.getWidth();
            int h = img.getHeight();
            if (asCape) {
                boolean validCape = (w == 64 && h == 32) || (w == 128 && h == 64) || (w == 22 && h == 17);
                if (!validCape) return null;
            } else {
                boolean validSkin = (w == 64 && (h == 64 || h == 32)) || (w == 128 && (h == 128 || h == 64));
                if (!validSkin) return null;
            }

            String fileName = file.getFileName().toString();
            String cleanTitle = fileName.substring(0, fileName.length() - 4).replace('_', ' ');
            String safeTexName = fileName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
            Identifier texIdentifier = Identifier.of("banglaclient", (asCape ? "capes/" : "skins/") + safeTexName);

            // Register native OpenGL texture once on GPU
            try (InputStream nativeIn = Files.newInputStream(file)) {
                NativeImage nativeImage = NativeImage.read(nativeIn);
                NativeImageBackedTexture nativeTexture = new NativeImageBackedTexture(nativeImage);
                MinecraftClient.getInstance().getTextureManager().registerTexture(texIdentifier, nativeTexture);
            }

            int cornerAlpha = 255;
            if (!asCape && h == w) {
                int[] sample = new int[1];
                img.getRGB(Math.min(w - 1, 54 * (w / 64)), Math.min(h - 1, 20 * (h / 64)), 1, 1, sample, 0, 1);
                cornerAlpha = (sample[0] >>> 24) & 0xFF;
            }
            boolean autoSlim = !asCape && h == w && cornerAlpha == 0;

            return new SkinEntry(fileName, cleanTitle, file, w, h, asCape, autoSlim, texIdentifier);
        } catch (Exception e) {
            return null;
        }
    }

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
                return "Rejected: Corrupted or unreadable PNG.";
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
            String safeFileName = sourceFile.getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
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
            MinecraftClient.getInstance().getTextureManager().destroyTexture(entry.getTextureId());
            boolean deleted = Files.deleteIfExists(entry.getFilePath());
            refreshLibraries();
            if (entry.isCape() && entry.getId().equals(selectedCapeId)) {
                selectedCapeId = "";
            } else if (!entry.isCape() && entry.getId().equals(selectedSkinId)) {
                selectedSkinId = "";
            }
            saveSelection();
            return deleted;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Resolves the genuine SkinTextures: uses the authenticated account skin (Steve/Alex fallback by UUID)
     * if no custom local skin is chosen, or returns the verified local texture.
     */
    public SkinTextures getActiveSkinTextures() {
        MinecraftClient mc = MinecraftClient.getInstance();
        SkinTextures vanillaDefault = getAuthenticatedAccountDefaultSkin(mc);

        SkinEntry skin = getSelectedSkin();
        SkinEntry cape = getSelectedCape();

        Identifier skinTexture = (skin != null) ? skin.getTextureId() : vanillaDefault.texture();
        SkinTextures.Model model = (skin != null)
                ? (skin.isSlimModel() ? SkinTextures.Model.SLIM : SkinTextures.Model.WIDE)
                : vanillaDefault.model();
        Identifier capeTexture = (cape != null) ? cape.getTextureId() : vanillaDefault.capeTexture();

        return new SkinTextures(
                skinTexture,
                vanillaDefault.textureUrl(),
                capeTexture,
                vanillaDefault.elytraTexture(),
                model,
                true
        );
    }

    public static SkinTextures getAuthenticatedAccountDefaultSkin(MinecraftClient mc) {
        if (mc == null) return DefaultSkinHelper.getSkinTextures(DefaultSkinHelper.ZERO_UUID);
        GameProfile profile = mc.getSession() != null ? mc.getSession().getProfile() : null;
        UUID uuid = profile != null ? profile.id() : DefaultSkinHelper.ZERO_UUID;
        if (mc.getSkinProvider() != null && profile != null) {
            return mc.getSkinProvider().getSkinTextures(profile);
        }
        return DefaultSkinHelper.getSkinTextures(uuid);
    }

    public boolean hasCustomSkinSelected() {
        return selectedSkinId != null && !selectedSkinId.isEmpty() && skinLibrary.containsKey(selectedSkinId);
    }

    public boolean hasCustomCapeSelected() {
        return selectedCapeId != null && !selectedCapeId.isEmpty() && capeLibrary.containsKey(selectedCapeId);
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
        if (selectedSkinId != null && !selectedSkinId.isEmpty() && skinLibrary.containsKey(selectedSkinId)) {
            return skinLibrary.get(selectedSkinId);
        }
        return null;
    }

    public SkinEntry getSelectedCape() {
        if (selectedCapeId != null && !selectedCapeId.isEmpty() && capeLibrary.containsKey(selectedCapeId)) {
            return capeLibrary.get(selectedCapeId);
        }
        return null;
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
