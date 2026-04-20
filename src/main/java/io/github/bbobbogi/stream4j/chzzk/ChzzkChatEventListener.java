package io.github.bbobbogi.stream4j.chzzk;
import io.github.bbobbogi.stream4j.chzzk.chat.*;

/**
 * Listener interface for receiving chat events.
 *
 * @since 1.0.0
 */
public interface ChzzkChatEventListener {
    /**
     * Called when connected to the chat server.
     *
     * @param chat connected chat instance
     * @param isReconnecting whether this is a reconnection
     */
    default void onConnect(ChzzkChat chat, boolean isReconnecting) {}

    /**
     * Called when the connection to the chat server is closed.
     *
     * @param code close code
     * @param reason close reason
     * @param remote whether it was closed remotely
     * @param tryingToReconnect whether reconnection is being attempted
     */
    default void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {}

    /**
     * Called when the broadcast ends.
     * Broadcast end is detected by polling live status every 30 seconds.
     *
     * @param chat chat instance
     */
    default void onBroadcastEnd(ChzzkChat chat) {}

    /**
     * Called when an error occurs.
     *
     * @param ex occurred exception
     */
    default void onError(Exception ex) {
        ex.printStackTrace();
    }

    /**
     * Called when a normal chat message is received.
     *
     * @param msg received chat message
     */
    default void onChat(ChatMessage msg) {}

    /**
     * Called when a system message is received (msgTypeCode=30).
     *
     * @param msg received system message
     */
    default void onSystemMessage(ChatMessage msg) {}

    /**
     * Called when a donation chat message is received.
     *
     * @param msg received donation message
     */
    default void onDonationChat(DonationMessage msg) {}

    /**
     * Called when a mission donation chat message is received.
     *
     * @param msg received mission donation message
     */
    default void onMissionDonationChat(MissionDonationMessage msg) {}

    /**
     * Called when a party donation chat message is received.
     *
     * @param msg received party donation message
     */
    default void onPartyDonationChat(PartyDonationMessage msg) {}

    /**
     * Called when a subscription chat message is received.
     *
     * @param msg received subscription message
     */
    default void onSubscriptionChat(SubscriptionMessage msg) {}

    /**
     * Called when a mission donation event is received.
     *
     * @param msg received mission donation message
     */
    default void onMissionDonation(MissionDonationMessage msg) {}

    /**
     * Called when a mission participation donation event is received.
     *
     * @param msg received mission participation donation message
     */
    default void onMissionDonationParticipation(MissionParticipationDonationMessage msg) {}

    /**
     * Called when a party donation info event is received.
     *
     * @param info party donation information
     */
    default void onPartyDonationInfo(PartyDonationInfo info) {}

    /**
     * Called when a subscription gift event is received.
     *
     * @param event subscription gift event
     */
    default void onSubscriptionGift(SubscriptionGiftEvent event) {}

    /**
     * Called when a subscription gift receiver event is received.
     *
     * @param event subscription gift receiver event
     */
    default void onSubscriptionGiftReceiver(SubscriptionGiftReceiverEvent event) {}

    /**
     * Called when a temporary restriction event is received.
     *
     * @param event temporary restriction event
     */
    default void onTemporaryRestrict(TemporaryRestrictEvent event) {}

    /**
     * Called when a donation activation state change event is received.
     *
     * @param event donation activation state change event
     */
    default void onChangeDonationActive(ChangeDonationActiveEvent event) {}

    /**
     * Called when a party donation finish event is received.
     *
     * @param event party donation finish event
     */
    default void onPartyDonationFinish(PartyDonationFinishEvent event) {}

    /**
     * Called when a party donation confirm event is received.
     *
     * @param event party donation confirm event
     */
    default void onPartyDonationConfirm(PartyDonationConfirmEvent event) {}

    /**
     * Called when an IIMS penalty event is received.
     *
     * @param event IIMS penalty event
     */
    default void onIimsPenalty(IimsPenaltyEvent event) {}
}
