package com.bbobbogi.stream4j.soop.chat;

public class SOOPDonationMessage {

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

    public SOOPDonationMessage(Type type, String to, String from, String fromUsername, int amount, int fanClubOrdinal) {
        this.type = type;
        this.to = to;
        this.from = from;
        this.fromUsername = fromUsername;
        this.amount = amount;
        this.fanClubOrdinal = fanClubOrdinal;
    }

    public Type getType() {
        return type;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public int getAmount() {
        return amount;
    }

    public int getFanClubOrdinal() {
        return fanClubOrdinal;
    }

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
