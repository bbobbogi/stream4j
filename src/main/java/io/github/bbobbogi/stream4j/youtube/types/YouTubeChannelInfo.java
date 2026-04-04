package io.github.bbobbogi.stream4j.youtube.types;

import io.github.bbobbogi.stream4j.common.ChannelInfo;

/**
 * Channel metadata for a YouTube live stream.
 *
 * @since 1.0.0
 */
public class YouTubeChannelInfo implements ChannelInfo {
    private final String channelId;
    private final String channelName;
    private final boolean live;

    /**
     * Creates a channel info object.
     *
     * @param channelId channel ID
     * @param channelName channel name
     * @param live current live state
     */
    public YouTubeChannelInfo(String channelId, String channelName, boolean live) {
        this.channelId = channelId;
        this.channelName = channelName;
        this.live = live;
    }

    /**
     * Returns the channel ID.
     *
     * @return channel ID
     */
    @Override
    public String getId() {
        return channelId;
    }

    /**
     * Returns the channel name.
     *
     * @return channel name
     */
    @Override
    public String getName() {
        return channelName;
    }

    /**
     * Returns whether the channel is currently live.
     *
     * @return {@code true} if live, otherwise {@code false}
     */
    @Override
    public boolean isLive() {
        return live;
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
     * Returns the channel name.
     *
     * @return channel name
     */
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
