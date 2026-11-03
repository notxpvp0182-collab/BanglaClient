package com.ahmed.banglaclient.cosmetics;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.config.ConfigManager;
import com.ahmed.banglaclient.config.ConfigValidator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
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

    public static final class AccountCosmetics {
        public String skinId = "";
        public String capeId = "";
        public boolean slimModel = false;
        public boolean skinOverride = true;
        public boolean capeOverride = true;
    }

    private final Path skinsDir;
    private final Path capesDir;
    private final Path selectedFile;

    private final Map<String, SkinEntry> skinLibrary = new LinkedHashMap<>();
    private final Map<String, SkinEntry> capeLibrary = new LinkedHashMap<>();
    private final Map<String, AccountCosmetics> accountCosmeticsMap = new HashMap<>();

    private String activeUsername = "player";

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

            // Permanently remove any legacy starter assets
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

            try (InputStream nativeIn = Files.newInputStream(file)) {
                NativeImage nativeImage = NativeImage.read(nativeIn);
                NativeImageBackedTexture nativeTexture = new NativeImageBackedTexture(() -> safeTexName, nativeImage);
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

            AccountCosmetics cos = getActiveAccountCosmetics();
            if (importAsCape) {
                cos.capeId = safeFileName;
                cos.capeOverride = true;
            } else {
                cos.skinId = safeFileName;
                cos.skinOverride = true;
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

            AccountCosmetics cos = getActiveAccountCosmetics();
            if (entry.isCape() && entry.getId().equals(cos.capeId)) {
                cos.capeId = "";
            } else if (!entry.isCape() && entry.getId().equals(cos.skinId)) {
                cos.skinId = "";
            }
            saveSelection();
            return deleted;
        } catch (IOException e) {
            return false;
        }
    }

    public synchronized void onAccountSwitched(String newUsername) {
        if (newUsername != null && !newUsername.isBlank()) {
            this.activeUsername = newUsername.trim().toLowerCase(Locale.ROOT);
            saveSelection();
        }
    }

    public AccountCosmetics getActiveAccountCosmetics() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.getSession() != null && mc.getSession().getUsername() != null) {
            this.activeUsername = mc.getSession().getUsername().toLowerCase(Locale.ROOT);
        }
        return accountCosmeticsMap.computeIfAbsent(this.activeUsername, k -> new AccountCosmetics());
    }

    public SkinTextures getActiveSkinTextures() {
        MinecraftClient mc = MinecraftClient.getInstance();
        SkinTextures vanillaDefault = getAuthenticatedAccountDefaultSkin(mc);

        AccountCosmetics cos = getActiveAccountCosmetics();
        SkinEntry skin = (cos.skinOverride && hasCustomSkinSelected()) ? getSelectedSkin() : null;
        SkinEntry cape = (cos.capeOverride && hasCustomCapeSelected()) ? getSelectedCape() : null;

        AssetInfo.TextureAsset body = (skin != null)
                ? new AssetInfo.TextureAssetInfo(skin.getTextureId(), skin.getTextureId())
                : vanillaDefault.body();
        AssetInfo.TextureAsset capeAsset = (cape != null)
                ? new AssetInfo.TextureAssetInfo(cape.getTextureId(), cape.getTextureId())
                : vanillaDefault.cape();
        PlayerSkinType model = (skin != null)
                ? (skin.isSlimModel() ? PlayerSkinType.SLIM : PlayerSkinType.WIDE)
                : vanillaDefault.model();

        return new SkinTextures(
                body,
                capeAsset,
                vanillaDefault.elytra(),
                model,
                true
        );
    }

    public static SkinTextures getAuthenticatedAccountDefaultSkin(MinecraftClient mc) {
        UUID uuid = new UUID(0L, 0L);
        if (mc != null && mc.getSession() != null && mc.getSession().getUuidOrNull() != null) {
            uuid = mc.getSession().getUuidOrNull();
        }
        return DefaultSkinHelper.getSkinTextures(uuid);
    }

    public boolean isCustomSkinOverride() {
        return getActiveAccountCosmetics().skinOverride && hasCustomSkinSelected();
    }

    public void setCustomSkinOverride(boolean enabled) {
        getActiveAccountCosmetics().skinOverride = enabled;
        saveSelection();
    }

    public boolean isCustomCapeOverride() {
        return getActiveAccountCosmetics().capeOverride && hasCustomCapeSelected();
    }

    public void setCustomCapeOverride(boolean enabled) {
        getActiveAccountCosmetics().capeOverride = enabled;
        saveSelection();
    }

    public boolean hasCustomSkinSelected() {
        String id = getActiveAccountCosmetics().skinId;
        return id != null && !id.isEmpty() && skinLibrary.containsKey(id);
    }

    public boolean hasCustomCapeSelected() {
        String id = getActiveAccountCosmetics().capeId;
        return id != null && !id.isEmpty() && capeLibrary.containsKey(id);
    }

    public void loadSelection() {
        JsonObject root = ConfigValidator.readSafeJsonObject(selectedFile);
        if (root.has("activeUsername")) {
            this.activeUsername = root.get("activeUsername").getAsString().toLowerCase(Locale.ROOT);
        }
        if (root.has("accounts") && root.get("accounts").isJsonObject()) {
            JsonObject accs = root.getAsJsonObject("accounts");
            for (Map.Entry<String, JsonElement> e : accs.entrySet()) {
                if (e.getValue().isJsonObject()) {
                    JsonObject obj = e.getValue().getAsJsonObject();
                    AccountCosmetics c = new AccountCosmetics();
                    if (obj.has("skinId")) c.skinId = obj.get("skinId").getAsString();
                    if (obj.has("capeId")) c.capeId = obj.get("capeId").getAsString();
                    if (obj.has("slimModel")) c.slimModel = obj.get("slimModel").getAsBoolean();
                    if (obj.has("skinOverride")) c.skinOverride = obj.get("skinOverride").getAsBoolean();
                    if (obj.has("capeOverride")) c.capeOverride = obj.get("capeOverride").getAsBoolean();
                    accountCosmeticsMap.put(e.getKey().toLowerCase(Locale.ROOT), c);
                }
            }
        }
    }

    public void saveSelection() {
        JsonObject root = new JsonObject();
        root.addProperty("activeUsername", activeUsername);
        JsonObject accs = new JsonObject();
        for (Map.Entry<String, AccountCosmetics> e : accountCosmeticsMap.entrySet()) {
            JsonObject obj = new JsonObject();
            AccountCosmetics c = e.getValue();
            obj.addProperty("skinId", c.skinId != null ? c.skinId : "");
            obj.addProperty("capeId", c.capeId != null ? c.capeId : "");
            obj.addProperty("slimModel", c.slimModel);
            obj.addProperty("skinOverride", c.skinOverride);
            obj.addProperty("capeOverride", c.capeOverride);
            accs.add(e.getKey(), obj);
        }
        root.add("accounts", accs);
        ConfigManager.writeAtomically(selectedFile, root);
    }

    public List<SkinEntry> getSkins() { return Collections.unmodifiableList(new ArrayList<>(skinLibrary.values())); }
    public List<SkinEntry> getCapes() { return Collections.unmodifiableList(new ArrayList<>(capeLibrary.values())); }

    public SkinEntry getSelectedSkin() {
        String id = getActiveAccountCosmetics().skinId;
        if (id != null && !id.isEmpty() && skinLibrary.containsKey(id)) {
            return skinLibrary.get(id);
        }
        return null;
    }

    public SkinEntry getSelectedCape() {
        String id = getActiveAccountCosmetics().capeId;
        if (id != null && !id.isEmpty() && capeLibrary.containsKey(id)) {
            return capeLibrary.get(id);
        }
        return null;
    }

    public void setSelectedSkinId(String id) {
        AccountCosmetics cos = getActiveAccountCosmetics();
        cos.skinId = id != null ? id : "";
        SkinEntry s = getSelectedSkin();
        if (s != null) cos.slimModel = s.isSlimModel();
        saveSelection();
    }

    public void setSelectedCapeId(String id) {
        AccountCosmetics cos = getActiveAccountCosmetics();
        cos.capeId = id != null ? id : "";
        saveSelection();
    }

    public boolean isSelectedSlimModel() { return getActiveAccountCosmetics().slimModel; }
    public void setSelectedSlimModel(boolean slim) {
        AccountCosmetics cos = getActiveAccountCosmetics();
        cos.slimModel = slim;
        SkinEntry s = getSelectedSkin();
        if (s != null) s.setSlimModel(slim);
        saveSelection();
    }

    public Path getSkinsDir() { return skinsDir; }
    public Path getCapesDir() { return capesDir; }
}
