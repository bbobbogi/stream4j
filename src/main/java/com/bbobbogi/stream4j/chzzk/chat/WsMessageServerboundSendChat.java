package com.bbobbogi.stream4j.chzzk.chat;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;

public class WsMessageServerboundSendChat extends WsMessageBase {
    public static class Body {
        public static class Extras {
            String chatType = "STREAMING";
            String osType = "PC";
            public String streamingChannelId = "";
            String emojis = "";
        }

        public String extras;
        public String msg;
        public long msgTime = System.currentTimeMillis();
        public int msgTypeCode = WsMessageTypes.ChatTypes.TEXT;
    }

    public WsMessageServerboundSendChat() {
        super(WsMessageTypes.Commands.SEND_CHAT);
    }

    public Body bdy = new Body();

    public int tid = 3;
    public String sid;
}
