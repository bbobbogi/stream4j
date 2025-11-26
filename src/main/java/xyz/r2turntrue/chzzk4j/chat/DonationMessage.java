package xyz.r2turntrue.chzzk4j.chat;

/**
 * 후원 메시지를 나타내는 클래스입니다.
 */
public class DonationMessage extends ChatMessage {
    /**
     * DonationMessage를 생성합니다.
     */
    DonationMessage() {
        super();
    }

    /**
     * 후원 금액을 반환합니다.
     *
     * @return 후원 금액
     */
    public int getPayAmount() {
        return extras.payAmount;
    }

    /**
     * 익명 후원 여부를 반환합니다.
     *
     * @return 익명 후원이면 {@code true}, 그렇지 않으면 {@code false}
     */
    public boolean isAnonymous() {
        return extras != null && extras.isAnonymous;
    }
}
