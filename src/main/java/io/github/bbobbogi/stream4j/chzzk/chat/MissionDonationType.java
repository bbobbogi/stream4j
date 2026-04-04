package io.github.bbobbogi.stream4j.chzzk.chat;

/**
 * Enum representing mission donation type.
 *
 * @since 1.0.0
 */
public enum MissionDonationType {
    /**
     * Solo mission.
     */
    ALONE,

    /**
     * Group mission.
     */
    GROUP,

    /**
     * Mission participation.
     */
    PARTICIPATION;

    /**
     * Converts a string to {@link MissionDonationType}.
     *
     * @param type mission donation type string
     * @return matched {@link MissionDonationType}, or {@code null} if unknown
     */
    public static MissionDonationType fromString(String type) {
        if (type == null) return null;
        try {
            return MissionDonationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
