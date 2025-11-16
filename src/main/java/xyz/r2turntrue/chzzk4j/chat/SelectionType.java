package xyz.r2turntrue.chzzk4j.chat;

/**
 * 구독권 선물 선택 방식
 */
public enum SelectionType {
    /**
     * 수동 선택 - 직접 선택한 사람에게 선물
     */
    MANUAL,

    /**
     * 랜덤 선택 - 무작위로 선택된 사람에게 선물
     */
    RANDOM;

    public static SelectionType fromString(String type) {
        if (type == null) return null;
        try {
            return SelectionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
