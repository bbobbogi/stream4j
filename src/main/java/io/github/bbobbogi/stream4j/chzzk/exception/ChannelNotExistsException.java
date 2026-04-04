package io.github.bbobbogi.stream4j.chzzk.exception;

/**
 * Exception thrown when a channel does not exist.
 *
 * @since 1.0.0
 */
public class ChannelNotExistsException extends NotExistsException {
    /**
     * Constructs an {@code ChannelNotExistsException}.
     *
     * @param reason Detailed message explaining the reason for the failure.
     */
    public ChannelNotExistsException(String reason) {
        super(reason);
    }
}
