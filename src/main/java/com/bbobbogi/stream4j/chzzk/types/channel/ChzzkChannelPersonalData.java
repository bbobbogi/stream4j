package com.bbobbogi.stream4j.chzzk.types.channel;

/**
 * 채널에 대한 사용자 개인 데이터를 나타내는 클래스입니다.
 */
public class ChzzkChannelPersonalData {
    private ChzzkChannelFollowingData following;
    private boolean privateUserBlock;

    private ChzzkChannelPersonalData() {}

    /**
     * Get following status of the logged user about the channel.
     *
     * @return 팔로잉 데이터
     */
    public ChzzkChannelFollowingData getFollowing() {
        return following;
    }

    /**
     * 비공개 사용자 차단 여부를 반환합니다.
     *
     * @return 비공개 사용자 차단 여부
     */
    public boolean isPrivateUserBlock() {
        return privateUserBlock;
    }
}
