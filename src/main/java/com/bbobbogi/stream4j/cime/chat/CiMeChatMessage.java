package com.bbobbogi.stream4j.cime.chat;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.List;

/**
 * ci.me 채팅 메시지를 나타내는 클래스입니다.
 */
public class CiMeChatMessage {
    public CiMeChatMessage() {
    }

    public String id;
    public String type;
    public String content;
    public Date sendTime;
    public String rawJson;
    public String senderUserId;
    public CiMeUser user;

    /**
     * ci.me 사용자 정보를 담는 클래스입니다.
     * <p>
     * JSON 형식 예시:
     * <pre>{"id":"1014236","ch":{"id":"1014172","na":"인방망령자"},"c":"D","bg":[],"dsc":0}</pre>
     */
    public static class CiMeUser {
        CiMeUser() {
        }

        String id;

        @SerializedName("ch")
        CiMeChannel channel;

        @SerializedName("c")
        String colorCode;

        @SerializedName("bg")
        List<CiMeBadge> badges;

        @SerializedName("dsc")
        int donationStreakCount;

        /**
         * 사용자 ID를 반환합니다.
         *
         * @return 사용자 ID
         */
        public String getId() {
            return id;
        }

        /**
         * 채널 정보를 반환합니다.
         *
         * @return 채널 정보
         */
        @Nullable
        public CiMeChannel getChannel() {
            return channel;
        }

        /**
         * 닉네임을 반환합니다. 채널의 이름입니다.
         *
         * @return 닉네임 (채널이 없을 경우 null)
         */
        @Nullable
        public String getNickname() {
            return channel != null ? channel.getName() : null;
        }

        /**
         * 색상 코드를 반환합니다.
         *
         * @return 색상 코드
         */
        public String getColorCode() {
            return colorCode;
        }

        /**
         * 배지 목록을 반환합니다.
         *
         * @return 배지 목록
         */
        @Nullable
        public List<CiMeBadge> getBadges() {
            return badges;
        }

        /**
         * 후원 연속일수를 반환합니다.
         *
         * @return 후원 연속일수
         */
        public int getDonationStreakCount() {
            return donationStreakCount;
        }

        @Override
        public String toString() {
            return "CiMeUser{" +
                    "id='" + id + '\'' +
                    ", channel=" + channel +
                    ", colorCode='" + colorCode + '\'' +
                    ", badges=" + badges +
                    ", donationStreakCount=" + donationStreakCount +
                    '}';
        }
    }

    /**
     * ci.me 채널 정보를 담는 클래스입니다.
     */
    public static class CiMeChannel {
        CiMeChannel() {
        }

        String id;

        @SerializedName("na")
        String name;

        /**
         * 채널 ID를 반환합니다.
         *
         * @return 채널 ID
         */
        public String getId() {
            return id;
        }

        /**
         * 채널 이름(닉네임)을 반환합니다.
         *
         * @return 채널 이름
         */
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "CiMeChannel{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    /**
     * ci.me 배지 정보를 담는 클래스입니다.
     */
    public static class CiMeBadge {
        CiMeBadge() {
        }

        String id;

        @SerializedName("na")
        String name;

        @SerializedName("de")
        String description;

        @SerializedName("ig")
        String imageUrl;

        /**
         * 배지 ID를 반환합니다.
         *
         * @return 배지 ID
         */
        public String getId() {
            return id;
        }

        /**
         * 배지 이름을 반환합니다.
         *
         * @return 배지 이름
         */
        public String getName() {
            return name;
        }

        /**
         * 배지 설명을 반환합니다.
         *
         * @return 배지 설명
         */
        public String getDescription() {
            return description;
        }

        /**
         * 배지 이미지 URL을 반환합니다.
         *
         * @return 이미지 URL
         */
        @Nullable
        public String getImageUrl() {
            return imageUrl;
        }

        @Override
        public String toString() {
            return "CiMeBadge{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", imageUrl='" + imageUrl + '\'' +
                    '}';
        }
    }

    /**
     * 메시지 ID를 반환합니다.
     *
     * @return 메시지 ID
     */
    public String getId() {
        return id;
    }

    /**
     * 메시지 타입을 반환합니다. (예: "MESSAGE")
     *
     * @return 메시지 타입
     */
    public String getType() {
        return type;
    }

    /**
     * 메시지 내용을 반환합니다.
     *
     * @return 메시지 내용
     */
    public String getContent() {
        return content;
    }

    /**
     * 전송 시간을 반환합니다.
     *
     * @return 전송 시간
     */
    @Nullable
    public Date getSendTime() {
        return sendTime;
    }

    /**
     * 원본 JSON 문자열을 반환합니다.
     *
     * @return 원본 JSON
     */
    public String getRawJson() {
        return rawJson;
    }

    /**
     * 전송자의 사용자 ID를 반환합니다.
     *
     * @return 사용자 ID
     */
    public String getSenderUserId() {
        return senderUserId;
    }

    /**
     * 전송자의 사용자 정보를 반환합니다.
     *
     * @return 사용자 정보
     */
    @Nullable
    public CiMeUser getUser() {
        return user;
    }

    /**
     * 사용자 정보 존재 여부를 반환합니다.
     *
     * @return 사용자 정보 존재 여부
     */
    public boolean hasUser() {
        return user != null;
    }

    @Override
    public String toString() {
        return "CiMeChatMessage{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", sendTime=" + sendTime +
                ", senderUserId='" + senderUserId + '\'' +
                ", user=" + user +
                '}';
    }
}
