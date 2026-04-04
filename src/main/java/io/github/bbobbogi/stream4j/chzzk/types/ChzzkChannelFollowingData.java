package io.github.bbobbogi.stream4j.chzzk.types;

/**
 * Class representing channel following data.
 *
 * @since 1.0.0
 */
public class ChzzkChannelFollowingData {
    private boolean following;
    private boolean notification;
    private String followDate;

    private ChzzkChannelFollowingData() {}

    /**
     * Get is me following the channel.
     *
     * @return whether you follow the channel
     */
    public boolean isFollowing() {
        return following;
    }

    /**
     * Get is me enabled the channel notification.
     *
     * @return whether notifications are enabled
     */
    public boolean isEnabledNotification() {
        return notification;
    }

    /**
     * Get when me followed the channel in yyyy-mm-dd HH:mm:ss format.
     *
     * @return follow date string
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
