package xyz.r2turntrue.chzzk4j.chat;

import com.google.gson.Gson;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

class WsMessageClientboundChat extends WsMessageBase {
    public WsMessageClientboundChat() {
        super(WsMessageTypes.Commands.CHAT);
    }

    static class Chat {
        public String uid;
        public String msg;
        public int msgTypeCode;
        public long msgTime;
        public long ctime;
        public String extras;
        public String profile;
        public String msgStatusType;
        public int mbrCnt;

        public <T extends ChatMessage> T toChatMessage(Class<T> clazz, String rawJson) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
            var msg = (T) clazz.getDeclaredConstructor().newInstance();

            msg.rawJson = rawJson;

            msg.content = this.msg;
            msg.msgTypeCode = msgTypeCode;
            msg.messageTime =new Date(msgTime);
            msg.createTime = new Date(ctime);
            msg.msgStatusType = msgStatusType;
            msg.memberCount = mbrCnt;
            msg.extras = new Gson().fromJson(extras, ChatMessage.Extras.class);
            msg.profile = new Gson().fromJson(profile, ChatMessage.Profile.class);
            msg.userIdHash = uid;
            return msg;
        }
    }

    public Chat[] bdy;
}
