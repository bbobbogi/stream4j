package io.github.bbobbogi.stream4j.soop.types;

import io.github.bbobbogi.stream4j.common.ChannelInfo;

public class SOOPChannelInfo implements ChannelInfo {
    private final String streamerId;
    private final String nickname;
    private final boolean live;

    public SOOPChannelInfo(String streamerId, String nickname, boolean live) {
        this.streamerId = streamerId;
        this.nickname = nickname;
        this.live = live;
    }

    @Override
    public String getId() {
        return streamerId;
    }

    @Override
    public String getName() {
        return nickname;
    }

    @Override
    public boolean isLive() {
        return live;
    }

    public String getStreamerId() {
        return streamerId;
    }

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
