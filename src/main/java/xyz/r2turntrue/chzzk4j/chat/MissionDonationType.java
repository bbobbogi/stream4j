package xyz.r2turntrue.chzzk4j.chat;

public enum MissionDonationType {
    ALONE,
    GROUP,
    PARTICIPATION;

    public static MissionDonationType fromString(String type) {
        if (type == null) return null;
        try {
            return MissionDonationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
