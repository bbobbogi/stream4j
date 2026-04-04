package io.github.bbobbogi.stream4j.youtube.chat;

/**
 * Plain text segment in an extended YouTube chat message.
 *
 * @since 1.0.0
 */
public class Text {
    private final String text;

    /**
     * Creates a text segment.
     *
     * @param text segment content
     */
    public Text(String text) {
        this.text = text;
    }

    /**
     * Returns the text content.
     *
     * @return text content
     */
    public String getText() {
        return text;
    }
}
