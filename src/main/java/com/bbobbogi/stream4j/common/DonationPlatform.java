package com.bbobbogi.stream4j.common;

import org.jetbrains.annotations.Nullable;

/**
 * 후원을 수신할 수 있는 스트리밍 플랫폼입니다.
 */
public enum DonationPlatform {
    /** 치지직 (CHZZK) - 네이버 */
    CHZZK,
    /** ci.me - Amazon IVS 기반 */
    CIME,
    /** 숲 (SOOP/AfreecaTV) */
    SOOP,
    /** 투네이션 (Toonation) */
    TOONATION,
    /** YouTube */
    YOUTUBE;

    /**
     * 이름으로 플랫폼을 찾습니다.
     *
     * @param name 플랫폼 이름 (대소문자 무관)
     * @return 일치하는 플랫폼, 없으면 {@code null}
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
