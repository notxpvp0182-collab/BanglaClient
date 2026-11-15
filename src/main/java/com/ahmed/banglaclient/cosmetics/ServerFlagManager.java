package com.ahmed.banglaclient.cosmetics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.DrawContext;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves Multiplayer Server IP addresses asynchronously to their Country Code (BD, IN, SG, US, DE, GB, etc.)
 * and renders crisp pixel-accurate Country Flags in the Multiplayer Server List.
 */
public final class ServerFlagManager {
    public record CountryData(String countryCode, String countryName) {}

    private static final Map<String, CountryData> CACHE = new ConcurrentHashMap<>();
    private static final Set<String> PENDING = ConcurrentHashMap.newKeySet();

    private ServerFlagManager() {}

    public static CountryData getOrResolveCountry(String rawAddress) {
        if (rawAddress == null || rawAddress.isBlank()) return null;
        String cleanHost = extractHost(rawAddress);
        if (cleanHost.isEmpty()) return null;

        CountryData cached = CACHE.get(cleanHost);
        if (cached != null) return cached;

        if (PENDING.add(cleanHost)) {
            CompletableFuture.runAsync(() -> {
                try {
                    if (cleanHost.equalsIgnoreCase("localhost") || cleanHost.equals("127.0.0.1")) {
                        CACHE.put(cleanHost, new CountryData("BD", "Localhost"));
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
                                String cc = obj.get("countryCode").getAsString().toUpperCase(Locale.ROOT);
                                String cn = obj.has("country") ? obj.get("country").getAsString() : cc;
                                CACHE.put(cleanHost, new CountryData(cc, cn));
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

    private static String extractHost(String raw) {
        String s = raw.trim().toLowerCase(Locale.ROOT);
        int colon = s.indexOf(':');
        return colon > 0 ? s.substring(0, colon) : s;
    }

    /**
     * Draws a crisp 13x9 Country Flag at (x, y) for the resolved ISO country code.
     */
    public static void drawCountryFlag(DrawContext c, int x, int y, String countryCode) {
        if (c == null || countryCode == null || countryCode.isEmpty()) return;
        int w = 13, h = 9;
        // Subtle 1px dark frame behind flag
        c.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xAA000000);

        switch (countryCode.toUpperCase(Locale.ROOT)) {
            case "BD" -> { // Bangladesh: Dark Green field + Red Circle
                c.fill(x, y, x + w, y + h, 0xFF006A4E);
                c.fill(x + 4, y + 2, x + 8, y + 7, 0xFFF42A41);
                c.fill(x + 3, y + 3, x + 9, y + 6, 0xFFF42A41);
            }
            case "IN" -> { // India: Saffron top, White middle + Navy Chakra, Green bottom
                c.fill(x, y, x + w, y + 3, 0xFFFF9933);
                c.fill(x, y + 3, x + w, y + 6, 0xFFFFFFFF);
                c.fill(x, y + 6, x + w, y + h, 0xFF138808);
                c.fill(x + 5, y + 3, x + 8, y + 6, 0xFF000080);
            }
            case "SG" -> { // Singapore: Red top with white crescent, White bottom
                c.fill(x, y, x + w, y + 4, 0xFFEF3340);
                c.fill(x, y + 4, x + w, y + h, 0xFFFFFFFF);
                c.fill(x + 2, y + 1, x + 5, y + 3, 0xFFFFFFFF);
            }
            case "US" -> { // USA: Red/white stripes + Blue canton
                for (int row = 0; row < h; row++) {
                    c.fill(x, y + row, x + w, y + row + 1, (row % 2 == 0) ? 0xFFB22234 : 0xFFFFFFFF);
                }
                c.fill(x, y, x + 6, y + 5, 0xFF3C3B6E);
            }
            case "DE" -> { // Germany: Black, Red, Gold
                c.fill(x, y, x + w, y + 3, 0xFF000000);
                c.fill(x, y + 3, x + w, y + 6, 0xFFDD0000);
                c.fill(x, y + 6, x + w, y + h, 0xFFFFCE00);
            }
            case "GB", "UK" -> { // UK: Navy field + White/Red Cross
                c.fill(x, y, x + w, y + h, 0xFF012169);
                c.fill(x + 5, y, x + 8, y + h, 0xFFFFFFFF);
                c.fill(x, y + 3, x + w, y + 6, 0xFFFFFFFF);
                c.fill(x + 6, y, x + 7, y + h, 0xFFC8102E);
                c.fill(x, y + 4, x + w, y + 5, 0xFFC8102E);
            }
            case "FR" -> { // France: Blue, White, Red vertical
                c.fill(x, y, x + 4, y + h, 0xFF0055A4);
                c.fill(x + 4, y, x + 9, y + h, 0xFFFFFFFF);
                c.fill(x + 9, y, x + w, y + h, 0xFFEF4135);
            }
            case "JP" -> { // Japan: White + Red sun
                c.fill(x, y, x + w, y + h, 0xFFFFFFFF);
                c.fill(x + 5, y + 2, x + 8, y + 7, 0xFFBC002D);
                c.fill(x + 4, y + 3, x + 9, y + 6, 0xFFBC002D);
            }
            case "CA" -> { // Canada: Red bars + White center + Red leaf
                c.fill(x, y, x + 3, y + h, 0xFFFF0000);
                c.fill(x + 3, y, x + 10, y + h, 0xFFFFFFFF);
                c.fill(x + 10, y, x + w, y + h, 0xFFFF0000);
                c.fill(x + 5, y + 3, x + 8, y + 6, 0xFFFF0000);
            }
            case "NL" -> { // Netherlands: Red, White, Blue
                c.fill(x, y, x + w, y + 3, 0xFFAE1C28);
                c.fill(x, y + 3, x + w, y + 6, 0xFFFFFFFF);
                c.fill(x, y + 6, x + w, y + h, 0xFF21468B);
            }
            case "RU" -> { // Russia: White, Blue, Red
                c.fill(x, y, x + w, y + 3, 0xFFFFFFFF);
                c.fill(x, y + 3, x + w, y + 6, 0xFF0039A6);
                c.fill(x, y + 6, x + w, y + h, 0xFFD52B1E);
            }
            case "PL" -> { // Poland: White, Red
                c.fill(x, y, x + w, y + 4, 0xFFFFFFFF);
                c.fill(x, y + 4, x + w, y + h, 0xFFDC143C);
            }
            case "BR" -> { // Brazil: Green + Yellow diamond + Blue center
                c.fill(x, y, x + w, y + h, 0xFF009739);
                c.fill(x + 3, y + 2, x + 10, y + 7, 0xFFFEDD00);
                c.fill(x + 5, y + 3, x + 8, y + 6, 0xFF012169);
            }
            default -> {
                int hash = countryCode.hashCode();
                int c1 = 0xFF000000 | ((hash & 0x7F7F7F) + 0x404040);
                int c2 = 0xFF000000 | (((hash >> 8) & 0x7F7F7F) + 0x404040);
                c.fill(x, y, x + w, y + 3, c1);
                c.fill(x, y + 3, x + w, y + 6, 0xFFFFFFFF);
                c.fill(x, y + 6, x + w, y + h, c2);
            }
        }
    }
}
