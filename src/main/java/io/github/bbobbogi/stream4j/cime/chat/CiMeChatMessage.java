package io.github.bbobbogi.stream4j.cime.chat;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.List;

/**
 * Chat message model for CiMe.
 *
 * @since 1.0.0
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
     * User metadata in a CiMe chat message.
     *
     * @since 1.0.0
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
         * Returns the user ID.
         *
         * @return user ID
         */
        public String getId() {
            return id;
        }

        /**
         * Returns channel information for the user.
         *
         * @return channel information, or {@code null}
         */
        @Nullable
        public CiMeChannel getChannel() {
            return channel;
        }

        /**
         * Returns the user's nickname.
         *
         * @return nickname, or {@code null} when channel data is missing
         */
        @Nullable
        public String getNickname() {
            return channel != null ? channel.getName() : null;
        }

        /**
         * Returns the display color code.
         *
         * @return color code
         */
        public String getColorCode() {
            return colorCode;
        }

        /**
         * Returns user badges.
         *
         * @return list of badges, or {@code null}
         */
        @Nullable
        public List<CiMeBadge> getBadges() {
            return badges;
        }

        /**
         * Returns the donation streak count.
         *
         * @return donation streak count
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
     * Channel metadata embedded in a CiMe chat user object.
     *
     * @since 1.0.0
     */
    public static class CiMeChannel {
        CiMeChannel() {
        }

        String id;

        @SerializedName("na")
        String name;

        /**
         * Returns the channel ID.
         *
         * @return channel ID
         */
        public String getId() {
            return id;
        }

        /**
         * Returns the channel display name.
         *
         * @return channel name
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
     * Badge metadata embedded in a CiMe chat user object.
     *
     * @since 1.0.0
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
         * Returns the badge ID.
         *
         * @return badge ID
         */
        public String getId() {
            return id;
        }

        /**
         * Returns the badge name.
         *
         * @return badge name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the badge description.
         *
         * @return badge description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Returns the badge image URL.
         *
         * @return image URL, or {@code null}
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
     * Returns the message ID.
     *
     * @return message ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the message type.
     *
     * @return message type (for example, {@code MESSAGE})
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the message content.
     *
     * @return message content
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the send time.
     *
     * @return send time, or {@code null}
     */
    @Nullable
    public Date getSendTime() {
        return sendTime;
    }

    /**
     * Returns the raw JSON payload.
     *
     * @return raw JSON string
     */
    public String getRawJson() {
        return rawJson;
    }

    /**
     * Returns the sender user ID.
     *
     * @return sender user ID
     */
    public String getSenderUserId() {
        return senderUserId;
    }

    /**
     * Returns sender user metadata.
     *
     * @return user metadata, or {@code null}
     */
    @Nullable
    public CiMeUser getUser() {
        return user;
    }

    /**
     * Returns whether user metadata is available.
     *
     * @return {@code true} if user metadata exists
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
