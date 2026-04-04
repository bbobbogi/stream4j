package io.github.bbobbogi.stream4j.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Normalized donation payload shared across all supported platforms.
 *
 * @param platform non-null platform where the donation event was generated
 * @param type non-null donation category
 * @param status non-null normalized donation status ({@link DonationStatus})
 * @param userId nullable donor identifier in the source platform
 * @param nickname non-null donor nickname
 * @param message non-null donation message text
 * @param anonymous whether the donor was anonymous in the source platform
 * @param parsedAmount non-null normalized amount and currency data ({@link CurrencyUtils.ParsedAmount})
 * @param raw nullable raw platform event object for advanced casting and access
 * @since 1.0.0
 */
public record Donation(
        @NotNull DonationPlatform platform,
        @NotNull DonationType type,
        @NotNull DonationStatus status,
        @Nullable String userId,
        @NotNull String nickname,
        @NotNull String message,
        boolean anonymous,
        @NotNull CurrencyUtils.ParsedAmount parsedAmount,
        @Nullable Object raw
) {
    public Donation {
        nickname = nickname != null ? nickname : "익명의 후원자";
        message = message != null ? message : "";
    }

    /**
     * Returns the numeric donation amount as an integer.
     *
     * @return parsed amount, or {@code 0} when parsing fails
     */
    public int amount() {
        try {
            return (int) Double.parseDouble(parsedAmount.amount());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Returns the normalized currency code.
     *
     * @return currency code from {@link #parsedAmount()}
     */
    public String currencyCode() {
        return parsedAmount.currencyCode();
    }

    /**
     * Converts the donation amount to KRW when conversion is supported.
     *
     * @return converted KRW amount, or {@code 0} when unavailable
     */
    public int amountInKRW() {
        return CurrencyUtils.toKRW(parsedAmount);
    }

    /**
     * Returns a formatted amount string for display.
     *
     * @return formatted amount text
     */
    public String formattedAmount() {
        return CurrencyUtils.format(parsedAmount);
    }
}
