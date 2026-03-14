package com.bbobbogi.stream4j.cime;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.protocols.Protocol;
import com.bbobbogi.stream4j.util.SharedHttpClient;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.AlreadyConnectedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * ci.me 채팅 클라이언트 클래스입니다.
 * Amazon IVS Chat 기반의 ci.me 채팅에 연결합니다.
 *
 * <p>사용 예시:</p>
 * <pre>
 * CiMeChat chat = new CiMeChatBuilder("channel_slug")
 *         .withChatListener(new CiMeChatEventListener() {
 *             {@literal @}Override
 *             public void onConnect(CiMeChat chat, boolean isReconnecting) {
 *                 System.out.println("Connected!");
 *             }
 *
 *             {@literal @}Override
 *             public void onChat(CiMeChatMessage msg) {
 *                 if (msg.hasUser()) {
 *                     System.out.println("[Chat] " + msg.getUser().getNickname() + ": " + msg.getContent());
 *                 } else {
 *                     System.out.println("[Chat] 익명: " + msg.getContent());
 *                 }
 *             }
 *         })
 *         .build();
 *
 * chat.connectBlocking();
 * </pre>
 */
public class CiMeChat {
    static final String CI_ME_API_URL = "https://ci.me/api/app";
    static final String IVS_CHAT_WS_URL = "wss://edge.ivschat.ap-northeast-2.amazonaws.com/";


    boolean reconnecting;
    boolean isDebug;
    boolean autoReconnect;

    private final Object lock = new Object();
    private CiMeChatWebsocketClient client;
    ArrayList<CiMeChatEventListener> listeners = new ArrayList<>();

    private final String channelSlug;

    CiMeChat(String channelSlug, boolean autoReconnect, boolean isDebug) {
        this.channelSlug = channelSlug;
        this.autoReconnect = autoReconnect;
        this.isDebug = isDebug;
    }

    /**
     * 채팅 서버 연결 상태를 반환합니다.
     *
     * @return 연결되어 있으면 {@code true}, 그렇지 않으면 {@code false}
     */
    public boolean isConnectedToChat() {
        synchronized (lock) {
            return client != null && client.isOpen();
        }
    }

    /**
     * 자동 재연결 설정 여부를 반환합니다.
     *
     * @return 자동 재연결이 활성화되어 있으면 {@code true}, 그렇지 않으면 {@code false}
     */
    public boolean shouldAutoReconnect() {
        return autoReconnect;
    }

    /**
     * 채널 슬러그를 반환합니다.
     *
     * @return 채널 슬러그
     */
    public String getChannelSlug() {
        return channelSlug;
    }

    /**
     * 비동기로 채팅에 연결합니다.
     *
     * @return 비동기 작업을 위한 CompletableFuture
     */
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
     * 동기로 채팅에 연결합니다.
     */
    public void connectBlocking() {
        connectAsync().join();
    }

    private void connectInternal() throws IOException, InterruptedException {
        synchronized (lock) {
            if (client != null && client.isOpen()) {
                throw new AlreadyConnectedException();
            }
        }

        reconnecting = false;

        // 1. Chat token 발급
        String token = fetchChatToken();

        if (isDebug) System.out.println("[CiMe] Token fetched successfully");

        // 2. Token을 WebSocket 서브프로토콜로 사용하여 연결
        Draft_6455 draft = new Draft_6455(
                Collections.emptyList(),
                Collections.singletonList(new Protocol(token))
        );

        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", "https://ci.me");

        CiMeChatWebsocketClient newClient = new CiMeChatWebsocketClient(
                this,
                URI.create(IVS_CHAT_WS_URL),
                draft,
                headers
        );

        synchronized (lock) {
            client = newClient;
        }

        newClient.connectBlocking();
    }

    /**
     * ci.me API에서 채팅 토큰을 발급받습니다.
     *
     * @return 채팅 토큰 문자열
     * @throws IOException 토큰 발급 실패 시
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
            return data.get("token").getAsString();
        }
    }

    /**
     * 비동기로 재연결합니다.
     * 새로운 토큰을 발급받아 재연결합니다.
     *
     * @return 비동기 작업을 위한 CompletableFuture
     */
    public CompletableFuture<Void> reconnectAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                CiMeChatWebsocketClient c;
                synchronized (lock) {
                    c = client;
                    client = null;
                }

                if (c != null && !c.isClosed() && !c.isClosing()) {
                    try {
                        c.closeBlocking();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                reconnecting = true;

                // 새 토큰을 발급받아 재연결
                connectInternal();
            } catch (Exception e) {
                for (CiMeChatEventListener listener : listeners) {
                    listener.onError(e);
                }
            }
        });
    }

    /**
     * 동기로 재연결합니다.
     */
    public void reconnectSync() {
        reconnectAsync().join();
    }

    /**
     * 비동기로 연결을 종료합니다.
     *
     * @return 비동기 작업을 위한 CompletableFuture
     */
    public CompletableFuture<Void> closeAsync() {
        return CompletableFuture.runAsync(() -> {
            CiMeChatWebsocketClient c;
            synchronized (lock) {
                c = client;
                client = null;
            }
            if (c != null) {
                if (!c.isClosed() && !c.isClosing()) {
                    try {
                        c.closeBlocking();
                        return;
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        });
    }

    /**
     * 동기로 연결을 종료합니다.
     */
    public void closeBlocking() {
        closeAsync().join();
    }

    /**
     * 공유 HTTP 클라이언트를 반환합니다.
     *
     * @return 공유 OkHttpClient 인스턴스
     */
    public static OkHttpClient getSharedHttpClient() {
        return SharedHttpClient.get();
    }
}
