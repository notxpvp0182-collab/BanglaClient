package com.ahmed.banglaclient.cosmetics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.Address;
import net.minecraft.client.network.AllowedAddressResolver;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced Server IP & SRV Bypass Geolocation Engine:
 * - Bypasses _minecraft._tcp SRV records via Minecraft's AllowedAddressResolver AND Google DNS-over-HTTPS (DoH)
 * - Queries 3 GeoIP APIs (ip-api.com, ipwho.is, freeipapi.com) to guarantee country detection
 * - Downloads & caches official Full-HD country flags from flagcdn.com
 * - Renders a sleek Question-Mark [?] Flag when a server is unreachable or still resolving
 */
public final class ServerFlagManager {
    public record CountryData(String countryCode, String countryName) {}

    private static final CountryData UNKNOWN_COUNTRY = new CountryData("unknown", "Unknown Location");
    private static final Map<String, CountryData> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Identifier> FLAG_TEXTURES = new ConcurrentHashMap<>();
    private static final Set<String> PENDING = ConcurrentHashMap.newKeySet();
    private static final Set<String> PENDING_FLAGS = ConcurrentHashMap.newKeySet();
    private static final Path FLAGS_DIR = FabricLoader.getInstance().getGameDir().resolve("banglaclient").resolve("flags");

    private ServerFlagManager() {}

    public static CountryData getOrResolveCountry(String rawAddress) {
        if (rawAddress == null || rawAddress.isBlank()) return UNKNOWN_COUNTRY;
        String key = extractHost(rawAddress);

        CountryData cached = CACHE.get(key);
        if (cached != null) {
            if (!"unknown".equals(cached.countryCode())) {
                ensureRealFlagTextureLoaded(cached.countryCode());
            }
            return cached;
        }

        if (PENDING.add(key)) {
            CompletableFuture.runAsync(() -> {
                try {
                    String cleanHost = extractHost(key);
                    if (cleanHost.contains("bangla") || cleanHost.endsWith(".bd") || cleanHost.equals("localhost") || cleanHost.equals("127.0.0.1")) {
                        CountryData bd = new CountryData("bd", "Bangladesh");
                        CACHE.put(key, bd);
                        ensureRealFlagTextureLoaded("bd");
                        return;
                    }

                    // Step 1: Bypass SRV / Proxy / Subdomain to find real IP and target hostname
                    String[] resolved = resolveServerIpWithSrvBypass(key, cleanHost);
                    String ip = resolved[0];
                    String targetHost = resolved[1];

                    // Step 2: Query Multi-API Geolocation for the resolved IP
                    CountryData geo = null;
                    if (ip != null && !ip.isEmpty()) {
                        geo = queryMultiGeoIp(ip, targetHost);
                    }

                    // Step 3: Fallback to target hostname region hints if IP geo failed
                    if (geo == null) {
                        geo = inferCountryFromHostname(targetHost);
                    }

                    if (geo != null && !"unknown".equals(geo.countryCode())) {
                        CACHE.put(key, geo);
                        ensureRealFlagTextureLoaded(geo.countryCode());
                    } else {
                        CACHE.put(key, UNKNOWN_COUNTRY);
                    }
                } catch (Throwable t) {
                    CACHE.put(key, UNKNOWN_COUNTRY);
                } finally {
                    PENDING.remove(key);
                }
            });
        }
        return UNKNOWN_COUNTRY;
    }

    /**
     * Bypasses Minecraft SRV records (_minecraft._tcp), CNAMEs, and subdomain proxies to extract the real IP.
     */
    private static String[] resolveServerIpWithSrvBypass(String rawAddress, String cleanHost) {
        String targetHost = cleanHost;

        // 1A: Try Minecraft's native AllowedAddressResolver (resolves _minecraft._tcp SRV records)
        try {
            ServerAddress parsed = ServerAddress.parse(rawAddress);
            Optional<Address> resolved = AllowedAddressResolver.DEFAULT.resolve(parsed);
            if (resolved.isPresent()) {
                var inet = resolved.get().getInetSocketAddress();
                if (inet != null && inet.getAddress() != null) {
                    String hostName = inet.getHostName();
                    if (hostName != null && !hostName.isEmpty()) targetHost = hostName.toLowerCase(Locale.ROOT);
                    return new String[]{inet.getAddress().getHostAddress(), targetHost};
                }
            }
        } catch (Throwable ignored) {}

        // 1B: Query Google DNS-over-HTTPS for _minecraft._tcp.<host> SRV record (works on all Android/Pojav networks!)
        try {
            String srvTarget = queryGoogleDoHSrv(cleanHost);
            if (srvTarget != null && !srvTarget.isEmpty()) {
                targetHost = srvTarget.toLowerCase(Locale.ROOT);
            }
        } catch (Throwable ignored) {}

        // 1C: Resolve targetHost via standard DNS or Google DoH A-record
        try {
            String ip = InetAddress.getByName(targetHost).getHostAddress();
            if (ip != null && !ip.isEmpty()) return new String[]{ip, targetHost};
        } catch (Throwable ignored) {}

        try {
            String dohIp = queryGoogleDoHARecord(targetHost);
            if (dohIp != null && !dohIp.isEmpty()) return new String[]{dohIp, targetHost};
        } catch (Throwable ignored) {}

        // 1D: Strip leading subdomain (e.g. play.gamertee.net -> gamertee.net) and resolve root domain IP
        int firstDot = cleanHost.indexOf('.');
        int lastDot = cleanHost.lastIndexOf('.');
        if (firstDot > 0 && firstDot < lastDot) {
            String rootDomain = cleanHost.substring(firstDot + 1);
            try {
                String rootSrv = queryGoogleDoHSrv(rootDomain);
                if (rootSrv != null && !rootSrv.isEmpty()) {
                    String ip = queryGoogleDoHARecord(rootSrv);
                    if (ip != null) return new String[]{ip, rootSrv};
                }
                String ip = InetAddress.getByName(rootDomain).getHostAddress();
                if (ip != null) return new String[]{ip, rootDomain};
            } catch (Throwable ignored) {}
            try {
                String ip = queryGoogleDoHARecord(rootDomain);
                if (ip != null) return new String[]{ip, rootDomain};
            } catch (Throwable ignored) {}
        }

        return new String[]{null, targetHost};
    }

