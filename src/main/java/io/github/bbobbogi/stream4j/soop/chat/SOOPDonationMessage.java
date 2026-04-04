package io.github.bbobbogi.stream4j.soop.chat;

/**
 * Donation event payload received from SOOP chat.
 *
 * @since 1.0.0
 */
public class SOOPDonationMessage {

    /**
     * Donation type emitted by SOOP.
     */
    public enum Type {
        TEXT,
        VIDEO,
        AD_BALLOON
    }

    private final Type type;
    private final String to;
    private final String from;
    private final String fromUsername;
    private final int amount;
    private final int fanClubOrdinal;

    /**
     * Creates a donation message model.
     *
     * @param type donation type
     * @param to target broadcaster or channel identifier
     * @param from donor user ID
     * @param fromUsername donor display name
     * @param amount donated star-balloon amount
     * @param fanClubOrdinal fan-club order index from SOOP payload
     */
    public SOOPDonationMessage(Type type, String to, String from, String fromUsername, int amount, int fanClubOrdinal) {
        this.type = type;
        this.to = to;
        this.from = from;
        this.fromUsername = fromUsername;
        this.amount = amount;
        this.fanClubOrdinal = fanClubOrdinal;
    }

    /**
     * Returns donation type.
     *
     * @return donation type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns target broadcaster or channel identifier.
     *
     * @return donation target value from payload
     */
    public String getTo() {
        return to;
    }

    /**
     * Returns donor user ID.
     *
     * @return donor user ID
     */
    public String getFrom() {
        return from;
    }

    /**
     * Returns donor display name.
     *
     * @return donor nickname
     */
    public String getFromUsername() {
        return fromUsername;
    }

    /**
     * Returns donated star-balloon amount.
     *
     * @return donation amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Returns fan-club sequence value from SOOP payload.
     *
     * @return fan-club ordinal, or {@code 0} when absent
     */
    public int getFanClubOrdinal() {
        return fanClubOrdinal;
    }

    /**
     * Returns whether this donation indicates a new fan-club milestone.
     *
     * @return {@code true} if fan-club ordinal is greater than zero
     */
    public boolean isNewFanClub() {
        return fanClubOrdinal > 0;
    }

    @Override
    public String toString() {
        return "SOOPDonationMessage{" +
                "type=" + type +
                ", to='" + to + '\'' +
                ", from='" + from + '\'' +
                ", fromUsername='" + fromUsername + '\'' +
                ", amount=" + amount +
                ", fanClubOrdinal=" + fanClubOrdinal +
                '}';
    }
}
