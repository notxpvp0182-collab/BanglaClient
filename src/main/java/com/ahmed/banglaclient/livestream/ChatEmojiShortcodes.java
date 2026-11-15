package com.ahmed.banglaclient.livestream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatEmojiShortcodes {
    private static final Pattern SHORTCODE_PAT = Pattern.compile(":([a-zA-Z0-9_\\-]+):");

    private static final Map<String, String> EMOJI_MAP = Map.ofEntries(
        Map.entry("red_heart", "❤"), Map.entry("wilted_flower", "🥀"), Map.entry("wilted_rose", "🥀"), Map.entry("rose", "🌹"),
        Map.entry("smiling_imp", "😈"), Map.entry("imp", "👿"), Map.entry("smiling_face_with_horns", "😈"), Map.entry("Star-Struck", "🤩"),
        Map.entry("heart", "❤"), Map.entry("orange_heart", "🧡"), Map.entry("yellow_heart", "💛"), Map.entry("green_heart", "💚"),
        Map.entry("blue_heart", "💙"), Map.entry("purple_heart", "💜"), Map.entry("black_heart", "🖤"), Map.entry("white_heart", "🤍"),
        Map.entry("broken_heart", "💔"), Map.entry("sparkling_heart", "💖"), Map.entry("two_hearts", "💕"),
        Map.entry("grinning_face", "😀"), Map.entry("Grinning_Face", "😀"), Map.entry("smiley", "😃"), Map.entry("smile", "😄"),
        Map.entry("grin", "😁"), Map.entry("laughing", "😆"), Map.entry("sweat_smile", "😅"), Map.entry("rofl", "🤣"),
        Map.entry("Rolling_on_the_Floor_Laughing", "🤣"), Map.entry("joy", "😂"), Map.entry("Face_with_Tears_of_Joy", "😂"),
        Map.entry("slightly_smiling_face", "🙂"), Map.entry("upside_down_face", "🙃"), Map.entry("wink", "😉"), Map.entry("blush", "😊"),
        Map.entry("innocent", "😇"), Map.entry("smiling_face_with_three_hearts", "🥰"), Map.entry("heart_eyes", "😍"),
        Map.entry("Smiling_Face_with_Heart-Eyes", "😍"), Map.entry("star_struck", "🤩"), Map.entry("face_blowing_a_kiss", "😘"),
        Map.entry("kissing_heart", "😘"), Map.entry("yum", "😋"), Map.entry("stuck_out_tongue", "😛"), Map.entry("zany_face", "🤪"),
        Map.entry("thinking_face", "🤔"), Map.entry("thinking", "🤔"), Map.entry("shushing_face", "🤫"), Map.entry("smirk", "😏"),
        Map.entry("unamused", "😒"), Map.entry("eyeroll", "🙄"), Map.entry("grimacing", "😬"), Map.entry("relieved", "😌"),
        Map.entry("pensive_face", "😔"), Map.entry("pensive", "😔"), Map.entry("sleepy", "😪"), Map.entry("sleeping", "😴"),
        Map.entry("mask", "😷"), Map.entry("sneezing_face", "🤧"), Map.entry("hot_face", "🥵"), Map.entry("cold_face", "🥶"),
        Map.entry("woozy_face", "🥴"), Map.entry("dizzy_face", "😵"), Map.entry("exploding_head", "🤯"), Map.entry("partying_face", "🥳"),
        Map.entry("sunglasses", "😎"), Map.entry("nerd_face", "🤓"), Map.entry("confused", "😕"), Map.entry("worried", "😟"),
        Map.entry("frowning_face", "☹"), Map.entry("open_mouth", "😮"), Map.entry("hushed", "😯"), Map.entry("astonished", "😲"),
        Map.entry("flushed", "😳"), Map.entry("pleading_face", "🥺"), Map.entry("cry", "😢"), Map.entry("Crying_Face", "😢"),
        Map.entry("sob", "😭"), Map.entry("Loudly_Crying_Face", "😭"), Map.entry("scream", "😱"), Map.entry("angry", "😠"),
        Map.entry("rage", "😡"), Map.entry("triumph", "😤"), Map.entry("skull", "💀"), Map.entry("poop", "💩"),
        Map.entry("clown_face", "🤡"), Map.entry("ghost", "👻"), Map.entry("alien", "👽"), Map.entry("robot", "🤖"),
        Map.entry("fire", "🔥"), Map.entry("sparkles", "✨"), Map.entry("star", "⭐"), Map.entry("zap", "⚡"),
        Map.entry("boom", "💥"), Map.entry("100", "💯"), Map.entry("crown", "👑"), Map.entry("trophy", "🏆"),
        Map.entry("thumbs_up", "👍"), Map.entry("thumbsup", "👍"), Map.entry("thumbs_down", "👎"), Map.entry("clap", "👏"),
        Map.entry("Clapping_Hands", "👏"), Map.entry("raised_hands", "🙌"), Map.entry("pray", "🙏"), Map.entry("Folded_Hands", "🙏"),
        Map.entry("wave", "👋"), Map.entry("Waving_Hand", "👋"), Map.entry("ok_hand", "👌"), Map.entry("v", "✌"),
        Map.entry("muscle", "💪"), Map.entry("eyes", "👀"), Map.entry("tada", "🎉"), Map.entry("Party_Popper", "🎉"),
        Map.entry("eyes-purple-crying", "😭"), Map.entry("face-red-heart-shape", "😍"), Map.entry("hand-pink-waving", "👋"),
        Map.entry("face-blue-smiling", "😊"), Map.entry("face-red-droopy-eyes", "🥺"), Map.entry("face-purple-crying", "😢"),
        Map.entry("face-green-smiling", "😁"), Map.entry("face-orange-frowning", "😠"), Map.entry("trophy-yellow-smiling", "🏆"),
        Map.entry("cat-orange-whistling", "🐱"), Map.entry("person-turqouise-waving", "🙋"), Map.entry("face-fuchsia-wide-eyes", "😲"),
        Map.entry("face-turquoise-covering-eyes", "🙈"), Map.entry("goat-turquoise-white-horns", "🐐")
    );

    private ChatEmojiShortcodes() {}

    public static String readRunsWithEmojis(JsonObject obj) {
        if (obj == null) return "";
        if (obj.has("simpleText")) return replaceShortcodes(obj.get("simpleText").getAsString());
        if (!obj.has("runs") || !obj.get("runs").isJsonArray()) return "";
        JsonArray runs = obj.getAsJsonArray("runs");
        StringBuilder sb = new StringBuilder();
        for (JsonElement e : runs) {
            JsonObject r = e.getAsJsonObject();
            if (r.has("text")) {
                sb.append(r.get("text").getAsString());
            } else if (r.has("emoji") && r.get("emoji").isJsonObject()) {
                JsonObject em = r.getAsJsonObject("emoji");
                boolean isCustom = em.has("isCustomEmoji") && em.get("isCustomEmoji").getAsBoolean();
                String emojiId = em.has("emojiId") ? em.get("emojiId").getAsString() : "";
                if (!isCustom && !emojiId.isEmpty() && emojiId.length() <= 8) {
                    sb.append(emojiId);
                } else if (em.has("shortcuts") && em.get("shortcuts").isJsonArray()) {
                    JsonArray sc = em.getAsJsonArray("shortcuts");
                    if (!sc.isEmpty()) {
                        sb.append(replaceShortcodes(sc.get(0).getAsString()));
                    } else {
                        sb.append("✨");
                    }
                } else {
                    sb.append("✨");
                }
            }
        }
        return replaceShortcodes(sb.toString());
    }

    public static String replaceShortcodes(String input) {
        if (input == null || input.isEmpty() || !input.contains(":")) return input != null ? input : "";
        Matcher m = SHORTCODE_PAT.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String code = m.group(1);
            String mapped = EMOJI_MAP.get(code);
            if (mapped == null) mapped = EMOJI_MAP.get(code.toLowerCase());
            if (mapped == null) {
                if (code.contains("heart")) mapped = "❤";
                else if (code.contains("cry") || code.contains("sob") || code.contains("tear")) mapped = "😭";
                else if (code.contains("laugh") || code.contains("joy") || code.contains("rofl")) mapped = "😂";
                else if (code.contains("smile") || code.contains("grin") || code.contains("happy")) mapped = "😀";
                else if (code.contains("wave") || code.contains("hand")) mapped = "👋";
                else if (code.contains("fire") || code.contains("lit")) mapped = "🔥";
                else mapped = "✨";
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(mapped));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
