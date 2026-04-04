package io.github.bbobbogi.stream4j.chzzk.chat;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * 채팅 메시지를 나타내는 클래스입니다.
 */
public class ChatMessage {
    /**
     * ChatMessage를 생성합니다.
     */
    public ChatMessage() {
    }

    /**
     * 운영 체제 타입을 나타내는 열거형입니다.
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
     * 메시지의 추가 정보를 담는 클래스입니다.
     */
    public static class Extras {
        /**
         * Extras를 생성합니다.
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
         * 후원 타입을 반환합니다.
         *
         * @return 후원 타입 문자열
         */
        public String getDonationType() {
            return donationType;
        }

        /**
         * 시스템 메시지 설명을 반환합니다.
         *
         * @return 설명 문자열
         */
        public String getDescription() {
            return description;
        }

        /**
         * 운영 체제 타입을 반환합니다.
         *
         * @return 운영 체제 타입
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
         * 후원 금액을 반환합니다.
         *
         * @return 후원 금액 (설정되지 않은 경우 -1)
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
     * 사용자 프로필 정보를 담는 클래스입니다.
     */
    public static class Profile {
        /**
         * Profile을 생성합니다.
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
         * 스트리밍 속성을 담는 클래스입니다.
         */
        public static class StreamingProperty {
            /**
             * StreamingProperty를 생성합니다.
             */
            StreamingProperty() {
            }

            Subscription subscription;

            /**
             * 구독 정보를 담는 클래스입니다.
             */
            public static class Subscription {
                /**
                 * Subscription을 생성합니다.
                 */
                Subscription() {
                }

                int accmulativeMonth;
                int tier;
                PartialBadge badge;

                /**
                 * 누적 구독 개월 수를 반환합니다.
                 *
                 * @return 누적 구독 개월 수
                 */
                public int getAccmulativeMonth() {
                    return accmulativeMonth;
                }

                /**
                 * 구독 티어를 반환합니다.
                 *
                 * @return 구독 티어
                 */
                public int getTier() {
                    return tier;
                }

                /**
                 * 구독 배지를 반환합니다.
                 *
                 * @return 구독 배지
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
         * 부분 배지 정보를 담는 클래스입니다.
         */
        public static class PartialBadge {
            /**
             * PartialBadge를 생성합니다.
             */
            PartialBadge() {
            }

            String imageUrl;

            /**
             * 배지 이미지 URL을 반환합니다.
             *
             * @return 이미지 URL
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
         * 활동 배지 정보를 담는 클래스입니다.
         */
        public static class ActivityBadge extends PartialBadge {
            /**
             * ActivityBadge를 생성합니다.
             */
            ActivityBadge() {
            }

            int badgeNo;
            String badgeId;
            boolean activated;

            /**
             * 배지 번호를 반환합니다.
             *
             * @return 배지 번호
             */
            public int getBadgeNo() {
                return badgeNo;
            }

            /**
             * 배지 ID를 반환합니다.
             *
             * @return 배지 ID
             */
            public String getBadgeId() {
                return badgeId;
            }

            /**
             * 배지 활성화 여부를 반환합니다.
             *
             * @return 활성화 여부
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
         * 닉네임을 반환합니다.
         *
         * @return 닉네임
         */
        public String getNickname() {
            return nickname;
        }

        /**
         * 프로필 이미지 URL을 반환합니다.
         *
         * @return 프로필 이미지 URL
         */
        public String getProfileImageUrl() {
            return profileImageUrl;
        }

        /**
         * 사용자 역할 코드를 반환합니다.
         *
         * @return 사용자 역할 코드
         */
        public String getUserRoleCode() {
            return userRoleCode;
        }

        /**
         * 인증 마크 여부를 반환합니다.
         *
         * @return 인증 마크 여부
         */
        public boolean isVerifiedMark() {
            return verifiedMark;
        }

        /**
         * 활동 배지 배열을 반환합니다.
         *
         * @return 활동 배지 배열
         */
        public ActivityBadge[] getActivityBadges() {
            return activityBadges;
        }

        /**
         * 구독 정보를 반환합니다.
         *
         * @return 구독 정보 (없는 경우 null)
         */
        @Nullable
        public StreamingProperty.Subscription getSubscription() {
            return streamingProperty.subscription;
        }

        /**
         * 구독 여부를 반환합니다.
         *
         * @return 구독 여부
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
     * 원본 JSON 문자열을 반환합니다.
     *
     * @return 원본 JSON 문자열
     */
    public String getRawJson() { return rawJson; }

    /**
     * 채팅 타입 코드를 반환합니다.
     *
     * @return 채팅 타입 코드
     */
    public int getChatTypeCode() {
        return msgTypeCode;
    }

    /**
     * 사용자 ID 해시를 반환합니다.
     *
     * @return 사용자 ID 해시
     */
    public String getUserIdHash() {
        return userIdHash;
    }

    /**
     * 사용자 ID 해시를 반환합니다.
     *
     * @return 사용자 ID 해시
     * @deprecated Use {@link #getUserIdHash()} instead
     */
    @Deprecated
    public String getUserId() {
        return userIdHash;
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
     * 메시지 시간을 반환합니다.
     *
     * @return 메시지 시간
     */
    public Date getMessageTime() {
        return messageTime;
    }

    /**
     * 생성 시간을 반환합니다.
     *
     * @return 생성 시간
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 추가 정보를 반환합니다.
     *
     * @return 추가 정보
     */
    public Extras getExtras() {
        return extras;
    }

    /**
     * 메시지 상태 타입을 반환합니다.
     *
     * @return 메시지 상태 타입
     */
    public String getMessageStatusType() {
        return msgStatusType;
    }

    /**
     * 멤버 수를 반환합니다.
     *
     * @return 멤버 수
     */
    public int getMemberCount() {
        return memberCount;
    }

    /**
     * 블라인드 메시지 여부를 반환합니다.
     *
     * @return 블라인드 여부
     */
    public boolean isBlind() {
        return "BLIND".equals(msgStatusType);
    }

    /**
     * 숨김 메시지 여부를 반환합니다.
     *
     * @return 숨김 여부
     */
    public boolean isHidden() {
        return "HIDDEN".equals(msgStatusType);
    }

    /**
     * 시스템 메시지 여부를 반환합니다.
     *
     * @return 시스템 메시지 여부
     */
    public boolean isSystemMessage() {
        return "SYSTEM_MESSAGE".equals(userIdHash);
    }

    /**
     * 오픈 메시지 여부를 반환합니다.
     *
     * @return 오픈 메시지 여부
     */
    public boolean isOpenMessage() {
        return "@OPEN".equals(userIdHash);
    }

    /**
     * 테스트 메시지 여부를 반환합니다.
     *
     * @return 테스트 메시지 여부
     */
    public boolean isTestMessage() {
        return "@TEST".equals(userIdHash);
    }

    /**
     * 사용자 메시지 여부를 반환합니다.
     *
     * @return 사용자 메시지 여부
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
     * 프로필 존재 여부를 반환합니다.
     *
     * @return 프로필 존재 여부
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
