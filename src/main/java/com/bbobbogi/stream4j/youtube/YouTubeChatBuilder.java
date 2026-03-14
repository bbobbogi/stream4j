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
    private int seenIdsMaxSize = 10000;

    public YouTubeChatBuilder(String id) { this.id = id; }

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
}
