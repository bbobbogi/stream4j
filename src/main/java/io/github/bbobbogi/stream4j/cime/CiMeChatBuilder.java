package io.github.bbobbogi.stream4j.cime;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builder for {@link CiMeChat} instances.
 *
 * <p>Defaults are auto-reconnect enabled and debug mode disabled.
 *
 * @since 1.0.0
 */
public class CiMeChatBuilder {

    private static final Pattern CIME_SLUG_URL_PATTERN = Pattern.compile("^(?:https?://)?(?:www\\.)?ci\\.me/@([^/?#]+)(?:/.*)?$");

    private final ArrayList<CiMeChatEventListener> listeners = new ArrayList<>();
    private final String channelSlug;
    private boolean autoReconnect = true;
    private boolean debug = false;

    /**
     * Creates a builder for a CiMe channel input.
     *
     * @param channelSlug channel slug or CiMe channel URL
     */
    public CiMeChatBuilder(String channelSlug) {
        this.channelSlug = resolveSlug(channelSlug);
    }

    /**
     * Resolves a channel input into a normalized slug.
     *
     * @param input channel slug, handle, or URL
     * @return normalized slug
     */
    public static String resolveSlug(String input) {
        if (input == null) {
            return null;
        }

        String trimmed = input.trim();
        Matcher matcher = CIME_SLUG_URL_PATTERN.matcher(trimmed);
        if (matcher.matches()) {
            return matcher.group(1);
        }

        if (trimmed.startsWith("@")) {
            return trimmed.substring(1);
        }

        return trimmed;
    }

    /**
     * Adds a chat event listener.
     * Default is no listeners.
     *
     * @param listener listener to add
     * @return this builder for chaining
     */
    public CiMeChatBuilder withChatListener(CiMeChatEventListener listener) {
        listeners.add(listener);
        return this;
    }

    /**
     * Configures automatic reconnection.
     * Default is {@code true}.
     *
     * @param autoReconnect whether automatic reconnect is enabled
     * @return this builder for chaining
     */
    public CiMeChatBuilder withAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
        return this;
    }

    /**
     * Enables debug logging.
     * Default is disabled.
     *
     * @return this builder for chaining
     */
    public CiMeChatBuilder withDebugMode() {
        this.debug = true;
        return this;
    }

    /**
     * Builds a {@link CiMeChat} instance.
     *
     * @return a configured CiMe chat client
     */
    public CiMeChat build() {
        CiMeChat chat = new CiMeChat(channelSlug, autoReconnect, debug);

        for (CiMeChatEventListener listener : listeners) {
            chat.listeners.add(listener);
        }

        return chat;
    }
}
