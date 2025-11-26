package xyz.r2turntrue.chzzk4j.types.channel;

/**
 * 채널 규칙 정보를 나타내는 클래스입니다.
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
     * @return 규칙 동의 여부
     */
    public boolean isAgree() {
        return agree;
    }

    /**
     * Get the id of channel.
     *
     * @return 채널 ID
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Get the rule string of channel.
     *
     * @return 채널 규칙 문자열
     */
    public String getRule() {
        return rule;
    }

    /**
     * Get when the rule updated in yyyy-mm-dd HH:mm:ss format.
     *
     * @return 규칙 업데이트 날짜
     */
    public String getUpdatedDate() {
        return updatedDate;
    }

    /**
     * Get the user is agreed to the rules of channel.
     *
     * @return 서비스 동의 여부
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
