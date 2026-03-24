import com.bbobbogi.stream4j.soop.*;
import com.bbobbogi.stream4j.soop.chat.SOOPDonationMessage;
import com.bbobbogi.stream4j.util.SharedHttpClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class SOOPChatTest extends ChzzkTestBase {

    private static final long TEST_DURATION_MS = 30_000;

    private Optional<String[]> findLiveStreamer() {
        try {
            Request request = new Request.Builder()
                    .url("https://live.sooplive.co.kr/api/main_broad_list_api.php?selectType=action&selectValue=all&orderType=view_cnt&pageNo=1&lang=ko_KR&_=" + System.currentTimeMillis())
                    .get()
                    .build();
            try (Response response = SharedHttpClient.get().newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) return Optional.empty();
                JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
                JsonArray broads = json.has("broad") ? json.getAsJsonArray("broad") : null;
                if (broads == null || broads.isEmpty()) return Optional.empty();
                JsonObject first = broads.get(0).getAsJsonObject();
                String userId = first.get("user_id").getAsString();
                String userNick = first.has("user_nick") ? first.get("user_nick").getAsString() : userId;
                return Optional.of(new String[]{userId, userNick});
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Test
    void testingSOOPChat() throws Exception {
        Optional<String[]> live = findLiveStreamer();
        Assumptions.assumeTrue(live.isPresent(), "라이브 중인 SOOP 채널이 없어 테스트를 건너뜁니다.");

        String streamerId = live.get()[0];
        String nickname = live.get()[1];
        System.out.println("테스트에 사용할 스트리머: " + nickname + " (" + streamerId + ")");

        SOOPChatBuilder builder = new SOOPChatBuilder(streamerId)
                .withDebugMode();

        String soopId = properties.getProperty("SOOP_ID");
        String soopPw = properties.getProperty("SOOP_PW");
        if (soopId != null && !soopId.isEmpty() && soopPw != null && !soopPw.isEmpty()) {
            builder.withCredentials(soopId, soopPw);
        }

        SOOPChat chat = builder
                .withChatListener(new SOOPChatEventListener() {
                    @Override
                    public void onConnect(SOOPChat chat, boolean isReconnecting) {
                        System.out.println("[SOOP] Connected! (reconnecting: " + isReconnecting + ")");
                    }

                    @Override
                    public void onError(Exception ex) {
                        ex.printStackTrace();
                    }

                    @Override
                    public void onChat(String userId, String username, String message) {
                        System.out.println("[SOOP Chat] " + username + ": " + message);
                    }

                    @Override
                    public void onDonation(SOOPChat chat, SOOPDonationMessage msg) {
                        System.out.println("[SOOP Donation] " + msg.getFromUsername() + ": " + msg.getAmount() + "개 " + msg.getType());
                    }

                    @Override
                    public void onSubscribe(SOOPChat chat, String from, String fromUsername, int monthCount, int tier) {
                        System.out.println("[SOOP Subscribe] " + fromUsername + ": " + monthCount + "개월 " + tier + "티어");
                    }

                    @Override
                    public void onConnectionClosed(int code, String reason, boolean remote, boolean tryingToReconnect) {
                        System.out.println("[SOOP] Connection closed: " + reason);
                    }
                })
                .build();

         chat.connect();
         Thread.sleep(TEST_DURATION_MS);
         chat.close();
    }
}
