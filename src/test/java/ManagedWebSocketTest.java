import org.junit.jupiter.api.Test;
import io.github.bbobbogi.stream4j.util.ManagedWebSocket;
import okhttp3.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ManagedWebSocketTest {

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(200, TimeUnit.MILLISECONDS)
            .build();

    private static final Request REFUSED_REQUEST = new Request.Builder()
            .url("ws://127.0.0.1:19999")
            .build();

    private ManagedWebSocket.Callback noopCallback() {
        return new ManagedWebSocket.Callback() {
            @Override public void onOpen(WebSocket ws, Response r) {}
            @Override public void onMessage(WebSocket ws, String t) {}
            @Override public void onClosed(int code, String reason) {}
            @Override public void onFailure(Throwable t) {}
        };
    }

    @Test
    void closeIsIdempotent() {
        AtomicInteger callbackCount = new AtomicInteger();
        ManagedWebSocket ws = new ManagedWebSocket(new ManagedWebSocket.Callback() {
            @Override public void onOpen(WebSocket w, Response r) {}
            @Override public void onMessage(WebSocket w, String t) {}
            @Override public void onClosed(int code, String reason) { callbackCount.incrementAndGet(); }
            @Override public void onFailure(Throwable t) { callbackCount.incrementAndGet(); }
        });

        for (int i = 0; i < 10; i++) {
            ws.close();
        }

        assertFalse(ws.isConnected());
        assertEquals(0, callbackCount.get(), "self-close must not fire callbacks");
    }

    @Test
    void concurrentClosesDoNotThrow() throws Exception {
        ManagedWebSocket ws = new ManagedWebSocket(noopCallback());

        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch latch = new CountDownLatch(8);

        for (int i = 0; i < 8; i++) {
            pool.submit(() -> {
                try {
                    ws.close();
                    ws.send("ignored");
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertFalse(ws.isConnected());
        pool.shutdown();
    }

    @Test
    void sendOnDisconnectedIsNoop() {
        ManagedWebSocket ws = new ManagedWebSocket(noopCallback());
        assertDoesNotThrow(() -> ws.send("should not throw"));
    }

    @Test
    void closeAfterFailureDoesNotDoubleCallback() throws Exception {
        AtomicInteger failureCount = new AtomicInteger();
        ManagedWebSocket ws = new ManagedWebSocket(new ManagedWebSocket.Callback() {
            @Override public void onOpen(WebSocket w, Response r) {}
            @Override public void onMessage(WebSocket w, String t) {}
            @Override public void onClosed(int code, String reason) {}
            @Override public void onFailure(Throwable t) { failureCount.incrementAndGet(); }
        });

        ws.connect(REFUSED_REQUEST, CLIENT);
        Thread.sleep(500);
        int countBeforeClose = failureCount.get();

        ws.close();
        Thread.sleep(300);

        assertEquals(countBeforeClose, failureCount.get(),
                "close must not trigger additional failure callback");
        assertFalse(ws.isConnected());
    }

    @Test
    void rapidCreateCloseDoesNotLeakThreads() throws Exception {
        int iterations = 20;
        int baselineThreads = Thread.activeCount();

        for (int i = 0; i < iterations; i++) {
            ManagedWebSocket ws = new ManagedWebSocket(noopCallback());
            ws.startPing("ping", 1);
            ws.close();
        }

        Thread.sleep(500);
        int afterThreads = Thread.activeCount();

        assertTrue(afterThreads <= baselineThreads + 3,
                "thread leak: baseline=" + baselineThreads + " after=" + afterThreads);
    }

    @Test
    void rapidReconnectPatternCleansUp() throws Exception {
        AtomicInteger failureCount = new AtomicInteger();
        int reconnects = 10;

        for (int i = 0; i < reconnects; i++) {
            ManagedWebSocket ws = new ManagedWebSocket(new ManagedWebSocket.Callback() {
                @Override public void onOpen(WebSocket w, Response r) {}
                @Override public void onMessage(WebSocket w, String t) {}
                @Override public void onClosed(int code, String reason) {}
                @Override public void onFailure(Throwable t) { failureCount.incrementAndGet(); }
            });

            ws.connect(REFUSED_REQUEST, CLIENT);
            ws.close();
            assertFalse(ws.isConnected(), "iteration " + i + ": must be disconnected after close");
        }

        Thread.sleep(1000);

        assertTrue(failureCount.get() <= reconnects,
                "failure count (" + failureCount.get() + ") should not exceed reconnect count (" + reconnects + ")");
    }

    @Test
    void volatileSwapPattern() throws Exception {
        volatile_field_holder holder = new volatile_field_holder();

        for (int i = 0; i < 5; i++) {
            ManagedWebSocket old = holder.ws;
            holder.ws = null;

            if (old != null) {
                old.close();
            }

            ManagedWebSocket fresh = new ManagedWebSocket(noopCallback());
            fresh.startPing("test", 60);
            holder.ws = fresh;
        }

        ManagedWebSocket last = holder.ws;
        holder.ws = null;
        if (last != null) {
            last.close();
        }

        Thread.sleep(300);
        int wsThreads = (int) Thread.getAllStackTraces().keySet().stream()
                .filter(t -> t.getName().contains("ws-ping"))
                .count();

        assertEquals(0, wsThreads, "all ping threads must be cleaned up");
    }

    @Test
    void failureCallbackFiresOnServerDisconnect() throws Exception {
        AtomicInteger failureCount = new AtomicInteger();
        CountDownLatch failureLatch = new CountDownLatch(1);

        ManagedWebSocket ws = new ManagedWebSocket(new ManagedWebSocket.Callback() {
            @Override public void onOpen(WebSocket w, Response r) {}
            @Override public void onMessage(WebSocket w, String t) {}
            @Override public void onClosed(int code, String reason) {}
            @Override public void onFailure(Throwable t) {
                failureCount.incrementAndGet();
                failureLatch.countDown();
            }
        });

        ws.connect(REFUSED_REQUEST, CLIENT);
        assertTrue(failureLatch.await(5, TimeUnit.SECONDS), "onFailure must fire on connection refused");
        assertEquals(1, failureCount.get(), "onFailure must fire exactly once");
        assertFalse(ws.isConnected(), "must be disconnected after failure");
    }

    @Test
    void reconnectRetryPatternSimulation() throws Exception {
        AtomicInteger connectAttempts = new AtomicInteger();
        int maxRetries = 3;

        Runnable simulateReconnect = new Runnable() {
            @Override
            public void run() {
                reconnectWithRetry(0, maxRetries, connectAttempts);
            }
        };

        CompletableFuture.runAsync(simulateReconnect).get(10, TimeUnit.SECONDS);

        assertTrue(connectAttempts.get() > 1, "must retry on failure, attempts=" + connectAttempts.get());
        assertEquals(maxRetries + 1, connectAttempts.get(),
                "must exhaust all retry attempts (initial + retries)");
    }

    private void reconnectWithRetry(int attempt, int maxAttempts, AtomicInteger counter) {
        try {
            counter.incrementAndGet();
            ManagedWebSocket ws = new ManagedWebSocket(noopCallback());
            ws.connect(REFUSED_REQUEST, CLIENT);
            Thread.sleep(300);
            if (!ws.isConnected()) {
                throw new RuntimeException("connection failed");
            }
        } catch (Exception e) {
            if (attempt < maxAttempts) {
                long delay = Math.min(100L * (1L << attempt), 1000);
                try { Thread.sleep(delay); } catch (InterruptedException ignored) {}
                reconnectWithRetry(attempt + 1, maxAttempts, counter);
            }
        }
    }

    @Test
    void failureCallbackEnablesReconnect() throws Exception {
        AtomicInteger cycleCount = new AtomicInteger();
        int totalCycles = 3;
        CountDownLatch doneLatch = new CountDownLatch(totalCycles);

        for (int i = 0; i < totalCycles; i++) {
            CountDownLatch failLatch = new CountDownLatch(1);

            ManagedWebSocket ws = new ManagedWebSocket(new ManagedWebSocket.Callback() {
                @Override public void onOpen(WebSocket w, Response r) {}
                @Override public void onMessage(WebSocket w, String t) {}
                @Override public void onClosed(int code, String reason) {}
                @Override public void onFailure(Throwable t) {
                    cycleCount.incrementAndGet();
                    failLatch.countDown();
                }
            });

            ws.connect(REFUSED_REQUEST, CLIENT);
            assertTrue(failLatch.await(5, TimeUnit.SECONDS), "cycle " + i + ": onFailure must fire");
            assertFalse(ws.isConnected());
            doneLatch.countDown();
        }

        assertTrue(doneLatch.await(5, TimeUnit.SECONDS));
        assertEquals(totalCycles, cycleCount.get(),
                "each connect-fail-reconnect cycle must trigger exactly one onFailure");
    }

    private static class volatile_field_holder {
        volatile ManagedWebSocket ws;
    }
}
