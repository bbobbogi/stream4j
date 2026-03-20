package com.bbobbogi.stream4j.chzzk.chat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import com.bbobbogi.stream4j.chzzk.Chzzk;
import com.bbobbogi.stream4j.chzzk.exception.ChatFailedConnectException;
import com.bbobbogi.stream4j.chzzk.exception.NotLoggedInException;
import com.bbobbogi.stream4j.chzzk.types.ChzzkUser;
import com.bbobbogi.stream4j.util.ManagedWebSocket;
import com.bbobbogi.stream4j.util.RawApiUtils;
import com.bbobbogi.stream4j.util.SharedHttpClient;

import java.io.IOException;
import com.bbobbogi.stream4j.util.NonRetryableException;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.AlreadyConnectedException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 치지직 채팅 클라이언트 클래스입니다.
 */
public class ChzzkChat {
    boolean reconnecting;
    Chzzk chzzk;

    private volatile ManagedWebSocket managedWs;
    ArrayList<ChatEventListener> listeners = new ArrayList<>();

    String accessToken;
    String userId;
    String channelId;
    String chatId;

    boolean autoReconnect = false;

    private final Gson gson = new Gson().newBuilder().disableHtmlEscaping().create();
    private String sid;
    private ScheduledExecutorService executor;
    private long lastSendPingTime;
    private long lastRecivedMessageTime;
    private long lastLiveCheckTime;
    private int liveCheckFailCount;
    private static final long LIVE_CHECK_INTERVAL_MS = 30_000;
    private static final int LIVE_CHECK_MAX_FAILURES = 3;

    private static final HashMap<Integer, Class<?>> CLIENTBOUND_MESSAGES = new HashMap<>() {{
        put(WsMessageTypes.Commands.CONNECTED, WsMessageClientboundConnected.class);
        put(WsMessageTypes.Commands.RECENT_CHAT, WsMessageClientboundRecentChat.class);
    }};

    /**
     * 채팅 서버 연결 상태를 반환합니다.
     *
     * @return 연결되어 있으면 {@code true}, 그렇지 않으면 {@code false}
     */
    public boolean isConnectedToChat() {
        ManagedWebSocket ws = managedWs;
        return ws != null && ws.isConnected();
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
        ManagedWebSocket ws = managedWs;
        if (ws == null || !ws.isConnected()) {
            throw new IllegalStateException("Connect to request recent chats!");
        }
        var msg = setupWsMessage(new WsMessageServerboundRequestRecentChat());
        msg.tid = 2;
        msg.bdy.recentMessageCount = chatCount;
        msg.sid = sid;
        ws.send(gson.toJson(msg));
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
        ManagedWebSocket ws = managedWs;
        if (ws == null || !ws.isConnected()) {
            throw new IllegalStateException("Connect to send chat!");
        }
        var msg = setupWsMessage(new WsMessageServerboundSendChat());
        msg.sid = sid;
        var extras = new WsMessageServerboundSendChat.Body.Extras();
        extras.streamingChannelId = channelId;
        msg.bdy.extras = gson.toJson(extras);
        msg.bdy.msg = content;
        msg.bdy.msgTypeCode = WsMessageTypes.ChatTypes.TEXT;
        ws.send(gson.toJson(msg));
    }

