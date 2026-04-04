package io.github.bbobbogi.stream4j.chzzk;

import io.github.bbobbogi.stream4j.chzzk.Chzzk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builder class for creating {@link ChzzkChat} instances.
 *
 * @since 1.0.0
 */
public class ChzzkChatBuilder {

    private static final Pattern CHZZK_CHANNEL_URL_PATTERN = Pattern.compile("^(?:https?://)?(?:m\\.)?chzzk\\.naver\\.com/(?:live/)?([a-f0-9]{32})(?:/.*)?$");

    private ArrayList<ChzzkChatEventListener> listeners = new ArrayList<>();
    private String channelId;
    private Chzzk chzzk;
    private boolean autoReconnect = true;
    private boolean debug = false;

    /**
     * Creates a {@link ChzzkChatBuilder}.
     *
     * @param chzzk {@link Chzzk} instance
     * @param channelId channel ID
     */
    public ChzzkChatBuilder(Chzzk chzzk, String channelId) {
        this.chzzk = chzzk;
        this.channelId = resolveChannelId(channelId);
    }

    public static String resolveChannelId(String input) {
        if (input == null) {
            return null;
        }

        String trimmed = input.trim();
        Matcher matcher = CHZZK_CHANNEL_URL_PATTERN.matcher(trimmed);
        if (matcher.matches()) {
            return matcher.group(1);
        }

        return trimmed;
    }

    /**
     * Adds a chat event listener.
     *
     * @param listener listener to add
     * @return this builder instance
     */
    public ChzzkChatBuilder withChatListener(ChzzkChatEventListener listener) {
        listeners.add(listener);

        return this;
    }

    /**
     * Sets the auto-reconnect option.
     *
     * @param autoReconnect whether to enable auto-reconnect
     * @return this builder instance
     */
    public ChzzkChatBuilder withAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;

        return this;
    }

    public ChzzkChatBuilder withDebugMode() {
        this.debug = true;
        return this;
    }

    /**
     * Creates a {@link ChzzkChat} instance.
     *
     * @return a new {@link ChzzkChat} instance
     * @throws IOException if the API request fails
     */
    public ChzzkChat build() throws IOException {
        ChzzkChat chat = new ChzzkChat(chzzk, channelId, autoReconnect, debug);

        for (ChzzkChatEventListener listener : listeners) {
            chat.addListener(listener);
        }

        return chat;
    }

}
