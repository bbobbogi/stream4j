package io.github.bbobbogi.stream4j.chzzk.types;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * 라이브 상세 정보를 나타내는 클래스입니다.
 */
public class ChzzkLiveDetail extends ChzzkLiveInfo {

    private transient final @NotNull DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final transient @NotNull ZoneId zoneId = ZoneId.of("Asia/Seoul");

    private int liveId;
    private String liveImageUrl;
    private String defaultThumbnailImageUrl;
    private String openDate;
    private String closeDate;
    private ChzzkLiveChannel channel;

    /**
     * ChzzkLiveDetail을 생성합니다.
     */
    ChzzkLiveDetail() {
        super();
    }

    /**
     * Get unique ID number of the live stream.
     *
     * @return 라이브 ID
     */
    public int getLiveId() {
        return liveId;
    }

    /**
     * Get URL of the automatically generated thumbnail image.
     *
     * @param resolution Image {@link Resolution}
     * @return 라이브 이미지 URL
     */
    public @NotNull String getLiveImageUrl(@NotNull Resolution resolution) {
        return liveImageUrl.replace("{type}", resolution.getRawAsString());
    }

    /**
     * Get default thumbnail image URL.
     *
     * @return 기본 썸네일 이미지 URL (Optional)
     */
    public @NotNull Optional<String> getDefaultThumbnailImageUrl() {
        return Optional.ofNullable(defaultThumbnailImageUrl);
    }

    /**
     * Get start time of the live stream.
     *
     * @return 방송 시작 시간 (Optional)
     */
    public @NotNull Optional<ZonedDateTime> getOpenDate() {
        if (openDate == null) {
            return Optional.empty();
        }
        ZonedDateTime date = LocalDateTime.parse(openDate, formatter).atZone(zoneId);
        return Optional.of(date);
    }

    /**
     * Get close time of the live stream.
     *
     * @return 방송 종료 시간 (Optional)
     */
    public @NotNull Optional<ZonedDateTime> getCloseDate() {
        if (closeDate == null) {
            return Optional.empty();
        }
        ZonedDateTime date = LocalDateTime.parse(closeDate, formatter).atZone(zoneId);
        return Optional.of(date);
    }

    /**
     * Get live stream channel.
     *
     * @return 라이브 채널 정보
     */
    public @NotNull ChzzkLiveChannel getLiveChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "ChzzkLiveDetailImpl{" +
                "liveId=" + liveId +
                ", liveImageUrl='" + liveImageUrl + '\'' +
                ", defaultThumbnailImageUrl='" + defaultThumbnailImageUrl + '\'' +
                ", openDate='" + openDate + '\'' +
                ", closeDate='" + closeDate + '\'' +
                ", channel=" + channel +
                ", liveTitle='" + liveTitle + '\'' +
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
