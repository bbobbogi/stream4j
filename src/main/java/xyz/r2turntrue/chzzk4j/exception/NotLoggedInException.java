package xyz.r2turntrue.chzzk4j.exception;

/**
 * 로그인이 필요한 작업에서 로그인되지 않은 경우 발생하는 예외입니다.
 */
public class NotLoggedInException extends Exception {
    /**
     * 지정된 메시지로 NotLoggedInException을 생성합니다.
     *
     * @param reason 예외 발생 이유
     */
    public NotLoggedInException(String reason) {
        super(reason);
    }
}
