package io.github.bbobbogi.stream4j.chzzk.types;

import io.github.bbobbogi.stream4j.common.ChannelInfo;

/**
 * Class representing Chzzk channel information.
 *
 * @since 1.0.0
 */
public class ChzzkChannelInfo extends ChzzkPartialChannel implements ChannelInfo {
    private String channelDescription;
    private int followerCount;
    private boolean openLive;

    private ChzzkChannelInfo() {
        super();
    }

    /**
     * Get description of the channel.
     *
     * @return channel description
     */
    public String getChannelDescription() {
        return channelDescription;
    }

    /**
     * Get the count of the channel's followers.
     *
     * @return follower count
     */
    public int getFollowerCount() {
        return followerCount;
    }

    /**
     * Get is the channel broadcasting.
     *
     * @return {@code true} if broadcasting, otherwise {@code false}
     */
    public boolean isBroadcasting() {
        return openLive;
    }

    @Override
    public String getId() {
        return getChannelId();
    }

    @Override
    public String getName() {
        return getChannelName();
    }

    @Override
    public boolean isLive() {
        return isBroadcasting();
    }

    @Override
    public String toString() {
        return "ChzzkChannelInfo{" +
                "parent=" + super.toString() +
                ", channelDescription='" + channelDescription + '\'' +
                ", followerCount=" + followerCount +
                ", openLive=" + openLive +
                '}';
    }
}
