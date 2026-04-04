package io.github.bbobbogi.stream4j.chzzk.types;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Class representing live detail information.
 *
 * @since 1.0.0
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
     * Creates a {@link ChzzkLiveDetail}.
     */
    ChzzkLiveDetail() {
        super();
    }

    /**
     * Get unique ID number of the live stream.
     *
     * @return live ID
     */
    public int getLiveId() {
        return liveId;
    }

    /**
     * Get URL of the automatically generated thumbnail image.
     *
     * @param resolution Image {@link Resolution}
     * @return live image URL
     */
    public @NotNull String getLiveImageUrl(@NotNull Resolution resolution) {
        return liveImageUrl.replace("{type}", resolution.getRawAsString());
    }

    /**
     * Get default thumbnail image URL.
     *
     * @return default thumbnail image URL (optional)
     */
    public @NotNull Optional<String> getDefaultThumbnailImageUrl() {
        return Optional.ofNullable(defaultThumbnailImageUrl);
    }

    /**
     * Get start time of the live stream.
     *
     * @return broadcast start time (optional)
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
     * @return broadcast end time (optional)
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
     * @return live channel information
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
