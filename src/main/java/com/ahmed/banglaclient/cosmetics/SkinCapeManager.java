package com.ahmed.banglaclient.cosmetics;

import com.ahmed.banglaclient.BanglaClient;
import com.ahmed.banglaclient.config.ConfigManager;
import com.ahmed.banglaclient.config.ConfigValidator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;

/**
 * Pure NativeImage-backed Skin & Cape Manager with Automatic Live-Reload on Android & Desktop.
 */
public final class SkinCapeManager {
    private static SkinCapeManager instance;

    public static final class AccountCosmetics {
        public String selectedSkinId = "";
        public String selectedCapeId = "";
        public boolean slimModel = false;
        public boolean customSkinOverride = true;
        public boolean customCapeOverride = true;
    }

    private final Path banglaRootDir;
    private final Path skinsDir;
    private final Path capesDir;
    private final Path selectionFile;

    private final Map<String, SkinEntry> skins = new LinkedHashMap<>();
    private final Map<String, SkinEntry> capes = new LinkedHashMap<>();
    private final Map<String, AccountCosmetics> perAccountMap = new LinkedHashMap<>();

    private String activeUsername = "Player";
    private long lastWatchCheckMs = 0L;
    private String lastFolderSignature = "";

    public static synchronized SkinCapeManager getInstance() {
        if (instance == null) {
            instance = new SkinCapeManager();
        }
        return instance;
    }

    public SkinCapeManager() {
        this.banglaRootDir = FabricLoader.getInstance().getGameDir().resolve("banglaclient");
        this.skinsDir = banglaRootDir.resolve("skins");
        this.capesDir = banglaRootDir.resolve("capes");
        this.selectionFile = banglaRootDir.resolve("selected.json");
        try {
            Files.createDirectories(skinsDir);
            Files.createDirectories(capesDir);
            seedDefaultCapesNative();
            loadSelection();
        } catch (IOException e) {
            BanglaClient.LOGGER.error("Failed to create skin/cape directories", e);
        }
    }

    private void seedDefaultCapesNative() {
        int[][] themes = {
                {0xFF0A0C12, 0xFFEF4444, 0xFFFBBF24},
                {0xFF08090D, 0xFFE2E8F0, 0xFF94A3B8},
                {0xFF14080E, 0xFFF43F5E, 0xFFFFFFFF}
        };
        String[] names = {"Bangla_Crown_Cape.png", "Aura_Obsidian_Cape.png", "Crimson_Royal_Cape.png"};
        for (int i = 0; i < names.length; i++) {
            Path p = capesDir.resolve(names[i]);
            if (!Files.exists(p)) {
                try (NativeImage img = new NativeImage(64, 32, true)) {
                    for (int y = 1; y < 17; y++) {
                        for (int x = 1; x < 21; x++) {
                            boolean border = (x == 1 || x == 20 || y == 1 || y == 16);
                            boolean emblem = (x >= 4 && x <= 8 && y >= 4 && y <= 13);
                            boolean crown = (x >= 5 && x <= 7 && y >= 5 && y <= 7);
                            int col = crown ? themes[i][2] : (border || emblem ? themes[i][1] : themes[i][0]);
                            img.setColorArgb(x, y, col);
                        }
                    }
                    img.writeTo(p);
                } catch (Throwable ignored) {}
            }
        }
    }

