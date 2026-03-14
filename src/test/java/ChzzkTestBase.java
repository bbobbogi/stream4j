import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xyz.r2turntrue.chzzk4j.Chzzk;
import xyz.r2turntrue.chzzk4j.ChzzkBuilder;
import xyz.r2turntrue.chzzk4j.util.RawApiUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public class ChzzkTestBase {
    protected Properties properties = new Properties();
    protected String currentUserId = "";
    protected Chzzk chzzk;
    protected Chzzk loginChzzk;
    protected boolean hasCredentials = false;

    public ChzzkTestBase() {
        // env.properties가 있으면 로드, 없으면 빈 Properties 사용
        File envFile = new File("env.properties");
        if (envFile.exists()) {
            try {
                properties.load(new FileInputStream(envFile));
            } catch (IOException e) {
                System.out.println("env.properties 로드 실패: " + e.getMessage());
            }
        } else {
            System.out.println("env.properties 파일이 없습니다. 익명 모드로 실행합니다.");
        }

        currentUserId = properties.getProperty("CURRENT_USER_ID", "");

        // 익명 Chzzk 인스턴스 (항상 생성)
        chzzk = new ChzzkBuilder()
                .build();

        // 로그인된 Chzzk 인스턴스 (인증 정보가 있을 때만 생성)
        String nidAut = properties.getProperty("NID_AUT");
        String nidSes = properties.getProperty("NID_SES");
        if (nidAut != null && !nidAut.isEmpty() && nidSes != null && !nidSes.isEmpty()) {
            loginChzzk = new ChzzkBuilder()
                    .withAuthorization(nidAut, nidSes)
                    .build();
            hasCredentials = true;
        } else {
            // 인증 정보가 없으면 익명 인스턴스로 대체
            loginChzzk = chzzk;
            hasCredentials = false;
            System.out.println("인증 정보가 없습니다. 로그인이 필요한 테스트는 건너뜁니다.");
        }
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
