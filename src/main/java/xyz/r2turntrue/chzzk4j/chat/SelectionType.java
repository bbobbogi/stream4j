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

    /**
     * 문자열에서 SelectionType으로 변환합니다.
     *
     * @param type 선택 타입 문자열
     * @return 해당하는 SelectionType (없으면 null)
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
