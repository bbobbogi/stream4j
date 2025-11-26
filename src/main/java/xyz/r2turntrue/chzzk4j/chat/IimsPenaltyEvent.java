package xyz.r2turntrue.chzzk4j.chat;

/**
 * IIMS 페널티 이벤트
 *
 * 부적절한 콘텐츠(IIMS - Inappropriate Image Monitoring System)로 인한
 * 사용자 제재 정보를 담고 있습니다.
 * EVENT 명령(cmd: 93006)으로 전송되며, type은 "IIMS_PENALTY"입니다.
 */
public class IimsPenaltyEvent {
    String userIdHash;
    String type;

    String rawJson;

    /**
     * IimsPenaltyEvent를 생성합니다.
     */
    IimsPenaltyEvent() {
    }

    /**
     * 제재받은 사용자 ID 해시를 반환합니다.
     *
     * @return 사용자 ID 해시
     */
    public String getUserIdHash() {
        return userIdHash;
    }

    /**
     * 이벤트 타입을 반환합니다.
     *
     * @return 이벤트 타입
     */
    public String getType() {
        return type;
    }

    /**
     * 원본 JSON 문자열을 반환합니다.
     *
     * @return 원본 JSON
     */
    public String getRawJson() {
        return rawJson;
    }
}
