package com.bbobbogi.stream4j.chzzk.exception;

import java.io.InvalidObjectException;

/**
 * 요청한 리소스가 존재하지 않을 때 발생하는 예외입니다.
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
