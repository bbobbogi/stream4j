package com.bbobbogi.stream4j.chzzk.exception;

/**
 * 채널이 존재하지 않을 때 발생하는 예외입니다.
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
