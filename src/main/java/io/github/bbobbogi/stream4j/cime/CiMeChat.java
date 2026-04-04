package io.github.bbobbogi.stream4j.cime;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import io.github.bbobbogi.stream4j.cime.chat.CiMeChatMessage;
import io.github.bbobbogi.stream4j.common.PlatformChat;
import io.github.bbobbogi.stream4j.util.ManagedWebSocket;
import io.github.bbobbogi.stream4j.util.SharedHttpClient;

import java.io.IOException;
import io.github.bbobbogi.stream4j.util.NonRetryableException;
import java.nio.channels.AlreadyConnectedException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket chat client for CiMe (Amazon IVS Chat).
 *
 * @since 1.0.0
 */
public class CiMeChat implements PlatformChat {
    static final String CI_ME_API_URL = "https://ci.me/api/app";
    static final String IVS_CHAT_WS_URL = "wss://edge.ivschat.ap-northeast-2.amazonaws.com/";


    boolean reconnecting;
    boolean isDebug;
    boolean autoReconnect;
    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private static final long RECONNECT_BASE_DELAY_MS = 1000;
    private static final long RECONNECT_MAX_DELAY_MS = 30000;

    private static final int SEEN_IDS_MAX_SIZE = 200;

    private volatile ManagedWebSocket managedWs;
    private volatile ScheduledExecutorService tokenRefreshScheduler;
    private final Gson gson = new Gson();
    private final Set<String> seenMessageIds = ConcurrentHashMap.newKeySet();
    ArrayList<CiMeChatEventListener> listeners = new ArrayList<>();

    private final String channelSlug;

    CiMeChat(String channelSlug, boolean autoReconnect, boolean isDebug) {
        this.channelSlug = channelSlug;
        this.autoReconnect = autoReconnect;
        this.isDebug = isDebug;
    }

    /**
     * Returns whether the WebSocket chat connection is active.
     *
     * @return {@code true} if connected, otherwise {@code false}
     */
    public boolean isConnectedToChat() {
        ManagedWebSocket ws = managedWs;
        return ws != null && ws.isConnected();
    }

    /**
     * Returns whether this chat client is connected.
     *
     * @return {@code true} if connected, otherwise {@code false}
     */
    @Override
    public boolean isConnected() {
        return isConnectedToChat();
    }

    /**
     * Returns whether automatic reconnection is enabled.
     *
     * @return {@code true} if auto reconnect is enabled
     */
    public boolean shouldAutoReconnect() {
        return autoReconnect;
    }

    /**
     * Returns the target channel slug.
     *
     * @return channel slug
     */
    public String getChannelSlug() {
        return channelSlug;
    }

    /**
     * Connects to chat asynchronously.
     *
     * @return a future that completes when connection setup finishes
     */
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

    /**
     * Connects to chat synchronously.
     */
    @Override
    public void connect() {
        connectAsync().join();
    }

    private void connectInternal() throws IOException {
        if (managedWs != null && managedWs.isConnected()) {
            throw new AlreadyConnectedException();
        }

        reconnecting = false;
        broadcastEnded = false;

        // 1. Chat token 발급
        String token = fetchChatToken();

        if (isDebug) System.out.println("[CiMe] Token fetched successfully");

        // 2. Token을 WebSocket 서브프로토콜로 사용하여 연결
        ManagedWebSocket ws = new ManagedWebSocket(new ManagedWebSocket.Callback() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                if (isDebug) System.out.println("[CiMe] WebSocket connected!");
                for (CiMeChatEventListener listener : listeners) {
                    listener.onConnect(CiMeChat.this, reconnecting);
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String message) {
                handleWsMessage(message);
            }

            @Override
            public void onClosed(int code, String reason) {
                boolean shouldReconnect = autoReconnect;
                for (CiMeChatEventListener listener : listeners) {
                    listener.onConnectionClosed(code, reason, true, shouldReconnect);
                }
                if (isDebug) {
                    System.out.println("[CiMe] WebSocket closed.");
                    System.out.println("Code: " + code);
                    System.out.println("Reason: " + reason);
                    System.out.println("Reconnect: " + shouldReconnect);
                }
                if (shouldReconnect) {
                    reconnectAsync();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                for (CiMeChatEventListener listener : listeners) {
                    listener.onError(t instanceof Exception ? (Exception) t : new RuntimeException(t));
                }
                boolean shouldReconnect = autoReconnect;
                for (CiMeChatEventListener listener : listeners) {
                    listener.onConnectionClosed(1006, t.getMessage(), true, shouldReconnect);
                }
                if (shouldReconnect) {
                    reconnectAsync();
                }
            }
        });

        managedWs = ws;

        Request request = new Request.Builder()
                .url(IVS_CHAT_WS_URL)
                .addHeader("Sec-WebSocket-Protocol", token)
                .addHeader("Origin", "https://ci.me")
                .build();

        OkHttpClient wsClient = SharedHttpClient.newBuilder()
                .pingInterval(30, TimeUnit.SECONDS)
                .build();
        ws.connect(request, wsClient);
    }

