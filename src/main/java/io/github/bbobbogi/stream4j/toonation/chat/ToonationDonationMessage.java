package io.github.bbobbogi.stream4j.toonation.chat;

import com.google.gson.annotations.SerializedName;

/**
 * Donation alert payload from Toonation WebSocket events.
 *
 * @since 1.0.0
 */
public class ToonationDonationMessage {

    @SerializedName("code")
    private int code;

    @SerializedName("content")
    private Content content;

    private transient String rawJson;

    /**
     * Creates an empty donation message model for JSON deserialization.
     */
    public ToonationDonationMessage() {}

    /**
     * Returns Toonation event code.
     *
     * @return event code value
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns nested content payload.
     *
     * @return content model, or {@code null}
     */
    public Content getContent() {
        return content;
    }

    /**
     * Returns donor account identifier.
     *
     * @return account ID, or {@code null}
     */
    public String getAccount() {
        return content != null ? content.account : null;
    }

    /**
     * Returns donor nickname.
     *
     * @return nickname, or {@code null}
     */
    public String getNickname() {
        return content != null ? content.name : null;
    }

    /**
     * Returns donated amount.
     *
     * @return donation amount
     */
    public int getAmount() {
        return content != null ? content.amount : 0;
    }

    /**
     * Returns donation message text.
     *
     * @return message text, or {@code null}
     */
    public String getMessage() {
        return content != null ? content.message : null;
    }

    /**
     * Returns whether donor identity is hidden.
     *
     * @return {@code true} if donation is anonymous
     */
    public boolean isAnonymous() {
        return content != null && content.hideinfo != 0;
    }

    /**
     * Returns whether this donation includes video metadata.
     *
     * @return {@code true} for video donation events
     */
    public boolean isVideoDonation() {
        return content != null && content.videoInfo != null;
    }

    /**
     * Returns raw JSON text originally received from WebSocket.
     *
     * @return raw JSON payload
     */
    public String getRawJson() {
        return rawJson;
    }

    /**
     * Sets raw JSON payload text.
     *
     * @param rawJson raw WebSocket message JSON
     */
    public void setRawJson(String rawJson) { this.rawJson = rawJson; }

    @Override
    public String toString() {
        return "ToonationDonationMessage{" +
                "code=" + code +
                ", nickname='" + getNickname() + '\'' +
                ", amount=" + getAmount() +
                ", message='" + (isVideoDonation() ? "[영상 후원]" : getMessage()) + '\'' +
                '}';
    }

    /**
     * Nested Toonation donation content payload.
     */
    public static class Content {
        @SerializedName("message")
        private String message;

        @SerializedName("amount")
        private int amount;

        @SerializedName("account")
        private String account;

        @SerializedName("name")
        private String name;

        @SerializedName("video_info")
        private VideoInfo videoInfo;

        @SerializedName("hideinfo")
        private int hideinfo;

        /**
         * Returns donation message text.
         *
         * @return message text
         */
        public String getMessage() { return message; }

        /**
         * Returns donated amount.
         *
         * @return donation amount
         */
        public int getAmount() { return amount; }

        /**
         * Returns donor account identifier.
         *
         * @return donor account ID
         */
        public String getAccount() { return account; }

        /**
         * Returns donor display name.
         *
         * @return donor nickname
         */
        public String getName() { return name; }

        /**
         * Returns video donation metadata.
         *
         * @return video info, or {@code null}
         */
        public VideoInfo getVideoInfo() { return videoInfo; }

        /**
         * Returns raw anonymity flag from payload.
         *
         * @return hide-info flag value
         */
        public int getHideinfo() { return hideinfo; }
    }

    /**
     * Video metadata attached to a Toonation donation.
     */
    public static class VideoInfo {
        @SerializedName("type")
        private String type;

        @SerializedName("title")
        private String title;

        /**
         * Returns video provider type.
         *
         * @return video type
         */
        public String getType() { return type; }

        /**
         * Returns video title.
         *
         * @return video title
         */
        public String getTitle() { return title; }
    }
}
