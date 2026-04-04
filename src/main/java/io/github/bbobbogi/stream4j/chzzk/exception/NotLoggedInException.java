package io.github.bbobbogi.stream4j.chzzk.exception;

/**
 * Exception thrown when an operation requiring login is called without login.
 *
 * @since 1.0.0
 */
public class NotLoggedInException extends Exception {
    /**
     * Creates a {@link NotLoggedInException} with the given message.
     *
     * @param reason reason for the exception
     */
    public NotLoggedInException(String reason) {
        super(reason);
    }
}
