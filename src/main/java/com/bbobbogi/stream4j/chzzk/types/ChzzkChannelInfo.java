package com.bbobbogi.stream4j.chzzk.types;

import com.bbobbogi.stream4j.common.ChannelInfo;

/**
 * 치지직 채널 정보를 나타내는 클래스입니다.
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
     * @return 채널 설명
     */
    public String getChannelDescription() {
        return channelDescription;
    }

    /**
     * Get the count of the channel's followers.
     *
     * @return 팔로워 수
     */
    public int getFollowerCount() {
        return followerCount;
    }

    /**
     * Get is the channel broadcasting.
     *
     * @return 방송 중이면 {@code true}, 그렇지 않으면 {@code false}
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
