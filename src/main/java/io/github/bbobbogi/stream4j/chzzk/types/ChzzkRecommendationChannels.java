package io.github.bbobbogi.stream4j.chzzk.types;

import java.util.Arrays;

/**
 * 추천 채널 목록을 나타내는 클래스입니다.
 */
public class ChzzkRecommendationChannels {
    private ChzzkRecommendationChannel[] recommendationChannels;

    /**
     * ChzzkRecommendationChannels를 생성합니다.
     */
    ChzzkRecommendationChannels() {
    }

    /**
     * 추천 채널 배열을 반환합니다.
     *
     * @return 추천 채널 배열
     */
    public ChzzkRecommendationChannel[] getChannels() {
        return recommendationChannels;
    }

    @Override
    public String toString() {
        return "ChzzkRecommendationChannels{" +
                "recommendationChannels=" + Arrays.toString(recommendationChannels) +
                '}';
    }
}
