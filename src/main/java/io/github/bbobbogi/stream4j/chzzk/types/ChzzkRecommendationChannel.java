package io.github.bbobbogi.stream4j.chzzk.types;


/**
 * 추천 채널 정보를 나타내는 클래스입니다.
 */
public class ChzzkRecommendationChannel {
    private ChzzkPartialChannel channel;

    /**
     * ChzzkRecommendationChannel을 생성합니다.
     */
    ChzzkRecommendationChannel() {
    }

    /**
     * 채널 정보를 반환합니다.
     *
     * @return 채널 정보
     */
    public ChzzkPartialChannel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "ChzzkRecommendationChannel{" +
                "channel=" + channel +
                '}';
    }
}
