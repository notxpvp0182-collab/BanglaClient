package com.ahmed.banglaclient.livestream.ui.font;

import java.text.Bidi;
import java.text.Normalizer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WorldLanguageShaper {
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>(512);

    private WorldLanguageShaper() {}

    public static String shape(String input) {
        if (input == null || input.isEmpty()) return "";
        if (CACHE.size() > 1024) CACHE.clear();
        return CACHE.computeIfAbsent(input, WorldLanguageShaper::shapeInternal);
    }

    private static String shapeInternal(String rawText) {
        String text = Normalizer.normalize(rawText, Normalizer.Form.NFC)
            .replace("\u09AF\u09BC", "\u09DF")
            .replace("\u09A1\u09BC", "\u09DC")
            .replace("\u09A2\u09BC", "\u09DD")
            .replace("\u200C", "")
            .replace("\u200D", "");

        boolean hasBengali = false;
        boolean hasDevanagari = false;
        boolean hasRtl = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '\u0980' && c <= '\u09FF') hasBengali = true;
            else if (c >= '\u0900' && c <= '\u097F') hasDevanagari = true;
            else if ((c >= '\u0590' && c <= '\u06FF') || (c >= '\u0750' && c <= '\u08FF')) hasRtl = true;
        }

        String out = text;
        if (hasBengali) out = shapeBengali(out);
        if (hasDevanagari) out = shapeDevanagari(out);
        if (hasRtl) out = shapeRtl(out);
        return out;
    }

    private static String shapeBengali(String s) {
        StringBuilder pre = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\u09B0' && i + 2 < s.length() && s.charAt(i + 1) == '\u09CD' && isBengaliConsonant(s.charAt(i + 2))) {
                pre.append(s.charAt(i + 2)).append('\u09CD');
                i += 2;
            } else {
                pre.append(c);
            }
        }

        String work = pre.toString();
        StringBuilder sb = new StringBuilder(work.length() + 4);
        int n = work.length();
        int i = 0;
        while (i < n) {
            char c = work.charAt(i);
            if (isBengaliConsonant(c)) {
                int clusterStart = i;
                int j = i + 1;
                while (j < n) {
                    char next = work.charAt(j);
                    if (next == '\u09BC') {
                        j++;
                    } else if (next == '\u09CD' && j + 1 < n && isBengaliConsonant(work.charAt(j + 1))) {
                        j += 2;
                    } else {
                        break;
                    }
                }
                String cluster = work.substring(clusterStart, j);
                if (j < n) {
                    char matra = work.charAt(j);
                    if (matra == '\u09BF' || matra == '\u09C7' || matra == '\u09C8') {
                        sb.append(matra).append(cluster);
                        i = j + 1;
                        continue;
                    } else if (matra == '\u09CB') {
                        sb.append('\u09C7').append(cluster).append('\u09BE');
                        i = j + 1;
                        continue;
                    } else if (matra == '\u09CC') {
                        sb.append('\u09C7').append(cluster).append('\u09D7');
                        i = j + 1;
                        continue;
                    }
                }
                sb.append(cluster);
                i = j;
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    private static String shapeDevanagari(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        int n = s.length();
        int i = 0;
        while (i < n) {
            char c = s.charAt(i);
            if (c >= '\u0915' && c <= '\u0939') {
                int start = i;
                int j = i + 1;
                while (j + 1 < n && s.charAt(j) == '\u094D' && s.charAt(j + 1) >= '\u0915' && s.charAt(j + 1) <= '\u0939') {
                    j += 2;
                }
                String cluster = s.substring(start, j);
                if (j < n && s.charAt(j) == '\u093F') {
                    sb.append('\u093F').append(cluster);
                    i = j + 1;
                    continue;
                }
                sb.append(cluster);
                i = j;
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    private static String shapeRtl(String s) {
        try {
            Bidi bidi = new Bidi(s, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
            if (!bidi.isLeftToRight()) {
                return new StringBuilder(s).reverse().toString();
            }
        } catch (Exception ignored) {}
        return s;
    }

    private static boolean isBengaliConsonant(char c) {
        return (c >= '\u0995' && c <= '\u09B9') || (c >= '\u09DC' && c <= '\u09DF') || c == '\u09CE';
    }
}
