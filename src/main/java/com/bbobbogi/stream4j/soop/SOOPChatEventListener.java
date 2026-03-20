package com.bbobbogi.stream4j.soop;

public interface SOOPChatEventListener {

    default void onConnect(SOOPChat chat, boolean isReconnecting) {
    }

    default void onChat(String userId, String username, String message) {
    }

    default void onDonation(SOOPChat chat, SOOPDonationMessage msg) {
    }

    default void onSubscribe(SOOPChat chat, String from, String fromUsername, int monthCount, int tier) {
    }

    default void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {
    }

    default void onBroadcastEnd(SOOPChat chat) {
    }

    default void onUnhandledPacket(String typeCode, String[] fields) {
    }

    default void onError(Exception ex) {
        ex.printStackTrace();
    }
}
