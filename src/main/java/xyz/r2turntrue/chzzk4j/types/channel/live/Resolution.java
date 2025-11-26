package xyz.r2turntrue.chzzk4j.types.channel.live;

import org.jetbrains.annotations.NotNull;

/**
 * 라이브 스트리밍 해상도를 나타내는 열거형입니다.
 */
public enum Resolution {

    /**
     * 1080p 해상도
     */
    R_1080(1080),

    /**
     * 720p 해상도
     */
    R_720(720),

    /**
     * 480p 해상도
     */
    R_480(480),

    /**
     * 360p 해상도
     */
    R_360(360),

    /**
     * 270p 해상도
     */
    R_270(270),

    /**
     * 144p 해상도
     */
    R_144(144);

    private final int raw;

    /**
     * Resolution 열거형 값을 생성합니다.
     *
     * @param raw 해상도 픽셀 값
     */
    Resolution(int raw) {
        this.raw = raw;
    }

    /**
     * 해상도 픽셀 값을 반환합니다.
     *
     * @return 해상도 픽셀 값
     */
    public int getRaw() {
        return raw;
    }

    /**
     * 해상도 픽셀 값을 문자열로 반환합니다.
     *
     * @return 해상도 픽셀 값 문자열
     */
    public @NotNull String getRawAsString() {
        return Integer.toString(raw);
    }

}
