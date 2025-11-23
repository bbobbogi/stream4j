import xyz.r2turntrue.chzzk4j.Chzzk;
import xyz.r2turntrue.chzzk4j.ChzzkBuilder;
import xyz.r2turntrue.chzzk4j.types.channel.ChzzkPartialChannel;
import xyz.r2turntrue.chzzk4j.types.channel.live.ChzzkLiveStatus;
import xyz.r2turntrue.chzzk4j.types.channel.recommendation.ChzzkRecommendationChannel;
import xyz.r2turntrue.chzzk4j.types.channel.recommendation.ChzzkRecommendationChannels;

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
     * 추천 채널 목록에서 현재 라이브 중인 채널을 찾아 반환합니다.
     * 라이브 중인 채널이 없으면 빈 Optional을 반환합니다.
     *
     * @return 라이브 중인 채널 ID (Optional)
     */
    protected Optional<String> findLiveChannelId() {
        try {
            ChzzkRecommendationChannels recommendationChannels = chzzk.getRecommendationChannels();
            ChzzkRecommendationChannel[] channels = recommendationChannels.getChannels();

            if (channels == null || channels.length == 0) {
                System.out.println("추천 채널 목록이 비어있습니다.");
                return Optional.empty();
            }

            for (ChzzkRecommendationChannel recommendationChannel : channels) {
                ChzzkPartialChannel channel = recommendationChannel.getChannel();
                if (channel == null || channel.getChannelId() == null) {
                    continue;
                }

                String channelId = channel.getChannelId();
                try {
                    ChzzkLiveStatus liveStatus = chzzk.getLiveStatus(channelId);
                    if (liveStatus.isOnline()) {
                        System.out.println("라이브 채널 발견: " + channel.getChannelName() + " (" + channelId + ")");
                        return Optional.of(channelId);
                    }
                } catch (Exception e) {
                    // 개별 채널 조회 실패 시 다음 채널로 계속
                    System.out.println("채널 상태 조회 실패: " + channelId);
                }
            }

            System.out.println("라이브 중인 채널을 찾지 못했습니다.");
            return Optional.empty();
        } catch (IOException e) {
            System.out.println("추천 채널 목록 조회 실패: " + e.getMessage());
            return Optional.empty();
        }
    }
}
