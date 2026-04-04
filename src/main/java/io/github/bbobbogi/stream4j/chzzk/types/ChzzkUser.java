package io.github.bbobbogi.stream4j.chzzk.types;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Class representing Chzzk user information.
 *
 * @since 1.0.0
 */
public class ChzzkUser {
    private boolean hasProfile;
    private String userIdHash;
    private String nickname;
    private String profileImageUrl;
    private Object[] penalties; // unknown
    private boolean officialNotiAgree;
    private String officialNotiAgreeUpdatedDate;
    private boolean verifiedMark;
    private boolean loggedIn;

    private ChzzkUser() {}

    /**
     * Get the user has profile.
     *
     * @return whether the user has a profile
     */
    public boolean isHasProfile() {
        return hasProfile;
    }

    /**
     * Get the user's id.
     *
     * @return user ID
     */
    public String getUserId() {
        return userIdHash;
    }

    /**
     * Get the nickname of the user.
     *
     * @return nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Get url of the user's profile image.
     *
     * @return profile image URL
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * Get user agreed to official notification.
     *
     * @return whether official notifications are enabled
     */
    public boolean isOfficialNotiAgree() {
        return officialNotiAgree;
    }

    /**
     * Get when user agreed to official notification in ISO-8601 format.
     *
     * @return official notification agreement update date
     */
    @Nullable
    public String getOfficialNotiAgreeUpdatedDate() {
        return officialNotiAgreeUpdatedDate;
    }

    /**
     * Get user has verified mark.
     *
     * @return whether the user has a verified mark
     */
    public boolean isVerifiedMark() {
        return verifiedMark;
    }

    @Override
    public String toString() {
        return "ChzzkUser{" +
                "hasProfile=" + hasProfile +
                ", userIdHash='" + userIdHash + '\'' +
                ", nickname='" + nickname + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", penalties=" + Arrays.toString(penalties) +
                ", officialNotiAgree=" + officialNotiAgree +
                ", officialNotiAgreeUpdatedDate='" + officialNotiAgreeUpdatedDate + '\'' +
                ", verifiedMark=" + verifiedMark +
                ", loggedIn=" + loggedIn +
                '}';
    }
}
