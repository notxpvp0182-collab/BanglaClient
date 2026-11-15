package com.ahmed.banglaclient.livestream;

import com.ahmed.banglaclient.config.ConfigManager;
import com.ahmed.banglaclient.config.ConfigValidator;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Built-in BANGLA CLIENT YouTube LiveStream & Live Chat Engine:
 * - Keyless YouTube InnerTube live stream & chat polling
 * - Smooth Chat Delay pacing from 0.0s to 5.0s (even at 0.0s, messages release smoothly one-by-one with animation instead of dumping all at once!)
 * - 5 Selectable Chat Animation Templates
 */
public final class LiveStreamManager {
    public enum ConnectionState {
        DISCONNECTED, CONNECTING, LIVE, OFFLINE, RECONNECTING, ERROR
    }

    public enum ChatAnimationTemplate {
        SLIDE_UP("Slide Up"),
        SLIDE_DOWN("Slide Down"),
        SLIDE_LEFT("Slide Left"),
        SCALE_POP("Scale Pop"),
        SMOOTH_FADE("Smooth Fade");

        private final String label;
        ChatAnimationTemplate(String label) { this.label = label; }
        public String getLabel() { return label; }
        public ChatAnimationTemplate next() {
            ChatAnimationTemplate[] vals = values();
            return vals[(ordinal() + 1) % vals.length];
        }
    }

    public record StreamSnapshot(
            ConnectionState state,
            String streamId,
            String channelName,
            String channelHandle,
            Long subscriberCount,
            String streamTitle,
            Long viewerCount,
            Long likeCount,
            long streamStartEpochMs,
            String statusMessage
    ) {
        public static StreamSnapshot disconnected() {
            return new StreamSnapshot(ConnectionState.DISCONNECTED, "", "--", "--", null, "--", null, null, 0L, "Not connected");
        }
    }

    public record ChatMessage(
            String id,
            String author,
            String content,
            long timestampMs,
            long releasedNanoTime,
            boolean owner,
            boolean moderator,
            boolean member
    ) {}

