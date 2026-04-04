package io.github.bbobbogi.stream4j.soop;

import io.github.bbobbogi.stream4j.soop.chat.SOOPDonationMessage;
import io.github.bbobbogi.stream4j.soop.chat.SOOPMissionEvent;

/**
 * Event listener for SOOP chat and donation events.
 *
 * <p>Callbacks are invoked on internal WebSocket/network threads.
 * Avoid long blocking work inside callbacks.
 *
 * @since 1.0.0
 */
public interface SOOPChatEventListener {

    /**
     * Called after a WebSocket connection is established.
     *
     * <p>Invoked on an internal WebSocket callback thread.
     *
     * @param chat active SOOP chat client
     * @param isReconnecting {@code true} if this connection is a reconnect
     */
    default void onConnect(SOOPChat chat, boolean isReconnecting) {
    }

    /**
     * Called when a chat message is received.
     *
     * <p>Invoked on an internal WebSocket callback thread.
     *
     * @param userId sender user ID
     * @param username sender display name
     * @param message chat message text
     */
    default void onChat(String userId, String username, String message) {
    }

    /**
     * Called when a donation event is received.
     *
     * <p>Invoked on an internal WebSocket callback thread.
     *
     * @param chat active SOOP chat client
     * @param msg parsed donation message payload
     */
    default void onDonation(SOOPChat chat, SOOPDonationMessage msg) {
    }

    /**
     * Called when a subscription continuation event is received.
     *
     * <p>Invoked on an internal WebSocket callback thread.
     *
     * @param chat active SOOP chat client
     * @param from subscriber user ID
     * @param fromUsername subscriber display name
     * @param monthCount continuous subscription months
     * @param tier subscription tier level
     */
    default void onSubscribe(SOOPChat chat, String from, String fromUsername, int monthCount, int tier) {
    }

    /**
     * Called when a new subscription starts.
     *
     * <p>Invoked on an internal WebSocket callback thread.
     *
     * @param chat active SOOP chat client
     * @param userId subscriber user ID
     * @param nickname subscriber display name
     * @param duration subscription duration value from SOOP payload
     */
    default void onNewSubscribe(SOOPChat chat, String userId, String nickname, int duration) {
    }

    /**
     * Called when a subscription gift event is received.
     *
     * <p>Invoked on an internal WebSocket callback thread.
     *
     * @param chat active SOOP chat client
     * @param gifterUserId gifter user ID
     * @param gifterNickname gifter display name
     * @param recipientUserId recipient user ID
     * @param recipientNickname recipient display name
     * @param months gifted subscription months
     */
    default void onSubscriptionGift(SOOPChat chat, String gifterUserId, String gifterNickname, String recipientUserId, String recipientNickname, int months) {
    }

    /**
     * Called when a mission event payload is received.
     *
     * <p>Invoked on an internal WebSocket callback thread.
     *
     * @param chat active SOOP chat client
     * @param event parsed mission event data
     */
    default void onMission(SOOPChat chat, SOOPMissionEvent event) {
    }

    /**
     * Called when the connection is closed.
     *
     * <p>Invoked on an internal WebSocket callback thread.
     *
     * @param code close status code
     * @param reason close reason message
     * @param remote {@code true} if closure was initiated remotely
     * @param tryingToReconnect {@code true} if reconnect logic will run
     */
    default void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {
    }

    /**
     * Called when the broadcast is determined to be ended.
     *
     * <p>Invoked on an internal WebSocket callback thread.
     *
     * @param chat active SOOP chat client
     */
    default void onBroadcastEnd(SOOPChat chat) {
    }

    /**
     * Called when an unhandled packet type is received.
     *
     * <p>Invoked on an internal WebSocket callback thread.
     *
     * @param typeCode packet type code
     * @param fields packet payload fields split by SOOP separator
     */
    default void onUnhandledPacket(String typeCode, String[] fields) {
    }

    /**
     * Called when an exception occurs in connection or parsing flow.
     *
     * <p>Invoked on an internal WebSocket callback thread.
     *
     * @param ex raised exception
     */
    default void onError(Exception ex) {
        ex.printStackTrace();
    }
}
