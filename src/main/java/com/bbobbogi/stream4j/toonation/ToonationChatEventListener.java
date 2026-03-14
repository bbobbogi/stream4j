package com.bbobbogi.stream4j.toonation;

public interface ToonationChatEventListener {

    default void onConnect(ToonationChat chat, boolean isReconnecting) {}

    default void onConnectionClosed(ToonationChat chat, int code, String reason, boolean remote, boolean tryingToReconnect) {}

    default void onError(Exception ex) {
        ex.printStackTrace();
    }

    default void onDonation(ToonationChat chat, ToonationDonationMessage msg) {}

    default void onBlocked(ToonationChat chat) {}
}
