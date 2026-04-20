package io.github.bbobbogi.stream4j.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Normalized subscription payload shared across all supported platforms.
 *
 * @param platform non-null platform where the subscription event was generated
 * @param type non-null subscription category ({@link SubscriptionType})
 * @param userId nullable subscriber identifier in the source platform
 * @param nickname non-null subscriber nickname
 * @param message non-null subscription message text
 * @param anonymous whether the subscriber was anonymous in the source platform
 * @param raw nullable raw platform event object for advanced casting and access
 * @since 1.0.0
 */
public record Subscription(
        @NotNull DonationPlatform platform,
        @NotNull SubscriptionType type,
        @Nullable String userId,
        @NotNull String nickname,
        @NotNull String message,
        boolean anonymous,
        @Nullable Object raw
) {
    public Subscription {
        nickname = nickname != null ? nickname : "익명의 구독자";
        message = message != null ? message : "";
    }
}
