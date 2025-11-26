package xyz.r2turntrue.chzzk4j.chat;

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


    public int getDurationTime() {
        return extras.durationTime;
    }

    public String getMissionDonationId() {
        return extras.missionDonationId;
    }

    public String getMissionDonationTypeRaw() {
        return extras.missionDonationType;
    }

    public MissionDonationType getMissionDonationType() {
        return MissionDonationType.fromString(extras.missionDonationType);
    }

    public String getMissionCreatedTimeRaw() {
        return extras.missionCreatedTime;
    }

    public LocalDateTime getMissionCreatedTime() {
        return parseMissionTime(extras.missionCreatedTime);
    }

    public String getMissionStartTimeRaw() {
        return extras.missionStartTime;
    }

    public LocalDateTime getMissionStartTime() {
        return parseMissionTime(extras.missionStartTime);
    }

    public String getMissionEndTimeRaw() {
        return extras.missionEndTime;
    }

    public LocalDateTime getMissionEndTime() {
        return parseMissionTime(extras.missionEndTime);
    }

    private LocalDateTime parseMissionTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(timeStr, MISSION_TIME_FORMATTER);
    }

    public String getMissionText() {
        return extras.missionText;
    }

    public int getTotalPayAmount() {
        return extras.totalPayAmount;
    }

    public int getParticipationCount() {
        return extras.participationCount;
    }

    public String getMissionStatusRaw() {
        return extras.status;
    }

    public MissionStatus getMissionStatus() {
        return MissionStatus.fromString(extras.status);
    }

    public boolean isMissionSucceed() {
        return extras.success;
    }

    /**
     * 닉네임 (익명일 경우 null)
     * EVENT 형태: 상속받은 profile 내부
     * CHAT/DONATION 형태: extras 내부 또는 profile 내부
     */
    public String getNickname() {
        if (profile != null && profile.getNickname() != null) return profile.getNickname();
        return extras != null ? extras.nickname : null;
    }

    /**
     * 사용자 ID 해시 (익명일 경우 null)
     * EVENT 형태: 상속받은 userIdHash 필드 사용
     * CHAT/DONATION 형태: extras 내부 또는 userIdHash 필드
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
     */
    public boolean isVerifiedMark() {
        if (profile != null) return profile.isVerifiedMark();
        return extras != null && extras.verifiedMark;
    }

    /**
     * 익명 여부
     * EVENT 형태: extras에 정보 없음 (항상 false)
     * CHAT/DONATION 형태: extras 내부
     */
    public boolean isAnonymous() {
        return extras != null && extras.isAnonymous;
    }

    /**
     * 익명 토큰 (익명일 경우에만 존재)
     * EVENT 형태: extras에 정보 없음
     * CHAT/DONATION 형태: extras 내부
     */
    public String getAnonymousToken() {
        return extras != null ? extras.anonymousToken : null;
    }

    /**
     * 후원 ID
     * EVENT 형태: extras에 정보 없음
     * CHAT/DONATION 형태: extras 내부
     */
    public String getDonationId() {
        return extras != null ? extras.donationId : null;
    }

    /**
     * 결제 타입 (예: "CURRENCY")
     * EVENT 형태: extras에 정보 없음
     * CHAT/DONATION 형태: extras 내부
     */
    public String getPayType() {
        return extras != null ? extras.payType : null;
    }

    /**
     * 연속 후원 일수
     * EVENT 형태: extras에 정보 없음
     * CHAT/DONATION 형태: extras 내부
     */
    public int getContinuousDonationDays() {
        return extras != null ? extras.continuousDonationDays : 0;
    }
}
