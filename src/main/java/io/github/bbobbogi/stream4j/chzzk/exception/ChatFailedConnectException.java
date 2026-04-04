package io.github.bbobbogi.stream4j.chzzk.exception;

/**
 * 채팅 서버 연결에 실패했을 때 발생하는 예외입니다.
 */
public class ChatFailedConnectException extends IllegalStateException {
    /**
     * 오류 코드
     */
    public int errorCode;

    /**
     * 오류 메시지
     */
    public String errorMessage;

    /**
     * ChatFailedConnectException을 생성합니다.
     *
     * @param errorCode 오류 코드
     * @param errorMessage 오류 메시지
     */
    public ChatFailedConnectException(int errorCode, String errorMessage) {
        super("Failed to connect to chat! (Message: " + errorMessage + ", Code: " + errorCode + ")");
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
