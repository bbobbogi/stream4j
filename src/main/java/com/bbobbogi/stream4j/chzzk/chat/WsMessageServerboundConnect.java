package com.bbobbogi.stream4j.chzzk.chat;

public class WsMessageServerboundConnect extends WsMessageBase {
    public WsMessageServerboundConnect() {
        super(WsMessageTypes.Commands.CONNECT);
    }

    public static class Body {
        public String accTkn;
        public String auth;
        public int devType = 2001;
        public String uid;
     }

     public Body bdy;
     public int tid = 1;
}
