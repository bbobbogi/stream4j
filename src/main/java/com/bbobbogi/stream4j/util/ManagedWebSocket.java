package com.bbobbogi.stream4j.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;

public class ManagedWebSocket {

    private final Object lock = new Object();
    private final Callback callback;
    private WebSocket webSocket;
    private ScheduledExecutorService pingScheduler;
    private ScheduledFuture<?> pingTask;
    private volatile boolean closing;
    private volatile long lastMessageTime;

    public ManagedWebSocket(Callback callback) {
        this.callback = callback;
    }

    public boolean isConnected() {
        synchronized (lock) {
            return webSocket != null;
        }
    }

    public void connect(Request request, OkHttpClient client) {
        synchronized (lock) {
            if (webSocket != null) {
                throw new IllegalStateException("Already connected");
            }
            closing = false;
        }

        WebSocket ws = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                lastMessageTime = System.currentTimeMillis();
                callback.onOpen(webSocket, response);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                lastMessageTime = System.currentTimeMillis();
                callback.onMessage(webSocket, text);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                lastMessageTime = System.currentTimeMillis();
                callback.onMessage(webSocket, bytes.utf8());
            }

            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                cleanup();
                if (!closing) {
                    callback.onClosed(code, reason);
                }
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                cleanup();
                webSocket.cancel();
                if (!closing) {
                    callback.onFailure(t);
                }
            }
        });

        synchronized (lock) {
            webSocket = ws;
        }
    }

    public void send(String text) {
        WebSocket ws;
        synchronized (lock) {
            ws = webSocket;
        }
        if (ws != null) {
            ws.send(text);
        }
    }

    public void startPing(String message, long intervalSeconds) {
        startPing(message, intervalSeconds, 0);
    }

    /**
     * 주기적으로 ping 메시지를 전송하고, 선택적으로 유휴 타임아웃을 감지합니다.
     *
     * @param message            전송할 ping 메시지 (null이면 전송 생략, 유휴 감시만 수행)
     * @param intervalSeconds    ping 전송 및 유휴 체크 간격 (초)
     * @param idleTimeoutSeconds 유휴 타임아웃 (초). 이 시간 동안 메시지가 없으면 onFailure 호출. 0이면 비활성화.
     */
    public void startPing(String message, long intervalSeconds, long idleTimeoutSeconds) {
        stopPing();
        lastMessageTime = System.currentTimeMillis();
        pingScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ws-ping");
            t.setDaemon(true);
            return t;
        });
        pingTask = pingScheduler.scheduleAtFixedRate(() -> {
            if (idleTimeoutSeconds > 0) {
                long elapsed = System.currentTimeMillis() - lastMessageTime;
                if (elapsed > idleTimeoutSeconds * 1000) {
                    callback.onFailure(new java.io.IOException(
                            "Idle timeout: no message for " + (elapsed / 1000) + "s (limit " + idleTimeoutSeconds + "s)"));
                    closeAsync();
                    return;
                }
            }
            if (message != null) {
                send(message);
            }
        }, 0, intervalSeconds, TimeUnit.SECONDS);
    }

    /**
     * 마지막 메시지 수신 시각을 반환합니다.
     *
     * @return epoch millis
     */
    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void stopPing() {
        if (pingTask != null) {
            pingTask.cancel(false);
            pingTask = null;
        }
        if (pingScheduler != null) {
            pingScheduler.shutdownNow();
            pingScheduler = null;
        }
    }

    public CompletableFuture<Void> closeAsync() {
        return CompletableFuture.runAsync(() -> {
            closing = true;
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

    private void cleanup() {
        synchronized (lock) {
            webSocket = null;
        }
        stopPing();
    }

    public interface Callback {
        void onOpen(WebSocket webSocket, Response response);
        void onMessage(WebSocket webSocket, String text);
        void onClosed(int code, String reason);
        void onFailure(Throwable t);
    }
}
