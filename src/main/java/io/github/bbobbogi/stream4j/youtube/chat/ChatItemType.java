package io.github.bbobbogi.stream4j.youtube.chat;

/**
 * Types of YouTube chat items.
 *
 * @since 1.0.0
 */
public enum ChatItemType {
    /** Regular text chat message. */
    MESSAGE,
    /** Paid Super Chat message. */
    PAID_MESSAGE,
    /** Paid Super Sticker message. */
    PAID_STICKER,
    /** Ticker version of a paid message. */
    TICKER_PAID_MESSAGE,
    /** New member announcement message. */
    NEW_MEMBER_MESSAGE,
}
