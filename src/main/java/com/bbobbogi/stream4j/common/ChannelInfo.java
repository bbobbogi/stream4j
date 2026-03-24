package com.bbobbogi.stream4j.common;

/**
 * 플랫폼 공통 채널 정보 인터페이스입니다.
 * <p>각 플랫폼(치지직, CiMe, SOOP, YouTube)의 채널 정보 클래스가 이 인터페이스를 구현합니다.</p>
 */
public interface ChannelInfo {

    /**
     * 플랫폼별 고유 채널 식별자를 반환합니다.
     *
     * @return 채널 ID
     */
    String getId();

    /**
     * 채널 표시 이름을 반환합니다.
     *
     * @return 채널 이름
     */
    String getName();

    /**
     * 현재 방송 중인지 여부를 반환합니다.
     *
     * @return 방송 중이면 {@code true}
     */
    boolean isLive();
}
