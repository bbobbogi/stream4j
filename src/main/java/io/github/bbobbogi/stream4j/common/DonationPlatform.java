package io.github.bbobbogi.stream4j.common;

import org.jetbrains.annotations.Nullable;

/**
 * Streaming platforms that can provide donation events.
 *
 * @since 1.0.0
 */
public enum DonationPlatform {
    /** Naver Chzzk platform. */
    CHZZK,
    /** CiMe platform based on Amazon IVS. */
    CIME,
    /** SOOP platform (formerly AfreecaTV). */
    SOOP,
    /** Toonation donation alert platform. */
    TOONATION,
    /** YouTube */
    YOUTUBE;

    /**
     * Resolves a platform by name.
     *
     * @param name platform name, case-insensitive
     * @return matching platform, or {@code null} when not found
     */
    @Nullable
    public static DonationPlatform from(String name) {
        if (name == null) return null;
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
