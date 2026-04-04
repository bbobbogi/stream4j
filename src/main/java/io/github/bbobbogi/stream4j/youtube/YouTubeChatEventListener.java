package io.github.bbobbogi.stream4j.youtube;

import io.github.bbobbogi.stream4j.youtube.chat.ChatItem;

/**
 * Listener for YouTube chat events.
 *
 * @since 1.0.0
 */
public interface YouTubeChatEventListener {

    /**
     * Called when the chat client connects.
     *
     * @param chat the connected chat client
     * @param isReconnecting {@code true} when this connection is from a reconnect attempt
     */
    default void onConnect(YouTubeChat chat, boolean isReconnecting) {}

    /**
     * Called when the chat connection is closed.
     *
     * @param code the close status code
     * @param reason the close reason
     * @param remote {@code true} if the remote endpoint initiated closure
     * @param tryingToReconnect {@code true} if a reconnect attempt will follow
     */
    default void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {}

    /**
     * Called when the broadcast ends.
     *
     * @param chat the chat client whose broadcast ended
     */
    default void onBroadcastEnd(YouTubeChat chat) {}

    /**
     * Called when an error occurs.
     *
     * @param ex the raised exception
     */
    default void onError(Exception ex) { ex.printStackTrace(); }

    /**
     * Called for a regular chat message.
     *
     * @param item the received chat item
     */
    default void onChat(ChatItem item) {}

    /**
     * Called for a Super Chat message.
     *
     * @param item the received paid message item
     */
    default void onSuperChat(ChatItem item) {}

    /**
     * Called for a Super Sticker event.
     *
     * @param item the received paid sticker item
     */
    default void onSuperSticker(ChatItem item) {}

    /**
     * Called when a new member message is received.
     *
     * @param item the membership chat item
     */
    default void onNewMember(ChatItem item) {}
}
