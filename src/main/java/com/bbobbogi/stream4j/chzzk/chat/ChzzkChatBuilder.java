package com.bbobbogi.stream4j.chzzk.chat;

import com.bbobbogi.stream4j.chzzk.Chzzk;

import java.io.IOException;
import java.util.ArrayList;

/**
 * {@link ChzzkChat} 인스턴스를 생성하기 위한 빌더 클래스입니다.
 */
public class ChzzkChatBuilder {

    private ArrayList<ChatEventListener> listeners = new ArrayList<>();
    private String channelId;
    private Chzzk chzzk;
    private boolean autoReconnect = true;

    /**
     * ChzzkChatBuilder를 생성합니다.
     *
     * @param chzzk Chzzk 인스턴스
     * @param channelId 채널 ID
     */
    public ChzzkChatBuilder(Chzzk chzzk, String channelId) {
        this.chzzk = chzzk;
        this.channelId = channelId;
    }

    /**
     * 채팅 이벤트 리스너를 추가합니다.
     *
     * @param listener 추가할 리스너
     * @return 현재 빌더 인스턴스
     */
    public ChzzkChatBuilder withChatListener(ChatEventListener listener) {
        listeners.add(listener);

        return this;
    }

    /**
     * 자동 재연결 설정을 지정합니다.
     *
     * @param autoReconnect 자동 재연결 여부
     * @return 현재 빌더 인스턴스
     */
    public ChzzkChatBuilder withAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;

        return this;
    }

    /**
     * {@link ChzzkChat} 인스턴스를 생성합니다.
     *
     * @return 생성된 ChzzkChat 인스턴스
     * @throws IOException API 요청 실패 시
     */
    public ChzzkChat build() throws IOException {
        ChzzkChat chat = new ChzzkChat(chzzk, channelId, autoReconnect);

        for (ChatEventListener listener : listeners) {
            chat.addListener(listener);
        }

        return chat;
    }

}
