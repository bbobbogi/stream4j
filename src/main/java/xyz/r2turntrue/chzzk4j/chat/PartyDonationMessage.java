package xyz.r2turntrue.chzzk4j.chat;

/**
 * 파티 후원 메시지
 *
 * 파티 후원에 참여한 사람의 정보를 담고 있습니다.
 * CHAT/DONATION 명령(cmd: 93101/93102)으로 전송되며, donationType은 "PARTY"입니다.
 *
 * partyName과 partyNo를 통해 어떤 파티에 후원했는지 확인할 수 있습니다.
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
public class PartyDonationMessage extends DonationMessage {
    public String getPartyDonationId() {
        return extras.partyDonationId;
    }

    public String getPartyName() {
        return extras.partyName;
    }

    public int getPartyNo() {
        return extras.partyNo;
    }
}
