package com.bbobbogi.stream4j.chzzk.chat;

class WsMessageServerboundRequestRecentChat extends WsMessageBase {
    public WsMessageServerboundRequestRecentChat() {
        super(WsMessageTypes.Commands.REQUEST_RECENT_CHAT);
    }

    static class Body {
        public int recentMessageCount;
    }

    public WsMessageServerboundRequestRecentChat.Body bdy = new WsMessageServerboundRequestRecentChat.Body();
    public String sid;
    public int tid = 2;
}
