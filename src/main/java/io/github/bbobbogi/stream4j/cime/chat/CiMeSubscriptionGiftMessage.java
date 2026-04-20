package io.github.bbobbogi.stream4j.cime.chat;

import org.jetbrains.annotations.Nullable;

public class CiMeSubscriptionGiftMessage {

    @Nullable private String nickname;
    @Nullable private String message;
    private int count;
    @Nullable private String targetType;
    private boolean anonymous;
    @Nullable private String rawJson;

    public @Nullable String getNickname() { return nickname; }
    public void setNickname(@Nullable String nickname) { this.nickname = nickname; }

    public @Nullable String getMessage() { return message; }
    public void setMessage(@Nullable String message) { this.message = message; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public @Nullable String getTargetType() { return targetType; }
    public void setTargetType(@Nullable String targetType) { this.targetType = targetType; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public @Nullable String getRawJson() { return rawJson; }
    public void setRawJson(@Nullable String rawJson) { this.rawJson = rawJson; }
}
