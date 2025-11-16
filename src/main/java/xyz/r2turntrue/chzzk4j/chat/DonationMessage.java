package xyz.r2turntrue.chzzk4j.chat;

public class DonationMessage extends ChatMessage {
    public int getPayAmount() {
        return extras.payAmount;
    }

    public boolean isAnonymous() {
        return extras != null && extras.isAnonymous;
    }
}
