package io.github.bbobbogi.stream4j.soop.chat;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Mission event payload emitted by SOOP chat.
 *
 * @since 1.0.0
 */
public class SOOPMissionEvent {

    private static final Gson GSON = new Gson();

    /**
     * Mission event type.
     */
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

    /**
     * Returns normalized mission event type.
     *
     * @return mapped mission event type, or {@link Type#UNKNOWN}
     */
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

    /**
     * Returns original mission type code from payload.
     *
     * @return raw mission type string
     */
    public String getTypeRaw() { return typeRaw; }

    /**
     * Returns mission title text.
     *
     * @return mission title
     */
    public String getTitle() { return title; }

    /**
     * Returns mission image URL.
     *
     * @return mission image URL
     */
    public String getImage() { return image; }

    /**
     * Returns whether this event is a relay mission.
     *
     * @return {@code true} for relay mission events
     */
    public boolean isRelay() { return relay; }

    /**
     * Returns gifted count contained in this mission event.
     *
     * @return mission gift count
     */
    public int getGiftCount() { return giftCount; }

    /**
     * Returns triggering user ID.
     *
     * @return user ID
     */
    public String getUserId() { return userId; }

    /**
     * Returns triggering user nickname.
     *
     * @return user nickname
     */
    public String getUserNick() { return userNick; }

    /**
     * Returns broadcaster ID for this mission.
     *
     * @return broadcaster user ID
     */
    public String getBjId() { return bjId; }

    /**
     * Returns broadcaster nickname for this mission.
     *
     * @return broadcaster nickname
     */
    public String getBjNick() { return bjNick; }

    /**
     * Returns mission status code from payload.
     *
     * @return mission status value
     */
    public String getMissionStatus() { return missionStatus; }

    /**
     * Returns settled count for settle events.
     *
     * @return settle count
     */
    public int getSettleCount() { return settleCount; }

    /**
     * Returns channel numeric identifier from payload.
     *
     * @return channel number
     */
    public int getChno() { return chno; }

    /**
     * Returns mission unique identifier.
     *
     * @return mission UUID value
     */
    public String getUuid() { return uuid; }

    /**
     * Returns mission key converted to a string value.
     *
     * @return normalized mission key string, or {@code null}
     */
    public String getKey() {
        if (keyRaw == null) return null;
        if (keyRaw instanceof Number) return String.valueOf(((Number) keyRaw).longValue());
        return keyRaw.toString();
    }

    /**
     * Parses a mission event from raw JSON payload.
     *
     * @param json mission JSON text
     * @return parsed mission event model
     */
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
