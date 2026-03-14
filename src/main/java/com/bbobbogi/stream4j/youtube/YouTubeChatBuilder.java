package com.bbobbogi.stream4j.youtube;

import java.util.ArrayList;

public class YouTubeChatBuilder {

    private final String id;
    private final ArrayList<YouTubeChatEventListener> listeners = new ArrayList<>();
    private IdType idType = IdType.VIDEO;
    private boolean topChatOnly = true;
    private boolean autoReconnect = true;
    private boolean debug = false;
    private long pollIntervalMs = 5000;

    public YouTubeChatBuilder(String id) { this.id = id; }

    public YouTubeChatBuilder withIdType(IdType type) { this.idType = type; return this; }

    public YouTubeChatBuilder withTopChatOnly(boolean topChatOnly) { this.topChatOnly = topChatOnly; return this; }

    public YouTubeChatBuilder withAutoReconnect(boolean autoReconnect) { this.autoReconnect = autoReconnect; return this; }

    public YouTubeChatBuilder withDebugMode() { this.debug = true; return this; }

    public YouTubeChatBuilder withPollInterval(long ms) { this.pollIntervalMs = ms; return this; }

    public YouTubeChatBuilder withChatListener(YouTubeChatEventListener listener) { listeners.add(listener); return this; }

    public YouTubeChat build() {
        YouTubeChat chat = new YouTubeChat(id, idType, topChatOnly, autoReconnect, debug, pollIntervalMs);

        for (YouTubeChatEventListener listener : listeners) {
            chat.listeners.add(listener);
        }

        return chat;
    }
}
