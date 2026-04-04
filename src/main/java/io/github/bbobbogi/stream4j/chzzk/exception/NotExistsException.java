package io.github.bbobbogi.stream4j.chzzk.exception;

import java.io.InvalidObjectException;

/**
 * Exception thrown when a requested resource does not exist.
 *
 * @since 1.0.0
 */
public class NotExistsException extends InvalidObjectException {
    /**
     * Constructs an {@code NotExistsException}.
     *
     * @param reason Detailed message explaining the reason for the failure.
     */
    public NotExistsException(String reason) {
        super(reason);
    }
}
