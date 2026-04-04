package io.github.bbobbogi.stream4j.chzzk.types;

import org.jetbrains.annotations.NotNull;

/**
 * Class representing live channel information.
 *
 * @since 1.0.0
 */
public class ChzzkLiveChannel {

    private String channelId;
    private String channelName;
    private String channelImageUrl;
    private boolean verifiedMark;

    /**
     * Creates a {@link ChzzkLiveChannel}.
     */
    ChzzkLiveChannel() {
    }

    /**
     * Get id of the live channel.
     *
     * @return channel ID
     */
    public @NotNull String getId() {
        return channelId;
    }

    /**
     * Get name of the live channel.
     *
     * @return channel name
     */
    public @NotNull String getName() {
        return channelName;
    }

    /**
     * Get image url of the live channel.
     *
     * @return channel image URL
     */
    public @NotNull String getImageUrl() {
        return channelImageUrl;
    }

    /**
     * Get verified mark status of the live channel.
     *
     * @return whether the channel has a verified mark
     */
    public boolean hasVerifiedMark() {
        return verifiedMark;
    }

    @Override
    public String toString() {
        return "ChzzkLiveChannelImpl{" +
                "channelId='" + channelId + '\'' +
                ", channelName='" + channelName + '\'' +
                ", channelImageUrl='" + channelImageUrl + '\'' +
                ", verifiedMark=" + verifiedMark +
                '}';
    }

}
