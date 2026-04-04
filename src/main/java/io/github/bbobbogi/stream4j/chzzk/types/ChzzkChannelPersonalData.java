package io.github.bbobbogi.stream4j.chzzk.types;

/**
 * Class representing user personal data for a channel.
 *
 * @since 1.0.0
 */
public class ChzzkChannelPersonalData {
    private ChzzkChannelFollowingData following;
    private boolean privateUserBlock;

    private ChzzkChannelPersonalData() {}

    /**
     * Get following status of the logged user about the channel.
     *
     * @return following data
     */
    public ChzzkChannelFollowingData getFollowing() {
        return following;
    }

    /**
     * Returns whether private users are blocked.
     *
     * @return whether private users are blocked
     */
    public boolean isPrivateUserBlock() {
        return privateUserBlock;
    }
}
