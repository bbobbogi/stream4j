package io.github.bbobbogi.stream4j.toonation;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builder for creating {@link ToonationChat} instances.
 *
 * <p>Toonation supports donation alerts only.
 *
 * @since 1.0.0
 */
public class ToonationChatBuilder {

    private static final Pattern TOONATION_URL_PATTERN = Pattern.compile("^(?:https?://)?toon\\.at/widget/alertbox/([^/?#]+)(?:/.*)?$");

    private final ArrayList<ToonationChatEventListener> listeners = new ArrayList<>();
    private final String alertboxKey;
    private boolean autoReconnect = true;
    private boolean debug = false;

    /**
     * Creates a builder for the target Toonation alertbox.
     *
     * @param alertboxKey alertbox key or alertbox URL
     */
    public ToonationChatBuilder(String alertboxKey) {
        this.alertboxKey = resolveAlertboxKey(alertboxKey);
    }

    /**
     * Resolves an alertbox key from a key or Toonation alertbox URL.
     *
     * @param input alertbox key or alertbox URL
     * @return normalized alertbox key, or {@code null} if input is {@code null}
     */
    public static String resolveAlertboxKey(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        Matcher matcher = TOONATION_URL_PATTERN.matcher(trimmed);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return trimmed;
    }

    /**
     * Registers a donation event listener.
     *
     * <p>Default is no listener.
     *
     * @param listener listener to receive Toonation events
     * @return this builder for chaining
     */
    public ToonationChatBuilder withChatListener(ToonationChatEventListener listener) {
        listeners.add(listener);
        return this;
    }

    /**
     * Configures automatic reconnection after disconnection.
     *
     * <p>Default is {@code true}.
     *
     * @param autoReconnect whether automatic reconnect is enabled
     * @return this builder for chaining
     */
    public ToonationChatBuilder withAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
        return this;
    }

    /**
     * Enables debug logging output.
     *
     * <p>Default is disabled.
     *
     * @return this builder for chaining
     */
    public ToonationChatBuilder withDebugMode() {
        this.debug = true;
        return this;
    }

    /**
     * Builds a {@link ToonationChat} instance with current configuration.
     *
     * @return configured Toonation chat client
     */
    public ToonationChat build() {
        ToonationChat chat = new ToonationChat(alertboxKey, autoReconnect, debug);

        for (ToonationChatEventListener listener : listeners) {
            chat.listeners.add(listener);
        }

        return chat;
    }
}
