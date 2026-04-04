package io.github.bbobbogi.stream4j.util;

import java.io.IOException;

/**
 * Exception indicating a failure that should not be retried.
 *
 * @apiNote This is an internal API and may change without notice.
 * @since 1.0.0
 */
public class NonRetryableException extends IOException {
    public NonRetryableException(String message) {
        super(message);
    }

    public NonRetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
