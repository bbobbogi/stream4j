package com.bbobbogi.stream4j.chzzk.types;

import org.jetbrains.annotations.NotNull;

/**
 * 라이브 채널 정보를 나타내는 클래스입니다.
 */
public class ChzzkLiveChannel {

    private String channelId;
    private String channelName;
    private String channelImageUrl;
    private boolean verifiedMark;

    /**
     * ChzzkLiveChannel을 생성합니다.
     */
    ChzzkLiveChannel() {
    }

    /**
     * Get id of the live channel.
     *
     * @return 채널 ID
     */
    public @NotNull String getId() {
        return channelId;
    }

    /**
     * Get name of the live channel.
     *
     * @return 채널 이름
     */
    public @NotNull String getName() {
        return channelName;
    }

    /**
     * Get image url of the live channel.
     *
     * @return 채널 이미지 URL
     */
    public @NotNull String getImageUrl() {
        return channelImageUrl;
    }

    /**
     * Get verified mark status of the live channel.
     *
     * @return 인증 마크 여부
     */
    public boolean hasVerifiedMark() {
        return verifiedMark;
    }

    @Override
    public String toString() {
        return "ChzzkLiveChannelImpl{" +
                "channelId='" + channelId + '\'' +
                ", channelName='" + channelName + '\'' +
                ", channelImageUrl='" + channelImageUrl + '\'' +
                ", verifiedMark=" + verifiedMark +
                '}';
    }

}
