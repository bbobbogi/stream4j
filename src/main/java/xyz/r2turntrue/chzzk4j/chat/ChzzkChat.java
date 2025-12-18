package xyz.r2turntrue.chzzk4j.chat;

import com.google.gson.JsonElement;
import xyz.r2turntrue.chzzk4j.Chzzk;
import xyz.r2turntrue.chzzk4j.exception.NotLoggedInException;
import xyz.r2turntrue.chzzk4j.types.ChzzkUser;
import xyz.r2turntrue.chzzk4j.util.RawApiUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.AlreadyConnectedException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * 치지직 채팅 클라이언트 클래스입니다.
 */
public class ChzzkChat {
    boolean reconnecting;
    Chzzk chzzk;

    private final Object lock = new Object();
    private ChatWebsocketClient client;
    ArrayList<ChatEventListener> listeners = new ArrayList<>();

    String accessToken;
    String userId;
    String channelId;
    String chatId;

    boolean autoReconnect = false;

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
     * 채팅 ID를 반환합니다.
     *
     * @return 채팅 ID
     */
    public String getChatId() {
        return chatId;
    }

    /**
     * 채널 ID를 반환합니다.
     *
     * @return 채널 ID
     */
    public String getChannelId() {
        return channelId;
    }

    ChzzkChat(Chzzk chzzk, String channelId, boolean autoReconnect) {
        this.chzzk = chzzk;
        this.channelId = channelId;
        this.autoReconnect = autoReconnect;
    }

    /**
     * Connects to the chat. This method doesn't block.
     *
     * @return 비동기 작업을 위한 CompletableFuture
     */
    public CompletableFuture<Void> connectAsync() {
        return connectFromChannelId(channelId, autoReconnect);
    }

    /**
     * Connects to the chat. This method blocks.
     */
    public void connectBlocking() {
        connectFromChannelId(channelId, autoReconnect).join();
    }

    /**
     * 이벤트 리스너를 추가합니다.
     *
     * @param listener 추가할 리스너
     * @deprecated Please add listeners when build {@link ChzzkChat} by {@link ChzzkChatBuilder}
     */
    @Deprecated
    public void addListener(ChatEventListener listener) {
        listeners.add(listener);
    }

    /**
     * 최근 채팅 메시지를 요청합니다.
     *
     * @param chatCount 요청할 채팅 메시지 수
     */
    public void requestRecentChat(int chatCount) {
        ChatWebsocketClient c;
        synchronized (lock) {
            c = client;
            if (c == null || !c.isOpen()) {
                throw new IllegalStateException("Connect to request recent chats!");
            }
        }
        c.requestRecentChat(chatCount);
    }

    /**
     * 채팅 메시지를 전송합니다.
     *
     * @param content 전송할 메시지 내용
     */
    public void sendChat(String content) {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Chat content must not be null or empty.");
        }
        ChatWebsocketClient c;
        synchronized (lock) {
            c = client;
            if (c == null || !c.isOpen()) {
                throw new IllegalStateException("Connect to send chat!");
            }
        }
        c.sendChat(content);
    }

    /**
     * Connect to chatting by the channel id
     *
     * @param channelId channel id to connect.
     * @param autoReconnect should reconnect automatically when disconnected by the server.
     * @throws IOException when failed to connect to the chat
     * @throws UnsupportedOperationException when failed to fetch chatChannelId! (Try to put NID_SES/NID_AUT when create {@link Chzzk}, because it's mostly caused by age restriction)
     */
    private CompletableFuture<Void> connectFromChannelId(String channelId, boolean autoReconnect) {
        return CompletableFuture.runAsync(() -> {
            try {
                JsonElement chatIdRaw = RawApiUtils.getContentJson(chzzk.getHttpClient(),
                                RawApiUtils.httpGetRequest(Chzzk.API_URL + "/service/v3/channels/" + channelId + "/live-detail").build(), chzzk.isDebug)
                        .getAsJsonObject()
                        .get("chatChannelId");

                if (chatIdRaw.isJsonNull()) {
                    throw new UnsupportedOperationException("Failed to fetch chatChannelId! (Try to put NID_SES/NID_AUT, because it's mostly caused by age restriction)");
                }

                connectFromChatId(channelId, chatIdRaw.getAsString(), autoReconnect).join();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private CompletableFuture<Void> connectFromChatId(String channelId, String chatId, boolean autoReconnect) {
        return CompletableFuture.runAsync(() -> {
            try {
                synchronized (lock) {
                    if (client != null && client.isOpen()) {
                        throw new AlreadyConnectedException();
                    }
                }

                reconnecting = false;

                this.autoReconnect = autoReconnect;

                this.channelId = channelId;
                this.chatId = chatId;

                userId = "";
                try {
                    ChzzkUser user = chzzk.getLoggedUser();
                    userId = user.getUserId();
                } catch (NotLoggedInException e) {
                }

                String accessTokenUrl = Chzzk.GAME_API_URL +
                        "/v1/chats/access-token?channelId=" + chatId +
                        "&chatType=STREAMING";
                accessToken = RawApiUtils.getContentJson(
                        chzzk.getHttpClient(),
                        RawApiUtils.httpGetRequest(accessTokenUrl).build(),
                        chzzk.isDebug
                ).getAsJsonObject().get("accessToken").getAsString();

                int serverId = 0;

                for (char i : channelId.toCharArray()) {
                    serverId += Character.getNumericValue(i);
                }

                serverId = Math.abs(serverId) % 9 + 1;

                ChatWebsocketClient newClient = new ChatWebsocketClient(this,
                        URI.create("wss://kr-ss" + serverId + ".chat.naver.com/chat"));

                synchronized (lock) {
                    client = newClient;
                }

                newClient.connectBlocking();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 비동기로 재연결합니다.
     *
     * @return 비동기 작업을 위한 CompletableFuture
     */
    public CompletableFuture<Void> reconnectAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                ChatWebsocketClient c;
                synchronized (lock) {
                    c = client;
                    if (c == null) {
                        throw new IllegalStateException("Client not initalized to reconnect!");
                    }
                }

                if (!c.isClosed() && !c.isClosing()) {
                    try {
                        c.closeBlocking();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                reconnecting = true;

                c.reconnectBlocking();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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
            ChatWebsocketClient c;
            synchronized (lock) {
                c = client;
                client = null;  // 재사용 방지
            }
            if (c != null && !c.isClosed() && !c.isClosing()) {
                try {
                    c.closeBlocking();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
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
}
