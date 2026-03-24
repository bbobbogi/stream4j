package com.bbobbogi.stream4j.youtube;

import com.bbobbogi.stream4j.common.PlatformChat;
import com.bbobbogi.stream4j.youtube.chat.*;
import com.bbobbogi.stream4j.youtube.types.LiveBroadcastDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class YouTubeChat implements PlatformChat {
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
    private volatile int consecutivePollErrors;
    private static final int MAX_CONSECUTIVE_POLL_ERRORS = 3;
    private static final long MAX_POLL_BACKOFF_MS = 30_000;

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

    @Override
    public boolean isConnected() { return connected; }
    public String getVideoId() { return liveChat != null ? liveChat.getVideoId() : null; }
    public String getChannelId() { return liveChat != null ? liveChat.getChannelId() : null; }

    @Override
    public CompletableFuture<Void> connectAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                connectInternal();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void connect() { connectAsync().join(); }

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

        schedulePoll(0);
    }

    private void schedulePoll(long delayMs) {
        if (poller == null || poller.isShutdown()) return;
        pollTask = poller.schedule(() -> {
            try {
                liveChat.update();
                consecutivePollErrors = 0;

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

                if (liveChat.isLiveEnded()) {
                    connected = false;
                    for (YouTubeChatEventListener l : listeners) l.onBroadcastEnd(YouTubeChat.this);
                    for (YouTubeChatEventListener l : listeners) l.onConnectionClosed(1000, "Broadcast ended", true, false);
                    return;
                }

                long nextInterval = liveChat.getRecommendedIntervalMs();
                if (nextInterval <= 0) nextInterval = pollIntervalMs;
                schedulePoll(nextInterval);

            } catch (IOException e) {
                consecutivePollErrors++;
                if (debug) System.out.println("[YouTube] Poll error (" + consecutivePollErrors + "/" + MAX_CONSECUTIVE_POLL_ERRORS + "): " + e.getMessage());

                if (consecutivePollErrors < MAX_CONSECUTIVE_POLL_ERRORS) {
                    long backoff = Math.min(pollIntervalMs * (1L << (consecutivePollErrors - 1)), MAX_POLL_BACKOFF_MS);
                    schedulePoll(backoff);
                } else if (autoReconnect) {
                    try {
                        liveChat.reset();
                        consecutivePollErrors = 0;
                        schedulePoll(pollIntervalMs);
                    } catch (IOException resetEx) {
                        connected = false;
                        for (YouTubeChatEventListener l : listeners) l.onBroadcastEnd(YouTubeChat.this);
                        for (YouTubeChatEventListener l : listeners) l.onConnectionClosed(1000, "Broadcast ended", true, false);
                    }
                } else {
                    connected = false;
                    boolean actuallyEnded = false;
                    try {
                        LiveBroadcastDetails info = liveChat.getBroadcastInfo();
                        actuallyEnded = info == null || !Boolean.TRUE.equals(info.isLiveNow);
                    } catch (Exception ignored) {
                        actuallyEnded = true;
                    }
                    if (actuallyEnded) {
                        for (YouTubeChatEventListener l : listeners) l.onBroadcastEnd(YouTubeChat.this);
                        for (YouTubeChatEventListener l : listeners) l.onConnectionClosed(1000, "Broadcast ended", true, false);
                    } else {
                        for (YouTubeChatEventListener l : listeners) l.onError(e);
                        for (YouTubeChatEventListener l : listeners) l.onConnectionClosed(1006, "Poll failed but broadcast still live: " + e.getMessage(), true, false);
                    }
                }
            }
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    private synchronized void stopPolling() {
        if (pollTask != null) { pollTask.cancel(false); pollTask = null; }
        if (poller != null) { poller.shutdownNow(); poller = null; }
    }

    @Override
    public CompletableFuture<Void> closeAsync() {
        return CompletableFuture.runAsync(() -> {
            connected = false;
            stopPolling();
            liveChat = null;
        });
    }

    @Override
    public void close() { closeAsync().join(); }

    @Override
    public CompletableFuture<Void> reconnectAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                close();
                connectInternal();
                for (YouTubeChatEventListener l : listeners) l.onConnect(this, true);
            } catch (Exception e) {
                for (YouTubeChatEventListener l : listeners) l.onError(e);
            }
        });
    }

    @Override
    public void reconnect() { reconnectAsync().join(); }
}
