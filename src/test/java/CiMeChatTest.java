import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import io.github.bbobbogi.stream4j.cime.*;
import io.github.bbobbogi.stream4j.cime.chat.CiMeChatMessage;

import java.util.Optional;

public class CiMeChatTest extends CiMeTestBase {
    // CI 환경에서는 30초, 로컬에서는 더 길게 대기
    private static final long TEST_DURATION_MS = 30_000;

    @Test
    void testingCiMeChat() throws InterruptedException {
        // 동적으로 라이브 중인 채널을 찾음
        Optional<String> liveChannelSlug = findLiveChannelSlug();
        Assumptions.assumeTrue(liveChannelSlug.isPresent(), "라이브 중인 ci.me 채널이 없어 테스트를 건너뜁니다.");

        String channelSlug = liveChannelSlug.get();
        System.out.println("테스트에 사용할 채널 슬러그: " + channelSlug);

        CiMeChat chat = new CiMeChatBuilder(channelSlug)
                .withDebugMode()
                .withChatListener(new CiMeChatEventListener() {
                    @Override
                    public void onConnect(CiMeChat chat, boolean isReconnecting) {
                        System.out.println("[CiMe] Connected! (reconnecting: " + isReconnecting + ")");
                    }

                    @Override
                    public void onError(Exception ex) {
                        ex.printStackTrace();
                    }

                    @Override
                    public void onChat(CiMeChatMessage msg) {
                        System.out.println(msg);

                        if (!msg.hasUser()) {
                            System.out.println("[CiMe Chat] 익명: " + msg.getContent());
                            return;
                        }

                        CiMeChatMessage.CiMeUser user = msg.getUser();
                        String nickname = user.getNickname() != null ? user.getNickname() : "알수없음";
                        System.out.println("[CiMe Chat] " + nickname + ": " + msg.getContent());
                    }

                    @Override
                    public void onEvent(String eventName, String rawJson) {
                        System.out.println("[CiMe Event] " + eventName + ": " + rawJson);
                    }

                    @Override
                    public void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {
                        System.out.println("[CiMe] Connection closed: " + reason + " (reconnecting: " + tryingToReconnect + ")");
                    }
                })
                .build();

         chat.connect();
         Thread.sleep(TEST_DURATION_MS);
         chat.close();
    }
}
