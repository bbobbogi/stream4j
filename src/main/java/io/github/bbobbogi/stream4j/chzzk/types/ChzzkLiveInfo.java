package io.github.bbobbogi.stream4j.chzzk.types;

import io.github.bbobbogi.stream4j.common.LiveInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Class representing live status information.
 *
 * @since 1.0.0
 */
public class ChzzkLiveInfo implements LiveInfo {

    /** Broadcast title. */
    protected String liveTitle;
    /** Broadcast status. */
    protected String status;
    /** Concurrent viewer count. */
    protected int concurrentUserCount;
    /** Accumulated viewer count. */
    protected int accumulateCount;
    /** Whether paid promotion is enabled. */
    protected boolean paidPromotion;
    /** Whether the stream is adult-only. */
    protected boolean adult;
    /** Whether clip is enabled. */
    protected boolean clipActive;
    /** Chat channel ID. */
    protected String chatChannelId;
    /** Tag list. */
    protected List<String> tags;
    /** Category type. */
    protected String categoryType;
    /** Live category. */
    protected String liveCategory;
    /** Live category value. */
    protected String liveCategoryValue;
    /** Live polling status JSON. */
    protected String livePollingStatusJson;
    /** Fault status. */
    protected Object faultStatus;
    /** User adult status. */
    protected Object userAdultStatus;
    /** Whether chat is enabled. */
    protected boolean chatActive;
    /** Chat-allowed group. */
    protected String chatAvailableGroup;
    /** Chat availability condition. */
    protected String chatAvailableCondition;
    /** Minimum follow time (minutes). */
    protected int minFollowerMinute;
    /** Whether chat donation ranking is exposed. */
    protected boolean chatDonationRankingExposure;

    ChzzkLiveInfo() {
    }

    @Override
    public @NotNull String getTitle() {
        return liveTitle;
    }

    public boolean isOnline() {
        return status.equalsIgnoreCase("open");
    }

    @Override
    public boolean isLive() {
        return isOnline();
    }

    public int getUserCount() {
        return concurrentUserCount;
    }

    public int getAccumulateUserCount() {
        return accumulateCount;
    }

    public boolean hasPaidPromotion() {
        return paidPromotion;
    }

    public boolean isNSFW() {
        return adult;
    }

    public boolean isClipActive() {
        return clipActive;
    }

    public @NotNull String getChatChannelId() {
        return chatChannelId;
    }

    public @NotNull List<String> getTags() {
        return List.copyOf(tags);
    }

    public @NotNull Optional<String> getCategoryType() {
        return Optional.ofNullable(categoryType);
    }

    public @NotNull Optional<String> getLiveCategory() {
        return Optional.ofNullable(liveCategory);
    }

    public @NotNull String getLiveCategoryValue() {
        return liveCategoryValue;
    }

    public boolean isChatActive() {
        return chatActive;
    }

    public @NotNull String getChatAvailableGroup() {
        return chatAvailableGroup;
    }

    public @NotNull String getChatAvailableCondition() {
        return chatAvailableCondition;
    }

    public int getMinFollowerMinute() {
        return minFollowerMinute;
    }

    public boolean isChatDonationRankingExposure() {
        return chatDonationRankingExposure;
    }

    @Override
    public String toString() {
        return "ChzzkLiveInfo{" +
                "liveTitle='" + liveTitle + '\'' +
                ", status='" + status + '\'' +
                ", concurrentUserCount=" + concurrentUserCount +
                ", accumulateCount=" + accumulateCount +
                ", paidPromotion=" + paidPromotion +
                ", adult=" + adult +
                ", clipActive=" + clipActive +
                ", chatChannelId='" + chatChannelId + '\'' +
                ", tags=" + tags +
                ", categoryType='" + categoryType + '\'' +
                ", liveCategory='" + liveCategory + '\'' +
                ", liveCategoryValue='" + liveCategoryValue + '\'' +
                ", livePollingStatusJson='" + livePollingStatusJson + '\'' +
                ", faultStatus=" + faultStatus +
                ", userAdultStatus=" + userAdultStatus +
                ", chatActive=" + chatActive +
                ", chatAvailableGroup='" + chatAvailableGroup + '\'' +
                ", chatAvailableCondition='" + chatAvailableCondition + '\'' +
                ", minFollowerMinute=" + minFollowerMinute +
                ", chatDonationRankingExposure=" + chatDonationRankingExposure +
                '}';
    }
}
