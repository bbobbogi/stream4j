import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bbobbogi.stream4j.util.SharedHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SOOPTestBase {

    /**
     * SOOP 인기 라이브 목록에서 라이브 중인 채널 목록을 반환합니다.
     *
     * @param maxChannels 최대 채널 수
     * @return [userId, userNick] 배열의 리스트
     */
    protected List<String[]> findLiveChannels(int maxChannels) {
        List<String[]> channels = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        try {
            Request request = new Request.Builder()
                    .url("https://live.sooplive.co.kr/api/main_broad_list_api.php?selectType=action&selectValue=all&orderType=view_cnt&pageNo=1&lang=ko_KR&_=" + System.currentTimeMillis())
                    .get()
                    .build();
            try (Response response = SharedHttpClient.get().newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) return channels;
                String body = response.body().string();
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                JsonArray broads = json.has("broad") ? json.getAsJsonArray("broad") : null;
                if (broads == null) return channels;
                for (JsonElement el : broads) {
                    try {
                        JsonObject b = el.getAsJsonObject();
                        String userId = b.has("user_id") ? b.get("user_id").getAsString() : null;
                        String userNick = b.has("user_nick") ? b.get("user_nick").getAsString() : userId;
                        if (userId != null && seen.add(userId)) {
                            channels.add(new String[]{userId, userNick});
                        }
                        if (channels.size() >= maxChannels) break;
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            System.out.println("[SOOP] 라이브 목록 조회 실패: " + e.getMessage());
        }
        return channels;
    }
}