    private static final LiveStreamManager INSTANCE = new LiveStreamManager();
    private static final Pattern YT_URL_PATTERN = Pattern.compile("(?:youtube\\.com/(?:watch\\?(?:[^#\\s]*&)?v=|live/|embed/)|youtu\\.be/)([A-Za-z0-9_-]{11})");
    private static final Pattern BARE_ID = Pattern.compile("^[A-Za-z0-9_-]{11}$");
    private static final Pattern API_KEY_PAT = Pattern.compile("\"INNERTUBE_API_KEY\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern CLIENT_VER_PAT = Pattern.compile("\"INNERTUBE_CLIENT_VERSION\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("([0-9]+(?:[.,][0-9]+)?)\\s*([KMBkmb])?");
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    private final AtomicReference<StreamSnapshot> snapshotRef = new AtomicReference<>(StreamSnapshot.disconnected());
    private final Deque<ChatMessage> chatBuffer = new ConcurrentLinkedDeque<>();
    private final Deque<ChatMessage> pendingDelayQueue = new ConcurrentLinkedDeque<>();
    private final Set<String> seenIds = ConcurrentHashMap.newKeySet();
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).followRedirects(HttpClient.Redirect.NORMAL).build();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "BanglaClient-LiveStream");
        t.setDaemon(true);
        return t;
    });

    private volatile String streamUrl = "";
    private volatile boolean autoConnect = false;
    private volatile int chatDelayMs = 0; // 0 ms to 5000 ms (0.0s to 5.0s)
    private volatile ChatAnimationTemplate animationTemplate = ChatAnimationTemplate.SLIDE_UP;
    private volatile long lastPacedReleaseMs = 0L;
    private volatile long lastReleaseNanoTime = System.nanoTime();
    private volatile int scrollLineOffset = 0;

    // Chat customization flags
    public boolean showYouTubeIcon = true;
    public boolean showUsername = true;
    public boolean showTimestamp = false;
    public boolean coloredUsernames = true;
    public boolean coloredMessages = false;
    public boolean rgbMode = false;
    public boolean newestAtTop = false;
    public boolean chatShadow = false;
    public boolean textShadow = true;

    private ScheduledFuture<?> videoTask;
    private ScheduledFuture<?> chatTask;
    private volatile String cachedInnertubeKey = "";
    private volatile String cachedClientVer = "2.20250214.01.00";
    private volatile String continuationToken = null;

    private LiveStreamManager() {
        loadConfig();
    }

    public synchronized void loadConfig() {
        try {
            Path cfgDir = FabricLoader.getInstance().getConfigDir();
            Path bcDir = cfgDir.resolve("banglaclient");
            Files.createDirectories(bcDir);
            Path primary = cfgDir.resolve("livestream.json");
            Path secondary = bcDir.resolve("livestream.json");
            Path target = Files.exists(primary) ? primary : secondary;
            if (!Files.exists(target)) {
                saveConfig();
                return;
            }
            JsonObject root = ConfigValidator.readSafeJsonObject(target);
            if (root.has("youtubeUrl")) this.streamUrl = root.get("youtubeUrl").getAsString();
            if (root.has("autoConnect")) this.autoConnect = root.get("autoConnect").getAsBoolean();
            if (root.has("chatDelayMs")) this.chatDelayMs = Math.max(0, Math.min(5000, root.get("chatDelayMs").getAsInt()));
            if (root.has("animationTemplate")) {
                try { this.animationTemplate = ChatAnimationTemplate.valueOf(root.get("animationTemplate").getAsString()); }
                catch (Exception ignored) {}
            }
            if (root.has("showYouTubeIcon")) this.showYouTubeIcon = root.get("showYouTubeIcon").getAsBoolean();
            if (root.has("showUsername")) this.showUsername = root.get("showUsername").getAsBoolean();
            if (root.has("showTimestamp")) this.showTimestamp = root.get("showTimestamp").getAsBoolean();
            if (root.has("coloredUsernames")) this.coloredUsernames = root.get("coloredUsernames").getAsBoolean();
            if (root.has("coloredMessages")) this.coloredMessages = root.get("coloredMessages").getAsBoolean();
            if (root.has("rgbMode")) this.rgbMode = root.get("rgbMode").getAsBoolean();
            if (root.has("newestAtTop")) this.newestAtTop = root.get("newestAtTop").getAsBoolean();
            if (root.has("chatShadow")) this.chatShadow = root.get("chatShadow").getAsBoolean();
            if (root.has("textShadow")) this.textShadow = root.get("textShadow").getAsBoolean();

            if (this.autoConnect && !this.streamUrl.isEmpty()) {
                connect(this.streamUrl);
            }
        } catch (Throwable ignored) {}
    }

    public synchronized void saveConfig() {
        try {
            Path cfgDir = FabricLoader.getInstance().getConfigDir();
            Path bcDir = cfgDir.resolve("banglaclient");
            Files.createDirectories(bcDir);
            JsonObject root = new JsonObject();
            root.addProperty("youtubeUrl", streamUrl != null ? streamUrl : "");
            root.addProperty("autoConnect", autoConnect);
            root.addProperty("chatDelayMs", chatDelayMs);
            root.addProperty("animationTemplate", animationTemplate.name());
            root.addProperty("showYouTubeIcon", showYouTubeIcon);
            root.addProperty("showUsername", showUsername);
            root.addProperty("showTimestamp", showTimestamp);
            root.addProperty("coloredUsernames", coloredUsernames);
            root.addProperty("coloredMessages", coloredMessages);
            root.addProperty("rgbMode", rgbMode);
            root.addProperty("newestAtTop", newestAtTop);
            root.addProperty("chatShadow", chatShadow);
            root.addProperty("textShadow", textShadow);
            ConfigManager.writeAtomically(cfgDir.resolve("livestream.json"), root);
            ConfigManager.writeAtomically(bcDir.resolve("livestream.json"), root);
        } catch (Throwable ignored) {}
    }

    public static LiveStreamManager get() { return INSTANCE; }

    public static Optional<String> extractVideoId(String raw) {
        if (raw == null) return Optional.empty();
        String trimmed = raw.trim();
        if (BARE_ID.matcher(trimmed).matches()) return Optional.of(trimmed);
        Matcher m = YT_URL_PATTERN.matcher(trimmed);
        return m.find() ? Optional.of(m.group(1)) : Optional.empty();
    }

    public synchronized void connect(String rawUrl) {
        this.streamUrl = rawUrl == null ? "" : rawUrl.trim();
        disconnect();
        Optional<String> idOpt = extractVideoId(this.streamUrl);
        if (idOpt.isEmpty()) {
            snapshotRef.set(new StreamSnapshot(ConnectionState.ERROR, "", "--", "--", null, "--", null, null, 0L, "Invalid YouTube Live URL"));
            return;
        }
        String videoId = idOpt.get();
        snapshotRef.set(new StreamSnapshot(ConnectionState.CONNECTING, videoId, "--", "--", null, "--", null, null, 0L, "Connecting..."));
        videoTask = executor.scheduleWithFixedDelay(() -> pollVideoMetadata(videoId), 0, 20, TimeUnit.SECONDS);
        chatTask = executor.scheduleWithFixedDelay(() -> pollLiveChat(videoId), 1, 3, TimeUnit.SECONDS);
    }

    public synchronized void disconnect() {
        if (videoTask != null) { videoTask.cancel(false); videoTask = null; }
        if (chatTask != null) { chatTask.cancel(false); chatTask = null; }
        continuationToken = null;
        chatBuffer.clear();
        pendingDelayQueue.clear();
        seenIds.clear();
        snapshotRef.set(StreamSnapshot.disconnected());
    }

    public synchronized void refresh() {
        if (!streamUrl.isEmpty()) connect(streamUrl);
    }

    /**
     * Releases queued chat messages one-by-one according to the 0.0s - 5.0s delay setting.
     * Even at 0.0s delay, spaces out burst messages by 180ms so each message animates smoothly!
     */
    public void tickChatPacing() {
        if (pendingDelayQueue.isEmpty()) return;
        long nowMs = System.currentTimeMillis();
        long effectiveIntervalMs = Math.max(180L, chatDelayMs);
        if (nowMs - lastPacedReleaseMs >= effectiveIntervalMs) {
            ChatMessage m = pendingDelayQueue.pollFirst();
            if (m != null) {
                lastPacedReleaseMs = nowMs;
                lastReleaseNanoTime = System.nanoTime();
                while (chatBuffer.size() >= 300) chatBuffer.pollFirst();
                chatBuffer.offerLast(new ChatMessage(
                        m.id(), m.author(), m.content(), m.timestampMs(),
                        lastReleaseNanoTime, m.owner(), m.moderator(), m.member()
                ));
            }
        }
    }

    private void pollVideoMetadata(String videoId) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://www.youtube.com/watch?v=" + videoId + "&hl=en"))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", USER_AGENT)
                    .header("Cookie", "CONSENT=YES+cb")
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() != 200) return;
            String html = resp.body();

            JsonObject playerResp = extractJson(html, "ytInitialPlayerResponse");
            JsonObject initialData = extractJson(html, "ytInitialData");
            if (playerResp == null) return;

            JsonObject vd = obj(playerResp, "videoDetails");
            if (vd == null) return;

            String title = str(vd, "title", "--");
            String author = str(vd, "author", "--");
            boolean isLive = vd.has("isLive") && vd.get("isLive").getAsBoolean();
            Long viewers = isLive ? parseLongSafe(str(vd, "viewCount", null)) : null;
            Long likes = null;
            Long subs = null;
            String handle = "--";

            JsonObject micro = obj(obj(playerResp, "microformat"), "playerMicroformatRenderer");
            JsonObject liveDetails = obj(micro, "liveBroadcastDetails");
            long startMs = parseIsoTime(str(liveDetails, "startTimestamp", null));

            if (initialData != null) {
                JsonObject primaryInfo = findObjectByKey(initialData, "videoPrimaryInfoRenderer", 0);
                if (primaryInfo != null) {
                    JsonObject likeModel = findObjectByKey(primaryInfo, "likeButtonViewModel", 0);
                    JsonObject btnModel = findObjectByKey(likeModel, "buttonViewModel", 0);
                    if (btnModel != null) likes = parseCompactNumber(str(btnModel, "title", null));
                }
                JsonObject ownerRenderer = findObjectByKey(initialData, "videoOwnerRenderer", 0);
                if (ownerRenderer != null) {
                    subs = parseCompactNumber(readRuns(obj(ownerRenderer, "subscriberCountText")));
                    JsonObject webCmd = findObjectByKey(ownerRenderer, "webCommandMetadata", 0);
                    String u = str(webCmd, "url", "");
                    if (u.contains("/@")) handle = u.substring(u.indexOf("/@") + 1);
                }
            }

            snapshotRef.set(new StreamSnapshot(
                    isLive ? ConnectionState.LIVE : ConnectionState.OFFLINE,
                    videoId, author, handle, subs, title, viewers, likes, startMs,
                    isLive ? "Live" : "Offline"
            ));
        } catch (Throwable ignored) {}
    }

    private void pollLiveChat(String videoId) {
        try {
            if (continuationToken != null && !cachedInnertubeKey.isEmpty()) {
                String url = "https://www.youtube.com/youtubei/v1/live_chat/get_live_chat?prettyPrint=false&key=" + cachedInnertubeKey;
                String body = "{\"context\":{\"client\":{\"clientName\":\"WEB\",\"clientVersion\":\""
                        + cachedClientVer + "\",\"hl\":\"en\",\"gl\":\"US\"}},\"continuation\":\""
                        + continuationToken.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
                HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .header("User-Agent", USER_AGENT)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)).build();
                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (resp.statusCode() == 200) {
                    JsonObject root = JsonParser.parseString(resp.body()).getAsJsonObject();
                    JsonObject cont = obj(obj(root, "continuationContents"), "liveChatContinuation");
                    if (cont != null) {
                        parseChatActions(array(cont, "actions"));
                        String next = extractNextContinuation(cont);
                        if (next != null) continuationToken = next;
                        return;
                    }
                }
            }

            String popoutUrl = "https://www.youtube.com/live_chat?v=" + videoId + "&is_popout=1&hl=en";
            HttpRequest req = HttpRequest.newBuilder(URI.create(popoutUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", USER_AGENT)
                    .header("Cookie", "CONSENT=YES+cb").GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() != 200) return;

            String html = resp.body();
            Matcher km = API_KEY_PAT.matcher(html);
            if (km.find()) cachedInnertubeKey = km.group(1);
            Matcher vm = CLIENT_VER_PAT.matcher(html);
            if (vm.find()) cachedClientVer = vm.group(1);

            JsonObject initialData = extractJson(html, "ytInitialData");
            JsonObject liveChat = obj(obj(initialData, "contents"), "liveChatRenderer");
            if (liveChat != null) {
                parseChatActions(array(liveChat, "actions"));
                continuationToken = extractNextContinuation(liveChat);
            }
        } catch (Throwable ignored) {}
    }

    private void parseChatActions(JsonArray actions) {
        if (actions == null) return;
        for (JsonElement el : actions) {
            JsonObject item = obj(obj(el.getAsJsonObject(), "addChatItemAction"), "item");
            if (item == null) continue;
            JsonObject r = obj(item, "liveChatTextMessageRenderer");
            if (r == null) r = obj(item, "liveChatPaidMessageRenderer");
            if (r == null) continue;

            String id = str(r, "id", null);
            String author = readRuns(obj(r, "authorName"));
            String text = readRuns(obj(r, "message"));
            if (id == null || text.isEmpty() || !seenIds.add(id)) continue;

            boolean owner = false, mod = false, member = false;
            JsonArray badges = array(r, "authorBadges");
            if (badges != null) {
                for (JsonElement b : badges) {
                    JsonObject br = obj(b.getAsJsonObject(), "liveChatAuthorBadgeRenderer");
                    if (br == null) continue;
                    if (br.has("customThumbnail")) member = true;
                    JsonObject icon = obj(br, "icon");
                    String type = icon != null ? str(icon, "iconType", "") : "";
                    if ("OWNER".equals(type)) owner = true;
                    else if ("MODERATOR".equals(type)) mod = true;
                }
            }

            while (pendingDelayQueue.size() >= 250) pendingDelayQueue.pollFirst();
            pendingDelayQueue.offerLast(new ChatMessage(
                    id, author.isEmpty() ? "Viewer" : author, text,
                    System.currentTimeMillis(), System.nanoTime(), owner, mod, member
            ));
        }
    }

    private static String extractNextContinuation(JsonObject container) {
        JsonArray conts = array(container, "continuations");
        if (conts == null) return null;
        for (JsonElement el : conts) {
            JsonObject cObj = el.getAsJsonObject();
            for (String k : new String[]{"invalidationContinuationData", "timedContinuationData", "reloadContinuationData"}) {
                JsonObject d = obj(cObj, k);
                if (d != null && d.has("continuation")) return d.get("continuation").getAsString();
            }
        }
        return null;
    }

    private static String readRuns(JsonObject obj) { if (true) return ChatEmojiShortcodes.readRunsWithEmojis(obj);
        if (obj == null) return "";
        if (obj.has("simpleText")) return obj.get("simpleText").getAsString();
        JsonArray runs = array(obj, "runs");
        if (runs == null) return "";
        StringBuilder sb = new StringBuilder();
        for (JsonElement e : runs) {
            JsonObject r = e.getAsJsonObject();
            if (r.has("text")) sb.append(r.get("text").getAsString());
        }
        return sb.toString();
    }

    private static JsonObject findObjectByKey(JsonElement element, String targetKey, int depth) {
        if (element == null || depth > 12 || !element.isJsonObject()) return null;
        JsonObject o = element.getAsJsonObject();
        if (o.has(targetKey) && o.get(targetKey).isJsonObject()) return o.getAsJsonObject(targetKey);
        for (String k : o.keySet()) {
            JsonElement ch = o.get(k);
            if (ch.isJsonObject()) {
                JsonObject f = findObjectByKey(ch, targetKey, depth + 1);
                if (f != null) return f;
            } else if (ch.isJsonArray()) {
                for (JsonElement it : ch.getAsJsonArray()) {
                    JsonObject f = findObjectByKey(it, targetKey, depth + 1);
                    if (f != null) return f;
                }
            }
        }
        return null;
    }

    private static JsonObject extractJson(String html, String marker) {
        int idx = html.indexOf(marker);
        if (idx < 0) return null;
        int start = html.indexOf('{', idx + marker.length());
        if (start < 0) return null;
        int depth = 0;
        boolean inStr = false, esc = false;
        for (int i = start; i < html.length(); i++) {
            char c = html.charAt(i);
            if (esc) { esc = false; continue; }
            if (c == '\\' && inStr) { esc = true; continue; }
            if (c == '"') { inStr = !inStr; continue; }
            if (!inStr) {
                if (c == '{') depth++;
                else if (c == '}' && --depth == 0) {
                    try { return JsonParser.parseString(html.substring(start, i + 1)).getAsJsonObject(); }
                    catch (Exception e) { return null; }
                }
            }
        }
        return null;
    }

    private static Long parseCompactNumber(String text) {
        if (text == null || text.isBlank()) return null;
        Matcher m = NUMBER_PATTERN.matcher(text.replace(",", ""));
        if (!m.find()) return null;
        try {
            double val = Double.parseDouble(m.group(1));
            String suf = m.group(2);
            if (suf != null) {
                switch (suf.toUpperCase(Locale.ROOT)) {
                    case "K" -> val *= 1_000.0;
                    case "M" -> val *= 1_000_000.0;
                    case "B" -> val *= 1_000_000_000.0;
                }
            }
            return Math.round(val);
        } catch (NumberFormatException e) { return null; }
    }

    private static Long parseLongSafe(String raw) {
        if (raw == null) return null;
        try { return Long.parseLong(raw.trim()); } catch (Exception e) { return null; }
    }

    private static long parseIsoTime(String iso) {
        if (iso == null || iso.isEmpty()) return 0L;
        try { return OffsetDateTime.parse(iso).toInstant().toEpochMilli(); } catch (Exception e) { return 0L; }
    }

    private static JsonObject obj(JsonObject p, String k) {
        return (p != null && p.has(k) && p.get(k).isJsonObject()) ? p.getAsJsonObject(k) : null;
    }

    private static JsonArray array(JsonObject p, String k) {
        return (p != null && p.has(k) && p.get(k).isJsonArray()) ? p.getAsJsonArray(k) : null;
    }

    private static String str(JsonObject p, String k, String def) {
        return (p != null && p.has(k) && !p.get(k).isJsonNull()) ? p.get(k).getAsString() : def;
    }

    public static String formatCompactNumber(Long count) {
        if (count == null) return "--";
        long c = count;
        if (c < 1_000L) return String.valueOf(c);
        if (c < 1_000_000L) return String.format(Locale.ROOT, "%.1fK", c / 1_000.0);
        return String.format(Locale.ROOT, "%.1fM", c / 1_000_000.0);
    }

    public StreamSnapshot snapshot() { return snapshotRef.get(); }
    public Deque<ChatMessage> chatMessages() { return chatBuffer; }
    public String getStreamUrl() { return streamUrl; }
    public void setStreamUrl(String url) { this.streamUrl = url != null ? url.trim() : ""; }
    public boolean isAutoConnect() { return autoConnect; }
    public void setAutoConnect(boolean autoConnect) { this.autoConnect = autoConnect; }
    public int getChatDelayMs() { return chatDelayMs; }
    public void setChatDelayMs(int ms) { this.chatDelayMs = Math.max(0, Math.min(5000, ms)); }
    public ChatAnimationTemplate getAnimationTemplate() { return animationTemplate; }
    public void setAnimationTemplate(ChatAnimationTemplate t) { if (t != null) this.animationTemplate = t; }
    public long getLastReleaseNanoTime() { return lastReleaseNanoTime; }
    public int getScrollLineOffset() { return scrollLineOffset; }
    public void scrollUp(int lines) { scrollLineOffset = Math.min(200, scrollLineOffset + lines); }
    public void scrollDown(int lines) { scrollLineOffset = Math.max(0, scrollLineOffset - lines); }
    public void jumpToLiveBottom() { scrollLineOffset = 0; }
}
