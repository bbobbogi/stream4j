package com.bbobbogi.stream4j.youtube.types;

import com.bbobbogi.stream4j.common.LiveInfo;

public class YouTubeLiveInfo implements LiveInfo {
    private final String videoId;
    private final String channelId;
    private final String channelName;
    private final String title;
    private final boolean live;
    private final String startTimestamp;
    private final String endTimestamp;
    private final int viewerCount;

    public YouTubeLiveInfo(String videoId, String channelId, String channelName, String title, boolean live,
                           String startTimestamp, String endTimestamp, int viewerCount) {
        this.videoId = videoId;
        this.channelId = channelId;
        this.channelName = channelName;
        this.title = title;
        this.live = live;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.viewerCount = viewerCount;
    }

    @Override
    public boolean isLive() {
        return live;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public boolean getLive() {
        return live;
    }

    public String getStartTimestamp() {
        return startTimestamp;
    }

    public String getEndTimestamp() {
        return endTimestamp;
    }

    public int getViewerCount() {
        return viewerCount;
    }

    @Override
    public String toString() {
        return "YouTubeLiveInfo{" +
                "videoId='" + videoId + '\'' +
                ", channelId='" + channelId + '\'' +
                ", channelName='" + channelName + '\'' +
                ", title='" + title + '\'' +
                ", live=" + live +
                ", startTimestamp='" + startTimestamp + '\'' +
                ", endTimestamp='" + endTimestamp + '\'' +
                ", viewerCount=" + viewerCount +
                '}';
    }
}
