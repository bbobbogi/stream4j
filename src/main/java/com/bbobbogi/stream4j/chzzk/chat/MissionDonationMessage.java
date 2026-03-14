package com.bbobbogi.stream4j.chzzk.chat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 미션 후원 메시지
 *
 * 미션 후원을 시작한 사람(미션 개설자)의 정보를 담고 있습니다.
 *
 * 두 가지 방식으로 전송됩니다:
 * 1. CHAT/DONATION 명령(cmd: 93101/93102)으로 채팅 메시지 형태
 *    - donationType: "MISSION"
 * 2. EVENT 명령(cmd: 93006)으로 이벤트 형태
 *    - type: "DONATION_MISSION_IN_PROGRESS" (미션 진행 상태 업데이트)
 *
 * 이벤트 전송 순서 (미션 라이프사이클):
 * 1. MissionDonationMessage (미션 생성) - status: PENDING
 *    - EVENT로 전송 + CHAT/DONATION으로도 전송
 * 2. MissionDonationMessage (미션 승인/거절) - status: APPROVED 또는 REJECTED
 *    - EVENT로 전송
 * 3. MissionParticipationDonationMessage (참여자 후원) - 참여자마다 반복 (APPROVED인 경우만)
 *    - EVENT로 전송 + CHAT/DONATION으로도 전송
 * 4. MissionDonationMessage (미션 완료/만료) - status: COMPLETED/EXPIRED
 *    - EVENT로 전송
 *
 * missionDonationType: ALONE(혼자 미션) / GROUP(그룹 미션)
 * status: PENDING(대기) / APPROVED(승인) / REJECTED(거부) / COMPLETED(완료) / EXPIRED(만료)
 */
public class MissionDonationMessage extends DonationMessage {
    private static final DateTimeFormatter MISSION_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * MissionDonationMessage를 생성합니다.
     */
    MissionDonationMessage() {
    }

    /**
     * 미션 지속 시간(초)을 반환합니다.
     *
     * @return 미션 지속 시간(초)
     */
    public int getDurationTime() {
        return extras.durationTime;
    }

    /**
     * 미션 후원 ID를 반환합니다.
     *
     * @return 미션 후원 ID
     */
    public String getMissionDonationId() {
        return extras.missionDonationId;
    }

    /**
     * 미션 후원 타입의 원본 문자열을 반환합니다.
     *
     * @return 미션 후원 타입 원본 문자열
     */
    public String getMissionDonationTypeRaw() {
        return extras.missionDonationType;
    }

    /**
     * 미션 후원 타입을 반환합니다.
     *
     * @return 미션 후원 타입
     */
    public MissionDonationType getMissionDonationType() {
        return MissionDonationType.fromString(extras.missionDonationType);
    }

    /**
     * 미션 생성 시간의 원본 문자열을 반환합니다.
     *
     * @return 미션 생성 시간 원본 문자열
     */
    public String getMissionCreatedTimeRaw() {
        return extras.missionCreatedTime;
    }

    /**
     * 미션 생성 시간을 반환합니다.
     *
     * @return 미션 생성 시간 (null일 수 있음)
     */
    public LocalDateTime getMissionCreatedTime() {
        return parseMissionTime(extras.missionCreatedTime);
    }

    /**
     * 미션 시작 시간의 원본 문자열을 반환합니다.
     *
     * @return 미션 시작 시간 원본 문자열
     */
    public String getMissionStartTimeRaw() {
        return extras.missionStartTime;
    }

    /**
     * 미션 시작 시간을 반환합니다.
     *
     * @return 미션 시작 시간 (null일 수 있음)
     */
    public LocalDateTime getMissionStartTime() {
        return parseMissionTime(extras.missionStartTime);
    }

    /**
     * 미션 종료 시간의 원본 문자열을 반환합니다.
     *
     * @return 미션 종료 시간 원본 문자열
     */
    public String getMissionEndTimeRaw() {
        return extras.missionEndTime;
    }

