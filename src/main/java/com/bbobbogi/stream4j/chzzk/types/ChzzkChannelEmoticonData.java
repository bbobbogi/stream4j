package com.bbobbogi.stream4j.chzzk.types;

/**
 * 채널 이모티콘 데이터를 나타내는 클래스입니다.
 */
public class ChzzkChannelEmoticonData {
    private String emojiId;
    private String imageUrl;

    private ChzzkChannelEmoticonData() {}

    /**
     * Get the emoticon's id.
     *
     * @return 이모티콘 ID
     */
    public String getEmoticonId() {
        return emojiId;
    }

    /**
     * Get url of the emoticon's image.
     *
     * @return 이모티콘 이미지 URL
     */
    public String getEmoticonImageUrl() {
        return imageUrl;
    }
}
