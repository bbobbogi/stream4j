package io.github.bbobbogi.stream4j.youtube;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.bbobbogi.stream4j.youtube.chat.YouTubeLiveChat;
import io.github.bbobbogi.stream4j.youtube.types.YouTubeChannelInfo;
import io.github.bbobbogi.stream4j.youtube.types.YouTubeLiveInfo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTube {
    private static final Gson gson = new Gson();

    private static final Pattern TITLE_TAG_PATTERN = Pattern.compile("<title>(.*?)</title>", Pattern.DOTALL);
    private static final Pattern OG_TITLE_PATTERN = Pattern.compile("<meta\\s+property=\\\"og:title\\\"\\s+content=\\\"([^\\\"]*)\\\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern CHANNEL_ID_PATTERN = Pattern.compile("\\\"channelId\\\":\\\"([^\\\"]+)\\\"");
    private static final Pattern OWNER_CHANNEL_NAME_PATTERN = Pattern.compile("\\\"ownerChannelName\\\":\\\"([^\\\"]+)\\\"");
    private static final Pattern AUTHOR_PATTERN = Pattern.compile("\\\"author\\\":\\\"([^\\\"]+)\\\"");
    private static final Pattern VIEW_COUNT_PATTERN = Pattern.compile("\\\"viewCount\\\":\\\"(\\d+)\\\"");

    public boolean isDebug;

    YouTube(YouTubeBuilder builder) {
        this.isDebug = builder.isDebugEnabled();
    }

    public YouTubeLiveInfo getLiveInfo(String videoIdOrUrl) throws IOException {
        String videoId = YouTubeLiveChat.getVideoIdFromURL(videoIdOrUrl);
        String clientVersion = getClientVersion();

        Map<String, String> headers = new HashMap<>();
        headers.put("x-youtube-client-name", "1");
        headers.put("x-youtube-client-version", clientVersion);

        String html = Util.getPageContent("https://www.youtube.com/watch?v=" + videoId, headers);
        if (html == null || html.isEmpty()) {
            throw new IOException("Couldn't fetch YouTube watch page");
        }

        LiveDetails details = parseLiveBroadcastDetails(html);
        String title = parseTitle(html);
        String channelId = extractFirst(html, CHANNEL_ID_PATTERN);
        String channelName = firstNonNull(
                decodeJsonText(extractFirst(html, OWNER_CHANNEL_NAME_PATTERN)),
                decodeJsonText(extractFirst(html, AUTHOR_PATTERN))
        );
        int viewerCount = parseViewerCount(html);

        return new YouTubeLiveInfo(
                videoId,
                channelId,
                channelName,
                title,
                details.liveNow,
                details.startTimestamp,
                details.endTimestamp,
                viewerCount
        );
    }

    public YouTubeChannelInfo getChannel(String videoIdOrUrl) throws IOException {
        YouTubeLiveInfo liveInfo = getLiveInfo(videoIdOrUrl);
        return new YouTubeChannelInfo(liveInfo.getChannelId(), liveInfo.getChannelName(), liveInfo.isLive());
    }

    public boolean isLive(String videoIdOrUrl) throws IOException {
        return getLiveInfo(videoIdOrUrl).isLive();
    }

    public YouTubeChatBuilder chat(String videoIdOrUrl) {
        return new YouTubeChatBuilder(videoIdOrUrl);
    }

    private static LiveDetails parseLiveBroadcastDetails(String html) {
        try {
            String playerResponseJson = extractJsonObjectByMarker(html, "ytInitialPlayerResponse = ");
            if (playerResponseJson == null) {
                playerResponseJson = extractJsonObjectByMarker(html, "\"ytInitialPlayerResponse\":");
            }
            if (playerResponseJson == null) {
                return new LiveDetails(false, null, null);
            }

            JsonObject playerResponse = JsonParser.parseString(playerResponseJson).getAsJsonObject();
            JsonElement liveBroadcastDetails = Util.searchJsonElementByKey("liveBroadcastDetails", playerResponse);
            if (liveBroadcastDetails == null || !liveBroadcastDetails.isJsonObject()) {
                return new LiveDetails(false, null, null);
            }

            JsonObject details = liveBroadcastDetails.getAsJsonObject();
            boolean isLiveNow = details.has("isLiveNow") && details.get("isLiveNow").getAsBoolean();
            String startTimestamp = details.has("startTimestamp") ? asNullableString(details.get("startTimestamp")) : null;
            String endTimestamp = details.has("endTimestamp") ? asNullableString(details.get("endTimestamp")) : null;
            return new LiveDetails(isLiveNow, startTimestamp, endTimestamp);
        } catch (Exception ignored) {
            return new LiveDetails(false, null, null);
        }
    }

    private static String parseTitle(String html) {
        Matcher ogTitleMatcher = OG_TITLE_PATTERN.matcher(html);
        if (ogTitleMatcher.find()) {
            return decodeHtmlText(ogTitleMatcher.group(1));
        }

        Matcher titleTagMatcher = TITLE_TAG_PATTERN.matcher(html);
        if (titleTagMatcher.find()) {
            String title = decodeHtmlText(titleTagMatcher.group(1));
            if (title != null && title.endsWith(" - YouTube")) {
                return title.substring(0, title.length() - " - YouTube".length());
            }
            return title;
        }

        return null;
    }

    private static int parseViewerCount(String html) {
        String rawViewCount = extractFirst(html, VIEW_COUNT_PATTERN);
        if (rawViewCount == null) {
            return 0;
        }
        try {
            long parsed = Long.parseLong(rawViewCount);
            return parsed > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) parsed;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static String extractFirst(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String firstNonNull(String first, String second) {
        return first != null ? first : second;
    }

    private static String decodeJsonText(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return gson.fromJson('"' + raw + '"', String.class);
        } catch (Exception ignored) {
            return raw;
        }
    }

    private static String decodeHtmlText(String raw) {
        if (raw == null) {
            return null;
        }
        return raw
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">");
    }

    private static String asNullableString(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsString();
    }

    private static String extractJsonObjectByMarker(String text, String marker) {
        int markerIndex = text.indexOf(marker);
        if (markerIndex < 0) {
            return null;
        }

        int start = text.indexOf('{', markerIndex + marker.length());
        if (start < 0) {
            return null;
        }

        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);

            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }

            if (c == '"') {
                inString = true;
                continue;
            }

            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return text.substring(start, i + 1);
                }
            }
        }

        return null;
    }

    private static String getClientVersion() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        return "2." + format.format(new Date(System.currentTimeMillis() - (24L * 60 * 1000)));
    }

    private static final class LiveDetails {
        private final boolean liveNow;
        private final String startTimestamp;
        private final String endTimestamp;

        private LiveDetails(boolean liveNow, String startTimestamp, String endTimestamp) {
            this.liveNow = liveNow;
            this.startTimestamp = startTimestamp;
            this.endTimestamp = endTimestamp;
        }
    }
}
