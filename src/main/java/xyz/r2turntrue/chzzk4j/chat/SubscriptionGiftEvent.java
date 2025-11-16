package xyz.r2turntrue.chzzk4j.chat;

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

    public String getGiftId() {
        return giftId;
    }

    public String getSelectionTypeRaw() {
        return selectionType;
    }

    public SelectionType getSelectionType() {
        return SelectionType.fromString(selectionType);
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPartialRefundedQuantity() {
        return partialRefundedQuantity;
    }

    public int getGiftTierNo() {
        return giftTierNo;
    }

    public String getUserIdHash() {
        return userIdHash;
    }

    public int getSenderTierNo() {
        return senderTierNo;
    }

    public String getGiftType() {
        return giftType;
    }

    public String getFailedUsers() {
        return failedUsers;
    }

    public int getCompletedQuantity() {
        return completedQuantity;
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
