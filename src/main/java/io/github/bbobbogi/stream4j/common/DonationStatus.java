package io.github.bbobbogi.stream4j.common;

/**
 * Status values used to normalize donation and mission states.
 *
 * @since 1.0.0
 */
public enum DonationStatus {
    /** Waiting for a mission result or platform confirmation. */
    PENDING,
    /** Mission or donation state was approved by the platform. */
    APPROVED,
    /** Mission or donation state was rejected by the platform. */
    REJECTED,
    /** Donation flow completed successfully. */
    SUCCESS,
    /** Donation flow failed. */
    FAILED,
    /** Donation or mission expired before completion. */
    EXPIRED,
    /** Party donation is open for participation. */
    OPEN,
    /** Party donation is closed. */
    CLOSED,
    /** Party donation finished event state. */
    FINISH,
    /** Party donation confirmation event state. */
    CONFIRM;

    /**
     * Converts a platform mission status string to {@link DonationStatus}.
     *
     * @param raw mission status text from the platform
     * @return normalized donation status, or {@link #SUCCESS} when unknown
     */
    public static DonationStatus fromMissionStatus(String raw) {
        if (raw == null) return SUCCESS;
        return switch (raw.toUpperCase()) {
            case "PENDING" -> PENDING;
            case "APPROVED" -> APPROVED;
            case "REJECTED" -> REJECTED;
            case "COMPLETED" -> SUCCESS;
            case "EXPIRED" -> EXPIRED;
            default -> SUCCESS;
        };
    }

    /**
     * Converts a platform party status string to {@link DonationStatus}.
     *
     * @param raw party status text from the platform
     * @return normalized donation status, or {@link #SUCCESS} when unknown
     */
    public static DonationStatus fromPartyStatus(String raw) {
        if (raw == null) return SUCCESS;
        return switch (raw.toUpperCase()) {
            case "OPEN" -> OPEN;
            case "CLOSED" -> CLOSED;
            default -> SUCCESS;
        };
    }
}
