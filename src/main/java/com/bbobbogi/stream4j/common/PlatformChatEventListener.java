package com.bbobbogi.stream4j.common;

/**
 * 플랫폼 공통 채팅 이벤트 리스너 인터페이스입니다.
 * <p>각 플랫폼의 ChatEventListener가 이 인터페이스를 확장합니다.</p>
 */
public interface PlatformChatEventListener {

    default void onConnect(PlatformChat chat, boolean isReconnecting) {}

    default void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {}

    default void onBroadcastEnd(PlatformChat chat) {}

    default void onError(Exception ex) {
        ex.printStackTrace();
    }

    default void onUnhandledEvent(String eventName, String rawData) {}
}
