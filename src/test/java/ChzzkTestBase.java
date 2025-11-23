import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xyz.r2turntrue.chzzk4j.Chzzk;
import xyz.r2turntrue.chzzk4j.ChzzkBuilder;
import xyz.r2turntrue.chzzk4j.util.RawApiUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public class ChzzkTestBase {
    Properties properties = new Properties();
    String currentUserId = "";
    Chzzk chzzk;
    Chzzk loginChzzk;

    public ChzzkTestBase() {
        try {
            properties.load(new FileInputStream("env.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        currentUserId = properties.getProperty("CURRENT_USER_ID");
        chzzk = new ChzzkBuilder()
                .withDebugMode()
                .build();
        loginChzzk = new ChzzkBuilder()
                .withDebugMode()
                .withAuthorization(properties.getProperty("NID_AUT"), properties.getProperty("NID_SES"))
                .build();
    }

    /**
     * 추천 스트리머 목록에서 현재 라이브 중인 채널을 찾아 반환합니다.
     * 라이브 중인 채널이 없으면 빈 Optional을 반환합니다.
     *
     * @return 라이브 중인 채널 ID (Optional)
     */
    protected Optional<String> findLiveChannelId() {
        try {
            JsonElement contentJson = RawApiUtils.getContentJson(
                    chzzk.getHttpClient(),
                    RawApiUtils.httpGetRequest(Chzzk.API_URL + "/service/v1/streamer-partners/recommended").build(),
                    chzzk.isDebug);

            JsonObject content = contentJson.getAsJsonObject();
            JsonArray streamerPartners = content.getAsJsonArray("streamerPartners");

            if (streamerPartners == null || streamerPartners.isEmpty()) {
                System.out.println("추천 스트리머 목록이 비어있습니다.");
                return Optional.empty();
            }

            for (JsonElement element : streamerPartners) {
                JsonObject streamer = element.getAsJsonObject();
                boolean openLive = streamer.get("openLive").getAsBoolean();

                if (openLive) {
                    String channelId = streamer.get("channelId").getAsString();
                    String channelName = streamer.get("channelName").getAsString();
                    System.out.println("라이브 채널 발견: " + channelName + " (" + channelId + ")");
                    return Optional.of(channelId);
                }
            }

            System.out.println("라이브 중인 채널을 찾지 못했습니다.");
            return Optional.empty();
        } catch (IOException e) {
            System.out.println("추천 스트리머 목록 조회 실패: " + e.getMessage());
            return Optional.empty();
        }
    }
}
