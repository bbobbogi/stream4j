package io.github.bbobbogi.stream4j.youtube;

import io.github.bbobbogi.stream4j.youtube.chat.IdType;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeChatBuilder {

    private static final Pattern WATCH_URL_PATTERN = Pattern.compile("^(?:https?://)?(?:www\\.|m\\.)?youtube\\.com/watch\\?.*$");
    private static final Pattern WATCH_VIDEO_ID_PATTERN = Pattern.compile("(?:^|[?&])v=([A-Za-z0-9_-]{11})(?:[&#]|$)");
    private static final Pattern USER_URL_PATTERN = Pattern.compile("^(?:https?://)?(?:www\\.|m\\.)?youtube\\.com/@([A-Za-z0-9._-]+)(?:/.*)?$");
    private static final Pattern CHANNEL_URL_PATTERN = Pattern.compile("^(?:https?://)?(?:www\\.|m\\.)?youtube\\.com/channel/([^/?#]+)(?:/.*)?$");
    private static final Pattern USER_ID_PATTERN = Pattern.compile("^@([A-Za-z0-9._-]+)$");
    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{11}$");

    private final String id;
    private final ArrayList<YouTubeChatEventListener> listeners = new ArrayList<>();
    private IdType idType;
    private boolean topChatOnly = true;
    private boolean autoReconnect = true;
    private boolean debug = false;
    private long pollIntervalMs = 5000;
    private int seenIdsMaxSize = 10000;

    public YouTubeChatBuilder(String id) {
        ResolvedInput resolved = resolveInput(id);
        this.id = resolved.id;
        this.idType = resolved.idType;
    }

    public static ResolvedInput resolveInput(String input) {
        if (input == null) {
            return new ResolvedInput(null, IdType.VIDEO);
        }

        String trimmed = input.trim();

        Matcher watchUrlMatcher = WATCH_URL_PATTERN.matcher(trimmed);
        if (watchUrlMatcher.matches()) {
            Matcher watchVideoIdMatcher = WATCH_VIDEO_ID_PATTERN.matcher(trimmed);
            if (watchVideoIdMatcher.find()) {
                return new ResolvedInput(watchVideoIdMatcher.group(1), IdType.VIDEO);
            }
        }

        Matcher channelUrlMatcher = CHANNEL_URL_PATTERN.matcher(trimmed);
        if (channelUrlMatcher.matches()) {
            return new ResolvedInput(channelUrlMatcher.group(1), IdType.CHANNEL);
        }

        Matcher userUrlMatcher = USER_URL_PATTERN.matcher(trimmed);
        if (userUrlMatcher.matches()) {
            return new ResolvedInput(userUrlMatcher.group(1), IdType.USER);
        }

        Matcher userMatcher = USER_ID_PATTERN.matcher(trimmed);
        if (userMatcher.matches()) {
            return new ResolvedInput(userMatcher.group(1), IdType.USER);
        }

        Matcher videoMatcher = VIDEO_ID_PATTERN.matcher(trimmed);
        if (videoMatcher.matches()) {
            return new ResolvedInput(trimmed, IdType.VIDEO);
        }

        return new ResolvedInput(trimmed, IdType.VIDEO);
    }

    public YouTubeChatBuilder withIdType(IdType type) { this.idType = type; return this; }

    public YouTubeChatBuilder withTopChatOnly(boolean topChatOnly) { this.topChatOnly = topChatOnly; return this; }

    public YouTubeChatBuilder withAutoReconnect(boolean autoReconnect) { this.autoReconnect = autoReconnect; return this; }

    public YouTubeChatBuilder withDebugMode() { this.debug = true; return this; }

    public YouTubeChatBuilder withPollInterval(long ms) { this.pollIntervalMs = ms; return this; }

    public YouTubeChatBuilder withChatListener(YouTubeChatEventListener listener) { listeners.add(listener); return this; }

    /**
     * 중복 메시지 감지를 위한 LRU 캐시의 최대 크기를 설정합니다.
     * 기본값은 10000입니다.
     *
     * @param maxSize LRU 캐시의 최대 크기
     * @return this builder
     */
    public YouTubeChatBuilder withSeenIdsMaxSize(int maxSize) { this.seenIdsMaxSize = maxSize; return this; }

    public YouTubeChat build() {
        YouTubeChat chat = new YouTubeChat(id, idType, topChatOnly, autoReconnect, debug, pollIntervalMs, seenIdsMaxSize);

        for (YouTubeChatEventListener listener : listeners) {
            chat.listeners.add(listener);
        }

        return chat;
    }

    public static final class ResolvedInput {
        private final String id;
        private final IdType idType;

        public ResolvedInput(String id, IdType idType) {
            this.id = id;
            this.idType = idType;
        }

        public String getId() {
            return id;
        }

        public IdType getIdType() {
            return idType;
        }
    }
}
