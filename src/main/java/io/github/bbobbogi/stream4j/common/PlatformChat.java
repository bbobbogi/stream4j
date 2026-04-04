package io.github.bbobbogi.stream4j.common;

import java.util.concurrent.CompletableFuture;

/**
 * Common interface for platform-specific chat clients.
 *
 * <p>Chat implementations for Chzzk, CiMe, SOOP, and YouTube expose these
 * lifecycle operations.
 *
 * @since 1.0.0
 */
public interface PlatformChat {

    /**
     * Returns whether this chat client is currently connected.
     *
     * @return {@code true} if connected
     */
    boolean isConnected();

    /**
     * Connects asynchronously.
     *
     * @return a future that completes when connection is established
     */
    CompletableFuture<Void> connectAsync();

    /**
     * Connects synchronously.
     */
    void connect();

    /**
     * Closes asynchronously.
     *
     * @return a future that completes when close is finished
     */
    CompletableFuture<Void> closeAsync();

    /**
     * Closes synchronously.
     */
    void close();

    /**
     * Reconnects asynchronously.
     *
     * @return a future that completes when reconnect is finished
     */
    CompletableFuture<Void> reconnectAsync();

    /**
     * Reconnects synchronously.
     */
    void reconnect();
}
