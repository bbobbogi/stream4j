package io.github.bbobbogi.stream4j.youtube;

import io.github.bbobbogi.stream4j.youtube.chat.IdType;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builder for {@link YouTubeChat} instances.
 *
 * <p>Accepts a video ID, watch URL, channel URL, user handle URL, or raw handle.
 * Defaults are top-chat-only mode enabled, auto-reconnect enabled, debug disabled,
 * polling interval 5000 ms, and seen-ID cache size 10000.
 *
 * @since 1.0.0
 */
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

    /**
     * Creates a builder with an input that is automatically resolved to an ID type.
     *
     * @param id the video ID, URL, or handle input
     */
    public YouTubeChatBuilder(String id) {
        ResolvedInput resolved = resolveInput(id);
        this.id = resolved.id;
        this.idType = resolved.idType;
    }

    /**
     * Resolves a user input into a normalized identifier and ID type.
     *
     * @param input the raw user input
     * @return the resolved input result
     */
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

    /**
     * Configures the identifier type used to resolve the target stream.
     * Default is the auto-detected type from the constructor input.
     *
     * @param type the identifier type
     * @return this builder for chaining
     */
    public YouTubeChatBuilder withIdType(IdType type) { this.idType = type; return this; }

    /**
     * Configures whether only top chat messages are fetched.
     * Default is {@code true}.
     *
     * @param topChatOnly {@code true} to fetch top chat only
     * @return this builder for chaining
     */
    public YouTubeChatBuilder withTopChatOnly(boolean topChatOnly) { this.topChatOnly = topChatOnly; return this; }

    /**
     * Configures automatic reconnection when polling fails.
     * Default is {@code true}.
     *
     * @param autoReconnect {@code true} to enable automatic reconnect
     * @return this builder for chaining
     */
    public YouTubeChatBuilder withAutoReconnect(boolean autoReconnect) { this.autoReconnect = autoReconnect; return this; }

    /**
     * Enables debug logging.
     * Default is disabled.
     *
     * @return this builder for chaining
     */
    public YouTubeChatBuilder withDebugMode() { this.debug = true; return this; }

    /**
     * Configures the base polling interval in milliseconds.
     * Default is {@code 5000}.
     *
     * @param ms poll interval in milliseconds
     * @return this builder for chaining
     */
    public YouTubeChatBuilder withPollInterval(long ms) { this.pollIntervalMs = ms; return this; }

    /**
     * Adds a chat event listener.
     * Default is no listeners.
     *
     * @param listener the listener to add
     * @return this builder for chaining
     */
    public YouTubeChatBuilder withChatListener(YouTubeChatEventListener listener) { listeners.add(listener); return this; }

    /**
     * Configures the maximum size of the LRU cache used for duplicate message detection.
     * Default is {@code 10000}.
     *
     * @param maxSize maximum number of seen IDs to keep
     * @return this builder for chaining
     */
    public YouTubeChatBuilder withSeenIdsMaxSize(int maxSize) { this.seenIdsMaxSize = maxSize; return this; }

    /**
     * Builds a {@link YouTubeChat} instance.
     *
     * @return a configured YouTube chat client
     */
    public YouTubeChat build() {
        YouTubeChat chat = new YouTubeChat(id, idType, topChatOnly, autoReconnect, debug, pollIntervalMs, seenIdsMaxSize);

        for (YouTubeChatEventListener listener : listeners) {
            chat.listeners.add(listener);
        }

        return chat;
    }

    /**
     * Resolved identifier result for chat target input.
     *
     * @since 1.0.0
     */
    public static final class ResolvedInput {
        private final String id;
        private final IdType idType;

        /**
         * Creates a resolved input object.
         *
         * @param id the normalized identifier
         * @param idType the resolved identifier type
         */
        public ResolvedInput(String id, IdType idType) {
            this.id = id;
            this.idType = idType;
        }

        /**
         * Returns the normalized identifier.
         *
         * @return the resolved identifier
         */
        public String getId() {
            return id;
        }

        /**
         * Returns the identifier type.
         *
         * @return the resolved identifier type
         */
        public IdType getIdType() {
            return idType;
        }
    }
}
