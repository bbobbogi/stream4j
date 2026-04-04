package io.github.bbobbogi.stream4j.cime.types;

import io.github.bbobbogi.stream4j.common.LiveInfo;

/**
 * Live metadata for a CiMe broadcast.
 *
 * @since 1.0.0
 */
public class CiMeLiveInfo implements LiveInfo {
    String title;
    int viewerCount;
    boolean clipActive;
    boolean isAdult;
    boolean canSubscription;
    boolean canChatDonation;
    boolean canMissionDonation;
    boolean canVideoDonation;
    boolean showSponsorRank;
    String serverDate;
    boolean canWatchUhd;
    String urlUhd;

    /**
     * Returns whether the channel is live.
     *
     * @return always {@code true} for this live-info response
     */
    @Override
    public boolean isLive() {
        return true;
    }

    /**
     * Returns the broadcast title.
     *
     * @return broadcast title
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Returns the current viewer count.
     *
     * @return viewer count
     */
    public int getViewerCount() {
        return viewerCount;
    }

    /**
     * Returns whether clipping is enabled.
     *
     * @return {@code true} if clipping is enabled
     */
    public boolean isClipActive() {
        return clipActive;
    }

    /**
     * Returns whether the broadcast is marked as adults only.
     *
     * @return {@code true} if adults-only
     */
    public boolean isAdult() {
        return isAdult;
    }

    /**
     * Returns whether subscriptions are available.
     *
     * @return {@code true} if subscription is available
     */
    public boolean canSubscription() {
        return canSubscription;
    }

    /**
     * Returns whether chat donations are available.
     *
     * @return {@code true} if chat donation is available
     */
    public boolean canChatDonation() {
        return canChatDonation;
    }

    /**
     * Returns whether mission donations are available.
     *
     * @return {@code true} if mission donation is available
     */
    public boolean canMissionDonation() {
        return canMissionDonation;
    }

    /**
     * Returns whether video donations are available.
     *
     * @return {@code true} if video donation is available
     */
    public boolean canVideoDonation() {
        return canVideoDonation;
    }

    /**
     * Returns whether sponsor ranking is shown.
     *
     * @return {@code true} if sponsor rank is visible
     */
    public boolean isShowSponsorRank() {
        return showSponsorRank;
    }

    /**
     * Returns server date information from the API.
     *
     * @return server date string
     */
    public String getServerDate() {
        return serverDate;
    }

    /**
     * Returns whether UHD playback is available.
     *
     * @return {@code true} if UHD is available
     */
    public boolean canWatchUhd() {
        return canWatchUhd;
    }

    /**
     * Returns the UHD stream URL when available.
     *
     * @return UHD URL
     */
    public String getUrlUhd() {
        return urlUhd;
    }

    @Override
    public String toString() {
        return "CiMeLiveInfo{" +
                "title='" + title + '\'' +
                ", viewerCount=" + viewerCount +
                ", clipActive=" + clipActive +
                ", isAdult=" + isAdult +
                ", canSubscription=" + canSubscription +
                ", canChatDonation=" + canChatDonation +
                ", canMissionDonation=" + canMissionDonation +
                ", canVideoDonation=" + canVideoDonation +
                ", showSponsorRank=" + showSponsorRank +
                ", serverDate='" + serverDate + '\'' +
                ", canWatchUhd=" + canWatchUhd +
                ", urlUhd='" + urlUhd + '\'' +
                '}';
    }
}
