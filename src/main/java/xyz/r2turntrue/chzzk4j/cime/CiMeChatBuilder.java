package xyz.r2turntrue.chzzk4j.cime;

import java.util.ArrayList;

/**
 * {@link CiMeChat} 인스턴스를 생성하기 위한 빌더 클래스입니다.
 *
 * <p>사용 예시:</p>
 * <pre>
 * CiMeChat chat = new CiMeChatBuilder("channel_slug")
 *         .withChatListener(new CiMeChatEventListener() {
 *             {@literal @}Override
 *             public void onChat(CiMeChatMessage msg) {
 *                 System.out.println(msg.getContent());
 *             }
 *         })
 *         .build();
 *
 * chat.connectBlocking();
 * </pre>
 */
public class CiMeChatBuilder {

    private final ArrayList<CiMeChatEventListener> listeners = new ArrayList<>();
    private final String channelSlug;
    private boolean autoReconnect = true;
    private boolean debug = false;

    /**
     * CiMeChatBuilder를 생성합니다.
     *
     * @param channelSlug 채널 슬러그 (ci.me URL에서 사용하는 채널 식별자)
     */
    public CiMeChatBuilder(String channelSlug) {
        this.channelSlug = channelSlug;
    }

    /**
     * 채팅 이벤트 리스너를 추가합니다.
     *
     * @param listener 추가할 리스너
     * @return 현재 빌더 인스턴스
     */
    public CiMeChatBuilder withChatListener(CiMeChatEventListener listener) {
        listeners.add(listener);
        return this;
    }

    /**
     * 자동 재연결 설정을 지정합니다.
     *
     * @param autoReconnect 자동 재연결 여부
     * @return 현재 빌더 인스턴스
     */
    public CiMeChatBuilder withAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
        return this;
    }

    /**
     * 디버그 모드를 활성화합니다.
     *
     * @return 현재 빌더 인스턴스
     */
    public CiMeChatBuilder withDebugMode() {
        this.debug = true;
        return this;
    }

    /**
     * {@link CiMeChat} 인스턴스를 생성합니다.
     *
     * @return 생성된 CiMeChat 인스턴스
     */
    public CiMeChat build() {
        CiMeChat chat = new CiMeChat(channelSlug, autoReconnect, debug);

        for (CiMeChatEventListener listener : listeners) {
            chat.listeners.add(listener);
        }

        return chat;
    }
}
