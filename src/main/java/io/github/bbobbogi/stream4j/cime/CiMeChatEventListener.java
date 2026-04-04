package io.github.bbobbogi.stream4j.cime;

import io.github.bbobbogi.stream4j.cime.chat.CiMeChatMessage;

/**
 * ci.me 채팅 이벤트를 수신하기 위한 리스너 인터페이스입니다.
 */
public interface CiMeChatEventListener {
    /**
     * 채팅 서버에 연결되었을 때 호출됩니다.
     *
     * @param chat 연결된 채팅 인스턴스
     * @param isReconnecting 재연결 여부
     */
    default void onConnect(CiMeChat chat, boolean isReconnecting) {}

    /**
     * 채팅 서버와의 연결이 종료되었을 때 호출됩니다.
     *
     * @param code 연결 종료 코드
     * @param reason 연결 종료 사유
     * @param remote 원격에서 종료되었는지 여부
     * @param tryingToReconnect 재연결 시도 여부
     */
    default void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {}

    default void onBroadcastEnd(CiMeChat chat) {}

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
    default void onChat(CiMeChatMessage msg) {}

    /**
     * 시스템 이벤트를 수신했을 때 호출됩니다.
     * (예: aws:DELETE_MESSAGE, aws:DISCONNECT_USER 등)
     *
     * @param eventName 이벤트 이름
     * @param rawJson 원본 JSON 문자열
     */
    default void onEvent(String eventName, String rawJson) {}
}
