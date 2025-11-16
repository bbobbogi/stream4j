package xyz.r2turntrue.chzzk4j.chat;

public interface ChatEventListener {
    default void onConnect(ChzzkChat chat, boolean isReconnecting) {}

    default void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {}

    default void onError(Exception ex) {
        ex.printStackTrace();
    }

    default void onChat(ChatMessage msg) {}

    default void onDonationChat(DonationMessage msg) {}

    default void onMissionDonationChat(MissionDonationMessage msg) {}

    default void onPartyDonationChat(PartyDonationMessage msg) {}

    default void onSubscriptionChat(SubscriptionMessage msg) {}

    // Mission event handlers
    default void onMissionDonation(MissionDonationMessage msg) {}

    default void onMissionDonationParticipation(MissionParticipationDonationMessage msg) {}

    // Party donation event handlers
    default void onPartyDonationInfo(PartyDonationInfo info) {}

    // Subscription gift event handlers
    default void onSubscriptionGift(SubscriptionGiftEvent event) {}

    default void onSubscriptionGiftReceiver(SubscriptionGiftReceiverEvent event) {}

    // Temporary restrict event handler
    default void onTemporaryRestrict(TemporaryRestrictEvent event) {}

    // Donation state change handlers
    default void onChangeDonationActive(ChangeDonationActiveEvent event) {}

    // Party donation lifecycle handlers
    default void onPartyDonationFinish(PartyDonationFinishEvent event) {}

    default void onPartyDonationConfirm(PartyDonationConfirmEvent event) {}

    // Penalty event handler
    default void onIimsPenalty(IimsPenaltyEvent event) {}
}
