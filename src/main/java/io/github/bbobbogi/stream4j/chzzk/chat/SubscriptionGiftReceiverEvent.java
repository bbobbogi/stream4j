package io.github.bbobbogi.stream4j.chzzk.chat;

/**
 * 구독권 선물 수신자 이벤트
 *
 * 구독권을 받는 사람의 정보를 담고 있습니다.
 * EVENT 명령(cmd: 93006)으로 전송되며, type은 "SUBSCRIPTION_GIFT_RECEIVER"입니다.
 *
 * 이벤트 전송 순서:
 * 1. SUBSCRIPTION_GIFT (발신자 정보) - 1회 전송
 * 2. SUBSCRIPTION_GIFT_RECEIVER (수신자 정보) - 수신자 수만큼 반복 전송 ← 현재 클래스
 *
 * giftId 필드를 통해 어떤 SUBSCRIPTION_GIFT에 속하는 수신자인지 확인할 수 있습니다.
 */
public class SubscriptionGiftReceiverEvent {
    String giftId;
    String receiverUserIdHash;
    String selectionType;
    int giftTierNo;
    String userIdHash;
    boolean receiverVerifiedMark;
    String giftType;
    String receiverNickname;
    String type;
    String giftTierName;

    public String rawJson;

    /**
     * SubscriptionGiftReceiverEvent를 생성합니다.
     */
    public SubscriptionGiftReceiverEvent() {
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
     * 수신자의 사용자 ID 해시를 반환합니다.
     *
     * @return 수신자 사용자 ID 해시
     */
    public String getReceiverUserIdHash() {
        return receiverUserIdHash;
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
     * 선물 티어 번호를 반환합니다.
     *
     * @return 선물 티어 번호
     */
    public int getGiftTierNo() {
        return giftTierNo;
    }

    /**
     * 발신자의 사용자 ID 해시를 반환합니다.
     *
     * @return 발신자 사용자 ID 해시
     */
    public String getUserIdHash() {
        return userIdHash;
    }

    /**
     * 수신자의 인증 마크 여부를 반환합니다.
     *
     * @return 수신자 인증 마크 여부
     */
    public boolean isReceiverVerifiedMark() {
        return receiverVerifiedMark;
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
     * 수신자의 닉네임을 반환합니다.
     *
     * @return 수신자 닉네임
     */
    public String getReceiverNickname() {
        return receiverNickname;
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