    public synchronized void checkAndLiveReload() {
        long now = System.currentTimeMillis();
        if (now - lastWatchCheckMs < 1200L) return;
        lastWatchCheckMs = now;
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.getTextureManager() == null) return;
            StringBuilder sig = new StringBuilder();
            if (Files.exists(skinsDir)) {
                try (var s = Files.list(skinsDir)) {
                    s.sorted().forEach(p -> {
                        try { sig.append(p.getFileName()).append(':').append(Files.size(p)).append(';'); } catch (Throwable ignored) {}
                    });
                }
            }
            sig.append("|");
            if (Files.exists(capesDir)) {
                try (var s = Files.list(capesDir)) {
                    s.sorted().forEach(p -> {
                        try { sig.append(p.getFileName()).append(':').append(Files.size(p)).append(';'); } catch (Throwable ignored) {}
                    });
                }
            }
            String cur = sig.toString();
            if (!cur.equals(lastFolderSignature)) {
                lastFolderSignature = cur;
                refreshLibraries();
                loadSelection();
            }
        } catch (Throwable ignored) {}
    }

    public synchronized void refreshLibraries() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getTextureManager() == null) return;

        for (SkinEntry old : skins.values()) {
            try { mc.getTextureManager().destroyTexture(old.getTextureId()); } catch (Throwable ignored) {}
        }
        for (SkinEntry old : capes.values()) {
            try { mc.getTextureManager().destroyTexture(old.getTextureId()); } catch (Throwable ignored) {}
        }
        skins.clear();
        capes.clear();
        scanDirectory(skinsDir, false, skins);
        scanDirectory(capesDir, true, capes);
    }

    private void scanDirectory(Path dir, boolean isCape, Map<String, SkinEntry> targetMap) {
        if (!Files.exists(dir)) return;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file : stream) {
                if (Files.isDirectory(file)) continue;
                String lower = file.getFileName().toString().toLowerCase(Locale.ROOT);
                if (!lower.endsWith(".png")) continue;
                SkinEntry entry = loadEntryNative(file, isCape);
                if (entry != null) {
                    targetMap.put(entry.getId(), entry);
                }
            }
        } catch (IOException e) {
            BanglaClient.LOGGER.warn("Error scanning cosmetics directory {}", dir, e);
        }
    }

    private SkinEntry loadEntryNative(Path file, boolean asCape) {
        try (InputStream stream = Files.newInputStream(file)) {
            NativeImage raw = NativeImage.read(stream);
            int w = raw.getWidth();
            int h = raw.getHeight();
            if (w < 16 || h < 8) {
                raw.close();
                return null;
            }

            NativeImage finalImage;
            int outW, outH;
            boolean slim = false;

            if (asCape) {
                outW = 64;
                outH = 32;
                if (w == 64 && h == 32) {
                    finalImage = raw;
                } else {
                    finalImage = new NativeImage(64, 32, true);
                    for (int y = 0; y < 32; y++) {
                        for (int x = 0; x < 64; x++) {
                            int sx = (w == 22 && h == 17) ? Math.min(w - 1, x) : (x * w / 64);
                            int sy = (w == 22 && h == 17) ? Math.min(h - 1, y) : (y * h / 32);
                            if (w == 22 && h == 17 && (x >= 22 || y >= 17)) {
                                finalImage.setColorArgb(x, y, 0);
                            } else {
                                finalImage.setColorArgb(x, y, raw.getColorArgb(sx, sy));
                            }
                        }
                    }
                    raw.close();
                }
            } else {
                outW = 64;
                outH = 64;
                if (w == 64 && h == 64) {
                    finalImage = raw;
                } else if (w == 64 && h == 32) {
                    finalImage = new NativeImage(64, 64, true);
                    for (int y = 0; y < 32; y++) {
                        for (int x = 0; x < 64; x++) {
                            finalImage.setColorArgb(x, y, raw.getColorArgb(x, y));
                        }
                    }
                    // Copy right leg & right arm to left leg & left arm for 64x32 legacy skins
                    for (int y = 16; y < 32; y++) {
                        for (int x = 0; x < 16; x++) {
                            finalImage.setColorArgb(x + 16, y + 32, raw.getColorArgb(x, y));
                        }
                        for (int x = 40; x < 56; x++) {
                            finalImage.setColorArgb(x - 8, y + 32, raw.getColorArgb(x, y));
                        }
                    }
                    raw.close();
                } else {
                    finalImage = new NativeImage(64, 64, true);
                    for (int y = 0; y < 64; y++) {
                        for (int x = 0; x < 64; x++) {
                            finalImage.setColorArgb(x, y, raw.getColorArgb(x * w / 64, y * h / 64));
                        }
                    }
                    raw.close();
                }
                slim = isSlimNative(finalImage);
            }

            String title = file.getFileName().toString();
            int dot = title.lastIndexOf('.');
            if (dot > 0) title = title.substring(0, dot);

            String safeId = file.getFileName().toString().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
            Identifier texId = Identifier.of("banglaclient", (asCape ? "capes/" : "skins/") + safeId);

            NativeImageBackedTexture dynTex = new NativeImageBackedTexture(() -> "bc_" + safeId, finalImage);
            MinecraftClient.getInstance().getTextureManager().registerTexture(texId, dynTex);

            return new SkinEntry(file.getFileName().toString(), title, file, outW, outH, slim, asCape, texId);
        } catch (Throwable t) {
            return null;
        }
    }

    private static boolean isSlimNative(NativeImage img) {
        try {
            for (int y = 20; y < 32; y++) {
                if (((img.getColorArgb(54, y) >>> 24) & 0xFF) > 16) return false;
                if (((img.getColorArgb(55, y) >>> 24) & 0xFF) > 16) return false;
            }
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public synchronized String importExternalFile(Path sourcePath, Boolean forceCapeOverride) {
        if (sourcePath == null || !Files.exists(sourcePath)) return "File does not exist.";
        String fileName = sourcePath.getFileName().toString();
        if (!fileName.toLowerCase(Locale.ROOT).endsWith(".png")) return "Only PNG files are supported.";

        try (InputStream in = Files.newInputStream(sourcePath);
             NativeImage probe = NativeImage.read(in)) {
            int w = probe.getWidth(), h = probe.getHeight();
            boolean isCape = forceCapeOverride != null ? forceCapeOverride : (w == h * 2 && h != 32);
            String safeName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path destDir = isCape ? capesDir : skinsDir;
            Files.copy(sourcePath, destDir.resolve(safeName), StandardCopyOption.REPLACE_EXISTING);
            lastFolderSignature = "";
            checkAndLiveReload();
            if (isCape) setSelectedCapeId(safeName);
            else setSelectedSkinId(safeName);
            return "Imported " + (isCape ? "Cape: " : "Skin: ") + safeName;
        } catch (Throwable t) {
            return "Failed to import PNG file.";
        }
    }

    public synchronized boolean removeEntry(SkinEntry entry) {
        if (entry == null) return false;
        try {
            try { MinecraftClient.getInstance().getTextureManager().destroyTexture(entry.getTextureId()); } catch (Throwable ignored) {}
            boolean deleted = Files.deleteIfExists(entry.getFilePath());
            AccountCosmetics active = getActiveAccountCosmetics();
            if (entry.isCape()) {
                capes.remove(entry.getId());
                if (entry.getId().equals(active.selectedCapeId)) active.selectedCapeId = "";
            } else {
                skins.remove(entry.getId());
                if (entry.getId().equals(active.selectedSkinId)) active.selectedSkinId = "";
            }
            saveSelection();
            return deleted;
        } catch (IOException e) {
            return false;
        }
    }

    private AccountCosmetics getActiveAccountCosmetics() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.getSession() != null && mc.getSession().getUsername() != null) {
            this.activeUsername = mc.getSession().getUsername();
        }
        return perAccountMap.computeIfAbsent(this.activeUsername, k -> new AccountCosmetics());
    }

    public void onAccountSwitched(String username) {
        setActiveAccountUsername(username);
    }

    public void setActiveAccountUsername(String username) {
        if (username != null && !username.isBlank()) {
            this.activeUsername = username.trim();
            perAccountMap.computeIfAbsent(this.activeUsername, k -> new AccountCosmetics());
        }
    }

    public List<SkinEntry> getSkins() {
        checkAndLiveReload();
        return Collections.unmodifiableList(new ArrayList<>(skins.values()));
    }

    public List<SkinEntry> getCapes() {
        checkAndLiveReload();
        return Collections.unmodifiableList(new ArrayList<>(capes.values()));
    }

    public SkinEntry getSelectedSkin() {
        checkAndLiveReload();
        AccountCosmetics ac = getActiveAccountCosmetics();
        if (!ac.customSkinOverride || ac.selectedSkinId == null || ac.selectedSkinId.isEmpty()) return null;
        return skins.get(ac.selectedSkinId);
    }

    public SkinEntry getSelectedCape() {
        checkAndLiveReload();
        AccountCosmetics ac = getActiveAccountCosmetics();
        if (!ac.customCapeOverride || ac.selectedCapeId == null || ac.selectedCapeId.isEmpty()) return null;
        return capes.get(ac.selectedCapeId);
    }

    public boolean hasCustomSkinSelected() { return getSelectedSkin() != null; }
    public boolean hasCustomCapeSelected() { return getSelectedCape() != null; }

    public void setSelectedSkinId(String id) {
        AccountCosmetics ac = getActiveAccountCosmetics();
        ac.selectedSkinId = id != null ? id : "";
        SkinEntry entry = skins.get(ac.selectedSkinId);
        if (entry != null) ac.slimModel = entry.isSlimModel();
        saveSelection();
    }

    public void setSelectedCapeId(String id) {
        AccountCosmetics ac = getActiveAccountCosmetics();
        ac.selectedCapeId = id != null ? id : "";
        saveSelection();
    }

    public boolean isSelectedSlimModel() { return getActiveAccountCosmetics().slimModel; }
    public void setSelectedSlimModel(boolean slimModel) { getActiveAccountCosmetics().slimModel = slimModel; saveSelection(); }
    public boolean isCustomSkinOverride() { return getActiveAccountCosmetics().customSkinOverride; }
    public void setCustomSkinOverride(boolean enabled) { getActiveAccountCosmetics().customSkinOverride = enabled; saveSelection(); }
    public boolean isCustomCapeOverride() { return getActiveAccountCosmetics().customCapeOverride; }
    public void setCustomCapeOverride(boolean enabled) { getActiveAccountCosmetics().customCapeOverride = enabled; saveSelection(); }

    public SkinTextures getActiveSkinTextures() {
        checkAndLiveReload();
        MinecraftClient mc = MinecraftClient.getInstance();
        SkinTextures vanillaDefault = getAuthenticatedAccountDefaultSkin(mc);

        SkinEntry skin = getSelectedSkin();
        SkinEntry cape = getSelectedCape();

        AssetInfo.TextureAsset body = (skin != null)
                ? new AssetInfo.TextureAssetInfo(skin.getTextureId(), skin.getTextureId())
                : vanillaDefault.body();
        AssetInfo.TextureAsset capeAsset = (cape != null)
                ? new AssetInfo.TextureAssetInfo(cape.getTextureId(), cape.getTextureId())
                : vanillaDefault.cape();
        PlayerSkinType model = (skin != null)
                ? (isSelectedSlimModel() ? PlayerSkinType.SLIM : PlayerSkinType.WIDE)
                : vanillaDefault.model();

        return new SkinTextures(body, capeAsset, vanillaDefault.elytra(), model, vanillaDefault.secure());
    }

    public static SkinTextures getAuthenticatedAccountDefaultSkin(MinecraftClient client) {
        if (client != null) {
            if (client.player != null && client.getNetworkHandler() != null) {
                PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
                if (entry != null && entry.getSkinTextures() != null) return entry.getSkinTextures();
            }
            if (client.getSession() != null && client.getSession().getUuidOrNull() != null) {
                return DefaultSkinHelper.getSkinTextures(client.getSession().getUuidOrNull());
            }
        }
        return DefaultSkinHelper.getSkinTextures(new UUID(0L, 0L));
    }

    public void loadSelection() {
        JsonObject root = ConfigValidator.readSafeJsonObject(selectionFile);
        if (root.has("accounts") && root.get("accounts").isJsonObject()) {
            JsonObject accObj = root.getAsJsonObject("accounts");
            for (Map.Entry<String, JsonElement> entry : accObj.entrySet()) {
                if (entry.getValue().isJsonObject()) {
                    JsonObject o = entry.getValue().getAsJsonObject();
                    AccountCosmetics ac = new AccountCosmetics();
                    if (o.has("selectedSkin")) ac.selectedSkinId = o.get("selectedSkin").getAsString();
                    if (o.has("selectedCape")) ac.selectedCapeId = o.get("selectedCape").getAsString();
                    if (o.has("slimModel")) ac.slimModel = o.get("slimModel").getAsBoolean();
                    if (o.has("customSkinOverride")) ac.customSkinOverride = o.get("customSkinOverride").getAsBoolean();
                    if (o.has("customCapeOverride")) ac.customCapeOverride = o.get("customCapeOverride").getAsBoolean();
                    perAccountMap.put(entry.getKey(), ac);
                }
            }
        }
    }

    public void saveSelection() {
        JsonObject root = new JsonObject();
        AccountCosmetics current = getActiveAccountCosmetics();
        root.addProperty("selectedSkin", current.selectedSkinId);
        root.addProperty("selectedCape", current.selectedCapeId);
        root.addProperty("slimModel", current.slimModel);

        JsonObject accObj = new JsonObject();
        for (Map.Entry<String, AccountCosmetics> entry : perAccountMap.entrySet()) {
            JsonObject o = new JsonObject();
            AccountCosmetics ac = entry.getValue();
            o.addProperty("selectedSkin", ac.selectedSkinId != null ? ac.selectedSkinId : "");
            o.addProperty("selectedCape", ac.selectedCapeId != null ? ac.selectedCapeId : "");
            o.addProperty("slimModel", ac.slimModel);
            o.addProperty("customSkinOverride", ac.customSkinOverride);
            o.addProperty("customCapeOverride", ac.customCapeOverride);
            accObj.add(entry.getKey(), o);
        }
        root.add("accounts", accObj);
        ConfigManager.writeAtomically(selectionFile, root);
    }

    public Path getSkinsDir() { return skinsDir; }
    public Path getCapesDir() { return capesDir; }
    public Path getBanglaRootDir() { return banglaRootDir; }
}
