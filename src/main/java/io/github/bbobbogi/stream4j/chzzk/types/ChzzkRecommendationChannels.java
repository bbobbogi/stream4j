package io.github.bbobbogi.stream4j.chzzk.types;

import java.util.Arrays;

/**
 * Class representing a list of recommended channels.
 *
 * @since 1.0.0
 */
public class ChzzkRecommendationChannels {
    private ChzzkRecommendationChannel[] recommendationChannels;

    /**
     * Creates a {@link ChzzkRecommendationChannels}.
     */
    ChzzkRecommendationChannels() {
    }

    /**
     * Returns the array of recommended channels.
     *
     * @return array of recommended channels
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
