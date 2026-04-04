package io.github.bbobbogi.stream4j.chzzk.chat;

import com.google.gson.Gson;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

public class WsMessageClientboundRecentChat extends WsMessageBase {
    public static class Body {
        static class RecentChat {
            // 최근 메시지 필드명 (실시간 채팅과 다름)
            public String userId;           // 실시간: uid
            public String content;          // 실시간: msg
            public int messageTypeCode;     // 실시간: msgTypeCode
            public long messageTime;        // 실시간: msgTime
            public long createTime;         // 실시간: ctime
            public String extras;
            public String profile;
            public String messageStatusType;// 실시간: msgStatusType
            public int memberCount;         // 실시간: mbrCnt

            public ChatMessage toChatMessage(Class<? extends ChatMessage> clazz, String rawJson) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
                var msg = (ChatMessage) clazz.getDeclaredConstructor().newInstance();
                msg.rawJson = rawJson;
                msg.content = content;
                msg.msgTypeCode = messageTypeCode;
                msg.messageTime =new Date(messageTime);
                msg.createTime = new Date(createTime);
                msg.msgStatusType = messageStatusType;
                msg.memberCount = memberCount;
                msg.extras = new Gson().fromJson(extras, ChatMessage.Extras.class);
                msg.profile = new Gson().fromJson(profile, ChatMessage.Profile.class);
                msg.userIdHash = userId;
                return msg;
            }
        }

        public RecentChat[] messageList;
        public int userCount;
    }

    public WsMessageClientboundRecentChat() {
        super(WsMessageTypes.Commands.RECENT_CHAT);
    }

    public Body bdy;
}
