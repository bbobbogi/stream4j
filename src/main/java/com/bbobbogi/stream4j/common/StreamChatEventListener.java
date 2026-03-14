package com.bbobbogi.stream4j.common;

public interface StreamChatEventListener {

    default void onConnect(DonationPlatform platform, String channelId) {}

    default void onChat(DonationPlatform platform, String channelId, String nickname, String message) {}

    default void onDonation(Donation donation) {}

    default void onBroadcastEnd(DonationPlatform platform, String channelId) {}

    default void onDisconnect(DonationPlatform platform, String channelId, String reason) {}

    default void onError(DonationPlatform platform, String channelId, Exception ex) {}
}
