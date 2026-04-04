package io.github.bbobbogi.stream4j.toonation.chat;

import com.google.gson.annotations.SerializedName;

public class ToonationDonationMessage {

    @SerializedName("code")
    private int code;

    @SerializedName("content")
    private Content content;

    private transient String rawJson;

    public ToonationDonationMessage() {}

    public int getCode() {
        return code;
    }

    public Content getContent() {
        return content;
    }

    public String getAccount() {
        return content != null ? content.account : null;
    }

    public String getNickname() {
        return content != null ? content.name : null;
    }

    public int getAmount() {
        return content != null ? content.amount : 0;
    }

    public String getMessage() {
        return content != null ? content.message : null;
    }

    public boolean isAnonymous() {
        return content != null && content.hideinfo != 0;
    }

    public boolean isVideoDonation() {
        return content != null && content.videoInfo != null;
    }

    public String getRawJson() {
        return rawJson;
    }

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

        public String getMessage() { return message; }
        public int getAmount() { return amount; }
        public String getAccount() { return account; }
        public String getName() { return name; }
        public VideoInfo getVideoInfo() { return videoInfo; }
        public int getHideinfo() { return hideinfo; }
    }

    public static class VideoInfo {
        @SerializedName("type")
        private String type;

        @SerializedName("title")
        private String title;

        public String getType() { return type; }
        public String getTitle() { return title; }
    }
}
