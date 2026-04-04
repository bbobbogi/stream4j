package io.github.bbobbogi.stream4j.chzzk.exception;

/**
 * Exception thrown when connection to the chat server fails.
 *
 * @since 1.0.0
 */
public class ChatFailedConnectException extends IllegalStateException {
    /**
     * Error code.
     */
    public int errorCode;

    /**
     * Error message.
     */
    public String errorMessage;

    /**
     * Creates a {@link ChatFailedConnectException}.
     *
     * @param errorCode error code
     * @param errorMessage error message
     */
    public ChatFailedConnectException(int errorCode, String errorMessage) {
        super("Failed to connect to chat! (Message: " + errorMessage + ", Code: " + errorCode + ")");
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
