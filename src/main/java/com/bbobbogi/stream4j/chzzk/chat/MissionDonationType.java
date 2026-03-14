package com.bbobbogi.stream4j.chzzk.chat;

/**
 * 미션 후원 타입을 나타내는 열거형입니다.
 */
public enum MissionDonationType {
    /**
     * 혼자 미션 - 개인이 혼자 수행하는 미션
     */
    ALONE,

    /**
     * 그룹 미션 - 여러 사람이 참여할 수 있는 그룹 미션
     */
    GROUP,

    /**
     * 미션 참여 - 그룹 미션에 참여하는 경우
     */
    PARTICIPATION;

    /**
     * 문자열에서 MissionDonationType으로 변환합니다.
     *
     * @param type 미션 후원 타입 문자열
     * @return 해당하는 MissionDonationType (없으면 null)
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
