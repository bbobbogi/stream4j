package com.bbobbogi.stream4j.soop;

import java.util.ArrayList;

public class SOOPChatBuilder {

    private final String streamerId;
    private final ArrayList<SOOPChatEventListener> listeners = new ArrayList<>();
    private boolean autoReconnect = true;
    private boolean debug = false;
    private int maxReconnectAttempts = 5;
    private long reconnectDelayMs = 5000;

    public SOOPChatBuilder(String streamerId) {
        this.streamerId = streamerId;
    }

    public SOOPChatBuilder withChatListener(SOOPChatEventListener listener) {
        listeners.add(listener);
        return this;
    }

    public SOOPChatBuilder withAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
        return this;
    }

    public SOOPChatBuilder withDebugMode() {
        this.debug = true;
        return this;
    }

    public SOOPChatBuilder withMaxReconnectAttempts(int maxReconnectAttempts) {
        this.maxReconnectAttempts = maxReconnectAttempts;
        return this;
    }

    public SOOPChatBuilder withReconnectDelay(long reconnectDelayMs) {
        this.reconnectDelayMs = reconnectDelayMs;
        return this;
    }

    public SOOPChat build() {
        SOOPChat chat = new SOOPChat(streamerId, autoReconnect, debug, maxReconnectAttempts, reconnectDelayMs);
        for (SOOPChatEventListener listener : listeners) {
            chat.listeners.add(listener);
        }
        return chat;
    }
}
