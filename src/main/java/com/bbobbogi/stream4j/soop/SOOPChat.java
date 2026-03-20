package com.bbobbogi.stream4j.soop;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.OkHttpClient;
import okhttp3.WebSocket;
import com.bbobbogi.stream4j.util.ManagedWebSocket;
import com.bbobbogi.stream4j.util.SharedHttpClient;

import java.io.IOException;
import com.bbobbogi.stream4j.util.NonRetryableException;
import java.nio.channels.AlreadyConnectedException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SOOPChat {

    private static final String LIVE_DETAIL_API = "https://live.sooplive.co.kr/afreeca/player_live_api.php?bjid=";
    private static final String LOGIN_API = "https://login.sooplive.co.kr/app/LoginAction.php";
    final ArrayList<SOOPChatEventListener> listeners = new ArrayList<>();
    private final String streamerId;
    private final boolean autoReconnect;
    private final boolean debug;
    private final int maxReconnectAttempts;
    private final long reconnectDelayMs;
    private OkHttpClient httpClient;

    private volatile ManagedWebSocket managedWs;
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
            long reconnectDelayMs,
            OkHttpClient httpClient
    ) {
        this.streamerId = streamerId;
        this.autoReconnect = autoReconnect;
        this.debug = debug;
        this.maxReconnectAttempts = maxReconnectAttempts;
        this.reconnectDelayMs = reconnectDelayMs;
        this.httpClient = httpClient != null ? httpClient : SharedHttpClient.get();
    }

    public boolean isConnected() {
        ManagedWebSocket ws = managedWs;
        return ws != null && ws.isConnected();
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
            ManagedWebSocket ws = managedWs;
            managedWs = null;

            connected = false;
            entered = false;
            reconnecting = false;

            if (ws != null) {
                ws.closeBlocking();
            }
        });
    }

    public void closeBlocking() {
        closeAsync().join();
    }

    public CompletableFuture<Void> reconnectAsync() {
        return CompletableFuture.runAsync(() -> reconnectWithRetry(0));
    }

    private void reconnectWithRetry(int attempt) {
        try {
            ManagedWebSocket ws = managedWs;
            managedWs = null;

            connected = false;
            entered = false;

            if (ws != null) {
                ws.closeBlocking();
            }

            reconnecting = true;
            connectInternal();
        } catch (NonRetryableException e) {
            for (SOOPChatEventListener listener : listeners) {
                listener.onError(e);
            }
        } catch (Exception e) {
            if (attempt < maxReconnectAttempts && autoReconnect) {
                long delay = Math.min(reconnectDelayMs * (1L << attempt), 30000);
                if (debug) System.out.println("[SOOP] Reconnect failed (attempt " + (attempt + 1) + "), retrying in " + delay + "ms: " + e.getMessage());
                try { Thread.sleep(delay); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); return; }
                reconnectWithRetry(attempt + 1);
            } else {
                for (SOOPChatEventListener listener : listeners) {
                    listener.onError(e);
                }
            }
        }
    }

    public void reconnectBlocking() {
        reconnectAsync().join();
    }

    private void connectInternal() throws Exception {
        if (managedWs != null && managedWs.isConnected()) {
            throw new AlreadyConnectedException();
        }

        LiveInfo liveInfo = fetchLiveInfo();
        if (liveInfo.chatNo == null || liveInfo.chatNo.isEmpty()) {
            String reason = switch (liveInfo.result) {
                case 0 -> "오프라인";
                case -6 -> "성인 인증 필요 (로그인 필요)";
                case -14 -> "구독자 전용 방송 (티어 " + liveInfo.minTier + " 이상)";
                default -> "RESULT=" + liveInfo.result;
            };
            throw new NonRetryableException("[SOOP] " + streamerId + " 연결 불가: " + reason);
        }

        chatNo = liveInfo.chatNo;
        String chatUrl = buildChatUrl(liveInfo.chatDomain, liveInfo.chatPort);

        if (debug) {
            System.out.println("[SOOP] Connecting to " + chatUrl);
        }

        ManagedWebSocket ws = new ManagedWebSocket(new ManagedWebSocket.Callback() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                managedWs.send(SOOPPacket.buildConnectPacket());
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                handleMessage(text);
            }

            @Override
            public void onClosed(int code, String reason) {
                handleClose(reason, true);
            }

            @Override
            public void onFailure(Throwable t) {
                handleError(t instanceof Exception ? (Exception) t : new RuntimeException(t));
                handleClose(t.getMessage(), true);
            }
        });

        managedWs = ws;

        Request request = new Request.Builder()
                .url(chatUrl)
                .addHeader("Sec-WebSocket-Protocol", "chat")
                .build();

        ws.connect(request, httpClient);
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

        try (Response response = httpClient.newCall(request).execute()) {
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

            boolean online = chatDomain != null && chatNo != null && !chatNo.isEmpty();
            int minTier = getAsInt(channel, "P_MIN_TIER");

            return new LiveInfo(
                    chatDomain,
                    parseIntSafe(chatPort),
                    chatNo,
                    online,
                    result,
                    minTier
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
                managedWs.send(SOOPPacket.buildJoinPacket(chatNo));
                for (SOOPChatEventListener listener : listeners) {
                    listener.onConnect(this, reconnecting);
                }
                break;

            case SOOPPacket.TYPE_JOIN:
                entered = true;
                managedWs.startPing(SOOPPacket.buildPingPacket(), 60, 180);
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
                String bjStat = getField(fields, 0);
                if ("0".equals(bjStat)) {
                    if (debug) System.out.println("[SOOP] Broadcast ended (svc_SETBJSTAT=0)");
                    ManagedWebSocket endWs = managedWs;
                    managedWs = null;
                    connected = false;
                    entered = false;
                    for (SOOPChatEventListener listener : listeners) {
                        listener.onBroadcastEnd(SOOPChat.this);
                    }
                    for (SOOPChatEventListener listener : listeners) {
                        listener.onConnectionClosed(1000, "Broadcast ended", false, false);
                    }
                    if (endWs != null) {
                        endWs.closeAsync();
                    }
                } else {
                    if (debug) System.out.println("[SOOP] BJ stat update (0007): " + bjStat);
                }
                break;

            default:
                if (debug) System.out.println("[SOOP] Unhandled packet(" + typeCode + "): " + packet);
                for (SOOPChatEventListener listener : listeners) {
                    listener.onUnhandledPacket(typeCode, fields);
                }
                break;
        }
    }

    void handleClose(String reason, boolean remote) {
        if (managedWs == null) {
            return;
        }
        managedWs = null;

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
        private final int result;
        private final int minTier;

        private LiveInfo(String chatDomain, int chatPort, String chatNo, boolean online, int result, int minTier) {
            this.chatDomain = chatDomain;
            this.chatPort = chatPort;
            this.chatNo = chatNo;
            this.online = online;
            this.result = result;
            this.minTier = minTier;
        }
    }
}
