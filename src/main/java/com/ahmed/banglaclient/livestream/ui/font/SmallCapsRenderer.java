package com.ahmed.banglaclient.livestream.ui.font;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SmallCapsRenderer {
    private static final char[] SMALL_CAPS_LUT = {
        'ᴀ','ʙ','ᴄ','ᴅ','ᴇ','ꜰ','ɢ','ʜ','ɪ','ᴊ','ᴋ','ʟ','ᴍ',
        'ɴ','ᴏ','ᴘ','ꞯ','ʀ','ꜱ','ᴛ','ᴜ','ᴠ','ᴡ','x','ʏ','ᴢ'
    };
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>(256);

    private SmallCapsRenderer() {}

    public static String toSmallCaps(String input) {
        if (input == null || input.isEmpty()) return "";
        if (CACHE.size() > 1024) CACHE.clear();
        return CACHE.computeIfAbsent(input, s -> {
            StringBuilder sb = new StringBuilder(s.length());
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c >= 'a' && c <= 'z') {
                    sb.append(SMALL_CAPS_LUT[c - 'a']);
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        });
    }
}
