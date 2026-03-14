package com.bbobbogi.stream4j.chzzk.chat;

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

    /**
     * 문자열에서 PartyStatus로 변환합니다.
     *
     * @param status 파티 상태 문자열
     * @return 해당하는 PartyStatus (없으면 null)
     */
    public static PartyStatus fromString(String status) {
        if (status == null) return null;
        try {
            return PartyStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
