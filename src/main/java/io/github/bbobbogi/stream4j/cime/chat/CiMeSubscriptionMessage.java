package io.github.bbobbogi.stream4j.cime.chat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CiMeSubscriptionMessage {

    @Nullable private String nickname;
    @Nullable private String message;
    @Nullable private String streamerName;
    private int duration;
    private int tier;
    @Nullable private String rawJson;

    public @Nullable String getNickname() { return nickname; }
    public void setNickname(@Nullable String nickname) { this.nickname = nickname; }

    public @Nullable String getMessage() { return message; }
    public void setMessage(@Nullable String message) { this.message = message; }

    public @Nullable String getStreamerName() { return streamerName; }
    public void setStreamerName(@Nullable String streamerName) { this.streamerName = streamerName; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getTier() { return tier; }
    public void setTier(int tier) { this.tier = tier; }

    public @Nullable String getRawJson() { return rawJson; }
    public void setRawJson(@Nullable String rawJson) { this.rawJson = rawJson; }
}
