package io.github.bbobbogi.stream4j.chzzk.chat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mission donation message.
 *
 * Contains information about the user who starts a mission donation.
 *
 * Sent in two ways:
 * 1. Chat message form via CHAT/DONATION commands (cmd: 93101/93102)
 *    - donationType: "MISSION"
 * 2. Event form via EVENT command (cmd: 93006)
 *    - type: "DONATION_MISSION_IN_PROGRESS" (mission progress update)
 *
 * Event sequence (mission lifecycle):
 * 1. MissionDonationMessage (mission created) - status: PENDING
 *    - sent via EVENT + also via CHAT/DONATION
 * 2. MissionDonationMessage (mission approved/rejected) - status: APPROVED or REJECTED
 *    - sent via EVENT
 * 3. MissionParticipationDonationMessage (participant donation) - repeated per participant (APPROVED only)
 *    - sent via EVENT + also via CHAT/DONATION
 * 4. MissionDonationMessage (mission completed/expired) - status: COMPLETED/EXPIRED
 *    - sent via EVENT
 *
 * missionDonationType: ALONE (solo mission) / GROUP (group mission)
 * status: PENDING / APPROVED / REJECTED / COMPLETED / EXPIRED
 *
 * @since 1.0.0
 */
public class MissionDonationMessage extends DonationMessage {
    private static final DateTimeFormatter MISSION_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Creates a {@link MissionDonationMessage}.
     */
    public MissionDonationMessage() {
    }

    /**
     * Returns mission duration in seconds.
     *
     * @return mission duration in seconds
     */
    public int getDurationTime() {
        return extras.durationTime;
    }

    /**
     * Returns mission donation ID.
     *
     * @return mission donation ID
     */
    public String getMissionDonationId() {
        return extras.missionDonationId;
    }

    /**
     * Returns raw mission donation type string.
     *
     * @return raw mission donation type string
     */
    public String getMissionDonationTypeRaw() {
        return extras.missionDonationType;
    }

    /**
     * Returns mission donation type.
     *
     * @return mission donation type
     */
    public MissionDonationType getMissionDonationType() {
        return MissionDonationType.fromString(extras.missionDonationType);
    }

    /**
     * Returns raw mission creation time string.
     *
     * @return raw mission creation time string
     */
    public String getMissionCreatedTimeRaw() {
        return extras.missionCreatedTime;
    }

    /**
     * Returns mission creation time.
     *
     * @return mission creation time (nullable)
     */
    public LocalDateTime getMissionCreatedTime() {
        return parseMissionTime(extras.missionCreatedTime);
    }

    /**
     * Returns raw mission start time string.
     *
     * @return raw mission start time string
     */
    public String getMissionStartTimeRaw() {
        return extras.missionStartTime;
    }

    /**
     * Returns mission start time.
     *
     * @return mission start time (nullable)
     */
    public LocalDateTime getMissionStartTime() {
        return parseMissionTime(extras.missionStartTime);
    }

    /**
     * Returns raw mission end time string.
     *
     * @return raw mission end time string
     */
    public String getMissionEndTimeRaw() {
        return extras.missionEndTime;
    }

    /**
     * Returns mission end time.
     *
     * @return mission end time (nullable)
     */
    public LocalDateTime getMissionEndTime() {
        return parseMissionTime(extras.missionEndTime);
    }

    private LocalDateTime parseMissionTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(timeStr, MISSION_TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns mission text.
     *
     * @return mission text
     */
    public String getMissionText() {
        return extras.missionText;
    }

    /**
     * Returns total payment amount.
     *
     * @return total payment amount
     */
    public int getTotalPayAmount() {
        return extras.totalPayAmount;
    }

    /**
     * Returns participant count.
     *
     * @return participant count
     */
    public int getParticipationCount() {
        return extras.participationCount;
    }

    /**
     * Returns raw mission status string.
     *
     * @return raw mission status string
     */
    public String getMissionStatusRaw() {
        return extras.status;
    }

    /**
     * Returns mission status.
     *
     * @return mission status
     */
    public MissionStatus getMissionStatus() {
        return MissionStatus.fromString(extras.status);
    }

    /**
     * Returns whether mission succeeded.
     *
     * @return mission success status
     */
    public boolean isMissionSucceed() {
        return extras.success;
    }

    /**
     * Nickname (null if anonymous).
     * EVENT form: from inherited profile.
     * CHAT/DONATION form: from extras or profile.
     *
     * @return nickname (null if anonymous)
     */
    public String getNickname() {
        if (profile != null && profile.getNickname() != null) return profile.getNickname();
        return extras != null ? extras.nickname : null;
    }

    /**
     * User ID hash (null if anonymous).
     * EVENT form: uses inherited userIdHash field.
     * CHAT/DONATION form: from extras or userIdHash field.
     *
     * @return user ID hash (null if anonymous)
     */
    @Override
    public String getUserIdHash() {
        if (super.getUserIdHash() != null) return super.getUserIdHash();
        return extras != null ? extras.userIdHash : null;
    }

    /**
     * Verified mark status.
     * EVENT form: from inherited profile.
     * CHAT/DONATION form: from extras or profile.
     *
     * @return verified mark status
     */
    public boolean isVerifiedMark() {
        if (profile != null) return profile.isVerifiedMark();
        return extras != null && extras.verifiedMark;
    }

    /**
     * Anonymous status.
     * EVENT form: no info in extras (always false).
     * CHAT/DONATION form: from extras.
     *
     * @return anonymous status
     */
    public boolean isAnonymous() {
        return extras != null && extras.isAnonymous;
    }

    /**
     * Anonymous token (present only when anonymous).
     * EVENT form: no info in extras.
     * CHAT/DONATION form: from extras.
     *
     * @return anonymous token (only when anonymous)
     */
    public String getAnonymousToken() {
        return extras != null ? extras.anonymousToken : null;
    }

    /**
     * Donation ID.
     * EVENT form: no info in extras.
     * CHAT/DONATION form: from extras.
     *
     * @return donation ID
     */
    public String getDonationId() {
        return extras != null ? extras.donationId : null;
    }

    /**
     * Payment type (e.g., "CURRENCY").
     * EVENT form: no info in extras.
     * CHAT/DONATION form: from extras.
     *
     * @return payment type
     */
    public String getPayType() {
        return extras != null ? extras.payType : null;
    }

    /**
     * Continuous donation days.
     * EVENT form: no info in extras.
     * CHAT/DONATION form: from extras.
     *
     * @return continuous donation days
     */
    public int getContinuousDonationDays() {
        return extras != null ? extras.continuousDonationDays : 0;
    }
}
