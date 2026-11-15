package com.ahmed.banglaclient.cosmetics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves any Multiplayer Server IP to its country and downloads/caches the OFFICIAL Full-HD Country Flag PNG
 * from flagcdn.com (supporting all 250+ countries in the world!).
 */
public final class ServerFlagManager {
    public record CountryData(String countryCode, String countryName) {}

    private static final Map<String, CountryData> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Identifier> FLAG_TEXTURES = new ConcurrentHashMap<>();
    private static final Set<String> PENDING = ConcurrentHashMap.newKeySet();
    private static final Set<String> PENDING_FLAGS = ConcurrentHashMap.newKeySet();
    private static final Path FLAGS_DIR = FabricLoader.getInstance().getGameDir().resolve("banglaclient").resolve("flags");

    private ServerFlagManager() {}

    public static CountryData getOrResolveCountry(String rawAddress) {
        if (rawAddress == null || rawAddress.isBlank()) return null;
        String cleanHost = extractHost(rawAddress);
        if (cleanHost.isEmpty()) return null;

        CountryData cached = CACHE.get(cleanHost);
        if (cached != null) {
            ensureRealFlagTextureLoaded(cached.countryCode());
            return cached;
        }

        if (PENDING.add(cleanHost)) {
            CompletableFuture.runAsync(() -> {
                try {
                    if (cleanHost.contains("bangla") || cleanHost.endsWith(".bd") || cleanHost.equalsIgnoreCase("localhost") || cleanHost.equals("127.0.0.1")) {
                        CountryData bd = new CountryData("bd", "Bangladesh");
                        CACHE.put(cleanHost, bd);
                        ensureRealFlagTextureLoaded("bd");
                        return;
                    }
                    String ip = InetAddress.getByName(cleanHost).getHostAddress();
                    URL url = new URL("http://ip-api.com/json/" + ip + "?fields=status,country,countryCode");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(4000);
                    conn.setReadTimeout(4000);
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    if (conn.getResponseCode() == 200) {
                        try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                            if (obj.has("countryCode")) {
                                String cc = obj.get("countryCode").getAsString().toLowerCase(Locale.ROOT);
                                String cn = obj.has("country") ? obj.get("country").getAsString() : cc.toUpperCase(Locale.ROOT);
                                CACHE.put(cleanHost, new CountryData(cc, cn));
                                ensureRealFlagTextureLoaded(cc);
                                return;
                            }
                        }
                    }
                } catch (Throwable ignored) {
                } finally {
                    PENDING.remove(cleanHost);
                }
            });
        }
        return null;
    }

    private static void ensureRealFlagTextureLoaded(String countryCodeLower) {
        if (countryCodeLower == null || countryCodeLower.isEmpty()) return;
        String cc = countryCodeLower.toLowerCase(Locale.ROOT);
        if (FLAG_TEXTURES.containsKey(cc) || !PENDING_FLAGS.add(cc)) return;

        CompletableFuture.runAsync(() -> {
            try {
                Files.createDirectories(FLAGS_DIR);
                Path flagFile = FLAGS_DIR.resolve(cc + ".png");
                if (!Files.exists(flagFile) || Files.size(flagFile) < 50) {
                    URL flagUrl = new URL("https://flagcdn.com/w80/" + cc + ".png");
                    HttpURLConnection conn = (HttpURLConnection) flagUrl.openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    conn.setConnectTimeout(4000);
                    conn.setReadTimeout(5000);
                    if (conn.getResponseCode() == 200) {
                        try (InputStream in = conn.getInputStream()) {
                            Files.copy(in, flagFile, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
                if (Files.exists(flagFile) && Files.size(flagFile) > 50) {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    if (mc != null) {
                        mc.execute(() -> {
                            try (InputStream in = Files.newInputStream(flagFile)) {
                                NativeImage img = NativeImage.read(in);
                                Identifier texId = Identifier.of("banglaclient", "flags/" + cc);
                                NativeImageBackedTexture tex = new NativeImageBackedTexture(() -> "flag_" + cc, img);
                                mc.getTextureManager().registerTexture(texId, tex);
                                FLAG_TEXTURES.put(cc, texId);
                            } catch (Throwable ignored) {}
                        });
                    }
                }
            } catch (Throwable ignored) {
            } finally {
                PENDING_FLAGS.remove(cc);
            }
        });
    }

    private static String extractHost(String raw) {
        String s = raw.trim().toLowerCase(Locale.ROOT);
        int colon = s.indexOf(':');
        return colon > 0 ? s.substring(0, colon) : s;
    }

    /**
     * Draws the official Full-HD rectangular Country Flag PNG texture at (x, y).
     */
    public static void drawCountryFlag(DrawContext c, int x, int y, String countryCode) {
        if (c == null || countryCode == null || countryCode.isEmpty()) return;
        String cc = countryCode.toLowerCase(Locale.ROOT);
        ensureRealFlagTextureLoaded(cc);
        Identifier texId = FLAG_TEXTURES.get(cc);
        int w = 14, h = 9;
        if (texId != null) {
            try {
                c.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0x88000000);
                c.drawTexture(RenderPipelines.GUI_TEXTURED, texId, x, y, 0.0f, 0.0f, w, h, w, h);
            } catch (Throwable ignored) {}
        }
    }
}
