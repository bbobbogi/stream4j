package io.github.bbobbogi.stream4j.common;

/**
 * Common interface for channel metadata across platforms.
 *
 * <p>Channel info models from each platform implement this contract.
 *
 * @since 1.0.0
 */
public interface ChannelInfo {

    /**
     * Returns the platform-specific channel identifier.
     *
     * @return channel identifier
     */
    String getId();

    /**
     * Returns the channel display name.
     *
     * @return channel name
     */
    String getName();

    /**
     * Returns whether the channel is currently live.
     *
     * @return {@code true} when live
     */
    boolean isLive();
}
