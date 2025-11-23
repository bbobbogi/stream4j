import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import xyz.r2turntrue.chzzk4j.chat.*;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatReconnectTest extends ChzzkTestBase {
    // CI 환경에서는 30초, 로컬에서는 700초 대기
    private static final long TEST_DURATION_MS = System.getenv("CI") != null ? 30_000 : 700_000;
    // 최대 재연결 횟수
    private static final int MAX_RECONNECT_COUNT = 5;
    // 재연결 딜레이 범위 (1~3초)
    private static final int MIN_RECONNECT_DELAY_MS = 1000;
    private static final int MAX_RECONNECT_DELAY_MS = 3000;

    // to test memory leaks
    @Test
    void testingChatReconnect() throws IOException, InterruptedException {
        // 동적으로 라이브 중인 채널을 찾음
        Optional<String> liveChannelId = findLiveChannelId();
        Assumptions.assumeTrue(liveChannelId.isPresent(), "라이브 중인 채널이 없어 테스트를 건너뜁니다.");

        String channelId = liveChannelId.get();
        System.out.println("테스트에 사용할 채널 ID: " + channelId);

        AtomicInteger reconnectCount = new AtomicInteger(0);
        Random random = new Random();

        ChzzkChat chat = chzzk.chat(channelId)
                        .withChatListener(new ChatEventListener() {
                            @Override
                            public void onConnect(ChzzkChat chat, boolean isReconnecting) {
                                int count = reconnectCount.incrementAndGet();
                                System.out.println("연결 " + count + "/" + MAX_RECONNECT_COUNT);

                                if (count < MAX_RECONNECT_COUNT) {
                                    // 랜덤 딜레이 후 재연결
                                    int delay = MIN_RECONNECT_DELAY_MS + random.nextInt(MAX_RECONNECT_DELAY_MS - MIN_RECONNECT_DELAY_MS);
                                    System.out.println("다음 재연결까지 " + delay + "ms 대기...");
                                    try {
                                        Thread.sleep(delay);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                    chat.reconnectAsync();
                                } else {
                                    System.out.println("최대 재연결 횟수 도달, 재연결 중단");
                                }
                            }
                        })
                        .build();
        chat.connectBlocking();
        Thread.sleep(TEST_DURATION_MS);
        chat.closeBlocking();
    }
}
