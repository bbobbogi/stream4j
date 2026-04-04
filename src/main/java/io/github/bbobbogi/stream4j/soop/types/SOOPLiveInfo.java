package io.github.bbobbogi.stream4j.soop.types;

import io.github.bbobbogi.stream4j.common.LiveInfo;

public class SOOPLiveInfo implements LiveInfo {
    private final boolean online;
    private final int result;
    private final String streamerId;
    private final String nickname;
    private final String title;
    private final String broadcastNo;
    private final int minTier;

    public SOOPLiveInfo(boolean online, int result, String streamerId, String nickname, String title, String broadcastNo, int minTier) {
        this.online = online;
        this.result = result;
        this.streamerId = streamerId;
        this.nickname = nickname;
        this.title = title;
        this.broadcastNo = broadcastNo;
        this.minTier = minTier;
    }

    @Override
    public boolean isLive() {
        return online;
    }

    public boolean isOnline() {
        return isLive();
    }

    @Override
    public String getTitle() {
        return title;
    }

    public int getResult() {
        return result;
    }

    public String getStreamerId() {
        return streamerId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getBroadcastNo() {
        return broadcastNo;
    }

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
