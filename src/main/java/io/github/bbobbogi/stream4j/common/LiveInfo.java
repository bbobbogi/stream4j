package io.github.bbobbogi.stream4j.common;

/**
 * Common interface for live broadcast metadata across platforms.
 *
 * <p>Live info models from each platform implement this contract.
 *
 * @since 1.0.0
 */
public interface LiveInfo {

    /**
     * Returns whether the broadcast is currently live.
     *
     * @return {@code true} when the broadcast is live
     */
    boolean isLive();

    /**
     * Returns the broadcast title.
     *
     * @return title text, or {@code null} when unavailable
     */
    String getTitle();
}
