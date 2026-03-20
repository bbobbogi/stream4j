package com.bbobbogi.stream4j.util;

import java.io.IOException;

public class NonRetryableException extends IOException {
    public NonRetryableException(String message) {
        super(message);
    }

    public NonRetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
