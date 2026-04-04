package io.github.bbobbogi.stream4j.chzzk.chat;

/**
 * Mission donation status.
 *
 * @since 1.0.0
 */
public enum MissionStatus {
    /**
     * Pending.
     */
    PENDING,

    /**
     * Rejected.
     */
    REJECTED,

    /**
     * Approved.
     */
    APPROVED,

    /**
     * Completed.
     */
    COMPLETED,

    /**
     * Expired.
     */
    EXPIRED;

    /**
     * Converts a string to {@link MissionStatus}.
     *
     * @param status mission status string
     * @return matched {@link MissionStatus}, or {@code null} if unknown
     */
    public static MissionStatus fromString(String status) {
        if (status == null) return null;
        try {
            return MissionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
