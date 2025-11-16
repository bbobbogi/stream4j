package xyz.r2turntrue.chzzk4j.chat;

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

    public int getDuration() {
        return duration;
    }

    public int getTimes() {
        return times;
    }

    public String getUserIdHash() {
        return userIdHash;
    }

    public String getCreatedTimeRaw() {
        return createdTime;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime != null ? LocalDateTime.parse(createdTime, ISO_FORMATTER) : null;
    }

    public String getType() {
        return type;
    }

    public String getRawJson() {
        return rawJson;
    }
}
