package io.github.bbobbogi.stream4j.chzzk.chat;

/**
 * Class representing a subscription message.
 * A chat message that includes subscription information.
 *
 * @since 1.0.0
 */
public class SubscriptionMessage extends ChatMessage {
    /**
     * Creates a {@link SubscriptionMessage}.
     */
    public SubscriptionMessage() {
    }

    /**
     * Returns the subscription month count.
     *
     * @return subscription month count
     */
    public int getSubscriptionMonth() {
        return extras.month;
    }

    /**
     * Returns the subscription tier name.
     *
     * @return subscription tier name
     */
    public String getSubscriptionTierName() {
        return extras.tierName;
    }

    /**
     * Returns whether this subscription is anonymous.
     *
     * @return anonymity status
     */
    public boolean isAnonymous() {
        return extras != null && extras.isAnonymous;
    }
}
