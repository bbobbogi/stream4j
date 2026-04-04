package io.github.bbobbogi.stream4j.soop.types;

import io.github.bbobbogi.stream4j.common.LiveInfo;

/**
 * SOOP live broadcast information model.
 *
 * @since 1.0.0
 */
public class SOOPLiveInfo implements LiveInfo {
    private final boolean online;
    private final int result;
    private final String streamerId;
    private final String nickname;
    private final String title;
    private final String broadcastNo;
    private final int minTier;

    /**
     * Creates a live information model.
     *
     * @param online live status resolved from SOOP live detail
     * @param result SOOP result code from live detail API
     * @param streamerId streamer user ID
     * @param nickname streamer display name
     * @param title broadcast title
     * @param broadcastNo broadcast numeric identifier
     * @param minTier minimum required subscription tier for chat access
     */
    public SOOPLiveInfo(boolean online, int result, String streamerId, String nickname, String title, String broadcastNo, int minTier) {
        this.online = online;
        this.result = result;
        this.streamerId = streamerId;
        this.nickname = nickname;
        this.title = title;
        this.broadcastNo = broadcastNo;
        this.minTier = minTier;
    }

    /**
     * Returns whether the broadcast is live.
     *
     * @return {@code true} if the stream is online
     */
    @Override
    public boolean isLive() {
        return online;
    }

    /**
     * Returns whether the broadcast is online.
     *
     * @return {@code true} if the stream is online
     */
    public boolean isOnline() {
        return isLive();
    }

    /**
     * Returns broadcast title.
     *
     * @return live title
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Returns raw SOOP live-detail result code.
     *
     * @return API result code
     */
    public int getResult() {
        return result;
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

    /**
     * Returns broadcast number value.
     *
     * @return broadcast number string
     */
    public String getBroadcastNo() {
        return broadcastNo;
    }

    /**
     * Returns minimum subscription tier required by the broadcast.
     *
     * @return minimum tier value, or {@code 0} when unrestricted
     */
    public int getMinTier() {
        return minTier;
    }

    @Override
    public String toString() {
        return "SOOPLiveInfo{" +
                "online=" + online +
                ", result=" + result +
                ", streamerId='" + streamerId + '\'' +
                ", nickname='" + nickname + '\'' +
                ", title='" + title + '\'' +
                ", broadcastNo='" + broadcastNo + '\'' +
                ", minTier=" + minTier +
                '}';
    }
}
