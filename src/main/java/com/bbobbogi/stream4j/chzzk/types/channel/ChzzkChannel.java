package com.bbobbogi.stream4j.chzzk.types.channel;

/**
 * 치지직 채널 정보를 나타내는 클래스입니다.
 */
public class ChzzkChannel extends ChzzkPartialChannel {
    private String channelDescription;
    private int followerCount;
    private boolean openLive;

    private ChzzkChannel() {
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
    public String toString() {
        return "ChzzkChannel{" +
                "parent=" + super.toString() +
                ", channelDescription='" + channelDescription + '\'' +
                ", followerCount=" + followerCount +
                ", openLive=" + openLive +
                '}';
    }
}