    /**
     * Requests a chat token from the CiMe API.
     *
     * @return chat token string
     * @throws IOException if token issuance fails
     */
    private String fetchChatToken() throws IOException {
        String url = CI_ME_API_URL + "/channels/" + channelSlug + "/chat-token";

        RequestBody emptyBody = RequestBody.create("", null);
        Request request = new Request.Builder()
                .url(url)
                .post(emptyBody)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36")
                .addHeader("Origin", "https://ci.me")
                .addHeader("Referer", "https://ci.me/")
                .build();

        try (Response response = SharedHttpClient.get().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 404 || response.code() == 403) {
                    throw new NonRetryableException("[CiMe] " + channelSlug + " 연결 불가: HTTP " + response.code());
                }
                throw new IOException("[CiMe] Failed to fetch chat token: HTTP " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("[CiMe] Empty response body for chat token");
            }

            String bodyString = body.string();
            if (isDebug) System.out.println("[CiMe] Token response: " + bodyString);

            JsonObject json = JsonParser.parseString(bodyString).getAsJsonObject();
            int code = json.get("code").getAsInt();
            if (code != 200) {
                throw new IOException("[CiMe] API error: code=" + code);
            }

            JsonObject data = json.getAsJsonObject("data");
            String token = data.get("token").getAsString();
            scheduleTokenRefresh(token);
            return token;
        }
    }

    private long parseJwtExpiration(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return 0;
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), java.nio.charset.StandardCharsets.UTF_8);
            JsonObject jwt = JsonParser.parseString(payload).getAsJsonObject();
            if (jwt.has("exp")) {
                return jwt.get("exp").getAsLong();
            }
        } catch (Exception e) {
            if (isDebug) System.out.println("[CiMe] Failed to parse JWT expiration: " + e.getMessage());
        }
        return 0;
    }

    private static final int TOKEN_REFRESH_MAX_ATTEMPTS = 3;
    private static final long TOKEN_REFRESH_BEFORE_EXPIRY_SECONDS = 300;
    private static final long TOKEN_REFRESH_RETRY_INTERVAL_SECONDS = 120;

    private void scheduleTokenRefresh(String token) {
        stopTokenRefreshScheduler();
        long expSeconds = parseJwtExpiration(token);
        if (expSeconds <= 0) return;

        long nowSeconds = System.currentTimeMillis() / 1000;
        long ttlSeconds = expSeconds - nowSeconds;
        long firstAttemptIn = Math.max(ttlSeconds - TOKEN_REFRESH_BEFORE_EXPIRY_SECONDS, 30);

        if (isDebug) System.out.println("[CiMe] Token TTL=" + ttlSeconds + "s, first refresh attempt in " + firstAttemptIn + "s");

        tokenRefreshScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cime-token-refresh");
            t.setDaemon(true);
            return t;
        });
        tokenRefreshScheduler.schedule(() -> attemptTokenRefresh(1), firstAttemptIn, TimeUnit.SECONDS);
    }

    private volatile boolean broadcastEnded;

    private void attemptTokenRefresh(int attempt) {
        if (managedWs == null || broadcastEnded) return;
        try {
            if (isDebug) System.out.println("[CiMe] Token refresh attempt " + attempt + "/" + TOKEN_REFRESH_MAX_ATTEMPTS);
            String newToken = fetchChatToken();

            if (managedWs == null || broadcastEnded) return;

            final ManagedWebSocket oldWs = managedWs;
            final ManagedWebSocket[] newWsHolder = new ManagedWebSocket[1];

            newWsHolder[0] = new ManagedWebSocket(new ManagedWebSocket.Callback() {
                @Override
                public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                    if (broadcastEnded) {
                        newWsHolder[0].closeAsync();
                        return;
                    }
                    if (isDebug) System.out.println("[CiMe] New connection established, closing old");
                    managedWs = newWsHolder[0];
                    if (oldWs != null) {
                        oldWs.closeAsync();
                    }
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    handleWsMessage(text);
                }

                @Override
                public void onClosed(int code, String reason) {
                    if (managedWs != newWsHolder[0]) return;
                    boolean shouldReconnect = autoReconnect;
                    for (CiMeChatEventListener listener : listeners) {
                        listener.onConnectionClosed(code, reason, true, shouldReconnect);
                    }
                    if (shouldReconnect) {
                        reconnectAsync();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    if (managedWs != newWsHolder[0]) return;
                    for (CiMeChatEventListener listener : listeners) {
                        listener.onError(t instanceof Exception ? (Exception) t : new RuntimeException(t));
                    }
                    boolean shouldReconnect = autoReconnect;
                    for (CiMeChatEventListener listener : listeners) {
                        listener.onConnectionClosed(1006, t.getMessage(), true, shouldReconnect);
                    }
                    if (shouldReconnect) {
                        reconnectAsync();
                    }
                }
            });

            Request request = new Request.Builder()
                    .url(IVS_CHAT_WS_URL)
                    .addHeader("Sec-WebSocket-Protocol", newToken)
                    .addHeader("Origin", "https://ci.me")
                    .build();

            OkHttpClient wsClient = SharedHttpClient.newBuilder()
                    .pingInterval(30, TimeUnit.SECONDS)
                    .build();
            newWsHolder[0].connect(request, wsClient);

        } catch (Exception e) {
            if (isDebug) System.out.println("[CiMe] Token refresh failed (attempt " + attempt + "): " + e.getMessage());
            if (attempt < TOKEN_REFRESH_MAX_ATTEMPTS && !broadcastEnded
                    && tokenRefreshScheduler != null && !tokenRefreshScheduler.isShutdown()) {
                tokenRefreshScheduler.schedule(
                        () -> attemptTokenRefresh(attempt + 1),
                        TOKEN_REFRESH_RETRY_INTERVAL_SECONDS, TimeUnit.SECONDS);
            }
        }
    }

    private void trimSeenIds() {
        if (seenMessageIds.size() > SEEN_IDS_MAX_SIZE) {
            int excess = seenMessageIds.size() - SEEN_IDS_MAX_SIZE / 2;
            var it = seenMessageIds.iterator();
            while (excess > 0 && it.hasNext()) {
                it.next();
                it.remove();
                excess--;
            }
        }
    }

    private void stopTokenRefreshScheduler() {
        ScheduledExecutorService scheduler = tokenRefreshScheduler;
        tokenRefreshScheduler = null;
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    /**
     * Reconnects asynchronously with retry logic.
     *
     * @return a future that completes after reconnect attempt handling
     */
    @Override
    public CompletableFuture<Void> reconnectAsync() {
        return CompletableFuture.runAsync(() -> reconnectWithRetry(0));
    }

    /**
     * Reconnects synchronously.
     */
    @Override
    public void reconnect() {
        reconnectAsync().join();
    }

    private void reconnectWithRetry(int attempt) {
        try {
            stopTokenRefreshScheduler();
            ManagedWebSocket ws = managedWs;
            managedWs = null;

            if (ws != null) {
                ws.close();
            }

            reconnecting = true;
            connectInternal();
        } catch (NonRetryableException e) {
            for (CiMeChatEventListener listener : listeners) {
                listener.onError(e);
            }
        } catch (Exception e) {
            if (attempt < MAX_RECONNECT_ATTEMPTS && autoReconnect) {
                long delay = Math.min(RECONNECT_BASE_DELAY_MS * (1L << attempt), RECONNECT_MAX_DELAY_MS);
                if (isDebug) System.out.println("[CiMe] Reconnect failed (attempt " + (attempt + 1) + "), retrying in " + delay + "ms: " + e.getMessage());
                try { Thread.sleep(delay); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); return; }
                reconnectWithRetry(attempt + 1);
            } else {
                for (CiMeChatEventListener listener : listeners) {
                    listener.onError(e);
                }
            }
        }
    }

    /**
     * Reconnects synchronously.
     */
    public void reconnectSync() {
        reconnectAsync().join();
    }

    /**
     * Closes the chat connection asynchronously.
     *
     * @return a future that completes when the connection is closed
     */
    @Override
    public CompletableFuture<Void> closeAsync() {
        return CompletableFuture.runAsync(() -> {
            stopTokenRefreshScheduler();
            ManagedWebSocket ws = managedWs;
            managedWs = null;
            if (ws != null) {
                ws.close();
            }
        });
    }

    private void handleWsMessage(String message) {
        try {
            if (isDebug) System.out.println("[CiMe] Message: " + message);

            JsonObject parsed = JsonParser.parseString(message).getAsJsonObject();

            if (!parsed.has("Type")) return;

            String type = parsed.get("Type").getAsString();

            if ("MESSAGE".equals(type)) {
                String msgId = parsed.has("Id") ? parsed.get("Id").getAsString() : null;
                if (msgId != null && !seenMessageIds.add(msgId)) return;
                trimSeenIds();
                processMessage(parsed);
            } else if ("EVENT".equals(type)) {
                String eventId = parsed.has("Id") ? parsed.get("Id").getAsString() : null;
                if (eventId != null && !seenMessageIds.add(eventId)) return;
                trimSeenIds();
                processEvent(parsed);
            } else if ("ERROR".equals(type)) {
                String errorMessage;
                if (parsed.has("ErrorMessage")) {
                    errorMessage = parsed.get("ErrorMessage").getAsString();
                } else if (parsed.has("Message")) {
                    errorMessage = parsed.get("Message").getAsString();
                } else {
                    errorMessage = parsed.toString();
                }
                int errorCode = parsed.has("ErrorCode") ? parsed.get("ErrorCode").getAsInt() : 0;
                boolean isTokenExpired = errorMessage != null && errorMessage.contains("expired");
                if (isDebug) System.out.println("[CiMe] Server error (code=" + errorCode + "): " + errorMessage);
                if (!isTokenExpired) {
                    for (CiMeChatEventListener listener : listeners) {
                        listener.onError(new RuntimeException("[CiMe] Server error: " + errorMessage));
                    }
                }
                CompletableFuture.runAsync(() -> {
                    ManagedWebSocket ws = managedWs;
                    managedWs = null;
                    if (ws != null) {
                        ws.close();
                    }
                    boolean shouldReconnect = isTokenExpired || autoReconnect;
                    if (!isTokenExpired) {
                        for (CiMeChatEventListener listener : listeners) {
                            listener.onConnectionClosed(1008, "Server error: " + errorMessage, true, shouldReconnect);
                        }
                    }
                    if (shouldReconnect) {
                        reconnectAsync();
                    }
                });
            }
        } catch (Exception ex) {
            for (CiMeChatEventListener listener : listeners) {
                listener.onError(ex);
            }
        }
    }

    private void processMessage(JsonObject parsed) {
        CiMeChatMessage msg = new CiMeChatMessage();
        msg.rawJson = parsed.toString();
        msg.id = parsed.has("Id") ? parsed.get("Id").getAsString() : null;
        msg.type = "MESSAGE";
        msg.content = parsed.has("Content") ? parsed.get("Content").getAsString() : "";

        // SendTime 파싱 (ISO 8601 형식)
        if (parsed.has("SendTime") && !parsed.get("SendTime").isJsonNull()) {
            try {
                String sendTimeStr = parsed.get("SendTime").getAsString();
                Instant instant = Instant.parse(sendTimeStr);
                msg.sendTime = Date.from(instant);
            } catch (Exception e) {
                if (isDebug) System.out.println("[CiMe] Failed to parse SendTime: " + e.getMessage());
            }
        }

        // Sender 파싱
        if (parsed.has("Sender") && !parsed.get("Sender").isJsonNull()) {
            JsonObject sender = parsed.getAsJsonObject("Sender");
            msg.senderUserId = sender.has("UserId") ? sender.get("UserId").getAsString() : null;

            // Sender.Attributes.user는 JSON 문자열로 인코딩되어 있음
            if (sender.has("Attributes") && !sender.get("Attributes").isJsonNull()) {
                JsonObject senderAttrs = sender.getAsJsonObject("Attributes");
                if (senderAttrs.has("user") && !senderAttrs.get("user").isJsonNull()) {
                    try {
                        String userJsonStr = senderAttrs.get("user").getAsString();
                        msg.user = gson.fromJson(userJsonStr, CiMeChatMessage.CiMeUser.class);
                    } catch (Exception e) {
                        if (isDebug)
                            System.out.println("[CiMe] Failed to parse user: " + e.getMessage());
                    }
                }
            }
        }

        for (CiMeChatEventListener listener : listeners) {
            listener.onChat(msg);
        }
    }

    /** 필터링할 이벤트 이름 목록 (리스너에 전달하지 않음) */
    private static final Set<String> IGNORED_EVENTS = Set.of(
            "MIDROLL_START",
            "BAN_USER",
            "aws:DISCONNECT_USER",
            "UPDATE_CHAT_NOTICE",
            "LIVE_STARTED",
            "GRANT_MANAGER",
            "REVOKE_MANAGER",
            "CHAT_MODE_NOTICE",
            "UPDATE_CHAT_MODE"
    );

    private void processEvent(JsonObject parsed) {
        String eventName = parsed.has("EventName") ? parsed.get("EventName").getAsString() : "UNKNOWN";

        if (IGNORED_EVENTS.contains(eventName)) {
            if (isDebug) System.out.println("[CiMe] Ignored event: " + eventName);
            return;
        }

        for (CiMeChatEventListener listener : listeners) {
            if ("LIVE_ENDED".equals(eventName)) {
                broadcastEnded = true;
                stopTokenRefreshScheduler();
                listener.onBroadcastEnd(this);
            }
            listener.onEvent(eventName, parsed.toString());
        }
    }

    /**
     * Closes the chat connection synchronously.
     */
    @Override
    public void close() {
        closeAsync().join();
    }

    /**
     * Returns the shared HTTP client used by this library.
     *
     * @return shared OkHttp client instance
     */
    public static OkHttpClient getSharedHttpClient() {
        return SharedHttpClient.get();
    }
}
