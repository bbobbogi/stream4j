package com.bbobbogi.stream4j.toonation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.bbobbogi.stream4j.util.SharedHttpClient;

import java.io.IOException;
import java.nio.channels.AlreadyConnectedException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToonationChat {

    static final String ALERTBOX_URL = "https://toon.at/widget/alertbox/";
    static final String WS_URL = "wss://ws.toon.at/";
    static final int DONATION_CODE = 101;
    private static final long PING_INTERVAL_SECONDS = 12;
    private static final Pattern PAYLOAD_PATTERN_UNICODE = Pattern.compile(
            "\\\\u0022payload\\\\u0022:\\s*\\\\u0022(.*?)\\\\u0022,", Pattern.MULTILINE);
    private static final Pattern PAYLOAD_PATTERN_PLAIN = Pattern.compile(
            "\"payload\":\\s*\"(.*?)\",", Pattern.MULTILINE);

    boolean isDebug;
    boolean autoReconnect;
    boolean reconnecting;
    final ArrayList<ToonationChatEventListener> listeners = new ArrayList<>();

    private final Object lock = new Object();
    private final String alertboxKey;
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private WebSocket webSocket;
    private ScheduledExecutorService pingScheduler;
    private ScheduledFuture<?> pingTask;

    ToonationChat(String alertboxKey, boolean autoReconnect, boolean isDebug) {
        this.alertboxKey = alertboxKey;
        this.autoReconnect = autoReconnect;
        this.isDebug = isDebug;
    }

    public boolean isConnected() {
        synchronized (lock) {
            return webSocket != null;
        }
    }

    public boolean shouldAutoReconnect() {
        return autoReconnect;
    }

    public String getAlertboxKey() {
        return alertboxKey;
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

    private void connectInternal() throws IOException {
        synchronized (lock) {
            if (webSocket != null) {
                throw new AlreadyConnectedException();
            }
        }

        reconnecting = false;
        String payload = fetchPayload();

        if (isDebug) System.out.println("[Toonation] Payload: " + payload);

        Request request = new Request.Builder()
                .url(WS_URL + payload)
                .build();

        OkHttpClient client = SharedHttpClient.newBuilder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        WebSocket ws = client.newWebSocket(request, new ToonationWebSocketListener());

        synchronized (lock) {
            webSocket = ws;
        }
    }

    private String fetchPayload() throws IOException {
        Request request = new Request.Builder()
                .url(ALERTBOX_URL + alertboxKey)
                .build();

        try (Response response = SharedHttpClient.get().newCall(request).execute()) {
            if (response.body() == null) {
                throw new IOException("[Toonation] Empty response body for alertbox page");
            }

            String html = response.body().string();

            if (!response.isSuccessful()) {
                if (isDebug) System.out.println("[Toonation] Get payload failed: " + html);
                throw new IOException("[Toonation] Failed to fetch alertbox page: HTTP " + response.code());
            }

            if (html.contains("widget_malformed_url_desc")) {
                throw new IOException("[Toonation] Invalid alertbox URL");
            }

            // unicode-escaped payload 먼저 시도, 실패 시 plain 시도
            Matcher m1 = PAYLOAD_PATTERN_UNICODE.matcher(html);
            if (m1.find()) {
                return m1.group(1);
            }

            Matcher m2 = PAYLOAD_PATTERN_PLAIN.matcher(html);
            if (m2.find()) {
                return m2.group(1);
            }

            throw new IOException("[Toonation] Payload not found in alertbox response");
        }
    }

    private void startPing() {
        stopPing();
        pingScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "toonation-ping");
            t.setDaemon(true);
            return t;
        });
        pingTask = pingScheduler.scheduleAtFixedRate(() -> {
            WebSocket ws;
            synchronized (lock) {
                ws = webSocket;
            }
            if (ws != null) {
                ws.send("#ping");
            }
        }, 0, PING_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void stopPing() {
        if (pingTask != null) {
            pingTask.cancel(false);
            pingTask = null;
        }
        if (pingScheduler != null) {
            pingScheduler.shutdownNow();
            pingScheduler = null;
        }
    }

    public CompletableFuture<Void> reconnectAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                WebSocket ws;
                synchronized (lock) {
                    ws = webSocket;
                    webSocket = null;
                }
                stopPing();

                if (ws != null) {
                    ws.cancel();
                }

                reconnecting = true;
                connectInternal();
            } catch (Exception e) {
                for (ToonationChatEventListener listener : listeners) {
                    listener.onError(e);
                }
            }
        });
    }

    public void reconnectBlocking() {
        reconnectAsync().join();
    }

    public CompletableFuture<Void> closeAsync() {
        return CompletableFuture.runAsync(() -> {
            WebSocket ws;
            synchronized (lock) {
                ws = webSocket;
                webSocket = null;
            }
            stopPing();
            if (ws != null) {
                ws.cancel();
            }
        });
    }

    public void closeBlocking() {
        closeAsync().join();
    }

    private class ToonationWebSocketListener extends WebSocketListener {

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            if (isDebug) System.out.println("[Toonation] WebSocket connected!");

            startPing();

            for (ToonationChatEventListener listener : listeners) {
                listener.onConnect(ToonationChat.this, reconnecting);
            }
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            if (text.isEmpty()) return;

            if ("#ping".equals(text)) {
                webSocket.send("#pong");
                return;
            } else if ("#pong".equals(text)) {
                return;
            } else if ("#block".equals(text)) {
                for (ToonationChatEventListener listener : listeners) {
                    listener.onBlocked(ToonationChat.this);
                }
                return;
            }

            try {
                if (isDebug) System.out.println("[Toonation] Message: " + text);

                ToonationDonationMessage msg = gson.fromJson(text, ToonationDonationMessage.class);
                if (msg == null || msg.getContent() == null) return;

                msg.setRawJson(text);

                if (msg.getCode() == DONATION_CODE) {
                    for (ToonationChatEventListener listener : listeners) {
                        listener.onDonation(ToonationChat.this, msg);
                    }
                }
            } catch (Exception ex) {
                if (isDebug) {
                    System.out.println("[Toonation] Error parsing JSON: " + text);
                    ex.printStackTrace();
                }
            }
        }

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            synchronized (lock) {
                if (ToonationChat.this.webSocket == null) return;
                ToonationChat.this.webSocket = null;
            }
            stopPing();

            for (ToonationChatEventListener listener : listeners) {
                listener.onConnectionClosed(code, reason, true, autoReconnect);
            }

            if (autoReconnect) {
                reconnecting = true;
                reconnectAsync();
            }
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
            synchronized (lock) {
                if (ToonationChat.this.webSocket == null) return;
                ToonationChat.this.webSocket = null;
            }
            stopPing();
            webSocket.cancel();

            for (ToonationChatEventListener listener : listeners) {
                listener.onError(t instanceof Exception ? (Exception) t : new RuntimeException(t));
            }

            if (autoReconnect) {
                reconnecting = true;
                reconnectAsync();
            }
        }
    }
}
