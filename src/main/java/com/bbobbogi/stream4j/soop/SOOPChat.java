package com.bbobbogi.stream4j.soop;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.protocols.Protocol;
import com.bbobbogi.stream4j.util.SharedHttpClient;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.AlreadyConnectedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SOOPChat {

    private static final String LIVE_DETAIL_API = "https://live.sooplive.co.kr/afreeca/player_live_api.php?bjid=";

    private final Object lock = new Object();
    final ArrayList<SOOPChatEventListener> listeners = new ArrayList<>();
    private final String streamerId;
    private final boolean autoReconnect;
    private final boolean debug;
    private final int maxReconnectAttempts;
    private final long reconnectDelayMs;

    private SOOPChatWebSocketClient webSocketClient;
    private String chatNo;

    volatile boolean reconnecting;
    private volatile boolean connected;
    private volatile boolean entered;
    private volatile int reconnectAttempts;

    SOOPChat(
            String streamerId,
            boolean autoReconnect,
            boolean debug,
            int maxReconnectAttempts,
            long reconnectDelayMs
    ) {
        this.streamerId = streamerId;
        this.autoReconnect = autoReconnect;
        this.debug = debug;
        this.maxReconnectAttempts = Math.max(0, maxReconnectAttempts);
        this.reconnectDelayMs = Math.max(0L, reconnectDelayMs);
    }

    public boolean isConnected() {
        synchronized (lock) {
            return webSocketClient != null && webSocketClient.isOpen();
        }
    }

    public CompletableFuture<Void> connectAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                connectInternal();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void connectBlocking() {
        connectAsync().join();
    }

    public CompletableFuture<Void> closeAsync() {
        return CompletableFuture.runAsync(() -> {
            SOOPChatWebSocketClient client;
            synchronized (lock) {
                client = webSocketClient;
                webSocketClient = null;
            }

            connected = false;
            entered = false;
            reconnecting = false;

            if (client != null && !client.isClosed() && !client.isClosing()) {
                try {
                    client.closeBlocking();
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    public void closeBlocking() {
        closeAsync().join();
    }

    public CompletableFuture<Void> reconnectAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                SOOPChatWebSocketClient client;
                synchronized (lock) {
                    client = webSocketClient;
                    webSocketClient = null;
                }

                connected = false;
                entered = false;

                if (client != null && !client.isClosed() && !client.isClosing()) {
                    try {
                        client.closeBlocking();
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }

                reconnecting = true;
                connectInternal();
            } catch (Exception e) {
                for (SOOPChatEventListener listener : listeners) {
                    listener.onError(e);
                }
            }
        });
    }

    public void reconnectBlocking() {
        reconnectAsync().join();
    }

    private void connectInternal() throws Exception {
        synchronized (lock) {
            if (webSocketClient != null && webSocketClient.isOpen()) {
                throw new AlreadyConnectedException();
            }
        }

        LiveInfo liveInfo = fetchLiveInfo();
        if (!liveInfo.online) {
            throw new IOException("[SOOP] Streamer is offline: " + streamerId);
        }

        chatNo = liveInfo.chatNo;
        String chatUrl = buildChatUrl(liveInfo.chatDomain, liveInfo.chatPort);

        Draft_6455 draft = new Draft_6455(
                Collections.emptyList(),
                Collections.singletonList(new Protocol("chat"))
        );

        Map<String, String> headers = new HashMap<>();
        headers.put("Sec-WebSocket-Protocol", "chat");

        SOOPChatWebSocketClient client = new SOOPChatWebSocketClient(
                this,
                URI.create(chatUrl),
                draft,
                headers
        );
        SSLContext sslContext = SSLContext.getDefault();
        client.setSocketFactory(sslContext.getSocketFactory());

        synchronized (lock) {
            webSocketClient = client;
        }

        if (debug) {
            System.out.println("[SOOP] Connecting to " + chatUrl);
        }

        client.connectBlocking();
    }

    private LiveInfo fetchLiveInfo() throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("bid", streamerId)
                .add("type", "live")
                .add("pwd", "")
                .add("player_type", "html5")
                .add("stream_type", "common")
                .add("quality", "HD")
                .add("mode", "landing")
                .add("from_api", "0")
                .add("is_revive", "false")
                .build();

        Request request = new Request.Builder()
                .url(LIVE_DETAIL_API + streamerId)
                .post(formBody)
                .build();

        try (Response response = SharedHttpClient.get().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("[SOOP] Failed to fetch live info: HTTP " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("[SOOP] Empty response body for live detail");
            }

            String bodyString = body.string();
            JsonObject root = JsonParser.parseString(bodyString).getAsJsonObject();
            JsonObject channel = root.has("CHANNEL") && root.get("CHANNEL").isJsonObject()
                    ? root.getAsJsonObject("CHANNEL")
                    : null;

            if (channel == null) {
                throw new IOException("[SOOP] Missing CHANNEL in live detail response");
            }

            String chatDomain = getAsString(channel, "CHDOMAIN");
            String chatPort = getAsString(channel, "CHPT");
            String chatNo = getAsString(channel, "CHATNO");
            String broadcastNo = getAsString(channel, "BNO");
            String bjNick = getAsString(channel, "BJNICK");
            String title = getAsString(channel, "TITLE");
            int result = getAsInt(channel, "RESULT");

            if (debug) {
                System.out.println("[SOOP] Live detail loaded - bno=" + broadcastNo + ", nick=" + bjNick + ", title=" + title);
            }

            return new LiveInfo(
                    chatDomain,
                    parseIntSafe(chatPort),
                    chatNo,
                    result == 1
            );
        }
    }

    private String buildChatUrl(String domain, int port) {
        int websocketPort = port + 1;
        return "wss://" + domain + ":" + websocketPort + "/Websocket/" + streamerId;
    }

    void handleMessage(String packet) {
        String typeCode = SOOPPacket.parseTypeCode(packet);
        if (typeCode == null) {
            return;
        }

        if (debug) {
            System.out.println("[SOOP] Packet(" + typeCode + "): " + packet);
        }

        String[] fields = SOOPPacket.splitPayload(packet);

        switch (typeCode) {
            case SOOPPacket.TYPE_CONNECT:
                connected = true;
                reconnectAttempts = 0;
                for (SOOPChatEventListener listener : listeners) {
                    listener.onConnect(this, reconnecting);
                }
                synchronized (lock) {
                    if (webSocketClient != null) {
                        webSocketClient.sendPacket(SOOPPacket.buildJoinPacket(chatNo));
                    }
                }
                break;

            case SOOPPacket.TYPE_JOIN:
                entered = true;
                synchronized (lock) {
                    if (webSocketClient != null) {
                        webSocketClient.startPing();
                    }
                }
                break;

            case SOOPPacket.TYPE_CHAT:
                emitChat(fields);
                break;

            case SOOPPacket.TYPE_TEXT_DONATION:
                emitDonation(
                        SOOPDonationMessage.Type.TEXT,
                        getField(fields, 1),
                        getField(fields, 2),
                        getField(fields, 3),
                        parseIntSafe(getField(fields, 4)),
                        parseIntSafe(getField(fields, 5))
                );
                break;

            case SOOPPacket.TYPE_VIDEO_DONATION:
                emitDonation(
                        SOOPDonationMessage.Type.VIDEO,
                        getField(fields, 2),
                        getField(fields, 3),
                        getField(fields, 4),
                        parseIntSafe(getField(fields, 5)),
                        parseIntSafe(getField(fields, 6))
                );
                break;

            case SOOPPacket.TYPE_AD_BALLOON:
                emitDonation(
                        SOOPDonationMessage.Type.AD_BALLOON,
                        getField(fields, 2),
                        getField(fields, 3),
                        getField(fields, 4),
                        parseIntSafe(getField(fields, 10)),
                        parseIntSafe(getField(fields, 11))
                );
                break;

            case SOOPPacket.TYPE_SUBSCRIBE:
                emitSubscribe(fields);
                break;

            case SOOPPacket.TYPE_DISCONNECT:
                closeAsync();
                break;

            default:
                break;
        }
    }

    void handleClose(String reason, boolean remote) {
        synchronized (lock) {
            if (webSocketClient == null) {
                return;
            }
            webSocketClient = null;
        }

        boolean shouldReconnect = remote && autoReconnect && reconnectAttempts < maxReconnectAttempts;

        connected = false;
        entered = false;

        for (SOOPChatEventListener listener : listeners) {
            listener.onConnectionClosed(remote ? 1006 : 1000, reason, remote, shouldReconnect);
        }

        if (shouldReconnect) {
            reconnectAttempts++;
            reconnecting = true;
            CompletableFuture.delayedExecutor(reconnectDelayMs, TimeUnit.MILLISECONDS)
                    .execute(this::reconnectBlocking);
        }
    }

    void handleError(Exception ex) {
        for (SOOPChatEventListener listener : listeners) {
            listener.onError(ex);
        }
    }

    private void emitChat(String[] fields) {
        String message = getField(fields, 1);
        String userId = getField(fields, 2);
        String username = getField(fields, 6);
        for (SOOPChatEventListener listener : listeners) {
            listener.onChat(userId, username, message);
        }
    }

    private void emitDonation(SOOPDonationMessage.Type type, String to, String from, String fromUsername, int amount, int fanClubOrdinal) {
        SOOPDonationMessage msg = new SOOPDonationMessage(type, to, from, fromUsername, amount, fanClubOrdinal);
        for (SOOPChatEventListener listener : listeners) {
            listener.onDonation(this, msg);
        }
    }

    private void emitSubscribe(String[] fields) {
        String from = getField(fields, 2);
        String fromUsername = getField(fields, 3);
        int monthCount = parseIntSafe(getField(fields, 4));
        int tier = parseIntSafe(getField(fields, 8));
        for (SOOPChatEventListener listener : listeners) {
            listener.onSubscribe(this, from, fromUsername, monthCount, tier);
        }
    }

    private static String getField(String[] fields, int index) {
        return fields != null && index >= 0 && index < fields.length ? fields[index] : null;
    }

    private static int parseIntSafe(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static String getAsString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    private static int getAsInt(JsonObject obj, String key) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            return 0;
        }
        try {
            return obj.get(key).getAsInt();
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static class LiveInfo {
        private final String chatDomain;
        private final int chatPort;
        private final String chatNo;
        private final boolean online;

        private LiveInfo(String chatDomain, int chatPort, String chatNo, boolean online) {
            this.chatDomain = chatDomain;
            this.chatPort = chatPort;
            this.chatNo = chatNo;
            this.online = online;
        }
    }
}
