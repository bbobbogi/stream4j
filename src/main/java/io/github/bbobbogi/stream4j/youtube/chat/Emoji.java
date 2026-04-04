package io.github.bbobbogi.stream4j.youtube.chat;

import java.util.List;

/**
 * Emoji segment in an extended YouTube chat message.
 *
 * @since 1.0.0
 */
public class Emoji {
    protected String emojiId;
    protected List<String> shortcuts;
    protected List<String> searchTerms;
    protected String iconURL;
    protected boolean isCustomEmoji;

    /**
     * Returns the emoji identifier.
     *
     * @return emoji ID
     */
    public String getEmojiId() {
        return emojiId;
    }

    /**
     * Returns the display shortcuts for this emoji.
     *
     * @return emoji shortcuts
     */
    public List<String> getShortcuts() {
        return shortcuts;
    }

    /**
     * Returns searchable terms for this emoji.
     *
     * @return search terms
     */
    public List<String> getSearchTerms() {
        return searchTerms;
    }

    /**
     * Returns the icon URL for this emoji.
     *
     * @return icon URL
     */
    public String getIconURL() {
        return iconURL;
    }

    /**
     * Returns whether this emoji is a custom channel emoji.
     *
     * @return {@code true} if custom, otherwise {@code false}
     */
    public boolean isCustomEmoji() {
        return isCustomEmoji;
    }

    @Override
    public String toString() {
        return "Emoji{" +
                "emojiId='" + emojiId + '\'' +
                ", shortcuts=" + shortcuts +
                ", searchTerms=" + searchTerms +
                ", iconURL='" + iconURL + '\'' +
                ", isCustomEmoji=" + isCustomEmoji +
                '}';
    }
}
