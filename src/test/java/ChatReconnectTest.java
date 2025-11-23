import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import xyz.r2turntrue.chzzk4j.chat.*;

import java.io.IOException;
import java.util.Optional;

public class ChatReconnectTest extends ChzzkTestBase {
    // to test memory leaks
    @Test
    void testingChatReconnect() throws IOException, InterruptedException {
        // 동적으로 라이브 중인 채널을 찾음
        Optional<String> liveChannelId = findLiveChannelId();
        Assumptions.assumeTrue(liveChannelId.isPresent(), "라이브 중인 채널이 없어 테스트를 건너뜁니다.");

        String channelId = liveChannelId.get();
        System.out.println("테스트에 사용할 채널 ID: " + channelId);

        ChzzkChat chat = chzzk.chat(channelId)
                        .withChatListener(new ChatEventListener() {
                            @Override
                            public void onConnect(ChzzkChat chat, boolean isReconnecting) {
                                chat.reconnectAsync();
                            }
                        })
                        .build();
        chat.connectBlocking();
        Thread.sleep(700000);
        chat.closeBlocking();
    }
}
