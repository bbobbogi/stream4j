package com.bbobbogi.stream4j.chzzk.chat;

/**
 * 구독권 선물 이벤트
 *
 * 구독권을 선물하는 사람의 정보를 담고 있습니다.
 * EVENT 명령(cmd: 93006)으로 전송되며, type은 "SUBSCRIPTION_GIFT"입니다.
 *
 * 이벤트 전송 순서:
 * 1. SUBSCRIPTION_GIFT (발신자 정보) - 1회 전송
 * 2. SUBSCRIPTION_GIFT_RECEIVER (수신자 정보) - 수신자 수만큼 반복 전송
 *
 * 예시:
 * - 한 명에게 선물: selectionType="MANUAL", quantity=1
 *   → SUBSCRIPTION_GIFT 1회 + SUBSCRIPTION_GIFT_RECEIVER 1회
 * - 랜덤 선물: selectionType="RANDOM", quantity=10
 *   → SUBSCRIPTION_GIFT 1회 + SUBSCRIPTION_GIFT_RECEIVER 10회
 */
public class SubscriptionGiftEvent {
    String giftId;
    String selectionType;
    int quantity;
    int partialRefundedQuantity;
    int giftTierNo;
    String userIdHash;
    int senderTierNo;
    String giftType;
    String failedUsers;
    int completedQuantity;
    String type;
    String giftTierName;

    String rawJson;

    /**
     * SubscriptionGiftEvent를 생성합니다.
     */
    SubscriptionGiftEvent() {
    }

    /**
     * 선물 ID를 반환합니다.
     *
     * @return 선물 ID
     */
    public String getGiftId() {
        return giftId;
    }

    /**
     * 선택 타입의 원본 문자열을 반환합니다.
     *
     * @return 선택 타입 원본 문자열
     */
    public String getSelectionTypeRaw() {
        return selectionType;
    }

    /**
     * 선택 타입을 반환합니다.
     *
     * @return 선택 타입
     */
    public SelectionType getSelectionType() {
        return SelectionType.fromString(selectionType);
    }

    /**
     * 선물 수량을 반환합니다.
     *
     * @return 선물 수량
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * 부분 환불된 수량을 반환합니다.
     *
     * @return 부분 환불 수량
     */
    public int getPartialRefundedQuantity() {
        return partialRefundedQuantity;
    }

    /**
     * 선물 티어 번호를 반환합니다.
     *
     * @return 선물 티어 번호
     */
    public int getGiftTierNo() {
        return giftTierNo;
    }

    /**
     * 사용자 ID 해시를 반환합니다.
     *
     * @return 사용자 ID 해시
     */
    public String getUserIdHash() {
        return userIdHash;
    }

    /**
     * 발신자 티어 번호를 반환합니다.
     *
     * @return 발신자 티어 번호
     */
    public int getSenderTierNo() {
        return senderTierNo;
    }

    /**
     * 선물 타입을 반환합니다.
     *
     * @return 선물 타입
     */
    public String getGiftType() {
        return giftType;
    }

    /**
     * 실패한 사용자 목록을 반환합니다.
     *
     * @return 실패한 사용자 목록
     */
    public String getFailedUsers() {
        return failedUsers;
    }

    /**
     * 완료된 수량을 반환합니다.
     *
     * @return 완료된 수량
     */
    public int getCompletedQuantity() {
        return completedQuantity;
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
     * 선물 티어 이름을 반환합니다.
     *
     * @return 선물 티어 이름
     */
    public String getGiftTierName() {
        return giftTierName;
    }

    /**
     * 원본 JSON 문자열을 반환합니다.
     *
     * @return 원본 JSON 문자열
     */
    public String getRawJson() {
        return rawJson;
    }
}
