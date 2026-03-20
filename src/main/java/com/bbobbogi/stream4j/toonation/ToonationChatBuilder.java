package com.bbobbogi.stream4j.toonation;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToonationChatBuilder {

    private static final Pattern TOONATION_URL_PATTERN = Pattern.compile("^(?:https?://)?toon\\.at/widget/alertbox/([^/?#]+)(?:/.*)?$");

    private final ArrayList<ToonationChatEventListener> listeners = new ArrayList<>();
    private final String alertboxKey;
    private boolean autoReconnect = true;
    private boolean debug = false;

    public ToonationChatBuilder(String alertboxKey) {
        this.alertboxKey = resolveAlertboxKey(alertboxKey);
    }

    public static String resolveAlertboxKey(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        Matcher matcher = TOONATION_URL_PATTERN.matcher(trimmed);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return trimmed;
    }

    public ToonationChatBuilder withChatListener(ToonationChatEventListener listener) {
        listeners.add(listener);
        return this;
    }

    public ToonationChatBuilder withAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
        return this;
    }

    public ToonationChatBuilder withDebugMode() {
        this.debug = true;
        return this;
    }

    public ToonationChat build() {
        ToonationChat chat = new ToonationChat(alertboxKey, autoReconnect, debug);

        for (ToonationChatEventListener listener : listeners) {
            chat.listeners.add(listener);
        }

        return chat;
    }
}
