package com.bbobbogi.stream4j.chzzk.chat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 임시 제재 이벤트
 *
 * 채팅 사용자에 대한 임시 제재 정보를 담고 있습니다.
 * EVENT 명령(cmd: 93006)으로 전송되며, type은 "TEMPORARY_RESTRICT"입니다.
 *
 * 제재 시간(duration)은 초 단위이며, 제재 횟수(times)는 누적 제재 횟수입니다.
 */
public class TemporaryRestrictEvent {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    int duration;
    int times;
    String userIdHash;
    String createdTime;
    String type;

    String rawJson;

    /**
     * TemporaryRestrictEvent를 생성합니다.
     */
    TemporaryRestrictEvent() {
    }

    /**
     * 제재 시간(초)을 반환합니다.
     *
     * @return 제재 시간(초)
     */
    public int getDuration() {
        return duration;
    }

    /**
     * 누적 제재 횟수를 반환합니다.
     *
     * @return 누적 제재 횟수
     */
    public int getTimes() {
        return times;
    }

    /**
     * 제재된 사용자의 ID 해시를 반환합니다.
     *
     * @return 사용자 ID 해시
     */
    public String getUserIdHash() {
        return userIdHash;
    }

    /**
     * 제재 생성 시간의 원본 문자열을 반환합니다.
     *
     * @return 제재 생성 시간 원본 문자열
     */
    public String getCreatedTimeRaw() {
        return createdTime;
    }

    /**
     * 제재 생성 시간을 반환합니다.
     *
     * @return 제재 생성 시간 (null일 수 있음)
     */
    public LocalDateTime getCreatedTime() {
        return createdTime != null ? LocalDateTime.parse(createdTime, ISO_FORMATTER) : null;
    }

    /**
     * 이벤트 타입을 반환합니다.
     *
     * @return 이벤트 타입
     */
    public String getType() {
        return type;
    }

    /**
     * 원본 JSON 문자열을 반환합니다.
     *
     * @return 원본 JSON 문자열
     */
    public String getRawJson() {
        return rawJson;
    }
}
