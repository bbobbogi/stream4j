package com.bbobbogi.stream4j.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 모든 플랫폼의 후원 정보를 통합하는 레코드입니다.
 *
 * @param platform       후원이 발생한 플랫폼
 * @param type           후원 종류
 * @param status         후원 상태 ({@link DonationStatus})
 * @param userId         후원자 ID (플랫폼별 식별자)
 * @param nickname       후원자 닉네임
 * @param message        후원 메시지
 * @param parsedAmount   플랫폼 단위 금액 + 통화 코드 ({@link CurrencyUtils.ParsedAmount})
 * @param raw            플랫폼별 원본 메시지 객체 (타입 캐스팅 필요)
 */
public record Donation(
        @NotNull DonationPlatform platform,
        @NotNull DonationType type,
        @NotNull DonationStatus status,
        @Nullable String userId,
        @NotNull String nickname,
        @NotNull String message,
        @NotNull CurrencyUtils.ParsedAmount parsedAmount,
        @Nullable Object raw
) {
    public Donation {
        nickname = nickname != null ? nickname : "익명의 후원자";
        message = message != null ? message : "";
    }

    public int amount() {
        try {
            return (int) Double.parseDouble(parsedAmount.amount());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String currencyCode() {
        return parsedAmount.currencyCode();
    }

    public int amountInKRW() {
        return CurrencyUtils.toKRW(parsedAmount);
    }

    public String formattedAmount() {
        return CurrencyUtils.format(parsedAmount);
    }
}
