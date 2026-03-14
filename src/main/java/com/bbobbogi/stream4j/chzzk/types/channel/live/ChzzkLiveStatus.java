package com.bbobbogi.stream4j.chzzk.types.channel.live;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * 라이브 상태 정보를 나타내는 클래스입니다.
 */
public class ChzzkLiveStatus {

    /** 방송 제목 */
    protected String liveTitle;
    /** 방송 상태 */
    protected String status;
    /** 동시 접속자 수 */
    protected int concurrentUserCount;
    /** 누적 시청자 수 */
    protected int accumulateCount;
    /** 유료 프로모션 여부 */
    protected boolean paidPromotion;
    /** 성인 전용 여부 */
    protected boolean adult;
    /** 클립 활성화 여부 */
    protected boolean clipActive;
    /** 채팅 채널 ID */
    protected String chatChannelId;
    /** 태그 목록 */
    protected List<String> tags;
    /** 카테고리 타입 */
    protected String categoryType;
    /** 라이브 카테고리 */
    protected String liveCategory;
    /** 라이브 카테고리 값 */
    protected String liveCategoryValue;
    /** 라이브 폴링 상태 JSON */
    protected String livePollingStatusJson;
    /** 오류 상태 */
    protected Object faultStatus;
    /** 사용자 성인 상태 */
    protected Object userAdultStatus;
    /** 채팅 활성화 여부 */
    protected boolean chatActive;
    /** 채팅 가능 그룹 */
    protected String chatAvailableGroup;
    /** 채팅 가능 조건 */
    protected String chatAvailableCondition;
    /** 최소 팔로우 시간(분) */
    protected int minFollowerMinute;
    /** 채팅 후원 랭킹 노출 여부 */
    protected boolean chatDonationRankingExposure;

    /**
     * ChzzkLiveStatus를 생성합니다.
     */
    ChzzkLiveStatus() {
    }

    /**
     * Get title of the live stream.
     *
     * @return 방송 제목
     */
    public @NotNull String getTitle() {
        return liveTitle;
    }

    /**
     * Get live stream is opened.
     *
     * @return 방송 중이면 {@code true}, 그렇지 않으면 {@code false}
     */
    public boolean isOnline() {
        return status.equalsIgnoreCase("open");
    }

    /**
     * Get current number of viewers watching the live stream.
     *
     * @return 현재 시청자 수
     */
    public int getUserCount() {
        return concurrentUserCount;
    }

    /**
     * Get cumulative count of viewers.
     *
     * @return 누적 시청자 수
     */
    public int getAccumulateUserCount() {
        return accumulateCount;
    }

    /**
     * Indicates the presence of paid promotions.
     *
     * @return 유료 프로모션 여부
     */
    public boolean hasPaidPromotion() {
        return paidPromotion;
    }

    /**
     * Indicates whether the channel is adult-only.
     *
     * @return 성인 전용 여부
     */
    public boolean isNSFW() {
        return adult;
    }

    /**
     * Indicates whether clips are enabled.
     *
     * @return 클립 활성화 여부
     */
    public boolean isClipActive() {
        return clipActive;
    }

    /**
     * Get unique ID number of the chat room.
     *
     * @return 채팅 채널 ID
     */
    public @NotNull String getChatChannelId() {
        return chatChannelId;
    }

    /**
     * Get tags of the live stream.
     *
     * @return 태그 목록
     */
    public @NotNull List<String> getTags() {
        return List.copyOf(tags);
    }

    /**
     * Get main category of the broadcast.
     * Typically, it returns "GAME" for game broadcasts, "ETC" for others,
     * and "null" if no category is set.
     *
     * @return 카테고리 타입 (Optional)
     */
    public @NotNull Optional<String> getCategoryType() {
        return Optional.ofNullable(categoryType);
    }

    /**
     * Get subcategory of the live stream.
     *
     * @return 라이브 카테고리 (Optional)
     */
    public @NotNull Optional<String> getLiveCategory() {
        return Optional.ofNullable(liveCategory);
    }

    /**
     * Get display name of the subcategory.
     *
     * @return 라이브 카테고리 값
     */
    public @NotNull String getLiveCategoryValue() {
        return liveCategoryValue;
    }

    /**
     * Get chat activation state of the live stream.
     *
     * @return 채팅 활성화 여부
     */
    public boolean isChatActive() {
        return chatActive;
    }

    /**
     * Get group of viewers who are allowed to send chat messages.
     *
     * @return 채팅 가능 그룹
     */
    public @NotNull String getChatAvailableGroup() {
        return chatAvailableGroup;
    }

    /**
     * Get conditions that viewers must meet to be able to send chat messages.
     *
     * @return 채팅 가능 조건
     */
    public @NotNull String getChatAvailableCondition() {
        return chatAvailableCondition;
    }

    /**
     * Get minimum follow time required to send chat messages.
     *
     * @return 최소 팔로우 시간(분)
     */
    public int getMinFollowerMinute() {
        return minFollowerMinute;
    }

    /**
     * Indicates whether the chat donation ranking is displayed.
     *
     * @return 채팅 후원 랭킹 노출 여부
     */
    public boolean isChatDonationRankingExposure() {
        return chatDonationRankingExposure;
    }

    @Override
    public String toString() {
        return "ChzzkLiveStatusImpl{" +
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
