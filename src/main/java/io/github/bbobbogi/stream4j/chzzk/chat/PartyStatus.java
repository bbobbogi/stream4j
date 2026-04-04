package io.github.bbobbogi.stream4j.chzzk.chat;

/**
 * Party donation status.
 *
 * @since 1.0.0
 */
public enum PartyStatus {
    /**
     * Open - party is open and participation is allowed.
     */
    OPEN,

    /**
     * Closed - party is closed and participation is not allowed.
     */
    CLOSED;

    /**
     * Converts a string to {@link PartyStatus}.
     *
     * @param status party status string
     * @return matched {@link PartyStatus}, or {@code null} if unknown
     */
    public static PartyStatus fromString(String status) {
        if (status == null) return null;
        try {
            return PartyStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
