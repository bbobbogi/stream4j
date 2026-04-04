package io.github.bbobbogi.stream4j.common;

/**
 * Unified callback interface for events from all configured platforms.
 *
 * <p>Callbacks are invoked from platform-specific worker threads, so listener
 * implementations should be thread-safe.
 *
 * @since 1.0.0
 */
public interface StreamChatEventListener {

    /**
     * Called when a platform connection is established.
     *
     * @param platform connected platform
     * @param channelId platform-specific channel identifier
     */
    default void onConnect(DonationPlatform platform, String channelId) {}

    /**
     * Called when a chat message is received.
     *
     * @param platform source platform
     * @param channelId platform-specific channel identifier
     * @param nickname sender nickname, may be {@code null}
     * @param message chat message content
     */
    default void onChat(DonationPlatform platform, String channelId, String nickname, String message) {}

    /**
     * Called when a donation-related event is received.
     *
     * @param donation normalized donation payload
     */
    default void onDonation(Donation donation) {}

    /**
     * Called when the platform reports that a broadcast has ended.
     *
     * @param platform source platform
     * @param channelId platform-specific channel identifier
     */
    default void onBroadcastEnd(DonationPlatform platform, String channelId) {}

    /**
     * Called when a platform connection is closed.
     *
     * @param platform source platform
     * @param channelId platform-specific channel identifier
     * @param reason close reason, may be {@code null}
     */
    default void onDisconnect(DonationPlatform platform, String channelId, String reason) {}

    /**
     * Called when an error occurs while processing a platform connection.
     *
     * @param platform source platform
     * @param channelId platform-specific channel identifier
     * @param ex raised exception
     */
    default void onError(DonationPlatform platform, String channelId, Exception ex) {}
}
