package io.github.bbobbogi.stream4j.cime.types;

import io.github.bbobbogi.stream4j.common.LiveInfo;

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

    @Override
    public boolean isLive() {
        return true;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public int getViewerCount() {
        return viewerCount;
    }

    public boolean isClipActive() {
        return clipActive;
    }

    public boolean isAdult() {
        return isAdult;
    }

    public boolean canSubscription() {
        return canSubscription;
    }

    public boolean canChatDonation() {
        return canChatDonation;
    }

    public boolean canMissionDonation() {
        return canMissionDonation;
    }

    public boolean canVideoDonation() {
        return canVideoDonation;
    }

    public boolean isShowSponsorRank() {
        return showSponsorRank;
    }

    public String getServerDate() {
        return serverDate;
    }

    public boolean canWatchUhd() {
        return canWatchUhd;
    }

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
