package io.github.bbobbogi.stream4j.chzzk.chat;

/**
 * IIMS penalty event.
 *
 * Contains user penalty information caused by inappropriate content
 * (IIMS - Inappropriate Image Monitoring System).
 * Sent through EVENT command (cmd: 93006), and type is "IIMS_PENALTY".
 *
 * @since 1.0.0
 */
public class IimsPenaltyEvent {
    String userIdHash;
    String type;

    public String rawJson;

    /**
     * Creates an {@link IimsPenaltyEvent}.
     */
    public IimsPenaltyEvent() {
    }

    /**
     * Returns the penalized user's ID hash.
     *
     * @return user ID hash
     */
    public String getUserIdHash() {
        return userIdHash;
    }

    /**
     * Returns the event type.
     *
     * @return event type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the raw JSON string.
     *
     * @return raw JSON
     */
    public String getRawJson() {
        return rawJson;
    }
}
