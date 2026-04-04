import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bbobbogi.stream4j.util.SharedHttpClient;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class YouTubeTestBase {
    private static final String[] YOUTUBE_SEARCH_QUERIES = {
            "게임", "버츄얼", "방송", "여캠", "남캠", "채팅"
    };

    /**
     * YouTube 라이브 채널 목록을 검색하여 반환합니다.
     *
     * @param maxChannels 최대 채널 수
     * @return [videoId, channelName] 배열의 리스트
     */
    protected List<String[]> findLiveChannels(int maxChannels) {
        List<String[]> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String query : YOUTUBE_SEARCH_QUERIES) {
            if (results.size() >= maxChannels) break;
            try {
                JsonObject context = new JsonObject();
                JsonObject client = new JsonObject();
                client.addProperty("clientName", "WEB");
                client.addProperty("clientVersion", "2.20260101.00.00");
                client.addProperty("hl", "ko");
                client.addProperty("gl", "KR");
                context.add("client", client);

                JsonObject body = new JsonObject();
                body.add("context", context);
                body.addProperty("query", query);
                body.addProperty("params", "EgJAAQ==");

                Request request = new Request.Builder()
                        .url("https://www.youtube.com/youtubei/v1/search?prettyPrint=false")
                        .header("Content-Type", "application/json")
                        .post(RequestBody.create(body.toString(), MediaType.get("application/json")))
                        .build();

                try (Response response = SharedHttpClient.get().newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) continue;
                    String json = response.body().string();
                    JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                    parseVideoRenderers(root, seen, results, maxChannels);
                }
            } catch (Exception e) {
                System.out.println("[YouTube] 검색 실패 (" + query + "): " + e.getMessage());
            }
        }
        return results;
    }

    private void parseVideoRenderers(JsonElement element, Set<String> seen, List<String[]> results, int maxChannels) {
        if (results.size() >= maxChannels) return;
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("videoRenderer")) {
                JsonObject vr = obj.getAsJsonObject("videoRenderer");
                String videoId = vr.has("videoId") ? vr.get("videoId").getAsString() : null;
                if (videoId != null && videoId.length() == 11 && seen.add(videoId)) {
                    String channelName = "";
                    try {
                        channelName = vr.getAsJsonObject("ownerText")
                                .getAsJsonArray("runs").get(0).getAsJsonObject()
                                .get("text").getAsString();
                    } catch (Exception ignored) {}
                    results.add(new String[]{videoId, channelName});
                }
            }
            for (var entry : obj.entrySet()) {
                parseVideoRenderers(entry.getValue(), seen, results, maxChannels);
            }
        } else if (element.isJsonArray()) {
            for (JsonElement item : element.getAsJsonArray()) {
                parseVideoRenderers(item, seen, results, maxChannels);
            }
        }
    }
}
