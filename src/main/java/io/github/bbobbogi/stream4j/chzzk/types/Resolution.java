package io.github.bbobbogi.stream4j.chzzk.types;

import org.jetbrains.annotations.NotNull;

/**
 * Enum representing live streaming resolution.
 *
 * @since 1.0.0
 */
public enum Resolution {

    /**
     * 1080p resolution.
     */
    R_1080(1080),

    /**
     * 720p resolution.
     */
    R_720(720),

    /**
     * 480p resolution.
     */
    R_480(480),

    /**
     * 360p resolution.
     */
    R_360(360),

    /**
     * 270p resolution.
     */
    R_270(270),

    /**
     * 144p resolution.
     */
    R_144(144);

    private final int raw;

    /**
     * Creates a {@link Resolution} enum value.
     *
     * @param raw resolution pixel value
     */
    Resolution(int raw) {
        this.raw = raw;
    }

    /**
     * Returns the resolution pixel value.
     *
     * @return resolution pixel value
     */
    public int getRaw() {
        return raw;
    }

    /**
     * Returns the resolution pixel value as a string.
     *
     * @return resolution pixel value string
     */
    public @NotNull String getRawAsString() {
        return Integer.toString(raw);
    }

}
