package io.github.bbobbogi.stream4j.chzzk.types;

/**
 * Class representing channel emoticon data.
 *
 * @since 1.0.0
 */
public class ChzzkChannelEmoticonData {
    private String emojiId;
    private String imageUrl;

    private ChzzkChannelEmoticonData() {}

    /**
     * Get the emoticon's id.
     *
     * @return emoticon ID
     */
    public String getEmoticonId() {
        return emojiId;
    }

    /**
     * Get url of the emoticon's image.
     *
     * @return emoticon image URL
     */
    public String getEmoticonImageUrl() {
        return imageUrl;
    }
}
