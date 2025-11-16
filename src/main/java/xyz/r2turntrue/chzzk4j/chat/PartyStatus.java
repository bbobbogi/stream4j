package xyz.r2turntrue.chzzk4j.chat;

/**
 * 파티 후원 상태
 */
public enum PartyStatus {
    /**
     * 열림 - 파티가 열려있어 참여 가능
     */
    OPEN,

    /**
     * 닫힘 - 파티가 닫혀 참여 불가
     */
    CLOSED;

    public static PartyStatus fromString(String status) {
        if (status == null) return null;
        try {
            return PartyStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
