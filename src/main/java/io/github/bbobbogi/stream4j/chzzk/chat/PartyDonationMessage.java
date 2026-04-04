package io.github.bbobbogi.stream4j.chzzk.chat;

/**
 * Party donation message.
 *
 * Contains information about a user participating in party donation.
 * Sent through CHAT/DONATION command (cmd: 93101/93102), and donationType is "PARTY".
 *
 * You can identify which party was donated to via partyName and partyNo.
 *
 * Party donation lifecycle:
 * 1. CHANGE_DONATION_ACTIVE (donationType: "PARTY", donationActive: true) - party donation starts
 * 2. PARTY_DONATION_INFO (party status info) - can be sent multiple times during party donation
 * 3. PartyDonationMessage (party donation) - repeated per participant
 * 4. CHANGE_DONATION_ACTIVE (donationType: "PARTY", donationActive: false) - party donation deactivated
 * 5. PARTY_DONATION_FINISH (confirmNeeded: true) - party donation ends
 * 6. PARTY_DONATION_CONFIRM - per-channel ranking and settlement info (repeated by channel count)
 *
 * Note: if chat is connected after party donation starts, event #1 may be missed and events can begin from #2.
 *
 * @since 1.0.0
 */
public class PartyDonationMessage extends DonationMessage {
    /**
     * Creates a {@link PartyDonationMessage}.
     */
    public PartyDonationMessage() {
    }

    /**
     * Returns the party donation ID.
     *
     * @return party donation ID
     */
    public String getPartyDonationId() {
        return extras.partyDonationId;
    }

    /**
     * Returns the party name.
     *
     * @return party name
     */
    public String getPartyName() {
        return extras.partyName;
    }

    /**
     * Returns the party number.
     *
     * @return party number
     */
    public int getPartyNo() {
        return extras.partyNo;
    }
}
