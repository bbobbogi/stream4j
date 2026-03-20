package com.bbobbogi.stream4j.chzzk.chat;

import com.bbobbogi.stream4j.chzzk.Chzzk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link ChzzkChat} 인스턴스를 생성하기 위한 빌더 클래스입니다.
 */
public class ChzzkChatBuilder {

    private static final Pattern CHZZK_CHANNEL_URL_PATTERN = Pattern.compile("^(?:https?://)?(?:m\\.)?chzzk\\.naver\\.com/(?:live/)?([a-f0-9]{32})(?:/.*)?$");

    private ArrayList<ChatEventListener> listeners = new ArrayList<>();
    private String channelId;
    private Chzzk chzzk;
    private boolean autoReconnect = true;
    private boolean debug = false;

    /**
     * ChzzkChatBuilder를 생성합니다.
     *
     * @param chzzk Chzzk 인스턴스
     * @param channelId 채널 ID
     */
    public ChzzkChatBuilder(Chzzk chzzk, String channelId) {
        this.chzzk = chzzk;
        this.channelId = resolveChannelId(channelId);
    }

    public static String resolveChannelId(String input) {
        if (input == null) {
            return null;
        }

        String trimmed = input.trim();
        Matcher matcher = CHZZK_CHANNEL_URL_PATTERN.matcher(trimmed);
        if (matcher.matches()) {
            return matcher.group(1);
        }

        return trimmed;
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

    public ChzzkChatBuilder withDebugMode() {
        this.debug = true;
        return this;
    }

    /**
     * {@link ChzzkChat} 인스턴스를 생성합니다.
     *
     * @return 생성된 ChzzkChat 인스턴스
     * @throws IOException API 요청 실패 시
     */
    public ChzzkChat build() throws IOException {
        ChzzkChat chat = new ChzzkChat(chzzk, channelId, autoReconnect, debug);

        for (ChatEventListener listener : listeners) {
            chat.addListener(listener);
        }

        return chat;
    }

}
