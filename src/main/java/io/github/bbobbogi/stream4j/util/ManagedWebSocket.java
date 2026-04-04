package io.github.bbobbogi.stream4j.util;

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

/**
 * Managed WebSocket wrapper with connection lifecycle and ping scheduling.
 *
 * @apiNote This is an internal API and may change without notice.
 * @since 1.0.0
 */
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
     * Sends ping messages periodically and optionally detects idle timeouts.
     *
     * @param message ping message to send ({@code null} to skip sending and only monitor idleness)
     * @param intervalSeconds ping send and idle-check interval in seconds
     * @param idleTimeoutSeconds idle timeout in seconds; if no message is received within this period,
     *                           {@code onFailure} is called; {@code 0} disables idle timeout detection
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
     * Returns the last message receive timestamp.
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

    public void close() {
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
