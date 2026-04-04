package io.github.bbobbogi.stream4j.chzzk.chat;

/**
 * Class representing a donation message.
 *
 * @since 1.0.0
 */
public class DonationMessage extends ChatMessage {
    /**
     * Creates a {@link DonationMessage}.
     */
    public DonationMessage() {
        super();
    }

    /**
     * Returns the donation amount.
     *
     * @return donation amount
     */
    public int getPayAmount() {
        return extras != null ? extras.payAmount : 0;
    }

    /**
     * Returns whether this donation is anonymous.
     *
     * @return {@code true} if anonymous, otherwise {@code false}
     */
    public boolean isAnonymous() {
        return extras != null && extras.isAnonymous;
    }
}
