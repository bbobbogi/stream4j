package xyz.r2turntrue.chzzk4j.types;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * 치지직 사용자 정보를 나타내는 클래스입니다.
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
     * @return 프로필 존재 여부
     */
    public boolean isHasProfile() {
        return hasProfile;
    }

    /**
     * Get the user's id.
     *
     * @return 사용자 ID
     */
    public String getUserId() {
        return userIdHash;
    }

    /**
     * Get the nickname of the user.
     *
     * @return 닉네임
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Get url of the user's profile image.
     *
     * @return 프로필 이미지 URL
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * Get user agreed to official notification.
     *
     * @return 공식 알림 동의 여부
     */
    public boolean isOfficialNotiAgree() {
        return officialNotiAgree;
    }

    /**
     * Get when user agreed to official notification in ISO-8601 format.
     *
     * @return 공식 알림 동의 업데이트 날짜
     */
    @Nullable
    public String getOfficialNotiAgreeUpdatedDate() {
        return officialNotiAgreeUpdatedDate;
    }

    /**
     * Get user has verified mark.
     *
     * @return 인증 마크 여부
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
