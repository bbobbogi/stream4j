package com.bbobbogi.stream4j.chzzk.types.channel.emoticon;

import java.util.List;

/**
 * 채널 이모티콘 팩 데이터를 나타내는 클래스입니다.
 */
public class ChzzkChannelEmotePackData {
    private String emojiPackId;
    private String emojiPackName;
    private String emojiPackImageUrl;
    private boolean emojiPackLocked;
    private List<ChzzkChannelEmoticonData> emojis;

    private ChzzkChannelEmotePackData() {}

    /**
     * Get the pack's id.
     *
     * @return 이모티콘 팩 ID
     */
    public String getPackId() {
        return emojiPackId;
    }

    /**
     * Get the name of the pack.
     *
     * @return 이모티콘 팩 이름
     */
    public String getPackName() {
        return emojiPackName;
    }

    /**
     * Get url of the pack's image.
     *
     * @return 이모티콘 팩 이미지 URL
     */
    public String getPackImageUrl() {
        return emojiPackImageUrl;
    }

    /**
     * Get the emoticons data of the pack.
     *
     * @return 이모티콘 데이터 목록
     */
    public List<ChzzkChannelEmoticonData> getEmojis() {
        return emojis;
    }
}
