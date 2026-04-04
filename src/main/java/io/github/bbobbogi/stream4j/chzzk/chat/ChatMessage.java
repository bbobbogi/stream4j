package io.github.bbobbogi.stream4j.chzzk.chat;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * Class representing a chat message.
 *
 * @since 1.0.0
 */
public class ChatMessage {
    /**
     * Creates a {@link ChatMessage}.
     */
    public ChatMessage() {
    }

    /**
     * Enum representing operating system type.
     *
     * @since 1.0.0
     */
    public enum OsType
    {
        /** PC */
        PC,
        /** Android */
        AOS,
        /** iOS */
        IOS
    }

    /**
     * Class containing extra message information.
     */
    public static class Extras {
        /**
         * Creates {@link Extras}.
         */
        Extras() {
        }

        // todo: emoji parsing implementation
        //public Emoji[] emojis;

        String donationType;
        String osType;

        int payAmount = -1;

        // Subscription
        int month = 0;
        String tierName = "";

        // Common donation fields (CHAT/DONATION 형태에서 extras 안에 포함)
        public String nickname;
        String userIdHash;
        public boolean verifiedMark;
        boolean isAnonymous;
        String anonymousToken;
        String donationId;
        String payType;
        int continuousDonationDays;

        // Mission
        int durationTime;
        String missionDonationId;
        String missionDonationType;
        String missionCreatedTime;
        String missionStartTime;
        String missionEndTime;
        String missionText;
        int totalPayAmount;
        int participationCount;
        String status;
        boolean success;

        // Mission Participation
        String relatedMissionDonationId;

        // Party Donation
        String partyDonationId;
        String partyName;
        int partyNo;

        // System Message
        String description;

        /**
         * Returns donation type.
         *
         * @return donation type string
         */
        public String getDonationType() {
            return donationType;
        }

        /**
         * Returns system message description.
         *
         * @return description string
         */
        public String getDescription() {
            return description;
        }

        /**
         * Returns operating system type.
         *
         * @return operating system type
         */
        public OsType getOsType() {
            if (osType == null) {
                return OsType.PC;
            }
            try {
                return OsType.valueOf(osType);
            } catch (IllegalArgumentException e) {
                return OsType.PC;
            }
        }

        /**
         * Returns donation amount.
         *
         * @return donation amount (-1 if not set)
         */
        public int getPayAmount() {
            return payAmount;
        }

        //public int getSubscriptionMonth() {
        //    return month;
        //}

        //public String getSubscriptionTierName() {
        //    return tierName;
        //}

        @Override
        public String toString() {
            return "Extras{" +
                    "osType='" + osType + '\'' +
                    ", payAmount=" + payAmount + '\'' +
                    ", month=" + month + '\'' +
                    ", tierName='" + tierName +
                    '}';
        }
    }

    /**
     * Class containing user profile information.
     */
    public static class Profile {
        /**
         * Creates {@link Profile}.
         */
        public Profile() {
        }

        public String nickname;
        String profileImageUrl;
        String userRoleCode;
        public boolean verifiedMark;

        ActivityBadge[] activityBadges;
        StreamingProperty streamingProperty;

        /**
         * Class containing streaming properties.
         */
        public static class StreamingProperty {
            /**
             * Creates {@link StreamingProperty}.
             */
            StreamingProperty() {
            }

            Subscription subscription;

            /**
             * Class containing subscription information.
             */
            public static class Subscription {
                /**
                 * Creates {@link Subscription}.
                 */
                Subscription() {
                }

                int accmulativeMonth;
                int tier;
                PartialBadge badge;

                /**
                 * Returns accumulated subscription months.
                 *
                 * @return accumulated subscription months
                 */
                public int getAccmulativeMonth() {
                    return accmulativeMonth;
                }

                /**
                 * Returns subscription tier.
                 *
                 * @return subscription tier
                 */
                public int getTier() {
                    return tier;
                }

                /**
                 * Returns subscription badge.
                 *
                 * @return subscription badge
                 */
                public PartialBadge getBadge() {
                    return badge;
                }

                @Override
                public boolean equals(Object o) {
                    if (this == o) return true;
                    if (o == null || getClass() != o.getClass()) return false;
                    Subscription that = (Subscription) o;
                    return accmulativeMonth == that.accmulativeMonth && tier == that.tier && Objects.equals(badge, that.badge);
                }

                @Override
                public int hashCode() {
                    return Objects.hash(accmulativeMonth, tier, badge);
                }

                @Override
                public String toString() {
                    return "Subscription{" +
                            "accmulativeMonth=" + accmulativeMonth +
                            ", tier=" + tier +
                            ", badge=" + badge +
                            '}';
                }
            }

            @Override
            public String toString() {
                return "StreamingProperty{" +
                        "subscription=" + subscription +
                        '}';
            }
        }

        /**
         * Class containing partial badge information.
         */
        public static class PartialBadge {
            /**
             * Creates {@link PartialBadge}.
             */
            PartialBadge() {
            }

            String imageUrl;

            /**
             * Returns badge image URL.
             *
             * @return image URL
             */
            public String getImageUrl() {
                return imageUrl;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                PartialBadge that = (PartialBadge) o;
                return Objects.equals(imageUrl, that.imageUrl);
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(imageUrl);
            }

            @Override
            public String toString() {
                return "PartialBadge{" +
                        "imageUrl='" + imageUrl + '\'' +
                        '}';
            }
        }

        /**
         * Class containing activity badge information.
         */
        public static class ActivityBadge extends PartialBadge {
            /**
             * Creates {@link ActivityBadge}.
             */
            ActivityBadge() {
            }

            int badgeNo;
            String badgeId;
            boolean activated;

