import io.github.bbobbogi.stream4j.cime.*;
import io.github.bbobbogi.stream4j.cime.chat.CiMeChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class CiMeDeduplicationTest {

    private CiMeChat chat;
    private AtomicInteger chatCount;
    private AtomicInteger eventCount;
    private Method handleWsMessage;

    @BeforeEach
    void setUp() throws Exception {
        chatCount = new AtomicInteger(0);
        eventCount = new AtomicInteger(0);

        chat = new CiMeChatBuilder("test-dedup")
                .withChatListener(new CiMeChatEventListener() {
                    @Override
                    public void onChat(CiMeChatMessage msg) {
                        chatCount.incrementAndGet();
                    }

                    @Override
                    public void onEvent(String eventName, String rawJson) {
                        eventCount.incrementAndGet();
                    }

                    @Override
                    public void onError(Exception ex) {}
                })
                .build();

        handleWsMessage = CiMeChat.class.getDeclaredMethod("handleWsMessage", String.class);
        handleWsMessage.setAccessible(true);
    }

    private String messageJson(String id, String content) {
        return "{\"Type\":\"MESSAGE\",\"Id\":\"" + id + "\",\"Content\":\"" + content + "\","
                + "\"Sender\":{\"UserId\":\"user1\",\"Attributes\":{\"c\":\"{}\"}}}";
    }

    private String eventJson(String id, String eventName) {
        return "{\"Type\":\"EVENT\",\"Id\":\"" + id + "\",\"EventName\":\"" + eventName + "\","
                + "\"Attributes\":{}}";
    }

    @Test
    void duplicateMessageIsFiltered() throws Exception {
        handleWsMessage.invoke(chat, messageJson("msg-1", "hello"));
        handleWsMessage.invoke(chat, messageJson("msg-1", "hello"));

        assertEquals(1, chatCount.get());
    }

    @Test
    void differentMessagesAreDelivered() throws Exception {
        handleWsMessage.invoke(chat, messageJson("msg-1", "hello"));
        handleWsMessage.invoke(chat, messageJson("msg-2", "world"));

        assertEquals(2, chatCount.get());
    }

    @Test
    void duplicateEventIsFiltered() throws Exception {
        handleWsMessage.invoke(chat, eventJson("evt-1", "SOME_EVENT"));
        handleWsMessage.invoke(chat, eventJson("evt-1", "SOME_EVENT"));

        assertEquals(1, eventCount.get());
    }

    @Test
    void differentEventsAreDelivered() throws Exception {
        handleWsMessage.invoke(chat, eventJson("evt-1", "EVENT_A"));
        handleWsMessage.invoke(chat, eventJson("evt-2", "EVENT_B"));

        assertEquals(2, eventCount.get());
    }

    @Test
    void messageAndEventWithSameIdAreDeduplicated() throws Exception {
        handleWsMessage.invoke(chat, messageJson("shared-1", "hello"));
        handleWsMessage.invoke(chat, eventJson("shared-1", "SOME_EVENT"));

        assertEquals(1, chatCount.get());
        assertEquals(0, eventCount.get());
    }

    // given: 토큰 갱신 시 구/신 두 연결이 겹침
    // when: 동일 ID 메시지가 양쪽에서 도착
    // then: 중복 없이 한 번만 전달
    @Test
    void simulateTokenRefreshOverlap() throws Exception {
        for (int i = 0; i < 10; i++) {
            handleWsMessage.invoke(chat, messageJson("overlap-" + i, "msg-" + i));
        }
        for (int i = 0; i < 10; i++) {
            handleWsMessage.invoke(chat, messageJson("overlap-" + i, "msg-" + i));
        }

        assertEquals(10, chatCount.get());
    }
}
