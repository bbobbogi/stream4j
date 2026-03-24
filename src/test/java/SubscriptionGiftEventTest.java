import com.bbobbogi.stream4j.chzzk.chat.SubscriptionGiftEvent;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionGiftEventTest {

    private final Gson gson = new Gson();

    @Test
    void failedUsers_emptyString() {
        String json = "{\"failedUsers\":\"\"}";
        SubscriptionGiftEvent event = gson.fromJson(json, SubscriptionGiftEvent.class);
        List<String> result = event.getFailedUsers();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void failedUsers_emptyArray() {
        String json = "{\"failedUsers\":[]}";
        SubscriptionGiftEvent event = gson.fromJson(json, SubscriptionGiftEvent.class);
        List<String> result = event.getFailedUsers();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void failedUsers_arrayWithValues() {
        String json = "{\"failedUsers\":[\"user1\",\"user2\"]}";
        SubscriptionGiftEvent event = gson.fromJson(json, SubscriptionGiftEvent.class);
        List<String> result = event.getFailedUsers();
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0));
        assertEquals("user2", result.get(1));
    }

    @Test
    void failedUsers_null() {
        String json = "{\"failedUsers\":null}";
        SubscriptionGiftEvent event = gson.fromJson(json, SubscriptionGiftEvent.class);
        List<String> result = event.getFailedUsers();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void failedUsers_missing() {
        String json = "{}";
        SubscriptionGiftEvent event = gson.fromJson(json, SubscriptionGiftEvent.class);
        List<String> result = event.getFailedUsers();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
