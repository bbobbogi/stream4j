package com.bbobbogi.stream4j.common;

/**
 * 플랫폼 공통 라이브 방송 정보 인터페이스입니다.
 * <p>각 플랫폼(치지직, CiMe, SOOP, YouTube)의 라이브 정보 클래스가 이 인터페이스를 구현합니다.</p>
 */
public interface LiveInfo {

    /**
     * 현재 방송 중인지 여부를 반환합니다.
     *
     * @return 방송 중이면 {@code true}
     */
    boolean isLive();

    /**
     * 방송 제목을 반환합니다.
     *
     * @return 방송 제목 (없으면 {@code null})
     */
    String getTitle();
}
