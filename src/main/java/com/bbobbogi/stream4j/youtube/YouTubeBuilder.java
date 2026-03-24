package com.bbobbogi.stream4j.youtube;

import com.bbobbogi.stream4j.common.PlatformApiBuilder;

public class YouTubeBuilder extends PlatformApiBuilder<YouTube, YouTubeBuilder> {
    @Override
    public YouTube build() {
        return new YouTube(this);
    }
}
