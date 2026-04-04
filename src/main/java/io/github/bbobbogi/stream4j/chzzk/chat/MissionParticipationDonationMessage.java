package io.github.bbobbogi.stream4j.chzzk.chat;

/**
 * Mission participation donation message.
 *
 * Contains information about a user participating in a mission donation.
 *
 * Sent in two ways:
 * 1. Chat message form via CHAT/DONATION commands (cmd: 93101/93102)
 *    - donationType: "MISSION_PARTICIPATION"
 * 2. Event form via EVENT command (cmd: 93006)
 *    - type: "DONATION_MISSION_PARTICIPATION" (mission participation event)
 *
 * Event sequence (mission lifecycle):
 * 1. MissionDonationMessage (mission created) - status: PENDING
 *    - sent via EVENT + also via CHAT/DONATION
 * 2. MissionDonationMessage (mission approved/rejected) - status: APPROVED or REJECTED
 *    - sent via EVENT
 * 3. MissionParticipationDonationMessage (participant donation) - repeated per participant (this class, APPROVED only)
 *    - sent via EVENT + also via CHAT/DONATION
 * 4. MissionDonationMessage (mission completed/expired) - status: COMPLETED/EXPIRED
 *    - sent via EVENT
 *
 * relatedMissionDonationId indicates which mission this participation belongs to.
 * missionDonationType: PARTICIPATION.
 *
 * @since 1.0.0
 */
public class MissionParticipationDonationMessage extends DonationMessage {

    /**
     * Creates a {@link MissionParticipationDonationMessage}.
     */
    public MissionParticipationDonationMessage() {
    }

    /**
     * Returns related mission donation ID.
     *
     * @return related mission donation ID
     */
    public String getRelatedMissionDonationId() {
        return extras.relatedMissionDonationId;
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
     * User ID hash.
     * EVENT form: uses inherited userIdHash field.
     * CHAT/DONATION form: from extras or userIdHash field.
     *
     * @return user ID hash
     */
    @Override
    public String getUserIdHash() {
        if (super.getUserIdHash() != null) return super.getUserIdHash();
        return extras != null ? extras.userIdHash : null;
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
