package com.bbobbogi.stream4j.soop.chat;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class SOOPMissionEvent {

    private static final Gson GSON = new Gson();

    public enum Type {
        GIFT,
        CHALLENGE_GIFT,
        NOTICE,
        CHALLENGE_NOTICE,
        SETTLE,
        CHALLENGE_SETTLE,
        UNKNOWN
    }

    @SerializedName("type")
    private String typeRaw;

    @SerializedName("key")
    private Object keyRaw;

    private String title;
    private String image;

    @SerializedName("is_relay")
    private boolean relay;

    @SerializedName("gift_count")
    private int giftCount;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("user_nick")
    private String userNick;

    @SerializedName("bj_id")
    private String bjId;

    @SerializedName("bj_nick")
    private String bjNick;

    @SerializedName("mission_status")
    private String missionStatus;

    @SerializedName("settle_count")
    private int settleCount;

    private int chno;
    private String uuid;

    public Type getType() {
        if (typeRaw == null) return Type.UNKNOWN;
        return switch (typeRaw) {
            case "GIFT" -> Type.GIFT;
            case "CHALLENGE_GIFT" -> Type.CHALLENGE_GIFT;
            case "NOTICE" -> Type.NOTICE;
            case "CHALLENGE_NOTICE" -> Type.CHALLENGE_NOTICE;
            case "SETTLE" -> Type.SETTLE;
            case "CHALLENGE_SETTLE" -> Type.CHALLENGE_SETTLE;
            default -> Type.UNKNOWN;
        };
    }

    public String getTypeRaw() { return typeRaw; }
    public String getTitle() { return title; }
    public String getImage() { return image; }
    public boolean isRelay() { return relay; }
    public int getGiftCount() { return giftCount; }
    public String getUserId() { return userId; }
    public String getUserNick() { return userNick; }
    public String getBjId() { return bjId; }
    public String getBjNick() { return bjNick; }
    public String getMissionStatus() { return missionStatus; }
    public int getSettleCount() { return settleCount; }
    public int getChno() { return chno; }
    public String getUuid() { return uuid; }

    public String getKey() {
        if (keyRaw == null) return null;
        if (keyRaw instanceof Number) return String.valueOf(((Number) keyRaw).longValue());
        return keyRaw.toString();
    }

    public static SOOPMissionEvent fromJson(String json) {
        return GSON.fromJson(json, SOOPMissionEvent.class);
    }

    @Override
    public String toString() {
        return "SOOPMissionEvent{type=" + typeRaw +
                ", title='" + title + '\'' +
                ", key=" + getKey() +
                ", giftCount=" + giftCount +
                ", userId='" + userId + '\'' +
                ", userNick='" + userNick + '\'' +
                '}';
    }
}
