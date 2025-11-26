package xyz.r2turntrue.chzzk4j.chat;

/**
 * 파티 후원 종료 이벤트
 *
 * 파티 후원이 종료되었을 때 전송됩니다.
 * EVENT 명령(cmd: 93006)으로 전송되며, type은 "PARTY_DONATION_FINISH"입니다.
 *
 * confirmNeeded가 true일 경우, 이후 PARTY_DONATION_CONFIRM 이벤트가 전송됩니다.
 *
 * 파티 후원 라이프사이클:
 * 1. CHANGE_DONATION_ACTIVE (donationType: "PARTY", donationActive: true) - 파티 후원 시작
 * 2. PARTY_DONATION_INFO (파티 상태 정보) - 파티 진행 중 여러 번 전송될 수 있음
 * 3. PartyDonationMessage (파티 후원) - 개별 참여자마다 반복 전송
 * 4. CHANGE_DONATION_ACTIVE (donationType: "PARTY", donationActive: false) - 파티 후원 비활성화
 * 5. PARTY_DONATION_FINISH (confirmNeeded: true) - 파티 후원 종료
 * 6. PARTY_DONATION_CONFIRM - 각 채널별 순위 및 정산 정보 (채널 수만큼 반복)
 *
 * 참고: 파티 후원 시작 후 채팅에 접속한 경우 1번 이벤트를 받지 못하고 2번부터 받을 수 있음
 */
public class PartyDonationFinishEvent {
    boolean confirmNeeded;
    String type;

    String rawJson;

    /**
     * PartyDonationFinishEvent를 생성합니다.
     */
    PartyDonationFinishEvent() {
    }

    /**
     * 확인이 필요한지 여부를 반환합니다.
     * true일 경우 PARTY_DONATION_CONFIRM 이벤트가 뒤따릅니다.
     *
     * @return 확인 필요 여부
     */
    public boolean isConfirmNeeded() {
        return confirmNeeded;
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
