package com.bbobbogi.stream4j.toonation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import com.bbobbogi.stream4j.util.ManagedWebSocket;
import com.bbobbogi.stream4j.util.SharedHttpClient;

import java.io.IOException;
import com.bbobbogi.stream4j.util.NonRetryableException;
import java.nio.channels.AlreadyConnectedException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bbobbogi.stream4j.common.PlatformChat;
import com.bbobbogi.stream4j.toonation.chat.ToonationDonationMessage;

public class ToonationChat implements PlatformChat {

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

    private final String alertboxKey;
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private volatile ManagedWebSocket managedWs;

    ToonationChat(String alertboxKey, boolean autoReconnect, boolean isDebug) {
        this.alertboxKey = alertboxKey;
        this.autoReconnect = autoReconnect;
        this.isDebug = isDebug;
    }

    @Override
    public boolean isConnected() {
        ManagedWebSocket ws = managedWs;
        return ws != null && ws.isConnected();
    }

    public boolean shouldAutoReconnect() {
        return autoReconnect;
    }

    public String getAlertboxKey() {
        return alertboxKey;
    }

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
    public void connect() {
        connectAsync().join();
    }

    private void connectInternal() throws IOException {
        if (managedWs != null && managedWs.isConnected()) {
            throw new AlreadyConnectedException();
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

        ManagedWebSocket ws = new ManagedWebSocket(new ManagedWebSocket.Callback() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                if (isDebug) System.out.println("[Toonation] WebSocket connected!");
                managedWs.startPing("#ping", PING_INTERVAL_SECONDS, 60);
                for (ToonationChatEventListener listener : listeners) {
                    listener.onConnect(ToonationChat.this, reconnecting);
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                if (text.isEmpty()) return;

                if ("#ping".equals(text)) {
                    managedWs.send("#pong");
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
            public void onClosed(int code, String reason) {
                for (ToonationChatEventListener listener : listeners) {
                    listener.onConnectionClosed(code, reason, true, autoReconnect);
                }

                if (autoReconnect) {
                    reconnecting = true;
                    reconnectAsync();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                for (ToonationChatEventListener listener : listeners) {
                    listener.onError(t instanceof Exception ? (Exception) t : new RuntimeException(t));
                }
                boolean shouldReconnect = autoReconnect;
                for (ToonationChatEventListener listener : listeners) {
                    listener.onConnectionClosed(1006, t.getMessage(), true, shouldReconnect);
                }
                if (shouldReconnect) {
                    reconnecting = true;
                    reconnectAsync();
                }
            }
        });

        managedWs = ws;

        ws.connect(request, client);
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
                throw new NonRetryableException("[Toonation] Invalid alertbox URL");
            }

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

    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private static final long RECONNECT_BASE_DELAY_MS = 1000;
    private static final long RECONNECT_MAX_DELAY_MS = 30000;

    @Override
    public CompletableFuture<Void> reconnectAsync() {
        return CompletableFuture.runAsync(() -> reconnectWithRetry(0));
    }

    @Override
    public void reconnect() {
        reconnectAsync().join();
    }

    private void reconnectWithRetry(int attempt) {
        try {
            ManagedWebSocket ws = managedWs;
            managedWs = null;

            if (ws != null) {
                ws.close();
            }

            reconnecting = true;
            connectInternal();
        } catch (NonRetryableException e) {
            for (ToonationChatEventListener listener : listeners) {
                listener.onError(e);
            }
        } catch (Exception e) {
            if (attempt < MAX_RECONNECT_ATTEMPTS && autoReconnect) {
                long delay = Math.min(RECONNECT_BASE_DELAY_MS * (1L << attempt), RECONNECT_MAX_DELAY_MS);
                if (isDebug) System.out.println("[Toonation] Reconnect failed (attempt " + (attempt + 1) + "), retrying in " + delay + "ms: " + e.getMessage());
                try { Thread.sleep(delay); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); return; }
                reconnectWithRetry(attempt + 1);
            } else {
                for (ToonationChatEventListener listener : listeners) {
                    listener.onError(e);
                }
            }
        }
    }

    @Override
    public CompletableFuture<Void> closeAsync() {
        return CompletableFuture.runAsync(() -> {
            ManagedWebSocket ws = managedWs;
            managedWs = null;
            if (ws != null) {
                ws.close();
            }
        });
    }

    @Override
    public void close() {
        closeAsync().join();
    }
}
