package io.github.bbobbogi.stream4j.chzzk.types;

import java.util.List;

/**
 * Class representing channel emote pack data.
 *
 * @since 1.0.0
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
     * @return emote pack ID
     */
    public String getPackId() {
        return emojiPackId;
    }

    /**
     * Get the name of the pack.
     *
     * @return emote pack name
     */
    public String getPackName() {
        return emojiPackName;
    }

    /**
     * Get url of the pack's image.
     *
     * @return emote pack image URL
     */
    public String getPackImageUrl() {
        return emojiPackImageUrl;
    }

    /**
     * Get the emoticons data of the pack.
     *
     * @return list of emoticon data
     */
    public List<ChzzkChannelEmoticonData> getEmojis() {
        return emojis;
    }
}
