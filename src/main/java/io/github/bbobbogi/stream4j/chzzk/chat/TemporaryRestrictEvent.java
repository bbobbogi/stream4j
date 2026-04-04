package io.github.bbobbogi.stream4j.chzzk.chat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Temporary restriction event.
 *
 * Contains temporary restriction information for a chat user.
 * Sent through EVENT command (cmd: 93006), and type is "TEMPORARY_RESTRICT".
 *
 * Restriction duration is in seconds, and times is the accumulated restriction count.
 *
 * @since 1.0.0
 */
public class TemporaryRestrictEvent {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    int duration;
    int times;
    String userIdHash;
    String createdTime;
    String type;

    public String rawJson;

    /**
     * Creates a {@link TemporaryRestrictEvent}.
     */
    public TemporaryRestrictEvent() {
    }

    /**
     * Returns the restriction duration in seconds.
     *
     * @return restriction duration in seconds
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Returns the accumulated restriction count.
     *
     * @return accumulated restriction count
     */
    public int getTimes() {
        return times;
    }

    /**
     * Returns the restricted user's ID hash.
     *
     * @return user ID hash
     */
    public String getUserIdHash() {
        return userIdHash;
    }

    /**
     * Returns the raw restriction creation time string.
     *
     * @return raw restriction creation time string
     */
    public String getCreatedTimeRaw() {
        return createdTime;
    }

    /**
     * Returns the restriction creation time.
     *
     * @return restriction creation time (nullable)
     */
    public LocalDateTime getCreatedTime() {
        return createdTime != null ? LocalDateTime.parse(createdTime, ISO_FORMATTER) : null;
    }

    /**
     * Returns the event type.
     *
     * @return event type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the raw JSON string.
     *
     * @return raw JSON string
     */
    public String getRawJson() {
        return rawJson;
    }
}
