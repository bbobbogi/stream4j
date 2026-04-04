package io.github.bbobbogi.stream4j.youtube.chat;

/**
 * Author roles that can appear in YouTube chat.
 *
 * @since 1.0.0
 */
public enum AuthorType {
    /** Regular viewer. */
    NORMAL,
    /** Verified channel user. */
    VERIFIED,
    /** Broadcast owner. */
    OWNER,
    /** Channel member. */
    MEMBER,
    /** Chat moderator. */
    MODERATOR,
    /** Official YouTube system account. */
    YOUTUBE,
}
