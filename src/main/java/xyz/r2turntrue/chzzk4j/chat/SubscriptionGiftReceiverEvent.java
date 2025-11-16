package xyz.r2turntrue.chzzk4j.chat;

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

    String rawJson;

    public String getGiftId() {
        return giftId;
    }

    public String getReceiverUserIdHash() {
        return receiverUserIdHash;
    }

    public String getSelectionTypeRaw() {
        return selectionType;
    }

    public SelectionType getSelectionType() {
        return SelectionType.fromString(selectionType);
    }

    public int getGiftTierNo() {
        return giftTierNo;
    }

    public String getUserIdHash() {
        return userIdHash;
    }

    public boolean isReceiverVerifiedMark() {
        return receiverVerifiedMark;
    }

    public String getGiftType() {
        return giftType;
    }

    public String getReceiverNickname() {
        return receiverNickname;
    }

    public String getType() {
        return type;
    }

    public String getGiftTierName() {
        return giftTierName;
    }

    public String getRawJson() {
        return rawJson;
    }
}
