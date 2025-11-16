package xyz.r2turntrue.chzzk4j.chat;

/**
 * 파티 후원 확인/정산 이벤트
 *
 * 파티 후원이 종료되고 정산 정보가 확정되었을 때 전송됩니다.
 * EVENT 명령(cmd: 93006)으로 전송되며, type은 "PARTY_DONATION_CONFIRM"입니다.
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
public class PartyDonationConfirmEvent {
    int rank;
    String channelName;
    String rankName;
    String type;

    String rawJson;

    /**
     * 파티 후원 순위
     */
    public int getRank() {
        return rank;
    }

    /**
     * 채널 이름
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * 순위 이름 (예: "참가비")
     */
    public String getRankName() {
        return rankName;
    }

    public String getType() {
        return type;
    }

    public String getRawJson() {
        return rawJson;
    }
}
