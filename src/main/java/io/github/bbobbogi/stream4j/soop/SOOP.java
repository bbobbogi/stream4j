package io.github.bbobbogi.stream4j.soop;

import io.github.bbobbogi.stream4j.soop.types.SOOPChannelInfo;
import io.github.bbobbogi.stream4j.soop.types.SOOPLiveInfo;
import io.github.bbobbogi.stream4j.util.SharedHttpClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

public class SOOP {
    private static final String LIVE_DETAIL_API = "https://live.sooplive.co.kr/afreeca/player_live_api.php?bjid=";

    public boolean isDebug;

    private final OkHttpClient httpClient;

    SOOP(SOOPBuilder builder) {
        this.httpClient = builder.httpClient != null ? builder.httpClient : SharedHttpClient.get();
        this.isDebug = builder.isDebugEnabled();
    }

    public SOOPLiveInfo getLiveInfo(String streamerId) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("bid", streamerId)
                .add("type", "live")
                .add("pwd", "")
                .add("player_type", "html5")
                .add("stream_type", "common")
                .add("quality", "HD")
                .add("mode", "landing")
                .add("from_api", "0")
                .add("is_revive", "false")
                .build();

        Request request = new Request.Builder()
                .url(LIVE_DETAIL_API + streamerId)
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("[SOOP] Failed to fetch live info: HTTP " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("[SOOP] Empty response body for live detail");
            }

            String bodyString = body.string();
            JsonObject root = JsonParser.parseString(bodyString).getAsJsonObject();
            JsonObject channel = root.has("CHANNEL") && root.get("CHANNEL").isJsonObject()
                    ? root.getAsJsonObject("CHANNEL")
                    : null;

            if (channel == null) {
                throw new IOException("[SOOP] Missing CHANNEL in live detail response");
            }

            String chatDomain = getAsString(channel, "CHDOMAIN");
            String chatNo = getAsString(channel, "CHATNO");
            int result = getAsInt(channel, "RESULT");
            String nickname = getAsString(channel, "BJNICK");
            String title = getAsString(channel, "TITLE");
            String broadcastNo = getAsString(channel, "BNO");
            int minTier = getAsInt(channel, "P_MIN_TIER");

            if (isDebug) {
                System.out.println("[SOOP] Live detail loaded - bno=" + broadcastNo + ", nick=" + nickname + ", title=" + title);
            }

            boolean online = chatDomain != null && chatNo != null && !chatNo.isEmpty();
            return new SOOPLiveInfo(online, result, streamerId, nickname, title, broadcastNo, minTier);
        }
    }

    public SOOPChannelInfo getChannel(String streamerId) throws IOException {
        SOOPLiveInfo liveInfo = getLiveInfo(streamerId);
        return new SOOPChannelInfo(streamerId, liveInfo.getNickname(), liveInfo.isLive());
    }

    public boolean isLive(String streamerId) throws IOException {
        return getLiveInfo(streamerId).isOnline();
    }

    public SOOPChatBuilder chat(String streamerId) {
        return new SOOPChatBuilder(streamerId);
    }

    private static String getAsString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    private static int getAsInt(JsonObject obj, String key) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            return 0;
        }
        try {
            return obj.get(key).getAsInt();
        } catch (Exception ignored) {
            return 0;
        }
    }
}
