package io.github.bbobbogi.stream4j.youtube.types;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Live broadcast timing details returned by YouTube APIs.
 *
 * @since 1.0.0
 */
public class LiveBroadcastDetails {
    @SerializedName("isLiveNow")
    @Expose
    public Boolean isLiveNow;
    @SerializedName("startTimestamp")
    @Expose
    public String startTimestamp;
    @SerializedName("endTimestamp")
    @Expose
    public String endTimestamp;

    /**
     * Returns whether the broadcast is currently live.
     *
     * @return live-now flag
     */
    public Boolean getLiveNow() {
        return isLiveNow;
    }

    /**
     * Returns the broadcast start timestamp.
     *
     * @return start timestamp, or {@code null}
     */
    public String getStartTimestamp() {
        return startTimestamp;
    }

    /**
     * Returns the broadcast end timestamp.
     *
     * @return end timestamp, or {@code null}
     */
    public String getEndTimestamp() {
        return endTimestamp;
    }

    @Override
    public String toString() {
        return "LiveBroadcastDetails{" +
                "isLiveNow=" + isLiveNow +
                ", startTimestamp='" + startTimestamp + '\'' +
                ", endTimestamp='" + endTimestamp + '\'' +
                '}';
    }
}
