package xyz.r2turntrue.chzzk4j.chat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import xyz.r2turntrue.chzzk4j.exception.ChatFailedConnectException;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 채팅 웹소켓 클라이언트입니다.
 */
public class ChatWebsocketClient extends WebSocketClient {

    private ChzzkChat chat;
    private Gson gson;
    private String sid;
    private ScheduledExecutorService executor;
    private long lastSendPingTime;
    private long lastRecivedMessageTime;

    /**
     * ChatWebsocketClient를 생성합니다.
     *
     * @param chat 채팅 인스턴스
     * @param websocketUri 웹소켓 URI
     */
    public ChatWebsocketClient(ChzzkChat chat, URI websocketUri) {
        super(websocketUri);
        this.chat = chat;
        this.gson = new Gson()
                .newBuilder()
                .disableHtmlEscaping()
                .create();
    }

    private HashMap<Integer, Class<?>> clientboundMessages = new HashMap<>() {{
        put(WsMessageTypes.Commands.CONNECTED, WsMessageClientboundConnected.class);
        put(WsMessageTypes.Commands.RECENT_CHAT, WsMessageClientboundRecentChat.class);
    }};

    @SuppressWarnings("unchecked")
    private Class<? extends WsMessageBase> getClientboundMessageClass(int id) {
        return (Class<? extends WsMessageBase>) clientboundMessages.get(id);
    }

