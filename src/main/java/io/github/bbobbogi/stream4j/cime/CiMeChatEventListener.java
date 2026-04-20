package io.github.bbobbogi.stream4j.cime;

import io.github.bbobbogi.stream4j.cime.chat.CiMeChatMessage;
import io.github.bbobbogi.stream4j.cime.chat.CiMeSubscriptionMessage;
import io.github.bbobbogi.stream4j.cime.chat.CiMeSubscriptionGiftMessage;

/**
 * Listener for CiMe chat events.
 *
 * @since 1.0.0
 */
public interface CiMeChatEventListener {
    /**
     * Called when the chat client connects.
     *
     * @param chat connected chat instance
     * @param isReconnecting whether this connection is from a reconnect attempt
     */
    default void onConnect(CiMeChat chat, boolean isReconnecting) {}

    /**
     * Called when the chat connection closes.
     *
     * @param code close status code
     * @param reason close reason
     * @param remote whether closure was initiated remotely
     * @param tryingToReconnect whether a reconnect attempt will follow
     */
    default void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {}

    /**
     * Called when the live broadcast ends.
     *
     * @param chat chat instance for the ended broadcast
     */
    default void onBroadcastEnd(CiMeChat chat) {}

    /**
     * Called when an error occurs.
     *
     * @param ex exception raised by the client
     */
    default void onError(Exception ex) {
        ex.printStackTrace();
    }

    /**
     * Called when a regular chat message is received.
     *
     * @param msg received chat message
     */
    default void onChat(CiMeChatMessage msg) {}

    /**
     * Called when a subscription event is received.
     *
     * @param msg parsed subscription payload
     */
    default void onSubscription(CiMeSubscriptionMessage msg) {}

    /**
     * Called when a gifted subscription event is received.
     *
     * @param msg parsed gifted subscription payload
     */
    default void onSubscriptionGift(CiMeSubscriptionGiftMessage msg) {}

    /**
     * Called when a non-chat system event is received.
     *
     * @param eventName event name
     * @param rawJson raw JSON payload
     */
    default void onEvent(String eventName, String rawJson) {}
}
