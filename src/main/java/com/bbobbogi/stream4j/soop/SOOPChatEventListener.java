package com.bbobbogi.stream4j.soop;

public interface SOOPChatEventListener {

    default void onConnect(SOOPChat chat) {
    }

    default void onChatRoomEntered(SOOPChat chat) {
    }

    default void onChat(SOOPChat chat, String userId, String username, String message) {
    }

    default void onDonation(SOOPChat chat, SOOPDonationMessage msg) {
    }

    default void onSubscribe(SOOPChat chat, String from, String fromUsername, int monthCount, int tier) {
    }

    default void onConnectionClosed(SOOPChat chat, String reason, boolean tryingToReconnect) {
    }

    default void onError(Exception ex) {
        ex.printStackTrace();
    }
}
