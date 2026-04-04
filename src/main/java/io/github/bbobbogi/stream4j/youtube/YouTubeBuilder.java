package io.github.bbobbogi.stream4j.youtube;

import io.github.bbobbogi.stream4j.common.PlatformApiBuilder;

/**
 * Builder for {@link YouTube} API clients.
 *
 * <p>Inherits common platform API options such as debug mode.
 * Debug mode is disabled by default.
 *
 * @since 1.0.0
 */
public class YouTubeBuilder extends PlatformApiBuilder<YouTube, YouTubeBuilder> {
    /**
     * Builds a {@link YouTube} client instance.
     *
     * @return a configured YouTube API client
     */
    @Override
    public YouTube build() {
        return new YouTube(this);
    }
}
