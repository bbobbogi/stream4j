package com.bbobbogi.stream4j.chzzk.types.channel;

import org.jetbrains.annotations.Nullable;
import com.bbobbogi.stream4j.chzzk.Chzzk;
import com.bbobbogi.stream4j.chzzk.exception.NotExistsException;
import com.bbobbogi.stream4j.chzzk.types.channel.emoticon.ChzzkChannelEmotePackData;

import java.io.IOException;

/**
 * 치지직 부분 채널 정보를 나타내는 클래스입니다.
 */
public class ChzzkPartialChannel {
    private String channelId;
    private String channelName;
    private String channelImageUrl;
    private boolean verifiedMark;
    private ChzzkChannelPersonalData personalData;
    private ChzzkChannelEmotePackData emotePackData;

    ChzzkPartialChannel() {}

    /**
     * Get this channel's {@link ChzzkChannelRules}.
     *
     * @param chzzk {@link Chzzk} 인스턴스
     * @return {@link ChzzkChannelRules} of the channel
     * @throws IOException if the request to API failed
     * @throws NotExistsException if the channel doesn't exists or the rules of the channel doesn't available
     */
    public ChzzkChannelRules getRules(Chzzk chzzk) throws IOException, NotExistsException {
        return chzzk.getChannelChatRules(channelId);
    }

    /**
     * Get the channel's id.
     *
     * @return 채널 ID
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Get the name of the channel.
     *
     * @return 채널 이름
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * Get url of the channel's image.
     *
     * @return 채널 이미지 URL (없는 경우 null)
     */
    @Nullable
    public String getChannelImageUrl() {
        return channelImageUrl;
    }

    /**
     * Get is the channel verified.
     *
     * @return 인증 마크 여부
     */
    public boolean isVerifiedMark() {
        return verifiedMark;
    }

    /**
     * Get personal data of logged user about the channel.
     * If not logged in, returns null.
     *
     * @return 개인 데이터 (로그인하지 않은 경우 null)
     */
    @Nullable
    public ChzzkChannelPersonalData getPersonalData() {
        return personalData;
    }

    /**
     * Get the emoticon pack data of the channel.
     *
     * @return 이모티콘 팩 데이터 (없는 경우 null)
     */
    @Nullable
    public ChzzkChannelEmotePackData getEmotePackData() {
        return emotePackData;
    }
    @Override
    public String toString() {
        return "ChzzkPartialChannel{" +
                "channelId='" + channelId + '\'' +
                ", channelName='" + channelName + '\'' +
                ", channelImageUrl='" + channelImageUrl + '\'' +
                ", verifiedMark=" + verifiedMark +
                ", personalData=" + personalData +
                '}';
    }
}
