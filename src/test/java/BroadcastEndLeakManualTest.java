import io.github.bbobbogi.stream4j.common.DonationPlatform;
import io.github.bbobbogi.stream4j.common.StreamChat;
import io.github.bbobbogi.stream4j.common.StreamChatEventListener;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Real-network check of the broadcast-end leak: CHZZK channels given in the {@code STREAM4J_ENDED_CHZZK}
 * environment variable (comma separated, expected to be offline) are connected through {@link StreamChat},
 * left until the 30 s live check reports the end, then closed. No chat scheduler thread may survive the close.
 */
@Tag("manual")
public class BroadcastEndLeakManualTest {

    @Test
    void closingAfterBroadcastEndLeavesNoChatThread() throws Exception {
        String configured = System.getenv("STREAM4J_ENDED_CHZZK");
        Assumptions.assumeTrue(configured != null && !configured.isBlank(), "STREAM4J_ENDED_CHZZK is not set");
        Set<String> ended = ConcurrentHashMap.newKeySet();
        var builder = StreamChat.builder().withListener(new StreamChatEventListener() {
            @Override
            public void onBroadcastEnd(DonationPlatform platform, String channel) {
                ended.add(channel);
            }
        });
        for (String channel : configured.split(",")) builder = builder.addChzzk(channel.trim());
        StreamChat chat = builder.build();

        chat.connectAllAsync().join();
        Thread.sleep(45_000);
        Assumptions.assumeFalse(ended.isEmpty(), "none of the channels reported a broadcast end");
        System.out.println("ended=" + ended.size() + " chatThreadsBeforeClose=" + chatThreads());

        chat.closeAllAsync().join();
        Thread.sleep(10_000);

        System.out.println("chatThreadsAfterClose=" + chatThreads());
        assertEquals(0, chatThreads(), "every chzzk-chat-ws thread must stop once the client is closed");
    }

    private static long chatThreads() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(thread -> thread.getName().startsWith("chzzk-chat-ws-")).count();
    }
}
