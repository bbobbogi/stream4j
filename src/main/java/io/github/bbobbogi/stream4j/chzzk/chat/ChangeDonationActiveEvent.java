package io.github.bbobbogi.stream4j.chzzk.chat;

/**
 * Donation activation state change event.
 *
 * Sent when activation state changes for a specific donation type.
 * Sent through EVENT command (cmd: 93006), and type is "CHANGE_DONATION_ACTIVE".
 *
 * donationType examples: "CHAT", "VIDEO", "MISSION", "PARTY".
 *
 * Party donation lifecycle (when donationType is "PARTY"):
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
public class ChangeDonationActiveEvent {
    boolean donationActive;
    String donationType;
    String type;

    public String rawJson;

    /**
     * Creates a {@link ChangeDonationActiveEvent}.
     */
    public ChangeDonationActiveEvent() {
    }

    /**
     * Returns whether donation is active.
     *
     * @return {@code true} if active, otherwise {@code false}
     */
    public boolean isDonationActive() {
        return donationActive;
    }

    /**
     * Returns the donation type.
     * <ul>
     *   <li>"CHAT": chat donation</li>
     *   <li>"VIDEO": video donation</li>
     *   <li>"MISSION": mission donation</li>
     *   <li>"PARTY": party donation</li>
     * </ul>
     *
     * @return donation type string
     */
    public String getDonationType() {
        return donationType;
    }

    /**
     * Returns the event type.
     *
     * @return event type string
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
