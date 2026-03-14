package com.bbobbogi.stream4j.youtube;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class YouTubeChat {
    final ArrayList<YouTubeChatEventListener> listeners = new ArrayList<>();

    private static final int DEFAULT_SEEN_IDS_MAX_SIZE = 10000;

    private final String id;
    private final IdType idType;
    private final boolean topChatOnly;
    private final boolean autoReconnect;
    private final boolean debug;
    private final long pollIntervalMs;
    private final int seenIdsMaxSize;

    private YouTubeLiveChat liveChat;
    private ScheduledExecutorService poller;
    private ScheduledFuture<?> pollTask;
    private final Set<String> seenIds;
    private volatile boolean connected;

    YouTubeChat(String id, IdType idType, boolean topChatOnly, boolean autoReconnect, boolean debug, long pollIntervalMs, int seenIdsMaxSize) {
        this.id = id;
        this.idType = idType;
        this.topChatOnly = topChatOnly;
        this.autoReconnect = autoReconnect;
        this.debug = debug;
        this.pollIntervalMs = pollIntervalMs;
        this.seenIdsMaxSize = seenIdsMaxSize > 0 ? seenIdsMaxSize : DEFAULT_SEEN_IDS_MAX_SIZE;
        this.seenIds = createLruCache(this.seenIdsMaxSize);
    }

    private static Set<String> createLruCache(int maxSize) {
        return Collections.newSetFromMap(new LinkedHashMap<String, Boolean>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                return size() > maxSize;
            }
        });
    }

    public boolean isConnected() { return connected; }
    public String getVideoId() { return liveChat != null ? liveChat.getVideoId() : null; }
    public String getChannelId() { return liveChat != null ? liveChat.getChannelId() : null; }

    public CompletableFuture<Void> connectAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                connectInternal();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void connectBlocking() { connectAsync().join(); }

    private void connectInternal() throws IOException {
        liveChat = new YouTubeLiveChat(id, topChatOnly, idType);
        connected = true;

        for (YouTubeChatEventListener l : listeners) {
            l.onConnect(this, false);
        }

        startPolling();
    }

    private void startPolling() {
        stopPolling();
        poller = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "youtube-chat-poller");
            t.setDaemon(true);
            return t;
        });

        pollTask = poller.scheduleAtFixedRate(() -> {
            try {
                liveChat.update();

                for (ChatItem item : liveChat.getChatItems()) {
                    if (item.getId() == null || !seenIds.add(item.getId())) continue;

                    switch (item.getType()) {
                        case PAID_MESSAGE:
                        case TICKER_PAID_MESSAGE:
                            for (YouTubeChatEventListener l : listeners) l.onSuperChat(item);
                            break;
                        case PAID_STICKER:
                            for (YouTubeChatEventListener l : listeners) l.onSuperSticker(item);
                            break;
                        case NEW_MEMBER_MESSAGE:
                            for (YouTubeChatEventListener l : listeners) l.onNewMember(item);
                            break;
                        case MESSAGE:
                        default:
                            for (YouTubeChatEventListener l : listeners) l.onChat(item);
                            break;
                    }
                }

            } catch (IOException e) {
                if (debug) System.out.println("[YouTube] Poll error: " + e.getMessage());

                if (autoReconnect) {
                    try {
                        liveChat.reset();
                    } catch (IOException resetEx) {
                        connected = false;
                        stopPolling();
                        for (YouTubeChatEventListener l : listeners) l.onBroadcastEnd(YouTubeChat.this);
                        for (YouTubeChatEventListener l : listeners) l.onConnectionClosed(1000, "Broadcast ended", true, false);
                    }
                } else {
                    connected = false;
                    stopPolling();
                    for (YouTubeChatEventListener l : listeners) l.onError(e);
                    for (YouTubeChatEventListener l : listeners) l.onConnectionClosed(1006, e.getMessage(), true, false);
                }
            }
        }, 0, pollIntervalMs, TimeUnit.MILLISECONDS);
    }

    private void stopPolling() {
        if (pollTask != null) { pollTask.cancel(false); pollTask = null; }
        if (poller != null) { poller.shutdownNow(); poller = null; }
    }

    public CompletableFuture<Void> closeAsync() {
        return CompletableFuture.runAsync(() -> {
            connected = false;
            stopPolling();
            liveChat = null;
        });
    }

    public void closeBlocking() { closeAsync().join(); }

    public CompletableFuture<Void> reconnectAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                closeBlocking();
                connectInternal();
                for (YouTubeChatEventListener l : listeners) l.onConnect(this, true);
            } catch (Exception e) {
                for (YouTubeChatEventListener l : listeners) l.onError(e);
            }
        });
    }

    public void reconnectBlocking() { reconnectAsync().join(); }
}
