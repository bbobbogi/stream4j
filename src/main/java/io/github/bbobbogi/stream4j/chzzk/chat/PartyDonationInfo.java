package io.github.bbobbogi.stream4j.chzzk.chat;

/**
 * 파티 후원 정보 이벤트
 *
 * 파티 후원의 현재 상태 정보를 담고 있습니다.
 * EVENT 명령(cmd: 93006)으로 전송되며, type은 "PARTY_DONATION_INFO"입니다.
 *
 * 파티 후원의 멤버 수, 총 후원 금액, 상태(OPEN/CLOSED) 등을 포함합니다.
 * 이 이벤트는 주기적으로 전송되는 것이 아니라 파티 후원 상태가 변경될 때마다 전송됩니다
 * (예: 멤버 수 변경, 총 후원 금액 변경, 상태 변경).
 *
 * 참고: 일정 금액 이하의 소액 후원은 PartyDonationMessage를 발생시키지 않고,
 * 누적 금액만 PARTY_DONATION_INFO의 totalDonationAmount에 반영될 수 있습니다.
 *
 * 파티 후원 라이프사이클:
 * 1. CHANGE_DONATION_ACTIVE (donationType: "PARTY", donationActive: true) - 파티 후원 시작
 * 2. PARTY_DONATION_INFO (파티 상태 정보) - 파티 상태 변경 시마다 전송될 수 있음
 * 3. PartyDonationMessage (파티 후원) - 개별 참여자마다 반복 전송 (일정 금액 이상)
 * 4. CHANGE_DONATION_ACTIVE (donationType: "PARTY", donationActive: false) - 파티 후원 비활성화
 * 5. PARTY_DONATION_FINISH (confirmNeeded: true) - 파티 후원 종료
 * 6. PARTY_DONATION_CONFIRM - 각 채널별 순위 및 정산 정보 (채널 수만큼 반복)
 *
 * 참고: 파티 후원 시작 후 채팅에 접속한 경우 1번 이벤트를 받지 못하고 2번부터 받을 수 있음
 */
public class PartyDonationInfo {
    boolean hostChannelVerifiedMark;
    String hostChannelNickname;
    int memberCount;
    String partyName;
    String type;
    String[] profileImageUrlList;
    int totalDonationAmount;
    String status;

    public String rawJson;

    /**
     * PartyDonationInfo를 생성합니다.
     */
    public PartyDonationInfo() {
    }

    /**
     * 원본 JSON 문자열을 반환합니다.
     *
     * @return 원본 JSON 문자열
     */
    public String getRawJson() {
        return rawJson;
    }

    /**
     * 호스트 채널의 인증 마크 여부를 반환합니다.
     *
     * @return 호스트 채널 인증 마크 여부
     */
    public boolean isHostChannelVerifiedMark() {
        return hostChannelVerifiedMark;
    }

    /**
     * 호스트 채널의 닉네임을 반환합니다.
     *
     * @return 호스트 채널 닉네임
     */
    public String getHostChannelNickname() {
        return hostChannelNickname;
    }

    /**
     * 파티 멤버 수를 반환합니다.
     *
     * @return 파티 멤버 수
     */
    public int getMemberCount() {
        return memberCount;
    }

    /**
     * 파티 이름을 반환합니다.
     *
     * @return 파티 이름
     */
    public String getPartyName() {
        return partyName;
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
     * 프로필 이미지 URL 목록을 반환합니다.
     *
     * @return 프로필 이미지 URL 배열
     */
    public String[] getProfileImageUrlList() {
        return profileImageUrlList;
    }

    /**
     * 총 후원 금액을 반환합니다.
     *
     * @return 총 후원 금액
     */
    public int getTotalDonationAmount() {
        return totalDonationAmount;
    }

    /**
     * 파티 상태의 원본 문자열을 반환합니다.
     *
     * @return 파티 상태 원본 문자열
     */
    public String getStatusRaw() {
        return status;
    }

    /**
     * 파티 상태를 반환합니다.
     *
     * @return 파티 상태
     */
    public PartyStatus getStatus() {
        return PartyStatus.fromString(status);
    }

    /**
     * 파티가 열려있는지 확인합니다.
     *
     * @return 파티가 열려있으면 true
     */
    public boolean isOpen() {
        return PartyStatus.OPEN == getStatus();
    }
}
