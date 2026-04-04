package io.github.bbobbogi.stream4j.youtube;

import io.github.bbobbogi.stream4j.common.PlatformApiBuilder;

public class YouTubeBuilder extends PlatformApiBuilder<YouTube, YouTubeBuilder> {
    @Override
    public YouTube build() {
        return new YouTube(this);
    }
}
