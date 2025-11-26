package xyz.r2turntrue.chzzk4j.chat;

/**
 * 후원 활성화 상태 변경 이벤트
 *
 * 특정 후원 타입의 활성화 상태가 변경되었을 때 전송됩니다.
 * EVENT 명령(cmd: 93006)으로 전송되며, type은 "CHANGE_DONATION_ACTIVE"입니다.
 *
 * donationType: "CHAT", "VIDEO", "MISSION", "PARTY" 등
 *
 * 파티 후원 라이프사이클 (donationType이 "PARTY"인 경우):
 * 1. CHANGE_DONATION_ACTIVE (donationType: "PARTY", donationActive: true) - 파티 후원 시작
 * 2. PARTY_DONATION_INFO (파티 상태 정보) - 파티 진행 중 여러 번 전송될 수 있음
 * 3. PartyDonationMessage (파티 후원) - 개별 참여자마다 반복 전송
 * 4. CHANGE_DONATION_ACTIVE (donationType: "PARTY", donationActive: false) - 파티 후원 비활성화
 * 5. PARTY_DONATION_FINISH (confirmNeeded: true) - 파티 후원 종료
 * 6. PARTY_DONATION_CONFIRM - 각 채널별 순위 및 정산 정보 (채널 수만큼 반복)
 *
 * 참고: 파티 후원 시작 후 채팅에 접속한 경우 1번 이벤트를 받지 못하고 2번부터 받을 수 있음
 */
public class ChangeDonationActiveEvent {
    boolean donationActive;
    String donationType;
    String type;

    String rawJson;

    /**
     * ChangeDonationActiveEvent를 생성합니다.
     */
    ChangeDonationActiveEvent() {
    }

    /**
     * 후원 활성화 여부를 반환합니다.
     *
     * @return 후원이 활성화되어 있으면 {@code true}, 그렇지 않으면 {@code false}
     */
    public boolean isDonationActive() {
        return donationActive;
    }

    /**
     * 후원 타입을 반환합니다.
     * <ul>
     *   <li>"CHAT": 채팅 후원</li>
     *   <li>"VIDEO": 영상 후원</li>
     *   <li>"MISSION": 미션 후원</li>
     *   <li>"PARTY": 파티 후원</li>
     * </ul>
     *
     * @return 후원 타입 문자열
     */
    public String getDonationType() {
        return donationType;
    }

    /**
     * 이벤트 타입을 반환합니다.
     *
     * @return 이벤트 타입 문자열
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
