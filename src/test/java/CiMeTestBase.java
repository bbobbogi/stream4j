import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import xyz.r2turntrue.chzzk4j.cime.CiMeChat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CiMeTestBase {
    protected static final String CI_ME_API_URL = "https://ci.me/api/app";
    protected final OkHttpClient httpClient = CiMeChat.getSharedHttpClient();
    protected boolean isDebug = true;

    /**
     * ci.me 인기 라이브 목록에서 현재 라이브 중인 채널 슬러그를 찾아 반환합니다.
     *
     * @return 라이브 중인 채널 슬러그 (Optional)
     */
    protected Optional<String> findLiveChannelSlug() {
        try {
            Request request = new Request.Builder()
                    .url(CI_ME_API_URL + "/lives?sort=POPULAR")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.out.println("[CiMe] 라이브 목록 조회 실패: HTTP " + response.code());
                    return Optional.empty();
                }

                ResponseBody body = response.body();
                if (body == null) return Optional.empty();

                String bodyString = body.string();
                JsonObject json = JsonParser.parseString(bodyString).getAsJsonObject();
                int code = json.get("code").getAsInt();
                if (code != 200) return Optional.empty();

                JsonObject data = json.getAsJsonObject("data");
                JsonArray sections = data.getAsJsonArray("sections");

                for (JsonElement sectionElement : sections) {
                    JsonObject section = sectionElement.getAsJsonObject();
                    if (!"LIVE".equals(section.get("type").getAsString())) continue;

                    JsonArray items = section.getAsJsonArray("items");
                    if (items == null || items.isEmpty()) continue;

                    for (JsonElement itemElement : items) {
                        try {
                            JsonObject item = itemElement.getAsJsonObject();
                            if (!item.has("state") || !"ACTIVE".equals(item.get("state").getAsString())) continue;
                            if (item.has("isAdult") && item.get("isAdult").getAsBoolean()) continue;

                            JsonObject channel = item.getAsJsonObject("channel");
                            String slug = channel.get("slug").getAsString();
                            String name = channel.get("name").getAsString();

                            System.out.println("[CiMe] 라이브 채널 발견: " + name + " (" + slug + ")");
                            return Optional.of(slug);
                        } catch (Exception e) {
                            if (isDebug) System.out.println("[CiMe] 아이템 파싱 스킵: " + e.getMessage());
                        }
                    }
                }
            }

            System.out.println("[CiMe] 라이브 중인 채널을 찾지 못했습니다.");
            return Optional.empty();
        } catch (Exception e) {
            System.out.println("[CiMe] 라이브 목록 조회 실패: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * ci.me 인기 라이브 목록에서 라이브 중인 채널 목록을 반환합니다.
     *
     * @param maxChannels 최대 채널 수
     * @return [slug, name] 배열의 리스트
     */
    protected List<String[]> findLiveChannels(int maxChannels) {
        List<String[]> channels = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        String[] sortTypes = {"POPULAR", "RECENT"};

        for (String sort : sortTypes) {
            try {
                Request request = new Request.Builder()
                        .url(CI_ME_API_URL + "/lives?sort=" + sort)
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) continue;

                    ResponseBody body = response.body();
                    if (body == null) continue;

                    String bodyString = body.string();
                    JsonObject json = JsonParser.parseString(bodyString).getAsJsonObject();
                    int code = json.get("code").getAsInt();
                    if (code != 200) continue;

                    JsonObject data = json.getAsJsonObject("data");
                    JsonArray sections = data.getAsJsonArray("sections");

                    for (JsonElement sectionElement : sections) {
                        JsonObject section = sectionElement.getAsJsonObject();
                        if (!"LIVE".equals(section.get("type").getAsString())) continue;

                        JsonArray items = section.getAsJsonArray("items");
                        if (items == null) continue;

                        for (JsonElement itemElement : items) {
                            try {
                                JsonObject item = itemElement.getAsJsonObject();
                                if (!item.has("state") || !"ACTIVE".equals(item.get("state").getAsString())) continue;
                                if (item.has("isAdult") && item.get("isAdult").getAsBoolean()) continue;

                                JsonObject channel = item.getAsJsonObject("channel");
                                String slug = channel.get("slug").getAsString();
                                String name = channel.get("name").getAsString();
                                if (seen.add(slug)) {
                                    channels.add(new String[]{slug, name});
                                }

                                if (channels.size() >= maxChannels) return channels;
                            } catch (Exception e) {
                                if (isDebug) System.out.println("[CiMe] 아이템 파싱 스킵: " + e.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("[CiMe] 라이브 목록 조회 실패 (" + sort + "): " + e.getMessage());
            }

            if (channels.size() >= maxChannels) break;
        }
        return channels;
    }
}
