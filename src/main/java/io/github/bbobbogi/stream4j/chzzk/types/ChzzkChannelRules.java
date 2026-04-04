package io.github.bbobbogi.stream4j.chzzk.types;

/**
 * Class representing channel rule information.
 *
 * @since 1.0.0
 */
public class ChzzkChannelRules {
    private boolean agree;
    private String channelId;
    private String rule;
    private String updatedDate;
    private boolean serviceAgree;

    private ChzzkChannelRules() {}

    /**
     * Get the user is agreed to the rules of channel.
     *
     * @return whether the user agreed to the rules
     */
    public boolean isAgree() {
        return agree;
    }

    /**
     * Get the id of channel.
     *
     * @return channel ID
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Get the rule string of channel.
     *
     * @return channel rule string
     */
    public String getRule() {
        return rule;
    }

    /**
     * Get when the rule updated in yyyy-mm-dd HH:mm:ss format.
     *
     * @return rule update date
     */
    public String getUpdatedDate() {
        return updatedDate;
    }

    /**
     * Get the user is agreed to the rules of channel.
     *
     * @return whether the user agreed to the service terms
     */
    public boolean isServiceAgree() {
        return serviceAgree;
    }

    @Override
    public String toString() {
        return "ChzzkChannelRules{" +
                "agree=" + agree +
                ", channelId='" + channelId + '\'' +
                ", rule='" + rule + '\'' +
                ", updatedDate='" + updatedDate + '\'' +
                ", serviceAgree=" + serviceAgree +
                '}';
    }
}
