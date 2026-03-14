package xyz.r2turntrue.chzzk4j.cime;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;
/**
 * ci.me 채팅 웹소켓 클라이언트입니다.
 * Amazon IVS Chat 프로토콜을 사용합니다.
 */
class CiMeChatWebsocketClient extends WebSocketClient {

    private final CiMeChat chat;
    private final Gson gson;

    /**
     * CiMeChatWebsocketClient를 생성합니다.
     *
     * @param chat 채팅 인스턴스
     * @param serverUri 웹소켓 URI
     * @param protocolDraft 웹소켓 Draft (토큰을 서브프로토콜로 포함)
     * @param httpHeaders 추가 HTTP 헤더
     */
    CiMeChatWebsocketClient(CiMeChat chat, URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders) {
        super(serverUri, protocolDraft, httpHeaders, 10000);
        setConnectionLostTimeout(0);
        this.chat = chat;
        this.gson = new Gson();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        if (chat.isDebug) System.out.println("[CiMe] WebSocket connected!");

        for (CiMeChatEventListener listener : chat.listeners) {
            listener.onConnect(chat, chat.reconnecting);
        }
    }

    @Override
    public void onMessage(String message) {
        try {
            if (chat.isDebug) System.out.println("[CiMe] Message: " + message);

            JsonObject parsed = JsonParser.parseString(message).getAsJsonObject();

            if (!parsed.has("Type")) return;

            String type = parsed.get("Type").getAsString();

            if ("MESSAGE".equals(type)) {
                processMessage(parsed);
            } else if ("EVENT".equals(type)) {
                processEvent(parsed);
            } else if ("ERROR".equals(type)) {
                String errorMessage = parsed.has("Message") ? parsed.get("Message").getAsString() : "Unknown error";
                for (CiMeChatEventListener listener : chat.listeners) {
                    listener.onError(new RuntimeException("[CiMe] Server error: " + errorMessage));
                }
            }
        } catch (Exception ex) {
            for (CiMeChatEventListener listener : chat.listeners) {
                listener.onError(ex);
            }
        }
    }

    private void processMessage(JsonObject parsed) {
        CiMeChatMessage msg = new CiMeChatMessage();
        msg.rawJson = parsed.toString();
        msg.id = parsed.has("Id") ? parsed.get("Id").getAsString() : null;
        msg.type = "MESSAGE";
        msg.content = parsed.has("Content") ? parsed.get("Content").getAsString() : "";

        // SendTime 파싱 (ISO 8601 형식)
        if (parsed.has("SendTime") && !parsed.get("SendTime").isJsonNull()) {
            try {
                String sendTimeStr = parsed.get("SendTime").getAsString();
                Instant instant = Instant.parse(sendTimeStr);
                msg.sendTime = Date.from(instant);
            } catch (Exception e) {
                if (chat.isDebug) System.out.println("[CiMe] Failed to parse SendTime: " + e.getMessage());
            }
        }

        // Sender 파싱
        if (parsed.has("Sender") && !parsed.get("Sender").isJsonNull()) {
            JsonObject sender = parsed.getAsJsonObject("Sender");
            msg.senderUserId = sender.has("UserId") ? sender.get("UserId").getAsString() : null;

            // Sender.Attributes.user는 JSON 문자열로 인코딩되어 있음
            if (sender.has("Attributes") && !sender.get("Attributes").isJsonNull()) {
                JsonObject senderAttrs = sender.getAsJsonObject("Attributes");
                if (senderAttrs.has("user") && !senderAttrs.get("user").isJsonNull()) {
                    try {
                        String userJsonStr = senderAttrs.get("user").getAsString();
                        msg.user = gson.fromJson(userJsonStr, CiMeChatMessage.CiMeUser.class);
                    } catch (Exception e) {
                        if (chat.isDebug)
                            System.out.println("[CiMe] Failed to parse user: " + e.getMessage());
                    }
                }
            }
        }

        for (CiMeChatEventListener listener : chat.listeners) {
            listener.onChat(msg);
        }
    }

    /** 필터링할 이벤트 이름 목록 (리스너에 전달하지 않음) */
    private static final Set<String> IGNORED_EVENTS = Set.of(
            "MIDROLL_START",
            "BAN_USER",
            "aws:DISCONNECT_USER",
            "UPDATE_CHAT_NOTICE",
            "LIVE_STARTED",
            "GRANT_MANAGER",
            "REVOKE_MANAGER",
            "CHAT_MODE_NOTICE",
            "UPDATE_CHAT_MODE"
    );

    private void processEvent(JsonObject parsed) {
        String eventName = parsed.has("EventName") ? parsed.get("EventName").getAsString() : "UNKNOWN";

        if (IGNORED_EVENTS.contains(eventName)) {
            if (chat.isDebug) System.out.println("[CiMe] Ignored event: " + eventName);
            return;
        }

        for (CiMeChatEventListener listener : chat.listeners) {
            listener.onEvent(eventName, parsed.toString());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        boolean shouldReconnect = remote && chat.autoReconnect;

        for (CiMeChatEventListener listener : chat.listeners) {
            listener.onConnectionClosed(code, reason, remote, shouldReconnect);
        }

        if (chat.isDebug) {
            System.out.println("[CiMe] WebSocket closed.");
            System.out.println("Code: " + code);
            System.out.println("Reason: " + reason);
            System.out.println("Remote Close: " + remote);
            System.out.println("Reconnect: " + shouldReconnect);
        }

        if (shouldReconnect) {
            chat.reconnectAsync();
        }
    }

    @Override
    public void onError(Exception ex) {
        for (CiMeChatEventListener listener : chat.listeners) {
            listener.onError(ex);
        }
    }
}
