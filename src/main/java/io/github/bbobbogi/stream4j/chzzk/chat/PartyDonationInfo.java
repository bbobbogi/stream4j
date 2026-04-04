package io.github.bbobbogi.stream4j.chzzk.chat;

/**
 * Party donation info event.
 *
 * Contains current state information of party donation.
 * Sent through EVENT command (cmd: 93006), and type is "PARTY_DONATION_INFO".
 *
 * Includes party member count, total donation amount, and status (OPEN/CLOSED).
 * This event is not sent periodically; it is sent whenever the party donation state changes
 * (e.g., member count change, total donation amount change, status change).
 *
 * Note: small donations under a certain amount may not generate PartyDonationMessage,
 * and only cumulative amount may be reflected in PARTY_DONATION_INFO.totalDonationAmount.
 *
 * Party donation lifecycle:
 * 1. CHANGE_DONATION_ACTIVE (donationType: "PARTY", donationActive: true) - party donation starts
 * 2. PARTY_DONATION_INFO (party status info) - can be sent whenever party state changes
 * 3. PartyDonationMessage (party donation) - repeated per participant (above certain amount)
 * 4. CHANGE_DONATION_ACTIVE (donationType: "PARTY", donationActive: false) - party donation deactivated
 * 5. PARTY_DONATION_FINISH (confirmNeeded: true) - party donation ends
 * 6. PARTY_DONATION_CONFIRM - per-channel ranking and settlement info (repeated by channel count)
 *
 * Note: if chat is connected after party donation starts, event #1 may be missed and events can begin from #2.
 *
 * @since 1.0.0
 */
public class PartyDonationInfo {
    boolean hostChannelVerifiedMark;
    String hostChannelNickname;
    int memberCount;
    String partyName;
    String type;
    String[] profileImageUrlList;
    int totalDonationAmount;
    String status;

    public String rawJson;

    /**
     * Creates a {@link PartyDonationInfo}.
     */
    public PartyDonationInfo() {
    }

    /**
     * Returns the raw JSON string.
     *
     * @return raw JSON string
     */
    public String getRawJson() {
        return rawJson;
    }

    /**
     * Returns whether the host channel has a verified mark.
     *
     * @return host channel verified mark status
     */
    public boolean isHostChannelVerifiedMark() {
        return hostChannelVerifiedMark;
    }

    /**
     * Returns the host channel nickname.
     *
     * @return host channel nickname
     */
    public String getHostChannelNickname() {
        return hostChannelNickname;
    }

    /**
     * Returns the party member count.
     *
     * @return party member count
     */
    public int getMemberCount() {
        return memberCount;
    }

    /**
     * Returns the party name.
     *
     * @return party name
     */
    public String getPartyName() {
        return partyName;
    }

    /**
     * Returns the event type.
     *
     * @return event type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the profile image URL list.
     *
     * @return profile image URL array
     */
    public String[] getProfileImageUrlList() {
        return profileImageUrlList;
    }

    /**
     * Returns the total donation amount.
     *
     * @return total donation amount
     */
    public int getTotalDonationAmount() {
        return totalDonationAmount;
    }

    /**
     * Returns the raw party status string.
     *
     * @return raw party status string
     */
    public String getStatusRaw() {
        return status;
    }

    /**
     * Returns the party status.
     *
     * @return party status
     */
    public PartyStatus getStatus() {
        return PartyStatus.fromString(status);
    }

    /**
     * Returns whether the party is open.
     *
     * @return {@code true} if the party is open
     */
    public boolean isOpen() {
        return PartyStatus.OPEN == getStatus();
    }
}
