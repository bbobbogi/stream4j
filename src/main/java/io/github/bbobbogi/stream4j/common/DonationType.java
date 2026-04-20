package io.github.bbobbogi.stream4j.common;

/**
 * Donation categories normalized across platforms.
 *
 * @since 1.0.0
 */
public enum DonationType {
    /** Chat donation, including standard paid chat events. */
    CHAT,
    /** Video donation event. */
    VIDEO,
    /** Mission donation event. */
    MISSION,
    /** Party donation event. */
    PARTY
}
