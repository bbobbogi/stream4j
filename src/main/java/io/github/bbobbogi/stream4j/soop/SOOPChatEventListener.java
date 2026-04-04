package io.github.bbobbogi.stream4j.soop;

import io.github.bbobbogi.stream4j.soop.chat.SOOPDonationMessage;
import io.github.bbobbogi.stream4j.soop.chat.SOOPMissionEvent;

public interface SOOPChatEventListener {

    default void onConnect(SOOPChat chat, boolean isReconnecting) {
    }

    default void onChat(String userId, String username, String message) {
    }

    default void onDonation(SOOPChat chat, SOOPDonationMessage msg) {
    }

    default void onSubscribe(SOOPChat chat, String from, String fromUsername, int monthCount, int tier) {
    }

    default void onNewSubscribe(SOOPChat chat, String userId, String nickname, int duration) {
    }

    default void onSubscriptionGift(SOOPChat chat, String gifterUserId, String gifterNickname, String recipientUserId, String recipientNickname, int months) {
    }

    default void onMission(SOOPChat chat, SOOPMissionEvent event) {
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