    private CompletableFuture<Void> connectFromChannelId(String channelId, boolean autoReconnect) {
        return CompletableFuture.runAsync(() -> {
            try {
                JsonElement chatIdRaw = RawApiUtils.getContentJson(chzzk.getHttpClient(),
                                RawApiUtils.httpGetRequest(Chzzk.API_URL + "/service/v3/channels/" + channelId + "/live-detail").build(), chzzk.isDebug)
                        .getAsJsonObject()
                        .get("chatChannelId");

                if (chatIdRaw.isJsonNull()) {
                    throw new NonRetryableException("Failed to fetch chatChannelId! (Try to put NID_SES/NID_AUT, because it's mostly caused by age restriction)");
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
                if (managedWs != null && managedWs.isConnected()) {
                    throw new AlreadyConnectedException();
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

                ManagedWebSocket ws = new ManagedWebSocket(new ManagedWebSocket.Callback() {
                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {
                        handleWsOpen();
                    }

                    @Override
                    public void onMessage(WebSocket webSocket, String text) {
                        handleWsMessage(text);
                    }

                    @Override
                    public void onClosed(int code, String reason) {
                        handleWsClosed(code, reason);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        handleWsFailure(t);
                    }
                });

                managedWs = ws;

                Request request = new Request.Builder()
                        .url("wss://kr-ss" + serverId + ".chat.naver.com/chat")
                        .build();

                ws.connect(request, SharedHttpClient.get());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 비동기로 재연결합니다.
     *
     * @return 비동기 작업을 위한 CompletableFuture
     */
    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private static final long RECONNECT_BASE_DELAY_MS = 1000;
    private static final long RECONNECT_MAX_DELAY_MS = 30000;

    public CompletableFuture<Void> reconnectAsync() {
        return CompletableFuture.runAsync(() -> reconnectWithRetry(0));
    }

    private void reconnectWithRetry(int attempt) {
        try {
            ManagedWebSocket ws = managedWs;
            managedWs = null;

            shutdownExecutor();
            if (ws != null) {
                ws.closeBlocking();
            }

            reconnecting = true;
            connectFromChatId(channelId, chatId, autoReconnect).join();
        } catch (Exception e) {
            Throwable cause = e instanceof java.util.concurrent.CompletionException && e.getCause() != null ? e.getCause() : e;
            if (cause instanceof NonRetryableException) {
                for (ChatEventListener listener : listeners) {
                    listener.onError(cause instanceof Exception ? (Exception) cause : new RuntimeException(cause));
                }
                return;
            }
            if (attempt < MAX_RECONNECT_ATTEMPTS && autoReconnect) {
                long delay = Math.min(RECONNECT_BASE_DELAY_MS * (1L << attempt), RECONNECT_MAX_DELAY_MS);
                if (chzzk.isDebug) System.out.println("[Chzzk] Reconnect failed (attempt " + (attempt + 1) + "), retrying in " + delay + "ms: " + cause.getMessage());
                try { Thread.sleep(delay); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); return; }
                reconnectWithRetry(attempt + 1);
            } else {
                for (ChatEventListener listener : listeners) {
                    listener.onError(cause instanceof Exception ? (Exception) cause : new RuntimeException(cause));
                }
            }
        }
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
            ManagedWebSocket ws = managedWs;
            managedWs = null;
            shutdownExecutor();
            if (ws != null) {
                ws.closeBlocking();
            }
        });
    }

    /**
     * 동기로 연결을 종료합니다.
     */
    public void closeBlocking() {
        closeAsync().join();
    }

    private void handleWsOpen() {
        if (chzzk.isDebug) System.out.println("Connected to websocket! Connecting to chat...");

        shutdownExecutor();
        synchronized (this) {
            executor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "chzzk-chat-ws-" + channelId);
                t.setDaemon(true);
                return t;
            });
        }

        lastRecivedMessageTime = lastSendPingTime = System.currentTimeMillis();
        WsMessageServerboundConnect handshake = setupWsMessage(new WsMessageServerboundConnect());
        handshake.bdy = new WsMessageServerboundConnect.Body();
        handshake.bdy.accTkn = accessToken;
        handshake.bdy.auth = chzzk.isLoggedIn() ? "SEND" : "READ";
        handshake.bdy.uid = userId;

        managedWs.send(gson.toJson(handshake));
    }

