package io.github.bbobbogi.stream4j.chzzk.chat;

/**
 * Subscription gift receiver event.
 *
 * Contains information about the user receiving a subscription gift.
 * Sent through EVENT command (cmd: 93006), and type is "SUBSCRIPTION_GIFT_RECEIVER".
 *
 * Event sequence:
 * 1. SUBSCRIPTION_GIFT (sender information) - sent once
 * 2. SUBSCRIPTION_GIFT_RECEIVER (receiver information) - repeated by receiver count (this class)
 *
 * giftId links this receiver to its SUBSCRIPTION_GIFT event.
 *
 * @since 1.0.0
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
     * Creates a {@link SubscriptionGiftReceiverEvent}.
     */
    public SubscriptionGiftReceiverEvent() {
    }

    /**
     * Returns the gift ID.
     *
     * @return gift ID
     */
    public String getGiftId() {
        return giftId;
    }

    /**
     * Returns the receiver user ID hash.
     *
     * @return receiver user ID hash
     */
    public String getReceiverUserIdHash() {
        return receiverUserIdHash;
    }

    /**
     * Returns the raw selection type string.
     *
     * @return raw selection type string
     */
    public String getSelectionTypeRaw() {
        return selectionType;
    }

    /**
     * Returns the selection type.
     *
     * @return selection type
     */
    public SelectionType getSelectionType() {
        return SelectionType.fromString(selectionType);
    }

    /**
     * Returns the gift tier number.
     *
     * @return gift tier number
     */
    public int getGiftTierNo() {
        return giftTierNo;
    }

    /**
     * Returns the sender user ID hash.
     *
     * @return sender user ID hash
     */
    public String getUserIdHash() {
        return userIdHash;
    }

    /**
     * Returns whether the receiver has a verified mark.
     *
     * @return receiver verified mark status
     */
    public boolean isReceiverVerifiedMark() {
        return receiverVerifiedMark;
    }

    /**
     * Returns the gift type.
     *
     * @return gift type
     */
    public String getGiftType() {
        return giftType;
    }

    /**
     * Returns the receiver nickname.
     *
     * @return receiver nickname
     */
    public String getReceiverNickname() {
        return receiverNickname;
    }

    /**
     * Returns the event type.
     *
     * @return event type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the gift tier name.
     *
     * @return gift tier name
     */
    public String getGiftTierName() {
        return giftTierName;
    }

    /**
     * Returns the raw JSON string.
     *
     * @return raw JSON string
     */
    public String getRawJson() {
        return rawJson;
    }
}
