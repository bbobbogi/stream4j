package com.bbobbogi.stream4j.chzzk.chat;

/**
 * 채팅 이벤트를 수신하기 위한 리스너 인터페이스입니다.
 */
public interface ChatEventListener {
    /**
     * 채팅 서버에 연결되었을 때 호출됩니다.
     *
     * @param chat 연결된 채팅 인스턴스
     * @param isReconnecting 재연결 여부
     */
    default void onConnect(ChzzkChat chat, boolean isReconnecting) {}

    /**
     * 채팅 서버와의 연결이 종료되었을 때 호출됩니다.
     *
     * @param code 연결 종료 코드
     * @param reason 연결 종료 사유
     * @param remote 원격에서 종료되었는지 여부
     * @param tryingToReconnect 재연결 시도 여부
     */
    default void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {}

    /**
     * 방송이 종료되었을 때 호출됩니다.
     * 30초 간격으로 라이브 상태를 폴링하여 감지합니다.
     *
     * @param chat 채팅 인스턴스
     */
    default void onBroadcastEnd(ChzzkChat chat) {}

    /**
     * 오류가 발생했을 때 호출됩니다.
     *
     * @param ex 발생한 예외
     */
    default void onError(Exception ex) {
        ex.printStackTrace();
    }

    /**
     * 일반 채팅 메시지를 수신했을 때 호출됩니다.
     *
     * @param msg 수신된 채팅 메시지
     */
    default void onChat(ChatMessage msg) {}

    /**
     * 후원 채팅 메시지를 수신했을 때 호출됩니다.
     *
     * @param msg 수신된 후원 메시지
     */
    default void onDonationChat(DonationMessage msg) {}

    /**
     * 미션 후원 채팅 메시지를 수신했을 때 호출됩니다.
     *
     * @param msg 수신된 미션 후원 메시지
     */
    default void onMissionDonationChat(MissionDonationMessage msg) {}

    /**
     * 파티 후원 채팅 메시지를 수신했을 때 호출됩니다.
     *
     * @param msg 수신된 파티 후원 메시지
     */
    default void onPartyDonationChat(PartyDonationMessage msg) {}

    /**
     * 구독 채팅 메시지를 수신했을 때 호출됩니다.
     *
     * @param msg 수신된 구독 메시지
     */
    default void onSubscriptionChat(SubscriptionMessage msg) {}

    /**
     * 미션 후원 이벤트를 수신했을 때 호출됩니다.
     *
     * @param msg 수신된 미션 후원 메시지
     */
    default void onMissionDonation(MissionDonationMessage msg) {}

    /**
     * 미션 참여 후원 이벤트를 수신했을 때 호출됩니다.
     *
     * @param msg 수신된 미션 참여 후원 메시지
     */
    default void onMissionDonationParticipation(MissionParticipationDonationMessage msg) {}

    /**
     * 파티 후원 정보 이벤트를 수신했을 때 호출됩니다.
     *
     * @param info 파티 후원 정보
     */
    default void onPartyDonationInfo(PartyDonationInfo info) {}

    /**
     * 구독권 선물 이벤트를 수신했을 때 호출됩니다.
     *
     * @param event 구독권 선물 이벤트
     */
    default void onSubscriptionGift(SubscriptionGiftEvent event) {}

    /**
     * 구독권 선물 수신자 이벤트를 수신했을 때 호출됩니다.
     *
     * @param event 구독권 선물 수신자 이벤트
     */
    default void onSubscriptionGiftReceiver(SubscriptionGiftReceiverEvent event) {}

    /**
     * 임시 제재 이벤트를 수신했을 때 호출됩니다.
     *
     * @param event 임시 제재 이벤트
     */
    default void onTemporaryRestrict(TemporaryRestrictEvent event) {}

    /**
     * 후원 활성화 상태 변경 이벤트를 수신했을 때 호출됩니다.
     *
     * @param event 후원 활성화 상태 변경 이벤트
     */
    default void onChangeDonationActive(ChangeDonationActiveEvent event) {}

    /**
     * 파티 후원 종료 이벤트를 수신했을 때 호출됩니다.
     *
     * @param event 파티 후원 종료 이벤트
     */
    default void onPartyDonationFinish(PartyDonationFinishEvent event) {}

    /**
     * 파티 후원 확인 이벤트를 수신했을 때 호출됩니다.
     *
     * @param event 파티 후원 확인 이벤트
     */
    default void onPartyDonationConfirm(PartyDonationConfirmEvent event) {}

    /**
     * IIMS 페널티 이벤트를 수신했을 때 호출됩니다.
     *
     * @param event IIMS 페널티 이벤트
     */
    default void onIimsPenalty(IimsPenaltyEvent event) {}
}
