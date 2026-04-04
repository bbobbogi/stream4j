package io.github.bbobbogi.stream4j.chzzk.chat;

/**
 * Party donation confirm/settlement event.
 *
 * Sent when party donation ends and settlement information is finalized.
 * Sent through EVENT command (cmd: 93006), and type is "PARTY_DONATION_CONFIRM".
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
public class PartyDonationConfirmEvent {
    int rank;
    String channelName;
    String rankName;
    String type;

    public String rawJson;

    /**
     * Creates a {@link PartyDonationConfirmEvent}.
     */
    public PartyDonationConfirmEvent() {
    }

    /**
     * Returns the party donation rank.
     *
     * @return party donation rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * Returns the channel name.
     *
     * @return channel name
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * Returns the rank name (e.g., "entry fee").
     *
     * @return rank name
     */
    public String getRankName() {
        return rankName;
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
     * Returns the raw JSON string.
     *
     * @return raw JSON string
     */
    public String getRawJson() {
        return rawJson;
    }
}
