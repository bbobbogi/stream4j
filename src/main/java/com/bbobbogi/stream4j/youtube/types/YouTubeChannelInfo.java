package com.bbobbogi.stream4j.youtube.types;

import com.bbobbogi.stream4j.common.ChannelInfo;

public class YouTubeChannelInfo implements ChannelInfo {
    private final String channelId;
    private final String channelName;
    private final boolean live;

    public YouTubeChannelInfo(String channelId, String channelName, boolean live) {
        this.channelId = channelId;
        this.channelName = channelName;
        this.live = live;
    }

    @Override
    public String getId() {
        return channelId;
    }

    @Override
    public String getName() {
        return channelName;
    }

    @Override
    public boolean isLive() {
        return live;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    @Override
    public String toString() {
        return "YouTubeChannelInfo{" +
                "channelId='" + channelId + '\'' +
                ", channelName='" + channelName + '\'' +
                ", live=" + live +
                '}';
    }
}
