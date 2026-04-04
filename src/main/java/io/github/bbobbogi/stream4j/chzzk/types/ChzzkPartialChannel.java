package io.github.bbobbogi.stream4j.chzzk.types;

import org.jetbrains.annotations.Nullable;
import io.github.bbobbogi.stream4j.chzzk.Chzzk;
import io.github.bbobbogi.stream4j.chzzk.exception.NotExistsException;

import java.io.IOException;

/**
 * Class representing partial Chzzk channel information.
 *
 * @since 1.0.0
 */
public class ChzzkPartialChannel {
    private String channelId;
    private String channelName;
    private String channelImageUrl;
    private boolean verifiedMark;
    private ChzzkChannelPersonalData personalData;
    private ChzzkChannelEmotePackData emotePackData;

    ChzzkPartialChannel() {}

    /**
     * Get this channel's {@link ChzzkChannelRules}.
     *
     * @param chzzk {@link Chzzk} instance
     * @return {@link ChzzkChannelRules} of the channel
     * @throws IOException if the request to API failed
     * @throws NotExistsException if the channel doesn't exists or the rules of the channel doesn't available
     */
    public ChzzkChannelRules getRules(Chzzk chzzk) throws IOException, NotExistsException {
        return chzzk.getChannelChatRules(channelId);
    }

    /**
     * Get the channel's id.
     *
     * @return channel ID
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Get the name of the channel.
     *
     * @return channel name
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * Get url of the channel's image.
     *
     * @return channel image URL (null if unavailable)
     */
    @Nullable
    public String getChannelImageUrl() {
        return channelImageUrl;
    }

    /**
     * Get is the channel verified.
     *
     * @return whether the channel has a verified mark
     */
    public boolean isVerifiedMark() {
        return verifiedMark;
    }

    /**
     * Get personal data of logged user about the channel.
     * If not logged in, returns null.
     *
     * @return personal data (null if not logged in)
     */
    @Nullable
    public ChzzkChannelPersonalData getPersonalData() {
        return personalData;
    }

    /**
     * Get the emoticon pack data of the channel.
     *
     * @return emote pack data (null if unavailable)
     */
    @Nullable
    public ChzzkChannelEmotePackData getEmotePackData() {
        return emotePackData;
    }
    @Override
    public String toString() {
        return "ChzzkPartialChannel{" +
                "channelId='" + channelId + '\'' +
                ", channelName='" + channelName + '\'' +
                ", channelImageUrl='" + channelImageUrl + '\'' +
                ", verifiedMark=" + verifiedMark +
                ", personalData=" + personalData +
                '}';
    }
}
