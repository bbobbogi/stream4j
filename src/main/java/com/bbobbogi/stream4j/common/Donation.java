package com.bbobbogi.stream4j.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 모든 플랫폼의 후원 정보를 통합하는 레코드입니다.
 * <p>
 * 치지직, ci.me, 투네이션 등 다양한 플랫폼의 후원 데이터를
 * 하나의 공통 타입으로 표현합니다.
 *
 * @param platform  후원이 발생한 플랫폼
 * @param type      후원 종류
 * @param userId    후원자 ID (플랫폼별 식별자)
 * @param nickname  후원자 닉네임
 * @param message   후원 메시지
 * @param amount    후원 금액 (원)
 * @param raw       플랫폼별 원본 메시지 객체 (타입 캐스팅 필요)
 */
public record Donation(
        @NotNull DonationPlatform platform,
        @NotNull DonationType type,
        @Nullable String userId,
        @NotNull String nickname,
        @NotNull String message,
        int amount,
        @Nullable Object raw
) {
    public Donation {
        nickname = nickname != null ? nickname : "익명의 후원자";
        message = message != null ? message : "";
    }

    public Donation(
            @NotNull DonationPlatform platform,
            @NotNull DonationType type,
            @Nullable String userId,
            @NotNull String nickname,
            @NotNull String message,
            int amount
    ) {
        this(platform, type, userId, nickname, message, amount, null);
    }
}
