package io.github.bbobbogi.stream4j.toonation;

import io.github.bbobbogi.stream4j.toonation.chat.ToonationDonationMessage;

/**
 * Event listener for Toonation donation alert events.
 *
 * <p>Toonation emits donation alerts only and does not emit chat messages.
 * Callbacks are invoked on internal WebSocket/network threads.
 *
 * @since 1.0.0
 */
public interface ToonationChatEventListener {

    /**
     * Called after a WebSocket connection is established.
     *
     * <p>Invoked on an internal WebSocket callback thread.
     *
     * @param chat active Toonation chat client
     * @param isReconnecting {@code true} if this connection is a reconnect
     */
    default void onConnect(ToonationChat chat, boolean isReconnecting) {}

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
    default void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {}

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

    /**
     * Called when a donation alert is received.
     *
     * <p>Invoked on an internal WebSocket callback thread.
     *
     * @param chat active Toonation chat client
     * @param msg parsed donation alert payload
     */
    default void onDonation(ToonationChat chat, ToonationDonationMessage msg) {}

    /**
     * Called when Toonation indicates broadcast end semantics.
     *
     * <p>Invoked on an internal WebSocket callback thread.
     *
     * @param chat active Toonation chat client
     */
    default void onBroadcastEnd(ToonationChat chat) {}

    /**
     * Called when the alertbox is blocked by Toonation.
     *
     * <p>Invoked on an internal WebSocket callback thread.
     *
     * @param chat active Toonation chat client
     */
    default void onBlocked(ToonationChat chat) {}
}
