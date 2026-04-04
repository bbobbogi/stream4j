package io.github.bbobbogi.stream4j.chzzk.chat;

/**
 * 미션 후원 상태
 */
public enum MissionStatus {
    /**
     * 대기 중 - 미션이 승인 대기 중
     */
    PENDING,

    /**
     * 거부됨 - 미션이 거부됨
     */
    REJECTED,

    /**
     * 승인됨 - 미션이 승인되어 진행 중
     */
    APPROVED,

    /**
     * 완료됨 - 미션이 완료됨
     */
    COMPLETED,

    /**
     * 만료됨 - 미션 시간이 만료됨
     */
    EXPIRED;

    /**
     * 문자열에서 MissionStatus로 변환합니다.
     *
     * @param status 미션 상태 문자열
     * @return 해당하는 MissionStatus (없으면 null)
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
