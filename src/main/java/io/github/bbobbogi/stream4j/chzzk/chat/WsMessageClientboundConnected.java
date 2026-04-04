package io.github.bbobbogi.stream4j.chzzk.chat;

public class WsMessageClientboundConnected extends WsMessageBase {
    public static class Body {
        public String sid;
    }

    public WsMessageClientboundConnected.Body bdy = new Body();
    public int retCode;
    public String retMsg;
}
