package io.github.bbobbogi.stream4j.common;

/**
 * Common listener interface for platform chat events.
 *
 * <p>Each platform-specific chat listener extends this interface.
 *
 * @since 1.0.0
 */
public interface PlatformChatEventListener {

    /**
     * Called when a chat connection is established.
     *
     * @param chat connected chat instance
     * @param isReconnecting whether this connect event is part of a reconnect
     */
    default void onConnect(PlatformChat chat, boolean isReconnecting) {}

    /**
     * Called when a chat connection is closed.
     *
     * @param code close status code
     * @param reason close reason text, may be {@code null}
     * @param remote whether the remote side closed the connection
     * @param tryingToReconnect whether reconnect will be attempted
     */
    default void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {}

    /**
     * Called when the platform reports that the broadcast has ended.
     *
     * @param chat chat instance that observed broadcast end
     */
    default void onBroadcastEnd(PlatformChat chat) {}

    /**
     * Called when an exception is raised by chat processing.
     *
     * @param ex thrown exception
     */
    default void onError(Exception ex) {
        ex.printStackTrace();
    }

    /**
     * Called for platform events that are not handled by dedicated callbacks.
     *
     * @param eventName platform event name
     * @param rawData raw event payload
     */
    default void onUnhandledEvent(String eventName, String rawData) {}
}
