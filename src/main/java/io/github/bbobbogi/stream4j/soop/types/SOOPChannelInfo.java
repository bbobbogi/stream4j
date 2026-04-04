package io.github.bbobbogi.stream4j.soop.types;

import io.github.bbobbogi.stream4j.common.ChannelInfo;

/**
 * Basic SOOP channel information model.
 *
 * @since 1.0.0
 */
public class SOOPChannelInfo implements ChannelInfo {
    private final String streamerId;
    private final String nickname;
    private final boolean live;

    /**
     * Creates a channel information model.
     *
     * @param streamerId streamer user ID
     * @param nickname streamer display name
     * @param live current live status
     */
    public SOOPChannelInfo(String streamerId, String nickname, boolean live) {
        this.streamerId = streamerId;
        this.nickname = nickname;
        this.live = live;
    }

    /**
     * Returns channel ID.
     *
     * @return streamer ID
     */
    @Override
    public String getId() {
        return streamerId;
    }

    /**
     * Returns channel display name.
     *
     * @return streamer nickname
     */
    @Override
    public String getName() {
        return nickname;
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
     * Returns streamer user ID.
     *
     * @return streamer ID
     */
    public String getStreamerId() {
        return streamerId;
    }

    /**
     * Returns streamer nickname.
     *
     * @return streamer display name
     */
    public String getNickname() {
        return nickname;
    }

    @Override
    public String toString() {
        return "SOOPChannelInfo{" +
                "streamerId='" + streamerId + '\'' +
                ", nickname='" + nickname + '\'' +
                ", live=" + live +
                '}';
    }
}
