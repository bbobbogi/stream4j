package io.github.bbobbogi.stream4j.chzzk.chat;

/**
 * Subscription gift selection type.
 *
 * @since 1.0.0
 */
public enum SelectionType {
    /**
     * Manual selection - gift to directly selected users.
     */
    MANUAL,

    /**
     * Random selection - gift to randomly selected users.
     */
    RANDOM;

    /**
     * Converts a string to {@link SelectionType}.
     *
     * @param type selection type string
     * @return matched {@link SelectionType}, or {@code null} if unknown
     */
    public static SelectionType fromString(String type) {
        if (type == null) return null;
        try {
            return SelectionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
