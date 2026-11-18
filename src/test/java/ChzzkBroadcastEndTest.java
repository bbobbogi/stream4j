import com.sun.net.httpserver.HttpServer;
import io.github.bbobbogi.stream4j.chzzk.Chzzk;
import io.github.bbobbogi.stream4j.chzzk.ChzzkBuilder;
import io.github.bbobbogi.stream4j.chzzk.ChzzkChat;
import io.github.bbobbogi.stream4j.chzzk.ChzzkChatEventListener;
import io.github.bbobbogi.stream4j.common.DonationPlatform;
import io.github.bbobbogi.stream4j.common.StreamChat;
import io.github.bbobbogi.stream4j.common.StreamChatEventListener;
import io.github.bbobbogi.stream4j.util.ManagedWebSocket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Broadcast-end handling for CHZZK: {@link StreamChat} must keep ended chats tracked so they can be closed,
 * and {@link ChzzkChat} must notify the end only once per online-to-offline transition. No real network.
 */
public class ChzzkBroadcastEndTest {

    private static final String CHANNEL_ID = "testchannel";

    private HttpServer server;
    private String originalApiUrl;
    private volatile String liveStatus = "CLOSE";
    private final AtomicInteger liveStatusRequests = new AtomicInteger();

    @BeforeEach
    void startServer() throws Exception {
        originalApiUrl = Chzzk.API_URL;
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/polling/v2/channels/" + CHANNEL_ID + "/live-status", exchange -> {
            liveStatusRequests.incrementAndGet();
            String status = liveStatus;
            if ("ERROR".equals(status)) {
                exchange.sendResponseHeaders(500, -1);
                exchange.close();
                return;
            }
            byte[] body = ("{\"code\":200,\"message\":null,\"content\":{\"liveTitle\":\"t\",\"status\":\""
                    + status + "\",\"concurrentUserCount\":0}}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        Chzzk.API_URL = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        Chzzk.API_URL = originalApiUrl;
        server.stop(0);
    }

    /** Counts calls to {@link #close()} instead of touching a socket. */
    private static class SpyWebSocket extends ManagedWebSocket {
        final AtomicInteger closeCalls = new AtomicInteger();

        SpyWebSocket() {
            super(new ManagedWebSocket.Callback() {
                @Override public void onOpen(okhttp3.WebSocket ws, okhttp3.Response r) {}
                @Override public void onMessage(okhttp3.WebSocket ws, String t) {}
                @Override public void onClosed(int code, String reason) {}
                @Override public void onFailure(Throwable t) {}
            });
        }

        @Override
        public void close() {
            closeCalls.incrementAndGet();
        }
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Object getField(Object target, String name) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }

    @Test
    @SuppressWarnings("unchecked")
    void streamChatKeepsChzzkChatTrackedAfterBroadcastEndAndClosesIt() throws Exception {
        List<String> ended = new CopyOnWriteArrayList<>();
        StreamChat streamChat = StreamChat.builder()
                .addChzzk(CHANNEL_ID)
                .withListener(new StreamChatEventListener() {
                    @Override
                    public void onBroadcastEnd(DonationPlatform platform, String channelId) {
                        ended.add(platform + ":" + channelId);
                    }
                })
                .build();

        Chzzk chzzk = new ChzzkBuilder().build();
        ChzzkChat chat = chzzk.chat(CHANNEL_ID).build();
        SpyWebSocket ws = new SpyWebSocket();
        setField(chat, "managedWs", ws);
        ((Map<String, ChzzkChat>) getField(streamChat, "chzzkChats")).put(CHANNEL_ID, chat);
        assertEquals(1, streamChat.getConnectionCount());

        Method handler = StreamChat.class.getDeclaredMethod("handleChzzkBroadcastEnd", String.class);
        handler.setAccessible(true);
        handler.invoke(streamChat, CHANNEL_ID);

        assertEquals(List.of("CHZZK:" + CHANNEL_ID), ended, "onBroadcastEnd must still be emitted");
        assertEquals(1, streamChat.getConnectionCount(), "ended chat must stay tracked while its socket is open");

        streamChat.closeAllAsync().join();

        assertEquals(1, ws.closeCalls.get(), "closeAllAsync must close the ended chat's socket");
        assertEquals(0, streamChat.getConnectionCount());
    }

    private ChzzkChat newChat(AtomicInteger endCount) throws Exception {
        Chzzk chzzk = new ChzzkBuilder().build();
        return chzzk.chat(CHANNEL_ID)
                .withChatListener(new ChzzkChatEventListener() {
                    @Override
                    public void onBroadcastEnd(ChzzkChat chat) {
                        endCount.incrementAndGet();
                    }
                })
                .build();
    }

    private static void check(ChzzkChat chat) throws Exception {
        Method m = ChzzkChat.class.getDeclaredMethod("checkBroadcastEnd");
        m.setAccessible(true);
        m.invoke(chat);
    }

    @Test
    void broadcastEndIsNotifiedOncePerOnlineToOfflineTransitionAndIsSilentWithoutDebug() throws Exception {
        AtomicInteger endCount = new AtomicInteger();
        ChzzkChat chat = newChat(endCount);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
        try {
            liveStatus = "CLOSE";
            check(chat);
            check(chat);
            check(chat);
            assertEquals(1, endCount.get(), "three offline checks in a row must notify once");

            liveStatus = "OPEN";
            check(chat);
            assertEquals(1, endCount.get(), "an online check must not notify");

            liveStatus = "CLOSE";
            check(chat);
            check(chat);
            assertEquals(2, endCount.get(), "a new online->offline transition must notify again");
        } finally {
            System.setOut(originalOut);
        }
        assertEquals("", captured.toString(StandardCharsets.UTF_8), "nothing may be printed when debug is off");
    }

    @Test
    void failurePathNotifiesOnceAndIsSilentWithoutDebug() throws Exception {
        AtomicInteger endCount = new AtomicInteger();
        ChzzkChat chat = newChat(endCount);

        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
        try {
            liveStatus = "ERROR";
            for (int i = 0; i < 6; i++) {
                check(chat);
            }
            assertEquals(1, endCount.get(), "repeated failures must notify once after the third");

            liveStatus = "OPEN";
            check(chat);
            liveStatus = "ERROR";
            for (int i = 0; i < 3; i++) {
                check(chat);
            }
            assertEquals(2, endCount.get(), "failures after being seen online again must notify again");
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
        assertEquals("", captured.toString(StandardCharsets.UTF_8), "nothing may be printed when debug is off");
        assertTrue(liveStatusRequests.get() > 0);
    }
}
