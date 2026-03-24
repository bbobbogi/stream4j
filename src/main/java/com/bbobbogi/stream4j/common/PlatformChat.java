package com.bbobbogi.stream4j.common;

import java.util.concurrent.CompletableFuture;

/**
 * 플랫폼 공통 채팅 클라이언트 인터페이스입니다.
 * <p>각 플랫폼(치지직, CiMe, SOOP, YouTube)의 Chat 클래스가 이 인터페이스를 구현합니다.</p>
 */
public interface PlatformChat {

    boolean isConnected();

    CompletableFuture<Void> connectAsync();

    void connect();

    CompletableFuture<Void> closeAsync();

    void close();

    CompletableFuture<Void> reconnectAsync();

    void reconnect();
}