    private void handleWsMessage(String message) {
        try {
            if (chzzk.isDebug) System.out.println("Message: " + message);

            JsonObject parsedMessage = JsonParser.parseString(message).getAsJsonObject();
            var cmdId = parsedMessage.get("cmd").getAsInt();
            var messageClass = getClientboundMessageClass(cmdId);

            if (messageClass == WsMessageClientboundConnected.class) {
                WsMessageClientboundConnected msg = gson.fromJson(parsedMessage, WsMessageClientboundConnected.class);
                if (msg.retCode == 0) {
                    if (chzzk.isDebug) System.out.println("Successfully connected!");
                    sid = msg.bdy.sid;

                    Runnable task = () -> {
                        long now = System.currentTimeMillis();

                        if (now - lastSendPingTime >= 60000 ||
                                now - lastRecivedMessageTime >= 20000) {
                            if (chzzk.isDebug) {
                                System.out.println("need client ping: current = " + (now / 1000) +
                                        ", ping = " + (lastSendPingTime / 1000) +
                                        ", recived message = " + (lastRecivedMessageTime / 1000));
                            }

                            managedWs.send(gson.toJson(new WsMessageServerboundPing()));
                            lastRecivedMessageTime = lastSendPingTime = now;
                        }

                        if (now - lastLiveCheckTime >= LIVE_CHECK_INTERVAL_MS) {
                            lastLiveCheckTime = now;
                            checkBroadcastEnd();
                        }
                    };

                    executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);

                    for (ChatEventListener listener : listeners) {
                        listener.onConnect(this, reconnecting);
                    }
                } else {
                    throw new ChatFailedConnectException(msg.retCode, msg.retMsg);
                }
            } else if (cmdId == WsMessageTypes.Commands.PING) {
                if (chzzk.isDebug) {
                    System.out.println("pong");
                    System.out.println(gson.toJson(new WsMessageServerboundPong()));
                }
                managedWs.send(gson.toJson(new WsMessageServerboundPong()));
            } else if (messageClass == WsMessageClientboundRecentChat.class) {
                JsonObject bdyObj = parsedMessage.getAsJsonObject("bdy");
                var messageList = bdyObj.getAsJsonArray("messageList");

                for (var chatJsonElement : messageList) {
                    JsonObject chatJson = chatJsonElement.getAsJsonObject();
                    try {
                        processChatMessageFromJson(chatJson, true);
                    } catch (InvocationTargetException | IllegalAccessException | InstantiationException |
                             NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }

            } else if (cmdId == WsMessageTypes.Commands.CHAT ||
                    cmdId == WsMessageTypes.Commands.DONATION) {
                lastRecivedMessageTime = System.currentTimeMillis();

                JsonArray bdyArray = parsedMessage.getAsJsonArray("bdy");

                for (var chatJsonElement : bdyArray) {
                    JsonObject chatJson = chatJsonElement.getAsJsonObject();
                    try {
                        processChatMessageFromJson(chatJson, false);
                    } catch (InvocationTargetException | IllegalAccessException | InstantiationException |
                             NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (cmdId == WsMessageTypes.Commands.EVENT) {
                processEvent(parsedMessage.getAsJsonObject("bdy"));
            } else if (cmdId == WsMessageTypes.Commands.PONG ||
                    cmdId == WsMessageTypes.Commands.BLIND ||
                    cmdId == WsMessageTypes.Commands.BLOCK ||
                    cmdId == WsMessageTypes.Commands.KICK ||
                    cmdId == WsMessageTypes.Commands.PENALTY) {
            }

        } catch (Exception ex) {
            for (ChatEventListener listener : listeners) {
                listener.onError(ex);
            }
        }
    }

    private void handleWsClosed(int code, String reason) {
        shutdownExecutor();

        boolean shouldReconnect = autoReconnect;

        for (ChatEventListener listener : listeners) {
            listener.onConnectionClosed(code, reason, true, shouldReconnect);
        }

        if (chzzk.isDebug) {
            System.out.println("Websocket connection closed.");
            System.out.println("Code: " + code);
            System.out.println("Reason: " + reason);
            System.out.println("Reconnect: " + shouldReconnect);
        }

        if (shouldReconnect) {
            reconnectAsync();
        }
    }

    private void handleWsFailure(Throwable t) {
        shutdownExecutor();

        for (ChatEventListener listener : listeners) {
            listener.onError(t instanceof Exception ? (Exception) t : new RuntimeException(t));
        }

        boolean shouldReconnect = autoReconnect;

        for (ChatEventListener listener : listeners) {
            listener.onConnectionClosed(1006, t.getMessage(), true, shouldReconnect);
        }

        if (shouldReconnect) {
            reconnectAsync();
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends WsMessageBase> getClientboundMessageClass(int id) {
        return (Class<? extends WsMessageBase>) CLIENTBOUND_MESSAGES.get(id);
    }

    private <T extends WsMessageBase> T setupWsMessage(T wsMessage) {
        wsMessage.cid = chatId;
        return wsMessage;
    }

    private void processChatMessageFromJson(JsonObject chatJson, boolean isRecentChat) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Class<? extends ChatMessage> clazz = ChatMessage.class;
        int messageTypeCode = chatJson.get(isRecentChat ? "messageTypeCode" : "msgTypeCode").getAsInt();

        if (messageTypeCode == WsMessageTypes.ChatTypes.DONATION) {
            try {
                JsonObject extrasObj = JsonParser.parseString(chatJson.get("extras").getAsString()).getAsJsonObject();
                if (extrasObj.has("donationType")) {
                    String donationType = extrasObj.get("donationType").getAsString();
                    if (donationType.equalsIgnoreCase("MISSION")) {
                        clazz = MissionDonationMessage.class;
                    } else if (donationType.equalsIgnoreCase("MISSION_PARTICIPATION")) {
                        clazz = MissionParticipationDonationMessage.class;
                    } else if (donationType.equalsIgnoreCase("PARTY")) {
                        clazz = PartyDonationMessage.class;
                    } else {
                        clazz = DonationMessage.class;
                    }
                } else {
                    clazz = DonationMessage.class;
                }
            } catch (Exception e) {
                clazz = DonationMessage.class;
            }
        } else if (messageTypeCode == WsMessageTypes.ChatTypes.SUBSCRIPTION) {
            clazz = SubscriptionMessage.class;
        }

        var msg = (ChatMessage) clazz.getDeclaredConstructor().newInstance();
        msg.rawJson = chatJson.toString();
        msg.content = chatJson.get(isRecentChat ? "content" : "msg").getAsString();
        msg.msgTypeCode = messageTypeCode;
        msg.createTime = new Date(chatJson.get(isRecentChat ? "createTime" : "ctime").getAsLong());

        if (chatJson.has("messageStatusType") && !chatJson.get("messageStatusType").isJsonNull()) {
            msg.msgStatusType = chatJson.get("messageStatusType").getAsString();
        } else if (chatJson.has("msgStatusType") && !chatJson.get("msgStatusType").isJsonNull()) {
            msg.msgStatusType = chatJson.get("msgStatusType").getAsString();
        }

        if (chatJson.has("extras") && !chatJson.get("extras").isJsonNull()) {
            msg.extras = gson.fromJson(chatJson.get("extras").getAsString(), ChatMessage.Extras.class);
        }

        if (chatJson.has("profile") && !chatJson.get("profile").isJsonNull()) {
            msg.profile = gson.fromJson(chatJson.get("profile").getAsString(), ChatMessage.Profile.class);
        }

        msg.userIdHash = chatJson.get(isRecentChat ? "userId" : "uid").getAsString();

        processChatMessage(msg, isRecentChat);
    }

    private void processChatMessage(ChatMessage msg, boolean isRecentChat) {
        for (ChatEventListener listener : listeners) {
            if (msg instanceof MissionParticipationDonationMessage) {
                if (isRecentChat) {
                    MissionParticipationDonationMessage missionMsg = (MissionParticipationDonationMessage) msg;
                    if (missionMsg.getMissionStatusRaw() != null) {
                        listener.onMissionDonationParticipation(missionMsg);
                    }
                }
            } else if (msg instanceof MissionDonationMessage) {
                if (isRecentChat) {
                    listener.onMissionDonation((MissionDonationMessage) msg);
                }
            } else if (msg instanceof PartyDonationMessage) {
                listener.onPartyDonationChat((PartyDonationMessage) msg);
            } else if (msg instanceof SubscriptionMessage) {
                listener.onSubscriptionChat((SubscriptionMessage) msg);
            } else if (msg instanceof DonationMessage) {
                listener.onDonationChat((DonationMessage) msg);
            } else {
                listener.onChat(msg);
            }
        }
    }

    private void processEvent(JsonObject data) {
        if (!data.has("type")) return;
        String eventType = data.get("type").getAsString();

        if (eventType.equals("DONATION_MISSION_IN_PROGRESS")) {
            MissionDonationMessage missionMsg = gson.fromJson(data, MissionDonationMessage.class);
            missionMsg.rawJson = data.toString();
            missionMsg.extras = gson.fromJson(data, ChatMessage.Extras.class);
            if (missionMsg.getMissionText() != null) {
                missionMsg.content = missionMsg.getMissionText();
            }
            applyProfileFromEvent(data, missionMsg);
            for (ChatEventListener listener : listeners) {
                listener.onMissionDonation(missionMsg);
            }
        } else if (eventType.equals("DONATION_MISSION_PARTICIPATION")) {
            MissionParticipationDonationMessage missionMsg = gson.fromJson(data, MissionParticipationDonationMessage.class);
            missionMsg.rawJson = data.toString();
            missionMsg.extras = gson.fromJson(data, ChatMessage.Extras.class);
            if (missionMsg.getMissionText() != null) {
                missionMsg.content = missionMsg.getMissionText();
            }
            applyProfileFromEvent(data, missionMsg);
            if (missionMsg.getMissionStatusRaw() != null) {
                for (ChatEventListener listener : listeners) {
                    listener.onMissionDonationParticipation(missionMsg);
                }
            }
        } else if (eventType.equals("PARTY_DONATION_INFO")) {
            PartyDonationInfo partyInfo = gson.fromJson(data, PartyDonationInfo.class);
            partyInfo.rawJson = data.toString();
            for (ChatEventListener listener : listeners) {
                listener.onPartyDonationInfo(partyInfo);
            }
        } else if (eventType.equals("SUBSCRIPTION_GIFT")) {
            SubscriptionGiftEvent giftEvent = gson.fromJson(data, SubscriptionGiftEvent.class);
            giftEvent.rawJson = data.toString();
            for (ChatEventListener listener : listeners) {
                listener.onSubscriptionGift(giftEvent);
            }
        } else if (eventType.equals("SUBSCRIPTION_GIFT_RECEIVER")) {
            SubscriptionGiftReceiverEvent receiverEvent = gson.fromJson(data, SubscriptionGiftReceiverEvent.class);
            receiverEvent.rawJson = data.toString();
            for (ChatEventListener listener : listeners) {
                listener.onSubscriptionGiftReceiver(receiverEvent);
            }
        } else if (eventType.equals("TEMPORARY_RESTRICT")) {
            TemporaryRestrictEvent restrictEvent = gson.fromJson(data, TemporaryRestrictEvent.class);
            restrictEvent.rawJson = data.toString();
            for (ChatEventListener listener : listeners) {
                listener.onTemporaryRestrict(restrictEvent);
            }
        } else if (eventType.equals("CHANGE_DONATION_ACTIVE")) {
            ChangeDonationActiveEvent activeEvent = gson.fromJson(data, ChangeDonationActiveEvent.class);
            activeEvent.rawJson = data.toString();
            for (ChatEventListener listener : listeners) {
                listener.onChangeDonationActive(activeEvent);
            }
        } else if (eventType.equals("PARTY_DONATION_FINISH")) {
            PartyDonationFinishEvent finishEvent = gson.fromJson(data, PartyDonationFinishEvent.class);
            finishEvent.rawJson = data.toString();
            for (ChatEventListener listener : listeners) {
                listener.onPartyDonationFinish(finishEvent);
            }
        } else if (eventType.equals("PARTY_DONATION_CONFIRM")) {
            PartyDonationConfirmEvent confirmEvent = gson.fromJson(data, PartyDonationConfirmEvent.class);
            confirmEvent.rawJson = data.toString();
            for (ChatEventListener listener : listeners) {
                listener.onPartyDonationConfirm(confirmEvent);
            }
        } else if (eventType.equals("IIMS_PENALTY")) {
            IimsPenaltyEvent penaltyEvent = gson.fromJson(data, IimsPenaltyEvent.class);
            penaltyEvent.rawJson = data.toString();
            for (ChatEventListener listener : listeners) {
                listener.onIimsPenalty(penaltyEvent);
            }
        }
    }

    private void applyProfileFromEvent(JsonObject data, ChatMessage msg) {
        if (data.has("nickname") || data.has("verifiedMark")) {
            if (msg.profile == null) {
                msg.profile = new ChatMessage.Profile();
            }
            if (data.has("nickname") && !data.get("nickname").isJsonNull()) {
                msg.profile.nickname = data.get("nickname").getAsString();
            }
            if (data.has("verifiedMark") && !data.get("verifiedMark").isJsonNull()) {
                msg.profile.verifiedMark = data.get("verifiedMark").getAsBoolean();
            }
        }
    }

    private void checkBroadcastEnd() {
        try {
            var status = chzzk.getLiveStatus(channelId);
            if (status == null || !status.isOnline()) {
                System.out.println("[Chzzk] Broadcast ended: " + channelId);
                liveCheckFailCount = 0;
                for (ChatEventListener listener : listeners) {
                    listener.onBroadcastEnd(this);
                }
            } else {
                liveCheckFailCount = 0;
            }
        } catch (Exception e) {
            liveCheckFailCount++;
            System.out.println("[Chzzk] Live check error (" + liveCheckFailCount + "/" + LIVE_CHECK_MAX_FAILURES + "): " + e.getMessage());
            if (liveCheckFailCount >= LIVE_CHECK_MAX_FAILURES) {
                System.out.println("[Chzzk] Live check failed " + LIVE_CHECK_MAX_FAILURES + " times, assuming broadcast ended: " + channelId);
                for (ChatEventListener listener : listeners) {
                    listener.onBroadcastEnd(this);
                }
            }
        }
    }

    private void shutdownExecutor() {
        synchronized (this) {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdownNow();
                executor = null;
            }
        }
    }
}