    private static String queryGoogleDoHSrv(String host) {
        try {
            JsonObject json = fetchJson("https://dns.google/resolve?name=_minecraft._tcp." + host + "&type=SRV");
            if (json != null && json.has("Answer") && json.get("Answer").isJsonArray()) {
                JsonArray ans = json.getAsJsonArray("Answer");
                for (JsonElement el : ans) {
                    JsonObject rec = el.getAsJsonObject();
                    if (rec.has("data")) {
                        String data = rec.get("data").getAsString().trim();
                        String[] parts = data.split("\\s+");
                        if (parts.length >= 4) {
                            String target = parts[3];
                            if (target.endsWith(".")) target = target.substring(0, target.length() - 1);
                            return target;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static String queryGoogleDoHARecord(String host) {
        try {
            JsonObject json = fetchJson("https://dns.google/resolve?name=" + host + "&type=A");
            if (json != null && json.has("Answer") && json.get("Answer").isJsonArray()) {
                JsonArray ans = json.getAsJsonArray("Answer");
                for (JsonElement el : ans) {
                    JsonObject rec = el.getAsJsonObject();
                    if (rec.has("type") && rec.get("type").getAsInt() == 1 && rec.has("data")) {
                        return rec.get("data").getAsString().trim();
                    }
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static CountryData queryMultiGeoIp(String ip, String targetHost) {
        // Provider 1: ip-api.com
        try {
            JsonObject obj = fetchJson("http://ip-api.com/json/" + ip + "?fields=status,country,countryCode,org,isp");
            if (obj != null && obj.has("countryCode")) {
                String cc = obj.get("countryCode").getAsString().toLowerCase(Locale.ROOT);
                String cn = obj.has("country") ? obj.get("country").getAsString() : cc.toUpperCase(Locale.ROOT);
                String org = (obj.has("org") ? obj.get("org").getAsString() : "") + " " + (obj.has("isp") ? obj.get("isp").getAsString() : "");
                if ((org.toLowerCase(Locale.ROOT).contains("cloudflare") || org.toLowerCase(Locale.ROOT).contains("tcpshield")) && "us".equals(cc)) {
                    CountryData hint = inferCountryFromHostname(targetHost);
                    if (hint != null) return hint;
                }
                if (!cc.isEmpty()) return new CountryData(cc, cn);
            }
        } catch (Throwable ignored) {}

        // Provider 2: ipwho.is (HTTPS fallback)
        try {
            JsonObject obj = fetchJson("https://ipwho.is/" + ip);
            if (obj != null && obj.has("country_code")) {
                String cc = obj.get("country_code").getAsString().toLowerCase(Locale.ROOT);
                String cn = obj.has("country") ? obj.get("country").getAsString() : cc.toUpperCase(Locale.ROOT);
                if (!cc.isEmpty()) return new CountryData(cc, cn);
            }
        } catch (Throwable ignored) {}

        // Provider 3: freeipapi.com (HTTPS fallback)
        try {
            JsonObject obj = fetchJson("https://freeipapi.com/api/json/" + ip);
            if (obj != null && obj.has("countryCode")) {
                String cc = obj.get("countryCode").getAsString().toLowerCase(Locale.ROOT);
                String cn = obj.has("countryName") ? obj.get("countryName").getAsString() : cc.toUpperCase(Locale.ROOT);
                if (!cc.isEmpty()) return new CountryData(cc, cn);
            }
        } catch (Throwable ignored) {}

        return null;
    }

    private static CountryData inferCountryFromHostname(String host) {
        if (host == null) return null;
        String h = host.toLowerCase(Locale.ROOT);
        if (h.contains("bangla") || h.endsWith(".bd") || h.contains(".bd.")) return new CountryData("bd", "Bangladesh");
        if (h.contains("india") || h.contains("mumbai") || h.endsWith(".in") || h.contains(".in.")) return new CountryData("in", "India");
        if (h.contains("singapore") || h.endsWith(".sg") || h.contains(".sg.") || h.contains("asia")) return new CountryData("sg", "Singapore");
        if (h.endsWith(".de") || h.contains(".de.") || h.contains("falkenstein") || h.contains("frankfurt")) return new CountryData("de", "Germany");
        if (h.endsWith(".uk") || h.endsWith(".co.uk") || h.contains("london")) return new CountryData("gb", "United Kingdom");
        if (h.endsWith(".fr") || h.contains("paris") || h.contains("grav")) return new CountryData("fr", "France");
        if (h.endsWith(".jp") || h.contains("tokyo")) return new CountryData("jp", "Japan");
        if (h.endsWith(".au") || h.contains("sydney")) return new CountryData("au", "Australia");
        return null;
    }

    private static JsonObject fetchJson(String urlStr) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setConnectTimeout(3500);
            conn.setReadTimeout(3500);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            if (conn.getResponseCode() == 200) {
                try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                    return JsonParser.parseReader(reader).getAsJsonObject();
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static void ensureRealFlagTextureLoaded(String countryCodeLower) {
        if (countryCodeLower == null || countryCodeLower.isEmpty() || "unknown".equals(countryCodeLower)) return;
        String cc = countryCodeLower.toLowerCase(Locale.ROOT);
        if (FLAG_TEXTURES.containsKey(cc) || !PENDING_FLAGS.add(cc)) return;

        CompletableFuture.runAsync(() -> {
            try {
                Files.createDirectories(FLAGS_DIR);
                Path flagFile = FLAGS_DIR.resolve(cc + ".png");
                if (!Files.exists(flagFile) || Files.size(flagFile) < 50) {
                    HttpURLConnection conn = (HttpURLConnection) new URL("https://flagcdn.com/w80/" + cc + ".png").openConnection();
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

    public static void registerResolvedServerIp(String hostname, String ipAddress) {
        if (hostname == null || ipAddress == null || hostname.isBlank() || ipAddress.isBlank()) return;
        String clean = extractHost(hostname);
        if (clean.isEmpty()) return;
        CountryData existing = CACHE.get(clean);
        if (existing != null && !"unknown".equals(existing.countryCode())) return;
        CompletableFuture.runAsync(() -> {
            CountryData geo = queryMultiGeoIp(ipAddress, clean);
            if (geo != null && !"unknown".equals(geo.countryCode())) {
                CACHE.put(clean, geo);
                ensureRealFlagTextureLoaded(geo.countryCode());
            }
        });
    }

    private static String extractHost(String raw) {
        String s = raw.trim().toLowerCase(Locale.ROOT);
        int colon = s.indexOf(':');
        return colon > 0 ? s.substring(0, colon) : s;
    }

    /**
     * Draws the official Full-HD rectangular Country Flag PNG at (x, y),
     * OR draws a sleek Question-Mark [?] Flag when the server's IP cannot be detected or is still resolving!
     */
    public static void drawCountryFlag(DrawContext c, int x, int y, String countryCode) {
        if (c == null) return;
        int w = 14, h = 9;
        

        if (countryCode != null && !countryCode.isEmpty() && !"unknown".equalsIgnoreCase(countryCode)) {
            String cc = countryCode.toLowerCase(Locale.ROOT);
            ensureRealFlagTextureLoaded(cc);
            Identifier texId = FLAG_TEXTURES.get(cc);
            if (texId != null) {
                try {
                    c.drawTexture(RenderPipelines.GUI_TEXTURED, texId, x, y, 0.0f, 0.0f, w, h, w, h);
                    return;
                } catch (Throwable ignored) {}
            }
        }

        // Fallback: Sleek Question-Mark [?] Flag for undetected / offline / resolving servers
        c.fill(x, y, x + w, y + h, 0xFF161922);
        c.fill(x, y, x + w, y + 1, 0x55FFFFFF);
        c.fill(x, y + h - 1, x + w, y + h, 0x33FFFFFF);
        c.fill(x, y, x + 1, y + h, 0x44FFFFFF);
        c.fill(x + w - 1, y, x + w, y + h, 0x44FFFFFF);

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.textRenderer != null) {
            Matrix3x2fStack m = c.getMatrices();
            m.pushMatrix();
            m.translate(x + 4.5f, y + 1.0f);
            m.scale(0.85f, 0.85f);
            c.drawTextWithShadow(mc.textRenderer, "?", 0, 0, 0xFFE2E8F0);
            m.popMatrix();
        }
    }
}
