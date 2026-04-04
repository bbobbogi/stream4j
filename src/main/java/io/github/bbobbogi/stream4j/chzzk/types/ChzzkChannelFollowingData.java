package io.github.bbobbogi.stream4j.chzzk.types;

/**
 * 채널 팔로잉 데이터를 나타내는 클래스입니다.
 */
public class ChzzkChannelFollowingData {
    private boolean following;
    private boolean notification;
    private String followDate;

    private ChzzkChannelFollowingData() {}

    /**
     * Get is me following the channel.
     *
     * @return 팔로잉 여부
     */
    public boolean isFollowing() {
        return following;
    }

    /**
     * Get is me enabled the channel notification.
     *
     * @return 알림 활성화 여부
     */
    public boolean isEnabledNotification() {
        return notification;
    }

    /**
     * Get when me followed the channel in yyyy-mm-dd HH:mm:ss format.
     *
     * @return 팔로우 날짜 문자열
     */
    public String getFollowDate() {
        return followDate;
    }

    @Override
    public String toString() {
        return "ChzzkChannelFollowingData{" +
                "following=" + following +
                ", notification=" + notification +
                ", followDate='" + followDate + '\'' +
                '}';
    }
}