    /**
     * 미션 종료 시간을 반환합니다.
     *
     * @return 미션 종료 시간 (null일 수 있음)
     */
    public LocalDateTime getMissionEndTime() {
        return parseMissionTime(extras.missionEndTime);
    }

    private LocalDateTime parseMissionTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(timeStr, MISSION_TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 미션 텍스트를 반환합니다.
     *
     * @return 미션 텍스트
     */
    public String getMissionText() {
        return extras.missionText;
    }

    /**
     * 총 결제 금액을 반환합니다.
     *
     * @return 총 결제 금액
     */
    public int getTotalPayAmount() {
        return extras.totalPayAmount;
    }

    /**
     * 참여자 수를 반환합니다.
     *
     * @return 참여자 수
     */
    public int getParticipationCount() {
        return extras.participationCount;
    }

    /**
     * 미션 상태의 원본 문자열을 반환합니다.
     *
     * @return 미션 상태 원본 문자열
     */
    public String getMissionStatusRaw() {
        return extras.status;
    }

    /**
     * 미션 상태를 반환합니다.
     *
     * @return 미션 상태
     */
    public MissionStatus getMissionStatus() {
        return MissionStatus.fromString(extras.status);
    }

    /**
     * 미션 성공 여부를 반환합니다.
     *
     * @return 미션 성공 여부
     */
    public boolean isMissionSucceed() {
        return extras.success;
    }

    /**
     * 닉네임 (익명일 경우 null)
     * EVENT 형태: 상속받은 profile 내부
     * CHAT/DONATION 형태: extras 내부 또는 profile 내부
     *
     * @return 닉네임 (익명일 경우 null)
     */
    public String getNickname() {
        if (profile != null && profile.getNickname() != null) return profile.getNickname();
        return extras != null ? extras.nickname : null;
    }

    /**
     * 사용자 ID 해시 (익명일 경우 null)
     * EVENT 형태: 상속받은 userIdHash 필드 사용
     * CHAT/DONATION 형태: extras 내부 또는 userIdHash 필드
     *
     * @return 사용자 ID 해시 (익명일 경우 null)
     */
    @Override
    public String getUserIdHash() {
        if (super.getUserIdHash() != null) return super.getUserIdHash();
        return extras != null ? extras.userIdHash : null;
    }

    /**
     * 인증 마크 여부
     * EVENT 형태: 상속받은 profile 내부
     * CHAT/DONATION 형태: extras 내부 또는 profile 내부
     *
     * @return 인증 마크 여부
     */
    public boolean isVerifiedMark() {
        if (profile != null) return profile.isVerifiedMark();
        return extras != null && extras.verifiedMark;
    }

    /**
     * 익명 여부
     * EVENT 형태: extras에 정보 없음 (항상 false)
     * CHAT/DONATION 형태: extras 내부
     *
     * @return 익명 여부
     */
    public boolean isAnonymous() {
        return extras != null && extras.isAnonymous;
    }

    /**
     * 익명 토큰 (익명일 경우에만 존재)
     * EVENT 형태: extras에 정보 없음
     * CHAT/DONATION 형태: extras 내부
     *
     * @return 익명 토큰 (익명일 경우에만 존재)
     */
    public String getAnonymousToken() {
        return extras != null ? extras.anonymousToken : null;
    }

    /**
     * 후원 ID
     * EVENT 형태: extras에 정보 없음
     * CHAT/DONATION 형태: extras 내부
     *
     * @return 후원 ID
     */
    public String getDonationId() {
        return extras != null ? extras.donationId : null;
    }

    /**
     * 결제 타입 (예: "CURRENCY")
     * EVENT 형태: extras에 정보 없음
     * CHAT/DONATION 형태: extras 내부
     *
     * @return 결제 타입
     */
    public String getPayType() {
        return extras != null ? extras.payType : null;
    }

    /**
     * 연속 후원 일수
     * EVENT 형태: extras에 정보 없음
     * CHAT/DONATION 형태: extras 내부
     *
     * @return 연속 후원 일수
     */
    public int getContinuousDonationDays() {
        return extras != null ? extras.continuousDonationDays : 0;
    }
}