    private <T extends WsMessageBase> T setupWsMessage(T wsMessage) {
        wsMessage.cid = chat.chatId;

        return wsMessage;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        if (chat.chzzk.isDebug) System.out.println("Connected to websocket! Connecting to chat...");

        // 스레드 누수 방지를 위한 동기화된 executor 관리
        synchronized (this) {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdownNow();
            }
            executor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "chzzk-chat-ws-" + chat.channelId);
                t.setDaemon(true);
                return t;
            });
        }

        lastRecivedMessageTime = lastSendPingTime = System.currentTimeMillis();
        WsMessageServerboundConnect handshake = setupWsMessage(new WsMessageServerboundConnect());
        handshake.bdy = new WsMessageServerboundConnect.Body();
        handshake.bdy.accTkn = chat.accessToken;
        handshake.bdy.auth = chat.chzzk.isLoggedIn() ? "SEND" : "READ";
        handshake.bdy.uid = chat.userId;

        this.send(gson.toJson(handshake));
    }

    private void processChatMessageFromJson(JsonObject chatJson, boolean isRecentChat) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Class<? extends ChatMessage> clazz = ChatMessage.class;
        int messageTypeCode = chatJson.get(isRecentChat ? "messageTypeCode" : "msgTypeCode").getAsInt();

        if (messageTypeCode == WsMessageTypes.ChatTypes.DONATION) {
            // Check donation type
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

        // msgStatusType 처리 (두 가지 필드명 모두 체크)
        if (chatJson.has("messageStatusType") && !chatJson.get("messageStatusType").isJsonNull()) {
            msg.msgStatusType = chatJson.get("messageStatusType").getAsString();
        } else if (chatJson.has("msgStatusType") && !chatJson.get("msgStatusType").isJsonNull()) {
            msg.msgStatusType = chatJson.get("msgStatusType").getAsString();
        }

        // extras 파싱 (null 체크)
        if (chatJson.has("extras") && !chatJson.get("extras").isJsonNull()) {
            msg.extras = gson.fromJson(chatJson.get("extras").getAsString(), ChatMessage.Extras.class);
        }

        // profile 파싱 (null 체크)
        if (chatJson.has("profile") && !chatJson.get("profile").isJsonNull()) {
            msg.profile = gson.fromJson(chatJson.get("profile").getAsString(), ChatMessage.Profile.class);
        }

        msg.userIdHash = chatJson.get(isRecentChat ? "userId" : "uid").getAsString();

        processChatMessage(msg, isRecentChat);
    }

    private void processChatMessage(ChatMessage msg, boolean isRecentChat) {
        for (ChatEventListener listener : chat.listeners) {
            //System.out.println("CC: " + msg.chatTypeCode);
            if (msg instanceof MissionParticipationDonationMessage) {
                // RECENT_CHAT에서만 처리 (실시간 CHAT/DONATION은 EVENT에서도 동일하게 오므로 중복 방지)
                if (isRecentChat) {
                    // status가 null이면 이벤트를 호출하지 않음
                    // RECENT_CHAT으로 오는 미션 완료 후 참여 메시지는 필수 필드가 누락되어 있어 처리하지 않음
                    MissionParticipationDonationMessage missionMsg = (MissionParticipationDonationMessage) msg;
                    if (missionMsg.getMissionStatusRaw() != null) {
                        listener.onMissionDonationParticipation(missionMsg);
                    }
                }
            } else if (msg instanceof MissionDonationMessage) {
                // RECENT_CHAT에서만 처리 (실시간 CHAT/DONATION은 EVENT에서도 동일하게 오므로 중복 방지)
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

    @Override
    public void onMessage(String message) {
        try {
            if (chat.chzzk.isDebug) System.out.println("Message: " + message);

            JsonObject parsedMessage = JsonParser.parseString(message)
                    .getAsJsonObject();

            var cmdId = parsedMessage
                    .get("cmd")
                    .getAsInt();

            var messageClass = getClientboundMessageClass(cmdId);

            if (messageClass == WsMessageClientboundConnected.class) {
                // handle connected message
                WsMessageClientboundConnected msg = gson.fromJson(parsedMessage, WsMessageClientboundConnected.class);
                if (msg.retCode == 0) {
                    if (chat.chzzk.isDebug) System.out.println("Successfully connected!");
                    sid = msg.bdy.sid;
                    for (ChatEventListener listener : chat.listeners) {
                        listener.onConnect(chat, chat.reconnecting);
                    }

                    Runnable task = () -> {
                        if(System.currentTimeMillis() - lastSendPingTime >= 60000 || // 1 minutes from last ping time
                            System.currentTimeMillis() - lastRecivedMessageTime >= 20000) { // 20 seconds later from last message
                            if (chat.chzzk.isDebug) {
                                System.out.println("need client ping: current = " + (System.currentTimeMillis() / 1000) +
                                        ", ping = " + (lastSendPingTime / 1000)  +
                                        ", recived message = " + (lastRecivedMessageTime/1000));
                            }

                            if(isOpen()) {
                                this.send(gson.toJson(new WsMessageServerboundPing()));
                            }

                            lastRecivedMessageTime = lastSendPingTime = System.currentTimeMillis();
                        }
                    };

                    executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
                } else {
                    throw new ChatFailedConnectException(msg.retCode, msg.retMsg);
                }
            } else if (cmdId == WsMessageTypes.Commands.PING) {
                if (chat.chzzk.isDebug) {
                    System.out.println("pong");
                    System.out.println(gson.toJson(new WsMessageServerboundPong()));
                }
                this.send(gson.toJson(new WsMessageServerboundPong()));
            } else if (messageClass == WsMessageClientboundRecentChat.class) {
                JsonObject bdyObj = parsedMessage.getAsJsonObject("bdy");
                var messageList = bdyObj.getAsJsonArray("messageList");

                for (var chatJsonElement : messageList) {
                    JsonObject chatJson = chatJsonElement.getAsJsonObject();

                    try {
                        processChatMessageFromJson(chatJson, true); // RECENT_CHAT
                    } catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
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
                        processChatMessageFromJson(chatJson, false); // 실시간 CHAT/DONATION
                    } catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (cmdId == WsMessageTypes.Commands.EVENT) {
                JsonObject data = parsedMessage.getAsJsonObject("bdy");

                if (data.has("type")) {
                    String eventType = data.get("type").getAsString();

                    // 미션 후원 진행 상태 업데이트
                    // 미션이 시작되거나 완료되었을 때 전송됨
                    if (eventType.equals("DONATION_MISSION_IN_PROGRESS")) {
                        MissionDonationMessage missionMsg = gson.fromJson(data, MissionDonationMessage.class);
                        missionMsg.rawJson = data.toString();

                        // bdy를 extras로 파싱
                        missionMsg.extras = gson.fromJson(data, ChatMessage.Extras.class);

                        // missionText를 content로 매핑
                        if (missionMsg.getMissionText() != null) {
                            missionMsg.content = missionMsg.getMissionText();
                        }

                        // nickname, verifiedMark를 profile로 매핑
                        if (data.has("nickname") || data.has("verifiedMark")) {
                            if (missionMsg.profile == null) {
                                missionMsg.profile = new ChatMessage.Profile();
                            }
                            if (data.has("nickname") && !data.get("nickname").isJsonNull()) {
                                missionMsg.profile.nickname = data.get("nickname").getAsString();
                            }
                            if (data.has("verifiedMark") && !data.get("verifiedMark").isJsonNull()) {
                                missionMsg.profile.verifiedMark = data.get("verifiedMark").getAsBoolean();
                            }
                        }

                        for (ChatEventListener listener : chat.listeners) {
                            listener.onMissionDonation(missionMsg);
                        }
                    }
                    // 미션 참여 후원 이벤트
                    // 누군가 미션에 후원으로 참여했을 때 전송됨
                    else if (eventType.equals("DONATION_MISSION_PARTICIPATION")) {
                        MissionParticipationDonationMessage missionMsg = gson.fromJson(data, MissionParticipationDonationMessage.class);
                        missionMsg.rawJson = data.toString();

                        // bdy를 extras로 파싱
                        missionMsg.extras = gson.fromJson(data, ChatMessage.Extras.class);

                        // missionText를 content로 매핑
                        if (missionMsg.getMissionText() != null) {
                            missionMsg.content = missionMsg.getMissionText();
                        }

                        // nickname, verifiedMark를 profile로 매핑
                        if (data.has("nickname") || data.has("verifiedMark")) {
                            if (missionMsg.profile == null) {
                                missionMsg.profile = new ChatMessage.Profile();
                            }
                            if (data.has("nickname") && !data.get("nickname").isJsonNull()) {
                                missionMsg.profile.nickname = data.get("nickname").getAsString();
                            }
                            if (data.has("verifiedMark") && !data.get("verifiedMark").isJsonNull()) {
                                missionMsg.profile.verifiedMark = data.get("verifiedMark").getAsBoolean();
                            }
                        }

                        // status가 null이면 이벤트를 호출하지 않음
                        // RECENT_CHAT으로 오는 미션 완료 후 참여 메시지는 필수 필드가 누락되어 있어 처리하지 않음
                        if (missionMsg.getMissionStatusRaw() != null) {
                            for (ChatEventListener listener : chat.listeners) {
                                listener.onMissionDonationParticipation(missionMsg);
                            }
                        }
                    }
                    // 파티 후원 정보 업데이트
                    // 파티 후원의 멤버 수, 총 금액 등이 변경되었을 때 전송됨
                    else if (eventType.equals("PARTY_DONATION_INFO")) {
                        PartyDonationInfo partyInfo = gson.fromJson(data, PartyDonationInfo.class);
                        partyInfo.rawJson = data.toString();

                        for (ChatEventListener listener : chat.listeners) {
                            listener.onPartyDonationInfo(partyInfo);
                        }
                    }
                    // 구독권 선물 이벤트 (발신자)
                    // 누군가 구독권을 선물했을 때 전송됨 (선물하는 사람 정보)
                    else if (eventType.equals("SUBSCRIPTION_GIFT")) {
                        SubscriptionGiftEvent giftEvent = gson.fromJson(data, SubscriptionGiftEvent.class);
                        giftEvent.rawJson = data.toString();

                        for (ChatEventListener listener : chat.listeners) {
                            listener.onSubscriptionGift(giftEvent);
                        }
                    }
                    // 구독권 선물 수신자 이벤트
                    // 구독권을 받는 각 사람마다 개별 이벤트로 전송됨
                    else if (eventType.equals("SUBSCRIPTION_GIFT_RECEIVER")) {
                        SubscriptionGiftReceiverEvent receiverEvent = gson.fromJson(data, SubscriptionGiftReceiverEvent.class);
                        receiverEvent.rawJson = data.toString();

                        for (ChatEventListener listener : chat.listeners) {
                            listener.onSubscriptionGiftReceiver(receiverEvent);
                        }
                    }
                    // 임시 제재 이벤트
                    // 채팅 사용자가 임시 제재를 받았을 때 전송됨
                    else if (eventType.equals("TEMPORARY_RESTRICT")) {
                        TemporaryRestrictEvent restrictEvent = gson.fromJson(data, TemporaryRestrictEvent.class);
                        restrictEvent.rawJson = data.toString();

                        for (ChatEventListener listener : chat.listeners) {
                            listener.onTemporaryRestrict(restrictEvent);
                        }
                    }
                    // 후원 활성화 상태 변경 이벤트
                    // 특정 후원 타입(예: 파티 후원)이 열리거나 닫힐 때 전송됨
                    else if (eventType.equals("CHANGE_DONATION_ACTIVE")) {
                        ChangeDonationActiveEvent activeEvent = gson.fromJson(data, ChangeDonationActiveEvent.class);
                        activeEvent.rawJson = data.toString();

                        for (ChatEventListener listener : chat.listeners) {
                            listener.onChangeDonationActive(activeEvent);
                        }
                    }
                    // 파티 후원 종료 이벤트
                    // 파티 후원이 종료되었을 때 전송됨
                    else if (eventType.equals("PARTY_DONATION_FINISH")) {
                        PartyDonationFinishEvent finishEvent = gson.fromJson(data, PartyDonationFinishEvent.class);
                        finishEvent.rawJson = data.toString();

                        for (ChatEventListener listener : chat.listeners) {
                            listener.onPartyDonationFinish(finishEvent);
                        }
                    }
                    // 파티 후원 확인/정산 이벤트
                    // 파티 후원 종료 후 각 채널별 순위 정보가 전송됨
                    else if (eventType.equals("PARTY_DONATION_CONFIRM")) {
                        PartyDonationConfirmEvent confirmEvent = gson.fromJson(data, PartyDonationConfirmEvent.class);
                        confirmEvent.rawJson = data.toString();

                        for (ChatEventListener listener : chat.listeners) {
                            listener.onPartyDonationConfirm(confirmEvent);
                        }
                    }
                    // IIMS 페널티 이벤트
                    // 부적절한 콘텐츠로 인한 사용자 제재 시 전송됨
                    else if (eventType.equals("IIMS_PENALTY")) {
                        IimsPenaltyEvent penaltyEvent = gson.fromJson(data, IimsPenaltyEvent.class);
                        penaltyEvent.rawJson = data.toString();

                        for (ChatEventListener listener : chat.listeners) {
                            listener.onIimsPenalty(penaltyEvent);
                        }
                    }
                }
            } else if (cmdId == WsMessageTypes.Commands.PONG ||
                    cmdId == WsMessageTypes.Commands.BLIND ||
                    cmdId == WsMessageTypes.Commands.BLOCK ||
                    cmdId == WsMessageTypes.Commands.KICK ||
                    cmdId == WsMessageTypes.Commands.PENALTY) {
            }

        } catch (Exception ex) {
            for (ChatEventListener listener : chat.listeners) {
                listener.onError(ex);
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        boolean shouldReconnect = remote && chat.autoReconnect;

        for (ChatEventListener listener : chat.listeners) {
            listener.onConnectionClosed(code, reason, remote, shouldReconnect);
        }

        if (chat.chzzk.isDebug) {
            System.out.println("Websocket connection closed.");
            System.out.println("Code: " + code);
            System.out.println("Reason: " + reason);
            System.out.println("Remote Close: " + remote);
            System.out.println("Reconnect: " + shouldReconnect);
        }

        if (shouldReconnect) {
            // 재연결 시 executor를 종료하지 않음 - onOpen에서 정리됨
            chat.reconnectAsync();
        } else {
            // 재연결하지 않을 때만 executor 종료
            synchronized (this) {
                if (executor != null && !executor.isShutdown()) {
                    executor.shutdownNow();
                }
            }
        }
    }

    @Override
    public void onError(Exception ex) {
        for (ChatEventListener listener : chat.listeners) {
            listener.onError(ex);
        }
    }

    /**
     * 채팅 메시지를 전송합니다.
     *
     * @param content 전송할 메시지 내용
     */
    public void sendChat(String content) {
        var msg = setupWsMessage(new WsMessageServerboundSendChat());
        msg.sid = sid;
        var extras = new WsMessageServerboundSendChat.Body.Extras();
        extras.streamingChannelId = chat.channelId;
        msg.bdy.extras = gson.toJson(extras);
        msg.bdy.msg = content;
        msg.bdy.msgTypeCode = WsMessageTypes.ChatTypes.TEXT;

        send(gson.toJson(msg));
    }

    /**
     * 최근 채팅 메시지를 요청합니다.
     *
     * @param chatCount 요청할 채팅 메시지 수
     */
    public void requestRecentChat(int chatCount) {
        var msg = setupWsMessage(new WsMessageServerboundRequestRecentChat());
        msg.tid = 2;
        msg.bdy.recentMessageCount = chatCount;
        msg.sid = sid;
        //System.out.println(gson.toJson(msg));
        send(gson.toJson(msg));
    }
}
