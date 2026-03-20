package com.bbobbogi.stream4j.common;

public enum DonationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    SUCCESS,
    FAILED,
    EXPIRED,
    OPEN,
    CLOSED,
    FINISH,
    CONFIRM;

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

    public static DonationStatus fromPartyStatus(String raw) {
        if (raw == null) return SUCCESS;
        return switch (raw.toUpperCase()) {
            case "OPEN" -> OPEN;
            case "CLOSED" -> CLOSED;
            default -> SUCCESS;
        };
    }
}
