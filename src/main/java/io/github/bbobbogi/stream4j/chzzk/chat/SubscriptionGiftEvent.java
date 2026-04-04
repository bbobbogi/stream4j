package io.github.bbobbogi.stream4j.chzzk.chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Subscription gift event.
 *
 * Contains information about the user who sends subscription gifts.
 * Sent through EVENT command (cmd: 93006), and type is "SUBSCRIPTION_GIFT".
 *
 * Event sequence:
 * 1. SUBSCRIPTION_GIFT (sender information) - sent once
 * 2. SUBSCRIPTION_GIFT_RECEIVER (receiver information) - repeated by receiver count
 *
 * Examples:
 * - Gift to one user: selectionType="MANUAL", quantity=1
 *   -> SUBSCRIPTION_GIFT once + SUBSCRIPTION_GIFT_RECEIVER once
 * - Random gift: selectionType="RANDOM", quantity=10
 *   -> SUBSCRIPTION_GIFT once + SUBSCRIPTION_GIFT_RECEIVER 10 times
 *
 * @since 1.0.0
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
    Object failedUsers;
    int completedQuantity;
    String type;
    String giftTierName;

    public String rawJson;

    /**
     * Creates a {@link SubscriptionGiftEvent}.
     */
    public SubscriptionGiftEvent() {
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
     * Returns the gift quantity.
     *
     * @return gift quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns the partially refunded quantity.
     *
     * @return partially refunded quantity
     */
    public int getPartialRefundedQuantity() {
        return partialRefundedQuantity;
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
     * Returns the user ID hash.
     *
     * @return user ID hash
     */
    public String getUserIdHash() {
        return userIdHash;
    }

    /**
     * Returns the sender tier number.
     *
     * @return sender tier number
     */
    public int getSenderTierNo() {
        return senderTierNo;
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
     * Returns the list of failed users.
     *
     * @return failed user list
     */
    public List<String> getFailedUsers() {
        if (failedUsers instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object item : list) {
                if (item != null) result.add(item.toString());
            }
            return result;
        }
        return List.of();
    }

    /**
     * Returns the completed quantity.
     *
     * @return completed quantity
     */
    public int getCompletedQuantity() {
        return completedQuantity;
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
