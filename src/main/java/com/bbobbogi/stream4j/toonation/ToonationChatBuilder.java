package com.bbobbogi.stream4j.toonation;

import java.util.ArrayList;

public class ToonationChatBuilder {

    private final ArrayList<ToonationChatEventListener> listeners = new ArrayList<>();
    private final String alertboxKey;
    private boolean autoReconnect = true;
    private boolean debug = false;

    public ToonationChatBuilder(String alertboxKey) {
        this.alertboxKey = alertboxKey;
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
