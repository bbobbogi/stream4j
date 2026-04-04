package io.github.bbobbogi.stream4j.chzzk.types;


/**
 * Class representing recommended channel information.
 *
 * @since 1.0.0
 */
public class ChzzkRecommendationChannel {
    private ChzzkPartialChannel channel;

    /**
     * Creates a {@link ChzzkRecommendationChannel}.
     */
    ChzzkRecommendationChannel() {
    }

    /**
     * Returns channel information.
     *
     * @return channel information
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
