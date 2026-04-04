package io.github.bbobbogi.stream4j.chzzk.types;

import io.github.bbobbogi.stream4j.common.LiveInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * 라이브 상태 정보를 나타내는 클래스입니다.
 */
public class ChzzkLiveInfo implements LiveInfo {

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
