package io.github.bbobbogi.stream4j.youtube.types;

import io.github.bbobbogi.stream4j.common.LiveInfo;

/**
 * Metadata for a YouTube live broadcast.
 *
 * @since 1.0.0
 */
public class YouTubeLiveInfo implements LiveInfo {
    private final String videoId;
    private final String channelId;
    private final String channelName;
    private final String title;
    private final boolean live;
    private final String startTimestamp;
    private final String endTimestamp;
    private final int viewerCount;

    /**
     * Creates a live info object.
     *
     * @param videoId the video ID
     * @param channelId the channel ID
     * @param channelName the channel name
     * @param title the live title
     * @param live live state
     * @param startTimestamp broadcast start timestamp
     * @param endTimestamp broadcast end timestamp
     * @param viewerCount current viewer count
     */
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

    /**
     * Returns whether the broadcast is currently live.
     *
     * @return {@code true} if live, otherwise {@code false}
     */
    @Override
    public boolean isLive() {
        return live;
    }

    /**
     * Returns the broadcast title.
     *
     * @return broadcast title
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Returns the YouTube video ID.
     *
     * @return video ID
     */
    public String getVideoId() {
        return videoId;
    }

    /**
     * Returns the channel ID.
     *
     * @return channel ID
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Returns the channel display name.
     *
     * @return channel name
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * Returns the live flag.
     *
     * @return {@code true} if live, otherwise {@code false}
     */
    public boolean getLive() {
        return live;
    }

    /**
     * Returns the scheduled or actual start timestamp.
     *
     * @return start timestamp, or {@code null}
     */
    public String getStartTimestamp() {
        return startTimestamp;
    }

    /**
     * Returns the actual end timestamp when available.
     *
     * @return end timestamp, or {@code null}
     */
    public String getEndTimestamp() {
        return endTimestamp;
    }

    /**
     * Returns the viewer count reported by YouTube.
     *
     * @return viewer count
     */
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
