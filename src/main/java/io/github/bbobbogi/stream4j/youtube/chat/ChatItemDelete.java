package io.github.bbobbogi.stream4j.youtube.chat;

/**
 * Represents a deleted chat item event.
 *
 * @since 1.0.0
 */
public class ChatItemDelete {
    protected String targetId;
    protected String message;

    /**
     * Returns the ID of the chat item that was deleted.
     *
     * @return target chat item ID
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * Returns the deletion notice message.
     *
     * @return deletion message
     */
    public String getMessage() {
        return message;
    }
}