            /**
             * Returns badge number.
             *
             * @return badge number
             */
            public int getBadgeNo() {
                return badgeNo;
            }

            /**
             * Returns badge ID.
             *
             * @return badge ID
             */
            public String getBadgeId() {
                return badgeId;
            }

            /**
             * Returns whether badge is activated.
             *
             * @return activation status
             */
            public boolean isActivated() {
                return activated;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                ActivityBadge that = (ActivityBadge) o;
                return badgeNo == that.badgeNo && activated == that.activated && Objects.equals(badgeId, that.badgeId) && Objects.equals(imageUrl, that.imageUrl);
            }

            @Override
            public int hashCode() {
                return Objects.hash(badgeNo, badgeId, imageUrl, activated);
            }

            @Override
            public String toString() {
                return "ActivityBadge{" +
                        "badgeNo=" + badgeNo +
                        ", badgeId='" + badgeId + '\'' +
                        ", imageUrl='" + imageUrl + '\'' +
                        ", activated=" + activated +
                        '}';
            }
        }

        /**
         * Returns nickname.
         *
         * @return nickname
         */
        public String getNickname() {
            return nickname;
        }

        /**
         * Returns profile image URL.
         *
         * @return profile image URL
         */
        public String getProfileImageUrl() {
            return profileImageUrl;
        }

        /**
         * Returns user role code.
         *
         * @return user role code
         */
        public String getUserRoleCode() {
            return userRoleCode;
        }

        /**
         * Returns verified mark status.
         *
         * @return verified mark status
         */
        public boolean isVerifiedMark() {
            return verifiedMark;
        }

        /**
         * Returns activity badge array.
         *
         * @return activity badge array
         */
        public ActivityBadge[] getActivityBadges() {
            return activityBadges;
        }

        /**
         * Returns subscription information.
         *
         * @return subscription information (null if unavailable)
         */
        @Nullable
        public StreamingProperty.Subscription getSubscription() {
            return streamingProperty.subscription;
        }

        /**
         * Returns whether user has a subscription.
         *
         * @return subscription status
         */
        public boolean hasSubscription() {
            return streamingProperty.subscription != null;
        }

        @Override
        public String toString() {
            return "Profile{" +
                    "nickname='" + nickname + '\'' +
                    ", profileImageUrl='" + profileImageUrl + '\'' +
                    ", userRoleCode='" + userRoleCode + '\'' +
                    ", verifiedMark=" + verifiedMark +
                    ", activityBadges=" + Arrays.toString(activityBadges) +
                    ", streamingProperty=" + streamingProperty +
                    '}';
        }
    }

    public int msgTypeCode = 0;
    public String userIdHash;
    public String content;
    Date messageTime;
    public Date createTime;
    public String msgStatusType;
    int memberCount;
    public Extras extras = new Extras();
    public Profile profile = new Profile();

    public String rawJson;

    /**
     * Returns raw JSON string.
     *
     * @return raw JSON string
     */
    public String getRawJson() { return rawJson; }

    /**
     * Returns chat type code.
     *
     * @return chat type code
     */
    public int getChatTypeCode() {
        return msgTypeCode;
    }

    /**
     * Returns user ID hash.
     *
     * @return user ID hash
     */
    public String getUserIdHash() {
        return userIdHash;
    }

    /**
     * Returns user ID hash.
     *
     * @return user ID hash
     * @deprecated Use {@link #getUserIdHash()} instead
     */
    @Deprecated
    public String getUserId() {
        return userIdHash;
    }

    /**
     * Returns message content.
     *
     * @return message content
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns message time.
     *
     * @return message time
     */
    public Date getMessageTime() {
        return messageTime;
    }

    /**
     * Returns creation time.
     *
     * @return creation time
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * Returns extra information.
     *
     * @return extra information
     */
    public Extras getExtras() {
        return extras;
    }

    /**
     * Returns message status type.
     *
     * @return message status type
     */
    public String getMessageStatusType() {
        return msgStatusType;
    }

    /**
     * Returns member count.
     *
     * @return member count
     */
    public int getMemberCount() {
        return memberCount;
    }

    /**
     * Returns whether this is a blind message.
     *
     * @return blind status
     */
    public boolean isBlind() {
        return "BLIND".equals(msgStatusType);
    }

    /**
     * Returns whether this is a hidden message.
     *
     * @return hidden status
     */
    public boolean isHidden() {
        return "HIDDEN".equals(msgStatusType);
    }

    /**
     * Returns whether this is a system message.
     *
     * @return system message status
     */
    public boolean isSystemMessage() {
        return "SYSTEM_MESSAGE".equals(userIdHash);
    }

    /**
     * Returns whether this is an open message.
     *
     * @return open message status
     */
    public boolean isOpenMessage() {
        return "@OPEN".equals(userIdHash);
    }

    /**
     * Returns whether this is a test message.
     *
     * @return test message status
     */
    public boolean isTestMessage() {
        return "@TEST".equals(userIdHash);
    }

    /**
     * Returns whether this is a user message.
     *
     * @return user message status
     */
    public boolean isUserMessage() {
        return !isBlind() && !isHidden() && !isSystemMessage() && !isOpenMessage() && !isTestMessage();
    }

    /**
     * Returns profile of sender of the message.
     * @return nullable {@link Profile}
     */
    @Nullable
    public Profile getProfile() {
        return profile;
    }

    /**
     * Returns whether profile exists.
     *
     * @return profile existence status
     */
    public boolean hasProfile() {
        return profile != null;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "userIdHash='" + userIdHash + '\'' +
                ", msgTypeCode='" + msgTypeCode + '\'' +
                ", content='" + content + '\'' +
                ", createTime=" + createTime +
                ", extras=" + extras +
                ", profile=" + profile +
                '}';
    }
}
