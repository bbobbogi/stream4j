package com.bbobbogi.stream4j.chzzk.chat;

/**
 * 구독 메시지를 나타내는 클래스입니다.
 * 채팅에서 구독 정보가 포함된 메시지입니다.
 */
public class SubscriptionMessage extends ChatMessage {
    /**
     * SubscriptionMessage를 생성합니다.
     */
    public SubscriptionMessage() {
    }

    /**
     * 구독 개월 수를 반환합니다.
     *
     * @return 구독 개월 수
     */
    public int getSubscriptionMonth() {
        return extras.month;
    }

    /**
     * 구독 티어 이름을 반환합니다.
     *
     * @return 구독 티어 이름
     */
    public String getSubscriptionTierName() {
        return extras.tierName;
    }

    /**
     * 익명 구독 여부를 반환합니다.
     *
     * @return 익명 여부
     */
    public boolean isAnonymous() {
        return extras != null && extras.isAnonymous;
    }
}
